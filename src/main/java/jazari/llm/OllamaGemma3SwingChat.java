package jazari.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class OllamaGemma3SwingChat extends JFrame implements ActionListener {

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
            Logger.getLogger(OllamaGemma3SwingChat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate"; // Ollama server address
    private static final String MODEL_NAME = "gemma3:4b"; // Name of the model to use

    private ChatPane chatPane;
    private JTextArea inputTextArea; // JTextArea instead of JTextField
    private JScrollPane inputScrollPane; // ScrollPane for JTextArea
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private EventLogger eventLogger;
    private Timer statusAnimationTimer;
    private int animationDots = 0;
    private final int MAX_DOTS = 5;
    private JButton cancelButton;
    private HttpClient httpClient;
    private CompletableFuture<HttpResponse<Stream<String>>> currentRequestFuture;
    private boolean isResponseCancelled = false;

    public OllamaGemma3SwingChat() {
        setTitle("Gemma 3 AI Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 450));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Start event logger
        eventLogger = new EventLogger();

        // Create HttpClient
        httpClient = HttpClient.newHttpClient();

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
        chatPane.showWelcomeMessage();
    }

    // Initialize status animation
    private void initStatusAnimation() {
        // Create timer for status label animation (300ms interval)
        statusAnimationTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStatusAnimation();
            }
        });
        statusAnimationTimer.setRepeats(true);
        statusLabel.putClientProperty("html.disable", Boolean.FALSE); // Enable HTML content
    }

// OnlineDot sınıfını genişletelim (OllamaGemma3SwingChat içinde iç sınıf olarak)
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

// Create an instance of AnimatedDot
    private AnimatedDot onlineDot;

// Create a small red square button for cancellation
    private void setupCancelButton() {
        // Create a small red square panel instead of a button
        cancelButton = new JButton();
        cancelButton.setPreferredSize(new Dimension(16, 16));
        cancelButton.setBackground(new Color(220, 53, 69)); // Bootstrap red
        cancelButton.setBorder(BorderFactory.createEmptyBorder());
        cancelButton.setFocusPainted(false);
        cancelButton.setVisible(false); // Initially hidden

        // Add tooltip to indicate its purpose
        cancelButton.setToolTipText("Cancel response");

        // Function when cancel button is clicked
        cancelButton.addActionListener(e -> {
            cancelCurrentRequest();
        });

        // Update status panel structure
        statusPanel.setLayout(new BorderLayout(10, 0));

        // Status indicator on the left
        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftStatusPanel.setOpaque(false);

        // Create animated green dot
        onlineDot = new AnimatedDot(new Color(67, 181, 129)); // Green

        statusLabel = new JLabel("Online");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(180, 180, 180));

        leftStatusPanel.add(onlineDot);
        leftStatusPanel.add(statusLabel);

        // Cancel button moved further right after Responding text
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(cancelButton);

        // Add to status panel
        statusPanel.add(leftStatusPanel, BorderLayout.WEST);
        statusPanel.add(centerPanel, BorderLayout.CENTER);
    }
// Update the status label animation

// Update the status label animation
    private void updateStatusAnimation() {
        // Base text "Responding"
        String baseText = "Responding";

        // Animation character index - increases cyclically
        animationDots = (animationDots + 1) % baseText.length();

        // Create combined string with HTML - no background, larger font size difference
        StringBuilder animatedText = new StringBuilder("<html>");

        // For each character
        for (int i = 0; i < baseText.length(); i++) {
            if (i == animationDots) {
                // Highlighted character - with MUCH larger font (18pt instead of 14pt)
                animatedText.append("<span style='font-size: 18pt;'>")
                        .append(baseText.charAt(i))
                        .append("</span>");
            } else {
                // Normal character - just append without any styling (no background)
                animatedText.append(baseText.charAt(i));
            }
        }

        animatedText.append("</html>");

        // Update the status label with the animated "Responding" text
        statusLabel.setText(animatedText.toString());

        // Update the animated dot
        onlineDot.updateAnimation();
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu devMenu = new JMenu("Developer");

        JMenuItem logMenuItem = new JMenuItem("Event Logger");
        logMenuItem.addActionListener(e -> eventLogger.toggleVisibility());
        devMenu.add(logMenuItem);

        JMenuItem testMenuItem = new JMenuItem("Event Test");
        testMenuItem.addActionListener(e -> testEventHandling());
        devMenu.add(testMenuItem);

        menuBar.add(devMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(36, 36, 40));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Logo and title
        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleContainer.setOpaque(false);

        // Simple simulated "logo"
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

        JLabel titleLabel = new JLabel("Gemma 3 Chat", JLabel.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        titleLabel.setForeground(new Color(114, 137, 218));

        titleContainer.add(logoPanel);
        titleContainer.add(Box.createHorizontalStrut(10));
        titleContainer.add(titleLabel);

        headerPanel.add(titleContainer, BorderLayout.CENTER);

        return headerPanel;
    }

    // New method: Create Input Panel
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

    // Change: createInputField to createInputTextArea
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

        // Small green dot (online indicator)
        JPanel onlineDot = new JPanel() {
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

        statusLabel = new JLabel("Online");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(180, 180, 180));

        onlineStatusPanel.add(onlineDot);
        onlineStatusPanel.add(statusLabel);

        panel.add(onlineStatusPanel, BorderLayout.WEST);

        return panel;
    }

    private void testEventHandling() {
        eventLogger.log("Starting event test...");
        chatPane.performEventTest();
    }

    // Create send icon - Paper plane icon
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            sendMessage();
        }
    }

// Update your sendMessage method to include these lines at the beginning
    private void sendMessage() {
        final String message = inputTextArea.getText().trim();
        // Placeholder message check
        if (message.equals("Type your message here...")) {
            inputTextArea.setText("");
            inputTextArea.requestFocusInWindow();
            return;
        }

        if (!message.isEmpty()) {
            // NEW: Check if previous response was cancelled
            if (isResponseCancelled) {
                clearOllamaState();
                isResponseCancelled = false;
            }

            chatPane.addUserMessage("You", message);
            inputTextArea.setText("");
            inputTextArea.requestFocusInWindow();

            // Lock interface and update status message
            setUIEnabled(false);

            // Get response using streaming
            generateTextStreaming(message);
        }
    }

    private void setUIEnabled(boolean enabled) {
        inputTextArea.setEnabled(enabled);
        sendButton.setEnabled(enabled);

        if (enabled) {
            // When UI is enabled
            statusLabel.setText("Online");
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

    private void cancelCurrentRequest() {
        if (currentRequestFuture != null) {
            try {
                // Log the cancellation attempt
                eventLogger.log("Forcefully terminating streaming response...");

                // Cancel the CompletableFuture
                currentRequestFuture.cancel(true);

                // Set flag immediately to stop processing incoming data
                isResponseCancelled = true;

                // Add message about cancellation
                SwingUtilities.invokeLater(() -> {
                    chatPane.addAIMessage("Gemma 3", "Response generation was cancelled by the user.");

                    // Re-enable the UI
                    setUIEnabled(true);
                });

                // Start an async task to restart the Ollama process
                CompletableFuture.runAsync(() -> {
                    forceRestartOllamaProcess();
                });

                // Reset current request
                currentRequestFuture = null;
            } catch (Exception e) {
                eventLogger.log("Error during cancellation: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> setUIEnabled(true));
            }
        }
    }

    private void forceRestartOllamaProcess() {
        try {
            eventLogger.log("Forcefully restarting Ollama process...");

            // Determine operating system
            String os = System.getProperty("os.name").toLowerCase();
            Process process = null;

            if (os.contains("win")) {
                // Windows - hide command window
                ProcessBuilder pb = new ProcessBuilder();

                // First kill
                pb.command("taskkill", "/F", "/IM", "ollama.exe");
                process = pb.start();
                process.waitFor();

                // Then restart - hiding the window
                pb = new ProcessBuilder();
                pb.command("cmd", "/c", "start", "/B", "ollama", "serve");
                // Redirect output to hide window
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                process = pb.start();
            } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
                // macOS or Linux
                // First kill
                process = Runtime.getRuntime().exec("pkill -f ollama");
                process.waitFor();
                // Then restart
                process = Runtime.getRuntime().exec("ollama serve &");
            }

            if (process != null) {
                process.waitFor(2, TimeUnit.SECONDS); // Give it a moment to start
            }

            // Create a fresh HTTP client
            httpClient = HttpClient.newHttpClient();

            eventLogger.log("Ollama process restart attempted");

            // Proactively warm up the model to reduce delay
            warmUpOllamaModel();

        } catch (Exception e) {
            eventLogger.log("Error restarting Ollama process: " + e.getMessage());
        }
    }

// New method to warm up the model after restart
    private void warmUpOllamaModel() {
        try {
            eventLogger.log("Warming up Ollama model...");

            // Prepare a simple request to initialize the model
            Map<String, Object> warmupBody = new HashMap<>();
            warmupBody.put("model", MODEL_NAME);
            warmupBody.put("prompt", "Hello, this is a warmup message.");
            warmupBody.put("stream", false);

            ObjectMapper mapper = new ObjectMapper();
            String warmupRequestBody = mapper.writeValueAsString(warmupBody);

            HttpRequest warmupRequest = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(warmupRequestBody, StandardCharsets.UTF_8))
                    .build();

            // Send the warmup request asynchronously
            CompletableFuture<HttpResponse<String>> warmupFuture
                    = httpClient.sendAsync(warmupRequest, HttpResponse.BodyHandlers.ofString());

            // Log when it completes
            warmupFuture.thenAccept(response -> {
                if (response.statusCode() == 200) {
                    eventLogger.log("Model warmup completed successfully");
                } else {
                    eventLogger.log("Model warmup failed: " + response.statusCode());
                }
            }).exceptionally(e -> {
                eventLogger.log("Error during model warmup: " + e.getMessage());
                return null;
            });

        } catch (Exception e) {
            eventLogger.log("Error warming up model: " + e.getMessage());
        }
    }

    public void generateTextStreaming(String prompt) {
        try {
            // Reset cancellation flag at the start of each request
            isResponseCancelled = false;

            ObjectMapper mapper = new ObjectMapper();

            // Create request body - set stream to true
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("prompt", prompt);
            requestBodyMap.put("model", MODEL_NAME);
            requestBodyMap.put("stream", true); // True for streaming response

            String requestBody = mapper.writeValueAsString(requestBodyMap);

            // Create request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            // Create a StringBuilder to read response
            final StringBuilder responseContent = new StringBuilder();

            // Start response - show empty message in chat window
            final String messageId = "msg_" + System.currentTimeMillis(); // Unique ID

            SwingUtilities.invokeLater(() -> {
                chatPane.addAIMessageWithId("Gemma 3", "", messageId);
            });

            // Send request asynchronously and process response - store reference
            currentRequestFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines());

            currentRequestFuture.thenAccept(response -> {
                if (response.statusCode() == 200) {
                    // Process response lines
                    try {
                        response.body().forEach(line -> {
                            // Check if the response has been cancelled
                            if (isResponseCancelled) {
                                return; // Skip processing this line
                            }

                            try {
                                // Each line is a JSON object, but there may be empty lines
                                if (line.trim().isEmpty()) {
                                    return;
                                }

                                JsonNode node = mapper.readTree(line);

                                // Check response content
                                if (node.has("response")) {
                                    String responsePart = node.get("response").asText();
                                    responseContent.append(responsePart);

                                    // Make updates in UI thread
                                    final String currentResponse = responseContent.toString();

                                    // Check again for cancellation before updating UI
                                    if (!isResponseCancelled) {
                                        SwingUtilities.invokeLater(() -> {
                                            try {
                                                chatPane.updateAIMessage(messageId, currentResponse);
                                            } catch (Exception e) {
                                                eventLogger.log("Streaming update error: " + e.getMessage());
                                            }
                                        });
                                    }
                                }

                                // Check if response is complete
                                if (node.has("done") && node.get("done").asBoolean()) {
                                    // Only finalize if not cancelled
                                    if (!isResponseCancelled) {
                                        SwingUtilities.invokeLater(() -> {
                                            try {
                                                // Set full content one last time
                                                chatPane.updateAIMessage(messageId, responseContent.toString());
                                            } catch (Exception e) {
                                                eventLogger.log("Final update error: " + e.getMessage());
                                            } finally {
                                                // Enable UI in any case
                                                setUIEnabled(true);
                                            }
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                if (!isResponseCancelled) {
                                    eventLogger.log("Error processing streaming response: " + e.getMessage());
                                }
                            }
                        });
                    } catch (Exception e) {
                        // May end up here if canceled
                        if (!currentRequestFuture.isCancelled() && !isResponseCancelled) {
                            // If not due to cancellation, show an error message
                            final String errorMsg = "Response processing error: " + e.getMessage();
                            SwingUtilities.invokeLater(() -> {
                                chatPane.updateAIMessage(messageId, errorMsg);
                                setUIEnabled(true);
                            });
                        }
                    }
                } else {
                    // In case of error
                    if (!isResponseCancelled) {
                        final String errorMsg = "Ollama API error: " + response.statusCode();
                        SwingUtilities.invokeLater(() -> {
                            chatPane.updateAIMessage(messageId, errorMsg);
                            setUIEnabled(true);
                        });
                    }
                }
            })
                    .exceptionally(e -> {
                        // May end up here if canceled
                        if (!currentRequestFuture.isCancelled() && !isResponseCancelled) {
                            // If not due to cancellation, show an error message
                            final String errorMsg = "Request error: " + e.getMessage();
                            SwingUtilities.invokeLater(() -> {
                                chatPane.updateAIMessage(messageId, errorMsg);
                                setUIEnabled(true);
                            });
                        }
                        return null;
                    });
        } catch (Exception e) {
            if (!isResponseCancelled) {
                final String errorMsg = "Error: " + e.getMessage();
                SwingUtilities.invokeLater(() -> {
                    chatPane.addAIMessage("Gemma 3", errorMsg);
                    setUIEnabled(true);
                });
            }
        }
    }

    // Add this method to the class
    private void clearOllamaState() {
        try {
            // Send a clear command as a normal prompt
            Map<String, Object> clearBody = new HashMap<>();
            clearBody.put("model", MODEL_NAME);
            clearBody.put("prompt", "/clear");  // or "/bye" depending on which works better
            clearBody.put("stream", false);

            ObjectMapper mapper = new ObjectMapper();
            String clearRequestBody = mapper.writeValueAsString(clearBody);

            HttpRequest clearRequest = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(clearRequestBody, StandardCharsets.UTF_8))
                    .build();

            // Send synchronously and ensure it completes
            HttpResponse<String> response = httpClient.send(clearRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                eventLogger.log("Model state cleared successfully");
            } else {
                eventLogger.log("Failed to clear model state: " + response.statusCode());
            }
        } catch (Exception e) {
            eventLogger.log("Error clearing model state: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OllamaGemma3SwingChat::new);
    }
}
