/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.machine_learning.data_loader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author cezerilab
 */
/**
 * Image Dataset Implementation
 */
public class ImageDataset extends SimpleDataset {

    public ImageDataset(String folderPath, int width, int height) throws IOException {
        super(width * height * 3, 0);  // RGB için 3 kanal
        loadImagesFromFolder(folderPath, width, height);
    }

    private void loadImagesFromFolder(String folderPath, int width, int height) throws IOException {
        File folder = new File(folderPath);
        File[] classFolders = folder.listFiles(File::isDirectory);

        if (classFolders != null) {
            labelSize = classFolders.length;

            for (int classIndex = 0; classIndex < classFolders.length; classIndex++) {
                File[] imageFiles = classFolders[classIndex].listFiles(
                        (dir, name) -> name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp)$")
                );

                if (imageFiles != null) {
                    for (File imageFile : imageFiles) {
                        BufferedImage img = ImageIO.read(imageFile);
                        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                        resized.getGraphics().drawImage(img, 0, 0, width, height, null);

                        double[] features = new double[width * height * 3];
                        double[] labels = new double[labelSize];

                        int index = 0;
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                int rgb = resized.getRGB(x, y);
                                features[index++] = ((rgb >> 16) & 0xFF) / 255.0;  // R
                                features[index++] = ((rgb >> 8) & 0xFF) / 255.0;   // G
                                features[index++] = (rgb & 0xFF) / 255.0;          // B
                            }
                        }

                        labels[classIndex] = 1.0;
                        samples.add(new Sample(features, labels));
                    }
                }
            }
        }
    }
}
