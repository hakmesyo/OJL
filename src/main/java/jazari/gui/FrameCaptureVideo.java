/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;

public class FrameCaptureVideo extends Frame {

    private BufferedImage screenshot;
    private BufferedImage originalImage;
    private Panel panel;
    private Rectangle selection;
    private Point startPoint;
    private Point endPoint;
    private final FrameScreenCapture frm;
    private boolean isMouseReleased = false;
    private boolean videoCaptureStop = false;

    public FrameCaptureVideo(FrameScreenCapture frm) {
        this.frm = frm;
        screenshot = FactoryUtils.captureWholeScreenWithRobot();
        originalImage = ImageProcess.clone(screenshot);

        Dimension dim = FactoryUtils.getScreenSize();
        setSize(dim);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setAlwaysOnTop(true);

        panel = new Panel() {
            @Override
            public void paint(Graphics g) {
                Graphics2D gr=(Graphics2D)g;
                super.paint(g);
                if (!isMouseReleased) {
                    gr.drawImage(originalImage, 0, 0, null);
                }
                if (selection != null) {
                    gr.setColor(Color.RED);
                    gr.setStroke(new BasicStroke(3));
                    gr.drawRect(selection.x - 3, selection.y - 3, selection.width + 6, selection.height + 6);
                    gr.setColor(Color.black);
                    gr.fillRect(selection.x-3, selection.y-25,200,20);
                    gr.setColor(Color.green);
                    gr.drawString("Press ESC to stop recording", selection.x-3, selection.y-10);
                    //drawCorners(g);
                }

            }

        };

        panel.setFocusable(true);
        panel.requestFocusInWindow();

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
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
                isMouseReleased = true;
                setBackground(new Color(0, 0, 0, 0));
                panel.repaint();
                captureScreenshots(selection);

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

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    videoCaptureStop = true;
                }
            }
        });

        setTitle("Screenshot App");
        setResizable(false);
        panel.setSize(dim);
        add(panel);
        setVisible(true);
    }

    private Rectangle createSelectionRectangle(Point startPoint, Point endPoint) {
        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(endPoint.x - startPoint.x);
        int height = Math.abs(endPoint.y - startPoint.y);
        return new Rectangle(x, y, width, height);
    }

    private void captureScreenshots(Rectangle selection) {
        double delay = 1000.0 / frm.fps;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!videoCaptureStop) {
                    screenshot = FactoryUtils.captureScreenWithRobot(new Rectangle(selection.x, selection.y, selection.width, selection.height));
                    frm.listImage.add(screenshot);
                    //ImageProcess.saveImage(screenshot, "images/screen_capture/" + System.currentTimeMillis() + ".jpg");
                    try {
                        Thread.sleep((long) delay);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FrameCaptureVideo.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                FactoryUtils.copyImage2ClipBoard(screenshot);
                frm.setImage(screenshot);
                frm.setState(Frame.NORMAL);
                dispose();
            }
        }).start();
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
                new FrameCaptureVideo(null);
            }
        });
    }
}
