
import jazari.matrix.CMatrix;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Teknofest
 */
public class TestImageSaveFile {

    public static void main(String[] args) {
        CMatrix img = CMatrix.getInstance()
                .imread("images/pullar.png")
                .imshow("Orijinal")
                .rgb2gray()
                .imshow()
                .addNoiseGaussian(0, 20)
                .imshow()
                .filterGammaMap(5, 0.2f)
                .imshow()
                ;

        img.clone().imsave("images", "out.bmp");
        img.clone().imsave("images", "out.jpg",45);
        img.clone().imsave("images", "out.png");

    }
}
