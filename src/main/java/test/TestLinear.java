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
public class TestLinear {
    public static void main(String[] args) {
        float[][] A={
            {1,1,1},
            {0,2,5},
            {2,5,-1}
        };
        float[][] B={
            {6},
            {-4},
            {27}
        };
        
        CMatrix cmA = CMatrix.getInstance(A);
        CMatrix cmB = CMatrix.getInstance(B);
        CMatrix cmX = cmA.inv().dot(cmB).println();
        
        float[][] f={
            {1,-1},
            {-1,1}
        };
        CMatrix cmF = CMatrix.getInstance(f).inv().println();
    }
}
