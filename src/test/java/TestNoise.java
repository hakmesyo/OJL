/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import java.awt.image.BufferedImage;
import jazari.interfaces.call_back_interface.CallBackCamera;
import jazari.matrix.CMatrix;

/**
 *
 * @author Teknofest
 */
public class TestNoise {

    public static void main(String[] args) {

        /*
        Gaussian Noise sigma=25
         */
        CMatrix cmGaussian = CMatrix.getInstance()
                .imread("images/pullar.png")
                //.rgb2gray()
                //.imshow()
                .addNoiseGaussian(0, 25) //.imshow()
                ;

        /*
        Salt and Pepper Noise yüzde 5
         */
        CMatrix cmPepperNoise = CMatrix.getInstance()
                .imread("images/pullar.png")
                //.rgb2gray()
                //.imshow()
                .addNoiseSaltAndPepper(0.05f) //.imshow()                
                ;

        /*
        Speckle Noise yüzde 10
         */
        float sigma = 0.1f; // Daha makul bir gürültü
        CMatrix cmSpeckleNoise = CMatrix.getInstance()
                .imread("images/pullar.png")
                .rgb2gray()
                .imshow("Original")
                .addNoiseSpeckle(sigma)
                .imshow("Noisy")
                ;
        cmSpeckleNoise.clone().filterMean(5).imshow("filter mean");
        cmSpeckleNoise.clone().filterMedian(5).imshow("filter median");
        cmSpeckleNoise.clone().filterGaussian(5,3).imshow("filter gaussian");
        cmSpeckleNoise.clone().filterLee(5, 0.1f).imshow("filter lee");
        cmSpeckleNoise.clone().filterKuan(5, 0.1f).imshow("filter Kuan");
        cmSpeckleNoise.clone().filterFrost(5, 0.1f,2).imshow("filter Frost");
        cmSpeckleNoise.clone().filterGammaMap(5, 0.1f).imshow("filter gamma map");
        
    }

}
