/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ai.djl.examples.training.transferlearning;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.nn.Block;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.SymbolBlock;
import ai.djl.nn.core.Linear;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TransferLearningTestTemplate {

    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        // Modeli yükleyeceğimiz yol
        String modelPath = "D:\\Dropbox\\NetbeansProjects\\OJL\\models\\squeezenet"; // Dosya yolunu değiştirin
        String testImagePath_close = "D:\\DATASETS\\pistachio_224_224\\test\\close"; // Test klasörü yolu
        String testImagePath_open = "D:\\DATASETS\\pistachio_224_224\\test\\open"; // Test klasörü yolu
        String[] classLabels = {"close", "open"}; // Sınıf etiketleri

        // Model yükleniyor
        Criteria<ai.djl.modality.cv.Image, ai.djl.modality.Classifications> criteria = Criteria.builder()
                .optApplication(ai.djl.Application.CV.IMAGE_CLASSIFICATION)
                .setTypes(ai.djl.modality.cv.Image.class, ai.djl.modality.Classifications.class)
                .optArtifactId("squeezenet")
                .optProgress(new ProgressBar())
                .build();

        ZooModel<ai.djl.modality.cv.Image, ai.djl.modality.Classifications> squeezeNet = criteria.loadModel();

        Model model = Model.newInstance("transfer-squeezenet");
        SymbolBlock baseBlock = (SymbolBlock) squeezeNet.getBlock();

        SequentialBlock newBlock = new SequentialBlock();
        for (int i = 0; i < baseBlock.getChildren().size() - 1; i++) {
            newBlock.add((Block) baseBlock.getChildren().get(i));
        }
        newBlock.add(Blocks.batchFlattenBlock());
        newBlock.add(Linear.builder().setUnits(2).build());
        model.setBlock(newBlock);

        // Modele bloğu ayarlayın
        model.setBlock(newBlock);

        // Şimdi modeli yükleyin
        model.load(Paths.get(modelPath), "squeezenet-transfer-pistachio");

        // Model yüklendi!
        System.out.println("Model başarıyla yüklendi: " + modelPath);

        // Test resimlerini listeleme
        List<Path> imageFiles_close = Files.list(Paths.get(testImagePath_close))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".jpg")) // Resim uzantısı
                .toList();
        List<Path> imageFiles_open = Files.list(Paths.get(testImagePath_open))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".jpg")) // Resim uzantısı
                .toList();
        List<Path> imageFiles = new ArrayList<>(imageFiles_close);
        imageFiles.addAll(imageFiles_open);
        // ImageClassificationTranslator oluşturma (Synset'i doğrudan ayarlama)
        ImageClassificationTranslator translator = ImageClassificationTranslator.builder()
                .addTransform(new ToTensor())
                .optSynset(Arrays.asList(classLabels)) // Sınıf etiketlerini liste olarak geçirme
                .build();

        List<String> trueLabels = new ArrayList<>();
        List<String> predictedLabels = new ArrayList<>();
        List<Double> probabilities = new ArrayList<>();

        // Her test görüntüsünü işleme
        for (Path imageFile : imageFiles) {
            System.out.println("Processing image: " + imageFile.getFileName());
            Predictor<Image, Classifications> predictor = model.newPredictor(translator);
            long t1 = FactoryUtils.tic();
            Image img = ImageFactory.getInstance().fromFile(imageFile);
            Classifications pred = predictor.predict(img);
            System.out.println("pred = " + pred);
            String trueLabel = determineTrueLabel(imageFile, classLabels);
            String predictedLabel = pred.best().getClassName();
            double probability = pred.best().getProbability();  // Olasılık değerini al

            trueLabels.add(trueLabel);
            predictedLabels.add(predictedLabel);
            probabilities.add(probability);
            t1 = FactoryUtils.toc(t1);
        }

        // Performans metriklerini hesapla
        Map<String, Object> metrics = calculatePerformanceMetrics(trueLabels, predictedLabels, classLabels);

        // Sonuçları yazdır
        System.out.println("Test Performance Metrics:");
        System.out.println("-------------------------");
        System.out.printf("Accuracy: %.4f%%\n", (double) metrics.get("accuracy") * 100);

        System.out.println("\nPrecision:");
        ((Map<String, Double>) metrics.get("precision")).forEach((key, value) -> System.out.printf("  %s: %.4f\n", key, value));

        System.out.println("\nRecall:");
        ((Map<String, Double>) metrics.get("recall")).forEach((key, value) -> System.out.printf("  %s: %.4f\n", key, value));

        System.out.println("\nF1-Score:");
        ((Map<String, Double>) metrics.get("f1Score")).forEach((key, value) -> System.out.printf("  %s: %.4f\n", key, value));
        System.out.println("\nConfusion Matrix:");
        int[][] confusionMatrix = (int[][]) metrics.get("confusionMatrix");
        for (int[] row : confusionMatrix) {
            for (int cell : row) {
                System.out.printf("%d ", cell);
            }
            System.out.println();
        }
        double[][] rocPoints = calculateROC(probabilities, trueLabels, "open");

        // ROC noktalarını yazdır (opsiyonel)
        System.out.println("ROC Curve Points (FPR, TPR):");
        for (int i = 0; i < rocPoints.length - 1; i++) {
            System.out.printf("%.4f, %.4f%n", rocPoints[0][i], rocPoints[1][i]);
        }
        System.out.printf("AUC: %.4f%n", rocPoints[0][rocPoints.length - 1]);
        CMatrix cm = CMatrix.getInstance(rocPoints[1])
                .plot(FactoryUtils.toFloatArray1D(rocPoints[0]));

    }

    public static Map<String, Object> calculatePerformanceMetrics(List<String> trueLabels, List<String> predictedLabels, String[] classLabels) {
        Map<String, Object> metrics = new HashMap<>();
        int[][] confusionMatrix = new int[classLabels.length][classLabels.length];

        // Confusion Matrix'i doldur
        for (int i = 0; i < trueLabels.size(); i++) {
            int trueIndex = Arrays.asList(classLabels).indexOf(trueLabels.get(i));
            int predIndex = Arrays.asList(classLabels).indexOf(predictedLabels.get(i));
            confusionMatrix[trueIndex][predIndex]++;
        }

        // Debug: Confusion Matrix'i yazdır
        System.out.println("Confusion Matrix:");
        for (int i = 0; i < confusionMatrix.length; i++) {
            for (int j = 0; j < confusionMatrix[i].length; j++) {
                System.out.print(confusionMatrix[i][j] + " ");
            }
            System.out.println();
        }

        // Accuracy hesapla
        int correctPredictions = 0;
        for (int i = 0; i < classLabels.length; i++) {
            correctPredictions += confusionMatrix[i][i];
        }
        double accuracy = (double) correctPredictions / trueLabels.size();

        // Precision, Recall ve F1-Score hesapla
        Map<String, Double> precisionMap = new HashMap<>();
        Map<String, Double> recallMap = new HashMap<>();
        Map<String, Double> f1ScoreMap = new HashMap<>();

        for (int i = 0; i < classLabels.length; i++) {
            int truePositives = confusionMatrix[i][i];
            int falsePositives = 0;
            int falseNegatives = 0;

            for (int j = 0; j < classLabels.length; j++) {
                if (i != j) {
                    falsePositives += confusionMatrix[j][i];
                    falseNegatives += confusionMatrix[i][j];
                }
            }

            double precision = (truePositives + falsePositives > 0) ? (double) truePositives / (truePositives + falsePositives) : 0.0;
            double recall = (truePositives + falseNegatives > 0) ? (double) truePositives / (truePositives + falseNegatives) : 0.0;
            double f1Score = (precision + recall > 0) ? 2 * (precision * recall) / (precision + recall) : 0.0;

            // Debug: Her sınıf için hesaplamaları yazdır
            System.out.println("Class: " + classLabels[i]);
            System.out.println("TP: " + truePositives + ", FP: " + falsePositives + ", FN: " + falseNegatives);
            System.out.println("Precision: " + precision + ", Recall: " + recall + ", F1-Score: " + f1Score);

            precisionMap.put(classLabels[i], precision);
            recallMap.put(classLabels[i], recall);
            f1ScoreMap.put(classLabels[i], f1Score);
        }

        // Metrikleri Map'e ekle
        metrics.put("accuracy", accuracy);
        metrics.put("precision", precisionMap);
        metrics.put("recall", recallMap);
        metrics.put("f1Score", f1ScoreMap);
        metrics.put("confusionMatrix", confusionMatrix);

        return metrics;
    }

    private static String determineTrueLabel(Path imagePath, String[] classLabels) {
        for (String label : classLabels) {
            if (imagePath.toString().toLowerCase().contains(label.toLowerCase())) {
                return label;
            }
        }
        System.out.println("Warning: Unable to determine true label for " + imagePath);
        return "unknown";
    }

    public static double[][] calculateROC(List<Double> probabilities, List<String> trueLabels, String positiveClass) {
        List<ProbabilityLabel> combinedList = new ArrayList<>();
        for (int i = 0; i < probabilities.size(); i++) {
            combinedList.add(new ProbabilityLabel(probabilities.get(i), trueLabels.get(i)));
        }
        Collections.sort(combinedList, Collections.reverseOrder());

        int totalPositives = (int) trueLabels.stream().filter(label -> label.equals(positiveClass)).count();
        int totalNegatives = trueLabels.size() - totalPositives;

        List<Double> fprList = new ArrayList<>();
        List<Double> tprList = new ArrayList<>();

        fprList.add(0.0); // Başlangıç noktası
        tprList.add(0.0); // Başlangıç noktası

        int truePositives = 0;
        int falsePositives = 0;

        for (ProbabilityLabel pl : combinedList) {
            if (pl.label.equals(positiveClass)) {
                truePositives++;
            } else {
                falsePositives++;
            }
            double tpr = (double) truePositives / totalPositives;
            double fpr = (double) falsePositives / totalNegatives;
            fprList.add(fpr);
            tprList.add(tpr);
        }

        // AUC hesapla
        double auc = calculateAUC(fprList, tprList);

        // 2x490 boyutunda bir matris oluştur
        double[][] rocMatrix = new double[2][fprList.size()];

        // FPR değerlerini ilk satıra yerleştir
        for (int i = 0; i < fprList.size(); i++) {
            rocMatrix[0][i] = fprList.get(i);
        }

        // TPR değerlerini ikinci satıra yerleştir
        for (int i = 0; i < tprList.size(); i++) {
            rocMatrix[1][i] = tprList.get(i);
        }

        return rocMatrix;
    }

    private static double calculateAUC(List<Double> fprList, List<Double> tprList) {
        double auc = 0.0;
        for (int i = 1; i < fprList.size(); i++) {
            auc += (fprList.get(i) - fprList.get(i - 1)) * (tprList.get(i) + tprList.get(i - 1)) / 2;
        }
        return auc;
    }

    private static class ProbabilityLabel implements Comparable<ProbabilityLabel> {

        double probability;
        String label;

        ProbabilityLabel(double probability, String label) {
            this.probability = probability;
            this.label = label;
        }

        @Override
        public int compareTo(ProbabilityLabel other) {
            return Double.compare(this.probability, other.probability);
        }
    }

}
