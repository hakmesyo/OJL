/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.sdnn.test;

import java.util.List;
import java.util.Random;
import jazari.deep_learning.sdnn.ActivationType;
import jazari.deep_learning.sdnn.DataSetSDNN;
import jazari.deep_learning.sdnn.Opt;
import jazari.deep_learning.sdnn.SNN;
import jazari.deep_learning.sdnn.UtilsSNN;
import jazari.factory.FactoryDataSetLoader;
import jazari.factory.FactoryUtils;
import jazari.utils.DataSet;

/**
 * Bu yaklaşımın geleneksel CNN'den farkı CNN'deki kernel içerisindeki
 * weightler yine tüm resmin pixelleri ile muhatap oluyordu., Ve bütün resimdeki
 * piksellerin kernel matrisindeki weightler üzerinde etkisi vardır., SNN de
 * durum farklıdır., Şöyle ki resimde hereket ettirilen bir window/kernel yoktur
 * dolayısıyla convolution işleminden bahsedilemez., İkinci fark; input imgedeki
 * spatial bir bölgedeki piksellerin her biri sadece bir alt katmandaki
 * (abstract) node ile bağlantısı bulunmaktadır., Bu SNN yi oldukça sparse veya
 * bağlantı adedi olarak oldukça seyrek bir yapıya dönüştürür., SNN'in diğer bir
 * üstünlüğü teorik olarak input imgenin kare olma zorunluluğunun
 * bulunmamasıdır.
 *
 * @author cezerilab
 */
public class TestMnist {

    private static final int EPOCHS = 20;
    private static final int BATCH_SIZE = 1;
    private static final float LEARNING_RATE = 1E-4f;
    private static final int NUMBER_OF_CLASSES = 10;
    private static final int IMG_WIDTH = 28;
    private static final int IMG_HEIGHT = 28;
    private static final int NUM_CHANNELS = 1;
    private static final int NUM_FILTERS = 1;
    private static final int PATCH_SIZE = 2;
    private static final int STRIDE = 2;
    private static final String PATH = "D:\\ai\\djl\\mnist\\csv";
    private static final String PATH_TRAIN = "D:\\ai\\djl\\mnist\\csv\\mnist_train.csv";
    private static final String PATH_TEST = "D:\\ai\\djl\\mnist\\csv\\mnist_test.csv";
    private static final String PATH_VALID = "";
    private static final String PATH_MODEL = "D:\\ai\\djl\\mnist\\csv";

    public static void main(String[] args) {
        Opt opt=new Opt(EPOCHS, BATCH_SIZE, LEARNING_RATE, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT, NUM_CHANNELS, NUM_FILTERS, PATCH_SIZE, STRIDE, PATH, PATH_TRAIN, PATH_TEST, PATH_VALID, PATH_MODEL);
        DataSetSDNN ds_train = UtilsSNN.generateDataSetFromCSV(PATH_TRAIN, NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT);
        DataSetSDNN ds_valid = UtilsSNN.generateDataSetFromCSV(PATH_VALID, NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT);
        DataSetSDNN ds_test = UtilsSNN.generateDataSetFromCSV(PATH_TEST, NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT);

        //UtilsSNN.trainAndSaveModel(opt,ds_train, ds_valid);
        UtilsSNN.testModel(opt,ds_test);
//        UtilsSNN.visualizeModel(opt,ds_test);
//        UtilsSNN.visualizeLearningMetrics(opt);
    }


}
