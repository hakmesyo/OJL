/*
 * FrameSourceCodeCollector.java
 * Modern LLM Source Code Collector with TreeView Selection
 * 
 * Features:
 * - Three collection modes: Structure Only, Full Project, Custom Selection
 * - Interactive file tree with tri-state checkboxes
 * - Real-time statistics (files, lines)
 * - Extension filtering
 * - Method signature extraction
 * - Asynchronous processing with progress indication
 * 
 * @author cezerilab
 */
package jazari.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

public class FrameSourceCodeCollector extends JFrame {

    // ==================== CONSTANTS ====================
    private static final Set<String> EXCLUDED_DIRS = Set.of(
            "build", "target", "dist", "out", ".git", ".idea", ".vscode", 
            "node_modules", "__pycache__", ".gradle", "bin", "obj", ".mvn"
    );

    private static final Set<String> CODE_EXTENSIONS = Set.of(
            "java", "py", "js", "ts", "jsx", "tsx", "cpp", "c", "cs", "go",
            "rb", "php", "swift", "kt", "rs", "scala", "groovy", "dart"
    );

    private static final Set<String> ALL_EXTENSIONS = Set.of(
            "java", "py", "js", "ts", "jsx", "tsx", "cpp", "c", "h", "hpp",
            "cs", "go", "rb", "php", "swift", "kt", "rs", "scala", "groovy",
            "dart", "lua", "pl", "sh", "bat", "ps1", "html", "css", "scss",
            "xml", "yaml", "yml", "json", "toml", "md", "txt", "sql", "gradle",
            "properties", "ini", "cfg", "conf"
    );

    private static final String[] QUICK_EXTENSIONS = {
            "java", "py", "js/ts", "c/cpp", "cs", "go", "rb", "php", "html/css", "json/xml"
    };
    
    // Preferences file
    private static final String PREFS_FILE = ".sourcecodecollector.prefs";

    // ==================== UI COMPONENTS ====================
    private JTextField projectPathField;
    private JTextField outputFileField;
    private JTree fileTree;
    private CheckBoxNode rootNode;
    private JTextArea logArea;
    private JLabel statsLabel;
    private JProgressBar progressBar;

    // Mode selection
    private JRadioButton rbStructureOnly;
    private JRadioButton rbFullProject;
    private JRadioButton rbCustomSelection;

    // Options
    private JCheckBox chkShowMethods;
    private JCheckBox chkIncludeTree;
    private JCheckBox chkIncludeStats;
    private JCheckBox chkPrependXRay;

    // Extension filter
    private JCheckBox chkAllFiles;
    private Map<String, JToggleButton> extensionChips = new LinkedHashMap<>();
    private JTextField customExtField;

    // Buttons
    private JButton btnBrowseProject;
    private JButton btnBrowseOutput;
    private JButton btnStart;
    private JButton btnOpenFile;
    private JButton btnOpenFolder;
    private JButton btnSelectAll;
    private JButton btnDeselectAll;
    private JButton btnExpandAll;
    private JButton btnCollapseAll;

    // State
    private Path currentProjectPath;
    private SwingWorker<Void, String> activeWorker;
    private Dimension screenSize;

    // ==================== STATIC INITIALIZATION ====================
    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FrameSourceCodeCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // ==================== CONSTRUCTOR ====================
    public FrameSourceCodeCollector() {
        initComponents();
        setupLayout();
        setupListeners();
        loadPreferences();
        updateExtensionChipsState();
        updateUIState();
    }

    // ==================== UI INITIALIZATION ====================
    private void initComponents() {
        setTitle("Source Code Collector for LLM");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Get screen dimensions and set window size dynamically
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        
        // Window width: 950px or 50% of screen width, whichever is larger
        int windowWidth = Math.max(950, screenWidth / 2);
        // Window height: screen height minus taskbar (approximately 30px)
        int windowHeight = screenHeight - 30;
        
        setPreferredSize(new Dimension(windowWidth, windowHeight));

        // Path fields
        projectPathField = new JTextField();
        projectPathField.setEditable(false);
        outputFileField = new JTextField();

        // Mode radio buttons
        rbStructureOnly = new JRadioButton("Structure Only (X-Ray)");
        rbStructureOnly.setToolTipText("Export only project structure with method signatures - no source code");
        rbFullProject = new JRadioButton("Full Project");
        rbFullProject.setToolTipText("Export structure + all matching source files");
        rbCustomSelection = new JRadioButton("Custom Selection");
        rbCustomSelection.setToolTipText("Manually select which files/folders to include");
        
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(rbStructureOnly);
        modeGroup.add(rbFullProject);
        modeGroup.add(rbCustomSelection);
        rbFullProject.setSelected(true);

        // Options
        chkShowMethods = new JCheckBox("Show Methods in Tree", true);
        chkShowMethods.setToolTipText("Display method signatures under each source file");
        chkIncludeTree = new JCheckBox("Include Tree in Output", true);
        chkIncludeTree.setToolTipText("Add directory structure to output file");
        chkIncludeStats = new JCheckBox("Include Statistics", true);
        chkIncludeStats.setToolTipText("Add file/line count statistics to output");
        
        chkPrependXRay = new JCheckBox("Prepend X-Ray", true);
        chkPrependXRay.setToolTipText("Add detailed project structure with method signatures before source code");

        // All Files checkbox
        chkAllFiles = new JCheckBox("All Files", true);
        chkAllFiles.setToolTipText("Include all supported file types");
        chkAllFiles.setFont(chkAllFiles.getFont().deriveFont(Font.BOLD));

        // Extension chips
        for (String ext : QUICK_EXTENSIONS) {
            JToggleButton chip = new JToggleButton(ext);
            chip.setFocusPainted(false);
            chip.setMargin(new Insets(2, 8, 2, 8));
            chip.setFont(chip.getFont().deriveFont(11f));
            // Default: java selected (will be overridden by preferences if available)
            if (ext.equals("java")) {
                chip.setSelected(true);
            }
            extensionChips.put(ext, chip);
        }
        customExtField = new JTextField(15);
        customExtField.setToolTipText("Additional extensions (comma separated, e.g.: vue,svelte,astro)");

        // Tree
        rootNode = new CheckBoxNode("No project loaded", null, false);
        fileTree = new JTree(new DefaultTreeModel(rootNode));
        fileTree.setRowHeight(22);
        fileTree.setCellRenderer(new CheckBoxTreeRenderer());
        fileTree.setRootVisible(true);
        fileTree.setShowsRootHandles(true);

        // Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setRows(6);

        // Stats and progress
        statsLabel = new JLabel("No project loaded");
        statsLabel.setFont(statsLabel.getFont().deriveFont(Font.BOLD));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        // Buttons
        btnBrowseProject = new JButton("Browse...");
        btnBrowseOutput = new JButton("Browse...");
        btnStart = new JButton("Start Collection");
        btnStart.setFont(btnStart.getFont().deriveFont(Font.BOLD));
        btnOpenFile = new JButton("Open Output");
        btnOpenFile.setEnabled(false);
        btnOpenFolder = new JButton("Open Folder");
        
        btnSelectAll = new JButton("Select All");
        btnDeselectAll = new JButton("Deselect All");
        btnExpandAll = new JButton("Expand");
        btnCollapseAll = new JButton("Collapse");
        
        // Set smaller font for tree control buttons
        Font smallFont = btnSelectAll.getFont().deriveFont(11f);
        btnSelectAll.setFont(smallFont);
        btnDeselectAll.setFont(smallFont);
        btnExpandAll.setFont(smallFont);
        btnCollapseAll.setFont(smallFont);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== TOP PANEL: Paths =====
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("Project Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        topPanel.add(new JLabel("Project:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        topPanel.add(projectPathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        topPanel.add(btnBrowseProject, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        topPanel.add(new JLabel("Output:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        topPanel.add(outputFileField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        topPanel.add(btnBrowseOutput, gbc);

        // ===== MODE PANEL =====
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        modePanel.setBorder(BorderFactory.createTitledBorder("Collection Mode"));
        modePanel.add(rbStructureOnly);
        modePanel.add(rbFullProject);
        modePanel.add(rbCustomSelection);

        // ===== EXTENSIONS PANEL =====
        JPanel extPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        extPanel.setBorder(BorderFactory.createTitledBorder("File Extensions"));
        extPanel.add(chkAllFiles);
        extPanel.add(new JSeparator(JSeparator.VERTICAL));
        extPanel.add(Box.createHorizontalStrut(5));
        for (JToggleButton chip : extensionChips.values()) {
            extPanel.add(chip);
        }
        extPanel.add(Box.createHorizontalStrut(10));
        extPanel.add(new JLabel("Custom:"));
        extPanel.add(customExtField);

        // ===== OPTIONS PANEL =====
        JPanel optPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 3));
        optPanel.add(chkShowMethods);
        optPanel.add(chkIncludeTree);
        optPanel.add(chkIncludeStats);
        optPanel.add(new JSeparator(JSeparator.VERTICAL));
        optPanel.add(chkPrependXRay);

        // ===== Combine top sections =====
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(topPanel);
        northPanel.add(modePanel);
        northPanel.add(extPanel);
        northPanel.add(optPanel);

        // ===== CENTER: Tree Panel =====
        JPanel treePanel = new JPanel(new BorderLayout(5, 5));
        treePanel.setBorder(BorderFactory.createTitledBorder("Project Files"));
        
        // Tree control buttons
        JPanel treeControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        treeControlPanel.add(btnSelectAll);
        treeControlPanel.add(btnDeselectAll);
        treeControlPanel.add(Box.createHorizontalStrut(15));
        treeControlPanel.add(btnExpandAll);
        treeControlPanel.add(btnCollapseAll);
        treeControlPanel.add(Box.createHorizontalGlue());
        treeControlPanel.add(statsLabel);
        
        JScrollPane treeScroll = new JScrollPane(fileTree);
        treeScroll.setPreferredSize(new Dimension(400, 350));
        
        treePanel.add(treeControlPanel, BorderLayout.NORTH);
        treePanel.add(treeScroll, BorderLayout.CENTER);

        // ===== BOTTOM: Log + Buttons =====
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBorder(BorderFactory.createTitledBorder("Log"));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        logPanel.add(progressBar, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.add(btnOpenFolder);
        buttonPanel.add(btnOpenFile);
        buttonPanel.add(btnStart);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(logPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        // ===== SPLIT PANE =====
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePanel, southPanel);
        splitPane.setResizeWeight(0.75);
        splitPane.setOneTouchExpandable(true);
        // Set divider location proportionally - tree gets most of the space
        int dividerLocation = (int) ((screenSize.height - 30 - 280) * 0.75); // 280px approximate header height
        splitPane.setDividerLocation(dividerLocation);

        add(northPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        pack();
        
        // Position window: centered horizontally, top of screen (y=0)
        setLocation((screenSize.width - getWidth()) / 2, 0);
    }

    private void setupListeners() {
        // Browse buttons
        btnBrowseProject.addActionListener(e -> browseProject());
        btnBrowseOutput.addActionListener(e -> browseOutput());
        
        // Action buttons
        btnStart.addActionListener(e -> startCollection());
        btnOpenFile.addActionListener(e -> openOutputFile());
        btnOpenFolder.addActionListener(e -> openProjectFolder());
        
        // Tree control buttons
        btnSelectAll.addActionListener(e -> setAllSelected(true));
        btnDeselectAll.addActionListener(e -> setAllSelected(false));
        btnExpandAll.addActionListener(e -> expandAllNodes());
        btnCollapseAll.addActionListener(e -> collapseAllNodes());
        
        // Mode radio buttons
        ActionListener modeListener = e -> updateUIState();
        rbStructureOnly.addActionListener(modeListener);
        rbFullProject.addActionListener(modeListener);
        rbCustomSelection.addActionListener(modeListener);
        
        // Extension chips - refresh tree when changed
        for (JToggleButton chip : extensionChips.values()) {
            chip.addActionListener(e -> {
                if (currentProjectPath != null) {
                    refreshTree();
                }
                savePreferences();
            });
        }
        
        // All Files checkbox
        chkAllFiles.addActionListener(e -> {
            updateExtensionChipsState();
            if (currentProjectPath != null) {
                refreshTree();
            }
            savePreferences();
        });
        
        // Custom extensions field
        customExtField.addActionListener(e -> {
            if (currentProjectPath != null) {
                refreshTree();
            }
            savePreferences();
        });
        
        // Options checkboxes - save preferences when changed
        chkShowMethods.addActionListener(e -> {
            if (currentProjectPath != null) {
                refreshTree();
            }
            savePreferences();
        });
        chkIncludeTree.addActionListener(e -> savePreferences());
        chkIncludeStats.addActionListener(e -> savePreferences());
        chkPrependXRay.addActionListener(e -> savePreferences());
        
        // Tree mouse listener for checkbox toggling
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!fileTree.isEnabled()) return;
                
                int row = fileTree.getRowForLocation(e.getX(), e.getY());
                if (row < 0) return;
                
                TreePath path = fileTree.getPathForRow(row);
                if (path == null) return;
                
                Rectangle rowBounds = fileTree.getRowBounds(row);
                if (rowBounds == null) return;
                
                // Check if click is in the checkbox area (first 20 pixels of the row)
                int checkboxWidth = 20;
                if (e.getX() < rowBounds.x + checkboxWidth + 5) {
                    Object lastComp = path.getLastPathComponent();
                    if (lastComp instanceof CheckBoxNode) {
                        CheckBoxNode node = (CheckBoxNode) lastComp;
                        toggleNodeSelection(node);
                        updateStats();
                        fileTree.repaint();
                    }
                }
            }
        });
        
        // Show methods checkbox
        chkShowMethods.addActionListener(e -> {
            if (currentProjectPath != null) {
                refreshTree();
            }
        });
    }

    // ==================== PROJECT LOADING ====================
    private void browseProject() {
        JFileChooser chooser = new JFileChooser(
                currentProjectPath != null ? currentProjectPath.toFile() : new File(".")
        );
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Project Directory");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentProjectPath = chooser.getSelectedFile().toPath();
            projectPathField.setText(currentProjectPath.toString());
            
            // Auto-set output file
            String defaultOutput = currentProjectPath.resolve("collected_source.txt").toString();
            outputFileField.setText(defaultOutput);
            
            // Load tree
            loadProjectTree();
            
            // Save preferences with new project path
            savePreferences();
        }
    }

    private void browseOutput() {
        JFileChooser chooser = new JFileChooser(
                currentProjectPath != null ? currentProjectPath.toFile() : new File(".")
        );
        chooser.setDialogTitle("Select Output File");
        chooser.setSelectedFile(new File("collected_source.txt"));
        
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".txt")) {
                path += ".txt";
            }
            outputFileField.setText(path);
        }
    }

    private void loadProjectTree() {
        if (currentProjectPath == null) return;
        
        logToUI("Loading project: " + currentProjectPath.getFileName());
        
        Set<String> selectedExtensions = getSelectedExtensions();
        rootNode = new CheckBoxNode(currentProjectPath.getFileName().toString(), currentProjectPath, true);
        
        populateTreeNodes(rootNode, currentProjectPath, selectedExtensions);
        
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        fileTree.setModel(model);
        
        // Expand first two levels
        expandToLevel(2);
        
        updateStats();
        updateUIState();
        
        logToUI("Project loaded successfully. " + countSelectedFiles(rootNode) + " files found.");
    }

    private void refreshTree() {
        if (currentProjectPath == null) return;
        
        // Store expansion state
        Set<String> expandedPaths = getExpandedPaths();
        
        // Reload
        loadProjectTree();
        
        // Restore expansion state
        restoreExpandedPaths(expandedPaths);
    }

    private void populateTreeNodes(CheckBoxNode parentNode, Path parentPath, Set<String> extensions) {
        try (Stream<Path> stream = Files.list(parentPath)) {
            List<Path> paths = stream
                    .filter(p -> !isExcluded(p))
                    .sorted((p1, p2) -> {
                        boolean isDir1 = Files.isDirectory(p1);
                        boolean isDir2 = Files.isDirectory(p2);
                        if (isDir1 != isDir2) return isDir1 ? -1 : 1;
                        return p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString());
                    })
                    .collect(Collectors.toList());
            
            for (Path path : paths) {
                String fileName = path.getFileName().toString();
                boolean isDir = Files.isDirectory(path);
                
                if (isDir) {
                    // Check if directory contains relevant files
                    if (containsRelevantFiles(path, extensions)) {
                        CheckBoxNode childNode = new CheckBoxNode(fileName, path, true);
                        parentNode.add(childNode);
                        populateTreeNodes(childNode, path, extensions);
                    }
                } else {
                    // Check extension
                    String ext = getFileExtension(path);
                    if (extensions.contains(ext.toLowerCase())) {
                        CheckBoxNode fileNode = new CheckBoxNode(fileName, path, true);
                        parentNode.add(fileNode);
                        
                        // Add method signatures if enabled
                        if (chkShowMethods.isSelected() && CODE_EXTENSIONS.contains(ext.toLowerCase())) {
                            List<String> methods = extractMethodSignatures(path);
                            for (String method : methods) {
                                CheckBoxNode methodNode = new CheckBoxNode(method, null, false);
                                methodNode.isMethod = true;
                                fileNode.add(methodNode);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logToUI("Error reading directory: " + parentPath);
        }
    }

    private boolean containsRelevantFiles(Path dir, Set<String> extensions) {
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(p -> !Files.isDirectory(p))
                    .filter(p -> !isExcluded(p))
                    .anyMatch(p -> extensions.contains(getFileExtension(p).toLowerCase()));
        } catch (IOException e) {
            return false;
        }
    }

    // ==================== TREE OPERATIONS ====================
    private void toggleNodeSelection(CheckBoxNode node) {
        if (node.isMethod) return; // Methods are not selectable
        
        SelectionState currentState = node.getSelectionState();
        boolean newSelected = (currentState != SelectionState.SELECTED);
        
        node.setSelected(newSelected);
        cascadeSelection(node, newSelected);
        updateParentStates((CheckBoxNode) node.getParent());
    }

    private void cascadeSelection(CheckBoxNode node, boolean selected) {
        for (int i = 0; i < node.getChildCount(); i++) {
            CheckBoxNode child = (CheckBoxNode) node.getChildAt(i);
            if (!child.isMethod) {
                child.setSelected(selected);
                if (child.getChildCount() > 0) {
                    cascadeSelection(child, selected);
                }
            }
        }
    }

    private void updateParentStates(CheckBoxNode parent) {
        if (parent == null) return;
        
        int selectedCount = 0;
        int selectableCount = 0;
        
        for (int i = 0; i < parent.getChildCount(); i++) {
            CheckBoxNode child = (CheckBoxNode) parent.getChildAt(i);
            if (!child.isMethod) {
                selectableCount++;
                SelectionState state = child.getSelectionState();
                if (state == SelectionState.SELECTED) {
                    selectedCount++;
                } else if (state == SelectionState.PARTIAL) {
                    selectedCount++; // Count partial as contributing
                }
            }
        }
        
        if (selectableCount == 0) return;
        
        if (selectedCount == 0) {
            parent.setSelected(false);
        } else if (selectedCount == selectableCount && allChildrenFullySelected(parent)) {
            parent.setSelected(true);
        } else {
            parent.selectionState = SelectionState.PARTIAL;
        }
        
        updateParentStates((CheckBoxNode) parent.getParent());
    }

    private boolean allChildrenFullySelected(CheckBoxNode node) {
        for (int i = 0; i < node.getChildCount(); i++) {
            CheckBoxNode child = (CheckBoxNode) node.getChildAt(i);
            if (!child.isMethod && child.getSelectionState() != SelectionState.SELECTED) {
                return false;
            }
        }
        return true;
    }

    private void setAllSelected(boolean selected) {
        if (rootNode != null) {
            rootNode.setSelected(selected);
            cascadeSelection(rootNode, selected);
            updateStats();
            fileTree.repaint();
        }
    }

    private void expandAllNodes() {
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            fileTree.expandRow(i);
        }
    }

    private void collapseAllNodes() {
        for (int i = fileTree.getRowCount() - 1; i >= 1; i--) {
            fileTree.collapseRow(i);
        }
    }

    private void expandToLevel(int level) {
        expandToLevel(new TreePath(rootNode), 0, level);
    }

    private void expandToLevel(TreePath path, int currentLevel, int maxLevel) {
        if (currentLevel >= maxLevel) return;
        
        fileTree.expandPath(path);
        CheckBoxNode node = (CheckBoxNode) path.getLastPathComponent();
        
        for (int i = 0; i < node.getChildCount(); i++) {
            CheckBoxNode child = (CheckBoxNode) node.getChildAt(i);
            if (!child.isMethod) {
                expandToLevel(path.pathByAddingChild(child), currentLevel + 1, maxLevel);
            }
        }
    }

    private Set<String> getExpandedPaths() {
        Set<String> expanded = new HashSet<>();
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            TreePath path = fileTree.getPathForRow(i);
            if (fileTree.isExpanded(path)) {
                expanded.add(pathToString(path));
            }
        }
        return expanded;
    }

    private void restoreExpandedPaths(Set<String> expandedPaths) {
        for (int i = 0; i < fileTree.getRowCount(); i++) {
            TreePath path = fileTree.getPathForRow(i);
            if (expandedPaths.contains(pathToString(path))) {
                fileTree.expandPath(path);
            }
        }
    }

    private String pathToString(TreePath path) {
        StringBuilder sb = new StringBuilder();
        for (Object node : path.getPath()) {
            sb.append("/").append(node.toString());
        }
        return sb.toString();
    }

    // ==================== COLLECTION LOGIC ====================
    private void startCollection() {
        String outputPath = outputFileField.getText().trim();
        if (outputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify an output file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (currentProjectPath == null) {
            JOptionPane.showMessageDialog(this, "Please select a project directory.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Disable UI
        setUIEnabled(false);
        logArea.setText("");
        progressBar.setValue(0);
        progressBar.setVisible(true);
        
        activeWorker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                boolean structureOnly = rbStructureOnly.isSelected();
                boolean fullProject = rbFullProject.isSelected();
                boolean customSelection = rbCustomSelection.isSelected();
                
                publish("Starting collection...");
                publish("Mode: " + (structureOnly ? "Structure Only" : (fullProject ? "Full Project" : "Custom Selection")));
                
                List<Path> filesToProcess = new ArrayList<>();
                
                if (structureOnly) {
                    // No files to process, just structure
                    publish("Collecting structure only (no source code).");
                } else if (fullProject) {
                    // Collect all files matching extensions
                    filesToProcess = collectAllFiles(currentProjectPath, getSelectedExtensions());
                    publish("Found " + filesToProcess.size() + " files to process.");
                } else if (customSelection) {
                    // Collect only selected files
                    filesToProcess = getSelectedFilePaths(rootNode);
                    publish("Processing " + filesToProcess.size() + " selected files.");
                }
                
                // Write output
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8))) {
                    
                    // Statistics
                    if (chkIncludeStats.isSelected()) {
                        publish("Generating statistics...");
                        String stats = generateStatistics(filesToProcess);
                        writer.write(stats);
                        writer.newLine();
                    }
                    
                    // X-Ray Structure (detailed tree with method signatures)
                    // For Structure Only mode: always include
                    // For Full/Custom mode: include if Prepend X-Ray is checked
                    boolean shouldIncludeXRay = structureOnly || chkPrependXRay.isSelected();
                    
                    if (shouldIncludeXRay) {
                        publish("Generating X-Ray structure...");
                        String xrayTree;
                        
                        if (customSelection && !filesToProcess.isEmpty()) {
                            // Custom Selection: X-Ray only for selected files
                            xrayTree = generateSelectedFilesTree(filesToProcess, currentProjectPath, true);
                        } else {
                            // Full Project or Structure Only: X-Ray entire project
                            xrayTree = generateDirectoryTree(currentProjectPath, true);
                        }
                        
                        writer.write("==================== X-RAY (PROJECT STRUCTURE) ====================\n");
                        writer.write(xrayTree);
                        writer.newLine();
                    } else if (chkIncludeTree.isSelected()) {
                        // Simple tree without method signatures (if X-Ray not selected but Include Tree is)
                        publish("Generating directory tree...");
                        String tree;
                        
                        if (customSelection && !filesToProcess.isEmpty()) {
                            tree = generateSelectedFilesTree(filesToProcess, currentProjectPath, chkShowMethods.isSelected());
                        } else {
                            tree = generateDirectoryTree(currentProjectPath, chkShowMethods.isSelected());
                        }
                        
                        writer.write("==================== PROJECT STRUCTURE ====================\n");
                        writer.write(tree);
                        writer.newLine();
                    }
                    
                    // Source files
                    if (!structureOnly && !filesToProcess.isEmpty()) {
                        publish("Writing source files...");
                        writer.write("==================== SOURCE CODE ====================\n\n");
                        
                        int total = filesToProcess.size();
                        for (int i = 0; i < total; i++) {
                            Path file = filesToProcess.get(i);
                            appendFileToWriter(file, writer, currentProjectPath);
                            
                            int progress = (int) (((i + 1.0) / total) * 100);
                            setProgress(progress);
                            
                            if ((i + 1) % 10 == 0 || i == total - 1) {
                                publish("Processed " + (i + 1) + "/" + total + " files");
                            }
                        }
                    }
                }
                
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    logToUI(msg);
                }
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    logToUI("Collection completed successfully!");
                    btnOpenFile.setEnabled(true);
                    showCompletionDialog(outputPath);
                } catch (Exception e) {
                    logToUI("ERROR: " + e.getMessage());
                    JOptionPane.showMessageDialog(FrameSourceCodeCollector.this,
                            "Error during collection: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setUIEnabled(true);
                    progressBar.setVisible(false);
                    activeWorker = null;
                }
            }
        };
        
        activeWorker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });
        
        activeWorker.execute();
    }

    private List<Path> collectAllFiles(Path root, Set<String> extensions) throws IOException {
        List<Path> files = new ArrayList<>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, java.nio.file.attribute.BasicFileAttributes attrs) {
                if (isExcluded(dir)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attrs) {
                String ext = getFileExtension(file);
                if (extensions.contains(ext.toLowerCase())) {
                    files.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return files;
    }

    private List<Path> getSelectedFilePaths(CheckBoxNode node) {
        List<Path> paths = new ArrayList<>();
        
        if (node.path != null && !Files.isDirectory(node.path)) {
            if (node.getSelectionState() == SelectionState.SELECTED) {
                paths.add(node.path);
            }
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            CheckBoxNode child = (CheckBoxNode) node.getChildAt(i);
            if (!child.isMethod) {
                paths.addAll(getSelectedFilePaths(child));
            }
        }
        
        return paths;
    }

    private void appendFileToWriter(Path file, BufferedWriter writer, Path projectRoot) throws IOException {
        String relativePath = projectRoot.relativize(file).toString().replace('\\', '/');
        
        writer.write("// ============================================================\n");
        writer.write("// FILE: " + relativePath + "\n");
        writer.write("// ============================================================\n");
        
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            writer.write("// ERROR: Could not read file - " + e.getMessage() + "\n");
        }
        
        writer.newLine();
    }

    // ==================== STATISTICS & TREE GENERATION ====================
    private String generateStatistics(List<Path> files) {
        AtomicLong totalLines = new AtomicLong(0);
        AtomicInteger methodCount = new AtomicInteger(0);
        
        for (Path file : files) {
            try {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                totalLines.addAndGet(lines.size());
                
                String content = String.join("\n", lines);
                methodCount.addAndGet(countMethods(content, getFileExtension(file)));
            } catch (IOException ignored) {}
        }
        
        boolean isMaven = Files.exists(currentProjectPath.resolve("pom.xml"));
        boolean isGradle = Files.exists(currentProjectPath.resolve("build.gradle"));
        
        String projectType = isMaven ? "Maven" : (isGradle ? "Gradle" : "Generic");
        
        StringBuilder sb = new StringBuilder();
        sb.append("==================== STATISTICS ====================\n");
        sb.append(String.format("%-25s %s\n", "Project Type:", projectType));
        sb.append(String.format("%-25s %,d\n", "Files Collected:", files.size()));
        sb.append(String.format("%-25s %,d\n", "Total Lines:", totalLines.get()));
        sb.append(String.format("%-25s %,d (estimated)\n", "Methods/Functions:", methodCount.get()));
        sb.append(String.format("%-25s %s\n", "Generated:", java.time.LocalDateTime.now().toString()));
        sb.append("====================================================\n");
        
        return sb.toString();
    }

    private String generateDirectoryTree(Path rootPath, boolean showMethods) {
        StringBuilder sb = new StringBuilder();
        sb.append(rootPath.getFileName().toString()).append("/\n");
        generateTreeRecursive(rootPath, "", sb, showMethods, getSelectedExtensions());
        return sb.toString();
    }

    private void generateTreeRecursive(Path dir, String prefix, StringBuilder sb, boolean showMethods, Set<String> extensions) {
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> paths = stream
                    .filter(p -> !isExcluded(p))
                    .filter(p -> Files.isDirectory(p) || extensions.contains(getFileExtension(p).toLowerCase()))
                    .sorted((p1, p2) -> {
                        boolean isDir1 = Files.isDirectory(p1);
                        boolean isDir2 = Files.isDirectory(p2);
                        if (isDir1 != isDir2) return isDir1 ? -1 : 1;
                        return p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString());
                    })
                    .collect(Collectors.toList());
            
            // Filter directories that don't contain relevant files
            paths = paths.stream()
                    .filter(p -> !Files.isDirectory(p) || containsRelevantFiles(p, extensions))
                    .collect(Collectors.toList());
            
            for (int i = 0; i < paths.size(); i++) {
                Path path = paths.get(i);
                boolean isLast = (i == paths.size() - 1);
                String connector = isLast ? "└── " : "├── ";
                String childPrefix = prefix + (isLast ? "    " : "│   ");
                
                sb.append(prefix).append(connector).append(path.getFileName());
                
                if (Files.isDirectory(path)) {
                    sb.append("/\n");
                    generateTreeRecursive(path, childPrefix, sb, showMethods, extensions);
                } else {
                    sb.append("\n");
                    
                    if (showMethods && CODE_EXTENSIONS.contains(getFileExtension(path).toLowerCase())) {
                        List<String> methods = extractMethodSignatures(path);
                        for (int j = 0; j < methods.size(); j++) {
                            boolean isLastMethod = (j == methods.size() - 1);
                            sb.append(childPrefix)
                              .append(isLastMethod ? "└── " : "├── ")
                              .append(methods.get(j))
                              .append("\n");
                        }
                    }
                }
            }
        } catch (IOException e) {
            sb.append(prefix).append("└── [Error: ").append(e.getMessage()).append("]\n");
        }
    }
    
    /**
     * Generate tree structure for only the selected files (Custom Selection mode)
     */
    private String generateSelectedFilesTree(List<Path> selectedFiles, Path projectRoot, boolean showMethods) {
        StringBuilder sb = new StringBuilder();
        sb.append(projectRoot.getFileName().toString()).append("/\n");
        
        // Build a tree structure from selected files
        // Group files by their parent directories
        Map<Path, List<Path>> filesByDirectory = new TreeMap<>();
        
        for (Path file : selectedFiles) {
            Path relativePath = projectRoot.relativize(file);
            Path parent = relativePath.getParent();
            if (parent == null) {
                parent = Paths.get("");
            }
            filesByDirectory.computeIfAbsent(parent, k -> new ArrayList<>()).add(file);
        }
        
        // Get all unique directory paths and sort them
        Set<Path> allDirs = new TreeSet<>();
        for (Path dir : filesByDirectory.keySet()) {
            Path current = dir;
            while (current != null && !current.toString().isEmpty()) {
                allDirs.add(current);
                current = current.getParent();
            }
        }
        
        // Build the tree recursively
        generateSelectedTreeRecursive(sb, projectRoot, Paths.get(""), "", allDirs, filesByDirectory, showMethods);
        
        return sb.toString();
    }
    
    /**
     * Recursively generate tree for selected files
     */
    private void generateSelectedTreeRecursive(StringBuilder sb, Path projectRoot, Path currentRelativePath, 
                                                String prefix, Set<Path> allDirs, 
                                                Map<Path, List<Path>> filesByDirectory, boolean showMethods) {
        // Get immediate children (directories and files) at this level
        List<Path> childDirs = new ArrayList<>();
        List<Path> childFiles = new ArrayList<>();
        
        // Find child directories
        for (Path dir : allDirs) {
            Path parent = dir.getParent();
            if (parent == null) parent = Paths.get("");
            
            if (parent.equals(currentRelativePath) && !dir.equals(currentRelativePath)) {
                childDirs.add(dir);
            }
        }
        
        // Find files in current directory
        List<Path> filesHere = filesByDirectory.get(currentRelativePath);
        if (filesHere != null) {
            childFiles.addAll(filesHere);
            childFiles.sort(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()));
        }
        
        // Sort directories
        childDirs.sort(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()));
        
        // Combine: directories first, then files
        List<Object[]> items = new ArrayList<>();
        for (Path dir : childDirs) {
            items.add(new Object[]{"dir", dir});
        }
        for (Path file : childFiles) {
            items.add(new Object[]{"file", file});
        }
        
        for (int i = 0; i < items.size(); i++) {
            Object[] item = items.get(i);
            boolean isLast = (i == items.size() - 1);
            String connector = isLast ? "└── " : "├── ";
            String childPrefix = prefix + (isLast ? "    " : "│   ");
            
            if ("dir".equals(item[0])) {
                Path dir = (Path) item[1];
                sb.append(prefix).append(connector).append(dir.getFileName()).append("/\n");
                generateSelectedTreeRecursive(sb, projectRoot, dir, childPrefix, allDirs, filesByDirectory, showMethods);
            } else {
                Path file = (Path) item[1];
                sb.append(prefix).append(connector).append(file.getFileName()).append("\n");
                
                // Add method signatures if enabled
                if (showMethods && CODE_EXTENSIONS.contains(getFileExtension(file).toLowerCase())) {
                    List<String> methods = extractMethodSignatures(file);
                    for (int j = 0; j < methods.size(); j++) {
                        boolean isLastMethod = (j == methods.size() - 1);
                        sb.append(childPrefix)
                          .append(isLastMethod ? "└── " : "├── ")
                          .append(methods.get(j))
                          .append("\n");
                    }
                }
            }
        }
    }

    // ==================== METHOD EXTRACTION ====================
    private List<String> extractMethodSignatures(Path file) {
        List<String> methods = new ArrayList<>();
        
        try {
            if (Files.size(file) > 500_000) return methods; // Skip large files
            
            String content = Files.readString(file, StandardCharsets.UTF_8);
            String ext = getFileExtension(file).toLowerCase();
            
            // Use specialized extraction for Java
            if (ext.equals("java")) {
                methods = extractJavaMethodSignatures(content);
            } else {
                Pattern pattern = getMethodPattern(ext);
                if (pattern != null) {
                    Matcher matcher = pattern.matcher(content);
                    while (matcher.find()) {
                        String sig = matcher.group(0).trim();
                        sig = cleanMethodSignature(sig);
                        if (isValidMethodSignature(sig, ext) && !methods.contains(sig)) {
                            if (sig.length() > 100) {
                                sig = sig.substring(0, 97) + "...";
                            }
                            methods.add(sig);
                        }
                    }
                }
            }
        } catch (IOException ignored) {}
        
        return methods;
    }
    
    /**
     * Extract Java method and constructor signatures using JavaParser for high accuracy
     */
    private List<String> extractJavaMethodSignatures(String content) {
        List<String> signatures = new ArrayList<>();
        
        try {
            var cu = StaticJavaParser.parse(content);
            
            // Extract constructors
            cu.findAll(ConstructorDeclaration.class).forEach(c -> {
                String sig = c.getDeclarationAsString(false, false, false);
                if (!signatures.contains(sig)) {
                    signatures.add(sig);
                }
            });
            
            // Extract methods
            cu.findAll(MethodDeclaration.class).forEach(m -> {
                String sig = m.getDeclarationAsString(false, false, false);
                if (!signatures.contains(sig)) {
                    signatures.add(sig);
                }
            });
            
        } catch (Exception e) {
            // Fallback to regex if JavaParser fails (e.g., incomplete/invalid Java file)
            return extractJavaMethodSignaturesRegex(content);
        }
        
        return signatures;
    }
    
    /**
     * Fallback regex-based Java method extraction (used when JavaParser fails)
     */
    private List<String> extractJavaMethodSignaturesRegex(String content) {
        List<String> methods = new ArrayList<>();
        
        // Remove comments first
        String cleanContent = removeJavaComments(content);
        
        // Pattern for method declarations (not calls)
        // Key: must have return type OR be a constructor, and must end with { or ;
        Pattern methodPattern = Pattern.compile(
            "^[ \\t]*" +
            "(?:@\\w+(?:\\([^)]*\\))?\\s*)*" +                    // Optional annotations
            "((?:public|protected|private|static|final|abstract|synchronized|native|strictfp|default)\\s+)+" + // At least one modifier required
            "(?:<[^>]+>\\s*)?" +                                  // Optional generics
            "([\\w<>\\[\\],\\.]+)\\s+" +                          // Return type
            "([a-zA-Z_][\\w]*)\\s*" +                             // Method name
            "\\(([^)]*)\\)\\s*" +                                 // Parameters
            "(?:throws\\s+[\\w,\\s\\.]+)?\\s*" +                  // Optional throws
            "[{;]",                                                // Opening brace or semicolon
            Pattern.MULTILINE
        );
        
        Matcher matcher = methodPattern.matcher(cleanContent);
        while (matcher.find()) {
            String modifiers = matcher.group(1).trim();
            String returnType = matcher.group(2).trim();
            String methodName = matcher.group(3);
            String params = matcher.group(4).trim();
            
            // Skip if method name is a keyword
            if (isJavaKeyword(methodName)) continue;
            
            String sig = modifiers + " " + returnType + " " + methodName + "(" + params + ")";
            sig = sig.replaceAll("\\s+", " ").trim();
            
            if (!methods.contains(sig)) {
                methods.add(sig);
            }
        }
        
        return methods;
    }
    
    /**
     * Remove Java comments from source code
     */
    private String removeJavaComments(String content) {
        // Remove single-line comments
        content = content.replaceAll("//[^\\n]*", "");
        // Remove multi-line comments
        content = content.replaceAll("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/", "");
        return content;
    }
    
    /**
     * Clean and simplify parameter list
     */
    private String cleanParameters(String params) {
        if (params == null || params.trim().isEmpty()) {
            return "";
        }
        
        // Simplify parameter list - just keep type and name
        params = params.trim();
        
        // Remove annotations from parameters
        params = params.replaceAll("@\\w+(?:\\([^)]*\\))?\\s*", "");
        
        // Clean up whitespace
        params = params.replaceAll("\\s+", " ");
        
        // If too long, truncate
        if (params.length() > 60) {
            params = params.substring(0, 57) + "...";
        }
        
        return params;
    }
    
    /**
     * Check if a word is a Java keyword (not a valid method name)
     */
    private boolean isJavaKeyword(String word) {
        Set<String> keywords = Set.of(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new", "package",
            "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient",
            "try", "void", "volatile", "while", "true", "false", "null"
        );
        return keywords.contains(word);
    }
    
    /**
     * Validate if extracted signature looks like a real method
     */
    private boolean isValidMethodSignature(String sig, String ext) {
        if (sig == null || sig.length() < 5) return false;
        
        // Must contain parentheses
        if (!sig.contains("(")) return false;
        
        // Should not start with control flow keywords
        String[] invalidStarts = {"if ", "if(", "else ", "while ", "while(", "for ", "for(",
                                  "switch ", "switch(", "catch ", "catch(", "return ", "throw "};
        String sigLower = sig.toLowerCase();
        for (String invalid : invalidStarts) {
            if (sigLower.startsWith(invalid)) return false;
        }
        
        return true;
    }

    private Pattern getMethodPattern(String ext) {
        return switch (ext) {
            // Java is handled separately with extractJavaMethodSignatures
            case "java" -> null;
            
            case "cs" -> Pattern.compile(
                "^[ \\t]*(?:(?:public|protected|private|internal|static|virtual|override|abstract|sealed|async)\\s+)*" +
                "[\\w<>\\[\\],\\s\\.]+\\s+\\w+\\s*\\([^)]*\\)\\s*(?:where\\s+[^{]+)?\\s*[{;]",
                Pattern.MULTILINE
            );
            
            case "cpp", "c", "h", "hpp" -> Pattern.compile(
                "^[ \\t]*(?:(?:virtual|static|inline|explicit|const|override)\\s+)*" +
                "[\\w<>\\*&:\\s]+\\s+\\w+\\s*\\([^)]*\\)\\s*(?:const)?\\s*(?:override)?\\s*[{;]",
                Pattern.MULTILINE
            );
            
            case "py" -> Pattern.compile(
                "^[ \\t]*(?:async\\s+)?def\\s+([a-zA-Z_][\\w]*)\\s*\\([^)]*\\)\\s*(?:->\\s*[^:]+)?\\s*:",
                Pattern.MULTILINE
            );
            
            case "js", "ts", "jsx", "tsx" -> Pattern.compile(
                "^[ \\t]*(?:export\\s+)?(?:async\\s+)?(?:" +
                "function\\s+\\w+\\s*\\([^)]*\\)|" +                    // function declarations
                "(?:const|let|var)\\s+\\w+\\s*=\\s*(?:async\\s+)?(?:function\\s*)?\\([^)]*\\)\\s*=>|" +  // arrow functions
                "(?:public|private|protected)?\\s*(?:static)?\\s*(?:async\\s+)?\\w+\\s*\\([^)]*\\)\\s*[:{]" +  // class methods
                ")",
                Pattern.MULTILINE
            );
            
            case "go" -> Pattern.compile(
                "^func\\s+(?:\\([^)]+\\)\\s+)?\\w+\\s*\\([^)]*\\)\\s*(?:[^{]+)?\\s*\\{",
                Pattern.MULTILINE
            );
            
            case "rb" -> Pattern.compile(
                "^[ \\t]*def\\s+(?:self\\.)?\\w+[\\w!?]*(?:\\([^)]*\\))?",
                Pattern.MULTILINE
            );
            
            case "php" -> Pattern.compile(
                "^[ \\t]*(?:(?:public|private|protected|static|final|abstract)\\s+)*function\\s+\\w+\\s*\\([^)]*\\)",
                Pattern.MULTILINE
            );
            
            case "kt" -> Pattern.compile(
                "^[ \\t]*(?:(?:public|private|protected|internal|open|override|suspend|inline)\\s+)*fun\\s+(?:<[^>]+>\\s+)?\\w+\\s*\\([^)]*\\)",
                Pattern.MULTILINE
            );
            
            case "swift" -> Pattern.compile(
                "^[ \\t]*(?:(?:public|private|internal|fileprivate|open|static|class|override)\\s+)*func\\s+\\w+\\s*(?:<[^>]+>)?\\s*\\([^)]*\\)",
                Pattern.MULTILINE
            );
            
            case "rs" -> Pattern.compile(
                "^[ \\t]*(?:pub\\s+)?(?:async\\s+)?fn\\s+\\w+\\s*(?:<[^>]+>)?\\s*\\([^)]*\\)",
                Pattern.MULTILINE
            );
            
            default -> null;
        };
    }

    private String cleanMethodSignature(String sig) {
        sig = sig.replaceAll("\\s+", " ").trim();
        sig = sig.replaceAll("\\{\\s*$", "").trim();
        sig = sig.replaceAll(";\\s*$", "").trim();
        sig = sig.replaceAll(":\\s*$", "").trim();  // For Python
        return sig;
    }

    private int countMethods(String content, String ext) {
        Pattern pattern = getMethodPattern(ext);
        if (pattern == null) return 0;
        
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) count++;
        return count;
    }

    // ==================== UTILITY METHODS ====================
    private Set<String> getSelectedExtensions() {
        Set<String> extensions = new HashSet<>();
        
        // If "All Files" is selected, return all supported extensions
        if (chkAllFiles.isSelected()) {
            extensions.addAll(ALL_EXTENSIONS);
            return extensions;
        }
        
        // Otherwise, use selected chips
        for (Map.Entry<String, JToggleButton> entry : extensionChips.entrySet()) {
            if (entry.getValue().isSelected()) {
                String ext = entry.getKey();
                if (ext.contains("/")) {
                    for (String e : ext.split("/")) {
                        extensions.add(e.trim().toLowerCase());
                    }
                } else {
                    extensions.add(ext.toLowerCase());
                }
            }
        }
        
        // Add custom extensions
        String customText = customExtField.getText().trim();
        if (!customText.isEmpty()) {
            for (String ext : customText.split("[,;\\s]+")) {
                ext = ext.trim().replaceAll("^\\*?\\.", "").toLowerCase();
                if (!ext.isEmpty()) {
                    extensions.add(ext);
                }
            }
        }
        
        return extensions;
    }

    private boolean isExcluded(Path path) {
        String name = path.getFileName().toString();
        if (name.startsWith(".")) return true;
        if (EXCLUDED_DIRS.contains(name.toLowerCase())) return true;
        
        try {
            return Files.isHidden(path);
        } catch (IOException e) {
            return false;
        }
    }

    private String getFileExtension(Path path) {
        String name = path.getFileName().toString();
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(idx + 1) : "";
    }

    private int countSelectedFiles(CheckBoxNode node) {
        int count = 0;
        
        if (node.path != null && !Files.isDirectory(node.path)) {
            if (node.getSelectionState() == SelectionState.SELECTED) {
                count++;
            }
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            CheckBoxNode child = (CheckBoxNode) node.getChildAt(i);
            if (!child.isMethod) {
                count += countSelectedFiles(child);
            }
        }
        
        return count;
    }

    private void updateStats() {
        if (rootNode == null || rootNode.path == null) {
            statsLabel.setText("No project loaded");
            return;
        }
        
        int selectedCount = countSelectedFiles(rootNode);
        statsLabel.setText("Selected: " + selectedCount + " files");
    }

    private void updateUIState() {
        boolean customMode = rbCustomSelection.isSelected();
        
        fileTree.setEnabled(customMode);
        btnSelectAll.setEnabled(customMode);
        btnDeselectAll.setEnabled(customMode);
        
        fileTree.repaint();
    }

    private void setUIEnabled(boolean enabled) {
        btnBrowseProject.setEnabled(enabled);
        btnBrowseOutput.setEnabled(enabled);
        btnStart.setEnabled(enabled);
        btnOpenFolder.setEnabled(enabled);
        rbStructureOnly.setEnabled(enabled);
        rbFullProject.setEnabled(enabled);
        rbCustomSelection.setEnabled(enabled);
        chkShowMethods.setEnabled(enabled);
        chkIncludeTree.setEnabled(enabled);
        chkIncludeStats.setEnabled(enabled);
        
        for (JToggleButton chip : extensionChips.values()) {
            chip.setEnabled(enabled);
        }
        customExtField.setEnabled(enabled);
        
        if (enabled) {
            updateUIState();
        } else {
            fileTree.setEnabled(false);
            btnSelectAll.setEnabled(false);
            btnDeselectAll.setEnabled(false);
        }
    }

    private void logToUI(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void openOutputFile() {
        String path = outputFileField.getText().trim();
        if (path.isEmpty()) return;
        
        File file = new File(path);
        if (file.exists()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Cannot open file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openProjectFolder() {
        if (currentProjectPath != null && Files.exists(currentProjectPath)) {
            try {
                Desktop.getDesktop().open(currentProjectPath.toFile());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Cannot open folder: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showCompletionDialog(String outputPath) {
        int result = JOptionPane.showConfirmDialog(this,
                "Collection completed successfully!\nOpen the output file now?",
                "Success", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            openOutputFile();
        }
    }

    // ==================== INNER CLASSES ====================
    
    /**
     * Selection state for tri-state checkbox
     */
    enum SelectionState {
        SELECTED, UNSELECTED, PARTIAL
    }

    /**
     * Tree node with checkbox support
     */
    static class CheckBoxNode extends DefaultMutableTreeNode {
        Path path;
        boolean isMethod = false;
        private boolean selected;
        SelectionState selectionState = SelectionState.UNSELECTED;

        public CheckBoxNode(String text, Path path, boolean selected) {
            super(text);
            this.path = path;
            this.selected = selected;
            this.selectionState = selected ? SelectionState.SELECTED : SelectionState.UNSELECTED;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            this.selectionState = selected ? SelectionState.SELECTED : SelectionState.UNSELECTED;
        }

        public SelectionState getSelectionState() {
            return selectionState;
        }
    }

    /**
     * Custom tree cell renderer with tri-state checkbox
     */
    class CheckBoxTreeRenderer extends JPanel implements TreeCellRenderer {
        private final JCheckBox checkBox;
        private final JLabel label;
        private final Icon folderIcon;
        private final Icon fileIcon;
        private final Icon methodIcon;

        public CheckBoxTreeRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
            setOpaque(false);

            checkBox = new JCheckBox();
            checkBox.setOpaque(false);

            label = new JLabel();
            label.setOpaque(false);

            add(checkBox);
            add(label);

            folderIcon = UIManager.getIcon("FileView.directoryIcon");
            fileIcon = UIManager.getIcon("FileView.fileIcon");
            methodIcon = UIManager.getIcon("Tree.leafIcon");
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
                                                       boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value instanceof CheckBoxNode) {
                CheckBoxNode node = (CheckBoxNode) value;

                // Configure checkbox
                if (node.isMethod) {
                    checkBox.setVisible(false);
                } else {
                    checkBox.setVisible(true);
                    checkBox.setEnabled(tree.isEnabled());
                    
                    SelectionState state = node.getSelectionState();
                    if (state == SelectionState.PARTIAL) {
                        // Tri-state: show as selected but with different appearance
                        checkBox.setSelected(true);
                        checkBox.setForeground(Color.GRAY);
                    } else {
                        checkBox.setSelected(state == SelectionState.SELECTED);
                        checkBox.setForeground(tree.getForeground());
                    }
                }

                // Configure label
                label.setText(node.toString());
                
                if (node.isMethod) {
                    label.setIcon(methodIcon);
                    label.setFont(label.getFont().deriveFont(Font.ITALIC, 11f));
                    label.setForeground(new Color(150, 150, 150));
                } else if (node.path != null && Files.isDirectory(node.path)) {
                    label.setIcon(folderIcon);
                    label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
                    label.setForeground(tree.getForeground());
                } else {
                    label.setIcon(fileIcon);
                    label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
                    label.setForeground(tree.getForeground());
                }

                // Selection highlighting
                if (selected && !node.isMethod) {
                    setBackground(UIManager.getColor("Tree.selectionBackground"));
                    setOpaque(true);
                } else {
                    setOpaque(false);
                }
            }

            return this;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.height = Math.max(d.height, 22);
            return d;
        }
    }

    // ==================== PREFERENCES ====================
    
    /**
     * Update extension chips enabled state based on All Files checkbox
     */
    private void updateExtensionChipsState() {
        boolean allFiles = chkAllFiles.isSelected();
        for (JToggleButton chip : extensionChips.values()) {
            chip.setEnabled(!allFiles);
        }
        customExtField.setEnabled(!allFiles);
    }
    
    /**
     * Get preferences file path in user home directory
     */
    private Path getPreferencesPath() {
        return Paths.get(System.getProperty("user.home"), PREFS_FILE);
    }
    
    /**
     * Save user preferences to file
     */
    private void savePreferences() {
        try {
            Properties props = new Properties();
            
            // Last project path
            if (currentProjectPath != null) {
                props.setProperty("lastProjectPath", currentProjectPath.toString());
            }
            
            // Collection mode
            String mode = rbStructureOnly.isSelected() ? "structure" : 
                         (rbFullProject.isSelected() ? "full" : "custom");
            props.setProperty("collectionMode", mode);
            
            // All Files checkbox
            props.setProperty("allFiles", String.valueOf(chkAllFiles.isSelected()));
            
            // Extension chips
            StringBuilder extBuilder = new StringBuilder();
            for (Map.Entry<String, JToggleButton> entry : extensionChips.entrySet()) {
                if (entry.getValue().isSelected()) {
                    if (extBuilder.length() > 0) extBuilder.append(",");
                    extBuilder.append(entry.getKey());
                }
            }
            props.setProperty("selectedExtensions", extBuilder.toString());
            
            // Custom extensions
            props.setProperty("customExtensions", customExtField.getText().trim());
            
            // Options checkboxes
            props.setProperty("showMethods", String.valueOf(chkShowMethods.isSelected()));
            props.setProperty("includeTree", String.valueOf(chkIncludeTree.isSelected()));
            props.setProperty("includeStats", String.valueOf(chkIncludeStats.isSelected()));
            props.setProperty("prependXRay", String.valueOf(chkPrependXRay.isSelected()));
            
            // Save to file
            try (OutputStream out = Files.newOutputStream(getPreferencesPath())) {
                props.store(out, "Source Code Collector Preferences");
            }
            
        } catch (IOException e) {
            // Silently ignore - preferences are not critical
            System.err.println("Could not save preferences: " + e.getMessage());
        }
    }
    
    /**
     * Load user preferences from file
     */
    private void loadPreferences() {
        Path prefsPath = getPreferencesPath();
        if (!Files.exists(prefsPath)) {
            return; // Use defaults
        }
        
        try {
            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(prefsPath)) {
                props.load(in);
            }
            
            // Collection mode
            String mode = props.getProperty("collectionMode", "full");
            switch (mode) {
                case "structure" -> rbStructureOnly.setSelected(true);
                case "full" -> rbFullProject.setSelected(true);
                case "custom" -> rbCustomSelection.setSelected(true);
            }
            
            // All Files checkbox
            String allFiles = props.getProperty("allFiles", "true");
            chkAllFiles.setSelected(Boolean.parseBoolean(allFiles));
            
            // Extension chips
            String selectedExt = props.getProperty("selectedExtensions", "java");
            Set<String> selected = new HashSet<>(Arrays.asList(selectedExt.split(",")));
            for (Map.Entry<String, JToggleButton> entry : extensionChips.entrySet()) {
                entry.getValue().setSelected(selected.contains(entry.getKey()));
            }
            
            // Custom extensions
            String customExt = props.getProperty("customExtensions", "");
            customExtField.setText(customExt);
            
            // Options checkboxes
            chkShowMethods.setSelected(Boolean.parseBoolean(props.getProperty("showMethods", "true")));
            chkIncludeTree.setSelected(Boolean.parseBoolean(props.getProperty("includeTree", "true")));
            chkIncludeStats.setSelected(Boolean.parseBoolean(props.getProperty("includeStats", "true")));
            chkPrependXRay.setSelected(Boolean.parseBoolean(props.getProperty("prependXRay", "true")));
            
            // Last project path - load after other settings so tree uses correct extensions
            String lastProject = props.getProperty("lastProjectPath", "");
            if (!lastProject.isEmpty()) {
                Path projectPath = Paths.get(lastProject);
                if (Files.exists(projectPath) && Files.isDirectory(projectPath)) {
                    currentProjectPath = projectPath;
                    projectPathField.setText(currentProjectPath.toString());
                    
                    // Auto-set output file
                    String defaultOutput = currentProjectPath.resolve("collected_source.txt").toString();
                    outputFileField.setText(defaultOutput);
                    
                    // Load tree after a short delay to ensure UI is ready
                    SwingUtilities.invokeLater(this::loadProjectTree);
                }
            }
            
        } catch (IOException e) {
            // Silently ignore - use defaults
            System.err.println("Could not load preferences: " + e.getMessage());
        }
    }

    // ==================== MAIN ====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FrameSourceCodeCollector frame = new FrameSourceCodeCollector();
            frame.setVisible(true);
        });
    }
}