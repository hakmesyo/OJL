package jazari.llm_forge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelSelectorPanel extends JPanel {

    private JComboBox<String> providerComboBox;
    private JComboBox<String> modelComboBox;
    private JButton authButton;
    private JButton activateButton;
    private JLabel statusLabel;
    private ModelManager modelManager;
    private SettingsManager settingsManager;
    private JFrame parentFrame;
    private List<ModelSelectionListener> listeners;
    private EventLogger logger;
    private JButton addProviderButton;
    private JButton addModelButton;

    public ModelSelectorPanel(JFrame parent, ModelManager modelManager, SettingsManager settingsManager) {
        this.parentFrame = parent;
        this.modelManager = modelManager;
        this.settingsManager = settingsManager;
        this.listeners = new ArrayList<>();
        this.logger = new EventLogger();

        initializeUI();
        populateProviders();
    }

    public ModelSelectorPanel(JFrame parent, ModelManager modelManager, SettingsManager settingsManager, EventLogger logger) {
        this.parentFrame = parent;
        this.modelManager = modelManager;
        this.settingsManager = settingsManager;
        this.listeners = new ArrayList<>();
        this.logger = logger;

        initializeUI();
        populateProviders();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 0));
        setBorder(new EmptyBorder(5, 10, 5, 10));

        // Provider seçim paneli
        JPanel providerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel providerLabel = new JLabel("Provider:");
        providerComboBox = new JComboBox<>();
        providerComboBox.setPreferredSize(new Dimension(150, 30));

        // + butonu ekleme (Provider)
        addProviderButton = new JButton("+");
        addProviderButton.setPreferredSize(new Dimension(30, 30));
        addProviderButton.setToolTipText("Add New Provider");
        addProviderButton.setFocusPainted(false);

        providerPanel.add(providerLabel);
        providerPanel.add(providerComboBox);
        providerPanel.add(addProviderButton);

        // Model seçim paneli
        JPanel modelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel modelLabel = new JLabel("Model:");
        modelComboBox = new JComboBox<>();
        modelComboBox.setPreferredSize(new Dimension(200, 30));

        // + butonu ekleme (Model)
        addModelButton = new JButton("+");
        addModelButton.setPreferredSize(new Dimension(30, 30));
        addModelButton.setToolTipText("Add New Model");
        addModelButton.setFocusPainted(false);

        modelPanel.add(modelLabel);
        modelPanel.add(modelComboBox);
        modelPanel.add(addModelButton);

        // Activate button
        activateButton = new JButton("Activate Model");
        activateButton.setPreferredSize(new Dimension(150, 30));
        activateButton.setBackground(new Color(114, 137, 218)); // Mavi
        activateButton.setForeground(Color.WHITE);
        activateButton.setFocusPainted(false);
        activateButton.setBorderPainted(false);
        activateButton.setOpaque(true);

        // Auth button aynı kalıyor
        authButton = new JButton("Set API Key");
        authButton.setPreferredSize(new Dimension(150, 30));
        authButton.setBackground(new Color(108, 117, 125));
        authButton.setForeground(Color.WHITE);
        authButton.setFocusPainted(false);
        authButton.setBorderPainted(false);
        authButton.setOpaque(true);

        // Status label aynı kalıyor
        statusLabel = new JLabel("No model selected");
        statusLabel.setForeground(Color.GRAY);

        // Kontrolleri panele ekleme
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlsPanel.add(providerPanel);
        controlsPanel.add(modelPanel);
        controlsPanel.add(activateButton);
        controlsPanel.add(authButton);

        // Ana bileşenleri panele ekleme
        add(controlsPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.EAST);

        // Listeners
        providerComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                handleProviderSelection();
            }
        });

        modelComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateUIForModelSelection();
            }
        });

        activateButton.addActionListener(e -> handleModelActivation());
        authButton.addActionListener(e -> showApiKeyDialog());

        // YENİ: Add Provider buton listener'ı
        addProviderButton.addActionListener(e -> showAddProviderDialog());

        // YENİ: Add Model buton listener'ı
        addModelButton.addActionListener(e -> showAddModelDialog());
    }

    // Provider ekleme dialogunu gösterme
    private void showAddProviderDialog() {
        log("Opening Add Provider dialog");

        CustomProviderDialog dialog = CustomProviderDialog.showDialog(parentFrame);

        if (dialog.isConfirmed()) {
            String providerName = dialog.getProviderName();
            String apiUrl = dialog.getApiUrl();
            List<String> models = dialog.getModels();

            // Yeni provider ekle
            CloudProvider customProvider = new CloudProvider(providerName, apiUrl, models);
            modelManager.registerProvider(customProvider);

            // UI'ı güncelle
            refreshProviders();

            log("Added custom provider: " + providerName);
            JOptionPane.showMessageDialog(
                    parentFrame,
                    "Custom provider \"" + providerName + "\" added successfully.",
                    "Provider Added",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Model ekleme dialogunu gösterme
    private void showAddModelDialog() {
        String selectedProvider = (String) providerComboBox.getSelectedItem();

        if (selectedProvider == null) {
            JOptionPane.showMessageDialog(
                    parentFrame,
                    "Please select a provider first.",
                    "Provider Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        log("Opening Add Model dialog for provider: " + selectedProvider);

        // Model ekleme dialogu
        String modelName = JOptionPane.showInputDialog(
                parentFrame,
                "Enter model name for " + selectedProvider + ":",
                "Add New Model",
                JOptionPane.PLAIN_MESSAGE);

        if (modelName != null && !modelName.trim().isEmpty()) {
            // Model Manager'a model ekle (ekleme metodu eklenecek)
            boolean added = modelManager.addModelToProvider(selectedProvider, modelName.trim());

            if (added) {
                // Modelleri yenile
                populateModelsForProvider(selectedProvider);

                // Eklenen modeli seç
                for (int i = 0; i < modelComboBox.getItemCount(); i++) {
                    if (modelComboBox.getItemAt(i).equals(modelName.trim())) {
                        modelComboBox.setSelectedIndex(i);
                        break;
                    }
                }

                log("Added model: " + modelName + " to provider: " + selectedProvider);
            } else {
                log("Failed to add model: " + modelName);
                JOptionPane.showMessageDialog(
                        parentFrame,
                        "Failed to add model. The provider may not support custom models.",
                        "Add Model Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Cloud provider modelleri için değişiklik
    private void populateModelsForProvider(String provider) {
        try {
            // Geçici olarak item listener'ı kaldır
            ItemListener[] listeners = modelComboBox.getItemListeners();
            for (ItemListener listener : listeners) {
                modelComboBox.removeItemListener(listener);
            }

            // Model combobox'ı temizle
            modelComboBox.removeAllItems();

            // Provider'ı kontrol et - Eğer OLLAMA ise modelleri getir
            if ("OLLAMA".equals(provider)) {
                // Ollama modelleri için orijinal kodu kullan
                List<String> models = modelManager.getModelsForProvider(provider);
                if (models.isEmpty()) {
                    log("No models available for provider: " + provider);
                    modelComboBox.addItem("No models found");
                    return;
                }

                // Modelleri combobox'a ekle
                for (String model : models) {
                    modelComboBox.addItem(model);
                }
            } else {
                // Cloud provider'lar için boş başla
                List<String> models = modelManager.getModelsForProvider(provider);

                if (models.isEmpty()) {
                    modelComboBox.addItem("Click + to add models");
                } else {
                    // Modelleri combobox'a ekle
                    for (String model : models) {
                        modelComboBox.addItem(model);
                    }
                }
            }

            // Listener'ları geri ekle
            for (ItemListener listener : listeners) {
                modelComboBox.addItemListener(listener);
            }

            // İlk modeli seç
            if (modelComboBox.getItemCount() > 0) {
                modelComboBox.setSelectedIndex(0);
                updateUIForModelSelection();
            }

        } catch (Exception e) {
            log("Error populating models: " + e.getMessage());
        }
    }

    // Yeni eklenen metod
    private void updateUIForModelSelection() {
        String selectedProvider = (String) providerComboBox.getSelectedItem();
        String selectedModel = (String) modelComboBox.getSelectedItem();

        if (selectedProvider == null || selectedModel == null) {
            return;
        }

        log("Model seçildi, aktivasyon bekleniyor: " + selectedProvider + " - " + selectedModel);
    }

    // Yeni eklenen metod
    private void handleModelActivation() {
        String selectedProvider = (String) providerComboBox.getSelectedItem();
        String selectedModel = (String) modelComboBox.getSelectedItem();

        if (selectedProvider == null || selectedModel == null) {
            log("Provider veya model seçili değil");
            JOptionPane.showMessageDialog(
                    parentFrame,
                    "Please select a provider and a model first.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        log("Model aktivasyonu başlatılıyor: " + selectedProvider + " - " + selectedModel);

        // Doğrudan handleModelSelection() metodunu çağıralım (mevcut işlevi kullanıyoruz)
        handleModelSelection();
    }

    private void populateProviders() {
        try {
            // Temporarily remove item listener to prevent events during population
            ItemListener[] listeners = providerComboBox.getItemListeners();
            for (ItemListener listener : listeners) {
                providerComboBox.removeItemListener(listener);
            }

            // Clear the combobox
            providerComboBox.removeAllItems();

            // Get all available providers from model manager
            List<String> providers = modelManager.getAllProviders();
            if (providers.isEmpty()) {
                log("No providers available");
                return;
            }

            // Özel sıralama - OLLAMA'yı en başa al
            List<String> sortedProviders = new ArrayList<>();

            // Önce OLLAMA'yı bul ve en başa al
            for (String provider : providers) {
                if (provider.equals("OLLAMA")) {
                    sortedProviders.add(0, provider);
                }
            }

            // Diğer provider'ları ekle (OLLAMA dışındakileri)
            for (String provider : providers) {
                if (!provider.equals("OLLAMA")) {
                    sortedProviders.add(provider);
                }
            }

            // Add providers to combobox
            for (String provider : sortedProviders) {
                providerComboBox.addItem(provider);
            }

            // Re-add listeners
            for (ItemListener listener : listeners) {
                providerComboBox.addItemListener(listener);
            }

            // Select first provider
            if (providerComboBox.getItemCount() > 0) {
                providerComboBox.setSelectedIndex(0);
                handleProviderSelection();
            }

        } catch (Exception e) {
            log("Error populating providers: " + e.getMessage());
        }
    }

    private void handleProviderSelection() {
        try {
            String selectedProvider = (String) providerComboBox.getSelectedItem();
            if (selectedProvider == null) {
                log("No provider selected");
                return;
            }

            log("Provider selected: " + selectedProvider);

            // Update auth button state based on provider
            updateAuthButtonState();

            // Populate models for this provider
            populateModelsForProvider(selectedProvider);

        } catch (Exception e) {
            log("Error handling provider selection: " + e.getMessage());
        }
    }

    private void handleModelSelection() {
        try {
            String selectedProvider = (String) providerComboBox.getSelectedItem();
            String selectedModel = (String) modelComboBox.getSelectedItem();

            if (selectedProvider == null || selectedModel == null) {
                log("Provider or model is null");
                statusLabel.setText("No model selected");
                statusLabel.setForeground(Color.GRAY);
                return;
            }

            log("Model selected: " + selectedProvider + " - " + selectedModel);

            // Check if the provider requires authentication
            boolean requiresAuth = modelManager.requiresAuthentication(selectedProvider);

            // Check if API key is set if required
            if (requiresAuth) {
                String apiKey = settingsManager.getApiKey(selectedProvider);
                if (apiKey.isEmpty()) {
                    log("API key required but not set");
                    statusLabel.setText("API key required");
                    statusLabel.setForeground(Color.RED);
                    return;
                }
            }

            // Try to set the current model in the model manager
            boolean success = modelManager.setCurrentModel(selectedProvider, selectedModel);

            if (success) {
                log("Model set successfully");
                statusLabel.setText("Model: " + selectedModel);
                statusLabel.setForeground(new Color(0, 120, 0)); // Green

                // Notify listeners
                notifyListeners(selectedProvider, selectedModel);

                // If provider requires auth, make sure it's initialized
                if (requiresAuth) {
                    String apiKey = settingsManager.getApiKey(selectedProvider);
                    if (!apiKey.isEmpty()) {
                        Map<String, String> config = new HashMap<>();
                        config.put("apiKey", apiKey);
                        boolean initSuccess = modelManager.initializeProvider(selectedProvider, config);
                        log("Provider initialization " + (initSuccess ? "successful" : "failed"));
                    }
                }
            } else {
                log("Failed to set model");
                statusLabel.setText("Failed to select model");
                statusLabel.setForeground(Color.RED);
            }

        } catch (Exception e) {
            log("Error handling model selection: " + e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void updateAuthButtonState() {
        try {
            String selectedProvider = (String) providerComboBox.getSelectedItem();
            if (selectedProvider == null) {
                authButton.setEnabled(false);
                authButton.setText("No Provider");
                authButton.setBackground(new Color(108, 117, 125)); // Gray
                return;
            }

            boolean requiresAuth = modelManager.requiresAuthentication(selectedProvider);
            authButton.setEnabled(true);

            if (requiresAuth) {
                String apiKey = settingsManager.getApiKey(selectedProvider);
                if (!apiKey.isEmpty()) {
                    // API key is set
                    String maskedKey = settingsManager.hashApiKey(apiKey);
                    authButton.setText("API Key: " + maskedKey);
                    authButton.setToolTipText("Change API key for " + selectedProvider);
                    authButton.setBackground(new Color(40, 167, 69)); // Green
                } else {
                    // API key required but not set
                    authButton.setText("Set API Key");
                    authButton.setToolTipText("Required: Set API key for " + selectedProvider);
                    authButton.setBackground(new Color(220, 53, 69)); // Red
                }
            } else {
                // No auth needed
                authButton.setText("No Auth Needed");
                authButton.setToolTipText(selectedProvider + " does not require authentication");
                authButton.setBackground(new Color(108, 117, 125)); // Gray
            }

        } catch (Exception e) {
            log("Error updating auth button state: " + e.getMessage());
        }
    }

    private void showApiKeyDialog() {
        try {
            String selectedProvider = (String) providerComboBox.getSelectedItem();
            if (selectedProvider == null) {
                return;
            }

            // Get required fields for this provider
            List<String> requiredFields = modelManager.getRequiredAuthFields(selectedProvider);

            // Show API key dialog
            Map<String, String> apiKeyData = APIKeyDialog.showDialog(parentFrame, selectedProvider, requiredFields);

            // If dialog was cancelled or no data returned
            if (apiKeyData == null || !apiKeyData.containsKey("apiKey")) {
                log("API key dialog cancelled or returned no data");
                return;
            }

            // Save API key
            settingsManager.setApiKey(selectedProvider, apiKeyData.get("apiKey"));
            log("API key saved for " + selectedProvider);

            // Initialize provider with new API key
            Map<String, String> config = new HashMap<>();
            config.putAll(apiKeyData);
            boolean success = modelManager.initializeProvider(selectedProvider, config);

            log("Provider initialization " + (success ? "successful" : "failed"));

            // Update UI
            updateAuthButtonState();
            handleModelSelection();

        } catch (Exception e) {
            log("Error showing API key dialog: " + e.getMessage());
        }
    }

    public void addModelSelectionListener(ModelSelectionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeModelSelectionListener(ModelSelectionListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String provider, String model) {
        for (ModelSelectionListener listener : listeners) {
            try {
                listener.onModelSelected(provider, model);
            } catch (Exception e) {
                log("Error notifying listener: " + e.getMessage());
            }
        }
    }

    public void refreshProviders() {
        populateProviders();
    }

    public void forceModelSelection() {
        handleModelSelection();
    }

    public boolean selectProvider(String providerName) {
        for (int i = 0; i < providerComboBox.getItemCount(); i++) {
            String item = providerComboBox.getItemAt(i);
            if (item.equals(providerName)) {
                providerComboBox.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    public boolean selectModel(String modelName) {
        for (int i = 0; i < modelComboBox.getItemCount(); i++) {
            String item = modelComboBox.getItemAt(i);
            if (item.equals(modelName)) {
                modelComboBox.setSelectedIndex(i);
                return true;
            }
        }
        return false;
    }

    public void promptForApiKey() {
        showApiKeyDialog();
    }

    public JComboBox<String> getProviderComboBox() {
        return providerComboBox;
    }

    public JComboBox<String> getModelComboBox() {
        return modelComboBox;
    }

    private void log(String message) {
        if (logger != null) {
            logger.log("ModelSelector: " + message);
        }
    }

    public interface ModelSelectionListener {

        void onModelSelected(String provider, String model);
    }
}
