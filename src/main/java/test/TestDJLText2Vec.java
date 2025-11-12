package test;

import ai.djl.Application;
import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import jazari.matrix.CMatrix;

public class TestDJLText2Vec {

    public static void main(String[] args) {
        String[] sentences = {
            "Yapay zeka geleceği şekillendiriyor.",
            "Makine öğrenmesi, yapay zekanın bir alt dalıdır.",
            "Bugün hava çok güzel ve güneşli.",
            "Derin öğrenme modelleri büyük veri setleri ile eğitilir."
        };

        System.out.println("Vektöre dönüştürülecek cümleler:");
        for (String sentence : sentences) {
            System.out.println("- " + sentence);
        }

        String modelUrl = "https://mlrepo.djl.ai/model/nlp/text_embedding/ai.djl.mxnet/bert/0.0.1/bert_12_768_12.zip";

        // 2. Adım: Criteria'yı hem URL hem de Artifact ID ile oluşturarak DJL'ye ipucu ver
        Criteria<String[], float[][]> criteria = Criteria.builder()
                // Uygulama türü, DJL'nin doğru çeviriciyi bulmasına yardımcı olur
                .optApplication(Application.NLP.TEXT_EMBEDDING)
                .setTypes(String[].class, float[][].class)
                .optEngine("MXNet")
                // Modelin nereden indirileceğini belirtiyoruz
                .optModelUrls(modelUrl)
                // İndirilen modelin "kim" olduğunu söylüyoruz. Bu sayede DJL,
                // bu artifact ile ilişkili varsayılan Translator'ı kullanır.
                .optArtifactId("bert")
                .optProgress(new ProgressBar())
                .build();

        try (ZooModel<String[], float[][]> model = criteria.loadModel(); Predictor<String[], float[][]> predictor = model.newPredictor()) {

            System.out.println("\nMXNet tabanlı BERT modeli başarıyla yüklendi ve çevirici (translator) bulundu.");
            System.out.println("Cümleler vektörlere dönüştürülüyor...");

            float[][] embeddings = predictor.predict(sentences);
            CMatrix sentenceVectors = CMatrix.getInstance(embeddings);

            System.out.println("\nOluşturulan CMatrix:");
            sentenceVectors.println();
            sentenceVectors.shape().println("Matris Boyutu:");

            System.out.println("\nİlk cümlenin diğer cümlelerle anlamsal benzerliği (Kosinüs Benzerliği):");

            CMatrix firstSentenceVector = sentenceVectors.getRow(0);

            for (int i = 0; i < sentences.length; i++) {
                CMatrix otherSentenceVector = sentenceVectors.getRow(i);
                float similarity = firstSentenceVector.cosineSimilarity(otherSentenceVector).meanTotal();

                System.out.printf("Benzerlik ('%s' <-> '%s'): %.4f\n", sentences[0], sentences[i], similarity);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
