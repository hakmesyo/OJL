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
public class TestReplicateImage {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .imread("images/gul_1.jpg")
                .imresize(0.1f)
                .imReplicate(5, 3)
                .imshow()
                ;
    }
}
