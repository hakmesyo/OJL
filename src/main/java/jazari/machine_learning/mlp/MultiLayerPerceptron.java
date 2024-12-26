/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.mlp;

/**
 *
 * @author cezerilab
 */
import jazari.machine_learning.mlp.enums.EActivationType;
import jazari.machine_learning.mlp.enums.EProblemType;
import jazari.machine_learning.mlp.enums.ELossFunction;
import jazari.machine_learning.mlp.enums.EOptimizerType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

// Abstract base Layer class
abstract class Layer implements Serializable {

    protected double[] output;
    protected double[] input;
    protected double[] delta;
    protected EActivationType activation;

    public abstract double[] forward(double[] input, boolean isTraining);

    public abstract void updateWeights(double learningRate, double momentum, EOptimizerType optimizer,
            double l1Lambda, double l2Lambda,
            double beta1, double beta2, double epsilon);

    public abstract double[] backward(double[] nextLayerDelta, double[][] nextLayerWeights);

    public double[] getOutput() {
        return output;
    }

    public double[] getInput() {
        return input;
    }

    public double[] getDelta() {
        return delta;
    }

    public void setDelta(double[] delta) {
        this.delta = delta;
    }

    public EActivationType getActivation() {
        return activation;
    }

    public abstract int getInputSize();

    public abstract int getOutputSize();

    public abstract double[][] getWeights();
}

class DenseLayer extends Layer {

    private double[][] weights;
    private double[] biases;
    private double dropoutRate;
    private double[][] momentumWeights;
    private double[] momentumBiases;
    private double[][] adamM;
    private double[][] adamV;
    private double[] adamBiasM;
    private double[] adamBiasV;
    private int timestep;
    private int inputSize;
    private int outputSize;

    public DenseLayer(int inputSize, int outputSize, EActivationType activation, double dropoutRate) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.weights = new double[inputSize][outputSize];
        this.biases = new double[outputSize];
        this.output = new double[outputSize];
        this.input = new double[inputSize];
        this.delta = new double[outputSize];
        this.activation = activation;
        this.dropoutRate = dropoutRate;

        // Optimizer arrays
        this.momentumWeights = new double[inputSize][outputSize];
        this.momentumBiases = new double[outputSize];
        this.adamM = new double[inputSize][outputSize];
        this.adamV = new double[inputSize][outputSize];
        this.adamBiasM = new double[outputSize];
        this.adamBiasV = new double[outputSize];
        this.timestep = 0;

//        // He initialization
//        Random random = new Random();
//        double scale = Math.sqrt(2.0 / inputSize);
//        for (int i = 0; i < inputSize; i++) {
//            for (int j = 0; j < outputSize; j++) {
//                weights[i][j] = random.nextGaussian() * scale;
//            }
//        }
//         He initialization yerine Xavier/Glorot initialization kullanalım
        double scale = Math.sqrt(2.0 / (inputSize + outputSize));  // He yerine Xavier
        Random random = new Random();
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weights[i][j] = random.nextGaussian() * scale;
            }
        }

//        double scale = Math.sqrt(1.0 / inputSize);  // Daha küçük initialization
//        Random random = new Random();
//        for (int i = 0; i < inputSize; i++) {
//            for (int j = 0; j < outputSize; j++) {
//                weights[i][j] = random.nextGaussian() * scale * 0.1;  // Ek scaling factor
//            }
//        }
    }

    @Override
    public double[] backward(double[] nextLayerDelta, double[][] nextLayerWeights) {
        if (nextLayerDelta == null || nextLayerWeights == null) {
            return delta;
        }

        double[] newDelta = new double[outputSize];

        for (int i = 0; i < newDelta.length; i++) {
            double sum = 0;
            for (int j = 0; j < nextLayerDelta.length; j++) {
                sum += nextLayerDelta[j] * nextLayerWeights[i][j];
            }
            newDelta[i] = sum * derivativeActivation(output[i]);
        }

        this.delta = newDelta;
        return newDelta;
    }

    private void updateAdam(double learningRate, double l1Lambda, double l2Lambda,
            double beta1, double beta2, double epsilon) {
        timestep++;

        // Weight updates
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                double gradient = delta[j] * input[i];

                // Regularization
                if (l1Lambda > 0) {
                    gradient += l1Lambda * Math.signum(weights[i][j]);
                }
                if (l2Lambda > 0) {
                    gradient += l2Lambda * weights[i][j];
                }

                // Adam update
                adamM[i][j] = beta1 * adamM[i][j] + (1 - beta1) * gradient;
                adamV[i][j] = beta2 * adamV[i][j] + (1 - beta2) * gradient * gradient;

                double mHat = adamM[i][j] / (1 - Math.pow(beta1, timestep));
                double vHat = adamV[i][j] / (1 - Math.pow(beta2, timestep));

                weights[i][j] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon);
            }
        }

        // Bias updates
        for (int j = 0; j < outputSize; j++) {
            adamBiasM[j] = beta1 * adamBiasM[j] + (1 - beta1) * delta[j];
            adamBiasV[j] = beta2 * adamBiasV[j] + (1 - beta2) * delta[j] * delta[j];

            double mHat = adamBiasM[j] / (1 - Math.pow(beta1, timestep));
            double vHat = adamBiasV[j] / (1 - Math.pow(beta2, timestep));

            biases[j] -= learningRate * mHat / (Math.sqrt(vHat) + epsilon);
        }
    }

    @Override
    public double[] forward(double[] input, boolean isTraining) {
        if (input.length != this.input.length) {
            throw new IllegalArgumentException(
                    "Input size mismatch. Expected: " + this.input.length
                    + ", Got: " + input.length);
        }

        this.input = input.clone();

        // Matrix multiplication and bias
        for (int i = 0; i < output.length; i++) {
            output[i] = biases[i];
            for (int j = 0; j < input.length; j++) {
                output[i] += input[j] * weights[j][i];
            }
        }

        applyActivation();

        if (isTraining && dropoutRate > 0) {
            applyDropout();
        }

        return output;
    }

    @Override
    public void updateWeights(double learningRate, double momentum, EOptimizerType optimizer,
            double l1Lambda, double l2Lambda,
            double beta1, double beta2, double epsilon) {
        switch (optimizer) {
            case ADAM:
                updateAdam(learningRate, l1Lambda, l2Lambda, beta1, beta2, epsilon);
                break;
            case SGD:
                updateSGD(learningRate, momentum, l1Lambda, l2Lambda);
                break;
            case RMSPROP:
                updateRMSprop(learningRate, l1Lambda, l2Lambda, beta2, epsilon);
                break;
        }
    }

    private void applyActivation() {
        switch (activation) {
            case RELU:
                for (int i = 0; i < output.length; i++) {
                    output[i] = Math.max(0, output[i]);
                }
                break;
            case SIGMOID:
                for (int i = 0; i < output.length; i++) {
                    output[i] = 1.0 / (1.0 + Math.exp(-output[i]));
                }
                break;
            case TANH:
                for (int i = 0; i < output.length; i++) {
                    output[i] = Math.tanh(output[i]);
                }
                break;
            case SOFTMAX:
                double sum = 0;
                double max = Arrays.stream(output).max().getAsDouble();
                for (int i = 0; i < output.length; i++) {
                    output[i] = Math.exp(output[i] - max);
                    sum += output[i];
                }
                for (int i = 0; i < output.length; i++) {
                    output[i] /= sum;
                }
                break;
            case LINEAR:
                // Linear için değişiklik yok
                break;
        }
    }

    private void applyDropout() {
        Random random = new Random();
        for (int i = 0; i < output.length; i++) {
            if (random.nextDouble() < dropoutRate) {
                output[i] = 0;
            } else {
                output[i] /= (1.0 - dropoutRate);
            }
        }
    }

    private void updateSGD(double learningRate, double momentum, double l1Lambda, double l2Lambda) {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                double gradient = delta[j] * input[i];

                // L1 ve L2 regularization
                if (l1Lambda > 0) {
                    gradient += l1Lambda * Math.signum(weights[i][j]);
                }
                if (l2Lambda > 0) {
                    gradient += l2Lambda * weights[i][j];
                }

                // Momentum update
                momentumWeights[i][j] = momentum * momentumWeights[i][j] - learningRate * gradient;
                weights[i][j] += momentumWeights[i][j];
            }
        }

        // Bias güncelleme
        for (int i = 0; i < biases.length; i++) {
            momentumBiases[i] = momentum * momentumBiases[i] - learningRate * delta[i];
            biases[i] += momentumBiases[i];
        }
    }

    private void updateRMSprop(double learningRate, double l1Lambda, double l2Lambda,
            double beta2, double epsilon) {
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                double gradient = delta[j] * input[i];

                // L1 ve L2 regularization
                if (l1Lambda > 0) {
                    gradient += l1Lambda * Math.signum(weights[i][j]);
                }
                if (l2Lambda > 0) {
                    gradient += l2Lambda * weights[i][j];
                }

                // RMSprop update
                adamV[i][j] = beta2 * adamV[i][j] + (1 - beta2) * gradient * gradient;
                weights[i][j] -= learningRate * gradient / (Math.sqrt(adamV[i][j]) + epsilon);
            }
        }

        // Bias güncelleme
        for (int i = 0; i < biases.length; i++) {
            adamBiasV[i] = beta2 * adamBiasV[i] + (1 - beta2) * delta[i] * delta[i];
            biases[i] -= learningRate * delta[i] / (Math.sqrt(adamBiasV[i]) + epsilon);
        }
    }

    private double derivativeActivation(double output) {
        switch (activation) {
            case RELU:
                return output > 0 ? 1 : 0;
            case SIGMOID:
                return output * (1 - output);
            case TANH:
                return 1 - output * output;
            case LINEAR:
                return 1;
            case SOFTMAX:
                return output * (1 - output);
            default:
                return 0;
        }
    }

    @Override
    public int getInputSize() {
        return input.length;
    }

    @Override
    public int getOutputSize() {
        return output.length;
    }

    @Override
    public double[][] getWeights() {
        return weights;
    }
}

class BatchNormLayer extends Layer {

    private double[] gamma;      // Scale parameter
    private double[] beta;       // Shift parameter
    private double[] mean;       // Running mean
    private double[] variance;   // Running variance
    private double epsilon;      // Small constant for numerical stability
    private double momentum;     // Momentum for running statistics

    // Forward pass için geçici değişkenler
    private double[] xNormalized;
    private double[] xCentered;
    private double[] batchMean;
    private double[] batchVar;
    private double[] stdDev;

    public BatchNormLayer(int size) {
        this.gamma = new double[size];
        this.beta = new double[size];
        this.mean = new double[size];
        this.variance = new double[size];
        this.output = new double[size];
        this.input = new double[size];
        this.delta = new double[size];
        this.epsilon = 1e-5;
        this.momentum = 0.9;
        this.activation = EActivationType.LINEAR;

        this.xNormalized = new double[size];
        this.xCentered = new double[size];
        this.batchMean = new double[size];
        this.batchVar = new double[size];
        this.stdDev = new double[size];

        // gamma'yı 1 ile, beta'yı 0 ile başlat
        Arrays.fill(gamma, 1.0);
        Arrays.fill(beta, 0.0);
    }

    @Override
    public double[] forward(double[] input, boolean isTraining) {
        this.input = input.clone();

        if (isTraining) {
            // Batch istatistiklerini hesapla
            calculateBatchStatistics(input);

            // Running statistics güncelle
            updateRunningStatistics();

            // Normalize
            normalize(isTraining);
        } else {
            // Test modunda running statistics kullan
            normalize(isTraining);
        }

        // Scale and shift
        for (int i = 0; i < output.length; i++) {
            output[i] = gamma[i] * xNormalized[i] + beta[i];
        }

        return output;
    }

    private void calculateBatchStatistics(double[] input) {
        // Mean hesaplama
        Arrays.fill(batchMean, 0.0);
        for (int i = 0; i < input.length; i++) {
            batchMean[i] = input[i];
        }

        // Variance hesaplama
        Arrays.fill(batchVar, 0.0);
        for (int i = 0; i < input.length; i++) {
            double diff = input[i] - batchMean[i];
            batchVar[i] += diff * diff;
        }
    }

    private void updateRunningStatistics() {
        for (int i = 0; i < mean.length; i++) {
            mean[i] = momentum * mean[i] + (1 - momentum) * batchMean[i];
            variance[i] = momentum * variance[i] + (1 - momentum) * batchVar[i];
        }
    }

    private void normalize(boolean isTraining) {
        double[] currentMean = isTraining ? batchMean : mean;
        double[] currentVar = isTraining ? batchVar : variance;

        for (int i = 0; i < input.length; i++) {
            xCentered[i] = input[i] - currentMean[i];
            stdDev[i] = Math.sqrt(currentVar[i] + epsilon);
            xNormalized[i] = xCentered[i] / stdDev[i];
        }
    }

    @Override
    public double[] backward(double[] nextLayerDelta, double[][] nextLayerWeights) {
        double[] gradInput = new double[input.length];

        // Gradient hesaplama
        for (int i = 0; i < gradInput.length; i++) {
            double gradGamma = delta[i] * xNormalized[i];
            double gradBeta = delta[i];

            double gradNorm = delta[i] * gamma[i];
            gradInput[i] = gradNorm / stdDev[i];

            // Gamma ve beta güncelleme
            gamma[i] -= 0.01 * gradGamma;  // Learning rate 0.01 olarak sabit
            beta[i] -= 0.01 * gradBeta;
        }

        return gradInput;
    }

    @Override
    public void updateWeights(double learningRate, double momentum, EOptimizerType optimizer,
            double l1Lambda, double l2Lambda,
            double beta1, double beta2, double epsilon) {
        // BatchNorm layer'ında weights yok, güncelleme gamma ve beta için backward'da yapılıyor
    }

    @Override
    public int getInputSize() {
        return input.length;
    }

    @Override
    public int getOutputSize() {
        return output.length;
    }

    @Override
    public double[][] getWeights() {
        return null; // BatchNorm layer'ında weights yok
    }

    // Getter/Setter metodları
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }

    public double[] getGamma() {
        return gamma;
    }

    public double[] getBeta() {
        return beta;
    }
}

public class MultiLayerPerceptron implements Serializable {

    private List<Layer> layers;
    private int inputSize;
    private EProblemType problemType;
    private double learningRate;
    private EOptimizerType optimizer;
    private double l1Lambda;
    private double l2Lambda;
    private double momentum;
    private double beta1;
    private double beta2;
    private double epsilon;
    private ELossFunction lossFunction;
    private String checkpointDir;
    private double bestLoss;
    private int epochsSinceImprovement;
    private int patience;

    public MultiLayerPerceptron(EProblemType problemType, int inputSize) {
        this.layers = new ArrayList<>();
        this.problemType = problemType;
        this.inputSize = inputSize;
        this.learningRate = 0.001;
        this.optimizer = EOptimizerType.ADAM;
        this.l1Lambda = 0;
        this.l2Lambda = 0;
        this.momentum = 0.9;
        this.beta1 = 0.9;
        this.beta2 = 0.999;
        this.epsilon = 1e-8;
        this.lossFunction = problemType == EProblemType.CLASSIFICATION
                ? ELossFunction.CROSS_ENTROPY : ELossFunction.MSE;
        this.checkpointDir = "checkpoints/";
        this.bestLoss = Double.MAX_VALUE;
        this.epochsSinceImprovement = 0;
        this.patience = 5;

        new File(checkpointDir).mkdirs();
    }

    public void addLayer(int outputSize, EActivationType activation, double dropoutRate) {
        int previousSize = layers.isEmpty() ? inputSize : layers.get(layers.size() - 1).getOutputSize();
        layers.add(new DenseLayer(previousSize, outputSize, activation, dropoutRate));
    }

    public void addBatchNormalization() {
        if (layers.isEmpty()) {
            throw new IllegalStateException("Cannot add batch normalization before adding a layer");
        }
        Layer lastLayer = layers.get(layers.size() - 1);
        layers.add(new BatchNormLayer(lastLayer.getOutputSize()));
    }

    public double[] forward(double[] input, boolean isTraining) {
        double[] current = input;
        for (Layer layer : layers) {
            current = layer.forward(current, isTraining);
        }
        return current;
    }

    public void backward(double[] target) {
        // Son katman için delta hesaplama
        Layer outputLayer = layers.get(layers.size() - 1);
        double[] outputDelta = calculateOutputGradient(outputLayer.getOutput(), target);
        outputLayer.setDelta(outputDelta);

        // Hidden layerlar için backpropagation
        for (int i = layers.size() - 2; i >= 0; i--) {
            Layer currentLayer = layers.get(i);
            Layer nextLayer = layers.get(i + 1);

            double[] delta;
            if (nextLayer instanceof BatchNormLayer) {
                delta = nextLayer.backward(currentLayer.getDelta(), null);
            } else {
                delta = currentLayer.backward(nextLayer.getDelta(), nextLayer.getWeights());
            }
            currentLayer.setDelta(delta);

            // Ağırlık güncellemesi
            currentLayer.updateWeights(learningRate, momentum, optimizer, l1Lambda, l2Lambda,
                    beta1, beta2, epsilon);
        }
    }

    private double[] calculateOutputGradient(double[] output, double[] target) {
        double[] gradient = new double[output.length];

        switch (lossFunction) {
            case CROSS_ENTROPY:
                for (int i = 0; i < gradient.length; i++) {
                    gradient[i] = output[i] - target[i];
                }
                break;
            case MSE:
                for (int i = 0; i < gradient.length; i++) {
                    gradient[i] = 2 * (output[i] - target[i]);
                }
                break;
            case HUBER:
                double delta = 1.0; // Huber loss delta parameter
                for (int i = 0; i < gradient.length; i++) {
                    double diff = output[i] - target[i];
                    gradient[i] = Math.abs(diff) <= delta ? diff : delta * Math.signum(diff);
                }
                break;
        }

        return gradient;
    }

    public double calculateLoss(double[] output, double[] target) {
        double loss = 0;

        switch (lossFunction) {
            case CROSS_ENTROPY:
                for (int i = 0; i < output.length; i++) {
                    loss += -target[i] * Math.log(Math.max(output[i], 1e-15));
                }
                break;
            case MSE:
                for (int i = 0; i < output.length; i++) {
                    loss += Math.pow(output[i] - target[i], 2);
                }
                loss /= output.length;
                break;
            case HUBER:
                double delta = 1.0;
                for (int i = 0; i < output.length; i++) {
                    double diff = Math.abs(output[i] - target[i]);
                    loss += diff <= delta
                            ? 0.5 * diff * diff
                            : delta * diff - 0.5 * delta * delta;
                }
                break;
        }

        // Regularization
        if (l1Lambda > 0 || l2Lambda > 0) {
            for (Layer layer : layers) {
                if (layer instanceof DenseLayer) {
                    double[][] weights = layer.getWeights();
                    for (double[] row : weights) {
                        for (double weight : row) {
                            if (l1Lambda > 0) {
                                loss += l1Lambda * Math.abs(weight);
                            }
                            if (l2Lambda > 0) {
                                loss += 0.5 * l2Lambda * weight * weight;
                            }
                        }
                    }
                }
            }
        }

        return loss;
    }

    public void saveCheckpoint(int epoch, double loss) {
        if (loss < bestLoss) {
            bestLoss = loss;
            epochsSinceImprovement = 0;

            String filename = String.format("%smodel_epoch_%d_loss_%.4f.bin",
                    checkpointDir, epoch, loss);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(this);
            } catch (IOException e) {
                System.err.println("Error saving checkpoint: " + e.getMessage());
            }
        } else {
            epochsSinceImprovement++;
        }
    }

    public static MultiLayerPerceptron loadCheckpoint(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (MultiLayerPerceptron) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading checkpoint: " + e.getMessage());
            return null;
        }
    }

    public boolean shouldEarlyStop() {
        return epochsSinceImprovement >= patience;
    }

    // Setter metodları
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public void setOptimizer(EOptimizerType optimizer) {
        this.optimizer = optimizer;
    }

    public void setRegularization(double l1Lambda, double l2Lambda) {
        this.l1Lambda = l1Lambda;
        this.l2Lambda = l2Lambda;
    }

    public void setLossFunction(ELossFunction lossFunction) {
        this.lossFunction = lossFunction;
    }

    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }

    public void setAdamParameters(double beta1, double beta2, double epsilon) {
        this.beta1 = beta1;
        this.beta2 = beta2;
        this.epsilon = epsilon;
    }

    public void setCheckpointDirectory(String dir) {
        this.checkpointDir = dir;
        new File(dir).mkdirs();
    }

    public void setPatience(int patience) {
        this.patience = patience;
    }

    // Getter metodları
    public EProblemType getProblemType() {
        return problemType;
    }

    public int getOutputSize() {
        return layers.isEmpty() ? 0 : layers.get(layers.size() - 1).getOutputSize();
    }

    public int getInputSize() {
        return inputSize;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    // Model özeti yazdırma
    public void printSummary() {
        System.out.println("\nModel Summary:");
        System.out.println("-------------");
        System.out.println("Problem Type: " + problemType);
        System.out.println("Optimizer: " + optimizer);
        System.out.println("Learning Rate: " + learningRate);
        System.out.println("Loss Function: " + lossFunction);
        System.out.println("L1 Regularization: " + l1Lambda);
        System.out.println("L2 Regularization: " + l2Lambda);
        System.out.println("\nLayers:");

        for (int i = 0; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            System.out.printf("Layer %d:\n", i + 1);
            if (layer instanceof BatchNormLayer) {
                System.out.println("  Type: Batch Normalization");
                System.out.printf("  Size: %d\n", layer.getOutputSize());
            } else {
                System.out.printf("  Type: %s\n",
                        i == 0 ? "Input" : i == layers.size() - 1 ? "Output" : "Hidden");
                System.out.printf("  Input Size: %d\n", layer.getInputSize());
                System.out.printf("  Output Size: %d\n", layer.getOutputSize());
                System.out.printf("  Activation: %s\n", layer.getActivation());
            }
            System.out.println();
        }
    }

    public void summary() {
        System.out.println("Model: MultiLayerPerceptron");
        System.out.println("_________________________________________________________________");
        System.out.printf("%-20s %-20s %-20s%n", "Layer (type)", "Output Shape", "Param #");
        System.out.println("=================================================================");

        int totalParams = 0;
        int layerIndex = 1;
        int prevLayerSize = inputSize;

        // Input layer bilgisi
        System.out.printf("%-20s %-20s %-20d%n",
                "input_layer",
                "[None, " + inputSize + "]",
                0);
        System.out.println("_________________________________________________________________");

        // Her layer için bilgileri yazdır
        for (Layer layer : layers) {
            String layerName;
            int numParams = 0;

            if (layer instanceof BatchNormLayer) {
                layerName = "batch_norm_" + layerIndex;
                numParams = layer.getOutputSize() * 4; // gamma, beta, moving_mean, moving_variance
            } else {
                layerName = "dense_" + layerIndex;
                numParams = (prevLayerSize * layer.getOutputSize()) + layer.getOutputSize(); // weights + biases
                prevLayerSize = layer.getOutputSize();
            }

            System.out.printf("%-20s %-20s %-20d%n",
                    layerName,
                    "[None, " + layer.getOutputSize() + "]",
                    numParams);

            System.out.println("_________________________________________________________________");

            totalParams += numParams;
            layerIndex++;
        }

        // Toplam parametreleri yazdır
        System.out.println("\nTotal params: " + String.format("%,d", totalParams));
        System.out.println("Trainable params: " + String.format("%,d", totalParams));
        System.out.println("Non-trainable params: 0");
        System.out.println("_________________________________________________________________");

        // Optimizer ve loss bilgisi
        System.out.println("\nOptimizer: " + optimizer);
        System.out.println("Loss: " + (problemType == EProblemType.CLASSIFICATION ? "categorical_crossentropy" : "mse"));
        System.out.println("Learning rate: " + learningRate);
    }
}
