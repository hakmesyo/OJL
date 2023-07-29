/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.snn;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * @author cezerilab
 */
public class TestSNN {

    public static int EPOCHS = 10;
    public static int BATCH_SIZE = 100;
    public static float LEARNING_RATE = 0.01f;
    public static int NUMBER_OF_CLASSES = 10;
//    public static int IMG_WIDTH = 28;
//    public static int IMG_HEIGHT = 28;
    public static int IMG_WIDTH = 224;
    public static int IMG_HEIGHT = 224;
    public static int NUM_FILTERS = 3;
    public static int PATCH_SIZE = 5;
    public static int STRIDE = 5;

    public static void main(String[] args) {
        String path_train = "C:\\ai\\djl\\mnist\\train";
        String path_test = "C:\\ai\\djl\\mnist\\test";

        LinkedHashMap<String, List<String>> tr = UtilsSNN.loadData(path_train, IMG_WIDTH, IMG_HEIGHT, NUM_FILTERS);
        List<String> X_train = tr.get("X");
        List<String> y_train = tr.get("Y");
        LinkedHashMap<String, List<String>> tst = UtilsSNN.loadData(path_test, IMG_WIDTH, IMG_HEIGHT, NUM_FILTERS);
        List<String> X_test = tst.get("X");
        List<String> y_test = tst.get("Y");

        
        SNN model = new SNN("Model_1", new Random(123))
                .addInputLayer(IMG_WIDTH, IMG_HEIGHT, NUM_FILTERS, PATCH_SIZE, STRIDE)
                .addHiddenLayer(ActivationType.sigmoid, PATCH_SIZE, STRIDE)
                .addHiddenLayer(ActivationType.sigmoid, PATCH_SIZE, STRIDE)
                //.addHiddenLayer(ActivationType.sigmoid, PATCH_SIZE, STRIDE)
                .addOutputLayer(ActivationType.softmax, NUMBER_OF_CLASSES);
        model = model.compile();
        model = model.summary();
        //model.dump();
        //model=model.fit(X_train, y_train, LEARNING_RATE, EPOCHS, BATCH_SIZE);
    }

}
