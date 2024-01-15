/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.Map;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestCalculateFileHistogramInFolder {
    static String path = "D:\\Dropbox\\NetbeansProjects\\LaserWeedingImageAcquisitionApp\\images\\ds";
    public static void main(String[] args) {
        //byFileName(path);
        byFolderName("C:\\Users\\cezerilab\\Desktop\\structured_folder");
    }

    private static void byFileName(String path) {
        Map map=FactoryUtils.getHashMapHistogramByFileName(path,"_");
        String[] labels=FactoryUtils.resolveHashMapToLabels(map);
        float[] val=FactoryUtils.resolveHashMapToArray(map);
        CMatrix cm = CMatrix.getInstance(val)
                .showBar(labels)
                ;
    }

    private static void byFolderName(String path) {
        Map map=FactoryUtils.getHashMapHistogramByFolderName(path);
        String[] labels=FactoryUtils.resolveHashMapToLabels(map);
        float[] val=FactoryUtils.resolveHashMapToArray(map);
        CMatrix cm = CMatrix.getInstance(val)
                .showBar(labels)
                ;
    }
}
