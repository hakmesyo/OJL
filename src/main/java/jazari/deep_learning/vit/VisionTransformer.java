package jazari.deep_learning.vit;

import java.util.ArrayList;
import java.util.List;

/**
 * VisionTransformer class implements the core of the Vision Transformer (ViT) model.
 * This class combines all the components (PatchEmbedding, PositionalEncoding, 
 * TransformerEncoder, LayerNorm, and LinearLayer) to create a complete ViT model.
 */
public class VisionTransformer {
    private PatchEmbedding patchEmbedding;
    private PositionalEncoding positionalEncoding;
    private List<TransformerEncoder> encoders;
    private LayerNorm layerNorm;
    private LinearLayer mlpHead;
    private int numPatches;
    private int embeddingDim;
    private int numClasses;

    /**
     * Constructor for VisionTransformer.
     * 
     * @param imageSize Size of the input image (assuming square image)
     * @param patchSize Size of each patch (assuming square patches)
     * @param numChannels Number of channels in the input image
     * @param embeddingDim Dimension of the token embeddings
     * @param numHeads Number of attention heads in each Transformer encoder
     * @param numLayers Number of Transformer encoder layers
     * @param numClasses Number of output classes
     * @param mlpDim Dimension of the MLP in the Transformer encoder
     */
    public VisionTransformer(int imageSize, int patchSize, int numChannels, int embeddingDim, 
                             int numHeads, int numLayers, int numClasses, int mlpDim) {
        this.numPatches = (imageSize / patchSize) * (imageSize / patchSize);
        this.embeddingDim = embeddingDim;
        this.numClasses = numClasses;
        
        this.patchEmbedding = new PatchEmbedding(patchSize, numChannels, embeddingDim);
        this.positionalEncoding = new PositionalEncoding(numPatches + 1, embeddingDim); // +1 for class token
        
        this.encoders = new ArrayList<>();
        for (int i = 0; i < numLayers; i++) {
            this.encoders.add(new TransformerEncoder(embeddingDim, numHeads, mlpDim));
        }
        
        this.layerNorm = new LayerNorm(embeddingDim, 1e-6f);
        this.mlpHead = new LinearLayer(embeddingDim, numClasses);
    }

    /**
     * Forward pass of the Vision Transformer.
     * 
     * @param image Input image as a 3D array [height][width][channel]
     * @return Class probabilities as a 1D array
     */
    public float[] forward(float[][][] image) {
        // Patch embedding
        float[][] x = patchEmbedding.forward(image);
        
        // Add class token
        float[][] xWithClassToken = addClassToken(x);
        
        // Add positional encoding
        xWithClassToken = positionalEncoding.addPositionalEncoding(xWithClassToken);
        
        // Pass through transformer encoders
        for (TransformerEncoder encoder : encoders) {
            xWithClassToken = encoder.forward(xWithClassToken);
        }
        
        // Apply layer norm
        xWithClassToken = layerNorm.forward(xWithClassToken);
        
        // Use the class token for classification
        float[] classToken = xWithClassToken[0];
        
        // Pass through MLP head
        float[] logits = mlpHead.forward(new float[][]{classToken})[0];
        
        // Apply softmax
        return softmax(logits);
    }

    /**
     * Adds a class token to the beginning of the patch embeddings.
     * 
     * @param x Patch embeddings
     * @return Patch embeddings with class token prepended
     */
    private float[][] addClassToken(float[][] x) {
        float[][] xWithClassToken = new float[x.length + 1][embeddingDim];
        // Initialize class token with zeros
        for (int j = 0; j < embeddingDim; j++) {
            xWithClassToken[0][j] = 0;
        }
        // Copy patch embeddings
        for (int i = 0; i < x.length; i++) {
            System.arraycopy(x[i], 0, xWithClassToken[i + 1], 0, embeddingDim);
        }
        return xWithClassToken;
    }

    /**
     * Backward pass of the Vision Transformer.
     * 
     * @param gradOutput Gradient of the loss with respect to the output of this model
     * @return Gradient of the loss with respect to the input image
     */
    public float[][][] backward(float[] gradOutput) {
        // Backprop through softmax and MLP head
        float[][] gradMlpHead = mlpHead.backward(new float[][]{gradOutput});
        
        // Backprop through layer norm
        float[][] gradLayerNorm = layerNorm.backward(gradMlpHead);
        
        // Backprop through transformer encoders
        float[][] gradEncoder = gradLayerNorm;
        for (int i = encoders.size() - 1; i >= 0; i--) {
            gradEncoder = encoders.get(i).backward(gradEncoder);
        }
        
        // Remove gradients for positional encoding (it's fixed)
        gradEncoder = removePositionalEncodingGradients(gradEncoder);
        
        // Remove class token gradient
        float[][] gradWithoutClassToken = removeClassTokenGradient(gradEncoder);
        
        // Backprop through patch embedding
        return patchEmbedding.backward(gradWithoutClassToken);
    }

    /**
     * Removes gradients corresponding to positional encodings.
     * 
     * @param gradients Gradients including positional encoding
     * @return Gradients without positional encoding
     */
    private float[][] removePositionalEncodingGradients(float[][] gradients) {
        // In this simple implementation, we just return the gradients as is
        // because positional encodings are added element-wise
        return gradients;
    }

    /**
     * Removes the gradient corresponding to the class token.
     * 
     * @param gradients Gradients including class token
     * @return Gradients without class token
     */
    private float[][] removeClassTokenGradient(float[][] gradients) {
        float[][] gradientsWithoutClassToken = new float[gradients.length - 1][gradients[0].length];
        for (int i = 1; i < gradients.length; i++) {
            System.arraycopy(gradients[i], 0, gradientsWithoutClassToken[i - 1], 0, gradients[0].length);
        }
        return gradientsWithoutClassToken;
    }

    /**
     * Updates the parameters of all layers in the model.
     * 
     * @param learningRate The learning rate for parameter updates
     */
    public void updateParameters(float learningRate) {
        patchEmbedding.updateParameters(learningRate);
        for (TransformerEncoder encoder : encoders) {
            encoder.updateParameters(learningRate);
        }
        layerNorm.updateParameters(learningRate);
        mlpHead.updateParameters(learningRate);
    }

    /**
     * Applies the softmax function to the input array.
     * 
     * @param input Input array
     * @return Array after applying softmax
     */
    private float[] softmax(float[] input) {
        float max = Float.NEGATIVE_INFINITY;
        for (float value : input) {
            if (value > max) {
                max = value;
            }
        }
        
        float sum = 0.0f;
        float[] output = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (float) Math.exp(input[i] - max);
            sum += output[i];
        }
        
        for (int i = 0; i < output.length; i++) {
            output[i] /= sum;
        }
        
        return output;
    }

    /**
     * Gets the number of patches.
     * 
     * @return The number of patches
     */
    public int getNumPatches() {
        return numPatches;
    }

    /**
     * Gets the embedding dimension.
     * 
     * @return The embedding dimension
     */
    public int getEmbeddingDim() {
        return embeddingDim;
    }

    /**
     * Gets the number of classes.
     * 
     * @return The number of classes
     */
    public int getNumClasses() {
        return numClasses;
    }
}