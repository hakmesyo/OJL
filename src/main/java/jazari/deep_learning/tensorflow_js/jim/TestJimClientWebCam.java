/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.tensorflow_js.jim;

import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamImageTransformer;
import com.github.sarxos.webcam.WebcamListener;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestJimClientWebCam {

    public static long t1 = System.currentTimeMillis();
    public static JimConnection ref;
    public static int fps=10;
    public static int wait=1000/fps;

    public static void main(String[] args) {
        ref = new JimConnection();
        ref.startJimCommunication("localhost", "8887",wait);

        CMatrix cm = CMatrix.getInstance().startCamera(new Dimension(640, 480),5);
        cm.addWebCamListener(new WebcamListener() {
            @Override
            public void webcamOpen(WebcamEvent we) {
            }

            @Override
            public void webcamClosed(WebcamEvent we) {
            }

            @Override
            public void webcamDisposed(WebcamEvent we) {
            }

            @Override
            public void webcamImageObtained(WebcamEvent we) {
                cm.webCam.setImageTransformer(new WebcamImageTransformer() {
                    @Override
                    public BufferedImage transform(BufferedImage img) {
                        if (System.currentTimeMillis() - t1 > 100) {
                            t1 = System.currentTimeMillis();
                            ref.setImage(img);
                        }
                        return img;
                    }

                });
            }
        });
    }


}
