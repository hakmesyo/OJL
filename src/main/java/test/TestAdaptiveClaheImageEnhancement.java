/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import jazari.image_processing.AdaptiveConcurrentClahe;
import jazari.matrix.CMatrix;
import org.opencv.core.Core;

/**
 *
 * @author cezerilab
 */
public class TestAdaptiveClaheImageEnhancement {
    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        //String filePath = "images/1-126.jpg"; // Resim dosyasının yolu
//        String filePath = "images/e1.jpg"; // Resim dosyasının yolu
        String filePath = "images/bf.jpg"; // Resim dosyasının yolu
        BufferedImage image = CMatrix.getInstance().imread(filePath).imshow().getImage();
        AdaptiveConcurrentClahe clahe=new AdaptiveConcurrentClahe();
        BufferedImage img2=clahe.process(image);
        CMatrix cm = CMatrix.getInstance(img2).imshow();
    }
    
}
