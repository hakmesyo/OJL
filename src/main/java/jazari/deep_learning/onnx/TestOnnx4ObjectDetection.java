/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.onnx;

import ai.djl.Application;
import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jazari.factory.FactoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestOnnx4ObjectDetection {

    private static final Logger logger = LoggerFactory.getLogger(TestOnnx4ObjectDetection.class);

    private TestOnnx4ObjectDetection() {
    }

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        DetectedObjects detection = predict();
        logger.info("{}", detection);
    }

    public static DetectedObjects predict() throws IOException, ModelException, TranslateException {
        Path imageFile = Paths.get("images/dog_cat.jpg");
        Image img = ImageFactory.getInstance().fromFile(imageFile);
        //Path modelPath =Paths.get("C:\\Users\\cezerilab\\Downloads\\my_model.pt");

        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                        .optApplication(Application.CV.OBJECT_DETECTION)
                        .setTypes(Image.class, DetectedObjects.class)
                        .optArgument("threshold", 0.5)
                        //.optModelName("my_model")
                        //.optModelPath(modelPath)
                        //.optEngine("PyTorch")
                        .optEngine("OnnxRuntime")
                        //.optEngine("MXNet")
                        .optProgress(new ProgressBar())
                        .build();
        ZooModel<Image, DetectedObjects> model = criteria.loadModel();
        Predictor<Image, DetectedObjects> predictor = model.newPredictor();
        long t=FactoryUtils.tic();
        for (int i = 0; i < 100; i++) {
            DetectedObjects detection = predictor.predict(img);
            //saveBoundingBoxImage(img, detection);
            t=FactoryUtils.toc(t);
        }
        DetectedObjects detection = predictor.predict(img);
        saveBoundingBoxImage(img, detection);
        return detection;
    }

    private static void saveBoundingBoxImage(Image img, DetectedObjects detection)
            throws IOException {
        Path outputDir = Paths.get("build/output");
        Files.createDirectories(outputDir);

        img.drawBoundingBoxes(detection);

        Path imagePath = outputDir.resolve("detected-dog_bike_car.png");
        // OpenJDK can't save jpg with alpha channel
        img.save(Files.newOutputStream(imagePath), "png");
        logger.info("Detected objects image has been saved in: {}", imagePath);
    }
}
