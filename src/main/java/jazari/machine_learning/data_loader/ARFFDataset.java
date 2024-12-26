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
import java.util.List;
import java.util.Map;

/**
 *
 * @author cezerilab
 */
public class ARFFDataset extends SimpleDataset {

    public ARFFDataset(String filePath) throws IOException {
        super(0, 0);  // Geçici değerler
        loadARFFData(filePath);
    }

    private void loadARFFData(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        List<String> attributeNames = new ArrayList<>();
        List<Boolean> isNominal = new ArrayList<>();  // Her attribute'un nominal olup olmadığını tut
        Map<Integer, Map<String, Integer>> nominalMaps = new HashMap<>();  // Her nominal attribute için mapping
        boolean dataSection = false;

        // Header parsing
        while ((line = reader.readLine()) != null) {
            line = line.trim().toLowerCase();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("@attribute")) {
                String[] parts = line.split("\\s+", 3);  // En fazla 3 parça
                attributeNames.add(parts[1].replace("'", "").trim());

                // Attribute tipini belirle
                boolean nominal = parts[2].contains("{");
                isNominal.add(nominal);

                // Eğer nominal ise değerlerini maple
                if (nominal) {
                    String values = parts[2].substring(parts[2].indexOf("{") + 1, parts[2].indexOf("}"));
                    String[] nominalValues = values.split(",");
                    Map<String, Integer> valueMap = new HashMap<>();
                    for (int i = 0; i < nominalValues.length; i++) {
                        valueMap.put(nominalValues[i].trim(), i);
                    }
                    nominalMaps.put(attributeNames.size() - 1, valueMap);
                }
            } else if (line.startsWith("@data")) {
                dataSection = true;
                break;
            }
        }

        // Son attribute'un tipine göre problem tipini belirle
        boolean isClassification = isNominal.get(isNominal.size() - 1);

        // Feature ve label boyutlarını ayarla
        this.featureSize = attributeNames.size() - 1;
        if (isClassification) {
            Map<String, Integer> labelMap = nominalMaps.get(featureSize);
            this.labelSize = labelMap.size();  // Sınıf sayısı kadar
        } else {
            this.labelSize = 1;  // Regression için tek boyut
        }

        // Veriyi oku
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty() && !line.startsWith("%")) {
                String[] values = line.split(",");
                double[] features = new double[featureSize];
                double[] labels;

                // Feature'ları parse et
                for (int i = 0; i < featureSize; i++) {
                    if (isNominal.get(i)) {
                        // Nominal değer
                        Map<String, Integer> valueMap = nominalMaps.get(i);
                        features[i] = valueMap.get(values[i].trim());
                    } else {
                        // Numerik değer
                        features[i] = Double.parseDouble(values[i].trim());
                    }
                }

                // Label'ı parse et
                if (isClassification) {
                    // Sınıflandırma için one-hot encoding
                    labels = new double[labelSize];
                    Map<String, Integer> labelMap = nominalMaps.get(featureSize);
                    int labelIndex = labelMap.get(values[featureSize].trim());
                    labels[labelIndex] = 1.0;
                } else {
                    // Regression için tek değer
                    labels = new double[1];
                    labels[0] = Double.parseDouble(values[featureSize].trim());
                }

                samples.add(new Sample(features, labels));
            }
        }

        System.out.println("\nARFF data loaded successfully:");
        System.out.println("Total samples: " + samples.size());
        System.out.println("Features per sample: " + featureSize);
        System.out.println("Problem type: " + (isClassification ? "Classification" : "Regression"));
        if (isClassification) {
            System.out.println("Number of classes: " + labelSize);
        }

        // Attribute tiplerini yazdır
        System.out.println("\nAttribute types:");
        for (int i = 0; i < attributeNames.size(); i++) {
            System.out.printf("%s: %s%n",
                    attributeNames.get(i),
                    isNominal.get(i) ? "Nominal" : "Numeric");
        }
    }
}
