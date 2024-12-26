package jazari.machine_learning.data_loader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import jazari.factory.FactoryMatrix;
import jazari.machine_learning.analysis.data_visualization.CorrelationHeatmap;
import jazari.machine_learning.analysis.data_visualization.FeatureScatterPlot;
import jazari.matrix.CMatrix;

/**
 * Main DataLoader class
 */
public class DataLoader {

    private Dataset dataset;
    private Dataset trainDataset;
    private Dataset valDataset;
    private Dataset testDataset;
    private int batchSize;
    private Random random;
    private Map<String, Integer> classMap;

    public DataLoader() {
        this.random = new Random();
        this.batchSize = 32;
        this.classMap = new HashMap<>();
    }

    /**
     * Veri setinin histogram görselleştirmesini gösterir
     */
    public void showTSNE() {
        float[][] ds = getData();
        CMatrix cm = CMatrix.getInstance(ds)
                .toDataSet()
                .tsne();
    }

    /**
     * Load data from delimited text file (CSV, TSV, Excel exported text etc.)
     */
    public void loadDelimitedText(String filepath, int labelColumn, String delimiter, boolean skipHeader)
            throws IOException {
        List<String[]> rows = new ArrayList<>();
        Set<String> uniqueClasses = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            if (skipHeader) {
                br.readLine();
            }

            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter);
                if (values.length > 0 && values[0].trim().length() > 0) {
                    rows.add(values);
                    uniqueClasses.add(values[labelColumn].trim());
                }
            }
        }

        if (rows.isEmpty()) {
            throw new IOException("No data found in file: " + filepath);
        }

        SimpleDataset dataset = new SimpleDataset(rows.get(0).length - 1, uniqueClasses.size());

        // Create class mapping
        int classIndex = 0;
        for (String className : uniqueClasses) {
            classMap.put(className, classIndex++);
        }

        // Add samples
        for (String[] row : rows) {
            try {
                double[] features = new double[row.length - 1];
                double[] labels = new double[uniqueClasses.size()];

                int featureIndex = 0;
                for (int i = 0; i < row.length; i++) {
                    if (i != labelColumn) {
                        features[featureIndex++] = Double.parseDouble(row[i].trim());
                    } else {
                        labels[classMap.get(row[i].trim())] = 1.0;
                    }
                }

                dataset.addSample(new Sample(features, labels));
            } catch (NumberFormatException e) {
                System.err.println("Warning: Skipping invalid row - " + String.join(delimiter, row));
            }
        }

        this.dataset = dataset;
        normalizeFeatures();

        System.out.println(String.format("\nLoaded data from: %s", filepath));
        System.out.println(String.format("Total samples: %d", dataset.size()));
        System.out.println(String.format("Features per sample: %d", dataset.getFeatureSize()));
        System.out.println(String.format("Number of classes: %d", dataset.getLabelSize()));
    }

    /**
     * Load CSV file (Convenience method)
     */
    public void loadCSV(String filepath, int labelColumn, boolean skipHeader) {
        try {
            loadDelimitedText(filepath, labelColumn, ",", skipHeader);
        } catch (IOException ex) {
            Logger.getLogger(DataLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Load MNIST dataset
     */
    public void loadMNIST(String imagesPath, String labelsPath) {
        try {
            this.dataset = new MNISTDataset(imagesPath, labelsPath);
        } catch (IOException ex) {
            Logger.getLogger(DataLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        normalizeFeatures();
    }

    /**
     * Load image dataset from folder
     */
    public void loadImages(String folderPath, int width, int height) {
        try {
            this.dataset = new ImageDataset(folderPath, width, height);
        } catch (IOException ex) {
            Logger.getLogger(DataLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        normalizeFeatures();
    }

    /**
     * Load ARFF file (Weka format)
     */
    public void loadARFF(String filePath) {
        try {
            this.dataset = new ARFFDataset(filePath);
        } catch (IOException ex) {
            Logger.getLogger(DataLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        normalizeFeatures();
    }

    /**
     * Load MATLAB .mat file
     *
     * @param matFilePath MATLAB .mat dosyasının yolu
     * @param featuresVarName Features matrisinin değişken adı
     * @param labelsVarName Labels vektörünün değişken adı
     */
    public void loadMAT(String matFilePath, String featuresVarName, String labelsVarName) throws IOException {
        this.dataset = new MATLABDataset(matFilePath, featuresVarName, labelsVarName);
        normalizeFeatures();
    }

    /**
     * Load data from JSON file
     *
     * @param filePath JSON dosyasının yolu
     * @param featuresKey Features array'inin JSON key'i
     * @param labelKey Label'ın JSON key'i
     */
    public void loadJSON(String filePath, String featuresKey, String labelKey) throws IOException {
        this.dataset = new JSONDataset(filePath, featuresKey, labelKey);
        normalizeFeatures();

        System.out.println(String.format("\nLoaded JSON data from: %s", filePath));
        System.out.println(String.format("Total samples: %d", dataset.size()));
        System.out.println(String.format("Features per sample: %d", dataset.getFeatureSize()));
        System.out.println(String.format("Number of classes: %d", dataset.getLabelSize()));
    }

    /**
     * Generate synthetic data for classification
     */
    public void generateSyntheticClassificationData(int n_samples, int n_features, int n_classes, long seed) {
        Random random = new Random(seed);
        float[][] rawData = FactoryMatrix.make_blobs(n_samples, n_features,
                n_classes, 100, 5, random);

        SimpleDataset syntheticDataset = new SimpleDataset(n_features, n_classes);

        for (float[] row : rawData) {
            double[] features = new double[n_features];
            double[] labels = new double[n_classes];

            for (int i = 0; i < n_features; i++) {
                features[i] = row[i];
            }

            int classIndex = (int) row[n_features];
            labels[classIndex] = 1.0;

            syntheticDataset.addSample(new Sample(features, labels));
        }

        this.dataset = syntheticDataset;
        normalizeFeatures();

        System.out.println("Synthetic data generated:");
        System.out.println("Total samples: " + (n_samples * n_classes));
        System.out.println("Features per sample: " + n_features);
        System.out.println("Number of classes: " + n_classes);
    }

    /**
     * Generate synthetic data
     */
    public void generateSyntheticClassificationData(int n_samples, int n_features, int n_classes, int mean_scale, int var_scale, long seed) {
        Random random = new Random(seed);
        float[][] rawData = FactoryMatrix.make_blobs(n_samples, n_features,
                n_classes, mean_scale, var_scale, random);

        SimpleDataset syntheticDataset = new SimpleDataset(n_features, n_classes);

        for (float[] row : rawData) {
            double[] features = new double[n_features];
            double[] labels = new double[n_classes];

            for (int i = 0; i < n_features; i++) {
                features[i] = row[i];
            }

            int classIndex = (int) row[n_features];
            labels[classIndex] = 1.0;

            syntheticDataset.addSample(new Sample(features, labels));
        }

        this.dataset = syntheticDataset;
        normalizeFeatures();

        System.out.println("Synthetic data generated:");
        System.out.println("Total samples: " + (n_samples * n_classes));
        System.out.println("Features per sample: " + n_features);
        System.out.println("Number of classes: " + n_classes);
    }

    /**
     * Sentetik regresyon verisi üretir
     *
     * @param n_samples toplam örnek sayısı
     * @param n_features girdi özelliklerinin sayısı
     * @param n_outputs çıktı değişkenlerinin sayısı (genelde 1)
     * @param noise gürültü seviyesi (0-1 arası)
     * @param seed random seed
     */
    public void generateSyntheticRegressionData(int n_samples, int n_features, int n_outputs, double noise, long seed) {
        Random random = new Random(seed);

        // Features matrisini oluştur
        double[][] features = new double[n_samples][n_features];
        double[][] outputs = new double[n_samples][n_outputs];

        // Her örnek için
        for (int i = 0; i < n_samples; i++) {
            // Feature'ları oluştur (-1 ile 1 arasında)
            for (int j = 0; j < n_features; j++) {
                features[i][j] = random.nextDouble() * 2 - 1;  // [-1, 1] arasında
            }

            // Her feature'ın bir katsayısı olsun
            double y = 0;
            for (int j = 0; j < n_features; j++) {
                y += features[i][j] * (j + 1);  // Lineer ilişki
            }

            // Nonlineer terimler ekle
            y += Math.sin(features[i][0] * Math.PI);  // Sinüzoidal ilişki
            y += Math.pow(features[i][1], 2);         // Karesel ilişki

            // Gürültü ekle
            y += random.nextGaussian() * noise;

            // Çıktıyı normalize et
            outputs[i][0] = y;
        }

        // SimpleDataset oluştur
        SimpleDataset syntheticDataset = new SimpleDataset(n_features, n_outputs);

        // Örnekleri dataset'e ekle
        for (int i = 0; i < n_samples; i++) {
            syntheticDataset.addSample(new Sample(features[i], outputs[i]));
        }

        this.dataset = syntheticDataset;

        System.out.println("\nSynthetic regression data generated:");
        System.out.println("Total samples: " + n_samples);
        System.out.println("Features per sample: " + n_features);
        System.out.println("Output dimensions: " + n_outputs);
    }

    /**
     * Split data into train, validation and test sets
     */
    public void splitData(double trainRatio, double valRatio, double testRatio) {
        if (dataset == null) {
            throw new IllegalStateException("No data loaded");
        }

        if (Math.abs(trainRatio + valRatio + testRatio - 1.0) > 1e-10) {
            throw new IllegalArgumentException("Ratios must sum to 1.0");
        }

        dataset.shuffle();
        int totalSize = dataset.size();
        int trainSize = (int) (totalSize * trainRatio);
        int valSize = (int) (totalSize * valRatio);

        List<Sample> allSamples = new ArrayList<>();
        for (int i = 0; i < totalSize; i++) {
            allSamples.add(dataset.getSample(i));
        }

        // Train dataset
        SimpleDataset trainSet = new SimpleDataset(dataset.getFeatureSize(), dataset.getLabelSize());
        for (int i = 0; i < trainSize; i++) {
            trainSet.addSample(allSamples.get(i));
        }
        this.trainDataset = trainSet;

        // Validation dataset
        SimpleDataset valSet = new SimpleDataset(dataset.getFeatureSize(), dataset.getLabelSize());
        for (int i = trainSize; i < trainSize + valSize; i++) {
            valSet.addSample(allSamples.get(i));
        }
        this.valDataset = valSet;

        // Test dataset
        SimpleDataset testSet = new SimpleDataset(dataset.getFeatureSize(), dataset.getLabelSize());
        for (int i = trainSize + valSize; i < totalSize; i++) {
            testSet.addSample(allSamples.get(i));
        }
        this.testDataset = testSet;

        System.out.println("\nVeri seti bölünmesi:");
        System.out.printf("Train set: %d örnek (%.1f%%)\n", trainSet.size(), trainRatio * 100);
        System.out.printf("Validation set: %d örnek (%.1f%%)\n", valSet.size(), valRatio * 100);
        System.out.printf("Test set: %d örnek (%.1f%%)\n", testSet.size(), testRatio * 100);
    }

    /**
     * Normalize features to [0,1] range
     */
    private void normalizeFeatures() {
        if (dataset.size() == 0) {
            return;
        }

        int featureSize = dataset.getFeatureSize();
        double[] minVals = new double[featureSize];
        double[] maxVals = new double[featureSize];
        Arrays.fill(minVals, Double.MAX_VALUE);
        Arrays.fill(maxVals, Double.MIN_VALUE);

        // Find min and max values
        for (int i = 0; i < dataset.size(); i++) {
            double[] features = dataset.getSample(i).getFeatures();
            for (int j = 0; j < featureSize; j++) {
                minVals[j] = Math.min(minVals[j], features[j]);
                maxVals[j] = Math.max(maxVals[j], features[j]);
            }
        }

        // Normalize
        for (int i = 0; i < dataset.size(); i++) {
            double[] features = dataset.getSample(i).getFeatures();
            for (int j = 0; j < featureSize; j++) {
                if (maxVals[j] > minVals[j]) {
                    features[j] = (features[j] - minVals[j]) / (maxVals[j] - minVals[j]);
                }
            }
        }
    }

    /**
     * Set batch size
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Get training
     *
     * @return batches
     */
    public List<Batch> getBatches() {
        return createBatches(trainDataset);
    }

    /**
     * Get validation batches
     *
     * @return
     */
    public List<Batch> getValidationBatches() {
        return createBatches(valDataset);
    }

    /**
     * Get test batches
     *
     * @return
     */
    public List<Batch> getTestBatches() {
        return createBatches(testDataset);
    }

    /**
     * Create batches from dataset
     */
    private List<Batch> createBatches(Dataset dataset) {
        if (dataset == null) {
            throw new IllegalStateException("Dataset is not initialized");
        }

        List<Batch> batches = new ArrayList<>();
        int numBatches = (int) Math.ceil((double) dataset.size() / batchSize);

        for (int i = 0; i < numBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, dataset.size());
            int currentBatchSize = end - start;

            double[][] batchFeatures = new double[currentBatchSize][];
            double[][] batchLabels = new double[currentBatchSize][];

            for (int j = 0; j < currentBatchSize; j++) {
                Sample sample = dataset.getSample(start + j);
                batchFeatures[j] = sample.getFeatures();
                batchLabels[j] = sample.getLabels();
            }

            batches.add(new Batch(batchFeatures, batchLabels));
        }

        return batches;
    }

    /**
     * Get input size
     *
     * @return
     */
    public int getInputSize() {
        return dataset != null ? dataset.getFeatureSize() : 0;
    }

    /**
     * Get number of classes
     */
    public int getNumClasses() {
        return dataset != null ? dataset.getLabelSize() : 0;
    }

    /**
     * Get class mapping
     */
    public Map<String, Integer> getClassMap() {
        return classMap;
    }

    /**
     * Print data summary
     */
    public void printDataSummary() {
        if (dataset == null) {
            System.out.println("No data loaded");
            return;
        }

        System.out.println("\nData Summary:");
        System.out.println("Total samples: " + dataset.size());
        System.out.println("Features per sample: " + dataset.getFeatureSize());
        System.out.println("Number of classes: " + dataset.getLabelSize());

        if (!classMap.isEmpty()) {
            System.out.println("\nClass mapping:");
            classMap.forEach((key, value)
                    -> System.out.println(String.format("  %s -> %d", key, value)));
        }
    }

    /**
     * Tüm veri setini float array olarak döndürür Her satır bir örnek olmak
     * üzere [örnekSayısı][özellikSayısı + sınıfSayısı] boyutunda array döner
     * Her satırın son elemanı sınıf indeksidir
     *
     * @return float[][] veri seti
     */
    public float[][] getData() {
        if (dataset == null || dataset.size() == 0) {
            return new float[0][0];
        }

        int numSamples = dataset.size();
        int numFeatures = dataset.getFeatureSize();
        float[][] data = new float[numSamples][numFeatures + 1];  // +1 for class label

        for (int i = 0; i < numSamples; i++) {
            Sample sample = dataset.getSample(i);

            // Feature'ları kopyala
            for (int j = 0; j < numFeatures; j++) {
                data[i][j] = (float) sample.getFeatures()[j];
            }

            // Son kolona sınıf indeksini ekle (one-hot encoded'dan sınıf indeksine çevir)
            double[] labels = sample.getLabels();
            int classIndex = 0;
            for (int j = 1; j < labels.length; j++) {
                if (labels[j] > labels[classIndex]) {
                    classIndex = j;
                }
            }
            data[i][numFeatures] = classIndex;
        }

        return data;
    }

    /**
     * Veri setinin feature'larını ve output ilişkilerini görselleştir
     */
    public void visualizeFeatures() {
        if (dataset == null || dataset.size() == 0) {
            System.out.println("No data loaded!");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Feature Analysis");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Ana panel için ScrollPane oluştur
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            // Scatter plotlar için panel
            JPanel mainPanel = new JPanel();

            // Ekran çözünürlüğüne göre sütun sayısını belirle
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int numColumns = screenSize.width >= 1920 ? 3 : 2;  // Geniş ekranlarda 3, dar ekranlarda 2 sütun

            // Satır sayısını hesapla (yukarı yuvarlama)
            int numFeatures = dataset.getFeatureSize();
            int numRows = (numFeatures + numColumns - 1) / numColumns;

            // Grid yerleşimini ayarla
            mainPanel.setLayout(new GridLayout(numRows, numColumns, 10, 10));  // 10px boşluklu grid

            // Her feature için scatter plot oluştur
            for (int i = 0; i < dataset.getFeatureSize(); i++) {
                List<Double> featureValues = new ArrayList<>();
                List<Double> outputValues = new ArrayList<>();

                for (int j = 0; j < dataset.size(); j++) {
                    Sample sample = dataset.getSample(j);
                    featureValues.add(sample.getFeatures()[i]);
                    outputValues.add(sample.getLabels()[0]);
                }

                // Feature scatter plot oluştur
                FeatureScatterPlot plot = new FeatureScatterPlot(
                        featureValues, outputValues, "Feature " + i);

                // Panel için border ekle
                plot.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

                // Boş grid hücrelerini doldurmak için panel ekle
                JPanel plotPanel = new JPanel(new BorderLayout());
                plotPanel.add(plot, BorderLayout.CENTER);
                mainPanel.add(plotPanel);
            }

            // Kalan boş grid hücrelerini doldur
            int remainder = numRows * numColumns - numFeatures;
            for (int i = 0; i < remainder; i++) {
                mainPanel.add(new JPanel());  // Boş panel ekle
            }

            scrollPane.setViewportView(mainPanel);

            // Frame boyutunu ayarla
            int frameWidth = Math.min(numColumns * 450, screenSize.width - 100);  // Her scatter plot ~400px + margin
            int frameHeight = Math.min(numRows * 450, screenSize.height - 100);
            frame.setSize(frameWidth, frameHeight);

            frame.add(scrollPane);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Frame'in minimum boyutunu ayarla
            frame.setMinimumSize(new Dimension(450, 450));  // En az bir scatter plot gösterilecek boyut

            // Frame'in yeniden boyutlandırılabilir olmasını sağla
            frame.setResizable(true);
        });
    }

    /**
     * Korelasyon matrisini göster
     */
    public void showCorrelationMatrix() {
        if (dataset == null || dataset.size() == 0) {
            System.out.println("No data loaded!");
            return;
        }

        // Korelasyon matrisini hesapla
        int numFeatures = dataset.getFeatureSize();
        double[][] correlations = new double[numFeatures + 1][numFeatures + 1];
        String[] labels = new String[numFeatures + 1];

        // Feature etiketleri
        for (int i = 0; i < numFeatures; i++) {
            labels[i] = "Feature " + i;
        }
        labels[numFeatures] = "Output";

        // Feature ve output değerlerini topla
        List<List<Double>> allValues = new ArrayList<>();
        for (int i = 0; i < numFeatures; i++) {
            List<Double> featureValues = new ArrayList<>();
            for (int j = 0; j < dataset.size(); j++) {
                featureValues.add(dataset.getSample(j).getFeatures()[i]);
            }
            allValues.add(featureValues);
        }

        // Output değerlerini ekle
        List<Double> outputValues = new ArrayList<>();
        for (int i = 0; i < dataset.size(); i++) {
            outputValues.add(dataset.getSample(i).getLabels()[0]);
        }
        allValues.add(outputValues);

        // Korelasyonları hesapla
        for (int i = 0; i <= numFeatures; i++) {
            for (int j = 0; j <= numFeatures; j++) {
                correlations[i][j] = calculateCorrelation(allValues.get(i), allValues.get(j));
            }
        }

        // Heatmap'i göster
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Correlation Matrix");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            CorrelationHeatmap heatmap = new CorrelationHeatmap(correlations, labels);
            frame.add(heatmap);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private double calculateCorrelation(List<Double> x, List<Double> y) {
        double meanX = x.stream().mapToDouble(d -> d).average().orElse(0.0);
        double meanY = y.stream().mapToDouble(d -> d).average().orElse(0.0);

        double numerator = 0;
        double denomX = 0;
        double denomY = 0;

        for (int i = 0; i < x.size(); i++) {
            double diffX = x.get(i) - meanX;
            double diffY = y.get(i) - meanY;

            numerator += diffX * diffY;
            denomX += diffX * diffX;
            denomY += diffY * diffY;
        }

        return numerator / (Math.sqrt(denomX) * Math.sqrt(denomY));
    }
}
