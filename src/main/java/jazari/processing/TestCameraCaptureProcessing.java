/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.processing;

import processing.core.*;
import processing.video.*;
import java.awt.image.BufferedImage;

public class TestCameraCaptureProcessing extends PApplet {
    
    private Capture cam;
    private BufferedImage bufferedImg;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        String[] cameras = Capture.list();
        
        if (cameras.length == 0) {
            println("Kamera bulunamadı. Programı sonlandırıyorum.");
            exit();
        } else {
            println("Kullanılabilir kameralar:");
            for (int i = 0; i < cameras.length; i++) {
                println(i + ": " + cameras[i]);
            }
            
            // İlk kamerayı kullan
            cam = new Capture(this, cameras[0]);
            cam.start();
        }
    }

    public void draw() {
        if (cam.available()) {
            cam.read();
            bufferedImg = (BufferedImage) cam.getNative();
            
            // BufferedImage'i ekrana çiz
            image(cam, 0, 0);
            
            // BufferedImage ile istediğiniz işlemleri yapabilirsiniz
            // Örneğin, piksel değerlerini okuma:
            int pixelColor = bufferedImg.getRGB(width/2, height/2);
            fill(pixelColor);
            ellipse(width/2, height/2, 10, 10);
        }
    }

    public static void main(String[] args) {
        PApplet.main("jazari.processing.TestCameraCaptureProcessing");
    }
}