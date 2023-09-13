/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.sdnn.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jazari.deep_learning.sdnn.ActivationType;
import jazari.deep_learning.sdnn.SNN;
import jazari.deep_learning.sdnn.UtilsSNN;
import jazari.factory.FactoryDataSetLoader;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import jazari.utils.DataSet;

/**
 * Bu yaklaşımın geleneksel CNN'den farkı CNN'de ki kernel içerisindeki
 * weightler yine tüm resmin pixelleri ile muhatap oluyordu. Ve bütün resimdeki
 * piksellerin kernel matrisindeki weightler üzerinde etkisi vardır. SNN de
 * durum farklıdır. Şöyle ki resimde hereket ettirilen bir window/kernel yoktur
 * dolayısıyla convolution işleminden bahsedilemez. İkinci fark; input imgedeki
 * spatial bir bölgedeki piksellerin her biri sadece bir alt katmandaki
 * (abstract) node ile bağlantısı bulunmaktadır. Bu SNN yi oldukça sparse veya
 * bağlantı adedi olarak oldukça seyrek bir yapıya dönüştürür. SNN'in diğer bir
 * üstünlüğü teorik olarak input imgenin kare olma zorunluluğunun
 * bulunmamasıdır.
 *
 * @author cezerilab
 */
public class TestMnistEnsemble {

    public static int EPOCHS = 500;
    public static int BATCH_SIZE = 1;
    public static float LEARNING_RATE = 1E-4f;
    //public static float LEARNING_RATE = 0.001f;
    public static int NUMBER_OF_CLASSES = 10;
    public static int IMG_WIDTH = 28;
    public static int IMG_HEIGHT = 28;
    public static int NUM_CHANNELS = 1;
    public static int NUM_FILTERS = 1;
    public static int PATCH_SIZE = 2;
    public static int STRIDE = PATCH_SIZE;
    public static String path_train = "D:\\ai\\djl\\mnist\\csv\\train_files";
    public static String path_test = "D:\\ai\\djl\\mnist\\csv\\test_files";
    public static String path_model = "D:\\ai\\djl\\mnist\\saved_model";

    public static void main(String[] args) {
        test_single_models();
        //test_znz_models();
    }

    private static void saveModels() {
        long t1 = FactoryUtils.tic();
        for (int i = 0; i < 10; i++) {
            DataSet ds_train = FactoryDataSetLoader.loadDataSetFromCSV(path_train + "/" + i + ".csv", NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT, 0);
            ds_train = FactoryDataSetLoader.normalizeDataSetMinMax(ds_train, 0, 1);
            DataSet ds_test = FactoryDataSetLoader.loadDataSetFromCSV(path_test + "/0.csv", NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT, 0);
            ds_test = FactoryDataSetLoader.normalizeDataSetMinMax(ds_test, 0, 1);
            int train_sample_size = ds_train.data.size();
            //int train_sample_size = 100;
            //int test_sample_size = ds_test.data.size();
            int test_sample_size = 1;
            float[][][][] X_train = ds_train.getSubsetX(0, train_sample_size);
            float[][] y_train = ds_train.getSubsetYEnsemble(0, train_sample_size);
            float[][][][] X_test = ds_test.getSubsetX(0, test_sample_size);
            float[][] y_test = ds_test.getSubsetYEnsemble(0, test_sample_size);

            t1 = FactoryUtils.toc("data loading elapsed time:", t1);

            SNN model = new SNN("Model_" + i, new Random(123), false)
                    .addInputLayer(IMG_WIDTH, IMG_HEIGHT, NUM_CHANNELS, NUM_FILTERS, PATCH_SIZE, STRIDE)
                    .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                    .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                    //.addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                    //.addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                    .addOutputLayer(ActivationType.softmax, NUMBER_OF_CLASSES);
            model.compile();
            model.summary();

            float train_acc = model.test(X_train, y_train, false);
            float test_acc = model.test(X_test, y_test, false);
            System.out.println("initial train_acc = " + train_acc + "  initial test_acc = " + test_acc);
            model.fit(X_train, y_train, LEARNING_RATE, EPOCHS, BATCH_SIZE);

            long t = System.currentTimeMillis();
            float final_train_acc = model.test(X_train, y_train, false);
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println("final train_acc = " + final_train_acc + " time = " + (System.currentTimeMillis() - t) + " ms");
            t = System.currentTimeMillis();
            float final_test_acc = model.test(X_test, y_test, true);
            System.out.println("final test_acc = " + final_test_acc + " time = " + (System.currentTimeMillis() - t) + " ms");
            int x = 3;

            model.saveModel(path_model + "/snn_" + i + ".model");
//        SNN model2=UtilsSNN.loadModel(path_model+"/snn_1.model");
//        float final_test_acc2=model2.test(X_test, y_test,true);
        }

    }

    private static SNN[] loadModels() {
        SNN[] ret = new SNN[10];
        for (int i = 0; i < 10; i++) {
            ret[i] = UtilsSNN.loadModel(path_model + "/snn_" + i + ".model");
        }
        return ret;
    }

    private static void evaluateEnsembleModel(SNN[] models, int n, int fromIndex, int toIndex) {
        DataSet ds_test = FactoryDataSetLoader.loadDataSetFromCSV(path_test + "/" + n + ".csv", NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT, 0);
        ds_test = FactoryDataSetLoader.normalizeDataSetMinMax(ds_test, 0, 1);
        float[][][][] X_test = ds_test.getSubsetX(fromIndex, toIndex);
        float[][] y_test = ds_test.getSubsetYEnsemble(fromIndex, toIndex);
        for (int i = 0; i < 10; i++) {
            SNN model = models[i];
            float acc = model.test(X_test, y_test, true);
            CMatrix.getInstance().setArray(model.getLayer(2).filters[0].weights(0)).println().map(0, 255).imresize(512, 512).imshow("weight");
//            CMatrix.getInstance().setArray(model.getLayer(1).filters[0].output()).map(0, 255).imresize(512,512).imshow("convolution");

//            float[][] input=CMatrix.getInstance().setArray(X_test[0][0]).println().toFloatArray2D();
//            float[][] weight=CMatrix.getInstance().setArray(model.getLayer(1).filters[0].weights(0)).println().toFloatArray2D();
//            CMatrix.getInstance(input).timesElement(CMatrix.getInstance(weight)).println().map(0, 255).imresize(512,512).imshow("product");
//            float[][] product=CMatrix.getInstance(input).timesElement(CMatrix.getInstance(weight)).println().toFloatArray2D();
//            float[][] difference=CMatrix.getInstance(input).minus(CMatrix.getInstance(product)).println().map(0, 255).imresize(512,512).imshow("difference").toFloatArray2D();
        }
    }

    private static void generateOnceClassDS() {
        String path_train = "D:\\ai\\djl\\mnist\\csv\\mnist_test.csv";
        String[] s = FactoryUtils.readFromFileAsString1D(path_train);
        List<String>[] str = new ArrayList[10];
        for (int i = 0; i < 10; i++) {
            str[i] = new ArrayList<>();
        }
        int n = s.length;
        for (int i = 0; i < s.length; i++) {
            int m = Integer.parseInt(s[i].charAt(0) + "");
            str[m].add(s[i]);
        }
        for (int i = 0; i < 10; i++) {
            FactoryUtils.writeToFile("D:\\ai\\djl\\mnist\\csv/test_files/" + i + ".csv", str[i]);
        }

    }

    private static void test_single_models() {
//        generateOnceClassDS();
//        saveModels();
//        SNN[] models = loadModels();
//        evaluateEnsembleModel(models, 0, 2, 3);
    }

    private static void test_znz_models() {
//        generateZnZDS();
//        saveZNZModels();

        SNN model = UtilsSNN.loadModel("D:\\ai\\djl\\mnist\\saved_model\\znz\\snn_2.model");
        DataSet ds_test = FactoryDataSetLoader.loadDataSetFromCSV(path_test + "/" + 2 + ".csv", NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT, 0);
        ds_test = FactoryDataSetLoader.normalizeDataSetMinMax(ds_test, 0, 1);
        float[][][][] X_test = ds_test.getSubsetX(0, ds_test.data.size());
        float[][] y_test = ds_test.getSubsetYEnsemble(0, ds_test.data.size());

        model.test(X_test, y_test, true);
    }

    private static void generateZnZDS() {
        String path = "D:\\ai\\djl\\mnist\\csv\\mnist_train.csv";
        String[] s = FactoryUtils.readFromFileAsString1D(path);
        List<String>[] str = new ArrayList[10];
        for (int i = 0; i < 10; i++) {
            str[i] = new ArrayList<>();
        }
        int n = s.length;
        for (int i = 0; i < s.length; i++) {
            int m = Integer.parseInt(s[i].charAt(0) + "");
            str[m].add(s[i]);
        }
        for (int i = 0; i < 10; i++) {
            List<String> data = getFilteredDS(str, i);
            FactoryUtils.writeToFile("D:\\ai\\djl\\mnist\\csv/znz/train/" + i + ".csv", data);
            System.out.println(i + ".process finished");
        }
    }

    private static List<String> getFilteredDS(List<String>[] ds, int n) {
        List<String> zanim = ds[n];
        List<String> ret = new ArrayList<>();
        for (String s : zanim) {
            s = s.replaceFirst("" + n, "0");
            ret.add(s);
        }
        for (int i = 0; i < ds.length; i++) {
            if (i == n) {
                continue;
            }
            int k = 0;
            for (String s : ds[i]) {
                if (k++ % 10 == 0) {
                    s = s.replaceFirst("" + i, "1");
                    ret.add(s);
                }
            }
        }

        return ret;
    }

    private static void saveZNZModels() {
        long t1 = FactoryUtils.tic();
        for (int i = 0; i < 10; i++) {
            DataSet ds_train = FactoryDataSetLoader.loadDataSetFromCSV("D:\\ai\\djl\\mnist\\csv/znz/train/" + i + ".csv", NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT, 0);
            ds_train = FactoryDataSetLoader.normalizeDataSetMinMax(ds_train, 0, 1);
            //DataSet ds_test = FactoryDataSetLoader.loadDataSetFromCSV(path_test + "/0.csv", NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT, 0);
            //ds_test = FactoryDataSetLoader.normalizeDataSetMinMax(ds_test, 0, 1);
            int train_sample_size = ds_train.data.size();
            //int train_sample_size = 100;
            //int test_sample_size = ds_test.data.size();
            //int test_sample_size = 1;
            float[][][][] X_train = ds_train.getSubsetX(0, train_sample_size);
            float[][] y_train = ds_train.getSubsetYEnsemble(0, train_sample_size);
            //float[][][][] X_test = ds_test.getSubsetX(0, test_sample_size);
            //float[][] y_test = ds_test.getSubsetYEnsemble(0, test_sample_size);

            t1 = FactoryUtils.toc("data loading elapsed time:", t1);

            SNN model = new SNN("Model_" + i, new Random(123), false)
                    .addInputLayer(IMG_WIDTH, IMG_HEIGHT, NUM_CHANNELS, NUM_FILTERS, PATCH_SIZE, STRIDE)
                    .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                    .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                    //.addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                    //.addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                    .addOutputLayer(ActivationType.softmax, 2);
            model.compile();
            model.summary();

            float train_acc = model.test(X_train, y_train, false);
            //float test_acc = model.test(X_test, y_test, false);
            System.out.println("initial train_acc = " + train_acc);
            model.fit(X_train, y_train, LEARNING_RATE, EPOCHS, BATCH_SIZE);

            long t = System.currentTimeMillis();
            float final_train_acc = model.test(X_train, y_train, false);
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println("final train_acc = " + final_train_acc + " time = " + (System.currentTimeMillis() - t) + " ms");
            t = System.currentTimeMillis();
            //float final_test_acc = model.test(X_test, y_test, true);
            //System.out.println("final test_acc = " + final_test_acc + " time = " + (System.currentTimeMillis() - t) + " ms");
            //int x = 3;

            model.saveModel("D:\\ai\\djl\\mnist\\saved_model\\znz/snn_" + i + ".model");
        }
    }

}
