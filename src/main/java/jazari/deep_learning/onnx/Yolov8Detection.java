/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.onnx;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import ai.djl.modality.cv.output.Rectangle;
//import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.translator.YoloV8TranslatorFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;
import jazari.matrix.CMatrix;

/**
 * An example of inference using an yolov8 model.
 */
public final class Yolov8Detection {

    private static final Logger logger = LoggerFactory.getLogger(Yolov8Detection.class);

    private Yolov8Detection() {
    }

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        DetectedObjects detection = predict();
        logger.info("{}", detection);
    }

    public static DetectedObjects predict() throws IOException, ModelException, TranslateException {
        //Path imgPath = Paths.get("images/dog_cat.jpg");
        Path imgPath = Paths.get("images/sim_1.jpg");
        Image img = ImageFactory.getInstance().fromFile(imgPath);
        //Path modelPath =Paths.get("C:\\Users\\cezerilab\\Downloads\\yolov8n.zip");
        //Path modelPath =Paths.get("C:\\Users\\cezerilab\\Downloads\\yolov8n_pytorch.zip");
        //Path modelPath =Paths.get("C:\\Users\\cezerilab\\Downloads\\my_model\\model_m.onnx");
        Path modelPath = Paths.get("C:\\Users\\cezerilab\\Downloads\\my_model\\model_n.onnx");
        //Path modelPath =Paths.get("C:\\Users\\cezerilab\\Downloads\\my_model\\model_n.pt");

        // Use DJL OnnxRuntime model zoo model, model can be found:
        // https://mlrepo.djl.ai/model/cv/object_detection/ai/djl/onnxruntime/yolov8n/0.0.1/yolov8n.zip
        Criteria<Image, DetectedObjects> criteria
                = Criteria.builder()
                        .setTypes(Image.class, DetectedObjects.class)
                        //.optModelUrls("djl://ai.djl.onnxruntime/yolov8n")
                        .optModelPath(modelPath)
                        .optEngine("OnnxRuntime")
                        //.optEngine("PyTorch")
                        .optArgument("width", 640)
                        .optArgument("height", 640)
                        .optArgument("resize", true)
                        .optArgument("rescale", true)
                        .optArgument("toTensor", true)
                        .optArgument("applyRatio", true)
                        //.optArgument("threshold", 0.6f)
                        // for performance optimization maxBox parameter can reduce number of
                        // considered boxes from 8400
                        //.optArgument("maxBox", 1000) //bu satır sorunludur açma
                        .optTranslatorFactory(new YoloV8TranslatorFactory())
                        .optProgress(new ProgressBar())
                        .build();

        try (ZooModel<Image, DetectedObjects> model = criteria.loadModel(); 
                Predictor<Image, DetectedObjects> predictor = model.newPredictor()) {
            Path outputPath = Paths.get("build/output");
            Files.createDirectories(outputPath);

            //File[] files = FactoryUtils.getFileArrayInFolderByExtension("C:\\ds_teknofest\\recorded_images\\yolo_ds\\images\\train", "jpg");
            File[] files = FactoryUtils.getFileArrayInFolderByExtension("C:\\ds_teknofest\\recorded_images\\yolo_ds\\images\\test", "jpg");
            long t = FactoryUtils.tic();
            CMatrix cm = CMatrix.getInstance();
            for (File file : files) {
                img = ImageFactory.getInstance().fromFile(file.toPath());
                int imageWidth = img.getWidth();
                int imageHeight = img.getHeight();
                DetectedObjects detection = predictor.predict(img);
                BufferedImage imgx = (BufferedImage) img.getWrappedImage();
                List<DetectedObjects.DetectedObject> list = detection.items();
                for (DetectedObject dt : list) {
                    BoundingBox bbox = dt.getBoundingBox();
                    Rectangle rect = bbox.getBounds();
                    int x = (int) (rect.getX() * imageWidth);
                    int y = (int) (rect.getY() * imageHeight);
                    int w=(int) (rect.getWidth() * imageWidth);
                    int h=(int) (rect.getHeight() * imageHeight);
                    java.awt.Rectangle r=new java.awt.Rectangle(x, y, w, h);
                    imgx = ImageProcess.drawRectangle(imgx, r, 2, Color.yellow);
                    imgx=ImageProcess.drawText(imgx, dt.getClassName(), x, y-5, Color.yellow);
                }
                
                cm.setImage(imgx).imshowRefresh();
                //
//                img.drawBoundingBoxes(detection);
//                cm.setImage((BufferedImage) img.getWrappedImage()).imshowRefresh();
                t = FactoryUtils.toc(t);
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException ex) {
//                    java.util.logging.Logger.getLogger(Yolov8Detection.class.getName()).log(Level.SEVERE, null, ex);
//                }
            }

            DetectedObjects detection = predictor.predict(img);
            if (detection.getNumberOfObjects() > 0) {
                img.drawBoundingBoxes(detection);
                Path output = outputPath.resolve("yolov8_detected.png");
                try (OutputStream os = Files.newOutputStream(output)) {
                    img.save(os, "png");
                }
                logger.info("Detected object saved in: {}", output);
            }
            return detection;
        }
    }
}
