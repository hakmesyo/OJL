package jazari.machine_learning.mlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jazari.machine_learning.analysis.confusion_matrix.ConfusionMatrixPlot;
import jazari.machine_learning.analysis.learning_curves.LearningCurvesPlot;
import org.nd4j.common.primitives.AtomicDouble;

public class ModelTrainer {

    private MultiLayerPerceptron model;
    private DataLoader dataLoader;

    // Eğitim metriklerini tutmak için listeler
    private List<Double> trainLosses;
    private List<Double> trainAccuracies;
    private List<Double> valLosses;
    private List<Double> valAccuracies;

    public ModelTrainer(MultiLayerPerceptron model, DataLoader loader) {
        this.model = model;
        this.dataLoader = loader;
        this.trainLosses = new ArrayList<>();
        this.trainAccuracies = new ArrayList<>();
        this.valLosses = new ArrayList<>();
        this.valAccuracies = new ArrayList<>();
    }

    // Normal seri çalışan train metodu
    public void train(int epochs) {
        // Metrikleri temizle
        trainLosses.clear();
        trainAccuracies.clear();
        valLosses.clear();
        valAccuracies.clear();

        for (int epoch = 1; epoch <= epochs; epoch++) {
            List<Batch> batches = dataLoader.getBatches();
            double totalLoss = 0;
            int correct = 0;
            int total = 0;

            // Train batches
            for (Batch batch : batches) {
                for (int i = 0; i < batch.features.length; i++) {
                    double[] output = model.forward(batch.features[i], true);
                    if (argmax(output) == argmax(batch.labels[i])) {
                        correct++;
                    }
                    total++;
                    totalLoss += model.calculateLoss(output, batch.labels[i]);
                    model.backward(batch.labels[i]);
                }
            }

            // Validation
            List<Batch> valBatches = dataLoader.getValidationBatches();
            double valLoss = 0;
            int valCorrect = 0;
            int valTotal = 0;

            for (Batch batch : valBatches) {
                for (int i = 0; i < batch.features.length; i++) {
                    double[] output = model.forward(batch.features[i], false);
                    if (argmax(output) == argmax(batch.labels[i])) {
                        valCorrect++;
                    }
                    valTotal++;
                    valLoss += model.calculateLoss(output, batch.labels[i]);
                }
            }

            // Metrikleri kaydet
            double epochTrainLoss = totalLoss / total;
            double epochTrainAcc = (double) correct / total;
            double epochValLoss = valLoss / valTotal;
            double epochValAcc = (double) valCorrect / valTotal;

            trainLosses.add(epochTrainLoss);
            trainAccuracies.add(epochTrainAcc);
            valLosses.add(epochValLoss);
            valAccuracies.add(epochValAcc);

            // Metrikleri yazdır
            System.out.printf("Epoch %d/%d - Loss: %.4f - Accuracy: %.4f - Val Loss: %.4f - Val Accuracy: %.4f%n",
                    epoch, epochs, epochTrainLoss, epochTrainAcc, epochValLoss, epochValAcc);
        }
    }

    public void evaluate() {
        List<Batch> testBatches = dataLoader.getTestBatches();
        double testLoss = 0;
        int correct = 0;
        int total = 0;

        for (Batch batch : testBatches) {
            for (int i = 0; i < batch.features.length; i++) {
                double[] output = model.forward(batch.features[i], false);
                if (argmax(output) == argmax(batch.labels[i])) {
                    correct++;
                }
                total++;
                testLoss += model.calculateLoss(output, batch.labels[i]);
            }
        }

        double accuracy = (double) correct / total;
        testLoss /= total;

        System.out.println("\nTest Results:");
        System.out.printf("Loss: %.4f - Accuracy: %.4f%n", testLoss, accuracy);

        // Confusion Matrix
        printConfusionMatrix(testBatches);
        plotConfusionMatrix(testBatches);

        // Öğrenme eğrileri
        if (!trainLosses.isEmpty()) {  // Eğer eğitim yapıldıysa
            SwingUtilities.invokeLater(() -> {
                LearningCurvesPlot learningPlot = new LearningCurvesPlot(
                        trainLosses, valLosses, trainAccuracies, valAccuracies);
                learningPlot.setVisible(true);
            });
        }
    }

    // Paralel çalışan train metodu
    public void trainParallel(int epochs) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            for (int epoch = 1; epoch <= epochs; epoch++) {
                final int currentEpoch = epoch;
                List<Batch> batches = dataLoader.getBatches();
                AtomicDouble totalLoss = new AtomicDouble(0);
                AtomicInteger correct = new AtomicInteger(0);
                AtomicInteger total = new AtomicInteger(0);

                // Batch'leri paralel işle
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (Batch batch : batches) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        for (int i = 0; i < batch.features.length; i++) {
                            double[] output = model.forward(batch.features[i], true);

                            if (argmax(output) == argmax(batch.labels[i])) {
                                correct.incrementAndGet();
                            }
                            total.incrementAndGet();

                            totalLoss.addAndGet(model.calculateLoss(output, batch.labels[i]));
                            model.backward(batch.labels[i]);
                        }
                    }, executor);

                    futures.add(future);
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                // Validation işlemleri (validation seri çalışacak)
                List<Batch> valBatches = dataLoader.getValidationBatches();
                double valLoss = 0;
                int valCorrect = 0;
                int valTotal = 0;

                for (Batch batch : valBatches) {
                    for (int i = 0; i < batch.features.length; i++) {
                        double[] output = model.forward(batch.features[i], false);
                        if (argmax(output) == argmax(batch.labels[i])) {
                            valCorrect++;
                        }
                        valTotal++;
                        valLoss += model.calculateLoss(output, batch.labels[i]);
                    }
                }

                double loss = totalLoss.get() / total.get();
                double accuracy = (double) correct.get() / total.get();
                double valAccuracy = (double) valCorrect / valTotal;
                valLoss /= valTotal;

                System.out.printf("Epoch %d/%d - Loss: %.4f - Accuracy: %.4f - Val Loss: %.4f - Val Accuracy: %.4f%n",
                        currentEpoch, epochs, loss, accuracy, valLoss, valAccuracy);
            }
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ModelTrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    private int[][] calculateConfusionMatrix(List<Batch> batches) {
        int numClasses = model.getOutputSize();
        int[][] matrix = new int[numClasses][numClasses];

        for (Batch batch : batches) {
            for (int i = 0; i < batch.features.length; i++) {
                double[] output = model.forward(batch.features[i], false);
                int predicted = argmax(output);
                int actual = argmax(batch.labels[i]);
                matrix[actual][predicted]++;
            }
        }
        return matrix;
    }

    private void printConfusionMatrix(List<Batch> batches) {
        int[][] matrix = calculateConfusionMatrix(batches);
        int numClasses = model.getOutputSize();

        // Sınıf isimlerini al
        String[] classNames = getClassNames();

        System.out.println("\nConfusion Matrix:");
        String headerLine = "-".repeat(14 + 12 * numClasses);
        System.out.println(headerLine);

        // Header
        System.out.print("  Actual\\Pred |");
        for (int i = 0; i < numClasses; i++) {
            System.out.printf("%12s", classNames[i]);
        }
        System.out.println("\n" + headerLine);

        // Matrix değerleri
        for (int i = 0; i < numClasses; i++) {
            System.out.printf("%12s |", classNames[i]);
            for (int j = 0; j < numClasses; j++) {
                System.out.printf("%12d", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println(headerLine);
    }

    public void plotConfusionMatrix(List<Batch> batches) {
        int[][] matrix = calculateConfusionMatrix(batches);
        String[] classNames = getClassNames();

        SwingUtilities.invokeLater(() -> {
            ConfusionMatrixPlot plot = new ConfusionMatrixPlot(matrix, classNames);
            plot.setSize(700, 700);
            plot.setLocationRelativeTo(null);
            plot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            plot.setVisible(true);
        });
    }

    private String[] getClassNames() {
        Map<String, Integer> classMap = dataLoader.getClassMap();
        String[] classNames = new String[model.getOutputSize()];

        // Eğer classMap boş değilse, sınıf isimlerini kullan
        if (!classMap.isEmpty()) {
            // Map'i ters çevir (index -> class name)
            for (Map.Entry<String, Integer> entry : classMap.entrySet()) {
                classNames[entry.getValue()] = entry.getKey();
            }
        } else {
            // Eğer classMap boşsa, sayısal indeksleri kullan
            for (int i = 0; i < classNames.length; i++) {
                classNames[i] = String.valueOf(i);
            }
        }

        return classNames;
    }

    private int argmax(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
