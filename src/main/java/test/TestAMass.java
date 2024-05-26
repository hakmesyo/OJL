/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestAMass {

    public static void main(String[] args) {
        String path = "D:\\DATASETS\\MASS\\CBIS_DDSM_JPG\\corrected_ds";
        //deneme();
        FactoryUtils.makeDirectory(path + "/yolods");
        FactoryUtils.makeDirectory(path + "/yolods/calc");
        FactoryUtils.makeDirectory(path + "/yolods/calc/train");
        FactoryUtils.makeDirectory(path + "/yolods/calc/test");
        FactoryUtils.makeDirectory(path + "/yolods/mass");
        FactoryUtils.makeDirectory(path + "/yolods/mass/train");
        FactoryUtils.makeDirectory(path + "/yolods/mass/test");
        //generateYoloTxt4OJL(path + "/calc/test", "D:\\DATASETS\\MASS\\CBIS_DDSM_JPG\\csv\\calc_test.csv", path + "/yolods/calc/test");
        //generateYoloTxt4OJL(path + "/calc/train", "D:\\DATASETS\\MASS\\CBIS_DDSM_JPG\\csv\\calc_train.csv", path + "/yolods/calc/train");
        //generateYoloTxt4OJL(path + "/mass/test", "D:\\DATASETS\\MASS\\CBIS_DDSM_JPG\\csv\\mass_test.csv", path + "/yolods/mass/test");
        generateYoloTxt4OJL(path + "/mass/train", "D:\\DATASETS\\MASS\\CBIS_DDSM_JPG\\csv\\mass_train.csv", path + "/yolods/mass/train");
    }

    private static void deneme() {
        String pp = "D:\\DATASETS\\MASS\\CBIS_DDSM_JPG\\corrected_ds\\mass\\train";
        CMatrix cm = CMatrix.getInstance()
                .imread(pp + "/benign_Mass-Training_P_00027_RIGHT_MLO_1_1.jpg")
                .rgb2gray();
        Rectangle rect = FactoryUtils.getWeightCenteredROIAsRectangle(cm.toFloatArray2D());
        System.out.println("rect = " + rect);
//        BufferedImage img=ImageProcess.imread(pp+"/benign_Mass-Training_P_00027_RIGHT_MLO_0.jpg");
//        img=ImageProcess.drawRectangle(img, rect, 5, Color.yellow);
//        CMatrix.getInstance(img).imshow();
        int w = 541;
        int h = 850;
        String txt = FactoryUtils.toYoloNativeTxtFormat(0, rect, w, h);
        System.out.println("txt = " + txt);
        FactoryUtils.saveFile(pp + "/benign_Mass-Training_P_00027_RIGHT_MLO_0.txt", txt);
    }

    private static void generateYoloTxt4OJL(String dsPath, String csvPath, String targetPath) {
        File[] files = FactoryUtils.getFileArrayInFolderByExtension(dsPath, "jpg");
        List<File> lst = Arrays.asList(files);
        String[] content = FactoryUtils.readFile(csvPath).split("\n");
        for (int i = 1; i < content.length; i += 2) {
            String[] s = content[i].split(",");
            String ss = s[11].split("/")[0];
            List<File> filteredFiles = lst.stream()
                    .filter(file -> file.getName().contains(ss))
                    .collect(Collectors.toList());
            String name = "";
            String txt = "";
            for (File filteredFile : filteredFiles) {
                if (filteredFile == filteredFiles.get(0)) {
                    FactoryUtils.copyFile(filteredFile, new File(targetPath + "/" + filteredFile.getName()));
                    name = FactoryUtils.getFileName(filteredFile.getName()) + ".txt";
                } else {
                    BufferedImage img = ImageProcess.imread(filteredFile);
//                    if (filteredFile.getAbsolutePath().equals("D:\\DATASETS\\MASS\\CBIS_DDSM_JPG\\corrected_ds\\calc\\train\\malignant_Calc-Training_P_00266_LEFT_MLO_1_1.jpg")) {
//                        System.out.println("burada dur");
//                    }
                    float[][] f = ImageProcess.to2DFloat(img);
                    Rectangle rect=null;
                    try {
                        rect = FactoryUtils.getWeightCenteredROIAsRectangle(f);
                    } catch (Exception e) {
                        System.out.println("************************* hata ******************* -->"+e.toString());
                    }
                    //1 0.68960524 0.5822785 0.23180008 0.1107595
                     
                    int w = img.getWidth();
                    int h = img.getHeight();
                    if (1.0f*rect.width/w > 0.95 || 1.0f*rect.height/h > 0.95) {
                        continue;
                    }
                    int classIndex = name.contains("benign") ? 0 : 1;
                    txt += FactoryUtils.toYoloNativeTxtFormat(classIndex, rect, w, h) + "\n";
                }
            }
            System.out.println("txt = " + txt);
            FactoryUtils.saveFile(targetPath + "/" + name, txt);
        }
    }
}
