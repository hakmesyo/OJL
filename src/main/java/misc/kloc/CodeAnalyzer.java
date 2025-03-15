/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package misc.kloc;

/**
 *
 * @author cezerilab
 */
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import jazari.gui.FlatLaf;

public class CodeAnalyzer extends JFrame {
    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FlatLaf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JTextField pathField;
    private JTextArea resultArea;
    private JButton analyzeButton;
    private JButton browseButton;
    private JProgressBar progressBar;
    private JTree fileTree;
    private JTable statsTable;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private File selectedDirectory;

    // Analiz sonuçları için veri yapıları
    private Map<String, Integer> extensionCount = new HashMap<>();
    private Map<String, Integer> extensionLines = new HashMap<>();
    private Map<String, Integer> packageCount = new HashMap<>();
    private List<FileStats> fileStatsList = new ArrayList<>();
    private int totalFiles = 0;
    private int totalLines = 0;
    private int totalClasses = 0;
    private int totalInterfaces = 0;
    private int totalEnums = 0;
    private int totalMethods = 0;
    // Kullanıcı tercihleri için
    private static final String LAST_USED_FOLDER = "last_used_folder";
    private Preferences prefs;

    public CodeAnalyzer() {
        setTitle("OJL Kod Analizi Uygulaması");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Kullanıcı tercihlerini yükle
        prefs = Preferences.userNodeForPackage(CodeAnalyzer.class);

        initUI();
    }

    private void initUI() {
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Üst panel - Path seçimi
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));

        JLabel pathLabel = new JLabel("Proje Yolu:");
        pathField = new JTextField();
        browseButton = new JButton("Gözat");
        analyzeButton = new JButton("Analiz Et");
        analyzeButton.setEnabled(false);

        topPanel.add(pathLabel, BorderLayout.WEST);
        topPanel.add(pathField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.add(browseButton);
        buttonPanel.add(analyzeButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Hazır");

        // Ana içerik paneli - Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.3);

        // Sol panel - Dosya ağacı
        rootNode = new DefaultMutableTreeNode("Proje");
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel);
        fileTree.setCellRenderer(new ProjectTreeCellRenderer());
        JScrollPane treeScrollPane = new JScrollPane(fileTree);

        // Sağ panel - Analiz sonuçları ve tablo
        JTabbedPane tabbedPane = new JTabbedPane();

        // Sonuç alanı
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane resultScrollPane = new JScrollPane(resultArea);

        // Tablo modeli
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("Dosya Adı");
        tableModel.addColumn("Uzantı");
        tableModel.addColumn("Satır Sayısı");
        tableModel.addColumn("Sınıf Sayısı");
        tableModel.addColumn("Metot Sayısı");
        tableModel.addColumn("Paket");

        statsTable = new JTable(tableModel);
        statsTable.setAutoCreateRowSorter(true);
        JScrollPane tableScrollPane = new JScrollPane(statsTable);

        tabbedPane.addTab("Özet", resultScrollPane);
        tabbedPane.addTab("Dosya Detayları", tableScrollPane);

        // Panelleri ana panel'e ekle
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        splitPane.setLeftComponent(treeScrollPane);
        splitPane.setRightComponent(tabbedPane);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel);

        // Dinleyicileri tanımla
        browseButton.addActionListener(e -> browsePath());
        analyzeButton.addActionListener(e -> analyzeProject());
        pathField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateAnalyzeButton();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateAnalyzeButton();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateAnalyzeButton();
            }
        });
    }

    private void updateAnalyzeButton() {
        String path = pathField.getText().trim();
        analyzeButton.setEnabled(!path.isEmpty() && new File(path).exists() && new File(path).isDirectory());
    }

    private void browsePath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Proje Klasörünü Seçin");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Son kullanılan klasörü yükle
        String lastPath = prefs.get(LAST_USED_FOLDER, null);
        if (lastPath != null) {
            File lastDir = new File(lastPath);
            if (lastDir.exists()) {
                fileChooser.setCurrentDirectory(lastDir);
            }
        } else {
            // Uygulama klasöründen başla
            try {
                File currentDir = new File(CodeAnalyzer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                fileChooser.setCurrentDirectory(currentDir);
            } catch (Exception e) {
                // Bir hata olursa varsayılan klasörü kullan
            }
        }

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = fileChooser.getSelectedFile();
            pathField.setText(selectedDirectory.getAbsolutePath());

            // Seçilen klasörü kaydet
            prefs.put(LAST_USED_FOLDER, fileChooser.getCurrentDirectory().getAbsolutePath());
        }
    }

    private void analyzeProject() {
        // Eski analiz sonuçlarını temizle
        clearResults();

        // Analiz işlemini arka planda çalıştır
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                analyzeDirectory(selectedDirectory);
                return null;
            }

            @Override
            protected void done() {
                updateUI();
                progressBar.setValue(100);
                progressBar.setString("Analiz tamamlandı");
            }
        };

        progressBar.setValue(0);
        progressBar.setString("Analiz ediliyor...");
        worker.execute();
    }

    private void clearResults() {
        extensionCount.clear();
        extensionLines.clear();
        packageCount.clear();
        fileStatsList.clear();
        totalFiles = 0;
        totalLines = 0;
        totalClasses = 0;
        totalInterfaces = 0;
        totalEnums = 0;
        totalMethods = 0;

        rootNode.removeAllChildren();
        treeModel.reload();

        DefaultTableModel model = (DefaultTableModel) statsTable.getModel();
        model.setRowCount(0);

        resultArea.setText("");
    }

    private void analyzeDirectory(File directory) {
        DefaultMutableTreeNode dirNode = addToTree(directory);

        File[] files = directory.listFiles();
        if (files != null) {
            // Önce analiz edilecek dosya sayısını hesapla
            int totalFilesToProcess = countJavaFiles(directory);
            AtomicInteger processedFiles = new AtomicInteger(0);

            // Her dosyayı işle
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!isIgnoredDirectory(file)) {
                        analyzeDirectory(file);
                    }
                } else if (file.getName().endsWith(".java")) {
                    analyzeJavaFile(file, processedFiles, totalFilesToProcess);
                    // Tree'ye ekleme işlemini kaldırıyoruz, lazily loading ile yapılacak
                    // addToTree(file, dirNode);
                }
            }
        }
    }

    private boolean isIgnoredDirectory(File dir) {
        String name = dir.getName();
        return name.equals("target") || name.equals("build")
                || name.equals(".git") || name.equals("nbproject")
                || name.equals("dist");
    }

    private int countJavaFiles(File dir) {
        AtomicInteger count = new AtomicInteger();
        try {
            Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".java")) {
                        count.incrementAndGet();
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (isIgnoredDirectory(dir.toFile())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count.get();
    }

    private void analyzeJavaFile(File file, AtomicInteger processedFiles, final int totalFilesToProcess) {
        try {
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            int lineCount = lines.size();
            int classCount = 0;
            int interfaceCount = 0;
            int enumCount = 0;
            int methodCount = 0;
            String packageName = "";

            boolean inComment = false;

            for (String line : lines) {
                line = line.trim();

                // Yorum satırlarını kontrol et
                if (line.startsWith("/*")) {
                    inComment = true;
                }

                if (inComment) {
                    if (line.endsWith("*/")) {
                        inComment = false;
                    }
                    continue;
                }

                if (line.startsWith("//") || line.isEmpty()) {
                    continue;
                }

                // Paket adını bul
                if (line.startsWith("package ")) {
                    packageName = line.substring(8, line.indexOf(';')).trim();
                    packageCount.put(packageName, packageCount.getOrDefault(packageName, 0) + 1);
                }

                // Sınıf, arayüz ve enum sayısını bul
                if (line.contains("class ") && !line.contains("private class ")) {
                    classCount++;
                    totalClasses++;
                }
                if (line.contains("interface ")) {
                    interfaceCount++;
                    totalInterfaces++;
                }
                if (line.contains("enum ")) {
                    enumCount++;
                    totalEnums++;
                }

                // Metot sayısını bul (basit bir yaklaşım)
                if ((line.contains("public ") || line.contains("private ")
                        || line.contains("protected ") || line.startsWith("void ")
                        || line.startsWith("int ") || line.startsWith("String "))
                        && line.contains("(") && line.contains(")") && !line.contains("new ")) {
                    methodCount++;
                    totalMethods++;
                }
            }

            // Dosya istatistiklerini kaydet
            FileStats stats = new FileStats(
                    file.getName(),
                    "java",
                    lineCount,
                    classCount,
                    methodCount,
                    packageName,
                    file.getAbsolutePath()
            );
            fileStatsList.add(stats);

            // İstatistikleri güncelle
            totalFiles++;
            totalLines += lineCount;
            extensionCount.put("java", extensionCount.getOrDefault("java", 0) + 1);
            extensionLines.put("java", extensionLines.getOrDefault("java", 0) + lineCount);

            // İlerleme çubuğunu güncelle
            final int processed = processedFiles.incrementAndGet();
            SwingUtilities.invokeLater(() -> {
                int progress = (int) ((double) processed / totalFilesToProcess * 100);
                progressBar.setValue(progress);
                progressBar.setString("Analiz ediliyor... (" + processed + "/" + totalFilesToProcess + ")");
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DefaultMutableTreeNode addToTree(File file) {
        return addToTree(file, rootNode);
    }

    private DefaultMutableTreeNode addToTree(File file, DefaultMutableTreeNode parentNode) {
        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file);

        if (file.isDirectory()) {
            // Sadece düğümü ekle, alt klasörleri daha sonra genişletildiğinde ekleyeceğiz
            SwingUtilities.invokeLater(() -> {
                treeModel.insertNodeInto(fileNode, parentNode, parentNode.getChildCount());
            });

            // Yer tutucu düğüm ekle (lazily loading için)
            DefaultMutableTreeNode placeholderNode = new DefaultMutableTreeNode("Yükleniyor...");
            fileNode.add(placeholderNode);
        } else {
            // Dosya düğümünü doğrudan ekle
            SwingUtilities.invokeLater(() -> {
                treeModel.insertNodeInto(fileNode, parentNode, parentNode.getChildCount());
            });
        }

        return fileNode;
    }

// Sınıf içinde yeni bir map ekliyoruz
    private Map<String, DefaultMutableTreeNode> packageNodes = new HashMap<>();

// Tree oluşturma fonksiyonunu değiştiriyoruz
    private void buildPackageTree() {
        rootNode.removeAllChildren();
        packageNodes.clear();

        // Proje kök düğümünü oluştur
        DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(selectedDirectory.getName());
        rootNode.add(projectNode);

        // Source Packages düğümünü oluştur
        DefaultMutableTreeNode sourcePackagesNode = new DefaultMutableTreeNode("Source Packages");
        projectNode.add(sourcePackagesNode);

        // Paket yapısını oluştur
        for (FileStats stat : fileStatsList) {
            if (stat.packageName != null && !stat.packageName.isEmpty()) {
                addFileToPackageTree(sourcePackagesNode, stat);
            }
        }

        treeModel.reload();

        // Ağacı genişlet
        SwingUtilities.invokeLater(() -> {
            expandAllNodes(fileTree, new TreePath(rootNode), 3); // 3 seviye genişlet
        });
    }

// Dosyayı paket ağacına eklemek için yeni metot
    private void addFileToPackageTree(DefaultMutableTreeNode sourcePackagesNode, FileStats stat) {
        String packageName = stat.packageName;
        String[] packageParts = packageName.split("\\.");

        DefaultMutableTreeNode currentNode = sourcePackagesNode;
        StringBuilder currentPackage = new StringBuilder();

        // Paket hiyerarşisini oluştur
        for (int i = 0; i < packageParts.length; i++) {
            if (i > 0) {
                currentPackage.append(".");
            }
            currentPackage.append(packageParts[i]);
            String pkgKey = currentPackage.toString();

            if (!packageNodes.containsKey(pkgKey)) {
                DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode(packageParts[i]);
                currentNode.add(packageNode);
                packageNodes.put(pkgKey, packageNode);
                currentNode = packageNode;
            } else {
                currentNode = packageNodes.get(pkgKey);
            }
        }

        // Dosya düğümünü ekle
        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(new File(stat.filePath));
        currentNode.add(fileNode);
    }

// Belirli bir seviyeye kadar tüm düğümleri genişleten yardımcı metot
    private void expandAllNodes(JTree tree, TreePath path, int level) {
        if (level <= 0) {
            return;
        }

        tree.expandPath(path);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            TreePath childPath = path.pathByAddingChild(child);
            expandAllNodes(tree, childPath, level - 1);
        }
    }

// updateUI metodunu değiştiriyoruz
    private void updateUI() {
        // Paket ağacını oluştur
        buildPackageTree();

        // Sonuç alanını güncelle
        StringBuilder result = new StringBuilder();
        result.append("===== OJL Kod Analiz Raporu =====\n\n");
        result.append(String.format("Toplam Java Dosyası: %d\n", totalFiles));
        result.append(String.format("Toplam Kod Satırı: %d\n", totalLines));
        result.append(String.format("Toplam Sınıf Sayısı: %d\n", totalClasses));
        result.append(String.format("Toplam Arayüz Sayısı: %d\n", totalInterfaces));
        result.append(String.format("Toplam Enum Sayısı: %d\n", totalEnums));
        result.append(String.format("Toplam Metot Sayısı: %d\n\n", totalMethods));

        result.append("===== Paket Dağılımı =====\n");
        List<Map.Entry<String, Integer>> sortedPackages = new ArrayList<>(packageCount.entrySet());
        sortedPackages.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        for (Map.Entry<String, Integer> entry : sortedPackages) {
            result.append(String.format("%s: %d dosya\n", entry.getKey(), entry.getValue()));
        }

        resultArea.setText(result.toString());

        // Tabloyu güncelle
        DefaultTableModel model = (DefaultTableModel) statsTable.getModel();
        model.setRowCount(0);
        for (FileStats stats : fileStatsList) {
            model.addRow(new Object[]{
                stats.fileName,
                stats.fileExt,
                stats.lineCount,
                stats.classCount,
                stats.methodCount,
                stats.packageName
            });
        }
    }

    // Tree için özel renderer
    private class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof File) {
                File file = (File) userObject;
                setText(file.getName());

                if (file.isDirectory()) {
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                } else if (file.getName().endsWith(".java")) {
                    setIcon(UIManager.getIcon("FileView.fileIcon"));
                }
            }

            return this;
        }
    }

    // Dosya istatistikleri için yardımcı sınıf
    private static class FileStats {

        String fileName;
        String fileExt;
        int lineCount;
        int classCount;
        int methodCount;
        String packageName;
        String filePath;

        public FileStats(String fileName, String fileExt, int lineCount, int classCount,
                int methodCount, String packageName, String filePath) {
            this.fileName = fileName;
            this.fileExt = fileExt;
            this.lineCount = lineCount;
            this.classCount = classCount;
            this.methodCount = methodCount;
            this.packageName = packageName;
            this.filePath = filePath;
        }
    }

    public static void main(String[] args) {
//        try {
//            // Look and Feel ayarla
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        SwingUtilities.invokeLater(() -> {
            CodeAnalyzer analyzer = new CodeAnalyzer();
            analyzer.setVisible(true);
        });
    }
}
