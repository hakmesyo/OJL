/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.awt.image.BufferedImage;
import jazari.image_processing.ImageProcess;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestSplitImageBndBox {

    public static void main(String[] args) {
        //String path = "D:\\Dropbox\\LibGDXProjects\\JumpyHero\\assets\\images\\entities\\JumpPlant2.png";
        String path = "D:\\Dropbox\\LibGDXProjects\\JumpyHero\\assets\\images\\entities\\SlimeOrange-outline.png";
        BufferedImage img = ImageProcess.imread(path);
        BufferedImage[][] imgs = ImageProcess.tileImage(img, 5, 6);
        
//        System.out.println(imgs.length);
//        CMatrix cm;
        int k=0;
        for (int i = 0; i < imgs.length; i++) {
            for (int j = 0; j < imgs[0].length; j++) {
                CMatrix.getInstance(imgs[i][j]).imshow();
                ImageProcess.saveImage(imgs[i][j], "D:\\Dropbox\\LibGDXProjects\\JumpyHero\\assets\\images\\entities\\plant_"+k+".png");
                k++;
            }
        }
    }
}
