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
public class TestHisteq {
    public static void main(String[] args) {
        //nativeJava();
        withOJL();
    }

    private static void nativeJava() {
        CMatrix cm = CMatrix.getInstance().imread("images/alyuvar.jpg").rgb2gray().imshow();
        CMatrix hist=cm.clone().hist(256).println();        
        float[] pdf=hist.clone().divideScalar(hist.sumTotal()).tr().plot().toFloatArray1D();
        float[] cdf=new float[pdf.length];
        cdf[0]=pdf[0];
        for (int i = 1; i < cdf.length; i++) {
            cdf[i]=cdf[i-1]+pdf[i];
        }
        CMatrix n_cdf=CMatrix.getInstance(cdf).map(0, 255).tr().floor().plot();
        float[] d_cdf=n_cdf.toFloatArray1D();        
        float[][] d=cm.imshow().clone().toFloatArray2D();
        float[][] d2=cm.clone().toFloatArray2D();
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                d2[i][j]=d_cdf[(int)d[i][j]];
            }
        }
        CMatrix.getInstance(d2).imshow();        
    }

    private static void withOJL() {
        CMatrix cm = CMatrix.getInstance().imread("images/alyuvar.jpg").rgb2gray();//.imshow();
        cm.clone().applyFunction(cm.clone().cdf().map(0, 255)).imshow().println();
    }
}
