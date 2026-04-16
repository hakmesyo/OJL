/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.awt.image.BufferedImage;
import jazari.factory.FactoryUtils;
import jazari.interfaces.call_back_interface.CallBackCamera;
import jazari.matrix.CMatrix;

/**
 *
 * @author Teknofest
 */
public class TestImageSave {

    public static void main(String[] args) {
//        CMatrix cm = CMatrix.getInstance()
//                .imread("images/bf.png")
//                .imshow()
//                .addNoiseGaussian(0, 0.5f)
//                .imshow()
//                .imsave("images", "out.bmp")
//                .imsave("images", "out.jpg")
//                .imsave("images", "out.png");
//        ;

//        CMatrix img = CMatrix.getInstance()
//                .imread("images/bf.png")
//                //.rgb2gray()
//                ;
//
//        for (int sigma : new int[]{10, 25, 50}) {
//            CMatrix noise = CMatrix.getInstance(img.clone())
//                    .addNoiseGaussian(0, (float) sigma)
//                    .imshow();
//
//        }
        CMatrix cm = CMatrix.getInstance()
                .startCamera(false, new CallBackCamera() {
                    @Override
                    public BufferedImage onFrame(BufferedImage image) {
                        return processImage(image);
                    }
                })
                .waitForKey();

    }

    private static BufferedImage processImage(BufferedImage image) {
        CMatrix cm = CMatrix
                .getInstance(image)
                .rgb2gray()
                .addNoiseGaussian(0, 12)
                .imshowRefresh();
        return image;
    }

}
