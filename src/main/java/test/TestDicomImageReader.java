/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import ij.*;
import ij.io.*;
import java.awt.image.BufferedImage;
import jazari.matrix.CMatrix;

public class TestDicomImageReader {

    public static void main(String[] args) {
        String dicomFilePath = "images/dicom_image.dcm"; // DICOM dosyasının yolunu buraya yazın
        CMatrix cm = CMatrix.getInstance().imread(dicomFilePath).imshow();
//        ImagePlus imp = IJ.openImage(dicomFilePath);
//        BufferedImage img=imp.getBufferedImage();
//        CMatrix cm = CMatrix.getInstance(img).imshow();
//
//        // Hastanın adını yazdırın
//        String patientName = (String) imp.getProperty("PatientName");
//        System.out.println("Hasta Adı: " + patientName);
//
//        // İmgeyi görüntüleyin
//        imp.show();
    }
}