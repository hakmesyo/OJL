/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OllamaGemma3Consumer {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate"; // Ollama serve adresi
    private static final String MODEL_NAME = "gemma3"; // Kullanılacak modelin adı

    public static void main(String[] args) throws Exception {
        String prompt = "Merhaba, nasılsın?"; // Modelle gönderilecek soru

        String response = generateText(prompt);
        System.out.println("Gemma 3'ten Yanıt: " + response);
    }

    public static String generateText(String prompt) throws Exception {
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
            throw new Exception("Ollama API hatası: " + response.statusCode() + " - " + response.body());
        }
    }
}
