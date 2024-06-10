/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.onnx;

import ai.djl.inference.*;
import ai.djl.modality.*;
import ai.djl.ndarray.*;
import ai.djl.ndarray.types.*;
import ai.djl.repository.zoo.*;
import ai.djl.translate.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import jazari.factory.FactoryUtils;

public class TestOnnx4Classification {

    public static class IrisFlower {

        public float sepalLength;
        public float sepalWidth;
        public float petalLength;
        public float petalWidth;

        public IrisFlower(float sepalLength, float sepalWidth, float petalLength, float petalWidth) {
            this.sepalLength = sepalLength;
            this.sepalWidth = sepalWidth;
            this.petalLength = petalLength;
            this.petalWidth = petalWidth;
        }
    }

    public static class MyTranslator implements NoBatchifyTranslator<IrisFlower, Classifications> {

        private final List<String> synset;

        public MyTranslator() {
            // species name
            synset = Arrays.asList("setosa", "versicolor", "virginica");
        }

        @Override
        public NDList processInput(TranslatorContext ctx, IrisFlower input) {
            float[] data = {input.sepalLength, input.sepalWidth, input.petalLength, input.petalWidth};
            NDArray array = ctx.getNDManager().create(data, new Shape(1, 4));
            return new NDList(array);
        }

        @Override
        public Classifications processOutput(TranslatorContext ctx, NDList list) {
            float[] data = list.get(1).toFloatArray();
            List<Double> probabilities = new ArrayList(data.length);
            for (float f : data) {
                probabilities.add((double) f);
            }
            return new Classifications(synset, probabilities);
        }
    }

    public static void main(String[] args) throws Exception {
        //ai.djl.modality.cv.MultiBoxDetection
        
        String modelUrl = "https://mlrepo.djl.ai/model/tabular/softmax_regression/ai/djl/onnxruntime/iris_flowers/0.0.1/iris_flowers.zip";
        //Path modelPath =Paths.get("C:\\Users\\dell_lab\\Downloads\\iris_flowers\\iris_flowers.onnx");
        Criteria<IrisFlower, Classifications> criteria = Criteria.builder()
                .setTypes(IrisFlower.class, Classifications.class)
                //.optModelPath(modelPath)
                .optModelUrls(modelUrl)
                .optTranslator(new MyTranslator())
                .optEngine("OnnxRuntime") // use OnnxRuntime engine by default
                .build();
        ZooModel<IrisFlower, Classifications> model = criteria.loadModel();
        Predictor<IrisFlower, Classifications> predictor = model.newPredictor();

        long t=FactoryUtils.tic();
        for (int i = 0; i < 10; i++) {
            IrisFlower info = new IrisFlower(1.0f, 2.0f, 3.0f, 4.0f);
            Classifications ret = predictor.predict(info);
            System.out.println("ret = " + ret);
            t=FactoryUtils.toc(t);
        }

        /*
        // ONNX modelini yükleyin
        Criteria<Image, DetectedObjects> criteria = Criteria
                .builder()
                .setTypes(Image.class, DetectedObjects.class)
                .optModelName("yolov8")
                .optEngine("onnxruntime")
                .optArtifact("onnxruntime", "1.12.1")
                .optFilter("onnxruntime", "cpu") // CPU için ayarlayın
                .optProgress(true)
                .build();

        try (ZooModel<Image, DetectedObjects> zooModel = ModelZoo.loadModel(criteria)) {

            // Translator'ı oluşturun
            Translator<Image, DetectedObjects> translator = Translator.builder()
                    .optInput(Image.class) // Giriş türü
                    .optOutput(DetectedObjects.class) // Çıkış türü
                    .optPreprocessor(new ToTensor()) // Ön işleme (görüntüyü tensöre dönüştürme)
                    .build();

            // Görüntü dosyasını yükleyin
            InputStream is = Paths.get("resim.jpg").toUri().toURL().openStream();
            Image img = Image.fromInputStream(is);

            // Predictor'ı oluşturun
            try (Model model = Model.newInstance(zooModel);
                 Predictor<Image, DetectedObjects> predictor = model.newPredictor(translator)) {
                // Inference yapın
                DetectedObjects detections = predictor.predict(img);

                // Sonuçları işle
                List<BoundingBox> boundingBoxes = detections.getBoundingBoxList();
                for (BoundingBox bbox : boundingBoxes) {
                    // Bounding box koordinatlarını ve sınıfını yazdırın
                    System.out.println("Sınıf: " + bbox.getClassName());
                    System.out.println("Konum: " + bbox.getCoordinates());
                }
            }
        }
         */
    }
}
