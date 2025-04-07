package jazari.speech_to_text_vosk;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import org.vosk.*;
import org.json.JSONObject;

/**
 * Türkçe ses tanıma için servis sınıfı
 * Kısmi sonuçları filtreleyen ve kararlı kısımları belirleyen geliştirilmiş versiyon
 */
public class VoiceRecognitionService {
    
    // Tanıma modeli ve tanıyıcı
    private Model model;
    private Recognizer recognizer;
    
    // Kayıt için gerekli bileşenler
    private TargetDataLine microphone;
    private Thread recognitionThread;
    private volatile boolean isRunning = false;
    
    // Duraksama algılama için değişkenler
    private Timer silenceTimer;
    private long lastSoundTime = 0; // Son ses algılama zamanı
    private static final double SOUND_THRESHOLD = 100.0; // Ses eşiği
    
    // Kısmi sonuç yönetimi için değişkenler
    private String lastPartialResult = ""; // Son kısmi sonuç
    private String stableText = ""; // Kararlı metin kısmı 
    private int stableCount = 0; // Bir metnin kaç kez tekrarlandığı
    private static final int STABLE_THRESHOLD = 3; // Kararlı sayılması için eşik değer
    
    // Dinleme durumu için callback'ler
    private Consumer<String> onPartialResultCallback;  // Geçici sonuç için callback
    private Consumer<String> onStablePartialResultCallback; // Kararlı geçici sonuç için callback
    private Consumer<String> onFinalResultCallback;    // Nihai sonuç için callback
    private Consumer<String> onStatusChangeCallback;   // Durum değişikliği için callback
    private Consumer<Exception> onErrorCallback;       // Hata durumu için callback
    
    /**
     * Ses tanıma servisini belirtilen model yolunu kullanarak başlatır
     * @param modelPath Türkçe ses tanıma modeli yolu
     * @throws IOException Model yüklenemezse
     */
    public VoiceRecognitionService(String modelPath) throws IOException {
        try {
            // Model ve tanıyıcıyı oluştur
            model = new Model(modelPath);
            recognizer = new Recognizer(model, 16000);
        } catch (IOException e) {
            cleanup();
            throw e;
        }
    }
    
    /**
     * Ses tanımayı başlatır ve mikrofonu açar
     * @throws LineUnavailableException Mikrofon kullanılamıyorsa
     */
    public void startListening() throws LineUnavailableException {
        if (isRunning) {
            return; // Zaten çalışıyorsa tekrar başlatma
        }
        
        // Kısmi sonuç değişkenlerini sıfırla
        lastPartialResult = "";
        stableText = "";
        stableCount = 0;
        
        // Mikrofon ayarları
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        
        if (!AudioSystem.isLineSupported(info)) {
            if (onErrorCallback != null) {
                onErrorCallback.accept(new Exception("Mikrofon bulunamadı veya desteklenmiyor"));
            }
            return;
        }
        
        try {
            // Mikrofonu aç ve kayda başla
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            
            isRunning = true;
            
            if (onStatusChangeCallback != null) {
                onStatusChangeCallback.accept("Dinleniyor...");
            }
            
            // Duraksama algılama timerını başlat
            silenceTimer = new Timer(true);
            silenceTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    checkSilence();
                }
            }, 250, 250); // Her 250 ms'de bir sessizliği kontrol et
            
            // Tanıma işlemini ayrı bir thread'de başlat
            recognitionThread = new Thread(() -> {
                byte[] buffer = new byte[4096];
                
                while (isRunning) {
                    int numBytesRead = microphone.read(buffer, 0, buffer.length);
                    
                    if (numBytesRead > 0) {
                        // Ses seviyesini kontrol et
                        double level = calculateSoundLevel(buffer, numBytesRead);
                        if (level > SOUND_THRESHOLD) {
                            lastSoundTime = System.currentTimeMillis();
                        }
                        
                        if (recognizer.acceptWaveForm(buffer, numBytesRead)) {
                            // Nihai sonuç alındığında
                            String result = recognizer.getResult();
                            JSONObject jsonResult = new JSONObject(result);
                            String text = jsonResult.getString("text");
                            
                            if (!text.isEmpty()) {
                                if (onFinalResultCallback != null) {
                                    // Nihai sonuç geldiğinde, kararlı metni de sıfırla
                                    stableText = "";
                                    onFinalResultCallback.accept(text);
                                }
                            }
                        } else {
                            // Ara sonuçlar (henüz tamamlanmamış cümle)
                            String partialResult = recognizer.getPartialResult();
                            JSONObject jsonPartial = new JSONObject(partialResult);
                            String partialText = jsonPartial.getString("partial");
                            
                            if (!partialText.isEmpty()) {
                                // Kısmi sonuçları işle ve filtrele
                                processPartialResult(partialText);
                            }
                        }
                    }
                }
            });
            
            recognitionThread.start();
            
        } catch (LineUnavailableException e) {
            cleanup();
            if (onErrorCallback != null) {
                onErrorCallback.accept(e);
            }
            throw e;
        }
    }
    
    /**
     * Kısmi sonuçları işleyerek kararlı ve değişen kısımları belirler
     * @param partialText Gelen kısmi sonuç metni
     */
    private void processPartialResult(String partialText) {
        // Tekrar eden kelimeleri filtrele
        partialText = removeRepeatedWords(partialText);
        
        // Kısmi sonuç değişmediyse
        if (partialText.equals(lastPartialResult)) {
            stableCount++;
            
            // Belirli bir sayıya ulaştıysa kararlı kabul et
            if (stableCount >= STABLE_THRESHOLD && !partialText.equals(stableText)) {
                stableText = partialText;
                if (onStablePartialResultCallback != null) {
                    onStablePartialResultCallback.accept(stableText);
                }
            }
        } else {
            // Değiştiyse sayacı sıfırla
            stableCount = 0;
            lastPartialResult = partialText;
            
            // Normal kısmi sonuç olarak gönder
            if (onPartialResultCallback != null) {
                onPartialResultCallback.accept(partialText);
            }
        }
    }
    
    /**
     * Tekrar eden kelimeleri temizler
     * @param text Temizlenecek metin
     * @return Temizlenmiş metin
     */
    private String removeRepeatedWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        String lastWord = "";
        
        for (String word : words) {
            // Eğer kelime son kelimeden farklıysa ekle
            if (!word.equals(lastWord)) {
                result.append(word).append(" ");
                lastWord = word;
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Sessizliği kontrol eden metot
     * Belirli bir süre sessizlik algılanırsa otomatik olarak dinlemeyi durdurur
     */
    private void checkSilence() {
        if (!isRunning) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastSound = currentTime - lastSoundTime;
        
        // 1 saniyeden fazla sessizlik varsa ve halihazırda konuşma tanıma yapılmışsa
        if (timeSinceLastSound > 1000 && (!stableText.isEmpty() || !lastPartialResult.isEmpty())) {
            stopListening();
        }
    }
    
    /**
     * Ses verilerinden ses seviyesini hesaplar
     * @param buffer Ses verisi
     * @param length Veri uzunluğu
     * @return Ses seviyesi
     */
    private double calculateSoundLevel(byte[] buffer, int length) {
        double sum = 0;
        // 16-bit PCM, LittleEndian
        for (int i = 0; i < length; i += 2) {
            int sample = ((buffer[i + 1] & 0xff) << 8) | (buffer[i] & 0xff);
            sum += Math.abs(sample);
        }
        return sum / (length / 2);
    }
    
    /**
     * Ses tanımayı durdurur ve kaynakları kapatır
     */
    public void stopListening() {
        isRunning = false;
        
        if (silenceTimer != null) {
            silenceTimer.cancel();
            silenceTimer = null;
        }
        
        if (recognitionThread != null) {
            try {
                recognitionThread.join(1000); // Thread'in sonlanmasını en fazla 1 saniye bekle
            } catch (InterruptedException e) {
                if (onErrorCallback != null) {
                    onErrorCallback.accept(e);
                }
            }
            recognitionThread = null;
        }
        
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }
        
        if (onStatusChangeCallback != null) {
            onStatusChangeCallback.accept("Dinleme durduruldu");
        }
    }
    
    /**
     * Tüm kaynakları temizler ve servis nesnesini kapatır
     */
    public void cleanup() {
        stopListening();
        
        if (recognizer != null) {
            recognizer.close();
            recognizer = null;
        }
        
        if (model != null) {
            model.close();
            model = null;
        }
    }
    
    /**
     * Servisin çalışır durumda olup olmadığını kontrol eder
     * @return Çalışıyor ise true, değilse false
     */
    public boolean isListening() {
        return isRunning;
    }
    
    // Callback ayarlayıcı metodlar
    
    /**
     * Geçici tanıma sonuçları için callback ayarlar
     * @param callback Geçici sonuç ile çağrılacak fonksiyon
     */
    public void setOnPartialResultCallback(Consumer<String> callback) {
        this.onPartialResultCallback = callback;
    }
    
    /**
     * Kararlı geçici tanıma sonuçları için callback ayarlar
     * @param callback Kararlı geçici sonuç ile çağrılacak fonksiyon
     */
    public void setOnStablePartialResultCallback(Consumer<String> callback) {
        this.onStablePartialResultCallback = callback;
    }
    
    /**
     * Nihai tanıma sonuçları için callback ayarlar
     * @param callback Nihai sonuç ile çağrılacak fonksiyon
     */
    public void setOnFinalResultCallback(Consumer<String> callback) {
        this.onFinalResultCallback = callback;
    }
    
    /**
     * Durum değişiklikleri için callback ayarlar
     * @param callback Durum mesajı ile çağrılacak fonksiyon
     */
    public void setOnStatusChangeCallback(Consumer<String> callback) {
        this.onStatusChangeCallback = callback;
    }
    
    /**
     * Hata durumları için callback ayarlar
     * @param callback Hata nesnesi ile çağrılacak fonksiyon
     */
    public void setOnErrorCallback(Consumer<Exception> callback) {
        this.onErrorCallback = callback;
    }
}