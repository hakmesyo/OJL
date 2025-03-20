package jazari.llm;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * Chat görüntüleme ve mesaj işleme için özel panel
 */
public class ChatPane extends JEditorPane {

    private final EventLogger eventLogger;
    private final HTMLEditorKit htmlKit;
    private final Color chatBgColor = new Color(36, 36, 40);
    private final Color userBubbleColor = new Color(0, 132, 87); // WhatsApp yeşil
    private final Color aiBubbleColor = new Color(59, 74, 131);  // WhatsApp mavi

    public ChatPane(EventLogger logger) {
        this.eventLogger = logger;

        // HTML içeriği olarak ayarla
        setContentType("text/html");
        setEditable(false);
        setBackground(chatBgColor);

        // Yazı tipi ayarları
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setFont(new Font("Dialog", Font.PLAIN, 14));

        // HTML Kit'i ayarla
        htmlKit = new HTMLEditorKit();
        setEditorKit(htmlKit);

        // CSS Stilleri - daha güvenilir hizalama ve sarma için
        String css = "body { font-family: Dialog; font-size: 14pt; color: #cccccc; "
                + "background-color: #24242c; margin: 10px; }\n"
                + "a { color: #7289da; text-decoration: none; }\n"
                + "a:hover { text-decoration: underline; }\n"
                + ".message-container { margin-top: 10px; margin-bottom: 20px; position: relative; }\n"
                + ".user-message { margin-right: 100px; margin-left: 10px; }\n"
                + ".ai-message { margin-left: 100px; margin-right: 10px; }\n"
                + ".message-bubble { padding: 10px; border-radius: 10px; box-shadow: 0 1px 2px rgba(0,0,0,0.2); position: relative; }\n"
                + ".user-bubble { background-color: #005c4b; color: white; }\n"
                + ".ai-bubble { background-color: #3b4a83; color: white; }\n"
                + ".sender { font-weight: bold; margin-bottom: 8px; font-size: 0.9em; }\n"
                + ".user-sender { color: #d1ffc8; }\n"
                + ".ai-sender { color: #e3e3ff; }\n"
                + ".message-content { white-space: normal !important; word-wrap: break-word !important; text-align: left !important; width: 100% !important; display: block !important; }\n"
                + ".user-content { color: #ffffff; }\n"
                + ".ai-content { color: #ffffff; }\n"
                + "p { margin: 0; padding: 0; text-align: left !important; }\n"
                + ".copy-button { position: absolute; top: 5px; right: 5px; font-size: 0.9em; "
                + "background-color: rgba(255,255,255,0.15); border-radius: 4px; "
                + "padding: 3px 8px; cursor: pointer; color: rgba(255,255,255,0.7); }\n"
                + ".copy-feedback { background-color: #43b581 !important; color: #ffffff !important; }\n"
                + ".code-block { position: relative; margin: 10px 0; padding: 10px; "
                + "background-color: #2d2d2d; border-radius: 5px; border-left: 3px solid #7289da; }\n"
                + ".code-header { display: flex; justify-content: space-between; margin-bottom: 8px; }\n"
                + ".code-language { color: #7289da; font-weight: bold; font-size: 0.8em; }\n"
                + ".code-content { font-family: monospace; white-space: pre; color: #cccccc; margin: 0; overflow-x: auto; }";

        htmlKit.getStyleSheet().addRule(css);

        // Hyperlink olaylarını dinle
        addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String url = e.getDescription();
                handleHyperlinkAction(url);
            }
        });
    }

    /**
     * Hoş geldiniz mesajını göster
     */
    public void showWelcomeMessage() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<div style='text-align:center; margin-top:100px;'>");
        html.append("<h2 style='color:#7289da;'>Hoş Geldiniz!</h2>");
        html.append("<p>Gemma 3 AI modeli ile sohbet etmeye başlayın.</p>");
        html.append("<p style='color:#999999; font-size:12pt;'>Mesajınızı aşağıdaki metin kutusuna yazın ve Gönder düğmesine tıklayın.</p>");
        html.append("</div>");
        html.append("</body></html>");

        setText(html.toString());
    }

    /**
     * Kullanıcı mesajını ekle
     */
    public void addUserMessage(String sender, String message) {
        appendMessageToHTML(sender, message, false);
    }

    /**
     * AI mesajını ekle
     */
    public void addAIMessage(String sender, String message) {
        appendMessageToHTML(sender, message, true);
    }

    /**
     * Mesajı HTML içeriğine ekle - WhatsApp tarzı zikzak görünüm
     */
    private void appendMessageToHTML(String sender, String message, boolean isAI) {
        eventLogger.log("Mesaj ekleniyor: Gönderen=" + sender + ", AI mi=" + isAI);

        // Mevcut HTML içeriğini al
        String currentContent = getText();

        // <body> etiketinin kapanışını bul
        int bodyEndIndex = currentContent.lastIndexOf("</body>");
        if (bodyEndIndex == -1) {
            // HTML yapısı beklenen şekilde değilse, yeni bir HTML yapısı oluştur
            currentContent = "<html><body></body></html>";
            bodyEndIndex = currentContent.lastIndexOf("</body>");
        }

        // Benzersiz mesaj ID'si oluştur
        String messageId = "msg_" + System.currentTimeMillis();

        // Mesaj HTML'ini oluştur
        StringBuilder messageHtml = new StringBuilder();

        // Daha basit ve doğrudan HTML yapısı - WhatsApp benzeri görünüm
        if (isAI) {
            // AI mesajı - sağda
            messageHtml.append("<div style='margin: 15px 10px 15px 100px;'>");
            messageHtml.append("<div style='background-color: rgba(59, 74, 131, 0.8); padding: 10px; border-radius: 10px; position: relative;'>");
        } else {
            // Kullanıcı mesajı - solda
            messageHtml.append("<div style='margin: 15px 100px 15px 10px;'>");
            messageHtml.append("<div style='background-color: rgba(0, 92, 75, 0.8); padding: 10px; border-radius: 10px; position: relative;'>");
        }

        // Gönderen bilgisi
        messageHtml.append("<div style='font-weight: bold; color: white; margin-bottom: 8px;'>");
        messageHtml.append(isAI ? sender : "Sen");
        messageHtml.append("</div>");

        // Mesaj içeriği
        // Kod içeriği mi kontrol et
        boolean containsCode = isAI && (message.contains("```")
                || message.contains("public class")
                || message.contains("function")
                || message.contains("def ")
                || message.contains("import "));

        if (containsCode) {
            messageHtml.append("<div style='color: white;'>");
            messageHtml.append(processCodeBlocks(message, messageId));
            messageHtml.append("</div>");
        } else {
            // Normal metin içeriği - ID'yi çift tırnak kullanıyoruz
            messageHtml.append("<div id=\"").append(messageId).append("\" style='color: white; white-space: pre-wrap; text-align: left;'>");
            messageHtml.append(escapeHtml(message));
            messageHtml.append("</div>");
        }

        // Baloncuk ve konteyner div'leri kapat
        messageHtml.append("</div></div>");

        // HTML'e mesajı ekle
        StringBuilder newContent = new StringBuilder(currentContent);
        newContent.insert(bodyEndIndex, messageHtml.toString());

        // Güncellenmiş HTML'i ayarla
        setText(newContent.toString());

        // Kaydırmayı en alta ayarla
        SwingUtilities.invokeLater(() -> {
            setCaretPosition(getDocument().getLength());
            scrollRectToVisible(new Rectangle(0, getHeight() - 1, 1, 1));
        });

        eventLogger.log("Mesaj eklendi, ID: " + messageId);
    }

    /**
     * Kod bloklarını işle ve formatlı HTML oluştur
     */
    private String processCodeBlocks(String message, String parentMessageId) {
        StringBuilder result = new StringBuilder();

        if (message.contains("```")) {
            String[] parts = message.split("```");

            for (int i = 0; i < parts.length; i++) {
                if (i % 2 == 0) {
                    // Normal metin
                    result.append(escapeHtml(parts[i]));
                } else {
                    // Kod bloğu
                    String codeContent = parts[i].trim();
                    String language = "";

                    // Dil belirteci varsa ayır
                    if (codeContent.contains("\n")) {
                        String firstLine = codeContent.substring(0, codeContent.indexOf("\n")).trim();
                        if (!firstLine.contains(" ")) {
                            language = firstLine;
                            codeContent = codeContent.substring(codeContent.indexOf("\n") + 1);
                        }
                    }

                    String codeBlockId = "code_" + parentMessageId + "_" + i;

                    // Kod bloğu HTML'i
                    result.append("<div class='code-block'>");

                    // Başlık çubuğu
                    result.append("<div class='code-header'>");
                    if (!language.isEmpty()) {
                        result.append("<div class='code-language'>").append(language.toUpperCase()).append("</div>");
                    } else {
                        result.append("<div class='code-language'>KOD</div>");
                    }

                    // Kopyalama butonu
                    result.append("<div>");
                    result.append("<a href='copycode:").append(codeBlockId).append("' style='text-decoration:none;'>");
                    result.append("<span id=\"btn_").append(codeBlockId).append("\" class='copy-button'>📋 Copy</span>");
                    result.append("</a>");
                    result.append("</div>");

                    result.append("</div>"); // header kapatma

                    // Kod içeriği - çift tırnak kullanıyoruz
                    result.append("<pre><code id=\"").append(codeBlockId).append("\" class='code-content'>");
                    result.append(escapeHtml(codeContent));
                    result.append("</code></pre>");
                    result.append("</div>"); // code-block kapatma
                }
            }
        } else {
            // Kod bloğu olmayan durumda
            result.append(escapeHtml(message));
        }

        return result.toString();
    }

    /**
     * Hyperlink eylemlerini işle
     */
    private void handleHyperlinkAction(String url) {
        eventLogger.log("Hyperlink tıklandı: " + url);

        try {
            if (url.startsWith("copycode:")) {
                String codeBlockId = url.substring(9);
                copyFormattedCodeToClipboard(codeBlockId);
            }
        } catch (Exception e) {
            eventLogger.log("Hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Formatlı kodu panoya kopyala
     */
    private void copyFormattedCodeToClipboard(String codeBlockId) {
        try {
            // HTML içeriği al
            String htmlText = getText();

            // Kod bloğu ID'sini bul
            int idIndex = htmlText.indexOf("id=\"" + codeBlockId + "\"");
            if (idIndex == -1) {
                eventLogger.log("Kod bloğu bulunamadı: " + codeBlockId);
                return;
            }

            // <code> elementinin başlangıcını bul
            int codeStart = htmlText.indexOf(">", idIndex) + 1;
            // </code> etiketini bul
            int codeEnd = htmlText.indexOf("</code>", codeStart);

            if (codeStart > 0 && codeEnd > codeStart) {
                // Kod içeriğini al
                String rawHtml = htmlText.substring(codeStart, codeEnd);

                // HTML ve Unicode karakterlerini çöz
                String codeText = unescapeHtml(rawHtml);

                // Panoya kopyala - formatlı metin olarak
                StringSelection selection = new StringSelection(codeText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

                // Kopyalama geri bildirimi göster
                showCopyFeedback(codeBlockId);

                eventLogger.log("Kod formatlı olarak kopyalandı");
            } else {
                eventLogger.log("Kod içeriği bulunamadı");
            }
        } catch (Exception e) {
            eventLogger.log("Kod kopyalama hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kopyalama işlemi geri bildirimi göster
     */
    private void showCopyFeedback(String id) {
        try {
            // Butonu güncelle (geri bildirim için)
            String htmlContent = getText();
            String buttonId = "id=\"btn_" + id + "\"";

            int buttonStartIndex = htmlContent.indexOf(buttonId);
            if (buttonStartIndex != -1) {
                int buttonTagStart = htmlContent.lastIndexOf("<span", buttonStartIndex);
                int buttonTagEnd = htmlContent.indexOf(">", buttonStartIndex) + 1;
                int buttonContentEnd = htmlContent.indexOf("</span>", buttonTagEnd);

                if (buttonTagStart != -1 && buttonTagEnd != -1 && buttonContentEnd != -1) {
                    // Buton HTML'ini güncelle - ✓ ve Copied olacak
                    String buttonPrefix = htmlContent.substring(buttonTagStart, buttonTagEnd);
                    String updatedButton = buttonPrefix.replace("copy-button", "copy-button copy-feedback") + "✓ Copied";

                    StringBuilder updatedContent = new StringBuilder(htmlContent);
                    updatedContent.replace(buttonTagStart, buttonContentEnd, updatedButton);

                    setText(updatedContent.toString());

                    // 2 saniye sonra butonu eski haline getir
                    Timer timer = new Timer(2000, e -> {
                        try {
                            String currentHtml = getText();
                            int currentButtonStart = currentHtml.indexOf(buttonId);

                            if (currentButtonStart != -1) {
                                int currentTagStart = currentHtml.lastIndexOf("<span", currentButtonStart);
                                int currentTagEnd = currentHtml.indexOf(">", currentButtonStart) + 1;
                                int currentContentEnd = currentHtml.indexOf("</span>", currentTagEnd);

                                if (currentTagStart != -1 && currentTagEnd != -1 && currentContentEnd != -1) {
                                    String originalButton = buttonPrefix.replace("copy-feedback", "") + "📋 Copy";

                                    StringBuilder restoredContent = new StringBuilder(currentHtml);
                                    restoredContent.replace(currentTagStart, currentContentEnd, originalButton);

                                    setText(restoredContent.toString());
                                }
                            }
                        } catch (Exception ex) {
                            eventLogger.log("Buton geri yükleme hatası: " + ex.getMessage());
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }

            // Kullanıcıya kopyalama bildirimi göster
            JWindow popup = new JWindow();
            JLabel label = new JLabel("   Formatted code copied!   ");
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Dialog", Font.BOLD, 12));
            label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            popup.getContentPane().add(label);
            popup.getContentPane().setBackground(new Color(67, 181, 129)); // Yeşil
            popup.pack();

            // Popup pozisyonu - mevcut pencereye göre
            Point p = this.getLocationOnScreen();
            int x = p.x + this.getWidth() - popup.getWidth() - 20;
            int y = p.y + 20;
            popup.setLocation(x, y);

            // Popup göster ve 1.5 saniye sonra kapat
            popup.setVisible(true);
            Timer timer = new Timer(1500, e -> popup.dispose());
            timer.setRepeats(false);
            timer.start();

        } catch (Exception e) {
            eventLogger.log("Feedback gösterme hatası: " + e.getMessage());
        }
    }

    /**
     * HTML metni için özel karakterleri kaçır
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>")
                .replace("  ", "&nbsp;&nbsp;");
    }

    /**
     * HTML kaçış karakterlerini geri çevir
     */
    private String unescapeHtml(String text) {
        if (text == null) {
            return "";
        }

        // HTML karakterleri
        text = text.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")
                .replace("&#160;", " ");

        // Türkçe karakterler
        text = text.replace("&#287;", "ğ")
                .replace("&#305;", "ı")
                .replace("&#351;", "ş")
                .replace("&#246;", "ö")
                .replace("&#252;", "ü")
                .replace("&#231;", "ç")
                .replace("&#304;", "İ")
                .replace("&#350;", "Ş")
                .replace("&#286;", "Ğ")
                .replace("&#220;", "Ü")
                .replace("&#214;", "Ö")
                .replace("&#199;", "Ç");

        // HTML satır sonları
        text = text.replace("<br>", "\n")
                .replace("<br/>", "\n");

        // Unicode emoji temizleme (&#55357;&#56523; gibi)
        text = text.replaceAll("&#\\d+;", "");

        return text;
    }

    /**
     * Olay testi için test içeriği ekle
     */
    public void performEventTest() {
        eventLogger.log("Olay testi başlatılıyor...");

        StringBuilder testHtml = new StringBuilder();
        testHtml.append("<html><body style='font-family:Dialog; color:#ffffff; background-color:#36393f;'>");
        testHtml.append("<div style='margin: 20px;'>");
        testHtml.append("<h3 style='color:#7289da;'>Test İçeriği</h3>");
        testHtml.append("<p>Test mesajları oluşturuldu.</p>");

        // Test ID'leri
        String testMessageId = "test_id_" + System.currentTimeMillis();

        // Test mesajı - WhatsApp tarzı
        testHtml.append("<div style='position: relative; margin: 20px 100px 20px 10px;'>");
        testHtml.append("<div style='background-color: #005c4b; padding: 10px; border-radius: 10px; position: relative;'>");
        testHtml.append("<div id=\"").append(testMessageId).append("\">Bu bir test mesajıdır.</div>");
        testHtml.append("</div>");
        testHtml.append("</div>");

        testHtml.append("</div>");
        testHtml.append("</body></html>");

        setText(testHtml.toString());
        eventLogger.log("Test içeriği yüklendi");
    }

    /**
     * Belirli bir ID ile AI mesajı ekle (streaming için)
     */
    public void addAIMessageWithId(String sender, String message, String messageId) {
        eventLogger.log("ID ile mesaj ekleniyor: Gönderen=" + sender + ", ID=" + messageId);

        // Mevcut HTML içeriğini al
        String currentContent = getText();

        // <body> etiketinin kapanışını bul
        int bodyEndIndex = currentContent.lastIndexOf("</body>");
        if (bodyEndIndex == -1) {
            // HTML yapısı beklenen şekilde değilse, yeni bir HTML yapısı oluştur
            currentContent = "<html><body></body></html>";
            bodyEndIndex = currentContent.lastIndexOf("</body>");
        }

        // Mesaj HTML'ini oluştur - tablo kullanarak daha güvenilir hizalama
        StringBuilder messageHtml = new StringBuilder();

        // AI mesajı - sağda, tablo yapısıyla
        messageHtml.append("<div class='message-container ai-message'>");
        messageHtml.append("<div class='message-bubble ai-bubble'>");

        // Gönderen bilgisi
        messageHtml.append("<table width='100%' cellspacing='0' cellpadding='0' border='0'>");
        messageHtml.append("<tr>");

        // Gönderen (sol hücre)
        messageHtml.append("<td align='left'>");
        messageHtml.append("<span class='sender ai-sender'>").append(sender).append("</span>");
        messageHtml.append("</td>");

        messageHtml.append("</tr>");
        messageHtml.append("</table>");

        // Mesaj içeriği - tabloda tek satır, tam genişlik
        messageHtml.append("<table width='100%' cellspacing='0' cellpadding='0' border='0'>");
        messageHtml.append("<tr>");
        messageHtml.append("<td align='left' style='word-wrap: break-word;'>");

        // Her paragrafı ayrı bir <p> içine koyalım
        messageHtml.append("<div id=\"").append(messageId).append("\" class='message-content ai-content'>");

        // Mesaj içeriğini paragraf olarak formatlayın
        String processedMessage = processMessageContent(message);
        messageHtml.append(processedMessage);

        messageHtml.append("</div>");
        messageHtml.append("</td>");
        messageHtml.append("</tr>");
        messageHtml.append("</table>");

        // Kapanış div'leri
        messageHtml.append("</div></div>");

        // HTML'e mesajı ekle
        StringBuilder newContent = new StringBuilder(currentContent);
        newContent.insert(bodyEndIndex, messageHtml.toString());

        // Güncellenmiş HTML'i ayarla
        setText(newContent.toString());

        // Kaydırmayı en alta ayarla
        SwingUtilities.invokeLater(() -> {
            setCaretPosition(getDocument().getLength());
            scrollRectToVisible(new Rectangle(0, getHeight() - 1, 1, 1));
        });

        eventLogger.log("ID ile mesaj eklendi: " + messageId);
    }

    // Mesaj içeriğini paragraf olarak formatlama metodu
    private String processMessageContent(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        // Kod bloğu varsa özel işle
        if (message.contains("```") || message.contains("public class")
                || message.contains("function") || message.contains("def ")
                || message.contains("import ")) {
            return processCodeBlocks(message, "msg_" + System.currentTimeMillis());
        }

        // Normal metin işleme - her satırı paragraf yap
        String[] lines = message.split("\n");
        StringBuilder formattedMessage = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                // Boş satır için boş paragraf
                formattedMessage.append("<p>&nbsp;</p>");
            } else {
                // Her satırı bir paragraf içine al
                formattedMessage.append("<p align='left'>").append(escapeHtml(line)).append("</p>");
            }
        }

        return formattedMessage.toString();
    }

    /**
     * AI tarafından gönderilen son mesajı günceller (streaming için)
     */
    public void updateLastAIMessage(String sender, String newContent) {
        try {
            // HTML içeriğini al
            String htmlContent = getText();
            Document doc = getDocument();

            // HTML belgesini incele
            if (doc instanceof HTMLDocument) {
                HTMLDocument htmlDoc = (HTMLDocument) doc;

                // AI mesaj div'lerini bul (sağdaki mavi baloncuklar)
                Element[] divs = findElementsByStyleClass(htmlDoc, HTML.Tag.DIV, "ai-bubble");

                if (divs != null && divs.length > 0) {
                    // En son AI div'i (son mesaj)
                    Element lastAIMessageDiv = divs[divs.length - 1];

                    // İçerik div'ini bul (message-content sınıfı)
                    Element[] contentDivs = findElementsByStyleClass(htmlDoc,
                            HTML.Tag.DIV, "message-content", lastAIMessageDiv);

                    if (contentDivs != null && contentDivs.length > 0) {
                        Element contentDiv = contentDivs[0];

                        // İçeriği güncelle
                        htmlDoc.setInnerHTML(contentDiv, formatMessageContent(newContent));

                        // Kaydırmayı en alta ayarla
                        setCaretPosition(getDocument().getLength());
                        scrollRectToVisible(new Rectangle(0, getHeight() - 1, 1, 1));
                        return;
                    }
                }

                // Elementleri bulamazsak, yeni bir mesaj ekle
                addAIMessage(sender, newContent);
            }
        } catch (Exception e) {
            eventLogger.log("Son mesaj güncellenirken hata: " + e.getMessage());

            // Hata durumunda yeni mesaj ekle
            addAIMessage(sender, newContent);
        }
    }

    /**
     * HTML belgesinde belirli tag ve sınıfa sahip elementleri bul
     */
    private Element[] findElementsByStyleClass(HTMLDocument doc, HTML.Tag tag,
            String styleClass) {
        return findElementsByStyleClass(doc, tag, styleClass, doc.getDefaultRootElement());
    }

    /**
     * Belirli bir parent element içinde belirli tag ve sınıfa sahip elementleri
     * bul
     */
    private Element[] findElementsByStyleClass(HTMLDocument doc, HTML.Tag tag,
            String styleClass, Element parent) {
        List<Element> matchingElements = new java.util.ArrayList<>();

        int count = parent.getElementCount();
        for (int i = 0; i < count; i++) {
            Element element = parent.getElement(i);

            // Element attribute'larını kontrol et
            AttributeSet attrs = element.getAttributes();
            if (attrs.getAttribute(HTML.Attribute.CLASS) != null
                    && attrs.getAttribute(HTML.Attribute.CLASS).toString().contains(styleClass)
                    && attrs.getAttribute(javax.swing.text.StyleConstants.NameAttribute) == tag) {
                matchingElements.add(element);
            }

            // Alt elemanları kontrol et
            if (element.getElementCount() > 0) {
                Element[] childMatches = findElementsByStyleClass(doc, tag, styleClass, element);
                if (childMatches != null && childMatches.length > 0) {
                    matchingElements.addAll(java.util.Arrays.asList(childMatches));
                }
            }
        }

        return matchingElements.toArray(new Element[0]);
    }

    /**
     * Mesaj içeriğini HTML olarak formatlayarak döndürür
     */
    private String formatMessageContent(String content) {
        // Kod bloklarını kontrol et
        if (content.contains("```") || content.contains("public class")
                || content.contains("function") || content.contains("def ")
                || content.contains("import ")) {
            return processCodeBlocks(content, "msg_" + System.currentTimeMillis());
        } else {
            return escapeHtml(content);
        }
    }

    /**
     * Belirli ID'ye sahip AI mesajını güncelle
     */
    public void updateAIMessage(String messageId, String newContent) {
        try {
            // HTML içeriğini al
            Document doc = getDocument();

            if (doc instanceof HTMLDocument) {
                HTMLDocument htmlDoc = (HTMLDocument) doc;

                // HTML elementini ID'ye göre bul
                Element element = htmlDoc.getElement(messageId);

                if (element != null) {
                    // Formatlanmış içerik oluştur
                    String formattedContent = processMessageContent(newContent);

                    try {
                        // Element içeriğini güncelle - daha güvenli yaklaşım
                        SwingUtilities.invokeLater(() -> {
                            try {
                                htmlDoc.setInnerHTML(element, formattedContent);

                                // Kaydırmayı en alta ayarla
                                setCaretPosition(getDocument().getLength());
                                scrollRectToVisible(new Rectangle(0, getHeight() - 1, 1, 1));
                            } catch (Exception ex) {
                                eventLogger.log("HTML güncellerken iç hata: " + ex.getMessage());
                                // Alternatif yaklaşım - tamamen yeni mesaj ekle
                                addAIMessage("Gemma 3", newContent);
                            }
                        });
                    } catch (Exception e) {
                        // Hata durumunda tamamen yeni bir mesaj eklemeyi dene
                        eventLogger.log("setInnerHTML hatası: " + e.getMessage());
                        addAIMessage("Gemma 3", newContent);
                    }
                } else {
                    // Element bulunamadı, loglama yap ve yeni mesaj ekle
                    eventLogger.log("Mesaj bulunamadı, ID: " + messageId);
                    addAIMessage("Gemma 3", newContent);
                }
            }
        } catch (Exception e) {
            eventLogger.log("Mesaj güncellenirken hata: " + e.getMessage());
            e.printStackTrace();

            // Hata durumunda tamamen yeni bir mesaj ekle
            addAIMessage("Gemma 3", newContent);
        }
    }
}
