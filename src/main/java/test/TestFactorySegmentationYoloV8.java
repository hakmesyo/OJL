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
import jazari.factory.FactorySegmentationYoloV8;
import jazari.factory.FactoryUtils;
import jazari.gui.FrameBasicImage;
import jazari.image_processing.ImageProcess;

public class TestFactorySegmentationYoloV8 {
    public static void main(String[] args) {
        Engine.getAllEngines();
        String modelPath = "models/lane_segment/model.onnx";
        File[] files = FactoryUtils.getFileArrayInFolderByExtension("dataset/ds_simulation/test", "jpg");
        ZooModel<Image, DetectedObjects> model = FactorySegmentationYoloV8.loadModel(modelPath, "OnnxRuntime");
        FrameBasicImage frm = new FrameBasicImage();
        frm.setVisible(true);
        
        long t1 = FactoryUtils.tic();
        for (File file : files) {
            BufferedImage img = ImageProcess.imread(file);
            DetectedObjects detected = FactorySegmentationYoloV8.predict(model, img, true);
            frm.setImage(img);
            System.out.println("detected = " + detected);
            t1 = FactoryUtils.toc(t1);
        }
    }
}