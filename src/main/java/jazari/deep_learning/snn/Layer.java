/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.snn;

/**
 *
 * @author cezerilab
 */
public class Layer {

    //All layers is a 2D structure by default. It holds one or more filters and each filter contains 2D Nodes
    //MLP differs form SNN in that in MLP each hidden neuron (node) connects to all neurons from previous layer
    //but in SNN only receptivefield window or square patch is cosidered which establishes the main contribution of this approach
    int layerIndex;
    int nrows;
    int ncols;
    LayerType layerType;
    Filter[] filters;
    int nFilter;
    int patchSize;
    int stride;
    ActivationType activationType;
    SNN model;
    Layer prevLayer;
    int nClasses;

    //constructor for input layer or hidden layer
    public Layer(
            SNN model,
            int layerIndex,
            LayerType layerType,
            ActivationType activationType,
            int patchSize,
            int stride) {
        this.model = model;
        this.layerType = layerType;
        this.layerIndex = layerIndex;
        this.activationType = activationType;
        this.patchSize = patchSize;
        this.stride = stride;
        this.nClasses=model.NUMBER_OF_CLASSES;
        filters = new Filter[model.nFilters];
        nFilter = filters.length;
        if (layerIndex > 0) {
            prevLayer = model.layers.get(layerIndex - 1);
        }
        for (int i = 0; i < nFilter; i++) {
            filters[i] = new Filter(i, this, activationType, patchSize, stride);
        }
        nrows = filters[0].nrows;
        ncols = filters[0].ncols;
    }

    //constructor for output layer
    public Layer(
            SNN model,
            int layerIndex,
            LayerType layerType,
            ActivationType activationType,
            int nClasses
    ) {
        this.model = model;
        this.layerIndex = layerIndex;
        this.layerType = layerType;
        this.activationType = activationType;
        this.nClasses = nClasses;
        filters = new Filter[1];
        nFilter = 1;
        if (layerIndex > 0) {
            prevLayer = model.layers.get(layerIndex - 1);
        }
        filters[0] = new Filter(0, this, activationType, patchSize, stride);
        nrows = filters[0].nrows;
        ncols = filters[0].ncols;
    }
    
    public Layer getOutputLayer(){
        return this.model.getOutputLayer();
    }
    
    public Layer getInputLayer(){
        return this.model.getInputLayer();
    }

    public Layer getFullyConnectedLayer(){
        return this.model.getFullyConnectedLayer();
    }

    @Override
    public String toString() {
        return "Layer{" + "layerIndex=" + layerIndex + ", layerType=" + layerType + ", activationType=" + activationType + ", nfilters=" + filters.length + ", patchSize=" + patchSize + ", stride=" + stride + ", nrows=" + nrows + ", ncols=" + ncols + '}';
    }

//    public int getSize() {
//        if (layerType == LayerType.input) {
//            return nrows * ncols ;
//        } else if (layerType == LayerType.hidden) {
//            //return this.filters.length * this.filters[0].nodes.length * this.filters[0].nodes[0].length * (patchSize * patchSize+1);
//            return nrows * ncols ;
//        } else {
//            return this.nClasses;
//        }
//    }
    public int getTotalParams() {
        if (layerType == LayerType.input) {
            return 0;
        } else if (layerType == LayerType.hidden) {
            return this.filters.length * this.filters[0].nodes.length * this.filters[0].nodes[0].length * (patchSize * patchSize + 1);
        } else {
            return this.prevLayer.nFilter * this.nClasses * (this.prevLayer.nrows * this.prevLayer.ncols+1);
        }
    }

    public int getOutputLayerSize() {
        return this.filters.length * this.filters[0].nodes.length * this.filters[0].nodes[0].length;
    }

//    public void dump() {
//        for (int i = 0; i < filters.length; i++) {
//            filters[i].dump();
//        }
//    }

    public void forwardPass() {
        for (Filter filter : filters) {
            filter.forwardPass();
        }
    }

    public float[] getOutputPredictedData() {
        float[] ret = new float[nClasses];
        for (int i = 0; i < this.nClasses; i++) {
            ret[i] = this.filters[0].nodes[i][0].dataOut;
        }
        return ret;
    }

    public void setOutputLayerSoftMaxValue(float[] predicted) {
        for (int i = 0; i < this.nClasses; i++) {
            filters[0].nodes[i][0].dataOut = predicted[i];
        }
    }

//    public Layer copy() {
//        Layer ret = null;
//        if (layerType == LayerType.input || layerType == LayerType.hidden) {
//            ret = new Layer(model, layerIndex, layerType, activationType, patchSize, stride);
//        } else {
//            ret = new Layer(model, layerIndex, layerType, activationType, nClasses);
//        }
//        for (int i = 0; i < ret.nFilter; i++) {
//            ret.filters[i] = filters[i].copy();
//        }
//        return ret;
//    }

    void feedInputData(float[][][] input) {
        for (int k = 0; k < nFilter; k++) {
            Filter filter=filters[k];
            Node[][] node=filter.nodes;
            for (int i = 0; i < filter.nrows; i++) {
                for (int j = 0; j < filter.ncols; j++) {
                    node[i][j].dataOut=input[k][i][j];
                }
            }
        }
    }

    public void updateWeights() {
        if (layerType!=LayerType.input) {
            for (Filter filter : filters) {
                filter.updateWeights();
            }
        }
    }

    public void printWeights() {
        if (layerType!=LayerType.input) {
            for (Filter filter : filters) {
                filter.printWeights();
            }
        }
    }

    public void backwardPass(float[] yActual, float[] yPredicted) {
        if (layerType!=LayerType.input) {
            for (Filter filter : filters) {
                filter.backwardPass(yActual,yPredicted);
            }
        }
    }

    public void addNoise(float val) {
        if (layerType!=LayerType.input) {
            for (Filter filter : filters) {
                filter.addNoise(val);
            }
        }
    }

}
