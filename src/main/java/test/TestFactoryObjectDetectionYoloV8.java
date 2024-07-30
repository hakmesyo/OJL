/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import ai.djl.engine.Engine;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.ZooModel;
import java.awt.image.BufferedImage;
import java.io.File;
import jazari.factory.FactoryObjectDetectionYoloV8;
import jazari.factory.FactoryUtils;
import jazari.gui.FrameBasicImage;
import jazari.image_processing.ImageProcess;

/**
 *
 * @author cezerilab
 */
public class TestFactoryObjectDetectionYoloV8 {

    public static void main(String[] args) {
//        System.setProperty("ai.djl.onnx.use_cuda", "true");
//        // GPU kullanılabilirliğini kontrol et
//        Engine engine = Engine.getInstance();
//        if (engine.getGpuCount() > 0) {
//            System.out.println("GPU bulundu ve kullanılabilir.");
//        } else {
//            System.out.println("GPU bulunamadı veya kullanılamıyor.");
//        }
        
        String modelPath = "models/my_model/model_n.onnx";
        //String modelPath="models/lane_segment/model.onnx";
        File[] files = FactoryUtils.getFileArrayInFolderByExtension("dataset/ds_simulation/test", "jpg");
        ZooModel<Image, DetectedObjects> model = FactoryObjectDetectionYoloV8.loadModel(modelPath, "OnnxRuntime");
        FrameBasicImage frm = new FrameBasicImage();
        frm.setVisible(true);

        long t1 = FactoryUtils.tic();
        for (File file : files) {
            BufferedImage img = ImageProcess.imread(file);
            DetectedObjects detected = FactoryObjectDetectionYoloV8.predict(model, img, true);
            frm.setImage(img);
            System.out.println("detected = " + detected);
            t1 = FactoryUtils.toc(t1);
        }

        frm.dispose();
    }
}
