/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.Set;
import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class TestStrToSetChars {
    public static void main(String[] args) {
        String s="e-mail:hakmesyo@gmail.com";
        Set<Character> set=FactoryUtils.str2set(s);
        System.out.println("set = " + set);
    }
}
