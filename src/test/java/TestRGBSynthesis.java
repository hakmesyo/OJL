
import jazari.matrix.CMatrix;

public class TestRGBSynthesis {
    public static void main(String[] args) {
        CMatrix r = CMatrix.getInstance().rand(300, 300, 0, 255); // Random Red noise
        CMatrix g = CMatrix.getInstance(300, 300, 0);            // No Green
        CMatrix b = CMatrix.getInstance(300, 300, 255);          // Full Blue
        
        CMatrix.fromRGB(r, g, b).imshow("Synthesized RGB");
    }
}