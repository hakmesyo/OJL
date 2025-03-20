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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

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
    private static final String MODEL_NAME = "gemma3"; // Kullanılacak modelin adı

    private ChatPane chatPane;
    private JTextArea inputTextArea; // JTextField yerine JTextArea
    private JScrollPane inputScrollPane; // JTextArea için ScrollPane
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

    public OllamaGemma3SwingChat() {
        setTitle("Gemma 3 AI Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 450));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(new EmptyBorder(10, 10, 10, 10));

        // Olay izleyiciyi başlat
        eventLogger = new EventLogger();

        // HttpClient oluştur
        httpClient = HttpClient.newHttpClient();

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

        // Input Panel oluştur
        JPanel inputPanel = createInputPanel();
        bottomPanel.add(inputPanel, BorderLayout.CENTER);

        // Önce Status Panel oluştur
        statusPanel = createStatusPanel();

        // Sonra Cancel Button'u ayarla (statusPanel artık null değil)
        setupCancelButton();

        // Status panel'i ekle
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        // İlk odağı input field'a ver
        SwingUtilities.invokeLater(() -> inputTextArea.requestFocusInWindow());

        // Animasyon başlatma
        initStatusAnimation();

        // Frame'i ekranın ortasına yerleştir
        setLocationRelativeTo(null);
        setVisible(true);

        // Hoş geldiniz mesajını göster
        chatPane.showWelcomeMessage();
    }

    private void setupCancelButton() {
        // Kırmızı renkli iptal butonu oluştur
        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(220, 53, 69)); // Bootstrap kırmızısı
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Dialog", Font.BOLD, 12));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        cancelButton.setVisible(false); // Başlangıçta gizli

        // İptal butonuna tıklandığında çalışacak işlev
        cancelButton.addActionListener(e -> {
            cancelCurrentRequest();
        });

        // Status panel yapısını güncelle ve butonu ekle
        statusPanel.setLayout(new BorderLayout(10, 0));

        // Sol tarafta durum göstergesi
        JPanel leftStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftStatusPanel.setOpaque(false);

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

        leftStatusPanel.add(onlineDot);
        leftStatusPanel.add(statusLabel);

        // Sağ tarafta iptal butonu (gizli)
        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightStatusPanel.setOpaque(false);
        rightStatusPanel.add(cancelButton);

        // Status panele ekle
        statusPanel.add(leftStatusPanel, BorderLayout.WEST);
        statusPanel.add(rightStatusPanel, BorderLayout.EAST);
    }

    // Constructor içinde veya init metodunda başlatma
    private void initStatusAnimation() {
        // Durum etiketi animasyonu için timer oluştur (300ms aralıkla)
        statusAnimationTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateStatusAnimation();
            }
        });
        statusAnimationTimer.setRepeats(true);
    }

// Durum etiketi animasyonunu güncelle
    private void updateStatusAnimation() {
        // Nokta sayısını artır ve MAX_DOTS'a ulaşınca sıfırla
        animationDots = (animationDots + 1) % (MAX_DOTS + 1);

        // Nokta sayısına göre durum metnini güncelle
        StringBuilder dotsText = new StringBuilder();
        for (int i = 0; i < animationDots; i++) {
            dotsText.append(".");
        }

        // "Yanıtlıyor..." şeklinde durum etiketini güncelle
        statusLabel.setText("Yanıtlıyor" + dotsText.toString());
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

    // Yeni metod: Input Panel oluşturma
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

        // Scroll panelin maksimum yüksekliğini sınırla
        int maxHeight = 120; // maksimum 120 piksel (yaklaşık 6-7 satır)
        inputScrollPane.setPreferredSize(new Dimension(inputScrollPane.getPreferredSize().width,
                Math.min(inputTextArea.getPreferredSize().height, maxHeight)));

        inputPanel.add(inputScrollPane, BorderLayout.CENTER);

        // Send Button
        sendButton = createSendButton();
        inputPanel.add(sendButton, BorderLayout.EAST);

        return inputPanel;
    }

    // Değişiklik: createInputField yerine createInputTextArea
    private JTextArea createInputTextArea() {
        JTextArea textArea = new JTextArea(3, 20); // 3 satır, 20 sütun başlangıç boyutu
        textArea.setFont(new Font("Dialog", Font.PLAIN, 14));
        textArea.setLineWrap(true); // Satır sonu sarma aktif
        textArea.setWrapStyleWord(true); // Kelime bazlı sarma
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 64), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        textArea.setBackground(new Color(49, 51, 56));
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        textArea.setText("Mesajınızı buraya yazın...");

        // Placeholder metni için focus dinleyicisi
        textArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (textArea.getText().equals("Mesajınızı buraya yazın...")) {
                    textArea.setText("");
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (textArea.getText().trim().isEmpty()) {
                    textArea.setText("Mesajınızı buraya yazın...");
                }
            }
        });

        // Enter ve Shift+Enter tuş dinleyicileri
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Enter tuşu basıldığında
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // Shift tuşu basılı değilse mesajı gönder ve Enter'ı engelle
                    if (!e.isShiftDown()) {
                        e.consume(); // Enter karakterinin eklenmesini engelle
                        sendMessage();
                    }
                    // Shift+Enter durumunda hiçbir şey yapmıyoruz, varsayılan davranış çalışacak
                }
            }
        });

        // InputMap ve ActionMap kullanarak daha güvenilir bir yaklaşım
        InputMap inputMap = textArea.getInputMap();
        ActionMap actionMap = textArea.getActionMap();

        // Enter tuşunu özel bir action ile değiştir
        KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        inputMap.put(enterKey, "sendMessage");
        actionMap.put("sendMessage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Shift+Enter için DEFAULT_ACTION kullanarak varsayılan davranışı koruyoruz
        KeyStroke shiftEnterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
        inputMap.put(shiftEnterKey, "insert-break");  // "insert-break" varsayılan yeni satır action'ı

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

// sendMessage metodunu da güncelleyelim
    private void sendMessage() {
        final String message = inputTextArea.getText().trim();
        // Placeholder mesajı göndermeyi engelle
        if (message.equals("Mesajınızı buraya yazın...")) {
            inputTextArea.setText("");
            inputTextArea.requestFocusInWindow();
            return;
        }

        if (!message.isEmpty()) {
            chatPane.addUserMessage("Sen", message);
            inputTextArea.setText("");
            inputTextArea.requestFocusInWindow();

            // Arayüzü kilitle ve durum mesajını güncelle
            setUIEnabled(false);

            // Streaming kullanarak yanıt al
            generateTextStreaming(message);
        }
    }

    private void setUIEnabled(boolean enabled) {
        inputTextArea.setEnabled(enabled);
        sendButton.setEnabled(enabled);

        if (enabled) {
            // UI etkinleştirildiğinde
            statusLabel.setText("Çevrimiçi");
            cancelButton.setVisible(false); // İptal butonunu gizle
            inputTextArea.requestFocusInWindow();
        } else {
            // UI devre dışı bırakıldığında
            statusLabel.setText("Yanıtlıyor");
            cancelButton.setVisible(true); // İptal butonunu göster
        }
    }

// Mevcut isteği iptal etme metodu
    private void cancelCurrentRequest() {
        if (currentRequestFuture != null && !currentRequestFuture.isDone()) {
            // Asenkron isteği iptal et
            currentRequestFuture.cancel(true);

            eventLogger.log("Kullanıcı yanıt üretimini iptal etti");

            // UI'ı normal durumuna getir
            SwingUtilities.invokeLater(() -> {
                // Mesajı güncelle
                chatPane.addAIMessage("Gemma 3", "Yanıt üretimi kullanıcı tarafından iptal edildi.");

                // UI'ı etkinleştir
                setUIEnabled(true);
            });
        }
    }

// generateTextStreaming metodunu güncelle
    public void generateTextStreaming(String prompt) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // İstek gövdesini oluştur - stream'i true yapıyoruz
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("prompt", prompt);
            requestBodyMap.put("model", MODEL_NAME);
            requestBodyMap.put("stream", true); // Streaming yanıt için true

            String requestBody = mapper.writeValueAsString(requestBodyMap);

            // İstek oluştur
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            // Yanıtı okumak için bir StringBuilder oluştur
            final StringBuilder responseContent = new StringBuilder();

            // Yanıtı başlat - boş bir mesaj ile chat penceresinde göster
            final String messageId = "msg_" + System.currentTimeMillis(); // Benzersiz ID

            SwingUtilities.invokeLater(() -> {
                chatPane.addAIMessageWithId("Gemma 3", "", messageId);
            });

            // İsteği asenkron olarak gönder ve yanıtı işle - referansı sakla
            currentRequestFuture = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofLines());

            currentRequestFuture.thenAccept(response -> {
                if (response.statusCode() == 200) {
                    // Yanıt satırlarını işle
                    try {
                        response.body().forEach(line -> {
                            try {
                                // Her satır bir JSON objesi, ancak boş satırlar olabilir
                                if (line.trim().isEmpty()) {
                                    return;
                                }

                                JsonNode node = mapper.readTree(line);

                                // Yanıt içeriğini kontrol et
                                if (node.has("response")) {
                                    String responsePart = node.get("response").asText();
                                    responseContent.append(responsePart);

                                    // UI thread'inde güncellemeleri yap
                                    final String currentResponse = responseContent.toString();

                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            chatPane.updateAIMessage(messageId, currentResponse);
                                        } catch (Exception e) {
                                            eventLogger.log("Streaming güncelleme hatası: " + e.getMessage());
                                        }
                                    });
                                }

                                // Yanıt tamamlandı mı kontrolü
                                if (node.has("done") && node.get("done").asBoolean()) {
                                    SwingUtilities.invokeLater(() -> {
                                        try {
                                            // Son kez tam içeriği ayarla
                                            chatPane.updateAIMessage(messageId, responseContent.toString());
                                        } catch (Exception e) {
                                            eventLogger.log("Son güncelleme hatası: " + e.getMessage());
                                        } finally {
                                            // Her durumda UI'ı etkinleştir
                                            setUIEnabled(true);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                eventLogger.log("Streaming yanıt işlenirken hata: " + e.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        // İptal edildiğinde buraya düşebilir
                        if (!currentRequestFuture.isCancelled()) {
                            // İptal nedeniyle değilse, bir hata mesajı göster
                            final String errorMsg = "Yanıt işleme hatası: " + e.getMessage();
                            SwingUtilities.invokeLater(() -> {
                                chatPane.updateAIMessage(messageId, errorMsg);
                                setUIEnabled(true);
                            });
                        }
                    }
                } else {
                    // Hata durumunda
                    final String errorMsg = "Ollama API hatası: " + response.statusCode();
                    SwingUtilities.invokeLater(() -> {
                        chatPane.updateAIMessage(messageId, errorMsg);
                        setUIEnabled(true);
                    });
                }
            })
                    .exceptionally(e -> {
                        // İptal edildiğinde buraya düşebilir
                        if (!currentRequestFuture.isCancelled()) {
                            // İptal nedeniyle değilse, bir hata mesajı göster
                            final String errorMsg = "İstek hatası: " + e.getMessage();
                            SwingUtilities.invokeLater(() -> {
                                chatPane.updateAIMessage(messageId, errorMsg);
                                setUIEnabled(true);
                            });
                        }
                        return null;
                    });
        } catch (Exception e) {
            final String errorMsg = "Hata: " + e.getMessage();
            SwingUtilities.invokeLater(() -> {
                chatPane.addAIMessage("Gemma 3", errorMsg);
                setUIEnabled(true);
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OllamaGemma3SwingChat::new);
    }
}
