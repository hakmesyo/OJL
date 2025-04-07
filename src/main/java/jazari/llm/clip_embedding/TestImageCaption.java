package jazari.llm.clip_embedding;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import jazari.factory.FactoryUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author cezerilab
 */
public class TestImageCaption {
    public static void main(String[] args) {
        try {
            System.out.println("Görüntü açıklama testi başlıyor...");
            
            // Test edilecek görüntülerin listesi
            List<String> imageFiles = new ArrayList<>();
            imageFiles.add("images/dog_cat.jpg");
            imageFiles.add("images/bird.jpg");
            imageFiles.add("images/kaplan1.jpg");
            imageFiles.add("images/babun.jpg");  // Eğer varsa
            imageFiles.add("images/yaprak.jpg"); // Eğer varsa
            imageFiles.add("images/sim_1.jpg"); // Eğer varsa
            
            // ClipEmbeddingClient oluştur
            ClipEmbeddingClient client = new ClipEmbeddingClient("localhost", 5000);
            
            // Her görüntü için açıklama üret
            long t1 = FactoryUtils.tic();
            for (String imagePath : imageFiles) {
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    System.out.println("UYARI: " + imagePath + " dosyası bulunamadı, atlanıyor.");
                    continue;
                }
                
                System.out.println("\n--- " + imagePath + " için açıklama üretiliyor ---");
                BufferedImage image = ImageIO.read(imageFile);
                
                
                String caption = client.generateCaption(image);
                t1 = FactoryUtils.toc(t1);
                
                System.out.println("Görüntü açıklaması: " + caption);
                //System.out.println("İşlem süresi: " + t1 + " ms");
            }
            
            System.out.println("\nGörüntü açıklama testi tamamlandı.");
            
        } catch (Exception e) {
            System.err.println("Hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}