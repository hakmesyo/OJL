package jazari.deep_learning.ai.djl.examples.training.transferlearning.squeezenet;

import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.training.loss.Loss;
import java.nio.file.Path;
import java.nio.file.Paths;
import jazari.factory.FactoryUtils;

public class SqueezeNetTrain {

    public static void main(String[] args) throws Exception {
        String modelPath = "models/squeezenet";
        Path modelDir = Paths.get(modelPath);
        String modelName = "squeezenet-transfer-pistachio";
        int batchSize = 32;
        int epochs = 50;
        int outputClasses = 2;

        ImageFolder dataset_train = FactoryUtils.loadDataSetDJL("D:\\DATASETS\\pistachio_224_224\\train", batchSize, 1, 224);
        ImageFolder dataset_val = FactoryUtils.loadDataSetDJL("D:\\DATASETS\\pistachio_224_224\\validation", batchSize, 1, 224);

        Model model = FactoryUtils.buildTransferLearningModel4Train("squeezenet", outputClasses);
        model = FactoryUtils.trainModel(model, Loss.softmaxCrossEntropyLoss(), epochs, dataset_train, dataset_val);

        model = FactoryUtils.saveModel(model, epochs, modelDir, modelName);

    }

}
