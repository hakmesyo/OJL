package jazari.interpreter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.*;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import javax.tools.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.java.JavaLanguageSupport;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rsyntaxtextarea.templates.*;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdvancedIDE extends JFrame {

    private RSyntaxTextArea textArea;
    private JButton runButton;
    private JButton clearButton;
    private JTextArea outputArea;
    private RuntimeCompiler compiler;
    private Map<String, Set<String>> packageMap = new HashMap<>();

    public AdvancedIDE() {
        super("Advanced Java IDE");
        compiler = new RuntimeCompiler();
        initializePackageStructure();  // YENİ EKLENEN
        initializeUI();
    }

    private void initializePackageStructure() {
        try {
            String classpath = System.getProperty("java.class.path");
            String[] paths = classpath.split(File.pathSeparator);

            for (String path : paths) {
                File file = new File(path);
                if (file.isDirectory()) {
                    scanDirectory(file, "", packageMap);
                } else if (path.endsWith(".jar")) {
                    scanJarFile(file, packageMap);
                }
            }

            // Debug için paket yapısını göster
            for (Map.Entry<String, Set<String>> entry : packageMap.entrySet()) {
                System.out.println("Package: " + entry.getKey());
                for (String className : entry.getValue()) {
                    System.out.println("  - " + className);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanDirectory(File directory, String packageName, Map<String, Set<String>> packageMap) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (file.isDirectory()) {
                    String newPackage = packageName.isEmpty() ? name : packageName + "." + name;
                    scanDirectory(file, newPackage, packageMap);
                } else if (name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6);
                    packageMap.computeIfAbsent(packageName, k -> new HashSet<>()).add(className);
                }
            }
        }
    }

    private void scanJarFile(File jarFile, Map<String, Set<String>> packageMap) {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    int lastDot = className.lastIndexOf('.');
                    if (lastDot > 0) {
                        String packageName = className.substring(0, lastDot);
                        String simpleClassName = className.substring(lastDot + 1);
                        packageMap.computeIfAbsent(packageName, k -> new HashSet<>()).add(simpleClassName);
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(AdvancedIDE.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private java.util.List<String> findPossibleImports(String className) {
        java.util.List<String> suggestions = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : packageMap.entrySet()) {
            if (entry.getValue().contains(className)) {
                suggestions.add(entry.getKey() + "." + className);
            }
        }

        for (String packageName : packageMap.keySet()) {
            if (packageName.endsWith(className)) {
                for (String subClassName : packageMap.get(packageName)) {
                    suggestions.add(packageName + "." + subClassName);
                }
            }
        }

        return suggestions;
    }

    private void insertImport(String importStr) {
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

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        textArea = new RSyntaxTextArea(20, 60);
        setupTextArea();
        setupCodeTemplates();
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setFoldIndicatorEnabled(true);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setupButtons(buttonPanel);

        outputArea = new JTextArea(8, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollPane, outputScrollPane);
        splitPane.setResizeWeight(0.7);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        addSampleCode();
    }

    private void setupTextArea() {
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

        // Özel popup menüsü için varsayılan menüyü devre dışı bırak
        textArea.setComponentPopupMenu(null);

        // Mouse Motion Listener - hover desteği için
        textArea.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int offset = textArea.viewToModel2D(e.getPoint());
                try {
                    int start = getWordStart(textArea, offset);
                    int end = getWordEnd(textArea, offset);
                    if (start != end) {
                        String word = textArea.getText(start, end - start);
                        if (word.length() > 0 && Character.isUpperCase(word.charAt(0))) {
                            java.util.List<String> suggestions = findPossibleImports(word);
                            if (!suggestions.isEmpty()) {
                                textArea.setToolTipText("Available imports: " + String.join(", ", suggestions));
                                return;
                            }
                        }
                    }
                    textArea.setToolTipText(null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Mouse listener - sağ tık menüsü için
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showSuggestionPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showSuggestionPopup(e);
                }
            }

            private void showSuggestionPopup(MouseEvent e) {
                try {
                    int offset = textArea.viewToModel2D(e.getPoint());
                    int start = getWordStart(textArea, offset);
                    int end = getWordEnd(textArea, offset);

                    if (start != end) {
                        String word = textArea.getText(start, end - start);
                        if (word.length() > 0 && Character.isUpperCase(word.charAt(0))) {
                            // Import önerileri menüsü
                            JPopupMenu popup = new JPopupMenu();
                            java.util.List<String> suggestions = findPossibleImports(word);

                            if (!suggestions.isEmpty()) {
                                for (String suggestion : suggestions) {
                                    JMenuItem importItem = new JMenuItem("Import " + suggestion);
                                    importItem.addActionListener(evt -> insertImport(suggestion));
                                    popup.add(importItem);
                                }
                                popup.addSeparator();
                            }

                            // Standart düzenleme menü öğeleri
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
        });

        // Key bindings ekle
        InputMap im = textArea.getInputMap();
        ActionMap am = textArea.getActionMap();

        // Ctrl+Space için manuel kod tamamlama
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_DOWN_MASK), "completeCode");
        am.put("completeCode", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int offset = textArea.getCaretPosition();
                    int start = getWordStart(textArea, offset);
                    int end = getWordEnd(textArea, offset);

                    if (start != end) {
                        String word = textArea.getText(start, end - start);
                        if (word.length() > 0) {
                            java.util.List<String> suggestions = findPossibleImports(word);
                            if (!suggestions.isEmpty()) {
                                JPopupMenu popup = new JPopupMenu();
                                for (String suggestion : suggestions) {
                                    JMenuItem item = new JMenuItem(suggestion);
                                    item.addActionListener(evt -> insertImport(suggestion));
                                    popup.add(item);
                                }

                                // Popup'ı caret pozisyonunda göster
                                try {
                                    Rectangle r = (Rectangle) textArea.modelToView2D(textArea.getCaretPosition());
                                    popup.show(textArea, (int) r.getX(), (int) (r.getY() + r.getHeight()));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }



    private void setupCodeTemplates() {
        CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();

        // For döngüsü
        ctm.addTemplate(new StaticCodeTemplate("for",
                "for (int i = 0; i < length; i++) {\n\t|\n}",
                "Basic for loop"));

        // Enhanced for
        ctm.addTemplate(new StaticCodeTemplate("fore",
                "for (${Type} ${elem} : ${collection}) {\n\t|\n}",
                "Enhanced for loop"));

        // While döngüsü
        ctm.addTemplate(new StaticCodeTemplate("while",
                "while (${condition}) {\n\t|\n}",
                "While loop"));

        // Do-while döngüsü
        ctm.addTemplate(new StaticCodeTemplate("do",
                "do {\n\t|\n} while (${condition});",
                "Do-while loop"));

        // If bloğu
        ctm.addTemplate(new StaticCodeTemplate("if",
                "if (${condition}) {\n\t|\n}",
                "If statement"));

        // If-else bloğu
        ctm.addTemplate(new StaticCodeTemplate("ife",
                "if (${condition}) {\n\t|\n} else {\n\t\n}",
                "If-else statement"));

        // Switch bloğu
        ctm.addTemplate(new StaticCodeTemplate("switch",
                "switch (${var}) {\n\tcase ${value}:\n\t\t|\n\t\tbreak;\n\tdefault:\n\t\tbreak;\n}",
                "Switch statement"));

        // Try-catch bloğu
        ctm.addTemplate(new StaticCodeTemplate("try",
                "try {\n\t|\n} catch (${Exception} e) {\n\te.printStackTrace();\n}",
                "Try-catch block"));
    }

    private void setupButtons(JPanel buttonPanel) {
        runButton = new JButton("Run");
        runButton.addActionListener(e -> runCode());

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            textArea.setText("");
            outputArea.setText("");
        });

        buttonPanel.add(runButton);
        buttonPanel.add(clearButton);
    }

    private void runCode() {
        String code = textArea.getText();
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
        String sampleCode = "import java.util.ArrayList;\n\n"
                + "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello from Advanced IDE!\");\n"
                + "        \n"
                + "        // CMatrix örneği (otomatik import önerisi gelecek)\n"
                + "        CMatrix cm = CMatrix.getInstance();\n"
                + "        \n"
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
        textArea.setText(sampleCode);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new AdvancedIDE().setVisible(true);
        });
    }
}

class RuntimeCompiler {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public void compile(String sourceCode, JTextArea outputArea) {
        try {
            String className = extractClassName(sourceCode);
            if (className == null) {
                throw new Exception("Could not find class name in source code");
            }

            File sourceFile = new File(TEMP_DIR + className + ".java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(sourceCode);
            }

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new Exception("No JavaCompiler available. Make sure you're using JDK, not JRE.");
            }

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            Iterable<? extends JavaFileObject> compilationUnits
                    = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));

            java.util.List<String> options = new ArrayList<>();
            options.add("-d");
            options.add(TEMP_DIR);

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    options,
                    null,
                    compilationUnits
            );

            boolean success = task.call();

            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                outputArea.append(String.format("Line %d: %s%n",
                        diagnostic.getLineNumber(),
                        diagnostic.getMessage(null)));
            }

            if (!success) {
                throw new Exception("Compilation failed");
            }

            URLClassLoader classLoader = URLClassLoader.newInstance(
                    new URL[]{new File(TEMP_DIR).toURI().toURL()});
            Class<?> loadedClass = Class.forName(className, true, classLoader);
            Method mainMethod = loadedClass.getMethod("main", String[].class);

            PrintStream originalOut = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream newOut = new PrintStream(baos);
            System.setOut(newOut);

            try {
                mainMethod.invoke(null, (Object) new String[]{});
            } finally {
                System.setOut(originalOut);
            }

            outputArea.append("\nOutput:\n" + baos.toString());

            sourceFile.delete();
            new File(TEMP_DIR + className + ".class").delete();

            classLoader.close();

        } catch (Exception e) {
            outputArea.append("\nExecution Error: " + e.getMessage());
            e.printStackTrace(new PrintStream(new OutputStreamAdapter(outputArea)));
        }
    }

    private String extractClassName(String sourceCode) {
        String[] lines = sourceCode.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("public class ")) {
                return line.substring(13, line.indexOf("{")).trim();
            }
        }
        return null;
    }
}

class OutputStreamAdapter extends OutputStream {

    private JTextArea textArea;
    private StringBuilder buffer;

    public OutputStreamAdapter(JTextArea textArea) {
        this.textArea = textArea;
        this.buffer = new StringBuilder();
    }

    @Override
    public void write(int b) {
        buffer.append((char) b);
        if (b == '\n') {
            SwingUtilities.invokeLater(() -> {
                textArea.append(buffer.toString());
                buffer.setLength(0);
            });
        }
    }

    @Override
    public void flush() {
        if (buffer.length() > 0) {
            SwingUtilities.invokeLater(() -> {
                textArea.append(buffer.toString());
                buffer.setLength(0);
            });
        }
    }
}
