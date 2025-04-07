package jazari.llm.clip_embedding;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.json.JSONObject;
import org.json.JSONArray;

public class ClipEmbeddingClient {
    private final String apiUrl;
    private final HttpClient client;
    
    public ClipEmbeddingClient(String host, int port) {
        this.apiUrl = "http://" + host + ":" + port + "/extract_features";
        this.client = HttpClient.newHttpClient();
    }
    
    /**
     * Görüntüden CLIP vektör gömmesini alır
     */
    public float[] getImageEmbeddings(BufferedImage image) throws Exception {
        if (image == null) {
            throw new IllegalArgumentException("Görüntü null olamaz");
        }
        
        // Görüntüyü Base64'e dönüştür
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean writeSuccess = ImageIO.write(image, "png", baos);
        
        if (!writeSuccess) {
            baos.reset();
            writeSuccess = ImageIO.write(image, "jpg", baos);
            
            if (!writeSuccess) {
                throw new RuntimeException("Görüntü ne PNG ne de JPG formatına dönüştürülemedi");
            }
        }
        
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
        
        // JSON isteği oluştur
        JSONObject requestBody = new JSONObject();
        requestBody.put("image", encodedImage);
        
        // HTTP isteğini gönder
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Yanıtı işle
        if (response.statusCode() != 200) {
            throw new RuntimeException("API yanıt hatası: " + response.statusCode() + " - " + response.body());
        }
        
        JSONObject jsonResponse = new JSONObject(response.body());
        
        if (!jsonResponse.getBoolean("success")) {
            throw new RuntimeException("API işlem hatası: " + jsonResponse.getString("error"));
        }
        
        JSONArray embeddingsArray = jsonResponse.getJSONArray("embeddings");
        
        // JSON dizisini float dizisine dönüştür
        float[] embeddings = new float[embeddingsArray.length()];
        for (int i = 0; i < embeddingsArray.length(); i++) {
            embeddings[i] = (float) embeddingsArray.getDouble(i);
        }
        
        return embeddings;
    }
    
    /**
     * Görüntü açıklaması oluşturur
     */
    public String generateCaption(BufferedImage image) throws Exception {
        if (image == null) {
            throw new IllegalArgumentException("Görüntü null olamaz");
        }
        
        // Görüntüyü Base64'e dönüştür
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean writeSuccess = ImageIO.write(image, "png", baos);
        
        if (!writeSuccess) {
            baos.reset();
            writeSuccess = ImageIO.write(image, "jpg", baos);
            
            if (!writeSuccess) {
                throw new RuntimeException("Görüntü ne PNG ne de JPG formatına dönüştürülemedi");
            }
        }
        
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.getEncoder().encodeToString(imageBytes);
        
        // JSON isteği oluştur
        JSONObject requestBody = new JSONObject();
        requestBody.put("image", encodedImage);
        
        // HTTP isteğini gönder (caption endpoint'ine)
        String captionUrl = apiUrl.replace("/extract_features", "/generate_caption");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(captionUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Yanıtı işle
        if (response.statusCode() != 200) {
            throw new RuntimeException("API yanıt hatası: " + response.statusCode() + " - " + response.body());
        }
        
        JSONObject jsonResponse = new JSONObject(response.body());
        
        if (!jsonResponse.getBoolean("success")) {
            throw new RuntimeException("API işlem hatası: " + jsonResponse.getString("error"));
        }
        
        return jsonResponse.getString("caption");
    }
    
    /**
     * Servisin çalışıp çalışmadığını kontrol eder
     */
    public boolean isServiceHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl.replace("/extract_features", "/health")))
                    .GET()
                    .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}