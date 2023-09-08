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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import jazari.factory.FactoryNormalization;
import jazari.image_processing.ImageProcess;

/**
 *
 * @author cezerilab
 */
public class SNN {

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

//    public SNN dump() {
//        for (Layer layer : layers) {
//            layer.dump();
//        }
//        return this;
//    }
    public SNN fit(float[][][][] X_train, float[][] y_train, float LEARNING_RATE, int EPOCHS, int BATCH_SIZE) {
        long t1 = FactoryUtils.tic();
        this.BATCH_SIZE = BATCH_SIZE;
        this.EPOCHS = EPOCHS;
        this.LEARNING_RATE = LEARNING_RATE;
        int number_of_batch = X_train.length / BATCH_SIZE;
        System.out.println("initial loss: " + calculateError(X_train, y_train));

        t1 = FactoryUtils.toc(t1);
        float e;

        float[] predicted;
        //CMatrix cm = CMatrix.getInstance();
        int incr = 0;
        for (int i = 1; i < EPOCHS; i++) {
            //LEARNING_RATE=(i%(EPOCHS/4)==0)?LEARNING_RATE*0.5f:LEARNING_RATE;
            //t1 = FactoryUtils.toc(i + ".epoch elapsed time lr=" + LEARNING_RATE + " :", t1);
            float err = 0;
            int k = 0;
            for (int j = 0; j < number_of_batch; j++) {
                for (int l = 0; l < BATCH_SIZE; l++) {
                    //t1 = FactoryUtils.toc(l+".loop cost:",t1);
                    feedInputLayerData(X_train[k]);
                    //cm.setArray(FactoryUtils.toARGB(X_train[k])).imshow();
                    //t1 = FactoryUtils.toc("feedInputLayerData cost:", t1);
                    predicted = forwardPass();
//                    System.out.println(j + ":" + l + " predicted = " + Arrays.toString(predicted));
                    //t1 = FactoryUtils.toc("forwardpass cost:", t1);

                    e = getCrossEntropyLoss(y_train[k], predicted);
                    //t1 = FactoryUtils.toc("getCrossEntropyLoss cost:",t1);
                    //if (j==0 && l<5) System.out.println("class index:"+FactoryUtils.getMaximumIndex(y_train[k])+" error = " + e+" actual:"+Arrays.toString(y_train[k])+" predicted:"+Arrays.toString(predicted));
                    err += e;
//                    System.out.println("current err:" + e + " total err:" + err);
//                    t1=FactoryUtils.toc("forwardpass elapsed time:",t1);
//                    System.out.println("-->*******--> backpropagation dan önceki weightler");
//                    printWeights();
                    backwardPass(y_train[k], predicted);
//                    System.out.println((incr++) + ".güncellenen weightler");
//                    printWeights();
//                    System.out.println("");

//                    System.out.println("backpropagation dan sonraki weightler");
//                    printWeights();
                    //t1 = FactoryUtils.toc("backwardPass cost:",t1);
//                    if (incr>14 && l>=20) {
//                        System.out.println("e:"+e+" err:"+err); 
//                    }
                    k++;
                    //System.out.println("");
                }
//                System.out.println((incr++) + ".güncellenen weightler");
//                printWeights();
//                System.out.println("");
            }
            System.out.println(i + ".epoch loss = " + err / y_train.length + " lr=" + LEARNING_RATE);
            if (i % 10 == 0) {
                //LEARNING_RATE*=0.1;
                float acc=test(X_train, y_train, false);
                System.out.println("**************"+i + ".epoch accuracy rate = " + acc);
            }
            if(i%50 == 0){
                LEARNING_RATE*=0.1;
            }
        }
        return this;
    }

    public void printWeights() {
        for (Layer layer : layers) {
            layer.printWeights();
        }

    }

    long t1 = FactoryUtils.tic();

//    private float[] forwardPassV1() {
//        //System.out.println("");        
//        //t1 = FactoryUtils.toc("coming cost:", t1);
//        Layer output = getOutputLayer();
//        float[] predicted = output.forwardPass();
//        //t1 = FactoryUtils.toc("forward cost:", t1);
//        predicted = UtilsSNN.softmax(predicted);
////        float sum=0;
////        for (int i = 0; i < predicted.length; i++) {
////            sum+=predicted[i];
////        }
////        System.out.println("sum = " + sum);
//        //t1 = FactoryUtils.toc("softmax cost:", t1);
//        output.setOutputLayerSoftMaxValue(predicted);
//        //t1 = FactoryUtils.toc("setOutputLayerSoftMaxValue cost:", t1);
//        return predicted;
//    }
//    private float[] forwardPassV2() {
//        //System.out.println("");        
//        //t1 = FactoryUtils.toc("coming cost:", t1);
//        //t1 = FactoryUtils.toc("forward cost:", t1);
//        for (int i = 1; i < layers.size() - 1; i++) {
//            Layer layer = layers.get(i);
//            for (int j = 0; j < nFilters; j++) {
//                Filter filter = layer.filters[j];
//                Node[][] nodes = filter.nodes;
//                for (int k = 0; k < nodes.length; k++) {
//                    for (int t = 0; t < nodes[0].length; t++) {
//                        Node[] prevNodes = nodes[k][t].prevNodes;
//                        float sum = 0;
//                        for (int m = 0; m < prevNodes.length; m++) {
//                            sum += prevNodes[m].outputData * nodes[k][t].weight[m];
//                        }
//                        sum += nodes[k][t].biasWeight;
//                        nodes[k][t].outputData = UtilsSNN.applyActivation(sum, layer.activationType);
//                    }
//                }
//            }
//        }
//        Layer outputLayer = getOutputLayer();
//        float[] predicted = outputLayer.filters[0].toArray1D();
//        predicted = UtilsSNN.softmax(predicted);
//        outputLayer.setOutputLayerSoftMaxValue(predicted);
//        //t1 = FactoryUtils.toc("setOutputLayerSoftMaxValue cost:", t1);
//        return predicted;
//    }
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
//        Layer layer=getOutputLayer();
//        layer.backwardPass(yActual, yPredicted);
//        layer.updateWeights();

//        Layer layer2=getFullyConnectedLayer();
//        layer2.backwardPass(yActual, yPredicted);
//        layer2.updateWeights();
    }

    //Back Propagation in training neural networks step by step https://www.youtube.com/watch?v=-zI1bldB8to , https://www.youtube.com/watch?v=0e0z28wAWfg
    //this web site is very informative: https://stackabuse.com/creating-a-neural-network-from-scratch-in-python-multi-class-classification/
    //https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/ 
    //çok çok iyi anlatılmış https://medium.com/@14prakash/back-propagation-is-very-simple-who-made-it-complicated-97b794c97e5c
    //çok çok iyi video : https://www.youtube.com/watch?v=-zI1bldB8to Back Propagation in training neural networks step by step
    //to calculate the derivative of cross entropy loss you can see at : https://deepnotes.io/softmax-crossentropy
    //çok iyi anlatım 8.5 Neural Networks: Backpropagation (UvA - Machine Learning 1 - 2020)  zaman olarak 23:20 de backpropagation anlatılıyor https://www.youtube.com/watch?v=Pz3yKyUYM7k&ab_channel=ErikBekkers
    private void backwardPass_eski(float[] y, float[] yPredicted) {
//        //long t1=FactoryUtils.tic();
//        //derivative of the categorical cross_entropy_loss
//        float[] dc = FactoryUtils.subtract(yPredicted, y);
//
//        /**
//         * Phase 1 : first calculate the change in weights of output layer nodes
//         * along with bias weight. Note that original weights are not updated
//         * now. All weights will be updated at the end. So we stored updated
//         * weights in corresponding temporary weights
//         */
//        Layer outputLayer = getOutputLayer();
//        for (int i = 0; i < y.length; i++) {
//            Node node = outputLayer.filters[0].nodes[i][0];
//            Node[] prevNodes = node.prevNodes;
//            for (int j = 0; j < prevNodes.length; j++) {
//                //update weights by W(next)=W-learning_rate*partial_derivative
//                node.partialDerivative = dc[i];
//                node.weightTemp[j] = node.weight[j] - LEARNING_RATE * node.partialDerivative * prevNodes[j].outputData;
//                prevNodes[j].onlyOutputLayerWeightTemp[i] = node.weightTemp[j];
//            }
//            node.biasWeightTemp = node.biasWeight - LEARNING_RATE * dc[i];
//        }
//
//        /**
//         * Phase 2 : Backpropagate error through previous hidden layers
//         */
//        for (int q = layers.size() - 2; q > 0; q--) {
//            Layer currentLayer = layers.get(q);
//            //if it is the last hidden layer (last hidden layer is simulates te fully connected dense layer)
//            if (q == layers.size() - 2) {
//                for (int j = 0; j < nFilters; j++) {
//                    Node[][] nodes = currentLayer.filters[j].nodes;
//                    for (int i = 0; i < nodes.length; i++) {
//                        for (int k = 0; k < nodes[0].length; k++) {
//                            Node node = nodes[j][k];
//                            float[] outWeights = nodes[i][k].onlyOutputLayerWeightTemp;
//                            float sum = 0;
//                            for (int m = 0; m < outWeights.length; m++) {
//                                sum += outWeights[m] * dc[m];
//                            }
//                            node.partialDerivative = UtilsSNN.sigmoidDerivative(node.getWeightedSum()) * sum;
//                            float pd = 0;
//                            for (int n = 0; n < node.prevNodes.length; n++) {
//                                pd = UtilsSNN.sigmoidDerivative(node.getWeightedSum()) * node.prevNodes[n].outputData * sum;
//                                node.weightTemp[n] = node.weight[n] - LEARNING_RATE * pd;
//                            }
//                            pd = UtilsSNN.sigmoidDerivative(node.getWeightedSum()) * node.biasValue * sum;
//                            node.biasWeightTemp = node.biasWeight - LEARNING_RATE * pd;
//                        }
//                    }
//                }
//            } else {
//                for (int j = 0; j < nFilters; j++) {
//                    Node[][] nodes = currentLayer.filters[j].nodes;
//                    for (int i = 0; i < nodes.length; i++) {
//                        for (int k = 0; k < nodes[0].length; k++) {
//                            Node node = nodes[i][k];
//                            Node[] prevNodes = node.prevNodes;
//                            Node nextNode = node.nextNode[0];
//                            float pd = 0;
//                            node.partialDerivative = nextNode.partialDerivative * UtilsSNN.sigmoidDerivative(node.getWeightedSum());
//                            for (int m = 0; m < prevNodes.length; m++) {
//                                pd = nextNode.partialDerivative * UtilsSNN.sigmoidDerivative(node.getWeightedSum()) * prevNodes[m].outputData;
//                                node.weightTemp[m] = node.weight[m] - LEARNING_RATE * pd;
//                            }
//                            pd = nextNode.partialDerivative * UtilsSNN.sigmoidDerivative(node.getWeightedSum());
//                            node.biasWeightTemp = node.biasWeight - LEARNING_RATE * pd;
//                        }
//                    }
//                }
//            }
//        }
//
//        /**
//         * Phase 3: update original weights with tempweights
//         *
//         */
//        for (Layer layer : layers) {
//            layer.updateWeights();
//            layer.printWeights();
//        }
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
            int prIndex = FactoryUtils.getMaximumIndex(pr);
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

}
