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
 * JSON Dataset Implementation
 */
public class JSONDataset extends SimpleDataset {

    public JSONDataset(String filePath, String featuresKey, String labelKey) throws IOException {
        super(0, 0);  // Geçici değerler, loadJSONData'da güncellenecek
        loadJSONData(filePath, featuresKey, labelKey);
    }

    private void loadJSONData(String filePath, String featuresKey, String labelKey) throws IOException {
        StringBuilder jsonContent = new StringBuilder();
        Set<String> uniqueClasses = new HashSet<>();

        // JSON dosyasını oku
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }
        }

        try {
            // Basit JSON parser implementasyonu
            String json = jsonContent.toString().trim();
            if (!json.startsWith("[")) {
                throw new IOException("JSON must be an array of objects");
            }

            // Array brackets'ları kaldır
            json = json.substring(1, json.length() - 1);

            // Her bir objeyi parse et
            List<Map<String, Object>> records = new ArrayList<>();
            int depth = 0;
            int startIndex = 0;

            for (int i = 0; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        String record = json.substring(startIndex, i + 1);
                        records.add(parseJsonObject(record));
                        startIndex = i + 2;  // Skip comma and space
                    }
                }
            }

            if (records.isEmpty()) {
                throw new IOException("No valid records found in JSON");
            }

            // Feature ve label boyutlarını belirle
            Map<String, Object> firstRecord = records.get(0);
            List<Double> firstFeatures = (List<Double>) firstRecord.get(featuresKey);
            this.featureSize = firstFeatures.size();

            // Unique sınıfları topla
            for (Map<String, Object> record : records) {
                uniqueClasses.add(record.get(labelKey).toString());
            }
            this.labelSize = uniqueClasses.size();

            // Sınıf mapping'i oluştur
            Map<String, Integer> classMap = new HashMap<>();
            int classIndex = 0;
            for (String className : uniqueClasses) {
                classMap.put(className, classIndex++);
            }

            // Verileri Sample'lara dönüştür
            for (Map<String, Object> record : records) {
                List<Double> features = (List<Double>) record.get(featuresKey);
                String className = record.get(labelKey).toString();

                double[] featureArray = new double[featureSize];
                double[] labelArray = new double[labelSize];

                for (int i = 0; i < features.size(); i++) {
                    featureArray[i] = features.get(i);
                }

                labelArray[classMap.get(className)] = 1.0;

                samples.add(new Sample(featureArray, labelArray));
            }

        } catch (Exception e) {
            throw new IOException("Error parsing JSON: " + e.getMessage());
        }
    }

    private Map<String, Object> parseJsonObject(String jsonObject) {
        Map<String, Object> result = new HashMap<>();
        // Basit JSON object parser
        // { ve } karakterlerini kaldır
        jsonObject = jsonObject.substring(1, jsonObject.length() - 1);

        // Key-value pairs'leri parse et
        String[] pairs = jsonObject.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim();

            if (value.startsWith("[")) {
                // Array değer
                value = value.substring(1, value.length() - 1);
                String[] elements = value.split(",");
                List<Double> list = new ArrayList<>();
                for (String element : elements) {
                    list.add(Double.parseDouble(element.trim()));
                }
                result.put(key, list);
            } else if (value.startsWith("\"")) {
                // String değer
                result.put(key, value.substring(1, value.length() - 1));
            } else {
                // Sayısal değer
                result.put(key, Double.parseDouble(value));
            }
        }

        return result;
    }
}
