package jazari.speech_to_text_vosk;

import javax.sound.sampled.*;
import org.vosk.*;
import org.json.JSONObject;

public class TurkishSpeechToText {
    
    public static void main(String[] args) throws Exception {
        // Mikrofondan ses kaydı için format belirleme
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Mikrofon bulunamadı veya desteklenmiyor");
            System.exit(1);
        }

        // Türkçe dil modelinin yolunu belirtin
        // Türkçe model indirme linki: https://alphacephei.com/vosk/models/vosk-model-small-tr-0.3.zip
        String modelPath = "models/speech_to_text/vosk-model-small-tr-0.3";
        
        System.out.println("Türkçe Vosk modeli yükleniyor...");
        Model model = new Model(modelPath);
        
        System.out.println("Konuşma tanıyıcı başlatılıyor...");
        Recognizer recognizer = new Recognizer(model, 16000);
        
        System.out.println("Mikrofondan kayıt başlatılıyor...");
        TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();
        
        System.out.println("Konuşmaya başlayabilirsiniz. Çıkmak için CTRL+C tuşlarına basın.");
        
        byte[] buffer = new byte[4096];
        boolean continueRecognition = true;
        
        while (continueRecognition) {
            int numBytesRead = microphone.read(buffer, 0, buffer.length);
            
            if (numBytesRead > 0) {
                if (recognizer.acceptWaveForm(buffer, numBytesRead)) {
                    // Sonuç alındığında
                    String result = recognizer.getResult();
                    JSONObject jsonResult = new JSONObject(result);
                    String text = jsonResult.getString("text");
                    
                    if (!text.isEmpty()) {
                        System.out.println("Tanınan metin: " + text);
                    }
                } else {
                    // Ara sonuçlar (henüz tamamlanmamış cümle)
                    String partialResult = recognizer.getPartialResult();
                    JSONObject jsonPartial = new JSONObject(partialResult);
                    String partialText = jsonPartial.getString("partial");
                    
                    if (!partialText.isEmpty()) {
                        System.out.print("\rGeçici sonuç: " + partialText);
                    }
                }
            }
        }
        
        // Temizlik yapılıyor
        recognizer.close();
        model.close();
        microphone.stop();
        microphone.close();
    }
}