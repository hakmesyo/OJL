

import jazari.matrix.CMatrix;

/**
 * ========================================================================
 * MÜHENDİSLİĞİN TEMEL DENKLEMİ: Ax = b
 * UYGULAMA 2: GÖRÜNTÜ RESTORASYONU (Image Restoration / Deconvolution)
 * ========================================================================
 * 
 * 1. GÖRÜNTÜ RESTORASYONU NEDİR?
 * Bozulmuş, bulanıklaşmış (blur) veya gürültüye (noise) maruz kalmış bir görüntüyü, 
 * bozulmaya sebep olan fiziksel veya optik hatayı tersine mühendislikle (matematiksel olarak) 
 * çözerek orijinal net haline geri döndürme işlemidir. (Inverse Filtering / Ters Filtreleme)
 * 
 * 2. NİÇİN İHTİYAÇ VARDIR VE HANGİ PROBLEMLERDE KULLANILIR?
 * Fiziksel dünyada kameralar titrer, atmosfer ışığı dağıtır, mikroskop lensleri kusurludur.
 * Bu fiziksel bozulmalar "A matrisi" yani Point Spread Function (PSF) olarak modellenir.
 *   - Astronomi: Hubble Uzay Teleskobu ilk fırlatıldığında aynasındaki milimetrik hata 
 *     yıldızları bulanık gösteriyordu. Bilim insanları bu hatayı bir "A" matrisi olarak modelledi 
 *     ve x=A^-1*b ile görüntüleri yazılımsal olarak netleştirdiler.
 *   - Adli Bilişim (Forensics): Hızla kaçan bir aracın güvenlik kamerasındaki bulanık (motion blur) 
 *     plakasının okunabilir hale getirilmesi.
 *   - Tıbbi Görüntüleme: MR cihazlarında veya hücre mikroskoplarında optik kırılmaların 
 *     düzeltilerek detayların ortaya çıkarılması.
 * 
 * 3. PROJELERDE BU İHTİYAÇ SÜREKLİ OLUR MU?
 * Güvenlik ve savunma sanayi (uydu görüntüleri vb.), tıbbi görüntüleme analizleri ve 
 * sinyal işleme tabanlı projelerde (örn: sismik veri netleştirme) sürekli bir ihtiyaçtır.
 * Derin öğrenme (Deep Learning) öncesinde bu matematiksel çözüm (Deconvolution) tek yöntemdi, 
 * bugün bile kritik kesinlik gerektiren yerlerde sinyal işlemi standartları arasındadır.
 * 
 * ------------------------------------------------------------------------
 * DENKLEM: A * x = b  =>  x = A^-1 * b (veya x = A \ b)
 * A: PSF (Point Spread Function) - Görüntüyü bozan optik karışım matrisi
 * b: Kameradan alınan bozuk/bulanık görüntü (gözlemimiz)
 * x: Bulmak istediğimiz NET ve ORİJİNAL piksel değerleri
 * ========================================================================
 */
public class TestImageRestorationAxB {

    public static void main(String[] args) {
        System.out.println("--- GÖRÜNTÜ RESTORASYONU (TERS FİLTRELEME) BAŞLIYOR ---\n");
        
        // A Matrisi (Sistem Davranışı): PSF (Point Spread Function). 
        // Sensördeki bir noktanın enerjisinin komşu piksellere nasıl dağıldığını gösteren bulanıklık modeli.
        float[][] A_BulaniklikModeli = {
            {0.6f, 0.3f, 0.1f}, // Gerçek değerin %60'ı kalmış, %30 ve %10'u komşulara taşmış
            {0.2f, 0.7f, 0.1f},
            {0.1f, 0.2f, 0.7f}
        };
        
        // b Vektörü (Ölçülen Çıktı): Kameradan aldığımız, bozulmuş ve bulanıklaşmış pikseller
        float[][] b_BozukPikseller = {
            {120},
            {150},
            {180}
        };

        // OJL ile matrisleri oluşturuyoruz
        CMatrix cmA = CMatrix.getInstance(A_BulaniklikModeli).println("A Matrisi (Bulanıklık Optik Modeli - PSF):");
        CMatrix cmB = CMatrix.getInstance(b_BozukPikseller).println("b Vektörü (Sensördeki Bulanık/Bozuk Pikseller):");

        System.out.println("Hesaplanıyor: OJL solve() metodu ile (x = A \\ b) ...\n");
        
        // ÇÖZÜM: OJL'nin .solve() metodu, lineer denklem sistemlerini (Ax=b) çözmek için 
        // doğrudan A.inv().dot(b) işleminden donanımsal olarak daha optimize ve stabil çalışır.
        CMatrix cmX = cmA.clone().solve(cmB);
        
        // Piksel değerleri tam sayı olacağı için .round() ile yuvarlıyoruz
        cmX.round().println("x Vektörü (KURTARILMIŞ NET ORİJİNAL PİKSEL DEĞERLERİ):");
        System.out.println("YORUM: Matematiksel tersine mühendislik sayesinde bulanıklık geri alındı.");
        System.out.println("Plaka okuma, yüz netleştirme veya tıbbi analiz artık bu 'x' verisi üzerinden yapılabilir.");
    }
}