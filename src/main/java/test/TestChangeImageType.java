/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.awt.image.BufferedImage;
import java.io.File;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;

/**
 *
 * @author cezerilab
 */
public class TestChangeImageType {
    public static void main(String[] args) {
        String path="C:\\Users\\cezerilab\\Desktop\\UYZ_veriseti\\Train";
        FactoryUtils.makeDirectory(path+"/ds");
        File[] files=FactoryUtils.getFileArrayInFolder(path);
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            BufferedImage img=ImageProcess.imread(file);
            ImageProcess.saveImage(img, file.getParent()+"/ds/"+System.currentTimeMillis()+".jpg");
        }
    }
    
}
