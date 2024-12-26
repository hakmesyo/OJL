/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.data_loader;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author cezerilab
 */
/**
 * MNIST Dataset Implementation
 */
public class MNISTDataset extends SimpleDataset {

    public MNISTDataset(String imagesPath, String labelsPath) throws IOException {
        super(784, 10);  // 28x28=784 features, 10 classes
        loadMNISTData(imagesPath, labelsPath);
    }

    private void loadMNISTData(String imagesPath, String labelsPath) throws IOException {
        try (GZIPInputStream gzipImages = new GZIPInputStream(new FileInputStream(imagesPath)); GZIPInputStream gzipLabels = new GZIPInputStream(new FileInputStream(labelsPath)); DataInputStream imagesStream = new DataInputStream(new BufferedInputStream(gzipImages)); DataInputStream labelsStream = new DataInputStream(new BufferedInputStream(gzipLabels))) {

            if (imagesStream.readInt() != 2051 || labelsStream.readInt() != 2049) {
                throw new IOException("Invalid MNIST file format");
            }

            int numImages = imagesStream.readInt();
            int numLabels = labelsStream.readInt();
            int numRows = imagesStream.readInt();
            int numCols = imagesStream.readInt();

            byte[] pixels = new byte[numRows * numCols];
            for (int i = 0; i < numImages; i++) {
                double[] features = new double[numRows * numCols];
                double[] labels = new double[10];

                imagesStream.readFully(pixels);
                int label = labelsStream.read();

                for (int j = 0; j < pixels.length; j++) {
                    features[j] = (pixels[j] & 0xFF) / 255.0;
                }

                labels[label] = 1.0;
                samples.add(new Sample(features, labels));
            }
        }
    }
}
