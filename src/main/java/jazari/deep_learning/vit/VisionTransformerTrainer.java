/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author cezerilab
 */
package jazari.deep_learning.vit;

import java.io.*;
import java.util.*;
import java.nio.file.*;

/**
 * VisionTransformerTrainer class for training and testing a Vision Transformer model.
 * This class is designed to work with various image datasets, not just MNIST.
 */
public class VisionTransformerTrainer {
    private static final int BATCH_SIZE = 32;
    private static final int NUM_EPOCHS = 10;
    private static final float LEARNING_RATE = 0.001f;

    private VisionTransformer model;
    private int imageSize;
    private int numChannels;

    /**
     * Constructor for VisionTransformerTrainer.
     *
     * @param imageSize Size of the input images (assuming square images)
     * @param patchSize Size of each patch (assuming square patches)
     * @param numChannels Number of channels in the input images
     * @param embeddingDim Dimension of the token embeddings
     * @param numHeads Number of attention heads in each Transformer encoder
     * @param numLayers Number of Transformer encoder layers
     * @param numClasses Number of output classes
     * @param mlpDim Dimension of the MLP in the Transformer encoder
     */
    public VisionTransformerTrainer(int imageSize, int patchSize, int numChannels, int embeddingDim, 
                                    int numHeads, int numLayers, int numClasses, int mlpDim) {
        this.imageSize = imageSize;
        this.numChannels = numChannels;
        this.model = new VisionTransformer(imageSize, patchSize, numChannels, embeddingDim, 
                                           numHeads, numLayers, numClasses, mlpDim);
    }

    /**
     * Trains the Vision Transformer model on the given dataset.
     *
     * @param trainImages List of training images
     * @param trainLabels List of corresponding training labels
     */
    public void train(List<float[][][]> trainImages, List<Integer> trainLabels) {
        System.out.println("Starting training...");
        for (int epoch = 0; epoch < NUM_EPOCHS; epoch++) {
            float totalLoss = 0;
            int correct = 0;
            for (int i = 0; i < trainImages.size(); i += BATCH_SIZE) {
                int batchEnd = Math.min(i + BATCH_SIZE, trainImages.size());
                List<float[][][]> batchImages = trainImages.subList(i, batchEnd);
                List<Integer> batchLabels = trainLabels.subList(i, batchEnd);

                float loss = trainBatch(batchImages, batchLabels);
                totalLoss += loss;

                // Calculate accuracy
                for (int j = 0; j < batchImages.size(); j++) {
                    float[] prediction = model.forward(batchImages.get(j));
                    int predictedLabel = argmax(prediction);
                    if (predictedLabel == batchLabels.get(j)) {
                        correct++;
                    }
                }
                //System.out.println(i+".batch, total correct = " + correct+" in "+i+" samples");
            }
            float accuracy = (float) correct / trainImages.size();
            System.out.printf("Epoch %d: Loss = %.4f, Accuracy = %.4f\n", epoch + 1, totalLoss / trainImages.size(), accuracy);
        }
    }

    /**
     * Trains the model on a single batch of data.
     *
     * @param batchImages List of images in the batch
     * @param batchLabels List of corresponding labels
     * @return Average loss for the batch
     */
    private float trainBatch(List<float[][][]> batchImages, List<Integer> batchLabels) {
        float totalLoss = 0;
        float[][] gradOutputs = new float[batchImages.size()][model.getNumClasses()];

        for (int i = 0; i < batchImages.size(); i++) {
            float[] output = model.forward(batchImages.get(i));
            float[] target = new float[model.getNumClasses()];
            target[batchLabels.get(i)] = 1;

            float loss = computeCrossEntropyLoss(output, target);
            totalLoss += loss;

            gradOutputs[i] = computeGradCrossEntropyLoss(output, target);
        }

        // Compute average gradients
        float[] avgGradOutput = new float[model.getNumClasses()];
        for (int i = 0; i < model.getNumClasses(); i++) {
            for (int j = 0; j < batchImages.size(); j++) {
                avgGradOutput[i] += gradOutputs[j][i];
            }
            avgGradOutput[i] /= batchImages.size();
        }

        model.backward(avgGradOutput);
        model.updateParameters(LEARNING_RATE);

        return totalLoss / batchImages.size();
    }

    /**
     * Tests the trained model on a test dataset.
     *
     * @param testImages List of test images
     * @param testLabels List of corresponding test labels
     * @return Accuracy of the model on the test dataset
     */
    public float test(List<float[][][]> testImages, List<Integer> testLabels) {
        System.out.println("Starting testing...");
        int correct = 0;
        for (int i = 0; i < testImages.size(); i++) {
            float[] prediction = model.forward(testImages.get(i));
            int predictedLabel = argmax(prediction);
            if (predictedLabel == testLabels.get(i)) {
                correct++;
            }
        }
        float accuracy = (float) correct / testImages.size();
        System.out.printf("Test Accuracy: %.4f\n", accuracy);
        return accuracy;
    }

    /**
     * Computes the cross-entropy loss.
     *
     * @param output Predicted probabilities
     * @param target True label (one-hot encoded)
     * @return Cross-entropy loss
     */
    private float computeCrossEntropyLoss(float[] output, float[] target) {
        float loss = 0;
        for (int i = 0; i < output.length; i++) {
            loss -= target[i] * Math.log(output[i] + 1e-10); // Adding small epsilon to avoid log(0)
        }
        return loss;
    }

    /**
     * Computes the gradient of the cross-entropy loss.
     *
     * @param output Predicted probabilities
     * @param target True label (one-hot encoded)
     * @return Gradient of the cross-entropy loss
     */
    private float[] computeGradCrossEntropyLoss(float[] output, float[] target) {
        float[] grad = new float[output.length];
        for (int i = 0; i < output.length; i++) {
            grad[i] = output[i] - target[i];
        }
        return grad;
    }

    /**
     * Finds the index of the maximum value in an array.
     *
     * @param array Input array
     * @return Index of the maximum value
     */
    private int argmax(float[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Loads image data from a file.
     *
     * @param filepath Path to the image data file
     * @return List of images as 3D float arrays
     * @throws IOException If there's an error reading the file
     */
    public List<float[][][]> loadImageData(String filepath) throws IOException {
        List<float[][][]> images = new ArrayList<>();
        byte[] bytes = Files.readAllBytes(Paths.get(filepath));
        
        int magicNumber = readInt(bytes, 0);
        int numImages = readInt(bytes, 4);
        int numRows = readInt(bytes, 8);
        int numCols = readInt(bytes, 12);
        
        if (numRows != imageSize || numCols != imageSize) {
            throw new IllegalArgumentException("Image size in file does not match the expected size");
        }
        
        int offset = 16;
        for (int i = 0; i < numImages; i++) {
            float[][][] image = new float[imageSize][imageSize][numChannels];
            for (int row = 0; row < imageSize; row++) {
                for (int col = 0; col < imageSize; col++) {
                    for (int c = 0; c < numChannels; c++) {
                        image[row][col][c] = (bytes[offset++] & 0xFF) / 255.0f;
                    }
                }
            }
            images.add(image);
        }
        return images;
    }

    /**
     * Loads label data from a file.
     *
     * @param filepath Path to the label data file
     * @return List of labels as integers
     * @throws IOException If there's an error reading the file
     */
    public List<Integer> loadLabelData(String filepath) throws IOException {
        List<Integer> labels = new ArrayList<>();
        byte[] bytes = Files.readAllBytes(Paths.get(filepath));
        
        int magicNumber = readInt(bytes, 0);
        int numLabels = readInt(bytes, 4);
        
        int offset = 8;
        for (int i = 0; i < numLabels; i++) {
            labels.add((int) bytes[offset++]);
        }
        return labels;
    }

    /**
     * Reads a 32-bit integer from a byte array.
     *
     * @param bytes Byte array
     * @param offset Starting offset in the array
     * @return 32-bit integer value
     */
    private int readInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
               ((bytes[offset + 1] & 0xFF) << 16) |
               ((bytes[offset + 2] & 0xFF) << 8) |
               (bytes[offset + 3] & 0xFF);
    }

    /**
     * Main method to demonstrate the usage of VisionTransformerTrainer.
     *
     * @param args Command line arguments (not used)
     * @throws IOException If there's an error reading the data files
     */
    public static void main(String[] args) throws IOException {
        // Example usage for MNIST dataset
        int imageSize = 28;
        int patchSize = 7;
        int numChannels = 1;
        int embeddingDim = 64;
        int numHeads = 4;
        int numLayers = 2;
        int numClasses = 10;
        int mlpDim = 128;

        VisionTransformerTrainer trainer = new VisionTransformerTrainer(
            imageSize, patchSize, numChannels, embeddingDim, numHeads, numLayers, numClasses, mlpDim);

        String path="D:\\DATASETS\\classification\\mnist";
        List<float[][][]> trainImages = trainer.loadImageData(path+"/train-images.idx3-ubyte");
        List<Integer> trainLabels = trainer.loadLabelData(path+"/train-labels.idx1-ubyte");
        List<float[][][]> testImages = trainer.loadImageData(path+"/t10k-images.idx3-ubyte");
        List<Integer> testLabels = trainer.loadLabelData(path+"/t10k-labels.idx1-ubyte");

        trainer.train(trainImages, trainLabels);
        trainer.test(testImages, testLabels);
    }
}