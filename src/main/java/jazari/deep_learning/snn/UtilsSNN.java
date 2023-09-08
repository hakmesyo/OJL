/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.snn;

import com.google.gson.reflect.TypeToken;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;

/**
 *
 * @author cezerilab
 */
public class UtilsSNN {

    public static float getRandomWeight(Random rnd) {
        return (float) (-1.0 + (rnd.nextDouble() * 2));
    }

    public static void dump(Layer layer) {
        System.out.println("");
        for (int i = 0; i < layer.filters.length; i++) {
            Filter filter = layer.filters[i];
            for (int j = 0; j < filter.nodes.length; j++) {
                for (int k = 0; k < filter.nodes[0].length; k++) {
                    Node node = filter.nodes[j][k];
                    System.out.println(node);
                }
            }
        }
    }

    public static float applyActivation(float sum, ActivationType activationType) {
        if (activationType == ActivationType.sigmoid) {
            return sigmoid(sum);
        } else if (activationType == ActivationType.relu) {
            return relu(sum);
        } else if (activationType == ActivationType.identity) {
            return sum;
        } else {
            return -1.0f;
        }
    }

    public static float[] softmax(float[] input) {
        float[] output = new float[input.length];
        float sum = 0.0f;

        // Compute the exponentials of each element in the input array
        for (int i = 0; i < input.length; i++) {
            output[i] = (float) Math.exp(input[i]);
            sum += output[i];
        }

        // Normalize by dividing each element by the sum of exponentials
        for (int i = 0; i < output.length; i++) {
            output[i] /= sum;
        }

        return output;
    }

    private static float sigmoid(float x) {
        float ret = (float) (1 / (1 + Math.pow(Math.E, -1 * x)));
        return ret;
    }

    public static float sigmoidDerivative(float x) {
        float ret = sigmoid(x) * (1 - sigmoid(x));
        return ret;
    }
    
    public static float applyDerivativeFunction(ActivationType act,float val){
        if (act==ActivationType.relu) {
            return reluDerivative(val);
        }else if(act==ActivationType.sigmoid){
            return sigmoidDerivative(val);
        }else{
            return -1;
        }
    }

    private static float relu(float x) {
//        if (x>10) {
//            return 10;
//        }
        float ret = Math.max(0, x);
        return ret;
    }

    private static float reluDerivative(float x) {
        float ret = 0;
        if (x > 0) {
            ret = 1;
        }
        return ret;
    }

    public static void softmax(Node[][] nodes) {
        int nrows = nodes.length;
        int ncols = nodes[0].length;
        float exp_z_sum = 0.0f;
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                exp_z_sum += Math.pow(Math.E, nodes[i][j].dataOut);
            }
        }
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                nodes[i][j].dataOut = (float) Math.pow(Math.E, nodes[i][j].dataOut) / exp_z_sum;
            }
        }
    }

    public static LinkedHashMap<String, List<String>> loadData(String path) {
        LinkedHashMap<String, List<String>> dataset = new LinkedHashMap<>();
        File[] dirs = FactoryUtils.getDirectories(path);
        List<String> lst_input = new ArrayList();
        List<String> lst_output = new ArrayList();
        for (File dir : dirs) {
            File[] files = FactoryUtils.getFileArrayInFolderForImages(dir.getAbsolutePath());
            for (File file : files) {
                lst_input.add(file.getAbsolutePath());
                lst_output.add(Arrays.toString(FactoryUtils.getOneHotEncoding(dirs.length, dir.getName())));
            }
        }
        //long seed = System.nanoTime();
        long seed=123;
        Collections.shuffle(lst_input, new Random(seed));
        Collections.shuffle(lst_output, new Random(seed));
        dataset.put("X", lst_input);
        dataset.put("Y", lst_output);
        return dataset;
    }

    public static float[][] to2dFloat(List<String> y_tr) {
        float[][] ret = new float[y_tr.size()][];
        int k = 0;
        for (String s : y_tr) {
            ret[k++] = getArray(s);
        }
        return ret;
    }

    private static float[] getArray(String s) {
        s = s.replace("[", "");
        s = s.replace("]", "");
        String[] str = s.split(",");
        float[] ret = new float[str.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Float.parseFloat(str[i]);
        }
        return ret;
    }

    public static float[][][][] loadData(List<String> X_tr, boolean isGray, int nFilter, int IMG_WIDTH, int IMG_HEIGHT) {
        BufferedImage img;
        int k = 0;
        float[][][][] ret = new float[X_tr.size()][nFilter][IMG_HEIGHT][IMG_WIDTH];
        for (String path : X_tr) {
            img = ImageProcess.imread(path);
            img = ImageProcess.resize(img, IMG_WIDTH, IMG_HEIGHT);
            if (isGray) {
                BufferedImage imgGray = ImageProcess.rgb2gray(img);
                ret[k++][0] = ImageProcess.bufferedImageToArray2D(imgGray);
            } else {
                if (nFilter == 1) {
                    ret[k++][0] = ImageProcess.bufferedImageToArray2D(img);
                } else if (nFilter == 3) {
                    BufferedImage imgRed = ImageProcess.getRedChannelColor(img);
                    ret[k++][0] = ImageProcess.bufferedImageToArray2D(imgRed);
                    BufferedImage imgGreen = ImageProcess.getRedChannelColor(img);
                    ret[k++][1] = ImageProcess.bufferedImageToArray2D(imgGreen);
                    BufferedImage imgBlue = ImageProcess.getRedChannelColor(img);
                    ret[k++][2] = ImageProcess.bufferedImageToArray2D(imgBlue);
                } else if (nFilter == 4) {
                    BufferedImage imgRed = ImageProcess.getRedChannelColor(img);
                    ret[k++][0] = ImageProcess.bufferedImageToArray2D(imgRed);
                    BufferedImage imgGreen = ImageProcess.getRedChannelColor(img);
                    ret[k++][1] = ImageProcess.bufferedImageToArray2D(imgGreen);
                    BufferedImage imgBlue = ImageProcess.getRedChannelColor(img);
                    ret[k++][2] = ImageProcess.bufferedImageToArray2D(imgBlue);
                    BufferedImage imgGray = ImageProcess.rgb2gray(img);
                    ret[k++][3] = ImageProcess.bufferedImageToArray2D(imgGray);
                }
            }

        }
        return ret;
    }


}
