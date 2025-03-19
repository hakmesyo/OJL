package jazari.interpreter;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.*;
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
import org.fife.ui.rsyntaxtextarea.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    private JButton newFileButton;

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
// Output alanı için daha iyi fare tekerleği desteği
        outputArea.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    // Ctrl tuşuna basılıysa font boyutunu değiştir
                    int rotation = e.getWheelRotation();
                    if (rotation < 0) {
                        changeOutputFontSize(1); // Büyüt
                    } else {
                        changeOutputFontSize(-1); // Küçült
                    }
                    e.consume(); // Olayı tüket
                } else {
                    // Ctrl basılı değilse, normal kaydırma davranışını gerçekleştir
                    // Olay işleyicisini outputArea'dan çıkar ve orijinal işleyiciye ilet

                    // Önce, listener'ı geçici olarak kaldır
                    MouseWheelListener[] listeners = outputArea.getMouseWheelListeners();
                    for (MouseWheelListener listener : listeners) {
                        if (listener == this) {
                            outputArea.removeMouseWheelListener(this);
                            break;
                        }
                    }

                    // Sonra, olayı parent bileşene ilet (JScrollPane)
                    Container parent = outputArea.getParent();
                    if (parent != null) {
                        parent.dispatchEvent(e);
                    }

                    // Son olarak, listener'ı geri ekle
                    outputArea.addMouseWheelListener(this);
                }
            }
        });

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
        // initializeUI metodunun sonuna ekleyin (setSize komutundan sonra)
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pencereyi tam ekran yap
        setLocationRelativeTo(null);

        addSampleCode();

        KeyStroke altShiftF = KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(altShiftF, "formatCodeGlobal");
        getRootPane().getActionMap().put("formatCodeGlobal", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formatCurrentCode();
            }
        });
    }
// Output alanının font boyutunu değiştirmek için yeni metod

    private void changeOutputFontSize(int sizeChange) {
        if (outputArea != null) {
            Font currentFont = outputArea.getFont();
            int newSize = currentFont.getSize() + sizeChange;

            // Minimum ve maksimum font boyutu sınırları
            newSize = Math.max(8, Math.min(36, newSize));

            Font newFont = new Font(currentFont.getFontName(), currentFont.getStyle(), newSize);
            outputArea.setFont(newFont);
        }
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        newFileButton = new JButton("New File");
        newFileButton.setToolTipText("Create new file");
        newFileButton.addActionListener(e -> createNewFile());
        toolBar.add(newFileButton, 0); // Araç çubuğunun başına ekleyin

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

    private void createNewFile() {
        // Kullanıcıdan dosya adı isteyin
        String fileName = JOptionPane.showInputDialog(this,
                "Enter file name:", "New File", JOptionPane.QUESTION_MESSAGE);

        if (fileName != null && !fileName.trim().isEmpty()) {
            // Eğer .java uzantısı yoksa ekleyin
            if (!fileName.toLowerCase().endsWith(".java")) {
                fileName += ".java";
            }

            // Yeni bir tab oluşturun
            addNewTab(fileName, null);

            // Eğer isterseniz temel bir Java sınıf şablonu ekleyebilirsiniz
            RSyntaxTextArea currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                String className = fileName.substring(0, fileName.lastIndexOf('.'));
                String classTemplate
                        = "public class " + className + " {\n\n"
                        + "\tpublic static void main(String[] args) {\n"
                        + "\t\t// Your code here\n"
                        + "\t\tSystem.out.println(\"Hello from " + className + "\");\n"
                        + "\t}\n"
                        + "}\n";
                currentEditor.setText(classTemplate);
                currentEditor.setCaretPosition(classTemplate.indexOf("// Your code here"));
            }
        }
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

        // Kaydırma paneline fare tekerleği listener'ı ekleyin
        scrollPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    // Ctrl tuşuna basılıysa font boyutunu değiştir
                    int rotation = e.getWheelRotation();
                    if (rotation < 0) {
                        changeFontSize(1); // Büyüt
                    } else {
                        changeFontSize(-1); // Küçült
                    }
                    e.consume(); // Olayı tüket
                }
                // Ctrl basılı değilse, normal kaydırma davranışı otomatik gerçekleşir
            }
        });

        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);

        // Tab'a kapatma butonu ekle
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1,
                new ButtonTabComponent(tabbedPane, this::closeTab));

        if (file != null) {
            openedFiles.put(file.getAbsolutePath(), textArea);
        }

        // Metin değiştikçe üyeleri güncelle
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateMembersFromText(textArea.getText(), title);
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateMembersFromText(textArea.getText(), title);
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                // Plain text için çağrılmaz
            }
        });

        // Başlangıçta da bir güncelleme yap
        if (file == null) {
            SwingUtilities.invokeLater(() -> {
                updateMembersFromText(textArea.getText(), title);
            });
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

//        Font currentFont = textArea.getFont();
//        Font largerFont = new Font(currentFont.getFontName(), currentFont.getStyle(), 18); // 16 punto
//        textArea.setFont(largerFont);
        // Otomatik tamamlama özelliğini kur - ESKİ KOD YERİNE BUNU KULLANIN
        JavaAutoCompleteProvider autoCompleteProvider = new JavaAutoCompleteProvider(textArea);
        autoCompleteProvider.install();

        // Diğer ayarları koru
        setupKeyBindings(textArea);
        setupCodeTemplates(textArea);
        textArea.setComponentPopupMenu(createEditorPopupMenu(textArea));
//        textArea.addMouseWheelListener(new MouseWheelListener() {
//            @Override
//            public void mouseWheelMoved(MouseWheelEvent e) {
//                // Ctrl tuşuna basılı mı kontrol et
//                if (e.isControlDown()) {
//                    // Tekerleğin yukarı/aşağı hareketi
//                    int rotation = e.getWheelRotation();
//
//                    // Tekerlek aşağı = negatif değer, yukarı = pozitif değer
//                    // Aşağı = küçültme, yukarı = büyütme
//                    if (rotation < 0) {
//                        changeFontSize(1); // Yukarı: Font boyutunu artır
//                    } else {
//                        changeFontSize(-1); // Aşağı: Font boyutunu azalt
//                    }
//
//                    // Olayın normal işlenmesini engelle
//                    e.consume();
//                }
//            }
//        });
    }

    /**
     * Editör için sağ tıklama popup menüsü oluşturur
     */
// Gelişmiş sağ tıklama menüsü kodu
    private JPopupMenu createEditorPopupMenu(RSyntaxTextArea textArea) {
        // Mevcut popup menüyü al (varsa)
        JPopupMenu existingPopup = textArea.getComponentPopupMenu();
        JPopupMenu popup = (existingPopup != null) ? existingPopup : new JPopupMenu();

        // Run seçeneği
        JMenuItem runItem = new JMenuItem("Run (F6)");
        runItem.addActionListener(e -> runCode());
        runItem.setMnemonic('R');
        popup.insert(runItem, 0);

        // Ayırıcı ekle
        popup.insert(new JPopupMenu.Separator(), 1);

        // Insert alt menüsü
        JMenu insertMenu = new JMenu("Insert");
        insertMenu.setMnemonic('I');

        // Constructor
        JMenuItem constructorItem = new JMenuItem("Constructor...");
        constructorItem.addActionListener(e -> insertConstructor());
        insertMenu.add(constructorItem);

        // Getter ve Setter
        JMenuItem getterSetterItem = new JMenuItem("Getter and Setter...");
        getterSetterItem.addActionListener(e -> insertGetterSetter());
        insertMenu.add(getterSetterItem);

        // Override metotları
        JMenuItem overrideItem = new JMenuItem("Override Methods...");
        overrideItem.addActionListener(e -> insertOverrideMethods());
        insertMenu.add(overrideItem);

        // Try/Catch bloğu
        JMenuItem tryCatchItem = new JMenuItem("Try/Catch Block");
        tryCatchItem.addActionListener(e -> insertTryCatch());
        insertMenu.add(tryCatchItem);

        // Main metodu
        JMenuItem mainMethodItem = new JMenuItem("Main Method");
        mainMethodItem.addActionListener(e -> insertMainMethod());
        insertMenu.add(mainMethodItem);

        // For döngüsü
        JMenuItem forLoopItem = new JMenuItem("For Loop");
        forLoopItem.addActionListener(e -> insertForLoop());
        insertMenu.add(forLoopItem);

        // While döngüsü
        JMenuItem whileLoopItem = new JMenuItem("While Loop");
        whileLoopItem.addActionListener(e -> insertWhileLoop());
        insertMenu.add(whileLoopItem);

        // If bloğu
        JMenuItem ifBlockItem = new JMenuItem("If Block");
        ifBlockItem.addActionListener(e -> insertIfBlock());
        insertMenu.add(ifBlockItem);

        // Switch bloğu
        JMenuItem switchBlockItem = new JMenuItem("Switch Block");
        switchBlockItem.addActionListener(e -> insertSwitchBlock());
        insertMenu.add(switchBlockItem);

        // Ana menüye Insert alt menüsünü ekle
        popup.insert(insertMenu, 2);

        // Format kodu seçeneği
        JMenuItem formatItem = new JMenuItem("Format Code (Alt+Shift+F)");
        formatItem.addActionListener(e -> formatCurrentCode());
        formatItem.setMnemonic('F');
        popup.add(formatItem);

        return popup;
    }

// Insert menüsü için gerekli metodlar
    private void insertConstructor() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }

        // Sınıf adını ve alanları analiz et
        String className = extractClassName(editor.getText());
        List<String> fields = extractFields(editor.getText());

        if (className == null || className.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Class name could not be determined.\nMake sure a class is defined in this file.",
                    "Insert Constructor", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Constructor template'i oluştur
        StringBuilder constructor = new StringBuilder();
        constructor.append("\n    /**\n");
        constructor.append("     * Constructor for ").append(className).append("\n");
        constructor.append("     */\n");
        constructor.append("    public ").append(className).append("(");

        // Parametreler
        if (!fields.isEmpty()) {
            for (int i = 0; i < fields.size(); i++) {
                String field = fields.get(i);
                String[] parts = field.trim().split("\\s+");
                // En az 2 parça olmalı: tip ve değişken adı
                if (parts.length >= 2) {
                    String type = parts[0];
                    String name = parts[parts.length - 1];
                    if (name.endsWith(";")) {
                        name = name.substring(0, name.length() - 1);
                    }

                    if (i > 0) {
                        constructor.append(", ");
                    }
                    constructor.append(type).append(" ").append(name);
                }
            }
        }

        constructor.append(") {\n");

        // Alan atamalarını ekle
        if (!fields.isEmpty()) {
            for (String field : fields) {
                String[] parts = field.trim().split("\\s+");
                if (parts.length >= 2) {
                    String name = parts[parts.length - 1];
                    if (name.endsWith(";")) {
                        name = name.substring(0, name.length() - 1);
                    }
                    constructor.append("        this.").append(name).append(" = ").append(name).append(";\n");
                }
            }
        } else {
            constructor.append("        // Initialize fields here\n");
        }

        constructor.append("    }\n");

        // İmleç konumuna constructor'ı ekle
        int caretPos = editor.getCaretPosition();
        editor.insert(constructor.toString(), caretPos);
    }

    private void insertGetterSetter() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }

        // Alanları analiz et
        List<String> fields = extractFields(editor.getText());

        if (fields.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No fields found in this class.\nAdd fields first to generate getters/setters.",
                    "Insert Getter/Setter", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Getter/Setter oluşturulacak alanları seçme iletişim kutusu
        JDialog dialog = new JDialog(this, "Select Fields", true);
        dialog.setLayout(new BorderLayout());

        JPanel fieldsPanel = new JPanel(new GridLayout(0, 1));
        List<JCheckBox> checkBoxes = new ArrayList<>();

        for (String field : fields) {
            String[] parts = field.trim().split("\\s+");
            if (parts.length >= 2) {
                String name = parts[parts.length - 1];
                if (name.endsWith(";")) {
                    name = name.substring(0, name.length() - 1);
                }
                JCheckBox checkBox = new JCheckBox(field.trim());
                checkBox.setSelected(true);
                checkBoxes.add(checkBox);
                fieldsPanel.add(checkBox);
            }
        }

        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            StringBuilder gettersSetters = new StringBuilder();

            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isSelected()) {
                    String field = fields.get(i);
                    String[] parts = field.trim().split("\\s+");
                    if (parts.length >= 2) {
                        String type = parts[0];
                        String name = parts[parts.length - 1];
                        if (name.endsWith(";")) {
                            name = name.substring(0, name.length() - 1);
                        }

                        // Getter
                        gettersSetters.append("\n    /**\n");
                        gettersSetters.append("     * @return the ").append(name).append("\n");
                        gettersSetters.append("     */\n");
                        gettersSetters.append("    public ").append(type).append(" get")
                                .append(Character.toUpperCase(name.charAt(0)))
                                .append(name.substring(1)).append("() {\n");
                        gettersSetters.append("        return ").append(name).append(";\n");
                        gettersSetters.append("    }\n\n");

                        // Setter
                        gettersSetters.append("    /**\n");
                        gettersSetters.append("     * @param ").append(name).append(" the ")
                                .append(name).append(" to set\n");
                        gettersSetters.append("     */\n");
                        gettersSetters.append("    public void set")
                                .append(Character.toUpperCase(name.charAt(0)))
                                .append(name.substring(1)).append("(").append(type).append(" ")
                                .append(name).append(") {\n");
                        gettersSetters.append("        this.").append(name).append(" = ").append(name).append(";\n");
                        gettersSetters.append("    }\n");
                    }
                }
            }

            // İmleç konumuna getter/setter'ları ekle
            int caretPos = editor.getCaretPosition();
            editor.insert(gettersSetters.toString(), caretPos);

            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void insertOverrideMethods() {
        // Bu kısım daha karmaşık ve reflection gerektiriyor
        JOptionPane.showMessageDialog(this,
                "Override Methods feature is not yet implemented.\nWill be added in a future update.",
                "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }

    private void insertTryCatch() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }

        String template = "try {\n    // Your code here\n} catch (Exception e) {\n    e.printStackTrace();\n}";
        int caretPos = editor.getCaretPosition();
        editor.insert(template, caretPos);
        editor.setCaretPosition(caretPos + template.indexOf("// Your code here"));
    }

    private void insertMainMethod() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }

        String template = "public static void main(String[] args) {\n    // Your code here\n}";
        int caretPos = editor.getCaretPosition();
        editor.insert(template, caretPos);
        editor.setCaretPosition(caretPos + template.indexOf("// Your code here"));
    }

    private void insertForLoop() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }

        String template = "for (int i = 0; i < 10; i++) {\n    // Your code here\n}";
        int caretPos = editor.getCaretPosition();
        editor.insert(template, caretPos);
        editor.setCaretPosition(caretPos + template.indexOf("// Your code here"));
    }

    private void insertWhileLoop() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }

        String template = "while (condition) {\n    // Your code here\n}";
        int caretPos = editor.getCaretPosition();
        editor.insert(template, caretPos);
        editor.setCaretPosition(caretPos + template.indexOf("condition"));
    }

    private void insertIfBlock() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }

        String template = "if (condition) {\n    // Your code here\n}";
        int caretPos = editor.getCaretPosition();
        editor.insert(template, caretPos);
        editor.setCaretPosition(caretPos + template.indexOf("condition"));
    }

    private void insertSwitchBlock() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }

        String template = "switch (variable) {\n    case value1:\n        // Code for value1\n        break;\n    case value2:\n        // Code for value2\n        break;\n    default:\n        // Default code\n        break;\n}";
        int caretPos = editor.getCaretPosition();
        editor.insert(template, caretPos);
        editor.setCaretPosition(caretPos + template.indexOf("variable"));
    }

// Yardımcı metotlar
    private String extractClassName(String code) {
        String className = null;

        // Regex ile sınıf tanımını ara
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\bclass\\s+([A-Za-z0-9_]+)");
        java.util.regex.Matcher matcher = pattern.matcher(code);

        if (matcher.find()) {
            className = matcher.group(1);
        }

        return className;
    }

    private List<String> extractFields(String code) {
        List<String> fields = new ArrayList<>();

        // Satırlara böl
        String[] lines = code.split("\n");
        boolean inClass = false;
        int bracketLevel = 0;

        for (String line : lines) {
            line = line.trim();

            // Sınıf tanımını bul
            if (!inClass && line.contains("class")) {
                inClass = true;
            }

            // Süslü parantezleri say
            if (inClass) {
                for (char c : line.toCharArray()) {
                    if (c == '{') {
                        bracketLevel++;
                    }
                    if (c == '}') {
                        bracketLevel--;
                    }
                }

                // Sınıf içindeki alanları bul
                if (bracketLevel > 0 && line.contains(";")
                        && !line.contains("(") && !line.contains("=")
                        && (line.contains("private ") || line.contains("protected ")
                        || line.contains("public ") || Character.isLowerCase(line.charAt(0)))) {

                    // Yorumları temizle
                    if (line.contains("//")) {
                        line = line.substring(0, line.indexOf("//"));
                    }

                    fields.add(line);
                }
            }
        }

        return fields;
    }

    private void changeFontSize(int sizeChange) {
        RSyntaxTextArea currentEditor = getCurrentEditor();
        if (currentEditor != null) {
            Font currentFont = currentEditor.getFont();
            int newSize = currentFont.getSize() + sizeChange;

            // Minimum ve maksimum font boyutu sınırları
            newSize = Math.max(8, Math.min(36, newSize));

            Font newFont = new Font(currentFont.getFontName(), currentFont.getStyle(), newSize);
            currentEditor.setFont(newFont);
        }
    }

    private void setupKeyBindings(RSyntaxTextArea textArea) {
        InputMap im = textArea.getInputMap();
        ActionMap am = textArea.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK), "formatCode");
        am.put("formatCode", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formatCurrentCode();
            }
        });
        // Ctrl+S için kaydetme
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "saveFile");
        am.put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentFile();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), "newFile");
        am.put("newFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createNewFile();
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
        // Shift+F6 için yeni binding ekle
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_DOWN_MASK), "runCodeShift");
        am.put("runCodeShift", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCode();
            }
        });

        // F9 için yeni binding ekle
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "runCodeF9");
        am.put("runCodeF9", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCode();
            }
        });
    }

// Kod formatlama metodunu ekleyin
    private void formatCurrentCode() {
        RSyntaxTextArea currentEditor = getCurrentEditor();
        if (currentEditor == null) {
            return;
        }

        try {
            String code = currentEditor.getText();
            String formattedCode = formatJavaCode(code);

            // Formatlanan kodu editöre yerleştir
            int caretPosition = currentEditor.getCaretPosition();
            currentEditor.setText(formattedCode);

            // İmleci mümkünse aynı pozisyonda tut
            try {
                if (caretPosition < formattedCode.length()) {
                    currentEditor.setCaretPosition(caretPosition);
                }
            } catch (Exception e) {
                // İmleç konumlandırma hatası olursa yok say
            }

            outputArea.append("\nCode formatted successfully.");
        } catch (Exception e) {
            outputArea.append("\nError formatting code: " + e.getMessage());
        }
    }

    /**
     * Java kodunu formatlar Bu basit bir formatlayıcıdır, daha gelişmiş
     * formatlama için kütüphaneler kullanılabilir
     */
    private String formatJavaCode(String code) {
        StringBuilder formattedCode = new StringBuilder();
        String[] lines = code.split("\n");
        int indentLevel = 0;
        boolean inComment = false;

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Boş satırı olduğu gibi ekle
            if (trimmedLine.isEmpty()) {
                formattedCode.append("\n");
                continue;
            }

            // Çok satırlı yorum kontrolü
            if (trimmedLine.contains("/*")) {
                inComment = true;
            }
            if (trimmedLine.contains("*/")) {
                inComment = false;
            }

            // Süslü parantezleri kontrol et ve indent seviyesini ayarla
            if (trimmedLine.endsWith("}") || trimmedLine.endsWith("};")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }

            // İndent uygula
            if (!inComment && !trimmedLine.startsWith("*")) {
                for (int i = 0; i < indentLevel; i++) {
                    formattedCode.append("    "); // 4 boşluk indent
                }
            } else {
                // Yorumlar için özel formatlama
                formattedCode.append(" "); // Yorum satırlarında sadece 1 boşluk bırak
            }

            // Satırı ekle
            formattedCode.append(trimmedLine).append("\n");

            // Süslü parantez sonrası indent seviyesini artır
            if (trimmedLine.endsWith("{")) {
                indentLevel++;
            }
        }

        return formattedCode.toString();
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
                boolean updated = false;

                // Önce açık dosya listesinden kontrol et
                for (Map.Entry<String, RSyntaxTextArea> entry : openedFiles.entrySet()) {
                    if (entry.getValue() == currentEditor) {
                        updateMembersTree(new File(entry.getKey()));
                        updated = true;
                        break;
                    }
                }

                // Eğer dosya olarak kayıtlı değilse metinden güncelle
                if (!updated) {
                    int selectedIndex = tabbedPane.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        String title = tabbedPane.getTitleAt(selectedIndex);
                        updateMembersFromText(currentEditor.getText(), title);
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

    private void updateMembersFromText(String text, String title) {
        try {
            // Önce tree'yi temizle
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(title + " Members");
            membersTreeModel.setRoot(root);

            DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode("Methods");
            DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode("Fields");
            root.add(methodsNode);
            root.add(fieldsNode);

            // Süslü parantez seviyesini takip et
            int bracketLevel = 0;
            boolean inMethod = false;

            // Metni satır satır işle
            String[] lines = text.split("\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                // Süslü parantez seviyesini takip et
                for (char c : line.toCharArray()) {
                    if (c == '{') {
                        bracketLevel++;
                    }
                    if (c == '}') {
                        bracketLevel--;
                    }
                }

                // Metod başlangıcını kontrol et
                if (line.contains("(") && line.contains(")")
                        && !line.contains("if") && !line.contains("for") && !line.contains("while")
                        && !line.contains("switch") && (line.endsWith("{")
                        || (i + 1 < lines.length && lines[i + 1].trim().equals("{")))) {

                    // Method adını ayıkla
                    String methodName = line;
                    // Yorumları temizle
                    if (methodName.contains("//")) {
                        methodName = methodName.substring(0, methodName.indexOf("//"));
                    }
                    // Method signature'ı al
                    if (methodName.contains("(")) {
                        int closingParenIndex = methodName.indexOf(")");
                        if (closingParenIndex != -1) {  // Eğer ")" karakteri varsa
                            methodName = methodName.substring(0, closingParenIndex + 1);
                            methodsNode.add(new DefaultMutableTreeNode(methodName));
                            inMethod = true;
                        }
                    }
                }

                // Metod içinde değilsek (bracketLevel = 1 sınıf seviyesi demektir)
                // ve süslü parantez seviyesi 1 ise (sınıf tanımı içinde ama metod içinde değil)
                if (!inMethod || bracketLevel == 1) {
                    // Alanları bul - sadece sınıf seviyesindeki değişkenleri
                    if (line.contains(";") && !line.contains("(") && !line.contains("import ")
                            && !line.contains("package ") && !line.startsWith("//") && !line.startsWith("for")
                            && !line.startsWith("if") && !line.startsWith("while") && !line.startsWith("return")) {

                        // Alan adını ayıkla
                        String fieldName = line;
                        // Yorumları temizle
                        if (fieldName.contains("//")) {
                            fieldName = fieldName.substring(0, fieldName.indexOf("//"));
                        }

                        // Değişken adını al
                        int semicolonIndex = fieldName.indexOf(";");
                        if (semicolonIndex != -1) {  // Eğer ";" karakteri varsa
                            fieldName = fieldName.substring(0, semicolonIndex);

                            // Başlangıçtaki erişim belirleyicilerini veya diğer anahtar kelimeleri temizle
                            fieldName = fieldName.replaceAll("^(public|private|protected|static|final|abstract|transient|volatile)\\s+", "");

                            // Veri tipini ve değişken adını içeren kısmı al
                            String[] parts = fieldName.split("=")[0].trim().split("\\s+");
                            if (parts.length >= 2) {
                                // Veri tipi ve değişken adı bir arada göster
                                fieldsNode.add(new DefaultMutableTreeNode(fieldName.trim()));
                            } else if (parts.length == 1) {
                                // Tek kelimeli, muhtemelen veri tipi olmayan bir atama (int a=5; gibi)
                                fieldsNode.add(new DefaultMutableTreeNode(fieldName.trim()));
                            }
                        }
                    }
                }

                // Metod sonu kontrolü - süslü parantez seviyesi düştüyse ve 1'e eşitse
                // (yani sınıf seviyesine döndüysek)
                if (bracketLevel == 1 && inMethod) {
                    inMethod = false;
                }
            }

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
        } catch (Exception e) {
            e.printStackTrace();
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
            String fileContent = "";

            // Tüm dosya içeriğini oku
            StringBuilder contentBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            reader.close();

            fileContent = contentBuilder.toString();

            DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode("Methods");
            DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode("Fields");
            root.add(methodsNode);
            root.add(fieldsNode);

            // Süslü parantez seviyesini takip et
            int bracketLevel = 0;
            boolean inMethod = false;

            // Dosyayı tekrar satır satır işle
            String[] lines = fileContent.split("\n");
            for (int i = 0; i < lines.length; i++) {
                line = lines[i].trim();

                // Süslü parantez seviyesini takip et
                for (char c : line.toCharArray()) {
                    if (c == '{') {
                        bracketLevel++;
                    }
                    if (c == '}') {
                        bracketLevel--;
                    }
                }

                // Metod başlangıcını kontrol et
                if (line.contains("(") && line.contains(")")
                        && !line.contains("if") && !line.contains("for") && !line.contains("while")
                        && !line.contains("switch") && (line.endsWith("{")
                        || (i + 1 < lines.length && lines[i + 1].trim().equals("{")))) {

                    // Method adını ayıkla
                    String methodName = line;
                    // Yorumları temizle
                    if (methodName.contains("//")) {
                        methodName = methodName.substring(0, methodName.indexOf("//"));
                    }
                    // Method signature'ı al
                    if (methodName.contains("(")) {
                        int closingParenIndex = methodName.indexOf(")");
                        if (closingParenIndex != -1) {  // Eğer ")" karakteri varsa
                            methodName = methodName.substring(0, closingParenIndex + 1);
                            methodsNode.add(new DefaultMutableTreeNode(methodName));
                            inMethod = true;
                        }
                    }
                }

                // Metod içinde değilsek (bracketLevel = 1 sınıf seviyesi demektir)
                // ve süslü parantez seviyesi 1 ise (sınıf tanımı içinde ama metod içinde değil)
                if (!inMethod || bracketLevel == 1) {
                    // Alanları bul - sadece sınıf seviyesindeki değişkenleri
                    if (line.contains(";") && !line.contains("(") && !line.contains("import ")
                            && !line.contains("package ") && !line.startsWith("//") && !line.startsWith("for")
                            && !line.startsWith("if") && !line.startsWith("while") && !line.startsWith("return")) {

                        // Alan adını ayıkla
                        String fieldName = line;
                        // Yorumları temizle
                        if (fieldName.contains("//")) {
                            fieldName = fieldName.substring(0, fieldName.indexOf("//"));
                        }

                        // Değişken adını al
                        int semicolonIndex = fieldName.indexOf(";");
                        if (semicolonIndex != -1) {  // Eğer ";" karakteri varsa
                            fieldName = fieldName.substring(0, semicolonIndex);

                            // Başlangıçtaki erişim belirleyicilerini veya diğer anahtar kelimeleri temizle
                            fieldName = fieldName.replaceAll("^(public|private|protected|static|final|abstract|transient|volatile)\\s+", "");

                            // Veri tipini ve değişken adını içeren kısmı al
                            String[] parts = fieldName.split("=")[0].trim().split("\\s+");
                            if (parts.length >= 2) {
                                // Veri tipi ve değişken adı bir arada göster
                                fieldsNode.add(new DefaultMutableTreeNode(fieldName.trim()));
                            } else if (parts.length == 1) {
                                // Tek kelimeli, muhtemelen veri tipi olmayan bir atama (int a=5; gibi)
                                fieldsNode.add(new DefaultMutableTreeNode(fieldName.trim()));
                            }
                        }
                    }
                }

                // Metod sonu kontrolü - süslü parantez seviyesi düştüyse ve 1'e eşitse
                // (yani sınıf seviyesine döndüysek)
                if (bracketLevel == 1 && inMethod) {
                    inMethod = false;
                }
            }

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

            // Örnek kod ekledikten sonra üyeleri güncelle
            Component comp = tabbedPane.getSelectedComponent();
            if (comp != null) {
                String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                updateMembersFromText(sampleCode, title);
            }
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
