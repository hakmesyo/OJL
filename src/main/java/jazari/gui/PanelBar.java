/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.gui;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryNormalization;
import jazari.matrix.CPoint;
import jazari.types.TFigureAttribute;
import org.drjekyll.fontchooser.FontDialog;

/**
 *
 * @author BAP1
 */
public class PanelBar extends javax.swing.JPanel {

    private float[][] data;
    private float[][] originalData;
    private float scale = 1;
    private Color[] color;
    private String[] labels;
    int max_label_index = -1;
    private boolean isValueVisible = false;
    private FrameBar frm;
    private boolean isDarkMode = true;
    private boolean isGridX = true;
    private boolean isGridY = true;
    private boolean isLegend = true;
    private int fromRight = 0;
    private int fromLeft = 0;
    private int fromTop = 0;
    private int fromBottom = 0;
    private int canvas_width = 0;
    private int canvas_height = 0;
    private TFigureAttribute figureAttribute;
    private int legendTextWidth;
    private int legendTextHeight;
    private int titleWidth;
    private int titleHeight;
    private Rectangle rectPlotCanvas;
    private Rectangle rectTitle;
    private Rectangle rectAxisX;
    private Rectangle rectAxisY;
    private Rectangle rectLegend;
    private float[][] realValues;
    //private CMatrix cm;
    private CPoint[][] mp;
    private String[] items;
    private long rand_seed;
    private boolean isTransposed;
    private Point mousePos;
    private float minValue;
    private boolean isXAxisTitleVisible = true;
    private boolean isYAxisTitleVisible = true;
    private boolean isFillBar = false;
    private Color colorLine = new Color(255, 255, 0);
    private boolean isMousePressed = false;
    private int virtualXAxisPosY;
    private Graphics2D gr;
    private int[][] mappedVal = null;

    public PanelBar(FrameBar frm, float[][] data, TFigureAttribute attr, String[] labels, String[] items, boolean isValueVisible) {
        this.data = data;
        this.frm = frm;
        this.isValueVisible = isValueVisible;
        this.originalData = (FactoryMatrix.clone(data));
//        this.originalData = FactoryMatrix.transpose(FactoryMatrix.clone(data));
//        this.data = FactoryNormalization.normalizeMax(FactoryMatrix.clone(data));
//        this.data = FactoryMatrix.transpose(this.data);
        if (attr != null) {
            figureAttribute = attr;
            if (figureAttribute.labels != null) {
                this.labels = figureAttribute.labels;
                max_label_index = getMaximumLengthIndex(this.labels);
            } else {
                this.labels = generateArtificialLabels(data.length);
            }
            if (figureAttribute.items != null) {
                this.items = figureAttribute.items;
            } else {
                this.items = new String[data.length];
                for (int i = 0; i < this.items.length; i++) {
                    this.items[i] = "Bar-" + (i + 1);
                }
            }
        } else {
            if (labels != null) {
                this.labels = labels;
                max_label_index = getMaximumLengthIndex(labels);
            } else {
                this.labels = generateArtificialLabels(data.length);
            }
            if (items != null) {
                this.items = items;
            } else {
                this.items = new String[data[0].length];
                for (int i = 0; i < this.items.length; i++) {
                    this.items[i] = "Bar-" + (i + 1);
                }
            }
            this.figureAttribute = new TFigureAttribute();
            this.figureAttribute.items = items;
            this.figureAttribute.labels = labels;
            this.figureAttribute.title = "Bar Plot";

        }
        color = FactoryUtils.generateColor(this.items.length);
        initialize();
    }

//    public float[][] getData() {
//        return getMatrix().getArray2Dfloat();
//    }
//
//    public void setData(CMatrix cm) {
//        getMatrix().setArray(cm.array);
//        this.data = FactoryNormalization.normalizeMax(FactoryMatrix.clone(getMatrix().getArray2Dfloat()));
//        this.data = FactoryMatrix.transpose(this.data);
//        this.originalData = FactoryMatrix.transpose(FactoryMatrix.clone(getMatrix().getArray2Dfloat()));
//        repaint();
//    }
    private void initialize() {
        minValue = FactoryUtils.getMinimum(data);
        int length = FactoryUtils.getLongestStringLength(FactoryUtils.formatFloat(data, 3));
        fromLeft = 50 + length * 5;
        fromRight = 20;
        fromTop = 100;
        fromBottom = 70;

        //henüz paint olayı çağrılmadığı için panelin büyüklüğü bilinmiyor o yüzden width ve height olarak 100,100 değerleri girildi
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
                        int wx = 0;
                        int xx = 0;
                        for (int i = 0; i < items.length; i++) {
                            if (i > 0) {
                                wx = 30 + FactoryUtils.getGraphicsTextWidth(gr, items[i - 1]);
                            }
                            xx += wx;
                            Rectangle rectLine = new Rectangle(rectLegend.x + xx + 10, rectLegend.y + 10, 10, 10);
                            if (rectLine.contains(mousePos)) {
                                colorLine = JColorChooser.showDialog(null, "Choose Color for BoundingBox", colorLine);
                                color[i] = colorLine != null ? colorLine : color[i];
                                repaint();
                                return;
                            }
                            Rectangle textLine = new Rectangle(rectLegend.x + xx + 25, rectLegend.y + 10, FactoryUtils.getGraphicsTextWidth(gr, items[i]), FactoryUtils.getGraphicsTextHeight(gr, items[i]));
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
//                    if (rectPlotCanvas.contains(mousePos)) {
//                        int selLineIndex = selectLine(mp);
//                        if (selLineIndex != -1) {
//                            itemSelected[selLineIndex] = !itemSelected[selLineIndex];
//                        } else {
//                            for (int i = 0; i < itemSelected.length; i++) {
//                                itemSelected[i] = false;
//                            }
//                        }
//                        vals = checkDataPoints(gr, mp, fromLeft, fromTop, canvas_width, canvas_height);
//                        repaint();
//                    } else 
                    if (rectTitle.contains(mousePos)) {
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
//                isMousePressed = false;
            }

        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                mousePos = e.getPoint();
//                int px=(int)FactoryUtils.map(mousePos.x, 0, getWidth(), 0, canvas_width);
//                int py=(int)FactoryUtils.map(mousePos.y, 0, getHeight(), 0  , canvas_height);
//                frm.setTitle("Bar Plot  ["+px+","+py+"]");
                frm.setTitle("Bar Plot  [" + mousePos.x + "," + mousePos.y + "]");
                //System.out.println(mousePos);
                //repaint();
            }
        });
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
        canvas_width = getWidth() - (fromLeft + fromRight);
        canvas_height = getHeight() - (fromBottom + fromTop);
        int py = fromTop + canvas_height;

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

        //float[][] d = cm.toFloatArray2D();
        mp = mappingDataToScreenCoordinates(data, fromLeft + 10, fromTop + 15, canvas_width - 20, canvas_height - 30);
        mappedVal = getMaxMinValue(mp);
        drawPlotCanvas(gr);
        drawYAxis(gr, px, py, canvas_width, fromRight, data);
        drawXAxis(gr, mp, mappedVal, px, py, w - fromRight, fromRight, data);
        drawBars(gr, mp, mappedVal, py, data);
        if (isLegend) {
            drawLegend(gr);
        }

    }

    private void drawYAxis(Graphics gr, int px, int taban, int w, int fromRight, float[][] d) {
        float maxY = FactoryUtils.getMaximum(d) * 1.05f;
        float minY = FactoryUtils.getMinimum(d);
        int sensitivity = FactoryUtils.getDigitSensitivity(Math.abs(maxY - minY));
        int art = 5;
        if (minY > 0) {
            minY = 0;
        } else {
            if (maxY < 0) {
                maxY = 0;
                virtualXAxisPosY = (int) (taban - canvas_height);
            } else {
                minY = minY * 1.05f;
                virtualXAxisPosY = (int) (taban - (1.0f * Math.abs(minY) * canvas_height / Math.abs(maxY - minY)));
            }
        }
        float deltaY = Math.abs(maxY - minY);
        float n = Math.round(taban / 50.0);
        float delta = deltaY / n;
        float[] yVal = new float[(int) n + 1];
        float dY = canvas_height / n;
        int q = 0;
        int shift = 20;

        for (int i = 0; i <= n; i++) {
            if (isDarkMode) {
                gr.setColor(Color.lightGray);
            } else {
                gr.setColor(Color.darkGray);
            }
            yVal[i] = (i * delta + minY);
            String val = FactoryUtils.formatFloatAsString(yVal[i], sensitivity);
            gr.drawString(val, px - 10 - FactoryUtils.getGraphicsTextWidth(gr, val), (int) (taban - i * dY) + art);
            gr.drawLine(px - 5, (int) (taban - i * dY), px, (int) (taban - i * dY));

            if (i > 0 && i < n && isGridX) {
                q = (int) (taban - i * dY + shift);
                if (isDarkMode) {
                    gr.setColor(Color.decode("#4F4F4F"));
                } else {
                    gr.setColor(Color.lightGray);
                }
                gr.drawLine(px + 2, (int) (taban - i * dY), px + w - 2, (int) (taban - i * dY));
            }
        }
    }

    private void drawXAxis(Graphics gr, CPoint[][] mp, int[][] mappedVal, int x0, int taban, int w, int fromRight, float[][] d) {
        //float min = FactoryUtils.getMinimum(cm.toFloatArray2D());
        //float max = FactoryUtils.getMaximum(cm.toFloatArray2D());
        //float pos_y = mappedVal[1][1] - FactoryUtils.map(0, min, max, mappedVal[1][0], mappedVal[1][1]) + 5;

        gr.setColor(Color.darkGray);
        //float n = (Math.round(w / 150.0) >= d.length) ? d.length - 1 : Math.round(w / 150.0) - 1;
        float n = d.length;
        //float incr = d.length / n;
        if (d.length <= 10) {
            //incr = 1;
            n = d.length;
        }
        int offset = 0;
        int rw = mappedVal[0][1] - mappedVal[0][0];
        float deltaX = 1.0f * rw / d.length;
        if (isGridY) {
            if (isDarkMode) {
                gr.setColor(Color.decode("#4F4F4F"));
            } else {
                gr.setColor(Color.lightGray);
            }
            for (int i = 0; i < n + 1; i++) {
                offset = mappedVal[0][0] + Math.round(i * deltaX);
                gr.drawLine(offset, fromTop + 2, offset, taban - 2);
            }
        }
        if (isDarkMode) {
            gr.setColor(Color.lightGray);
        } else {
            gr.setColor(Color.darkGray);
        }

        for (int i = 0; i < n; i++) {
            offset = mappedVal[0][0] + Math.round(i * deltaX + (deltaX - FactoryUtils.getGraphicsTextWidth(gr, labels[i] + "")) / 2);
            gr.drawString(labels[i], offset, taban + 20);
        }
    }

    private void drawBars(Graphics2D gr, CPoint[][] mp, int[][] mpv, int taban, float[][] d) {
        float min = FactoryUtils.getMinimum(data);
        float max = FactoryUtils.getMaximum(data);
        int sensitivity = FactoryUtils.getDigitSensitivity(Math.abs(max - min));

        //int pos_y = (int) (mpv[1][1] - FactoryUtils.map(0, min, max, mpv[1][0], mpv[1][1])) + 8;
        int pos_y = virtualXAxisPosY;
        if (min < 0) {
            if (isDarkMode) {
                gr.setColor(Color.lightGray);
            } else {
                gr.setColor(Color.darkGray);
            }
            gr.drawLine(fromLeft - 5, pos_y, fromLeft + canvas_width, pos_y);
            gr.drawString("0", fromLeft - 16, pos_y + 5);
        }

        int n = d.length;
        int rw = mpv[0][1] - mpv[0][0];
        int m = d[0].length;
        float deltaX = 1.0f * rw / n;
        int offset = 0;
        for (int r = 0; r < items.length; r++) {
            for (int i = 0; i < n; i++) {
                gr.setColor(color[r]);
                offset = mpv[0][0] + Math.round((i * deltaX) + (r + 1) * (deltaX / (m + 2)));
                if (min > 0) {
                    if (!isFillBar) {
                        updateStrokeAlpha(gr, 10, 0.1f);
                        gr.drawRect(offset, mp[i][r].row, (int) (deltaX / (m + 2)), Math.abs(taban - mp[i][r].row));
                    } else {
                        updateStrokeAlpha(gr, 10, 0.5f);
                        gr.fillRect(offset, mp[i][r].row, (int) (deltaX / (m + 2)), Math.abs(taban - mp[i][r].row));
                    }
                    updateStrokeAlpha(gr, 1, 1f);
                    gr.drawRect(offset, mp[i][r].row, (int) (deltaX / (m + 2)), Math.abs(taban - mp[i][r].row));
                } else {
                    if (mp[i][r].weight > 0) {
                        if (!isFillBar) {
                            updateStrokeAlpha(gr, 10, 0.1f);
                            gr.drawRect(offset, mp[i][r].row, (int) (deltaX / (m + 2)), Math.abs(pos_y - mp[i][r].row));
                        } else {
                            updateStrokeAlpha(gr, 10, 0.5f);
                            gr.fillRect(offset, mp[i][r].row, (int) (deltaX / (m + 2)), Math.abs(pos_y - mp[i][r].row));
                        }
                        updateStrokeAlpha(gr, 1, 1f);
                        gr.drawRect(offset, mp[i][r].row, (int) (deltaX / (m + 2)), Math.abs(pos_y - mp[i][r].row));
                    } else {
                        if (!isFillBar) {
                            updateStrokeAlpha(gr, 10, 0.1f);
                            gr.drawRect(offset, pos_y, (int) (deltaX / (m + 2)), Math.abs(pos_y - mp[i][r].row));
                        } else {
                            updateStrokeAlpha(gr, 10, 0.5f);
                            gr.fillRect(offset, pos_y, (int) (deltaX / (m + 2)), Math.abs(pos_y - mp[i][r].row));
                        }
                        updateStrokeAlpha(gr, 1, 1f);
                        gr.drawRect(offset, pos_y, (int) (deltaX / (m + 2)), Math.abs(pos_y - mp[i][r].row));
                    }
                }
                if (isValueVisible) {
                    if (isDarkMode) {
                        gr.setColor(Color.lightGray);
                    } else {
                        gr.setColor(Color.darkGray);
                    }
                    String s = FactoryUtils.formatFloatAsString(mp[i][r].weight, sensitivity);
                    int w = FactoryUtils.getGraphicsTextWidth(gr, s);
                    if (mp[i][r].weight > 0) {
                        gr.drawString(s, offset + ((int) (deltaX / (m + 2)) - w) / 2, mp[i][r].row - 5);
                    } else {
                        gr.drawString(s, offset + ((int) (deltaX / (m + 2)) - w) / 2, mp[i][r].row + 12);
                    }

                }
            }
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

    private CPoint[][] mappingDataToScreenCoordinates(float[][] d, int fromLeft, int fromTop, int w, int h) {
        CPoint[][] ret = new CPoint[d.length][d[0].length];
        float maxY = FactoryUtils.getMaximum(d);
        float minY = FactoryUtils.getMinimum(d);
        if (minY > 0) {
            minY = 0;
        }
        if (maxY < 0) {
            maxY = 0;
        }
        float deltaY = maxY - minY;
        float maxX = d.length;
        float cellWidth = w / (maxX - 1);
        float cellHeight = h / deltaY;
        for (int r = 0; r < d[0].length; r++) {
            for (int c = 0; c < d.length; c++) {
                CPoint p = new CPoint();
                p.column = (int) Math.round(fromLeft + (c) * cellWidth);
                p.row = (int) Math.round((fromTop + h) - (d[c][r] - minY) * cellHeight);
                p.weight = d[c][r];
                ret[c][r] = p;
            }
        }
        return ret;
    }

    public void setMatrix(float[][] data) {
        this.data = data;
        rand_seed = System.currentTimeMillis();
        figureAttribute.items = generateItemText(data[0].length);
        color = FactoryUtils.getRandomColors(figureAttribute.items.length, rand_seed);
        repaint();
    }

    public void setMatrix(float[][] data, String[] labels) {
        this.data = data;
        this.labels = labels;
        rand_seed = System.currentTimeMillis();
        color = FactoryUtils.getRandomColors(data.length, rand_seed);
        figureAttribute.items = generateItemText(data.length);
        repaint();
    }

    private String[] generateItemText(int n) {
        if (figureAttribute.items!=null && figureAttribute.items.length != 0) {
            return figureAttribute.items;
        }
        String[] ret = new String[n];
        for (int i = 0; i < n; i++) {
            ret[i] = "Item-" + (i + 1);
        }
        return ret;
    }

    public void setMatrix(float[][] data, boolean isColorPersist) {
        this.data = data;
        rand_seed = System.currentTimeMillis();
        if (isColorPersist) {
            if (color == null) {
                color = FactoryUtils.getRandomColors(data.length, rand_seed);
            }
        } else {
            color = FactoryUtils.getRandomColors(data.length, rand_seed);
        }

        figureAttribute.items = generateItemText(data.length);
        repaint();
    }

    public float[][] getMatrix() {
        return this.data;
    }

    private void drawLegend(Graphics2D gr) {
        legendTextWidth = FactoryUtils.getMaxGraphicsTextSeriesWidth(gr, items);
        legendTextHeight = FactoryUtils.getMaxGraphicsTextHeight(gr, items);
        int w = legendTextWidth;
        int px = rectPlotCanvas.x + (rectPlotCanvas.width - w) / 2;
        int py = rectPlotCanvas.y - 40;
        int h = 2 * legendTextHeight;

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
        int wx = 0;
        int xx = 0;
        for (String item : items) {
            if (k > 0) {
                wx = 30 + FactoryUtils.getGraphicsTextWidth(gr, items[k - 1]);
            }
            xx += wx;
            gr.setColor(color[k++]);
            gr.drawRect(px + xx + 10, py + 10, 10, 10);
            gr.setColor(temp);
            gr.drawString(item, px + xx + 25, py + 20);
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
        if (figureAttribute.fontTitle == null) {
            gr.setFont(gr.getFont().deriveFont(20.0f));
        } else {
            gr.setFont(figureAttribute.fontTitle);
        }
        titleWidth = FactoryUtils.getGraphicsTextWidth(gr, figureAttribute.title);
        titleHeight = FactoryUtils.getGraphicsTextHeight(gr, figureAttribute.title);
        int pTitleX = fromLeft + (canvas_width - titleWidth) / 2;
        gr.drawString(figureAttribute.title, pTitleX, 40);
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
        if (figureAttribute.fontAxisX == null) {
            gr.setFont(gr.getFont().deriveFont(Font.ITALIC, 16.0f));
        } else {
            gr.setFont(figureAttribute.fontAxisX);
        }
        int axis_width = FactoryUtils.getGraphicsTextWidth(gr, figureAttribute.axis_names[1]);
        int axis_height = FactoryUtils.getGraphicsTextHeight(gr, figureAttribute.axis_names[1]);
        int px = 70 + fromRight + (canvas_width - axis_width) / 2;
        int py = getHeight() - 20;
        if (isXAxisTitleVisible) {
            gr.drawString(figureAttribute.axis_names[1], px, py);
        }
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
        if (figureAttribute.fontAxisY == null) {
            gr.setFont(gr.getFont().deriveFont(Font.ITALIC, 16.0f));
        } else {
            gr.setFont(figureAttribute.fontAxisY);
        }
        int axis_width = FactoryUtils.getGraphicsTextWidth(gr, figureAttribute.axis_names[0]);
        int axis_height = FactoryUtils.getGraphicsTextHeight(gr, figureAttribute.axis_names[0]);
        int px = 30;
        int py = getHeight() - (getHeight() - axis_width) / 2 - 10;
        Point p;
        if (isYAxisTitleVisible) {
            p = FactoryUtils.drawRotatedString(gr, figureAttribute.axis_names[0], px, py, -Math.PI / 2);
        }
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
    }// </editor-fold>//GEN-END:initComponents

    private int getMaximumLengthIndex(String[] labels) {
        int max = 0;
        int ret = 0;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].length() > max) {
                max = labels[i].length();
                ret = i;
            }
        }
        return ret;
    }

    private String[] generateArtificialLabels(int n) {
        String[] ret = new String[n];
        for (int i = 0; i < n; i++) {
            ret[i] = "" + i;
        }
        return ret;
    }

    public void setValueVisible(boolean valueVisible) {
        this.isValueVisible = valueVisible;
        repaint();
    }

    public void setLegend(boolean selected) {
        isLegend = selected;
        repaint();
    }

    void setDarkMode(boolean selected) {
        isDarkMode = selected;
        repaint();
    }

    public void setGridy(boolean selected) {
        isGridY = selected;
        repaint();
    }

    public void setGridx(boolean selected) {
        isGridX = selected;
        repaint();
    }

    public void setPointType(String toString) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public void setTranspose(boolean selected) {
        isTransposed = selected;
        repaint();
    }

    private void updateStrokeAlpha(Graphics2D gr, int stroke_width, float alpha_value) {
        BasicStroke stroke = new BasicStroke(stroke_width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        gr.setStroke(stroke);
        AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha_value);
        gr.setComposite(alcom);
    }

    public void setXAxisVisible(boolean selected) {
        isXAxisTitleVisible = selected;
        repaint();
    }

    public void setYAxisVisible(boolean selected) {
        isYAxisTitleVisible = selected;
        repaint();
    }

    public void setFillBar(boolean selected) {
        isFillBar = selected;
        repaint();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
