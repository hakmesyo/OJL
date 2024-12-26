/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.data_loader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author cezerilab
 */
/**
 * Text Dataset Implementation for custom text formats
 */
public class TextDataset extends SimpleDataset {

    public TextDataset(String filePath, String delimiter, int labelColumn) throws IOException {
        super(0, 0);  // Geçici değerler, loadTextData'da güncellenecek
        loadTextData(filePath, delimiter, labelColumn);
    }

    private void loadTextData(String filePath, String delimiter, int labelColumn) throws IOException {
        List<String[]> rows = new ArrayList<>();
        Set<String> uniqueClasses = new HashSet<>();

        // İlk geçiş: Veriyi oku ve unique sınıfları bul
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Yorum satırlarını ve boş satırları atla
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                String[] values = line.split(delimiter);
                if (values.length > labelColumn) {
                    rows.add(values);
                    uniqueClasses.add(values[labelColumn].trim());
                }
            }
        }

        if (rows.isEmpty()) {
            throw new IOException("No valid data found in file");
        }

        // Feature ve label boyutlarını ayarla
        this.featureSize = rows.get(0).length - 1;
        this.labelSize = uniqueClasses.size();

        // Sınıf mapping'i oluştur
        Map<String, Integer> classMap = new HashMap<>();
        int classIndex = 0;
        for (String className : uniqueClasses) {
            classMap.put(className, classIndex++);
        }

        // İkinci geçiş: Veriyi Sample'lara dönüştür
        for (String[] row : rows) {
            try {
                double[] features = new double[featureSize];
                double[] labels = new double[labelSize];

                int featureIndex = 0;
                for (int i = 0; i < row.length; i++) {
                    if (i != labelColumn) {
                        features[featureIndex++] = Double.parseDouble(row[i].trim());
                    } else {
                        String className = row[i].trim();
                        labels[classMap.get(className)] = 1.0;
                    }
                }

                samples.add(new Sample(features, labels));
            } catch (NumberFormatException e) {
                System.err.println("Warning: Skipping invalid row: " + String.join(delimiter, row));
            }
        }
    }
}

