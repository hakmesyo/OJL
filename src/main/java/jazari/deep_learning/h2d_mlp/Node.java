/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.h2d_mlp;

import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class Node {
    float data;
    float weight;  
    float freezed_weight;  
    float[] weightsToOutputLayer;
    float[] freezed_weightsToOutputLayer;
    Node nextNode;
    //Node[][] prevNodes;
    Channel ch;
    int px;
    int py;
    boolean isBias = false;

    public Node(Channel ch, int px, int py) {
        this.ch = ch;
        this.px = px;
        this.py = py;
        if (px == -1 && py == -1) {
            isBias = true;
            data = 1;
        }
    }

    @Override
    public String toString() {
        return "pos:" + px + "," + py + "; data:" + data+" ; weight:"+FactoryUtils.formatDouble(weight);
    }

    public Node dump() {
        System.out.println(toString());
        return this;
    }

    public Node traceForward() {
        if (isBias) {
            System.out.println("bias node can't be traced");
            return this;
        }
        System.out.println("");
        Node temp = this;
        while (temp.nextNode != null) {
            temp = temp.dump().nextNode;
        }
        temp = temp.dump().nextNode;
        return this;
    }
    
    public boolean isOutput(){
        return nextNode==null;
    }

}
