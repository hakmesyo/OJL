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
public class TestApplyFunction {
    public static void main(String[] args) {
        CMatrix cm_1 = CMatrix.getInstance()
                .range(0,80)
                .multiplyScalar(0.33f)               
                ;
        CMatrix cm_2 = CMatrix.getInstance()
                .range(80,180)
                .multiplyScalar(3.7f)
                .minusScalar(269.93f)
                ;
        CMatrix cm_3 = CMatrix.getInstance()
                .range(180,256)
                .multiplyScalar(0.56f) 
                .addScalar(292.43f)
                ;
        CMatrix cm_total = cm_1.cat(2, cm_2).cat(2, cm_3)
                .map(0, 255)
                .round()
                .transpose()
                .plot()
                ;
        
        CMatrix cm = CMatrix.getInstance().imread("images/alyuvar.jpg")
                .rgb2gray()
                .imshow()
                .applyFunction(cm_total)
                .imshow();
         
    }
}
