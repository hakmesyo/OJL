package jazari.deep_learning.vit;

/**
 * PositionalEncoding class for Vision Transformer (ViT) implementation.
 * This class generates and adds positional encodings to the input embeddings.
 * Positional encodings help the model distinguish between different positions in the input sequence.
 */
public class PositionalEncoding {
    private float[][] encodings;

    /**
     * Constructor for PositionalEncoding.
     * 
     * @param numPatches The number of patches (or sequence length)
     * @param embeddingDim The dimension of the embeddings
     */
    public PositionalEncoding(int numPatches, int embeddingDim) {
        this.encodings = generatePositionalEncodings(numPatches, embeddingDim);
    }

    /**
     * Generates positional encodings using sine and cosine functions.
     * 
     * @param numPatches The number of patches (or sequence length)
     * @param embeddingDim The dimension of the embeddings
     * @return A 2D array of positional encodings
     */
    private float[][] generatePositionalEncodings(int numPatches, int embeddingDim) {
        float[][] encodings = new float[numPatches][embeddingDim];
        for (int pos = 0; pos < numPatches; pos++) {
            for (int i = 0; i < embeddingDim; i++) {
                double angle = pos / Math.pow(10000, (2.0 * i) / embeddingDim);
                encodings[pos][i] = (float) (i % 2 == 0 ? Math.sin(angle) : Math.cos(angle));
            }
        }
        return encodings;
    }

    /**
     * Adds positional encodings to the input embeddings.
     * 
     * @param x Input embeddings as a 2D array [numPatches][embeddingDim]
     * @return Embeddings with added positional encodings
     */
    public float[][] addPositionalEncoding(float[][] x) {
        if (x.length != encodings.length || x[0].length != encodings[0].length) {
            throw new IllegalArgumentException("Input dimensions do not match positional encoding dimensions");
        }
        float[][] result = new float[x.length][x[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[i].length; j++) {
                result[i][j] = x[i][j] + encodings[i][j];
            }
        }
        return result;
    }

    /**
     * Getter method for the encodings.
     * 
     * @return The positional encodings as a 2D array
     */
    public float[][] getEncodings() {
        return encodings;
    }

    /**
     * Utility method to print encodings for debugging purposes.
     */
    public void printEncodings() {
        System.out.println("Positional Encodings:");
        for (int i = 0; i < encodings.length; i++) {
            System.out.print("Position " + i + ": ");
            for (int j = 0; j < encodings[i].length; j++) {
                System.out.printf("%.4f ", encodings[i][j]);
            }
            System.out.println();
        }
    }
}