/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.gui;

import jazari.types.TFigureAttribute;
import jazari.matrix.CPoint;
import jazari.factory.FactoryUtils;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jazari.factory.FactoryMatrix;
import org.drjekyll.fontchooser.FontDialog;

/**
 *
 * @author BAP1
 */
public class PanelPlot extends javax.swing.JPanel implements MouseWheelListener {

    //private CMatrix cm;
    private float[] xAxis;
    private Color[] color;
    private boolean isShowPoint = false;
    private boolean lblShow = false;
    private Point mousePos = new Point(0, 0);
    private float scale = 1;
    private TFigureAttribute figureAttribute;
    private String[] items;
    private long rand_seed;
    private float alpha_blending = 0.65f;
    private boolean isMousePressed = false;
    Graphics2D gr;
    CPoint[][] mp;
    private int fromRight = 50;
    private int fromLeft = 100;
    private int fromTop = 50;
    private int fromBottom = 70;
    private int canvas_width = 300;
    private int canvas_height = 200;
    private boolean[] itemSelected;
    private AffineTransform afft = new AffineTransform();
    private AffineTransform temp_afft = new AffineTransform();
    private AffineTransform inverseScale = new AffineTransform();
    private float zoom = 1;
    private Color prevColor;
    private Stroke prevStroke;
    private AlphaComposite prevAlcom;
    private float prevAlpha;
    private boolean isDarkMode = true;
    private boolean isGridX = true;
    private boolean isGridY = true;
    private boolean isLegend = true;
    private Rectangle rectPlotCanvas;
    private Rectangle rectInnerCanvas = new Rectangle();
    private Rectangle rectTitle;
    private Rectangle rectAxisX;
    private Rectangle rectAxisY;
    private Rectangle rectLegend;
    private float[][] realValues;
    int[] vals;
    private Color colorLine = new Color(255, 255, 0);
    private int legendTextWidth;
    private int legendTextHeight;
    private int titleWidth;
    private int titleHeight;
    private CPoint selectedCPoint;
    private int selectedItemIndex;
    private int selectedLineIndex;
    private float selectedLineIndexValue;
    private float[][] data = null;
    private boolean isMouseDragged;
    Rectangle r1 = new Rectangle();

    public PanelPlot(float[][] data) {
        //this.data = FactoryMatrix.transpose(FactoryMatrix.clone(data));
        this.data = FactoryMatrix.transpose(data);
        if (figureAttribute == null) {
            items = new String[data[0].length];
            for (int i = 0; i < items.length; i++) {
                items[i] = "Line - " + (i + 1);
            }
            this.figureAttribute = new TFigureAttribute();
            this.figureAttribute.items = items;
        }
        initialize();
        //repaint();
    }

    public PanelPlot(float[][] data, float[] x) {
        this.xAxis = x;
        //this.data = FactoryMatrix.transpose(FactoryMatrix.clone(data));
        this.data = FactoryMatrix.transpose(data);
        items = new String[data[0].length];
        for (int i = 0; i < items.length; i++) {
            items[i] = "Line - " + (i + 1);
        }
        this.figureAttribute = new TFigureAttribute();
        initialize();
        //repaint();
    }

    public PanelPlot(float[][] data, TFigureAttribute attr) {
        //this.data = FactoryMatrix.transpose(FactoryMatrix.clone(data));
        this.data = FactoryMatrix.transpose(data);
        this.figureAttribute = attr;
        this.items = attr.items;
        initialize();
        //repaint();
    }

    public PanelPlot(float[][] data, TFigureAttribute attr, float[] x) {
        this.xAxis = x;
        //this.data = FactoryMatrix.transpose(FactoryMatrix.clone(data));
        this.data = FactoryMatrix.transpose(data);
        this.figureAttribute = attr;
        this.items = attr.items;
        initialize();
        //repaint();
    }

    public void setXAxis(float[] x) {
        this.xAxis = x;
        repaint();
    }

    @Override
    public void paint(Graphics g) {

        gr = (Graphics2D) g;
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Font fnt = gr.getFont();
        gr.setFont(new Font(fnt.getFontName(), 1, 14));
        int w = getWidth();
        int h = getHeight();
        int px = fromLeft;
        int py = h - 90;

        //ekranın tümünü dark mode veya beyaz ile boya
        if (isDarkMode) {
            gr.setColor(Color.DARK_GRAY);
        } else {
            gr.setColor(Color.white);
        }
        gr.fillRect(0, 0, w, h);

        drawTitle(gr, w);
        drawTitleAxisX(gr);
        drawTitleAxisY(gr);

        //çizim tuvali
        drawPlotCanvas(gr);

        mp = mappingDataToScreenCoordinates(data, fromLeft + 10, fromTop + 10, canvas_width - 20, canvas_height - 20);
        int[][] mappedVal = getMaxMinValue(mp);
        drawYAxis(gr, mp, mappedVal, px, py, w - fromRight - 20, fromRight, data);
        drawXAxis(gr, mp, mappedVal, px, py, w - fromRight, fromRight, data);
        for (int r = 0; r < items.length; r++) {
            drawPolyLines(gr, mp, r);
        }

        rectInnerCanvas.x = rectPlotCanvas.x + 10;
        rectInnerCanvas.y = rectPlotCanvas.y;
        rectInnerCanvas.width = rectPlotCanvas.width - 20;
        rectInnerCanvas.height = rectPlotCanvas.height;
        if (isMouseDragged && rectInnerCanvas.contains(mousePos)) {
            if (isDarkMode) {
                gr.setColor(Color.lightGray);
            }else{
                gr.setColor(Color.darkGray);
            }
            gr.drawLine(mousePos.x, rectPlotCanvas.y, mousePos.x, rectPlotCanvas.y + rectPlotCanvas.height);
            int pxx = mousePos.x;
            int index = Math.round(FactoryUtils.map(pxx, rectInnerCanvas.x, rectInnerCanvas.x + rectInnerCanvas.width, 0, data[0].length));
            for (int i = 0; i < mp.length; i++) {
                r1.x = mp[i][index].column - 4;
                r1.y = mp[i][index].row - 4;
                r1.width = 8;
                r1.height = 8;
                if (r1.contains(mousePos.x, mp[i][index].row)) {
                    if (isDarkMode) {
                        gr.setColor(Color.darkGray);
                        gr.fillRect(mousePos.x, mp[i][index].row - 10, 50, 17);
                        gr.setColor(color[i]);
                        if (xAxis == null) {
                            gr.drawString("" + index + "," + FactoryUtils.formatFloatAsString(data[i][index], 2), mousePos.x, mp[i][index].row);
                        } else {
                            gr.drawString("" + (index + xAxis[0]) + "," + FactoryUtils.formatFloatAsString(data[i][index], 2), mousePos.x, mp[i][index].row);
                        }
                    } else {
                        gr.setColor(Color.white);
                        gr.fillRect(mousePos.x, mp[i][index].row - 10, 50, 17);
                        gr.setColor(color[i]);
                        if (xAxis == null) {
                            gr.drawString("" + index + "," + FactoryUtils.formatFloatAsString(data[i][index], 2), mousePos.x, mp[i][index].row);
                        } else {
                            gr.drawString("" + (index + xAxis[0]) + "," + FactoryUtils.formatFloatAsString(data[i][index], 2), mousePos.x, mp[i][index].row);
                        }
                    }
                }
            }

        }

        if (isLegend) {
            drawLegend(gr);
        }

    }

    private void push(Color col) {
        prevColor = gr.getColor();
        gr.setColor(col);
        prevStroke = gr.getStroke();
        Stroke s = new BasicStroke(2.0f, // Width 
                BasicStroke.CAP_SQUARE, // End cap 
                BasicStroke.JOIN_MITER, // Join style 
                10.0f, // Miter limit 
                new float[]{5.0f, 5.0f}, // Dash pattern 
                0.0f);
        gr.setStroke(s);
        prevAlpha = 0.35f;
        prevAlcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, prevAlpha);
        gr.setComposite(prevAlcom);
    }

    private void pop() {
        prevAlpha = 1f;
        prevAlcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, prevAlpha);
        gr.setComposite(prevAlcom);
        gr.setStroke(prevStroke);
        gr.setColor(prevColor);
    }

    private CPoint[][] mappingDataToScreenCoordinates(float[][] d, int fromLeft, int fromTop, int w, int h) {
        CPoint[][] ret = new CPoint[d.length][d[0].length];
        float maxY = FactoryUtils.getMaximum(d);
        float minY = FactoryUtils.getMinimum(d);
        float deltaY = maxY - minY;
        float maxX = d[0].length;
        float cellWidth = w / (maxX - 1);
        float cellHeight = h / deltaY;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                CPoint p = new CPoint();
                p.column = (int) Math.round(fromLeft + (j) * cellWidth);
                p.row = (int) Math.round((fromTop + h) - (d[i][j] - minY) * cellHeight);
                ret[i][j] = p;
            }
        }
        return ret;
    }

    public void setMatrix(float[][] data) {
        this.data = FactoryMatrix.transpose(FactoryMatrix.clone(data));
        //this.data = data;
        rand_seed = System.currentTimeMillis();
        figureAttribute.items = generateItemText(this.data.length);
        color = FactoryUtils.getRandomColors(figureAttribute.items.length, rand_seed);
        repaint();
    }

    public void setMatrix(float[][] data, boolean isColorPersist) {
        this.data = FactoryMatrix.transpose(FactoryMatrix.clone(data));
        //this.data = data;
        rand_seed = System.currentTimeMillis();
        if (isColorPersist) {
            if (color == null) {
                color = FactoryUtils.getRandomColors(this.data.length, rand_seed);
            }
        } else {
            color = FactoryUtils.getRandomColors(this.data.length, rand_seed);
        }

        figureAttribute.items = generateItemText(this.data.length);
        repaint();
    }

    public float[][] getMatrix() {
        return data;
    }

    private void initialize() {
        realValues = FactoryUtils.formatFloat(data, 2);
        float[] yy = calculateYAxisLabels();
        int length = FactoryUtils.getLongestStringLength(FactoryUtils.formatFloat(data, 3));
        fromLeft = 50 + length * 5;
        fromRight = 50;
        fromTop = 50;
        fromBottom = 70;
        itemSelected = new boolean[items.length];
        //henüz paint olayı çağrılmadığı için panelin büyüklüğünü bilmiyor o yüzden width ve height olarak 100,100 değerleri girildi
        rectPlotCanvas = new Rectangle(fromLeft, fromRight, 100, 100);
        rectLegend = new Rectangle(0, 0, 100, 100);
        rectTitle = new Rectangle(0, 0, 100, 100);
        rectAxisX = new Rectangle(0, 0, 100, 100);
        rectAxisY = new Rectangle(0, 0, 100, 100);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {  // Added method for double click detection
                if (evt.getClickCount() == 2) {  // Check for double click
                    mousePos = evt.getPoint();
                    if (isLegend && rectLegend.contains(mousePos)) {
                        for (int i = 0; i < items.length; i++) {
                            Rectangle rectLine = new Rectangle(rectLegend.x + 10, rectLegend.y + (i * legendTextHeight) + 10, 30, legendTextHeight);
                            if (rectLine.contains(mousePos)) {
                                colorLine = JColorChooser.showDialog(null, "Choose Color for BoundingBox", colorLine);
                                color[i] = colorLine != null ? colorLine : color[i];
                                repaint();
                                return;
                            }
                            Rectangle textLine = new Rectangle(rectLegend.x + 50, rectLegend.y + (i * legendTextHeight) + 10, legendTextWidth, legendTextHeight);
                            if (textLine.contains(mousePos)) {
                                String st = JOptionPane.showInputDialog(null, "set legend text as", items[i]);
                                if (st != null) {
                                    items[i] = st;
                                }
                                repaint();
                                return;
                            }
                        }
                    }
                    if (rectPlotCanvas.contains(mousePos)) {
                        int selLineIndex = selectLine(mp);
                        if (selLineIndex != -1) {
                            itemSelected[selLineIndex] = !itemSelected[selLineIndex];
                        } else {
                            for (int i = 0; i < itemSelected.length; i++) {
                                itemSelected[i] = false;
                            }
                        }
                        vals = checkDataPoints(gr, mp, fromLeft, fromTop, canvas_width, canvas_height);
                        repaint();
                    } else if (rectTitle.contains(mousePos)) {
                        String st = JOptionPane.showInputDialog(null, "set title as", figureAttribute.title);
                        if (st != null) {
                            figureAttribute.title = st;
                        }
                        JLabel lbl = new JLabel();
                        Font temp = lbl.getFont();
                        FontDialog.showDialog(lbl);
                        if (!temp.equals(lbl.getFont())) {
                            figureAttribute.fontTitle = lbl.getFont();
                        }
                        repaint();
                    } else if (rectAxisX.contains(mousePos)) {
                        String st = JOptionPane.showInputDialog(null, "set axis name as", figureAttribute.axis_names[1]);
                        if (st != null) {
                            figureAttribute.axis_names[1] = st;
                        }
                        JLabel lbl = new JLabel();
                        Font temp = lbl.getFont();
                        FontDialog.showDialog(lbl);
                        if (!temp.equals(lbl.getFont())) {
                            figureAttribute.fontAxisX = lbl.getFont();
                        }
                        repaint();
                    } else if (rectAxisY.contains(mousePos)) {
                        String st = JOptionPane.showInputDialog(null, "set axis name as", figureAttribute.axis_names[0]);
                        if (st != null) {
                            figureAttribute.axis_names[0] = st;
                        }
                        JLabel lbl = new JLabel();
                        Font temp = lbl.getFont();
                        FontDialog.showDialog(lbl);
                        if (!temp.equals(lbl.getFont())) {
                            figureAttribute.fontAxisY = lbl.getFont();
                        }
                        repaint();
                    } else {
                        if (isMousePressed) {
                            isMousePressed = false;
                        }
                    }
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                mousePos = evt.getPoint();
                isMousePressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                isMouseDragged = false;
                //setCursor(Cursor.getDefaultCursor());
                repaint();
            }

        });

        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
            }

            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                mousePos = e.getPoint();
                isMouseDragged = true;
                repaint();
            }
        });

    }

    private int selectLine(CPoint[][] mp) {
        int ret = -1;
        int nr = mp.length;
        int nc = mp[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                if (mp[i][j].row > mousePos.y - 5
                        && mp[i][j].row < mousePos.y + 5
                        && mp[i][j].column > mousePos.x - 5
                        && mp[i][j].column < mousePos.x + 5) {
                    ret = i;
                    return ret;

                }
            }
        }
        return ret;
    }

    private String[] generateItemText(int n) {
        if (figureAttribute.items.length != 0) {
            return figureAttribute.items;
        }
        String[] ret = new String[n];
        for (int i = 0; i < n; i++) {
            ret[i] = "Item-" + (i + 1);
        }
        return ret;
    }

    private void drawPolyLines(Graphics2D gr, CPoint[][] mp, int nr) {
        gr.setColor(color[nr]);
        int nc = mp[0].length;
        int[] xp = new int[nc];
        int[] yp = new int[nc];
        for (int j = 0; j < nc; j++) {
            CPoint p = mp[nr][j];
            yp[j] = p.row;
            xp[j] = p.column;
        }
        String pType = figureAttribute.pointType;
        Stroke prev = gr.getStroke();
        Stroke stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        float alpha = this.alpha_blending;
        if (itemSelected[nr]) {
            alpha = 0.85f;
            stroke = new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        } else if (checkItemSelected()) {
            alpha = this.alpha_blending;
            stroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        }
        AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        gr.setComposite(alcom);

        if (pType.equals("-")) {
            if (figureAttribute.isStroke) {
                gr.setStroke(figureAttribute.stroke.get(nr));
                gr.drawPolyline(xp, yp, nc);
            } else {
                gr.setStroke(stroke);
                gr.drawPolyline(xp, yp, nc);
            }
        } else if (pType.equals(".")) {
            if (figureAttribute.isStroke) {
                gr.setStroke(figureAttribute.stroke.get(nr));
                gr.drawPolyline(xp, yp, nc);
            } else {
                gr.setStroke(stroke);
                int pointRadius = 2;
                for (int i = 0; i < xp.length - 1; i++) {
                    gr.fillOval(xp[i] - pointRadius, yp[i] - pointRadius, pointRadius * 2, pointRadius * 2);
                    gr.drawLine(xp[i], yp[i], xp[i + 1], yp[i + 1]);
                }
            }
        } else if (pType.equals("*")) {
            if (figureAttribute.isStroke) {
                gr.setStroke(figureAttribute.stroke.get(nr));
                gr.drawPolyline(xp, yp, nc);
            } else {
                gr.setStroke(stroke);
                for (int i = 0; i < xp.length - 1; i++) {
                    gr.drawString(pType, xp[i] - 4, yp[i] + 6);
                    gr.drawLine(xp[i], yp[i], xp[i + 1], yp[i + 1]);
                }
            }
        } else if (pType.equals("o")) {
            if (figureAttribute.isStroke) {
                gr.setStroke(figureAttribute.stroke.get(nr));
                gr.drawPolyline(xp, yp, nc);
            } else {
                gr.setStroke(stroke);
                int pointRadius = 4;
                for (int i = 0; i < xp.length - 1; i++) {
                    gr.drawOval(xp[i] - pointRadius, yp[i] - pointRadius, pointRadius * 2, pointRadius * 2);
                    gr.drawLine(xp[i], yp[i], xp[i + 1], yp[i + 1]);
                }
                gr.drawOval(xp[xp.length - 1] - pointRadius, yp[xp.length - 1] - pointRadius, pointRadius * 2, pointRadius * 2);
            }
        }
        if (isShowPoint) {
            paintDataPoints(gr, xp, yp);
        }
        alpha = 1f;
        alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        gr.setComposite(alcom);
        gr.setStroke(prev);
    }

    private void drawMouseAxis(Graphics2D gr, Point mousePos, int fromLeft, int fromTop, int canvas_width, int canvas_height) {
        Color prevColor = gr.getColor();
        gr.setColor(Color.red);
        Stroke prev = gr.getStroke();
        Stroke s = new BasicStroke(2.0f, // Width 
                BasicStroke.CAP_SQUARE, // End cap 
                BasicStroke.JOIN_MITER, // Join style 
                10.0f, // Miter limit 
                new float[]{5.0f, 5.0f}, // Dash pattern 
                0.0f);
        gr.setStroke(s);
        float alpha = 0.35f;
        AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        gr.setComposite(alcom);
        //gr.drawLine(fromLeft, mousePos.y, fromLeft + canvas_width, mousePos.y);
        gr.drawLine(mousePos.x - 3, fromTop, mousePos.x - 3, fromTop + canvas_height);

        alpha = 1f;
        alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        gr.setComposite(alcom);
        gr.setStroke(prev);
        gr.setColor(prevColor);
    }

    private int[] checkDataPoints(Graphics2D gr, CPoint[][] mp, int fromLeft, int fromTop, int canvas_width, int canvas_height) {
        int[] vals = new int[items.length];
        for (int i = 0; i < mp.length; i++) {
            for (int j = 0; j < mp[0].length; j++) {
                if (mp[i][j].column > mousePos.x - 5 && mp[i][j].column < mousePos.x + 5) {
                    gr.setColor(Color.red);
                    //red squares on the lines
                    gr.fillRect(mp[i][j].column - 1, mp[i][j].row - 1, 5, 5);

                    gr.drawRect(fromLeft - 55, mp[i][j].row - 11, 50, 20);
                    gr.drawRect(fromLeft + canvas_width + 2, mp[i][j].row - 11, 50, 20);
                    push(color[i]);
                    gr.drawString("" + FactoryUtils.formatFloat(data[i][j], 3), fromLeft - 50, mp[i][j].row + 6);
                    gr.drawString("" + FactoryUtils.formatFloat(data[i][j], 3), fromLeft + canvas_width + 5, mp[i][j].row + 6);
                    gr.drawLine(fromLeft, mp[i][j].row + 1, fromLeft + canvas_width, mp[i][j].row + 1);
                    gr.drawLine(mp[i][j].column + 1, fromTop, mp[i][j].column + 1, fromTop + canvas_height);
                    pop();

                    //red rectangles denoting index values on the X axis bottom and up
                    gr.drawRect(mp[i][j].column - 25, fromTop - 20, 50, 20);
                    gr.drawRect(mp[i][j].column - 25, fromTop + canvas_height + 3, 50, 20);
                    //red rectangles denoting y value  
//                    gr.drawRect(fromLeft-50, mousePos.y-10, 50, 20);
//                    gr.drawRect(fromLeft+canvas_width, mousePos.y-10, 50, 20);

                    gr.setColor(Color.gray);
                    gr.drawString("" + i, mp[i][j].column - 17, fromTop - 3);
                    gr.drawString("" + i, mp[i][j].column - 17, fromTop + canvas_height + 20);

//                    gr.drawString("" + FactoryUtils.formatDouble(cm.toDoubleArray2D()[i][j]), fromLeft-50, mousePos.y-10);
                    //lst.add(items[i] + ":" + cm.toDoubleArray2D()[i][j]);
                    vals[i] = i;
                    break;
                }
            }
        }
        return vals;
    }

    private void paintDataPoints(Graphics gr, int[] xp, int[] yp) {
        int r = 6;
        for (int i = 0; i < xp.length; i++) {
            gr.fillRect(xp[i] - r / 2, yp[i] - r / 2, r, r);
        }
    }

    private float[] calculateYAxisLabels() {
        float[][] d = data;
        int y = (getHeight() - 90);
        float n = Math.round(y / 100.0);
        float maxY = FactoryUtils.getMaximum(d);
        float minY = FactoryUtils.getMinimum(d);
        float deltaY = maxY - minY;
        float[] yVal = new float[(int) n + 1];
        float delta = deltaY / n;
        for (int i = 0; i <= n; i++) {
            if (maxY > 10) {
                yVal[i] = Math.round(Math.round((i * delta + minY) * n) / n);
            } else {
                yVal[i] = (i * delta + minY);
            }

        }
        return yVal;
    }

    private void drawYAxis(Graphics gr, CPoint[][] mp, int[][] mappedVal, int x0, int y0, int w, int fromRight, float[][] d) {
        float maxY = FactoryUtils.getMaximum(d);
        float minY = FactoryUtils.getMinimum(d);
        float deltaY = maxY - minY;
        float n = Math.round(y0 / 100.0);
        //int l = y0 - 50;
        int l = mappedVal[1][1] - mappedVal[1][0];
        //int top = 50;
        y0 = mappedVal[1][1];
        //gr.drawLine(x0, top, x0, y0);
        float delta = deltaY / n;
        float[] yVal = new float[(int) n + 1];
        float dY = l / n;
        int q = 0;
        int art = 5;
        int shift = 20;
        for (int i = 0; i <= n; i++) {
            if (isDarkMode) {
                gr.setColor(Color.lightGray);
            } else {
                gr.setColor(Color.darkGray);
            }
            if (maxY > 10) {
                yVal[i] = Math.round(Math.round((i * delta + minY) * n) / n);
            } else {
                yVal[i] = (i * delta + minY);
            }
            String val = FactoryUtils.formatFloatAsString(yVal[i], 3);
            gr.drawString(val, x0 - 10 - FactoryUtils.getGraphicsTextWidth(gr, val), (int) (y0 - i * dY) + art);
            gr.drawLine(x0 - 5, (int) (y0 - i * dY), x0, (int) (y0 - i * dY));

            if (isGridX) {
                q = (int) (y0 - i * dY + shift);
                if (isDarkMode) {
                    gr.setColor(Color.decode("#4F4F4F"));
                } else {
                    //gr.setColor(Color.white);
                    gr.setColor(Color.lightGray);
                }
                if (i < n) {
                    gr.drawLine(x0 + 2, q - (int) dY / 2, x0 + w - fromRight - 12, q - (int) dY / 2);
                }
                if (i > 0) {
                    gr.drawLine(x0 + 2, q, x0 + w - fromRight - 12, q);
                }
            }
        }
    }

    private void drawXAxis(Graphics gr, CPoint[][] mp, int[][] mappedVal, int x0, int y0, int w, int fromRight, float[][] d) {
        gr.setColor(Color.darkGray);
        int nc = d[0].length;
        float n = (Math.round(w / 150.0) >= nc) ? nc - 1 : Math.round(w / 150.0) - 1;
        float incr = nc / n;
        if (nc <= 10) {
            incr = 1;
            n = nc;
        }
        for (int i = 0; i <= n; i++) {
            if (nc <= 10 && i == nc) {
                continue;
            }
            if (isDarkMode) {
                gr.setColor(Color.lightGray);
            } else {
                gr.setColor(Color.darkGray);
            }
            int index = Math.round(i * incr);
            int offset = FactoryUtils.getGraphicsTextWidth(gr, index + "") / 2;
            if (i == n) {
                index = nc - 1;
            }
            if (xAxis == null || xAxis.length == 0) {
                gr.drawString("" + index, mp[0][index].column - offset, y0 + 40);
            } else {
                gr.drawString("" + FactoryUtils.formatFloatAsString(xAxis[index], 3), mp[0][index].column - offset, y0 + 40);
            }
            gr.drawLine(mp[0][index].column, y0 + 20, mp[0][index].column, y0 + 25);
            if (isGridY) {
                if (isDarkMode) {
                    gr.setColor(Color.decode("#4F4F4F"));
                } else {
                    gr.setColor(Color.lightGray);
                }
                gr.drawLine(mp[0][index].column, fromTop + 2, mp[0][index].column, y0 + 18);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>                        

    // Variables declaration - do not modify                     
    // End of variables declaration                   
    public void setScale(float scale) {
        this.scale = scale;
        repaint();
    }

    public float getScale() {
        return scale;
    }

    public void setPlotType(String type) {
        this.figureAttribute.pointType = type;
        repaint();
    }

    public void setFigureAttribute(TFigureAttribute figureAttribute) {
        this.figureAttribute = figureAttribute;
        if (figureAttribute.items != null || figureAttribute.items.length > 0) {
            this.items = figureAttribute.items;
        }
        //this.items=figureAttribute.items;
        repaint();
    }

    public TFigureAttribute getFigureAttribute() {
        return this.figureAttribute;
    }

    public void setRandomSeed(long currentTimeMillis) {
        this.rand_seed = currentTimeMillis;
        color = FactoryUtils.getRandomColors(data.length, rand_seed);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {

            Point2D p1 = e.getPoint();
            p1.setLocation(p1.getX(), getHeight() - p1.getY());
            Point2D p2 = null;
            try {
                p2 = afft.inverseTransform(p1, null);
            } catch (NoninvertibleTransformException ex) {
                ex.printStackTrace();
                return;
            }

            zoom -= (0.1 * e.getWheelRotation());
            zoom = (float) Math.max(0.01, zoom);

            afft.setToIdentity();
            afft.translate(p1.getX(), p1.getY());
            afft.scale(zoom, zoom);
            afft.translate(-p2.getX(), -p2.getY());

            try {
                inverseScale = afft.createInverse();
            } catch (NoninvertibleTransformException ex) {
                Logger.getLogger(PanelPlot.class.getName()).log(Level.SEVERE, null, ex);
            }

            revalidate();
            repaint();
        }
    }

    private Point2D getScreenCoordinate() {
        Point2D c = new Point2D.Double(
                mousePos.x,
                mousePos.y);
        Point2D screenPoint = inverseScale.transform(c, new Point2D.Double());
        screenPoint.setLocation(FactoryUtils.formatDouble(screenPoint.getX(), 2), FactoryUtils.formatDouble(screenPoint.getY(), 2));
        return screenPoint;
    }

    private boolean checkItemSelected() {
        boolean ret = false;
        for (int i = 0; i < items.length; i++) {
            if (itemSelected[i]) {
                return true;
            }
        }
        return ret;
    }

    public void setDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        repaint();
    }

    public void setGridy(boolean selected) {
        this.isGridY = selected;
        repaint();
    }

    public void setGridx(boolean selected) {
        this.isGridX = selected;
        repaint();
    }

    public void setPointType(String selected) {
        figureAttribute.pointType = selected;
        repaint();
    }

    public void setLegend(boolean selected) {
        isLegend = selected;
        repaint();
    }

    private void drawLegend(Graphics2D gr) {
        legendTextWidth = FactoryUtils.getMaxGraphicsTextWidth(gr, items);
        legendTextHeight = FactoryUtils.getMaxGraphicsTextHeight(gr, items);
        int w = legendTextWidth + 60;
        int px = rectPlotCanvas.x + rectPlotCanvas.width - w - 10;
        int py = rectPlotCanvas.y + 10;
        int h = (items.length + 1) * legendTextHeight;

        rectLegend.x = px;
        rectLegend.y = py;
        rectLegend.width = w;
        rectLegend.height = h;

        if (isDarkMode) {
            gr.setColor(Color.darkGray);
            gr.fillRoundRect(px, py, w, h, 5, 5);
            gr.setColor(Color.decode("#9F9F9F"));
            gr.drawRoundRect(px, py, w, h, 5, 5);
            gr.setColor(Color.decode("#BFBFBF"));
        } else {
            gr.setColor(Color.white);
            gr.fillRoundRect(px, py, w, h, 5, 5);
            gr.setColor(Color.decode("#BFBFBF"));
            gr.drawRoundRect(px, py, w, h, 5, 5);
            gr.setColor(Color.darkGray);
        }
        Stroke prev = gr.getStroke();
        Stroke stroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
        gr.setComposite(alcom);
        gr.setStroke(stroke);
        int k = 0;
        Color temp = gr.getColor();
        for (String item : items) {
            gr.setColor(color[k]);
            int yy = py + 20 + (k) * legendTextHeight;
            gr.drawLine(px + 10, yy - 5, px + 35, yy - 5);
            gr.setColor(temp);
            gr.drawString(item, px + 50, yy);
            k++;
        }
        alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
        gr.setComposite(alcom);
        gr.setStroke(prev);
    }

    private void drawTitle(Graphics2D gr, int w) {
        if (isDarkMode) {
            gr.setColor(Color.lightGray);
        } else {
            gr.setColor(Color.darkGray);
        }
        gr.setFont(figureAttribute.fontTitle);
        titleWidth = FactoryUtils.getGraphicsTextWidth(gr, figureAttribute.title);
        titleHeight = FactoryUtils.getGraphicsTextHeight(gr, figureAttribute.title);
        int pTitleX = (w + 30 - titleWidth) / 2;
        gr.drawString(figureAttribute.title, pTitleX, 30);
        rectTitle.x = pTitleX;
        rectTitle.y = 20;
        rectTitle.width = titleWidth;
        rectTitle.height = titleHeight;
        gr.setFont(FactoryUtils.getDefaultFont());
    }

    private void drawTitleAxisX(Graphics2D gr) {
        if (isDarkMode) {
            gr.setColor(Color.lightGray);
        } else {
            gr.setColor(Color.darkGray);
        }
        gr.setFont(figureAttribute.fontAxisX);
        int axis_width = FactoryUtils.getGraphicsTextWidth(gr, figureAttribute.axis_names[1]);
        int axis_height = FactoryUtils.getGraphicsTextHeight(gr, figureAttribute.axis_names[1]);
        int px = (getWidth() - axis_width) / 2;
        int py = getHeight() - 20;
        gr.drawString(figureAttribute.axis_names[1], px, py);
        rectAxisX.x = px;
        rectAxisX.y = py - 10;
        rectAxisX.width = axis_width;
        rectAxisX.height = axis_height;
        gr.setFont(FactoryUtils.getDefaultFont());
    }

    private void drawTitleAxisY(Graphics2D gr) {
        if (isDarkMode) {
            gr.setColor(Color.lightGray);
        } else {
            gr.setColor(Color.darkGray);
        }
        gr.setFont(figureAttribute.fontAxisY);
        int axis_width = FactoryUtils.getGraphicsTextWidth(gr, figureAttribute.axis_names[0]);
        int axis_height = FactoryUtils.getGraphicsTextHeight(gr, figureAttribute.axis_names[0]);
        int px = 30;
        int py = getHeight() - (getHeight() - axis_width) / 2 - 10;
        Point p = FactoryUtils.drawRotatedString(gr, figureAttribute.axis_names[0], px, py, -Math.PI / 2);
        rectAxisY.x = px - 10;
        rectAxisY.y = py - axis_width;
        //90 derece döndüğü için tersi alındı
        rectAxisY.width = axis_height;
        rectAxisY.height = axis_width;
        gr.setFont(FactoryUtils.getDefaultFont());
    }

    private void drawPlotCanvas(Graphics2D gr) {
        canvas_width = getWidth() - (fromLeft + fromRight);
        canvas_height = getHeight() - (fromBottom + fromTop);

        //plot alanını ilgili renkle boya
        rectPlotCanvas.x = fromLeft;
        rectPlotCanvas.y = fromTop;
        rectPlotCanvas.width = canvas_width;
        rectPlotCanvas.height = canvas_height;
        if (isDarkMode) {
            gr.setColor(Color.darkGray);
            gr.fillRect(fromLeft, fromTop, canvas_width, canvas_height);
            gr.setColor(Color.lightGray);
            gr.drawRect(fromLeft, fromTop, canvas_width, canvas_height);
        } else {
            // gr.setColor(Color.decode("#EAEAF2"));
            gr.setColor(Color.white);
            gr.fillRect(fromLeft, fromTop, canvas_width, canvas_height);
            gr.setColor(Color.black);
            gr.drawRect(fromLeft, fromTop, canvas_width, canvas_height);
        }
    }

    private int[][] getMaxMinValue(CPoint[][] d) {
        int[][] ret = new int[2][2];
        int maxX = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (d[i][j].row > maxY) {
                    maxY = d[i][j].row;
                }
                if (d[i][j].row < minY) {
                    minY = d[i][j].row;
                }
                if (d[i][j].column > maxX) {
                    maxX = d[i][j].column;
                }
                if (d[i][j].column < minX) {
                    minX = d[i][j].column;
                }
            }
        }
        ret[0][0] = minX;
        ret[0][1] = maxX;
        ret[1][0] = minY;
        ret[1][1] = maxY;
        return ret;
    }

}
