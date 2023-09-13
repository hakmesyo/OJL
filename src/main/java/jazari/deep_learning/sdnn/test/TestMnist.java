/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.sdnn.test;

import java.util.Random;
import jazari.deep_learning.sdnn.ActivationType;
import jazari.deep_learning.sdnn.SNN;
import jazari.factory.FactoryDataSetLoader;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import jazari.utils.DataSet;

/**
 * Bu yaklaşımın geleneksel CNN'den farkı CNN'de ki kernel içerisindeki weightler yine tüm resmin pixelleri
 * ile muhatap oluyordu. Ve bütün resimdeki piksellerin kernel matrisindeki weightler üzerinde etkisi vardır.
 * SNN de durum farklıdır. Şöyle ki resimde hereket ettirilen bir window/kernel yoktur dolayısıyla convolution
 * işleminden bahsedilemez. İkinci fark; input imgedeki spatial bir bölgedeki piksellerin her biri sadece bir alt
 * katmandaki (abstract) node ile bağlantısı bulunmaktadır. Bu SNN yi oldukça sparse veya bağlantı adedi olarak
 * oldukça seyrek bir yapıya dönüştürür. SNN'in diğer bir üstünlüğü teorik olarak input imgenin kare olma 
 * zorunluluğunun bulunmamasıdır.
 * @author cezerilab
 */
public class TestMnist {

    public static int EPOCHS = 500;
    public static int BATCH_SIZE = 100;
    public static float LEARNING_RATE = 1E-4f;
    //public static float LEARNING_RATE = 0.001f;
    public static int NUMBER_OF_CLASSES = 10;
    public static int IMG_WIDTH = 28;
    public static int IMG_HEIGHT = 28;
    public static int NUM_CHANNELS = 1;
    public static int NUM_FILTERS = 1;
    public static int PATCH_SIZE = 2;
    public static int STRIDE = PATCH_SIZE;

    public static void main(String[] args) {
        //String path_train = "C:\\ai\\djl\\mnist\\train";
        //String path_train = "C:\\ai\\djl\\mnist_reduced\\train";
        //String path_test = "D:\\ai\\djl\\mnist\\test";
        //String path_train = "dataset/mnist_train_reduced.csv";
        String path_train = "D:\\ai\\djl\\mnist\\csv\\mnist_train.csv";
        String path_test = "D:\\ai\\djl\\mnist\\csv\\mnist_test.csv";

        //FactoryUtils.reduceDataSet(path_train_whole,path_train,0.1f);
        long t1 = FactoryUtils.tic();

//        LinkedHashMap<String, List<String>> tr = UtilsSNN.loadData(path_train);
//        List<String> X_tr = tr.get("X");
//        List<String> y_tr = tr.get("Y");
//        float[][][][] X_train = UtilsSNN.loadData(X_tr, true,1,IMG_WIDTH, IMG_WIDTH);
//        float[][] y_train=UtilsSNN.to2dFloat(y_tr);
//        LinkedHashMap<String, List<String>> tst = UtilsSNN.loadData(path_test);
//        List<String> X_ts = tst.get("X");
//        List<String> y_ts = tst.get("Y");
//        float[][][][] X_test = UtilsSNN.loadData(X_ts, true, 1,IMG_WIDTH, IMG_WIDTH);
//        float[][] y_test=UtilsSNN.to2dFloat(y_ts);
        //DataSet ds_train=FactoryDataSetLoader.loadDataSetFromImage(path_train, "gray");
        DataSet ds_train = FactoryDataSetLoader.loadDataSetFromCSV(path_train, NUM_CHANNELS,NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT,0);
        ds_train = FactoryDataSetLoader.normalizeDataSetMinMax(ds_train, 0, 1);
        //ds_train.shuffle(new Random(123));
        DataSet ds_test = FactoryDataSetLoader.loadDataSetFromCSV(path_test, NUM_CHANNELS,NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT,0);
        ds_test = FactoryDataSetLoader.normalizeDataSetMinMax(ds_test, 0, 1);
        //ds_test.shuffle(new Random(123));
        //float[][][][] X_train=ds_train.getTrainX();
        //float[][] y_train=ds_train.getTrainY();
        int sample_size = 60_000;
        float[][][][] X_train = ds_train.getSubsetX(0, sample_size);
        float[][] y_train = ds_train.getSubsetY(0, sample_size);
        float[][][][] X_test = ds_test.getSubsetX(0, 10_000);
        float[][] y_test = ds_test.getSubsetY(0, 10_000);

        t1 = FactoryUtils.toc("data loading elapsed time:", t1);

        SNN model = new SNN("Model_1", new Random(123),false)
                .addInputLayer(IMG_WIDTH, IMG_HEIGHT, NUM_CHANNELS, NUM_FILTERS, PATCH_SIZE, STRIDE)
                .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                //.addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                //.addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                //.addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                .addOutputLayer(ActivationType.softmax, NUMBER_OF_CLASSES);
        model.compile();
        model.summary();
        
        float train_acc=model.test(X_train, y_train,false);
        float test_acc=model.test(X_test, y_test,false);
        System.out.println("initial train_acc = " + train_acc+"  initial test_acc = "+test_acc);
        
        model.fit(X_train, y_train, LEARNING_RATE, EPOCHS, BATCH_SIZE);

        long t=System.currentTimeMillis();
        float final_train_acc=model.test(X_train, y_train,false);
        System.out.println("----------------------------------------------------------");
        System.out.println("final train_acc = " + final_train_acc+" time = "+(System.currentTimeMillis()-t)+" ms");
        t=System.currentTimeMillis();
        float final_test_acc=model.test(X_test, y_test,false);
        System.out.println("final test_acc = " + final_test_acc+" time = "+(System.currentTimeMillis()-t)+" ms");
        int x = 3;
        
    }

}
