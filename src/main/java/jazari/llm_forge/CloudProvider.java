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
    // Özel provider constructor

    public CloudProvider(String providerName, String apiUrl, List<String> models) {
        this.type = null; // Özel tip yok
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

        switch (type) {
            case ANTHROPIC:
                return "ANTHROPIC";
            case OPENAI:
                return "OPENAI";
            case GOOGLE:
                return "GOOGLE";
            default:
                return type.name();
        }
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
        // API anahtarı olmadığında da sunucu sağlayıcıları görünsün ama isAvailable false olsun
        // Böylece drop-down listesinde görünecek ama kullanmak için API anahtarı gerekecek
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

            ObjectMapper mapper = new ObjectMapper();
            String requestBody;

            if (type == ProviderType.ANTHROPIC) {
                requestBody = createAnthropicRequestBody(mapper, modelName, prompt);
            } else if (type == ProviderType.OPENAI) {
                requestBody = createOpenAIRequestBody(mapper, modelName, prompt);
            } else if (type == ProviderType.GOOGLE) {
                requestBody = createGeminiRequestBody(mapper, modelName, prompt);
            } else {
                result.completeExceptionally(new IllegalStateException("Unknown provider type"));
                return result;
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .header("Content-Type", "application/json");

            // Build URL and set appropriate headers based on provider
            String fullUrl = "";
            if (type == ProviderType.ANTHROPIC) {
                fullUrl = apiUrl;
                requestBuilder.header("x-api-key", apiKey)
                        .header("anthropic-version", "2023-06-01");
            } else if (type == ProviderType.OPENAI) {
                fullUrl = apiUrl;
                requestBuilder.header("Authorization", "Bearer " + apiKey);
            } else if (type == ProviderType.GOOGLE) {
                // For Gemini, we need to append the model name and the API key as a query parameter
                fullUrl = apiUrl + "/" + modelName + ":generateContent?key=" + apiKey;
            }

            requestBuilder.uri(URI.create(fullUrl));

            HttpRequest request = requestBuilder
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            final StringBuilder responseContent = new StringBuilder();

            if (type == ProviderType.ANTHROPIC) {
                // Anthropic supports streaming similar to OpenAI
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
                                            if (completeCallback != null) {
                                                completeCallback.run();
                                            }
                                            result.complete(responseContent.toString());
                                            return;
                                        }

                                        JsonNode node = mapper.readTree(jsonStr);

                                        if (node.has("content") && node.get("content").isArray()) {
                                            ArrayNode contentArray = (ArrayNode) node.get("content");
                                            for (JsonNode content : contentArray) {
                                                if (content.has("text")) {
                                                    String text = content.get("text").asText();
                                                    responseContent.append(text);

                                                    if (responseCallback != null) {
                                                        responseCallback.accept(text);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    if (!isResponseCancelled) {
                                        result.completeExceptionally(e);
                                    }
                                }
                            });

                            if (!isResponseCancelled && !result.isDone()) {
                                if (completeCallback != null) {
                                    completeCallback.run();
                                }
                                result.complete(responseContent.toString());
                            }
                        } catch (Exception e) {
                            if (!currentRequestFuture.isCancelled() && !isResponseCancelled && !result.isDone()) {
                                result.completeExceptionally(e);
                            }
                        }
                    } else {
                        if (!isResponseCancelled && !result.isDone()) {
                            try {
                                String errorBody = response.body().toString();
                                result.completeExceptionally(
                                        new RuntimeException("API error: " + response.statusCode() + " - " + errorBody));
                            } catch (Exception e) {
                                result.completeExceptionally(
                                        new RuntimeException("API error: " + response.statusCode()));
                            }
                        }
                    }
                }).exceptionally(e -> {
                    if (!currentRequestFuture.isCancelled() && !isResponseCancelled && !result.isDone()) {
                        result.completeExceptionally(e);
                    }
                    return null;
                });
            } else {
                // For non-streaming APIs or simpler response handling (OpenAI, Gemini)
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200) {
                                try {
                                    JsonNode rootNode = mapper.readTree(response.body());
                                    String content = extractContentFromResponse(rootNode);

                                    if (responseCallback != null) {
                                        responseCallback.accept(content);
                                    }

                                    if (completeCallback != null) {
                                        completeCallback.run();
                                    }

                                    result.complete(content);
                                } catch (Exception e) {
                                    result.completeExceptionally(e);
                                }
                            } else {
                                result.completeExceptionally(
                                        new RuntimeException("API error: " + response.statusCode() + " - " + response.body()));
                            }
                        })
                        .exceptionally(e -> {
                            result.completeExceptionally(e);
                            return null;
                        });
            }

        } catch (Exception e) {
            result.completeExceptionally(e);
        }

        return result;
    }

    private String createAnthropicRequestBody(ObjectMapper mapper, String modelName, String prompt) throws Exception {
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("model", modelName);
        rootNode.put("stream", true);
        rootNode.put("max_tokens", 1000);

        ArrayNode messagesNode = mapper.createArrayNode();
        ObjectNode userMessageNode = mapper.createObjectNode();
        userMessageNode.put("role", "user");
        userMessageNode.put("content", prompt);
        messagesNode.add(userMessageNode);

        rootNode.set("messages", messagesNode);

        return mapper.writeValueAsString(rootNode);
    }

    private String createOpenAIRequestBody(ObjectMapper mapper, String modelName, String prompt) throws Exception {
        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("model", modelName);
        rootNode.put("stream", false);
        rootNode.put("max_tokens", 1000);

        ArrayNode messagesNode = mapper.createArrayNode();
        ObjectNode userMessageNode = mapper.createObjectNode();
        userMessageNode.put("role", "user");
        userMessageNode.put("content", prompt);
        messagesNode.add(userMessageNode);

        rootNode.set("messages", messagesNode);

        return mapper.writeValueAsString(rootNode);
    }

    private String createGeminiRequestBody(ObjectMapper mapper, String modelName, String prompt) throws Exception {
        ObjectNode rootNode = mapper.createObjectNode();

        // Contents array with the user's message
        ArrayNode contentsNode = mapper.createArrayNode();
        ObjectNode contentObj = mapper.createObjectNode();

        // Parts array with the text part
        ArrayNode partsNode = mapper.createArrayNode();
        ObjectNode partObj = mapper.createObjectNode();
        partObj.put("text", prompt);
        partsNode.add(partObj);

        // Add parts to content
        contentObj.set("parts", partsNode);
        contentObj.put("role", "user");
        contentsNode.add(contentObj);

        // Add contents to root
        rootNode.set("contents", contentsNode);

        // Generation config
        ObjectNode generationConfig = mapper.createObjectNode();
        generationConfig.put("maxOutputTokens", 1000);
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topP", 0.95);
        generationConfig.put("topK", 40);

        rootNode.set("generationConfig", generationConfig);

        return mapper.writeValueAsString(rootNode);
    }

    private String extractContentFromResponse(JsonNode responseNode) {
        StringBuilder content = new StringBuilder();

        try {
            if (type == ProviderType.ANTHROPIC) {
                if (responseNode.has("content") && responseNode.get("content").isArray()) {
                    for (JsonNode partNode : responseNode.get("content")) {
                        if (partNode.has("text")) {
                            content.append(partNode.get("text").asText());
                        }
                    }
                }
            } else if (type == ProviderType.OPENAI) {
                if (responseNode.has("choices") && responseNode.get("choices").isArray()
                        && responseNode.get("choices").size() > 0) {
                    JsonNode firstChoice = responseNode.get("choices").get(0);
                    if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                        content.append(firstChoice.get("message").get("content").asText());
                    }
                }
            } else if (type == ProviderType.GOOGLE) {
                if (responseNode.has("candidates") && responseNode.get("candidates").isArray()
                        && responseNode.get("candidates").size() > 0) {
                    JsonNode firstCandidate = responseNode.get("candidates").get(0);
                    if (firstCandidate.has("content")
                            && firstCandidate.get("content").has("parts")
                            && firstCandidate.get("content").get("parts").isArray()
                            && firstCandidate.get("content").get("parts").size() > 0) {

                        JsonNode parts = firstCandidate.get("content").get("parts");
                        for (JsonNode part : parts) {
                            if (part.has("text")) {
                                content.append(part.get("text").asText());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            content.append("Error parsing response: ").append(e.getMessage());
        }

        return content.toString();
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
            currentRequestFuture = null;
        }
    }
}
