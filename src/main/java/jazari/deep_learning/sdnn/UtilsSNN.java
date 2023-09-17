/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.sdnn;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import jazari.factory.FactoryDataSetLoader;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;
import jazari.utils.DataSet;

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

    public static float applyDerivativeFunction(ActivationType act, float val) {
        if (act == ActivationType.relu) {
            return reluDerivative(val);
        } else if (act == ActivationType.sigmoid) {
            return sigmoidDerivative(val);
        } else {
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
        int index = 0;
        for (File dir : dirs) {
            File[] files = FactoryUtils.getFileArrayInFolderForImages(dir.getAbsolutePath());
            for (File file : files) {
                lst_input.add(file.getAbsolutePath());
                //lst_output.add(Arrays.toString(FactoryUtils.getOneHotEncoding(dirs.length, dir.getName())));
                lst_output.add(Arrays.toString(FactoryUtils.getOneHotEncoding(dirs.length, index + "")));
            }
            index++;
        }
        //long seed = System.nanoTime();
        long seed = 123;
        Collections.shuffle(lst_input, new Random(seed));
        Collections.shuffle(lst_output, new Random(seed));
        dataset.put("X", lst_input);
        dataset.put("Y", lst_output);
        return dataset;
    }

    public static LinkedHashMap<String, List<String>> loadDataFilterOneClass(String path, int filterClassIndex) {
        LinkedHashMap<String, List<String>> dataset = new LinkedHashMap<>();
        File[] dirs = FactoryUtils.getDirectories(path);
        List<String> lst_input = new ArrayList();
        List<String> lst_output = new ArrayList();
        File[] files = FactoryUtils.getFileArrayInFolderForImages(dirs[filterClassIndex].getAbsolutePath());
        for (File file : files) {
            lst_input.add(file.getAbsolutePath());
            //lst_output.add(Arrays.toString(FactoryUtils.getOneHotEncoding(dirs.length, dir.getName())));
            lst_output.add(Arrays.toString(FactoryUtils.getOneHotEncoding(dirs.length, filterClassIndex + "")));
        }
        //long seed = System.nanoTime();
        long seed = 123;
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

    public static float[][][][] loadData(List<String> X_tr, boolean isGray, int nChannel, int IMG_WIDTH, int IMG_HEIGHT) {
        BufferedImage img;
        int k = 0;
        float[][][][] ret = new float[X_tr.size()][nChannel][IMG_HEIGHT][IMG_WIDTH];
        for (String path : X_tr) {
            img = ImageProcess.imread(path);
            img = ImageProcess.resize(img, IMG_WIDTH, IMG_HEIGHT);
            if (isGray) {
                BufferedImage imgGray = ImageProcess.rgb2gray(img);
                ret[k++][0] = ImageProcess.bufferedImageToArray2D(imgGray);
            } else {
                if (nChannel == 1) {
                    ret[k++][0] = ImageProcess.bufferedImageToArray2D(img);
                } else if (nChannel == 3) {
                    BufferedImage imgRed = ImageProcess.getRedChannelColor(img);
                    ret[k++][0] = ImageProcess.bufferedImageToArray2D(imgRed);
                    BufferedImage imgGreen = ImageProcess.getRedChannelColor(img);
                    ret[k++][1] = ImageProcess.bufferedImageToArray2D(imgGreen);
                    BufferedImage imgBlue = ImageProcess.getRedChannelColor(img);
                    ret[k++][2] = ImageProcess.bufferedImageToArray2D(imgBlue);
                } else if (nChannel == 4) {
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

    public static void saveModel(String path, SNN model) {
        FactoryUtils.serialize(model, path);
//        FileOutputStream file = null;
//        try {
//            file = new FileOutputStream(path);
//            ObjectOutputStream out = new ObjectOutputStream(file);
//            // Method for serialization of object
//            out.writeObject(model);
//            out.close();
//            file.close();
//            System.out.println("Object has been serialized");
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(SNN.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(SNN.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                file.close();
//            } catch (IOException ex) {
//                Logger.getLogger(SNN.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
    }

    public static SNN loadModel(String path) {
        return (SNN) FactoryUtils.deserialize(path);
    }

    public static DataSetSDNN generateDataSetFromImage(String PATH, int NUM_CHANNELS, int IMG_WIDTH, int IMG_HEIGHT) {
        LinkedHashMap<String, List<String>> tr = UtilsSNN.loadData(PATH);
        List<String> X_tr = tr.get("X");
        List<String> y_tr = tr.get("Y");
        float[][][][] X = UtilsSNN.loadData(X_tr, true, NUM_CHANNELS, IMG_WIDTH, IMG_HEIGHT);
        X = FactoryDataSetLoader.normalizeDataSetMinMax(X, 0, 1);
        float[][] y = UtilsSNN.to2dFloat(y_tr);
        return new DataSetSDNN(X, y);
    }

    public static DataSetSDNN generateDataSetFromImageFilterOneClass(String PATH, int filterClassIndex, int NUM_CHANNELS, int IMG_WIDTH, int IMG_HEIGHT) {
        LinkedHashMap<String, List<String>> tr = UtilsSNN.loadDataFilterOneClass(PATH, filterClassIndex);
        List<String> X_tr = tr.get("X");
        List<String> y_tr = tr.get("Y");
        float[][][][] X = UtilsSNN.loadData(X_tr, true, NUM_CHANNELS, IMG_WIDTH, IMG_HEIGHT);
        X = FactoryDataSetLoader.normalizeDataSetMinMax(X, 0, 1);
        float[][] y = UtilsSNN.to2dFloat(y_tr);
        return new DataSetSDNN(X, y);
    }

    public static DataSetSDNN generateDataSetFromCSV(String PATH, int NUM_CHANNELS, int NUMBER_OF_CLASSES, int IMG_WIDTH, int IMG_HEIGHT) {
        if (PATH.isBlank() || PATH.isEmpty() || PATH == null) {
            return null;
        }
        DataSet ds_train = FactoryDataSetLoader.loadDataSetFromCSV(PATH, NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT, 0);
        ds_train = FactoryDataSetLoader.normalizeDataSetMinMax(ds_train, 0, 1);
        ds_train.shuffle(new Random(123));
        float[][][][] X = ds_train.getDataX();
        float[][] y = ds_train.getDataY();
        return new DataSetSDNN(X, y);
    }

    public static DataSetSDNN generateDataSetFromCsvFilterOneClass(String PATH, int filterClassIndex, int NUM_CHANNELS, int NUMBER_OF_CLASSES, int IMG_WIDTH, int IMG_HEIGHT) {
        if (PATH.isBlank() || PATH.isEmpty() || PATH == null) {
            return null;
        }
        DataSet ds_train = FactoryDataSetLoader.loadDataSetFromCsvFilterOneClass(PATH, filterClassIndex, NUM_CHANNELS, NUMBER_OF_CLASSES, IMG_WIDTH, IMG_HEIGHT, 0);
        ds_train = FactoryDataSetLoader.normalizeDataSetMinMax(ds_train, 0, 1);
        ds_train.shuffle(new Random(123));
        float[][][][] X = ds_train.getDataX();
        float[][] y = ds_train.getDataY();
        return new DataSetSDNN(X, y);
    }

    public static void trainAndSaveModel(Opt opt, DataSetSDNN ds_train, DataSetSDNN ds_valid) {

        SNN model = new SNN("Model_" + 0, new Random(123), false)
                .addInputLayer(opt.IMG_WIDTH, opt.IMG_HEIGHT, opt.NUM_CHANNELS, opt.NUM_FILTERS, opt.PATCH_SIZE, opt.STRIDE)
                .addHiddenLayer(ActivationType.relu, opt.PATCH_SIZE, opt.STRIDE)
                .addHiddenLayer(ActivationType.relu, opt.PATCH_SIZE, opt.STRIDE)
                //                .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                //                .addHiddenLayer(ActivationType.relu, PATCH_SIZE, STRIDE)
                .addOutputLayer(ActivationType.softmax, opt.NUMBER_OF_CLASSES);
        model.compile();
        model.summary();

        //start transfer learning
        model = UtilsSNN.loadModel(opt.PATH_MODEL + "/snn_0.model");

        float train_acc = model.test(ds_train, false);
        if (ds_valid == null) {
            System.out.println("initial train_acc = " + train_acc);
        } else {
            float valid_acc = model.test(ds_valid, false);
            System.out.println("initial train_acc = " + train_acc + " initial validation_acc = " + valid_acc);
        }

        model.fit(ds_train, ds_valid, opt.LEARNING_RATE, opt.EPOCHS, opt.BATCH_SIZE, false);

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
        model.saveModel(opt.PATH_MODEL + "/snn_" + 0 + ".model");
        model.saveTrainingMetrics(opt.PATH_MODEL + "/snn_0.metrics");
    }

    public static void testModel(Opt opt, DataSetSDNN ds) {
        SNN model = UtilsSNN.loadModel(opt.PATH_MODEL + "/snn_0.model");
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

    public static void visualizeModel(Opt opt, DataSetSDNN ds) {
        SNN model = UtilsSNN.loadModel(opt.PATH_MODEL + "/snn_0.model");
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

    public static void visualizeLearningMetrics(Opt opt) {
        SNN model = UtilsSNN.loadModel(opt.PATH_MODEL + "/snn_0.model");
        model.summary();

        List<String> lst = model.loadTrainingMetrics(opt.PATH + "/snn_0.metrics");
        model.plotLearningMetrics(lst);
    }
}
