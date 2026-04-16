/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.Random;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

/**
 *
 * @author dell_lab
 */
public class TestAnnotateImage {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .annotateImage("images/bird.jpg")
                
                ;
    }
}
