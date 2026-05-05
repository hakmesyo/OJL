
import java.awt.Color;
import jazari.matrix.CMatrix;

public class TestGradientComparison {
    public static void main(String[] args) {
        Color c1 = Color.RED;
        Color c2 = Color.BLUE;
        Color c3 = Color.GREEN;
        Color c4 = Color.YELLOW;

        // RGB Modu: Renkler "mat" ve doğrudan karışır.
        CMatrix.fromColorGradientRGB(600, 800, c1, c2, c3, c4)
               .imshow("RGB Based Gradient (Linear)");

        // HSV Modu: Renkler renk çemberi üzerinden "canlı" ve gökkuşağımsı geçer.
        CMatrix.fromColorGradientHSV(600, 800, c1, c2, c3, c4)
               .imshow("HSV Based Gradient (Perceptual)");
    }
}