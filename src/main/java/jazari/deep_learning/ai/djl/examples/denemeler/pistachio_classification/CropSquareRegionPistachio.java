/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ai.djl.examples.denemeler.pistachio_classification;

import java.awt.image.BufferedImage;
import java.io.File;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;

/**
 *
 * @author cezerilab
 */
public class CropSquareRegionPistachio {
    public static void main(String[] args) {
        String path="C:/ai/djl/pistachio_binary";
        File[] files=FactoryUtils.getFileArrayInFolderByExtension(path+"/close", "jpg");
        for (File file : files) {
            BufferedImage img=ImageProcess.imread(file);
            img=ImageProcess.cropImageWithCentered224(img);
            ImageProcess.saveImage(img, path+"/close/"+file.getName());
        }
    }
}
