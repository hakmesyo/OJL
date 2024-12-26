/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.machine_learning.mlp.enums.EActivationType;
import jazari.machine_learning.data_loader.DataLoader;
import jazari.machine_learning.mlp.ModelTrainer;
import jazari.machine_learning.mlp.MultiLayerPerceptron;
import jazari.machine_learning.mlp.enums.EOptimizerType;
import jazari.machine_learning.mlp.enums.EProblemType;

/**
 *
 * @author cezerilab
 */
public class TestMLP {
    public static void main(String[] args) {
        performIrisDataSet();
    }

    private static void performIrisDataSet() {
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

        System.out.println("\nFinal performance:");
        trainer.evaluate();
    }
}
