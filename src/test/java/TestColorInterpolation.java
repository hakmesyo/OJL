import java.awt.Color;
import jazari.matrix.CMatrix;

public class TestColorInterpolation {
    public static void main(String[] args) {
        // Senaryo 1: Senin istediğin Siyah-Kırmızı-Yeşil-Mavi dörtlüsü
        CMatrix.fromColorGradientRGB(600, 800, 
            Color.BLACK,   // Üst Sol
            Color.RED,     // Üst Sağ
            Color.GREEN,   // Alt Sol
            Color.BLUE     // Alt Sağ
        ).imshow("Corner Gradient 1");

        // Senaryo 2: Daha dramatik bir geçiş (Sarı'dan Magenta'ya, Cyan'dan Beyaz'a)
        CMatrix.fromColorGradientRGB(600, 800, 
            Color.YELLOW, 
            Color.MAGENTA, 
            Color.CYAN, 
            Color.WHITE
        ).imshow("Corner Gradient 2");

        // Senaryo 3: RGB kanallarını anlamak için (Sadece Kırmızı ve Mavi ağırlıklı)
        CMatrix.fromColorGradientRGB(600, 800, 
            new Color(255, 0, 0), // Tam Kırmızı
            new Color(0, 0, 0),   // Siyah
            new Color(0, 0, 0),   // Siyah
            new Color(0, 0, 255)  // Tam Mavi
        ).imshow("Corner Gradient 3");
    }
}