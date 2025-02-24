/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
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
            // Sınıf adını kaynak kodundan çıkar
            String className = extractClassName(sourceCode);
            if (className == null) {
                throw new Exception("Could not find class name in source code");
            }

            // Geçici dosya oluştur
            File sourceFile = new File(TEMP_DIR + className + ".java");
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
            Class<?> loadedClass = Class.forName(className, true, classLoader);

            // main metodunu bul ve çalıştır
            Method mainMethod = loadedClass.getMethod("main", String[].class);

            // System.out'u yeniden yönlendir
            PrintStream originalOut = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream newOut = new PrintStream(baos);
            System.setOut(newOut);

            try {
                // Kodu çalıştır
                mainMethod.invoke(null, (Object) new String[]{});
            } finally {
                // Her durumda orijinal System.out'u geri yükle
                System.setOut(originalOut);
            }

            // Çıktıyı al ve göster
            outputArea.append("\nOutput:\n" + baos.toString());

            // Geçici dosyaları temizle
            sourceFile.delete();
            new File(TEMP_DIR + className + ".class").delete();

            // Class loader'ı kapat
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