/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class DenemeDataAugmentation {
    public static void main(String[] args) {
        double[] d1=new double[5];
        double[] d2=new double[5];
        for (int i = 0; i < 5; i++) {
            d1[i]=Math.random();
        }
        for (int i = 0; i < 5; i++) {
            d2[i]=Math.random();
        }
        
        System.out.println(Arrays.toString(d1));
        System.out.println(Arrays.toString(d2));
        double corr=korelasyonHesapla(d1,d2);
        System.out.println("corr = " + corr);
        
//        double[][] veri = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
//        double[] hedef = {10, 20, 30};
//        
//        for (int i = 0; i < veri[0].length; i++) {
//            double[] ozellik = new double[veri.length];
//            for (int j = 0; j < veri.length; j++) {
//                ozellik[j] = veri[j][i];
//            }
//            double korelasyon = korelasyonHesapla(ozellik, hedef);
//            System.out.println("Özellik " + i + " korelasyonu: " + korelasyon);
//        }
    }
    
    private static double korelasyonHesapla(double[] x, double[] y) {
        double ortalamX = Arrays.stream(x).average().orElse(0.0);
        double ortalamY = Arrays.stream(y).average().orElse(0.0);
        
        double pay = 0, paydaX = 0, paydaY = 0;
        for (int i = 0; i < x.length; i++) {
            pay += (x[i] - ortalamX) * (y[i] - ortalamY);
            paydaX += Math.pow(x[i] - ortalamX, 2);
            paydaY += Math.pow(y[i] - ortalamY, 2);
        }
        
        return pay / Math.sqrt(paydaX * paydaY);
    }

//    public static void main(String[] args) {
//        double[] d={-5000.0, -200.0, -300.0, -240.0, -210.0,-300.0, -10.0};
//        double sum=0;
//        for (int i = 0; i < d.length; i++) {
//            sum+=d[i];
//        }
//        double mean=sum/d.length;
//        System.out.println("mean = " + mean);
//        
//        double max=0;
//        int index=0;
//        for (int i = 0; i < d.length; i++) {
//            if (Math.abs(mean-d[i])>max) {
//                max=Math.abs(mean-d[i]);
//                index=i;
//            }
//        }
//        System.out.println(index+". eleman aykırıdır "+d[index]);
//        
//        
//        
//        List<Double> veriler = new ArrayList<>(List.of(1.0, 200.0, 300.0, 240.0, 210.0,300.0));
//
//        Collections.sort(veriler);
//        int n = veriler.size();
//        double q1 = veriler.get(n / (n-1));
//        double q3 = veriler.get((n-2) * n / (n-1));
//        double iqr = q3 - q1;
//        double altSinir = q1 - (n-2)/2 * iqr;
//        double ustSinir = q3 + (n-2)/2 * iqr;
//
//        List<Double> aykiriDegerler = new ArrayList<>();
//        for (Double veri : veriler) {
//            if (veri < altSinir || veri > ustSinir) {
//                aykiriDegerler.add(veri);
//            }
//        }
//
//        System.out.println("Aykırı değerler: " + aykiriDegerler);
    //}
}
