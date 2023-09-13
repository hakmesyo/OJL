/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package jazari.deep_learning.snn;

import java.io.Serializable;
import java.util.Arrays;
import jazari.factory.FactoryNormalization;
import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class Node  implements Serializable{

    int row;
    int col;
    float biasValue = 1.0f;
    float biasWeight;
    float biasWeightTemp;
    float[][][] weightIn;
    float[] onlyOutputLayerWeight;
    float[] onlyOutputLayerWeightTemp;
    float[][][] weightTempIn;
    LayerType layerType;
    Layer layer;
    Node[][][] prevNodes;
    Node[] nextNode = new Node[1];
    Filter filter;
    int filterIndex;
    float dataOut;
    float[][][] partialDerivativeIn = null;
    float gradient;
    int patch_size;

    public Node(Filter filter, int row, int col) {
        this.filter = filter;
        this.filterIndex = filter.filterIndex;
        this.layer = filter.layer;
        this.layerType = filter.layerType;
        this.row = row;
        this.col = col;
        this.patch_size=filter.patchSize;
        //biasWeight = UtilsSNN.getRandomWeight(layer.model.rnd);
        if (layerType != LayerType.input) {
            if (layerType == LayerType.output) {
                nextNode = new Node[layer.nClasses];
            }
            linkPrevNodes();
            if (layerType == LayerType.output) {
                weightIn = setRandomWeights();
                biasWeight = UtilsSNN.getRandomWeight(layer.model.rnd);
            } else {
                weightIn = setConstantWeights(0.05f);
                biasWeight = 0.05f;
//                weightIn = setRandomWeights();
//                biasWeight = UtilsSNN.getRandomWeight(layer.model.rnd);
            }
            weightTempIn = new float[weightIn.length][weightIn[0].length][weightIn[0][0].length];

        }
        if (layerType == LayerType.input) {
            this.dataOut = layer.model.input[filter.filterIndex][row][col];
        }
    }

    public float forwardPass() {
        float ret = getWeightedSum();
        if (layer.layerType != LayerType.output) {
            ret = UtilsSNN.applyActivation(ret, filter.activationType);
        }
        this.dataOut = ret;
        if (layer.model.isDebug) {
            System.out.println(this);
        }
        return ret;
    }

    public float getWeightedSum() {
        float weightedSum = 0f;
        if (this.layerType == LayerType.input) {
            weightedSum = layer.model.input[filterIndex][row][col];
        } else if (layerType == LayerType.hidden) {
            int ps = filter.patchSize;
            for (int i = 0; i < ps; i++) {
                for (int j = 0; j < ps; j++) {
                    weightedSum += prevNodes[filterIndex][i][j].dataOut * weightIn[filterIndex][i][j];
                }

            }
            weightedSum += this.biasValue * this.biasWeight;

        } else if (filter.layer.layerType == LayerType.output) {
            int nFilter = layer.nFilter;
            int ps = nFilter * layer.prevLayer.nrows * layer.prevLayer.ncols;
            for (int i = 0; i < nFilter; i++) {
                for (int j = 0; j < prevNodes[i].length; j++) {
                    for (int k = 0; k < prevNodes[i][j].length; k++) {
                        weightedSum += prevNodes[i][j][k].dataOut * weightIn[i][j][k];
                    }
                }
            }
            weightedSum += this.biasValue * this.biasWeight;
        }
        return weightedSum;
    }

    public float getOutput() {
        float ret = getWeightedSum();
        if (layerType == LayerType.hidden) {
            ret = UtilsSNN.applyActivation(ret, filter.activationType);
        }
        return ret;
    }

    public void linkPrevNodes() {
        if (this.filter.layer.layerType == LayerType.hidden) {
            int p = this.filter.patchSize;
            int n = p * p;
            prevNodes = new Node[layer.nFilter][p][p];
            for (int j = 0; j < p; j++) {
                for (int k = 0; k < p; k++) {
                    Node node = filter.prevFilter.nodes[row * p + j][col * p + k];
                    prevNodes[filterIndex][j][k] = node;
                    prevNodes[filterIndex][j][k].nextNode[0] = this;
                }
            }
        } else {
            //output layer
            int nr = layer.prevLayer.nrows;
            int nc = layer.prevLayer.ncols;
            int nFilter = layer.prevLayer.nFilter;
            prevNodes = new Node[nFilter][nr][nc];
            for (int i = 0; i < nFilter; i++) {
                for (int j = 0; j < nr; j++) {
                    for (int k = 0; k < nc; k++) {
                        Node node = layer.prevLayer.filters[i].nodes[j][k];
                        prevNodes[i][j][k] = node;
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Node{" + "layer index=" + layer.layerIndex + ", row=" + row + ", col=" + col + ", weights=" + Arrays.deepToString(weightIn) + ", biasWeight="+biasWeight+", output=" + getOutput() + ", activationFunction=" + filter.activationType + '}';
    }

    private float[][][] setRandomWeights() {
        int m = layer.nFilter;
        int nr = prevNodes[0].length;
        int nc = prevNodes[0][0].length;
        float[][][] ret = new float[m][nr][nc];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < nr; j++) {
                for (int k = 0; k < nc; k++) {
                    ret[i][j][k] = UtilsSNN.getRandomWeight(layer.model.rnd);
                }
            }
        }
        return ret;
    }

    private float[][][] setConstantWeights(float val) {
        int m = layer.nFilter;
        int nr = prevNodes[0].length;
        int nc = prevNodes[0][0].length;
        float[][][] ret = new float[m][nr][nc];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < nr; j++) {
                for (int k = 0; k < nc; k++) {
                    ret[i][j][k] = val;
                }
            }
        }
        return ret;
    }

    public void updateWeights() {
        if (layerType == LayerType.output) {
            for (int i = 0; i < layer.nFilter; i++) {
                for (int j = 0; j < weightIn[0].length; j++) {
                    for (int k = 0; k < weightIn[0][0].length; k++) {
                        weightIn[i][j][k] = weightTempIn[i][j][k];
                    }
                }
            }
        } else if (layerType == LayerType.hidden) {
            for (int j = 0; j < weightIn[0].length; j++) {
                for (int k = 0; k < weightIn[0][0].length; k++) {
                    weightIn[filterIndex][j][k] = weightTempIn[filterIndex][j][k];
                }
            }
        }
        biasWeight = biasWeightTemp;
        if (this.onlyOutputLayerWeightTemp != null) {
            int nr = this.onlyOutputLayerWeightTemp.length;
            for (int i = 0; i < nr; i++) {
                this.onlyOutputLayerWeight[i] = this.onlyOutputLayerWeightTemp[i];
            }
        }
    }

    //see this video for further info https://www.youtube.com/watch?v=sIX_9n-1UbM&ab_channel=FirstPrinciplesofComputerVision
    public void backwardPass(float[] yActual, float[] yPredicted) {
        float[] dc = FactoryUtils.subtract(yPredicted, yActual);
        if (layerType == LayerType.output) {
            //gradient of the softmax along with categorical cross entropy is y(predicted)-y(actual)
            gradient = dc[row];
            for (int i = 0; i < layer.nFilter; i++) {
                for (int j = 0; j < prevNodes[0].length; j++) {
                    for (int k = 0; k < prevNodes[0][0].length; k++) {
                        partialDerivativeIn[i][j][k] = gradient * prevNodes[i][j][k].dataOut;
                        //update weights by W(next)=W-learning_rate*partial_derivative
                        weightTempIn[i][j][k] = weightIn[i][j][k] - layer.model.LEARNING_RATE * partialDerivativeIn[i][j][k];
                        prevNodes[i][j][k].onlyOutputLayerWeightTemp[row] = weightTempIn[i][j][k];
                    }
                }
            }
            biasWeightTemp = biasWeight - layer.model.LEARNING_RATE * dc[row];
        } else if (layer.layerIndex == layer.model.getFullyConnectedLayer().layerIndex) {
            gradient = 0;
            Node[] nn = this.nextNode;
            int nr = nn.length;
            for (int i = 0; i < nr; i++) {
                //gradient += nn[i].gradient * this.onlyOutputLayerWeight[i];
                gradient += nn[i].gradient * this.onlyOutputLayerWeightTemp[i];
            }
            float der = UtilsSNN.applyDerivativeFunction(layer.activationType, this.dataOut);
            gradient = gradient * der;
            for (int i = 0; i < prevNodes[filterIndex].length; i++) {
                for (int j = 0; j < prevNodes[filterIndex][0].length; j++) {
                    partialDerivativeIn[filterIndex][i][j]=gradient * prevNodes[filterIndex][i][j].dataOut;
                    weightTempIn[filterIndex][i][j] = weightIn[filterIndex][i][j] - layer.model.LEARNING_RATE * partialDerivativeIn[filterIndex][i][j];
                }
            }
            biasWeightTemp = biasWeight - layer.model.LEARNING_RATE * gradient;
        } else {
//            System.out.println(this);
//            if (layer.layerIndex==3 && row==0 && col==2) {
//                System.out.println("burası");
//            }
            gradient = 0;
            Node[] nn = this.nextNode;
            if (nn[0]==null) {
                return;
            }
            int nr = nn.length;
            gradient = nn[0].gradient * nn[0].weightTempIn[filterIndex][row%patch_size][col%patch_size];
            float der = UtilsSNN.applyDerivativeFunction(layer.activationType, this.dataOut);
            gradient = gradient * der;
            for (int i = 0; i < prevNodes[filterIndex].length; i++) {
                for (int j = 0; j < prevNodes[filterIndex][0].length; j++) {
                    partialDerivativeIn[filterIndex][i][j]=gradient * prevNodes[filterIndex][i][j].dataOut;
                    weightTempIn[filterIndex][i][j] = weightIn[filterIndex][i][j] - layer.model.LEARNING_RATE * partialDerivativeIn[filterIndex][i][j];
                }
            }
            biasWeightTemp = biasWeight - layer.model.LEARNING_RATE * gradient;
        }

    }

    public void addNoise(float val) {
        biasWeight+=(Math.random()>0.5)?-val:val;
        for (int f = 0; f < weightIn.length; f++) {
            for (int i = 0; i < weightIn[f].length; i++) {
                for (int j = 0; j < weightIn[f][0].length; j++) {
                    weightIn[f][i][j]+=(Math.random()>0.5)?-val:val;
                }
            }
        }
    }

}
