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
public class TestSNN {

    public static int EPOCHS = 1000;
    public static int BATCH_SIZE = 100;
    public static float LEARNING_RATE = 1E-3f;
    public static int NUMBER_OF_CLASSES = 10;
    public static int IMG_WIDTH = 28;
    public static int IMG_HEIGHT = 28;
//    public static int IMG_WIDTH = 224;
//    public static int IMG_HEIGHT = 224;
    public static int NUM_FILTERS = 1;
    public static int PATCH_SIZE = 5;
    public static int STRIDE = PATCH_SIZE;

    public static void main(String[] args) {
        //String path_train = "C:\\ai\\djl\\mnist\\train";
        //String path_train = "C:\\ai\\djl\\mnist_reduced\\train";
        //String path_test = "D:\\ai\\djl\\mnist\\test";
        String path_train = "C:\\ai\\djl\\mnist\\csv\\mnist_train.csv";
        
        //FactoryUtils.reduceDataSet(path_train_whole,path_train,0.1f);

        long t1=FactoryUtils.tic();
        
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
        DataSet ds_train=FactoryDataSetLoader.loadDataSetFromCSV(path_train,NUMBER_OF_CLASSES,IMG_WIDTH,IMG_HEIGHT);
        ds_train=FactoryDataSetLoader.normalizeDataSetMinMax(ds_train, 0, 1);
        ds_train.shuffle(new Random(123));
        //float[][][][] X_train=ds_train.getTrainX();
        //float[][] y_train=ds_train.getTrainY();
        float[][][][] X_train=ds_train.getTrainX(0,100);
        float[][] y_train=ds_train.getTrainY(0,100);
        
        t1=FactoryUtils.toc("data loading elapsed time:",t1);

        
        SNN model = new SNN("Model_1", new Random(123))
                .addInputLayer(IMG_WIDTH, IMG_HEIGHT, NUM_FILTERS, PATCH_SIZE, STRIDE)
                .addHiddenLayer(ActivationType.sigmoid, PATCH_SIZE, STRIDE)
                //.addHiddenLayer(ActivationType.sigmoid, PATCH_SIZE, STRIDE)
                //.addHiddenLayer(ActivationType.sigmoid, PATCH_SIZE, STRIDE)
                .addOutputLayer(ActivationType.softmax, NUMBER_OF_CLASSES);
        model.compile();
        model.summary();
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
        int x=3;
    }

}
