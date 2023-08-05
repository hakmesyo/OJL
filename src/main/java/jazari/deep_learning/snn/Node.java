/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package jazari.deep_learning.snn;

/**
 *
 * @author cezerilab
 */
public class Node {

    int row;
    int col;
    float biasValue = 1.0f;
    float biasWeight;
    float weight;
    float[] weightOutputLayer;
    Node[][] prevNodes;
    Node[][][] prevNodes4OutputLayer;  //since each node of outputlayer links to the previous hidden layers node for filter each filter
    Filter filter;
    float data;

    public Node(Filter filter, int row, int col) {
        this.filter = filter;
        this.row = row;
        this.col = col;
        biasWeight = UtilsSNN.getRandomWeight(filter.layer.model.rnd);
        weight = UtilsSNN.getRandomWeight(filter.layer.model.rnd);
        linkPrevNodes();
    }

    public float getOutput() {
        float ret = 0;
        if (filter.layer.layerType == LayerType.input) {
            ret = filter.layer.model.input[filter.filterIndex][row][col];
        } else if (filter.layer.layerType == LayerType.hidden) {
            float weightedSum = 0f;
            int k=0;
            for (int i = 0; i < filter.patchSize; i++) {
                for (int j = 0; j < filter.patchSize; j++) {
                    Node node = prevNodes[i][j];
                    //System.out.println((k++)+".inner loop node of hidden");
                    weightedSum += node.getOutput() * node.weight;
                }
            }
            weightedSum += this.biasValue * this.biasWeight;
            ret = UtilsSNN.applyActivation(weightedSum, filter.activationType);
        } else if (filter.layer.layerType == LayerType.output) {
            float weightedSum = 0f;
            int nFilter = filter.layer.model.nFilters;
            int z=0;
            for (int k = 0; k < nFilter; k++) {
                for (int i = 0; i < filter.layer.prevLayer.nrows; i++) {
                    for (int j = 0; j < filter.layer.prevLayer.ncols; j++) {
                        Node node = prevNodes4OutputLayer[k][i][j];
                        node.data = node.getOutput();
                        weightedSum += node.data * node.weightOutputLayer[row];
                        //System.out.println((z++)+". inner output layer");
                    }
                }
                weightedSum += this.biasValue * this.biasWeight;
            }
            //ret = UtilsSNN.applyActivation(weightedSum, ActivationType.identity);
            ret = weightedSum;

        }
        return ret;
    }

    public void linkPrevNodes() {
        if (this.filter.layer.layerType == LayerType.input) {
            prevNodes = null;
            prevNodes4OutputLayer = null;
            data = filter.layer.model.input[filter.filterIndex][row][col];
        } else if (this.filter.layer.layerType == LayerType.hidden) {
            prevNodes4OutputLayer = null;
            int n = this.filter.patchSize;
            prevNodes = new Node[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    Node node = filter.layer.prevLayer.filters[filter.filterIndex].nodes[row * n + i][col * n + j];
                    prevNodes[i][j] = node;
                }
            }
        } else {
            //output layer
            int nr = this.filter.layer.prevLayer.nrows;
            int nc = this.filter.layer.prevLayer.ncols;
            int nFilter = this.filter.layer.prevLayer.filters.length;
            prevNodes4OutputLayer = new Node[nFilter][nr][nc];
            for (int i = 0; i < nFilter; i++) {
                for (int j = 0; j < nr; j++) {
                    for (int k = 0; k < nc; k++) {
                        Node node = filter.layer.prevLayer.filters[i].nodes[j][k];
                        if (node.weightOutputLayer == null) {
                            node.weightOutputLayer = new float[filter.layer.nClasses];
                        }
                        prevNodes4OutputLayer[i][j][k] = node;
                        node.weightOutputLayer[row] = UtilsSNN.getRandomWeight(filter.layer.model.rnd);
//                        for (int l = 0; l < this.filter.layer.getSize(); l++) {
//                            node.weightOutputLayer[l]=UtilsSNN.getRandomWeight(filter.layer.model.rnd);
//                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Node{" + "row=" + row + ", col=" + col + ", weights=" + weight + ", output=" + getOutput() + ", activationFunction=" + filter.activationType + '}';
        //return "[weight=" + weight + ", output=" + getOutput() +']';
    }

    public Node copy() {
        Node ret = new Node(filter, row, col);
        ret.biasValue = biasValue;
        ret.biasWeight = biasWeight;
        ret.data = data;
        ret.weight = weight;
        if (weightOutputLayer != null) {
            ret.weightOutputLayer = new float[weightOutputLayer.length];
            for (int i = 0; i < weightOutputLayer.length; i++) {
                ret.weightOutputLayer[i] = weightOutputLayer[i];
            }
        }
        return ret;
    }

}
