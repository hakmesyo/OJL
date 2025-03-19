package jazari.interpreter;

import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Java kodu için otomatik tamamlama sağlayan yardımcı sınıf. Sınıfların tüm
 * public metodlarını ve alanlarını dinamik olarak listeler.
 */
public class JavaAutoCompleteProvider {

    private static final Logger LOGGER = Logger.getLogger(JavaAutoCompleteProvider.class.getName());

    // Sınıf üyelerini önbelleğe alma
    private static final Map<String, List<Completion>> CLASS_MEMBER_CACHE = new HashMap<>();

    // İçe aktarılan sınıfları ve paketleri takip etme
    private Set<String> importedClasses = new HashSet<>();
    private Set<String> importedPackages = new HashSet<>();

    // Sık kullanılan sınıflar ve paketleri 
    private static final Map<String, String> COMMON_CLASSES = new HashMap<>();

    static {
        // Yaygın Java sınıfları
        COMMON_CLASSES.put("String", "java.lang");
        COMMON_CLASSES.put("Object", "java.lang");
        COMMON_CLASSES.put("System", "java.lang");
        COMMON_CLASSES.put("Math", "java.lang");
        COMMON_CLASSES.put("Integer", "java.lang");
        COMMON_CLASSES.put("Boolean", "java.lang");
        COMMON_CLASSES.put("Double", "java.lang");
        COMMON_CLASSES.put("ArrayList", "java.util");
        COMMON_CLASSES.put("List", "java.util");
        COMMON_CLASSES.put("Map", "java.util");
        COMMON_CLASSES.put("HashMap", "java.util");
        COMMON_CLASSES.put("File", "java.io");
        COMMON_CLASSES.put("BufferedReader", "java.io");
        COMMON_CLASSES.put("BufferedWriter", "java.io");
        COMMON_CLASSES.put("JFrame", "javax.swing");
        COMMON_CLASSES.put("JPanel", "javax.swing");
        COMMON_CLASSES.put("JButton", "javax.swing");

        // Jazari kütüphanesi sınıfları
        COMMON_CLASSES.put("CMatrix", "jazari.matrix");
        COMMON_CLASSES.put("ImageProcess", "jazari.image_processing");
    }

    private RSyntaxTextArea textArea;
    private AutoCompletion autoCompletion;
    private DefaultCompletionProvider provider;

    // Tamamlayıcı paket adları - jazari paketlerinin kendi sınıfları için kullanılır
    private List<String> jazariPackages = Arrays.asList(
            "jazari.matrix", "jazari.image_processing", "jazari.deep_learning",
            "jazari.types", "jazari.utils", "jazari.gui"
    );

    /**
     * Bir metin alanı için otomatik tamamlama sağlayıcısı oluşturur
     *
     * @param textArea Tamamlama eklenecek metin alanı
     */
    public JavaAutoCompleteProvider(RSyntaxTextArea textArea) {
        this.textArea = textArea;
        this.provider = createCompletionProvider();
        this.autoCompletion = new AutoCompletion(provider);
        configureAutoCompletion();
    }

    /**
     * Otomatik tamamlama özelliğini metin alanına ekler
     */
    public void install() {
        // Tamamlama sistemini yükle
        autoCompletion.install(textArea);

        // java.lang paketini varsayılan olarak ekle
        importedPackages.add("java.lang");

        // Import ifadelerini izlemek için dokuman dinleyicisi ekle
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                analyzeImports();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                analyzeImports();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Düz metin için çağrılmaz
            }
        });

        // Nokta yazıldığında tamamlama listesini göstermek için key listener ekle
        textArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    try {
                        int caretPos = textArea.getCaretPosition();
                        int lineStart = textArea.getLineStartOffset(textArea.getCaretLineNumber());
                        String lineText = textArea.getText(lineStart, caretPos - lineStart);

                        // Son kelimeyi al (şablon kısaltması)
                        String lastWord = "";
                        int wordStart = caretPos;
                        for (int i = caretPos - 1; i >= lineStart; i--) {
                            char c = textArea.getText(i, 1).charAt(0);
                            if (!Character.isJavaIdentifierPart(c)) {
                                break;
                            }
                            wordStart = i;
                        }

                        if (wordStart < caretPos) {
                            lastWord = textArea.getText(wordStart, caretPos - wordStart);
                        }

                        boolean handled = false;

                        // Tüm şablonları kontrol et
                        switch (lastWord) {
                            case "sout":
                                String soutTemplate = "System.out.println();";
                                replaceTemplate(textArea, wordStart, caretPos, soutTemplate, soutTemplate.length() - 2);
                                handled = true;
                                break;

                            case "psvm":
                                String psvmTemplate = "public static void main(String[] args) {\n\t\n}";
                                replaceTemplate(textArea, wordStart, caretPos, psvmTemplate, psvmTemplate.indexOf("\t") + 1);
                                handled = true;
                                break;

                            case "fori":
                                String foriTemplate = "for (int i = 0; i < length; i++) {\n\t\n}";
                                replaceTemplate(textArea, wordStart, caretPos, foriTemplate, foriTemplate.indexOf("\t") + 1);
                                handled = true;
                                break;

                            case "foreach":
                                String foreachTemplate = "for (Type item : collection) {\n\t\n}";
                                replaceTemplate(textArea, wordStart, caretPos, foreachTemplate, foreachTemplate.indexOf("Type"));
                                handled = true;
                                break;

                            case "try":
                                String tryTemplate = "try {\n\t\n} catch (Exception e) {\n\te.printStackTrace();\n}";
                                replaceTemplate(textArea, wordStart, caretPos, tryTemplate, tryTemplate.indexOf("\t") + 1);
                                handled = true;
                                break;

                            case "ifn":
                                String ifnTemplate = "if (var == null) {\n\t\n}";
                                replaceTemplate(textArea, wordStart, caretPos, ifnTemplate, ifnTemplate.indexOf("var"));
                                handled = true;
                                break;

                            case "inn":
                                String innTemplate = "if (var != null) {\n\t\n}";
                                replaceTemplate(textArea, wordStart, caretPos, innTemplate, innTemplate.indexOf("var"));
                                handled = true;
                                break;

                            case "for":
                                String forTemplate = "for (int i = 0; i < length; i++) {\n\t\n}";
                                replaceTemplate(textArea, wordStart, caretPos, forTemplate, forTemplate.indexOf("length"));
                                handled = true;
                                break;

                            case "main":
                                String mainTemplate = "public static void main(String[] args) {\n\t\n}";
                                replaceTemplate(textArea, wordStart, caretPos, mainTemplate, mainTemplate.indexOf("\t") + 1);
                                handled = true;
                                break;

                            case "class":
                                String classTemplate = "public class ClassName {\n\t\n}";
                                replaceTemplate(textArea, wordStart, caretPos, classTemplate, classTemplate.indexOf("ClassName"));
                                handled = true;
                                break;
                        }

                        if (handled) {
                            e.consume(); // TAB tuşunun normal davranışını engelle
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                // Nokta tuşuna basıldığında
                if (e.getKeyChar() == '.') {
                    // Önce karakterin yazılmasını bekle
                    SwingUtilities.invokeLater(() -> {
                        // 100ms bekleme süresi
                        Timer timer = new Timer(100, event -> {
                            try {
                                // Noktadan önceki sınıf/değişken adını çıkar
                                String varName = extractVarName();
                                if (varName != null && !varName.isEmpty()) {
                                    // Doğrudan bu sınıfın üyelerini getirmek için classpath'te ara
                                    tryLoadClassDirect(varName);

                                    // Tamamlamayı tetikle
                                    autoCompletion.doCompletion();
                                }
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Tamamlama hatası", ex);
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();
                    });
                }
            }
        });
        // Temel Java anahtar kelimelerini yükle
        addBasicJavaCompletions();

        // İlk import analizini yap
        analyzeImports();
    }
// Şablon değiştirme yardımcı metodu

    private void replaceTemplate(RSyntaxTextArea textArea, int start, int end, String template, int caretOffset) {
        try {
            textArea.replaceRange(template, start, end);
            textArea.setCaretPosition(start + caretOffset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Temel Java paketleri - sınıf başında tanımlayın
    private static final String[] BASE_PACKAGES = {
        "java.lang.", "java.util.", "java.io.", "javax.swing.", "jazari.matrix.",
        "jazari.image_processing.", "jazari.types.", "jazari.utils."
    };

    /**
     * Import ifadelerini analiz eder ve kullanılabilir sınıfları saptar
     */
    private void analyzeImports() {
        try {
            String text = textArea.getText();

            // İmport listelerini temizle
            importedPackages.clear();
            importedPackages.add("java.lang"); // java.lang her zaman import edilmiştir

            // Jazari paketlerini varsayılan olarak ekle
            for (String basePackage : BASE_PACKAGES) {
                if (basePackage.startsWith("jazari.")) {
                    importedPackages.add(basePackage.substring(0, basePackage.length() - 1));
                }
            }

            // Import ifadelerini bul - sadece bilgi için
            Pattern importPattern = Pattern.compile("import\\s+([^;]+);");
            Matcher matcher = importPattern.matcher(text);

            while (matcher.find()) {
                String importStmt = matcher.group(1).trim();
                System.out.println("İmport tespit edildi: " + importStmt);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Import analizi sırasında hata", e);
        }
    }

    /**
     * Bir sınıfı yüklemeyi dener ve başarılı olursa önbelleğe alır
     */
    private void tryLoadAndCacheClass(String fullClassName) {
        try {
            Class<?> clazz = Class.forName(fullClassName);
            System.out.println("Sınıf başarıyla yüklendi: " + fullClassName);

            // İnstance ve statik üyelerin ikisini de önbelleğe al
            getClassMembers(fullClassName, false);
            getClassMembers(fullClassName, true);
        } catch (ClassNotFoundException e) {
            // Sessizce başarısızlığı geç
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Sınıf yüklenirken hata: " + fullClassName, e);
        }
    }

    /**
     * Sık kullanılan ve import edilen sınıfların üyelerini önbelleğe alır
     */
    private void cacheCommonClassMembers() {
        // Import edilen Jazari sınıflarını önbelleğe al
        for (String importedClass : importedClasses) {
            // Eğer jazari paketinden bir sınıf ise önbelleğe al
            for (String jazariPackage : jazariPackages) {
                if (importedClass.startsWith(jazariPackage) && !CLASS_MEMBER_CACHE.containsKey(importedClass)) {
                    try {
                        // Sınıfı yükle ve önbelleğe al
                        Class<?> clazz = Class.forName(importedClass);
                        getClassMembers(importedClass, false); // önbelleğe almak için çağır
                        LOGGER.info("Jazari sınıfı önbelleğe alındı: " + importedClass);
                    } catch (ClassNotFoundException e) {
                        LOGGER.log(Level.WARNING, "Jazari sınıfı bulunamadı: " + importedClass, e);
                    }
                }
            }
        }

        // Yaygın sınıfları önbelleğe al
        for (Map.Entry<String, String> entry : COMMON_CLASSES.entrySet()) {
            String className = entry.getKey();
            String packageName = entry.getValue();
            String fullClassName = packageName + "." + className;

            // Eğer önbellekte yoksa
            if (!CLASS_MEMBER_CACHE.containsKey(fullClassName)) {
                try {
                    // Sınıfı yükle ve önbelleğe al
                    Class<?> clazz = Class.forName(fullClassName);
                    getClassMembers(fullClassName, false); // önbelleğe almak için çağır
                } catch (ClassNotFoundException e) {
                    // Sessizce geç
                }
            }
        }
    }

    /**
     * Tamamlama sağlayıcısını oluşturur
     */
    private DefaultCompletionProvider createCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider() {
            private List<Completion> currentCompletions = null;

            @Override
            protected List<Completion> getCompletionsImpl(JTextComponent comp) {
                String text = getTextBeforeCaret();

                if (text.endsWith(".")) {
                    // Nokta sonrası durumda, tüm tamamlamaları al
                    String varName = extractVarName();
                    if (varName != null && !varName.isEmpty()) {
                        List<Completion> memberCompletions = getMemberCompletions(varName);
                        if (memberCompletions != null && !memberCompletions.isEmpty()) {
                            // Tamamlama listesini sakla (filtreleme için)
                            currentCompletions = new ArrayList<>(memberCompletions);
                            return memberCompletions;
                        }
                    }
                    return super.getCompletionsImpl(comp);
                } else if (text.matches(".*\\.[a-zA-Z]+$") && currentCompletions != null) {
                    // Nokta + harfler durumunda, önceki listeyi filtrele
                    int dotIndex = text.lastIndexOf('.');
                    String prefix = text.substring(dotIndex + 1);

                    // Manuel filtreleme
                    List<Completion> filtered = new ArrayList<>();
                    for (Completion completion : currentCompletions) {
                        if (completion.getInputText().toLowerCase().startsWith(prefix.toLowerCase())) {
                            filtered.add(completion);
                        }
                    }

                    if (!filtered.isEmpty()) {
                        return filtered;
                    }
                }

                // Diğer durumlar için varsayılan davranışa geri dön
                return super.getCompletionsImpl(comp);
            }
        };

        // '.' ifadesinden sonra otomatik tamamlamayı aktif et
        provider.setAutoActivationRules(true, ".");

        return provider;
    }

    /**
     * İmleç konumundan önceki metni alır
     */
    private String getTextBeforeCaret() {
        try {
            int caretPos = textArea.getCaretPosition();
            String fullText = textArea.getText(0, caretPos);

            // Alt satırlardaki metot zincirlerini birleştir
            // Yeni satır karakterlerini kaldır
            return fullText.replace("\n", "").replace("\r", "");
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * İmleçten önceki kodda, noktadan önce gelen değişken/sınıf adını çıkarır
     */
    private String extractVarName() {
        String text = getTextBeforeCaret();
        int dotIndex = text.lastIndexOf('.');
        if (dotIndex <= 0) {
            return null;
        }

        // Noktadan önceki metni al
        String beforeDot = text.substring(0, dotIndex);

        // Son geçerli Java tanımlayıcısını bul
        Pattern pattern = Pattern.compile("[a-zA-Z_$][a-zA-Z0-9_$]*$");
        Matcher matcher = pattern.matcher(beforeDot);

        if (matcher.find()) {
            return matcher.group();
        }

        // Metot zinciri olabilir - metot çağrısı sonrası
        if (beforeDot.endsWith(")")) {
            String chainType = analyzeDotChainReturnType(beforeDot);
            if (chainType != null) {
                return "MethodChain:" + chainType;  // Özel bir prefix ile metot zincirini işaretle
            }
        }

        return null;
    }

    /**
     * Otomatik tamamlama davranışını yapılandırır
     */
    private void configureAutoCompletion() {
        autoCompletion.setShowDescWindow(true);
        autoCompletion.setParameterAssistanceEnabled(true);
        autoCompletion.setAutoActivationEnabled(true);
        autoCompletion.setAutoCompleteSingleChoices(false);

        // 400ms tamamlama gecikmesi, kısa sürede kullanıcıyı rahatsız etmemek için
        autoCompletion.setAutoActivationDelay(400);

        // CTRL+SPACE ile manuel tetikleme
        autoCompletion.setTriggerKey(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,
                KeyEvent.CTRL_DOWN_MASK));

        // Tamamlama listesini sınırlandırmama
        autoCompletion.setListCellRenderer(new DefaultListCellRenderer());
        // Maksimum görüntülenen öğe sayısını artır (varsayılan genellikle 10-20 arası)
        try {
            // Reflection ile provider'ın maksimum görünür öğe sayısını değiştir
            Field field = autoCompletion.getClass().getDeclaredField("provider");
            field.setAccessible(true);
            CompletionProvider provider = (CompletionProvider) field.get(autoCompletion);
            if (provider instanceof DefaultCompletionProvider) {
                Field maxField = DefaultCompletionProvider.class.getDeclaredField("maxResultCount");
                maxField.setAccessible(true);
                maxField.setInt(provider, 1000); // Daha büyük bir değer
            }
        } catch (Exception e) {
            LOGGER.warning("Tamamlama listesi sınırı değiştirilemedi: " + e.getMessage());
        }
    }

    /**
     * Verilen sınıf adını doğrudan yüklemeyi dener
     */
    private void tryLoadClassDirect(String className) {
        System.out.println("Yüklenmeye çalışılan sınıf: " + className);

        // CMatrix veya DummyVar için özel durum kontrolü
        if (className.equals("CMatrix") || className.equals("DummyVar")) {
            // loadAllCMatrixMethods() metodu zaten çağrıldıysa tekrar çağırmayız
            // bu da getClassMembers() ile çakışmasını önler
            if (!className.equals("DummyVar")) {
                loadAllCMatrixMethods();
            }
            return;
        }

        // 1. Doğrudan sınıf adıyla dene
        tryLoadAndCacheClass(className);

        // 2. Tam paket adlarıyla dene
        for (String basePackage : BASE_PACKAGES) {
            tryLoadAndCacheClass(basePackage + className);
        }
    }

    /**
     * Herhangi bir sınıfın metodlarını yükler
     *
     * @param className Tam sınıf adı
     * @param staticOnly Sadece statik metodları yükle
     * @return Tamamlama listesi
     */
    private List<Completion> loadAllClassMethods(String className, boolean staticOnly) {
        List<Completion> completions = new ArrayList<>();
        try {
            Class<?> targetClass = Class.forName(className);

            // Sınıfın kendi metodlarını ekle
            for (Method method : targetClass.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                if (staticOnly && !Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                String methodName = method.getName();

                // Metot parametrelerini ve dönüş tipini biçimlendir
                StringBuilder params = new StringBuilder();
                Class<?>[] paramTypes = method.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) {
                        params.append(", ");
                    }
                    params.append(getSimpleTypeName(paramTypes[i]));
                    params.append(" param").append(i + 1);
                }

                String returnType = getSimpleTypeName(method.getReturnType());
                String methodSig = methodName + "(" + params + ")";

                // Tamamlama ekle
                addMethodCompletion(completions, methodName, methodSig, returnType, paramTypes.length > 0);
            }

            // Üst sınıflardan gelen metotları ekle (kalıtım)
            Set<String> addedMethods = new HashSet<>();
            for (Completion comp : completions) {
                addedMethods.add(comp.getInputText());
            }

            for (Method method : targetClass.getMethods()) {
                if (addedMethods.contains(method.getName())
                        || !Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                if (staticOnly && !Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                String methodName = method.getName();

                // Object sınıfından gelen temel metotları atla
                if (methodName.equals("wait") || methodName.equals("notify")
                        || methodName.equals("notifyAll") || methodName.equals("getClass")
                        || methodName.equals("hashCode") || methodName.equals("equals")
                        || methodName.equals("toString")) {
                    continue;
                }

                // Metot parametrelerini ve dönüş tipini biçimlendir
                StringBuilder params = new StringBuilder();
                Class<?>[] paramTypes = method.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) {
                        params.append(", ");
                    }
                    params.append(getSimpleTypeName(paramTypes[i]));
                    params.append(" param").append(i + 1);
                }

                String returnType = getSimpleTypeName(method.getReturnType());
                String methodSig = methodName + "(" + params + ")";

                // Tamamlama ekle
                addMethodCompletion(completions, methodName, methodSig, returnType, paramTypes.length > 0);
                addedMethods.add(methodName);
            }

            // Alan değişkenleri ekle
            for (Field field : targetClass.getDeclaredFields()) {
                if (!Modifier.isPublic(field.getModifiers())) {
                    continue;
                }

                if (staticOnly && !Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String fieldName = field.getName();
                String fieldType = getSimpleTypeName(field.getType());

                completions.add(new BasicCompletion(
                        provider,
                        fieldName,
                        fieldType,
                        fieldType + " " + fieldName
                ));
            }

            System.out.println(className + " sınıfı için " + completions.size() + " üye yüklendi");

        } catch (ClassNotFoundException e) {
            LOGGER.warning("Sınıf bulunamadı: " + className);
        } catch (Exception e) {
            LOGGER.severe("Sınıf metodları yüklenirken hata: " + className + " - " + e.getMessage());
        }

        return completions;
    }

    /**
     * Bir metot tamamlaması ekler
     */
    private void addMethodCompletion(List<Completion> completions, String methodName,
            String methodSig, String returnType, boolean hasParams) {
        if (hasParams) {
            // Parametreli metot şablonu
            String template = methodName + "(${cursor})";
            completions.add(new TemplateCompletion(
                    provider,
                    methodName,
                    methodSig + " : " + returnType,
                    template,
                    methodSig,
                    returnType + " " + methodSig
            ));
        } else {
            // Parametresiz metot
            String template = methodName + "()";
            completions.add(new TemplateCompletion(
                    provider,
                    methodName,
                    methodSig + " : " + returnType,
                    template,
                    methodSig,
                    returnType + " " + methodSig
            ));
        }
    }

    /**
     * Metot zincirindeki son metodun dönüş tipini analiz eder
     */
    private String analyzeDotChainReturnType(String beforeDot) {
        try {
            // Birkaç özel durumu hızlıca kontrol et
            if (beforeDot.contains("CMatrix.getInstance()")
                    || beforeDot.contains("imread(")
                    || beforeDot.contains("imshow(")) {
                return "jazari.matrix.CMatrix";
            }

            // Metot zincirinde son metodu bul
            Pattern lastMethodPattern = Pattern.compile("(?:\\.|^)([A-Za-z][A-Za-z0-9_]*)\\(.*\\)$");
            Matcher lastMethodMatcher = lastMethodPattern.matcher(beforeDot);

            if (lastMethodMatcher.find()) {
                String lastMethodName = lastMethodMatcher.group(1);

                // Bilinen metot dönüş tiplerini kontrol et (önbellek gibi)
                if (lastMethodName.equals("getInstance") && beforeDot.contains("CMatrix")) {
                    return "jazari.matrix.CMatrix";
                }

                // Sınıfı tahmin etmeye çalış
                Pattern classPattern = Pattern.compile("([A-Z][A-Za-z0-9_]*)\\.");
                Matcher classMatcher = classPattern.matcher(beforeDot);

                if (classMatcher.find()) {
                    String className = classMatcher.group(1);
                    String fullClassName = findFullClassName(className);

                    if (fullClassName != null) {
                        // Sınıfı bulduk, şimdi metodu arayalım
                        try {
                            Class<?> clazz = Class.forName(fullClassName);

                            // Metot dönüş tipini bul
                            for (Method method : clazz.getMethods()) {
                                if (method.getName().equals(lastMethodName)) {
                                    return method.getReturnType().getName();
                                }
                            }
                        } catch (Exception e) {
                            // Sessizce hataları geç
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Metot zinciri analizi başarısız", e);
        }

        return null;
    }

    /**
     * CMatrix sınıfının tüm metodlarını yükler
     */
    private void loadAllCMatrixMethods() {
        try {
            Class<?> cmatrixClass = Class.forName("jazari.matrix.CMatrix");
            List<Completion> allCompletions = new ArrayList<>();

            // CMatrix sınıfının tüm metodlarını getir
            for (Method method : cmatrixClass.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                String methodName = method.getName();

                // Metot parametrelerini ve dönüş tipini biçimlendir
                StringBuilder params = new StringBuilder();
                Class<?>[] paramTypes = method.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) {
                        params.append(", ");
                    }
                    params.append(getSimpleTypeName(paramTypes[i]));
                    params.append(" param").append(i + 1);
                }

                String returnType = getSimpleTypeName(method.getReturnType());
                String methodSig = methodName + "(" + params + ")";

                // Farklı tamamlama varyantları oluştur
                if (paramTypes.length > 0) {
                    // Parametreli metot şablonu
                    String template = methodName + "(${cursor})";
                    allCompletions.add(new TemplateCompletion(
                            provider,
                            methodName,
                            methodSig + " : " + returnType,
                            template,
                            methodSig,
                            returnType + " " + methodSig
                    ));
                } else {
                    // Parametresiz metot
                    String template = methodName + "()";
                    allCompletions.add(new TemplateCompletion(
                            provider,
                            methodName,
                            methodSig + " : " + returnType,
                            template,
                            methodSig,
                            returnType + " " + methodSig
                    ));
                }
            }

            // Kalıtım yoluyla gelen metodları da ekle
            for (Method method : cmatrixClass.getMethods()) {
                // Daha önce eklenen metodları atla
                boolean alreadyAdded = false;
                for (Completion comp : allCompletions) {
                    if (comp.getInputText().equals(method.getName())) {
                        alreadyAdded = true;
                        break;
                    }
                }

                if (alreadyAdded || !Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                String methodName = method.getName();

                // Object sınıfından gelen temel metodları atla
                if (methodName.equals("wait") || methodName.equals("notify")
                        || methodName.equals("notifyAll") || methodName.equals("getClass")
                        || methodName.equals("hashCode") || methodName.equals("equals")
                        || methodName.equals("toString")) {
                    continue;
                }

                // Metot parametrelerini ve dönüş tipini biçimlendir
                StringBuilder params = new StringBuilder();
                Class<?>[] paramTypes = method.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) {
                        params.append(", ");
                    }
                    params.append(getSimpleTypeName(paramTypes[i]));
                    params.append(" param").append(i + 1);
                }

                String returnType = getSimpleTypeName(method.getReturnType());
                String methodSig = methodName + "(" + params + ")";

                // Farklı tamamlama varyantları oluştur
                if (paramTypes.length > 0) {
                    // Parametreli metot şablonu
                    String template = methodName + "(${cursor})";
                    allCompletions.add(new TemplateCompletion(
                            provider,
                            methodName,
                            methodSig + " : " + returnType,
                            template,
                            methodSig,
                            returnType + " " + methodSig
                    ));
                } else {
                    // Parametresiz metot
                    String template = methodName + "()";
                    allCompletions.add(new TemplateCompletion(
                            provider,
                            methodName,
                            methodSig + " : " + returnType,
                            template,
                            methodSig,
                            returnType + " " + methodSig
                    ));
                }
            }

            // Tüm metodları provider'a ekle - diğer eklenen metodları temizle
            if (provider instanceof DefaultCompletionProvider) {
                DefaultCompletionProvider defaultProvider = (DefaultCompletionProvider) provider;

                // Önce mevcut tamamlamaları temizle
                defaultProvider.clear();

                // Şimdi tüm metodları ekle
                for (Completion comp : allCompletions) {
                    defaultProvider.addCompletion(comp);
                }

                System.out.println("CMatrix için " + allCompletions.size() + " metod yüklendi!");
            }

        } catch (Exception e) {
            LOGGER.severe("CMatrix metodları yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Bir sınıfın üyelerini analiz eder ve tamamlama listesine ekler
     */
    private List<Completion> getClassMembers(String className, boolean staticOnly) {
        // Önbellekten kontrol etme işlemini kaldır
        List<Completion> completions = new ArrayList<>();

        try {
            Class<?> clazz = Class.forName(className);

            System.out.println("Sınıf yüklendi: " + className);
            System.out.println("Metod sayısı: " + clazz.getDeclaredMethods().length);

            // Sadece bu sınıfın kendi metodlarını al
            for (Method method : clazz.getDeclaredMethods()) {
                // Sadece public metodlar
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                if (staticOnly && !Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                String methodName = method.getName();

                // Debug
                System.out.println("İşlenen metod: " + methodName);

                // Metot parametrelerini biçimlendir
                StringBuilder params = new StringBuilder();
                Class<?>[] paramTypes = method.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) {
                        params.append(", ");
                    }
                    params.append(getSimpleTypeName(paramTypes[i]));
                    params.append(" param").append(i + 1);
                }

                // Tamamlama metni ve açıklama
                String returnType = getSimpleTypeName(method.getReturnType());
                String methodSig = methodName + "(" + params + ")";

                // Farklı tamamlama varyantları oluştur
                if (paramTypes.length > 0) {
                    // Parametreli metot şablonu
                    String template = methodName + "(${cursor})";
                    completions.add(new TemplateCompletion(
                            provider,
                            methodName,
                            methodSig + " : " + returnType,
                            template,
                            methodSig,
                            returnType + " " + methodSig
                    ));
                } else {
                    // Parametresiz metot
                    String template = methodName + "()";
                    completions.add(new TemplateCompletion(
                            provider,
                            methodName,
                            methodSig + " : " + returnType,
                            template,
                            methodSig,
                            returnType + " " + methodSig
                    ));
                }
            }

            // Public alanları ekle - yine sadece bu sınıfınkileri
            for (Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isPublic(field.getModifiers())) {
                    continue;
                }

                if (staticOnly && !Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                String fieldName = field.getName();
                String fieldType = getSimpleTypeName(field.getType());

                completions.add(new BasicCompletion(
                        provider,
                        fieldName,
                        fieldType,
                        fieldType + " " + fieldName
                ));
            }

            // Şimdi getMethods() ile tüm metodları (kalıtımla gelenler dahil) kontrol et
            // ve daha önce eklenmediyse ekle
            Set<String> addedMethods = new HashSet<>();
            for (Completion comp : completions) {
                addedMethods.add(comp.getInputText());
            }

            for (Method method : clazz.getMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }

                if (staticOnly && !Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                String methodName = method.getName();

                // Daha önce eklediyse geç
                if (addedMethods.contains(methodName)) {
                    continue;
                }

                // Debug
                System.out.println("Kalıtımla gelen metod: " + methodName);

                // Native metodlar ve JDK iç metodlarını atla
                if (methodName.startsWith("wait") || methodName.startsWith("notify")
                        || methodName.equals("getClass") || methodName.equals("hashCode")
                        || methodName.equals("equals") || methodName.equals("toString")) {
                    continue;
                }

                // Metot parametrelerini biçimlendir
                StringBuilder params = new StringBuilder();
                Class<?>[] paramTypes = method.getParameterTypes();
                for (int i = 0; i < paramTypes.length; i++) {
                    if (i > 0) {
                        params.append(", ");
                    }
                    params.append(getSimpleTypeName(paramTypes[i]));
                    params.append(" param").append(i + 1);
                }

                // Tamamlama metni ve açıklama
                String returnType = getSimpleTypeName(method.getReturnType());
                String methodSig = methodName + "(" + params + ")";

                // Farklı tamamlama varyantları oluştur
                if (paramTypes.length > 0) {
                    // Parametreli metot şablonu
                    String template = methodName + "(${cursor})";
                    completions.add(new TemplateCompletion(
                            provider,
                            methodName,
                            methodSig + " : " + returnType,
                            template,
                            methodSig,
                            returnType + " " + methodSig
                    ));
                } else {
                    // Parametresiz metot
                    String template = methodName + "()";
                    completions.add(new TemplateCompletion(
                            provider,
                            methodName,
                            methodSig + " : " + returnType,
                            template,
                            methodSig,
                            returnType + " " + methodSig
                    ));
                }

                addedMethods.add(methodName);
            }

            // Önbelleğe ekleme kısmını kaldır
            System.out.println("Toplam bulunan metod sayısı: " + completions.size());

        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Sınıf bulunamadı: " + className, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Sınıf üyeleri analiz edilirken hata: " + className, e);
        }

        return completions;
    }

    /**
     * Temel Java anahtar kelimelerini ve şablonlarını ekler
     */
    private void addBasicJavaCompletions() {
        // Java anahtar kelimeleri
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "false", "final", "finally", "float", "for", "goto", "if",
            "implements", "import", "instanceof", "int", "interface", "long",
            "native", "new", "null", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized",
            "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"
        };

        // Anahtar kelimeleri ekle
        for (String keyword : keywords) {
            provider.addCompletion(new BasicCompletion(provider, keyword));
        }

        // Yaygın sınıfları ekle
        for (Map.Entry<String, String> entry : COMMON_CLASSES.entrySet()) {
            String className = entry.getKey();
            String packageName = entry.getValue();

            // Basit tamamlama olarak ekle
            BasicCompletion completion = new BasicCompletion(
                    provider,
                    className,
                    "Class in " + packageName
            );
            provider.addCompletion(completion);
        }

        // Kod şablonları ekle
        addCodeTemplates();
    }

    /**
     * Yaygın kod şablonlarını ekler
     */
    private void addCodeTemplates() {
        provider.addCompletion(new TemplateCompletion(
                provider,
                "sout",
                "System.out.println",
                "System.out.println(${cursor});",
                "Print to console",
                "Outputs text to the standard console"
        ));

        provider.addCompletion(new TemplateCompletion(
                provider,
                "psvm",
                "public static void main",
                "public static void main(String[] args) {\n\t${cursor}\n}",
                "Main method",
                "Standard Java application entry point"
        ));

        provider.addCompletion(new TemplateCompletion(
                provider,
                "fori",
                "for loop with index",
                "for (int ${i} = 0; ${i} < ${limit}; ${i}++) {\n\t${cursor}\n}",
                "For loop",
                "Standard for loop with index counter"
        ));

        provider.addCompletion(new TemplateCompletion(
                provider,
                "foreach",
                "for-each loop",
                "for (${Type} ${var} : ${collection}) {\n\t${cursor}\n}",
                "For-each loop",
                "Loop through elements in a collection"
        ));

        provider.addCompletion(new TemplateCompletion(
                provider,
                "try",
                "try-catch block",
                "try {\n\t${cursor}\n} catch (${Exception} e) {\n\te.printStackTrace();\n}",
                "Try-catch block",
                "Exception handling block"
        ));

        provider.addCompletion(new TemplateCompletion(
                provider,
                "ifn",
                "if null check",
                "if (${var} == null) {\n\t${cursor}\n}",
                "If null check",
                "Check if variable is null"
        ));

        provider.addCompletion(new TemplateCompletion(
                provider,
                "inn",
                "if not null check",
                "if (${var} != null) {\n\t${cursor}\n}",
                "If not null check",
                "Check if variable is not null"
        ));
    }

    /**
     * Bir sınıf veya değişken için üye tamamlamalarını alır
     */
    private List<Completion> getMemberCompletions(String varName) {
        // Metot zinciri kontrolü
        if (varName != null && varName.startsWith("MethodChain:")) {
            String className = varName.substring("MethodChain:".length());
            try {
                // Sınıfı doğrudan yükle ve metodlarını getir
                return loadAllClassMethods(className, false);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Metot zinciri tamamlanırken hata: " + e.getMessage());
            }
            return Collections.emptyList();
        }

        List<Completion> completions = new ArrayList<>();

        try {
            // 1. Bu bir sınıf adı olabilir
            if (varName != null && Character.isUpperCase(varName.charAt(0))) {
                // Sınıf adı olabilir
                String fullClassName = findFullClassName(varName);
                if (fullClassName != null) {
                    // Statik üyeleri yükle
                    return loadAllClassMethods(fullClassName, true);
                }
            }

            // 2. Değişken tipi kontrolü
            String varType = findVariableType(varName);
            if (varType != null) {
                String fullClassName = findFullClassName(varType);
                if (fullClassName != null) {
                    // Normal sınıf üyelerini yükle
                    return loadAllClassMethods(fullClassName, false);
                }
            }

            // 3. Bazı özel durumlar (System, Math gibi)
            if (varName != null) {
                if (varName.equals("System")) {
                    addSystemCompletions(completions);
                    return completions;
                } else if (varName.equals("Math")) {
                    addMathCompletions(completions);
                    return completions;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Üye tamamlamaları oluşturulurken hata", e);
        }

        return completions;
    }

    /**
     * Sınıf adı için tam paket yolunu bulur
     */
    private String findFullClassName(String className) {
        // 1. Özel durumları kontrol et
        if (className.equals("String") || className.equals("Integer")
                || className.equals("Boolean") || className.equals("System")
                || className.equals("Object") || className.equals("Math")) {
            return "java.lang." + className;
        }

        // 2. Tam import edilmiş sınıfları kontrol et
        for (String importedClass : importedClasses) {
            if (importedClass.endsWith("." + className)) {
                return importedClass;
            }
        }

        // 3. İmport edilmiş paketlerdeki sınıfları kontrol et
        for (String packageName : importedPackages) {
            String fullName = packageName + "." + className;
            try {
                Class.forName(fullName);
                return fullName;
            } catch (ClassNotFoundException e) {
                // Bu pakette bu sınıf yok, diğer paketlere bak
            }
        }

        // 4. COMMON_CLASSES listesindeki sınıfları kontrol et
        if (COMMON_CLASSES.containsKey(className)) {
            return COMMON_CLASSES.get(className) + "." + className;
        }

        return null;
    }

    /**
     * System sınıfı için özel tamamlamalar ekler
     */
    private void addSystemCompletions(List<Completion> completions) {
        completions.add(new TemplateCompletion(
                provider,
                "out",
                "System.out - PrintStream",
                "out",
                "Standard output stream",
                "The standard output stream for console output"
        ));

        completions.add(new TemplateCompletion(
                provider,
                "err",
                "System.err - PrintStream",
                "err",
                "Standard error stream",
                "The standard error output stream"
        ));

        completions.add(new TemplateCompletion(
                provider,
                "in",
                "System.in - InputStream",
                "in",
                "Standard input stream",
                "The standard input stream for console input"
        ));

        completions.add(new TemplateCompletion(
                provider,
                "currentTimeMillis",
                "System.currentTimeMillis() - long",
                "currentTimeMillis()",
                "Current time in milliseconds",
                "Returns the current time in milliseconds since epoch"
        ));
    }

    /**
     * Math sınıfı için özel tamamlamalar ekler
     */
    private void addMathCompletions(List<Completion> completions) {
        String[] mathFunctions = {"abs", "sin", "cos", "tan", "sqrt", "pow", "log", "exp", "round", "floor", "ceil", "random"};

        for (String func : mathFunctions) {
            completions.add(new TemplateCompletion(
                    provider,
                    func,
                    "Math." + func + "(...)",
                    func + "(${cursor})",
                    "Math function",
                    "Mathematical function in Math class"
            ));
        }

        // Sabitler
        completions.add(new BasicCompletion(
                provider,
                "PI",
                "double",
                "Mathematical constant PI (3.14159...)"
        ));

        completions.add(new BasicCompletion(
                provider,
                "E",
                "double",
                "Mathematical constant e (2.71828...)"
        ));
    }

    /**
     * Bir değişken adı için değişken tipini bulmaya çalışır (basit analiz)
     */
    /**
     * Bir değişken adı için değişken tipini bulmaya çalışır (gelişmiş analiz)
     */
    private String findVariableType(String varName) {
        if (varName == null || varName.isEmpty()) {
            return null;
        }

        // Bazı özel durumlar için hızlı kontrol
        if (varName.equals("out") || varName.equals("err")) {
            return "java.io.PrintStream";
        }
        if (varName.equals("in")) {
            return "java.io.InputStream";
        }

        String text = textArea.getText();

        // 1. Yerel değişken tanımlarını ara - jenerik tiplerle
        Pattern localVarPattern = Pattern.compile(
                "\\b([A-Za-z][A-Za-z0-9_]*(?:<[^>]*>)?)\\s+"
                + Pattern.quote(varName) + "\\s*[=;]"
        );
        Matcher localVarMatcher = localVarPattern.matcher(text);

        if (localVarMatcher.find()) {
            String type = localVarMatcher.group(1);
            System.out.println("Bulunan değişken tipi (yerel): " + type);
            return type;
        }

        // 2. Sınıf alanlarını ara - jenerik tiplerle
        Pattern fieldPattern = Pattern.compile(
                "\\b(private|protected|public)\\s+([A-Za-z][A-Za-z0-9_<>\\[\\],\\s]*?)\\s+"
                + Pattern.quote(varName) + "\\s*[=;]"
        );
        Matcher fieldMatcher = fieldPattern.matcher(text);

        if (fieldMatcher.find()) {
            String type = fieldMatcher.group(2).trim();
            System.out.println("Bulunan değişken tipi (alan): " + type);
            return type;
        }

        // 3. Metot parametrelerini ara - jenerik tiplerle
        Pattern paramPattern = Pattern.compile(
                "\\([^)]*?\\b([A-Za-z][A-Za-z0-9_<>\\[\\],\\s]*?)\\s+"
                + Pattern.quote(varName) + "\\b[^)]*?\\)"
        );
        Matcher paramMatcher = paramPattern.matcher(text);

        if (paramMatcher.find()) {
            String type = paramMatcher.group(1).trim();
            System.out.println("Bulunan değişken tipi (parametre): " + type);
            return type;
        }

        // 4. For each döngülerini ara - jenerik tiplerle
        Pattern forEachPattern = Pattern.compile(
                "for\\s*\\(\\s*([A-Za-z][A-Za-z0-9_<>\\[\\],\\s]*?)\\s+"
                + Pattern.quote(varName) + "\\s*:"
        );
        Matcher forEachMatcher = forEachPattern.matcher(text);

        if (forEachMatcher.find()) {
            String type = forEachMatcher.group(1).trim();
            System.out.println("Bulunan değişken tipi (foreach): " + type);
            return type;
        }

        // 5. Bir üye tanımından önce atama yapılan değişkenler için tip çıkarımı
        Pattern assignmentPattern = Pattern.compile(
                Pattern.quote(varName) + "\\s*=\\s*new\\s+([A-Za-z][A-Za-z0-9_<>\\[\\],\\s]*?)\\("
        );
        Matcher assignmentMatcher = assignmentPattern.matcher(text);

        if (assignmentMatcher.find()) {
            String type = assignmentMatcher.group(1).trim();
            System.out.println("Bulunan değişken tipi (atama): " + type);
            return type;
        }

        // 6. Metot dönüş tipi ile otomatik eşlenen atamalar (list = Arrays.asList(...))
        Pattern methodAssignPattern = Pattern.compile(
                Pattern.quote(varName) + "\\s*=\\s*([A-Za-z][A-Za-z0-9_]*)\\."
        );
        Matcher methodAssignMatcher = methodAssignPattern.matcher(text);

        if (methodAssignMatcher.find()) {
            String callerClass = methodAssignMatcher.group(1);
            System.out.println("Metot çağrısı sınıfı: " + callerClass);
            // Bu sınıfı ve metodunu çözümleme işlemi daha karmaşık bir algoritmadır
            // İleri analiz için bu bir başlangıç noktası olabilir
        }

        return null;
    }

    /**
     * Bir tür için basitleştirilmiş tip adını döndürür
     */
    private String getSimpleTypeName(Class<?> type) {
        if (type.isArray()) {
            return getSimpleTypeName(type.getComponentType()) + "[]";
        }

        String name = type.getName();

        // Primitive tipleri kontrol et
        if (name.equals("java.lang.String")) {
            return "String";
        }
        if (name.equals("java.lang.Integer")) {
            return "Integer";
        }
        if (name.equals("java.lang.Boolean")) {
            return "Boolean";
        }
        if (name.equals("java.lang.Double")) {
            return "Double";
        }
        if (name.equals("java.lang.Object")) {
            return "Object";
        }

        // Nitelikli adın son bölümünü al
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1) {
            return name.substring(lastDot + 1);
        }

        return name;
    }

    /**
     * IDE açıldığında önbelleği temizler
     */
    public static void clearCache() {
        CLASS_MEMBER_CACHE.clear();
    }

    /**
     * Önbelleğe özel bir sınıf ekler
     */
    public void preloadClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            getClassMembers(className, false); // instance members
            getClassMembers(className, true);  // static members
            LOGGER.info("Sınıf önbelleğe alındı: " + className);
        } catch (ClassNotFoundException e) {
            LOGGER.warning("Önbellek için sınıf bulunamadı: " + className);
        }
    }

    /**
     * Jazari kütüphanesinin temel sınıflarını önbelleğe alır
     */
    public void preloadJazariClasses() {
        String[] jazariClasses = {
            "jazari.matrix.CMatrix",
            "jazari.image_processing.ImageProcess",
            "jazari.types.TPoint",
            "jazari.types.TMatrixOperator",
            "jazari.utils.FileUtils"
        };

        for (String className : jazariClasses) {
            preloadClass(className);
        }
    }
}
