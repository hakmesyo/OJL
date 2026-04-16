/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import jazari.matrix.CMatrix;

/**
 *
 * @author Teknofest
 */
public class TestShape {
    public static void main(String[] args) {
        CMatrix img = CMatrix.getInstance()
                .imread("images/bf.jpg")
                .rgb2gray()
                .shape()
                ;
        int[] shape=img.shapeArray();
        CMatrix gauss=CMatrix.getInstance((int)shape[0],(int)shape[1]).addNoiseGaussian(0,12).imshow();
    }
}
