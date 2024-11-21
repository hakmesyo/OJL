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
 * TransformerEncoder class for Vision Transformer (ViT) implementation.
 * This class implements a single Transformer encoder layer, which consists of
 * multi-head self-attention followed by a feed-forward network, with layer
 * normalization and residual connections.
 */
public class TransformerEncoder {
    private MultiHeadAttention multiHeadAttention;
    private FeedForward feedForward;
    private LayerNorm layerNorm1;
    private LayerNorm layerNorm2;
    private float[][] lastInput;
    private float[][] lastAttentionOutput;
    private float[][] lastLayerNorm1Output;
    private float[][] lastLayerNorm2Output;

    /**
     * Constructor for TransformerEncoder.
     * 
     * @param embeddingDim Dimension of the input embeddings
     * @param numHeads Number of attention heads
     * @param mlpDim Dimension of the feed-forward network
     */
    public TransformerEncoder(int embeddingDim, int numHeads, int mlpDim) {
        this.multiHeadAttention = new MultiHeadAttention(embeddingDim, numHeads);
        this.feedForward = new FeedForward(embeddingDim, mlpDim);
        this.layerNorm1 = new LayerNorm(embeddingDim, 1e-6f);
        this.layerNorm2 = new LayerNorm(embeddingDim, 1e-6f);
    }

    /**
     * Forward pass of the TransformerEncoder.
     * 
     * @param x Input tensor as a 2D array [seqLen][embeddingDim]
     * @return Output tensor after applying self-attention and feed-forward layers
     */
    public float[][] forward(float[][] x) {
        this.lastInput = x;

        // Layer Norm 1
        float[][] normX1 = layerNorm1.forward(x);
        this.lastLayerNorm1Output = normX1;

        // Multi-head Attention
        float[][] attnOutput = multiHeadAttention.forward(normX1);
        this.lastAttentionOutput = attnOutput;

        // Residual connection
        float[][] residual1 = addResidual(x, attnOutput);

        // Layer Norm 2
        float[][] normX2 = layerNorm2.forward(residual1);
        this.lastLayerNorm2Output = normX2;

        // Feed Forward
        float[][] ffOutput = feedForward.forward(normX2);

        // Residual connection
        return addResidual(residual1, ffOutput);
    }

    /**
     * Backward pass of the TransformerEncoder.
     * 
     * @param gradOutput Gradient of the loss with respect to the output of this layer
     * @return Gradient of the loss with respect to the input of this layer
     */
    public float[][] backward(float[][] gradOutput) {
        // Gradient through second residual connection
        float[][] gradResidual2 = gradOutput;
        float[][] gradFeedForward = feedForward.backward(gradResidual2);

        // Gradient through Layer Norm 2
        float[][] gradLayerNorm2 = layerNorm2.backward(gradFeedForward);

        // Gradient through first residual connection
        float[][] gradResidual1 = addResidual(gradLayerNorm2, gradResidual2);

        // Gradient through Multi-head Attention
        float[][] gradAttention = multiHeadAttention.backward(gradResidual1);

        // Gradient through Layer Norm 1
        float[][] gradLayerNorm1 = layerNorm1.backward(gradAttention);

        // Final gradient
        return addResidual(gradLayerNorm1, gradResidual1);
    }

    /**
     * Updates the parameters of all components in this encoder layer.
     * 
     * @param learningRate The learning rate for parameter updates
     */
    public void updateParameters(float learningRate) {
        multiHeadAttention.updateParameters(learningRate);
        feedForward.updateParameters(learningRate);
        layerNorm1.updateParameters(learningRate);
        layerNorm2.updateParameters(learningRate);
    }

    /**
     * Adds residual connection.
     * 
     * @param input Original input
     * @param residual Residual to be added
     * @return Sum of input and residual
     */
    private float[][] addResidual(float[][] input, float[][] residual) {
        float[][] output = new float[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                float residualVal = (residual.length == 1) ? residual[0][j] : residual[i][j];
                output[i][j] = input[i][j] + residualVal;
            }
        }
        return output;
    }
}
