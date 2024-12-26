/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package jazari.data_analytics.core;

/**
 *
 * @author cezerilab
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

public interface Plot {

    void setDimension(Dimension dimension);

    void setTitle(String title);

    void draw(Graphics2D g2);

    void setBackgroundColor(Color color);

    void addMouseListener(MouseListener ml);

    void addMouseMotionListener(MouseMotionListener mml);

    void addMouseWheelListener(MouseWheelListener mwl);

    JPopupMenu getPopupMenu();

    void resetView();

    Dimension getDimension();

    void paintComponent(Graphics g); // protected ->
}
