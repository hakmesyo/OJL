/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.llm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Pong extends JFrame {

    private Paddle paddle1;
    private Paddle paddle2;
    private Ball ball;
    private Timer timer;

    public Pong() {
        setTitle("Pong");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel gamePanel = new JPanel();
        add(gamePanel);

        gamePanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                paddle1.move(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                paddle1.stopMoving(e.getKeyCode());
            }
        });

        paddle1 = new Paddle(10, 500, 10, 100, 5);
        paddle2 = new Paddle(780, 500, 10, 100, 5);
        ball = new Ball(400, 300, 10, 5);

        add(paddle1);
        add(paddle2);
        add(ball);

        timer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ball.move();
                paddle1.moveAI();
                paddle2.moveAI();
                repaint();
            }
        });
        timer.start();

        setVisible(true);
    }

    class Paddle extends JComponent {

        int x, y, width, height, speed;
        boolean movingUp, movingDown;

        public Paddle(int x, int y, int width, int height, int speed) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
            setBounds(x, y, width, height);
        }

        public void move(int keyCode) {
            if (keyCode == KeyEvent.VK_W) {
                movingUp = true;
            } else if (keyCode == KeyEvent.VK_S) {
                movingUp = false;
            }
            if (keyCode == KeyEvent.VK_UP) {
                movingDown = true;
            } else if (keyCode == KeyEvent.VK_DOWN) {
                movingDown = false;
            }
        }

        public void stopMoving(int keyCode) {
            if (keyCode == KeyEvent.VK_W) {
                movingUp = false;
            }
            if (keyCode == KeyEvent.VK_S) {
                movingUp = false;
            }
            if (keyCode == KeyEvent.VK_UP) {
                movingDown = false;
            }
            if (keyCode == KeyEvent.VK_DOWN) {
                movingDown = false;
            }
        }

        public void moveAI() {
            if (ball.y < this.y + this.height / 2 && this.y > 0) {
                this.y -= speed;
            } else if (ball.y > this.y + this.height / 2 && this.y + this.height < getHeight()) {
                this.y += speed;
            }

            if (this.y < 0) {
                this.y = 0;
            }
            if (this.y + this.height > getHeight()) {
                this.y = getHeight() - this.height;
            }
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, width, height);
        }
    }

    class Ball extends JComponent {

        int x, y, diameter, speedX, speedY;

        public Ball(int x, int y, int diameter, int speed) {
            this.x = x;
            this.y = y;
            this.diameter = diameter;
            this.speedX = speed;
            this.speedY = speed;
        }

        public void move() {
            x += speedX;
            y += speedY;

            if (x + diameter > 800 || x < 0) {
                speedX = -speedX;
            }
            if (y + diameter > 600 || y < 0) {
                speedY = -speedY;
            }

            if (intersects(paddle1) || intersects(paddle2)) {
                speedX = -speedX;
            }

            setLocation(x, y);
        }

        private boolean intersects(JComponent paddle) {
            return (this.x < paddle.getX() + paddle.getWidth()
                    && this.x + this.diameter > paddle.getX()
                    && this.y < paddle.getY() + paddle.getHeight()
                    && this.y + this.diameter > paddle.getY());
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.RED);
            g.fillOval(x, y, diameter, diameter);
        }
    }

    public static void main(String[] args) {
        new Pong();
    }
}
