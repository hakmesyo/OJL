/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.factory;

import java.awt.image.BufferedImage;
import java.io.File;
import jazari.image_processing.ImageProcess;
import jazari.utils.Data;
import jazari.utils.DataSet;

/**
 *
 * @author cezerilab
 */
public class FactoryDataSetLoader {

    public static DataSet loadDataSetFromImage(String imageFolderPath, String channelType) {
        DataSet ds = new DataSet();
        File[] dirs = FactoryUtils.getDirectories(imageFolderPath);
        ds.nClasses = dirs.length;
        float[] y = new float[dirs.length];
        //float scale = 1.0f / 255.0f;
        BufferedImage img;
        if (channelType.equals("gray")) {
            float[][] gray = null;
            for (int i = 0; i < dirs.length; i++) {
                y[i] = i;
                File[] files = FactoryUtils.getFileArrayInFolderForImages(dirs[i].getAbsolutePath());
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        Data data = new Data();
                        img = ImageProcess.imread(file);
                        gray = ImageProcess.bufferedImageToArray2D(img);
                        //gray = FactoryUtils.timesScalar(gray, scale);
                        data.gray = gray;
                        data.classLabelIndex = i;
                        ds.data.add(data);
                    }
                }
            }
            ds.classLabelIndex = y;
        } else if (channelType.equals("rgb")) {
            ds.nChannel = 3;
            float[][] red = null;
            float[][] green = null;
            float[][] blue = null;
            for (int i = 0; i < dirs.length; i++) {
                y[i] = i;
                File[] files = FactoryUtils.getFileArrayInFolderForImages(dirs[i].getAbsolutePath());
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        Data data = new Data();
                        img = ImageProcess.imread(file);
                        red = ImageProcess.bufferedImageToArray2D(ImageProcess.getRedChannelColor(img));
                        green = ImageProcess.bufferedImageToArray2D(ImageProcess.getGreenChannelColor(img));
                        blue = ImageProcess.bufferedImageToArray2D(ImageProcess.getBlueChannelColor(img));
                        data.red = red;
                        data.green = green;
                        data.blue = blue;
                        data.classLabelIndex = i;
                        ds.data.add(data);
                    }
                }
            }
            ds.classLabelIndex = y;

        }
        return ds;
    }

    public static DataSet normalizeDataSetMinMax(DataSet ds, float min, float max) {
        if (ds.nChannel == 1) {
            float[][][][] dat = ds.getTrainX();
            for (int i = 0; i < dat.length; i++) {
                dat[i][0] = FactoryNormalization.normalizeWithRange(dat[i][0], min, max);
            }
        }
        return ds;
    }

    public static DataSet loadDataSetFromCSV(String csvPath, int nClasses, int w, int h) {
        DataSet ds = new DataSet();
        ds.nClasses = nClasses;
        float[][] d = FactoryUtils.readCSV(csvPath, ',', 0);
        float[][] tr_d = FactoryMatrix.transpose(d);
        float[] y = tr_d[0];
        float[][] dataRows = new float[tr_d.length - 1][tr_d[0].length];
        for (int i = 0; i < dataRows.length; i++) {
            dataRows[i] = tr_d[i + 1];
        }
        int nr = y.length;
        dataRows = FactoryMatrix.transpose(dataRows);
        float[][][] data = new float[nr][w][h];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < w; j++) {
                for (int k = 0; k < h; k++) {
                    data[i][j][k] = dataRows[i][j * h + k];
                }
            }
        }
        ds.classLabelIndex = FactoryUtils.getUniqueValues(y);
        ds.nChannel = 1;

        for (int i = 0; i < nr; i++) {
            Data dt = new Data();
            dt.classLabelIndex = (int) y[i];
            dt.gray = data[i];
            ds.data.add(dt);
        }
        return ds;
    }

}
