package misc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrameWithRectangle extends JFrame {

    private Rectangle rectangle;
    private Point startPoint;
    private Point endPoint;

    public FrameWithRectangle() {
        setTitle("Frame with Rectangle");
        setUndecorated(true); // Remove window decorations
        setSize(400, 400);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (rectangle != null) {
                    g.setColor(Color.RED);
                    g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
                }
            }
        };
        contentPane.setOpaque(false); // Make the panel transparent
        setContentPane(contentPane);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                endPoint = e.getPoint();
                int x = Math.min(startPoint.x, endPoint.x);
                int y = Math.min(startPoint.y, endPoint.y);
                int width = Math.abs(endPoint.x - startPoint.x);
                int height = Math.abs(endPoint.y - startPoint.y);
                rectangle = new Rectangle(x, y, width, height);
                contentPane.repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                int x = Math.min(startPoint.x, endPoint.x);
                int y = Math.min(startPoint.y, endPoint.y);
                int width = Math.abs(endPoint.x - startPoint.x);
                int height = Math.abs(endPoint.y - startPoint.y);
                rectangle = new Rectangle(x, y, width, height);
                contentPane.repaint();
            }
        };
        contentPane.addMouseListener(mouseAdapter);
        contentPane.addMouseMotionListener(mouseAdapter);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FrameWithRectangle::new);
    }
}
