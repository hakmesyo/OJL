/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author cezerilab
 */

public class DynamicCodeExecution {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // Yazılan sınıfın kodunu String olarak girin.
        String sinifKodu = "public class SinifOrnek {" +
                "    public static void main(String[] args) {" +
                "        System.out.println(\"Merhaba Dünya!\");" +
                "    }" +
                "}";

        // Sınıf kodunu byte dizisine dönüştürme.
        byte[] sinifByteDizisi = sinifKodu.getBytes();

        // Sınıfı yükleme.
        ClassLoader classLoader = new ByteClassLoader();
        
        try {
            Class<?> sinif = classLoader.loadClass("SinifOrnek");

            // Sınıfın main metodunu bulma.
            Method mainMetod = sinif.getMethod("main", String[].class);

            // main metodunu çalıştırma.
            mainMetod.invoke(null, new Object[] {new String[] {}});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static class ByteClassLoader extends ClassLoader {
        public Class<?> loadClass(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
}

