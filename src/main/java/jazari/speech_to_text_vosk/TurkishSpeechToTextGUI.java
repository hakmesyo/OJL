package jazari.speech_to_text_vosk;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.vosk.*;
import org.json.JSONObject;
import com.formdev.flatlaf.FlatLightLaf; // FlatLaf import
import com.formdev.flatlaf.FlatDarkLaf;  // Koyu tema için

public class TurkishSpeechToTextGUI extends JFrame {
    
    private JTextArea resultTextArea;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;
    
    private Model model;
    private Recognizer recognizer;
    private TargetDataLine microphone;
    private Thread recognitionThread;
    private volatile boolean isRunning = false;
    
    public TurkishSpeechToTextGUI() {
        // Pencere ayarları
        setTitle("Türkçe Konuşma Tanıma");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Logo panel (isteğe bağlı)
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Türkçe Konuşma Tanıma");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerPanel.add(titleLabel);
        
        // Sonuç metni için text area
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);
        resultTextArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultTextArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JScrollPane scrollPane = new JScrollPane(resultTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Durum etiketi
        statusLabel = new JLabel("Hazır");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Butonlar için panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        // Başlat butonu
        startButton = new JButton("Dinlemeyi Başlat");
        startButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        startButton.setFocusPainted(false);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startRecognition();
            }
        });
        
        // Durdur butonu
        stopButton = new JButton("Dinlemeyi Durdur");
        stopButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        stopButton.setEnabled(false);
        stopButton.setFocusPainted(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopRecognition();
            }
        });
        
        // Temizle butonu
        JButton clearButton = new JButton("Metni Temizle");
        clearButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultTextArea.setText("");
            }
        });
        
        // Tema değiştirme butonu
        JToggleButton themeToggle = new JToggleButton("Koyu Tema");
        themeToggle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        themeToggle.setFocusPainted(false);
        themeToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (themeToggle.isSelected()) {
                    themeToggle.setText("Açık Tema");
                    setDarkTheme();
                } else {
                    themeToggle.setText("Koyu Tema");
                    setLightTheme();
                }
            }
        });
        
        // Butonları panele ekle
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(themeToggle);
        
        // Panelleri ana pencereye ekle
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Ana paneli pencereye ekle
        getContentPane().add(mainPanel);
        
        // Programı başlatmadan önce model yükleme
        loadModel();
        
        // Penceredeki X (kapat) butonuna basıldığında kaynakları temizle
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }
    
    private void setLightTheme() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void setDarkTheme() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void loadModel() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    statusLabel.setText("Türkçe model yükleniyor...");
                    // Model yolunu projenizin yapısına göre ayarlayın
                    String modelPath = "models/speech_to_text/vosk-model-small-tr-0.3";
                    model = new Model(modelPath);
                    recognizer = new Recognizer(model, 16000);
                    statusLabel.setText("Model yüklendi. Başlamaya hazır.");
                    startButton.setEnabled(true);
                } catch (Exception e) {
                    statusLabel.setText("Model yüklenemedi: " + e.getMessage());
                    JOptionPane.showMessageDialog(TurkishSpeechToTextGUI.this,
                            "Model yüklenirken hata oluştu:\n" + e.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    startButton.setEnabled(false);
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private void startRecognition() {
        try {
            // Mikrofondan ses kaydı için format belirleme
            AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            
            if (!AudioSystem.isLineSupported(info)) {
                JOptionPane.showMessageDialog(this, "Mikrofon bulunamadı veya desteklenmiyor", 
                        "Hata", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();
            
            isRunning = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            statusLabel.setText("Dinleniyor... Konuşmaya başlayabilirsiniz.");
            
            // Dinleme işlemini ayrı bir thread'de başlat
            recognitionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[4096];
                    
                    while (isRunning) {
                        int numBytesRead = microphone.read(buffer, 0, buffer.length);
                        
                        if (numBytesRead > 0) {
                            if (recognizer.acceptWaveForm(buffer, numBytesRead)) {
                                // Sonuç alındığında
                                String result = recognizer.getResult();
                                JSONObject jsonResult = new JSONObject(result);
                                final String text = jsonResult.getString("text");
                                
                                if (!text.isEmpty()) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            resultTextArea.append(text + ".\n");
                                            // En sona kaydır
                                            resultTextArea.setCaretPosition(resultTextArea.getDocument().getLength());
                                        }
                                    });
                                }
                            } else {
                                // Ara sonuçlar (henüz tamamlanmamış cümle)
                                String partialResult = recognizer.getPartialResult();
                                JSONObject jsonPartial = new JSONObject(partialResult);
                                final String partialText = jsonPartial.getString("partial");
                                
                                if (!partialText.isEmpty()) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            statusLabel.setText("Dinleniyor: " + partialText);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });
            recognitionThread.start();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ses kaydı başlatılırken hata oluştu:\n" + e.getMessage(), 
                    "Hata", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            stopRecognition();
        }
    }
    
    private void stopRecognition() {
        isRunning = false;
        
        if (recognitionThread != null) {
            try {
                recognitionThread.join(1000); // Thread'in sonlanmasını en fazla 1 saniye bekle
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recognitionThread = null;
        }
        
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        statusLabel.setText("Dinleme durduruldu.");
    }
    
    private void cleanup() {
        stopRecognition();
        
        if (recognizer != null) {
            recognizer.close();
            recognizer = null;
        }
        
        if (model != null) {
            model.close();
            model = null;
        }
    }
    
    public static void main(String[] args) {
        try {
            // FlatLaf temasını yükle
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("FlatLaf teması yüklenemedi: " + ex);
            // Varsayılan görünüme geri dön
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Swing uygulamasını EDT (Event Dispatch Thread) üzerinde başlat
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TurkishSpeechToTextGUI app = new TurkishSpeechToTextGUI();
                app.setVisible(true);
            }
        });
    }
}