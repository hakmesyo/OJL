/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.app;

import jazari.matrix.CMatrix;

/**
 *
 * @author dell_lab
 */
public class TestWeedImage {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .imread("C:\\Users\\dell_lab\\Downloads\\weed_1.jpg")
                .imshow()
                .rgb2gray()
                //.binarizeOtsu()
                .threshold(160,190)
                .imshow()
                ;
    }
}
