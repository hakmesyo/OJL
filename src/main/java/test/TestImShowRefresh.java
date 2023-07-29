/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.io.File;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

/**
 *
 * @author dell_lab
 */
public class TestImShowRefresh {
    public static void main(String[] args) {
        File[] files=FactoryUtils.getFileArrayInFolderByExtension("images", "jpg");
        for (File file : files) {
            CMatrix.getInstance(file).imshowRefresh();
        }
    }
}
