/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.llm_chat.test;

import javax.swing.*;
import java.awt.*;

public class JetUygulamasi extends JFrame {

    public JetUygulamasi() {
        setTitle("Jet Uygulaması");
        setSize(400, 300); // Pencere boyutu
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Pencere kapatıldığında uygulamayı sonlandır
        setLocationRelativeTo(null); // Pencereyi ekranın ortasına yerleştir

        // Ana panel oluştur
        JPanel anaPanel = new JPanel();
        anaPanel.setLayout(new BorderLayout());

        // Jet panelini oluştur
        JPanel jetPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                cizJet(g); // Jet'i çiz
            }
        };
        jetPanel.setBackground(Color.WHITE); // Arka plan rengi
        anaPanel.add(jetPanel, BorderLayout.CENTER);

        // Butonu oluştur
        JButton maviButon = new JButton("Mavi Buton");
        maviButon.setBackground(Color.BLUE); // Arka plan rengini mavi yap
        maviButon.setForeground(Color.WHITE); // Yazı rengini beyaz yap
        anaPanel.add(maviButon, BorderLayout.SOUTH); // Butonu alta yerleştir

        // Paneli pencereye ekle
        add(anaPanel);

        setVisible(true); // Pencereyi görünür yap
    }

    // Jet çizme fonksiyonu
    private void cizJet(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Jetin temel şekli için koordinatlar (basit bir üçgen)
        int[] xPoints = {50, 200, 50};
        int[] yPoints = {200, 100, 100};
        int nPoints = 3;

        // Jeti çiz
        g2d.setColor(Color.RED); // Jetin rengi
        g2d.fillPolygon(xPoints, yPoints, nPoints);

        // Ek detaylar (örneğin, pencere)
        g2d.setColor(Color.BLACK);
        g2d.drawRect(100, 120, 30, 20);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JetUygulamasi());
    }
}
