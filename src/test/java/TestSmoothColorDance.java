import java.awt.Color;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

public class TestSmoothColorDance {

    public static void main(String[] args) {
        int width = 800;
        int height = 600;

        new Thread(() -> {
            float time = 0;
            // noiseScale ne kadar küçükse, geçişler o kadar "bulutsu" ve ağır olur.
            float noiseScale = 0.01f; 

            while (true) {
                // Her köşe için Perlin uzayında farklı rotalar çiziyoruz.
                // Math.sin kullanımı Hue'nun 0.0-1.0 sınırında zıplamasını engeller.
                
                float h1 = getSmoothHue(time, 0, noiseScale);
                float h2 = getSmoothHue(time, 1000, noiseScale);
                float h3 = getSmoothHue(time, 2000, noiseScale);
                float h4 = getSmoothHue(time, 3000, noiseScale);

                // Doygunluk (S) ve Parlaklık (V) değerlerini de hafifçe oynatarak 
                // derinlik hissi verebiliriz.
                Color c1 = Color.getHSBColor(h1, 0.9f, 0.9f);
                Color c2 = Color.getHSBColor(h2, 0.9f, 0.9f);
                Color c3 = Color.getHSBColor(h3, 0.9f, 0.9f);
                Color c4 = Color.getHSBColor(h4, 0.9f, 0.9f);

                CMatrix.fromColorGradientHSV(height, width, c1, c2, c3, c4)
                       .imshowRefresh("Silky Color Dance - OJL Optimized");

                // Zaman adımı - Akıcılık için küçük tutuyoruz.
                time += 0.5f;

                FactoryUtils.delay(15); 
            }
        }).start();
    }

    /**
     * Perlin gürültüsünü alıp sinüs dalgasıyla harmanlayarak 
     * 0.0-1.0 arasında pürüzsüz bir Hue değeri üretir.
     */
    private static float getSmoothHue(float time, float offset, float scale) {
        // FactoryUtils içindeki perlin gürültüsünü alıyoruz
        float noise = FactoryUtils.perlinNoise(time + offset, scale);
        
        // Gürültüyü bir açıyla (PI) çarparak sinüse sokuyoruz. 
        // Bu, değerin 0-1 arasında yumuşak bir döngü yapmasını sağlar.
        double smooth = Math.sin(noise * Math.PI); 
        
        // [-1, 1] arasını [0, 1] arasına çekiyoruz
        return (float) (smooth * 0.5 + 0.5);
    }
}