/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import jazari.matrix.CMatrix;
import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryUtils;
import static test.TestOptimizedConvolution.convolve;
import static test.TestOptimizedConvolution.printMatrix;

/**
 *
 * @author BAP1
 */
public class TestConvolution {

    public static void main(String[] args) {
        float[][] kernel = {
            {-1, -1, -1},
            {0, 0, 0},
            {1, 1, 1}
        };
        CMatrix cm_kernel = CMatrix.getInstance(kernel);

        CMatrix cm = CMatrix.getInstance()
                .imread("images/chessboard.PNG")
                .imresize(0.5f)
                .rgb2gray()
                .imshow()
                ;
        
        CMatrix cm1 = cm.clone().conv(cm_kernel).round().map(0, 255).binarizeImage().imshow();//.map(0, 255);//.println().imshow();                
        CMatrix cm2 = cm.clone().conv(cm_kernel.clone().T()).round().map(0, 255).binarizeImage().imshow();//.map(0, 255);//.println().imshow();                
        cm1.add(cm2).map(0, 255).println().imshow();//.add(cm2).add(cm2_1).imshow();
        
//
//        double[][] d_X = {
//            {-1, -1, -1, -1, -1, -1, -1, -1, -1},
//            {-1, 1, -1, -1, -1, -1, -1, 1, -1},
//            {-1, -1, 1, -1, -1, -1, 1, -1, -1},
//            {-1, -1, -1, 1, -1, 1, -1, -1, -1},
//            {-1, -1, -1, -1, 1, -1, -1, -1, -1},
//            {-1, -1, -1, 1, -1, 1, -1, -1, -1},
//            {-1, -1, 1, -1, -1, -1, 1, -1, -1},
//            {-1, 1, -1, -1, -1, -1, -1, 1, -1},
//            {-1, -1, -1, -1, -1, -1, -1, -1, -1}
//        };
//        double[][] d_M = {
//            {-1, -1, -1, -1, -1, -1, -1, -1, -1},
//            {-1, 1, -1, -1, -1, -1, -1, 1, -1},
//            {-1, 1, 1, -1, -1, -1, 1, 1, -1},
//            {-1, 1, -1, 1, -1, 1, -1, 1, -1},
//            {-1, 1, -1, -1, 1, -1, -1, 1, -1},
//            {-1, 1, -1, -1, -1, -1, -1, 1, -1},
//            {-1, 1, -1, -1, -1, -1, -1, 1, -1},
//            {-1, 1, -1, -1, -1, -1, -1, 1, -1},
//            {-1, -1, -1, -1, -1, -1, -1, -1, -1}
//        };
//        double[][] k_diag_1 = {
//            {1, -1, -1},
//            {-1, 1, -1},
//            {-1, -1, 1}
//        };
//        double[][] k_diag_2 = {
//            {-1, -1, 1},
//            {-1, 1, -1},
//            {1, -1, -1}
//        };
//        int w = 400;
//        CMatrix ck_dg1 = CMatrix.getInstance(k_diag_1).heatmap(Color.cyan, 200, 200, true, true);;
//        CMatrix ck_dg2 = CMatrix.getInstance(k_diag_2).heatmap(Color.cyan, 200, 200, true, true);;
//        CMatrix cm = CMatrix.getInstance(d_X)
//                .heatmap(Color.gray, w, w, true, true);
//        CMatrix cm_dg1 = cm.convolve(ck_dg1)
//                .heatmap(Color.cyan, w, w, true, true);
//        CMatrix cm_dg2 = cm.convolve(ck_dg2)
//                .heatmap(Color.cyan, w, w, true, true);
//
//        // Örnek matris ve kernel
//        int n = 10;
//        float[][] matrix = FactoryMatrix.randMatrix(n, n, 100, 123);
//
//        float[][] kernel = {
//            {1, 0, 1},
//            {0, 1, 0},
//            {1, 0, 1}
//        };
//        CMatrix cm_kernel = CMatrix.getInstance(kernel).heatmap(true);
//
//        // Convolution işlemini paralel olarak gerçekleştir
//        float[][] convolvedMatrix = null;
//        long t1 = FactoryUtils.tic();
//        for (int i = 0; i < 100; i++) {
////            convolvedMatrix = convolveParallel(matrix, kernel);
//            convolvedMatrix = convolve(matrix, kernel);
//            t1 = FactoryUtils.toc(t1);
//        }
////        
////        System.out.println("bitti");
////        // Sonucu yazdır
////        System.out.println("Orijinal Matris:");
////        printMatrix(matrix);
//////
////        System.out.println("\nKernel Matrisi:");
////        printMatrix(kernel);
//////
//        System.out.println("\nConvolution Sonucu:");
//        printMatrix(convolvedMatrix);
//
////        CMatrix cm = CMatrix.getInstance()
////                .randWithSeed(n,n,100,123)
////                
////                
//////                .println()
//////                .convolve(CMatrix.getInstance().setArray(kernel))
//////                .println()
////                ;
////        CMatrix cm2 = CMatrix.getInstance().setArray(kernel);
////        for (int i = 0; i < 2; i++) {            
////            cm.tic().convolve(cm2).toc();
////        }
////        cm.println();
    }

    // Convolution işlemini gerçekleştiren fonksiyon
    public static float[][] convolve(float[][] matrix, float[][] kernel) {
        int matrixRows = matrix.length;
        int matrixCols = matrix[0].length;
        int kernelSize = kernel.length;

        // Sonuç matrisini oluştur
        float[][] convolvedMatrix = new float[matrixRows - kernelSize + 1][matrixCols - kernelSize + 1];

        // Her piksel için convolution işlemini gerçekleştir
        for (int i = 0; i <= matrixRows - kernelSize; i++) {
            for (int j = 0; j <= matrixCols - kernelSize; j++) {
                convolvedMatrix[i][j] = calculateConvolution(matrix, kernel, i, j);
            }
        }

        return convolvedMatrix;
    }

    // Paralel convolution işlemini gerçekleştiren fonksiyon
    public static float[][] convolveParallel(float[][] matrix, float[][] kernel) throws InterruptedException {
        int matrixRows = matrix.length;
        int matrixCols = matrix[0].length;
        int kernelSize = kernel.length;

        // Sonuç matrisini oluştur
        float[][] convolvedMatrix = new float[matrixRows - kernelSize + 1][matrixCols - kernelSize + 1];

        // Kullanılabilir işlemci sayısı kadar thread oluştur
        int numProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numProcessors);

        // Her satırı ayrı bir thread'de işleyen Runnable'lar oluştur
        for (int i = 0; i <= matrixRows - kernelSize; i++) {
            int row = i;
            executor.execute(() -> {
                for (int j = 0; j <= matrixCols - kernelSize; j++) {
                    convolvedMatrix[row][j] = calculateConvolution(matrix, kernel, row, j);
                }
            });
        }

        // Tüm thread'lerin bitmesini bekle
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        return convolvedMatrix;
    }

    // Belirli bir piksel için convolution işlemini hesaplayan fonksiyon
    private static float calculateConvolution(float[][] matrix, float[][] kernel, int row, int col) {
        float sum = 0;
        for (int i = 0; i < kernel.length; i++) {
            for (int j = 0; j < kernel[0].length; j++) {
                sum += matrix[row + i][col + j] * kernel[i][j];
            }
        }
        return sum;
    }

    // Matrisi yazdıran yardımcı fonksiyon
    public static void printMatrix(float[][] matrix) {
        for (float[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}
