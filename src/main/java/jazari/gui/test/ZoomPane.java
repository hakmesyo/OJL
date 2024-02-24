/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui.test;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class ZoomPane extends JFrame {
    private JPanel panel;
    private BufferedImage image;
    private double scale = 1.0; // Başlangıçta ölçek 1.0
    private Point zoomPoint = new Point(0, 0); // Başlangıçta bir değere sahip olmalı

    public ZoomPane() {
        setTitle("Resim Yakınlaştırma");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                
                // Resmin boyutunu ve konumunu hesapla
                int imageWidth = (int) (image.getWidth() * scale);
                int imageHeight = (int) (image.getHeight() * scale);
                int x = zoomPoint.x - (int) (zoomPoint.x * scale);
                int y = zoomPoint.y - (int) (zoomPoint.y * scale);
                
                // Resmi çiz
                g2d.translate(x, y); // Zoom noktasına taşı
                g2d.scale(scale, scale); // Ölçeği uygula
                g2d.drawImage(image, 0, 0, this); // Ters kaydırma
                g2d.dispose();
            }
        };

        add(panel);
        try {
            image = ImageIO.read(new File("images/peppers.png"));
        } catch (IOException ex) {
            Logger.getLogger(ZoomPane.class.getName()).log(Level.SEVERE, null, ex);
        }

        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches < 0) {
                    // Tekerlek yukarı doğru döndürüldüğünde, resmi büyüt
                    scale *= 1.1; // 10% oranında büyüt
                } else {
                    // Tekerlek aşağı doğru döndürüldüğünde, resmi küçült
                    scale /= 1.1; // 10% oranında küçült
                }
                
                // Zoom yapılacak noktayı al
                zoomPoint = e.getPoint();
                
                panel.repaint(); // Yeniden çiz
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ZoomPane().setVisible(true);
            }
        });
    }
}
