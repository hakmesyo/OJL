package jazari.llm_forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModelManager {

    private Map<String, LLMProvider> providers;
    private LLMProvider currentProvider;
    private String currentModel;

    public ModelManager() {
        providers = new HashMap<>();
    }

    public boolean addModelToProvider(String providerName, String modelName) {
        LLMProvider provider = providers.get(providerName);
        if (provider != null) {
            // Eğer provider özelleştirilebilir bir provider ise
            if (provider instanceof CloudProvider) {
                CloudProvider cloudProvider = (CloudProvider) provider;
                return cloudProvider.addModel(modelName);
            }
        }
        return false;
    }

    public void registerProvider(LLMProvider provider) {
        if (provider != null && provider.isAvailable()) {
            providers.put(provider.getProviderName(), provider);
        }
    }

    public List<String> getAllProviders() {
        return new ArrayList<>(providers.keySet());
    }

    public List<String> getModelsForProvider(String providerName) {
        LLMProvider provider = providers.get(providerName);
        if (provider != null) {
            return provider.getAvailableModels();
        }
        return new ArrayList<>();
    }

    public boolean setCurrentModel(String providerName, String modelName) {
        LLMProvider provider = providers.get(providerName);
        if (provider != null && provider.getAvailableModels().contains(modelName)) {
            currentProvider = provider;
            currentModel = modelName;
            return true;
        }
        return false;
    }

    public LLMProvider getCurrentProvider() {
        return currentProvider;
    }

    public String getCurrentModel() {
        return currentModel;
    }

    public CompletableFuture<String> generateResponse(
            String prompt,
            Consumer<String> responseCallback,
            Runnable completeCallback) {

        if (currentProvider == null || currentModel == null) {
            throw new IllegalStateException("No model selected");
        }

        return currentProvider.generateResponse(
                currentModel,
                prompt,
                responseCallback,
                completeCallback);
    }

    public boolean cancelGeneration() {
        if (currentProvider != null) {
            return currentProvider.cancelGeneration();
        }
        return false;
    }

    public boolean requiresAuthentication(String providerName) {
        LLMProvider provider = providers.get(providerName);
        return provider != null && provider.requiresAuthentication();
    }

    public List<String> getRequiredAuthFields(String providerName) {
        LLMProvider provider = providers.get(providerName);
        if (provider != null) {
            return provider.getRequiredAuthFields();
        }
        return new ArrayList<>();
    }

    public boolean initializeProvider(String providerName, Map<String, String> config) {
        LLMProvider provider = providers.get(providerName);
        if (provider != null) {
            return provider.initialize(config);
        }
        return false;
    }

    public void shutdown() {
        for (LLMProvider provider : providers.values()) {
            provider.shutdown();
        }
        providers.clear();
        currentProvider = null;
        currentModel = null;
    }
}
