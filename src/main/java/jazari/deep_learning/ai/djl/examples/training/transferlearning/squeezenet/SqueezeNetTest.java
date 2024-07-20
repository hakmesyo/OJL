/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ai.djl.examples.training.transferlearning.squeezenet;

import ai.djl.Model;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import jazari.factory.FactoryUtils;

public class SqueezeNetTest {

    public static void main(String[] args) throws Exception {
        String modelPath = "D:\\Dropbox\\NetbeansProjects\\OJL\\models\\squeezenet"; // Dosya yolunu değiştirin
        String testImagePath = "D:\\DATASETS\\pistachio_224_224\\test"; // Test klasörü yolu
        String[] classLabels = {"close", "open"}; // Sınıf etiketleri
        
        Model model = FactoryUtils.buildTransferLearningModel4Test("squeezenet", 2);
        model.load(Paths.get(modelPath), "squeezenet-transfer-pistachio");
        FactoryUtils.printModelSummary(model);
        System.out.println("Model başarıyla yüklendi: " + modelPath);

        ImageClassificationTranslator translator=FactoryUtils.getImageClassificationTranslator("close", "open");
        List<Path> imageFiles=FactoryUtils.getImageList(testImagePath,"jpg");
        
        FactoryUtils.evaluateModel(model,translator,imageFiles,false,classLabels);        
    }
}
