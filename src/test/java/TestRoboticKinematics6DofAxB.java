

import jazari.matrix.CMatrix;

/**
 * ========================================================================
 * MÜHENDİSLİĞİN TEMEL DENKLEMİ: Ax = b
 * UYGULAMA 3 (GELİŞMİŞ): 6 DOF ENDÜSTRİYEL ROBOT KİNEMATİĞİ (KUKA, ABB vb.)
 * ========================================================================
 * 
 * 1. 6 DOF (6 SERBESTLİK DERECESİ) NEDİR?
 * Bir robotun uzayda herhangi bir X, Y, Z koordinatına ulaşması (Pozisyon) ve 
 * o noktada aletini (kaynak torcu, matkap, tutucu) istediği Roll, Pitch, Yaw 
 * (Oryantasyon/Yönelim) açılarında tutabilmesi için tam 6 bağımsız ekleme 
 * (motora) ihtiyacı vardır. 
 * 
 * KUKA gibi endüstriyel robotlarda eksenler dipten uca şu şekilde sıralanır:
 *   - Eksen 1 (Base): Gövdenin sağa-sola dönüşü (Bel)
 *   - Eksen 2 (Shoulder): Ana kolun ileri-geri hareketi (Omuz)
 *   - Eksen 3 (Elbow): Ön kolun bükülmesi (Dirsek)
 *     -> (Bu ilk 3 eksen X, Y, Z pozisyonunu belirler)
 *   - Eksen 4 (Wrist Roll): Ön kolun kendi etrafında dönmesi
 *   - Eksen 5 (Wrist Pitch): Bileğin yukarı-aşağı bükülmesi
 *   - Eksen 6 (Tool Roll): Tam uç noktasının kendi etrafında dönmesi (Matkap ucu gibi)
 *     -> (Bu son 3 eksen (Spherical Wrist) aletin uzaydaki duruş açısını (Oryantasyon) belirler)
 * 
 * 2. DENKLEM: J * Teta = V  =>  Teta = J^-1 * V
 *   - A (J) Matrisi: 6x6 boyutundaki "Jacobian Matrisi". 
 *     Her bir sütun bir motoru (6 adet), her bir satır ise uzaydaki 
 *     bir serbestlik derecesini (Vx, Vy, Vz, Wx, Wy, Wz) temsil eder.
 *   - b (V) Vektörü: 6x1 boyutundaki Hedef Hız Vektörü.
 *     İlk 3 eleman: Ulaşmak istediğimiz çizgisel hızlar (X, Y, Z mm/sn)
 *     Son 3 eleman: Ulaşmak istediğimiz açısal hızlar (Roll, Pitch, Yaw rad/sn)
 *   - x (Teta) Vektörü: 6x1 boyutunda. OJL'nin çözeceği, robotun 6 motoruna 
 *     eşzamanlı olarak gönderilecek olan açısal hız komutları (rad/sn).
 * ========================================================================
 */
public class TestRoboticKinematics6DofAxB {

    public static void main(String[] args) {
        System.out.println("--- 6 DOF ENDÜSTRİYEL ROBOT TERS KİNEMATİK (INVERSE JACOBIAN) ---");
        System.out.println("Problem: Robot ucunun 6D (3 Çizgisel + 3 Açısal) hedef hızına ulaşması için");
        System.out.println("         6 motorun (eklemin) dönme hızlarını eşzamanlı olarak bulma.\n");
        
        // A Matrisi (Jacobian Matrisi - 6x6):
        // Satırlar: Uzaydaki 6 boyut (X, Y, Z, Roll, Pitch, Yaw)
        // Sütunlar: 6 adet motor (Base, Shoulder, Elbow, Wrist1, Wrist2, Tool)
        // Değerler, robotun o anki duruşuna ve kol uzunluklarına bağlı türevsel oranlardır.
        float[][] A_Jacobian_6x6 = {
            { 0.0f,   1.2f,   1.5f,   0.0f,   0.0f,  0.0f}, // d(X)     / d(Teta 1..6)
            { 1.8f,   0.0f,   0.0f,   0.0f,  -0.5f,  0.0f}, // d(Y)     / d(Teta 1..6)
            { 0.0f,  -1.5f,  -0.8f,   0.0f,   0.0f,  0.0f}, // d(Z)     / d(Teta 1..6)
            { 0.0f,   0.0f,   0.0f,   1.0f,   0.0f,  0.7f}, // d(Roll)  / d(Teta 1..6)
            {-1.0f,   0.0f,   0.0f,   0.0f,   1.0f,  0.0f}, // d(Pitch) / d(Teta 1..6)
            { 0.0f,  -1.0f,  -1.0f,   0.0f,   0.0f,  1.0f}  // d(Yaw)   / d(Teta 1..6)
        };
        
        // b Vektörü (Hedef Uç Hızı Vektörü - 6x1):
        // Robotun ucunun (TCP) anlık olarak gitmesini istediğimiz uzaysal hedef hızlar.
        float[][] b_TargetVelocity_6D = {
            { 50.0f},  // Vx: X ekseninde 50 mm/sn çizgisel hız
            { 0.0f},   // Vy: Y ekseninde hareket etmesin
            {-20.0f},  // Vz: Z ekseninde (aşağı) 20 mm/sn çizgisel hız
            { 0.1f},   // Wx (Roll): X ekseni etrafında 0.1 rad/sn dönme
            { 0.0f},   // Wy (Pitch): Y ekseni etrafında dönmesin
            {-0.05f}   // Wz (Yaw): Z ekseni etrafında -0.05 rad/sn dönme
        };

        // OJL ile matrisleri oluşturup ekrana yazdırıyoruz
        CMatrix cmA = CMatrix.getInstance(A_Jacobian_6x6).println("A Matrisi (6x6 Jacobian - Anlık Geometrik/Kaldıraç Modeli):");
        CMatrix cmB = CMatrix.getInstance(b_TargetVelocity_6D).println("b Vektörü (6x1 İstenen Uç Hızları [Vx, Vy, Vz, Wx, Wy, Wz]):");

        System.out.println("Hesaplanıyor: x = A^-1 * b (Jacobian Ters Kinematik Çözümü) ...\n");
        
        // ÇÖZÜM: 6x6 matrisin tersini alıp 6x1 vektör ile çarpmak OJL için çok hafif bir işlemdir.
        // Endüstriyel robotlar bu işlemi saniyede binlerce kez (1kHz - 2kHz) döngüde yaparlar.
        CMatrix cmX = cmA.clone().inv().dot(cmB);
        
        cmX.formatFloat(4).println("x Vektörü (6 MOTORA EŞZAMANLI GÖNDERİLECEK AÇISAL HIZ KOMUTLARI - rad/sn):");
        System.out.println("YORUM:");
        System.out.println("1. Satır -> Eksen 1 (Base)     Motorunun anlık dönüş hızı");
        System.out.println("2. Satır -> Eksen 2 (Shoulder) Motorunun anlık dönüş hızı");
        System.out.println("3. Satır -> Eksen 3 (Elbow)    Motorunun anlık dönüş hızı");
        System.out.println("4. Satır -> Eksen 4 (Wrist 1)  Motorunun anlık dönüş hızı");
        System.out.println("5. Satır -> Eksen 5 (Wrist 2)  Motorunun anlık dönüş hızı");
        System.out.println("6. Satır -> Eksen 6 (Tool)     Motorunun anlık dönüş hızı");
        System.out.println("\nBu 6 motor aynı anda bu hızlarla dönerse, robotun kaynak ucu");
        System.out.println("tam olarak b vektöründeki hıza (hem pozisyon hem yönelim olarak) ulaşacaktır.");
    }
}