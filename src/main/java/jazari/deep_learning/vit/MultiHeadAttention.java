package jazari.deep_learning.vit;

import java.util.Random;

/**
 * MultiHeadAttention class for Vision Transformer (ViT) implementation. This
 * class implements the multi-head attention mechanism, which allows the model
 * to jointly attend to information from different representation subspaces.
 */
public class MultiHeadAttention {

    private int embeddingDim;
    private int numHeads;
    private int headDim;
    private LinearLayer queryLayer;
    private LinearLayer keyLayer;
    private LinearLayer valueLayer;
    private LinearLayer outputLayer;
    private float[][] lastInput;
    private float[][][] lastQueries;
    private float[][][] lastKeys;
    private float[][][] lastValues;
    private float[][][] lastAttentionWeights;

    /**
     * Constructor for MultiHeadAttention.
     *
     * @param embeddingDim The dimension of the input embeddings
     * @param numHeads The number of attention heads
     */
    public MultiHeadAttention(int embeddingDim, int numHeads) {
        this.embeddingDim = embeddingDim;
        this.numHeads = numHeads;
        this.headDim = embeddingDim / numHeads;

        if (embeddingDim % numHeads != 0) {
            throw new IllegalArgumentException("Embedding dimension must be divisible by number of heads");
        }

        this.queryLayer = new LinearLayer(embeddingDim, embeddingDim);
        this.keyLayer = new LinearLayer(embeddingDim, embeddingDim);
        this.valueLayer = new LinearLayer(embeddingDim, embeddingDim);
        this.outputLayer = new LinearLayer(embeddingDim, embeddingDim);
    }

    /**
     * Forward pass of the MultiHeadAttention layer.
     *
     * @param x Input tensor as a 2D array [seqLen][embeddingDim]
     * @return Output tensor after applying multi-head attention
     */
    public float[][] forward(float[][] x) {
        int seqLen = x.length;
        this.lastInput = x;

        float[][] queries = queryLayer.forward(x);
        float[][] keys = keyLayer.forward(x);
        float[][] values = valueLayer.forward(x);

        float[][][] splitQueries = splitHeads(queries);
        float[][][] splitKeys = splitHeads(keys);
        float[][][] splitValues = splitHeads(values);

        this.lastQueries = splitQueries;
        this.lastKeys = splitKeys;
        this.lastValues = splitValues;

        float[][][] attentionOutput = scaledDotProductAttention(splitQueries, splitKeys, splitValues);
        float[][] concatenated = concatenateHeads(attentionOutput);
        return outputLayer.forward(concatenated);
    }

    /**
     * Splits the input tensor into multiple heads.
     *
     * @param x Input tensor
     * @return Split tensor with shape [seqLen][numHeads][headDim]
     */
    private float[][][] splitHeads(float[][] x) {
        int seqLen = x.length;
        float[][][] split = new float[seqLen][numHeads][headDim];

        for (int i = 0; i < seqLen; i++) {
            for (int h = 0; h < numHeads; h++) {
                System.arraycopy(x[i], h * headDim, split[i][h], 0, headDim);
            }
        }
        return split;
    }

    /**
     * Concatenates the multi-head outputs back into a single tensor.
     *
     * @param x Input tensor with shape [seqLen][numHeads][headDim]
     * @return Concatenated tensor with shape [seqLen][embeddingDim]
     */
    private float[][] concatenateHeads(float[][][] x) {
        int seqLen = x.length;
        float[][] concatenated = new float[seqLen][embeddingDim];

        for (int i = 0; i < seqLen; i++) {
            for (int h = 0; h < numHeads; h++) {
                System.arraycopy(x[i][h], 0, concatenated[i], h * headDim, headDim);
            }
        }
        return concatenated;
    }

    /**
     * Computes scaled dot-product attention.
     *
     * @param queries Query tensor
     * @param keys Key tensor
     * @param values Value tensor
     * @return Attention output
     */
    private float[][][] scaledDotProductAttention(float[][][] queries, float[][][] keys, float[][][] values) {
        int seqLen = queries.length;
        float[][][] output = new float[seqLen][numHeads][headDim];
        float scaleFactor = (float) Math.sqrt(headDim);

        // Compute attention scores
        float[][][] attentionScores = new float[seqLen][numHeads][seqLen];
        for (int i = 0; i < seqLen; i++) {
            for (int h = 0; h < numHeads; h++) {
                for (int j = 0; j < seqLen; j++) {
                    float sum = 0;
                    for (int k = 0; k < headDim; k++) {
                        sum += queries[i][h][k] * keys[j][h][k];
                    }
                    attentionScores[i][h][j] = sum / scaleFactor;
                }
            }
        }

        // Apply softmax to get attention weights
        float[][][] attentionWeights = applySoftmax(attentionScores);
        this.lastAttentionWeights = attentionWeights;

        // Compute weighted sum of values
        for (int i = 0; i < seqLen; i++) {
            for (int h = 0; h < numHeads; h++) {
                for (int k = 0; k < headDim; k++) {
                    float sum = 0;
                    for (int j = 0; j < seqLen; j++) {
                        sum += attentionWeights[i][h][j] * values[j][h][k];
                    }
                    output[i][h][k] = sum;
                }
            }
        }

        return output;
    }

    /**
     * Applies softmax function to the input tensor.
     *
     * @param input Input tensor
     * @return Tensor after applying softmax
     */
    private float[][][] applySoftmax(float[][][] input) {
        float[][][] output = new float[input.length][input[0].length][input[0][0].length];

        for (int i = 0; i < input.length; i++) {
            for (int h = 0; h < input[0].length; h++) {
                float max = Float.NEGATIVE_INFINITY;
                for (int j = 0; j < input[0][0].length; j++) {
                    if (input[i][h][j] > max) {
                        max = input[i][h][j];
                    }
                }

                float sum = 0;
                for (int j = 0; j < input[0][0].length; j++) {
                    output[i][h][j] = (float) Math.exp(input[i][h][j] - max);
                    sum += output[i][h][j];
                }

                for (int j = 0; j < input[0][0].length; j++) {
                    output[i][h][j] /= sum;
                }
            }
        }

        return output;
    }

    /**
     * Backward pass of the MultiHeadAttention layer.
     *
     * @param gradOutput Gradient of the loss with respect to the output of this
     * layer
     * @return Gradient of the loss with respect to the input of this layer
     */
    public float[][] backward(float[][] gradOutput) {
        float[][] gradOutputLayer = outputLayer.backward(gradOutput);
        float[][][] gradMultiHead = splitHeads(gradOutputLayer);

        float[][][] gradValues = new float[gradMultiHead.length][numHeads][headDim];
        float[][][] gradKeys = new float[gradMultiHead.length][numHeads][headDim];
        float[][][] gradQueries = new float[gradMultiHead.length][numHeads][headDim];

//        for (int i = 0; i < gradMultiHead.length; i++) {
//            for (int h = 0; h < numHeads; h++) {
//                for (int j = 0; j < gradMultiHead.length; j++) {
//                    for (int k = 0; k < headDim; k++) {
//                        gradValues[j][h][k] += lastAttentionWeights[i][h][j] * gradMultiHead[i][h][k];
//                        gradKeys[j][h][k] += lastQueries[i][h][k] * gradMultiHead[i][h][j] / (float)Math.sqrt(headDim);
//                        gradQueries[i][h][k] += lastKeys[j][h][k] * gradMultiHead[i][h][j] / (float)Math.sqrt(headDim);
//                    }
//                }
//            }
//        }
        int seqLen = gradMultiHead.length;
        for (int i = 0; i < seqLen; i++) {
            for (int h = 0; h < numHeads; h++) {
                for (int j = 0; j < seqLen; j++) {
                    float attentionWeight = lastAttentionWeights[i][h][j];
                    float scaledGrad = gradMultiHead[i][h][0] / (float) Math.sqrt(headDim);
                    for (int k = 0; k < headDim; k++) {
                        gradValues[j][h][k] += attentionWeight * gradMultiHead[i][h][k];
                        gradKeys[j][h][k] += lastQueries[i][h][k] * scaledGrad;
                        gradQueries[i][h][k] += lastKeys[j][h][k] * scaledGrad;
                    }
                }
            }
        }

        float[][] gradValue = concatenateHeads(gradValues);
        float[][] gradKey = concatenateHeads(gradKeys);
        float[][] gradQuery = concatenateHeads(gradQueries);

        float[][] gradInput = new float[lastInput.length][embeddingDim];
        float[][] gradV = valueLayer.backward(gradValue);
        float[][] gradK = keyLayer.backward(gradKey);
        float[][] gradQ = queryLayer.backward(gradQuery);

        for (int i = 0; i < gradInput.length; i++) {
            for (int j = 0; j < embeddingDim; j++) {
                gradInput[i][j] = gradV[i][j] + gradK[i][j] + gradQ[i][j];
            }
        }

        return gradInput;
    }

    /**
     * Updates the parameters of the layer.
     *
     * @param learningRate The learning rate for parameter updates
     */
    public void updateParameters(float learningRate) {
        queryLayer.updateParameters(learningRate);
        keyLayer.updateParameters(learningRate);
        valueLayer.updateParameters(learningRate);
        outputLayer.updateParameters(learningRate);
    }
}
