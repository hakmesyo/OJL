/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.awt.image.BufferedImage;
import jazari.interfaces.call_back_interface.CallBackCamera;
import jazari.matrix.CMatrix;

/**
 *
 * @author Teknofest
 */
public class TestNoise {

    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .imread("images/bf.png")
                .rgb2gray()
                .imshow()
                .addNoiseGaussian(0, 30)
                .imshow();
        cm.startCamera(0, true, (img) -> {
            // Her kare geldiğinde bu metodu çağırıyoruz ve dönen sonucu return ediyoruz
            return processImage();
        });
    }

    private static BufferedImage processImage() {
        return null;
    }
}
