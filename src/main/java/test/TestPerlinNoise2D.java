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
public class TestPerlinNoise2D {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                //.zeros(500, 500)
                .perlinNoise2D(300,300,7, 0)
                .rgb2gray()
                .map(0, 255)
                .imshow()
                
                ;
    }
}
