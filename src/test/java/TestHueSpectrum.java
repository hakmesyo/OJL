
import jazari.matrix.CMatrix;

public class TestHueSpectrum {
    public static void main(String[] args) {
        // Generate a horizontal gradient from 0 to 255
        CMatrix hue = CMatrix.getInstance().range(0, 256).replicateColumn(256);
        
        // Build the card: Hue varies, Saturation and Value are fixed at max (255)
        CMatrix.fromHSV(hue, 255, 255).imshow("Hue Color Card");
    }
}