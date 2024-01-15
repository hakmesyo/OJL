/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import javax.swing.*;
import java.awt.*;

public class TestBarPlotExample extends JFrame {

    public TestBarPlotExample(String title) {
        super(title);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Verileri temsil eden dizi
        int[] values = {10, 20, 15, 25};

        // Etiketler
        String[] labels = {"Bar 1", "Bar 2", "Bar 3", "Bar 4"};

        // Grafik paneli
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarPlot(g, values, labels);
            }
        };

        setContentPane(chartPanel);
    }

    private void drawBarPlot(Graphics g, int[] values, String[] labels) {
        int barWidth = 40;
        int spacing = 10;
        int x = 50;
        int yScale = 5;

        for (int i = 0; i < values.length; i++) {
            int barHeight = values[i] * yScale;

            // Bar'ı yukarı doğru çiz
            g.fillRect(x, getHeight() - barHeight, barWidth, barHeight);

            // Etiketleri çiz
            g.setColor(Color.BLACK);
            drawRotatedString(g, labels[i], x + barWidth / 2, getHeight() - barHeight - 5, -Math.PI / 2);

            x += barWidth + spacing;
        }
    }

    private void drawRotatedString(Graphics g, String text, int x, int y, double angle) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.rotate(angle, x, y);
        g2d.drawString(text, x, y);
        g2d.rotate(-angle, x, y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TestBarPlotExample example = new TestBarPlotExample("Bar Plot Örneği");
            example.setVisible(true);
        });
    }
}
