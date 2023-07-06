package misc;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

public class TransparentFrameExample {

    private static Point startPoint;
    private static Point endPoint;
    private static Rectangle customRectangle;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Frame frame = new Frame();
            frame.setSize(300, 250);
            frame.setLocationRelativeTo(null);
            frame.setUndecorated(true);
            frame.setAlwaysOnTop(true);
            //frame.setOpacity(0.7f);
            frame.setBackground(new Color(0, 0, 0, 255));
            Random rnd=new Random();
            Panel panel = new Panel() {
                @Override
                public void paint(Graphics g) {
                    super.paint(g);
                    //g.setColor(Color.RED);
                    //g.drawRect(rnd.nextInt(200), rnd.nextInt(150), 200, 150);
                    if (customRectangle != null) {
                        g.setColor(Color.BLUE);
                        g.drawRect(
                                customRectangle.x,
                                customRectangle.y,
                                customRectangle.width,
                                customRectangle.height
                        );
                    }
                }
            };

            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    startPoint = e.getPoint();
                    endPoint = startPoint;
                    customRectangle = null;
                    panel.repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    endPoint = e.getPoint();
                    customRectangle = createCustomRectangle(startPoint, endPoint);
                    //frame.setOpacity(0.1f);
                    frame.setBackground(new Color(0, 0, 0, 100));
                    panel.repaint();
                }
            });

            panel.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    endPoint = e.getPoint();
                    customRectangle = createCustomRectangle(startPoint, endPoint);
                    panel.repaint();
                }
            });

            panel.setSize(300, 250);

            frame.add(panel);
            frame.setVisible(true);
        });
    }

    private static Rectangle createCustomRectangle(Point startPoint, Point endPoint) {
        int x = Math.min(startPoint.x, endPoint.x);
        int y = Math.min(startPoint.y, endPoint.y);
        int width = Math.abs(endPoint.x - startPoint.x);
        int height = Math.abs(endPoint.y - startPoint.y);
        return new Rectangle(x, y, width, height);
    }
}
