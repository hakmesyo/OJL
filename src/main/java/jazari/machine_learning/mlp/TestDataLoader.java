package jazari.machine_learning.mlp;

import jazari.machine_learning.data_loader.Batch;
import jazari.machine_learning.data_loader.DataLoader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TestDataLoader {

    public static void main(String[] args) {
        testAllFormats();
    }

    private static void testAllFormats() {
        System.out.println("DataLoader Test Suite");
        System.out.println("===================\n");

        // Her test için yeni bir DataLoader instance'ı kullan
        testSyntheticData();
        testCSVData();
        testMNISTData();
        testImageData();
        testARFFData();
        testJSONData();
        testMATLABData();
        testTextData();
        testVisualizeDataset();
    }

    private static void testSyntheticData() {
        System.out.println("Testing Synthetic Data Generation");
        System.out.println("---------------------------------");
        try {
            DataLoader loader = new DataLoader();

            // 2D veri üret: 3 sınıf, her sınıf için 100 örnek, 2 özellik
            loader.generateSyntheticClassificationData(
                    100, // samples per class
                    2, // features
                    3, // classes
                    42 // random seed
            );

            // Veriyi böl
            loader.splitData(0.7, 0.15, 0.15);
            loader.setBatchSize(32);

            // Test et
            testDatasetFunctionality(loader, "Synthetic");

        } catch (Exception e) {
            System.err.println("Error in synthetic data test: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testCSVData() {
        System.out.println("Testing CSV Data Loading");
        System.out.println("-----------------------");
        try {
            DataLoader loader = new DataLoader();

            // Iris veya benzer bir CSV dosyası
            loader.loadCSV("dataset/iris.csv", 4, true);  // son kolon label
            loader.splitData(0.7, 0.15, 0.15);
            loader.setBatchSize(32);

            testDatasetFunctionality(loader, "CSV");

        } catch (Exception e) {
            System.err.println("Error in CSV data test: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testMNISTData() {
        System.out.println("Testing MNIST Data Loading");
        System.out.println("-------------------------");
        try {
            DataLoader loader = new DataLoader();

            loader.loadMNIST(
                    "dataset/mnist/train-images-idx3-ubyte.gz",
                    "dataset/mnist/train-labels-idx1-ubyte.gz"
            );

            loader.splitData(0.7, 0.15, 0.15);
            loader.setBatchSize(32);

            testDatasetFunctionality(loader, "MNIST");

        } catch (Exception e) {
            System.err.println("Error in MNIST data test: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testImageData() {
        System.out.println("Testing Image Data Loading");
        System.out.println("-------------------------");
        try {
            DataLoader loader = new DataLoader();

            // Her sınıf için ayrı klasörde resimler olmalı
            loader.loadImages("dataset/images", 32, 32);  // 32x32 boyutunda resimler
            loader.splitData(0.7, 0.15, 0.15);
            loader.setBatchSize(32);

            testDatasetFunctionality(loader, "Image");

        } catch (Exception e) {
            System.err.println("Error in image data test: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testARFFData() {
        System.out.println("Testing ARFF Data Loading");
        System.out.println("------------------------");
        try {
            DataLoader loader = new DataLoader();

            //loader.loadARFF("dataset/runoff_data.arff");
            loader.loadARFF("dataset/iris.arff");
            loader.splitData(0.7, 0.15, 0.15);
            loader.setBatchSize(32);

            testDatasetFunctionality(loader, "ARFF");

        } catch (Exception e) {
            System.err.println("Error in ARFF data test: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testJSONData() {
        System.out.println("Testing JSON Data Loading");
        System.out.println("------------------------");
        try {
            DataLoader loader = new DataLoader();

            loader.loadJSON("dataset/data.json", "features", "label");
            loader.splitData(0.7, 0.15, 0.15);
            loader.setBatchSize(32);

            testDatasetFunctionality(loader, "JSON");

        } catch (Exception e) {
            System.err.println("Error in JSON data test: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testMATLABData() {
        System.out.println("Testing MATLAB Data Loading");
        System.out.println("--------------------------");
        try {
            DataLoader loader = new DataLoader();

            loader.loadMAT("dataset/data.mat", "X", "y");  // X: features, y: labels
            loader.splitData(0.7, 0.15, 0.15);
            loader.setBatchSize(32);

            testDatasetFunctionality(loader, "MATLAB");

        } catch (Exception e) {
            System.err.println("Error in MATLAB data test: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testTextData() {
        System.out.println("Testing Text Data Loading");
        System.out.println("------------------------");
        try {
            DataLoader loader = new DataLoader();

            loader.loadDelimitedText("dataset/data.txt", 4, "\t", true);  // tab-separated
            loader.splitData(0.7, 0.15, 0.15);
            loader.setBatchSize(32);

            testDatasetFunctionality(loader, "Text");

        } catch (Exception e) {
            System.err.println("Error in text data test: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void testDatasetFunctionality(DataLoader loader, String dataType) {
        // Temel kontroller
        System.out.println("\nChecking " + dataType + " dataset functionality:");

        // 1. Batch'leri kontrol et
        List<Batch> trainBatches = loader.getBatches();
        List<Batch> valBatches = loader.getValidationBatches();
        List<Batch> testBatches = loader.getTestBatches();

        System.out.println("- Number of training batches: " + trainBatches.size());
        System.out.println("- Number of validation batches: " + valBatches.size());
        System.out.println("- Number of test batches: " + testBatches.size());

        // 2. İlk batch'i kontrol et
        if (!trainBatches.isEmpty()) {
            Batch firstBatch = trainBatches.get(0);
            System.out.println("- First batch size: " + firstBatch.getSize());
            System.out.println("- Feature dimension: " + firstBatch.features[0].length);
            System.out.println("- Label dimension: " + firstBatch.labels[0].length);
        }

        // 3. Veri normalleştirmeyi kontrol et
        if (!trainBatches.isEmpty() && trainBatches.get(0).features.length > 0) {
            double[] firstFeatures = trainBatches.get(0).features[0];
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (double feature : firstFeatures) {
                min = Math.min(min, feature);
                max = Math.max(max, feature);
            }
            System.out.println("- Feature range: [" + min + ", " + max + "]");
        }

        // 4. Class mapping'i kontrol et
        Map<String, Integer> classMap = loader.getClassMap();
        if (!classMap.isEmpty()) {
            System.out.println("- Number of classes: " + classMap.size());
            System.out.println("- Class mapping: " + classMap);
        }

        System.out.println("Test completed successfully for " + dataType + " dataset");
    }

    /**
     * Örnek veri dosyaları oluştur (test için)
     */
    private static void createSampleFiles() {
        // JSON örneği
        String jsonData = "[\n"
                + "  {\"features\": [1.2, 3.4, 5.6], \"label\": \"class1\"},\n"
                + "  {\"features\": [2.3, 4.5, 6.7], \"label\": \"class2\"}\n"
                + "]";

        // Text dosyası örneği
        String textData = "1.2\t3.4\t5.6\tclass1\n"
                + "2.3\t4.5\t6.7\tclass2\n";

        // ARFF örneği
        String arffData = "@RELATION test\n\n"
                + "@ATTRIBUTE feature1 NUMERIC\n"
                + "@ATTRIBUTE feature2 NUMERIC\n"
                + "@ATTRIBUTE class {class1,class2}\n\n"
                + "@DATA\n"
                + "1.2,3.4,class1\n"
                + "2.3,4.5,class2\n";

        try {
            // Örnek dosyaları oluştur
            new File("dataset").mkdirs();

            try (FileWriter fw = new FileWriter("dataset/data.json")) {
                fw.write(jsonData);
            }

            try (FileWriter fw = new FileWriter("dataset/data.txt")) {
                fw.write(textData);
            }

            try (FileWriter fw = new FileWriter("dataset/data.arff")) {
                fw.write(arffData);
            }

        } catch (IOException e) {
            System.err.println("Error creating sample files: " + e.getMessage());
        }
    }

    private static void testVisualizeDataset() {
        DataLoader loader = new DataLoader();
        loader.loadCSV("dataset/iris.csv", 4, true);
        loader.showTSNE();
    }
}
