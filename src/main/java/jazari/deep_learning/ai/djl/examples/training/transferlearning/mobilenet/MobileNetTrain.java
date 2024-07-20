/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ai.djl.examples.training.transferlearning.mobilenet;

/**
 *
 * @author cezerilab
 */
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.nn.Block;
import ai.djl.nn.Parameter;
import ai.djl.nn.SequentialBlock;
import ai.djl.training.loss.Loss;
import ai.djl.util.Pair;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import jazari.factory.FactoryUtils;

public class MobileNetTrain {

    public static void main(String[] args) throws IOException, MalformedModelException {
        String modelPath = "models/mobilenet";
        Path modelDir = Paths.get(modelPath);
        String modelName = "mobilenet-transfer-pistachio";
        //String datasetPath="D:\\DATASETS\\classification\\cats_dogs";
        //String datasetPath="D:\\DATASETS\\classification\\number_dataset";
        String datasetPath="D:\\DATASETS\\classification\\pistachio_224_224";
        int batchSize = 16;
        int epochs = 2;
        int outputClasses = 2;

        ImageFolder dataset_train = FactoryUtils.loadDataSetDJL(datasetPath+"/train", batchSize, 3, 224);
        ImageFolder dataset_val = FactoryUtils.loadDataSetDJL(datasetPath+"/validation", batchSize, 3, 224);

        Model model = FactoryUtils.buildTransferLearningModel4Train("mobilenet", outputClasses);
        model = FactoryUtils.trainModel(model, Loss.softmaxCrossEntropyLoss(), epochs, dataset_train, dataset_val);
        printModelSummary(model);

        FactoryUtils.saveModel(model, epochs, modelDir, modelName);
    }

    public static void printModelSummary(Model model) {
        System.out.println("Model Name: " + model.getName());

        Block block = model.getBlock();
        System.out.println("\nModel Structure:");
        printBlockStructure(block, 0);

        long totalParams = countParameters(block);
        System.out.println("\nTotal parameters: " + totalParams);
    }

    private static void printBlockStructure(Block block, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + block.getClass().getSimpleName());

        if (block instanceof SequentialBlock) {
            SequentialBlock seqBlock = (SequentialBlock) block;
            for (Pair<String, Block> child : seqBlock.getChildren()) {
                printBlockStructure(child.getValue(), depth + 1);
            }
        } else if (block instanceof ai.djl.nn.ParallelBlock) {
            ai.djl.nn.ParallelBlock parallelBlock = (ai.djl.nn.ParallelBlock) block;
            for (Pair<String, Block> child : parallelBlock.getChildren()) {
                printBlockStructure(child.getValue(), depth + 1);
            }
        }

        // Parametrelerin şeklini yazdır
        for (Pair<String, Parameter> param : block.getParameters()) {
            System.out.println(indent + "  " + param.getKey() + ": " + param.getValue().getShape());
        }
    }

    private static long countParameters(Block block) {
        long count = 0;
        for (Pair<String, Parameter> param : block.getParameters()) {
            count += param.getValue().getArray().size();
        }

        if (block instanceof SequentialBlock) {
            SequentialBlock seqBlock = (SequentialBlock) block;
            for (Pair<String, Block> child : seqBlock.getChildren()) {
                count += countParameters(child.getValue());
            }
        }

        return count;
    }

}
/**
 * eski uzun kodlar
 */

//    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
//        // Veri seti yolunu ve parametreleri ayarlayın
//        String datasetRootTrain = "D:\\DATASETS\\pistachio_224_224\\train";
//        String datasetRootVal = "D:\\DATASETS\\pistachio_224_224\\validation";
//        int batchSize = 32;
//        int epochs = 2;
//        int outputClasses = 2; // Veri setinizdeki sınıf sayısı
//
//        // train veri setini yükleyin
//        ImageFolder dataset_train = ImageFolder.builder()
//                .setRepositoryPath(Paths.get(datasetRootTrain))
//                .optMaxDepth(1)
//                .addTransform(new Resize(224, 224))
//                .addTransform(new ToTensor())
//                .setSampling(batchSize, true)
//                .build();
//
//        dataset_train.prepare();
//        
//        // validation veri setini yükleyin
//        ImageFolder dataset_val = ImageFolder.builder()
//                .setRepositoryPath(Paths.get(datasetRootVal))
//                .optMaxDepth(1)
//                .addTransform(new Resize(224, 224))
//                .addTransform(new ToTensor())
//                .setSampling(batchSize, true)
//                .build();
//
//        dataset_val.prepare();
//
//        // MobileNet modelini yükleyin
//        Criteria<ai.djl.modality.cv.Image, ai.djl.modality.Classifications> criteria = Criteria.builder()
//                .optApplication(Application.CV.IMAGE_CLASSIFICATION)
//                .setTypes(ai.djl.modality.cv.Image.class, ai.djl.modality.Classifications.class)
//                .optGroupId("ai.djl.mxnet")
//                .optArtifactId("mobilenet")
//                .optEngine("MXNet")
//                .optProgress(new ProgressBar())
//                .build();
//
//        ZooModel<ai.djl.modality.cv.Image, ai.djl.modality.Classifications> mobileNet = criteria.loadModel();
//
//        // Transfer learning için modeli hazırlayın
//        Model model = Model.newInstance("transfer-mobilenet");
//        SequentialBlock newBlock = new SequentialBlock();
//        SymbolBlock baseBlock = (SymbolBlock) mobileNet.getBlock();
//        baseBlock.removeLastBlock();
//        newBlock.add(baseBlock);
//        newBlock.add(Linear.builder().setUnits(outputClasses).build());
//        model.setBlock(newBlock);
//
//        // Eğitim konfigürasyonunu ayarlayın
//        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
//                .addEvaluator(new Accuracy())
//                .optDevices(Engine.getInstance().getDevices(1))
//                .addTrainingListeners(TrainingListener.Defaults.logging());
//
//        // Modeli eğitin
//        try (Trainer trainer = model.newTrainer(config)) {
//            trainer.initialize(new Shape(1, 3, 224, 224));
//
//            EasyTrain.fit(trainer, epochs, dataset_train, dataset_val);
//
//            TrainingResult result = trainer.getTrainingResult();
//            System.out.println(result);
//
//            model.setProperty("Epoch", result.getEpoch()+"");
//            model.setProperty(
//                    "Accuracy",
//                    String.format("%.5f", result.getValidateEvaluation("Accuracy")));
//            model.setProperty("Loss", String.format("%.5f", result.getValidateLoss()));
//            // Modeli kaydedin
//            FactoryUtils.makeDirectory("models/mobilenet");
//            model.save(Paths.get("models/mobilenet"), "mobilenet-transfer");
//
//            System.out.println("Model eğitimi tamamlandı ve kaydedildi.");
//        }
//
//    }

