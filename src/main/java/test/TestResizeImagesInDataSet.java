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
public class TestResizeImagesInDataSet {
    public static void main(String[] args) {
        String path="D:\\DATASETS\\MASS\\CBIS_DDSM_JPG\\yolo_ds\\calc\\yolo\\images\\val";
        FactoryUtils.resizeImages(path, path, 0.5f);
        
    }
}
