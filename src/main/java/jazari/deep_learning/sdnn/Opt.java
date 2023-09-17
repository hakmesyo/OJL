/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.sdnn;

/**
 *
 * @author cezerilab
 */
public class Opt {

    int EPOCHS;
    int BATCH_SIZE;
    float LEARNING_RATE;
    int NUMBER_OF_CLASSES;
    int IMG_WIDTH;
    int IMG_HEIGHT;
    int NUM_CHANNELS;
    int NUM_FILTERS;
    int PATCH_SIZE;
    int STRIDE;
    String PATH;
    String PATH_TRAIN;
    String PATH_TEST;
    String PATH_VALID;
    String PATH_MODEL;   

    public Opt(int EPOCHS, int BATCH_SIZE, float LEARNING_RATE, int NUMBER_OF_CLASSES, int IMG_WIDTH, int IMG_HEIGHT, int NUM_CHANNELS, int NUM_FILTERS, int PATCH_SIZE, int STRIDE, String PATH, String PATH_TRAIN, String PATH_TEST, String PATH_VALID, String PATH_MODEL) {
        this.EPOCHS = EPOCHS;
        this.BATCH_SIZE = BATCH_SIZE;
        this.LEARNING_RATE = LEARNING_RATE;
        this.NUMBER_OF_CLASSES = NUMBER_OF_CLASSES;
        this.IMG_WIDTH = IMG_WIDTH;
        this.IMG_HEIGHT = IMG_HEIGHT;
        this.NUM_CHANNELS = NUM_CHANNELS;
        this.NUM_FILTERS = NUM_FILTERS;
        this.PATCH_SIZE = PATCH_SIZE;
        this.STRIDE = STRIDE;
        this.PATH = PATH;
        this.PATH_TRAIN = PATH_TRAIN;
        this.PATH_TEST = PATH_TEST;
        this.PATH_VALID = PATH_VALID;
        this.PATH_MODEL = PATH_MODEL;
    }
    
}
