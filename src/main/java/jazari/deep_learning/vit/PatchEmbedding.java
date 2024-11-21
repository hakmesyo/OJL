package jazari.deep_learning.vit;

import java.util.Random;

/**
 * PatchEmbedding class for Vision Transformer (ViT) implementation.
 * This class is responsible for dividing an input image into patches
 * and projecting each patch into a lower-dimensional embedding space.
 */
public class PatchEmbedding {
    private int patchSize;
    private int numChannels;
    private int embeddingDim;
    private float[][][][] weights;
    private float[] bias;
    private float[][][] lastInput;
    private float[][] lastOutput;
    private float[][][][] gradWeights;
    private float[] gradBias;

    /**
     * Constructor for PatchEmbedding.
     * 
     * @param patchSize The size of each patch (assuming square patches)
     * @param numChannels The number of channels in the input image
     * @param embeddingDim The dimension of the output embedding
     */
    public PatchEmbedding(int patchSize, int numChannels, int embeddingDim) {
        this.patchSize = patchSize;
        this.numChannels = numChannels;
        this.embeddingDim = embeddingDim;
        initializeParameters();
    }

    /**
     * Initialize the weights and biases of the embedding layer.
     * Uses He initialization for weights and zeros for biases.
     */
    private void initializeParameters() {
        Random random = new Random();
        float stddev = (float) Math.sqrt(2.0 / (numChannels * patchSize * patchSize));
        
        weights = new float[embeddingDim][numChannels][patchSize][patchSize];
        for (int i = 0; i < embeddingDim; i++) {
            for (int c = 0; c < numChannels; c++) {
                for (int h = 0; h < patchSize; h++) {
                    for (int w = 0; w < patchSize; w++) {
                        weights[i][c][h][w] = (float) (random.nextGaussian() * stddev);
                    }
                }
            }
        }

        bias = new float[embeddingDim];
        for (int i = 0; i < embeddingDim; i++) {
            bias[i] = 0.0f;
        }

        gradWeights = new float[embeddingDim][numChannels][patchSize][patchSize];
        gradBias = new float[embeddingDim];
    }

    /**
     * Forward pass of the PatchEmbedding layer.
     * 
     * @param image Input image as a 3D array [height][width][channel]
     * @return Embedded patches as a 2D array [numPatches][embeddingDim]
     */
    public float[][] forward(float[][][] image) {
        int imageHeight = image.length;
        int imageWidth = image[0].length;
        int numPatchesH = imageHeight / patchSize;
        int numPatchesW = imageWidth / patchSize;
        int numPatches = numPatchesH * numPatchesW;

        float[][] embeddings = new float[numPatches][embeddingDim];

        for (int ph = 0; ph < numPatchesH; ph++) {
            for (int pw = 0; pw < numPatchesW; pw++) {
                int patchIndex = ph * numPatchesW + pw;
                float[] patchEmbedding = embedPatch(image, ph * patchSize, pw * patchSize);
                embeddings[patchIndex] = patchEmbedding;
            }
        }

        this.lastInput = image;
        this.lastOutput = embeddings;
        return embeddings;
    }

    /**
     * Embed a single patch from the input image.
     * 
     * @param image Input image
     * @param startH Starting height of the patch
     * @param startW Starting width of the patch
     * @return Embedded patch as a 1D array
     */
    private float[] embedPatch(float[][][] image, int startH, int startW) {
        float[] embedding = new float[embeddingDim];
        for (int e = 0; e < embeddingDim; e++) {
            float sum = 0.0f;
            for (int c = 0; c < numChannels; c++) {
                for (int h = 0; h < patchSize; h++) {
                    for (int w = 0; w < patchSize; w++) {
                        sum += image[startH + h][startW + w][c] * weights[e][c][h][w];
                    }
                }
            }
            embedding[e] = sum + bias[e];
        }
        return embedding;
    }

    /**
     * Backward pass of the PatchEmbedding layer.
     * 
     * @param gradOutput Gradient of the loss with respect to the output of this layer
     * @return Gradient of the loss with respect to the input of this layer
     */
    public float[][][] backward(float[][] gradOutput) {
        int imageHeight = lastInput.length;
        int imageWidth = lastInput[0].length;
        float[][][] gradInput = new float[imageHeight][imageWidth][numChannels];

        int numPatchesH = imageHeight / patchSize;
        int numPatchesW = imageWidth / patchSize;

        for (int ph = 0; ph < numPatchesH; ph++) {
            for (int pw = 0; pw < numPatchesW; pw++) {
                int patchIndex = ph * numPatchesW + pw;
                backwardPatch(gradOutput[patchIndex], gradInput, ph * patchSize, pw * patchSize);
            }
        }

        return gradInput;
    }

    /**
     * Compute gradients for a single patch.
     * 
     * @param gradOutput Gradient for a single patch
     * @param gradInput Accumulated gradients for the input
     * @param startH Starting height of the patch
     * @param startW Starting width of the patch
     */
    private void backwardPatch(float[] gradOutput, float[][][] gradInput, int startH, int startW) {
        for (int e = 0; e < embeddingDim; e++) {
            gradBias[e] += gradOutput[e];
            for (int c = 0; c < numChannels; c++) {
                for (int h = 0; h < patchSize; h++) {
                    for (int w = 0; w < patchSize; w++) {
                        float inputValue = lastInput[startH + h][startW + w][c];
                        gradWeights[e][c][h][w] += gradOutput[e] * inputValue;
                        gradInput[startH + h][startW + w][c] += gradOutput[e] * weights[e][c][h][w];
                    }
                }
            }
        }
    }

    /**
     * Updates the parameters of the layer using the computed gradients.
     * 
     * @param learningRate The learning rate for parameter updates
     */
    public void updateParameters(float learningRate) {
        for (int e = 0; e < embeddingDim; e++) {
            bias[e] -= learningRate * gradBias[e];
            for (int c = 0; c < numChannels; c++) {
                for (int h = 0; h < patchSize; h++) {
                    for (int w = 0; w < patchSize; w++) {
                        weights[e][c][h][w] -= learningRate * gradWeights[e][c][h][w];
                    }
                }
            }
        }
        
        // Reset gradients after update
        gradWeights = new float[embeddingDim][numChannels][patchSize][patchSize];
        gradBias = new float[embeddingDim];
    }

    // Getter methods for testing and debugging
    public int getPatchSize() { return patchSize; }
    public int getNumChannels() { return numChannels; }
    public int getEmbeddingDim() { return embeddingDim; }
    public float[][][][] getWeights() { return weights; }
    public float[] getBias() { return bias; }
}