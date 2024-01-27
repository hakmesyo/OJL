/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestMatrixMultiplication {
    public static void main(String[] args) {
        CMatrix cm1 = CMatrix.getInstance()
                .range(6)
                .reshape(3,2)
                .println()                
                ;
        CMatrix cm2 = cm1.clone().multiplyScalar(100).transpose().println();
             
        CMatrix cm3 = cm1.matmul(cm2).println();
    }
}
