/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import jazari.types.TBoundingBox;

/**
 *
 * @author dell_lab
 */
public class TestFindBndBoxOnMaskImage {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance().imread("images/mask_1.png").rgb2gray();
        
        int[][] d=cm.toIntArray2D();
        TBoundingBox bndbox=null;
        long t1=FactoryUtils.tic();
        for (int i = 0; i < 1000; i++) {
            bndbox=FactoryUtils.findBoundingBox(d);
            t1=FactoryUtils.toc(t1);
        }
        //t1=FactoryUtils.toc(t1);
        System.out.println("bndbox = " + bndbox);
    }
}
