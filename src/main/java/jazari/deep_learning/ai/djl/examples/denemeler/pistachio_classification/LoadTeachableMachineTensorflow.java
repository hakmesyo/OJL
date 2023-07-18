/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ai.djl.examples.denemeler.pistachio_classification;

import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.*;
import ai.djl.modality.*;
import ai.djl.modality.cv.*;
import ai.djl.modality.cv.util.*;
import ai.djl.ndarray.*;
import ai.djl.repository.zoo.*;
import ai.djl.translate.*;
import ai.djl.training.util.*;
import ai.djl.util.*;
import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import org.tensorflow.TensorFlow;

/**
 *
 * @author cezerilab
 */
public class LoadTeachableMachineTensorflow {

    static String modelUrl = "https://resources.djl.ai/demo/pneumonia-detection-model/saved_model.zip";

    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        String imagePath = "https://resources.djl.ai/images/chest_xray.jpg";
        Image image = ImageFactory.getInstance().fromUrl(imagePath);
        image.getWrappedImage();

        Criteria<Image, Classifications> criteria
                = Criteria.builder()
                        .setTypes(Image.class, Classifications.class)
                        .optModelUrls(modelUrl)
                        .optTranslator(new MyTranslator())
                        .optEngine("TensorFlow")
                        .optDevice(Device.cpu())
                        .optProgress(new ProgressBar())
                        .build();
        Engine.getEngine("TensorFlow");
        TensorFlow.loadLibrary("C:\\Users\\cezerilab\\.djl.ai\\tensorflow\\2.10.1-cpu-win-x86_64/jnitensorflow.dll");
        ZooModel model = criteria.loadModel();

        Predictor<Image, Classifications> predictor = model.newPredictor();
        Classifications classifications = predictor.predict(image);
        System.out.println("classifications = " + classifications);
    }
}

class MyTranslator implements Translator<Image, Classifications> {

    private static final List<String> CLASSES = Arrays.asList("Normal", "Pneumonia");

    @Override
    public NDList processInput(TranslatorContext ctx, Image input) {
        NDManager manager = ctx.getNDManager();
        NDArray array = input.toNDArray(manager, Image.Flag.COLOR);
        array = NDImageUtils.resize(array, 224).div(255.0f);
        return new NDList(array);
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) {
        NDArray probabilities = list.singletonOrThrow();
        return new Classifications(CLASSES, probabilities);
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
}
