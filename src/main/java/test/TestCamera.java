/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import jazari.factory.FactoryWebCam;
import jazari.gui.FrameBasicImage;
import jazari.gui.FrameImage;
import jazari.image_processing.ImageProcess;
import jazari.interfaces.call_back_interface.CallBackCamera;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestCamera {

    public static void main(String[] args) {
        //        CMatrix cm = CMatrix.getInstance() //                .startCamera(0, new Dimension(1280, 720),new Dimension(640,360))  
        //                .startCamera(new Dimension(640, 480), 30)
        //                .startCamera(0, new Dimension(1280, 720)) 
        //                .startCamera(0, new Dimension(1920, 1080), new Dimension(640,360))               
        //                .startCamera(0, new Dimension(1920, 1080))               
        //                .startCamera(0)               
        ;
//        FactoryWebCam factoryWebCam = new FactoryWebCam().openWebCam(0).startWebCAM(30);
//        Webcam webCam = factoryWebCam.webCam;
//        WebcamPanel panel = new WebcamPanel(webCam);
//        panel.setImageSizeDisplayed(true);
//        //panel.setFPSLimited(true);
//        //panel.setFPSLimit(30);
//        panel.setFPSDisplayed(true);
//
//        JFrame window = new JFrame("Test webcam panel");
//        window.add(panel);
//        window.setResizable(true);
//        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        window.pack();
//        window.setVisible(true);

//        FactoryWebCam factoryWebCam = FactoryWebCam.getInstance().openWebCam(0);
//        FrameBasicImage frm=new FrameBasicImage();
//        frm.setVisible(true);
//        frm.setSize(640, 480);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    BufferedImage img = factoryWebCam.getImage();
//                    img=ImageProcess.rgb2gray(img);
//                    //img=ImageProcess.pixelsToImageGray(ImageProcess.edgeDetectionCanny(img));
//                    frm.setImage(img);
//                    try {
//                        Thread.sleep(30);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(TestCamera.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        }).start();
//        FactoryWebCam cam = FactoryWebCam.getInstance().openWebCam(0);
//        cam.setCallback(true, new CallBackCamera() {
//            @Override
//            public BufferedImage onFrame(BufferedImage image) {
//                image = ImageProcess.rgb2gray(image);
//                image = ImageProcess.flipImageLeft2Right(image);
//                return image;
//            }
//        });
        CMatrix cm = CMatrix.getInstance()
                .startCamera(0)
//                .startCamera(0,true, (BufferedImage image) -> {
//                    //image = ImageProcess.rgb2gray(image);
//                    image = ImageProcess.flipImageLeft2Right(image);
//                    return image;
//        })
        ;

    }

}
