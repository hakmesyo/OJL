/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.sdnn;

import java.io.Serializable;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author cezerilab
 */
public class SNN implements Serializable {

    String modelName = "";
    List<Layer> layers = new ArrayList();
    int layerIndex;
    int NUMBER_OF_CLASSES;
    int BATCH_SIZE;
    float LEARNING_RATE;
    int EPOCHS;
    int PATCH_SIZE;
    int STRIDE;
    Random rnd;
    int nFilters;
    int nChannels;
    int nrows;
    int ncols;
    float[][][] input;
    private float JITTER_VALUE = 0.01f;
    public boolean isDebug = false;
    List<String> lstMetrics = new ArrayList<>();

    public SNN(String modelName, Random rnd, boolean isDebug) {
        this.rnd = rnd;
        this.isDebug = isDebug;
        this.modelName = modelName;
    }

    public SNN addInputLayer(int width, int height, int nChannels, int nFilters, int patchSize, int stride) {
        this.nrows = height;
        this.ncols = width;
        this.nFilters = nFilters;
        this.nChannels = nChannels;
        this.PATCH_SIZE = patchSize;
        this.STRIDE = stride;
        input = new float[nFilters][nrows][ncols];
        layers.add(new Layer(this, layerIndex, LayerType.input, ActivationType.none, patchSize, stride));
        layerIndex++;
        return this;
    }

    public SNN addHiddenLayer(ActivationType activationType, int patchSize, int stride) {
        layers.add(new Layer(this, layerIndex, LayerType.hidden, activationType, patchSize, stride));
        layerIndex++;
        return this;
    }

    public SNN addOutputLayer(ActivationType activationType, int NUMBER_OF_CLASSES) {
        this.NUMBER_OF_CLASSES = NUMBER_OF_CLASSES;
        layers.add(new Layer(this, layerIndex, LayerType.output, activationType, NUMBER_OF_CLASSES));
        layerIndex++;
        return this;
    }

    public SNN summary() {
        String ret = "";
        System.out.println("Model:" + modelName);
        System.out.println("===============================================================");
        System.out.println("Layer(type)                 Output(type)              Param#   ");
        System.out.println("===============================================================");
        System.out.println("Input Layer                 [(None," + input.length + "," + input[0].length + "," + input[0][0].length + ")]\t\t0");
        for (int i = 1; i < layers.size() - 1; i++) {
            System.out.println("Hidden Layer-" + i + "              [(None," + layers.get(i).filters.length + "," + layers.get(i).filters[0].nodes.length + "," + layers.get(i).filters[0].nodes[0].length + ")]\t\t" + decimalFormat(getLayerParams(layers.get(i))));
        }
        System.out.println("Output Layer                [(None," + layers.get(layers.size() - 1).nClasses + ")]\t\t" + decimalFormat(getLayerParams(getOutputLayer())));
        System.out.println("===============================================================");
        System.out.println("Total params        :" + decimalFormat(getTotalParams()));
        System.out.println("Trainable params    :" + decimalFormat(getTrainableParams()));
        System.out.println("Non-Trainable params:" + decimalFormat(getNonTrainableParams()));
        return this;
    }

    public static String decimalFormat(float value) {
        DecimalFormat df = new DecimalFormat("###,###,###");
        return df.format(value);
    }

    public int getTotalParams() {
        int ret = 0;
        for (int i = 0; i < layers.size(); i++) {
            ret += getLayerParams(layers.get(i));
        }
        return ret;
    }

    public int getTrainableParams() {
        int ret = getTotalParams();
        return ret;
    }

    public int getNonTrainableParams() {
        int ret = 0;
        return ret;
    }

    public int getLayerParams(Layer current) {
        int ret = current.getTotalParams();
        return ret;
    }

    public SNN compile() {
        for (int i = 0; i < layers.size(); i++) {
            layers.get(i).nClasses = NUMBER_OF_CLASSES;
        }

        Layer layerOut = getOutputLayer();
        Node[][] nodex = layerOut.filters[0].nodes;

        for (int i = 0; i < nodex.length; i++) {
            for (int j = 0; j < nodex[0].length; j++) {
                Node nd = nodex[i][j];
                nd.partialDerivativeIn = new float[nFilters][layerOut.prevLayer.nrows][layerOut.prevLayer.ncols];
            }
        }

        Layer layerFully = getFullyConnectedLayer();
        for (int i = 0; i < layerFully.nFilter; i++) {
            Node[][] nodes = layerFully.filters[i].nodes;
            for (int j = 0; j < nodes.length; j++) {
                for (int k = 0; k < nodes[0].length; k++) {
                    nodes[j][k].onlyOutputLayerWeight = new float[NUMBER_OF_CLASSES];
                    nodes[j][k].onlyOutputLayerWeightTemp = new float[NUMBER_OF_CLASSES];
                    nodes[j][k].partialDerivativeIn = new float[nFilters][PATCH_SIZE][PATCH_SIZE];
                    nodes[j][k].nextNode = new Node[NUMBER_OF_CLASSES];
                    for (int q = 0; q < NUMBER_OF_CLASSES; q++) {
                        nodes[j][k].nextNode[q] = getOutputLayer().filters[0].nodes[q][0];
                    }
                }
            }
        }
        for (int q = 1; q < layers.size() - 1; q++) {
            Layer ly = layers.get(q);
            for (int i = 0; i < ly.nFilter; i++) {
                Node[][] nodes = ly.filters[i].nodes;
                for (int j = 0; j < nodes.length; j++) {
                    for (int k = 0; k < nodes[0].length; k++) {
                        //nodes[j][k].onlyOutputLayerWeight = null;
                        //nodes[j][k].onlyOutputLayerWeightTemp = null;
                        nodes[j][k].partialDerivativeIn = new float[nFilters][PATCH_SIZE][PATCH_SIZE];
                        //nodes[j][k].nextNode = new Node[1];
                        //nodes[j][k].nextNode[0]=layers.get(q+1).filters[i].nodes[j/2][k/2];
                    }
                }
            }
        }
        return this;
    }

    public Layer getOutputLayer() {
        return layers.get(layers.size() - 1);
    }

    public Layer getInputLayer() {
        return layers.get(0);
    }

    public Layer getLayer(int index) {
        return layers.get(index);
    }

    public Layer getFullyConnectedLayer() {
        return layers.get(layers.size() - 2);
    }

    public SNN fit(DataSetSDNN ds_train, DataSetSDNN ds_valid, float LEARNING_RATE, int EPOCHS, int BATCH_SIZE, boolean isLossOnly) {
        this.BATCH_SIZE = BATCH_SIZE;
        this.EPOCHS = EPOCHS;
        this.LEARNING_RATE = LEARNING_RATE;
        int number_of_batch = ds_train.X.length / BATCH_SIZE;
        System.out.println("\ninitial loss: " + calculateError(ds_train) / ds_train.size);
        System.out.println("");
        float e;
        float[] predicted;
        System.out.println("------------------------------------------------------------------------------------");
        for (int i = 0; i < EPOCHS; i++) {
            float err = 0;
            int k = 0;
            long t = System.currentTimeMillis();
            for (int j = 0; j < number_of_batch; j++) {
                for (int l = 0; l < BATCH_SIZE; l++) {
                    feedInputLayerData(ds_train.X[k]);
                    predicted = forwardPass();
                    e = getCrossEntropyLoss(ds_train.y[k], predicted);
                    err += e;
                    backwardPass(ds_train.y[k], predicted);
                    k++;
                }
            }

            String str = "";
            float acc_loss = err / ds_train.size;
            long dt = System.currentTimeMillis() - t;

            if (isLossOnly) {
                str = (i + 1) + ".epoch, train loss = " + acc_loss +  ", time = " + dt + " ms" + ", lr = " + LEARNING_RATE;
                System.out.println(str);
                lstMetrics.add(str);
            } else {
                float train_acc = test(ds_train, false);
                if (ds_valid!=null) {
                    float valid_acc = test(ds_valid, false);
                    float valid_loss = getValidationLoss(ds_valid);
                    str = (i + 1) + ".epoch, train loss = " + acc_loss + ", train acc = " + train_acc + ", validation loss = " + valid_loss + ", validation acc = " + valid_acc + ", time = " + dt + " ms" + ", lr = " + LEARNING_RATE;
                    System.out.println(str);
                    lstMetrics.add(str);
                } else {
                    str = (i + 1) + ".epoch, train loss = " + acc_loss + ", train acc = " + train_acc + ", time = " + dt + " ms" + ", lr = " + LEARNING_RATE;
                    System.out.println(str);
                    lstMetrics.add(str);
                }
            }
        }
        return this;
    }

    public void saveTrainingMetrics(String filePath) {
        if (!lstMetrics.isEmpty()) {
            FactoryUtils.writeOnFile(filePath, lstMetrics);
        }
    }

    public List<String> loadTrainingMetrics(String filePath) {
        if (FactoryUtils.isFileExist(filePath)) {
            return FactoryUtils.readFileAsList(filePath);
        } else {
            System.err.println("no file at path error");
            return null;
        }
    }

    public void plotLearningMetrics(List<String> lst) {
        for (String st : lst) {
            String[] s = st.split(",");

        }
    }

    public void plotConfusionMatrix() {

    }

    public float getValidationLoss(DataSetSDNN ds) {
        int n = ds.size;
        float[] predicted = null;
        float e = 0;
        float err = 0;
        for (int i = 0; i < n; i++) {
            feedInputLayerData(ds.X[i]);
            predicted = forwardPass();
            e = getCrossEntropyLoss(ds.y[i], predicted);
            err += e;
        }
        return err / n;
    }

    public void addNoise(float val) {
        for (Layer layer : layers) {
            layer.addNoise(val);
        }
    }

    public void printWeights() {
        for (Layer layer : layers) {
            layer.printWeights();
        }

    }

    public float[] forwardPass() {
        for (int i = 1; i < layers.size(); i++) {
            layers.get(i).forwardPass();
        }
        Layer outputLayer = getOutputLayer();
        float[] predicted = outputLayer.filters[0].toArray1D();
        predicted = UtilsSNN.softmax(predicted);
        outputLayer.setOutputLayerSoftMaxValue(predicted);
        return predicted;
    }

    //Back Propagation in training neural networks step by step https://www.youtube.com/watch?v=-zI1bldB8to , https://www.youtube.com/watch?v=0e0z28wAWfg
    //this web site is very informative: https://stackabuse.com/creating-a-neural-network-from-scratch-in-python-multi-class-classification/
    //https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/ 
    //çok çok iyi anlatılmış https://medium.com/@14prakash/back-propagation-is-very-simple-who-made-it-complicated-97b794c97e5c
    //çok çok iyi video : https://www.youtube.com/watch?v=-zI1bldB8to Back Propagation in training neural networks step by step
    //to calculate the derivative of cross entropy loss you can see at : https://deepnotes.io/softmax-crossentropy
    //çok iyi anlatım 8.5 Neural Networks: Backpropagation (UvA - Machine Learning 1 - 2020)  zaman olarak 23:20 de backpropagation anlatılıyor https://www.youtube.com/watch?v=Pz3yKyUYM7k&ab_channel=ErikBekkers
    //değişik ve çok harika süper bir anlatım : Backpropagation Algorithm | Neural Networks https://www.youtube.com/watch?v=sIX_9n-1UbM&ab_channel=FirstPrinciplesofComputerVision
    public void backwardPass(float[] yActual, float[] yPredicted) {
        for (int i = layers.size() - 1; i >= 1; i--) {
            layers.get(i).backwardPass(yActual, yPredicted);
        }
        for (int i = layers.size() - 1; i >= 1; i--) {
            layers.get(i).updateWeights();
        }
    }

    //calculate cross-entropy loss Cross_Entropy= - Sum(y(i)*log(yy(i))
    public float calculateError(DataSetSDNN ds) {
        float ret = 0;
        //isDebug=true;
        for (int i = 0; i < ds.X.length; i++) {
            this.input[0] = ds.X[i][0];
            float[] predicted = forwardPass();
            float err = getCrossEntropyLoss(ds.y[i], predicted);
            ret += err;
        }
        return FactoryUtils.formatFloat(ret);
    }

    public float[][] getArray(String s) {
        s = s.replace("[", "");
        s = s.replace("]", "");
        String[] str = s.split(",");
        float[][] ret = new float[1][str.length];
        for (int i = 0; i < ret[0].length; i++) {
            ret[0][i] = Float.parseFloat(str[i]);
        }
        return ret;
    }

    //look at:https://medium.com/codex/mlps-applications-with-tweaks-in-its-structure-c9aa3f05578 
    public float getCrossEntropyLoss(float[] trueY, float[] predictedY) {
        float err = 0;
        int n = trueY.length;
        for (int i = 0; i < n; i++) {
            if (trueY[i] == 1) {
                err = trueY[i] * (float) Math.log(predictedY[i] + 1E-55); //buradaki log ln demektir veya log e tabanına göre demektir
                return -err;
            }
        }
        return err;
    }

    public void feedInputLayerData(float[][][] f) {
        input = f;
        Layer inputLayer = getInputLayer();
        inputLayer.feedInputData(input);
    }

    public float test(DataSetSDNN ds, boolean isDebug) {
        int nSample = ds.X.length;
        float cnt = 0;
        long t1 = FactoryUtils.tic();
        for (int i = 0; i < nSample; i++) {
            feedInputLayerData(ds.X[i]);
            float[] pr = forwardPass();
            if (isDebug) {
//                CMatrix.getInstance().setArray(X[i][0]).println().map(0, 255).imresize(512, 512).imshow();
//                CMatrix.getInstance().setArray(getLayer(2).filters[0].weights(0)).println().map(0, 255).imresize(512, 512).imshow();
//                CMatrix.getInstance().setArray(getLayer(3).filters[0].output()).println().map(0, 255).imresize(512, 512).imshow();
            }

            int prIndex = FactoryUtils.getMaximumIndex(pr);
            //System.out.println("confidence:"+pr[prIndex]);
            int drIndex = FactoryUtils.getMaximumIndex(ds.y[i]);
            if (isDebug) {
                System.out.println("index=" + prIndex + " predicted = " + Arrays.toString(pr));
                System.out.println("index=" + drIndex + " desired = " + Arrays.toString(ds.y[i]));
            }
            if (prIndex == drIndex) {
                cnt++;
            }
            if (isDebug) {
                t1 = FactoryUtils.toc(t1);
                System.out.println("accuracy rate:" + (cnt / nSample) + "\n");
            }
        }
        return cnt / nSample;
    }

    public void saveModel(String path) {
        UtilsSNN.saveModel(path, this);
    }

}
