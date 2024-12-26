/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.data_loader;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author cezerilab
 */
/**
 * MATLAB Dataset Implementation for .mat files
 */
public class MATLABDataset extends SimpleDataset {

    public MATLABDataset(String matFilePath, String featuresVarName, String labelsVarName) throws IOException {
        super(0, 0);  // Geçici değerler, loadMatlabData'da güncellenecek
        loadMatlabData(matFilePath, featuresVarName, labelsVarName);
    }

    private void loadMatlabData(String matFilePath, String featuresVarName, String labelsVarName) throws IOException {
        try {
            // MAT dosyasını oku
            MatFileReader matReader = new MatFileReader(matFilePath);

            // Features matrisini al
            MLArray featuresArray = matReader.getMLArray(featuresVarName);
            if (!(featuresArray instanceof MLDouble)) {
                throw new IOException("Features variable must be a double array");
            }
            MLDouble features = (MLDouble) featuresArray;

            // Labels matrisini al
            MLArray labelsArray = matReader.getMLArray(labelsVarName);
            if (!(labelsArray instanceof MLDouble)) {
                throw new IOException("Labels variable must be a double array");
            }
            MLDouble labels = (MLDouble) labelsArray;

            // Boyutları kontrol et
            if (features.getM() != labels.getM()) {
                throw new IOException("Number of samples in features and labels must match");
            }

            // Feature ve label boyutlarını ayarla
            this.featureSize = features.getN();
            Set<Double> uniqueLabels = new HashSet<>();
            for (int i = 0; i < labels.getM(); i++) {
                uniqueLabels.add(labels.get(i, 0));
            }
            this.labelSize = uniqueLabels.size();

            // Sınıf mapping'i oluştur
            Map<Double, Integer> classMap = new HashMap<>();
            int classIndex = 0;
            for (Double label : uniqueLabels) {
                classMap.put(label, classIndex++);
            }

            // Verileri Sample'lara dönüştür
            for (int i = 0; i < features.getM(); i++) {
                double[] featureArray = new double[featureSize];
                double[] labelArray = new double[labelSize];

                // Features kopyala
                for (int j = 0; j < featureSize; j++) {
                    featureArray[j] = features.get(i, j);
                }

                // Label'ı one-hot encoding yap
                int labelIndex = classMap.get(labels.get(i, 0));
                labelArray[labelIndex] = 1.0;

                samples.add(new Sample(featureArray, labelArray));
            }

            System.out.println("\nMATLAB data loaded:");
            System.out.println("Number of samples: " + features.getM());
            System.out.println("Number of features: " + featureSize);
            System.out.println("Number of classes: " + labelSize);

        } catch (IOException e) {
            throw new IOException("Error reading MAT file: " + e.getMessage());
        }
    }
}