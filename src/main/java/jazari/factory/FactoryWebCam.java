/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.factory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import jazari.gui.FrameBasicImage;
import jazari.interfaces.call_back_interface.CallBackCamera;

/**
 *
 * @author cezerilab
 */
public class FactoryWebCam {

    public Webcam webCam;
    public WebcamPanel panel;
    private static Dimension size;
    private static boolean isFlipped = true;
    public static BufferedImage currentImage;
    private CallBackCamera callback;
    public FrameBasicImage frm = new FrameBasicImage();

    private FactoryWebCam() {
    }

    public static FactoryWebCam getInstance() {
        FactoryWebCam ret = new FactoryWebCam();
        return ret;
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
        webCam.setViewSize(size);
        webCam.open(true);
        return this;
    }

    public FactoryWebCam openWebCam(int cameraIndex, Dimension size) {
        webCam = Webcam.getWebcams().get(cameraIndex);
        webCam.setCustomViewSizes(size); // register custom resolutions
        webCam.setViewSize(size);
        webCam.open(true);
        return this;
    }

    public FactoryWebCam setCallback(boolean isCameraVisible, CallBackCamera callback) {
        this.callback = callback;
        new Thread(new Runnable() {

            @Override
            public void run() {
                // Webcam listener'ı ekle
                if (webCam != null) {
                    if (isCameraVisible) {
                        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frm.setVisible(true);
                    }
                    webCam.addWebcamListener(new WebcamListener() {
                        @Override
                        public void webcamImageObtained(WebcamEvent we) {
                            // Her yeni görüntü alındığında callback'i çağır
                            if (callback != null) {
                                BufferedImage processedImage = callback.onFrame(we.getImage());
                                currentImage = processedImage;
                                if (isCameraVisible) {
                                    frm.setImage(currentImage);
                                }
                            }
                        }

                        @Override
                        public void webcamOpen(WebcamEvent we) {
                            System.err.println("Camera is opened");
                        }

                        @Override
                        public void webcamClosed(WebcamEvent we) {
                            System.err.println("Camera is closed");
                        }

                        @Override
                        public void webcamDisposed(WebcamEvent we) {
                            System.err.println("Camera is disposed");
                        }
                    });
                }
                if (!isCameraVisible) {
                    try {
                        Thread.sleep(Long.MAX_VALUE);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
        return this;
    }

    public FactoryWebCam showWebCAM() {
        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
        panel.setFPSDisplayed(true);

        JFrame window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
        return this;
    }

    public FactoryWebCam showWebCAM(Dimension dim) {
        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
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

    public FactoryWebCam showWebCAM(int fps) {
        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
        panel.setFPSLimited(true);
        panel.setFPSLimit(fps);
        panel.setFPSDisplayed(true);

        JFrame window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //window.setPreferredSize(new Dimension(640, 480));
        window.pack();
        window.setVisible(true);
        return this;
    }

    public FactoryWebCam showWebCAM(Dimension resize, int fps) {
        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
        panel.setFPSDisplayed(true);
        panel.setFPSLimited(true);
        panel.setFPSLimit(fps);

        JFrame window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setPreferredSize(resize);
        window.pack();
        window.setVisible(true);
        return this;
    }

    public BufferedImage getImage() {
        return webCam.getImage();
    }

    public FactoryWebCam flipImageAlongVerticalAxis() {
        isFlipped = !isFlipped;
        return this;
    }

}
