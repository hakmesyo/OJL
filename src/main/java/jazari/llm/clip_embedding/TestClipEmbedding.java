package jazari.llm.clip_embedding;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class TestClipEmbedding {

    public static void main(String[] args) {
        try {
            System.out.println("Görüntü sınıflandırma testi başlıyor...");

            // Dosyaların var olduğunu kontrol et
            //File dogFile = new File("images/dog_1.png");
            File dogFile = new File("images/kaplan1.jpg");
            File birdFile = new File("images/bird.jpg");
            File testFile = new File("images/dog_cat.jpg");
            //File testFile = new File("images/butterfly.jpg");
            //File testFile = new File("images/horoz.jpg");
            //File testFile = new File("images/babun.jpg");
            //File testFile = new File("images/yaprak.jpg");

            if (!dogFile.exists()) {
                System.err.println("HATA: " + dogFile.getAbsolutePath() + " dosyası bulunamadı.");
                return;
            }

            if (!birdFile.exists()) {
                System.err.println("HATA: " + birdFile.getAbsolutePath() + " dosyası bulunamadı.");
                return;
            }

            if (!testFile.exists()) {
                System.err.println("HATA: " + testFile.getAbsolutePath() + " dosyası bulunamadı.");
                return;
            }

            System.out.println("Tüm görüntü dosyaları mevcut.");

            // Görüntüleri yükle
            System.out.println("Görüntüler yükleniyor...");
            BufferedImage dogImage = ImageIO.read(dogFile);
            BufferedImage birdImage = ImageIO.read(birdFile);
            BufferedImage testImage = ImageIO.read(testFile);

            System.out.println("Köpek görüntüsü: " + dogImage.getWidth() + "x" + dogImage.getHeight());
            System.out.println("Kuş görüntüsü: " + birdImage.getWidth() + "x" + birdImage.getHeight());
            System.out.println("Test görüntüsü: " + testImage.getWidth() + "x" + testImage.getHeight());

            // Sınıflandırıcıyı oluştur
            ImageClassifier classifier = new ImageClassifier();

            // Örnek sınıfları ekle
            System.out.println("\n--- Sınıf Örnekleri Ekleniyor ---");
            classifier.addClass("köpek", dogImage);
            classifier.addClass("kuş", birdImage);

            long t1=FactoryUtils.tic();
            for (int i = 0; i < 10; i++) {
                // Sınıflandırma yap
                //System.out.println("\n--- Sınıflandırma Yapılıyor ---");
                String prediction = classifier.classifyImage(testImage);

                //System.out.println("\nTahmin edilen sınıf: " + prediction);
                t1=FactoryUtils.toc(t1);
                System.out.println("");
            }

        } catch (Exception e) {
            System.err.println("Hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
