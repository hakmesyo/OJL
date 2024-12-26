package jazari.machine_learning.mlp;

import jazari.machine_learning.data_loader.Batch;
import jazari.machine_learning.data_loader.DataLoader;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
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
import jazari.machine_learning.analysis.prediction_plot.PredictionPlot;
import jazari.machine_learning.analysis.residual_plot.ResidualPlot;
import jazari.machine_learning.analysis.roc_curve.ROCCurvePlot;
import jazari.machine_learning.mlp.enums.EProblemType;
import org.nd4j.common.primitives.AtomicDouble;

public class ModelTrainer {

    private MultiLayerPerceptron model;
    private DataLoader dataLoader;
    private List<Double> trainLosses;
    private List<Double> trainAccuracies;
    private List<Double> valLosses;
    private List<Double> valAccuracies;

    // Learning Curves için yeni field'lar
    private JFrame learningCurvesFrame;
    private LearningCurvesPlot learningPlot;

    private Thread trainingThread;  // Thread'i sınıf seviyesinde tanımla
    private volatile boolean isTrainingComplete = false;  // Eğitim durumunu takip et
    private CompletableFuture<Void> trainingFuture;

    public ModelTrainer(MultiLayerPerceptron model, DataLoader loader) {
        this.model = model;
        this.dataLoader = loader;
        this.trainLosses = new ArrayList<>();
        this.trainAccuracies = new ArrayList<>();
        this.valLosses = new ArrayList<>();
        this.valAccuracies = new ArrayList<>();

        // Learning Curves frame'ini başlangıçta oluştur
        initLearningCurvesFrame();
    }

    private void initLearningCurvesFrame() {
        SwingUtilities.invokeLater(() -> {
            learningCurvesFrame = new JFrame("Learning Curves");
            learningPlot = new LearningCurvesPlot(
                    trainLosses, valLosses, trainAccuracies, valAccuracies
            );
            learningCurvesFrame.add(learningPlot.getContentPane());
            learningCurvesFrame.setSize(1000, 800);
            learningCurvesFrame.setMinimumSize(new Dimension(800, 600));
            learningCurvesFrame.setLocationRelativeTo(null);
            learningCurvesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        });
    }

    public void train(int epochs) {
        // Metrikleri temizle
        trainLosses.clear();
        trainAccuracies.clear();
        valLosses.clear();
        valAccuracies.clear();
        isTrainingComplete = false;

        // Learning Curves frame'ini baştan göster
        SwingUtilities.invokeLater(() -> {
            if (!learningCurvesFrame.isVisible()) {
                learningCurvesFrame.setVisible(true);
            }
        });

        // Training future'ı oluştur
        trainingFuture = CompletableFuture.runAsync(() -> {
            try {
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

                    // Her epoch sonunda öğrenme eğrilerini güncelle
                    final int currentEpoch = epoch;
                    SwingUtilities.invokeLater(() -> {
                        // GUI güncellemesi
                        paintLearningCurves();

                        // Metrikleri yazdır
                        System.out.printf("Epoch %d/%d - Loss: %.4f - Accuracy: %.4f - Val Loss: %.4f - Val Accuracy: %.4f%n",
                                currentEpoch, epochs, epochTrainLoss, epochTrainAcc, epochValLoss, epochValAcc);
                    });

                    // Küçük bir gecikme ekle (GUI'nin güncellenmesi için)
                    try {
                        Thread.sleep(50);  // 50ms bekle
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                isTrainingComplete = true;

            } catch (Exception e) {
                System.err.println("Training error: " + e.getMessage());
                e.printStackTrace();
                isTrainingComplete = false;
                throw new CompletionException(e);
            }
        });
    }

    public void waitForTrainingComplete() {
        if (trainingFuture != null) {
            try {
                trainingFuture.get(); // Future'ın tamamlanmasını bekle
                while (!isTrainingComplete) {
                    Thread.sleep(100); // Eğitimin gerçekten bitmesini bekle
                }
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                System.err.println("Error waiting for training: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public boolean isTrainingComplete() {
        return isTrainingComplete;
    }

    // GUI güncellemesi için yeni metod
    private void paintLearningCurves() {
        if (!learningCurvesFrame.isVisible()) {
            learningCurvesFrame.setVisible(true);
        }
        learningPlot.updateCurves(trainLosses, valLosses, trainAccuracies, valAccuracies);
        learningPlot.getContentPane().repaint();
    }

    public void evaluate() {
        if (model.getProblemType().equals(EProblemType.REGRESSION)) {
            evaluateRegression();
        } else {
            evaluateClassification();  // Mevcut classification evaluate
        }
    }

    private void evaluateRegression() {
        List<Batch> testBatches = dataLoader.getTestBatches();
        double mse = 0;
        double mae = 0;
        double r2_numerator = 0;
        double r2_denominator = 0;
        double mean_actual = 0;
        int total = 0;

        // İlk geçiş: ortalama hesapla
        for (Batch batch : testBatches) {
            for (int i = 0; i < batch.features.length; i++) {
                mean_actual += batch.labels[i][0];
                total++;
            }
        }
        mean_actual /= total;

        // İkinci geçiş: metrikleri hesapla
        for (Batch batch : testBatches) {
            for (int i = 0; i < batch.features.length; i++) {
                double[] output = model.forward(batch.features[i], false);
                double actual = batch.labels[i][0];
                double predicted = output[0];

                // MSE
                double error = actual - predicted;
                mse += error * error;

                // MAE
                mae += Math.abs(error);

                // R2 score
                r2_numerator += error * error;
                r2_denominator += Math.pow(actual - mean_actual, 2);
            }
        }

        mse /= total;
        mae /= total;
        double r2 = 1 - (r2_numerator / r2_denominator);

        System.out.println("\nRegression Metrics:");
        System.out.println("==========================================");
        System.out.printf("Mean Squared Error (MSE): %.6f%n", mse);
        System.out.printf("Mean Absolute Error (MAE): %.6f%n", mae);
        System.out.printf("R² Score: %.6f%n", r2);
    }

    public void evaluateClassification() {
        List<Batch> testBatches = dataLoader.getTestBatches();
        double testLoss = 0;
        int correct = 0;
        int total = 0;

        // Her sınıf için TP, FP, FN sayılarını tut
        int numClasses = model.getOutputSize();
        int[] truePositives = new int[numClasses];
        int[] falsePositives = new int[numClasses];
        int[] falseNegatives = new int[numClasses];

        for (Batch batch : testBatches) {
            for (int i = 0; i < batch.features.length; i++) {
                double[] output = model.forward(batch.features[i], false);
                int predicted = argmax(output);
                int actual = argmax(batch.labels[i]);

                if (predicted == actual) {
                    correct++;
                    truePositives[actual]++;
                } else {
                    falsePositives[predicted]++;
                    falseNegatives[actual]++;
                }

                total++;
                testLoss += model.calculateLoss(output, batch.labels[i]);
            }
        }

        double accuracy = (double) correct / total;
        testLoss /= total;

        // Metrikleri yazdır
        System.out.println("\nTest Results:");
        System.out.println("==========================================");
        System.out.printf("Loss: %.4f - Accuracy: %.4f%n", testLoss, accuracy);

        // Her sınıf için metrikleri hesapla ve yazdır
        String[] classNames = getClassNames();
        System.out.println("\nDetailed Metrics per Class:");
        System.out.println("==========================================");
        System.out.printf("%-15s %-12s %-12s %-12s %-12s%n",
                "Class", "Precision", "Recall", "F1-Score", "Support");

        double macroF1 = 0;
        double macroPrecision = 0;
        double macroRecall = 0;
        int validClassCount = 0;

        for (int i = 0; i < numClasses; i++) {
            double precision = truePositives[i] == 0 ? 0
                    : (double) truePositives[i] / (truePositives[i] + falsePositives[i]);
            double recall = truePositives[i] == 0 ? 0
                    : (double) truePositives[i] / (truePositives[i] + falseNegatives[i]);
            double f1 = precision == 0 || recall == 0 ? 0
                    : 2 * precision * recall / (precision + recall);
            int support = truePositives[i] + falseNegatives[i];

            if (support > 0) {
                macroPrecision += precision;
                macroRecall += recall;
                macroF1 += f1;
                validClassCount++;
            }

            System.out.printf("%-15s %-12.4f %-12.4f %-12.4f %-12d%n",
                    classNames[i], precision, recall, f1, support);
        }

        // Macro averages
        if (validClassCount > 0) {
            macroPrecision /= validClassCount;
            macroRecall /= validClassCount;
            macroF1 /= validClassCount;

            System.out.println("\nMacro Averages:");
            System.out.println("==========================================");
            System.out.printf("Precision: %.4f%n", macroPrecision);
            System.out.printf("Recall: %.4f%n", macroRecall);
            System.out.printf("F1-Score: %.4f%n", macroF1);
        }

        // Confusion Matrix'i sadece yazdır
        int[][] confMatrix = calculateConfusionMatrix(testBatches);
        printConfusionMatrix(confMatrix, classNames);
    }

// Confusion Matrix'i görselleştirmek için ayrı bir metod
    public void showConfusionMatrix() {
        List<Batch> testBatches = dataLoader.getTestBatches();
        int[][] matrix = calculateConfusionMatrix(testBatches);
        String[] classNames = getClassNames();

        SwingUtilities.invokeLater(() -> {
            ConfusionMatrixPlot plot = new ConfusionMatrixPlot(matrix, classNames);
            plot.setSize(700, 700);
            plot.setLocationRelativeTo(null);
            plot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            plot.setVisible(true);
        });
    }

// Confusion Matrix'i konsola yazdırma
    private void printConfusionMatrix(int[][] matrix, String[] classNames) {
        System.out.println("\nConfusion Matrix:");
        String headerLine = "-".repeat(14 + 12 * matrix.length);
        System.out.println(headerLine);

        // Header
        System.out.print("  Actual\\Pred |");
        for (int i = 0; i < matrix.length; i++) {
            System.out.printf("%12s", classNames[i]);
        }
        System.out.println("\n" + headerLine);

        // Matrix değerleri
        for (int i = 0; i < matrix.length; i++) {
            System.out.printf("%12s |", classNames[i]);
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%12d", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println(headerLine);
    }

    public void showROCCurve() {
        List<Batch> testBatches = dataLoader.getTestBatches();
        int numClasses = model.getOutputSize();
        String[] classNames = getClassNames();

        // Her sınıf için tahminleri ve gerçek değerleri topla
        List<List<Double>> probsPerClass = new ArrayList<>(numClasses);
        List<List<Integer>> actualsPerClass = new ArrayList<>(numClasses);

        for (int i = 0; i < numClasses; i++) {
            probsPerClass.add(new ArrayList<>());
            actualsPerClass.add(new ArrayList<>());
        }

        // Tahminleri topla
        for (Batch batch : testBatches) {
            for (int i = 0; i < batch.features.length; i++) {
                double[] output = model.forward(batch.features[i], false);
                int actualClass = argmax(batch.labels[i]);

                // Her sınıf için olasılıkları ve gerçek değerleri kaydet
                for (int classIdx = 0; classIdx < numClasses; classIdx++) {
                    probsPerClass.get(classIdx).add(output[classIdx]);
                    actualsPerClass.get(classIdx).add(actualClass == classIdx ? 1 : 0);
                }
            }
        }

        // ROC plot'u göster
        SwingUtilities.invokeLater(() -> {
            ROCCurvePlot rocPlot = new ROCCurvePlot(probsPerClass, actualsPerClass, classNames);
            rocPlot.setSize(800, 800);
            rocPlot.setLocationRelativeTo(null);
            rocPlot.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            rocPlot.setVisible(true);
        });
    }    // Paralel çalışan train metodu

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

                // Validation işlemleri
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

                // Metrikleri kaydet
                trainLosses.add(loss);
                trainAccuracies.add(accuracy);
                valLosses.add(valLoss);
                valAccuracies.add(valAccuracy);

                // Öğrenme eğrilerini güncelle
                paintLearningCurves();

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

    public void showPredictionPlot() {
        if (!model.getProblemType().equals(EProblemType.REGRESSION)) {
            System.out.println("Prediction plot is only available for regression problems!");
            return;
        }

        List<Batch> testBatches = dataLoader.getTestBatches();
        List<Double> actuals = new ArrayList<>();
        List<Double> predictions = new ArrayList<>();

        // Tahminleri topla
        for (Batch batch : testBatches) {
            for (int i = 0; i < batch.features.length; i++) {
                double[] output = model.forward(batch.features[i], false);
                actuals.add(batch.labels[i][0]);
                predictions.add(output[0]);
            }
        }

        // Frame'i oluştur ve göster
        SwingUtilities.invokeLater(() -> {
            PredictionPlot plot = new PredictionPlot(actuals, predictions);
            plot.setVisible(true);  // Görünür yap
        });
    }

    public void showResidualPlot() {
        if (!model.getProblemType().equals(EProblemType.REGRESSION)) {
            System.out.println("Residual plot is only available for regression problems!");
            return;
        }

        List<Batch> testBatches = dataLoader.getTestBatches();
        List<Double> actuals = new ArrayList<>();
        List<Double> residuals = new ArrayList<>();

        for (Batch batch : testBatches) {
            for (int i = 0; i < batch.features.length; i++) {
                double[] output = model.forward(batch.features[i], false);
                double actual = batch.labels[i][0];
                double predicted = output[0];
                actuals.add(actual);
                residuals.add(actual - predicted);
            }
        }

        // Frame'i oluştur ve göster
        SwingUtilities.invokeLater(() -> {
            ResidualPlot plot = new ResidualPlot(actuals, residuals);
            plot.setVisible(true);  // Görünür yap
        });
    }

}
