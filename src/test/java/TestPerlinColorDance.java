import java.awt.Color;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

public class TestPerlinColorDance {

    public static void main(String[] args) {
        int width = 800;
        int height = 600;

        new Thread(() -> {
            float time = 0;
            // Gürültünün ne kadar "hızlı" değişeceğini belirleyen ölçek
            // Küçük değerler daha ağır ve epik geçişler, büyük değerler daha hızlı dans sağlar.
            float noiseScale = 0.05f; 

            while (true) {
                // Her köşe için farklı bir 'seed' (ofset) kullanarak Perlin gürültüsü üretiyoruz.
                // FactoryUtils.perlinNoise(değer, ölçek) metodunu kullanıyoruz.
                
                // Top-Left Hue
                float h1 = FactoryUtils.perlinNoise(time, noiseScale);
                // Top-Right Hue (Farklı ofset vererek farklı renkte olmasını sağlıyoruz)
                float h2 = FactoryUtils.perlinNoise(time + 100, noiseScale);
                // Bottom-Left Hue
                float h3 = FactoryUtils.perlinNoise(time + 200, noiseScale);
                // Bottom-Right Hue
                float h4 = FactoryUtils.perlinNoise(time + 300, noiseScale);

                // Üretilen 0-1 arası Perlin değerlerini renge çeviriyoruz
                Color c1 = Color.getHSBColor(h1, 1.0f, 1.0f);
                Color c2 = Color.getHSBColor(h2, 1.0f, 1.0f);
                Color c3 = Color.getHSBColor(h3, 1.0f, 1.0f);
                Color c4 = Color.getHSBColor(h4, 1.0f, 1.0f);

                // HSV tabanlı gradient ile köşeleri birleştiriyoruz
                CMatrix.fromColorGradientHSV(height, width, c1, c2, c3, c4)
                       .imshowRefresh("Perlin Color Dance - OJL Experimental");

                // Zamanı ilerletiyoruz
                time += 0.1f;

                // Akıcı bir görüntü için 30-60 FPS arası bir bekleme
                FactoryUtils.delay(20); 
            }
        }).start();
    }
}