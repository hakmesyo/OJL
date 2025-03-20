package jazari.llm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Olayları izlemek ve görüntülemek için özel günlük sınıfı
 */
public class EventLogger {

    private JTextArea logArea;
    private JFrame logFrame;
    private boolean isVisible = false;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    /**
     * Olay izleyici penceresini oluşturur
     */
    public EventLogger() {
        // Log penceresi oluştur
        logFrame = new JFrame("Olay İzleyici");
        logFrame.setSize(600, 400);
        logFrame.setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(new Color(45, 45, 48));
        logArea.setForeground(new Color(220, 220, 220));

        JScrollPane scrollPane = new JScrollPane(logArea);
        logFrame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(60, 60, 63));

        JButton clearButton = new JButton("Temizle");
        clearButton.addActionListener((ActionEvent e) -> logArea.setText(""));

        JButton saveButton = new JButton("Kaydet");
        saveButton.addActionListener((ActionEvent e) -> saveLogToFile());

        buttonPanel.add(clearButton);
        buttonPanel.add(saveButton);

        logFrame.add(buttonPanel, BorderLayout.SOUTH);
        logFrame.setLocationRelativeTo(null);

        // İlk log kaydı
        log("Olay İzleyici başlatıldı");
    }

    /**
     * Bir olay mesajını günlüğe kaydeder
     */
    public void log(String message) {
        if (logArea != null) {
            String timestamp = timeFormat.format(new Date());
            String logEntry = "[" + timestamp + "] " + message + "\n";

            logArea.append(logEntry);
            // Otomatik kaydır
            logArea.setCaretPosition(logArea.getDocument().getLength());

            // Standart çıktıya da yaz (debug için)
            System.out.println(logEntry);
        }
    }

    /**
     * Günlüğü bir dosyaya kaydet
     */
    private void saveLogToFile() {
        try {
            // Zaman damgalı dosya adı oluştur
            String fileName = "gemma3_log_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";

            // Dosya seçiciyi göster
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Günlüğü Kaydet");
            fileChooser.setSelectedFile(new java.io.File(fileName));

            int userSelection = fileChooser.showSaveDialog(logFrame);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();

                try (java.io.FileWriter writer = new java.io.FileWriter(fileToSave)) {
                    writer.write(logArea.getText());
                }

                JOptionPane.showMessageDialog(
                        logFrame,
                        "Günlük dosyası başarıyla kaydedildi:\n" + fileToSave.getAbsolutePath(),
                        "Başarılı",
                        JOptionPane.INFORMATION_MESSAGE
                );

                log("Günlük dosyası kaydedildi: " + fileToSave.getAbsolutePath());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    logFrame,
                    "Günlük dosyasını kaydederken hata oluştu:\n" + e.getMessage(),
                    "Hata",
                    JOptionPane.ERROR_MESSAGE
            );

            log("HATA: Günlük dosyası kaydedilemedi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * İzleyici penceresini göster
     */
    public void show() {
        if (!isVisible) {
            logFrame.setVisible(true);
            isVisible = true;
            log("İzleyici penceresi açıldı");
        } else {
            logFrame.toFront();
        }
    }

    /**
     * İzleyici penceresini gizle
     */
    public void hide() {
        if (isVisible) {
            logFrame.setVisible(false);
            isVisible = false;
            log("İzleyici penceresi kapatıldı");
        }
    }

    /**
     * İzleyici penceresinin görünürlük durumunu döndür
     */
    public boolean isShowing() {
        return isVisible;
    }

    /**
     * İzleyici penceresinin görünürlüğünü değiştir
     */
    public void toggleVisibility() {
        if (isVisible) {
            hide();
        } else {
            show();
        }
    }
}
