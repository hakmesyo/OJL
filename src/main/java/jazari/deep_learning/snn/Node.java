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
            for (int i = 0; i < filter.patchSize; i++) {
                for (int j = 0; j < filter.patchSize; j++) {
                    Node node = prevNodes[i][j];
                    weightedSum += node.getOutput() * node.weight;
                }
            }
            weightedSum += this.biasValue * this.biasWeight;
            ret = UtilsSNN.applyActivation(weightedSum, filter.activationType);
        } else if (filter.layer.layerType == LayerType.output) {
            float weightedSum = 0f;
            int nFilter = filter.layer.model.nFilters;
            for (int k = 0; k < nFilter; k++) {
                for (int i = 0; i < filter.patchSize; i++) {
                    for (int j = 0; j < filter.patchSize; j++) {
                        Node node = prevNodes4OutputLayer[k][i][j];
                        weightedSum += node.getOutput() * node.weight;
                    }
                }
                //no bias at output layer so skip adding bias term on weightedSum
            }
            ret = UtilsSNN.applyActivation(weightedSum, filter.activationType);

        }
        return ret;
    }
    
    public void linkPrevNodes(){
        if (this.filter.layer.layerType==LayerType.input) {
            prevNodes=null;
            prevNodes4OutputLayer=null;
            data=getOutput();
        }else if(this.filter.layer.layerType==LayerType.hidden){
            int n=this.filter.patchSize;
            prevNodes=new Node[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    Node node=filter.layer.prevLayer.filters[filter.filterIndex].nodes[row*n][col*n];
                    prevNodes[i][j]=node;
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Node{" + "row=" + row + ", col=" + col + ", weights=" + weight + ", output=" + getOutput() + ", activationFunction=" + filter.activationType + '}';
        //return "[weight=" + weight + ", output=" + getOutput() +']';
    }
    
    
}
