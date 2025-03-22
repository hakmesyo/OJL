package jazari.llm_forge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    public OllamaProvider() {
        this(DEFAULT_OLLAMA_URL);
    }
    
    public OllamaProvider(String ollamaUrl) {
        this.ollamaUrl = ollamaUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.availableModels = new ArrayList<>();
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
            // Try with Ollama API first
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
                // Fallback to command line if API fails
                fetchModelsFromCommandLine();
            }
        } catch (Exception e) {
            // Fallback to command line
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
                // Skip the header line
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                
                // Parse the model name from the line
                String[] parts = line.trim().split("\\s+");
                if (parts.length > 0) {
                    availableModels.add(parts[0]);
                }
            }
            
            process.waitFor();
        } catch (Exception e) {
            // Ignore errors, models will be empty
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
                // Try running a simple command to check if ollama is installed
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
        
        // Re-create HTTP client
        this.httpClient = HttpClient.newHttpClient();
        
        // Try to refresh models to validate connection
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
            // Reset cancellation flag
            isResponseCancelled = false;
            
            ObjectMapper mapper = new ObjectMapper();
            
            // Create request body
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("prompt", prompt);
            requestBodyMap.put("model", modelName);
            requestBodyMap.put("stream", true);
            
            String requestBody = mapper.writeValueAsString(requestBodyMap);
            
            // Create request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            
            // Create a StringBuilder to accumulate the response
            final StringBuilder responseContent = new StringBuilder();
            
            // Send request asynchronously
            currentRequestFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines());
            
            currentRequestFuture.thenAccept(response -> {
                if (response.statusCode() == 200) {
                    // Process response lines
                    try {
                        response.body().forEach(line -> {
                            // Check if cancelled
                            if (isResponseCancelled) {
                                return;
                            }
                            
                            try {
                                // Skip empty lines
                                if (line.trim().isEmpty()) {
                                    return;
                                }
                                
                                JsonNode node = mapper.readTree(line);
                                
                                // Check response content
                                if (node.has("response")) {
                                    String responsePart = node.get("response").asText();
                                    responseContent.append(responsePart);
                                    
                                    // Call callback with current chunk
                                    if (responseCallback != null) {
                                        responseCallback.accept(responsePart);
                                    }
                                }
                                
                                // Check if response is complete
                                if (node.has("done") && node.get("done").asBoolean()) {
                                    // Only complete if not cancelled
                                    if (!isResponseCancelled) {
                                        // Call complete callback
                                        if (completeCallback != null) {
                                            completeCallback.run();
                                        }
                                        
                                        // Complete the future
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
                
                // Restart the Ollama process - this is optional and might be aggressive
                // forceRestartOllamaProcess();
                
                // Reset current request
                currentRequestFuture = null;
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
    
    private void forceRestartOllamaProcess() {
        try {
            // Get the operating system
            String os = System.getProperty("os.name").toLowerCase();
            Process process = null;
            
            if (os.contains("win")) {
                // Windows
                ProcessBuilder pb = new ProcessBuilder();
                
                // Kill
                pb.command("taskkill", "/F", "/IM", "ollama.exe");
                process = pb.start();
                process.waitFor();
                
                // Restart
                pb = new ProcessBuilder();
                pb.command("cmd", "/c", "start", "/B", "ollama", "serve");
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                process = pb.start();
            } else {
                // macOS or Linux
                process = Runtime.getRuntime().exec("pkill -f ollama");
                process.waitFor();
                process = Runtime.getRuntime().exec("ollama serve &");
            }
            
            if (process != null) {
                process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
            }
            
            // Create a fresh HTTP client
            httpClient = HttpClient.newHttpClient();
            
            // Warm up the model
            warmUpOllamaModel();
        } catch (Exception e) {
            // Ignore errors during restart
        }
    }
    
    private void warmUpOllamaModel() {
        try {
            // Simple request to warm up the model
            Map<String, Object> warmupBody = new HashMap<>();
            warmupBody.put("model", availableModels.isEmpty() ? "gemma3" : availableModels.get(0));
            warmupBody.put("prompt", "Hello, this is a warmup message.");
            warmupBody.put("stream", false);
            
            ObjectMapper mapper = new ObjectMapper();
            String warmupRequestBody = mapper.writeValueAsString(warmupBody);
            
            HttpRequest warmupRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ollamaUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(warmupRequestBody, StandardCharsets.UTF_8))
                    .build();
            
            httpClient.sendAsync(warmupRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            // Ignore errors during warmup
        }
    }
    
    @Override
    public boolean requiresAuthentication() {
        return false; // Ollama doesn't require authentication
    }
    
    @Override
    public List<String> getRequiredAuthFields() {
        return new ArrayList<>(); // No auth fields needed
    }
    
    @Override
    public void shutdown() {
        // Cancel any ongoing requests
        if (currentRequestFuture != null) {
            currentRequestFuture.cancel(true);
            currentRequestFuture = null;
        }
    }
}