/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.matrix.CMatrix;

/**
 *
 * @author Cezeri
 */
public class TestImageMirrorOrFlip {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .imread("images/dog_cat.jpg")
                //.imshow()
                .imFlipVertical()
                .imshowAutoResized(true)
                .imMirrorHorizontal()
                .imshowAutoResized(true)
                
                
                ;
    }
}
