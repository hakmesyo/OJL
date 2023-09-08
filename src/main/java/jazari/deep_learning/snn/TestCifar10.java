/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.snn;

import java.util.Random;
import jazari.factory.FactoryDataSetLoader;
import jazari.factory.FactoryUtils;
import jazari.utils.DataSet;

/**
 *
 * @author cezerilab
 */
public class TestCifar10 {
    public static int EPOCHS = 20;
    public static int BATCH_SIZE = 100;
    public static float LEARNING_RATE = 1E-2f;
    public static int NUMBER_OF_CLASSES = 10;
    public static int IMG_WIDTH = 32;
    public static int IMG_HEIGHT = 32;
    public static int NUM_FILTERS = 3;
    public static int PATCH_SIZE = 2;
    public static int STRIDE = PATCH_SIZE;

    public static void main(String[] args) {
        String path_train = "C:\\ai\\djl\\cifar10\\csv\\train.csv";
        String path_test = "C:\\ai\\djl\\cifar10\\csv\\test.csv";

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
        DataSet ds_train = FactoryDataSetLoader.loadDataSetFromCSV(path_train, NUM_FILTERS,NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT,-1);
        ds_train = FactoryDataSetLoader.normalizeDataSetMinMax(ds_train, 0, 1);
        ds_train.shuffle(new Random(123));
        DataSet ds_test = FactoryDataSetLoader.loadDataSetFromCSV(path_test, NUM_FILTERS,NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT,-1);
        ds_test = FactoryDataSetLoader.normalizeDataSetMinMax(ds_test, 0, 1);
        ds_test.shuffle(new Random(123));
        //float[][][][] X_train=ds_train.getTrainX();
        //float[][] y_train=ds_train.getTrainY();
        int sample_size = 50000;
        float[][][][] X_train = ds_train.getSubsetX(0, sample_size);
        float[][] y_train = ds_train.getSubsetY(0, sample_size);
        float[][][][] X_test = ds_test.getSubsetX(0, 10000);
        float[][] y_test = ds_test.getSubsetY(0, 10000);

        t1 = FactoryUtils.toc("data loading elapsed time:", t1);

        SNN model = new SNN("Model_1", new Random(123))
                .addInputLayer(IMG_WIDTH, IMG_HEIGHT, NUM_FILTERS, PATCH_SIZE, STRIDE)
                .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                //.addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                .addOutputLayer(ActivationType.softmax, NUMBER_OF_CLASSES);
        model.compile();
        model.summary();
        
        float train_acc=model.test(X_train, y_train,false);
        float test_acc=model.test(X_test, y_test,false);
        System.out.println("initial train_acc = " + train_acc+"  initial test_acc = "+test_acc);

        //model.dump();
//        Layer output=model.getOutputLayer();
//        float[] ret=output.forwardPass();
//        System.out.println("ret = " + Arrays.toString(ret));
//        ret=UtilsSNN.softmax(ret);
//        System.out.println("ret = " + Arrays.toString(ret));
//        float value1=output.filters[0].nodes[0][0].getOutput();
//        float value2=output.filters[0].nodes[1][0].getOutput();
//        float value3=output.filters[0].nodes[2][0].getOutput();
        model.fit(X_train, y_train, LEARNING_RATE, EPOCHS, BATCH_SIZE);

        float final_train_acc=model.test(X_train, y_train,false);
        float final_test_acc=model.test(X_test, y_test,false);
        System.out.println("final train_acc = " + final_train_acc+"  final test_acc = "+final_test_acc);
        int x = 3;
    }

    
}
