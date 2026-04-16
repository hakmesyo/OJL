

import jazari.matrix.CMatrix;

/**
 * ========================================================================
 * MÜHENDİSLİĞİN TEMEL DENKLEMİ: Ax = b
 * UYGULAMA 1: KAMERA KALİBRASYONU (Camera Calibration)
 * ========================================================================
 * 
 * 1. KAMERA KALİBRASYONU NEDİR?
 * Kamera kalibrasyonu, 3 boyutlu (3D) gerçek dünya koordinatlarındaki bir noktanın, 
 * 2 boyutlu (2D) kamera sensöründe (fotoğrafta) tam olarak hangi piksele düşeceğini 
 * hesaplayan matematiksel bir modelin parametrelerini bulma işlemidir.
 * Bu parametreler ikiye ayrılır:
 *   - İç Parametreler (Intrinsic): Odak uzaklığı, optik merkez, lens bükülmeleri (distortion).
 *   - Dış Parametreler (Extrinsic): Kameranın 3D dünyadaki konumu ve açısı (Rotation/Translation).
 * 
 * 2. NİÇİN İHTİYAÇ VARDIR VE HANGİ PROBLEMLERDE KULLANILIR?
 * Lensler ışığı büker. Fotoğraftaki bir pikselin gerçek dünyada kaç metreye denk geldiğini 
 * bilmek için kameranın bu bükme/izdüşüm karakteristiğini (A matrisi) çözmemiz gerekir.
 *   - Otonom Araçlar: Öndeki aracın tam olarak kaç metre uzakta olduğunu hesaplamak (Mesafe tahmini).
 *   - Artırılmış Gerçeklik (AR/VR): Sanal bir objenin (Pokemon veya IKEA koltuğu) gerçek zemine tam oturması.
 *   - Endüstriyel Kalite Kontrol: Fabrika bantlarındaki ürünlerin milimetrik ölçümleri.
 * 
 * 3. PROJELERDE BU İHTİYAÇ SÜREKLİ OLUR MU?
 * EVET. Kameranın lensi değiştiğinde, kamera robota/araca bir milimetre bile farklı açıyla 
 * takıldığında kalibrasyon matrisi değişir ve Ax=b denkleminin yeniden çözülmesi gerekir. 
 * Sadece görüntü sınıflandırma (kedi mi köpek mi?) yapılıyorsa gerekmez, ancak 
 * uzaysal "Ölçüm" veya "Konumlandırma" varsa kalibrasyon şarttır.
 * 
 * 4. ÖRNEK VERİ SETLERİ VE PLATFORMLAR NELERDİR?
 * Endüstri standardı otonom sürüş veri setleri, fotoğrafların yanında kalibrasyon matrislerini de verir.
 *   - KITTI Vision Benchmark Suite: Otonom algoritmaların test edildiği en ünlü veri setidir.
 *   - Cityscapes Dataset: Kalibre edilmiş stereo kamera görüntüleri sunar.
 *   - OpenCV & MATLAB: Geliştiriciler kendi kameralarını "dama tahtası (checkerboard)" göstererek 
 *     kalibre etmek için bu kütüphanelerdeki hazır kalibrasyon fonksiyonlarını kullanırlar.
 * 
 * ------------------------------------------------------------------------
 * DENKLEM: A * x = b  =>  x = A^-1 * b
 * A: 3D Dünya Koordinatları (Gözlemlenen dama tahtası köşeleri)
 * b: 2D Sensör Pikselleri (Bu köşelerin fotoğrafta düştüğü yerler)
 * x: Bulmak istediğimiz İzdüşüm/Kalibrasyon parametreleri
 * ========================================================================
 */
public class TestCameraCalibrationAxB {

    public static void main(String[] args) {
        System.out.println("--- KAMERA KALİBRASYONU PARAMETRE ÇÖZÜMÜ BAŞLIYOR ---\n");
        
        // A Matrisi (Sistem Katsayıları): 3D Dünya Uzayından alınan referans noktalar (Örn: Dama tahtası)
        float[][] A_DunyaKoordinatlari = {
            {20.0f,  10.0f, -10.0f},
            {-30.0f, -10.0f,  20.0f},
            {-20.0f,  10.0f,  20.0f}
        };
        
        // b Vektörü (Ölçülen Çıktı): Kamerada (Görüntüde) bu noktaların düştüğü piksel (u, v) değerleri
        float[][] b_PikselDegerleri = {
            {800},
            {-110},
            {-300}
        };

        // OJL kullanarak dizileri yüksek performanslı CMatrix nesnelerine dönüştürüyoruz
        CMatrix cmA = CMatrix.getInstance(A_DunyaKoordinatlari).println("A Matrisi (3D Dünya Noktaları):");
        CMatrix cmB = CMatrix.getInstance(b_PikselDegerleri).println("b Vektörü (2D Sensör Ölçümleri/Pikseller):");

        System.out.println("Hesaplanıyor: x = A^-1 * b ...\n");
        
        // ÇÖZÜM: OJL'nin gücü ile tek satırda A'nın tersini alıp b ile nokta çarpımı (dot product) yapıyoruz.
        CMatrix cmX = cmA.clone().inv().dot(cmB);
        
        cmX.formatFloat(3).println("x Vektörü (BULUNAN KAMERA KALİBRASYON PARAMETRELERİ):");
        System.out.println("YORUM: Elde edilen bu 'x' parametreleri artık bu kameranın beynidir.");
        System.out.println("Kamera yazılımına bu x değerleri yüklenerek gerçek dünya ölçümleri yapılabilir.");
    }
}