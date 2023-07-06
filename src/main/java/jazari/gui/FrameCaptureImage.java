/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;

public class FrameCaptureImage extends Frame {

    private Robot robot;
    private BufferedImage screenshot;
    private BufferedImage originalImage;
    private JPanel panel;
    private Rectangle selection;
    private Point startPoint;
    private Point endPoint;
    private final FrameScreenCapture frm;
    private boolean isMouseReleased = false;
    private boolean isSelectionRemove = false;

    public FrameCaptureImage(FrameScreenCapture frm) {
        this.frm = frm;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        screenshot = FactoryUtils.captureWholeScreenWithRobot();
        originalImage = ImageProcess.clone(screenshot);
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (originalImage != null) {
                    g.drawImage(originalImage, 0, 0, null);
                }
                if (isSelectionRemove) {
                    g.drawImage(originalImage, 0, 0, null);
                } else {
                    if (selection != null) {
                        g.setColor(Color.RED);
                        g.drawRect(selection.x, selection.y, selection.width, selection.height);
                        drawCorners(g);
                    }
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(screenshot.getWidth(), screenshot.getHeight());
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isSelectionRemove = false;
                startPoint = e.getPoint();
                endPoint = startPoint;
                selection = null;
                isMouseReleased = false;
                originalImage = FactoryUtils.captureWholeScreenWithRobot();
                panel.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                endPoint = e.getPoint();
                selection = createSelectionRectangle(startPoint, endPoint);
                isSelectionRemove = true;
                panel.repaint();
                FactoryUtils.bekle(100);
                captureScreenshot(selection);
                isMouseReleased = true;
                FactoryUtils.copyImage2ClipBoard(screenshot);
                frm.setImage(screenshot);
                frm.setState(Frame.NORMAL);
                System.out.println("çıktı");
                //FactoryUtils.bekle(1000);
                dispose();

            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                selection = createSelectionRectangle(startPoint, endPoint);
                panel.repaint();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Screenshot App");
        setResizable(false);
        add(panel);
        setUndecorated(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private Rectangle createSelectionRectangle(Point startPoint, Point endPoint) {
        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(endPoint.x - startPoint.x);
        int height = Math.abs(endPoint.y - startPoint.y);
        return new Rectangle(x, y, width, height);
    }

    private void captureScreenshot(Rectangle selection) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        //originalImage = robot.createScreenCapture(screenRectangle);
        screenshot = originalImage.getSubimage(selection.x, selection.y, selection.width, selection.height);
    }

    private void drawCorners(Graphics g) {
        int x = selection.x;
        int y = selection.y;
        int width = selection.width;
        int height = selection.height;
        int cornerSize = 5;
        g.setColor(Color.blue);
        g.fillRect(x - cornerSize, y - cornerSize, cornerSize * 2, cornerSize * 2);
        g.fillRect(x + width - cornerSize, y - cornerSize, cornerSize * 2, cornerSize * 2);
        g.fillRect(x - cornerSize, y + height - cornerSize, cornerSize * 2, cornerSize * 2);
        g.fillRect(x + width - cornerSize, y + height - cornerSize, cornerSize * 2, cornerSize * 2);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FrameCaptureImage(null);
            }
        });
    }
}
