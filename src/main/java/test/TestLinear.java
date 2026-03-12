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
        
        CMatrix cmA = CMatrix.getInstance(A).println();
        CMatrix cmB = CMatrix.getInstance(B).println();
        CMatrix cmX = cmA.clone().inv().dot(cmB).println();
        CMatrix cmS = cmA.clone().solve(cmB).println();
        
//        float[][] f={
//            {1,-1},
//            {-1,1}
//        };
//        CMatrix cmF = CMatrix.getInstance(f).inv().println();
    }
}
