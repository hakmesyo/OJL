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
//        CMatrix cm = CMatrix.getInstance()
//                //.rand(150, 150)
//                .imread("images/artificial.jpg")
//                .rgb2gray()
//                .addNoise(5.5f)
//                //.map(0, 255)
//                .imshow();
//        float[][] d = cm.toFloatArray2D();
//
//        for (int i = 0; i < 10; i++) {
//            d = meanFilter(d);
//            //CMatrix.getInstance(d).imshow();
//        }
//        CMatrix.getInstance(d).imshow();

        
    }

    private static void nativeJava() {
        CMatrix cm = CMatrix.getInstance().imread("images/alyuvar.jpg").rgb2gray().imshow();

        CMatrix hist = cm.clone().hist(256);
        float[] pdf = hist.clone().divideScalar(hist.sumTotal()).tr().plot().toFloatArray1D();
        float[] cdf = new float[pdf.length];
        cdf[0] = pdf[0];
        for (int i = 1; i < pdf.length; i++) {
            cdf[i] = cdf[i - 1] + pdf[i];
        }
        CMatrix n_cdf = CMatrix.getInstance(cdf).map(0, 255).tr().floor().plot();
        float[] d_cdf = n_cdf.toFloatArray1D();
        float[][] d = cm.clone().toFloatArray2D();
        float[][] d2 = cm.clone().toFloatArray2D();
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                d2[i][j] = d_cdf[(int) d[i][j]];
            }
        }
        CMatrix.getInstance(d2).imshow();
    }

    private static void withOJL() {
        CMatrix cm = CMatrix.getInstance().imread("images/alyuvar.jpg").rgb2gray().imshow();
        cm.clone().applyFunction(cm.clone().cdf().map(0, 255)).imshow();
        
        
    }

    private static float[][] meanFilter(float[][] d) {
        float[][] fm = new float[d.length][d[0].length];
        float[][] kernel = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
        for (int i = 1; i < d.length - 1; i++) {
            for (int j = 1; j < d[0].length - 1; j++) {
                fm[i][j] = applyFilter(crop(d, i, j, kernel.length), kernel);
            }
        }
        return fm;
    }

    private static float[][] crop(float[][] d, int i, int j, int size) {
        float[][] ret = new float[size][size];
        for (int k = 0; k < size; k++) {
            for (int l = 0; l < size; l++) {
                ret[k][l] = d[i - size / 2 + k][j - size / 2 + l];
            }
        }
        return ret;
    }

    private static float applyFilter(float[][] img, float[][] kernel) {
        float sum = 0;
        for (int i = 0; i < img.length; i++) {
            for (int j = 0; j < img[0].length; j++) {
                sum += img[i][j] * kernel[i][j];
            }
        }
        float avg = sum / (img.length * img.length);
        return avg;
    }
}
