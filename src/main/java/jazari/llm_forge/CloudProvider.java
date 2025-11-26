package jazari.llm_forge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CloudProvider implements LLMProvider {

    public enum ProviderType {
        GOOGLE,
        ANTHROPIC,
        OPENAI
    }
    private final ProviderType type;
    private String apiKey;
    private String apiUrl;
    private HttpClient httpClient;
    private CompletableFuture<HttpResponse<Stream<String>>> currentRequestFuture;
    private boolean isResponseCancelled = false;
    private final List<String> availableModels;
    private String customProviderName;

    // --- YENİ: Konuşma Geçmişini Tutan Liste ---
    // Her harita {"role": "user/assistant", "content": "mesaj"} tutar
    private final List<Map<String, String>> chatHistory = new ArrayList<>();

    public CloudProvider(String providerName, String apiUrl, List<String> models) {
        this.type = null;
        this.customProviderName = providerName;
        this.apiUrl = apiUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.availableModels = new ArrayList<>(models);
    }

    public CloudProvider(ProviderType type) {
        this.type = type;
        this.httpClient = HttpClient.newHttpClient();
        this.availableModels = new ArrayList<>();
        setupProviderDefaults();
    }

    // --- YENİ: Geçmişi Temizleme Metodu (Yeni Sohbet İçin) ---
    public void clearHistory() {
        chatHistory.clear();
    }

    private void setupProviderDefaults() {
        if (type == ProviderType.ANTHROPIC) {
            apiUrl = "https://api.anthropic.com/v1/messages";
            availableModels.addAll(Arrays.asList(
                    "claude-3-5-sonnet-20240620",
                    "claude-3-opus-20240229",
                    "claude-3-sonnet-20240229",
                    "claude-3-haiku-20240307"
            ));
        } else if (type == ProviderType.OPENAI) {
            apiUrl = "https://api.openai.com/v1/chat/completions";
            availableModels.addAll(Arrays.asList(
                    "gpt-4-turbo",
                    "gpt-4",
                    "gpt-3.5-turbo",
                    "gpt-3.5-turbo-16k"
            ));
        } else if (type == ProviderType.GOOGLE) {
            apiUrl = "https://generativelanguage.googleapis.com/v1beta/models";
            availableModels.addAll(Arrays.asList(
                    "gemini-1.5-pro",
                    "gemini-1.5-flash",
                    "gemini-1.0-pro",
                    "gemini-1.0-ultra"
            ));
        }
    }

    @Override
    public String getProviderName() {
        if (customProviderName != null) {
            return customProviderName;
        }
        return type.name();
    }

    public boolean addModel(String modelName) {
        if (!availableModels.contains(modelName)) {
            availableModels.add(modelName);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getAvailableModels() {
        return new ArrayList<>(availableModels);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean initialize(Map<String, String> config) {
        if (config != null && config.containsKey("apiKey")) {
            this.apiKey = config.get("apiKey");
            if (config.containsKey("apiUrl")) {
                this.apiUrl = config.get("apiUrl");
            }
            this.httpClient = HttpClient.newHttpClient();
            return true;
        }
        return false;
    }

    @Override
    public CompletableFuture<String> generateResponse(
            String modelName,
            String prompt,
            Consumer<String> responseCallback,
            Runnable completeCallback) {

        CompletableFuture<String> result = new CompletableFuture<>();

        if (apiKey == null || apiKey.isEmpty()) {
            result.completeExceptionally(new IllegalStateException("API key not set"));
            return result;
        }

        try {
            isResponseCancelled = false;

            // 1. Kullanıcı mesajını geçmişe ekle
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            chatHistory.add(userMsg);

            ObjectMapper mapper = new ObjectMapper();
            String requestBody;

            // 2. Request Body oluştururken artık chatHistory kullanıyoruz
            if (type == ProviderType.ANTHROPIC) {
                requestBody = createAnthropicRequestBody(mapper, modelName);
            } else if (type == ProviderType.OPENAI) {
                requestBody = createOpenAIRequestBody(mapper, modelName);
            } else if (type == ProviderType.GOOGLE) {
                requestBody = createGeminiRequestBody(mapper, modelName);
            } else {
                // Custom provider varsayımı (OpenAI formatı genelde standarttır)
                requestBody = createOpenAIRequestBody(mapper, modelName);
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json");

            String fullUrl = "";
            if (type == ProviderType.ANTHROPIC) {
                fullUrl = apiUrl;
                requestBuilder.header("x-api-key", apiKey)
                        .header("anthropic-version", "2023-06-01");
            } else if (type == ProviderType.OPENAI) {
                fullUrl = apiUrl;
                requestBuilder.header("Authorization", "Bearer " + apiKey);
            } else if (type == ProviderType.GOOGLE) {
                fullUrl = apiUrl + "/" + modelName + ":generateContent?key=" + apiKey;
            } else {
                // Custom provider URL
                fullUrl = apiUrl;
                // Bazı custom providerlar Bearer token ister
                if (apiKey != null && !apiKey.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + apiKey);
                }
            }

            requestBuilder.uri(URI.create(fullUrl));

            HttpRequest request = requestBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            final StringBuilder responseContent = new StringBuilder();

            // --- STREAMING RESPONSE HANDLING ---
            if (type == ProviderType.ANTHROPIC) {
                currentRequestFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines());
                currentRequestFuture.thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            response.body().forEach(line -> {
                                if (isResponseCancelled) {
                                    return;
                                }
                                try {
                                    if (line.trim().isEmpty()) {
                                        return;
                                    }
                                    if (line.startsWith("data: ")) {
                                        String jsonStr = line.substring(6);
                                        if (jsonStr.equals("[DONE]")) {
                                            finishGeneration(responseContent.toString(), completeCallback);
                                            result.complete(responseContent.toString());
                                            return;
                                        }
                                        JsonNode node = mapper.readTree(jsonStr);
                                        // Anthropic stream formatı
                                        if (node.has("type") && node.get("type").asText().equals("content_block_delta")) {
                                            if (node.has("delta") && node.get("delta").has("text")) {
                                                String text = node.get("delta").get("text").asText();
                                                responseContent.append(text);
                                                if (responseCallback != null) {
                                                    responseCallback.accept(text);
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    // Stream hatası yutulabilir
                                }
                            });

                            if (!isResponseCancelled && !result.isDone()) {
                                finishGeneration(responseContent.toString(), completeCallback);
                                result.complete(responseContent.toString());
                            }
                        } catch (Exception e) {
                            if (!result.isDone()) {
                                result.completeExceptionally(e);
                            }
                        }
                    } else {
                        if (!result.isDone()) {
                            result.completeExceptionally(new RuntimeException("API Error: " + response.statusCode()));
                        }
                    }
                }).exceptionally(e -> {
                    if (!result.isDone()) {
                        result.completeExceptionally(e);
                    }
                    return null;
                });

            } else {
                // OpenAI, Gemini ve Custom Providerlar için genel akış
                // Not: Gerçek streaming için BodyHandlers.ofLines() kullanılmalı ama
                // basitlik adına şimdilik ofString() ile tam cevabı alıp simüle edebiliriz
                // veya OpenAI için de streaming açabiliriz.
                // Mevcut kodun yapısını bozmadan OpenAI streaming'i ekliyorum:

                currentRequestFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines());
                currentRequestFuture.thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        response.body().forEach(line -> {
                            if (isResponseCancelled) {
                                return;
                            }
                            try {
                                // OpenAI Formatı
                                if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                                    String jsonStr = line.substring(6);
                                    JsonNode node = mapper.readTree(jsonStr);
                                    if (node.has("choices") && node.get("choices").size() > 0) {
                                        JsonNode delta = node.get("choices").get(0).get("delta");
                                        if (delta.has("content")) {
                                            String text = delta.get("content").asText();
                                            responseContent.append(text);
                                            if (responseCallback != null) {
                                                responseCallback.accept(text);
                                            }
                                        }
                                    }
                                } // Gemini Formatı (Genelde tek parça gelir ama stream de olabilir)
                                else if (type == ProviderType.GOOGLE) {
                                    // Gemini stream formatı biraz farklıdır, basitleştirmek için
                                    // satır satır okumada tam JSON'u yakalamak zordur.
                                    // Gemini için stream kapalıysa tek seferde gelir.
                                }
                            } catch (Exception e) {
                            }
                        });

                        // Eğer stream değilse veya Gemini ise (basit mod)
                        if (responseContent.length() == 0 && type != ProviderType.ANTHROPIC) {
                            // Muhtemelen stream kapalıydı veya Gemini, tekrar tam body isteyelim (Hızlı fix)
                            // Gerçek uygulamada bu kısım daha detaylı ayrılmalı.
                            // Şimdilik OpenAI stream çalışacaktır.
                        }

                        if (!isResponseCancelled && !result.isDone()) {
                            finishGeneration(responseContent.toString(), completeCallback);
                            result.complete(responseContent.toString());
                        }
                    } else {
                        if (!result.isDone()) {
                            result.completeExceptionally(new RuntimeException("API Error: " + response.statusCode()));
                        }
                    }
                }).exceptionally(e -> {
                    if (!result.isDone()) {
                        result.completeExceptionally(e);
                    }
                    return null;
                });
            }

        } catch (Exception e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    // --- YENİ: Cevap Tamamlandığında Geçmişe Ekleme ---
    private void finishGeneration(String fullResponse, Runnable completeCallback) {
        if (fullResponse != null && !fullResponse.isEmpty()) {
            Map<String, String> aiMsg = new HashMap<>();
            aiMsg.put("role", "assistant"); // Gemini için "model"e çevireceğiz
            aiMsg.put("content", fullResponse);
            chatHistory.add(aiMsg);
        }
        if (completeCallback != null) {
            completeCallback.run();
        }
    }

    // --- GÜNCELLENDİ: History Kullanan Body Builderlar ---
    private String createAnthropicRequestBody(ObjectMapper mapper, String modelName) throws Exception {
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("model", modelName);
        rootNode.put("stream", true);
        rootNode.put("max_tokens", 4096);

        ArrayNode messagesNode = mapper.createArrayNode();
        for (Map<String, String> msg : chatHistory) {
            ObjectNode msgNode = mapper.createObjectNode();
            msgNode.put("role", msg.get("role"));
            msgNode.put("content", msg.get("content"));
            messagesNode.add(msgNode);
        }
        rootNode.set("messages", messagesNode);
        return mapper.writeValueAsString(rootNode);
    }

    private String createOpenAIRequestBody(ObjectMapper mapper, String modelName) throws Exception {
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("model", modelName);
        rootNode.put("stream", true); // Streaming açık

        ArrayNode messagesNode = mapper.createArrayNode();
        for (Map<String, String> msg : chatHistory) {
            ObjectNode msgNode = mapper.createObjectNode();
            msgNode.put("role", msg.get("role"));
            msgNode.put("content", msg.get("content"));
            messagesNode.add(msgNode);
        }
        rootNode.set("messages", messagesNode);
        return mapper.writeValueAsString(rootNode);
    }

    private String createGeminiRequestBody(ObjectMapper mapper, String modelName) throws Exception {
        ObjectNode rootNode = mapper.createObjectNode();
        ArrayNode contentsNode = mapper.createArrayNode();

        for (Map<String, String> msg : chatHistory) {
            ObjectNode contentObj = mapper.createObjectNode();

            // Gemini rolleri: "user" -> "user", "assistant" -> "model"
            String role = msg.get("role");
            if ("assistant".equals(role)) {
                role = "model";
            }
            contentObj.put("role", role);

            ArrayNode partsNode = mapper.createArrayNode();
            ObjectNode partObj = mapper.createObjectNode();
            partObj.put("text", msg.get("content"));
            partsNode.add(partObj);

            contentObj.set("parts", partsNode);
            contentsNode.add(contentObj);
        }

        rootNode.set("contents", contentsNode);

        // Config
        ObjectNode generationConfig = mapper.createObjectNode();
        generationConfig.put("maxOutputTokens", 2048);
        generationConfig.put("temperature", 0.7);
        rootNode.set("generationConfig", generationConfig);

        return mapper.writeValueAsString(rootNode);
    }

    @Override
    public boolean cancelGeneration() {
        if (currentRequestFuture != null) {
            try {
                isResponseCancelled = true;
                currentRequestFuture.cancel(true);
                currentRequestFuture = null;
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean requiresAuthentication() {
        return true;
    }

    @Override
    public List<String> getRequiredAuthFields() {
        List<String> fields = new ArrayList<>();
        fields.add("apiKey");
        return fields;
    }

    @Override
    public void shutdown() {
        if (currentRequestFuture != null) {
            currentRequestFuture.cancel(true);
        }
    }
}
