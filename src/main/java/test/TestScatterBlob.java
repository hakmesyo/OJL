/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import jazari.types.TMatrixOperator;

/**
 *
 * @author Teknofest
 */
public class TestScatterBlob {

    public static void main(String[] args) {
        //CMatrix data = CMatrix.getInstance().make_blobs(500, 4, 3, 42).scatterBlob("0,3");

//        CMatrix zamanSerisi = CMatrix.getInstance(new float[][]{
//            {1, 150.75f},
//            {2, 152.50f},
//            {3, 151.25f}
//        });
//        zamanSerisi.shape();
//        zamanSerisi.plot();
//        zamanSerisi.clone().cmd(":","1").plot(zamanSerisi.clone().getColumn(0));
//        CMatrix cm = CMatrix.getInstance()
//                .rand(1,100)
//                .plot()
//                
//                ;
//        CMatrix veriler = CMatrix.getInstance(1.0, 2.0, Double.NaN, 4.0, 5.0).println();
//
//// NaN olmayan değerlerin ortalamasını hesapla
//        float ortalama = veriler.nanmean();
//
//// NaN olan değerleri ortalama ile doldur
//        veriler = veriler.fillNaN(ortalama);
//
//        veriler.println("OJL ile Doldurulmuş Veriler");
//        CMatrix veriler = CMatrix.getInstance(-99,1.0, 2000.0, 3.0, 4.0, 100.0);
//
//// Veriyi sırala
//        CMatrix siraliVeriler = veriler.sort();
//
//// Çeyrek değerleri (Q1, Q3) ve IQR'ı hesapla
//        double q1 = siraliVeriler.getPercentile(25);
//        double q3 = siraliVeriler.getPercentile(75);
//        double iqr = q3 - q1;
//        double altSinir = q1 - 1.5 * iqr;
//        double ustSinir = q3 + 1.5 * iqr;
//
//        System.out.println("Alt Sınır: " + altSinir + ", Üst Sınır: " + ustSinir);
//
//// Aykırı değerleri bul ve göster
//        CMatrix aykiriDegerlerIndex = veriler.findIndex(TMatrixOperator.SMALLER, (float) altSinir)
//                .cat(1, veriler.findIndex(TMatrixOperator.GREATER, (float) ustSinir));
//
//        veriler.findValuesByIndex(aykiriDegerlerIndex).println("Aykırı Değerler");

//CMatrix veriler = CMatrix.getInstance(1.0, 2.0, 3.0, 4.0, 5.0);
//// Tek satırda Min-Max Normalizasyon
//CMatrix normalizeVeriler = veriler.normalizeMinMax();
//normalizeVeriler.println("OJL ile Normalize Edilmiş Veriler");

//CMatrix veriler = CMatrix.getInstance(1.0, 2.0, 3.0, 4.0, 5.0);
//// Tek satırda Z-score Standardizasyon
//CMatrix standardizeVeriler = veriler.normalizeZScore();
//standardizeVeriler.println("OJL ile Standardize Edilmiş Veriler");

//CMatrix veri = CMatrix.getInstance(new float[][]{
//    {1, 2, 3, 10},
//    {4, 5, 6, 20},
//    {7, 8, 9, 35}
//});
//// Tek satırda korelasyon matrisini hesapla
//CMatrix korelasyonMatrisi = veri.corrcoef().println();
//// Korelasyon matrisini ısı haritası olarak görselleştir
//korelasyonMatrisi.heatmap(true); // 'true' hücre değerlerini gösterir

// Sınıf etiketlerini içeren bir CMatrix (0: kırmızı, 1: mavi, 2: yeşil)
CMatrix kategoriler = CMatrix.getInstance(0, 1, 2, 0, 1);
// OJL'nin dahili fonksiyonu ile One-Hot Encoding uygula
int nclass=FactoryUtils.getUniqueValues(kategoriler.toFloatArray1D()).length;
CMatrix oneHotKodlar = kategoriler.getOneHotEncoding(nclass); // 3: toplam sınıf sayısı
oneHotKodlar.println("One-Hot Kodlanmış Veriler");




    }
}
