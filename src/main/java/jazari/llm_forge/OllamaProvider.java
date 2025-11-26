package jazari.llm_forge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class OllamaProvider implements LLMProvider {

    private static final String DEFAULT_OLLAMA_URL = "http://localhost:11434";
    private String ollamaUrl;
    private HttpClient httpClient;
    private CompletableFuture<HttpResponse<Stream<String>>> currentRequestFuture;
    private boolean isResponseCancelled = false;
    private List<String> availableModels;

    // --- YENİ: Konuşma Geçmişi (Hafıza) ---
    private final List<Map<String, String>> chatHistory = new ArrayList<>();

    public OllamaProvider() {
        this(DEFAULT_OLLAMA_URL);
    }

    public OllamaProvider(String ollamaUrl) {
        this.ollamaUrl = ollamaUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.availableModels = new ArrayList<>();
    }

    // --- YENİ: Yeni sohbet başlatmak için metot ---
    public void clearHistory() {
        chatHistory.clear();
    }

    @Override
    public String getProviderName() {
        return "OLLAMA";
    }

    @Override
    public List<String> getAvailableModels() {
        if (availableModels.isEmpty()) {
            refreshAvailableModels();
        }
        return new ArrayList<>(availableModels);
    }

    private void refreshAvailableModels() {
        availableModels.clear();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/api/tags"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(response.body());
                JsonNode modelsNode = rootNode.get("models");

                if (modelsNode != null && modelsNode.isArray()) {
                    for (JsonNode model : modelsNode) {
                        String modelName = model.get("name").asText();
                        availableModels.add(modelName);
                    }
                }
            } else {
                fetchModelsFromCommandLine();
            }
        } catch (Exception e) {
            fetchModelsFromCommandLine();
        }
    }

    private void fetchModelsFromCommandLine() {
        try {
            Process process = Runtime.getRuntime().exec("ollama list");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                String[] parts = line.trim().split("\\s+");
                if (parts.length > 0) {
                    availableModels.add(parts[0]);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            // Ignore errors
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(2))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() < 400;
        } catch (Exception e) {
            try {
                Process process = Runtime.getRuntime().exec("ollama --version");
                int exitCode = process.waitFor();
                return exitCode == 0;
            } catch (Exception ex) {
                return false;
            }
        }
    }

    @Override
    public boolean initialize(Map<String, String> config) {
        if (config != null && config.containsKey("url")) {
            this.ollamaUrl = config.get("url");
        }
        this.httpClient = HttpClient.newHttpClient();
        try {
            refreshAvailableModels();
            return !availableModels.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public CompletableFuture<String> generateResponse(
            String modelName,
            String prompt,
            Consumer<String> responseCallback,
            Runnable completeCallback) {

        CompletableFuture<String> result = new CompletableFuture<>();

        try {
            isResponseCancelled = false;
            ObjectMapper mapper = new ObjectMapper();

            // 1. Kullanıcı mesajını hafızaya ekle
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            chatHistory.add(userMsg);

            // 2. Request Body Oluştur (/api/chat formatına uygun)
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("model", modelName);
            rootNode.put("stream", true);

            // Tüm geçmişi 'messages' dizisi olarak ekle
            ArrayNode messagesArray = mapper.createArrayNode();
            for (Map<String, String> msg : chatHistory) {
                ObjectNode msgNode = mapper.createObjectNode();
                msgNode.put("role", msg.get("role"));
                msgNode.put("content", msg.get("content"));
                messagesArray.add(msgNode);
            }
            rootNode.set("messages", messagesArray);

            String requestBody = mapper.writeValueAsString(rootNode);

            // 3. İstek URL'sini /api/chat olarak değiştir
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/api/chat")) // generate -> chat
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            final StringBuilder responseContent = new StringBuilder();

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

                                JsonNode node = mapper.readTree(line);

                                // /api/chat cevabında içerik "message" -> "content" altındadır
                                if (node.has("message") && node.get("message").has("content")) {
                                    String responsePart = node.get("message").get("content").asText();
                                    responseContent.append(responsePart);

                                    if (responseCallback != null) {
                                        responseCallback.accept(responsePart);
                                    }
                                }

                                if (node.has("done") && node.get("done").asBoolean()) {
                                    if (!isResponseCancelled) {
                                        // 4. Cevap bittiğinde, asistanın cevabını hafızaya ekle
                                        Map<String, String> aiMsg = new HashMap<>();
                                        aiMsg.put("role", "assistant");
                                        aiMsg.put("content", responseContent.toString());
                                        chatHistory.add(aiMsg);

                                        if (completeCallback != null) {
                                            completeCallback.run();
                                        }
                                        result.complete(responseContent.toString());
                                    }
                                }
                            } catch (Exception e) {
                                if (!isResponseCancelled) {
                                    result.completeExceptionally(e);
                                }
                            }
                        });
                    } catch (Exception e) {
                        if (!currentRequestFuture.isCancelled() && !isResponseCancelled) {
                            result.completeExceptionally(e);
                        }
                    }
                } else {
                    if (!isResponseCancelled) {
                        result.completeExceptionally(
                                new RuntimeException("Ollama API error: " + response.statusCode()));
                    }
                }
            }).exceptionally(e -> {
                if (!currentRequestFuture.isCancelled() && !isResponseCancelled) {
                    result.completeExceptionally(e);
                }
                return null;
            });

        } catch (Exception e) {
            result.completeExceptionally(e);
        }

        return result;
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
        return false;
    }

    @Override
    public List<String> getRequiredAuthFields() {
        return new ArrayList<>();
    }

    @Override
    public void shutdown() {
        if (currentRequestFuture != null) {
            currentRequestFuture.cancel(true);
            currentRequestFuture = null;
        }
    }
}
