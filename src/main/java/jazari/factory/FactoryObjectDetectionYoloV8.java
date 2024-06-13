/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.factory;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.translator.YoloV8TranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cezerilab
 */
public class FactoryObjectDetectionYoloV8 {

    private static Predictor<Image, DetectedObjects> predictor = null;

    /**
     * load Yolov8 models trained and converted as onnx, mxnet, pytorch or
     * tensorflow synset.txt which holds class names should exist in model
     * folder as well
     *
     * @param modelPathStr:model path
     * @param engineStr:write on of the "OnnxRuntime", "PyTorch", "MXNet",
     * "TensorFlow"
     * @return loaded model
     */
    public static ZooModel<Image, DetectedObjects> loadModel(String modelPathStr, String engineStr) {
        Path modelPath = Paths.get(modelPathStr);
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
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
            Logger.getLogger(FactoryObjectDetectionYoloV8.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * predict bboxes for the image
     *
     * @param model : model loaded
     * @param imageFile : File
     * @return
     */
    public static DetectedObjects predict(ZooModel<Image, DetectedObjects> model, File imageFile) {
        try {
            Image img = ImageFactory.getInstance().fromFile(imageFile.toPath());
            DetectedObjects detection = predictor.predict(img);
            return detection;
        } catch (IOException | TranslateException ex) {
            Logger.getLogger(FactoryObjectDetectionYoloV8.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static DetectedObjects predict(ZooModel<Image, DetectedObjects> model, File imageFile, boolean isBboxShown) {
        Image img;
        BufferedImage bimg = null;
        try {
            img = ImageFactory.getInstance().fromFile(imageFile.toPath());
            bimg = (BufferedImage) img;
        } catch (IOException ex) {
            Logger.getLogger(FactoryObjectDetectionYoloV8.class.getName()).log(Level.SEVERE, null, ex);
        }
        return predict(model, bimg, isBboxShown);
    }

    public static DetectedObjects predict(ZooModel<Image, DetectedObjects> model, BufferedImage image) {
        Image img = ImageFactory.getInstance().fromImage(image);
        DetectedObjects detection = null;
        try {
            detection = predictor.predict(img);
        } catch (TranslateException ex) {
            Logger.getLogger(FactoryObjectDetectionYoloV8.class.getName()).log(Level.SEVERE, null, ex);
        }
        return detection;
    }

    public static DetectedObjects predict(ZooModel<Image, DetectedObjects> model, BufferedImage image, boolean isBboxShown) {
        Image img = ImageFactory.getInstance().fromImage(image);
        DetectedObjects detection = null;
        try {
            detection = predictor.predict(img);
            if (isBboxShown) {
                Graphics2D g = (Graphics2D) image.getGraphics();
                int stroke = 2;
                g.setStroke(new BasicStroke(stroke));
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int imageWidth = image.getWidth();
                int imageHeight = image.getHeight();

                List<DetectedObjects.DetectedObject> list = detection.items();
                int k = 10;
                Map<String, Integer> classNumberTable = new ConcurrentHashMap<>();
                for (DetectedObjects.DetectedObject result : list) {
                    String className = result.getClassName();
                    BoundingBox box = result.getBoundingBox();
                    g.setPaint(Color.YELLOW);
//                    if (classNumberTable.containsKey(className)) {
//                        g.setPaint(new Color(classNumberTable.get(className)));
//                    } else {
//                        g.setPaint(new Color(k));
//                        classNumberTable.put(className, k);
//                        k = (k + 100) % 255;
//                    }

                    Rectangle rectangle = box.getBounds();
                    int x = (int) (rectangle.getX() * imageWidth);
                    int y = (int) (rectangle.getY() * imageHeight);
                    g.drawRect(
                            x,
                            y,
                            (int) (rectangle.getWidth() * imageWidth),
                            (int) (rectangle.getHeight() * imageHeight));
                    drawText(g, className, x-2, y-25, stroke, 4);
                }
                g.dispose();
            }
        } catch (TranslateException ex) {
            Logger.getLogger(FactoryObjectDetectionYoloV8.class.getName()).log(Level.SEVERE, null, ex);
        }
        return detection;
    }

    private static void drawText(Graphics2D g, String text, int x, int y, int stroke, int padding) {
        FontMetrics metrics = g.getFontMetrics();
        x += stroke / 2;
        y += stroke / 2;
        int width = metrics.stringWidth(text) + padding * 2 - stroke / 2;
        int height = metrics.getHeight() + metrics.getDescent();
        int ascent = metrics.getAscent();
        java.awt.Rectangle background = new java.awt.Rectangle(x, y, width, height);
        g.setColor(Color.black);
        g.fillRect(x, y-5, width+10, height+10);        
        g.fill(background);
        g.setPaint(Color.WHITE);
        g.drawString(text, x + padding, y + ascent);
    }
}
