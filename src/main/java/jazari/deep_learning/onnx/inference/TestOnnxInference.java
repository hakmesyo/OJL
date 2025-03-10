/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.onnx.inference;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.ZooModel;
import java.awt.image.BufferedImage;
import java.util.List;
import jazari.factory.FactoryObjectDetectionYoloV8;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

/**
 *
 * @author Cezeri
 */
public class TestOnnxInference {

    public static void main(String[] args) {
        //ZooModel<Image, DetectedObjects> model= FactoryObjectDetectionYoloV8.loadModel("C:\\Users\\Cezeri\\Downloads\\den1.onnx", "OnnxRuntime");
        ZooModel<Image, DetectedObjects> model = FactoryObjectDetectionYoloV8.loadModel("models/my_model/model_m.onnx", "OnnxRuntime");
        System.out.println("model = " + model);
        System.out.println("bugün hava çok güzelş");
        BufferedImage tempImg = CMatrix.getInstance().imread("images/sim_1.jpg").getBufferedImage();
        //BufferedImage tempImg=CMatrix.getInstance().imread("C:\\Users\\Cezeri\\Downloads\\den1.jpg").getBufferedImage();
        long t1=FactoryUtils.tic();
        for (int i = 0; i < 10; i++) {
            DetectedObjects detectedObjects = FactoryObjectDetectionYoloV8.predict(model, tempImg, true);
            //System.out.println("detectedObjects = " + detectedObjects);
            List<DetectedObjects.DetectedObject> list = detectedObjects.items();
            for (DetectedObjects.DetectedObject detectedObject : list) {
                System.out.println("detectedObject = " + detectedObject);
            }
            t1=FactoryUtils.toc(t1);
        }

    }
}
