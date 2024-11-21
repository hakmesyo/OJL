/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.vit;

/**
 *
 * @author cezerilab
 */
/**
 * FeedForward class for Vision Transformer (ViT) implementation. This class
 * implements a simple feed-forward network with two linear layers and a GELU
 * activation function in between.
 */
public class FeedForward {

    private LinearLayer fc1;
    private LinearLayer fc2;
    private float[][] lastInput;
    private float[][] lastHiddenOutput;

    /**
     * Constructor for FeedForward.
     *
     * @param embeddingDim Dimension of the input and output embeddings
     * @param mlpDim Dimension of the hidden layer
     */
    public FeedForward(int embeddingDim, int mlpDim) {
        this.fc1 = new LinearLayer(embeddingDim, mlpDim);
        this.fc2 = new LinearLayer(mlpDim, embeddingDim);
    }

    /**
     * Forward pass of the FeedForward network.
     *
     * @param x Input tensor as a 2D array [batchSize][embeddingDim]
     * @return Output tensor after applying two linear transformations with GELU
     * in between
     */
    public float[][] forward(float[][] x) {
        this.lastInput = x;
        float[][] hidden = fc1.forward(x);
        hidden = applyGELU(hidden);
        this.lastHiddenOutput = hidden;
        return fc2.forward(hidden);
    }

    /**
     * Backward pass of the FeedForward network.
     *
     * @param gradOutput Gradient of the loss with respect to the output of this
     * layer
     * @return Gradient of the loss with respect to the input of this layer
     */
    public float[][] backward(float[][] gradOutput) {
        float[][] gradFc2 = fc2.backward(gradOutput);
        float[][] gradGELU = applyGELUGradient(gradFc2, lastHiddenOutput);
        return fc1.backward(gradGELU);
    }

    /**
     * Updates the parameters of both linear layers.
     *
     * @param learningRate The learning rate for parameter updates
     */
    public void updateParameters(float learningRate) {
        fc1.updateParameters(learningRate);
        fc2.updateParameters(learningRate);
    }

    /**
     * Applies the GELU (Gaussian Error Linear Unit) activation function.
     *
     * @param x Input tensor
     * @return Tensor after applying GELU
     */
    private float[][] applyGELU(float[][] x) {
        float[][] output = new float[x.length][x[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                output[i][j] = (float) (0.5 * x[i][j] * (1 + Math.tanh(Math.sqrt(2 / Math.PI) * (x[i][j] + 0.044715 * Math.pow(x[i][j], 3)))));
            }
        }
        return output;
    }

    /**
     * Computes the gradient of the GELU activation function.
     *
     * @param gradOutput Gradient of the loss with respect to the output of GELU
     * @param x Original input to GELU
     * @return Gradient of the loss with respect to the input of GELU
     */
    private float[][] applyGELUGradient(float[][] gradOutput, float[][] x) {
        float[][] gradient = new float[x.length][x[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                float cdf = (float) (0.5 * (1 + Math.tanh(Math.sqrt(2 / Math.PI) * (x[i][j] + 0.044715 * Math.pow(x[i][j], 3)))));
                float pdf = (float) (1 / Math.sqrt(2 * Math.PI) * Math.exp(-0.5 * x[i][j] * x[i][j]));
                // Use gradOutput[0][j] if gradOutput.length is 1, otherwise use gradOutput[i][j]
                float gradOutputValue = (gradOutput.length == 1) ? gradOutput[0][j] : gradOutput[i][j];
                gradient[i][j] = gradOutputValue * (cdf + (float) x[i][j] * pdf);

                //gradient[i][j] = gradOutput[i][j] * (cdf + x[i][j] * pdf);
            }
        }
        return gradient;
    }
}
