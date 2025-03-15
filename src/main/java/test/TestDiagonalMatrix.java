/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.matrix.CMatrix;

/**
 *
 * @author Cezeri
 */
public class TestDiagonalMatrix {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .range2D(0, 100, 100)
                .diag()
                //.println()
                ;
        cm.clone().diag(cm.toFloatArray1D()).heatmap();
    }
}
