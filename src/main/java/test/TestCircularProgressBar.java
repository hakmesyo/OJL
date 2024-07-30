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
public class TestCircularProgressBar {
    public static void main(String[] args) {
        for (int i = 1; i <= 100; i++) {
            FactoryUtils.showCircularProgressBar(i);
            FactoryUtils.sleep(10);
        }
        for (int i = 1; i <= 100; i++) {
            FactoryUtils.showCircularProgressBar(i);
            FactoryUtils.sleep(10);
        }
    }
}
