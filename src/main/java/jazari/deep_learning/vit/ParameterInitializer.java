/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.vit;

/**
 *
 * @author cezerilab
 */
import java.util.Random;

public class ParameterInitializer {
    private static final Random random = new Random();

    // Xavier/Glorot Uniform Initialization
    public static float[][] xavierUniform(int fanIn, int fanOut) {
        float limit = (float) Math.sqrt(6.0 / (fanIn + fanOut));
        return uniformDistribution(fanIn, fanOut, -limit, limit);
    }

    // Xavier/Glorot Normal Initialization
    public static float[][] xavierNormal(int fanIn, int fanOut) {
        float stdDev = (float) Math.sqrt(2.0 / (fanIn + fanOut));
        return normalDistribution(fanIn, fanOut, 0, stdDev);
    }

    // He Uniform Initialization
    public static float[][] heUniform(int fanIn, int fanOut) {
        float limit = (float) Math.sqrt(6.0 / fanIn);
        return uniformDistribution(fanIn, fanOut, -limit, limit);
    }

    // He Normal Initialization
    public static float[][] heNormal(int fanIn, int fanOut) {
        float stdDev = (float) Math.sqrt(2.0 / fanIn);
        return normalDistribution(fanIn, fanOut, 0, stdDev);
    }

    // LeCun Uniform Initialization
    public static float[][] lecunUniform(int fanIn, int fanOut) {
        float limit = (float) Math.sqrt(3.0 / fanIn);
        return uniformDistribution(fanIn, fanOut, -limit, limit);
    }

    // LeCun Normal Initialization
    public static float[][] lecunNormal(int fanIn, int fanOut) {
        float stdDev = (float) Math.sqrt(1.0 / fanIn);
        return normalDistribution(fanIn, fanOut, 0, stdDev);
    }

    // Orthogonal Initialization
    public static float[][] orthogonal(int rows, int cols) {
        float[][] matrix = normalDistribution(rows, cols, 0, 1);
        return makeOrthogonal(matrix);
    }

    // Uniform Distribution
    private static float[][] uniformDistribution(int rows, int cols, float min, float max) {
        float[][] matrix = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = min + random.nextFloat() * (max - min);
            }
        }
        return matrix;
    }

    // Normal Distribution
    private static float[][] normalDistribution(int rows, int cols, float mean, float stdDev) {
        float[][] matrix = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = (float) (random.nextGaussian() * stdDev + mean);
            }
        }
        return matrix;
    }

    // Helper method for Orthogonal Initialization
    private static float[][] makeOrthogonal(float[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        
        // Apply Gram-Schmidt process
        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < i; j++) {
                float dot = 0;
                for (int k = 0; k < rows; k++) {
                    dot += matrix[k][i] * matrix[k][j];
                }
                for (int k = 0; k < rows; k++) {
                    matrix[k][i] -= dot * matrix[k][j];
                }
            }
            
            // Normalize the column
            float norm = 0;
            for (int k = 0; k < rows; k++) {
                norm += matrix[k][i] * matrix[k][i];
            }
            norm = (float) Math.sqrt(norm);
            for (int k = 0; k < rows; k++) {
                matrix[k][i] /= norm;
            }
        }
        
        return matrix;
    }

    // Initialize bias
    public static float[] initializeBias(int size, float value) {
        float[] bias = new float[size];
        for (int i = 0; i < size; i++) {
            bias[i] = value;
        }
        return bias;
    }

    // Utility method to print a matrix (for debugging)
    public static void printMatrix(float[][] matrix) {
        for (float[] row : matrix) {
            for (float val : row) {
                System.out.printf("%8.4f ", val);
            }
            System.out.println();
        }
    }
}
