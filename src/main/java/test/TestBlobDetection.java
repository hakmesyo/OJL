/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import jazari.matrix.CMatrix;

/**
 *
 * @author BAP1
 */
public class TestBlobDetection {
    public static void main(String[] args) {
//        CMatrix cm = CMatrix.getInstance()
//                .imread("images/coins.png")
//                .imshow()
//                .histeq()
//                .imshow()
//                .filterGaussian(5)
//                .imshow()            
//                
//                ;
        CMatrix cm2 = CMatrix.getInstance()
                .make_blobs(500, 5, 3)
                //.scatter()
                //.println()
                ;
        cm2.clone().scatter(0,1);
        //cm2.clone().scatter(1,2);
        //cm2.clone().scatter(2,4);
        
//        CMatrix cm1 = CMatrix.getInstance().rand(500,2);
//        cm1.clone().scatter(0,1);
//        
        CMatrix cm3 = CMatrix.getInstance().randn(500,1);
        //CMatrix cm4=cm3.clone().multiplyScalar(2.5f).addScalar(7.3f);
        //CMatrix cm4=cm3.clone().addNoise(0.05f).tr();
        CMatrix cm4=cm3.clone().jitter(0.1f).tr();
        CMatrix cm5 = cm3.clone().cat(1, cm4.clone()).scatter();
        
//        CMatrix cm6 = CMatrix.getInstance()
//                .rand(1,500)
//                .shape()
//                .reshape(25,2,5,2)
//                .println()
//                
//                
//                ;
    }
}
