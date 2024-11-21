/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.io.File;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestAlmondClassification {

    int[][] griGoruntu = {
        {100, 150, 200},
        {50, 75, 125},
        {25, 60, 80}
    };

    static String path = "D:\\DATASETS\\classification\\almond_dataset";

    public static void main(String[] args) {
        preAnalysis(path + "/DAMAGED/Damaged (221).JPG");
        preAnalysis(path + "/NODAMAGE/nodamage (85).JPG");
//        checkRoughness("NODAMAGE");
//        checkRoughness("DAMAGED");
    }

    private static void preAnalysis(String p) {
        CMatrix cm = CMatrix.getInstance()
                .imread(p)
                //.imshowAutoResized(true)
                .rgb2gray()
                //.imshowAutoResized(true)
                .filterGaussian(3)
                //                .filterGaussian(3)
                //                .filterGaussian(3)
                //.imshowAutoResized(true)
                .edgeDetectionCanny()
                .imshowAutoResized(true);
        float m = cm.meanTotal();
        System.out.println("mean = " + m);
    }

    private static void checkRoughness(String className) {
        String p = path + "/" + className;
        File[] imageFiles = FactoryUtils.getFileArrayInFolderByExtension(p, "JPG");
        float[] means = new float[imageFiles.length];
        int i = 0;
        for (File imageFile : imageFiles) {
            CMatrix cm = CMatrix.getInstance()
                    .imread(imageFile)
                    .rgb2gray()
                    .filterGaussian(3)
                    .filterGaussian(3)
                    .filterGaussian(3)
                    .edgeDetectionCanny();
            means[i] = cm.meanTotal();
            System.out.println(imageFile.getName() + ":" + means[i]);
            i++;
        }
        CMatrix cm = CMatrix.getInstance(means).transpose().plot();
    }
}
