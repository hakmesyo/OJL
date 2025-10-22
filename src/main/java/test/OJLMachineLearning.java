package test;


import jazari.factory.FactoryMatrix;
import jazari.machine_learning.data_loader.DataLoader;
import jazari.machine_learning.mlp.ModelTrainer;
import jazari.machine_learning.mlp.MultiLayerPerceptron;
import jazari.machine_learning.mlp.enums.EActivationType;
import jazari.machine_learning.mlp.enums.ELossFunction;
import jazari.machine_learning.mlp.enums.EOptimizerType;
import jazari.machine_learning.mlp.enums.EProblemType;
import jazari.matrix.CMatrix;
import java.io.File;

public class OJLMachineLearning {

    public static void main(String[] args) {
        // 1. Adım: OJL API'si ile Sahte Veri Seti Oluşturma
        // CMatrix.make_blobs() metodu, sınıflandırma için uygun, kümelenmiş bir veri seti oluşturur.
        // Parametreler: (örnek sayısı, öznitelik sayısı, sınıf sayısı, rastgelelik için başlangıç değeri)
        System.out.println("1. Adım: Sahte veri seti oluşturuluyor...");
        CMatrix data = CMatrix.getInstance().make_blobs(1500, 4, 3, 42);
        data.scatterBlob();

        // OJL'nin MLP modülü DataLoader sınıfı ile çalıştığı için,
        // oluşturduğumuz CMatrix'i geçici bir CSV dosyasına kaydedip oradan yükleyeceğiz.
        String csvPath = "temp_synthetic_data.csv";
        data.saveNewFileAsCSV(csvPath);
        System.out.println("Veri seti geçici olarak '" + csvPath + "' dosyasına kaydedildi.");

        // 2. Adım: Veri Setini Yükleme ve Partisyonlara Ayırma
        System.out.println("\n2. Adım: Veri seti yükleniyor ve train/validation/test olarak ayrılıyor...");
        DataLoader dataLoader = new DataLoader();
        
        // CSV dosyasını yükle. Son sütun (indeks 4) etiket sütunudur. Başlık satırı yok.
        dataLoader.loadCSV(csvPath, 4, false);
        
        // Veriyi %70 train, %15 validation ve %15 test olarak ayır.
        dataLoader.splitData(0.70, 0.15, 0.15);
        dataLoader.printDataSummary();

        // 3. Adım: Yapay Sinir Ağı Modelini Tanımlama
        System.out.println("\n3. Adım: Yapay Sinir Ağı (MLP) modeli tanımlanıyor...");
        int inputSize = dataLoader.getInputSize();
        int numClasses = dataLoader.getNumClasses();
        
        MultiLayerPerceptron mlp = new MultiLayerPerceptron(EProblemType.CLASSIFICATION,inputSize);
        
        // Katmanları ekle: Giriş -> Gizli Katman 1 -> Gizli Katman 2 -> Çıkış
        mlp.addLayer(64, EActivationType.RELU, 0.1); // 64 nöronlu, ReLU aktivasyonlu ve %10 dropout'lu gizli katman
        mlp.addLayer(32, EActivationType.RELU, 0.1); // 32 nöronlu, ReLU aktivasyonlu ve %10 dropout'lu gizli katman
        mlp.addLayer(numClasses, EActivationType.SOFTMAX, 0.0); // Sınıf sayısı kadar nöronlu, Softmax aktivasyonlu çıkış katmanı
        
        // Modelin eğitim parametrelerini ayarla
        mlp.setLossFunction(ELossFunction.CROSS_ENTROPY);
        mlp.setOptimizer(EOptimizerType.ADAM);
        mlp.setLearningRate(0.001);
        
        // Modelin özetini yazdır
        mlp.summary();

        // 4. Adım: Modeli Eğitme ve Canlı Kayıp (Loss) Değerlerini Gösterme
        System.out.println("\n4. Adım: Model eğitiliyor... Lütfen açılacak canlı grafiği takip ediniz.");
        ModelTrainer trainer = new ModelTrainer(mlp, dataLoader);
        
        // Modeli 50 epoch boyunca eğit. Bu işlem sırasında canlı loss grafiği otomatik olarak açılacaktır.
        trainer.train(50);
        
        // Eğitimin bitmesini bekle
        trainer.waitForTrainingComplete();
        System.out.println("Eğitim tamamlandı.");

        // 5. Adım: Genelleme Performansını Değerlendirme
        System.out.println("\n5. Adım: Test seti üzerinde genelleme performansı değerlendiriliyor...");
        trainer.evaluate();

        // 6. Adım: Temizlik
        // Geçici olarak oluşturulan CSV dosyasını sil.
        new File(csvPath).delete();
        System.out.println("\nGeçici dosya '" + csvPath + "' silindi.");
    }
}