/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.spin;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class SPIN {

    // Activation function types
    public enum Activation {
        RELU, SIGMOID, TANH
    }

    // Model configuration
    private final int inputHeight;
    private final int inputWidth;
    private final int numClasses;
    private final List<Integer> spatialLayers;
    private final double learningRate;
    private final Activation activation;
    private final boolean useBatchNorm;

    // Model parameters
    private List<SpatialLayer> layers;
    private double[][][] finalWeights; // Weights for the final fully connected layer
    private double[] finalBiases;

    // Cache for backpropagation
    private List<double[][][]> layerInputs;
    private List<double[][][]> layerOutputs;
    private double[] finalLayerInput;

    public SPIN(int inputHeight, int inputWidth, int numClasses, List<Integer> spatialLayers,
            double learningRate, Activation activation, boolean useBatchNorm) {
        this.inputHeight = inputHeight;
        this.inputWidth = inputWidth;
        this.numClasses = numClasses;
        this.spatialLayers = spatialLayers;
        this.learningRate = learningRate;
        this.activation = activation;
        this.useBatchNorm = useBatchNorm;

        initializeModel();
    }

    private void initializeModel() {
        layers = new ArrayList<>();
        int currentHeight = inputHeight;
        int currentWidth = inputWidth;

        // Initialize spatial layers
        for (int numFeatureMaps : spatialLayers) {
            currentHeight /= 2;
            currentWidth /= 2;

            SpatialLayer layer = new SpatialLayer(
                    currentHeight,
                    currentWidth,
                    numFeatureMaps
            );
            layers.add(layer);
        }

        // Calculate total features size for final layer
        int totalFeaturesSize = 0;
        currentHeight = inputHeight;
        currentWidth = inputWidth;
        for (int numFeatureMaps : spatialLayers) {
            currentHeight /= 2;
            currentWidth /= 2;
            totalFeaturesSize += currentHeight * currentWidth * numFeatureMaps;
        }

        // Initialize final fully connected layer
        finalWeights = new double[numClasses][totalFeaturesSize][1];
        finalBiases = new double[numClasses];

        // Random initialization for final layer
        Random random = new Random(42);
        double weightScale = 0.1;

        for (int i = 0; i < numClasses; i++) {
            for (int j = 0; j < totalFeaturesSize; j++) {
                finalWeights[i][j][0] = random.nextGaussian() * weightScale;
            }
            finalBiases[i] = 0;
        }
    }

    private double[] combineFeatures(List<double[][][]> layerOutputs) {
        // Calculate total size needed
        int totalSize = 0;
        for (double[][][] output : layerOutputs) {
            totalSize += output.length * output[0].length * output[0][0].length;
        }

        double[] combined = new double[totalSize];
        int currentIndex = 0;

        // Flatten and combine all layer outputs
        for (double[][][] output : layerOutputs) {
            int depth = output.length;
            int height = output[0].length;
            int width = output[0][0].length;

            for (int d = 0; d < depth; d++) {
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        combined[currentIndex++] = output[d][h][w];
                    }
                }
            }
        }

        return combined;
    }

    private static class BatchNormLayer {

        private final int size;
        private final double epsilon = 1e-5;
        private final double momentum = 0.9;

        private double[] gamma;  // scale parameter
        private double[] beta;   // shift parameter
        private double[] runningMean;
        private double[] runningVar;

        // Cache for backprop
        private double[] xNorm;
        private double[] var;
        private double[] std;
        private double[] xCentered;

        public BatchNormLayer(int size) {
            this.size = size;
            this.gamma = new double[size];
            this.beta = new double[size];
            this.runningMean = new double[size];
            this.runningVar = new double[size];

            // Initialize gamma and beta
            Arrays.fill(gamma, 1.0);
            Arrays.fill(beta, 0.0);
        }

        public double[] forward(double[] input, boolean training) {
            if (training) {
                // Calculate mean and variance for current batch
                double mean = 0;
                double var = 0;

                for (double val : input) {
                    mean += val;
                }
                mean /= input.length;

                for (double val : input) {
                    var += (val - mean) * (val - mean);
                }
                var /= input.length;

                // Update running statistics
                for (int i = 0; i < size; i++) {
                    runningMean[i] = momentum * runningMean[i] + (1 - momentum) * mean;
                    runningVar[i] = momentum * runningVar[i] + (1 - momentum) * var;
                }

                // Normalize
                std = new double[input.length];
                xCentered = new double[input.length];
                xNorm = new double[input.length];
                double stdDev = Math.sqrt(var + epsilon);

                for (int i = 0; i < input.length; i++) {
                    xCentered[i] = input[i] - mean;
                    std[i] = stdDev;
                    xNorm[i] = xCentered[i] / std[i];
                }
            } else {
                // Use running statistics
                xNorm = new double[input.length];
                for (int i = 0; i < input.length; i++) {
                    xNorm[i] = (input[i] - runningMean[i]) / Math.sqrt(runningVar[i] + epsilon);
                }
            }

            // Scale and shift
            double[] output = new double[input.length];
            for (int i = 0; i < input.length; i++) {
                output[i] = gamma[i % size] * xNorm[i] + beta[i % size];
            }

            return output;
        }

        public double[] backward(double[] dout) {
            int m = dout.length;

            double[] dgamma = new double[size];
            double[] dbeta = new double[size];

            // Gradient with respect to gamma and beta
            for (int i = 0; i < m; i++) {
                dgamma[i % size] += dout[i] * xNorm[i];
                dbeta[i % size] += dout[i];
            }

            // Gradient with respect to x
            double[] dx = new double[m];
            for (int i = 0; i < m; i++) {
                dx[i] = dout[i] * gamma[i % size] / std[i];
            }

            // Update parameters
            for (int i = 0; i < size; i++) {
                gamma[i] += -0.01 * dgamma[i];  // Learning rate 0.01
                beta[i] += -0.01 * dbeta[i];
            }

            return dx;
        }
    }

    private static class Activations {

        private static final double LEAKY_RELU_ALPHA = 0.01;

        public static double activate(double x, Activation type) {
            switch (type) {
                case RELU:
                    return x > 0 ? x : LEAKY_RELU_ALPHA * x;  // Leaky ReLU
                case SIGMOID:
                    return 1.0 / (1.0 + Math.exp(-x));
                case TANH:
                    return Math.tanh(x);
                default:
                    return x;
            }
        }

        public static double activateDerivative(double x, Activation type) {
            switch (type) {
                case RELU:
                    return x > 0 ? 1.0 : LEAKY_RELU_ALPHA;  // Leaky ReLU derivative
                case SIGMOID:
                    double sig = 1.0 / (1.0 + Math.exp(-x));
                    return sig * (1 - sig);
                case TANH:
                    double tanh = Math.tanh(x);
                    return 1 - tanh * tanh;
                default:
                    return 1;
            }
        }

        public static double[] softmax(double[] input) {
            double[] output = new double[input.length];
            double max = Arrays.stream(input).max().getAsDouble();
            double sum = 0.0;

            // Stability trick: subtract max before exp
            for (int i = 0; i < input.length; i++) {
                output[i] = Math.exp(input[i] - max);
                sum += output[i];
            }

            // Normalize
            for (int i = 0; i < output.length; i++) {
                output[i] /= sum;
            }

            return output;
        }
    }

    private class SpatialLayer {

        private final int height;
        private final int width;
        private final int numFeatureMaps;
        private final double[][][][] weights; // [featureMap][inputH][inputW][4]
        private final double[] biases;
        private final BatchNormLayer batchNorm;  // Batch normalization layer

        public SpatialLayer(int height, int width, int numFeatureMaps) {
            this.height = height;
            this.width = width;
            this.numFeatureMaps = numFeatureMaps;

            // Initialize weights and biases
            this.weights = new double[numFeatureMaps][height][width][4];
            this.biases = new double[numFeatureMaps];

            // Initialize batch normalization layer
            this.batchNorm = new BatchNormLayer(height * width * numFeatureMaps);

            Random random = new Random(42);
            // He initialization için weight scale
            double weightScale = Math.sqrt(2.0 / 4);  // 4 = receptive field size (2x2)

            for (int fm = 0; fm < numFeatureMaps; fm++) {
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        for (int i = 0; i < 4; i++) {
                            weights[fm][h][w][i] = random.nextGaussian() * weightScale;
                        }
                    }
                }
                biases[fm] = 0;  // Initialize biases to zero
            }
        }
    }

    private double[] flatten3DArray(double[][][] array3D) {
        int depth = array3D.length;
        int height = array3D[0].length;
        int width = array3D[0][0].length;
        double[] flattened = new double[depth * height * width];

        int idx = 0;
        for (int d = 0; d < depth; d++) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    flattened[idx++] = array3D[d][h][w];
                }
            }
        }
        return flattened;
    }

    private double[][][] reshape1DTo3D(double[] array1D, int depth, int height, int width) {
        // Debug print
//        System.out.printf("Reshaping array of length %d to %dx%dx%d (total: %d)\n",
//                array1D.length, depth, height, width, depth * height * width);

        if (array1D.length != depth * height * width) {
            throw new IllegalArgumentException(String.format(
                    "Array length %d does not match target shape %dx%dx%d = %d",
                    array1D.length, depth, height, width, depth * height * width));
        }

        double[][][] reshaped = new double[depth][height][width];
        int idx = 0;

        try {
            for (int d = 0; d < depth; d++) {
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++) {
                        reshaped[d][h][w] = array1D[idx++];
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.printf("Error at index %d while reshaping to %dx%dx%d\n", idx, depth, height, width);
            throw e;
        }

        return reshaped;
    }

    public void summary() {
        System.out.println("\nModel Summary:");
        System.out.println("=============");
        System.out.println("Input shape: " + inputHeight + "x" + inputWidth);

        int currentHeight = inputHeight;
        int currentWidth = inputWidth;
        long totalParams = 0;

        for (int i = 0; i < layers.size(); i++) {
            currentHeight /= 2;
            currentWidth /= 2;
            int numFeatureMaps = spatialLayers.get(i);
            long layerParams = (4 * numFeatureMaps) * (currentHeight * currentWidth);
            totalParams += layerParams;

            System.out.printf("Spatial Layer %d: Output shape: %dx%dx%d, Parameters: %d\n",
                    i + 1, currentHeight, currentWidth, numFeatureMaps, layerParams);
        }

        int finalFlattenSize = currentHeight * currentWidth * spatialLayers.get(spatialLayers.size() - 1);
        long fcParams = (finalFlattenSize * numClasses) + numClasses;
        totalParams += fcParams;

        System.out.printf("Fully Connected Layer: Output shape: %d, Parameters: %d\n",
                numClasses, fcParams);
        System.out.println("Total parameters: " + totalParams);
    }

    private double[] forward(double[][] input) {
        //System.out.println("\nForward Pass Debug:");
        layerInputs = new ArrayList<>();
        layerOutputs = new ArrayList<>();

        // Convert input to 3D array (1 channel)
        double[][][] currentInput = new double[1][input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                currentInput[0][i][j] = input[i][j];
            }
        }

        // Input stats için debug
        double inputSum = 0;
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                inputSum += input[i][j];
            }
        }
        //System.out.printf("Input mean: %.4f\n", inputSum / (input.length * input[0].length));

        // Forward through spatial layers
        for (int layerIdx = 0; layerIdx < layers.size(); layerIdx++) {
            SpatialLayer layer = layers.get(layerIdx);
            layerInputs.add(currentInput);

            double[][][] output = new double[layer.numFeatureMaps][layer.height][layer.width];
            double layerSum = 0;
            int layerCount = 0;

            // Process each feature map
            for (int fm = 0; fm < layer.numFeatureMaps; fm++) {
                for (int h = 0; h < layer.height; h++) {
                    for (int w = 0; w < layer.width; w++) {
                        double sum = 0;
                        for (int ch = 0; ch < currentInput.length; ch++) {
                            int startH = h * 2;
                            int startW = w * 2;

                            for (int i = 0; i < 2; i++) {
                                for (int j = 0; j < 2; j++) {
                                    sum += currentInput[ch][startH + i][startW + j]
                                            * layer.weights[fm][h][w][i * 2 + j];
                                }
                            }
                        }

                        sum += layer.biases[fm];
                        output[fm][h][w] = Activations.activate(sum, activation);
                        layerSum += output[fm][h][w];
                        layerCount++;
                    }
                }
            }

            // Debug: Layer stats
//            System.out.printf("Layer %d mean activation: %.4f\n",
//                    layerIdx + 1, layerSum / layerCount);

            layerOutputs.add(output);
            currentInput = output;
        }

        // Now combine all layer outputs using skip connections
        double[] combinedFeatures = combineFeatures(layerOutputs);

        // Final fully connected layer
        double[] output = new double[numClasses];
        for (int i = 0; i < numClasses; i++) {
            double sum = 0;
            for (int j = 0; j < combinedFeatures.length; j++) {
                sum += combinedFeatures[j] * finalWeights[i][j][0];
            }
            sum += finalBiases[i];
            output[i] = sum;
        }

//        System.out.printf("Pre-softmax output mean: %.4f\n",
//                Arrays.stream(output).average().getAsDouble());

        // Apply softmax
        output = Activations.softmax(output);

        // Debug: Print softmax outputs
//        System.out.print("Softmax outputs: \n");
//        for (int i = 0; i < output.length; i++) {
//            System.out.printf("Class %d: %.4f ", i, output[i]);
//        }
//        System.out.println();

        return output;
    }

    private void backward(double[] output, int target, double[][] input) {
//        System.out.println("\nBackward Pass Debug:");

        // 1. Calculate initial gradient (softmax derivative * cross-entropy loss derivative)
        double[] dout = Arrays.copyOf(output, output.length);
        dout[target] -= 1.0;

        // Debug: Initial gradient stats
        double gradSum = 0;
        for (double grad : dout) {
            gradSum += Math.abs(grad);
        }
//        System.out.printf("Initial gradient mean abs: %.4f\n", gradSum / dout.length);

        // 2. Get combined features gradient
        // Calculate total features size
        int totalFeaturesSize = 0;
        int currentHeight = inputHeight;
        int currentWidth = inputWidth;
        for (int numFeatureMaps : spatialLayers) {
            currentHeight /= 2;
            currentWidth /= 2;
            totalFeaturesSize += currentHeight * currentWidth * numFeatureMaps;
        }

        double[] dCombinedFeatures = new double[totalFeaturesSize];

        // 3. Calculate gradients for final layer weights and get input gradients
        double weightGradSum = 0;
        int weightCount = 0;

        for (int i = 0; i < numClasses; i++) {
            for (int j = 0; j < totalFeaturesSize; j++) {
                // Gradient for weights
                double grad = dout[i] * finalWeights[i][j][0];
                weightGradSum += Math.abs(grad);
                weightCount++;

                // Update weights
                finalWeights[i][j][0] -= learningRate * grad;
                // Accumulate gradients for combined features
                dCombinedFeatures[j] += dout[i] * finalWeights[i][j][0];
            }
            // Update biases
            finalBiases[i] -= learningRate * dout[i];
        }

//        System.out.printf("Final layer weight gradients mean abs: %.4f\n",
//                weightGradSum / weightCount);

        // 4. Split gradients back to each layer
        List<double[][][]> layerGradients = new ArrayList<>();
        int currentIndex = 0;

        for (int layerIdx = layers.size() - 1; layerIdx >= 0; layerIdx--) {
            SpatialLayer layer = layers.get(layerIdx);
            double[][][] layerGrad = new double[layer.numFeatureMaps][layer.height][layer.width];

            // Fill in gradients for this layer
            for (int fm = 0; fm < layer.numFeatureMaps; fm++) {
                for (int h = 0; h < layer.height; h++) {
                    for (int w = 0; w < layer.width; w++) {
                        layerGrad[fm][h][w] = dCombinedFeatures[currentIndex++];
                    }
                }
            }

            layerGradients.add(0, layerGrad); // Add at beginning since we're going backwards
        }

        // 5. Backpropagate through spatial layers
        for (int layerIdx = layers.size() - 1; layerIdx >= 0; layerIdx--) {
            SpatialLayer layer = layers.get(layerIdx);
            double[][][] layerInput = layerInputs.get(layerIdx);
            double[][][] layerGrad = layerGradients.get(layerIdx);

            // Debug stats for current layer
            double layerGradSum = 0;
            int gradCount = 0;

            // 6. Gradient through activation function
            double[][][] dActivation = new double[layerGrad.length][layerGrad[0].length][layerGrad[0][0].length];
            for (int fm = 0; fm < layerGrad.length; fm++) {
                for (int h = 0; h < layerGrad[0].length; h++) {
                    for (int w = 0; w < layerGrad[0][0].length; w++) {
                        double activationGrad = Activations.activateDerivative(layerGrad[fm][h][w], activation);
                        dActivation[fm][h][w] = layerGrad[fm][h][w] * activationGrad;
                        layerGradSum += Math.abs(dActivation[fm][h][w]);
                        gradCount++;
                    }
                }
            }

            // 7. Calculate gradients for current layer weights and prepare for previous layer
            double[][][] dPrevLayer = null;
            if (layerIdx > 0) {
                SpatialLayer prevLayer = layers.get(layerIdx - 1);
                dPrevLayer = new double[prevLayer.numFeatureMaps][prevLayer.height][prevLayer.width];
            } else {
                dPrevLayer = new double[1][input.length][input[0].length];
            }

            // For each feature map in current layer
            for (int fm = 0; fm < layer.numFeatureMaps; fm++) {
                for (int h = 0; h < layer.height; h++) {
                    for (int w = 0; w < layer.width; w++) {
                        double currentGrad = dActivation[fm][h][w];

                        // For each input channel
                        for (int ch = 0; ch < layerInput.length; ch++) {
                            int startH = h * 2;
                            int startW = w * 2;

                            // Process 2x2 receptive field with boundary checks
                            for (int i = 0; i < 2; i++) {
                                for (int j = 0; j < 2; j++) {
                                    int inputH = startH + i;
                                    int inputW = startW + j;

                                    // Check boundaries
                                    if (inputH < layerInput[ch].length && inputW < layerInput[ch][0].length) {
                                        // Calculate gradient for weight
                                        double inputVal = layerInput[ch][inputH][inputW];
                                        double weightGrad = currentGrad * inputVal;

                                        // Update weight
                                        layer.weights[fm][h][w][i * 2 + j] -= learningRate * weightGrad;

                                        // Accumulate gradient for previous layer
                                        dPrevLayer[ch][inputH][inputW]
                                                += currentGrad * layer.weights[fm][h][w][i * 2 + j];
                                    }
                                }
                            }
                        }

                        // Update bias
                        layer.biases[fm] -= learningRate * currentGrad;
                    }
                }
            }

//            System.out.printf("Layer %d gradient mean abs: %.4f\n", layerIdx + 1, layerGradSum / gradCount);

            // Add skip connection gradients to previous layer if it exists
            if (layerIdx > 0) {
                double[][][] skipGrad = layerGradients.get(layerIdx - 1);
                for (int fm = 0; fm < skipGrad.length; fm++) {
                    for (int h = 0; h < skipGrad[0].length; h++) {
                        for (int w = 0; w < skipGrad[0][0].length; w++) {
                            dPrevLayer[fm][h][w] += skipGrad[fm][h][w];
                        }
                    }
                }
            }
        }
    }

    public void fit(double[][][] trainData, int[] trainLabels,
            double[][][] valData, int[] valLabels,
            int epochs, int batchSize) {
        int numSamples = trainData.length;
        int numBatches = (numSamples + batchSize - 1) / batchSize;

        for (int epoch = 0; epoch < epochs; epoch++) {
            System.out.printf("\nEpoch %d/%d\n", epoch + 1, epochs);

            // Training
            double trainLoss = 0;
            int trainCorrect = 0;

            for (int batch = 0; batch < numBatches; batch++) {
                int startIdx = batch * batchSize;
                int endIdx = Math.min(startIdx + batchSize, numSamples);

                for (int i = startIdx; i < endIdx; i++) {
                    // Forward pass
                    double[] output = forward(trainData[i]);
                    int predicted = argmax(output);

                    if (predicted == trainLabels[i]) {
                        trainCorrect++;
                    }

                    // Calculate loss
                    trainLoss += categoricalCrossEntropyLoss(output, trainLabels[i]);

                    // Backward pass
                    backward(output, trainLabels[i], trainData[i]);
                }

                // Print progress
//                if ((batch + 1) % 10 == 0) {
//                    System.out.printf("Batch %d/%d\n", batch + 1, numBatches);
//                }
            }

            trainLoss /= numSamples;
            double trainAcc = (double) trainCorrect / numSamples;

            // Validation
            double valLoss = 0;
            int valCorrect = 0;

            for (int i = 0; i < valData.length; i++) {
                double[] output = forward(valData[i]);
                int predicted = argmax(output);

                if (predicted == valLabels[i]) {
                    valCorrect++;
                }

                valLoss += categoricalCrossEntropyLoss(output, valLabels[i]);
            }

            valLoss /= valData.length;
            double valAcc = (double) valCorrect / valData.length;

            System.out.printf("train_loss: %.4f - train_acc: %.4f - val_loss: %.4f - val_acc: %.4f\n",
                    trainLoss, trainAcc, valLoss, valAcc);
        }
    }

    public static class EvaluationMetrics {

        private final double accuracy;
        private final double[] precision;
        private final double[] recall;
        private final double[] f1Score;
        private final int[][] confusionMatrix;
        private final int numClasses;

        public EvaluationMetrics(int numClasses, int[] trueLabels, int[] predictedLabels) {
            this.numClasses = numClasses;
            this.confusionMatrix = new int[numClasses][numClasses];
            this.precision = new double[numClasses];
            this.recall = new double[numClasses];
            this.f1Score = new double[numClasses];

            // Calculate confusion matrix
            for (int i = 0; i < trueLabels.length; i++) {
                confusionMatrix[trueLabels[i]][predictedLabels[i]]++;
            }

            // Calculate metrics
            int totalCorrect = 0;
            for (int i = 0; i < numClasses; i++) {
                totalCorrect += confusionMatrix[i][i];
            }
            this.accuracy = (double) totalCorrect / trueLabels.length;

            // Calculate precision, recall, and F1 score for each class
            for (int i = 0; i < numClasses; i++) {
                int truePositives = confusionMatrix[i][i];
                int falsePositives = 0;
                int falseNegatives = 0;

                // Calculate false positives and negatives
                for (int j = 0; j < numClasses; j++) {
                    if (i != j) {
                        falsePositives += confusionMatrix[j][i];
                        falseNegatives += confusionMatrix[i][j];
                    }
                }

                // Calculate precision
                precision[i] = truePositives == 0 ? 0
                        : (double) truePositives / (truePositives + falsePositives);

                // Calculate recall
                recall[i] = truePositives == 0 ? 0
                        : (double) truePositives / (truePositives + falseNegatives);

                // Calculate F1 score
                f1Score[i] = (precision[i] + recall[i] == 0) ? 0
                        : 2 * (precision[i] * recall[i]) / (precision[i] + recall[i]);
            }
        }

        public void printResults() {
            System.out.println("\nEvaluation Results:");
            System.out.println("===================");

            // Print overall accuracy
            System.out.printf("\nOverall Accuracy: %.4f\n", accuracy);

            // Print per-class metrics
            System.out.println("\nPer-class Metrics:");
            System.out.println("Class\tPrecision\tRecall\t\tF1 Score");
            System.out.println("-----\t---------\t------\t\t--------");
            for (int i = 0; i < numClasses; i++) {
                System.out.printf("%d\t%.4f\t\t%.4f\t\t%.4f\n",
                        i, precision[i], recall[i], f1Score[i]);
            }

            // Print confusion matrix
            System.out.println("\nConfusion Matrix:");
            System.out.println("Predicted class →");
            System.out.print("   ");
            for (int i = 0; i < numClasses; i++) {
                System.out.printf("%5d", i);
            }
            System.out.println("\n   " + "-----".repeat(numClasses));

            for (int i = 0; i < numClasses; i++) {
                System.out.printf("%2d |", i);
                for (int j = 0; j < numClasses; j++) {
                    System.out.printf("%5d", confusionMatrix[i][j]);
                }
                System.out.println();
            }
        }
    }

    public EvaluationMetrics evaluateModel(double[][][] testData, int[] testLabels) {
        int[] predictedLabels = new int[testLabels.length];

        // Get predictions for all test samples
        for (int i = 0; i < testData.length; i++) {
            double[] output = forward(testData[i]);
            predictedLabels[i] = argmax(output);
        }

        return new EvaluationMetrics(numClasses, testLabels, predictedLabels);
    }

    private double categoricalCrossEntropyLoss(double[] output, int target) {
        return -Math.log(Math.max(output[target], 1e-10));
    }

    private int argmax(double[] array) {
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    // MNIST data loading utilities
    public static class MNISTLoader {

        public static double[][][] loadImages(String filepath) throws IOException {
            try (DataInputStream in = new DataInputStream(
                    new GZIPInputStream(new FileInputStream(filepath)))) {
                int magic = in.readInt();
                int numImages = in.readInt();
                int numRows = in.readInt();
                int numCols = in.readInt();

                double[][][] images = new double[numImages][numRows][numCols];

                for (int i = 0; i < numImages; i++) {
                    for (int r = 0; r < numRows; r++) {
                        for (int c = 0; c < numCols; c++) {
                            images[i][r][c] = in.readUnsignedByte() / 255.0;
                        }
                    }
                }

                return images;
            }
        }

        public static int[] loadLabels(String filepath) throws IOException {
            try (DataInputStream in = new DataInputStream(
                    new GZIPInputStream(new FileInputStream(filepath)))) {
                int magic = in.readInt();
                int numLabels = in.readInt();

                int[] labels = new int[numLabels];

                for (int i = 0; i < numLabels; i++) {
                    labels[i] = in.readUnsignedByte();
                }

                return labels;
            }
        }
    }

    // Main method for testing
    public static void main(String[] args) throws IOException {
        // Load MNIST data
        String basePath = "D:\\DATASETS\\classification\\mnist/";
        double[][][] trainImages = MNISTLoader.loadImages(basePath + "train-images-idx3-ubyte.gz");
        int[] trainLabels = MNISTLoader.loadLabels(basePath + "train-labels-idx1-ubyte.gz");
        double[][][] testImages = MNISTLoader.loadImages(basePath + "t10k-images-idx3-ubyte.gz");
        int[] testLabels = MNISTLoader.loadLabels(basePath + "t10k-labels-idx1-ubyte.gz");

        // Create and train model
        List<Integer> spatialLayers = Arrays.asList(2, 4, 8);
        SPIN model = new SPIN(
                28,
                28,
                10,
                spatialLayers,
                0.001,
                Activation.RELU,
                true);

        model.summary();

        // Split training data into train and validation
        int valSize = 10000;
        double[][][] valImages = Arrays.copyOfRange(trainImages, 50000 - valSize, 50000);
        int[] valLabels = Arrays.copyOfRange(trainLabels, 50000 - valSize, 50000);
        double[][][] finalTrainImages = Arrays.copyOfRange(trainImages, 0, 50000 - valSize);
        int[] finalTrainLabels = Arrays.copyOfRange(trainLabels, 0, 50000 - valSize);

        // Train
        model.fit(finalTrainImages, finalTrainLabels, valImages, valLabels, 10, 32);

        // Evaluate on test set
        System.out.println("\nEvaluating model on test set...");
        EvaluationMetrics metrics = model.evaluateModel(testImages, testLabels);
        metrics.printResults();
    }
}
