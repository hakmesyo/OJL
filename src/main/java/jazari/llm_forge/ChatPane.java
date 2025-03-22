package jazari.llm_forge;

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

public class ChatPane extends JEditorPane {

    private final EventLogger eventLogger;
    private final HTMLEditorKit htmlKit;
    private final Color chatBgColor = new Color(36, 36, 40);
    private final Color userBubbleColor = new Color(0, 132, 87); // Green
    private final Color aiBubbleColor = new Color(59, 74, 131);  // Blue

    public ChatPane(EventLogger logger) {
        this.eventLogger = logger;

        // Set as HTML content
        setContentType("text/html");
        setEditable(false);
        setBackground(chatBgColor);

        // Font settings
        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        setFont(new Font("Dialog", Font.PLAIN, 14));

        // Set up HTML Kit
        htmlKit = new HTMLEditorKit();
        setEditorKit(htmlKit);

        // CSS Styles
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
                + ".code-content { font-family: monospace; white-space: pre; color: #cccccc; margin: 0; overflow-x: auto; }\n"
                + ".welcome-container { text-align: center; margin-top: 80px; }\n"
                + ".welcome-title { color: #7289da; font-size: 24pt; margin-bottom: 20px; }\n"
                + ".welcome-text { color: #cccccc; font-size: 14pt; margin-bottom: 10px; }\n"
                + ".welcome-hint { color: #999999; font-size: 12pt; }";

        htmlKit.getStyleSheet().addRule(css);

        // Listen to hyperlink events
        addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String url = e.getDescription();
                handleHyperlinkAction(url);
            }
        });
    }

    public void showWelcomeMessage() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<div class='welcome-container'>");
        html.append("<div class='welcome-title'>Welcome to Jazari Chat Forge!</div>");
        html.append("<p class='welcome-text'>Start chatting with your favorite LLM models.</p>");
        html.append("<p class='welcome-hint'>Select a model from the top menu and type your message below.</p>");
        html.append("</div>");
        html.append("</body></html>");

        setText(html.toString());
    }

    public void addUserMessage(String sender, String message) {
        appendMessageToHTML(sender, message, false);
    }

    public void addAIMessage(String sender, String message) {
        appendMessageToHTML(sender, message, true);
    }

    private void appendMessageToHTML(String sender, String message, boolean isAI) {
        eventLogger.log("Adding message: Sender=" + sender + ", isAI=" + isAI);

        // Get current HTML content
        String currentContent = getText();

        // Find the closing of the <body> tag
        int bodyEndIndex = currentContent.lastIndexOf("</body>");
        if (bodyEndIndex == -1) {
            // If HTML structure is not as expected, create a new HTML structure
            currentContent = "<html><body></body></html>";
            bodyEndIndex = currentContent.lastIndexOf("</body>");
        }

        // Create unique message ID
        String messageId = "msg_" + System.currentTimeMillis();

        // Create message HTML
        StringBuilder messageHtml = new StringBuilder();

        // Message container with appropriate class
        if (isAI) {
            messageHtml.append("<div class='message-container ai-message'>");
            messageHtml.append("<div class='message-bubble ai-bubble'>");
        } else {
            messageHtml.append("<div class='message-container user-message'>");
            messageHtml.append("<div class='message-bubble user-bubble'>");
        }

        // Sender information
        messageHtml.append("<div class='sender ").append(isAI ? "ai-sender" : "user-sender").append("'>");
        messageHtml.append(sender);
        messageHtml.append("</div>");

        // Message content
        boolean containsCode = isAI && (message.contains("```")
                || message.contains("public class")
                || message.contains("function")
                || message.contains("def ")
                || message.contains("import "));

        if (containsCode) {
            messageHtml.append("<div class='message-content ").append(isAI ? "ai-content" : "user-content").append("'>");
            messageHtml.append(processCodeBlocks(message, messageId));
            messageHtml.append("</div>");
        } else {
            messageHtml.append("<div id=\"").append(messageId).append("\" class='message-content ")
                      .append(isAI ? "ai-content" : "user-content").append("'>");
            messageHtml.append(escapeHtml(message));
            messageHtml.append("</div>");
        }

        // Close bubble and container divs
        messageHtml.append("</div></div>");

        // Add message to HTML
        StringBuilder newContent = new StringBuilder(currentContent);
        newContent.insert(bodyEndIndex, messageHtml.toString());

        // Set updated HTML
        setText(newContent.toString());

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            setCaretPosition(getDocument().getLength());
            scrollRectToVisible(new Rectangle(0, getHeight() - 1, 1, 1));
        });

        eventLogger.log("Message added, ID: " + messageId);
    }

    private String processCodeBlocks(String message, String parentMessageId) {
        StringBuilder result = new StringBuilder();

        if (message.contains("```")) {
            String[] parts = message.split("```");

            for (int i = 0; i < parts.length; i++) {
                if (i % 2 == 0) {
                    // Normal text
                    result.append(escapeHtml(parts[i]));
                } else {
                    // Code block
                    String codeContent = parts[i].trim();
                    String language = "";

                    // Separate language indicator if present
                    if (codeContent.contains("\n")) {
                        String firstLine = codeContent.substring(0, codeContent.indexOf("\n")).trim();
                        if (!firstLine.contains(" ")) {
                            language = firstLine;
                            codeContent = codeContent.substring(codeContent.indexOf("\n") + 1);
                        }
                    }

                    String codeBlockId = "code_" + parentMessageId + "_" + i;

                    // Code block HTML
                    result.append("<div class='code-block'>");

                    // Title bar
                    result.append("<div class='code-header'>");
                    if (!language.isEmpty()) {
                        result.append("<div class='code-language'>").append(language.toUpperCase()).append("</div>");
                    } else {
                        result.append("<div class='code-language'>CODE</div>");
                    }

                    // Copy button
                    result.append("<div>");
                    result.append("<a href='copycode:").append(codeBlockId).append("' style='text-decoration:none;'>");
                    result.append("<span id=\"btn_").append(codeBlockId).append("\" class='copy-button'>📋 Copy</span>");
                    result.append("</a>");
                    result.append("</div>");

                    result.append("</div>"); // close header

                    // Code content
                    result.append("<pre><code id=\"").append(codeBlockId).append("\" class='code-content'>");
                    result.append(escapeHtml(codeContent));
                    result.append("</code></pre>");
                    result.append("</div>"); // close code-block
                }
            }
        } else {
            // In case there's no code block markers but it still looks like code
            String codeBlockId = "code_" + parentMessageId + "_full";
            
            result.append("<div class='code-block'>");
            result.append("<div class='code-header'>");
            result.append("<div class='code-language'>CODE</div>");
            
            // Copy button
            result.append("<div>");
            result.append("<a href='copycode:").append(codeBlockId).append("' style='text-decoration:none;'>");
            result.append("<span id=\"btn_").append(codeBlockId).append("\" class='copy-button'>📋 Copy</span>");
            result.append("</a>");
            result.append("</div>");
            
            result.append("</div>"); // close header
            
            // Code content
            result.append("<pre><code id=\"").append(codeBlockId).append("\" class='code-content'>");
            result.append(escapeHtml(message));
            result.append("</code></pre>");
            result.append("</div>"); // close code-block
        }

        return result.toString();
    }

    private void handleHyperlinkAction(String url) {
        eventLogger.log("Hyperlink clicked: " + url);

        try {
            if (url.startsWith("copycode:")) {
                String codeBlockId = url.substring(9);
                copyFormattedCodeToClipboard(codeBlockId);
            }
        } catch (Exception e) {
            eventLogger.log("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void copyFormattedCodeToClipboard(String codeBlockId) {
        try {
            // Get HTML content
            String htmlText = getText();

            // Find code block ID
            int idIndex = htmlText.indexOf("id=\"" + codeBlockId + "\"");
            if (idIndex == -1) {
                eventLogger.log("Code block not found: " + codeBlockId);
                return;
            }

            // Find start of <code> element
            int codeStart = htmlText.indexOf(">", idIndex) + 1;
            // Find </code> tag
            int codeEnd = htmlText.indexOf("</code>", codeStart);

            if (codeStart > 0 && codeEnd > codeStart) {
                // Get code content
                String rawHtml = htmlText.substring(codeStart, codeEnd);

                // Decode HTML and Unicode characters
                String codeText = unescapeHtml(rawHtml);

                // Copy to clipboard
                StringSelection selection = new StringSelection(codeText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

                // Show copy feedback
                showCopyFeedback(codeBlockId);

                eventLogger.log("Code copied to clipboard");
            } else {
                eventLogger.log("Code content not found");
            }
        } catch (Exception e) {
            eventLogger.log("Error copying code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showCopyFeedback(String id) {
        try {
            // Update button (for feedback)
            String htmlContent = getText();
            String buttonId = "id=\"btn_" + id + "\"";

            int buttonStartIndex = htmlContent.indexOf(buttonId);
            if (buttonStartIndex != -1) {
                int buttonTagStart = htmlContent.lastIndexOf("<span", buttonStartIndex);
                int buttonTagEnd = htmlContent.indexOf(">", buttonStartIndex) + 1;
                int buttonContentEnd = htmlContent.indexOf("</span>", buttonTagEnd);

                if (buttonTagStart != -1 && buttonTagEnd != -1 && buttonContentEnd != -1) {
                    // Update button HTML
                    String buttonPrefix = htmlContent.substring(buttonTagStart, buttonTagEnd);
                    String updatedButton = buttonPrefix.replace("copy-button", "copy-button copy-feedback") + "✓ Copied";

                    StringBuilder updatedContent = new StringBuilder(htmlContent);
                    updatedContent.replace(buttonTagStart, buttonContentEnd, updatedButton);

                    setText(updatedContent.toString());

                    // Restore button to original state after 2 seconds
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
                            eventLogger.log("Button restore error: " + ex.getMessage());
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                }
            }

            // Show copy notification to user
            JWindow popup = new JWindow();
            JLabel label = new JLabel("   Code copied to clipboard   ");
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Dialog", Font.BOLD, 12));
            label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            popup.getContentPane().add(label);
            popup.getContentPane().setBackground(new Color(67, 181, 129)); // Green
            popup.pack();

            // Popup position
            Point p = this.getLocationOnScreen();
            int x = p.x + this.getWidth() - popup.getWidth() - 20;
            int y = p.y + 20;
            popup.setLocation(x, y);

            // Show popup and close after 1.5 seconds
            popup.setVisible(true);
            Timer timer = new Timer(1500, e -> popup.dispose());
            timer.setRepeats(false);
            timer.start();

        } catch (Exception e) {
            eventLogger.log("Error showing feedback: " + e.getMessage());
        }
    }

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

    private String unescapeHtml(String text) {
        if (text == null) {
            return "";
        }

        // HTML characters
        text = text.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")
                .replace("&#160;", " ");

        // HTML line breaks
        text = text.replace("<br>", "\n")
                .replace("<br/>", "\n");

        return text;
    }

    public void addAIMessageWithId(String sender, String message, String messageId) {
        eventLogger.log("Adding message with ID: Sender=" + sender + ", ID=" + messageId);

        // Get current HTML content
        String currentContent = getText();

        // Find the closing of the <body> tag
        int bodyEndIndex = currentContent.lastIndexOf("</body>");
        if (bodyEndIndex == -1) {
            // If HTML structure is not as expected, create a new HTML structure
            currentContent = "<html><body></body></html>";
            bodyEndIndex = currentContent.lastIndexOf("</body>");
        }

        // Create message HTML
        StringBuilder messageHtml = new StringBuilder();

        // AI message container and bubble
        messageHtml.append("<div class='message-container ai-message'>");
        messageHtml.append("<div class='message-bubble ai-bubble'>");

        // Sender information
        messageHtml.append("<div class='sender ai-sender'>").append(sender).append("</div>");

        // Message content with ID for future updates
        messageHtml.append("<div id=\"").append(messageId).append("\" class='message-content ai-content'>");
        
        // Format message content - will be empty initially for streaming messages
        String processedMessage = escapeHtml(message);
        messageHtml.append(processedMessage);

        messageHtml.append("</div>");
        messageHtml.append("</div></div>");

        // Add message to HTML
        StringBuilder newContent = new StringBuilder(currentContent);
        newContent.insert(bodyEndIndex, messageHtml.toString());

        // Set updated HTML
        setText(newContent.toString());

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            setCaretPosition(getDocument().getLength());
            scrollRectToVisible(new Rectangle(0, getHeight() - 1, 1, 1));
        });

        eventLogger.log("Message added with ID: " + messageId);
    }

    public void updateAIMessage(String messageId, String newContent) {
        try {
            // Get document
            Document doc = getDocument();

            if (doc instanceof HTMLDocument) {
                HTMLDocument htmlDoc = (HTMLDocument) doc;

                // Find existing element by ID
                Element element = findElementById(htmlDoc, messageId);

                if (element != null) {
                    // Process and update content
                    try {
                        // Check if content contains code
                        boolean containsCode = newContent.contains("```") || 
                                               newContent.contains("public class") || 
                                               newContent.contains("function") || 
                                               newContent.contains("def ") || 
                                               newContent.contains("import ");
                        
                        String formattedContent;
                        if (containsCode) {
                            formattedContent = processCodeBlocks(newContent, messageId);
                        } else {
                            formattedContent = escapeHtml(newContent);
                        }
                        
                        // Update element content
                        SwingUtilities.invokeLater(() -> {
                            try {
                                ((HTMLDocument)doc).setInnerHTML(element, formattedContent);
                                
                                // Scroll to bottom
                                setCaretPosition(doc.getLength());
                                scrollRectToVisible(new Rectangle(0, getHeight() - 1, 1, 1));
                            } catch (Exception ex) {
                                eventLogger.log("Error updating HTML: " + ex.getMessage());
                            }
                        });
                    } catch (Exception e) {
                        eventLogger.log("setInnerHTML error: " + e.getMessage());
                    }
                } else {
                    // Element not found, log and add new message
                    eventLogger.log("Message element not found, ID: " + messageId);
                    addAIMessage("AI", newContent);
                }
            }
        } catch (Exception e) {
            eventLogger.log("Error updating message: " + e.getMessage());
            // Add new message in case of error
            addAIMessage("AI", newContent);
        }
    }
    
    private Element findElementById(HTMLDocument doc, String id) {
        // Recursive function to find element with matching ID
        return findElementByIdRecursive(doc.getDefaultRootElement(), id);
    }
    
    private Element findElementByIdRecursive(Element element, String id) {
        // Check if this element has the ID we're looking for
        AttributeSet attrs = element.getAttributes();
        if (attrs.getAttribute(HTML.Attribute.ID) != null && 
            attrs.getAttribute(HTML.Attribute.ID).toString().equals(id)) {
            return element;
        }
        
        // Check child elements
        for (int i = 0; i < element.getElementCount(); i++) {
            Element child = element.getElement(i);
            Element found = findElementByIdRecursive(child, id);
            if (found != null) {
                return found;
            }
        }
        
        return null;
    }
}