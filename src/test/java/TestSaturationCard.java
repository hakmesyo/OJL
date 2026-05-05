
import jazari.matrix.CMatrix;

public class TestSaturationCard {
    public static void main(String[] args) {
        CMatrix h = CMatrix.getInstance(256, 256, 0); // Fixed Red (0)
        CMatrix s = CMatrix.getInstance().range(0, 256).replicateColumn(256); // Varying Saturation
        CMatrix v = CMatrix.getInstance(256, 256, 255); // Fixed full brightness
        
        CMatrix.fromHSV(h, s, v).imshow("Saturation Gradient (Red Base)");
    }
}