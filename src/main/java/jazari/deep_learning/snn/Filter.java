/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.snn;

/**
 *
 * @author DELL LAB
 */
public class Filter {

    int filterIndex;
    Layer layer;
    int nrows;
    int ncols;
    Node[][] nodes;
    int patchSize;
    int stride;
    ActivationType activationType;

    public Filter(int filterIndex, Layer layer, ActivationType activationType, int patchSize, int stride) {
        this.filterIndex = filterIndex;
        this.layer = layer;
        this.patchSize = patchSize;
        this.stride = stride;
        this.activationType = activationType;
        buildNodes();
        nrows = nodes.length;
        ncols = nodes[0].length;
    }

    private void buildNodes() {
        //for input layer
        if (layer.layerType == LayerType.input) {
            int nr = layer.model.nrows;
            int nc = layer.model.ncols;
            nodes = new Node[nr][nc];
            for (int i = 0; i < nr; i++) {
                for (int j = 0; j < nc; j++) {
                    nodes[i][j] = new Node(this, i, j);
                }
            }
            return;
        } else if (layer.layerType == LayerType.hidden) {
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
                ret[i][j] = nodes[i][j].data;
            }
        }
        return ret;
    }

    public int getNodeCount() {
        return nodes.length * nodes[0].length;
    }

    public void dump() {
        System.out.println("Filter:"+filterIndex);
        
        System.out.println("\tWeights:["+nrows+"x"+ncols+"]");
        for (int i = 0; i < nrows; i++) {
            System.out.print("\t");
            for (int j = 0; j < ncols; j++) {
                System.out.print(nodes[i][j].weight+",");
            }
            System.out.println("");
        }
        
        System.out.println("\tOutput:["+nrows+"x"+ncols+"]");
        for (int i = 0; i < nrows; i++) {
            System.out.print("\t");
            for (int j = 0; j < ncols; j++) {
                System.out.print(nodes[i][j].getOutput()+",");
            }
            System.out.println("");
        }
    }

}
