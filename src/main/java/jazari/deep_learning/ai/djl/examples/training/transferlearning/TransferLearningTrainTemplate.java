/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ai.djl.examples.training.transferlearning;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.engine.Engine;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.SymbolBlock;
import ai.djl.nn.core.Linear;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.TrainingResult;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import jazari.factory.FactoryUtils;

public class TransferLearningTrainTemplate {

    public static void main(String[] args) throws Exception {
        String modelPath = "models/squeezenet";
        int batchSize = 32;
        int epochs = 50;
        int outputClasses = 2;

        ImageFolder dataset_train = ImageFolder.builder()
                .setRepositoryPath(Paths.get("D:\\DATASETS\\pistachio_224_224\\train"))
                .optMaxDepth(1)
                .addTransform(new Resize(224, 224))
                .addTransform(new ToTensor())
                .setSampling(batchSize, true)
                .build();
        try {
            dataset_train.prepare();
        } catch (IOException | TranslateException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ImageFolder dataset_val = ImageFolder.builder()
                .setRepositoryPath(Paths.get("D:\\DATASETS\\pistachio_224_224\\valdation"))
                .optMaxDepth(1)
                .addTransform(new Resize(224, 224))
                .addTransform(new ToTensor())
                .setSampling(batchSize, true)
                .build();
        try {
            dataset_train.prepare();
        } catch (IOException | TranslateException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        Criteria<ai.djl.modality.cv.Image, ai.djl.modality.Classifications> criteria = Criteria.builder()
                .optApplication(ai.djl.Application.CV.IMAGE_CLASSIFICATION)
                .setTypes(ai.djl.modality.cv.Image.class, ai.djl.modality.Classifications.class)
                .optArtifactId("squeezenet")
                .optProgress(new ProgressBar())
                .build();

        ZooModel<ai.djl.modality.cv.Image, ai.djl.modality.Classifications> net = null;
        try {
            net = criteria.loadModel();
        } catch (IOException | ModelNotFoundException | MalformedModelException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        Model model = Model.newInstance("transfer-squeezenet");
        SymbolBlock baseBlock = (SymbolBlock) net.getBlock();

        SequentialBlock newBlock = new SequentialBlock();
        for (int i = 0; i < baseBlock.getChildren().size() - 1; i++) {
            newBlock.add((Block) baseBlock.getChildren().get(i));
        }
        newBlock.add(Blocks.batchFlattenBlock());
        newBlock.add(Linear.builder().setUnits(outputClasses).build());
        model.setBlock(newBlock);

        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .optDevices(Engine.getInstance().getDevices(1))
                .addTrainingListeners(TrainingListener.Defaults.logging());

        try (Trainer trainer = model.newTrainer(config)) {
            Shape inputShape = new Shape(1, 3, 224, 224);
            trainer.initialize(inputShape);

            NDManager manager = NDManager.newBaseManager();
            NDArray sampleInput = manager.randomUniform(0f, 1f, inputShape);
            NDArray output = trainer.forward(new NDList(sampleInput)).singletonOrThrow();
            System.out.println("Model output shape: " + output.getShape());

            EasyTrain.fit(trainer, epochs, dataset_train, dataset_val);

            TrainingResult result = trainer.getTrainingResult();
            System.out.println(result);
        }

        Path modelDir = Paths.get(modelPath);
        String modelName = "squeezenet-transfer-pistachio";

        try {
            Files.createDirectories(modelDir);

            model.setProperty("Epoch", String.valueOf(epochs));
            // Modeli SymbolBlock olarak kaydet
            model.save(modelDir, modelName);

            System.out.println("Model saved successfully.");
            System.out.println("Saved model files:");
        } catch (Exception e) {
            System.err.println("Error saving model or creating symbol file: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
