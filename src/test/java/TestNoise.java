/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.awt.image.BufferedImage;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;
import jazari.interfaces.call_back_interface.CallBackCamera;
import jazari.matrix.CMatrix;

/**
 *
 * @author Teknofest
 */
public class TestNoise {

    private static BufferedImage process(BufferedImage image) {
        return cm.setImage(image)
                .rgb2gray()
                .imFlipHorizontal()
                .filterMosaic(25)
                .imshowRefresh("processed image")
                .getBufferedImage();
    }

    private static CMatrix cm = CMatrix.getInstance();

    public static void main(String[] args) {
        CMatrix img = CMatrix.getInstance()
                .imread("images/pullar.png")
                ;
        img.clone()
                .addNoise(10).imshow("standard");
        img.clone()
                .addNoiseSpeckle(0.15f).imshow("speckle");
        img.clone()
                .addNoiseGaussian(0,30).imshow("Gaussian");
        img.clone()
                .addNoiseSaltAndPepper(0.05f).imshow("Salt-Pepper");
        img.clone()
                .addNoise(10)                
                .addNoiseGaussian(0, 30)
                .addNoiseSpeckle(0.15f)
                .addNoiseSaltAndPepper(0.05f)
                .imshow("Karma Gürültü");

//        CMatrix orig = CMatrix.getInstance().imread("images/bird.jpg").rgb2gray();
//        CMatrix noisy = orig.clone().addNoise(2);
//        // 1. Sinyal Gücü (Orijinal resmin ortalaması veya RMS değeri)
//        float sigPow = orig.meanTotal();
//        // 2. Gürültü Gücü (Hatalı: noisy.stdTotal() -> Doğru: noise bileşeni)
//        // İki matrisin farkının standart sapmasını alıyoruz
//        double noiPow = noisy.clone().minus(orig).stdTotal();
//        // 3. SNR dB Hesaplama (20 * log10(genlik_oranı))
//        double snr = 20 * Math.log10(sigPow / (noiPow + 0.0001)); // 0'a bölme koruması
//        System.out.println("SNR = " + snr + " dB");
//        CMatrix.getInstance()
//                .startCamera(0, false,new CallBackCamera() {
//                    @Override
//                    public BufferedImage onFrame(BufferedImage image) {
//                        cm=cm.setImage(image);
//                        cm.clone().imshow("original");
//                        cm.clone().rgb2gray().imshow("gray");
//                        cm.clone().getRedChannelColor().imshow("red channel");
//                        cm.clone().getGreenChannelColor().imshow("green channel");
//                        cm.clone().getBlueChannelColor().imshow("blue channel");
//                        cm.clone().rgb2hsv().imshow("hsv");
//                        return image;
//                    }
//
//                }).waitForKey();
//        CMatrix orig = CMatrix.getInstance()
//                .imread("images/bird.jpg")
//                //.rgb2gray()
//                .imshow("original")
//                ;
//
//        System.out.println("Orijinal:");
//        System.out.println("mean=" + orig.clone().mean().mean());
//        System.out.println("std =" + orig.clone().std().mean());
//
//
//        CMatrix noisy=orig.clone().addNoise(30).imshow("noisy");
//        System.out.println("Gürültülü:");
//        System.out.println("mean=" + noisy.clone().mean().mean());
//        System.out.println("std =" + noisy.clone().std().std());
//        
//        orig.clone().addNoiseGaussian(0,30).imshow("noisy gaussian");
//        CMatrix img = CMatrix.getInstance()
//                .imread("images/bf.png")
//                .rgb2gray()
//                .imshow("gray",0.5f)
//                ;
//
//        for (int sigma : new int[]{10, 25, 50}) {
//            CMatrix noise = CMatrix.getInstance(img.clone())
//                    .addNoiseGaussian(0, (float) sigma)
//                    .imshow("sigma:"+sigma,0.5f);
//
//        }
//        /*
//        Gaussian Noise sigma=25
//         */
//        CMatrix cmGaussian = CMatrix.getInstance()
//                .imread("images/pullar.png")
//                //.rgb2gray()
//                //.imshow()
//                .addNoiseGaussian(0, 25) //.imshow()
//                ;
//
//        /*
//        Salt and Pepper Noise yüzde 5
//         */
//        CMatrix cmPepperNoise = CMatrix.getInstance()
//                .imread("images/pullar.png")
//                //.rgb2gray()
//                //.imshow()
//                .addNoiseSaltAndPepper(0.05f) //.imshow()                
//                ;
//
//        /*
//        Speckle Noise yüzde 10
//         */
//        float sigma = 0.1f; // Daha makul bir gürültü
//        CMatrix cmSpeckleNoise = CMatrix.getInstance()
//                .imread("images/pullar.png")
//                .rgb2gray()
//                .imshow("Original")
//                .addNoiseSpeckle(sigma)
//                .imshow("Noisy");
//        cmSpeckleNoise.clone().filterMean(5).imshow("filter mean");
//        cmSpeckleNoise.clone().filterMedian(5).imshow("filter median");
//        cmSpeckleNoise.clone().filterGaussian(5, 3).imshow("filter gaussian");
//        cmSpeckleNoise.clone().filterLee(5, 0.1f).imshow("filter lee");
//        cmSpeckleNoise.clone().filterKuan(5, 0.1f).imshow("filter Kuan");
//        cmSpeckleNoise.clone().filterFrost(5, 0.1f, 2).imshow("filter Frost");
//        cmSpeckleNoise.clone().filterGammaMap(5, 0.1f).imshow("filter gamma map");
    }

}
