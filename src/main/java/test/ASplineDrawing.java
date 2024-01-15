/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class ASplineDrawing extends JFrame {

    private EditableSplinePanel splinePanel;

    public ASplineDrawing() {
        setTitle("Düzenlenebilir Spline Çizimi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        splinePanel = new EditableSplinePanel();
        add(splinePanel);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ASplineDrawing());
    }

    private class EditableSplinePanel extends JPanel {

        private ArrayList<ArrayList<Point>> splines;
        private ArrayList<Point> currentSpline;
        private Point selectedPoint;

        public EditableSplinePanel() {
            splines = new ArrayList<>();
            currentSpline = new ArrayList<>();
            selectedPoint = null;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        if (!currentSpline.isEmpty()) {
                            splines.add(new ArrayList<>(currentSpline));
                            currentSpline.clear();
                        }
                    } else {
                        currentSpline.add(e.getPoint());
                    }
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    selectedPoint = findSelectedPoint(e.getPoint());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    selectedPoint = null;
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (selectedPoint != null) {
                        selectedPoint.setLocation(e.getPoint());
                        repaint();
                    }
                }
            });
        }

        private Point findSelectedPoint(Point mousePoint) {
            for (ArrayList<Point> spline : splines) {
                for (Point point : spline) {
                    if (point.distance(mousePoint) <= 5) { // Assuming a point is selected if it's within 5 pixels
                        return point;
                    }
                }
            }
            return null;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);

            // Set the stroke to a thicker line (20 pixels)
            g2d.setStroke(new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            for (ArrayList<Point> spline : splines) {
                g2d.setColor(Color.BLUE);
                if (spline.size() > 1) {
                    drawSpline(g2d, spline);
                }

                // Draw clicked points
                g2d.setColor(Color.RED);
                for (Point point : spline) {
                    g2d.fillOval(point.x - 5, point.y - 5, 10, 10);
                }
            }

            // Draw current spline
            g2d.setColor(Color.BLUE);
            if (currentSpline.size() > 1) {
                drawSpline(g2d, currentSpline);
            }

            // Draw clicked points for current spline
            g2d.setColor(Color.RED);
            for (Point point : currentSpline) {
                g2d.fillOval(point.x - 5, point.y - 5, 10, 10);
            }
        }

        private void drawSpline(Graphics2D g2d, ArrayList<Point> spline) {
            Path2D path = new Path2D.Double();
            path.moveTo(spline.get(0).getX(), spline.get(0).getY());

            for (int i = 0; i < spline.size() - 1; i++) {
                double xAvg = (spline.get(i).getX() + spline.get(i + 1).getX()) / 2;
                double yAvg = (spline.get(i).getY() + spline.get(i + 1).getY()) / 2;
                path.quadTo(spline.get(i).getX(), spline.get(i).getY(), xAvg, yAvg);
            }

            Point lastPoint = spline.get(spline.size() - 1);
            path.lineTo(lastPoint.getX(), lastPoint.getY());

            g2d.draw(path);
        }
    }
}
