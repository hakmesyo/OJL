/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.mlp;

import jazari.machine_learning.data_loader.DataLoader;
import jazari.machine_learning.mlp.enums.EActivationType;
import jazari.machine_learning.mlp.enums.EProblemType;
import jazari.machine_learning.mlp.enums.EOptimizerType;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestMLP {

    public static void main(String[] args) {
//        performSyntheticDataSet();
//        performIris();
        performMNIST();
    }
    
    private static void performSyntheticDataSet() {
        CMatrix cm=CMatrix.getInstance()
                .make_blobs(100, 5, 3)
                .tsne()
                ;
    }
    
    private static void performIris() {
        DataLoader loader = new DataLoader();
        loader.loadCSV("dataset/iris.csv", 4, true);

        loader.splitData(0.7, 0.15, 0.15);
        loader.setBatchSize(4);  // Daha da küçük batch size

        System.out.println("Class mapping: " + loader.getClassMap());

        MultiLayerPerceptron mlp = new MultiLayerPerceptron(EProblemType.CLASSIFICATION, 4);

        // Daha geniş tek layer
        mlp.addLayer(21, EActivationType.RELU, 0.0);
        mlp.addLayer(3, EActivationType.SOFTMAX, 0);

        // Farklı hiperparametreler
        mlp.setLearningRate(0.005);  // Daha küçük learning rate
        mlp.setOptimizer(EOptimizerType.ADAM);
        mlp.setRegularization(0.0001, 0.0001);  // Çok hafif regularization
        mlp.setAdamParameters(0.9, 0.999, 1e-8);

        // Veri setini kontrol edelim
        System.out.println("\nData Distribution:");
        loader.printDataSummary();  // Bu metodu DataLoader'a eklememiz gerekecek

        mlp.summary();

        ModelTrainer trainer = new ModelTrainer(mlp, loader);
        System.out.println("\nInitial performance:");
        trainer.evaluate();

        System.out.println("\nTraining started...");
        trainer.train(50);  // Daha az epoch
        trainer.waitForTrainingComplete();
        

        System.out.println("\nFinal performance:");
        trainer.evaluate();
        trainer.showConfusionMatrix();
        trainer.showROCCurve();
    }

    private static void performMNIST() {
        try {
            DataLoader loader = new DataLoader();
            loader.loadMNIST("dataset/mnist/train-images-idx3-ubyte.gz",
                    "dataset/mnist/train-labels-idx1-ubyte.gz");

            // Veri kontrolü
            loader.printDataSummary();

            loader.splitData(0.7, 0.15, 0.15);
            loader.setBatchSize(64);

            System.out.println("Class mapping: 0-9 digits");

            int inputFeatures = 784;   // 28x28 = 784 piksel
            int numClasses = 10;       // 0-9 rakamları
            int hiddenSize1 = 128;     // İlk hidden layer
            int hiddenSize2 = 64;      // İkinci hidden layer
            int hiddenSize = 32;     // İlk hidden layer

            System.out.println("Input features: " + inputFeatures);
            System.out.println("First hidden layer size: " + hiddenSize1);
            System.out.println("Second hidden layer size: " + hiddenSize2);
            System.out.println("Number of classes: " + numClasses);

//            MultiLayerPerceptron mlp = new MultiLayerPerceptron(ProblemType.CLASSIFICATION, inputFeatures);
//            mlp.addLayer(hiddenSize1, ActivationType.RELU, 0.3);
//            mlp.addBatchNormalization();
//            mlp.addLayer(hiddenSize2, ActivationType.RELU, 0.3);
//            mlp.addBatchNormalization();
//            mlp.addLayer(hiddenSize, ActivationType.RELU, 0.3);
//            mlp.addBatchNormalization();
//            mlp.addLayer(numClasses, ActivationType.SOFTMAX, 0);
//            MultiLayerPerceptron mlp = new MultiLayerPerceptron(ProblemType.CLASSIFICATION, inputFeatures);
//            mlp.addLayer(128, ActivationType.RELU, 0.2);
//            mlp.addBatchNormalization();
//            mlp.addLayer(64, ActivationType.RELU, 0.2);
//            mlp.addBatchNormalization();
//            mlp.addLayer(numClasses, ActivationType.SOFTMAX, 0);
//
//            mlp.setLearningRate(0.01);
//            mlp.setOptimizer(OptimizerType.ADAM);
//            mlp.setRegularization(0.0, 0.0); 
//            mlp.setAdamParameters(0.9, 0.999, 1e-8);
            MultiLayerPerceptron mlp = new MultiLayerPerceptron(EProblemType.CLASSIFICATION, inputFeatures);
            mlp.addLayer(32, EActivationType.RELU, 0.0);  // Dropout'u kaldırdık
            mlp.addLayer(numClasses, EActivationType.SOFTMAX, 0);

            mlp.setLearningRate(0.001);
            mlp.setOptimizer(EOptimizerType.ADAM);
            mlp.setRegularization(0.0, 0.0);  // Regularization yok
            mlp.setAdamParameters(0.9, 0.999, 1e-8);

            mlp.summary();

            ModelTrainer trainer = new ModelTrainer(mlp, loader);

            System.out.println("\nInitial performance:");
            trainer.evaluate();

            System.out.println("\nTraining started...");
            trainer.train(50);
            trainer.waitForTrainingComplete();

            System.out.println("\nFinal performance:");
            trainer.evaluate();

        } catch (Exception e) {
            System.err.println("Error occurred:");
            e.printStackTrace();
        }
    }


}
