/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.llm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class YilanOyunu extends JPanel implements KeyListener {

    private int uzunluk = 3;
    private int x[], y[];
    private int siradakiX[], siradakiY[];
    private int sira = 0;
    private Timer timer;

    public YilanOyunu() {
        setPreferredSize(new Dimension(400, 400));
        addKeyListener(this); // Doğru kullanım: addKeyListener()
        timer = new Timer(100, null);
        timer.start();
    }

    public void paintComponent() {
        paintComponent();
        setBackground(Color.BLACK);
        drawSnake();
        drawFood();
    }

    private void drawSnake() {
        x[0] = 100;
        y[0] = 100;

        for (int i = 1; i < uzunluk; i++) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
    }

    private void drawFood() {
        int foodX = (int) (Math.random() * 400);
        int foodY = (int) (Math.random() * 400);
        setLocation(foodX, foodY);
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (y[0] > 0) {
                y[0] -= 20;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (y[0] < 380) {
                y[0] += 20;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (x[0] > 0) {
                x[0] -= 20;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (x[0] < 420) {
                x[0] += 20;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new YilanOyunu());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
