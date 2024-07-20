/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ai.djl.examples.training.transferlearning.mobilenet;

/**
 *
 * @author cezerilab
 */
import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.SymbolBlock;
import ai.djl.nn.core.Linear;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import jazari.factory.FactoryUtils;

public class MobileNetTest {

    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException {
        String modelPath = "models/mobilenet";
        String testImagePath = "D:\\DATASETS\\pistachio_224_224\\test";
        String[] classLabels = {"close", "open"};

        Model model = buildTransferLearningModel4Test("mobilenet", 2);
        model.load(Paths.get(modelPath), "mobilenet-transfer-pistachio");

        FactoryUtils.printModelSummary(model);
        System.out.println("Model başarıyla yüklendi: " + modelPath);

        ImageClassificationTranslator translator = FactoryUtils.getImageClassificationTranslator(classLabels);
        List<Path> imageFiles = FactoryUtils.getImageList(testImagePath, "jpg");

        FactoryUtils.evaluateModel(model, translator, imageFiles, false, classLabels);
    }

    public static Model buildTransferLearningModel4Test(String modelName, int nOutputClasses) throws IOException, ModelNotFoundException, MalformedModelException {
        Criteria<Image, Classifications> criteria = Criteria.builder()
                .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                .setTypes(Image.class, Classifications.class)
                .optProgress(new ProgressBar())
                .optEngine("MXNet")
                .optGroupId("ai.djl.mxnet")
                .optArtifactId("mobilenet")
                .optFilter("flavor", "v2")
                .optFilter("multiplier", "1.0")
                .optFilter("dataset", "imagenet")
                .build();

        try (ZooModel<Image, Classifications> baseModel = criteria.loadModel()) {
            Model model = Model.newInstance("transfer-" + modelName);
            SymbolBlock baseBlock = (SymbolBlock) baseModel.getBlock();
            baseBlock.removeLastBlock();

            SequentialBlock newBlock = new SequentialBlock();
            newBlock.add(baseBlock);
            newBlock.add(Blocks.batchFlattenBlock());
            newBlock.add(Linear.builder().setUnits(nOutputClasses).build());
            model.setBlock(newBlock);

            return model;
        }
    }
}
