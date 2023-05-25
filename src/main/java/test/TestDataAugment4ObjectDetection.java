/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestDataAugment4ObjectDetection {

    public static void main(String[] args) {
        long t1 = FactoryUtils.tic();
        //String path = "C:\\Users\\cezerilab\\Desktop\\trafik_dataset\\object_detection\\assets";
        String path = "C:\\Users\\dell_lab\\Desktop\\trafik_dataset\\object_detection\\assets";
        String pathImgBackground = path + "/background.jpg";
        String str_ada = path + "/ada.png";
        String str_durak = path + "/durak.png";
        String str_dur = path + "/dur.png";
        String str_girilmez = path + "/girilmez.png";
        String str_park = path + "/park.png";
        String str_park_yapilmaz_1 = path + "/park_yapilmaz_1.png";
        String str_park_yapilmaz_2 = path + "/park_yapilmaz_2.png";
        String str_park_yapilmaz_3 = path + "/park_yapilmaz_3.png";
        String str_park_engelli = path + "/park_engelli.png";
        String str_saga_donulmez = path + "/saga_donulmez.png";
        String str_sola_donulmez = path + "/sola_donulmez.png";
        String str_ileriden_saga = path + "/ileriden_saga.png";
        String str_ileriden_sola = path + "/ileriden_sola.png";
        String str_ileri_veya_saga = path + "/ileri_veya_saga.png";
        String str_ileri_veya_sola = path + "/ileri_veya_sola.png";
        String str_yaya_gecidi = path + "/yaya_gecidi.png";
        BufferedImage img = augmentDataForObjectDetection(pathImgBackground, 10, 
                str_ada, 
                str_durak, 
                str_dur,
                str_girilmez,
                str_park,
                str_park_yapilmaz_1,
                str_park_yapilmaz_2,
                str_park_yapilmaz_3,
                str_park_engelli,
                str_saga_donulmez,
                str_sola_donulmez,
                str_ileriden_saga,
                str_ileriden_sola,
                str_ileri_veya_saga,
                str_ileri_veya_sola,
                str_yaya_gecidi
                
        );
        CMatrix cm = CMatrix.getInstance(img).imshow();
        t1 = FactoryUtils.toc(t1);
    }

    private static BufferedImage augmentDataForObjectDetection(String pathImgBackground, int numberOfAssets, String... pathAsset) {
        BufferedImage img = ImageProcess.imread(pathImgBackground);
        BufferedImage[] assets = new BufferedImage[pathAsset.length];
        for (int i = 0; i < pathAsset.length; i++) {
            assets[i] = ImageProcess.imread(pathAsset[i]);
        }
        int w = img.getWidth();
        int h = img.getHeight();
        List<Rectangle> asset_bbox = new ArrayList();
        for (int j = 0; j < pathAsset.length; j++) {
            int i = 0;
            while (i < numberOfAssets) {
                int resW = (int) (20 + Math.random() * 50);
                BufferedImage temp = ImageProcess.resize(ImageProcess.clone(assets[j]), resW, resW);
                int px = (int) (Math.random() * (w - temp.getWidth()));
                int py = (int) (Math.random() * (h - temp.getWidth()));
                Rectangle rect = new Rectangle(px, py, resW, resW);
                boolean is_intersect = false;
                for (Rectangle rectangle : asset_bbox) {
                    if (rectangle.intersects(rect)) {
                        is_intersect = true;
                        //System.out.println("ne yazık ki intersect asset "+rect+" onceki rect "+rectangle);
                        break;
                    }
                }
                if (is_intersect) {
                    continue;
                }
                i++;
                //System.out.println(i+".bbox helal buldu "+rect);
                asset_bbox.add(rect);
                img = ImageProcess.overlayImage(img, temp,new Point(px, py), 0.85f);
            }
        }
        return img;
    }
}

