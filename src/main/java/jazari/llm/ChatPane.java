package jazari.llm;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

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

        // CSS Stilleri - WhatsApp benzeri zikzak görünüm
        String css = "body { font-family: Dialog; font-size: 14pt; color: #cccccc; "
                + "background-color: #24242c; margin: 10px; }\n"
                + "a { color: #7289da; text-decoration: none; }\n"
                + "a:hover { text-decoration: underline; }\n"
                + ".message-container { margin-top: 10px; margin-bottom: 20px; position: relative; }\n"
                + ".user-message { margin-right: 100px; margin-left: 10px; }\n"
                + // Kullanıcı mesajı solda
                ".ai-message { margin-left: 100px; margin-right: 10px; }\n"
                + // AI mesajı sağda
                ".message-bubble { padding: 10px; border-radius: 10px; box-shadow: 0 1px 2px rgba(0,0,0,0.2); position: relative; }\n"
                + ".user-bubble { background-color: #005c4b; color: white; }\n"
                + // WhatsApp yeşili
                ".ai-bubble { background-color: #3b4a83; color: white; }\n"
                + // WhatsApp mavisi
                ".sender { font-weight: bold; margin-bottom: 8px; font-size: 0.9em; }\n"
                + ".user-sender { color: #d1ffc8; }\n"
                + ".ai-sender { color: #e3e3ff; }\n"
                + ".message-content { white-space: pre-wrap; }\n"
                + ".user-content { color: #ffffff; }\n"
                + ".ai-content { color: #ffffff; }\n"
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

        // Gönderen bilgisi ve kopyalama butonu yan yana - justify-content: space-between kullanarak
        messageHtml.append("<div style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;'>");

        // Gönderen bilgisi - solda
        messageHtml.append("<div style='font-weight: bold; color: white;'>");
        messageHtml.append(isAI ? sender : "Sen");
        messageHtml.append("</div>");

        // Kopyalama butonu - en sağda
        messageHtml.append("<a href='copy:").append(messageId).append("' style='text-decoration: none;'>");
        messageHtml.append("<span id=\"btn_").append(messageId).append("\" ");
        messageHtml.append("style='font-size: 10pt; padding: 2px 6px; ");
        messageHtml.append("background-color: rgba(255,255,255,0.2); border-radius: 4px; color: white;'>");
        messageHtml.append("📋 Copy</span></a>");

        messageHtml.append("</div>"); // Başlık satırını kapat

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
            messageHtml.append("<div id=\"").append(messageId).append("\" style='color: white; white-space: pre-wrap;'>");
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
                    result.append("<div>");
                    // Kopyalama butonu için "Copy" metni eklendi
                    result.append("<a href='copycode:").append(codeBlockId).append("' data-id='")
                            .append(codeBlockId).append("' style='text-decoration:none;'>");
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
            if (url.startsWith("copy:")) {
                String messageId = url.substring(5);
                copyToClipboard(messageId);
            } else if (url.startsWith("copycode:")) {
                String codeBlockId = url.substring(9);
                copyCodeToClipboard(codeBlockId);
            } else if (url.startsWith("retry:")) {
                String messageToRetry = url.substring(6);
                messageToRetry = unescapeHtml(messageToRetry);
                retryMessage(messageToRetry);
            } else {
                eventLogger.log("Bilinmeyen URL formatı: " + url);
            }
        } catch (Exception e) {
            eventLogger.log("Hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Metin ID'sine göre panoya kopyala
     */
    private void copyToClipboard(String messageId) {
        eventLogger.log("Panoya kopyalama başladı, ID: " + messageId);

        try {
            // HTML içeriğini al
            String htmlContent = getText();

            // Element ID'sini bul - ÖNEMLİ: Çift tırnak kullanıyoruz
            String idAttribute = "id=\"" + messageId + "\"";
            int elementStartIndex = htmlContent.indexOf(idAttribute);

            if (elementStartIndex != -1) {
                // Element içeriğini bul
                int contentStartIndex = htmlContent.indexOf(">", elementStartIndex) + 1;
                int contentEndIndex = htmlContent.indexOf("</div>", contentStartIndex);

                if (contentStartIndex > 0 && contentEndIndex > contentStartIndex) {
                    // İçeriği al ve HTML formatından temizle
                    String content = htmlContent.substring(contentStartIndex, contentEndIndex);
                    String plainText = unescapeHtml(content);

                    // Panoya kopyala
                    StringSelection selection = new StringSelection(plainText);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

                    // Kullanıcıya görsel bildirim göster
                    showCopyFeedback(messageId);

                    eventLogger.log("Metin başarıyla kopyalandı");
                } else {
                    eventLogger.log("Metin içeriği bulunamadı: startIndex=" + contentStartIndex + ", endIndex=" + contentEndIndex);
                }
            } else {
                // HTML içeriğini günlüğe ekle (hata ayıklama için)
                eventLogger.log("Element ID bulunamadı: " + messageId);
                eventLogger.log("HTML içeriği (ilk 500 karakter): " + htmlContent.substring(0, Math.min(500, htmlContent.length())));
            }
        } catch (Exception e) {
            eventLogger.log("Kopyalama hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kod bloğunu panoya kopyala
     */
    private void copyCodeToClipboard(String codeBlockId) {
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

                // HTML karakterlerini temizle, ham metne çevir
                String plainCode = rawHtml;

                // Aşama 1: HTML elementlerini temizle (eğer varsa)
                plainCode = plainCode.replaceAll("<[^>]*>", "");

                // Aşama 2: HTML karakter referanslarını düz karakterlere çevir
                plainCode = plainCode.replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&amp;", "&")
                        .replace("&quot;", "\"")
                        .replace("&#39;", "'")
                        .replace("&nbsp;", " ")
                        .replace("&#160;", " ");

                // Aşama 3: Türkçe karakterleri düzelt
                plainCode = plainCode.replace("&#287;", "ğ")
                        .replace("&#305;", "ı")
                        .replace("&#351;", "ş")
                        .replace("&#246;", "ö")
                        .replace("&#252;", "ü")
                        .replace("&#231;", "ç")
                        .replace("&#304;", "İ");

                // Son olarak, herhangi bir fazlalığı kaldır (span etiketleri, vs.)
                plainCode = plainCode.replaceAll("Copy\\s*</span>.*$", "").trim();

                // Eğer hala sorun varsa, Java sınıfı yapısını koruyarak sonuçları oluşturmayı dene
                if (plainCode.contains("public class")) {
                    // Bir Java sınıfı ile başlıyorsa, düzgün biçimlendir
                    plainCode = formatJavaCode(plainCode);
                }

                // Panoya kopyala
                StringSelection selection = new StringSelection(plainCode);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

                // Geri bildirim
                showCopyFeedback(codeBlockId);

                eventLogger.log("Kod kopyalandı");
            } else {
                eventLogger.log("Kod içeriği bulunamadı");
            }
        } catch (Exception e) {
            eventLogger.log("Kod kopyalama hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

// Java kodunu biçimlendirme (gerekirse)
    private String formatJavaCode(String messyCode) {
        try {
            StringBuilder formattedCode = new StringBuilder();
            String[] lines = messyCode.split("\n");

            for (String line : lines) {
                // Fazla boşlukları temizle
                line = line.replaceAll("\\s+", " ").trim();

                // Açılış ve kapanış parantezleri için satır sonu ekle
                if (line.contains("{")) {
                    line = line.replace("{", " {");
                }

                // Temiz satırı ekle
                formattedCode.append(line).append("\n");
            }

            return formattedCode.toString();
        } catch (Exception e) {
            return messyCode; // Hata durumunda orijinal kodu döndür
        }
    }
// HTML'den kod içeriğini çıkarma yardımcı metodu

    private String extractCodeContent(String codeBlockId) {
        try {
            String htmlContent = getText();
            String idAttribute = "id=\"" + codeBlockId + "\"";
            int startIndex = htmlContent.indexOf(idAttribute);

            if (startIndex != -1) {
                // Kod bloğunun içeriğini bul
                int contentStart = htmlContent.indexOf(">", startIndex) + 1;
                int contentEnd = htmlContent.indexOf("</code>", contentStart);

                if (contentStart > 0 && contentEnd > contentStart) {
                    String content = htmlContent.substring(contentStart, contentEnd);

                    // HTML karakterlerini temizle
                    content = content.replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("&amp;", "&")
                            .replace("&quot;", "\"")
                            .replace("&#39;", "'")
                            .replace("&#160;", " ")
                            .replace("&nbsp;", " ")
                            .replace("<br>", "\n");

                    // Türkçe karakterler
                    content = content.replace("&#287;", "ğ")
                            .replace("&#305;", "ı")
                            .replace("&#351;", "ş")
                            .replace("&#246;", "ö")
                            .replace("&#252;", "ü")
                            .replace("&#231;", "ç")
                            .replace("&#304;", "İ");

                    return content;
                }
            }
            return "";
        } catch (Exception e) {
            eventLogger.log("Kod çıkarma hatası: " + e.getMessage());
            return "";
        }
    }

    /**
     * Kopyalama butonunu güncelle - tik işareti ve rengi değiştir
     */
    private void updateCopyButton(String id, boolean isMessage) {
        try {
            // HTML içeriğini al
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
        } catch (Exception e) {
            eventLogger.log("Buton güncelleme hatası: " + e.getMessage());
        }
    }

    /**
     * Kopyalama işlemi geri bildirimi göster - geçici popup
     */
    private void showCopyFeedback(String id) {
        // Butonu güncelle
        updateCopyButton(id, true);

        // Geçici popup mesajı
        JWindow popup = new JWindow();
        JLabel label = new JLabel("   Copied to clipboard!   ");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Dialog", Font.BOLD, 12));
        label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        popup.getContentPane().add(label);
        popup.getContentPane().setBackground(new Color(67, 181, 129)); // Yeşil
        popup.pack();

        // Popup pozisyonu - mevcut pencereye göre
        try {
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
            eventLogger.log("Popup gösterme hatası: " + e.getMessage());
        }
    }

    /**
     * Mesajı tekrar işle
     */
    private void retryMessage(String originalMessage) {
        eventLogger.log("Mesaj tekrar isteniyor: " + originalMessage);
        // Bu metod OllamaGemma3SwingChat sınıfında işlenecek
        firePropertyChange("retryMessage", null, originalMessage);
    }

    /**
     * JavaScript metni için özel karakterleri kaçır
     */
    private String escapeJavaScriptText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    /**
     * HTML metni için özel karakterleri kaçır
     */
    private String escapeHtml(String text) {
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

        return text.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#160;", " ") // HTML indent boşluk karakteri
                .replace("&#305;", "ı")
                .replace("&#231;", "ç")
                .replace("&#246;", "ö")
                .replace("&#252;", "ü")
                .replace("&#287;", "ğ")
                .replace("&#350;", "Ş")
                .replace("&#351;", "ş")
                .replace("&#304;", "İ")
                .replace("&#39;", "'")
                .replace("<br>", "\n")
                .replace("&nbsp;", " ");
    }

    /**
     * Olay testi için test içeriği ekle
     */
    public void performEventTest() {
        eventLogger.log("Olay testi başlatılıyor...");

        StringBuilder testHtml = new StringBuilder();
        testHtml.append("<html><body style='font-family:Dialog; color:#ffffff; background-color:#36393f;'>");
        testHtml.append("<div style='margin: 20px;'>");
        testHtml.append("<h3 style='color:#7289da;'>Test Bağlantıları</h3>");
        testHtml.append("<p>Aşağıdaki bağlantıları tıklayarak olay işleme mekanizmasını test edin:</p>");

        // Test ID'leri
        String testMessageId = "test_id_" + System.currentTimeMillis();
        String testCodeId = "test_code_id_" + System.currentTimeMillis();

        // Test mesajı - WhatsApp tarzı
        testHtml.append("<div style='position: relative; margin: 20px 100px 20px 10px;'>");
        testHtml.append("<div style='background-color: #005c4b; padding: 10px; border-radius: 10px; position: relative;'>");
        testHtml.append("<a href='copy:").append(testMessageId).append("' style='text-decoration:none;'>");
        testHtml.append("<span id=\"btn_").append(testMessageId).append("\" class='copy-button'>📋 Copy</span>");
        testHtml.append("</a>");
        testHtml.append("<div id=\"").append(testMessageId).append("\">Bu bir test mesajıdır.</div>");
        testHtml.append("</div>");
        testHtml.append("</div>");

        // Test kod bloğu - WhatsApp tarzı
        testHtml.append("<div style='position: relative; margin: 20px 10px 20px 100px;'>");
        testHtml.append("<div style='background-color: #3b4a83; padding: 10px; border-radius: 10px; position: relative;'>");
        testHtml.append("<div class='code-block'>");
        testHtml.append("<div class='code-header'>");
        testHtml.append("<div class='code-language'>TEST</div>");
        testHtml.append("<div>");
        testHtml.append("<a href='copycode:").append(testCodeId).append("' style='text-decoration:none;'>");
        testHtml.append("<span id=\"btn_").append(testCodeId).append("\" class='copy-button'>📋 Copy</span>");
        testHtml.append("</a>");
        testHtml.append("</div>");
        testHtml.append("</div>");
        testHtml.append("<pre><code id=\"").append(testCodeId).append("\" class='code-content'>function testCode() {\n  console.log(\"Test kodu\");\n}</code></pre>");
        testHtml.append("</div>");
        testHtml.append("</div>");
        testHtml.append("</div>");

        testHtml.append("</div>");
        testHtml.append("</body></html>");

        setText(testHtml.toString());
        eventLogger.log("Test içeriği yüklendi, ID'ler: " + testMessageId + ", " + testCodeId);
    }
}
