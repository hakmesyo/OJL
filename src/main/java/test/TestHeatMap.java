/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import jazari.matrix.CMatrix;
import java.awt.Color;
import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryUtils;

/**
 *
 * @author elcezerilab
 */
public class TestHeatMap {

    public static void main(String[] args) {
//        CMatrix heatData = CMatrix.getInstance().rand(10, 10).heatmap();
//        CMatrix kernelData = CMatrix.getInstance(FactoryUtils.kernelGaussian2D(11, 5f));
        CMatrix heatData = CMatrix.getInstance().rand(10, 10);
        heatData.clone().heatmap();
        CMatrix barData = CMatrix.getInstance().rand(5, 3);
        barData.clone().bar();
        barData.clone().heatmap();
               
//
//        System.out.println("Matrisler oluşturuldu");

        //        CMatrix heatData = CMatrix.getInstance().rand(10, 10).heatmap();
        //// Display as heatmap
        ////        heatData.heatmap();
        //
        //        FactoryUtils.bekle(3000);
        //        
        //        float[][] gkernel = FactoryUtils.kernelGaussian2D(11, 5f);
        //        CMatrix cm_kernel = CMatrix
        //                .getInstance(gkernel)
        //                .println()
        //                .heatmap()
        //                //.heatmap(true, true)
        //                ;
        //        
        //        FactoryUtils.bekle(3000);
        //
        //// Create data for bar chart
        //        CMatrix barData = CMatrix.getInstance().rand(5, 3);
        //        String[] categories = {"Category 1", "Category 2", "Category 3", "Category 4", "Category 5"};
        //        barData.bar(categories);
        //        //        float[][] kernel = {
        //            {1, 0, 1},
        //            {0, 1, 0},
        //            {1, 0, 1}
        //        };
        //        CMatrix heatData = CMatrix.getInstance().rand(10, 10).heatmap();
        //        float[][] gkernel=FactoryUtils.kernelGaussian2D(11, 5f);
        //        CMatrix cm_kernel = CMatrix
        //                .getInstance(gkernel)
        //                .println()
        //                .heatmap()
        //                .heatmap(true,true)
        //                ;
        //        CMatrix cm2 = CMatrix.getInstance()
        //                .imread("images/pullar.png")
        //                .rgb2gray()
        //                .imhist()
        //                .transpose();
        //        
        //        cm2=    cm2.pow(2)
        //                .timesScalar(3)
        //                .minus(cm2.powerByScalar(4))
        //                .head()
        //                ;
        //        CMatrix cm = CMatrix.getInstance()
        //                .zeros(30,50)
        //                .perlinNoise(0.5)
        ////                .rand()
        ////                .map(0, 255)
        ////                .round()
        ////                .heatmap()
        ////                .head()
        ////                .heatmap()
        ////                .heatmap(Color.decode("0xFFFF00"))
        ////                .heatmap(Color.RED)
        //                .heatmap(Color.cyan)
        //                
        //                .imread("images/pullar.png")
        //                .imshow()
        //                .imhist()
        //                .getRedChannelColor()
        //                .rgb2gray()
        //                .imshow()
        //                .imhist()
        //                .prevFirst()
        //                .getGreenChannelColor()
        //                .rgb2gray()
        //                .imshow()
        //                .imhist()
        //                .prevFirst()
        //                .getBlueChannelColor()
        //                .rgb2gray()
        //                .imshow()
        //                .imhist()
        //                .prevFirst()
        //                
        ////                .rgb2gray()
        ////                .imshow()
        ////                .map(-100, 100)
        ////                .heatmap(true)
        //                
        //                
        ;

        double[][] d = {
            {0.77, -0.11, 0.11, 0.33, 0.55, -0.11, 0.33},
            {-0.11, 1, -0.11, 0.33, -0.11, 0.11, -0.11},
            {0.11, -0.11, 1.00, -0.33, 0.11, -0.11, 0.55},
            {0.33, 0.33, -0.33, 0.55, -0.33, 0.33, 0.33},
            {0.55, -0.11, 0.11, -0.33, 1.00, -0.11, 0.11},
            {-0.11, 0.11, -0.11, 0.33, -0.11, 1.00, -0.11},
            {0.33, -0.11, 0.55, 0.33, 0.11, -0.11, 0.77}
        };
        CMatrix cm = CMatrix.getInstance(d)
                //                        .scatter()
                //                        .plot()
                //                        .heatmap(Color.cyan,500,500,true,true)
                .imread("images/pullar.png") //                        .imshow()
                //                        .imhist()
                //                        .rgb2gray()
                //                        .hist(256)
                //                        .prev()
                //                        .rgb2gray()
                //                        .imshow()
                //                        .hist()
                //.plot()
                //.heatmap()
                ;

    }
}
