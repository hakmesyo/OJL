/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

/**
 *
 * @author cezerilab
 */
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

public class TestOptimizedConvolution {

    public static void main(String[] args) throws InterruptedException {
        // Örnek matris ve kernel
        int n=10;
        float[][] matrix = FactoryMatrix.randMatrix(n, n, 100,123);

        float[][] kernel = {
            {1, 0, 1},
            {0, 1, 0},
            {1, 0, 1}
        };

        // Convolution işlemini paralel olarak gerçekleştir
        float[][] convolvedMatrix = null;
        long t1=FactoryUtils.tic();
        for (int i = 0; i < 100; i++) {
//            convolvedMatrix = convolveParallel(matrix, kernel);
            convolvedMatrix = convolve(matrix, kernel);
            t1=FactoryUtils.toc(t1);
        }
//        
//        System.out.println("bitti");
//        // Sonucu yazdır
//        System.out.println("Orijinal Matris:");
//        printMatrix(matrix);
////
//        System.out.println("\nKernel Matrisi:");
//        printMatrix(kernel);
////
        System.out.println("\nConvolution Sonucu:");
        printMatrix(convolvedMatrix);
        
        CMatrix cm = CMatrix.getInstance()
                .randWithSeed(n,n,100,123)
                
                
//                .println()
//                .convolve(CMatrix.getInstance().setArray(kernel))
//                .println()
                ;
        CMatrix cm2 = CMatrix.getInstance().setArray(kernel);
        for (int i = 0; i < 2; i++) {            
            cm.tic().convolve(cm2).toc();
        }
        cm.println();
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
