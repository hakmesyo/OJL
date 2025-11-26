package jazari.llm_forge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatPane extends JPanel implements Scrollable {

    private final EventLogger eventLogger;
    private final JPanel contentPanel;
    private final Map<String, MessagePanel> messageMap = new HashMap<>();
    private final Color chatBgColor = new Color(36, 36, 40);
    private JScrollPane parentScrollPane;

    // --- KIRPIŞMA ÖNLEYİCİ (THROTTLING) ---
    // Gelen güncellemeleri burada biriktireceğiz
    private final Map<String, String> pendingUpdates = new ConcurrentHashMap<>();
    private final Timer uiUpdateTimer;

    public ChatPane(EventLogger logger) {
        this.eventLogger = logger;
        setLayout(new BorderLayout());
        setBackground(chatBgColor);

        // Mesajların alt alta dizileceği ana panel
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(chatBgColor);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Wrapper
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(chatBgColor);
        wrapper.add(contentPanel, BorderLayout.NORTH);

        add(wrapper, BorderLayout.CENTER);

        // --- ZAMANLAYICIYI BAŞLAT ---
        // 50ms = Saniyede 20 kare (FPS). İnsan gözü için akıcıdır ama işlemciyi yormaz.
        uiUpdateTimer = new Timer(50, e -> processPendingUpdates());
        uiUpdateTimer.setRepeats(true);
        uiUpdateTimer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof JScrollPane) {
                parentScrollPane = (JScrollPane) parent;
                break;
            }
            parent = parent.getParent();
        }
    }

    // --- AKILLI GÜNCELLEME MEKANİZMASI ---
    private void processPendingUpdates() {
        if (pendingUpdates.isEmpty()) {
            return;
        }

        // Bekleyen güncellemeleri işle
        for (Map.Entry<String, String> entry : pendingUpdates.entrySet()) {
            String msgId = entry.getKey();
            String content = entry.getValue();

            MessagePanel panel = messageMap.get(msgId);
            if (panel != null) {
                panel.updateContent(content);
            }
        }

        // Listeyi temizle
        pendingUpdates.clear();

        // Layout'u SADECE BİR KERE güncelle
        contentPanel.revalidate();
        contentPanel.repaint();

        // Kaydırma
        scrollToBottom();
    }

    public void updateAIMessage(String messageId, String newContent) {
        // Burası çok hızlı çağrılıyor (Streaming).
        // UI işlemini doğrudan yapmak yerine havuza atıyoruz.
        // Timer bunu alıp işleyecek.
        pendingUpdates.put(messageId, newContent);
    }

    public void showWelcomeMessage() {
        JLabel welcomeTitle = new JLabel("Welcome to Jazari Chat Forge!", SwingConstants.CENTER);
        welcomeTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        welcomeTitle.setForeground(new Color(114, 137, 218));
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeText = new JLabel("Start chatting with your favorite LLM models.", SwingConstants.CENTER);
        welcomeText.setFont(new Font("Dialog", Font.PLAIN, 14));
        welcomeText.setForeground(new Color(204, 204, 204));
        welcomeText.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setOpaque(false);
        welcomePanel.setBorder(new EmptyBorder(50, 0, 20, 0));
        welcomePanel.add(welcomeTitle);
        welcomePanel.add(Box.createVerticalStrut(10));
        welcomePanel.add(welcomeText);

        contentPanel.add(welcomePanel);
        revalidate();
        repaint();
    }

    public void addUserMessage(String sender, String message) {
        addMessage(sender, message, false, "msg_" + System.currentTimeMillis());
    }

    public void addAIMessage(String sender, String message) {
        addMessage(sender, message, true, "msg_" + System.currentTimeMillis());
    }

    public void addAIMessageWithId(String sender, String message, String messageId) {
        addMessage(sender, message, true, messageId);
    }

    private void addMessage(String sender, String message, boolean isAI, String messageId) {
        MessagePanel msgPanel = new MessagePanel(sender, isAI);
        msgPanel.updateContent(message);

        messageMap.put(messageId, msgPanel);
        contentPanel.add(msgPanel);
        contentPanel.add(Box.createVerticalStrut(15));

        scrollToBottom();
        revalidate();
        repaint();
    }

    private void scrollToBottom() {
        if (parentScrollPane != null) {
            // Sadece kullanıcı en alttaysa veya çok yakınsa otomatik kaydır
            // Bu, kullanıcı yukarı okurken rahatsız edilmesini engeller
            JScrollBar vertical = parentScrollPane.getVerticalScrollBar();
            int extent = vertical.getModel().getExtent();
            int max = vertical.getMaximum();
            int value = vertical.getValue();

            // Eğer scroll en alttan 50 piksel yukarıdaysa, otomatik kaydır
            if (max - (value + extent) < 100) {
                SwingUtilities.invokeLater(() -> vertical.setValue(max));
            }
        }
    }

    // --- Scrollable Interface ---
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    // =================================================================================
    // INNER CLASS: MessagePanel
    // =================================================================================
    private class MessagePanel extends JPanel {

        private final boolean isAI;
        private final JPanel contentContainer;
        private final List<ComponentTypeWrapper> components = new ArrayList<>();

        private static class ComponentTypeWrapper {

            boolean isCode;
            JComponent component;

            ComponentTypeWrapper(boolean isCode, JComponent component) {
                this.isCode = isCode;
                this.component = component;
            }
        }

        public MessagePanel(String sender, boolean isAI) {
            this.isAI = isAI;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(0, isAI ? 0 : 50, 0, isAI ? 50 : 0));

            JLabel senderLabel = new JLabel(sender);
            senderLabel.setFont(new Font("Dialog", Font.BOLD, 12));
            senderLabel.setForeground(isAI ? new Color(227, 227, 255) : new Color(209, 255, 200));
            senderLabel.setBorder(new EmptyBorder(0, 5, 2, 0));
            add(senderLabel, BorderLayout.NORTH);

            contentContainer = new JPanel();
            contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
            contentContainer.setBackground(isAI ? new Color(59, 74, 131) : new Color(0, 92, 75));
            contentContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

            add(contentContainer, BorderLayout.CENTER);
        }

        public void updateContent(String text) {
            if (text == null || text.isEmpty()) {
                return;
            }

            String[] parts = text.split("```", -1);
            int currentPartIndex = 0;

            for (int i = 0; i < parts.length; i++) {
                boolean isCodeBlock = (i % 2 == 1);
                String partText = parts[i];

                if (!isCodeBlock && partText.isEmpty() && i != 0) {
                    continue;
                }

                if (currentPartIndex < components.size()) {
                    ComponentTypeWrapper wrapper = components.get(currentPartIndex);

                    if (wrapper.isCode == isCodeBlock) {
                        // Sadece metin değiştiyse güncelle
                        if (isCodeBlock) {
                            updateCodePanel(wrapper.component, partText);
                        } else {
                            JTextArea ta = (JTextArea) wrapper.component;
                            // Gereksiz repaint'i önlemek için kontrol
                            if (!ta.getText().equals(partText)) {
                                ta.setText(partText);
                            }
                        }
                    } else {
                        rebuildFromIndex(currentPartIndex, parts, i);
                        return;
                    }
                } else {
                    JComponent newComp;
                    if (isCodeBlock) {
                        newComp = createCodePanel(partText);
                    } else {
                        newComp = createTextComponent(partText);
                    }

                    components.add(new ComponentTypeWrapper(isCodeBlock, newComp));
                    contentContainer.add(newComp);
                }
                currentPartIndex++;
            }
        }

        private void rebuildFromIndex(int startIndex, String[] parts, int partArrayIndex) {
            while (components.size() > startIndex) {
                components.remove(components.size() - 1);
                contentContainer.remove(contentContainer.getComponentCount() - 1);
            }

            for (int i = partArrayIndex; i < parts.length; i++) {
                boolean isCode = (i % 2 == 1);
                String txt = parts[i];
                if (!isCode && txt.isEmpty()) {
                    continue;
                }

                JComponent newComp = isCode ? createCodePanel(txt) : createTextComponent(txt);
                components.add(new ComponentTypeWrapper(isCode, newComp));
                contentContainer.add(newComp);
            }
        }

        private JComponent createTextComponent(String text) {
            JTextArea textArea = new JTextArea(text);
            textArea.setFont(new Font("Dialog", Font.PLAIN, 14));
            textArea.setForeground(Color.WHITE);
            textArea.setBackground(new Color(0, 0, 0, 0));
            textArea.setOpaque(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setFocusable(true);
            textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
            return textArea;
        }

        private JPanel createCodePanel(String rawCode) {
            String language = "CODE";
            String code = rawCode;

            if (rawCode.contains("\n")) {
                String firstLine = rawCode.substring(0, rawCode.indexOf("\n")).trim();
                if (!firstLine.isEmpty() && !firstLine.contains(" ") && firstLine.length() < 20) {
                    language = firstLine.toUpperCase();
                    if (rawCode.length() > firstLine.length() + 1) {
                        code = rawCode.substring(rawCode.indexOf("\n") + 1);
                    } else {
                        code = "";
                    }
                }
            }

            JPanel codePanel = new JPanel(new BorderLayout());
            codePanel.setBackground(new Color(45, 45, 45));
            codePanel.setBorder(BorderFactory.createLineBorder(new Color(114, 137, 218), 1));
            codePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(30, 30, 30));
            header.setBorder(new EmptyBorder(2, 5, 2, 5));

            JLabel langLabel = new JLabel(language);
            langLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
            langLabel.setForeground(new Color(114, 137, 218));

            JButton copyBtn = new JButton("Copy");
            copyBtn.setFont(new Font("Dialog", Font.PLAIN, 10));
            copyBtn.setMargin(new Insets(1, 4, 1, 4));
            copyBtn.setFocusable(false);

            header.add(langLabel, BorderLayout.WEST);
            header.add(copyBtn, BorderLayout.EAST);

            JTextArea codeArea = new JTextArea(code);
            codeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
            codeArea.setForeground(new Color(200, 200, 200));
            codeArea.setBackground(new Color(45, 45, 45));
            codeArea.setBorder(new EmptyBorder(5, 5, 5, 5));
            codeArea.setEditable(false);

            copyBtn.addActionListener(e -> {
                StringSelection selection = new StringSelection(codeArea.getText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                copyBtn.setText("Copied!");
                Timer t = new Timer(1500, evt -> copyBtn.setText("Copy"));
                t.setRepeats(false);
                t.start();
            });

            codePanel.add(header, BorderLayout.NORTH);
            codePanel.add(codeArea, BorderLayout.CENTER);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setOpaque(false);
            wrapper.add(codePanel, BorderLayout.CENTER);
            wrapper.setBorder(new EmptyBorder(5, 0, 5, 0));
            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

            wrapper.putClientProperty("codeArea", codeArea);
            wrapper.putClientProperty("langLabel", langLabel);

            return wrapper;
        }

        private void updateCodePanel(JComponent wrapper, String rawCode) {
            JTextArea codeArea = (JTextArea) wrapper.getClientProperty("codeArea");
            JLabel langLabel = (JLabel) wrapper.getClientProperty("langLabel");

            if (codeArea == null) {
                return;
            }

            String language = "CODE";
            String code = rawCode;

            if (rawCode.contains("\n")) {
                String firstLine = rawCode.substring(0, rawCode.indexOf("\n")).trim();
                if (!firstLine.isEmpty() && !firstLine.contains(" ") && firstLine.length() < 20) {
                    language = firstLine.toUpperCase();
                    if (rawCode.length() > firstLine.length() + 1) {
                        code = rawCode.substring(rawCode.indexOf("\n") + 1);
                    } else {
                        code = "";
                    }
                }
            }

            if (!codeArea.getText().equals(code)) {
                codeArea.setText(code);
            }
            if (!langLabel.getText().equals(language)) {
                langLabel.setText(language);
            }
        }
    }
}
