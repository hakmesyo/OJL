/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.snn;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;

/**
 *
 * @author cezerilab
 */
public class TestPng2Csv {
    public static void main(String[] args) {
        String path="C:\\ai\\djl\\cifar10\\png\\test";
        String path_label="C:\\ai\\djl\\cifar10\\png\\labels.txt";
        
        String[] class_labels=FactoryUtils.readFile(path_label).split("\n");
        File[] dirs=FactoryUtils.getDirectories(path);
        
        List<String> rows=new ArrayList();
        for (int i = 0; i < dirs.length; i++) {
            File[] files=FactoryUtils.getFileArrayInFolderByExtension(path+"/"+dirs[i].getName(), "png");
            int k=0;
            for (File file : files) {
                BufferedImage img=ImageProcess.imread(file);
                //float[][][] d=ImageProcess.imageToPixelsColorFloatFaster(img);
                int[][][] d=ImageProcess.imageToPixelsColorIntFaster(img);
                String row=getCsvStringRowFromPixelArray(d,dirs[i].getName(),class_labels);
                rows.add(row);
                System.out.println(i+":"+(k++)+".file processed");
            }
        }
        FactoryUtils.writeToFile(path+"/test.csv", rows,500);
        
    }

    private static String getCsvStringRowFromPixelArray(int[][][] d, String dirName, String[] class_labels) {
        String ret="";
        int nr=d[0].length;
        int nc=d[0][0].length;
        for (int f = 0; f < 3; f++) {
            for (int i = 0; i < nr; i++) {
                for (int j = 0; j < nc; j++) {
                    ret=ret.concat(d[f+1][i][j]+",");
                }
            }
        }
        int index=getIndex(dirName,class_labels);
        if (index==-1) {
            System.err.println("index is -1");
            System.exit(-1);
        }
        ret=ret.concat(index+"");
        return ret;
    }

    private static int getIndex(String dirName, String[] class_labels) {
        int ret=-1;
        for (int i = 0; i < class_labels.length; i++) {
            if (dirName.equals(class_labels[i])) {
                ret=i;
            }
        }
        return ret;
    }
}
