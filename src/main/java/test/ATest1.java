/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.matrix.CMatrix;
import jazari.types.TMatrixOperator;

/**
 *
 * @author cezerilab
 */
public class ATest1 {

    public static void main(String[] args) {

CMatrix cm = CMatrix.getInstance()
                .range("0:5")
                .transpose()
                .replicateRow(5)
                .dump()
                ;

//        CMatrix cm = CMatrix.getInstance()
//                .imread("images/bf.png")
//                .imread("images/blob.jpg")
//                .rgb2gray()
//                //.cmd("0:2:end",":")
//                //.imshow()
//                .addNoisePartial(10,0.7f)
//                .imshow()
//                
//                
//                
//                ;
    }
}
