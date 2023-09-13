/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.snn.test;

import java.util.ArrayList;
import java.util.List;
import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class TestReorderCSV {
    public static void main(String[] args) {
        String path_train = "D:\\ai\\djl\\mnist\\csv\\mnist_test.csv";
        String[] s=FactoryUtils.readFromFileAsString1D(path_train);
        List<String>[] str=new ArrayList[10];
        for (int i = 0; i < 10; i++) {
            str[i]=new ArrayList<>();
        }
        int n=s.length;
        for (int i = 0; i < s.length; i++) {
            int m=Integer.parseInt(s[i].charAt(0)+"");
            str[m].add(s[i]);
        }
        for (int i = 0; i < 10; i++) {
            FactoryUtils.writeToFile("D:\\ai\\djl\\mnist\\csv/test_files/"+i+".csv", str[i]);
        }
    }
}
