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
public class TestFillNaN {

    public static void main(String[] args) {

        CMatrix veri = CMatrix.getInstance(new float[][]{
            {1, 2, 3, 10},
            {4, 5, 6, 20},
            {7, 8, 9, 30}
        });
// Tek satırda korelasyon matrisini hesapla
        CMatrix korelasyonMatrisi = veri.corrcoef();
// Korelasyon matrisini ısı haritası olarak görselleştir
        korelasyonMatrisi.heatmap(true);

//        CMatrix cm = CMatrix.getInstance()
//                .rand(50, 1)
//                //.linspace(0, 100, 1)
//                
//                .tr()
//                .plot()
//                .multiplyScalar(100)
//                .println()
//                .round()
//                .println()
//                .zscore()
//                //.normalizeMinMax()
//                .println()
//                
//                ;
//        CMatrix cm = CMatrix
//                .getInstance(Double.NaN,1,Double.NaN,5,-7,12)
//                .println()
//                .transpose()
//                .println();
//        float m=(float)Math.random()*100;
//        System.out.println("ortalama = " + m);
//        cm=cm.fillNaN(m).println();
    }
}
