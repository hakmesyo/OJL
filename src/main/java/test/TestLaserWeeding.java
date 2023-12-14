/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.awt.Dimension;
import jazari.matrix.CMatrix;

/**
 *
 * @author dell_lab
 */
public class TestLaserWeeding {
    public static void main(String[] args) {
        CMatrix cm = CMatrix.getInstance()
                .startCamera(0, new Dimension(640, 480), 10)
                .recordCameraImage("C:\\temp_images",0.5)
                ;
    }
}
