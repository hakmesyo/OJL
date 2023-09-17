/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.factory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
            float[][][][] dat = ds.getDataX();
            for (int i = 0; i < dat.length; i++) {
                dat[i][0] = FactoryNormalization.normalizeWithRange(dat[i][0], min, max);
            }
        }else{
            float[][][][] dat = ds.getDataX();
            for (int i = 0; i < dat.length; i++) {
                dat[i][0] = FactoryNormalization.normalizeWithRange(dat[i][0], min, max);
                dat[i][1] = FactoryNormalization.normalizeWithRange(dat[i][1], min, max);
                dat[i][2] = FactoryNormalization.normalizeWithRange(dat[i][2], min, max);
            }
        }
        return ds;
    }
    
    public static float[][][][] normalizeDataSetMinMax(float[][][][] data, float min, float max) {
        if (data[0].length == 1) {
            for (int i = 0; i < data.length; i++) {
                data[i][0] = FactoryNormalization.normalizeWithRange(data[i][0], min, max);
            }
        }else{
            for (int i = 0; i < data.length; i++) {
                data[i][0] = FactoryNormalization.normalizeWithRange(data[i][0], min, max);
                data[i][1] = FactoryNormalization.normalizeWithRange(data[i][1], min, max);
                data[i][2] = FactoryNormalization.normalizeWithRange(data[i][2], min, max);
            }
        }
        return data;
    }

    public static DataSet loadDataSetFromCSV(String csvPath, int nChannel, int nClasses, int w, int h, int classLabelIndex) {
        DataSet ds = new DataSet();
        ds.nClasses = nClasses;
        float[][] d = FactoryUtils.readCSV_slow(csvPath, ',', 0);
        float[][] tr_d = FactoryMatrix.transpose(d);
        float[] y = (classLabelIndex == -1) ? tr_d[tr_d.length - 1] : tr_d[classLabelIndex];
        float[][] dataRows = new float[tr_d.length - 1][tr_d[0].length];
        if (classLabelIndex == -1) {//means class labels at the end
            for (int i = 0; i < dataRows.length; i++) {
                dataRows[i] = tr_d[i];
            }
        } else if (classLabelIndex == 0) {//means class labels at the start
            for (int i = 0; i < dataRows.length; i++) {
                dataRows[i] = tr_d[i + 1];
            }
        }
        int nr = y.length;
        dataRows = FactoryMatrix.transpose(dataRows);
        float[][][][] data = new float[nr][nChannel][w][h];
        for (int i = 0; i < nr; i++) {
            for (int t = 0; t < nChannel; t++) {
                for (int j = 0; j < w; j++) {
                    for (int k = 0; k < h; k++) {
                        data[i][t][j][k] = dataRows[i][t * w * h + j * h + k];
                    }
                }
            }
        }
        ds.classLabelIndex = FactoryUtils.getUniqueValues(y);
        ds.classLabelIndex = FactoryUtils.sortArrayAscend(ds.classLabelIndex);
        ds.nChannel = nChannel;

        if (nChannel == 1) {
            for (int i = 0; i < nr; i++) {
                Data dt = new Data();
                dt.classLabelIndex = (int) y[i];
                dt.gray = data[i][0];
                ds.data.add(dt);
            }
        } else {
            for (int i = 0; i < nr; i++) {
                Data dt = new Data();
                dt.classLabelIndex = (int) y[i];
                dt.red = data[i][0];
                dt.green = data[i][1];
                dt.blue = data[i][2];
                ds.data.add(dt);
            }
        }
        return ds;
    }
    
    public static DataSet loadDataSetFromCsvFilterOneClass(String csvPath, int filterClassIndex, int nChannel, int nClasses, int w, int h, int classLabelIndex) {
        DataSet ds = new DataSet();
        ds.nClasses = nClasses;
        float[][] d = FactoryUtils.readCSV_slow(csvPath, ',', 0);
        int n=d.length;
        int lastIndex=d[0].length;
        List<float[]> lst=new ArrayList<>();
        if (classLabelIndex == -1) {//means class labels at the end
            for (int i = 0; i < n; i++) {
                if (d[i][lastIndex-1]==filterClassIndex) {
                    lst.add(d[i]);
                }
            } 
        }else if(classLabelIndex == 0) {//means class labels at the start
            for (int i = 0; i < n; i++) {
                if (d[i][0]==filterClassIndex) {
                    lst.add(d[i]);
                }
            } 
        }
        
        d=new float[lst.size()][lastIndex];
        int q=0;
        for (float[] fs : lst) {
            d[q++]=fs;
        }
        
        
        float[][] tr_d = FactoryMatrix.transpose(d);
        float[] y = (classLabelIndex == -1) ? tr_d[tr_d.length - 1] : tr_d[classLabelIndex];
        float[][] dataRows = new float[tr_d.length - 1][tr_d[0].length];
        if (classLabelIndex == -1) {//means class labels at the end
            for (int i = 0; i < dataRows.length; i++) {
                dataRows[i] = tr_d[i];
            }
        } else if (classLabelIndex == 0) {//means class labels at the start
            for (int i = 0; i < dataRows.length; i++) {
                dataRows[i] = tr_d[i + 1];
            }
        }
        int nr = y.length;
        dataRows = FactoryMatrix.transpose(dataRows);
        float[][][][] data = new float[nr][nChannel][w][h];
        for (int i = 0; i < nr; i++) {
            for (int t = 0; t < nChannel; t++) {
                for (int j = 0; j < w; j++) {
                    for (int k = 0; k < h; k++) {
                        data[i][t][j][k] = dataRows[i][t * w * h + j * h + k];
                    }
                }
            }
        }
        //ds.classLabelIndex = FactoryUtils.getUniqueValues(y);
        ds.classLabelIndex = FactoryMatrix.range1D(0, nClasses, 1);
        ds.nChannel = nChannel;

        if (nChannel == 1) {
            for (int i = 0; i < nr; i++) {
                Data dt = new Data();
                dt.classLabelIndex = (int) y[i];
                dt.gray = data[i][0];
                ds.data.add(dt);
            }
        } else {
            for (int i = 0; i < nr; i++) {
                Data dt = new Data();
                dt.classLabelIndex = (int) y[i];
                dt.red = data[i][0];
                dt.green = data[i][1];
                dt.blue = data[i][2];
                ds.data.add(dt);
            }
        }
        return ds;
    }

}
