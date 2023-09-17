/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.sdnn;

/**
 *
 * @author cezerilab
 */
public class DataSetSDNN {
    public float[][][][] X;
    public float[][] y;
    public int size=0;

    public DataSetSDNN(float[][][][] X, float[][] y) {
        this.X = X;
        this.y = y;
        this.size=X.length;
    }
    
    
}
