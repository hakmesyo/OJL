/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class TestDataSetReduction {
    public static void main(String[] args) {
        String path_from="C:\\Users\\cezerilab\\Desktop\\ds_yabancı_ot_saksı";
        String path_to="C:\\Users\\cezerilab\\Desktop\\ds_yabancı_ot_saksı_reduced";
        FactoryUtils.reduceDataSet(path_from, path_to, "jpg", false, 0.1f);
    }
}
