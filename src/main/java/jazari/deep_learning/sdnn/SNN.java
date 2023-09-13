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

    private int getTotalParams() {
        int ret = 0;
        for (int i = 0; i < layers.size(); i++) {
            ret += getLayerParams(layers.get(i));
        }
        return ret;
    }

    private int getTrainableParams() {
        int ret = getTotalParams();
        return ret;
    }

    private int getNonTrainableParams() {
        int ret = 0;
        return ret;
    }

    private int getLayerParams(Layer current) {
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

    public SNN fit(float[][][][] X_train, float[][] y_train, float LEARNING_RATE, int EPOCHS, int BATCH_SIZE) {
        long t1 = FactoryUtils.tic();
        this.BATCH_SIZE = BATCH_SIZE;
        this.EPOCHS = EPOCHS;
        this.LEARNING_RATE = LEARNING_RATE;
        int number_of_batch = X_train.length / BATCH_SIZE;
        System.out.println("\ninitial loss: " + calculateError(X_train, y_train) / y_train.length);
        t1 = FactoryUtils.toc(t1);
        System.out.println("");
        float e;

        float[] predicted;
        CMatrix cm1 = CMatrix.getInstance();
        CMatrix cm2 = CMatrix.getInstance();
        int incr = 0;
        long t = System.currentTimeMillis();
//        float[][] weights = getLayer(1).filters[0].weights(0);
//        cm1.setArray(weights).println().map(0, 255).imresize(512, 512).imshowRefresh();
        System.out.println("------------------------------------------------------------------------------------");
        for (int i = 0; i < EPOCHS; i++) {
            //LEARNING_RATE=(i%(EPOCHS/4)==0)?LEARNING_RATE*0.5f:LEARNING_RATE;
            //t1 = FactoryUtils.toc(i + ".epoch elapsed time lr=" + LEARNING_RATE + " :", t1);
            float err = 0;
            int k = 0;
            t = System.currentTimeMillis();
            for (int j = 0; j < number_of_batch; j++) {
                for (int l = 0; l < BATCH_SIZE; l++) {
                    feedInputLayerData(X_train[k]);
                    predicted = forwardPass();
                    e = getCrossEntropyLoss(y_train[k], predicted);
                    err += e;
                    backwardPass(y_train[k], predicted);
                    k++;
                }
//                System.out.println((incr++) + ".güncellenen weightler");
//                printWeights();
//                System.out.println("");
            }

            long dt = System.currentTimeMillis() - t;
            float acc = test(X_train, y_train, false);
            System.out.println((i + 1) + ".epoch, loss = " + err / y_train.length + ", lr = " + LEARNING_RATE + ", acc = " + acc + ", time = " + dt + " ms");

            //cm1.setArray(getLayer(1).filters[0].weights(0)).println().map(0, 255).imresize(512, 512).imshowRefresh();
            //cm1.setArray(getLayer(1).filters[0].output()).println().map(0, 255).imresize(512, 512).imshowRefresh();
            if (i % 10 == 0) {
                //LEARNING_RATE*=0.1;
//                float acc = test(X_train, y_train, false);
//                System.out.println("**************" + i + ".epoch accuracy rate = " + acc);
                //float[][] weights=getLayer(1).filters[0].getWeightsIn();
                //cm.setArray(weights).println().map(0, 255).imresize(128,128).imshow();
                //JITTER_VALUE*=0.5;
                //addNoise(JITTER_VALUE);
            }
            //cm1.setArray(getLayer(2).filters[0].output()).println().map(0, 255).imresize(512, 512).imshow();
            if (i % 5 == 0) {
                //                cm1.setArray(getLayer(1).filters[0].output()).println().map(0, 255).imresize(512, 512).imshow();
                //                   cm1.setArray(getLayer(2).filters[0].weights(0)).map(0, 255).imresize(512, 512).imshow()
                //                      .setArray(getLayer(2).filters[0].weights(1)).map(0, 255).imresize(512, 512).imshow()
                //                      .setArray(getLayer(2).filters[0].weights(2)).map(0, 255).imresize(512, 512).imshow()
                //                      .setArray(getLayer(2).filters[0].weights(3)).map(0, 255).imresize(512, 512).imshow()
                //                      .setArray(getLayer(2).filters[0].weights(4)).map(0, 255).imresize(512, 512).imshow()
                //                      .setArray(getLayer(2).filters[0].weights(5)).map(0, 255).imresize(512, 512).imshow()
                //                      .setArray(getLayer(2).filters[0].weights(6)).map(0, 255).imresize(512, 512).imshow()
                //                      .setArray(getLayer(2).filters[0].weights(7)).map(0, 255).imresize(512, 512).imshow()
                //                      .setArray(getLayer(2).filters[0].weights(8)).map(0, 255).imresize(512, 512).imshow()
                //                      .setArray(getLayer(2).filters[0].weights(9)).map(0, 255).imresize(512, 512).imshow()

                ;
                int qw = 3;
//                float[][] output=getLayer(1).filters[0].output();
//                cm2.setArray(output).map(0, 255).imresize(512,512).imshowRefresh();

//                LEARNING_RATE*=0.1;
            }
        }
        return this;
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

    long t1 = FactoryUtils.tic();

    private float[] forwardPass() {
        for (int i = 1; i < layers.size(); i++) {
            layers.get(i).forwardPass();
        }
        Layer outputLayer = getOutputLayer();
        float[] predicted = outputLayer.filters[0].toArray1D();
        //predicted = FactoryNormalization.normalizeMinMax(predicted);
        predicted = UtilsSNN.softmax(predicted);
        outputLayer.setOutputLayerSoftMaxValue(predicted);
        //t1 = FactoryUtils.toc("setOutputLayerSoftMaxValue cost:", t1);
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
    private void backwardPass(float[] yActual, float[] yPredicted) {
        for (int i = layers.size() - 1; i >= 1; i--) {
            layers.get(i).backwardPass(yActual, yPredicted);
        }
        for (int i = layers.size() - 1; i >= 1; i--) {
            layers.get(i).updateWeights();
            //layers.get(i).printWeights();
        }
    }


    //calculate cross-entropy loss Cross_Entropy= - Sum(y(i)*log(yy(i))
    private float calculateError(float[][][][] X, float[][] y) {
        float ret = 0;
        //isDebug=true;
        for (int i = 0; i < X.length; i++) {
            this.input[0] = X[i][0];
            float[] predicted = forwardPass();
            float err = getCrossEntropyLoss(y[i], predicted);
            ret += err;
        }
        return FactoryUtils.formatFloat(ret);
    }

    private float[][] getArray(String s) {
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
    private float getCrossEntropyLoss(float[] trueY, float[] predictedY) {
        float err = 0;
        //float[] predictedY_1 = this.layers.get(layers.size() - 1).filters[0].toArray1D();
        int n = 0;
        for (int i = 0; i < trueY.length; i++) {
            if (trueY[i] == 1) {
                err = trueY[i] * (float) Math.log(predictedY[i] + 1E-55); //buradaki log ln demektir veya log e tabanına göre demektir
                return -err;
            }
        }
//        for (int i = 0; i < trueY.length; i++) {
//            err += trueY[i] * Math.log(predictedY[i] + 1E-55); //buradaki log ln demektir veya log e tabanına göre demektir
//        }
//        err = -err;
        //System.out.println("err = " + err);
        return err;
    }

    public void feedInputLayerData(float[][][] f) {
        input = f;
        Layer inputLayer = getInputLayer();
        inputLayer.feedInputData(input);
        //CMatrix cm = CMatrix.getInstance(input[0]).map(0, 255).imshow().imresize(500,500).imshow();
        //int a=1;
    }

    public float test(float[][][][] X, float[][] y, boolean isDebug) {
        int nSample = X.length;
        float cnt = 0;
        long t1 = FactoryUtils.tic();
        for (int i = 0; i < nSample; i++) {
            feedInputLayerData(X[i]);
            float[] pr = forwardPass();
            if (isDebug) {
                //CMatrix.getInstance().setArray(X[i][0]).println().map(0, 255).imresize(512, 512).imshow();
//                CMatrix.getInstance().setArray(getLayer(2).filters[0].weights(0)).println().map(0, 255).imresize(512, 512).imshow();
//                CMatrix.getInstance().setArray(getLayer(2).filters[0].output()).println().map(0, 255).imresize(512, 512).imshow();
            }

            int prIndex = FactoryUtils.getMaximumIndex(pr);
            //System.out.println("confidence:"+pr[prIndex]);
            int drIndex = FactoryUtils.getMaximumIndex(y[i]);
            if (isDebug) {
                System.out.println("index=" + prIndex + " predicted = " + Arrays.toString(pr));
                System.out.println("index=" + drIndex + " desired = " + Arrays.toString(y[i]));
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
