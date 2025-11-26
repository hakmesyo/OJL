package jazari.llm_forge;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

public class JazariChatForge extends JFrame implements ActionListener, ModelSelectorPanel.ModelSelectionListener {

    // Initialize FlatDarkLaf theme
    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // FlatDarkLaf theme settings
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.width", 12);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(JazariChatForge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ChatPane chatPane;
    private JTextArea inputTextArea;
    private JScrollPane inputScrollPane;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private EventLogger eventLogger;
    private Timer statusAnimationTimer;
    private int animationDots = 0;
    private JButton cancelButton;
    private ModelSelectorPanel modelSelectorPanel;

    private ModelManager modelManager;
    private SettingsManager settingsManager;

    private String currentProvider;
    private String currentModel;
    private boolean isResponseCancelled = false;
    private boolean isGeneratingResponse = false;
    private CompletableFuture<String> currentResponseFuture;
    private AnimatedDot onlineDot;

    public JazariChatForge() {
        setTitle("Jazari Chat Forge");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(700, 500));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Initialize managers
        settingsManager = new SettingsManager();
        modelManager = new ModelManager();

        // Start event logger
        eventLogger = new EventLogger();

        // Register providers
        registerProviders();

        // Add menu
        setupMenu();

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Chat Area
        chatPane = new ChatPane(eventLogger);
        scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);

        // Create Input Panel
        JPanel inputPanel = createInputPanel();
        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        // First create Status Panel
        statusPanel = createStatusPanel();

        // Then set up Cancel Button (statusPanel is no longer null)
        setupCancelButton();

        // Add status panel
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        // Set initial focus to input field
        SwingUtilities.invokeLater(() -> inputTextArea.requestFocusInWindow());

        // Initialize animation
        initStatusAnimation();

        // Center the frame on screen
        setLocationRelativeTo(null);
        setVisible(true);

        // Show welcome message
        showWelcomeMessage();
    }

    private void registerProviders() {
        // Register Ollama provider
        OllamaProvider ollamaProvider = new OllamaProvider();
        if (ollamaProvider.isAvailable()) {
            modelManager.registerProvider(ollamaProvider);
            eventLogger.log("Registered Ollama provider with "
                    + ollamaProvider.getAvailableModels().size() + " models");
        } else {
            eventLogger.log("Ollama provider not available");
        }

        // Always register cloud providers
        // Register Anthropic provider
        CloudProvider claudeProvider = new CloudProvider(CloudProvider.ProviderType.ANTHROPIC);
        String claudeKey = settingsManager.getApiKey("ANTHROPIC");
        Map<String, String> claudeConfig = new HashMap<>();
        if (!claudeKey.isEmpty()) {
            claudeConfig.put("apiKey", claudeKey);
            claudeProvider.initialize(claudeConfig);
            eventLogger.log("Initialized Claude provider with saved key");
        }
        modelManager.registerProvider(claudeProvider);
        eventLogger.log("Registered Claude provider");

        // Register OpenAI provider
        CloudProvider openaiProvider = new CloudProvider(CloudProvider.ProviderType.OPENAI);
        String openaiKey = settingsManager.getApiKey("OPENAI");
        Map<String, String> openaiConfig = new HashMap<>();
        if (!openaiKey.isEmpty()) {
            openaiConfig.put("apiKey", openaiKey);
            openaiProvider.initialize(openaiConfig);
            eventLogger.log("Initialized OpenAI provider with saved key");
        }
        modelManager.registerProvider(openaiProvider);
        eventLogger.log("Registered OpenAI provider");

        // Register Gemini provider
        CloudProvider geminiProvider = new CloudProvider(CloudProvider.ProviderType.GOOGLE);
        String geminiKey = settingsManager.getApiKey("GEMINI");
        Map<String, String> geminiConfig = new HashMap<>();
        if (!geminiKey.isEmpty()) {
            geminiConfig.put("apiKey", geminiKey);
            geminiProvider.initialize(geminiConfig);
            eventLogger.log("Initialized Gemini provider with saved key");
        }
        modelManager.registerProvider(geminiProvider);
        eventLogger.log("Registered Gemini provider");
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Developer menu
        JMenu devMenu = new JMenu("Developer");
        JMenuItem logMenuItem = new JMenuItem("Event Logger");
        logMenuItem.addActionListener(e -> eventLogger.toggleVisibility());
        devMenu.add(logMenuItem);

        // Settings menu
        JMenu settingsMenu = new JMenu("Settings");

        // Add custom provider menu item
        JMenuItem addProviderMenuItem = new JMenuItem("Add Custom Provider");
        addProviderMenuItem.addActionListener(e -> showAddCustomProviderDialog());
        settingsMenu.add(addProviderMenuItem);

        JMenuItem clearKeysMenuItem = new JMenuItem("Clear API Keys");
        clearKeysMenuItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to clear all saved API keys?",
                    "Clear API Keys",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                settingsManager.clearApiKeys();
                modelSelectorPanel.refreshProviders();
                JOptionPane.showMessageDialog(
                        this,
                        "All API keys have been cleared.",
                        "Keys Cleared",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        settingsMenu.add(clearKeysMenuItem);

        menuBar.add(devMenu);
        menuBar.add(settingsMenu);
        setJMenuBar(menuBar);
    }

    private void showAddCustomProviderDialog() {
        CustomProviderDialog dialog = CustomProviderDialog.showDialog(this);

        if (dialog.isConfirmed()) {
            String providerName = dialog.getProviderName();
            String apiUrl = dialog.getApiUrl();
            List<String> models = dialog.getModels();

            // Yeni provider ekle
            CloudProvider customProvider = new CloudProvider(providerName, apiUrl, models);
            modelManager.registerProvider(customProvider);

            // UI'ı güncelle
            modelSelectorPanel.refreshProviders();

            JOptionPane.showMessageDialog(
                    this,
                    "Custom provider \"" + providerName + "\" added successfully.",
                    "Provider Added",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(36, 36, 40));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Logo and title
        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleContainer.setOpaque(false);

        // Simple logo
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(114, 137, 218));
                g2d.fillOval(0, 0, 24, 24);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(7, 7, 10, 10);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(24, 24);
            }
        };

        JLabel titleLabel = new JLabel("Jazari Chat Forge", JLabel.LEFT);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        titleLabel.setForeground(new Color(114, 137, 218));

        titleContainer.add(logoPanel);
        titleContainer.add(Box.createHorizontalStrut(10));
        titleContainer.add(titleLabel);

        // Add Model Selector Panel
        modelSelectorPanel = new ModelSelectorPanel(this, modelManager, settingsManager);
        modelSelectorPanel.addModelSelectionListener(this);

        headerPanel.add(titleContainer, BorderLayout.WEST);
        headerPanel.add(modelSelectorPanel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        // Text Area
        inputTextArea = createInputTextArea();

        // Scroll Pane
        inputScrollPane = new JScrollPane(inputTextArea);
        inputScrollPane.setBorder(BorderFactory.createEmptyBorder());
        inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        inputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // Limit the maximum height of the scroll pane
        int maxHeight = 120; // maximum 120 pixels (approximately 6-7 lines)
        inputScrollPane.setPreferredSize(new Dimension(inputScrollPane.getPreferredSize().width,
                Math.min(inputTextArea.getPreferredSize().height, maxHeight)));

        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        // Send Button
        sendButton = createSendButton();
        inputPanel.add(sendButton, BorderLayout.EAST);

        return inputPanel;
    }

    private JTextArea createInputTextArea() {
        JTextArea textArea = new JTextArea(3, 20); // 3 rows, 20 columns initial size
        textArea.setFont(new Font("Dialog", Font.PLAIN, 14));
        textArea.setLineWrap(true); // Line wrapping active
        textArea.setWrapStyleWord(true); // Word-based wrapping
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 64), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        textArea.setBackground(new Color(49, 51, 56));
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        textArea.setText("Type your message here...");

        // Focus listener for placeholder text
        textArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (textArea.getText().equals("Type your message here...")) {
                    textArea.setText("");
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (textArea.getText().trim().isEmpty()) {
                    textArea.setText("Type your message here...");
                }
            }
        });

        // Enter and Shift+Enter key listeners
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // When Enter key is pressed
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // If Shift key is not pressed, send message and prevent Enter
                    if (!e.isShiftDown()) {
                        e.consume(); // Prevent Enter character from being added
                        sendMessage();
                    }
                    // In case of Shift+Enter, do nothing, default behavior will work
                }
            }
        });

        // More reliable approach using InputMap and ActionMap
        InputMap inputMap = textArea.getInputMap();
        ActionMap actionMap = textArea.getActionMap();

        // Replace Enter key with a custom action
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        inputMap.put(enterKey, "sendMessage");
        actionMap.put("sendMessage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // For Shift+Enter, use DEFAULT_ACTION to maintain default behavior
        KeyStroke shiftEnterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
        inputMap.put(shiftEnterKey, "insert-break");  // "insert-break" is the default new line action

        return textArea;
    }

    private JButton createSendButton() {
        JButton button = new JButton();
        button.setIcon(createSendIcon());
        button.setBackground(new Color(114, 137, 218));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(this);
        return button;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        panel.setOpaque(false);

        JPanel onlineStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        onlineStatusPanel.setOpaque(false);

        // Online status indicator (will be replaced by animated dot)
        JPanel staticDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(67, 181, 129)); // Green
                g2d.fillOval(0, 0, 8, 8);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(8, 8);
            }
        };

        statusLabel = new JLabel("No model selected");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(180, 180, 180));

        onlineStatusPanel.add(staticDot);
        onlineStatusPanel.add(statusLabel);

        panel.add(onlineStatusPanel, BorderLayout.WEST);

        return panel;
    }

    private class AnimatedDot extends JPanel {

        private float scale = 1.0f;
        private boolean growing = true;
        private final float MIN_SCALE = 0.7f;
        private final float MAX_SCALE = 1.7f;
        private final float SCALE_STEP = 0.25f;
        private final Color dotColor;

        public AnimatedDot(Color color) {
            this.dotColor = color;
            setOpaque(false);
        }

        public void updateAnimation() {
            // Update scale
            if (growing) {
                scale += SCALE_STEP;
                if (scale >= MAX_SCALE) {
                    growing = false;
                }
            } else {
                scale -= SCALE_STEP;
                if (scale <= MIN_SCALE) {
                    growing = true;
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = 8;
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;

            // Calculate scaled size
            int scaledSize = (int) (size * scale);

            // Draw the dot centered
            g2d.setColor(dotColor);
            g2d.fillOval(centerX - scaledSize / 2, centerY - scaledSize / 2, scaledSize, scaledSize);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(12, 12); // Slightly larger to accommodate animation
        }
    }

    private void setupCancelButton() {
        // Create cancel button
        cancelButton = new JButton();
        cancelButton.setPreferredSize(new Dimension(16, 16));
        cancelButton.setBackground(new Color(220, 53, 69)); // Red
        cancelButton.setBorder(BorderFactory.createEmptyBorder());
        cancelButton.setFocusPainted(false);
        cancelButton.setVisible(false); // Initially hidden

        // Add tooltip
        cancelButton.setToolTipText("Cancel response");

        // Function when cancel button is clicked
        cancelButton.addActionListener(e -> {
            cancelCurrentResponse();
        });

        // Update status panel structure
        statusPanel.setLayout(new BorderLayout(10, 0));

        // Status indicator on the left
        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftStatusPanel.setOpaque(false);

        // Create animated green dot
        onlineDot = new AnimatedDot(new Color(67, 181, 129)); // Green

        statusLabel = new JLabel("No model selected");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(180, 180, 180));

        leftStatusPanel.add(onlineDot);
        leftStatusPanel.add(statusLabel);

        // Cancel button moved further right after status text
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(cancelButton);

        // Add to status panel
        statusPanel.add(leftStatusPanel, BorderLayout.WEST);
        statusPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private void initStatusAnimation() {
        // Create timer for status label animation (100ms interval)
        statusAnimationTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStatusAnimation();
            }
        });
        statusAnimationTimer.setRepeats(true);
    }

    private void updateStatusAnimation() {
        // Base text "Responding"
        String baseText = "Responding";

        // Animation character index - increases cyclically
        animationDots = (animationDots + 1) % baseText.length();

        // Create combined string with HTML
        StringBuilder animatedText = new StringBuilder("<html>");

        // For each character
        for (int i = 0; i < baseText.length(); i++) {
            if (i == animationDots) {
                // Highlighted character
                animatedText.append("<span style='font-size: 18pt;'>")
                        .append(baseText.charAt(i))
                        .append("</span>");
            } else {
                // Normal character
                animatedText.append(baseText.charAt(i));
            }
        }

        animatedText.append("</html>");

        // Update the status label with the animated text
        statusLabel.setText(animatedText.toString());

        // Update the animated dot
        onlineDot.updateAnimation();
    }

    private ImageIcon createSendIcon() {
        int size = 24;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Plane body
        int[] xPoints = {2, size - 2, 2, 10};
        int[] yPoints = {2, size / 2, size - 2, size / 2};

        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(xPoints, yPoints, 4);

        g2d.dispose();
        return new ImageIcon(img);
    }

    private void showWelcomeMessage() {
        chatPane.showWelcomeMessage();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            sendMessage();
        }
    }

    private void sendMessage() {
        final String message = inputTextArea.getText().trim();

        // Placeholder kontrolü
        if (message.equals("Type your message here...")) {
            inputTextArea.setText("");
            inputTextArea.requestFocusInWindow();
            return;
        }

        if (message.isEmpty()) {
            return;
        }

        // Model seçili mi kontrolü
        if (currentModel == null || currentProvider == null
                || modelManager.getCurrentModel() == null
                || modelManager.getCurrentProvider() == null) {

            // Eğer UI'da seçili görünüyorsa zorla set etmeyi dene
            if (modelSelectorPanel != null) {
                String uiProvider = (String) modelSelectorPanel.getProviderComboBox().getSelectedItem();
                String uiModel = (String) modelSelectorPanel.getModelComboBox().getSelectedItem();

                if (uiProvider != null && uiModel != null) {
                    boolean success = modelManager.setCurrentModel(uiProvider, uiModel);
                    if (success) {
                        currentProvider = uiProvider;
                        currentModel = uiModel;
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to initialize model.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a model first.", "No Model Selected", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } else {
                return;
            }
        }

        // API Key kontrolü
        if (modelManager.requiresAuthentication(currentProvider)) {
            String apiKey = settingsManager.getApiKey(currentProvider);
            if (apiKey.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please set an API key for " + currentProvider, "API Key Required", JOptionPane.WARNING_MESSAGE);
                if (modelSelectorPanel != null) {
                    modelSelectorPanel.promptForApiKey();
                    if (settingsManager.getApiKey(currentProvider).isEmpty()) {
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        isResponseCancelled = false;
        chatPane.addUserMessage("You", message);
        inputTextArea.setText("");
        inputTextArea.requestFocusInWindow();
        setUIEnabled(false);

        String messageId = "msg_" + System.currentTimeMillis();
        chatPane.addAIMessageWithId(currentModel, "", messageId);

        // --- DEĞİŞİKLİK BURADA: Thread Safety sağlandı ---
        final StringBuilder fullResponse = new StringBuilder();

        Consumer<String> updateCallback = chunk -> {
            // Veriyi UI thread'ine taşıyıp orada birleştiriyoruz
            SwingUtilities.invokeLater(() -> {
                if (!isResponseCancelled) {
                    fullResponse.append(chunk);
                    chatPane.updateAIMessage(messageId, fullResponse.toString());
                }
            });
        };

        Runnable completeCallback = () -> {
            SwingUtilities.invokeLater(() -> {
                if (!isResponseCancelled) {
                    setUIEnabled(true);
                }
            });
        };

        try {
            isGeneratingResponse = true;
            currentResponseFuture = modelManager.generateResponse(message, updateCallback, completeCallback);

            currentResponseFuture.whenComplete((result, ex) -> {
                isGeneratingResponse = false;
                if (ex != null && !isResponseCancelled) {
                    String errorMsg = ex.getMessage();
                    eventLogger.log("Response error: " + errorMsg);
                    SwingUtilities.invokeLater(() -> {
                        chatPane.updateAIMessage(messageId, "Error: " + errorMsg);
                        setUIEnabled(true);
                    });
                }
            });
        } catch (Exception e) {
            isGeneratingResponse = false;
            eventLogger.log("Exception: " + e.getMessage());
            if (!isResponseCancelled) {
                chatPane.updateAIMessage(messageId, "Error: " + e.getMessage());
                setUIEnabled(true);
            }
        }
    }

//    private void sendMessage() {
//        final String message = inputTextArea.getText().trim();
//
//        // Skip if message is the placeholder text
//        if (message.equals("Type your message here...")) {
//            inputTextArea.setText("");
//            inputTextArea.requestFocusInWindow();
//            return;
//        }
//
//        // Skip if message is empty
//        if (message.isEmpty()) {
//            return;
//        }
//
//        // Debug: Check model selection state
//        eventLogger.log("Current provider: " + (currentProvider != null ? currentProvider : "null"));
//        eventLogger.log("Current model: " + (currentModel != null ? currentModel : "null"));
//        eventLogger.log("ModelManager current provider: "
//                + (modelManager.getCurrentProvider() != null
//                ? modelManager.getCurrentProvider().getProviderName() : "null"));
//        eventLogger.log("ModelManager current model: " + modelManager.getCurrentModel());
//
//        // Check if a model is selected in both UI and ModelManager
//        if (currentModel == null || currentProvider == null
//                || modelManager.getCurrentModel() == null
//                || modelManager.getCurrentProvider() == null) {
//
//            // Try to force model selection if UI shows a selection
//            if (modelSelectorPanel != null) {
//                String uiProvider = (String) modelSelectorPanel.getProviderComboBox().getSelectedItem();
//                String uiModel = (String) modelSelectorPanel.getModelComboBox().getSelectedItem();
//
//                if (uiProvider != null && uiModel != null) {
//                    eventLogger.log("Attempting to force-select model: " + uiProvider + " - " + uiModel);
//                    boolean success = modelManager.setCurrentModel(uiProvider, uiModel);
//                    if (success) {
//                        currentProvider = uiProvider;
//                        currentModel = uiModel;
//                        eventLogger.log("Force-selected model: " + uiModel);
//                    } else {
//                        eventLogger.log("Force-selection failed");
//                        JOptionPane.showMessageDialog(
//                                this,
//                                "Failed to initialize model. Please select a different model.",
//                                "Model Selection Error",
//                                JOptionPane.ERROR_MESSAGE);
//                        return;
//                    }
//                } else {
//                    JOptionPane.showMessageDialog(
//                            this,
//                            "Please select a model first.",
//                            "No Model Selected",
//                            JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
//            } else {
//                JOptionPane.showMessageDialog(
//                        this,
//                        "Please select a model first.",
//                        "No Model Selected",
//                        JOptionPane.WARNING_MESSAGE);
//                return;
//            }
//        }
//
//        // Check if API key is required and set
//        if (modelManager.requiresAuthentication(currentProvider)) {
//            String apiKey = settingsManager.getApiKey(currentProvider);
//            if (apiKey.isEmpty()) {
//                // Prompt for API key
//                eventLogger.log("API key required for " + currentProvider);
//
//                JOptionPane.showMessageDialog(
//                        this,
//                        "Please set an API key for " + currentProvider + ".",
//                        "API Key Required",
//                        JOptionPane.WARNING_MESSAGE);
//
//                // Open API key dialog
//                if (modelSelectorPanel != null) {
//                    modelSelectorPanel.promptForApiKey();
//
//                    // Check if key was set
//                    apiKey = settingsManager.getApiKey(currentProvider);
//                    if (apiKey.isEmpty()) {
//                        eventLogger.log("API key dialog cancelled or empty key");
//                        return;
//                    } else {
//                        eventLogger.log("API key set for " + currentProvider);
//                    }
//                } else {
//                    return;
//                }
//            }
//        }
//
//        // Reset cancellation flag
//        isResponseCancelled = false;
//
//        // Add user message to chat
//        chatPane.addUserMessage("You", message);
//
//        // Clear input
//        inputTextArea.setText("");
//        inputTextArea.requestFocusInWindow();
//
//        // Disable UI and update status
//        setUIEnabled(false);
//
//        // Generate response
//        String messageId = "msg_" + System.currentTimeMillis();
//        chatPane.addAIMessageWithId(currentModel, "", messageId);
//
//        // Message callbacks
//        final StringBuilder fullResponse = new StringBuilder();
//        Consumer<String> updateCallback = chunk -> {
//            fullResponse.append(chunk);
//            SwingUtilities.invokeLater(() -> {
//                if (!isResponseCancelled) {
//                    chatPane.updateAIMessage(messageId, fullResponse.toString());
//                }
//            });
//        };
//
//        Runnable completeCallback = () -> {
//            SwingUtilities.invokeLater(() -> {
//                if (!isResponseCancelled) {
//                    setUIEnabled(true);
//                }
//            });
//        };
//
//        try {
//            // Store the future for potential cancellation
//            isGeneratingResponse = true;
//            currentResponseFuture = modelManager.generateResponse(message, updateCallback, completeCallback);
//
//            // Handle completion or errors
//            currentResponseFuture.whenComplete((result, ex) -> {
//                isGeneratingResponse = false;
//                if (ex != null && !isResponseCancelled) {
//                    String errorMsg = ex.getMessage();
//                    eventLogger.log("Response error: " + errorMsg);
//
//                    SwingUtilities.invokeLater(() -> {
//                        chatPane.updateAIMessage(messageId, "Error: " + errorMsg);
//                        setUIEnabled(true);
//
//                        // Show more user-friendly message for common errors
//                        if (errorMsg != null
//                                && (errorMsg.contains("API key")
//                                || errorMsg.contains("authentication")
//                                || errorMsg.contains("Authorization"))) {
//
//                            JOptionPane.showMessageDialog(
//                                    this,
//                                    "There was a problem with your API key for " + currentProvider
//                                    + ".\nPlease check that your API key is correct and has sufficient credits.",
//                                    "API Key Error",
//                                    JOptionPane.ERROR_MESSAGE);
//                        }
//                    });
//                }
//            });
//        } catch (Exception e) {
//            isGeneratingResponse = false;
//            String errorMsg = e.getMessage();
//            eventLogger.log("Exception: " + errorMsg);
//
//            if (!isResponseCancelled) {
//                chatPane.updateAIMessage(messageId, "Error: " + errorMsg);
//                setUIEnabled(true);
//            }
//        }
//    }
    private void cancelCurrentResponse() {
        if (isGeneratingResponse) {
            isResponseCancelled = true;

            // Try to cancel via model manager
            modelManager.cancelGeneration();

            // Also try to cancel the future
            if (currentResponseFuture != null && !currentResponseFuture.isDone()) {
                currentResponseFuture.cancel(true);
            }

            // Add a message about cancellation
            chatPane.addAIMessage(currentModel, "Response generation was cancelled by the user.");

            // Re-enable the UI
            setUIEnabled(true);

            // Reset state
            isGeneratingResponse = false;
        }
    }

    private void setUIEnabled(boolean enabled) {
        inputTextArea.setEnabled(enabled);
        sendButton.setEnabled(enabled);

        if (enabled) {
            // When UI is enabled
            if (currentModel != null) {
                statusLabel.setText("Model: " + currentModel);
                statusLabel.setForeground(new Color(0, 120, 0)); // Green for active
            } else {
                statusLabel.setText("No model selected");
                statusLabel.setForeground(Color.GRAY);
            }
            cancelButton.setVisible(false); // Hide cancel button
            inputTextArea.requestFocusInWindow();
            // Stop animation timer
            statusAnimationTimer.stop();
        } else {
            // When UI is disabled (waiting for response)
            statusLabel.setText("Responding");
            cancelButton.setVisible(true); // Show cancel button
            // Start animation timer
            statusAnimationTimer.start();
        }
    }

    @Override
    public void onModelSelected(String provider, String model) {
        eventLogger.log("Model selected: " + provider + " - " + model);
        currentProvider = provider;
        currentModel = model;
        statusLabel.setText("Model: " + model);

        // Update UI state to show model is selected
        setUIEnabled(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JazariChatForge::new);
    }
}
