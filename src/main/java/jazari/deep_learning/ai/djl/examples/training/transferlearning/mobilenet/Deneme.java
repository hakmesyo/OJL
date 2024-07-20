/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.ai.djl.examples.training.transferlearning.mobilenet;

import ai.djl.basicmodelzoo.BasicModelZoo;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.repository.Artifact;
import ai.djl.repository.zoo.ModelLoader;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author cezerilab
 */
public class Deneme {

    public static void main(String[] args) throws IOException, ModelNotFoundException {
        Map<String, String> filter = new HashMap<>();
        filter.put("layers", "50");
        filter.put("flavor", "v1");
        filter.put("dataset", "cifar10");

        //ZooModel<Image, Classifications> model = BasicModelZoo.listModelZoo().RESNET.loadModel(filter);
        Collection<ModelZoo> modelZoos = ModelZoo.listModelZoo();
        ModelZoo mz=ModelZoo.getModelZoo("ai.djl.mxnet");
        ModelLoader mlo=mz.getModelLoader("mobilenet");
        for (Artifact listModel : mlo.listModels()) {
            System.out.println(listModel.getName());
        }
        //BasicModelZoo.getModelZoo("ai.djl.mxnet").getModelLoader("mobilenet")

        // Eğer bir List'e ihtiyacınız varsa:
//        List<ModelZoo> modelZooList = modelZoos.stream().collect(Collectors.toList());
//
        System.out.println("Available Model Zoos:");
        for (ModelZoo zoo : modelZoos) {
            System.out.println(zoo.getGroupId());
            if (zoo.getGroupId().equals("ai.djl.mxnet")) {
                Collection<ModelLoader> ml = zoo.getModelLoaders();
                for (ModelLoader modelLoader : ml) {
                    //System.out.println("modelLoader = " + modelLoader.getArtifactId());
                    if (modelLoader.getArtifactId().equals("mobilenet")) {
                        List<Artifact> lst = modelLoader.listModels();
                        for (Artifact artifact : lst) {
                            //System.out.println("artifact = " + artifact.getName()+" version:"+artifact.getVersion());
                            System.out.println("artifact = " + artifact);
                        }
                    }

                }
            }

        }

        // Belirli bir model arayalım
//        for (ModelZoo zoo : modelZoos) {
//            for (Artifact artifact : zoo.listModels()) {
//                Map<String, String> properties = artifact.getProperties();
//                if (properties.entrySet().containsAll(filter.entrySet())) {
//                    System.out.println("Found matching model: " + artifact.getName());
//                    System.out.println("Properties: " + properties);
//                }
//            }
//        }
    }
}
