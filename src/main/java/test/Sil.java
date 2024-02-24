/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.io.File;
import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 *
 * @author cezerilab
 */
public class Sil {

    private static INDArray array;

    public static void main(String[] args) {
        CMatrix cm1 = CMatrix.getInstance()
                .range(0,500)
                .perlinNoise(0.01f)
                ;
        CMatrix cm2 = CMatrix.getInstance()
                .range(-250,250)
                .perlinNoise(0.01f)
                ;
        CMatrix cm3 = CMatrix.getInstance()
                .range(-500,0)
                .perlinNoise(0.01f)
                ;
        CMatrix cm = cm1.cat(1, cm2).cat(1, cm3).transpose().plot();
        
//        float[] ses=CMatrix.getInstance()
//                .range(0,500)
//                //.range(-250,250)
//                //.range(-500,0)
//                .perlinNoise(0.01f)
//                //.println()
//                .transpose()
//                .plot()
//                .toFloatArray1D()
//                
//                ;
        
        
        
//        CMatrix cmx = CMatrix.getInstance()
//                .range(0, 255)
//                .reshape(1,255)
//                .println()
////                .imread("images/bf.jpg")
////                //.println()
////                .printlnFull()
////                //.imshow()
//                ;
//        float[] f=cmx.toFloatArray1D();
//        for (int i = 0; i < 10; i++) {
//            f[i]+=3*i;
//            System.out.println("f["+i+"] = " + f[i]);
//        }
//        cmx.setArray(f).transpose().println();
 
//        int n=10000;
//        long t1=FactoryUtils.tic();
//        float[][] d=new float[n][n];
//        for (int i = 0; i < 1000; i++) {            
//          //cm.pow(2);
//          //CMatrix cm = CMatrix.getInstance().randn(n,n);
//          //d=FactoryMatrix.randMatrix(n, n);
//          //CMatrix.getInstance().rand(n, n);
//          array = Nd4j.rand(n, n);
//        }
//        t1=FactoryUtils.toc(t1);
//        FactoryUtils.makeDirectory("images/sil");
//        CMatrix cm = CMatrix.getInstance();
//        File[] files=FactoryUtils.getFileListInFolderForImages("images");
//        for (File file : files) {
//            cm.imread(file).rgb2gray().imresize(224, 224).imsave("images/sil/"+file.getName());
//        }
//        CMatrix cm = CMatrix.getInstance()
//                .imread("images/pullar.png")
//                .imshow()
//                ;
//        System.out.println("time:"+System.currentTimeMillis());
//        CMatrix cm = CMatrix.getInstance()
//                .range(-300, 300) 
//                .gaussmf(50, 0)
//                .transpose()
//                .plot()
//                
//                ;
//        CMatrix cm = CMatrix.getInstance()
////                .imread("images/pullar.png")
//                .imread("images/kaplan.jpg")
//                //.imhist()
//                .imshow()
//                ;
//        CMatrix cm = CMatrix.getInstance()
//                .range(0,720)
//                .println()
//                .perlinNoise(0.03f)
////                .toRadians()
////                .sin()
//                .plot()
//                ;
//        CMatrix cm1 = cm.addScalar(5).jitter(0.5f).cat(1, cm).plot();
//        double n=-1.57E6;
//        System.out.println("n = " + n);
//        CMatrix cm = CMatrix.getInstance()
//                .randn(500,20)
//                .getHistogramData(30)
//                .plot()
//                //.bar()
//                ;
//        double a=n*6;
//        System.out.println("a = " + a);
    }
}
