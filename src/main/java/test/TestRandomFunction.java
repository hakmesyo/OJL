/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.Random;
import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestRandomFunction {
    static int max=100000;
    public static void main(String[] args) {
        uniformRandom();
        normalRandom();
    }   

    private static void uniformRandom() {        
        float[] r=new float[max];
        for (int i = 0; i < max; i++) {
            r[i]=(int)(Math.random()*100);
        }
        float[] hist=FactoryUtils.hist(r, 10);                
        CMatrix cm = CMatrix.getInstance(hist).T().bar();
    }

    private static void normalRandom() {
        CMatrix cm = CMatrix.getInstance().randn(max, 1).map(-100, 0);
        float[] r=cm.toFloatArray1D();
        float[] hist=FactoryUtils.hist(r, 10);                
        CMatrix cm2 = CMatrix.getInstance(hist).T().bar();
    }
}