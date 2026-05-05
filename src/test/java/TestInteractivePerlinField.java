import java.awt.Color;
import java.awt.image.BufferedImage;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

public class TestInteractivePerlinField {

    public static void main(String[] args) {
        int w = 600, h = 600;
        new Thread(() -> {
            float t = 0;
            while (true) {
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                for (int i = 0; i < h; i += 2) { // Hız için 2'şer atlıyoruz
                    for (int j = 0; j < w; j += 2) {
                        // Karmaşık dalga girişimi (Interference) formülü
                        double v = Math.sin(j / 40.0 + t);
                        v += Math.sin((i / 30.0 + t) / 2.0);
                        v += Math.sin((j + i + t) / 50.0);
                        
                        // Perlin gürültüsü ile dalgaları organikleştirme
                        float p = FactoryUtils.perlinNoise(t + (float)(v * 10), 0.1f);
                        
                        // Renk spektrumunda yumuşak ama hızlı gezinti
                        float hue = (float) (Math.sin(p * Math.PI + v) * 0.5 + 0.5);
                        int rgb = Color.HSBtoRGB(hue, 0.8f, 0.9f);

                        // 2x2 blok boyama
                        img.setRGB(j, i, rgb);
                        if (j + 1 < w) img.setRGB(j + 1, i, rgb);
                        if (i + 1 < h) img.setRGB(j, i + 1, rgb);
                        if (j + 1 < w && i + 1 < h) img.setRGB(j + 1, i + 1, rgb);
                    }
                }
                // OJL'nin yenileme metodu
                CMatrix.getInstance(img).imshowRefresh("OJL Liquid Color Flow");
                
                t += 0.15f; // Hareket hızı (Bunu artırırsanız daha çılgın akar)
                FactoryUtils.delay(10);
            }
        }).start();
    }
}