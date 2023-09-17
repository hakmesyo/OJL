/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.sdnn.test;

import java.io.File;
import jazari.factory.FactoryUtils;

/**
 *
 * @author dell_lab
 */
public class TestShuffleDataSet {

    public static String path = "D:\\ai\\djl\\pistachio_224_224";

    public static void main(String[] args) {
//        collectToDS();
//        organizeDS();
    }

    private static void collectToDS() {
        File[] files = FactoryUtils.getFileListDataSetForImageClassification(path + "/test/open");
        for (int i = 0; i < files.length; i++) {
            FactoryUtils.copyFile(files[i], new File(path + "/ds/open/" + System.currentTimeMillis() + ".jpg"));
        }
    }

    private static void organizeDS() {
        File[] files = FactoryUtils.getFileListDataSetForImageClassification(path + "/ds/open");
        files = FactoryUtils.shuffle(files, 123);
        FactoryUtils.makeDirectory(path + "/ds/train");
        FactoryUtils.makeDirectory(path + "/ds/train/close");
        FactoryUtils.makeDirectory(path + "/ds/train/open");
        FactoryUtils.makeDirectory(path + "/ds/test");
        FactoryUtils.makeDirectory(path + "/ds/test/close");
        FactoryUtils.makeDirectory(path + "/ds/test/open");

        for (int i = 0; i < files.length; i++) {
            if (i < 50) {
                FactoryUtils.copyFile(files[i], new File(path + "/ds/test/open/" + files[i].getName()));
            }else{
                FactoryUtils.copyFile(files[i], new File(path + "/ds/train/open/" + files[i].getName()));
            }
        }

    }
}
