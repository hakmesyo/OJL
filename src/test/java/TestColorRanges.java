
import jazari.matrix.CMatrix;

public class TestColorRanges {
    public static void main(String[] args) {
        // Data: 0 to 180
        CMatrix data = CMatrix.getInstance().range(0, 181).replicateColumn(181);
        CMatrix s = CMatrix.getInstance(181, 181, 255);
        CMatrix v = CMatrix.getInstance(181, 181, 255);

        // Case 1: OpenCV Standard (Hue Max = 180) -> Full spectrum shown
        CMatrix.fromHSV(data, s, v, 180, 255, 255).imshow("OpenCV Scale (Full Spectrum)");

        // Case 2: Degrees Scale (Hue Max = 360) -> Only half spectrum (0-180 degrees) shown
        CMatrix.fromHSV(data, s, v, 360, 255, 255).imshow("Degrees Scale (Half Spectrum)");
    }
}