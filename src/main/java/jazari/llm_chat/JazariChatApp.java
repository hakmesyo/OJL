package jazari.llm_chat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formdev.flatlaf.FlatLightLaf;
import java.io.File;
import java.util.HashMap;
import jazari.speech_to_text_vosk.VoiceRecognitionService;

public class JazariChatApp {

    // API endpointi
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static String apiKey = ""; // API anahtarını Preferences'tan alacağız
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Preferences prefs = Preferences.userNodeForPackage(JazariChatApp.class);
    private static VoiceIntegration voiceIntegration;

    // Sohbet geçmişi için sınıf
    private static class ChatMessage {

        enum Role {
            USER, ASSISTANT
        }

        private final Role role;
        private final String content;
        private final LocalDateTime timestamp;

        public ChatMessage(Role role, String content) {
            this.role = role;
            this.content = content;
            this.timestamp = LocalDateTime.now();
        }

        public Role getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    private static void setupWindowCloseHandler() {
        mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                savePreferences();

                // Ses tanıma kaynakları da temizlenecek
                if (voiceIntegration != null) {
                    voiceIntegration.cleanup();
                }
            }
        });
    }

    // Sohbet oturumu sınıfı
    private static class ChatSession {

        private String name;
        private LocalDateTime creationTime;
        private final List<ChatMessage> messages;

        public ChatSession(String name) {
            this.name = name;
            this.creationTime = LocalDateTime.now();
            this.messages = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LocalDateTime getCreationTime() {
            return creationTime;
        }

        public List<ChatMessage> getMessages() {
            return messages;
        }

        public void addMessage(ChatMessage message) {
            messages.add(message);
        }

        // Oturumun ilk kullanıcı mesajını önizleme olarak döndür
        public String getPreview() {
            for (ChatMessage message : messages) {
                if (message.getRole() == ChatMessage.Role.USER) {
                    String content = message.getContent();
                    if (content.length() > 50) {
                        return content.substring(0, 47) + "...";
                    }
                    return content;
                }
            }
            return "";
        }
    }

    // Aktif sohbet mesajları
    private static final List<ChatMessage> chatHistory = new ArrayList<>();

    // Tüm sohbet oturumları
    private static final List<ChatSession> sessions = new ArrayList<>();

    // Aktif sohbet oturumu
    private static ChatSession currentSession;

    // Kullanılacak renkler
    private static final Color LIGHT_THEME_BG = new Color(248, 249, 252);
    private static final Color DARK_THEME_BG = new Color(23, 23, 23);
    private static final Color PRIMARY_BUTTON = new Color(79, 70, 229); // Indigo rengi
    private static final Color CODE_BLOCK_BG = new Color(40, 44, 52);
    private static final Color CODE_BLOCK_BG_DARK = new Color(30, 33, 40);
    private static final Color CODE_TEXT_COLOR = new Color(171, 178, 191);
    private static final Color CODE_BORDER_COLOR = new Color(60, 64, 72);
    private static final Color SESSION_ACTIVE_BG = new Color(235, 236, 247);
    private static final Color SESSION_HOVER_BG = new Color(242, 242, 242);
    private static boolean isDarkMode = false;

    // Ana pencere ve bileşenler
    private static JFrame mainFrame;
    private static JPanel chatPanel;
    private static JPanel messagesPanel;
    private static JScrollPane scrollPane;
    private static JTextArea inputArea;
    private static JLabel statusLabel;
    private static JPanel sessionsPanel;

    public static void show() {
        // FlatLaf temasını yükle
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf tema yüklenirken hata oluştu: " + ex);
        }

        // Tüm ayarları yükle (API anahtarı ve LLM seçimi dahil)
        loadPreferences();

        SwingUtilities.invokeLater(() -> createAndShowGUI());

    }

    public static void main(String[] args) {
        // FlatLaf temasını yükle
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf tema yüklenirken hata oluştu: " + ex);
        }

        // Tüm ayarları yükle (API anahtarı ve LLM seçimi dahil)
        loadPreferences();

        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        // Ana pencere oluşturma
        mainFrame = new JFrame("Jazari LLM Chat Interface");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setSize(1000, 800);
        mainFrame.setMinimumSize(new Dimension(700, 500));

        // Sol kenar panel
        JPanel sidePanel = createSidePanel();

        // Sohbet panel - tüm mesajları içerecek
        chatPanel = new JPanel(new BorderLayout(0, 10));
        chatPanel.setBackground(LIGHT_THEME_BG);
        chatPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Mesajları gösterecek panel
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(LIGHT_THEME_BG);

        // Otomatik kaydırma için scroll pane
        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        // Durum çubuğu - ÖNEMLİ: statusLabel'ı VoiceIntegration'dan önce oluşturun
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new EmptyBorder(5, 15, 5, 15));
        statusPanel.setBackground(LIGHT_THEME_BG);
        statusLabel = new JLabel("Hazır");
        statusLabel.setForeground(Color.GRAY);
        statusPanel.add(statusLabel, BorderLayout.WEST);

        // Giriş alanı paneli
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(LIGHT_THEME_BG);

        inputArea = new JTextArea(2, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputArea.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Placeholder text
        inputArea.setText("LLM'e bir soru sorun...");
        inputArea.setForeground(Color.GRAY);

        // Placeholder text işlemleri
        inputArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (inputArea.getText().equals("LLM'e bir soru sorun...")) {
                    inputArea.setText("");
                    inputArea.setForeground(isDarkMode ? Color.WHITE : Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (inputArea.getText().isEmpty()) {
                    inputArea.setText("LLM'e bir soru sorun...");
                    inputArea.setForeground(Color.GRAY);
                }
            }
        });

        // Enter tuşu ile gönderme
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Shift+Enter ile yeni satır, sadece Enter ile gönder
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume(); // Enter tuşunun normal işlevini engelle
                    if (!inputArea.getText().equals("LLM'e bir soru sorun...")
                            && !inputArea.getText().trim().isEmpty()) {
                        sendMessage();
                    }
                }
            }
        });

        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));

        // Gönder butonu
        // İkon kullanmayı dene, yoksa metin göster
        ImageIcon sendIcon = null;
        try {
            // Kaynakları doğrudan sınıf yükleyici ile almayı dene
            java.net.URL iconURL = JazariChatApp.class.getClassLoader().getResource("icons/send_icon.png");
            if (iconURL != null) {
                sendIcon = new ImageIcon(iconURL);
            }
        } catch (Exception e) {
            System.err.println("Gönder ikonu yüklenemedi: " + e.getMessage());
        }

        JButton sendButton;
        if (sendIcon != null) {
            sendButton = new JButton(sendIcon);
        } else {
            sendButton = new JButton("➤");
            sendButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        }

        sendButton.setBackground(PRIMARY_BUTTON);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setPreferredSize(new Dimension(50, 40));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sendButton.addActionListener(e -> {
            if (!inputArea.getText().equals("LLM'e bir soru sorun...")
                    && !inputArea.getText().trim().isEmpty()) {
                sendMessage();
            }
        });

        // Alt panel - kullanıcı girişi ve buton
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        bottomPanel.setBackground(LIGHT_THEME_BG);

        bottomPanel.add(inputScrollPane, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // Güvenli kullanım bildirimi
        JLabel disclaimerLabel = new JLabel("LLM AI yanıtları her zaman doğru olmayabilir. Önemli konularda profesyonel tavsiye alın.");
        disclaimerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        disclaimerLabel.setForeground(Color.GRAY);
        disclaimerLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel disclaimerPanel = new JPanel(new BorderLayout());
        disclaimerPanel.setBackground(LIGHT_THEME_BG);
        disclaimerPanel.add(disclaimerLabel, BorderLayout.CENTER);
        disclaimerPanel.setBorder(new EmptyBorder(10, 0, 5, 0));

        // Giriş panelini tamamla
        inputPanel.add(bottomPanel, BorderLayout.CENTER);
        inputPanel.add(disclaimerPanel, BorderLayout.SOUTH);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // Pencere içeriğini düzenle
        mainFrame.add(sidePanel, BorderLayout.WEST);
        mainFrame.add(chatPanel, BorderLayout.CENTER);
        mainFrame.add(statusPanel, BorderLayout.SOUTH);

        // SES TANIMA ENTEGRASYONU - statusLabel oluşturulduktan sonra
        voiceIntegration = VoiceIntegration.integrate(inputArea, bottomPanel, statusLabel);

        // Pencereyi gösterme
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);

        // GUI tamamen oluşturuldu, şimdi oturumları yükleyebiliriz
        loadSessions();

        // Pencere kapatıldığında ayarları kaydet
        setupWindowCloseHandler();
    }
    
    

    /**
     * Sol kenar panelini oluşturur
     */
    private static JPanel createSidePanel() {
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(250, 0));
        sidePanel.setBackground(Color.WHITE);
        sidePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));

        // Üst kısım - logo ve başlık
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel titleLabel = new JLabel("LLM ChatBot");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(PRIMARY_BUTTON);

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Butonlar için panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Yeni sohbet butonu
        JButton newChatButton = createMenuButton("Yeni Sohbet");
        newChatButton.addActionListener(e -> createNewSession());

        // API Ayarları butonu
        JButton apiSettingsButton = createMenuButton("API Ayarları");
        apiSettingsButton.addActionListener(e -> showAPISettingsDialog());

        // Butonları panele ekle
        buttonPanel.add(newChatButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(apiSettingsButton);

        // Sohbet geçmişi başlığı
        JPanel historyHeaderPanel = new JPanel(new BorderLayout());
        historyHeaderPanel.setBackground(Color.WHITE);
        historyHeaderPanel.setBorder(new EmptyBorder(15, 15, 5, 15));

        JLabel historyLabel = new JLabel("Sohbet Geçmişi");
        historyLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyHeaderPanel.add(historyLabel, BorderLayout.CENTER);

        // Kaydırma çubuğu olan konteyner
        sessionsPanel = new JPanel();
        sessionsPanel.setLayout(new BoxLayout(sessionsPanel, BoxLayout.Y_AXIS));
        sessionsPanel.setBackground(Color.WHITE);
        sessionsPanel.setBorder(new EmptyBorder(5, 15, 5, 15));
        sessionsPanel.setFocusable(true); // Odaklanabilir yapın

        JScrollPane sessionsScrollPane = new JScrollPane(sessionsPanel);
        sessionsScrollPane.setBorder(null);
        sessionsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        sessionsScrollPane.setFocusable(true); // Scroll pane de odaklanabilir olsun

        // Alt kısım - sürüm bilgisi
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel versionLabel = new JLabel("v1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(Color.GRAY);

        footerPanel.add(versionLabel, BorderLayout.SOUTH);

        // Panelleri birleştir
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        sidePanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(historyHeaderPanel, BorderLayout.NORTH);
        centerPanel.add(sessionsScrollPane, BorderLayout.CENTER);

        sidePanel.add(centerPanel, BorderLayout.CENTER);
        sidePanel.add(footerPanel, BorderLayout.SOUTH);

        return sidePanel;
    }

    /**
     * Yan panel için menü butonu oluşturur
     */
    private static JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.DARK_GRAY);
        button.setBackground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover efekti
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SESSION_HOVER_BG);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(isDarkMode ? DARK_THEME_BG : Color.WHITE);
            }
        });

        button.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(220, 40));

        return button;
    }

    /**
     * Sohbet oturumları listesini günceller
     */
    private static void refreshSessionsList() {
        if (sessionsPanel == null) {
            return;
        }

        // Tüm bileşenleri temizle
        sessionsPanel.removeAll();

        // Sohbet oturumları boşsa bilgi mesajı göster
        if (sessions.isEmpty()) {
            JLabel emptyLabel = new JLabel("Henüz sohbet oturumu yok");
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            sessionsPanel.add(emptyLabel);
        } else {
            // Her oturum için bir panel ekle
            for (ChatSession session : sessions) {
                JPanel sessionItemPanel = createSessionPanel(session);
                sessionsPanel.add(sessionItemPanel);
                sessionsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        // Paneli güncelle
        sessionsPanel.revalidate();
        sessionsPanel.repaint();
    }

    /**
     * Sohbet oturumu için panel oluşturur
     */
    /**
     * Sohbet oturumu için panel oluşturur
     */
    private static JPanel createSessionPanel(ChatSession session) {
        // Ana panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        panel.setName("SessionPanel_" + session.getName()); // Panel'e tanımlayıcı isim ver

        // Oturum aktifse arka planı değiştir
        if (session == currentSession) {
            panel.setBackground(SESSION_ACTIVE_BG);
        } else {
            panel.setBackground(Color.WHITE);
        }

        // Oturum adını kısalt gerekirse
        String displayName = session.getName();
        if (displayName.length() > 20) {
            displayName = displayName.substring(0, 17) + "...";
        }

        // Tarih formatı
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String dateStr = session.getCreationTime().format(formatter);

        // Önizleme metni
        String preview = session.getPreview();
        if (preview.length() > 35) {
            preview = preview.substring(0, 32) + "...";
        }

        // İçerik paneli
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(panel.getBackground());
        contentPanel.setName("ContentPanel_" + session.getName()); // İçerik paneline tanımlayıcı isim ver

        // Oturum adı etiketi
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameLabel.setName("NameLabel_" + session.getName());
        contentPanel.add(nameLabel);

        // Tarih etiketi
        JLabel dateLabel = new JLabel(dateStr);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateLabel.setName("DateLabel_" + session.getName());
        contentPanel.add(dateLabel);

        // Önizleme varsa ekle
        if (!preview.isEmpty()) {
            JLabel previewLabel = new JLabel(preview);
            previewLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            previewLabel.setForeground(Color.DARK_GRAY);
            previewLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            previewLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            previewLabel.setName("PreviewLabel_" + session.getName());
            contentPanel.add(previewLabel);
        }

        // İçerik panelini ana panele ekle
        panel.add(contentPanel, BorderLayout.CENTER);

        // Sağ tıklama menüsü
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem renameItem = new JMenuItem("Yeniden Adlandır");
        renameItem.addActionListener(e -> renameSession(session));

        JMenuItem deleteItem = new JMenuItem("Sil");
        deleteItem.addActionListener(e -> deleteSession(session));

        popupMenu.add(renameItem);
        popupMenu.add(deleteItem);

        // Sağ tıklama menüsü ekle
        panel.setComponentPopupMenu(popupMenu);
        contentPanel.setComponentPopupMenu(popupMenu);

        // Tüm bileşenlere tıklama olayı ekle
        java.awt.event.MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                System.out.println("Oturum tıklandı: " + session.getName() + " - Kaynak: " + evt.getSource().getClass().getName());
                if (session != currentSession) {
                    selectSession(session);
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (session != currentSession) {
                    panel.setBackground(SESSION_HOVER_BG);
                    contentPanel.setBackground(SESSION_HOVER_BG);

                    // Tüm etiketlerin arka planını da güncelle
                    for (Component comp : contentPanel.getComponents()) {
                        if (comp instanceof JLabel) {
                            comp.setBackground(SESSION_HOVER_BG);
                        }
                    }
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (session != currentSession) {
                    panel.setBackground(Color.WHITE);
                    contentPanel.setBackground(Color.WHITE);

                    // Tüm etiketlerin arka planını da güncelle
                    for (Component comp : contentPanel.getComponents()) {
                        if (comp instanceof JLabel) {
                            comp.setBackground(Color.WHITE);
                        }
                    }
                }
            }
        };

        // Ana panele mouse listener ekle
        panel.addMouseListener(mouseAdapter);

        // İçerik paneline de mouse listener ekle
        contentPanel.addMouseListener(mouseAdapter);

        // İçerik panelindeki etiketlere mouse listener ekle
        nameLabel.addMouseListener(mouseAdapter);
        dateLabel.addMouseListener(mouseAdapter);
        // Önizleme etiketine de ekleyelim, varsa
        for (Component comp : contentPanel.getComponents()) {
            if (comp instanceof JLabel && comp.getName() != null && comp.getName().startsWith("PreviewLabel_")) {
                comp.addMouseListener(mouseAdapter);
            }
        }

        // Tıklanabilir olduğunu belirtmek için imleç ayarları
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        contentPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dateLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Odaklanabilirlik ayarları
        panel.setFocusable(true);
        contentPanel.setFocusable(true);

        // Panel boyutları
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        panel.setPreferredSize(new Dimension(220, 70));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Hizalama
        panel.setOpaque(true); // Arka plan rengini göster
        contentPanel.setOpaque(true);

        return panel;
    }

    /**
     * Yeni bir sohbet oturumu oluşturur
     */
    private static void createNewSession() {
        String defaultName = "New Chat";

        String sessionName = (String) JOptionPane.showInputDialog(mainFrame,
                "Yeni sohbet için bir isim girin:",
                "Yeni Sohbet",
                JOptionPane.PLAIN_MESSAGE,
                null, // icon
                null, // selection values
                defaultName); // initial value - buraya default değeri verdik

        if (sessionName == null) {
            // İptal edildi
            return;
        }

        // Boş isim kontrolü
        if (sessionName.toString().trim().isEmpty()) {
            sessionName = defaultName;
        }

        // Yeni oturum oluştur
        ChatSession newSession = new ChatSession(sessionName.toString().trim());

        // Listeye ekle
        sessions.add(0, newSession); // En başa ekle

        // Oturumu aktif et
        selectSession(newSession);

        // Oturumları kaydet
        saveSessions();

        statusLabel.setText("Yeni sohbet başlatıldı: " + sessionName);
    }

    private static void selectSession(ChatSession session) {
        if (session == null) {
            System.out.println("HATA: Null oturum seçilmeye çalışıldı!");
            return;
        }

        System.out.println("Oturum seçiliyor: " + session.getName() + ", şu anki: "
                + (currentSession != null ? currentSession.getName() : "null"));

        // Aktif oturumu değiştir
        currentSession = session;

        // Mevcut oturumun mesajlarını chat history'e kopyala
        chatHistory.clear();
        chatHistory.addAll(session.getMessages());

        // Mesajları ekranda göster
        refreshMessages();

        // Oturum listesini güncelle (seçili oturumu vurgulamak için)
        refreshSessionsList();

        // Durum çubuğunu güncelle
        statusLabel.setText(session.getName() + " oturumu yüklendi");

        System.out.println("Oturum yüklendi: " + session.getName() + ", mesaj sayısı: " + chatHistory.size());
    }

    /**
     * Aktif oturumun mesajlarını yükler
     */
    private static void loadSessionMessages() {
        if (currentSession == null) {
            System.out.println("Hata: currentSession null");
            return;
        }

        chatHistory.clear();
        List<ChatMessage> messages = currentSession.getMessages();
        System.out.println("Yüklenen mesaj sayısı: " + messages.size());

        chatHistory.addAll(messages);
        refreshMessages();
    }

    /**
     * Bir oturumu yeniden adlandırır
     */
    private static void renameSession(ChatSession session) {
        String newName = JOptionPane.showInputDialog(mainFrame,
                "Yeni sohbet adını girin:",
                session.getName());

        if (newName != null && !newName.trim().isEmpty()) {
            session.setName(newName.trim());
            refreshSessionsList();
            saveSessions();
        }
    }

    /**
     * Bir oturumu siler
     */
    private static void deleteSession(ChatSession session) {
        int choice = JOptionPane.showConfirmDialog(mainFrame,
                "'" + session.getName() + "' oturumunu silmek istediğinizden emin misiniz?",
                "Oturum Sil", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            sessions.remove(session);

            // Eğer silinen oturum aktifse, başka bir oturuma geç
            if (session == currentSession) {
                if (!sessions.isEmpty()) {
                    selectSession(sessions.get(0));
                } else {
                    currentSession = null;
                    chatHistory.clear();
                    refreshMessages();
                }
            }

            refreshSessionsList();
            saveSessions();
        }
    }

    /**
     * Panel içindeki bileşenlerin renklerini günceller
     */
    private static void updatePanelColors(JPanel panel, Color bgColor) {
        panel.setBackground(bgColor);
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                comp.setBackground(bgColor);
                updatePanelColors((JPanel) comp, bgColor);
            } else if (comp instanceof JScrollPane) {
                comp.setBackground(bgColor);
                JScrollPane scrollPane = (JScrollPane) comp;
                if (scrollPane.getViewport() != null && scrollPane.getViewport().getView() != null) {
                    scrollPane.getViewport().getView().setBackground(bgColor);
                }
            }
        }
    }

    /**
     * Mesaj gönderme işlemi
     */
    private static void sendMessage() {
        String prompt = inputArea.getText().trim();
        if (prompt.isEmpty() || prompt.equals("LLM'e bir soru sorun...")) {
            return;
        }

        if (apiKey.isEmpty()) {
            showAPISettingsDialog();
            return;
        }

        // Durum mesajını güncelle
        statusLabel.setText("İstek gönderiliyor...");

        // Kullanıcı mesajını oluştur
        ChatMessage userMessage = new ChatMessage(ChatMessage.Role.USER, prompt);

        // Kullanıcı mesajını sohbet geçmişine ekle
        chatHistory.add(userMessage);

        // Kullanıcı mesajını mevcut oturuma da ekle
        if (currentSession != null) {
            currentSession.addMessage(userMessage);

            // Oturumları kaydet
            saveSessions();

            // Oturum listesini yenile (mesaj içeriğini göstermek için)
            refreshSessionsList();
        }

        // Kullanıcı mesajını ekrana yazdır
        refreshMessages();

        // Giriş alanını temizle
        inputArea.setText("");
        inputArea.requestFocus();

        // "Yazıyor..." göster
        ChatMessage typingMessage = new ChatMessage(ChatMessage.Role.ASSISTANT, "Düşünüyor...");
        chatHistory.add(typingMessage);
        refreshMessages();

        // API isteğini ayrı bir thread'de yap
        new Thread(() -> {
            try {
                // Gemini API yerine genel AI API çağrısı yap
                String response = callAIAPI(prompt);

                // Son eklenen "Düşünüyor..." mesajını kaldır
                SwingUtilities.invokeLater(() -> {
                    chatHistory.remove(chatHistory.size() - 1);

                    // Asıl yanıtı ekle
                    ChatMessage assistantMessage = new ChatMessage(ChatMessage.Role.ASSISTANT, response);
                    chatHistory.add(assistantMessage);

                    // Asistant mesajını mevcut oturuma da ekle
                    if (currentSession != null) {
                        currentSession.addMessage(assistantMessage);
                        saveSessions();
                    }

                    refreshMessages();

                    // Durum mesajını güncelle
                    statusLabel.setText("Yanıt alındı");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    // Son eklenen "Düşünüyor..." mesajını kaldır
                    chatHistory.remove(chatHistory.size() - 1);

                    // Hata mesajını ekle
                    ChatMessage errorMessage = new ChatMessage(ChatMessage.Role.ASSISTANT, "Hata: " + ex.getMessage());
                    chatHistory.add(errorMessage);

                    // Mevcut oturuma da ekle
                    if (currentSession != null) {
                        currentSession.addMessage(errorMessage);
                        saveSessions();
                    }

                    refreshMessages();

                    // Durum mesajını güncelle
                    statusLabel.setText("Hata: " + ex.getMessage());
                });
            }
        }).start();
    }

    /**
     * Mesajları ekrana yazdırma
     */
    private static void refreshMessages() {
        // Mesajlar panelini temizle
        messagesPanel.removeAll();

        // Her mesaj için
        for (ChatMessage message : chatHistory) {
            // Mesaj paneli
            JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
            messagePanel.setBackground(isDarkMode ? DARK_THEME_BG : LIGHT_THEME_BG);
            messagePanel.setBorder(new EmptyBorder(10, 5, 10, 5));

            // Gönderen bilgisi ve eylemler için üst panel
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(isDarkMode ? DARK_THEME_BG : LIGHT_THEME_BG);

            // Sol taraf - gönderen bilgisi
            JLabel senderLabel = new JLabel(message.getRole() == ChatMessage.Role.USER ? "Siz" : "AI");
            senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            senderLabel.setForeground(message.getRole() == ChatMessage.Role.USER
                    ? (isDarkMode ? Color.WHITE : Color.BLACK) : PRIMARY_BUTTON);

            // Sağ taraf - zaman ve eylemler
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightPanel.setBackground(isDarkMode ? DARK_THEME_BG : LIGHT_THEME_BG);

            // Zaman damgası
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String timeStr = message.getTimestamp().format(formatter);
            JLabel timeLabel = new JLabel(timeStr);
            timeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            timeLabel.setForeground(Color.GRAY);

            // Kopyala butonu (her mesaj için)
            JButton copyMsgButton = new JButton("Kopyala");
            copyMsgButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            copyMsgButton.setBorderPainted(false);
            copyMsgButton.setContentAreaFilled(false);
            copyMsgButton.setFocusPainted(false);
            copyMsgButton.setForeground(Color.GRAY);
            copyMsgButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // Tüm mesaj içeriğini kopyalamak için
            final String messageToCopy = message.getContent();
            copyMsgButton.addActionListener(e -> {
                StringSelection selection = new StringSelection(messageToCopy);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                copyMsgButton.setText("Kopyalandı!");
                statusLabel.setText("Mesaj panoya kopyalandı");

                // 2 saniye sonra geri değiştir
                Timer timer = new Timer(2000, evt -> {
                    copyMsgButton.setText("Kopyala");
                    statusLabel.setText("Hazır");
                });
                timer.setRepeats(false);
                timer.start();
            });

            rightPanel.add(copyMsgButton);
            rightPanel.add(timeLabel);

            topPanel.add(senderLabel, BorderLayout.WEST);
            topPanel.add(rightPanel, BorderLayout.EAST);

            messagePanel.add(topPanel, BorderLayout.NORTH);

            // Mesaj içeriği
            String content = message.getContent();

            // Hem kullanıcı hem de asistan mesajlarını aynı şekilde işle
            processMessageContent(messagePanel, content);

            // Mesaj panelini ana panele ekle
            messagesPanel.add(messagePanel);

            // Mesajları ayıran çizgi
            if (message != chatHistory.get(chatHistory.size() - 1)) {
                JSeparator separator = new JSeparator();
                separator.setForeground(new Color(200, 200, 200, 50));
                separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                messagesPanel.add(separator);
            }
        }

        // Panel'i güncelle
        messagesPanel.revalidate();
        messagesPanel.repaint();

        // Otomatik kaydır
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    /**
     * Mesaj içeriğini işleyen yardımcı metot (hem kullanıcı hem asistan
     * mesajları için)
     */
    private static void processMessageContent(JPanel messagePanel, String content) {
        // İçerik paneli
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(isDarkMode ? DARK_THEME_BG : LIGHT_THEME_BG);
        contentPanel.setBorder(new EmptyBorder(5, 15, 5, 5));

        // Kod bloklarını ve normal metni ayır
        Pattern codeBlockPattern = Pattern.compile("```(.*?)```", Pattern.DOTALL);
        Matcher matcher = codeBlockPattern.matcher(content);

        int lastEnd = 0;

        while (matcher.find()) {
            // Kod bloğundan önceki normal metni ekle
            if (matcher.start() > lastEnd) {
                String normalText = content.substring(lastEnd, matcher.start());
                addTextWithScrolling(contentPanel, normalText, false);
            }

            // Kod bloğunu işle
            String codeBlock = matcher.group(1).trim();
            String language = "";

            // Dil belirtilmiş mi kontrol et
            if (codeBlock.contains("\n")) {
                String firstLine = codeBlock.substring(0, codeBlock.indexOf("\n")).trim();
                if (!firstLine.contains(" ") && !firstLine.isEmpty()) {
                    language = firstLine;
                    codeBlock = codeBlock.substring(codeBlock.indexOf("\n")).trim();
                }
            }

            // Kod bloğu paneli
            addCodeBlock(contentPanel, codeBlock, language);

            lastEnd = matcher.end();
        }

        // Geriye kalan normal metni ekle
        if (lastEnd < content.length()) {
            String normalText = content.substring(lastEnd);
            addTextWithScrolling(contentPanel, normalText, false);
        }

        // İçerik panelini mesaj paneline ekle
        messagePanel.add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Normal metin eklemek için yardımcı metot (kaydırma çubuğu ile)
     */
    private static void addTextWithScrolling(JPanel container, String text, boolean isCode) {
        JTextArea textArea = new JTextArea();
        textArea.setText(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(!isCode);
        textArea.setBackground(isCode
                ? (isDarkMode ? CODE_BLOCK_BG_DARK : CODE_BLOCK_BG)
                : (isDarkMode ? DARK_THEME_BG : LIGHT_THEME_BG));
        textArea.setForeground(isCode ? CODE_TEXT_COLOR
                : (isDarkMode ? Color.LIGHT_GRAY : Color.BLACK));
        textArea.setFont(isCode
                ? new Font("Monospaced", Font.PLAIN, 13)
                : new Font("Segoe UI", Font.PLAIN, 14));

        // Yüksekliği içeriğe göre hesapla
        int lineCount = text.split("\n").length;
        int textLength = text.length();

        // Metin uzun mu?
        boolean isLongText = lineCount > 10 || textLength > 500;

        // Kaydırma çubuğu ve sınırlı yükseklik sadece uzun metinler için
        if (isLongText) {
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());

            // Maksimum yükseklik 300px
            scrollPane.setPreferredSize(new Dimension(500, Math.min(lineCount * 18, 300)));
            container.add(scrollPane);
        } else {
            // Kısa metinler için doğrudan textArea ekle, yüksekliği içeriğe göre ayarla
            textArea.setPreferredSize(new Dimension(500, lineCount * 18 + 5));
            container.add(textArea);
        }
    }

    /**
     * Kod bloğu eklemek için yardımcı metot
     */
    private static void addCodeBlock(JPanel container, String codeBlock, String language) {
        // Kod bloğu paneli
        JPanel codePanel = new JPanel(new BorderLayout());
        codePanel.setBackground(isDarkMode ? CODE_BLOCK_BG_DARK : CODE_BLOCK_BG);
        codePanel.setBorder(BorderFactory.createLineBorder(CODE_BORDER_COLOR, 1, true));

        // Kod başlık paneli
        JPanel codeHeaderPanel = new JPanel(new BorderLayout());
        codeHeaderPanel.setBackground(CODE_BORDER_COLOR);
        codeHeaderPanel.setBorder(new EmptyBorder(5, 8, 5, 8));

        // Dil etiketi
        JLabel langLabel = new JLabel(language.isEmpty() ? "kod" : language);
        langLabel.setForeground(Color.LIGHT_GRAY);
        langLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Kopyala butonu
        JButton copyButton = new JButton("Kopyala");
        copyButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        copyButton.setBorderPainted(false);
        copyButton.setFocusPainted(false);
        copyButton.setBackground(new Color(60, 63, 65));
        copyButton.setForeground(Color.LIGHT_GRAY);
        copyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Kopyala butonu tıklama olayı
        copyButton.addActionListener(e -> {
            StringSelection selection = new StringSelection(codeBlock);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            copyButton.setText("Kopyalandı!");
            statusLabel.setText("Kod panoya kopyalandı");

            // 2 saniye sonra geri değiştir
            Timer timer = new Timer(2000, evt -> {
                copyButton.setText("Kopyala");
                statusLabel.setText("Hazır");
            });
            timer.setRepeats(false);
            timer.start();
        });

        codeHeaderPanel.add(langLabel, BorderLayout.WEST);
        codeHeaderPanel.add(copyButton, BorderLayout.EAST);

        // Kod içeriği
        JTextArea codeArea = new JTextArea();
        codeArea.setText(codeBlock);
        codeArea.setEditable(false);
        codeArea.setBackground(isDarkMode ? CODE_BLOCK_BG_DARK : CODE_BLOCK_BG);
        codeArea.setForeground(CODE_TEXT_COLOR);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        // Kaydırma çubuğu
        JScrollPane codeScrollPane = new JScrollPane(codeArea);
        codeScrollPane.setBorder(null);

        // Yüksekliği kod satırlarına göre hesapla, minimum 50px, maksimum 300px
        int lineCount = codeBlock.split("\n").length;
        codeScrollPane.setPreferredSize(new Dimension(500, Math.min(Math.max(lineCount * 18, 50), 300)));

        codePanel.add(codeHeaderPanel, BorderLayout.NORTH);
        codePanel.add(codeScrollPane, BorderLayout.CENTER);

        container.add(codePanel);
    }

    /**
     * Gemini API'sine istek gönderen ve ham yanıtı debug amaçlı gösteren metot
     */
    private static String callGeminiAPI(String prompt) throws IOException, InterruptedException {
        // API isteği için JSON oluşturma
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "topK", 40,
                        "topP", 0.95,
                        "maxOutputTokens", 4096 // Limit artırıldı
                )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // HTTP isteği oluşturma
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "?key=" + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        // API isteğini gönderme
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Yanıtı işleme
        if (response.statusCode() == 200) {
            return parseGeminiResponse(response.body());
        } else {
            return "Hata kodu: " + response.statusCode() + "\n" + response.body();
        }
    }

    private static String parseGeminiResponse(String responseJson) throws IOException {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseJson, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }

            return "Yanıt ayrıştırılamadı: " + responseJson;
        } catch (Exception e) {
            return "Yanıt ayrıştırma hatası: " + e.getMessage();
        }
    }

    /**
     * Claude API'sine istek gönderen metot
     */
    private static String callClaudeAPI(String prompt) throws IOException, InterruptedException {
        // API endpointi
        String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

        // API isteği için JSON oluşturma
        Map<String, Object> requestBody = Map.of(
                "model", "claude-3-opus-20240229", // veya 3-sonnet ya da 3-haiku kullanabilirsiniz
                "max_tokens", 4000,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        // HTTP isteği oluşturma
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CLAUDE_API_URL))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey) // Claude API anahtarınız
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        // API isteğini gönderme
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Debug amaçlı ham yanıtı kaydet
        String rawResponse = response.body();

        // Konsola yazdır
        System.out.println("--- HAM CLAUDE YANITI ---");
        System.out.println(rawResponse);
        System.out.println("--- YANIT SONU ---");

        // Yanıtı işleme
        if (response.statusCode() == 200) {
            return parseClaudeResponse(rawResponse);
        } else {
            return "Hata kodu: " + response.statusCode() + "\n" + rawResponse;
        }
    }

    /**
     * Claude API yanıtını ayrıştıran metot
     */
    private static String parseClaudeResponse(String responseJson) throws IOException {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseJson, Map.class);

            // Claude API yanıtını ayrıştır
            Map<String, Object> content = (Map<String, Object>) ((List<Map<String, Object>>) responseMap.get("content")).get(0);
            String text = (String) content.get("text");

            return text;
        } catch (Exception e) {
            return "Yanıt ayrıştırma hatası: " + e.getMessage() + "\n\nHam yanıt:\n" + responseJson;
        }
    }

    /**
     * API seçimi için tercih ayarları
     */
    private static enum AIModel {
        GEMINI,
        CLAUDE
    }

    // Kullanıcının tercih ettiği model
    private static AIModel selectedModel = AIModel.CLAUDE; // Varsayılan olarak Claude

    /**
     * Seçilen modele göre API çağrısı yapar
     */
    private static String callAIAPI(String prompt) throws IOException, InterruptedException {
        if (selectedModel == AIModel.CLAUDE) {
            return callClaudeAPI(prompt);
        } else {
            return callGeminiAPI(prompt);
        }
    }

    /**
     * API ayarları diyalog penceresini gösterir
     */
    private static void showAPISettingsDialog() {
        JDialog dialog = new JDialog(mainFrame, "API Ayarları", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(450, 350); // Biraz daha büyük
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("AI Model ve API Ayarları");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Model seçimi için radio butonları
        JRadioButton claudeButton = new JRadioButton("Claude API");
        JRadioButton geminiButton = new JRadioButton("Gemini API");

        ButtonGroup modelGroup = new ButtonGroup();
        modelGroup.add(claudeButton);
        modelGroup.add(geminiButton);

        // Varsayılan seçimi ayarla
        if (selectedModel == AIModel.CLAUDE) {
            claudeButton.setSelected(true);
        } else {
            geminiButton.setSelected(true);
        }

        claudeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        geminiButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        // API key için etiket
        JLabel apiKeyLabel = new JLabel("API Anahtarı:");
        apiKeyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // API key panel - yatay düzen için
        JPanel apiKeyPanel = new JPanel(new BorderLayout(5, 0));
        apiKeyPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 35));
        apiKeyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Şifrelenmiş alan olarak API anahtarı
        JPasswordField apiKeyField = new JPasswordField(apiKey);
        apiKeyField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Göster/Gizle düğmesi
        JToggleButton showHideButton = new JToggleButton("Göster");
        showHideButton.setPreferredSize(new Dimension(80, 35));
        showHideButton.addActionListener(e -> {
            if (showHideButton.isSelected()) {
                apiKeyField.setEchoChar((char) 0); // Göster
                showHideButton.setText("Gizle");
            } else {
                apiKeyField.setEchoChar('•'); // Gizle
                showHideButton.setText("Göster");
            }
        });

        apiKeyPanel.add(apiKeyField, BorderLayout.CENTER);
        apiKeyPanel.add(showHideButton, BorderLayout.EAST);

        // Butonlar
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton clearButton = new JButton("API Anahtarını Sil");
        JButton cancelButton = new JButton("İptal");
        JButton saveButton = new JButton("Kaydet");

        // API anahtarını silme
        clearButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(dialog,
                    "API anahtarınızı silmek istediğinizden emin misiniz?",
                    "Onay", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                apiKeyField.setText("");
                apiKey = "";
                prefs.remove("API_KEY"); // Preferences'dan da kaldır
                JOptionPane.showMessageDialog(dialog,
                        "API anahtarı başarıyla silindi.",
                        "API Anahtarı Silindi", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            // Seçilen modeli kaydet
            selectedModel = claudeButton.isSelected() ? AIModel.CLAUDE : AIModel.GEMINI;

            // API anahtarını kaydet
            apiKey = new String(apiKeyField.getPassword()); // JPasswordField'dan güvenli okuma

            // Kaydet
            prefs.put("API_KEY", apiKey);
            prefs.put("AI_MODEL", selectedModel.name());

            // Doğrulama için konsola yazdır
            System.out.println("Saved API key: " + (apiKey.isEmpty() ? "empty" : "not empty"));
            System.out.println("Saved AI model: " + selectedModel);

            JOptionPane.showMessageDialog(dialog, "Ayarlar başarıyla kaydedildi!",
                    "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        // API Anahtarı Sil düğmesini sola, diğerlerini sağa
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftButtonPanel.add(clearButton);

        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightButtonPanel.add(cancelButton);
        rightButtonPanel.add(saveButton);

        JPanel combinedButtonPanel = new JPanel(new BorderLayout());
        combinedButtonPanel.add(leftButtonPanel, BorderLayout.WEST);
        combinedButtonPanel.add(rightButtonPanel, BorderLayout.EAST);
        combinedButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tüm bileşenleri panele ekle
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(new JLabel("Kullanılacak AI modelini seçin:"));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(claudeButton);
        panel.add(geminiButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(apiKeyLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(apiKeyPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(combinedButtonPanel);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    /**
     * Oturumları kaydet
     */
    /**
     * Oturumları JSON dosyasına kaydet
     */
    private static void saveSessions() {
        try {
            // Kullanıcı ev dizininde bir klasör oluştur
            String appDir = System.getProperty("user.home") + File.separator + ".jazarichat";
            File dir = new File(appDir);
            if (!dir.exists()) {
                dir.mkdir();
            }

            // JSON dosyası oluştur
            File sessionsFile = new File(appDir + File.separator + "sessions.json");

            // Session bilgilerini içeren liste
            List<Map<String, Object>> sessionDataList = new ArrayList<>();

            for (ChatSession session : sessions) {
                // Her session için bir map oluştur
                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("name", session.getName());
                sessionData.put("creationTime", session.getCreationTime().toString());

                // Session'daki mesajlar için liste
                List<Map<String, Object>> messagesData = new ArrayList<>();

                for (ChatMessage message : session.getMessages()) {
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("role", message.getRole().name());
                    messageData.put("content", message.getContent());
                    messageData.put("timestamp", message.getTimestamp().toString());
                    messagesData.add(messageData);
                }

                sessionData.put("messages", messagesData);
                sessionDataList.add(sessionData);
            }

            // JSON'a dönüştür ve dosyaya yaz
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(sessionsFile, sessionDataList);

            System.out.println("Oturumlar başarıyla JSON dosyasına kaydedildi: " + sessionsFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Oturumlar JSON'a kaydedilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Oturumları JSON dosyasından yükle
     */
    private static void loadSessions() {
        sessions.clear();

        try {
            // Dosya yolunu belirle
            String appDir = System.getProperty("user.home") + File.separator + ".jazarichat";
            File sessionsFile = new File(appDir + File.separator + "sessions.json");

            // Dosya yoksa, yeni bir tane oluştur
            if (!sessionsFile.exists()) {
                System.out.println("Oturum dosyası bulunamadı, yeni bir oturum oluşturuluyor.");
                createNewSession();
                return;
            }

            // JSON dosyasını oku
            List<Map<String, Object>> sessionDataList = objectMapper.readValue(
                    sessionsFile,
                    new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
            });

            for (Map<String, Object> sessionData : sessionDataList) {
                String sessionName = (String) sessionData.get("name");
                String creationTimeStr = (String) sessionData.get("creationTime");

                // Zaman bilgisini parse et
                LocalDateTime creationTime = LocalDateTime.parse(creationTimeStr);

                // Yeni oturum oluştur
                ChatSession session = new ChatSession(sessionName);

                // Reflection ile oluşturma zamanını ayarla
                try {
                    java.lang.reflect.Field field = ChatSession.class.getDeclaredField("creationTime");
                    field.setAccessible(true);
                    field.set(session, creationTime);
                } catch (Exception e) {
                    System.out.println("Oluşturma zamanı ayarlanamadı: " + e.getMessage());
                }

                // Mesajları yükle
                List<Map<String, Object>> messagesData = (List<Map<String, Object>>) sessionData.get("messages");
                if (messagesData != null) {
                    for (Map<String, Object> messageData : messagesData) {
                        String roleStr = (String) messageData.get("role");
                        String content = (String) messageData.get("content");
                        String timestampStr = (String) messageData.get("timestamp");

                        // Role ve zaman bilgisini parse et
                        ChatMessage.Role role = ChatMessage.Role.valueOf(roleStr);
                        LocalDateTime timestamp = LocalDateTime.parse(timestampStr);

                        // Yeni mesaj oluştur
                        ChatMessage message = new ChatMessage(role, content);

                        // Reflection ile timestamp'i ayarla
                        try {
                            java.lang.reflect.Field field = ChatMessage.class.getDeclaredField("timestamp");
                            field.setAccessible(true);
                            field.set(message, timestamp);
                        } catch (Exception e) {
                            System.out.println("Mesaj zamanı ayarlanamadı: " + e.getMessage());
                        }

                        session.addMessage(message);
                    }
                }

                // Oturumu listeye ekle
                sessions.add(session);
            }

            System.out.println("Oturumlar başarıyla yüklendi: " + sessions.size() + " oturum bulundu.");

            // Eğer oturum varsa, ilkini mevcut oturum olarak seç
            if (!sessions.isEmpty()) {
                currentSession = sessions.get(0);

                // GUI'yi güncelle
                SwingUtilities.invokeLater(() -> {
                    refreshSessionsList();
                    chatHistory.clear();
                    chatHistory.addAll(currentSession.getMessages());
                    refreshMessages();
                });
            } else {
                // Oturum yoksa yeni bir tane oluştur
                createNewSession();
            }

        } catch (Exception e) {
            System.err.println("Oturumlar yüklenirken hata: " + e.getMessage());
            e.printStackTrace();

            // Hata durumunda yeni bir oturum oluştur
            createNewSession();
        }
    }

    /**
     * Çıkışta ayarları kaydet
     */
    private static void savePreferences() {
        // API anahtarını kaydet
        if (!apiKey.isEmpty()) {
            prefs.put("API_KEY", apiKey);
        }

        // Model seçimini kaydet
        prefs.put("AI_MODEL", selectedModel.name());

        // Tema ayarını kaydet
        prefs.putBoolean("DARK_MODE", isDarkMode);

        // Oturumları kaydet
        saveSessions();
    }

    private static void loadPreferences() {
        // API anahtarını yükle
        apiKey = prefs.get("API_KEY", "");
        System.out.println("Loaded API key: " + (apiKey.isEmpty() ? "empty" : "not empty")); // Debug için

        // AI modelini yükle
        try {
            String modelName = prefs.get("AI_MODEL", AIModel.CLAUDE.name());
            selectedModel = AIModel.valueOf(modelName);
            System.out.println("Loaded AI model: " + selectedModel); // Debug için
        } catch (Exception e) {
            selectedModel = AIModel.CLAUDE; // Varsayılan olarak Claude
            System.out.println("Error loading AI model, defaulting to: " + selectedModel);
        }

    }

}
