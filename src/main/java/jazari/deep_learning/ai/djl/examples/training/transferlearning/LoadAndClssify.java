/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ai.djl.examples.training.transferlearning;

/**
 *
 * @author cezerilab
 */
import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.SymbolBlock;
import ai.djl.nn.core.Linear;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LoadAndClssify {

    public static void main(String[] args) throws IOException, ModelException, TranslateException {
        // Eğitilmiş model dosyasının yolu
        String modelDir = "models/mobilenet";
        String modelName = "mobilenet-transfer-pistachio";
        String paramsFileName = "mobilenet-transfer-pistachio-0002.params";

        // Test edilecek görüntünün yolu
        String testImagePath = "D:\\DATASETS\\classification\\pistachio_224_224\\test\\close\\550.jpg";

        // Modeli yükle
        try (Model model = Model.newInstance(modelName)) {
            // Model dosyası yolunu ayarla
            Path modelPath = Paths.get(modelDir);

            // Modeli yükle
            model.load(modelPath, modelName);

            // Model bloklarını al
            SymbolBlock baseBlock = (SymbolBlock) model.getBlock();
            if (baseBlock == null) {
                throw new IllegalArgumentException("Model block could not be loaded correctly.");
            }

            // Yeni blok oluştur ve mevcut blokları ekle
            SequentialBlock newBlock = new SequentialBlock();
            newBlock.add(baseBlock);
            newBlock.add(Blocks.batchFlattenBlock());
            newBlock.add(Linear.builder().setUnits(2).build()); // Çıktı katmanını ayarla

            model.setBlock(newBlock);

            // Görüntü sınıflandırma çeviricisi oluştur
            Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                    .addTransform(new ToTensor())
                    .addTransform(new Normalize(new float[]{0.485f, 0.456f, 0.406f}, new float[]{0.229f, 0.224f, 0.225f}))
                    .optApplySoftmax(true)
                    .build();

            // Predictor oluştur
            try (Predictor<Image, Classifications> predictor = model.newPredictor(translator)) {
                // Görüntüyü yükle
                Image img = ImageFactory.getInstance().fromFile(Paths.get(testImagePath));

                // Sınıflandırma yap
                Classifications classifications = predictor.predict(img);

                // Sonuçları yazdır
                List<Classifications.Classification> items = classifications.items();
                for (Classifications.Classification item : items) {
                    System.out.printf("Class: %s, Probability: %.5f%n", item.getClassName(), item.getProbability());
                }
            }
        }
    }
}
