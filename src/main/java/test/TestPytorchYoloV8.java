/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

/**
 *
 * @author Teknofest
 */
import ai.djl.Device;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.training.util.ProgressBar;
import java.io.File;

public class TestPytorchYoloV8 {
    public static void main(String[] args) throws Exception {
        // Model yolu
        String modelPath = "models/my_model/model.pt";

        // GPU kullanımını etkinleştir
        Device device = Device.gpu();

        // Model yükleme kriterleri
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optModelPath(new File(modelPath).toPath())
                .optEngine("PyTorch")
                .optDevice(device)
                .build();

        // Modeli yükle
        try (Predictor<Image, DetectedObjects> predictor = criteria.loadModel().newPredictor()) {
            // Test görüntüsü yükle
//            Image img = ImageFactory.getInstance().fromFile(new File("path/to/test/image.jpg"));
//
//            // Tahmin yap
//            DetectedObjects detection = predictor.predict(img);
//
//            // Sonuçları yazdır
//            System.out.println(detection);
        }
    }
}