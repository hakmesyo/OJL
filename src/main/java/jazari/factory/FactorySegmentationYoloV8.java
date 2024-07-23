/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.factory;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Mask;
import ai.djl.modality.cv.translator.YoloV8TranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FactorySegmentationYoloV8 {

    private static Predictor<Image, DetectedObjects> predictor = null;

    public static ZooModel<Image, DetectedObjects> loadModel(String modelPathStr, String engineStr) {
        Path modelPath = Paths.get(modelPathStr);
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .optApplication(Application.CV.INSTANCE_SEGMENTATION)
                .setTypes(Image.class, DetectedObjects.class)
                .optModelPath(modelPath)
                .optEngine(engineStr)
                .optArgument("width", 640)
                .optArgument("height", 640)
                .optArgument("resize", true)
                .optArgument("rescale", true)
                .optArgument("toTensor", true)
                .optArgument("applyRatio", true)
                .optTranslatorFactory(new YoloV8TranslatorFactory())
                .optProgress(new ProgressBar())
                .build();
        try {
            ZooModel<Image, DetectedObjects> model = criteria.loadModel();
            predictor = model.newPredictor();
            return model;
        } catch (IOException | ModelNotFoundException | MalformedModelException ex) {
            Logger.getLogger(FactorySegmentationYoloV8.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static DetectedObjects predict(ZooModel<Image, DetectedObjects> model, BufferedImage image, boolean isMaskShown) {
        Image img = ImageFactory.getInstance().fromImage(image);
        DetectedObjects detection = null;
        try {
            detection = predictor.predict(img);
            if (isMaskShown) {
                drawSegmentationResults(image, detection);
            }
        } catch (TranslateException ex) {
            Logger.getLogger(FactorySegmentationYoloV8.class.getName()).log(Level.SEVERE, null, ex);
        }
        return detection;
    }

    private static void drawSegmentationResults(BufferedImage image, DetectedObjects detection) {
        Graphics2D g = (Graphics2D) image.getGraphics();
        int width = image.getWidth();
        int height = image.getHeight();

        List<DetectedObjects.DetectedObject> list = detection.items();
        for (DetectedObjects.DetectedObject result : list) {
            String className = result.getClassName();
            Mask mask = (Mask) result.getBoundingBox();
            

            float r = (float) Math.random();
            float g1 = (float) Math.random();
            float b = (float) Math.random();
            Color color = new Color(r, g1, b, 0.5f);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
//                    if (mask.getProbDist().inMask(x, y)) {
//                        image.setRGB(x, y, color.getRGB());
//                    }
                }
            }
        }
        g.dispose();
    }
}
