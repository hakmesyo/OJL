
import jazari.matrix.CMatrix;

public class TestImageReconstruction {
    public static void main(String[] args) {
        CMatrix original = CMatrix.getInstance().imread("images/bird.jpg")
                .imshow("Original");
        
        // Extract channels (assume we implement getHSVChannels() or similar)
        CMatrix h = original.clone().getHueChannel();
        // Reduce saturation to 20%
        CMatrix s = original.clone().getSaturationChannel().multiplyScalar(0.2f); 
        CMatrix v = original.clone().getValueChannel();
        
        CMatrix.fromHSV(h, s, v).imshow("Reconstructed (Desaturated)");
    }
}