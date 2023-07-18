/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package misc.benchmarks;

import jazari.matrix.CMatrix;

/**
 *
 * @author dell_lab
 */
public class MatrixMultiplication {
    public static void main(String[] args) {
        CMatrix cm1 = CMatrix.getInstance()
                .rand(2000, 2000)                
                ;
        CMatrix cm2 = CMatrix.getInstance()
                .rand(2000, 2000)                
                ;
        
        for (int i = 0; i < 50; i++) {
            cm1.tic().dot(cm2).toc();
        }
        
    }
}
