/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.snn;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import jazari.factory.FactoryUtils;

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
        } else {
            return -1.0f;
        }
    }

    private static float sigmoid(float x) {
        float ret = (float) (1 / (1 + Math.pow(Math.E, -1 * x)));
        return ret;
    }

    public static float sigmoidDerivative(float x) {
        float ret = sigmoid(x) * (1 - sigmoid(x));
        return ret;
    }

    private static float relu(float x) {
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
        int nrows=nodes.length;
        int ncols=nodes[0].length;
        float exp_z_sum = 0.0f;
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                exp_z_sum += Math.pow(Math.E, nodes[i][j].data);
            }
        }
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                nodes[i][j].data = (float) Math.pow(Math.E, nodes[i][j].data) / exp_z_sum;
            }
        }
    }

    public static double[] convertOneHotEncoding(int n,String class_number){
        double[] ret=new double[n];
        double cln=Double.parseDouble(class_number);
        if (cln==0) {
            ret[0]=1;
        }else{
            ret[(int)cln]=1;
        }
        return ret;
    }

    public static LinkedHashMap<String, List<String>> loadData(String path, int w, int h, int ch) {
        LinkedHashMap<String, List<String>> dataset = new LinkedHashMap<>();
        File[] dirs = FactoryUtils.getDirectories(path);
        List<String> lst_input = new ArrayList();
        List<String> lst_output = new ArrayList();
        for (File dir : dirs) {
            File[] files = FactoryUtils.getFileArrayInFolderForImages(dir.getAbsolutePath());
            for (File file : files) {
                lst_input.add(file.getAbsolutePath());
                lst_output.add(Arrays.toString(convertOneHotEncoding(dirs.length, dir.getName())));
            }
        }
        long seed = System.nanoTime();
        Collections.shuffle(lst_input, new Random(seed));
        Collections.shuffle(lst_output, new Random(seed));
        dataset.put("X", lst_input);
        dataset.put("Y", lst_output);
        return dataset;
    }
}
