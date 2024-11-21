package jazari.deep_learning.vit;

/**
 * LayerNorm class for Vision Transformer (ViT) implementation.
 * This class implements Layer Normalization, which normalizes the inputs
 * across the features, helping to stabilize the learning process.
 */
public class LayerNorm {
    private int featureDim;
    private float epsilon;
    private float[] gamma;
    private float[] beta;
    private float[][] lastInput;
    private float[][] lastOutput;
    private float[] lastMean;
    private float[] lastVar;

    /**
     * Constructor for LayerNorm.
     * 
     * @param featureDim The dimension of the features to be normalized
     * @param epsilon A small value added to the variance for numerical stability
     */
    public LayerNorm(int featureDim, float epsilon) {
        this.featureDim = featureDim;
        this.epsilon = epsilon;
        this.gamma = new float[featureDim];
        this.beta = new float[featureDim];
        initializeParameters();
    }

    /**
     * Initializes the learnable parameters gamma and beta.
     */
    private void initializeParameters() {
        for (int i = 0; i < featureDim; i++) {
            gamma[i] = 1.0f;
            beta[i] = 0.0f;
        }
    }

    /**
     * Forward pass of the LayerNorm.
     * 
     * @param x Input tensor as a 2D array [batchSize][featureDim]
     * @return Normalized output
     */
    public float[][] forward(float[][] x) {
        int batchSize = x.length;
        float[][] normalized = new float[batchSize][featureDim];
        lastInput = x;
        lastMean = new float[batchSize];
        lastVar = new float[batchSize];

        for (int i = 0; i < batchSize; i++) {
            // Compute mean
            float mean = 0;
            for (int j = 0; j < featureDim; j++) {
                mean += x[i][j];
            }
            mean /= featureDim;
            lastMean[i] = mean;

            // Compute variance
            float variance = 0;
            for (int j = 0; j < featureDim; j++) {
                variance += (x[i][j] - mean) * (x[i][j] - mean);
            }
            variance /= featureDim;
            lastVar[i] = variance;

            // Normalize and scale
            for (int j = 0; j < featureDim; j++) {
                normalized[i][j] = (x[i][j] - mean) / (float) Math.sqrt(variance + epsilon);
                normalized[i][j] = gamma[j] * normalized[i][j] + beta[j];
            }
        }

        lastOutput = normalized;
        return normalized;
    }

    /**
     * Backward pass of the LayerNorm.
     * 
     * @param gradOutput Gradient of the loss with respect to the output of this layer
     * @return Gradient of the loss with respect to the input of this layer
     */
    public float[][] backward(float[][] gradOutput) {
        int batchSize = gradOutput.length;
        float[][] gradInput = new float[batchSize][featureDim];
        float[] gradGamma = new float[featureDim];
        float[] gradBeta = new float[featureDim];

        for (int i = 0; i < batchSize; i++) {
            float mean = lastMean[i];
            float var = lastVar[i];
            float invStd = (float) (1.0 / Math.sqrt(var + epsilon));

            float[] dxhat = new float[featureDim];
            float dvar = 0;
            float dmean = 0;

            for (int j = 0; j < featureDim; j++) {
                dxhat[j] = gradOutput[i][j] * gamma[j];
                gradGamma[j] += gradOutput[i][j] * (lastInput[i][j] - mean) * invStd;
                gradBeta[j] += gradOutput[i][j];
            }

            for (int j = 0; j < featureDim; j++) {
                dvar += dxhat[j] * (lastInput[i][j] - mean) * -0.5f * invStd * invStd * invStd;
                dmean += dxhat[j] * -invStd;
            }

            dmean += dvar * -2f * (lastInput[i][0] - mean) / featureDim;

            for (int j = 0; j < featureDim; j++) {
                gradInput[i][j] = dxhat[j] * invStd + 
                                  dvar * 2f * (lastInput[i][j] - mean) / featureDim + 
                                  dmean / featureDim;
            }
        }

        // Update gamma and beta
        float learningRate = 0.01f; // This should be a configurable parameter
        for (int j = 0; j < featureDim; j++) {
            gamma[j] -= learningRate * gradGamma[j] / batchSize;
            beta[j] -= learningRate * gradBeta[j] / batchSize;
        }

        return gradInput;
    }

    /**
     * Updates the parameters of the layer.
     * 
     * @param learningRate The learning rate for parameter updates
     */
    public void updateParameters(float learningRate) {
        // Parameters are already updated in the backward pass
        // This method is kept for consistency with other layers
    }

    // Getter methods for testing and debugging
    public float[] getGamma() { return gamma; }
    public float[] getBeta() { return beta; }
}