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
        filters = new Filter[model.nFilters];
        if (layerIndex > 0) {
            prevLayer = model.layers.get(layerIndex - 1);
        }
        for (int i = 0; i < filters.length; i++) {
            filters[i] = new Filter(i, this, activationType, patchSize, stride);
        }
        nrows=filters[0].nrows;
        ncols=filters[0].ncols;
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
        if (layerIndex > 0) {
            prevLayer = model.layers.get(layerIndex - 1);
        }
        filters[0]=new Filter(0, this, activationType, patchSize, stride);
        nrows=filters[0].nrows;
        ncols=filters[0].ncols;
    }

    @Override
    public String toString() {
        return "Layer{" + "layerIndex=" + layerIndex + ", layerType=" + layerType + ", activationType=" + activationType + ", nfilters=" + filters.length + ", patchSize=" + patchSize + ", stride=" + stride + ", nrows=" + nrows +", ncols=" + ncols +'}';
    }

    public int getSize() {
        if (layerType == LayerType.input) {
            return 0;
        }else if (layerType == LayerType.hidden) {
            //return this.filters.length * this.filters[0].nodes.length * this.filters[0].nodes[0].length * (patchSize * patchSize+1);
            return this.filters.length * this.filters[0].getNodeCount() * (patchSize * patchSize+1);
        }else{
            return this.nClasses*this.model.nFilters*this.prevLayer.ncols*this.prevLayer.nrows;
        }
    }

    public int getOutputLayerSize() {
        return this.filters.length * this.filters[0].nodes.length * this.filters[0].nodes[0].length;
    }
    
    public void dump(){
        for (int i = 0; i < filters.length; i++) {
            filters[i].dump();
        }
    }

}
