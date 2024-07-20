package jazari.deep_learning.ai.djl.examples.training.transferlearning.mobilenet;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.metric.Metrics;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.core.Linear;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.RandomAccessDataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Adam;
import ai.djl.translate.TranslateException;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.training.util.ProgressBar;

import java.io.IOException;
import java.nio.file.Paths;

public class MobileNetV3TransferLearning {
    public static void main(String[] args) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        // Veri setini yükle
        ImageFolder dataset = ImageFolder.builder()
                .setRepositoryPath(Paths.get("D:\\DATASETS\\pistachio_224_224\\train"))
                .optMaxDepth(1)
                .addTransform(new Resize(224, 224))
                .addTransform(new ToTensor())
                .setSampling(32, true)
                .build();

        dataset.prepare();

        // MobileNet V3 modelini yükle
        Criteria<Image, Classifications> criteria = Criteria.builder()
                .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                .setTypes(Image.class, Classifications.class)
                .optFilter("backbone", "mobilenet")
                .optProgress(new ProgressBar())
                .build();

        ZooModel<Image, Classifications> model = criteria.loadModel();

        // Transfer learning için son katmanı değiştir
        SequentialBlock newBlock = new SequentialBlock();
        for (int i = 0; i < model.getBlock().getChildren().size() - 1; i++) {
            newBlock.add((Block) model.getBlock().getChildren().get(i));
        }
        newBlock.add(Linear.builder().setUnits(dataset.getClasses().size()).build());

        // Eğitim konfigürasyonunu oluştur
        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy())
                .optOptimizer(Adam.builder().build())
                .addTrainingListeners(TrainingListener.Defaults.logging());

        // Trainer oluştur ve eğit
        try (Model newModel = Model.newInstance("mobilenet_v3_transfer")) {
            newModel.setBlock(newBlock);

            try (Trainer trainer = newModel.newTrainer(config)) {
                trainer.setMetrics(new Metrics());

                Shape inputShape = new Shape(1, 3, 224, 224);
                trainer.initialize(inputShape);

                EasyTrain.fit(trainer, 10, dataset, null);

                // Eğitilmiş modeli kaydet
                newModel.save(Paths.get("models/mobilenet"), "mobilenet_v3_custom");
            }
        }
    }
}