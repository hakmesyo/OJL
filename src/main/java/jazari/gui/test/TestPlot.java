/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui.test;

import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestPlot {
    public static void main(String[] args) {
        int min=-500;
        int max=500;
        CMatrix cm1 = CMatrix.getInstance()
                .range(min,max)
                .perlinNoise(0.01f)
                ;
        CMatrix cm2 = CMatrix.getInstance()
                .range(min,max)
                .perlinNoise(0.02f)
                ;
        cm1.cat(1, cm2).plot(CMatrix.getInstance().range(min,max).toFloatArray1D());
    }
}
