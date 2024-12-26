/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.data_loader;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author cezerilab
 */
/**
 * NumPy Dataset Implementation for .npy files
 */
public class NumpyDataset extends SimpleDataset {

    public NumpyDataset(String npyFilePath) throws IOException {
        super(0, 0);  // Geçici değerler, loadNumpyData'da güncellenecek
        loadNumpyData(npyFilePath);
    }

    private void loadNumpyData(String npyFilePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(npyFilePath); DataInputStream dis = new DataInputStream(fis)) {

            // NPY format magic number ve versiyon kontrolü
            byte[] magic = new byte[6];
            dis.readFully(magic);
            if (!new String(magic, 0, 6).equals("\u0093NUMPY")) {
                throw new IOException("Invalid NPY file format");
            }

            byte major = dis.readByte();
            byte minor = dis.readByte();

            // Header length oku
            short headerLen = dis.readShort();

            // Header string'i oku
            byte[] headerBytes = new byte[headerLen];
            dis.readFully(headerBytes);
            String header = new String(headerBytes);

            // Header'dan shape ve dtype bilgisini parse et
            // Örnek header: "{'descr': '<f8', 'fortran_order': False, 'shape': (100, 5)}"
            String[] parts = header.split(",");
            int[] shape = parseShape(header);

            if (shape.length != 2) {
                throw new IOException("Only 2D arrays are supported");
            }

            // Shape bilgisinden feature ve label boyutlarını ayarla
            this.featureSize = shape[1] - 1;  // Son sütun label
            this.labelSize = countUniqueLabels(dis, shape);

            // Veriyi oku
            for (int i = 0; i < shape[0]; i++) {
                double[] features = new double[featureSize];
                double[] labels = new double[labelSize];

                // Feature'ları oku
                for (int j = 0; j < featureSize; j++) {
                    features[j] = dis.readDouble();
                }

                // Label'ı oku ve one-hot encoding yap
                int label = (int) dis.readDouble();
                labels[label] = 1.0;

                samples.add(new Sample(features, labels));
            }
        }
    }

    private int[] parseShape(String header) {
        // Shape bilgisini header'dan çıkar
        int start = header.indexOf("shape") + 7;
        int end = header.indexOf(")", start);
        String shapeStr = header.substring(start, end);
        String[] dims = shapeStr.split(",");
        int[] shape = new int[dims.length];
        for (int i = 0; i < dims.length; i++) {
            shape[i] = Integer.parseInt(dims[i].trim());
        }
        return shape;
    }

    private int countUniqueLabels(DataInputStream dis, int[] shape) throws IOException {
        // Dosyanın başına dön
        dis.reset();
        Set<Integer> uniqueLabels = new HashSet<>();

        // Her satırın son sütununu oku ve unique label'ları say
        for (int i = 0; i < shape[0]; i++) {
            dis.skipBytes(shape[1] * 8 - 8);  // Son sütuna kadar atla
            uniqueLabels.add((int) dis.readDouble());
        }

        return uniqueLabels.size();
    }
}