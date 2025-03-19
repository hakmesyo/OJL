package jazari.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JTextArea;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 *
 * @author cezerilab
 */
public class RuntimeCompiler {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public void compile(String sourceCode, JTextArea outputArea) {
        try {
            // Paket adını ve sınıf adını kaynak kodundan çıkar
            String packageName = extractPackageName(sourceCode);
            String className = extractClassName(sourceCode);

            if (className == null) {
                throw new Exception("Could not find class name in source code");
            }

            // Tam nitelikli sınıf adı (fully qualified class name)
            String fullyQualifiedName = packageName == null ? className : packageName + "." + className;

            // Paket yapısını oluştur (eğer paket belirtilmişse)
            File packageDir = new File(TEMP_DIR);
            if (packageName != null && !packageName.isEmpty()) {
                String packagePath = packageName.replace('.', File.separatorChar);
                packageDir = new File(TEMP_DIR, packagePath);
                packageDir.mkdirs(); // Paket dizin yapısını oluştur
            }
            // Geçici dosya oluştur (paket yapısı içinde)
            File sourceFile = new File(packageDir, className + ".java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(sourceCode);
            }

            // Compiler'ı hazırla
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new Exception("No JavaCompiler available. Make sure you're using JDK, not JRE.");
            }

            // Compile çıktısını yakala
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            // Kaynak dosyayı derlenecek dosyalar listesine ekle
            Iterable<? extends JavaFileObject> compilationUnits
                    = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));

            // Derleme seçeneklerini ayarla
            List<String> options = new ArrayList<>();
            options.add("-d");
            options.add(TEMP_DIR);

            // Derleme işlemini gerçekleştir
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    options,
                    null,
                    compilationUnits
            );

            boolean success = task.call();

            // Derleme hatalarını göster
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                outputArea.append(String.format("Line %d: %s%n",
                        diagnostic.getLineNumber(),
                        diagnostic.getMessage(null)));
            }

            if (!success) {
                throw new Exception("Compilation failed");
            }

            // Derlenmiş sınıfı yükle ve çalıştır
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File(TEMP_DIR).toURI().toURL()});

            // Tam paket yolu ile sınıfı yükle
            Class<?> loadedClass = Class.forName(fullyQualifiedName, true, classLoader);

            // main metodunu bul
            Method mainMethod = loadedClass.getMethod("main", String[].class);

            // Güvenli şekilde kullanıcı kodunu çalıştır
            runUserCode(null, mainMethod, new String[]{}, outputArea);

            // Geçici dosyaları temizle - paket dizini içindeki sınıf dosyasını sil
            sourceFile.delete();
            if (packageName != null && !packageName.isEmpty()) {
                // Paket yapısı içindeki class dosyasını sil
                String packagePath = packageName.replace('.', File.separatorChar);
                File classFile = new File(new File(TEMP_DIR, packagePath), className + ".class");
                classFile.delete();
            } else {
                new File(TEMP_DIR + className + ".class").delete();
            }

            // Class loader'ı kapat
            classLoader.close();

        } catch (Exception e) {
            outputArea.append("\nExecution Error: " + e.toString());
            e.printStackTrace(new PrintStream(new OutputStreamAdapter(outputArea)));
        }
    }

    /**
     * Derlenen kullanıcı kodunu güvenli bir şekilde çalıştırır ve System.exit()
     * çağrılarını engeller.
     *
     * @param instance Derlenen sınıfın bir örneği (static main metodu için null
     * olabilir)
     * @param mainMethod Çağrılacak main metodu
     * @param args Main metoduna geçirilecek argümanlar (genellikle boş String
     * dizisi)
     * @param outputArea Çıktının yazdırılacağı metin alanı
     */
    private void runUserCode(Object instance, Method mainMethod, String[] args, JTextArea outputArea) {
        // Orijinal çıkış akışlarını yedekle
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        // Orijinal güvenlik yöneticisini sakla
        SecurityManager originalSecurityManager = System.getSecurityManager();

        try {
            // Çıkışı IDE'nin output paneline yönlendir
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream customOut = new PrintStream(baos);
            System.setOut(customOut);
            System.setErr(customOut);

            // Özel güvenlik yöneticisi oluştur ve ayarla
// Özel güvenlik yöneticisi oluştur ve ayarla
            SecurityManager customSecurityManager = new SecurityManager() {
                @Override
                public void checkExit(int status) {
                    // JFrame için System.exit çağrılarına izin ver
                    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                    for (StackTraceElement element : stackTrace) {
                        // JFrame'den gelen System.exit çağrılarına izin ver
                        if (element.getClassName().contains("javax.swing.JFrame")
                                && element.getMethodName().equals("setDefaultCloseOperation")) {
                            return; // Bu durumda izin ver
                        }
                    }

                    // Diğer System.exit() çağrılarını engelle
                    throw new SecurityException("System.exit çağrısı engellendi");
                }

                // Diğer güvenlik kontrolleri için orijinal davranışı koru
                @Override
                public void checkPermission(Permission perm) {
                    if (perm.getName() != null && perm.getName().startsWith("exitVM")) {
                        // JFrame'den gelen çağrılar için kontrol
                        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                        boolean isFromJFrame = false;

                        for (StackTraceElement element : stackTrace) {
                            if (element.getClassName().contains("javax.swing.JFrame")
                                    && element.getMethodName().equals("setDefaultCloseOperation")) {
                                isFromJFrame = true;
                                break;
                            }
                        }

                        if (!isFromJFrame) {
                            throw new SecurityException("System.exit çağrısı engellendi");
                        }
                    }

                    // Diğer tüm izinlere izin ver
                    if (originalSecurityManager != null) {
                        // Eğer orijinal güvenlik yöneticisi varsa, ona yönlendir
                        try {
                            originalSecurityManager.checkPermission(perm);
                        } catch (SecurityException se) {
                            // Sınırlandırılmış API erişimi sorunlarını yok say
                            if (!perm.getName().contains("accessClassInPackage.sun")
                                    && !perm.getName().contains("modifyThread")) {
                                throw se;
                            }
                        }
                    }
                }
            };
            System.setSecurityManager(customSecurityManager);

            outputArea.append("\n--- Program Çalıştırılıyor ---\n");

            try {
                // User kodunu çalıştır
                mainMethod.invoke(instance, (Object) args);

                // Çıktıyı al ve göster
                outputArea.append("\nOutput:\n" + baos.toString());
                outputArea.append("\n--- Program Çalışması Tamamlandı ---\n");

            } catch (SecurityException se) {
                // System.exit çağrısı engellendi
                outputArea.append("\nOutput:\n" + baos.toString());
                outputArea.append("\n--- Program System.exit() çağırdı, ancak IDE korundu. ---\n");
            } catch (InvocationTargetException ite) {
                // Kullanıcı kodundan gelen exception
                Throwable cause = ite.getCause();
                outputArea.append("\nOutput:\n" + baos.toString());
                outputArea.append("\n--- Program Hatası ---\n");

                // Detaylı hata stack trace'i yazdır
                cause.printStackTrace(new PrintStream(new OutputStreamAdapter(outputArea)));
            } catch (Exception e) {
                // Diğer tüm hatalar
                outputArea.append("\nOutput:\n" + baos.toString());
                outputArea.append("\n--- Çalıştırma Hatası ---\n");
                outputArea.append(e.toString());
            }

        } finally {
            // Her durumda orijinal değerleri geri yükle
            System.setOut(originalOut);
            System.setErr(originalErr);
            System.setSecurityManager(originalSecurityManager);
        }
    }

    private String extractClassName(String sourceCode) {
        String[] lines = sourceCode.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("public class ")) {
                String classDeclaration = line.substring(13).trim();
                // Sınıf adını ayıkla (generic, inheritance vb ifadeleri temizle)
                int endIndex = classDeclaration.indexOf(" ");
                if (endIndex == -1) {
                    endIndex = classDeclaration.indexOf("{");
                }
                if (endIndex == -1) {
                    endIndex = classDeclaration.length();
                }
                return classDeclaration.substring(0, endIndex).trim();
            }
        }
        return null;
    }

    private String extractPackageName(String sourceCode) {
        String[] lines = sourceCode.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("package ")) {
                return line.substring(8, line.indexOf(";")).trim();
            }
        }
        return null; // Varsayılan paket (default package)
    }

    // OutputStreamAdapter iç sınıfı, tek metot için ayrı bir sınıf yaratmamak için
    class OutputStreamAdapter extends java.io.OutputStream {

        private JTextArea textArea;

        public OutputStreamAdapter(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) {
            textArea.append(String.valueOf((char) b));
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}
