import java.awt.Color;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

public class TestAnimatedGradient {

    public static void main(String[] args) {
        int width = 800;
        int height = 600;

        // Animasyonu ana thread'i bloklamamak için yeni bir thread içinde başlatıyoruz
        new Thread(() -> {
            float hueOffset = 0;

            while (true) {
                // Her köşeye farklı bir başlangıç açısı veriyoruz (0, 0.25, 0.5, 0.75)
                // ve hueOffset ile bu renklerin çember etrafında dönmesini sağlıyoruz.
                
                Color c1 = Color.getHSBColor(hueOffset, 1.0f, 1.0f);            // Top-Left
                Color c2 = Color.getHSBColor(hueOffset + 0.25f, 1.0f, 1.0f);     // Top-Right
                Color c3 = Color.getHSBColor(hueOffset + 0.50f, 1.0f, 1.0f);     // Bottom-Left
                Color c4 = Color.getHSBColor(hueOffset + 0.75f, 1.0f, 1.0f);     // Bottom-Right

                // Matrisi HSV tabanlı oluşturursak geçişler çok daha canlı olur
                CMatrix.fromColorGradientHSV(height, width, c1, c2, c3, c4)
                       .imshowRefresh("Dynamic Color Flow - OJL Animation");

                // Renk tonunu her adımda %0.5 artır (Hız ayarı)
                hueOffset += 0.005f;
                if (hueOffset > 1.0f) hueOffset = 0;

                // İşlemciyi yormamak ve gözün algılayacağı hıza çekmek için küçük bir gecikme
                FactoryUtils.delay(20); 
            }
        }).start();
    }
}