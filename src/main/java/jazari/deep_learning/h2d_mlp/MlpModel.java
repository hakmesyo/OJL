/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.h2d_mlp;

import java.awt.image.BufferedImage;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jazari.factory.FactoryMatrix;
import jazari.image_processing.ImageProcess;

/**
 *
 * @author cezerilab
 */
public class MlpModel {

    private String modelName = "";
    public List<MlpLayer> layers = new ArrayList();
    private int BATCH_SIZE = 16;
    float LEARNING_RATE;
    int EPOCHS;
    Random rnd;
    int nchannels;
    int nrows;
    int ncols;

    public MlpModel(String modelName, int nrows, int ncols, int nchannels, Random rnd) {
        this.rnd = rnd;
        this.modelName = modelName;
        this.nrows = nrows;
        this.ncols = ncols;
        this.nchannels = nchannels;
    }

    public MlpModel addLayer(MlpLayer layer) {
        layer.rnd = this.rnd;
        layers.add(layer);
        return this;
    }

    public MlpLayer getLastLayer() {
        if (layers.size() > 0) {
            return layers.get(layers.size() - 1);
        } else {
            return null;
        }

    }

    public String summary() {
        String ret = "";
        System.out.println("Model:" + modelName);
        System.out.println("===============================================================");
        System.out.println("Layer(type)                 Output(type)                Param# ");
        System.out.println("===============================================================");
        System.out.println("Input Layer                 [(None," + layers.get(0).channels.size() + "," + layers.get(0).channels.get(0).nodes.length + "," + layers.get(0).channels.get(0).nodes[0].length + ")]\t\t0");
        for (int i = 1; i < layers.size() - 1; i++) {
            System.out.println("Hidden Layer-" + i + "              [(None," + layers.get(0).channels.size() + "," + layers.get(i).channels.get(0).nodes.length + "," + layers.get(i).channels.get(0).nodes[0].length + ")]\t\t" + decimalFormat(getLayerParams(layers.get(i))));
        }
        System.out.println("Output Layer                [(None," + layers.get(0).channels.size() + "," + layers.get(layers.size() - 1).channels.get(0).nodes.length + ")]\t\t" + decimalFormat(getOutputLayerParams(layers.get(layers.size() - 1))));
        System.out.println("===============================================================");
        System.out.println("Total params        :" + decimalFormat(getTotalParams()));
        System.out.println("Trainable params    :" + decimalFormat(getTrainableParams()));
        System.out.println("Non-Trainable params:" + decimalFormat(getNonTrainableParams()));
        return ret;
    }

    public static String decimalFormat(float value) {
        DecimalFormat df = new DecimalFormat("###,###,###");
        return df.format(value);
    }

    private int getTotalParams() {
        int ret = 0;
        for (int i = 1; i < layers.size() - 1; i++) {
            ret += getLayerParams(layers.get(i));
        }
        ret += getOutputLayerParams(layers.get(layers.size() - 1));
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

    private int getLayerParams(MlpLayer current) {
        int ret = (current.channels.get(0).nodes.length * current.channels.get(0).nodes[0].length) * 4;
        return ret;
    }

    private int getOutputLayerParams(MlpLayer current) {
        int ret = current.size * current.prevLayer.size;
        return ret;
    }

    public MlpModel compile() {
        //for input layer
        MlpLayer layer = layers.get(0);
        layer.prevLayer = null;
        layer.nextLayer = layers.get(1);

        //for hidden layers
        for (int i = 1; i < layers.size() - 1; i++) {
            layer = layers.get(i);
            layer.prevLayer = layers.get(i - 1);
            layer.nextLayer = layers.get(i + 1);
        }

        //for output layer
        layer = layers.get(layers.size() - 1);
        layer.prevLayer = layers.get(layers.size() - 2);
        layer.nextLayer = null;

        //for input layer
        linkInputLayerNodes();
        //for hidden layers
        for (int i = 1; i < layers.size() - 1; i++) {
            linkHiddenLayerNodes(i);
        }
        //for output layer
//        linkOutputLayerNodes();

        return this;
    }

    private void linkInputLayerNodes() {
        MlpLayer layer = layers.get(0);
        MlpLayer nextLayer = layers.get(1);
        for (int i = 0; i < layer.nrows; i++) {
            for (int j = 0; j < layer.ncols; j++) {
                for (int k = 0; k < layer.nchannels; k++) {
                    Node node = layer.channels.get(k).nodes[i][j];
                    node.nextNode = nextLayer.channels.get(k).nodes[i / 2][j / 2];
                    node.weight = Utils.getRandomWeight(this.rnd);
                }
            }
        }
    }

    private void linkHiddenLayerNodes(int curr) {
        MlpLayer layer = layers.get(curr);
        MlpLayer nextLayer = layers.get(curr + 1);
        for (int i = 0; i < layer.nrows; i++) {
            for (int j = 0; j < layer.ncols; j++) {
                for (int t = 0; t < layer.nchannels; t++) {
                    Node node = layer.channels.get(t).nodes[i][j];
                    node.weight = Utils.getRandomWeight(this.rnd);
                    layer.channels.get(t).bias.weight = Utils.getRandomWeight(this.rnd);

                    if (layer.nextLayer.layerType == LayerType.output) {
                        for (int k = 0; k < layer.nrows; k++) {
                            for (int l = 0; l < layer.ncols; l++) {
                                Node temp = layer.channels.get(t).nodes[k][l];
                                temp.weightsToOutputLayer = new float[layer.nextLayer.size];
                                for (int m = 0; m < layer.nextLayer.size; m++) {
                                    temp.weightsToOutputLayer[m] = Utils.getRandomWeight(this.rnd);
                                }
                            }
                        }
                    }

                    //link next nodes (note that each node has only one next node)
                    if (nextLayer.channels.get(t).nodes[i / 2].length > 1) {
                        node.nextNode = nextLayer.channels.get(t).nodes[i / 2][j / 2];
                    } else {
                        node.nextNode = null;
                    }
                }
            }
        }
    }

//    private void linkOutputLayerNodes() {
//        MlpLayer layer = layers.get(layers.size() - 1);
//        for (int i = 0; i < layer.nrows; i++) {
//            for (int j = 0; j < layer.ncols; j++) {
//                Node node = layer.nodes[i][j];
//                node.next = null;
//            }
//        }
//    }
    public void fit(List<String> X_train, List<String> y_train, float LEARNING_RATE, int EPOCHS, int BATCH_SIZE) {
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
                    for (int k = 0; k < this.nchannels; k++) {
                        img = ImageProcess.rgb2gray(ImageProcess.imread(X_train.get(i)));
                        XX = FactoryUtils.timesScalar(ImageProcess.imageToPixelsFloat(img), scale);

                        //t1 = FactoryUtils.toc("--------------------\ndata loading cost :",t1);
                        this.layers.get(0).channels.get(k).feed(XX);
                        //t1 = FactoryUtils.toc("feeding cost:",t1);
                        forwardPass(k);
                        //t1 = FactoryUtils.toc("forward pass cost:",t1);
                        yy = getArray(y_train.get(j * BATCH_SIZE + l));
                        backwardPass(XX, yy, this.LEARNING_RATE, k);
                        //t1 = FactoryUtils.toc("backward pass cost:",t1);

                        //calculate error
                        yy = FactoryUtils.timesScalar(getArray(y_train.get(i)), -1);
                        //yy = cm.setArray(yy).timesScalar(-1).toFloatArray2D();

                        est_y = this.layers.get(layers.size() - 1).channels.get(k).toArray2D();
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

            this.layers.get(0).channels.get(ch).feed(XX);
            forwardPass(ch);
            yy = FactoryUtils.timesScalar(getArray(y.get(i)), -1);
            //yy = cm.setArray(yy).timesScalar(-1).toFloatArray2D();

            est_y = this.layers.get(layers.size() - 1).channels.get(ch).toArray2D();
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
        float x1, w1, x2, w2, x3, w3, x4, w4, sum, data;
        Node n1, n2, n3, n4;
        for (int i = 0; i < layers.size() - 1; i++) {
            MlpLayer layer = layers.get(i);
            for (int j = 0; j < layer.nrows; j += 2) {
                for (int k = 0; k < layer.ncols; k += 2) {
                    Channel channel = layer.channels.get(ch);
                    n1 = channel.nodes[j][k];
                    x1 = n1.data;
                    w1 = n1.weight;
                    if (k + 1 >= layer.ncols && j + 1 >= layer.nrows) {
                        n2 = channel.nodes[j][k - 2 + 1];
                        x2 = channel.nodes[j][k - 2 + 1].data;
                        w2 = channel.nodes[j][k - 2 + 1].weight;

                        n3 = channel.nodes[j - 2 + 1][k];
                        x3 = channel.nodes[j - 2 + 1][k].data;
                        w3 = channel.nodes[j - 2 + 1][k].weight;

                        n4 = channel.nodes[j - 2 + 1][k - 2 + 1];
                        x4 = channel.nodes[j - 2 + 1][k - 2 + 1].data;
                        w4 = channel.nodes[j - 2 + 1][k - 2 + 1].weight;
                    } else if (k + 1 >= layer.ncols) {
                        n2 = channel.nodes[j][k - 2 + 1];
                        x2 = channel.nodes[j][k - 2 + 1].data;
                        w2 = channel.nodes[j][k - 2 + 1].weight;

                        n3 = channel.nodes[j + 1][k];
                        x3 = channel.nodes[j + 1][k].data;
                        w3 = channel.nodes[j + 1][k].weight;

                        n4 = channel.nodes[j + 1][k - 2 + 1];
                        x4 = channel.nodes[j + 1][k - 2 + 1].data;
                        w4 = channel.nodes[j + 1][k - 2 + 1].weight;
                    } else if (j + 1 >= layer.nrows) {
                        n2 = channel.nodes[j][k + 1];
                        x2 = channel.nodes[j][k + 1].data;
                        w2 = channel.nodes[j][k + 1].weight;

                        n3 = channel.nodes[j - 2 + 1][k];
                        x3 = channel.nodes[j - 2 + 1][k].data;
                        w3 = channel.nodes[j - 2 + 1][k].weight;

                        n4 = channel.nodes[j - 2 + 1][k + 1];
                        x4 = channel.nodes[j - 2 + 1][k + 1].data;
                        w4 = channel.nodes[j - 2 + 1][k + 1].weight;
                    } else {
                        n2 = channel.nodes[j][k + 1];
                        x2 = channel.nodes[j][k + 1].data;
                        w2 = channel.nodes[j][k + 1].weight;

                        n3 = channel.nodes[j + 1][k];
                        x3 = channel.nodes[j + 1][k].data;
                        w3 = channel.nodes[j + 1][k].weight;

                        n4 = channel.nodes[j + 1][k + 1];
                        x4 = channel.nodes[j + 1][k + 1].data;
                        w4 = channel.nodes[j + 1][k + 1].weight;
                    }
                    //eğer en son hidden katman ise
                    if (layer.nextLayer.channels.get(ch).nodes[j / 2].length == 1) {
                        float total_sum = 0;
                        for (int l = 0; l < layer.nextLayer.channels.get(ch).nodes.length; l++) {
                            x1 = n1.data;
                            w1 = n1.weightsToOutputLayer[l];

                            x2 = n2.data;
                            w2 = n2.weightsToOutputLayer[l];

                            x3 = n3.data;
                            w3 = n3.weightsToOutputLayer[l];

                            x4 = n4.data;
                            w4 = n4.weightsToOutputLayer[l];

                            sum = x1 * w1 + x2 * w2 + x3 * w3 + x4 * w4 + channel.bias.data * channel.bias.weight;
//                            data = layer.applyActivation(sum);
                            data = sum;
                            layer.nextLayer.channels.get(ch).nodes[l][0].data = data;
                            total_sum += data;
                        }
                        //System.out.println("total_sum = " + total_sum);
                        layer.nextLayer.channels.get(ch).softmax();
                        //System.out.println("tot:"+CMatrix.getInstance(layer.nextLayer.toArray2D()).sumTotal());
                    } else {
                        sum = x1 * w1 + x2 * w2 + x3 * w3 + x4 * w4 + channel.bias.data * channel.bias.weight;
                        data = channel.applyActivation(sum);
                        layer.nextLayer.channels.get(ch).nodes[j / 2][k / 2].data = data;
                    }

                }
            }
        }
    }

    //Back Propagation in training neural networks step by step https://www.youtube.com/watch?v=-zI1bldB8to , https://www.youtube.com/watch?v=0e0z28wAWfg
    //this web site is very informative: https://stackabuse.com/creating-a-neural-network-from-scratch-in-python-multi-class-classification/
    //https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/ 
    //çok çok iyi anlatılmış https://medium.com/@14prakash/back-propagation-is-very-simple-who-made-it-complicated-97b794c97e5c
    //çok çok iyi video : https://www.youtube.com/watch?v=-zI1bldB8to Back Propagation in training neural networks step by step
    private void backwardPass(float[][] X, float[][] Y, float learning_rate, int ch) {
        float[][] output = CMatrix.getInstance(layers.get(layers.size() - 1).channels.get(ch).toArray2D()).transpose().toFloatArray2D();
        //calculate the derivative of cross entropy loss you can see at : https://deepnotes.io/softmax-crossentropy
        float[][] derivative_cross_entropy_loss = new float[Y.length][Y[0].length];
        int n = layers.size();
        for (int t = n - 1; t > 0; t--) {

            if (layers.get(t).layerType == LayerType.output) {
                MlpLayer layer = layers.get(t - 1);
                for (int i = 0; i < Y[0].length; i++) {
                    derivative_cross_entropy_loss[0][i] = 2 * (output[0][i] - Y[0][i]);
                    for (int j = 0; j < 2; j++) {
                        for (int k = 0; k < 2; k++) {
                            Node node = layer.channels.get(ch).nodes[j][k];
                            float dw = derivative_cross_entropy_loss[0][i] * node.data;
                            node.weightsToOutputLayer[i] = node.weightsToOutputLayer[i] - learning_rate * dw;
                        }
                    }
                    layer.channels.get(ch).bias.weight = layer.channels.get(ch).bias.weight - learning_rate * derivative_cross_entropy_loss[0][i];
                }
            } else if (layers.get(t).layerType == LayerType.hidden) {
                MlpLayer layer = layers.get(t-1);
                for (int i = 0; i < Y[0].length; i++) {
                    derivative_cross_entropy_loss[0][i] = 2 * (output[0][i] - Y[0][i]);
                    for (int j = 0; j < 2; j++) {
                        for (int k = 0; k < 2; k++) {
                            Node node = layer.channels.get(ch).nodes[j][k];
//                        float dw = derivative_cross_entropy_loss[0][i] * node.data;
//                        node.weightsToOutputLayer[i] = node.weightsToOutputLayer[i] - learning_rate * dw;
//                        layer.channels.get(ch).bias.weight = layer.channels.get(ch).bias.weight - learning_rate * derivative_cross_entropy_loss[0][i];
                        }
                    }
                }
            }
        }
//        Utils.dump(last_layer);
//        int a = 3;
//        //calculate the derivative of sigmoid at output layer
//        float[][] derivative_sigmoid = new float[Y.length][Y[0].length];
//        for (int i = 0; i < Y[0].length; i++) {
//            derivative_sigmoid[0][i] = output[0][i] * (1 - output[0][i]);
//        }

    }

}
