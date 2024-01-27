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
public class TestCMatrixView {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .range(30)                
                .println()
                .reshape(1,5,1,6,1)
                .println()
                .squeeze()
                .println()
                .unsqueeze(2)
                .println()
                ;
//        CMatrix cm2 = CMatrix.getInstance()
//                .reshape(cm,6,5)
//                .println()
//                .shape()
//                ;
    }
}
