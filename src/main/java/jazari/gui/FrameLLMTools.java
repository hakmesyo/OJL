/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jazari.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main frame for LLM-related tools including source code collector with
 * customizable file extensions.
 *
 * @author cezerilab
 */
public class FrameLLMTools extends javax.swing.JFrame {

    // Default file extensions - expanded list
    private final String[] DEFAULT_EXTENSIONS = {
        "java", "txt", "csv", "js", "py", "cs", "html", "css", "go", "cpp", "c",
        "php", "ts", "json", "xml", "md", "sql", "yaml/yml", "rb", "sh", "bat",
        "jsx", "tsx", "swift", "kt", "rs", "pde", "ino"
    };

    private static final Set<String> EXCLUDED_DIRS = new HashSet<>(Arrays.asList(
            "build", "target", ".git", ".idea", ".vscode", "nbproject", "dist"
    ));

    // Map to store checkboxes
    private final Map<String, JCheckBox> extensionCheckboxes = new HashMap<>();
    private FrameCircularProgressBar progressFrame;
    private SwingWorker<Void, String> activeWorker;

    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FrameLLMTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates new form FrameMainLLM
     */
    public FrameLLMTools() {
        initComponents();
        logArea.setDoubleBuffered(true);
        initializeExtensionCheckboxes();
    }

    /**
     * Initialize checkboxes for file extensions
     */
    private void initializeExtensionCheckboxes() {
        // Set layout for the panel - using 4 columns to fit all checkboxes better
        jPanel3.setLayout(new GridLayout(0, 4, 5, 5));

        // Create checkboxes for each extension
        for (String ext : DEFAULT_EXTENSIONS) {
            JCheckBox checkbox = new JCheckBox("*." + ext);
            checkbox.setSelected("java".equals(ext)); // Select Java by default
            extensionCheckboxes.put(ext, checkbox);
            jPanel3.add(checkbox);
        }
    }

    /**
     * Enable or disable UI components
     *
     * @param enabled true to enable components, false to disable
     */
    private void setComponentsEnabled(boolean enabled) {
        browseProjectButton.setEnabled(enabled);
        browseOutputButton.setEnabled(enabled);
        startButton.setEnabled(enabled);
        projectPathField.setEnabled(enabled);
        outputFileField.setEnabled(enabled);
        customExtensionsField.setEnabled(enabled);

        // Enable/disable all extension checkboxes
        for (JCheckBox checkbox : extensionCheckboxes.values()) {
            checkbox.setEnabled(enabled);
        }
    }

    /**
     * Get all selected file extensions
     *
     * @return List of selected file extensions
     */
    private List<String> getSelectedExtensions() {
        List<String> extensions = new ArrayList<>();

        // Add extensions from checkboxes
        for (Map.Entry<String, JCheckBox> entry : extensionCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                String key = entry.getKey();

                // Special handling for yaml/yml
                if (key.equals("yaml/yml")) {
                    extensions.add("yaml");
                    extensions.add("yml");
                } else {
                    extensions.add(key);
                }
            }
        }

        // Add custom extensions if any
        String customExtText = customExtensionsField.getText().trim();
        if (!customExtText.isEmpty()) {
            Arrays.stream(customExtText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(ext -> {
                        // Remove leading dot if present
                        if (ext.startsWith(".")) {
                            ext = ext.substring(1);
                        }
                        // Don't add duplicates
                        if (!extensions.contains(ext)) {
                            extensions.add(ext);
                        }
                    });
        }

        return extensions;
    }

    /**
     * Log message to the UI
     *
     * @param message Message to display
     */
    private void logToUI(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto scroll
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Find all files with selected extensions in the given directory and its
     * subdirectories
     *
     * @param directory Root directory to search from
     * @param extensions List of file extensions to include
     * @return List of matching file paths
     * @throws IOException If directory cannot be read
     */
    private List<Path> findSourceFiles(String directory, List<String> extensions) throws IOException {
        Path startPath = Paths.get(directory);

        if (!Files.exists(startPath)) {
            throw new IOException("Belirtilen dizin bulunamadı: " + directory);
        }

        try (Stream<Path> pathStream = Files.walk(startPath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return extensions.stream().anyMatch(ext -> fileName.endsWith("." + ext.toLowerCase()));
                    })
                    // ESKİ METOT YERİNE YENİSİNİ KULLANIYORUZ
                    .filter(path -> !isPathExcluded(path))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Bir dosya yolunun, işleme dahil edilmemesi gereken bir klasörde olup
     * olmadığını kontrol eder (örn: build, target, .git).
     *
     * @param path Kontrol edilecek dosya yolu.
     * @return Yol dışlanacak bir klasördeyse true, aksi halde false.
     */
    private boolean isPathExcluded(Path path) {
        // Path'i String'e çevirirken platforma uygun ayırıcıyı kullanmasını sağlıyoruz.
        String pathStr = path.toString();
        return pathStr.contains(File.separator + "build" + File.separator)
                || pathStr.contains(File.separator + "target" + File.separator)
                || pathStr.contains(File.separator + ".git" + File.separator);
    }

    /**
     * Tek bir kaynak dosyasının başlığını ve içeriğini verilen BufferedWriter'a
     * yazar.
     *
     * @param sourceFile Yazılacak dosyanın yolu.
     * @param writer Yazma işleminin yapılacağı BufferedWriter nesnesi.
     * @param projectRoot Projenin kök dizini (göreli yol hesabı için).
     * @throws IOException Dosya okuma/yazma hatası olursa.
     */
    private void appendFileToWriter(Path sourceFile, BufferedWriter writer, Path projectRoot) throws IOException {
        String relativePath = projectRoot.relativize(sourceFile).toString();

        // Dosya başlığını yaz
        writer.write("============================================================\n");
        writer.write("FILE: " + relativePath + "\n");
        writer.write("============================================================\n");

        // Kaynak dosyayı satır satır oku ve doğrudan çıktı dosyasına yaz
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile.toFile()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            String errorMsg = "Uyarı: " + relativePath + " okunurken hata oluştu, atlanıyor: " + e.getMessage();
            writer.write(errorMsg + "\n");
            logToUI(errorMsg); // Hataları log'a da yazalım
        }

        writer.newLine(); // Dosyalar arasına boşluk ekle
    }
//    private void combineSourceFiles(List<Path> sourceFiles, String outputFilePath) throws IOException {
//        // Create a StringBuilder to store content for both file and UI
//        StringBuilder contentBuilder = new StringBuilder();
//
//        // Generate the directory tree first
//        logToUI("Proje dizin yapısı oluşturuluyor...");
//        Path projectRoot = Paths.get(projectPathField.getText());
//        String directoryTree = generateDirectoryTree(projectRoot);
//
//        // ESKİ SATIR:
//        // try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
//        // YENİ SATIR: FileWriter yerine OutputStreamWriter kullanarak UTF-8 kodlamasını zorunlu kılıyoruz.
//        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8))) {
//            // Write the project structure tree to the file
//            String treeHeader = "==================== PROJECT STRUCTURE ====================";
//            writer.write(treeHeader);
//            writer.newLine();
//            writer.write(directoryTree);
//            writer.newLine();
//
//            // Also add it to the content builder for the UI log
//            contentBuilder.append(treeHeader).append("\n");
//            contentBuilder.append(directoryTree).append("\n");
//
//            // Now, process and combine the source files
//            int count = 0;
//            for (Path sourceFile : sourceFiles) {
//                count++;
//                if (count % 10 == 0) {
//                    logToUI("İşleniyor: " + count + "/" + sourceFiles.size() + " dosya");
//                }
//
//                String fileHeader = "============================================================";
//                // Use relative path for cleaner output
//                String relativePath = projectRoot.relativize(sourceFile).toString();
//                String filePath = "FILE: " + relativePath;
//                String fileFooter = "============================================================";
//
//                writer.write(fileHeader);
//                writer.newLine();
//                writer.write(filePath);
//                writer.newLine();
//                writer.write(fileFooter);
//                writer.newLine();
//
//                contentBuilder.append(fileHeader).append("\n");
//                contentBuilder.append(filePath).append("\n");
//                contentBuilder.append(fileFooter).append("\n");
//
//                // Read and write file content
//                // ESKİ SATIR:
//                // try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile.toFile()))) {
//                // YENİ SATIR: Dosyayı okurken de UTF-8 kullandığımızdan emin olalım (en iyi pratik)
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile.toFile()), StandardCharsets.UTF_8))) {
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        writer.write(line);
//                        writer.newLine();
//                        contentBuilder.append(line).append("\n");
//                    }
//                } catch (IOException e) {
//                    // Eğer bir dosya UTF-8 olarak okunamıyorsa loglayıp devam edelim.
//                    String errorMsg = "Uyarı: " + relativePath + " dosyası okunurken bir sorun oluştu (belki farklı bir encoding?), dosya atlanıyor: " + e.getMessage();
//                    writer.write(errorMsg);
//                    writer.newLine();
//                    contentBuilder.append(errorMsg).append("\n");
//                }
//
//                // Add space between files
//                writer.newLine();
//                writer.newLine();
//                contentBuilder.append("\n\n");
//            }
//        }
//
//        // Display content in the logArea after all processing is complete
//        logToUI("\n--- OLUŞTURULAN DOSYA İÇERİĞİ ---\n");
//        logToUI(contentBuilder.toString());
//        logToUI("\n--- DOSYA İÇERİĞİ SONU ---\n");
//    }

    /**
     * Generates a string representation of the directory tree for the given
     * path.
     *
     * @param rootPath The starting path of the project.
     * @return A string containing the formatted directory tree.
     */
    private String generateDirectoryTree(Path rootPath) {
        if (!Files.isDirectory(rootPath)) {
            return "Error: Provided path is not a directory.";
        }
        StringBuilder treeBuilder = new StringBuilder();
        treeBuilder.append(rootPath.getFileName().toString()).append("\n");
        generateTreeRecursive(rootPath, "", treeBuilder);
        return treeBuilder.toString();
    }

    /**
     * Dizin ağacını özyineli olarak oluşturur. EXCLUDED_DIRS listesindeki
     * klasörleri ve dosyaları ağaca dahil etmez.
     *
     * @param directory İşlenecek mevcut dizin.
     * @param prefix Ağaç görünümü için girinti öneki.
     * @param treeBuilder Ağacın oluşturulduğu StringBuilder nesnesi.
     */
    private void generateTreeRecursive(Path directory, String prefix, StringBuilder treeBuilder) {
        try (Stream<Path> stream = Files.list(directory)) {
            List<Path> paths = stream
                    // DEĞİŞİKLİK BURADA:
                    // Bir dosya veya klasörün adının dışlama listemizde olup olmadığını kontrol ediyoruz.
                    .filter(p -> !EXCLUDED_DIRS.contains(p.getFileName().toString()))
                    .sorted(Comparator.comparing((Path p) -> !Files.isDirectory(p)) // Önce klasörler
                            .thenComparing(Path::getFileName)).collect(Collectors.toList());

            for (int i = 0; i < paths.size(); i++) {
                Path path = paths.get(i);
                boolean isLast = (i == paths.size() - 1);

                treeBuilder.append(prefix);
                treeBuilder.append(isLast ? "└── " : "├── ");
                treeBuilder.append(path.getFileName().toString());
                treeBuilder.append("\n");

                if (Files.isDirectory(path)) {
                    generateTreeRecursive(path, prefix + (isLast ? "    " : "│   "), treeBuilder);
                }
            }
        } catch (IOException e) {
            treeBuilder.append(prefix).append("└── [Dizin okunurken hata: ").append(e.getMessage()).append("]\n");
        }
    }

    private List<String> extractSignaturesFromFile(Path javaFile) {
        List<String> signatures = new ArrayList<>();
        // Metotları, enum'ları ve record'ları yakalamak için Regex desenleri
        // Bu desenler en yaygın durumları yakalar.
        String methodPattern = "^\\s*(public|private|protected|static|final|synchronized|abstract|default|\\s)+\\s*[\\w\\<\\>\\[\\]\\?,\\s]+\\s+([a-zA-Z_][\\w]*)\\s*\\([^\\)]*\\)\\s*(\\{|throws|;)";
        String enumPattern = "^\\s*(public|private|protected)?\\s*enum\\s+([a-zA-Z_][\\w]*).*";
        String recordPattern = "^\\s*(public|private|protected)?\\s*record\\s+([a-zA-Z_][\\w]*)\\s*\\([^\\)]*\\).*";

        try {
            List<String> lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmedLine = line.trim();

                // Satırın yorum satırı olup olmadığını kontrol et (basit kontrol)
                if (trimmedLine.startsWith("//") || trimmedLine.startsWith("*") || trimmedLine.startsWith("/*")) {
                    continue;
                }

                Matcher methodMatcher = Pattern.compile(methodPattern).matcher(trimmedLine);
                Matcher enumMatcher = Pattern.compile(enumPattern).matcher(trimmedLine);
                Matcher recordMatcher = Pattern.compile(recordPattern).matcher(trimmedLine);

                if (recordMatcher.matches()) {
                    // 💿 ikonu ile record'ları belirtelim
                    //signatures.add("💿 " + trimmedLine.replaceAll("\\s*\\{\\s*$", ""));
                    signatures.add(" " + trimmedLine.replaceAll("\\s*\\{\\s*$", ""));
                } else if (enumMatcher.matches()) {
                    // 🔶 ikonu ile enum'ları belirtelim
                    //signatures.add("🔶 " + trimmedLine.replaceAll("\\s*\\{\\s*$", ""));
                    signatures.add(" " + trimmedLine.replaceAll("\\s*\\{\\s*$", ""));
                } else if (methodMatcher.matches()) {
                    // 🌿 ikonu ile metotları belirtelim
                    //signatures.add("🌿 " + trimmedLine.replaceAll("\\s*\\{\\s*$", ""));
                    signatures.add(" " + trimmedLine.replaceAll("\\s*\\{\\s*$", ""));
                }
            }
        } catch (IOException e) {
            signatures.add("[Dosya okunurken hata: " + e.getMessage() + "]");
        }
        return signatures;
    }

    private String generateDirectoryTree(Path rootPath, boolean showSignatures) {
        if (!Files.isDirectory(rootPath)) {
            return "Hata: Belirtilen yol bir dizin değil.";
        }
        StringBuilder treeBuilder = new StringBuilder();
        treeBuilder.append(rootPath.getFileName().toString()).append("\n");
        generateTreeRecursive(rootPath, "", treeBuilder, showSignatures);
        return treeBuilder.toString();
    }

    /**
     * Dizin ağacını özyineli olarak oluşturur.
     *
     * @param directory İşlenecek mevcut dizin.
     * @param prefix Ağaç görünümü için girinti öneki.
     * @param treeBuilder Ağacın oluşturulduğu StringBuilder nesnesi.
     * @param showSignatures Eğer true ise, .java dosyalarının içindeki
     * metotları da gösterir.
     */
    private void generateTreeRecursive(Path directory, String prefix, StringBuilder treeBuilder, boolean showSignatures) {
        try (Stream<Path> stream = Files.list(directory)) {
            List<Path> paths = stream
                    .filter(p -> !EXCLUDED_DIRS.contains(p.getFileName().toString()))
                    .sorted(Comparator.comparing((Path p) -> !Files.isDirectory(p))
                            .thenComparing(Path::getFileName)).collect(Collectors.toList());

            for (int i = 0; i < paths.size(); i++) {
                Path path = paths.get(i);
                boolean isLast = (i == paths.size() - 1);
                String currentPrefix = prefix + (isLast ? "└── " : "├── ");
                String childPrefix = prefix + (isLast ? "    " : "│   ");

                treeBuilder.append(currentPrefix);
                treeBuilder.append(path.getFileName().toString());
                treeBuilder.append("\n");

                if (Files.isDirectory(path)) {
                    generateTreeRecursive(path, childPrefix, treeBuilder, showSignatures);
                } // YENİ KISIM: Eğer bu bir .java dosyasıysa ve checkbox işaretliyse...
                else if (showSignatures && path.toString().endsWith(".java")) {
                    List<String> signatures = extractSignaturesFromFile(path);
                    for (int j = 0; j < signatures.size(); j++) {
                        boolean isLastSignature = (j == signatures.size() - 1);
                        treeBuilder.append(childPrefix);
                        treeBuilder.append(isLastSignature ? "└── " : "├── ");
                        treeBuilder.append(signatures.get(j));
                        treeBuilder.append("\n");
                    }
                }
            }
        } catch (IOException e) {
            treeBuilder.append(prefix).append("└── [Dizin okunurken hata: ").append(e.getMessage()).append("]\n");
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        projectPathField = new javax.swing.JTextField();
        browseProjectButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        outputFileField = new javax.swing.JTextField();
        browseOutputButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        logArea = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        customExtensionsField = new javax.swing.JTextField();
        reOpenFileButton = new javax.swing.JButton();
        chkMethod = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("LLM Tools");

        jLabel1.setText("Project Directory:");

        browseProjectButton.setText("Browse");
        browseProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Output File:");

        browseOutputButton.setText("Browse");
        browseOutputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseOutputButtonActionPerformed(evt);
            }
        });

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        logArea.setEditable(false);
        logArea.setColumns(20);
        logArea.setRows(5);
        jScrollPane1.setViewportView(logArea);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("File Extensions"));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jLabel3.setText("Select file types to include:");

        jLabel4.setText("Additional Extensions (comma separated):");

        customExtensionsField.setToolTipText("Add custom extensions separated by commas (e.g. json,md,xml)");

        reOpenFileButton.setText("Re-Open File");
        reOpenFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reOpenFileButtonActionPerformed(evt);
            }
        });

        chkMethod.setText("Show Methods");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(projectPathField)
                            .addComponent(outputFileField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(browseOutputButton)
                            .addComponent(browseProjectButton)))
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(reOpenFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chkMethod)))
                        .addGap(0, 423, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(customExtensionsField)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(projectPathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseProjectButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(outputFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseOutputButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(customExtensionsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(reOpenFileButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(chkMethod)
                    .addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Source Code Collector", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 789, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 611, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Misc", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void browseProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Project Directory");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            projectPathField.setText(selectedFile.getAbsolutePath());

            // *** DEĞİŞİKLİK BURADA ***
            // Proje dizini her seçildiğinde çıktı dosyasını otomatik olarak ayarla.
            // 'if' kontrolü kaldırıldı, böylece her seferinde güncelleme yapılır.
            String defaultOutput = selectedFile.getAbsolutePath() + File.separator
                    + "collected_source_code.txt";
            outputFileField.setText(defaultOutput);
        }
    }//GEN-LAST:event_browseProjectButtonActionPerformed

    private void browseOutputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseOutputButtonActionPerformed
        // Start from current working directory
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select Output File");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".txt")) {
                path += ".txt";
            }
            outputFileField.setText(path);
        }
    }//GEN-LAST:event_browseOutputButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed

        // 1. Girdi Alanlarını Doğrula
        String projectPath = projectPathField.getText().trim();
        String outputFile = outputFileField.getText().trim();

        if (projectPath.isEmpty() || outputFile.isEmpty() || getSelectedExtensions().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen tüm alanları doldurun ve en az bir uzantı seçin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Arayüzü Hazırla: Bileşenleri devre dışı bırak, log'u temizle, progress bar'ı göster
        setComponentsEnabled(false);
        reOpenFileButton.setEnabled(false);
        logArea.setText("");

        progressFrame = new FrameCircularProgressBar();
        progressFrame.setLocationRelativeTo(this);
        progressFrame.setVisible(true);

        // 3. Arka Plan İşlemini SwingWorker ile Tanımla
        activeWorker = new SwingWorker<Void, String>() {

            /**
             * Arka planda çalışacak olan uzun süreli işlem. UI'ı dondurmaz.
             */
            @Override
            protected Void doInBackground() throws Exception {
                // 1. Dosyaları bul
                publish("Dosyalar taranıyor...");
                List<Path> sourceFiles = findSourceFiles(projectPath, getSelectedExtensions());

                if (sourceFiles.isEmpty()) {
                    publish("Eşleşen dosya bulunamadı!");
                    return null;
                }
                publish("Toplam " + sourceFiles.size() + " adet dosya bulundu. Birleştiriliyor...");

                // 2. Proje ağacını ve istatistikleri oluştur
                Path projectRoot = Paths.get(projectPathField.getText());

                // İstatistikleri hesapla
                String statistics = generateStatistics(sourceFiles, projectRoot);

                // Checkbox'ın durumuna göre dinamik olarak proje ağacını oluştur
                boolean shouldShowSignatures = chkMethod.isSelected();
                String directoryTree = generateDirectoryTree(projectRoot, shouldShowSignatures);

                // Oluşturulan bilgileri UI'da göstermek için publish et
                publish("\n" + statistics);
                publish("\n==================== PROJECT STRUCTURE ====================\n" + directoryTree);

                // 3. Çıktı dosyasını oluşturmaya başla
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

                    // Önce istatistikleri ve proje ağacını dosyaya yaz
                    writer.write(statistics);
                    writer.newLine();
                    writer.write("==================== PROJECT STRUCTURE ====================\n");
                    writer.write(directoryTree);
                    writer.newLine();

                    // 4. Tüm dosyaları döngüye alıp içeriğini dosyaya ekle
                    int totalFiles = sourceFiles.size();
                    for (int i = 0; i < totalFiles; i++) {
                        Path sourceFile = sourceFiles.get(i);
                        appendFileToWriter(sourceFile, writer, projectRoot);

                        // 5. İlerlemeyi hesapla ve UI'ı güncellemek için setProgress'i çağır
                        int progress = (int) (((i + 1.0) / totalFiles) * 100);
                        setProgress(progress);
                    }
                }
                return null;
            }

            /**
             * doInBackground içinde publish() ile gönderilen mesajları alır ve
             * UI'da işler.
             */
            @Override
            protected void process(List<String> chunks) {
                for (String message : chunks) {
                    logToUI(message);
                }
            }

            /**
             * doInBackground metodu tamamlandığında (başarılı veya hatalı)
             * çalışır.
             */
            @Override
            protected void done() {
                try {
                    get(); // Arka planda bir hata oluştuysa burada yakalanır.
                    publish("İşlem başarıyla tamamlandı!");
                    showCompletionDialog(outputFile);
                    reOpenFileButton.setEnabled(true); // İşlem başarılıysa butonu aktif et
                } catch (Exception e) {
                    publish("HATA: İşlem sırasında bir sorun oluştu: " + e.getMessage());
                    JOptionPane.showMessageDialog(FrameLLMTools.this,
                            "İşlem sırasında bir hata oluştu:\n" + e.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Her durumda (başarılı veya hatalı) arayüzü tekrar kullanılabilir hale getir.
                    setComponentsEnabled(true);
                    if (progressFrame != null) {
                        progressFrame.dispose();
                    }
                    activeWorker = null;
                }
            }
        };

        // 4. Worker'ın İlerleme Güncellemelerini Dinle ve Progress Bar'a Yansıt
        activeWorker.addPropertyChangeListener(pce -> {
            if ("progress".equals(pce.getPropertyName())) {
                if (progressFrame != null) {
                    progressFrame.setProgress((Integer) pce.getNewValue());
                }
            }
        });

        // 5. Arka Plan İşlemini Başlat
        activeWorker.execute();


    }//GEN-LAST:event_startButtonActionPerformed

    /**
     * Verilen dosya listesi üzerinde istatistiksel analiz yapar. Dosya
     * sayısını, toplam satır sayısını ve basit desen eşleşmesiyle
     * metot/fonksiyon sayısını hesaplar.
     *
     * @param sourceFiles Analiz edilecek dosyaların listesi.
     * @param projectRoot Projenin kök dizini (pom.xml'i bulmak için).
     * @return Biçimlendirilmiş istatistik metnini içeren bir String.
     */
    private String generateStatistics(List<Path> sourceFiles, Path projectRoot) {
        long totalLines = 0;
        long methodCount = 0;
        boolean pomExists = Files.exists(projectRoot.resolve("pom.xml"));

        // Basit bir metot/fonksiyon tanımı deseni. Java, C#, JS, Python, Go için çalışır.
        // public void myMethod(...), function myFunc(...), def my_func(...), func myFunc(...)
        String methodPattern = "(public|private|protected|static|func|def|function)\\s+[\\w<>]+\\s+[\\w<>]+\\s*\\(.*\\)\\s*(\\{|:)?";

        for (Path file : sourceFiles) {
            try {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                totalLines += lines.size();

                for (String line : lines) {
                    // Trimlenmiş satır desene uyuyorsa metot sayısını artır
                    if (line.trim().matches(methodPattern)) {
                        methodCount++;
                    }
                }
            } catch (IOException e) {
                // Bir dosya okunamıyorsa görmezden gel, log'a zaten yazılıyor.
            }
        }

        // Sayıları binlik ayraçlarla biçimlendir
        java.text.NumberFormat numberFormat = java.text.NumberFormat.getInstance();
        String formattedFileCount = numberFormat.format(sourceFiles.size());
        String formattedLineCount = numberFormat.format(totalLines);
        String formattedMethodCount = numberFormat.format(methodCount);

        StringBuilder stats = new StringBuilder();
        stats.append("==================== STATISTICS ====================\n");
        stats.append(String.format("%-25s %s\n", "Project Type:", pomExists ? "Maven (pom.xml found)" : "Standard"));
        stats.append(String.format("%-25s %s\n", "Number of Files Scanned:", formattedFileCount));
        stats.append(String.format("%-25s %s\n", "Total Lines of Code:", formattedLineCount));
        stats.append(String.format("%-25s %s (estimated)\n", "Number of Methods/Functions:", formattedMethodCount));
        stats.append("====================================================\n");

        return stats.toString();
    }

    private void reOpenFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reOpenFileButtonActionPerformed
        String outputFilePath = outputFileField.getText().trim();
        if (outputFilePath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Açılacak bir dosya yolu belirtilmemiş.",
                    "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File fileToOpen = new File(outputFilePath);
        if (!fileToOpen.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Dosya bulunamadı:\n" + outputFilePath,
                    "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Desktop.getDesktop().open(fileToOpen);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Dosya açılamadı: " + e.getMessage(),
                    "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_reOpenFileButtonActionPerformed

    /**
     * İşlem tamamlandığında kullanıcıya dosyayı açma seçeneği sunan yardımcı
     * metot.
     *
     * @param outputFile Açılacak dosyanın yolu.
     */
    private void showCompletionDialog(String outputFile) {
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrameLLMTools frm = new FrameLLMTools();
                frm.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseOutputButton;
    private javax.swing.JButton browseProjectButton;
    private javax.swing.JCheckBox chkMethod;
    private javax.swing.JTextField customExtensionsField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea logArea;
    private javax.swing.JTextField outputFileField;
    private javax.swing.JTextField projectPathField;
    private javax.swing.JButton reOpenFileButton;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
}
