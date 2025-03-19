/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class FrameBasicImage extends JFrame {

    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FrameBasicImage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private ImageDisplayLabel img;
    private long lastTime = System.currentTimeMillis();
    private double fps = 0;
    private int frameCount = 0;
    private long fpsStartTime = System.currentTimeMillis();
    private static final int FPS_UPDATE_INTERVAL = 500; // Her 500ms'de bir güncelle
    private int imageWidth = 0;
    private int imageHeight = 0;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private String originalTitle = "Camera View";

    public FrameBasicImage() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setTitle(originalTitle);

        img = new ImageDisplayLabel();
        add(img, BorderLayout.CENTER);
    }

    public void setImage(BufferedImage resim) {
        // Görüntü boyutlarını kaydet
        imageWidth = resim.getWidth();
        imageHeight = resim.getHeight();

        // FPS hesapla
        updateFPS();

        // Frame başlığını güncelle
        updateWindowTitle();

        // Görüntüyü ayarla (özel JLabel sınıfımız FPS bilgisini çizecek)
        img.setImage(resim);
        pack();
    }

    private void updateFPS() {
        // Frame sayacını artır
        frameCount++;

        // Mevcut zaman
        long now = System.currentTimeMillis();

        // FPS hesaplama aralığına ulaştı mı kontrol et
        if (now - fpsStartTime >= FPS_UPDATE_INTERVAL) {
            // FPS hesapla
            fps = (double) frameCount * 1000 / (now - fpsStartTime);

            // Sayaçları sıfırla
            frameCount = 0;
            fpsStartTime = now;
        }
    }

    private void updateWindowTitle() {
        //setTitle(originalTitle + " - " + imageWidth + "x" + imageHeight + " | FPS: " + df.format(fps));
        setTitle(originalTitle + " - " + imageWidth + "x" + imageHeight + " | FPS: " + (int)(fps));
    }

    // Görüntü üzerine bilgi yazan özel JLabel sınıfı
    private class ImageDisplayLabel extends JLabel {

        private BufferedImage image;

        public void setImage(BufferedImage img) {
            this.image = img;
            setIcon(new ImageIcon(img));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (image != null) {
                // Bilgileri görüntünün üzerine çiz
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Arka plan dikdörtgeni çiz
//                g2d.setColor(new Color(0, 0, 0, 180));
//                String infoText = imageWidth + "x" + imageHeight + " | FPS: " + df.format(fps);
//                int textWidth = g2d.getFontMetrics().stringWidth(infoText);
//                int textHeight = g2d.getFontMetrics().getHeight();
//
//                g2d.fillRect(getWidth() - textWidth - 20, 10, textWidth + 10, textHeight + 2);

                // Metni çiz
//                g2d.setColor(Color.WHITE);
//                g2d.drawString(infoText, getWidth() - textWidth - 15, 10 + textHeight - 2);
            }
        }
    }

    // FPS değerini döndür
    public double getFPS() {
        return fps;
    }

    // Frame başlığını ayarla
    public void setFrameTitle(String title) {
        this.originalTitle = title;
        updateWindowTitle();
    }
}
