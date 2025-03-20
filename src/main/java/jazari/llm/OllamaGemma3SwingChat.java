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
import java.util.logging.Level;
import java.util.logging.Logger;

public class OllamaGemma3SwingChat extends JFrame implements ActionListener {
    
    
           

    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            // FlatDarkLaf tema ayarları
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

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate"; // Ollama serve adresi
    private static final String MODEL_NAME = "gemma3:12b"; // Kullanılacak modelin adı

    private ChatPane chatPane;
    private JTextField inputField;
    private JButton sendButton;
    private JScrollPane scrollPane;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private EventLogger eventLogger;

    public OllamaGemma3SwingChat() {
        setTitle("Gemma 3 AI Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 450));
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Olay izleyiciyi başlat
        eventLogger = new EventLogger();

        // Menü ekle
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

        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

        // Input Field
        inputField = createInputField();
        inputPanel.add(inputField, BorderLayout.CENTER);

        // Send Button
        sendButton = createSendButton();
        inputPanel.add(sendButton, BorderLayout.EAST);

        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        // Status Panel
        statusPanel = createStatusPanel();
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        // İlk odağı input field'a ver
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());

        // Frame'i ekranın ortasına yerleştir
        setLocationRelativeTo(null);
        setVisible(true);

        // Hoş geldiniz mesajını göster
        chatPane.showWelcomeMessage();
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu devMenu = new JMenu("Geliştirici");
        
        JMenuItem logMenuItem = new JMenuItem("Olay İzleyici");
        logMenuItem.addActionListener(e -> eventLogger.toggleVisibility());
        devMenu.add(logMenuItem);
        
        JMenuItem testMenuItem = new JMenuItem("Olay Testi");
        testMenuItem.addActionListener(e -> testEventHandling());
        devMenu.add(testMenuItem);
        
        menuBar.add(devMenu);
        setJMenuBar(menuBar);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(36, 36, 40));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Logo ve başlık
        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titleContainer.setOpaque(false);

        // Basit simüle edilmiş "logo"
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

    private JTextField createInputField() {
        JTextField field = new JTextField("Mesajınızı buraya yazın...");
        field.setFont(new Font("Dialog", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 64), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        field.setBackground(new Color(49, 51, 56));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);

        // Placeholder metni için focus dinleyicisi
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals("Mesajınızı buraya yazın...")) {
                    field.setText("");
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setText("Mesajınızı buraya yazın...");
                }
            }
        });

        // Enter tuşuyla gönderme
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        
        return field;
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

        // Küçük yeşil nokta (çevrimiçi göstergesi)
        JPanel onlineDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(67, 181, 129)); // Yeşil
                g2d.fillOval(0, 0, 8, 8);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(8, 8);
            }
        };

        statusLabel = new JLabel("Çevrimiçi");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(180, 180, 180));

        onlineStatusPanel.add(onlineDot);
        onlineStatusPanel.add(statusLabel);

        panel.add(onlineStatusPanel, BorderLayout.WEST);
        
        return panel;
    }

    private void testEventHandling() {
        eventLogger.log("Olay testi başlatılıyor...");
        chatPane.performEventTest();
    }

    private void sendMessage() {
        final String message = inputField.getText().trim();
        // Placeholder mesajı göndermeyi engelle
        if (message.equals("Mesajınızı buraya yazın...")) {
            inputField.setText("");
            inputField.requestFocusInWindow();
            return;
        }

        if (!message.isEmpty()) {
            chatPane.addUserMessage("Sen", message);
            inputField.setText("Mesajınızı buraya yazın...");
            inputField.requestFocusInWindow();

            // Arayüzü kilitle ve durum mesajını güncelle
            setUIEnabled(false);

            // Ollama'dan yanıt almak için arka plan thread'i başlat
            final String finalMessage = message;
            new Thread(() -> {
                String response = generateText(finalMessage);
                SwingUtilities.invokeLater(() -> {
                    chatPane.addAIMessage("Gemma 3", response);
                    setUIEnabled(true);
                });
            }).start();
        }
    }

    private void setUIEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        if (enabled) {
            statusLabel.setText("Çevrimiçi");
            inputField.requestFocusInWindow();
        } else {
            statusLabel.setText("Yanıtlıyor...");
        }
    }

    // Gönder ikonu oluşturma - Paper plane icon
    private ImageIcon createSendIcon() {
        int size = 24;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Uçak gövdesi
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

    public String generateText(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            // İstek gövdesini oluştur
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("prompt", prompt);
            requestBodyMap.put("model", MODEL_NAME);
            requestBodyMap.put("stream", false); // Tam yanıtı almak için stream'i false yapıyoruz

            String requestBody = mapper.writeValueAsString(requestBodyMap);

            // İstek oluştur
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            // İsteği gönder ve yanıtı al
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Yanıtı işle
            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                return root.get("response").asText();
            } else {
                return "Ollama API hatası: " + response.statusCode() + " - " + response.body();
            }
        } catch (Exception e) {
            return "Hata: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OllamaGemma3SwingChat::new);
    }
}