package jazari.llm_chat;

import jazari.speech_to_text_vosk.VoiceRecognitionService;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * JazariChatApp için ses tanıma entegrasyonu
 * Geliştirilmiş kısmi sonuç yönetimi içeren versiyon
 */
public class VoiceIntegration {
    
    // Ses tanıma servisi
    private VoiceRecognitionService voiceService;
    
    // UI bileşenleri
    private final JButton micButton;
    private final JTextArea inputArea;
    private final JLabel statusLabel;
    private boolean isListening = false;
    
    // Mevcut metin yönetimi
    private String currentFieldText = "";
    
    // Ses tanıma modeli yolu
    private static final String MODEL_PATH = "models/speech_to_text/vosk-model-small-tr-0.3";
    
    /**
     * Ses tanıma entegrasyonunu oluşturur
     * 
     * @param inputArea Tanıma sonuçlarının yazılacağı JTextArea
     * @param sendPanel Mikrofon butonunun ekleneceği panel
     * @param statusLabel Durum mesajlarının gösterileceği etiket
     */
    public VoiceIntegration(JTextArea inputArea, JPanel sendPanel, JLabel statusLabel) {
        this.inputArea = inputArea;
        this.statusLabel = statusLabel;
        
        // Mikrofon butonu oluştur
        micButton = createMicrophoneButton();
        
        // Mikrofon butonunu ilgili panele ekle
        sendPanel.add(micButton, BorderLayout.WEST);
        
        // Ses tanıma servisini hazırla
        initVoiceService();
    }
    
    /**
     * Mikrofon butonunu oluşturan metot
     * @return Oluşturulan mikrofon butonu
     */
    private JButton createMicrophoneButton() {
        // İkon kullanmayı dene, yoksa metin göster
        Icon micIcon = null;
        Icon micActiveIcon = null;
        
        try {
            // Kaynakları doğrudan sınıf yükleyici ile almayı dene
            java.net.URL micIconURL = JazariChatApp.class.getClassLoader().getResource("icons/mic_icon.png");
            java.net.URL micActiveIconURL = JazariChatApp.class.getClassLoader().getResource("icons/mic_active_icon.png");
            
            if (micIconURL != null) {
                micIcon = new ImageIcon(micIconURL);
            }
            
            if (micActiveIconURL != null) {
                micActiveIcon = new ImageIcon(micActiveIconURL);
            }
        } catch (Exception e) {
            System.err.println("Mikrofon ikonu yüklenemedi: " + e.getMessage());
        }
        
        // İkonları sakla
        final Icon finalMicIcon = micIcon;
        final Icon finalMicActiveIcon = micActiveIcon;
        
        // Butonu oluştur
        JButton button;
        if (micIcon != null) {
            button = new JButton(micIcon);
        } else {
            button = new JButton("🎤");
            button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        }
        
        button.setToolTipText("Mikrofon ile konuş");
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(Color.DARK_GRAY);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setPreferredSize(new Dimension(40, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Tıklama olayını ekle
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isListening) {
                    stopListening();
                    
                    // İkonu normal hale getir
                    if (finalMicIcon != null) {
                        button.setIcon(finalMicIcon);
                    } else {
                        button.setText("🎤");
                        button.setBackground(new Color(240, 240, 240));
                    }
                } else {
                    startListening();
                    
                    // İkonu aktif hale getir
                    if (finalMicActiveIcon != null) {
                        button.setIcon(finalMicActiveIcon);
                    } else {
                        button.setText("🎤");
                        button.setBackground(new Color(255, 100, 100));
                    }
                }
            }
        });
        
        return button;
    }
    
    /**
     * Ses tanıma servisini başlatır
     */
    private void initVoiceService() {
        try {
            voiceService = new VoiceRecognitionService(MODEL_PATH);
            
            // Geçici sonuçlar için callback - Sadece debug için göstereceğiz
            voiceService.setOnPartialResultCallback(partialText -> {
                if (!partialText.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        // Geçici sonuçları sadece durum çubuğunda göster
                        if (statusLabel != null) {
                            statusLabel.setText("Dinleniyor: " + partialText);
                        }
                    });
                }
            });
            
            // Kararlı geçici sonuçlar için callback
            voiceService.setOnStablePartialResultCallback(stableText -> {
                if (!stableText.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        // Mevcut metin alanı içeriğini kontrol et
                        String fieldText = inputArea.getText();
                        
                        // Placeholder text kontrolü
                        if (fieldText.equals("LLM'e bir soru sorun...")) {
                            fieldText = "";
                        }
                        
                        // Metni akıllıca güncelle
                        String updatedText = intelligentTextUpdate(fieldText, stableText);
                        inputArea.setText(updatedText);
                        inputArea.setForeground(Color.GRAY); // Henüz nihai sonuç değil, gri renkte göster
                    });
                }
            });
            
            // Nihai sonuçlar için callback
            voiceService.setOnFinalResultCallback(finalText -> {
                if (!finalText.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        // Mevcut metin alanı içeriğini kontrol et
                        String fieldText = inputArea.getText();
                        
                        // Placeholder text kontrolü
                        if (fieldText.equals("LLM'e bir soru sorun...")) {
                            fieldText = "";
                        }
                        
                        // Metni akıllıca güncelle ve sonuna nokta ekle
                        String updatedText = intelligentTextUpdate(fieldText, finalText);
                        if (!updatedText.isEmpty() && 
                            !updatedText.endsWith(".") && 
                            !updatedText.endsWith("?") && 
                            !updatedText.endsWith("!")) {
                            updatedText += ".";
                        }
                        
                        inputArea.setText(updatedText);
                        inputArea.setForeground(Color.BLACK); // Nihai sonuç, normal renkte göster
                        
                        // Mevcut metni güncelle
                        currentFieldText = updatedText;
                    });
                }
            });
            
            // Durum değişiklikleri için callback
            voiceService.setOnStatusChangeCallback(status -> {
                SwingUtilities.invokeLater(() -> {
                    // NullPointerException kontrolü eklendi
                    if (statusLabel != null) {
                        statusLabel.setText(status);
                    } else {
                        System.out.println("Durum: " + status + " (statusLabel null olduğu için gösterilemiyor)");
                    }
                });
            });
            
            // Hata durumları için callback
            voiceService.setOnErrorCallback(error -> {
                SwingUtilities.invokeLater(() -> {
                    stopListening();
                    JOptionPane.showMessageDialog(null,
                            "Ses tanıma hatası: " + error.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                    
                    // NullPointerException kontrolü eklendi
                    if (statusLabel != null) {
                        statusLabel.setText("Hata: " + error.getMessage());
                    } else {
                        System.out.println("Hata: " + error.getMessage() + " (statusLabel null olduğu için gösterilemiyor)");
                    }
                    
                    micButton.setBackground(new Color(240, 240, 240));
                });
            });
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Ses tanıma modeli yüklenemedi: " + e.getMessage() +
                    "\nModel yolu: " + MODEL_PATH,
                    "Model Hatası", JOptionPane.ERROR_MESSAGE);
            
            // Butonu devre dışı bırak
            micButton.setEnabled(false);
        }
    }
    
    /**
     * Mevcut metni ve tanınan metni akıllıca birleştirir
     * 
     * @param currentText Mevcut metin
     * @param recognizedText Tanınan yeni metin
     * @return Güncellenmiş metin
     */
    private String intelligentTextUpdate(String currentText, String recognizedText) {
        // Eğer mevcut metin boşsa, sadece tanınan metni döndür
        if (currentText == null || currentText.isEmpty()) {
            return recognizedText;
        }
        
        // Eğer mevcut metin bir noktalama işareti ile bitiyorsa
        if (currentText.endsWith(".") || currentText.endsWith("?") || currentText.endsWith("!")) {
            // Yeni metni bir boşlukla ekle
            return currentText + " " + recognizedText;
        } else {
            // Önceki cümleyi tamamlayıp yeni bir cümle başlat
            // Mevcut metindeki son noktalama işaretini bul
            int lastPeriod = Math.max(
                Math.max(currentText.lastIndexOf('.'), currentText.lastIndexOf('?')), 
                currentText.lastIndexOf('!')
            );
            
            if (lastPeriod >= 0) {
                // Eğer noktalama işareti varsa, oraya kadar olan kısmı koru ve sonrasını değiştir
                return currentText.substring(0, lastPeriod + 1) + " " + recognizedText;
            } else {
                // Eğer noktalama işareti yoksa, metni tamamen değiştir
                return recognizedText;
            }
        }
    }
    
    /**
     * Dinlemeyi başlatır
     */
    private void startListening() {
        if (voiceService != null) {
            try {
                // Placeholder text kontrolü
                if (inputArea.getText().equals("LLM'e bir soru sorun...")) {
                    inputArea.setText("");
                }
                
                // Mevcut metni kaydet
                currentFieldText = inputArea.getText();
                
                voiceService.startListening();
                isListening = true;
                
                // NullPointerException kontrolü eklendi
                if (statusLabel != null) {
                    statusLabel.setText("Dinleniyor... Konuşmaya başlayabilirsiniz.");
                } else {
                    System.out.println("Dinleniyor... Konuşmaya başlayabilirsiniz. (statusLabel null olduğu için gösterilemiyor)");
                }
                
            } catch (LineUnavailableException e) {
                JOptionPane.showMessageDialog(null,
                        "Mikrofon açılamadı: " + e.getMessage(),
                        "Mikrofon Hatası", JOptionPane.ERROR_MESSAGE);
                stopListening();
            }
        }
    }
    
    /**
     * Dinlemeyi durdurur
     */
    private void stopListening() {
        if (voiceService != null) {
            voiceService.stopListening();
        }
        
        isListening = false;
        
        // Mikrofon butonunu normal duruma getir
        micButton.setBackground(new Color(240, 240, 240));
        micButton.setText("🎤");
        
        // NullPointerException kontrolü eklendi
        if (statusLabel != null) {
            statusLabel.setText("Dinleme durduruldu.");
        } else {
            System.out.println("Dinleme durduruldu. (statusLabel null olduğu için gösterilemiyor)");
        }
    }
    
    /**
     * Kaynakları temizler
     */
    public void cleanup() {
        if (voiceService != null) {
            voiceService.stopListening();
            voiceService.cleanup();
        }
    }
    
    /**
     * JazariChatApp'e ses tanımayı entegre etmek için kullanılacak statik metot
     * 
     * @param inputArea Tanıma sonuçlarının yazılacağı JTextArea
     * @param buttonPanel Mikrofon butonunun ekleneceği panel
     * @param statusLabel Durum mesajlarının gösterileceği etiket
     * @return Oluşturulan VoiceIntegration nesnesi
     */
    public static VoiceIntegration integrate(JTextArea inputArea, JPanel buttonPanel, JLabel statusLabel) {
        return new VoiceIntegration(inputArea, buttonPanel, statusLabel);
    }
}