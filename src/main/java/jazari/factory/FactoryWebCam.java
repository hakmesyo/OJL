/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.factory;

import jazari.gui.FrameImage;
import jazari.image_processing.ImageProcess;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFrame;

/**
 *
 * @author cezerilab
 */
public class FactoryWebCam {

    //private FactoryWebCam factWebCam = new FactoryWebCam();
    public Webcam webCam;
    public WebcamPanel panel;
    private FrameImage frm = new FrameImage();
    private boolean isMotionDetectionImage = false;
    private boolean isMotionDetectionVideo = false;
    private boolean isVideoRecord = false;
    private boolean isImageRecord = false;
    private static String folderPath = "recorded";
    private static Dimension size;
    private static boolean isFlipped = false;
    public static BufferedImage currentImage;

    public FactoryWebCam() {
//        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frm.setVisible(true);

    }

    public FactoryWebCam openWebCam() {
        webCam = Webcam.getDefault();
        size = WebcamResolution.VGA.getSize();
        webCam.setViewSize(size);
        webCam.open(true);
        return this;
    }

    public FactoryWebCam openWebCam(int cameraIndex) {
        webCam = Webcam.getWebcams().get(cameraIndex);
        size = WebcamResolution.VGA.getSize();
        webCam.setViewSize(size);
        webCam.open(true);
        return this;
    }

    public FactoryWebCam openWebCam(Dimension size) {
        webCam = Webcam.getDefault();
        webCam.setCustomViewSizes(size); // register custom resolutions
        //size = WebcamResolution.VGA.getSize();
        webCam.setViewSize(size);
        webCam.open(true);
        return this;
    }

    public FactoryWebCam openWebCam(int cameraIndex, Dimension size) {
        webCam = Webcam.getWebcams().get(cameraIndex);
        webCam.setCustomViewSizes(size); // register custom resolutions
        webCam.setViewSize(size);
        //size = WebcamResolution.VGA.getSize();
        webCam.open(true);
        return this;
    }

    public FactoryWebCam startWebCAM(Dimension dim) {
        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
//        panel.setFPSLimited(true);
//        panel.setFPSLimit(fps);
        panel.setFPSDisplayed(true);

        JFrame window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setPreferredSize(dim);
        window.pack();
        window.setVisible(true);
        return this;
    }

    public FactoryWebCam startWebCAM(int fps) {
        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
        panel.setFPSLimited(true);
        panel.setFPSLimit(fps);
        panel.setFPSDisplayed(true);

        JFrame window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setPreferredSize(new Dimension(640, 480));
        window.pack();
        window.setVisible(true);

        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                long t1=FactoryUtils.tic();
                while (true) {
                    try {
                        currentImage = webCam.getImage();
                        frm.setImage(currentImage,"","");
                        t1=FactoryUtils.toc(t1);
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }).start();
         */
        return this;
    }

    public FactoryWebCam startWebCAM(Dimension resize, int fps) {
        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
        //panel.setFPSDisplayed(true);
        panel.setFPSLimited(true);
        panel.setFPSLimit(fps);

        JFrame window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setPreferredSize(resize);
        window.pack();
        window.setVisible(true);

        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                long t1 = FactoryUtils.tic();
                while (true) {
                    try {
                        currentImage = webCam.getImage();
                        currentImage = ImageProcess.toBufferedImage(currentImage, 5);
                        currentImage = ImageProcess.resize(currentImage, resize.width, resize.height);
                        frm.setImage(currentImage, "", "");
                        t1 = FactoryUtils.toc(t1);
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }).start();
         */
        return this;
    }

    private static float calculateDifferentPixels(float[][] bf_m, float[][] bf_prev_m) {
        float diffRatio = 0;
        int nr = bf_m.length;
        int nc = bf_m[0].length;
        int cnt = 0;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                if (Math.abs((bf_m[i][j] - bf_prev_m[i][j])) >= 3) {
                    cnt++;
                }
            }
        }
        //System.out.println("cnt = " + cnt);
        diffRatio = 1.0f * cnt / (nr * nc);
        //System.out.println("diffRatio = " + diffRatio);
        return diffRatio;
    }

    public BufferedImage getImage() {
        return webCam.getImage();
    }

    public FactoryWebCam flipImageAlongVerticalAxis() {
        isFlipped = !isFlipped;
        return this;
    }

    public FactoryWebCam startMotionDetectionImage() {
        isMotionDetectionImage = true;
        isMotionDetectionVideo = false;
        return this;
    }

    public FactoryWebCam startMotionDetectionImage(String folderPath) {
        FactoryWebCam.folderPath = folderPath;
        isMotionDetectionImage = true;
        isMotionDetectionVideo = false;
        return this;
    }

    public FactoryWebCam stopMotionDetectionImage() {
        isMotionDetectionImage = false;
        return this;
    }

    public FactoryWebCam startMotionDetectionVideo(String folderPath) {
        FactoryWebCam.folderPath = folderPath;
        isMotionDetectionVideo = true;
        isMotionDetectionImage = false;
        return this;
    }

    public FactoryWebCam stopMotionDetectionVideo() {
        isMotionDetectionVideo = false;
        return this;
    }

}
