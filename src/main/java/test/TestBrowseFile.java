/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.io.File;
import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class TestBrowseFile {
    public static void main(String[] args) {
        File file=FactoryUtils.browseFile();
        System.out.println("file = " + file);
    }
}
