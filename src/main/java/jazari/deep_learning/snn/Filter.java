/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.snn;

import java.util.Arrays;
import jazari.matrix.CMatrix;

/**
 *
 * @author DELL LAB
 */
public class Filter {

    int filterIndex;
    Layer layer;
    LayerType layerType;
    int nrows;
    int ncols;
    Node[][] nodes;
    int patchSize;
    int stride;
    ActivationType activationType;
    Filter prevFilter;

    public Filter(int filterIndex, Layer layer, ActivationType activationType, int patchSize, int stride) {
        this.filterIndex = filterIndex;
        this.layer = layer;
        this.layerType = layer.layerType;
        if (layer.prevLayer != null) {
            this.prevFilter = layer.prevLayer.filters[filterIndex];
        }
        this.patchSize = patchSize;
        this.stride = stride;
        this.activationType = activationType;
        buildNodes();
        nrows = nodes.length;
        ncols = nodes[0].length;
    }

    private void buildNodes() {
        //for input layer
        if (layerType == LayerType.input) {
            int nr = layer.model.nrows;
            int nc = layer.model.ncols;
            nodes = new Node[nr][nc];
            for (int i = 0; i < nr; i++) {
                for (int j = 0; j < nc; j++) {
                    nodes[i][j] = new Node(this, i, j);
                }
            }
        } else if (layerType == LayerType.hidden) {
            //for hidden layer
            int nr = layer.prevLayer.filters[0].nodes.length;
            int nc = layer.prevLayer.filters[0].nodes[0].length;
            int dr = (nr / stride) - (patchSize / stride) + 1;
            int dc = (nc / stride) - (patchSize / stride) + 1;
            this.nodes = new Node[dr][dc];
            for (int i = 0; i < dr; i++) {
                for (int j = 0; j < dc; j++) {
                    nodes[i][j] = new Node(this, i, j);
                }
            }
        } else {
            //for output layer
            int nr = layer.nClasses;
            this.nodes = new Node[nr][1];
            for (int i = 0; i < nr; i++) {
                nodes[i][0] = new Node(this, i, 0);
            }
        }

    }

    public float[][] toArray2D() {
        int nr = nodes.length;
        int nc = nodes[0].length;
        float[][] ret = new float[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = nodes[i][j].dataOut;
            }
        }
        return ret;
    }

    public float[][] output() {
        return toArray2D();
    }

    public float[][] weights(int outputIndex) {
        int nr = this.prevFilter.nrows;
        int nc = this.prevFilter.ncols;
        float[][] ret = new float[nr][nc];
        if (layerType.equals(LayerType.hidden)) {
            for (int i = 0; i < nodes.length; i++) {
                for (int j = 0; j < nodes[0].length; j++) {
                    for (int k = 0; k < patchSize; k++) {
                        for (int m = 0; m < patchSize; m++) {
                            ret[i * patchSize + k][j * patchSize + m] = nodes[i][j].weightIn[filterIndex][k][m];
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < nr; i++) {
                for (int j = 0; j < nc; j++) {
                    ret[i][j]=nodes[outputIndex][0].weightIn[filterIndex][i][j];
                }
            }

        }
        return ret;
    }

    public float[] toArray1D() {
        int nr = nodes.length;
        float[] ret = new float[nr];
        for (int i = 0; i < nr; i++) {
            ret[i] = nodes[i][0].dataOut;
        }
        return ret;
    }

    public int getNodeCount() {
        return nodes.length * nodes[0].length;
    }

//    public void dump() {
//        System.out.println("Filter:" + filterIndex);
//
//        System.out.println("\tWeights:[" + nrows + "x" + ncols + "]");
//        for (int i = 0; i < nrows; i++) {
//            System.out.print("\t");
//            for (int j = 0; j < ncols; j++) {
//                System.out.print(nodes[i][j].weightIn + ",");
//            }
//            System.out.println("");
//        }
//
//        System.out.println("\tOutput:[" + nrows + "x" + ncols + "]");
//        for (int i = 0; i < nrows; i++) {
//            System.out.print("\t");
//            for (int j = 0; j < ncols; j++) {
//                System.out.print(nodes[i][j].getOutput() + ",");
//            }
//            System.out.println("");
//        }
//    }
//    public Filter copy() {
//        Filter ret=new Filter(filterIndex, layer, activationType, patchSize, stride);
//        for (int i = 0; i < nrows; i++) {
//            for (int j = 0; j < ncols; j++) {
//                ret.nodes[i][j]=nodes[i][j].copy();
//            }
//        }
//        return ret;
//    }
    public void forwardPass() {
        //float[][] p=new float[nrows][ncols];
        for (int i = 0; i < nrows; i++) {
            for (int j = 0; j < ncols; j++) {
                //System.out.println("önceki değer:"+nodes[i][j]);
                nodes[i][j].forwardPass();
                //System.out.println("sonraki değer:"+nodes[i][j]);
                //p[i][j]=nodes[i][j].outputData;
            }
        }
        //CMatrix cm = CMatrix.getInstance(p).map(0, 255).imshow().imresize(500, 500).imshow(this.toString());
        //int a=1;
    }

    @Override
    public String toString() {
        return "Filter{" + "filterIndex=" + filterIndex + ", layer=" + layer + ", layerType=" + layerType + '}';
    }

    public void updateWeights() {
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[0].length; j++) {
                nodes[i][j].updateWeights();
            }
        }
    }

    public void printWeights() {
        System.out.println("Layer " + layer.layerIndex + " Filter " + filterIndex);
        System.out.println(Arrays.deepToString(nodes));
    }

    public void backwardPass(float[] yActual, float[] yPredicted) {
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[0].length; j++) {
                nodes[i][j].backwardPass(yActual, yPredicted);
            }
        }
    }

    public void addNoise(float val) {
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes[0].length; j++) {
                nodes[i][j].addNoise(val);
            }
        }
    }

}
