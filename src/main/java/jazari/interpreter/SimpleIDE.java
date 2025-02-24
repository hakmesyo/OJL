/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.interpreter;

/**
 *
 * @author cezerilab
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.*;
import java.io.*;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.autocomplete.*;
import java.util.Arrays;
import java.util.List;
import org.fife.rsta.ac.LanguageSupportFactory;

public class SimpleIDE extends JFrame {

    private RSyntaxTextArea textArea;
    private JButton runButton;
    private JButton clearButton;
    private JTextArea outputArea;
    private RuntimeCompiler compiler;

    public SimpleIDE() {
        super("Simple Java IDE with Runtime Compiler");
        compiler = new RuntimeCompiler();
        initializeUI();
    }

    private void initializeUI() {
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Kod editörü alanı
        textArea = new RSyntaxTextArea(20, 60);
        setupTextArea();
        setupAutoComplete();
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);

        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setupButtons(buttonPanel);

        // Çıktı alanı
        outputArea = new JTextArea(8, 60);
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        // Splitter panel oluştur
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                scrollPane, outputScrollPane);
        splitPane.setResizeWeight(0.7);

        // Panelleri yerleştirme
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Frame ayarları
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Örnek kod ekle
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

        // Java tamamlama sağlayıcısını kur
        DefaultCompletionProvider javaProvider = createJavaCompletionProvider();
        AutoCompletion ac = new AutoCompletion(javaProvider);
        ac.setShowDescWindow(true);
        ac.setParameterAssistanceEnabled(true);
        ac.setAutoCompleteEnabled(true);
        ac.setAutoActivationEnabled(true);
        ac.setAutoActivationDelay(100);  // 100ms gecikme
        ac.install(textArea);

        // Java dil desteğini etkinleştir
        LanguageSupportFactory.get().register(textArea);
    }

    private DefaultCompletionProvider createJavaCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();

        // Java temel sınıfları için otomatik tamamlama
        addJavaCompletions(provider, "System", Arrays.asList(
                new String[]{"out", "err", "in", "currentTimeMillis()", "nanoTime()",
                    "gc()", "exit()", "getProperty()", "getenv()"}
        ));

        addJavaCompletions(provider, "System.out", Arrays.asList(
                new String[]{"println()", "print()", "printf()", "format()"}
        ));

        // String sınıfı için metodlar
        addJavaCompletions(provider, "String", Arrays.asList(
                new String[]{"length()", "substring()", "indexOf()", "lastIndexOf()",
                    "replace()", "trim()", "toLowerCase()", "toUpperCase()",
                    "split()", "concat()", "contains()", "isEmpty()"}
        ));

        // ArrayList için metodlar
        addJavaCompletions(provider, "ArrayList", Arrays.asList(
                new String[]{"add()", "remove()", "get()", "set()", "size()",
                    "clear()", "isEmpty()", "contains()", "indexOf()"}
        ));

        // Temel Java tipleri
        provider.addCompletion(new BasicCompletion(provider, "int"));
        provider.addCompletion(new BasicCompletion(provider, "double"));
        provider.addCompletion(new BasicCompletion(provider, "boolean"));
        provider.addCompletion(new BasicCompletion(provider, "char"));
        provider.addCompletion(new BasicCompletion(provider, "byte"));
        provider.addCompletion(new BasicCompletion(provider, "short"));
        provider.addCompletion(new BasicCompletion(provider, "long"));
        provider.addCompletion(new BasicCompletion(provider, "float"));

        return provider;
    }

    private void addJavaCompletions(DefaultCompletionProvider provider, String className,
            List<String> methods) {
        for (String method : methods) {
            provider.addCompletion(new BasicCompletion(provider,
                    className + "." + method,
                    null,
                    className + "." + method));
        }
    }

    private void setupAutoComplete() {
        // Provider oluştur
        DefaultCompletionProvider provider = new DefaultCompletionProvider();

        // Java temel sınıfları için tamamlamalar
        addJavaLangCompletions(provider);
        addJavaUtilCompletions(provider);
        addJavaIoCompletions(provider);

        // AutoCompletion'ı oluştur ve ayarla
        AutoCompletion ac = new AutoCompletion(provider);
        ac.setShowDescWindow(true);
        ac.setParameterAssistanceEnabled(true);
        ac.setAutoCompleteEnabled(true);
        ac.setAutoActivationEnabled(true);
        ac.setAutoActivationDelay(300);
        ac.install(textArea);
    }

    private void addJavaLangCompletions(DefaultCompletionProvider provider) {
        // System sınıfı için tamamlamalar
        provider.addCompletion(new BasicCompletion(provider, "System.out.println",
                "Prints a message to the console", "System.out.println();"));
        provider.addCompletion(new BasicCompletion(provider, "System.out.print",
                "Prints a message to the console without a newline", "System.out.print();"));

        // String metodları
        provider.addCompletion(new BasicCompletion(provider, "String",
                "String class", "String"));
        provider.addCompletion(new BasicCompletion(provider, "length()",
                "Returns the length of a string", "length()"));
        provider.addCompletion(new BasicCompletion(provider, "substring",
                "Returns a substring", "substring()"));

        // Temel veri tipleri
        provider.addCompletion(new BasicCompletion(provider, "int",
                "Integer primitive type", "int"));
        provider.addCompletion(new BasicCompletion(provider, "double",
                "Double primitive type", "double"));
        provider.addCompletion(new BasicCompletion(provider, "boolean",
                "Boolean primitive type", "boolean"));

        // Kontrol yapıları
        provider.addCompletion(new BasicCompletion(provider, "if",
                "If statement", "if () {\n    \n}"));
        provider.addCompletion(new BasicCompletion(provider, "for",
                "For loop", "for (int i = 0; i < ; i++) {\n    \n}"));
        provider.addCompletion(new BasicCompletion(provider, "while",
                "While loop", "while () {\n    \n}"));

        // Method ve sınıf tanımlamaları
        provider.addCompletion(new BasicCompletion(provider, "public",
                "Public access modifier", "public"));
        provider.addCompletion(new BasicCompletion(provider, "private",
                "Private access modifier", "private"));
        provider.addCompletion(new BasicCompletion(provider, "protected",
                "Protected access modifier", "protected"));
        provider.addCompletion(new BasicCompletion(provider, "class",
                "Class declaration", "class"));
    }

    private void addJavaUtilCompletions(DefaultCompletionProvider provider) {
        // Collections
        provider.addCompletion(new BasicCompletion(provider, "ArrayList",
                "Creates a new ArrayList", "ArrayList<>()"));
        provider.addCompletion(new BasicCompletion(provider, "HashMap",
                "Creates a new HashMap", "HashMap<,>()"));
        provider.addCompletion(new BasicCompletion(provider, "HashSet",
                "Creates a new HashSet", "HashSet<>()"));

        // Scanner
        provider.addCompletion(new BasicCompletion(provider, "Scanner",
                "Creates a new Scanner", "Scanner"));
    }

    private void addJavaIoCompletions(DefaultCompletionProvider provider) {
        // File operations
        provider.addCompletion(new BasicCompletion(provider, "File",
                "Creates a new File object", "File"));
        provider.addCompletion(new BasicCompletion(provider, "FileReader",
                "Creates a new FileReader", "FileReader"));
        provider.addCompletion(new BasicCompletion(provider, "FileWriter",
                "Creates a new FileWriter", "FileWriter"));
        provider.addCompletion(new BasicCompletion(provider, "BufferedReader",
                "Creates a new BufferedReader", "BufferedReader"));
        provider.addCompletion(new BasicCompletion(provider, "BufferedWriter",
                "Creates a new BufferedWriter", "BufferedWriter"));
    }

    private void setupButtons(JPanel buttonPanel) {
        runButton = new JButton("Run");
        runButton.setIcon(createImageIcon("/icons/run.png", "Run"));
        runButton.addActionListener(e -> runCode());

        clearButton = new JButton("Clear");
        clearButton.setIcon(createImageIcon("/icons/clear.png", "Clear"));
        clearButton.addActionListener(e -> {
            textArea.setText("");
            outputArea.setText("");
        });

        buttonPanel.add(runButton);
        buttonPanel.add(clearButton);
    }

    private ImageIcon createImageIcon(String path, String description) {
        // Bu metot ikon yükleme için kullanılabilir
        // Şu an için null dönüyoruz
        return null;
    }

    private void runCode() {
        String code = textArea.getText();
        outputArea.setText("Compiling and running code...\n");

        // Run button'u devre dışı bırak
        runButton.setEnabled(false);

        // Ayrı bir thread'de çalıştır
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
        String sampleCode = "public class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello from runtime compiler!\");\n"
                + "        \n"
                + "        // Basit bir hesaplama\n"
                + "        int sum = 0;\n"
                + "        for(int i = 1; i <= 10; i++) {\n"
                + "            sum += i;\n"
                + "        }\n"
                + "        System.out.println(\"Sum of numbers from 1 to 10: \" + sum);\n"
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
            new SimpleIDE().setVisible(true);
        });
    }
}