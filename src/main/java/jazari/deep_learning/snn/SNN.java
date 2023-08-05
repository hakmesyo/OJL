/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.snn;

import java.awt.image.BufferedImage;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jazari.image_processing.ImageProcess;

/**
 *
 * @author cezerilab
 */
public class SNN {

    String modelName = "";
    List<Layer> layers = new ArrayList();
    int layerIndex;
    int BATCH_SIZE;
    float LEARNING_RATE;
    int EPOCHS;
    int PATCH_SIZE;
    int STRIDE;
    Random rnd;
    int nFilters;
    int nrows;
    int ncols;
    float[][][] input;

    public SNN(String modelName, Random rnd) {
        this.rnd = rnd;
        this.modelName = modelName;
    }

    public SNN addInputLayer(int width, int height, int nFilters, int patchSize, int stride) {
        this.nrows = height;
        this.ncols = width;
        this.nFilters = nFilters;
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
        System.out.println("Output Layer                [(None," + layers.get(layers.size() - 1).nClasses + ")]\t\t" + decimalFormat(getLayerParams(layers.get(layers.size() - 1))));
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
        return this;
    }

    public Layer getOutputLayer() {
        return layers.get(layers.size() - 1);
    }

    public Layer getInpuLayer() {
        return layers.get(0);
    }

    public Layer getHiddenLayer(int index) {
        return layers.get(index);
    }

    public SNN dump() {
        for (Layer layer : layers) {
            layer.dump();
        }
        return this;
    }

    public SNN fit(float[][][][] X_train, float[][] y_train, float LEARNING_RATE, int EPOCHS, int BATCH_SIZE) {
        long t1 = FactoryUtils.tic();
        this.BATCH_SIZE = BATCH_SIZE;
        this.EPOCHS = EPOCHS;
        this.LEARNING_RATE = LEARNING_RATE;
        int number_of_batch = X_train.length / BATCH_SIZE;
        System.out.println("initial loss: " + calculateError(X_train, y_train));

        t1 = FactoryUtils.toc(t1);
        float e;

        float[] predicted;// = forwardPass();
        for (int i = 0; i < EPOCHS; i++) {
            t1 = FactoryUtils.toc(i + ".epoch elapsed time:", t1);
            float err = 0;
            int k = 0;
            for (int j = 0; j < number_of_batch; j++) {
                for (int l = 0; l < BATCH_SIZE; l++) {
                    //t1 = FactoryUtils.toc(l+".loop cost:",t1);
                    feedInputLayerData(X_train[k]);
                    //input=X_train[k];
                    //t1 = FactoryUtils.toc("feed input data cost:",t1);
                    //forwardPass();
                    predicted = forwardPass();
                    //t1 = FactoryUtils.toc("forwardpass cost:",t1);
                    e = getCrossEntropyLoss(y_train[k], predicted);
                    //t1 = FactoryUtils.toc("getCrossEntropyLoss cost:",t1);
                    //System.out.println("error = " + e);
                    err += e;
                    //t2=FactoryUtils.toc("forwardpass elapsed time:",t2);
                    backwardPass(y_train[k], predicted);
                    //t1 = FactoryUtils.toc("backwardPass cost:",t1);
                    k++;
                    //System.out.println("");
                }
            }
            System.out.println(i + ".epoch loss = " + err);
//            if (i%100==0) {
//                LEARNING_RATE*=0.1;
//            }
        }
        return this;
    }

    public SNN fitV2(float[][][][] X_train, float[][] y_train, float LEARNING_RATE, int EPOCHS, int BATCH_SIZE) {
//        long t1 = FactoryUtils.tic();
//        this.BATCH_SIZE = BATCH_SIZE;
//        this.EPOCHS = EPOCHS;
//        this.LEARNING_RATE = LEARNING_RATE;
//        int number_of_batch = X_train.length / BATCH_SIZE;
//        System.out.println("initial loss: " + calculateError(X_train, y_train));
//
//        t1 = FactoryUtils.toc(t1);
//        long t2 = FactoryUtils.tic();
//        //BufferedImage img;
//        //float[][] XX;
//        //float[][] yy;
//        //float scale = 1.0f / 255;
//        //float[][] est_y;
//        //float[][] log_y;
//        //CMatrix cm = CMatrix.getInstance();
//        float e;
//        for (int i = 0; i < EPOCHS; i++) {
//            t1 = FactoryUtils.toc(i + ".epoch elapsed time:", t1);
//            float err = 0;
//            int k = 0;
//            for (int j = 0; j < number_of_batch; j++) {
//                //t2=FactoryUtils.toc("--------------------\nbatch (100 images) elapsed time:",t2);
//                for (int l = 0; l < BATCH_SIZE; l++) {
//                    //img = ImageProcess.rgb2gray(ImageProcess.imread(X_train.get(i)));
//                    //XX = FactoryUtils.timesScalar(ImageProcess.imageToPixelsFloat(img), scale);
//                    //t2=FactoryUtils.toc("--------------------\nimage load elapsed time:",t2);
//                    feedInputLayerData(X_train[k]);
//                    //forwardPass();
//                    float[] predicted = forwardPass();
//                    //t2=FactoryUtils.toc("forwardpass elapsed time:",t2);
//                    e = getCrossEntropyLoss(y_train[k]);
//                    err += e;
//                    backwardPass(X_train[k][0], y_train[k], predicted,getOutputLayer());
//                    k++;
//                    //t2=FactoryUtils.toc("error calculation elapsed time:",t2);
//                    //System.out.println((j*l+l)+".input data error:"+err);
//                    /*
//                    for (int k = 0; k < this.nFilters; k++) {
//                        img = ImageProcess.rgb2gray(ImageProcess.imread(X_train.get(i)));
//                        XX = FactoryUtils.timesScalar(ImageProcess.imageToPixelsFloat(img), scale);
//
//                        t1 = FactoryUtils.toc("--------------------\ndata loading cost :", t1);
//                        //this.layers.get(0).filters[k].feedInputData(XX);
//                        t1 = FactoryUtils.toc("feeding cost:", t1);
//                        forwardPass();
//                        t1 = FactoryUtils.toc("forward pass cost:", t1);
//                        yy = getArray(y_train.get(j * BATCH_SIZE + l));
//                        backwardPass(XX, yy, this.LEARNING_RATE, k);
//                        t1 = FactoryUtils.toc("backward pass cost:", t1);
//
//                        //calculate error
//                        yy = FactoryUtils.timesScalar(getArray(y_train.get(i)), -1);
//                        //yy = cm.setArray(yy).timesScalar(-1).toFloatArray2D();
//
//                        est_y = this.layers.get(layers.size() - 1).filters[k].toArray2D();
//                        //log_y = FactoryUtils.log
//                        log_y = cm.setArray(est_y).logPlusScalar(1E-15f).transpose().toFloatArray2D();
//
//                        err += cm.setArray(yy).multiplyElement(CMatrix.getInstance(log_y)).sumTotal();
//                    }
//                     */
//                }
//                //t1 = FactoryUtils.toc(j+".chunk cost:", t1);
//                //System.out.println(j+".chunk error:"+err);
//
//            }
//            //t1 = FactoryUtils.toc(i + ".epoch cost:", t1);
//            //float loss = calculateError(X_train, y_train, 0);
//            System.out.println(i + ".epoch loss = " + err);
//
////            if (i % 10 == 0 || i == EPOCHS) {
////                for (int j = 0; j < this.nchannels; j++) {
////                    float loss = calculateError(X_train, y_train, j);
////                    System.out.println("channel " + j + " epoch: " + i + "\tloss: " + loss + "\taccuracy:" + FactoryUtils.formatDouble(loss));
////                }
////            }
//        }
        return this;

    }

    //calculate cross-entropy loss Cross_Entropy= - Sum(y(i)*log(yy(i))
    private float calculateError(float[][][][] X, float[][] y) {
        float ret = 0;
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
    
    long t1 = FactoryUtils.tic();
    private float[] forwardPass() {
        //System.out.println("");        
        //t1 = FactoryUtils.toc("coming cost:", t1);
        Layer output = getOutputLayer();
        float[] predicted = output.forwardPass();
        //t1 = FactoryUtils.toc("forward cost:", t1);
        predicted = UtilsSNN.softmax(predicted);
        //t1 = FactoryUtils.toc("softmax cost:", t1);
        output.setOutputLayerSoftMaxValue(predicted);
        //t1 = FactoryUtils.toc("setOutputLayerSoftMaxValue cost:", t1);
        return predicted;
    }

    //Back Propagation in training neural networks step by step https://www.youtube.com/watch?v=-zI1bldB8to , https://www.youtube.com/watch?v=0e0z28wAWfg
    //this web site is very informative: https://stackabuse.com/creating-a-neural-network-from-scratch-in-python-multi-class-classification/
    //https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/ 
    //çok çok iyi anlatılmış https://medium.com/@14prakash/back-propagation-is-very-simple-who-made-it-complicated-97b794c97e5c
    //çok çok iyi video : https://www.youtube.com/watch?v=-zI1bldB8to Back Propagation in training neural networks step by step
    //to calculate the derivative of cross entropy loss you can see at : https://deepnotes.io/softmax-crossentropy
    private void backwardPass(float[] y, float[] yPredicted) {
        //long t1=FactoryUtils.tic();
        //SNN freezedSNN = copy();
        //t1=FactoryUtils.toc("copy cost:",t1);
        //derivative of the categorical cross_entropy_loss
        float[] dc = subtract(yPredicted, y);

        //first update weights of output layer nodes along with bias weight 
        Layer outputLayer = getOutputLayer();
        //Layer frzOutputLayer = freezedSNN.getOutputLayer();
        for (int i = 0; i < y.length; i++) {
            Node[][][] lastHiddenLayerNodes = outputLayer.filters[0].nodes[i][0].prevNodes4OutputLayer;
            //Node[][][] frzLastHiddenLayerNodes = frzOutputLayer.filters[0].nodes[i][0].prevNodes4OutputLayer;
            for (int j = 0; j < nFilters; j++) {
                for (int k = 0; k < PATCH_SIZE; k++) {
                    for (int n = 0; n < PATCH_SIZE; n++) {
                        //calculate partial derivative of loss with respect to the corresponding node weights
                        float partialDerivative = dc[i] * lastHiddenLayerNodes[j][k][n].data;
                        //float partialDerivative = dc[i] * frzLastHiddenLayerNodes[j][k][n].data;
                        //update weights by W(next)=W-learning_rate*partial_derivative
                        lastHiddenLayerNodes[j][k][n].weightOutputLayer[i] = lastHiddenLayerNodes[j][k][n].weightOutputLayer[i] - LEARNING_RATE * partialDerivative;
                    }
                }
            }
            outputLayer.filters[0].nodes[i][0].biasWeight = outputLayer.filters[0].nodes[i][0].biasWeight - LEARNING_RATE * dc[i];
        }
        //t1=FactoryUtils.toc("remaining cost:",t1);
    }

    //look at:https://medium.com/codex/mlps-applications-with-tweaks-in-its-structure-c9aa3f05578 
    private float getCrossEntropyLoss(float[] trueY, float[] predictedY) {
        float err = 0;
        float[] predictedY_1 = this.layers.get(layers.size() - 1).filters[0].toArray1D();
        for (int i = 0; i < trueY.length; i++) {
            err += trueY[i] * Math.log(predictedY[i]);
        }
        err = -err;
        //System.out.println("err = " + err);
        return err;
    }

    public void feedInputLayerData(float[][][] f) {
        input = f;
    }

    private float[] subtract(float[] s, float[] y) {
        float[] ret = new float[s.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = s[i] - y[i];
        }
        return ret;
    }

    public SNN copy() {
        SNN ret = new SNN(modelName, rnd);
        for (int i = 0; i < layers.size(); i++) {
            ret.layers.add(this.layers.get(i).copy());
        }
        return ret;
    }

}
