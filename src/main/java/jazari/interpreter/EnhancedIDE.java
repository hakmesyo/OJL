package jazari.interpreter;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.ui.rsyntaxtextarea.templates.*;
import jazari.gui.FrameMainLLM;
import org.fife.ui.rtextarea.RTextScrollPane;

public class EnhancedIDE extends JFrame {

    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FrameMainLLM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JTabbedPane tabbedPane;
    private JTextArea outputArea;
    private RuntimeCompiler compiler;
    private Map<String, Set<String>> packageMap = new HashMap<>();
    private JTree projectTree;
    private JTree membersTree;
    private DefaultTreeModel projectTreeModel;
    private DefaultTreeModel membersTreeModel;
    private File currentProjectDirectory;
    private JButton saveButton;
    private JButton openButton;
    private JButton runButton;
    private JButton clearButton;

    // Tab yönetimi için
    private Map<String, RSyntaxTextArea> openedFiles = new HashMap<>();
    private Map<String, String> fileContents = new HashMap<>();

// Kalıcı olarak son açılan dizini saklamak için gereken metodlar ve değişkenler
    private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator + ".ojl_ide_config";
    private static File lastOpenedDirectory = null;

// Ayarları yükle
    private void loadSettings() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(configFile));
                String lastDirPath = reader.readLine();
                reader.close();

                if (lastDirPath != null && !lastDirPath.isEmpty()) {
                    File dir = new File(lastDirPath);
                    if (dir.exists() && dir.isDirectory()) {
                        lastOpenedDirectory = dir;
                        currentProjectDirectory = dir;
                        loadProjectStructure(dir);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading settings: " + e.getMessage());
            // Hata olursa varsayılan davranışı bozma
        }
    }

// Ayarları kaydet
    private void saveSettings() {
        try {
            if (currentProjectDirectory != null && currentProjectDirectory.exists()) {
                File configFile = new File(CONFIG_FILE);
                BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
                writer.write(currentProjectDirectory.getAbsolutePath());
                writer.close();
            }
        } catch (Exception e) {
            System.err.println("Error saving settings: " + e.getMessage());
            // Kullanıcıya görünür bir hata gösterme, sadece loglama
        }
    }

// openProject metodunu güncelle
    private void openProject() {
        JFileChooser fileChooser = new JFileChooser();

        // Eğer daha önce bir dizin açıldıysa onu kullan, yoksa çalışma dizininden başla
        if (lastOpenedDirectory != null && lastOpenedDirectory.exists()) {
            fileChooser.setCurrentDirectory(lastOpenedDirectory);
        } else {
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        }

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Open Project Directory");

        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentProjectDirectory = fileChooser.getSelectedFile();
            // Son açılan dizini güncelle
            lastOpenedDirectory = currentProjectDirectory;
            loadProjectStructure(currentProjectDirectory);
            // Ayarları kalıcı olarak kaydet
            saveSettings();
        }
    }

// constructor'ı da güncelleyelim
    public EnhancedIDE() {
        super("Advanced Java IDE");
        compiler = new RuntimeCompiler();
        initializePackageStructure();
        initializeUI();
        setupLookAndFeel();
        addUIListeners();

        // Ayarları yükle - son açılan dizini otomatik olarak gösterecek
        loadSettings();
    }

    private void addUIListeners() {
        setupTreeMouseListener();
        setupTabChangeListener();
        setupMembersTreeMouseListener();
    }

    private void setupLookAndFeel() {
        // Ağaç kontrollerini tema renklerine göre ayarla
        projectTree.setBackground(new Color(43, 43, 43));
        projectTree.setForeground(Color.WHITE);
        membersTree.setBackground(new Color(43, 43, 43));
        membersTree.setForeground(Color.WHITE);
    }

    private void initializePackageStructure() {
        // Basit package yapısı
        packageMap.put("java.util", new HashSet<>(Arrays.asList("ArrayList", "List", "Map", "HashMap")));
        packageMap.put("java.io", new HashSet<>(Arrays.asList("File", "FileReader", "BufferedReader")));
        packageMap.put("javax.swing", new HashSet<>(Arrays.asList("JFrame", "JPanel", "JButton")));
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Toolbar oluşturma
        JToolBar toolBar = createToolBar();
        mainPanel.add(toolBar, BorderLayout.NORTH);

        // Tabbed pane oluşturma
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // Default tab oluştur
        addNewTab("New File", null);

        // Çıktı alanı
        outputArea = new JTextArea(8, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        // Proje ağacı paneli
        JPanel leftPanel = createLeftPanel();

        // Ana düzen
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, new JPanel(new BorderLayout()));
        horizontalSplit.setDividerLocation(250);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tabbedPane, outputScrollPane);
        verticalSplit.setResizeWeight(0.7);

        horizontalSplit.setRightComponent(verticalSplit);

        mainPanel.add(horizontalSplit, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Pencere kapatıldığında ayarları kaydet
        // Bu kodu initializeUI() metoduna ekleyin (setDefaultCloseOperation satırından sonra)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveSettings();
            }
        });
        setSize(1200, 800);
        setLocationRelativeTo(null);

        addSampleCode();
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        saveButton = new JButton("Save");
        saveButton.setToolTipText("Save current file");
        saveButton.addActionListener(e -> saveCurrentFile());

        openButton = new JButton("Open Project");
        openButton.setToolTipText("Open project folder");
        openButton.addActionListener(e -> openProject());

        runButton = new JButton("Run");
        runButton.setToolTipText("Run current file");
        runButton.addActionListener(e -> runCode());

        clearButton = new JButton("Clear");
        clearButton.setToolTipText("Clear editor");
        clearButton.addActionListener(e -> {
            RSyntaxTextArea currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                currentEditor.setText("");
            }
            outputArea.setText("");
        });

        toolBar.add(openButton);
        toolBar.add(saveButton);
        toolBar.addSeparator();
        toolBar.add(runButton);
        toolBar.add(clearButton);

        return toolBar;
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());

        // Proje ağacı
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Projects");
        projectTreeModel = new DefaultTreeModel(root);
        projectTree = new JTree(projectTreeModel);
        projectTree.setRootVisible(true);

        // Class members ağacı
        DefaultMutableTreeNode membersRoot = new DefaultMutableTreeNode("Members");
        membersTreeModel = new DefaultTreeModel(membersRoot);
        membersTree = new JTree(membersTreeModel);
        membersTree.setRootVisible(true);

        // Ağaçları yerleştir
        JSplitPane treeSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(projectTree), new JScrollPane(membersTree));
        treeSplitPane.setDividerLocation(400);

        leftPanel.add(treeSplitPane, BorderLayout.CENTER);

        return leftPanel;
    }

    private void addNewTab(String title, File file) {
        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        setupTextArea(textArea);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);

        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);

        // Tab'a kapatma butonu ekle
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1,
                new ButtonTabComponent(tabbedPane, this::closeTab));

        if (file != null) {
            openedFiles.put(file.getAbsolutePath(), textArea);
        }
    }

    private void closeTab(int index) {
        if (tabbedPane.getTabCount() <= 1) {
            // En az bir tab açık olmalı
            addNewTab("New File", null);
        }

        // Tab'ı kapat
        Component comp = tabbedPane.getComponentAt(index);
        tabbedPane.remove(index);

        // Açık dosyalardan çıkar
        for (Map.Entry<String, RSyntaxTextArea> entry : openedFiles.entrySet()) {
            if (comp instanceof RTextScrollPane
                    && ((RTextScrollPane) comp).getTextArea() == entry.getValue()) {
                openedFiles.remove(entry.getKey());
                fileContents.remove(entry.getKey());
                break;
            }
        }
    }

    private RSyntaxTextArea getCurrentEditor() {
        Component comp = tabbedPane.getSelectedComponent();
        if (comp instanceof RTextScrollPane) {
            return (RSyntaxTextArea) ((RTextScrollPane) comp).getTextArea();
        }
        return null;
    }

    private void setupTextArea(RSyntaxTextArea textArea) {
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setAutoIndentEnabled(true);
        textArea.setBracketMatchingEnabled(true);
        textArea.setMarkOccurrences(true);
        textArea.setPaintTabLines(true);

        // Tema ayarları
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(textArea);
        } catch (IOException e) {
            textArea.setBackground(new Color(43, 43, 43));
            textArea.setForeground(Color.WHITE);
            textArea.setCaretColor(Color.WHITE);
        }

        // Otomatik tamamlama ayarları
        LanguageSupportFactory.get().register(textArea);
        JavaLanguageSupport jls = new JavaLanguageSupport();
        jls.setAutoCompleteEnabled(true);
        jls.setAutoActivationEnabled(true);
        jls.setShowDescWindow(true);
        jls.setParameterAssistanceEnabled(true);
        jls.install(textArea);

        setupBasicCodeCompletion(textArea);
        setupKeyBindings(textArea);
        setupCodeTemplates(textArea);
    }

    // Basitleştirilmiş kod tamamlama
    private void setupBasicCodeCompletion(RSyntaxTextArea textArea) {
        // Import önerileri için mouse listener
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showEditMenuPopup(e, textArea);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showEditMenuPopup(e, textArea);
                }
            }
        });
    }

    private void showEditMenuPopup(MouseEvent e, RSyntaxTextArea textArea) {
        try {
            int offset = textArea.viewToModel2D(e.getPoint());
            int start = getWordStart(textArea, offset);
            int end = getWordEnd(textArea, offset);

            if (start != end) {
                String word = textArea.getText(start, end - start);
                if (word.length() > 0) {
                    // Düzenleme menüsü
                    JPopupMenu popup = new JPopupMenu();

                    // Sınıf adıysa import önerileri ekle
                    if (Character.isUpperCase(word.charAt(0))) {
                        List<String> suggestions = findPossibleImports(word);
                        if (!suggestions.isEmpty()) {
                            for (String suggestion : suggestions) {
                                JMenuItem importItem = new JMenuItem("Import " + suggestion);
                                importItem.addActionListener(evt -> insertImport(suggestion, textArea));
                                popup.add(importItem);
                            }
                            popup.addSeparator();
                        }
                    }

                    // Düzenleme menüsü
                    JMenuItem cutItem = new JMenuItem("Cut");
                    JMenuItem copyItem = new JMenuItem("Copy");
                    JMenuItem pasteItem = new JMenuItem("Paste");
                    JMenuItem selectAllItem = new JMenuItem("Select All");

                    cutItem.addActionListener(evt -> textArea.cut());
                    copyItem.addActionListener(evt -> textArea.copy());
                    pasteItem.addActionListener(evt -> textArea.paste());
                    selectAllItem.addActionListener(evt -> textArea.selectAll());

                    popup.add(cutItem);
                    popup.add(copyItem);
                    popup.add(pasteItem);
                    popup.add(selectAllItem);

                    popup.show(textArea, e.getX(), e.getY());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getWordStart(RSyntaxTextArea textArea, int offset) {
        try {
            String text = textArea.getText(0, offset);
            int start = offset;
            while (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
                start--;
            }
            return start;
        } catch (Exception e) {
            return offset;
        }
    }

    private int getWordEnd(RSyntaxTextArea textArea, int offset) {
        try {
            String text = textArea.getText();
            int end = offset;
            while (end < text.length() && Character.isJavaIdentifierPart(text.charAt(end))) {
                end++;
            }
            return end;
        } catch (Exception e) {
            return offset;
        }
    }

    private List<String> findPossibleImports(String className) {
        List<String> suggestions = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : packageMap.entrySet()) {
            if (entry.getValue().contains(className)) {
                suggestions.add(entry.getKey() + "." + className);
            }
        }

        return suggestions;
    }

    private void insertImport(String importStr, RSyntaxTextArea textArea) {
        try {
            String text = textArea.getText();
            String[] lines = text.split("\n");
            int insertPosition = 0;

            if (text.contains("import " + importStr + ";")) {
                return;  // Zaten varsa ekleme
            }

            boolean foundPackage = false;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.startsWith("package ")) {
                    foundPackage = true;
                    insertPosition = textArea.getLineEndOffset(i);
                    break;
                }
            }

            if (!foundPackage) {
                insertPosition = 0;
            }

            String importStatement = "\nimport " + importStr + ";\n";
            textArea.insert(importStatement, insertPosition);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupKeyBindings(RSyntaxTextArea textArea) {
        InputMap im = textArea.getInputMap();
        ActionMap am = textArea.getActionMap();

        // Ctrl+S için kaydetme
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "saveFile");
        am.put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentFile();
            }
        });

        // F6 için çalıştırma
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "runCode");
        am.put("runCode", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCode();
            }
        });
    }

    private void setupCodeTemplates(RSyntaxTextArea textArea) {
        CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();

        // For döngüsü
        ctm.addTemplate(new StaticCodeTemplate("for",
                "for (int i = 0; i < length; i++) {\n\t|\n}",
                "Basic for loop"));

        // Main method
        ctm.addTemplate(new StaticCodeTemplate("main",
                "public static void main(String[] args) {\n\t|\n}",
                "Main method"));

        // System.out.println
        ctm.addTemplate(new StaticCodeTemplate("sout",
                "System.out.println(|);",
                "Print to console"));

        // Class definition
        ctm.addTemplate(new StaticCodeTemplate("class",
                "public class ${ClassName} {\n\t|\n}",
                "Class definition"));
    }

    private void loadProjectStructure(File projectDir) {
        // Proje ağacını temizle
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(projectDir.getName());
        projectTreeModel.setRoot(root);

        // Proje dizinini tara
        scanProjectDirectory(projectDir, root);

        // Ağacı daralt - varsayılan olarak tüm tree kapalı olsun
        for (int i = 0; i < projectTree.getRowCount(); i++) {
            projectTree.collapseRow(i);
        }

        // Sadece kök düğümü genişlet
        projectTree.expandRow(0);

        // Project adını başlıkta göster
        setTitle("Open Jazari Library IDE - " + projectDir.getName());
    }

    private void scanProjectDirectory(File directory, DefaultMutableTreeNode node) {
        File[] files = directory.listFiles();
        if (files != null) {
            // Java dosyalarını içeren klasörleri belirlemek için önce kontrol yapalım
            boolean containsJavaFiles = false;
            List<File> javaFiles = new ArrayList<>();
            List<File> subDirs = new ArrayList<>();

            for (File file : files) {
                if (file.isHidden()) {
                    continue;
                }

                if (file.isDirectory()) {
                    subDirs.add(file);
                } else if (file.getName().endsWith(".java")) {
                    containsJavaFiles = true;
                    javaFiles.add(file);
                }
            }

            // Sadece Java dosyası içeren veya alt klasörlerinde Java dosyası olan klasörleri ekle
            if (containsJavaFiles) {
                // Java dosyalarını ekle
                for (File javaFile : javaFiles) {
                    FileNode fileNode = new FileNode(javaFile);
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(fileNode);
                    node.add(childNode);
                }
            }

            // Alt dizinleri tara
            for (File subDir : subDirs) {
                DefaultMutableTreeNode subDirNode = new DefaultMutableTreeNode(subDir.getName());
                boolean added = false;

                // Boş düğüm oluşturup alt içeriği işle
                scanProjectDirectory(subDir, subDirNode);

                // Eğer alt düğüm boş değilse (yani Java dosyası içeriyorsa) ekle
                if (subDirNode.getChildCount() > 0) {
                    node.add(subDirNode);
                    added = true;
                }
            }
        }
    }

    private void setupTreeMouseListener() {
        // Mevcut selection listener'ı kaldır
        for (TreeSelectionListener listener : projectTree.getTreeSelectionListeners()) {
            projectTree.removeTreeSelectionListener(listener);
        }

        // Çift tıklama için mouse listener ekle
        projectTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) projectTree.getLastSelectedPathComponent();

                    if (node == null) {
                        return;
                    }

                    Object nodeInfo = node.getUserObject();
                    if (node.isLeaf() && nodeInfo instanceof FileNode) {
                        FileNode fileNode = (FileNode) nodeInfo;
                        openFile(fileNode.getFile());
                        updateMembersTree(fileNode.getFile());
                    }
                }
            }
        });
    }

    private void setupTabChangeListener() {
        tabbedPane.addChangeListener(e -> {
            // Aktif tab değiştiğinde members panelini güncelle
            RSyntaxTextArea currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                // Açık dosyayı bul
                for (Map.Entry<String, RSyntaxTextArea> entry : openedFiles.entrySet()) {
                    if (entry.getValue() == currentEditor) {
                        updateMembersTree(new File(entry.getKey()));
                        break;
                    }
                }
            }
        });
    }

    private void setupMembersTreeMouseListener() {
        membersTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) membersTree.getLastSelectedPathComponent();

                    if (node == null || node.isRoot()
                            || node.toString().equals("Methods")
                            || node.toString().equals("Fields")
                            || node.toString().equals("No methods found")
                            || node.toString().equals("No fields found")) {
                        return;
                    }

                    // Seçilen metodun/alanın adını al
                    String selectedItem = node.toString().trim();
                    RSyntaxTextArea currentEditor = getCurrentEditor();

                    if (currentEditor != null) {
                        // Metot/alan adını kodda ara ve o konuma git
                        String text = currentEditor.getText();
                        int index = text.indexOf(selectedItem);

                        if (index >= 0) {
                            currentEditor.setCaretPosition(index);
                            currentEditor.requestFocus();

                            // Görünen bölgeyi ayarla
                            try {
                                Rectangle rect = currentEditor.modelToView(index);
                                if (rect != null) {
                                    // Seçilen yerin merkeze gelmesini sağla
                                    JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
                                    rect.y = Math.max(0, rect.y - scrollPane.getViewport().getHeight() / 2);
                                    currentEditor.scrollRectToVisible(rect);

                                    // İlgili satırı seç
                                    int line = currentEditor.getLineOfOffset(index);
                                    int lineStart = currentEditor.getLineStartOffset(line);
                                    int lineEnd = currentEditor.getLineEndOffset(line);
                                    currentEditor.select(lineStart, lineEnd);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    private void openFile(File file) {
        try {
            // Dosya zaten açıksa, o tab'a geç
            if (openedFiles.containsKey(file.getAbsolutePath())) {
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    Component comp = tabbedPane.getComponentAt(i);
                    if (comp instanceof RTextScrollPane
                            && ((RTextScrollPane) comp).getTextArea() == openedFiles.get(file.getAbsolutePath())) {
                        tabbedPane.setSelectedIndex(i);
                        return;
                    }
                }
            }

            // Dosyayı oku
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            // Yeni tab ekle
            addNewTab(file.getName(), file);

            // İçeriği ayarla
            RSyntaxTextArea currentEditor = getCurrentEditor();
            currentEditor.setText(content.toString());
            currentEditor.setCaretPosition(0);

            // Dosya içeriğini sakla (değişiklikleri kontrol için)
            fileContents.put(file.getAbsolutePath(), content.toString());

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error opening file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void saveCurrentFile() {
        RSyntaxTextArea currentEditor = getCurrentEditor();
        if (currentEditor == null) {
            return;
        }

        // Açık dosyayı bul
        String filePath = null;
        for (Map.Entry<String, RSyntaxTextArea> entry : openedFiles.entrySet()) {
            if (entry.getValue() == currentEditor) {
                filePath = entry.getKey();
                break;
            }
        }

        if (filePath != null) {
            // Mevcut dosyayı kaydet
            saveFile(new File(filePath), currentEditor.getText());
        } else {
            // Yeni dosya oluştur
            saveAsNewFile(currentEditor.getText());
        }
    }

    private void saveFile(File file, String content) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();

            // Kaydedilen içeriği güncelle
            fileContents.put(file.getAbsolutePath(), content);

            outputArea.append("\nFile saved: " + file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving file: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void saveAsNewFile(String content) {
        JFileChooser fileChooser = new JFileChooser();
        if (currentProjectDirectory != null) {
            fileChooser.setCurrentDirectory(currentProjectDirectory);
        }
        fileChooser.setFileFilter(new FileNameExtensionFilter("Java Files (*.java)", "java"));

        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // .java uzantısı ekle
            if (!file.getName().toLowerCase().endsWith(".java")) {
                file = new File(file.getAbsolutePath() + ".java");
            }

            saveFile(file, content);

            // Tab başlığını güncelle
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex >= 0) {
                tabbedPane.setTitleAt(selectedIndex, file.getName());
                // Tab component'ını güncelle
                tabbedPane.setTabComponentAt(selectedIndex,
                        new ButtonTabComponent(tabbedPane, this::closeTab));
            }

            // Açık dosya listesine ekle
            openedFiles.put(file.getAbsolutePath(), getCurrentEditor());

            // Proje ağacını güncelle (eğer proje dizinine kaydedildiyse)
            if (currentProjectDirectory != null
                    && file.getAbsolutePath().startsWith(currentProjectDirectory.getAbsolutePath())) {
                loadProjectStructure(currentProjectDirectory);
            }
        }
    }

    private void updateMembersTree(File file) {
        try {
            // Önce tree'yi temizle
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(file.getName() + " Members");
            membersTreeModel.setRoot(root);

            // Basit bir Java parser kullanarak sınıf içindeki üyeleri bul
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode("Methods");
            DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode("Fields");
            root.add(methodsNode);
            root.add(fieldsNode);

            // Basit Java parser
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Metodları bul
                if ((line.contains("public ") || line.contains("private ")
                        || line.contains("protected ") || line.startsWith("void ")
                        || line.contains(" static ")) && line.contains("(")
                        && !line.contains("=") && !line.contains("new ") && !line.contains(";")) {

                    // Method adını ayıkla
                    String methodName = line;
                    // Yorumları temizle
                    if (methodName.contains("//")) {
                        methodName = methodName.substring(0, methodName.indexOf("//"));
                    }// Method signature'ı al
                    if (methodName.contains("(")) {
                        methodName = methodName.substring(0, methodName.indexOf(")") + 1);
                        methodsNode.add(new DefaultMutableTreeNode(methodName));
                    }
                }

                // Alanları bul
                if ((line.contains("private ") || line.contains("public ")
                        || line.contains("protected ") || line.contains(" static "))
                        && line.contains(";") && !line.contains("(")) {

                    // Alan adını ayıkla
                    String fieldName = line;
                    // Yorumları temizle
                    if (fieldName.contains("//")) {
                        fieldName = fieldName.substring(0, fieldName.indexOf("//"));
                    }

                    // Değişken adını al
                    fieldName = fieldName.substring(0, fieldName.indexOf(";"));
                    fieldsNode.add(new DefaultMutableTreeNode(fieldName));
                }
            }
            reader.close();

            // Eğer boşsa placeholder ekle
            if (methodsNode.getChildCount() == 0) {
                methodsNode.add(new DefaultMutableTreeNode("No methods found"));
            }
            if (fieldsNode.getChildCount() == 0) {
                fieldsNode.add(new DefaultMutableTreeNode("No fields found"));
            }

            // Ağacı güncelle ve genişlet
            membersTreeModel.reload();
            for (int i = 0; i < membersTree.getRowCount(); i++) {
                membersTree.expandRow(i);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runCode() {
        RSyntaxTextArea currentEditor = getCurrentEditor();
        if (currentEditor == null) {
            return;
        }

        String code = currentEditor.getText();
        outputArea.setText("Compiling and running code...\n");

        runButton.setEnabled(false);

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    compiler.compile(code, outputArea);
                } catch (Exception e) {
                    publish("\nError: " + e.getMessage());
                    e.printStackTrace(new PrintStream(new OutputStreamAdapter(outputArea)));
                }
                return null;
            }

            @Override
            protected void done() {
                runButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void addSampleCode() {
        String sampleCode
                = "import java.util.ArrayList;\n"
                + "import jazari.matrix.CMatrix;\n\n"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello from Advanced IDE!\");\n"
                + "        \n"
                + "        // CMatrix örneği (otomatik import önerisi gelecek)\n"
                + "        CMatrix cm = CMatrix.getInstance()\n"
                + "                     .imread(\"images/pullar.png\")\n"
                + "                     .imshow()\n"
                + "                     ;\n"
                + "        // String işlemleri\n"
                + "        String text = \"Hello World\";\n"
                + "        System.out.println(\"Length: \" + text.length());\n"
                + "        \n"
                + "        // ArrayList örneği\n"
                + "        ArrayList<String> list = new ArrayList<>();\n"
                + "        list.add(\"Java\");\n"
                + "        System.out.println(\"List size: \" + list.size());\n"
                + "    }\n"
                + "}\n";

        RSyntaxTextArea currentEditor = getCurrentEditor();
        if (currentEditor != null) {
            currentEditor.setText(sampleCode);
        }
    }

    // Tab kapatma butonu için özel component
    class ButtonTabComponent extends JPanel {

        private final JTabbedPane pane;
        private final Consumer<Integer> closeAction;

        public ButtonTabComponent(JTabbedPane pane, Consumer<Integer> closeAction) {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            this.pane = pane;
            this.closeAction = closeAction;
            setOpaque(false);

            JLabel label = new JLabel() {
                @Override
                public String getText() {
                    int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                    if (i != -1) {
                        return pane.getTitleAt(i);
                    }
                    return null;
                }
            };

            add(label);
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

            JButton button = new JButton("×");
            button.setPreferredSize(new Dimension(17, 17));
            button.setToolTipText("Close this tab");
            button.setContentAreaFilled(false);
            button.setFocusable(false);
            button.setBorder(BorderFactory.createEtchedBorder());
            button.setBorderPainted(false);
            button.addActionListener(e -> {
                int i = pane.indexOfTabComponent(ButtonTabComponent.this);
                if (i != -1) {
                    closeAction.accept(i);
                }
            });
            add(button);
        }
    }

    // File düğümü için yardımcı sınıf
    class FileNode {

        private final File file;

        public FileNode(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String toString() {
            return file.getName();
        }
    }

    // JTextArea için OutputStream adaptörü
    class OutputStreamAdapter extends OutputStream {

        private final JTextArea textArea;

        public OutputStreamAdapter(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            // EDT'de çalıştır
            SwingUtilities.invokeLater(() -> {
                textArea.append(String.valueOf((char) b));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            // EDT'de çalıştır
            SwingUtilities.invokeLater(() -> {
                textArea.append(new String(b, off, len));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EnhancedIDE().setVisible(true);
        });
    }
}
