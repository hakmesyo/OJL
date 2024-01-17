/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.gui;

import jazari.types.TFigureAttribute;
import jazari.matrix.CMatrix;
import jazari.matrix.CPoint;
import jazari.factory.FactoryUtils;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
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
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.drjekyll.fontchooser.FontDialog;

/**
 *
 * @author BAP1
 */
public class PanelPlot extends javax.swing.JPanel implements MouseWheelListener {

    private CMatrix cm;
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
    private int selectedLineIndex;
    private int selectedLineValue;

    public PanelPlot(CMatrix ff) {
        this.cm = ff;
        if (figureAttribute == null) {
            items = new String[cm.getColumnNumber()];
            for (int i = 0; i < items.length; i++) {
                items[i] = "Line - " + (i + 1);
            }
            this.figureAttribute = new TFigureAttribute();
            this.figureAttribute.items = items;
        }
        initialize();
        //repaint();
    }

    public PanelPlot(CMatrix ff, float[] x) {
        this.xAxis = x;
        this.cm = ff;
        items = new String[cm.getColumnNumber()];
        for (int i = 0; i < items.length; i++) {
            items[i] = "Line - " + (i + 1);
        }
        this.figureAttribute = new TFigureAttribute();
        initialize();
        //repaint();
    }

    public PanelPlot(CMatrix ff, TFigureAttribute attr) {
        this.cm = ff;
        this.figureAttribute = attr;
        this.items = attr.items;
        initialize();
        //repaint();
    }

    public PanelPlot(CMatrix ff, TFigureAttribute attr, float[] x) {
        this.xAxis = x;
        this.cm = ff;
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

//        Point2D p2d = getScreenCoordinate();
//        System.out.println(p2d.getX() + "," + p2d.getY());
//        temp_afft = gr.getTransform();
//        gr.translate(0.0, h);      // Move the origin to the lower left for actual cartesian coordinate system
//        gr.scale(1.0, -1.0);       // Flip the sign of the coordinate system
        mp = mappingDataToScreenCoordinates(cm.toFloatArray2D(), fromLeft + 10, fromTop + 10, canvas_width - 20, canvas_height - 20);
        int[][] mappedVal = getMaxMinValue(mp);
        drawYAxis(gr, mappedVal, px, py, w - fromRight - 20, fromRight, cm.toFloatArray2D());
        drawXAxis(gr, mappedVal, px, py, w - fromRight, fromRight, cm.toFloatArray2D());
        for (int r = 0; r < items.length; r++) {
            drawPolyLines(gr, mp, r);
        }

        if (selectedCPoint != null) {
            if (isDarkMode) {
                gr.setColor(Color.darkGray);
                gr.fillRect(selectedCPoint.column + 10, selectedCPoint.row - 10, 90, 17);
                gr.setColor(Color.lightGray);
                if (xAxis == null) {
                    gr.drawString("( x=" + selectedLineIndex + " ; y=" + realValues[selectedLineIndex][selectedLineValue] + " )", selectedCPoint.column + 10, selectedCPoint.row);
                } else {
                    gr.drawString("( x=" + (selectedLineIndex + xAxis[0]) + " ; y=" + realValues[selectedLineIndex][selectedLineValue] + " )", selectedCPoint.column + 10, selectedCPoint.row);
                }
                gr.fillOval(selectedCPoint.column - 3, selectedCPoint.row - 3, 6, 6);
            } else {
                gr.setColor(Color.white);
                gr.fillRect(selectedCPoint.column + 10, selectedCPoint.row - 10, 90, 17);
                gr.setColor(Color.darkGray);
                if (xAxis == null) {
                    gr.drawString("( x=" + selectedLineIndex + " ; y=" + realValues[selectedLineIndex][selectedLineValue] + " )", selectedCPoint.column + 10, selectedCPoint.row);
                } else {
                    gr.drawString("( x=" + (selectedLineIndex + xAxis[0]) + " ; y=" + realValues[selectedLineIndex][selectedLineValue] + " )", selectedCPoint.column + 10, selectedCPoint.row);
                }
                gr.fillOval(selectedCPoint.column - 3, selectedCPoint.row - 3, 6, 6);
            }
        }

        if (isLegend) {
            drawLegend(gr);
        }

        //drawItems(gr, this.items, w, h, fromRight, fromTop);
//        if (isTraced) {
//            if (mousePos.x > fromLeft && mousePos.x < fromLeft + canvas_width && mousePos.y > fromTop && mousePos.y < fromTop + canvas_height) {
//                if (isMousePressed) {
//                    int selectedLineIndex = selectLine(mp);
//                    if (selectedLineIndex != -1) {
//                        itemSelected[selectedLineIndex] = !itemSelected[selectedLineIndex];
//                    } else {
//                        for (int i = 0; i < itemSelected.length; i++) {
//                            itemSelected[i] = false;
//                        }
//
//                    }
//                    isMousePressed = false;
//                    repaint();
//                }
//                //drawMouseAxis(gr, mousePos, fromLeft, fromTop, canvas_width, canvas_height);
//                vals = checkDataPoints(gr, mp, fromLeft, fromTop, canvas_width, canvas_height);
//                drawItems(gr, this.items, w, h, fromRight, fromTop);
//            } else {
//                if (isMousePressed) {
//                    isMousePressed = false;
//                }
//                drawItems(gr, this.items, w, h, fromRight, fromTop);
//            }
//        } else {
//            drawItems(gr, this.items, w, h, fromRight, fromTop);
//        }
        //gr.setStroke(new BasicStroke());
        //gr.setColor(Color.black);
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
        float maxY = getMaxYValue(d);
        float minY = getMinYValue(d);
        float deltaY = maxY - minY;
        float maxX = d.length;
        float cellWidth = w / maxX;
        float cellHeight = h / deltaY;
        for (int r = 0; r < d[0].length; r++) {
            for (int c = 0; c < d.length; c++) {
                CPoint p = new CPoint();
                if (c == 0) {
                    p.column = (int) Math.round(fromLeft + (c) * cellWidth);
                } else {
                    p.column = (int) Math.round(fromLeft + (c + 1) * cellWidth);
                }

                p.row = (int) Math.round((fromTop + h) - (d[c][r] - minY) * cellHeight);
                ret[c][r] = p;
            }
        }
        return ret;
    }

    public void setMatrix(CMatrix m) {
        this.cm = m.clone();
        rand_seed = System.currentTimeMillis();
        color = FactoryUtils.getRandomColors(cm.getRowNumber(), rand_seed);
        figureAttribute.items = generateItemText(cm.getRowNumber());
        repaint();
    }

    public CMatrix getMatrix() {
        return this.cm;
    }

    private void initialize() {
        realValues = FactoryUtils.formatFloat(cm.toFloatArray2D(), 2);
        float[] yy = calculateYAxisLabels();
        int length = FactoryUtils.getLongestStringLength(cm.formatFloat(3).toFloatArray1D());
        fromLeft = 30 + length * 5;
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
                        String st = JOptionPane.showInputDialog(null, "set axis name as", figureAttribute.axis_names[0]);
                        if (st != null) {
                            figureAttribute.axis_names[0] = st;
                        }
                        JLabel lbl = new JLabel();
                        Font temp = lbl.getFont();
                        FontDialog.showDialog(lbl);
                        if (!temp.equals(lbl.getFont())) {
                            figureAttribute.fontAxisX = lbl.getFont();
                        }
                        repaint();
                    } else if (rectAxisY.contains(mousePos)) {
                        String st = JOptionPane.showInputDialog(null, "set axis name as", figureAttribute.axis_names[1]);
                        if (st != null) {
                            figureAttribute.axis_names[1] = st;
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
//                isMousePressed = false;
            }

        });

        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
//                mousePos = e.getPoint();
//                Rectangle r1 = new Rectangle();
//                for (int i = 0; i < mp.length; i++) {
//                    for (int j = 0; j < mp[0].length; j++) {
//                        r1.x = mp[i][j].column - 4;
//                        r1.y = mp[i][j].row - 4;
//                        r1.width = 8;
//                        r1.height = 8;
//                        if (r1.contains(mousePos)) {
//                            selectedCPoint = mp[i][j];
//                            selectedLineIndex = i;
//                            selectedLineValue = j;
//                            //System.out.println(mousePos);
//                            repaint();
//                            return;
//                        }
//                    }
//                }
//                selectedCPoint = null;
//                selectedLineIndex = -1;
//                repaint();
            }

            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                mousePos = e.getPoint();
                Rectangle r1 = new Rectangle();
                for (int i = 0; i < mp.length; i++) {
                    for (int j = 0; j < mp[0].length; j++) {
                        r1.x = mp[i][j].column - 4;
                        r1.y = mp[i][j].row - 4;
                        r1.width = 8;
                        r1.height = 8;
                        if (r1.contains(mousePos)) {
                            selectedCPoint = mp[i][j];
                            selectedLineIndex = i;
                            selectedLineValue = j;
                            //System.out.println(mousePos);
                            repaint();
                            return;
                        }
                    }
                }
                selectedCPoint = null;
                selectedLineIndex = -1;
                repaint();
                System.gc();
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
                    ret = j;
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
        int[] xp = new int[cm.getRowNumber()];
        int[] yp = new int[cm.getRowNumber()];
        for (int j = 0; j < cm.getRowNumber(); j++) {
            CPoint p = mp[j][nr];
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
                gr.drawPolyline(xp, yp, cm.getRowNumber());
            } else {
                gr.setStroke(stroke);
                gr.drawPolyline(xp, yp, cm.getRowNumber());
            }
        } else if (pType.equals(".")) {
            if (figureAttribute.isStroke) {
                gr.setStroke(figureAttribute.stroke.get(nr));
                gr.drawPolyline(xp, yp, cm.getRowNumber());
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
                gr.drawPolyline(xp, yp, cm.getRowNumber());
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
                gr.drawPolyline(xp, yp, cm.getRowNumber());
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
                    push(color[j]);
                    gr.drawString("" + FactoryUtils.formatFloat(cm.toFloatArray2D()[i][j], 3), fromLeft - 50, mp[i][j].row + 6);
                    gr.drawString("" + FactoryUtils.formatFloat(cm.toFloatArray2D()[i][j], 3), fromLeft + canvas_width + 5, mp[i][j].row + 6);
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
                    vals[j] = i;
                    break;
                }
            }
        }
        /*
        float alpha = 0.65f;
        AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        gr.setComposite(alcom);
        gr.setColor(Color.gray);
        int index=FactoryUtils.getLongestStringIndex(items);
        int itemWidth = FactoryUtils.getGraphicsTextWidth(gr, items[index]);
        int n = items.length;
        gr.fillRect(mousePos.x+5, mousePos.y+5, (int)(itemWidth*2.5), 30 * n);
        gr.setColor(Color.blue);
        for (int i = 0; i < items.length; i++) {
            gr.drawString(items[i]+":"+FactoryUtils.formatDouble(cm.toDoubleArray2D()[i][vals[i]],2), mousePos.x+10, mousePos.y+30+i*30);
        }        
        alpha = 1f;
        alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        gr.setComposite(alcom);
         */
        return vals;
    }

    private void paintDataPoints(Graphics gr, int[] xp, int[] yp) {
        int r = 6;
        for (int i = 0; i < xp.length; i++) {
            gr.fillRect(xp[i] - r / 2, yp[i] - r / 2, r, r);
        }
    }

    private float[] calculateYAxisLabels() {
        float[][] d = cm.toFloatArray2D();
        int y = (getHeight() - 90);
        float n = Math.round(y / 100.0);
        float maxY = getMaxYValue(d);
        float minY = getMinYValue(d);
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

    private void drawYAxis(Graphics gr, int[][] mappedVal, int x0, int y0, int w, int fromRight, float[][] d) {
        float maxY = getMaxYValue(d);
        float minY = getMinYValue(d);
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
                    gr.drawLine(x0 + 2, q - (int) dY / 2, x0 + w - fromRight + 4, q - (int) dY / 2);
                }
                if (i > 0) {
                    gr.drawLine(x0 + 2, q, x0 + w - fromRight + 4, q);
                }
            }
        }
    }

    private void drawXAxis(Graphics gr, int[][] mappedVal, int x0, int y0, int w, int fromRight, float[][] d) {
        gr.setColor(Color.darkGray);
        float maxX = 0;
        float minX = 0;
        float n = Math.round(w / 150.0);
        //int l = w - (x0 + fromRight);
        int l = mappedVal[0][1] - mappedVal[0][0];
        x0 = mappedVal[0][0];
        float dx = l / n;
        float[] xVal = new float[(int) n + 1];
        int top = 50;
        if (this.xAxis != null && this.xAxis.length != 0) {
            maxX = FactoryUtils.getMaximum(this.xAxis);
            minX = FactoryUtils.getMinimum(this.xAxis);
            maxX = Math.abs(maxX - minX);
        } else {
            maxX = d.length;
        }
        float delta = maxX / n;
        //gr.drawString(figureAttribute.axis_names[0], w - 110, y0 + 40);
        int q = 0;
        for (int i = 0; i <= n; i++) {
            if (isDarkMode) {
                gr.setColor(Color.lightGray);
            } else {
                gr.setColor(Color.darkGray);
            }
            if (i < n) {
                xVal[i] = (Math.round(minX + i * delta));
            } else {
                xVal[i] = (Math.round(minX + i * delta)) - 1;
            }

            q = (int) (x0 + i * dx);
            gr.drawString(FactoryUtils.formatFloatAsString(xVal[i], 3), q - 10, y0 + 40);
            gr.drawLine(q, y0 + 20, q, y0 + 25);
            if (isGridY) {
                if (isDarkMode) {
                    gr.setColor(Color.decode("#4F4F4F"));
                } else {
                    gr.setColor(Color.lightGray);
                }
                if (i < n) {
                    gr.drawLine(q + (int) dx / 2, top + 2, q + (int) dx / 2, y0 + 18);
                }
                if (i > 0) {
                    gr.drawLine(q, top + 2, q, y0 + 18);
                }
            }

        }
    }

    private float getMaxYValue(float[][] d) {
        float ret = 0;
        ret = d[0][0];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (ret < d[i][j]) {
                    ret = d[i][j];
                }
            }
        }
        return ret;
    }

    private float getMinYValue(float[][] d) {
        float ret = 0;
        ret = d[0][0];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (ret > d[i][j]) {
                    ret = d[i][j];
                }
            }
        }
        return ret;
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
        color = FactoryUtils.getRandomColors(cm.getRowNumber(), rand_seed);
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
        int axis_width = FactoryUtils.getGraphicsTextWidth(gr, figureAttribute.axis_names[0]);
        int axis_height = FactoryUtils.getGraphicsTextHeight(gr, figureAttribute.axis_names[0]);
        int px = (getWidth() - axis_width) / 2;
        int py = getHeight() - 20;
        gr.drawString(figureAttribute.axis_names[0], px, py);
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
        int axis_width = FactoryUtils.getGraphicsTextWidth(gr, figureAttribute.axis_names[1]);
        int axis_height = FactoryUtils.getGraphicsTextHeight(gr, figureAttribute.axis_names[1]);
        int px = 30;
        int py = getHeight() - (getHeight() - axis_width) / 2-10;
        Point p = FactoryUtils.drawRotatedString(gr, figureAttribute.axis_names[1], px, py, -Math.PI / 2);
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
