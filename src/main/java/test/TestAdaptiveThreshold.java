/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import jazari.matrix.CMatrix;
import java.awt.Color;

/**
 *
 * @author cezerilab
 */
public class TestAdaptiveThreshold {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .imread("images/pullar.png")
                //.imread("images/bird.jpg")
                //.drawLine(0, 100, 300, 100, 3, Color.yellow)
                .imshow("original")
//                .imthresholdColorRange(100,255,10,70,20,100)
//                .imshow()
//                .rgb2gray()
//                //.threshold(45)
//                //.threshold(45,110)
//                //.thresholdOtsu()
//                .binarizeOtsu()
//                .imshow("after threshold")
//                //.drawLine(0, 0, 300, 0, 3, Color.white)
//                
//                .imshow()
                ;
//        float[] ff=cm.getPixelColorARGB(11,11);
//        float f=cm.clone().rgb2gray().getPixelColorGray(11,11);
//        int a=3;
    }
}
