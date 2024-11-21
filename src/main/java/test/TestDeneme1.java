/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestDeneme1 {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .imread("images/bird.jpg")
                .imshow()
                ;
    }
}
