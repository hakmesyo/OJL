/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui;

import javax.swing.*;
import java.awt.*;

public class PanelCircularProgressBar extends JPanel {

    private int progress = 0;
    private Color progressColor = Color.GREEN;

    public PanelCircularProgressBar() {
        setPreferredSize(new Dimension(100, 100));
    }

    public void setProgress(int progress) {
        this.progress = progress;
        repaint();
    }

    public void setProgressColor(Color color) {
        this.progressColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int diameter = Math.min(width, height);
        int x = (width - diameter) / 2;
        int y = (height - diameter) / 2;

        // Arka plan dairesi
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, width, height);
        g2d.fillOval(x, y, diameter, diameter);

        // Progress yayı
        g2d.setColor(progressColor);
        g2d.setStroke(new BasicStroke(10));
        int startAngle = 90;  // Başlangıç açısı 0 derece
        int arcAngle = (int) (-progress / 100.0 * 360); // Negatif açı ile saat yönünde
        g2d.drawArc(x + 10, y + 10, diameter - 20, diameter - 20, startAngle, arcAngle);

        // Progress metni
        g2d.setColor(getForeground());
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        String text = progress + "%";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int textX = (width - textWidth) / 2;
        int textY = (height - textHeight) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }

    public void setValue(int val) {
        progress=val;
        //revalidate();
        repaint();
    }
}


