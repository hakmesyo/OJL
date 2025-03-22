package jazari.llm_forge;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface LLMProvider {
    
    String getProviderName();
    
    List<String> getAvailableModels();
    
    boolean isAvailable();
    
    boolean initialize(Map<String, String> config);
    
    CompletableFuture<String> generateResponse(
        String modelName,
        String prompt,
        Consumer<String> responseCallback,
        Runnable completeCallback
    );
    
    boolean cancelGeneration();
    
    boolean requiresAuthentication();
    
    List<String> getRequiredAuthFields();
    
    void shutdown();
}