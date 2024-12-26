/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.mlp;

import jazari.machine_learning.data_loader.DataLoader;
import jazari.machine_learning.mlp.enums.EActivationType;
import jazari.machine_learning.mlp.enums.EOptimizerType;
import jazari.machine_learning.mlp.enums.EProblemType;

/**
 *
 * @author cezerilab
 */
public class TestSyntheticDataSet {

    public static void main(String[] args) {
        //testClassification();
        testRegression();
    }

    private static void testRegression() {
        // 1. Veri setini oluştur - Regresyon için
        System.out.println("Creating synthetic regression dataset...");
        DataLoader loader = new DataLoader();

        // Regresyon için sentetik veri üret
        loader.generateSyntheticRegressionData(
                600, // toplam örnek sayısı
                5, // input features
                1, // output dimension (regresyon için 1)
                0.1, // noise level (gürültü seviyesi)
                42 // random seed
        );

        // Veri yüklendikten hemen sonra
        System.out.println("\nVisualizing data relationships...");
        loader.visualizeFeatures();  // Her feature için scatter plot
        loader.showCorrelationMatrix();  // Korelasyon matrisi

        // 2. Veriyi böl: %70 train, %15 validation, %15 test
        loader.splitData(0.7, 0.15, 0.15);
        loader.setBatchSize(32);

        // 3. Model oluştur - Regresyon için
        System.out.println("\nCreating regression model...");
        MultiLayerPerceptron mlp = new MultiLayerPerceptron(EProblemType.REGRESSION, 5);

        // Hidden layers
        mlp.addLayer(8, EActivationType.RELU, 0.0);

        // Output layer - Regresyon için Linear aktivasyon
        mlp.addLayer(1, EActivationType.LINEAR, 0);

        // Model parametrelerini ayarla
        mlp.setLearningRate(0.001);
        mlp.setOptimizer(EOptimizerType.ADAM);
        mlp.setRegularization(0.0001, 0.0001);  // L1 ve L2 regularization

        // Model özetini göster
        mlp.summary();

        // 4. Model eğitimi
        System.out.println("\nTraining model...");
        ModelTrainer trainer = new ModelTrainer(mlp, loader);

        // Başlangıç performansını göster
        System.out.println("\nInitial performance:");
        trainer.evaluate();
        trainer.showPredictionPlot();
        trainer.showResidualPlot();

        // Eğitim
        System.out.println("\nTraining started...");
        trainer.train(50);
        System.out.println("Waiting for training to complete...");
        trainer.waitForTrainingComplete();
        System.out.println("Training completed!");

        // Final değerlendirme
        if (trainer.isTrainingComplete()) {
            System.out.println("\nFinal performance:");
            trainer.evaluate();
            trainer.showPredictionPlot();
            trainer.showResidualPlot();
        }
    }

    private static void testClassification() {
        // 1. Veri setini oluştur
        System.out.println("Creating synthetic dataset...");
        DataLoader loader = new DataLoader();

        // Her sınıf için 200 örnek, 2 özellik, 3 sınıf oluştur
        loader.generateSyntheticClassificationData(
                200, // samples per class
                5, // features
                3, // classes
                35, // mean_scale
                5, // var_scale
                42 // random seed
        );
        //Verileri analiz yapabilmek için görselleştir
        loader.showTSNE();
        loader.showCorrelationMatrix();

        // 2. Veriyi böl: %70 train, %15 validation, %15 test
        loader.splitData(0.7, 0.15, 0.15);
        loader.setBatchSize(32);

        // 3. Model oluştur
        System.out.println("\nCreating model...");
        MultiLayerPerceptron mlp = new MultiLayerPerceptron(EProblemType.CLASSIFICATION, 5);

        // Hidden layers
        mlp.addLayer(10, EActivationType.RELU, 0.0);
        mlp.addLayer(5, EActivationType.RELU, 0.0);

        // Output layer
        mlp.addLayer(3, EActivationType.SOFTMAX, 0);

        // Model parametrelerini ayarla
        mlp.setLearningRate(0.001);
        mlp.setOptimizer(EOptimizerType.ADAM);
        mlp.setRegularization(0.0001, 0.0001);

        // Model özetini göster
        mlp.summary();

        // 4. Model eğitimi
        System.out.println("\nTraining model...");
        ModelTrainer trainer = new ModelTrainer(mlp, loader);

        // Başlangıç performansını göster
        System.out.println("\nInitial performance:");
        trainer.evaluate();
        trainer.showConfusionMatrix();

        // Eğitim
        System.out.println("\nTraining started...");
        trainer.train(50);
        System.out.println("Waiting for training to complete...");
        trainer.waitForTrainingComplete();
        System.out.println("Training completed!");

        // Final değerlendirme
        if (trainer.isTrainingComplete()) {
            System.out.println("\nFinal performance:");
            trainer.evaluate();
            trainer.showConfusionMatrix();
            trainer.showROCCurve();
        }
    }
}
