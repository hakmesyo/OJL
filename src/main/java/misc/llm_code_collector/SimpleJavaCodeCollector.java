/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package misc.llm_code_collector;

/**
 *
 * @author cezerilab
 */
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jazari.gui.FlatLaf;

/**
 * Java kaynak kodlarını toplayıp tek bir dosyada birleştiren basit bir uygulama.
 */
public class SimpleJavaCodeCollector extends JFrame {
    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FlatLaf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JTextField projectPathField;
    private JTextField outputFileField;
    private JButton browseProjectButton;
    private JButton browseOutputButton;
    private JButton startButton;
    private JTextArea logArea;

    public SimpleJavaCodeCollector() {
        // Temel pencere ayarları
        setTitle("Java Kaynak Kodu Toplayıcı");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ana panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Giriş paneli
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Proje yolu alanı
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Proje Dizini:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        projectPathField = new JTextField(30);
        inputPanel.add(projectPathField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0;
        browseProjectButton = new JButton("Gözat");
        inputPanel.add(browseProjectButton, gbc);
        
        // Çıktı dosyası alanı
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Çıktı Dosyası:"), gbc);
        
        gbc.gridx = 1;
        outputFileField = new JTextField(30);
        inputPanel.add(outputFileField, gbc);
        
        gbc.gridx = 2;
        browseOutputButton = new JButton("Gözat");
        inputPanel.add(browseOutputButton, gbc);
        
        // Başlat butonu
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        startButton = new JButton("Başlat");
        inputPanel.add(startButton, gbc);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        
        // Log alanı
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        add(panel);
        
        // Olayları ekle
        browseProjectButton.addActionListener(e -> browseProjectDirectory());
        browseOutputButton.addActionListener(e -> browseOutputFile());
        startButton.addActionListener(e -> startCollection());
    }
    
    private void browseProjectDirectory() {
        // Geçerli çalışma dizininden başla
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Proje Dizinini Seçin");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            projectPathField.setText(selectedFile.getAbsolutePath());
            
            // Otomatik çıktı dosyası adı öner
            if (outputFileField.getText().isEmpty()) {
                String defaultOutput = selectedFile.getAbsolutePath() + File.separator + 
                                      "collected_java_code.txt";
                outputFileField.setText(defaultOutput);
            }
        }
    }
    
    private void browseOutputFile() {
        // Geçerli çalışma dizininden başla
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Çıktı Dosyasını Seçin");
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".txt")) {
                path += ".txt";
            }
            outputFileField.setText(path);
        }
    }
    
    private void startCollection() {
        String projectPath = projectPathField.getText().trim();
        String outputFile = outputFileField.getText().trim();
        
        if (projectPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen proje dizinini seçin!", 
                                         "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (outputFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen çıktı dosyasını belirtin!", 
                                         "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Arayüzü devre dışı bırak
        setComponentsEnabled(false);
        logArea.setText("");
        
        // Arkaplanda çalıştır
        new Thread(() -> {
            try {
                collectJavaCode(projectPath, outputFile);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("HATA: " + e.getMessage() + "\n");
                    JOptionPane.showMessageDialog(this, "İşlem sırasında bir hata oluştu: " + 
                                                e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                SwingUtilities.invokeLater(() -> setComponentsEnabled(true));
            }
        }).start();
    }
    
    private void setComponentsEnabled(boolean enabled) {
        browseProjectButton.setEnabled(enabled);
        browseOutputButton.setEnabled(enabled);
        startButton.setEnabled(enabled);
        projectPathField.setEnabled(enabled);
        outputFileField.setEnabled(enabled);
    }
    
    private void collectJavaCode(String projectPath, String outputFile) throws IOException {
        logToUI("Java dosyaları taranıyor: " + projectPath);
        
        // Java dosyalarını bul
        List<Path> javaFiles = findJavaFiles(projectPath);
        
        if (javaFiles.isEmpty()) {
            logToUI("Belirtilen dizinde hiç Java dosyası bulunamadı!");
            return;
        }
        
        logToUI("Toplam " + javaFiles.size() + " Java dosyası bulundu.");
        
        // Dosyaları birleştir
        logToUI("Dosyalar birleştiriliyor...");
        combineJavaFiles(javaFiles, outputFile);
        
        logToUI("İşlem tamamlandı! Çıktı dosyası: " + outputFile);
        
        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(this, 
                    "İşlem başarıyla tamamlandı!\nÇıktı dosyasını şimdi açmak ister misiniz?", 
                    "İşlem Tamamlandı", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                try {
                    Desktop.getDesktop().open(new File(outputFile));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Dosya açılamadı: " + e.getMessage(), 
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
    
    private void logToUI(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Otomatik kaydırma
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private List<Path> findJavaFiles(String directory) throws IOException {
        Path startPath = Paths.get(directory);
        
        if (!Files.exists(startPath)) {
            throw new IOException("Belirtilen dizin bulunamadı: " + directory);
        }
        
        try (Stream<Path> pathStream = Files.walk(startPath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".java"))
                    .filter(path -> !isInBuildOrTarget(path)) // build ve target klasörlerini hariç tut
                    .collect(Collectors.toList());
        }
    }
    
    private boolean isInBuildOrTarget(Path path) {
        String pathStr = path.toString();
        return pathStr.contains(File.separator + "build" + File.separator) || 
               pathStr.contains(File.separator + "target" + File.separator);
    }
    
    private void combineJavaFiles(List<Path> javaFiles, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            int count = 0;
            for (Path javaFile : javaFiles) {
                count++;
                if (count % 10 == 0) {
                    logToUI("İşleniyor: " + count + "/" + javaFiles.size() + " dosya");
                }
                
                // Dosya bilgisini ekle
                writer.write("============================================================");
                writer.newLine();
                writer.write("DOSYA: " + javaFile.toString());
                writer.newLine();
                writer.write("============================================================");
                writer.newLine();
                
                // Dosya içeriğini oku ve yaz
                try (BufferedReader reader = new BufferedReader(new FileReader(javaFile.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
                
                // Dosyalar arasında boşluk ekle
                writer.newLine();
                writer.newLine();
            }
        }
    }
    
    public static void main(String[] args) {
        // Uygulamayı başlat
        SwingUtilities.invokeLater(() -> {
            SimpleJavaCodeCollector app = new SimpleJavaCodeCollector();
            app.setVisible(true);
        });
    }
}