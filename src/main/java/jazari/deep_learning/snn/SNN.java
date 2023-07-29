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
    Random rnd;
    int nFilters;
    int nrows;
    int ncols;
    float[][][] input;

    public SNN(String modelName, Random rnd) {
        this.rnd = rnd;
        this.modelName = modelName;
    }

    public SNN addInputLayer(int width,int height,int nFilters,int patchSize, int stride) {
        this.nrows = height;
        this.ncols = width;
        this.nFilters = nFilters;
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

    public Layer getLastLayer() {
        if (layers.size() > 0) {
            return layers.get(layers.size() - 1);
        } else {
            return null;
        }
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
        int ret = current.getSize();
        return ret;
    }

    public SNN compile() {
        return this;
    }
    
    public SNN dump(){
        for (Layer layer : layers) {
            layer.dump();
        }
        return this;
    }

    public SNN fit(List<String> X_train, List<String> y_train, float LEARNING_RATE, int EPOCHS, int BATCH_SIZE) {
        long t1 = FactoryUtils.tic();
        this.BATCH_SIZE = BATCH_SIZE;
        this.EPOCHS = EPOCHS;
        this.LEARNING_RATE = LEARNING_RATE;
        int number_of_batch = X_train.size() / BATCH_SIZE;
//        for (int k = 0; k < this.nchannels; k++) {
//            System.out.println("channel " + k + " epoch: " + 0 + " loss: " + calculateError(X_train, y_train, k));
//        }
        t1 = FactoryUtils.toc(t1);
        BufferedImage img;
        float[][] XX;
        float[][] yy;
        float scale = 1.0f / 255;
        float[][] est_y;
        float[][] log_y;
        CMatrix cm = CMatrix.getInstance();
        for (int i = 0; i < EPOCHS; i++) {
            //t1 = FactoryUtils.toc("backward pass:", t1);
            float err = 0;
            for (int j = 0; j < number_of_batch; j++) {
                for (int l = 0; l < BATCH_SIZE; l++) {
                    for (int k = 0; k < this.nFilters; k++) {
                        img = ImageProcess.rgb2gray(ImageProcess.imread(X_train.get(i)));
                        XX = FactoryUtils.timesScalar(ImageProcess.imageToPixelsFloat(img), scale);

                        t1 = FactoryUtils.toc("--------------------\ndata loading cost :", t1);
                        //this.layers.get(0).filters[k].feedInputData(XX);
                        t1 = FactoryUtils.toc("feeding cost:", t1);
                        forwardPass(k);
                        t1 = FactoryUtils.toc("forward pass cost:", t1);
                        yy = getArray(y_train.get(j * BATCH_SIZE + l));
                        backwardPass(XX, yy, this.LEARNING_RATE, k);
                        t1 = FactoryUtils.toc("backward pass cost:", t1);

                        //calculate error
                        yy = FactoryUtils.timesScalar(getArray(y_train.get(i)), -1);
                        //yy = cm.setArray(yy).timesScalar(-1).toFloatArray2D();

                        est_y = this.layers.get(layers.size() - 1).filters[k].toArray2D();
                        //log_y = FactoryUtils.log
                        log_y = cm.setArray(est_y).logPlusScalar(1E-15f).transpose().toFloatArray2D();

                        err += cm.setArray(yy).multiplyElement(CMatrix.getInstance(log_y)).sumTotal();
                    }
                }
                //t1 = FactoryUtils.toc(j+".chunk cost:", t1);
                //System.out.println(j+".chunk error:"+err);

            }
            //t1 = FactoryUtils.toc(i + ".epoch cost:", t1);
            //float loss = calculateError(X_train, y_train, 0);
            System.out.println(i + ".epoch loss = " + err);

//            if (i % 10 == 0 || i == EPOCHS) {
//                for (int j = 0; j < this.nchannels; j++) {
//                    float loss = calculateError(X_train, y_train, j);
//                    System.out.println("channel " + j + " epoch: " + i + "\tloss: " + loss + "\taccuracy:" + FactoryUtils.formatDouble(loss));
//                }
//            }
        }
        return this;

    }

    //calculate cross-entropy loss Cross_Entropy= - Sum(y(i)*log(yy(i))
    private float calculateError(List<String> X, List<String> y, int ch) {
        float ret = 0;
        BufferedImage img;
        float[][] XX;
        float[][] yy;
        float[][] est_y;
        float[][] log_y;
        float scale = 1.0f / 255;
        CMatrix cm = CMatrix.getInstance();
        //long t1=FactoryUtils.tic();
        for (int i = 0; i < X.size(); i++) {
//            CMatrix cm = CMatrix.getInstance().imread(X.get(i)).rgb2gray().timesScalar(1.0f / 255.0f);
//            float[][] xx = cm.toFloatArray2D();
            img = ImageProcess.rgb2gray(ImageProcess.imread(X.get(i)));
            XX = FactoryUtils.timesScalar(ImageProcess.imageToPixelsFloat(img), scale);

            //this.layers.get(0).filters[ch].feedInputData(XX);
            forwardPass(ch);
            yy = FactoryUtils.timesScalar(getArray(y.get(i)), -1);
            //yy = cm.setArray(yy).timesScalar(-1).toFloatArray2D();

            est_y = this.layers.get(layers.size() - 1).filters[ch].toArray2D();
            //log_y = FactoryUtils.log
            log_y = cm.setArray(est_y).logPlusScalar(1E-15f).transpose().toFloatArray2D();

            float err = cm.setArray(yy).multiplyElement(CMatrix.getInstance(log_y)).sumTotal();
            ret += err;
            //t1 = FactoryUtils.toc(i + ".error process cost:", t1);
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

    private void forwardPass(int ch) {
//        float x1, w1, x2, w2, x3, w3, x4, w4, sum, data;
//        Node n1, n2, n3, n4;
//        for (int i = 0; i < layers.size() - 1; i++) {
//            Layer layer = layers.get(i);
//            for (int j = 0; j < layer.filters.length; j++) {
//                Filter filter=layer.filters[j];
//                for (int k = 0; k < filter.nodes.length; k++) {
//                    for (int l = 0; l < filter.nodes[0].length; l++) {
//                        n1=filter.nodes[k][l];
//                        x1 = n1.data;
//                        w1 = n1.weight;
//                    }
//                }
//            }
//            
//            
//            
//            for (int j = 0; j < layer.nrows; j += 2) {
//                for (int k = 0; k < layer.ncols; k += 2) {
//                    Channel channel = layer.channels.get(ch);
//                    n1 = channel.nodes[j][k];
//                    x1 = n1.data;
//                    w1 = n1.weight;
//                    if (k + 1 >= layer.ncols && j + 1 >= layer.nrows) {
//                        n2 = channel.nodes[j][k - 2 + 1];
//                        x2 = channel.nodes[j][k - 2 + 1].data;
//                        w2 = channel.nodes[j][k - 2 + 1].weight;
//
//                        n3 = channel.nodes[j - 2 + 1][k];
//                        x3 = channel.nodes[j - 2 + 1][k].data;
//                        w3 = channel.nodes[j - 2 + 1][k].weight;
//
//                        n4 = channel.nodes[j - 2 + 1][k - 2 + 1];
//                        x4 = channel.nodes[j - 2 + 1][k - 2 + 1].data;
//                        w4 = channel.nodes[j - 2 + 1][k - 2 + 1].weight;
//                    } else if (k + 1 >= layer.ncols) {
//                        n2 = channel.nodes[j][k - 2 + 1];
//                        x2 = channel.nodes[j][k - 2 + 1].data;
//                        w2 = channel.nodes[j][k - 2 + 1].weight;
//
//                        n3 = channel.nodes[j + 1][k];
//                        x3 = channel.nodes[j + 1][k].data;
//                        w3 = channel.nodes[j + 1][k].weight;
//
//                        n4 = channel.nodes[j + 1][k - 2 + 1];
//                        x4 = channel.nodes[j + 1][k - 2 + 1].data;
//                        w4 = channel.nodes[j + 1][k - 2 + 1].weight;
//                    } else if (j + 1 >= layer.nrows) {
//                        n2 = channel.nodes[j][k + 1];
//                        x2 = channel.nodes[j][k + 1].data;
//                        w2 = channel.nodes[j][k + 1].weight;
//
//                        n3 = channel.nodes[j - 2 + 1][k];
//                        x3 = channel.nodes[j - 2 + 1][k].data;
//                        w3 = channel.nodes[j - 2 + 1][k].weight;
//
//                        n4 = channel.nodes[j - 2 + 1][k + 1];
//                        x4 = channel.nodes[j - 2 + 1][k + 1].data;
//                        w4 = channel.nodes[j - 2 + 1][k + 1].weight;
//                    } else {
//                        n2 = channel.nodes[j][k + 1];
//                        x2 = channel.nodes[j][k + 1].data;
//                        w2 = channel.nodes[j][k + 1].weight;
//
//                        n3 = channel.nodes[j + 1][k];
//                        x3 = channel.nodes[j + 1][k].data;
//                        w3 = channel.nodes[j + 1][k].weight;
//
//                        n4 = channel.nodes[j + 1][k + 1];
//                        x4 = channel.nodes[j + 1][k + 1].data;
//                        w4 = channel.nodes[j + 1][k + 1].weight;
//                    }
//                    //eğer en son hidden katman ise
//                    if (layer.nextLayer.channels.get(ch).nodes[j / 2].length == 1) {
//                        float total_sum = 0;
//                        for (int l = 0; l < layer.nextLayer.channels.get(ch).nodes.length; l++) {
//                            x1 = n1.data;
//                            w1 = n1.weightsToOutputLayer[l];
//
//                            x2 = n2.data;
//                            w2 = n2.weightsToOutputLayer[l];
//
//                            x3 = n3.data;
//                            w3 = n3.weightsToOutputLayer[l];
//
//                            x4 = n4.data;
//                            w4 = n4.weightsToOutputLayer[l];
//
//                            sum = x1 * w1 + x2 * w2 + x3 * w3 + x4 * w4 + channel.bias.data * channel.bias.weight;
////                            data = layer.applyActivation(sum);
//                            data = sum;
//                            layer.nextLayer.channels.get(ch).nodes[l][0].data = data;
//                            total_sum += data;
//                        }
//                        //System.out.println("total_sum = " + total_sum);
//                        layer.nextLayer.channels.get(ch).softmax();
//                        //System.out.println("tot:"+CMatrix.getInstance(layer.nextLayer.toArray2D()).sumTotal());
//                    } else {
//                        sum = x1 * w1 + x2 * w2 + x3 * w3 + x4 * w4 + channel.bias.data * channel.bias.weight;
//                        data = channel.applyActivation(sum);
//                        layer.nextLayer.channels.get(ch).nodes[j / 2][k / 2].data = data;
//                    }
//
//                }
//            }
//        }
    }

    //Back Propagation in training neural networks step by step https://www.youtube.com/watch?v=-zI1bldB8to , https://www.youtube.com/watch?v=0e0z28wAWfg
    //this web site is very informative: https://stackabuse.com/creating-a-neural-network-from-scratch-in-python-multi-class-classification/
    //https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/ 
    //çok çok iyi anlatılmış https://medium.com/@14prakash/back-propagation-is-very-simple-who-made-it-complicated-97b794c97e5c
    //çok çok iyi video : https://www.youtube.com/watch?v=-zI1bldB8to Back Propagation in training neural networks step by step
    private void backwardPass(float[][] X, float[][] Y, float learning_rate, int ch) {
//        float[][] output = CMatrix.getInstance(layers.get(layers.size() - 1).channels.get(ch).toArray2D()).transpose().toFloatArray2D();
//        //calculate the derivative of cross entropy loss you can see at : https://deepnotes.io/softmax-crossentropy
//        float[][] derivative_cross_entropy_loss = new float[Y.length][Y[0].length];
//        int n = layers.size();
//        for (int t = n - 1; t > 0; t--) {
//
//            if (layers.get(t).layerType == LayerType.output) {
//                MlpLayer layer = layers.get(t - 1);
//                for (int i = 0; i < Y[0].length; i++) {
//                    derivative_cross_entropy_loss[0][i] = 2 * (output[0][i] - Y[0][i]);
//                    for (int j = 0; j < 2; j++) {
//                        for (int k = 0; k < 2; k++) {
//                            Node node = layer.channels.get(ch).nodes[j][k];
//                            float dw = derivative_cross_entropy_loss[0][i] * node.data;
//                            node.weightsToOutputLayer[i] = node.weightsToOutputLayer[i] - learning_rate * dw;
//                        }
//                    }
//                    layer.channels.get(ch).bias.weight = layer.channels.get(ch).bias.weight - learning_rate * derivative_cross_entropy_loss[0][i];
//                }
//            } else if (layers.get(t).layerType == LayerType.hidden) {
//                MlpLayer layer = layers.get(t-1);
//                for (int i = 0; i < Y[0].length; i++) {
//                    derivative_cross_entropy_loss[0][i] = 2 * (output[0][i] - Y[0][i]);
//                    for (int j = 0; j < 2; j++) {
//                        for (int k = 0; k < 2; k++) {
//                            Node node = layer.channels.get(ch).nodes[j][k];
////                        float dw = derivative_cross_entropy_loss[0][i] * node.data;
////                        node.weightsToOutputLayer[i] = node.weightsToOutputLayer[i] - learning_rate * dw;
////                        layer.channels.get(ch).bias.weight = layer.channels.get(ch).bias.weight - learning_rate * derivative_cross_entropy_loss[0][i];
//                        }
//                    }
//                }
//            }
//        }
//        Utils.dump(last_layer);
//        int a = 3;
//        //calculate the derivative of sigmoid at output layer
//        float[][] derivative_sigmoid = new float[Y.length][Y[0].length];
//        for (int i = 0; i < Y[0].length; i++) {
//            derivative_sigmoid[0][i] = output[0][i] * (1 - output[0][i]);
//        }

    }

}
