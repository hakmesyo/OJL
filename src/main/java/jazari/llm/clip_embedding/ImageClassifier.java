package jazari.llm.clip_embedding;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ImageClassifier {
    private final ClipEmbeddingClient clipClient;
    private Map<String, float[]> classEmbeddings = new HashMap<>();
    
    public ImageClassifier() {
        // CLIP servisine bağlan (yerel makinede çalışıyorsa)
        clipClient = new ClipEmbeddingClient("localhost", 5000);
        
        // Servis sağlık kontrolü
        if (!clipClient.isServiceHealthy()) {
            System.err.println("UYARI: CLIP servisi yanıt vermiyor!");
        }
    }
    
    /**
     * Görüntüyü sınıflandırır
     */
    public String classifyImage(BufferedImage image) throws Exception {
        if (classEmbeddings.isEmpty()) {
            throw new IllegalStateException("Sınıflandırma yapmadan önce örnek sınıflar eklenmelidir.");
        }
        
        // Görüntünün vektör gömmesini al
        float[] imageEmbedding = clipClient.getImageEmbeddings(image);
        
        // En yakın sınıfı bul (kosinüs benzerliği kullanarak)
        String bestClass = null;
        float bestSimilarity = -1;
        
        // Sadece benzerlik değerlerini ve sonucu yazdır
        for (Map.Entry<String, float[]> entry : classEmbeddings.entrySet()) {
            float similarity = cosineSimilarity(imageEmbedding, entry.getValue());
            System.out.println("Sınıf '" + entry.getKey() + "' benzerliği: " + similarity);
            
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestClass = entry.getKey();
            }
        }
        
        System.out.println("En yakın sınıf: " + bestClass + " (Benzerlik: " + bestSimilarity + ")");
        return bestClass;
    }
    
    /**
     * Görüntünün açıklamasını üretir
     */
    public String describeImage(BufferedImage image) throws Exception {
        return clipClient.generateCaption(image);
    }
    
    /**
     * Sınıf örneği ekler
     */
    public void addClass(String className, BufferedImage exampleImage) throws Exception {
        float[] embedding = clipClient.getImageEmbeddings(exampleImage);
        classEmbeddings.put(className, embedding);
    }
    
    // İki vektör arasındaki kosinüs benzerliğini hesaplar
    private float cosineSimilarity(float[] vector1, float[] vector2) {
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            normA += vector1[i] * vector1[i];
            normB += vector2[i] * vector2[i];
        }
        
        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }
}