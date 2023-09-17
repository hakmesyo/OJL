/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.sdnn.test;

import java.util.List;
import java.util.Random;
import jazari.deep_learning.sdnn.ActivationType;
import jazari.deep_learning.sdnn.DataSetSDNN;
import jazari.deep_learning.sdnn.SNN;
import jazari.deep_learning.sdnn.UtilsSNN;

/**
 *
 * @author cezerilab
 */
public class TestRice {

    private static final int EPOCHS = 10;
    private static final int BATCH_SIZE = 1;
    private static final float LEARNING_RATE = 1E-4f;
    private static final int NUMBER_OF_CLASSES = 5;
    private static final int IMG_WIDTH = 250;
    private static final int IMG_HEIGHT = 250;
    private static final int NUM_CHANNELS = 1;
    private static final int NUM_FILTERS = 1;
    private static final int PATCH_SIZE = 2;
    private static final int STRIDE = PATCH_SIZE;
    private static final String PATH = "D:\\ai\\djl\\rice";
    private static final String PATH_TRAIN = "D:\\ai\\djl\\rice\\train";
    private static final String PATH_TEST = "D:\\ai\\djl\\rice\\test";
    private static final String PATH_VALID = "D:\\ai\\djl\\rice\\valid";
    private static final String PATH_MODEL = "D:\\ai\\djl\\rice";

    public static void main(String[] args) {
        DataSetSDNN ds_train=UtilsSNN.generateDataSetFromImage(PATH_TRAIN,NUM_CHANNELS,IMG_WIDTH,IMG_HEIGHT);
        DataSetSDNN ds_valid=UtilsSNN.generateDataSetFromImage(PATH_VALID,NUM_CHANNELS,IMG_WIDTH,IMG_HEIGHT);
        DataSetSDNN ds_test=UtilsSNN.generateDataSetFromImage(PATH_TEST,NUM_CHANNELS,IMG_WIDTH,IMG_HEIGHT);

        trainAndSaveModel(ds_train, ds_valid);
//        testModel(ds_test);
//        visualizeModel(ds_test);
//        visualizeLearningMetrics();
        int a = 1;

    }

    private static void trainAndSaveModel(DataSetSDNN ds_train, DataSetSDNN ds_valid) {
        SNN model = new SNN("Model_" + 0, new Random(123), false)
                .addInputLayer(IMG_WIDTH, IMG_HEIGHT, NUM_CHANNELS, NUM_FILTERS, PATCH_SIZE, STRIDE)
                .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                //                .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                //                .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                .addOutputLayer(ActivationType.softmax, NUMBER_OF_CLASSES);
        model.compile();
        model.summary();

        //start transfer learning
        //model = UtilsSNN.loadModel(PATH_MODEL + "/snn_0.model");
        
        float train_acc = model.test(ds_train, false);
        if (ds_valid == null) {
            System.out.println("initial train_acc = " + train_acc);
        } else {
            float valid_acc = model.test(ds_valid, false);
            System.out.println("initial train_acc = " + train_acc + " initial validation_acc = " + valid_acc);
        }

        model.fit(ds_train, ds_valid, LEARNING_RATE, EPOCHS, BATCH_SIZE, false);

        long t = System.currentTimeMillis();
        float final_train_acc = model.test(ds_train, false);
        if (ds_valid == null) {
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println("final train_acc = " + final_train_acc + ", time = " + (System.currentTimeMillis() - t) + " ms");
        } else {
            float final_valid_acc = model.test(ds_valid, false);
            System.out.println("-------------------------------------------------------------------------------------");
            System.out.println("final train_acc = " + final_train_acc + ", final validation_acc = " + final_valid_acc + ", time = " + (System.currentTimeMillis() - t) + " ms");
        }
        model.saveModel(PATH_MODEL + "/snn_" + 0 + ".model");
        model.saveTrainingMetrics(PATH_MODEL + "/snn_0.metrics");
    }

    private static void testModel(DataSetSDNN ds) {
        SNN model = UtilsSNN.loadModel(PATH_MODEL + "/snn_0.model");
        model.summary();

        for (int i = 0; i < 1; i++) {
            long t1 = System.currentTimeMillis();
            float test_acc = model.test(ds, false);
            long t2 = System.currentTimeMillis() - t1;
            float time = (1.0f * t2 / ds.size);
            float fps = 1000.0f / time;
            System.out.println("\n--------------------------------------------------\n"
                    + "test_acc = " + test_acc + ", time = " + time + " ms, FPS = " + fps);
        }
    }

    private static void visualizeModel(DataSetSDNN ds) {
        SNN model = UtilsSNN.loadModel(PATH_MODEL + "/snn_0.model");
        model.summary();

        model.feedInputLayerData(ds.X[1]);
        model.forwardPass();
        model.getLayer(0).visualizeOutputs();
        model.getLayer(1).visualizeWeights();
        model.getLayer(1).visualizeOutputs();
        model.getLayer(2).visualizeWeights();
        model.getLayer(2).visualizeOutputs();
        model.getLayer(3).visualizeWeights();
        model.getLayer(3).visualizeOutputs();
    }

    private static void visualizeLearningMetrics() {
        SNN model = UtilsSNN.loadModel(PATH_MODEL + "/snn_0.model");
        model.summary();
        
        List<String> lst=model.loadTrainingMetrics(PATH+"/snn_0.metrics");
        model.plotLearningMetrics(lst);
    }

}
