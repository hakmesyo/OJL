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
public class TestChangeImageFileExtension {
    public static void main(String[] args) {
        String path="C:\\Users\\cezerilab\\Desktop\\ds_robotaksi\\trafik_dataset\\segmentation\\ds_lane";
        FactoryUtils.changeImageFileExtension(path, "png", "jpg");
        
    }
}
