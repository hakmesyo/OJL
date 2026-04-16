

import jazari.matrix.CMatrix;

/**
 * ========================================================================
 * MÜHENDİSLİĞİN TEMEL DENKLEMİ: Ax = b
 * UYGULAMA 3: ROBOTİK KİNEMATİK (Ters Kinematik / Inverse Jacobian)
 * ========================================================================
 * 
 * 1. ROBOTİK TERS KİNEMATİK NEDİR?
 * Bir robot kolunun uç işlevcisinin (end-effector / tutucu el), uzayda belirli bir 
 * X, Y, Z koordinatına veya hızına ulaşması için, robotun gövde, omuz, dirsek ve 
 * bilek motorlarının (eklemlerinin) hangi açılarda dönmesi gerektiğini hesaplamaktır.
 * 
 * 2. NİÇİN İHTİYAÇ VARDIR VE HANGİ PROBLEMLERDE KULLANILIR?
 * Robotların beyni "eklem açılarını (Radyan/Derece)" anlar ve motorları buna göre sürer.
 * Ancak biz insanlar robota "Şu X, Y, Z koordinatındaki bardağı tut" deriz. 
 * İnsanın anladığı Kartezyen uzay (b) ile robotun anladığı Motor uzayını (x) 
 * birbirine bağlayan matematiksel köprü Jacobian Matrisidir (A).
 *   - Endüstriyel Robotlar: Araba fabrikalarındaki 6 eksenli KUKA, ABB robotlarının
 *     araba kapısına düz bir çizgi halinde kaynak yapması.
 *   - Oyun Motorları ve Animasyon (IK - Inverse Kinematics): Unity/Unreal Engine'de 
 *     bir karakterin yokuş çıkarken ayağının zemine tam olarak basmasını sağlamak.
 *   - Cerrahi Robotlar: Da Vinci robotlarında doktorun konsoldaki el hızının (b), 
 *     hastanın içindeki milimetrik robot kolların (x) hareketlerine dönüştürülmesi.
 * 
 * 3. PROJELERDE BU İHTİYAÇ SÜREKLİ OLUR MU?
 * Endüstriyel otomasyon, robotik manipülasyon ve oyun geliştirmede (CGI/Animasyon) 
 * saniyede yüzlerce kez (real-time) hesaplanan, en kritik kontrol döngüsü problemidir.
 * Robotun ucu her hareket ettiğinde, kolların kaldıracı değiştiği için "A" (Jacobian) 
 * matrisi sürekli güncellenir ve x = A^-1 * b sürekli yeniden çözülür.
 * 
 * ------------------------------------------------------------------------
 * DENKLEM: J * Teta = V  =>  Teta = J^-1 * V  (Ax = b formatının Robotikteki hali)
 * A (J): Jacobian Matrisi - Robotun anlık duruş geometrisi ve kaldıraç oranları.
 * b (V): İstenen hedef hız - Robotun ucunun X,Y,Z yönünde gitmesini istediğimiz hız.
 * x (Teta): Motorlara verilecek dönme hızları (Açısal hız komutları).
 * ========================================================================
 */
public class TestRoboticKinematicsAxB {

    public static void main(String[] args) {
        System.out.println("--- ROBOTİK TERS KİNEMATİK (INVERSE JACOBIAN) HESABI BAŞLIYOR ---\n");
        
        // A Matrisi (Sistem Davranışı): Jacobian Matrisi. 
        // 1. motor dönerse X,Y,Z ne kadar değişir? 2. motor dönerse ne kadar değişir? (Türevsel kaldıraç oranları)
        float[][] A_Jacobian = {
            { 0.5f, -0.2f,  0.0f},
            { 0.1f,  0.8f,  0.4f},
            {-0.3f,  0.1f,  0.6f}
        };
        
        // b Vektörü (Ölçülen/İstenen Çıktı): Robotun ucunun ulaşmasını İSTEDİĞİMİZ Uzaysal Hedef Hızlar (Vx, Vy, Vz)
        float[][] b_HedefUcHizi = {
            { 1.5f}, // X ekseninde saniyede 1.5 birim hız
            { 2.0f}, // Y ekseninde saniyede 2.0 birim hız
            {-1.0f}  // Z ekseninde (aşağı doğru) saniyede 1.0 birim hız
        };

        // OJL ile matrisleri oluşturuyoruz
        CMatrix cmA = CMatrix.getInstance(A_Jacobian).println("A Matrisi (Jacobian - Robotun Anlık Duruş Geometrisi):");
        CMatrix cmB = CMatrix.getInstance(b_HedefUcHizi).println("b Vektörü (İstenen Uç Hızı [Vx, Vy, Vz]):");

        System.out.println("Hesaplanıyor: x = A^-1 * b (Jacobian'ın Tersi * İstenen Hız) ...\n");
        
        // ÇÖZÜM: Robotun motorlarına gidecek komutları bulmak için matrisin tersini alıp vektörle çarpıyoruz
        CMatrix cmX = cmA.clone().inv().dot(cmB);
        
        cmX.formatFloat(4).println("x Vektörü (MOTORLARA GÖNDERİLECEK AÇISAL HIZ KOMUTLARI - rad/sn):");
        System.out.println("YORUM: 1., 2. ve 3. motorlara bu x vektöründeki hız komutları gönderildiğinde,");
        System.out.println("robotun uç noktası uzayda tam olarak istediğimiz b vektöründeki hızda hareket edecektir.");
    }
}