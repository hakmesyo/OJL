/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.speech_to_text_whisper;


//import io.github.givimad.whisperjni.WhisperFullParams;
//import io.github.givimad.whisperjni.WhisperJNI;
//
//import javax.sound.sampled.*;
//import java.nio.file.Paths;

public class WhisperTest {

//    private static final int SAMPLE_RATE = 16000; // Whisper 16kHz bekliyor
//    private static final String MODEL_PATH = "models/speech_to_text/ggml-tiny.bin"; // Model yolunu ayarlayın
//    //private static final String MODEL_PATH = "models/speech_to_text/ggml-base.bin"; // Model yolunu ayarlayın
//    //private static final String MODEL_PATH = "models/speech_to_text/ggml-small.bin"; // Model yolunu ayarlayın
//
//    public static void main(String[] args) {
//        try {
//            // Whisper kütüphanesini yükle
//            WhisperJNI.loadLibrary();
//            WhisperJNI.setLibraryLogger(null); // Log çıktılarını kapat (isteğe bağlı)
//
//            // Whisper oluştur
//            WhisperJNI whisper = new WhisperJNI();
//            
//            // Model dosyasının var olduğundan emin olun
//            var modelPath = Paths.get(MODEL_PATH);
//            System.out.println("Model yükleniyor: " + modelPath.toAbsolutePath());
//            
//            // Whisper modelini yükle
//            var ctx = whisper.init(modelPath);
//            
//            // Parametreleri ayarla
//            var params = new WhisperFullParams();
//            params.language = "tr"; // Türkçe için - "language" field olarak erişilir
//            params.translate = false; // Çeviri istemiyoruz
//            params.noContext = true; // Her tanıma bağımsız olsun
//            params.singleSegment = true; // Her tanıma tek segment olarak kabul edilsin
//            
//            // Mikrofon ayarları
//            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
//            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
//            
//            if (!AudioSystem.isLineSupported(info)) {
//                System.err.println("Mikrofon bulunamadı veya desteklenmiyor");
//                return;
//            }
//            
//            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
//            microphone.open(format);
//            microphone.start();
//            
//            System.out.println("Konuşmaya başlayabilirsiniz. Çıkmak için ENTER tuşuna basın.");
//            
//            // Kayıt ve tanıma için thread oluştur
//            Thread recognitionThread = new Thread(() -> {
//                try {
//                    // Her 3 saniyede bir tanıma yap
//                    byte[] buffer = new byte[3 * SAMPLE_RATE * 2]; // 3 saniyelik 16-bit audio
//                    
//                    while (true) {
//                        // Sesi oku
//                        int bytesRead = microphone.read(buffer, 0, buffer.length);
//                        
//                        if (bytesRead <= 0) continue;
//                        
//                        // Byte array'i float array'e dönüştür (Whisper float[] bekliyor)
//                        float[] samples = convertBytesToFloats(buffer, bytesRead);
//                        
//                        // Whisper ile tanıma yap
//                        int result = whisper.full(ctx, params, samples, samples.length);
//                        
//                        if (result != 0) {
//                            System.err.println("Tanıma hatası: " + result);
//                            continue;
//                        }
//                        
//                        // Sonuçları al
//                        int numSegments = whisper.fullNSegments(ctx);
//                        
//                        if (numSegments > 0) {
//                            StringBuilder recognizedText = new StringBuilder();
//                            
//                            for (int i = 0; i < numSegments; i++) {
//                                String segmentText = whisper.fullGetSegmentText(ctx, i);
//                                recognizedText.append(segmentText);
//                            }
//                            
//                            String text = recognizedText.toString().trim();
//                            if (!text.isEmpty()) {
//                                System.out.println("Tanınan metin: " + text);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            });
//            
//            recognitionThread.setDaemon(true);
//            recognitionThread.start();
//            
//            // Ana thread'de ENTER tuşuna basılmasını bekle
//            System.in.read();
//            
//            // Temizlik
//            microphone.stop();
//            microphone.close();
//            ctx.close();
//            
//            System.out.println("Program sonlandırıldı.");
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//    
//    // Byte array'i float array'e dönüştürme (16-bit PCM, Little Endian)
//    private static float[] convertBytesToFloats(byte[] bytes, int length) {
//        length = length & ~1; // Çift sayıya yuvarla
//        float[] floats = new float[length / 2];
//        
//        for (int i = 0; i < length; i += 2) {
//            // 16-bit PCM, Little Endian
//            short sample = (short) ((bytes[i+1] & 0xff) << 8 | (bytes[i] & 0xff));
//            // Normalize to [-1.0, 1.0]
//            floats[i/2] = sample / 32768.0f;
//        }
//        
//        return floats;
//    }
}