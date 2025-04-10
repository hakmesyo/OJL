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
 * Custom panel for chat display and message processing
 */
public class ChatPane extends JEditorPane {

    private final EventLogger eventLogger;
    private final HTMLEditorKit htmlKit;
    private final Color chatBgColor = new Color(36, 36, 40);
    private final Color userBubbleColor = new Color(0, 132, 87); // WhatsApp green
    private final Color aiBubbleColor = new Color(59, 74, 131);  // WhatsApp blue

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

        // CSS Styles - for more reliable alignment and wrapping
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

        // Listen to hyperlink events
        addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                String url = e.getDescription();
                handleHyperlinkAction(url);
            }
        });
    }

    /**
     * Show welcome message
     */
    public void showWelcomeMessage() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<div style='text-align:center; margin-top:100px;'>");
        html.append("<h2 style='color:#7289da;'>Welcome!</h2>");
        html.append("<p>Start chatting with the Gemma 3 AI model.</p>");
        html.append("<p style='color:#999999; font-size:12pt;'>Type your message in the text box below and click the Send button.</p>");
        html.append("</div>");
        html.append("</body></html>");

        setText(html.toString());
    }

    /**
     * Add user message
     */
    public void addUserMessage(String sender, String message) {
        appendMessageToHTML(sender, message, false);
    }

    /**
     * Add AI message
     */
    public void addAIMessage(String sender, String message) {
        appendMessageToHTML(sender, message, true);
    }

    /**
     * Append message to HTML content - WhatsApp-style zigzag view
     */
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

        // Simpler and more direct HTML structure - WhatsApp-like appearance
        if (isAI) {
            // AI message - on the right
            messageHtml.append("<div style='margin: 15px 10px 15px 100px;'>");
            messageHtml.append("<div style='background-color: rgba(59, 74, 131, 0.8); padding: 10px; border-radius: 10px; position: relative;'>");
        } else {
            // User message - on the left
            messageHtml.append("<div style='margin: 15px 100px 15px 10px;'>");
            messageHtml.append("<div style='background-color: rgba(0, 92, 75, 0.8); padding: 10px; border-radius: 10px; position: relative;'>");
        }

        // Sender information
        messageHtml.append("<div style='font-weight: bold; color: white; margin-bottom: 8px;'>");
        messageHtml.append(isAI ? sender : "You");
        messageHtml.append("</div>");

        // Message content
        // Check if it's code content
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
            // Normal text content - using double quotes for ID
            messageHtml.append("<div id=\"").append(messageId).append("\" style='color: white; white-space: pre-wrap; text-align: left;'>");
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

    /**
     * Process code blocks and create formatted HTML
     */
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

                    // Code content - using double quotes
                    result.append("<pre><code id=\"").append(codeBlockId).append("\" class='code-content'>");
                    result.append(escapeHtml(codeContent));
                    result.append("</code></pre>");
                    result.append("</div>"); // close code-block
                }
            }
        } else {
            // In case there's no code block
            result.append(escapeHtml(message));
        }

        return result.toString();
    }

    /**
     * Handle hyperlink actions
     */
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

    /**
     * Copy formatted code to clipboard
     */
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

                // Copy to clipboard - as formatted text
                StringSelection selection = new StringSelection(codeText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

                // Show copy feedback
                showCopyFeedback(codeBlockId);

                eventLogger.log("Code copied as formatted");
            } else {
                eventLogger.log("Code content not found");
            }
        } catch (Exception e) {
            eventLogger.log("Error copying code: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show copy operation feedback
     */
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
                    // Update button HTML - will be ✓ and Copied
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
            JLabel label = new JLabel("   Formatted code copied!   ");
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Dialog", Font.BOLD, 12));
            label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

            popup.getContentPane().add(label);
            popup.getContentPane().setBackground(new Color(67, 181, 129)); // Green
            popup.pack();

            // Popup position - relative to current window
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

    /**
     * Escape special characters for HTML text
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
     * Convert HTML escape characters back
     */
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

        // Turkish characters
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

        // HTML line breaks
        text = text.replace("<br>", "\n")
                .replace("<br/>", "\n");

        // Unicode emoji cleaning (like &#55357;&#56523;)
        text = text.replaceAll("&#\\d+;", "");

        return text;
    }

    /**
     * Add test content for event testing
     */
    public void performEventTest() {
        eventLogger.log("Starting event test...");

        StringBuilder testHtml = new StringBuilder();
        testHtml.append("<html><body style='font-family:Dialog; color:#ffffff; background-color:#36393f;'>");
        testHtml.append("<div style='margin: 20px;'>");
        testHtml.append("<h3 style='color:#7289da;'>Test Content</h3>");
        testHtml.append("<p>Test messages created.</p>");

        // Test IDs
        String testMessageId = "test_id_" + System.currentTimeMillis();

        // Test message - WhatsApp style
        testHtml.append("<div style='position: relative; margin: 20px 100px 20px 10px;'>");
        testHtml.append("<div style='background-color: #005c4b; padding: 10px; border-radius: 10px; position: relative;'>");
        testHtml.append("<div id=\"").append(testMessageId).append("\">This is a test message.</div>");
        testHtml.append("</div>");
        testHtml.append("</div>");

        testHtml.append("</div>");
        testHtml.append("</body></html>");

        setText(testHtml.toString());
        eventLogger.log("Test content loaded");
    }

    /**
     * Add AI message with specific ID (for streaming)
     */
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

        // Create message HTML - using table for more reliable alignment
        StringBuilder messageHtml = new StringBuilder();

        // AI message - on right, with table structure
        messageHtml.append("<div class='message-container ai-message'>");
        messageHtml.append("<div class='message-bubble ai-bubble'>");

        // Sender information
        messageHtml.append("<table width='100%' cellspacing='0' cellpadding='0' border='0'>");
        messageHtml.append("<tr>");

        // Sender (left cell)
        messageHtml.append("<td align='left'>");
        messageHtml.append("<span class='sender ai-sender'>").append(sender).append("</span>");
        messageHtml.append("</td>");

        messageHtml.append("</tr>");
        messageHtml.append("</table>");

        // Message content - single row in table, full width
        messageHtml.append("<table width='100%' cellspacing='0' cellpadding='0' border='0'>");
        messageHtml.append("<tr>");
        messageHtml.append("<td align='left' style='word-wrap: break-word;'>");

        // Put each paragraph in separate <p>
        messageHtml.append("<div id=\"").append(messageId).append("\" class='message-content ai-content'>");

        // Format message content as paragraphs
        String processedMessage = processMessageContent(message);
        messageHtml.append(processedMessage);

        messageHtml.append("</div>");
        messageHtml.append("</td>");
        messageHtml.append("</tr>");
        messageHtml.append("</table>");

        // Closing divs
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

    // Method to format message content as paragraphs
    private String processMessageContent(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        // Process as code block if contains code
        if (message.contains("```") || message.contains("public class")
                || message.contains("function") || message.contains("def ")
                || message.contains("import ")) {
            return processCodeBlocks(message, "msg_" + System.currentTimeMillis());
        }

        // Normal text processing - make each line a paragraph
        String[] lines = message.split("\n");
        StringBuilder formattedMessage = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                // Empty paragraph for blank line
                formattedMessage.append("<p>&nbsp;</p>");
            } else {
                // Put each line in a paragraph
                formattedMessage.append("<p align='left'>").append(escapeHtml(line)).append("</p>");
            }
        }

        return formattedMessage.toString();
    }

    /**
     * Update the last message sent by AI (for streaming)
     */
    public void updateLastAIMessage(String sender, String newContent) {
        try {
            // Get HTML content
            String htmlContent = getText();
            Document doc = getDocument();

            // Examine HTML document
            if (doc instanceof HTMLDocument) {
                HTMLDocument htmlDoc = (HTMLDocument) doc;

                // Find AI message divs (blue bubbles on the right)
                Element[] divs = findElementsByStyleClass(htmlDoc, HTML.Tag.DIV, "ai-bubble");

                if (divs != null && divs.length > 0) {
                    // Last AI div (last message)
                    Element lastAIMessageDiv = divs[divs.length - 1];

                    // Find content div (message-content class)
                    Element[] contentDivs = findElementsByStyleClass(htmlDoc,
                            HTML.Tag.DIV, "message-content", lastAIMessageDiv);

                    if (contentDivs != null && contentDivs.length > 0) {
                        Element contentDiv = contentDivs[0];

                        // Update content
                        htmlDoc.setInnerHTML(contentDiv, formatMessageContent(newContent));

                        // Scroll to bottom
                        setCaretPosition(getDocument().getLength());
                        scrollRectToVisible(new Rectangle(0, getHeight() - 1, 1, 1));
                        return;
                    }
                }

                // If elements not found, add a new message
                addAIMessage(sender, newContent);
            }
        } catch (Exception e) {
            eventLogger.log("Error updating last message: " + e.getMessage());

            // Add new message in case of error
            addAIMessage(sender, newContent);
        }
    }

    /**
     * Find elements with specific tag and class in HTML document
     */
    private Element[] findElementsByStyleClass(HTMLDocument doc, HTML.Tag tag,
            String styleClass) {
        return findElementsByStyleClass(doc, tag, styleClass, doc.getDefaultRootElement());
    }

    /**
     * Find elements with specific tag and class within a parent element
     */
    private Element[] findElementsByStyleClass(HTMLDocument doc, HTML.Tag tag,
            String styleClass, Element parent) {
        List<Element> matchingElements = new java.util.ArrayList<>();

        int count = parent.getElementCount();
        for (int i = 0; i < count; i++) {
            Element element = parent.getElement(i);

            // Check element attributes
            AttributeSet attrs = element.getAttributes();
            if (attrs.getAttribute(HTML.Attribute.CLASS) != null
                    && attrs.getAttribute(HTML.Attribute.CLASS).toString().contains(styleClass)
                    && attrs.getAttribute(javax.swing.text.StyleConstants.NameAttribute) == tag) {
                matchingElements.add(element);
            }

            // Check child elements
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
     * Return message content formatted as HTML
     */
    private String formatMessageContent(String content) {
        // Check for code blocks
        if (content.contains("```") || content.contains("public class")
                || content.contains("function") || content.contains("def ")
                || content.contains("import ")) {
            return processCodeBlocks(content, "msg_" + System.currentTimeMillis());
        } else {
            return escapeHtml(content);
        }
    }

    /**
     * Update AI message with specific ID
     */
    public void updateAIMessage(String messageId, String newContent) {
        try {
            // Get HTML content
            Document doc = getDocument();

            if (doc instanceof HTMLDocument) {
                HTMLDocument htmlDoc = (HTMLDocument) doc;

                // Find HTML element by ID
                Element element = htmlDoc.getElement(messageId);

                if (element != null) {
                    // Create formatted content
                    String formattedContent = processMessageContent(newContent);

                    try {
                        // Update element content - safer approach
                        SwingUtilities.invokeLater(() -> {
                            try {
                                htmlDoc.setInnerHTML(element, formattedContent);

                                // Scroll to bottom
                                setCaretPosition(getDocument().getLength());
                                scrollRectToVisible(new Rectangle(0, getHeight() - 1, 1, 1));
                            } catch (Exception ex) {
                                eventLogger.log("Internal error updating HTML: " + ex.getMessage());
                                // Alternative approach - add completely new message
                                addAIMessage("Gemma 3", newContent);
                            }
                        });
                    } catch (Exception e) {
                        // Try adding a completely new message in case of error
                        eventLogger.log("setInnerHTML error: " + e.getMessage());
                        addAIMessage("Gemma 3", newContent);
                    }
                } else {
                    // Element not found, log and add new message
                    eventLogger.log("Message not found, ID: " + messageId);
                    addAIMessage("Gemma 3", newContent);
                }
            }
        } catch (Exception e) {
            eventLogger.log("Error updating message: " + e.getMessage());
            e.printStackTrace();

            // Add completely new message in case of error
            addAIMessage("Gemma 3", newContent);
        }
    }
}