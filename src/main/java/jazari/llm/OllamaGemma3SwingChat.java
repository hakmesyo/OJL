/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OllamaGemma3SwingChat extends JFrame implements ActionListener {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate"; // Ollama serve adresi
    private static final String MODEL_NAME = "gemma3"; // Kullanılacak modelin adı

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JScrollPane scrollPane;

    public OllamaGemma3SwingChat() {
        setTitle("Ollama Gemma 3 Sohbet");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // Chat Area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Input Field and Send Button
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Gönder");
        sendButton.addActionListener(this);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendButton) {
            String message = inputField.getText();
            if (!message.isEmpty()) {
                chatArea.append("Sen: " + message + "\n");
                inputField.setText("");

                // Ollama'dan yanıt al
                String response = generateText(message);
                chatArea.append("Gemma 3: " + response + "\n");

                // Chat Area'yı en alta kaydır
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
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