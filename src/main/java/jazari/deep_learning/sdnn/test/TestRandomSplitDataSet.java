/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.sdnn.test;

import java.io.File;
import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class TestRandomSplitDataSet {

    static String path = "C:\\Users\\cezerilab\\Downloads\\cats_dogs\\train_transformed";

    public static void main(String[] args) {
        //split2Dir();
        FactoryUtils.splitTrainValidTestFolder(path, 0.7f, 0.1f, 0.2f);

    }

    private static void split2Dir() {
        FactoryUtils.makeDirectory(path + "/cats");
        FactoryUtils.makeDirectory(path + "/dogs");
        File[] files = FactoryUtils.getFileArrayInFolderForImages(path);
        for (File file : files) {
            if (file.getName().startsWith("c")) {
                FactoryUtils.copyFile(file, new File(path + "/cats/" + file.getName()));
            } else if (file.getName().startsWith("d")) {
                FactoryUtils.copyFile(file, new File(path + "/dogs/" + file.getName()));
            }
        }
    }
}
