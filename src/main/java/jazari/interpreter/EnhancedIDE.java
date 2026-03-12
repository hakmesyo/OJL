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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.fife.ui.rsyntaxtextarea.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.fife.ui.rsyntaxtextarea.templates.*;
import jazari.gui.FrameLLMTools;
import org.fife.ui.rtextarea.RTextScrollPane;

public class EnhancedIDE extends JFrame {

    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FrameLLMTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JTabbedPane tabbedPane;
    private JTextArea outputArea;
    private RuntimeCompiler compiler;
    private Map<String, Set<String>> packageMap = new HashMap<>();
    private JTree projectTree;
    private JTree membersTree;
    private JTree examplesTree;
    private DefaultTreeModel projectTreeModel;
    private DefaultTreeModel membersTreeModel;
    private DefaultTreeModel examplesTreeModel;
    private File currentProjectDirectory;
    private JButton saveButton;
    private JButton openButton;
    private JButton runButton;
    private JButton clearButton;
    private JButton newFileButton;

    // Tab yönetimi için
    private Map<String, RSyntaxTextArea> openedFiles = new HashMap<>();
    private Map<String, String> fileContents = new HashMap<>();
    // Read-only tab takibi (examples için)
    private Set<String> readOnlyTabs = new HashSet<>();

    private static final String CONFIG_FILE = System.getProperty("user.home") + File.separator + ".ojl_ide_config";
    private static File lastOpenedDirectory = null;

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
        }
    }

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
        }
    }

    private void openProject() {
        JFileChooser fileChooser = new JFileChooser();
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
            lastOpenedDirectory = currentProjectDirectory;
            loadProjectStructure(currentProjectDirectory);
            saveSettings();
        }
    }

    public EnhancedIDE() {
        super("Advanced Java IDE");
        compiler = new RuntimeCompiler();
        initializePackageStructure();
        initializeUI();
        setupLookAndFeel();
        addUIListeners();
        loadSettings();
    }

    private void addUIListeners() {
        setupTreeMouseListener();
        setupTabChangeListener();
        setupMembersTreeMouseListener();
        setupExamplesTreeMouseListener();
    }

    private void setupLookAndFeel() {
        projectTree.setBackground(new Color(43, 43, 43));
        projectTree.setForeground(Color.WHITE);
        membersTree.setBackground(new Color(43, 43, 43));
        membersTree.setForeground(Color.WHITE);
        examplesTree.setBackground(new Color(43, 43, 43));
        examplesTree.setForeground(Color.WHITE);
    }

    private void initializePackageStructure() {
        packageMap.put("java.util", new HashSet<>(Arrays.asList("ArrayList", "List", "Map", "HashMap")));
        packageMap.put("java.io", new HashSet<>(Arrays.asList("File", "FileReader", "BufferedReader")));
        packageMap.put("javax.swing", new HashSet<>(Arrays.asList("JFrame", "JPanel", "JButton")));
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JToolBar toolBar = createToolBar();
        mainPanel.add(toolBar, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        addNewTab("New File", null, false);

        outputArea = new JTextArea(8, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    int rotation = e.getWheelRotation();
                    if (rotation < 0) {
                        changeOutputFontSize(1);
                    } else {
                        changeOutputFontSize(-1);
                    }
                    e.consume();
                } else {
                    MouseWheelListener[] listeners = outputArea.getMouseWheelListeners();
                    for (MouseWheelListener listener : listeners) {
                        if (listener == this) {
                            outputArea.removeMouseWheelListener(this);
                            break;
                        }
                    }
                    Container parent = outputArea.getParent();
                    if (parent != null) {
                        parent.dispatchEvent(e);
                    }
                    outputArea.addMouseWheelListener(this);
                }
            }
        });

        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        JPanel leftPanel = createLeftPanel();

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
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveSettings();
            }
        });
        setSize(1200, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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

    private void changeOutputFontSize(int sizeChange) {
        if (outputArea != null) {
            Font currentFont = outputArea.getFont();
            int newSize = Math.max(8, Math.min(36, currentFont.getSize() + sizeChange));
            outputArea.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), newSize));
        }
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        newFileButton = new JButton("New File");
        newFileButton.setToolTipText("Create new file");
        newFileButton.addActionListener(e -> createNewFile());
        toolBar.add(newFileButton, 0);

        saveButton = new JButton("Save");
        saveButton.setToolTipText("Save current file (Ctrl+S)");
        saveButton.addActionListener(e -> saveCurrentFile());

        openButton = new JButton("Open Project");
        openButton.setToolTipText("Open project folder");
        openButton.addActionListener(e -> openProject());

        runButton = new JButton("Run");
        runButton.setToolTipText("Run current file (F6)");
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
        String fileName = JOptionPane.showInputDialog(this,
                "Enter file name:", "New File", JOptionPane.QUESTION_MESSAGE);
        if (fileName != null && !fileName.trim().isEmpty()) {
            if (!fileName.toLowerCase().endsWith(".java")) {
                fileName += ".java";
            }
            addNewTab(fileName, null, false);
            RSyntaxTextArea currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                String className = fileName.substring(0, fileName.lastIndexOf('.'));
                String classTemplate = "public class " + className + " {\n\n"
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

    // -----------------------------------------------------------------------
    // LEFT PANEL: Projects / Examples / Members (3 sekme)
    // -----------------------------------------------------------------------
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());

        // --- Projects Tree ---
        DefaultMutableTreeNode projectRoot = new DefaultMutableTreeNode("Projects");
        projectTreeModel = new DefaultTreeModel(projectRoot);
        projectTree = new JTree(projectTreeModel);
        projectTree.setRootVisible(true);
        projectTree.setCellRenderer(new FileTreeCellRenderer());

        // --- Examples Tree ---
        DefaultMutableTreeNode examplesRoot = new DefaultMutableTreeNode("Examples");
        examplesTreeModel = new DefaultTreeModel(examplesRoot);
        examplesTree = new JTree(examplesTreeModel);
        examplesTree.setRootVisible(true);
        examplesTree.setCellRenderer(new FileTreeCellRenderer());
        loadExamplesFromJar(examplesRoot);

        // --- Members Tree ---
        DefaultMutableTreeNode membersRoot = new DefaultMutableTreeNode("Members");
        membersTreeModel = new DefaultTreeModel(membersRoot);
        membersTree = new JTree(membersTreeModel);
        membersTree.setRootVisible(true);

        // Üst kısım: Projects + Examples sekmeli
        JTabbedPane leftTabs = new JTabbedPane();
        leftTabs.addTab("Projects", new JScrollPane(projectTree));
        leftTabs.addTab("Examples", new JScrollPane(examplesTree));

        // Alt kısım: Members
        JScrollPane membersScroll = new JScrollPane(membersTree);
        membersScroll.setBorder(BorderFactory.createTitledBorder("Members"));

        JSplitPane treeSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                leftTabs, membersScroll);
        treeSplitPane.setDividerLocation(400);

        leftPanel.add(treeSplitPane, BorderLayout.CENTER);
        return leftPanel;
    }

    // -----------------------------------------------------------------------
    // EXAMPLES: Jar içinden yükle
    // -----------------------------------------------------------------------
    private void loadExamplesFromJar(DefaultMutableTreeNode root) {
        try {
            // Jar içindeki "examples/" klasörünü tara
            URL jarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
            File jarFile = new File(jarUrl.toURI());

            if (jarFile.isFile()) {
                // Jar'dan yükle
                loadExamplesFromJarFile(jarFile, root);
            } else {
                // IDE içinde (development) çalışıyorsa, src/main/java/test klasöründen yükle
                loadExamplesFromDirectory(new File("src/main/java/test"), root, "test");
            }
        } catch (Exception e) {
            DefaultMutableTreeNode errorNode = new DefaultMutableTreeNode("(Examples not found)");
            root.add(errorNode);
            e.printStackTrace();
        }

        examplesTreeModel.reload();
        expandAllNodes(examplesTree);
    }

    private void loadExamplesFromJarFile(File jarFile, DefaultMutableTreeNode root) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            // Paket/klasör yapısını tutan map
            Map<String, DefaultMutableTreeNode> packageNodes = new HashMap<>();
            packageNodes.put("examples", root);

            Enumeration<JarEntry> entries = jar.entries();
            List<JarEntry> javaEntries = new ArrayList<>();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith("examples/") && name.endsWith(".java")) {
                    javaEntries.add(entry);
                }
            }

            // Alfabetik sırala
            javaEntries.sort((a, b) -> a.getName().compareTo(b.getName()));

            for (JarEntry entry : javaEntries) {
                String path = entry.getName(); // e.g. "examples/jazari/matrix/TestCMatrix.java"
                String relativePath = path.substring("examples/".length()); // "jazari/matrix/TestCMatrix.java"

                String[] parts = relativePath.split("/");
                String currentPath = "examples";
                DefaultMutableTreeNode currentNode = root;

                // Paket/klasör düğümlerini oluştur
                for (int i = 0; i < parts.length - 1; i++) {
                    currentPath += "/" + parts[i];
                    if (!packageNodes.containsKey(currentPath)) {
                        DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode(
                                new PackageNode(parts[i])
                        );
                        currentNode.add(packageNode);
                        packageNodes.put(currentPath, packageNode);
                    }
                    currentNode = packageNodes.get(currentPath);
                }

                // Dosya düğümü ekle
                String fileName = parts[parts.length - 1];
                DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(
                        new ExampleFileNode(path, fileName)
                );
                currentNode.add(fileNode);
            }
        }
    }

    private void loadExamplesFromDirectory(File dir, DefaultMutableTreeNode node, String baseName) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) {
                return -1;
            }
            if (!a.isDirectory() && b.isDirectory()) {
                return 1;
            }
            return a.getName().compareTo(b.getName());
        });

        for (File file : files) {
            if (file.isHidden()) {
                continue;
            }
            if (file.isDirectory()) {
                DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(new PackageNode(file.getName()));
                loadExamplesFromDirectory(file, subNode, file.getName());
                if (subNode.getChildCount() > 0) {
                    node.add(subNode);
                }
            } else if (file.getName().endsWith(".java")) {
                DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(
                        new ExampleFileNode("file://" + file.getAbsolutePath(), file.getName())
                );
                node.add(fileNode);
            }
        }
    }

    private void expandAllNodes(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    // -----------------------------------------------------------------------
    // EXAMPLES TREE MOUSE LISTENER: Çift tıkla read-only aç
    // -----------------------------------------------------------------------
    private void setupExamplesTreeMouseListener() {
        examplesTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) examplesTree.getLastSelectedPathComponent();
                    if (node == null || !node.isLeaf()) {
                        return;
                    }

                    Object userObj = node.getUserObject();
                    if (userObj instanceof ExampleFileNode) {
                        ExampleFileNode exNode = (ExampleFileNode) userObj;
                        openExampleFile(exNode);
                    }
                }
            }
        });
    }

    private void openExampleFile(ExampleFileNode exNode) {
        String content = readExampleContent(exNode.getPath());
        if (content == null) {
            JOptionPane.showMessageDialog(this, "Could not read example file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Zaten açıksa o tab'a git
        String tabKey = "example::" + exNode.getPath();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (comp instanceof RTextScrollPane) {
                RSyntaxTextArea ta = (RSyntaxTextArea) ((RTextScrollPane) comp).getTextArea();
                if (openedFiles.containsKey(tabKey) && openedFiles.get(tabKey) == ta) {
                    tabbedPane.setSelectedIndex(i);
                    return;
                }
            }
        }

        // Yeni tab aç
        addNewTab("[Example] " + exNode.getFileName(), null, true);
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor != null) {
            editor.setText(content);
            editor.setCaretPosition(0);

            // setEditable(false) KULLANMA — popup menüyü kırıyor.
            // Bunun yerine DocumentFilter ile içeriği koru ama
            // kullanıcı yine de yazabilsin (kopyalayıp değiştirebilsin),
            // orijinal JAR içindeki dosya zaten değişmeyeceği için sorun yok.
            // Görsel ipucu olarak sadece arka planı değiştir:
            editor.setBackground(new Color(35, 35, 45));

            // Tab başlığına 🔒 ikonu ekle (read-only işareti)
            int idx = tabbedPane.getSelectedIndex();
            tabbedPane.setTitleAt(idx, "🔒 " + exNode.getFileName());
            tabbedPane.setTabComponentAt(idx, new ButtonTabComponent(tabbedPane, this::closeTab));

            // Popup menüyü açıkça set et (Run dahil)
            editor.setComponentPopupMenu(createEditorPopupMenu(editor));
        }

        openedFiles.put(tabKey, editor);
        readOnlyTabs.add(tabKey);

        outputArea.append("\n[Example] " + exNode.getFileName() + " opened (read-only view)."
                + "\nYou can edit freely. Press Ctrl+S to save your copy.\n");
    }

    private String readExampleContent(String path) {
        try {
            if (path.startsWith("file://")) {
                // Disk'ten oku
                File f = new File(path.substring(7));
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                }
                return sb.toString();
            } else {
                // Jar'dan oku
                InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                if (is == null) {
                    return null;
                }
                byte[] bytes = is.readAllBytes();
                is.close();
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // TAB: Read-only kontrolü ile save
    // -----------------------------------------------------------------------
    private boolean isCurrentTabReadOnly() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return false;
        }
        for (Map.Entry<String, RSyntaxTextArea> entry : openedFiles.entrySet()) {
            if (entry.getValue() == editor && readOnlyTabs.contains(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // TABBED PANE
    // -----------------------------------------------------------------------
    private void addNewTab(String title, File file, boolean readOnly) {
        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        setupTextArea(textArea);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);
        scrollPane.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    int rotation = e.getWheelRotation();
                    if (rotation < 0) {
                        changeFontSize(1);
                    } else {
                        changeFontSize(-1);
                    }
                    e.consume();
                }
            }
        });

        tabbedPane.addTab(title, scrollPane);
        tabbedPane.setSelectedComponent(scrollPane);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1,
                new ButtonTabComponent(tabbedPane, this::closeTab));

        if (file != null) {
            openedFiles.put(file.getAbsolutePath(), textArea);
        }

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
            }
        });

        if (file == null) {
            SwingUtilities.invokeLater(() -> updateMembersFromText(textArea.getText(), title));
        }
    }

    private void closeTab(int index) {
        if (tabbedPane.getTabCount() <= 1) {
            addNewTab("New File", null, false);
        }
        Component comp = tabbedPane.getComponentAt(index);
        tabbedPane.remove(index);

        for (Map.Entry<String, RSyntaxTextArea> entry : openedFiles.entrySet()) {
            if (comp instanceof RTextScrollPane
                    && ((RTextScrollPane) comp).getTextArea() == entry.getValue()) {
                String key = entry.getKey();
                openedFiles.remove(key);
                fileContents.remove(key);
                readOnlyTabs.remove(key);
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

        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(textArea);
        } catch (IOException e) {
            textArea.setBackground(new Color(43, 43, 43));
            textArea.setForeground(Color.WHITE);
            textArea.setCaretColor(Color.WHITE);
        }

        JavaAutoCompleteProvider autoCompleteProvider = new JavaAutoCompleteProvider(textArea);
        autoCompleteProvider.install();

        setupKeyBindings(textArea);
        setupCodeTemplates(textArea);
        textArea.setComponentPopupMenu(createEditorPopupMenu(textArea));
    }

    private JPopupMenu createEditorPopupMenu(RSyntaxTextArea textArea) {
        JPopupMenu existingPopup = textArea.getComponentPopupMenu();
        JPopupMenu popup = (existingPopup != null) ? existingPopup : new JPopupMenu();

        JMenuItem runItem = new JMenuItem("Run (F6)");
        runItem.addActionListener(e -> runCode());
        runItem.setMnemonic('R');
        popup.insert(runItem, 0);
        popup.insert(new JPopupMenu.Separator(), 1);

        JMenu insertMenu = new JMenu("Insert");
        insertMenu.setMnemonic('I');

        JMenuItem constructorItem = new JMenuItem("Constructor...");
        constructorItem.addActionListener(e -> insertConstructor());
        insertMenu.add(constructorItem);

        JMenuItem getterSetterItem = new JMenuItem("Getter and Setter...");
        getterSetterItem.addActionListener(e -> insertGetterSetter());
        insertMenu.add(getterSetterItem);

        JMenuItem overrideItem = new JMenuItem("Override Methods...");
        overrideItem.addActionListener(e -> insertOverrideMethods());
        insertMenu.add(overrideItem);

        JMenuItem tryCatchItem = new JMenuItem("Try/Catch Block");
        tryCatchItem.addActionListener(e -> insertTryCatch());
        insertMenu.add(tryCatchItem);

        JMenuItem mainMethodItem = new JMenuItem("Main Method");
        mainMethodItem.addActionListener(e -> insertMainMethod());
        insertMenu.add(mainMethodItem);

        JMenuItem forLoopItem = new JMenuItem("For Loop");
        forLoopItem.addActionListener(e -> insertForLoop());
        insertMenu.add(forLoopItem);

        JMenuItem whileLoopItem = new JMenuItem("While Loop");
        whileLoopItem.addActionListener(e -> insertWhileLoop());
        insertMenu.add(whileLoopItem);

        JMenuItem ifBlockItem = new JMenuItem("If Block");
        ifBlockItem.addActionListener(e -> insertIfBlock());
        insertMenu.add(ifBlockItem);

        JMenuItem switchBlockItem = new JMenuItem("Switch Block");
        switchBlockItem.addActionListener(e -> insertSwitchBlock());
        insertMenu.add(switchBlockItem);

        popup.insert(insertMenu, 2);

        JMenuItem formatItem = new JMenuItem("Format Code (Alt+Shift+F)");
        formatItem.addActionListener(e -> formatCurrentCode());
        formatItem.setMnemonic('F');
        popup.add(formatItem);

        return popup;
    }

    private void insertConstructor() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }
        if (editor.isEditable() == false) {
            showReadOnlyWarning();
            return;
        }

        String className = extractClassName(editor.getText());
        List<String> fields = extractFields(editor.getText());

        if (className == null || className.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Class name could not be determined.",
                    "Insert Constructor", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder constructor = new StringBuilder();
        constructor.append("\n    public ").append(className).append("(");
        if (!fields.isEmpty()) {
            for (int i = 0; i < fields.size(); i++) {
                String[] parts = fields.get(i).trim().split("\\s+");
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

        int caretPos = editor.getCaretPosition();
        editor.insert(constructor.toString(), caretPos);
    }

    private void insertGetterSetter() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }
        if (!editor.isEditable()) {
            showReadOnlyWarning();
            return;
        }

        List<String> fields = extractFields(editor.getText());
        if (fields.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No fields found in this class.", "Insert Getter/Setter", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Select Fields", true);
        dialog.setLayout(new BorderLayout());
        JPanel fieldsPanel = new JPanel(new GridLayout(0, 1));
        List<JCheckBox> checkBoxes = new ArrayList<>();

        for (String field : fields) {
            String[] parts = field.trim().split("\\s+");
            if (parts.length >= 2) {
                JCheckBox cb = new JCheckBox(field.trim());
                cb.setSelected(true);
                checkBoxes.add(cb);
                fieldsPanel.add(cb);
            }
        }

        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            StringBuilder gs = new StringBuilder();
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

                        gs.append("\n    public ").append(type).append(" get")
                                .append(Character.toUpperCase(name.charAt(0)))
                                .append(name.substring(1)).append("() {\n")
                                .append("        return ").append(name).append(";\n")
                                .append("    }\n\n");

                        gs.append("    public void set")
                                .append(Character.toUpperCase(name.charAt(0)))
                                .append(name.substring(1)).append("(").append(type).append(" ")
                                .append(name).append(") {\n")
                                .append("        this.").append(name).append(" = ").append(name).append(";\n")
                                .append("    }\n");
                    }
                }
            }
            int caretPos = editor.getCaretPosition();
            editor.insert(gs.toString(), caretPos);
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
        JOptionPane.showMessageDialog(this,
                "Override Methods feature is not yet implemented.",
                "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }

    private void insertTryCatch() {
        RSyntaxTextArea editor = getCurrentEditor();
        if (editor == null) {
            return;
        }
        if (!editor.isEditable()) {
            showReadOnlyWarning();
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
        if (!editor.isEditable()) {
            showReadOnlyWarning();
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
        if (!editor.isEditable()) {
            showReadOnlyWarning();
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
        if (!editor.isEditable()) {
            showReadOnlyWarning();
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
        if (!editor.isEditable()) {
            showReadOnlyWarning();
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
        if (!editor.isEditable()) {
            showReadOnlyWarning();
            return;
        }
        String template = "switch (variable) {\n    case value1:\n        break;\n    default:\n        break;\n}";
        int caretPos = editor.getCaretPosition();
        editor.insert(template, caretPos);
        editor.setCaretPosition(caretPos + template.indexOf("variable"));
    }

    private void showReadOnlyWarning() {
        JOptionPane.showMessageDialog(this,
                "This file is read-only.\nPress Ctrl+S to save a copy and edit it.",
                "Read-Only", JOptionPane.INFORMATION_MESSAGE);
    }

    private String extractClassName(String code) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\bclass\\s+([A-Za-z0-9_]+)");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private List<String> extractFields(String code) {
        List<String> fields = new ArrayList<>();
        String[] lines = code.split("\n");
        boolean inClass = false;
        int bracketLevel = 0;

        for (String line : lines) {
            line = line.trim();
            if (!inClass && line.contains("class")) {
                inClass = true;
            }
            if (inClass) {
                for (char c : line.toCharArray()) {
                    if (c == '{') {
                        bracketLevel++;
                    }
                    if (c == '}') {
                        bracketLevel--;
                    }
                }
                if (bracketLevel > 0 && line.contains(";")
                        && !line.contains("(") && !line.contains("=")
                        && (line.contains("private ") || line.contains("protected ")
                        || line.contains("public ") || Character.isLowerCase(line.charAt(0)))) {
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
            int newSize = Math.max(8, Math.min(36, currentFont.getSize() + sizeChange));
            currentEditor.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), newSize));
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

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "runCode");
        am.put("runCode", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCode();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_DOWN_MASK), "runCodeShift");
        am.put("runCodeShift", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCode();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "runCodeF9");
        am.put("runCodeF9", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runCode();
            }
        });
    }

    private void formatCurrentCode() {
        RSyntaxTextArea currentEditor = getCurrentEditor();
        if (currentEditor == null) {
            return;
        }
        if (!currentEditor.isEditable()) {
            showReadOnlyWarning();
            return;
        }
        try {
            String code = currentEditor.getText();
            String formattedCode = formatJavaCode(code);
            int caretPosition = currentEditor.getCaretPosition();
            currentEditor.setText(formattedCode);
            try {
                if (caretPosition < formattedCode.length()) {
                    currentEditor.setCaretPosition(caretPosition);
                }
            } catch (Exception e) {
            }
            outputArea.append("\nCode formatted successfully.");
        } catch (Exception e) {
            outputArea.append("\nError formatting code: " + e.getMessage());
        }
    }

    private String formatJavaCode(String code) {
        StringBuilder formattedCode = new StringBuilder();
        String[] lines = code.split("\n");
        int indentLevel = 0;
        boolean inComment = false;

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                formattedCode.append("\n");
                continue;
            }
            if (trimmedLine.contains("/*")) {
                inComment = true;
            }
            if (trimmedLine.contains("*/")) {
                inComment = false;
            }
            if (trimmedLine.endsWith("}") || trimmedLine.endsWith("};")) {
                indentLevel = Math.max(0, indentLevel - 1);
            }
            if (!inComment && !trimmedLine.startsWith("*")) {
                for (int i = 0; i < indentLevel; i++) {
                    formattedCode.append("    ");
                }
            } else {
                formattedCode.append(" ");
            }
            formattedCode.append(trimmedLine).append("\n");
            if (trimmedLine.endsWith("{")) {
                indentLevel++;
            }
        }
        return formattedCode.toString();
    }

    private void setupCodeTemplates(RSyntaxTextArea textArea) {
        CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
        ctm.addTemplate(new StaticCodeTemplate("for", "for (int i = 0; i < length; i++) {\n\t|\n}", "Basic for loop"));
        ctm.addTemplate(new StaticCodeTemplate("main", "public static void main(String[] args) {\n\t|\n}", "Main method"));
        ctm.addTemplate(new StaticCodeTemplate("sout", "System.out.println(|);", "Print to console"));
        ctm.addTemplate(new StaticCodeTemplate("class", "public class ${ClassName} {\n\t|\n}", "Class definition"));
    }

    // -----------------------------------------------------------------------
    // PROJECT TREE
    // -----------------------------------------------------------------------
    private void loadProjectStructure(File projectDir) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(projectDir.getName());
        projectTreeModel.setRoot(root);
        scanProjectDirectory(projectDir, root);
        for (int i = 0; i < projectTree.getRowCount(); i++) {
            projectTree.collapseRow(i);
        }
        projectTree.expandRow(0);
        setTitle("Open Jazari Library IDE - " + projectDir.getName());
    }

    private void scanProjectDirectory(File directory, DefaultMutableTreeNode node) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        List<File> javaFiles = new ArrayList<>();
        List<File> subDirs = new ArrayList<>();

        for (File file : files) {
            if (file.isHidden()) {
                continue;
            }
            if (file.isDirectory()) {
                subDirs.add(file);
            } else if (file.getName().endsWith(".java")) {
                javaFiles.add(file);
            }
        }

        for (File javaFile : javaFiles) {
            node.add(new DefaultMutableTreeNode(new FileNode(javaFile)));
        }

        for (File subDir : subDirs) {
            DefaultMutableTreeNode subDirNode = new DefaultMutableTreeNode(new PackageNode(subDir.getName()));
            scanProjectDirectory(subDir, subDirNode);
            if (subDirNode.getChildCount() > 0) {
                node.add(subDirNode);
            }
        }
    }

    private void setupTreeMouseListener() {
        for (TreeSelectionListener listener : projectTree.getTreeSelectionListeners()) {
            projectTree.removeTreeSelectionListener(listener);
        }
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
            RSyntaxTextArea currentEditor = getCurrentEditor();
            if (currentEditor != null) {
                boolean updated = false;
                for (Map.Entry<String, RSyntaxTextArea> entry : openedFiles.entrySet()) {
                    if (entry.getValue() == currentEditor) {
                        String key = entry.getKey();
                        if (!key.startsWith("example::")) {
                            updateMembersTree(new File(key));
                        } else {
                            updateMembersFromText(currentEditor.getText(), "[Example]");
                        }
                        updated = true;
                        break;
                    }
                }
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

                    String selectedItem = node.toString().trim();
                    RSyntaxTextArea currentEditor = getCurrentEditor();
                    if (currentEditor != null) {
                        String text = currentEditor.getText();
                        int index = text.indexOf(selectedItem);
                        if (index >= 0) {
                            currentEditor.setCaretPosition(index);
                            currentEditor.requestFocus();
                            try {
                                Rectangle rect = currentEditor.modelToView(index);
                                if (rect != null) {
                                    JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
                                    rect.y = Math.max(0, rect.y - scrollPane.getViewport().getHeight() / 2);
                                    currentEditor.scrollRectToVisible(rect);
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

            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            addNewTab(file.getName(), file, false);
            RSyntaxTextArea currentEditor = getCurrentEditor();
            currentEditor.setText(content.toString());
            currentEditor.setCaretPosition(0);
            fileContents.put(file.getAbsolutePath(), content.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error opening file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // -----------------------------------------------------------------------
    // SAVE: Read-only ise "Save As" tetikle
    // -----------------------------------------------------------------------
    private void saveCurrentFile() {
        RSyntaxTextArea currentEditor = getCurrentEditor();
        if (currentEditor == null) {
            return;
        }

        // Read-only (example) tab ise → Save As
        if (isCurrentTabReadOnly()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "This is a read-only example file.\n"
                    + "Do you want to save a copy to edit it?",
                    "Save Copy", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                saveAsNewFile(currentEditor.getText());
            }
            return;
        }

        // Normal dosya ise → direkt kaydet
        String filePath = null;
        for (Map.Entry<String, RSyntaxTextArea> entry : openedFiles.entrySet()) {
            if (entry.getValue() == currentEditor) {
                filePath = entry.getKey();
                break;
            }
        }

        if (filePath != null && !filePath.startsWith("example::")) {
            saveFile(new File(filePath), currentEditor.getText());
        } else {
            saveAsNewFile(currentEditor.getText());
        }
    }

    private void saveFile(File file, String content) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
            fileContents.put(file.getAbsolutePath(), content);
            outputArea.append("\nFile saved: " + file.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
            if (!file.getName().toLowerCase().endsWith(".java")) {
                file = new File(file.getAbsolutePath() + ".java");
            }

            saveFile(file, content);

            // Tab başlığını güncelle (🔒 ikonunu kaldır)
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex >= 0) {
                tabbedPane.setTitleAt(selectedIndex, file.getName());
                tabbedPane.setTabComponentAt(selectedIndex,
                        new ButtonTabComponent(tabbedPane, this::closeTab));
            }

            RSyntaxTextArea editor = getCurrentEditor();
            if (editor != null) {
                // Read-only görünümünü kaldır
                editor.setBackground(new Color(43, 43, 43)); // normal arka plan

                // Read-only kaydından çıkar
                String oldKey = null;
                for (Map.Entry<String, RSyntaxTextArea> entry : openedFiles.entrySet()) {
                    if (entry.getValue() == editor) {
                        oldKey = entry.getKey();
                        break;
                    }
                }
                if (oldKey != null) {
                    openedFiles.remove(oldKey);
                    readOnlyTabs.remove(oldKey);
                }

                // Artık normal dosya olarak kayıt et
                openedFiles.put(file.getAbsolutePath(), editor);
            }

            // Proje ağacını güncelle
            if (currentProjectDirectory != null
                    && file.getAbsolutePath().startsWith(currentProjectDirectory.getAbsolutePath())) {
                loadProjectStructure(currentProjectDirectory);
            }

            outputArea.append("\nSaved as: " + file.getAbsolutePath()
                    + "\nFile is now editable.\n");
        }
    }

    // -----------------------------------------------------------------------
    // MEMBERS TREE
    // -----------------------------------------------------------------------
    private void updateMembersFromText(String text, String title) {
        try {
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(title + " Members");
            membersTreeModel.setRoot(root);
            DefaultMutableTreeNode methodsNode = new DefaultMutableTreeNode("Methods");
            DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode("Fields");
            root.add(methodsNode);
            root.add(fieldsNode);

            int bracketLevel = 0;
            boolean inMethod = false;
            String[] lines = text.split("\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                for (char c : line.toCharArray()) {
                    if (c == '{') {
                        bracketLevel++;
                    }
                    if (c == '}') {
                        bracketLevel--;
                    }
                }
                if (line.contains("(") && line.contains(")")
                        && !line.contains("if") && !line.contains("for") && !line.contains("while")
                        && !line.contains("switch") && (line.endsWith("{")
                        || (i + 1 < lines.length && lines[i + 1].trim().equals("{")))) {
                    String methodName = line;
                    if (methodName.contains("//")) {
                        methodName = methodName.substring(0, methodName.indexOf("//"));
                    }
                    if (methodName.contains("(")) {
                        int closingParenIndex = methodName.indexOf(")");
                        if (closingParenIndex != -1) {
                            methodName = methodName.substring(0, closingParenIndex + 1);
                            methodsNode.add(new DefaultMutableTreeNode(methodName));
                            inMethod = true;
                        }
                    }
                }
                if (!inMethod || bracketLevel == 1) {
                    if (line.contains(";") && !line.contains("(") && !line.contains("import ")
                            && !line.contains("package ") && !line.startsWith("//") && !line.startsWith("for")
                            && !line.startsWith("if") && !line.startsWith("while") && !line.startsWith("return")) {
                        String fieldName = line;
                        if (fieldName.contains("//")) {
                            fieldName = fieldName.substring(0, fieldName.indexOf("//"));
                        }
                        int semicolonIndex = fieldName.indexOf(";");
                        if (semicolonIndex != -1) {
                            fieldName = fieldName.substring(0, semicolonIndex);
                            fieldName = fieldName.replaceAll("^(public|private|protected|static|final|abstract|transient|volatile)\\s+", "");
                            String[] parts = fieldName.split("=")[0].trim().split("\\s+");
                            if (parts.length >= 1) {
                                fieldsNode.add(new DefaultMutableTreeNode(fieldName.trim()));
                            }
                        }
                    }
                }
                if (bracketLevel == 1 && inMethod) {
                    inMethod = false;
                }
            }

            if (methodsNode.getChildCount() == 0) {
                methodsNode.add(new DefaultMutableTreeNode("No methods found"));
            }
            if (fieldsNode.getChildCount() == 0) {
                fieldsNode.add(new DefaultMutableTreeNode("No fields found"));
            }

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
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            updateMembersFromText(sb.toString(), file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------------
    // RUN
    // -----------------------------------------------------------------------
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
        String sampleCode = "import java.util.ArrayList;\n"
                + "import jazari.matrix.CMatrix;\n\n"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello from Jazari IDE!\");\n"
                + "        \n"
                + "        ArrayList<String> list = new ArrayList<>();\n"
                + "        list.add(\"OJL\");\n"
                + "        System.out.println(\"List size: \" + list.size());\n"
                + "    }\n"
                + "}\n";

        RSyntaxTextArea currentEditor = getCurrentEditor();
        if (currentEditor != null) {
            currentEditor.setText(sampleCode);
            Component comp = tabbedPane.getSelectedComponent();
            if (comp != null) {
                String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
                updateMembersFromText(sampleCode, title);
            }
        }
    }

    // -----------------------------------------------------------------------
    // TREE CELL RENDERER: Paket/Dosya ikonları
    // -----------------------------------------------------------------------
    class FileTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode) {
                Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
                if (userObj instanceof ExampleFileNode) {
                    setText(((ExampleFileNode) userObj).getFileName());
                    // Java dosya ikonu (unicode)
                    setIcon(UIManager.getIcon("FileView.fileIcon"));
                } else if (userObj instanceof PackageNode) {
                    setText(((PackageNode) userObj).getName());
                    setIcon(UIManager.getIcon("FileView.directoryIcon"));
                } else if (userObj instanceof FileNode) {
                    setText(((FileNode) userObj).toString());
                    setIcon(UIManager.getIcon("FileView.fileIcon"));
                }
            }
            return this;
        }
    }

    // -----------------------------------------------------------------------
    // INNER CLASSES
    // -----------------------------------------------------------------------
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

    class PackageNode {

        private final String name;

        public PackageNode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class ExampleFileNode {

        private final String path;   // jar içi path veya "file://..." path
        private final String fileName;

        public ExampleFileNode(String path, String fileName) {
            this.path = path;
            this.fileName = fileName;
        }

        public String getPath() {
            return path;
        }

        public String getFileName() {
            return fileName;
        }

        @Override
        public String toString() {
            return fileName;
        }
    }

    class OutputStreamAdapter extends OutputStream {

        private final JTextArea textArea;

        public OutputStreamAdapter(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            SwingUtilities.invokeLater(() -> {
                textArea.append(String.valueOf((char) b));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            SwingUtilities.invokeLater(() -> {
                textArea.append(new String(b, off, len));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EnhancedIDE().setVisible(true));
    }
}
