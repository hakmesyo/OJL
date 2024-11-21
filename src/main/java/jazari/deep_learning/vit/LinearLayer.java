package jazari.deep_learning.vit;

import java.util.Random;

/**
 * LinearLayer class for Vision Transformer (ViT) implementation.
 * This class implements a fully connected layer (also known as a dense layer)
 * that performs a linear transformation of the input.
 */
public class LinearLayer {
    private float[][] weights;
    private float[] bias;
    private int inFeatures;
    private int outFeatures;
    
    private float[][] lastInput;
    private float[][] lastOutput;

    /**
     * Constructor for LinearLayer.
     * 
     * @param inFeatures Number of input features
     * @param outFeatures Number of output features
     */
    public LinearLayer(int inFeatures, int outFeatures) {
        this.inFeatures = inFeatures;
        this.outFeatures = outFeatures;
        this.weights = new float[outFeatures][inFeatures];
        this.bias = new float[outFeatures];
        initializeParameters();
    }

    /**
     * Initializes the weights and biases of the layer.
     * Uses He initialization for weights and zeros for biases.
     */
    private void initializeParameters() {
        Random random = new Random();
        float stddev = (float) Math.sqrt(2.0 / inFeatures); // He initialization

        for (int i = 0; i < outFeatures; i++) {
            for (int j = 0; j < inFeatures; j++) {
                weights[i][j] = (float) (random.nextGaussian() * stddev);
            }
            bias[i] = 0; // Initialize bias to zero
        }
    }

    /**
     * Forward pass of the LinearLayer.
     * 
     * @param input Input tensor as a 2D array [batchSize][inFeatures]
     * @return Output tensor as a 2D array [batchSize][outFeatures]
     */
    public float[][] forward(float[][] input) {
        int batchSize = input.length;
        float[][] output = new float[batchSize][outFeatures];

        for (int i = 0; i < batchSize; i++) {
            for (int j = 0; j < outFeatures; j++) {
                output[i][j] = bias[j];
                for (int k = 0; k < inFeatures; k++) {
                    output[i][j] += input[i][k] * weights[j][k];
                }
            }
        }

        this.lastInput = input;
        this.lastOutput = output;
        return output;
    }

    /**
     * Backward pass of the LinearLayer.
     * 
     * @param gradOutput Gradient of the loss with respect to the output of this layer
     * @return Gradient of the loss with respect to the input of this layer
     */
    public float[][] backward(float[][] gradOutput) {
        int batchSize = gradOutput.length;
        float[][] gradInput = new float[batchSize][inFeatures];
        float[][] gradWeights = new float[outFeatures][inFeatures];
        float[] gradBias = new float[outFeatures];

        // Compute gradients w.r.t. weights and bias
        for (int i = 0; i < batchSize; i++) {
            for (int j = 0; j < outFeatures; j++) {
                gradBias[j] += gradOutput[i][j];
                for (int k = 0; k < inFeatures; k++) {
                    gradWeights[j][k] += gradOutput[i][j] * lastInput[i][k];
                }
            }
        }

        // Compute gradients w.r.t. input
        for (int i = 0; i < batchSize; i++) {
            for (int k = 0; k < inFeatures; k++) {
                gradInput[i][k] = 0;
                for (int j = 0; j < outFeatures; j++) {
                    gradInput[i][k] += gradOutput[i][j] * weights[j][k];
                }
            }
        }

        // Update weights and bias
        updateParameters(gradWeights, gradBias);

        return gradInput;
    }

    /**
     * Updates the parameters of the layer.
     * 
     * @param gradWeights Gradients of the weights
     * @param gradBias Gradients of the bias
     */
    private void updateParameters(float[][] gradWeights, float[] gradBias) {
        float learningRate = 0.01f; // This should be a configurable parameter
        float batchSize = lastInput.length;
        
        for (int i = 0; i < outFeatures; i++) {
            for (int j = 0; j < inFeatures; j++) {
                weights[i][j] -= learningRate * gradWeights[i][j] / batchSize;
            }
            bias[i] -= learningRate * gradBias[i] / batchSize;
        }
    }

    /**
     * Updates the parameters of the layer.
     * This method is kept for consistency with other layers, but actual update
     * is performed in the backward pass.
     * 
     * @param learningRate The learning rate for parameter updates
     */
    public void updateParameters(float learningRate) {
        // Actual update is done in the backward method
    }

    // Getter methods for testing and debugging
    public int getInFeatures() { return inFeatures; }
    public int getOutFeatures() { return outFeatures; }
    public float[][] getWeights() { return weights; }
    public float[] getBias() { return bias; }
    public float[][] getLastOutput() { return lastOutput; }

    /**
     * Prints the weights of the layer. Useful for debugging.
     */
    public void printWeights() {
        System.out.println("Weights:");
        for (float[] row : weights) {
            for (float weight : row) {
                System.out.printf("%8.4f ", weight);
            }
            System.out.println();
        }
    }

    /**
     * Prints the bias of the layer. Useful for debugging.
     */
    public void printBias() {
        System.out.println("Bias:");
        for (float b : bias) {
            System.out.printf("%8.4f ", b);
        }
        System.out.println();
    }
}