/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.mlp;

/**
 *
 * @author cezerilab
 */
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class DataLoader {

    private double[][] features;
    private double[][] labels;
    private double[][] trainFeatures;
    private double[][] trainLabels;
    private double[][] valFeatures;
    private double[][] valLabels;
    private double[][] testFeatures;
    private double[][] testLabels;
    private int batchSize;
    private Random random;
    private Map<String, Integer> classMap;  // Sınıf isimlerini indekslere eşlemek için

    public DataLoader() {
        this.random = new Random();
        this.batchSize = 32;
        this.classMap = new HashMap<>();
    }

    public void loadCSV(String filepath, int labelColumn, boolean skipHeader) {
        List<String[]> rows = new ArrayList<>();
        Set<String> uniqueClasses = new HashSet<>();

        // Önce benzersiz sınıfları bul
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            if (skipHeader) {
                br.readLine(); // Header'ı atla
            }
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                rows.add(values);
                uniqueClasses.add(values[labelColumn].trim());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataLoader.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Sınıf mapping'ini oluştur
        int classIndex = 0;
        for (String className : uniqueClasses) {
            classMap.put(className, classIndex++);
        }

        // Veriyi features ve labels olarak ayırma
        features = new double[rows.size()][rows.get(0).length - 1];
        labels = new double[rows.size()][uniqueClasses.size()];

        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            int featureIndex = 0;
            for (int j = 0; j < row.length; j++) {
                if (j != labelColumn) {
                    features[i][featureIndex++] = Double.parseDouble(row[j].trim());
                } else {
                    // One-hot encoding yapma
                    int classIdx = classMap.get(row[j].trim());
                    labels[i][classIdx] = 1.0;
                }
            }
        }

        // Normalizasyon
        normalizeFeatures();
    }

    private int getClassIndex(String className) {
        Integer index = classMap.get(className.trim());
        if (index == null) {
            throw new IllegalArgumentException("Unknown class: " + className);
        }
        return index;
    }

    // Sınıf mapping'ini alma
    public Map<String, Integer> getClassMap() {
        return classMap;
    }

    // MNIST görüntülerini okuma
    public void loadMNIST(String imagesPath, String labelsPath) throws IOException {
        // Gzip stream'leri oluşturma
        try (GZIPInputStream gzipImages = new GZIPInputStream(new FileInputStream(imagesPath)); GZIPInputStream gzipLabels = new GZIPInputStream(new FileInputStream(labelsPath)); DataInputStream imagesStream = new DataInputStream(new BufferedInputStream(gzipImages)); DataInputStream labelsStream = new DataInputStream(new BufferedInputStream(gzipLabels))) {

            // Magic number'ları kontrol et
            int imagesMagic = imagesStream.readInt();
            if (imagesMagic != 2051) {
                throw new IOException("Invalid images file magic number: " + imagesMagic);
            }

            int labelsMagic = labelsStream.readInt();
            if (labelsMagic != 2049) {
                throw new IOException("Invalid labels file magic number: " + labelsMagic);
            }

            // Boyutları oku
            int numImages = imagesStream.readInt();
            int numLabels = labelsStream.readInt();

            if (numImages != numLabels) {
                throw new IOException("Image and label counts do not match");
            }

            int numRows = imagesStream.readInt();
            int numCols = imagesStream.readInt();
            int imageSize = numRows * numCols;

            // Feature ve label array'lerini oluştur
            features = new double[numImages][imageSize];
            labels = new double[numImages][10];  // MNIST 10 sınıf içerir (0-9)

            // Veriyi oku
            byte[] pixels = new byte[imageSize];
            for (int i = 0; i < numImages; i++) {
                // Label oku ve one-hot encoding yap
                int label = labelsStream.read();
                labels[i][label] = 1.0;

                // Görüntü piksellerini oku
                imagesStream.readFully(pixels);

                // Pikselleri normalize et ve feature array'ine aktar
                for (int j = 0; j < imageSize; j++) {
                    // Unsigned byte değerini al ve 0-1 arasına normalize et
                    features[i][j] = ((pixels[j] & 0xFF) / 255.0) * 0.99 + 0.01;  // 0-1 yerine 0.01-1 arası
                }

                // İlerleme durumunu göster (her 1000 görüntüde bir)
                if ((i + 1) % 1000 == 0) {
                    System.out.printf("Processed %d/%d images\n", i + 1, numImages);
                }
            }

            System.out.println("MNIST data loaded successfully");
            System.out.println("Number of images: " + numImages);
            System.out.println("Image size: " + numRows + "x" + numCols);
        }
    }

    public void printDataSummary() {
        // İlk birkaç örneği kontrol et
        System.out.println("\nData Summary:");
        System.out.println("First image pixel range:");
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        // İlk görüntünün min-max değerlerini bul
        for (double pixel : features[0]) {
            min = Math.min(min, pixel);
            max = Math.max(max, pixel);
        }
        System.out.printf("Min pixel value: %.4f\n", min);
        System.out.printf("Max pixel value: %.4f\n", max);

        // Label dağılımını kontrol et
        int[] labelCounts = new int[10];
        for (double[] label : labels) {
            int idx = 0;
            for (int i = 0; i < label.length; i++) {
                if (label[i] == 1.0) {
                    idx = i;
                    break;
                }
            }
            labelCounts[idx]++;
        }

        System.out.println("\nLabel distribution:");
        for (int i = 0; i < labelCounts.length; i++) {
            System.out.printf("Class %d: %d samples\n", i, labelCounts[i]);
        }
    }

    // Veriyi train, validation ve test setlerine bölme
    public void splitData(double trainRatio, double valRatio) {
        int totalSize = features.length;
        int trainSize = (int) (totalSize * trainRatio);
        int valSize = (int) (totalSize * valRatio);

        // Veriyi karıştırma
        shuffleData();

        // Train seti
        trainFeatures = Arrays.copyOfRange(features, 0, trainSize);
        trainLabels = Arrays.copyOfRange(labels, 0, trainSize);

        // Validation seti
        valFeatures = Arrays.copyOfRange(features, trainSize, trainSize + valSize);
        valLabels = Arrays.copyOfRange(labels, trainSize, trainSize + valSize);

        // Test seti
        testFeatures = Arrays.copyOfRange(features, trainSize + valSize, totalSize);
        testLabels = Arrays.copyOfRange(labels, trainSize + valSize, totalSize);
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    // Train batches
    public List<Batch> getBatches() {
        return createBatches(trainFeatures, trainLabels);
    }

    // Validation batches
    public List<Batch> getValidationBatches() {
        return createBatches(valFeatures, valLabels);
    }

    // Test batches
    public List<Batch> getTestBatches() {
        return createBatches(testFeatures, testLabels);
    }

    private List<Batch> createBatches(double[][] features, double[][] labels) {
        List<Batch> batches = new ArrayList<>();
        int numBatches = (int) Math.ceil((double) features.length / batchSize);

        for (int i = 0; i < numBatches; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, features.length);

            double[][] batchFeatures = Arrays.copyOfRange(features, start, end);
            double[][] batchLabels = Arrays.copyOfRange(labels, start, end);

            batches.add(new Batch(batchFeatures, batchLabels));
        }

        return batches;
    }

    private void normalizeFeatures() {
        // Min-max normalizasyon
        for (int j = 0; j < features[0].length; j++) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            // Min ve max bulma
            for (double[] feature : features) {
                min = Math.min(min, feature[j]);
                max = Math.max(max, feature[j]);
            }

            // Normalizasyon
            if (max > min) {
                for (double[] feature : features) {
                    feature[j] = (feature[j] - min) / (max - min);
                }
            }
        }
    }

    private void shuffleData() {
        for (int i = features.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            // Features değiş tokuş
            double[] tempFeatures = features[i];
            features[i] = features[j];
            features[j] = tempFeatures;

            // Labels değiş tokuş
            double[] tempLabels = labels[i];
            labels[i] = labels[j];
            labels[j] = tempLabels;
        }
    }

    private int getNumClasses(List<String[]> rows, int labelColumn) {
        Set<String> uniqueClasses = new HashSet<>();
        for (String[] row : rows) {
            uniqueClasses.add(row[labelColumn].trim());
        }
        return uniqueClasses.size();
    }

    // Getter metodları
    public int getTrainSize() {
        return trainFeatures != null ? trainFeatures.length : 0;
    }

    public int getValSize() {
        return valFeatures != null ? valFeatures.length : 0;
    }

    public int getTestSize() {
        return testFeatures != null ? testFeatures.length : 0;
    }

    public int getInputSize() {
        return features != null && features.length > 0 ? features[0].length : 0;
    }

    public int getNumClasses() {
        return labels != null && labels.length > 0 ? labels[0].length : 0;
    }
}

// Batch sınıfı
class Batch {

    public final double[][] features;
    public final double[][] labels;

    public Batch(double[][] features, double[][] labels) {
        this.features = features;
        this.labels = labels;
    }

    public int getSize() {
        return features.length;
    }
}
