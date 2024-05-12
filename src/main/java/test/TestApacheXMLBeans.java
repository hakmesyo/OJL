/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.io.File;
import java.io.IOException;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

/**
 *
 * @author cezerilab
 */
public class TestApacheXMLBeans {

    public static void main(String[] args) {
        try {
            // XML dosyasını oku
            File xmlFile = new File("dataset/20586908.xml");

            // XML dosyasını Java nesnesine dönüştür
            XmlObject xmlObject = XmlObject.Factory.parse(xmlFile);

            // XML içeriğini yazdır
            System.out.println(xmlObject.xmlText());
            
            // String değerine eriş
            XmlCursor cursor = xmlObject.newCursor();
            cursor.selectPath("/*/dict/array/dict/array/dict/array/string");
            while (cursor.toNextSelection()) {
                String stringValue = cursor.getTextValue();
                System.out.println("String Value: " + stringValue);
            }
            cursor.dispose();
        } catch (XmlException | IOException e) {
            e.printStackTrace();
        }

    }
}
