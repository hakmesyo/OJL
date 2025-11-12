package jazari.gui;

import jazari.types.TFigureAttribute;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jazari.factory.FactoryUtils;
import org.drjekyll.fontchooser.FontDialog;

/**
 * Matplotlib'den ilham alan, modern, sağlam ve tamamen interaktif bir çizgi
 * grafiği paneli. Bu son versiyon, yeniden boyutlandırmaya duyarlı legend ve
 * mouse merkezli zoom özelliklerini içerir.
 *
 * @author BAP1 (Orijinal Yazar), Gemini (Kapsamlı Modernizasyon ve
 * İnteraktivite)
 */
public class PanelPlot extends JPanel {

    private float[][] data;
    private float[] xAxis;
    private TFigureAttribute figureAttribute;
    private Color[] colors;
    private long randomSeed = System.currentTimeMillis();
    private int paletteOffset = 0;

    // --- KONTROL EDİLEBİLİR STİL DEĞİŞKENLERİ ---
    private boolean isDarkMode = false;
    private boolean isGridX = true;
    private boolean isGridY = true;

    // --- STİL SABİTLERİ ---
    private Color colorBackground, colorPlotArea, colorAxis, colorGrid, colorText;
    private static final Color COLOR_HIGHLIGHT = new Color(255, 100, 0, 150);
    private static final Color COLOR_ZOOM_RECT = new Color(0, 120, 215, 50);
    private static final Color COLOR_ZOOM_RECT_BORDER = new Color(0, 120, 215);
    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 16);
    private static final Font FONT_AXIS_TITLE = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONT_AXIS_LABELS = new Font("SansSerif", Font.PLAIN, 11);
    private static final Color[] HIGH_CONTRAST_PALETTE = {
        new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44),
        new Color(214, 39, 40), new Color(148, 103, 189), new Color(140, 86, 75),
        new Color(227, 119, 194), new Color(127, 127, 127), new Color(188, 189, 34),
        new Color(23, 190, 207)
    };

    // --- PADDING ---
    private static final int PADDING_TOP = 50;
    private static final int PADDING_BOTTOM = 60;
    private static final int PADDING_LEFT = 70;
    private static final int PADDING_RIGHT = 30;

    // --- ETKİLEŞİM DEĞİŞKENLERİ ---
    private float scaleX = 1.0f, scaleY = 1.0f;
    private float translateX = 0, translateY = 0;
    private Point lastPoint;
    private Rectangle zoomRect;
    private int highlightedSeriesIndex = -1, highlightedPointIndex = -1;
    private boolean isDraggingPoint = false;
    private boolean isDraggingLegend = false;
    private Point legendDragOffset;
    private boolean isLegendPositionDefault = true;

    // --- DİNAMİK DÜZENLEME İÇİN ALANLAR ---
    private Rectangle rectTitle, rectAxisX, rectAxisY, rectLegend;

    // --- VERİ SINIRLARI (DÜNYA KOORDİNATLARI) ---
    private float minX, maxX, minY, maxY;

    public PanelPlot(float[][] data) {
        this.data = data;
        this.figureAttribute = new TFigureAttribute();
        this.figureAttribute.items = generateDefaultItems(data.length);

        initializePanel();
        assignColorsFromPalette(data.length);
        calculateBounds();
    }

    private void initializePanel() {
        updateColors();
        setupInteractions();
        rectTitle = new Rectangle();
        rectAxisX = new Rectangle();
        rectAxisY = new Rectangle();
        rectLegend = new Rectangle();
    }

    private void setupInteractions() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (figureAttribute.isLegend && rectLegend.contains(e.getPoint())) {
                        isDraggingLegend = true;
                        isLegendPositionDefault = false;
                        legendDragOffset = new Point(e.getX() - rectLegend.x, e.getY() - rectLegend.y);
                        return;
                    }
                    if (!tryStartPointDrag(e.getPoint())) {
                        zoomRect = new Rectangle(e.getX(), e.getY(), 0, 0);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (zoomRect != null) {
                    if (Math.abs(zoomRect.width) > 5 && Math.abs(zoomRect.height) > 5) {
                        applyZoomToRect();
                    }
                    zoomRect = null;
                    repaint();
                }
                isDraggingPoint = false;
                isDraggingLegend = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick(e.getPoint());
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                float worldXBeforeZoom = screenToWorldX(e.getX());
                float worldYBeforeZoom = screenToWorldY(e.getY());

                double zoomFactor = Math.pow(1.1, -e.getWheelRotation());
                scaleX *= zoomFactor;
                scaleY *= zoomFactor;
                scaleX = Math.max(0.1f, Math.min(100.0f, scaleX));
                scaleY = Math.max(0.1f, Math.min(100.0f, scaleY));

                int screenXAfterZoom = worldToScreenX(worldXBeforeZoom);
                int screenYAfterZoom = worldToScreenY(worldYBeforeZoom);

                translateX += e.getX() - screenXAfterZoom;
                translateY += e.getY() - screenYAfterZoom;

                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDraggingLegend) {
                    rectLegend.x = e.getX() - legendDragOffset.x;
                    rectLegend.y = e.getY() - legendDragOffset.y;
                    repaint();
                    return;
                }
                if (isDraggingPoint) {
                    float newY = screenToWorldY(e.getY());
                    data[highlightedSeriesIndex][highlightedPointIndex] = newY;
                    repaint();
                } else if (SwingUtilities.isMiddleMouseButton(e)) {
                    translateX += e.getX() - lastPoint.x;
                    translateY += e.getY() - lastPoint.y;
                    lastPoint = e.getPoint();
                    repaint();
                } else if (zoomRect != null) {
                    zoomRect.setFrameFromDiagonal(lastPoint, e.getPoint());
                    repaint();
                }
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (isLegendPositionDefault) {
                    updateDefaultLegendPosition();
                }
            }
        });
    }

    private void updateDefaultLegendPosition() {
        int plotWidth = getWidth() - PADDING_LEFT - PADDING_RIGHT;
        if (plotWidth <= 0) {
            return; // Henüz boyutlar ayarlanmamışsa çık
        }
        int legendWidth = 140;
        int margin = 10;
        rectLegend.x = PADDING_LEFT + plotWidth - legendWidth - margin;
        rectLegend.y = PADDING_TOP + margin;
    }

    public void resetView() {
        scaleX = 1.0f;
        scaleY = 1.0f;
        translateX = 0;
        translateY = 0;
        isLegendPositionDefault = true;
        updateDefaultLegendPosition();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        if (data == null || data.length == 0) {
            return;
        }

        int plotWidth = getWidth() - PADDING_LEFT - PADDING_RIGHT;
        int plotHeight = getHeight() - PADDING_TOP - PADDING_BOTTOM;

        g2d.setColor(colorPlotArea);
        g2d.fillRect(PADDING_LEFT, PADDING_TOP, plotWidth, plotHeight);

        Shape oldClip = g2d.getClip();
        g2d.setClip(PADDING_LEFT, PADDING_TOP, plotWidth, plotHeight);
        drawGrid(g2d, plotWidth, plotHeight);
        drawLines(g2d, plotWidth, plotHeight);
        drawHighlight(g2d);
        g2d.setClip(oldClip);

        drawAxes(g2d, plotWidth, plotHeight);
        drawAxisLabels(g2d, plotWidth, plotHeight);
        drawTitles(g2d, plotWidth, plotHeight);
        drawLegend(g2d, plotWidth, plotHeight);
        drawZoomRect(g2d);
    }

    private void drawLegend(Graphics2D g2d, int plotWidth, int plotHeight) {
        if (!figureAttribute.isLegend) {
            return;
        }

        if (isLegendPositionDefault) {
            updateDefaultLegendPosition();
        }

        int legendX = rectLegend.x;
        int legendY = rectLegend.y;
        int itemHeight = 20;
        int legendWidth = 140;
        int legendHeight = data.length * itemHeight + 10;
        rectLegend.setSize(legendWidth, legendHeight);

        g2d.setColor(isDarkMode ? new Color(50, 50, 50, 220) : new Color(255, 255, 255, 220));
        g2d.fillRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);
        g2d.setColor(isDarkMode ? Color.DARK_GRAY : Color.LIGHT_GRAY);
        g2d.drawRoundRect(legendX, legendY, legendWidth, legendHeight, 10, 10);

        g2d.setFont(FONT_AXIS_LABELS);
        for (int i = 0; i < data.length; i++) {
            g2d.setColor(colors[i]);
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(legendX + 10, legendY + (i * itemHeight) + 15, legendX + 30, legendY + (i * itemHeight) + 15);
            g2d.setColor(colorText);
            g2d.drawString(figureAttribute.items[i], legendX + 40, legendY + (i * itemHeight) + 20);
        }
    }

    // --- Diğer Tüm Metotlar (Değişiklik Yok, Tamlık İçin Eklendi) ---
    private void updateColors() {
        if (isDarkMode) {
            colorBackground = new Color(45, 45, 45);
            colorPlotArea = new Color(30, 30, 30);
            colorAxis = Color.LIGHT_GRAY;
            colorGrid = new Color(60, 60, 60);
            colorText = Color.WHITE;
        } else {
            colorBackground = new Color(240, 240, 240);
            colorPlotArea = Color.WHITE;
            colorAxis = Color.BLACK;
            colorGrid = new Color(220, 220, 220);
            colorText = Color.BLACK;
        }
        setBackground(colorBackground);
    }

    private void calculateBounds() {
        if (data == null || data.length == 0 || data[0].length == 0) {
            return;
        }
        minX = (xAxis != null && xAxis.length > 0) ? FactoryUtils.getMinimum(xAxis) : 0;
        maxX = (xAxis != null && xAxis.length > 0) ? FactoryUtils.getMaximum(xAxis) : data[0].length - 1;
        minY = FactoryUtils.getMinimum(data);
        maxY = FactoryUtils.getMaximum(data);
        float xMargin = (maxX - minX) * 0.05f, yMargin = (maxY - minY) * 0.1f;
        if (xMargin == 0) {
            xMargin = 1;
        }
        if (yMargin == 0) {
            yMargin = 1;
        }
        minX -= xMargin;
        maxX += xMargin;
        minY -= yMargin;
        maxY += yMargin;
    }

    private boolean tryStartPointDrag(Point p) {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                float xVal = (xAxis != null) ? xAxis[j] : j;
                int screenX = worldToScreenX(xVal);
                int screenY = worldToScreenY(data[i][j]);
                if (p.distance(screenX, screenY) < 8) {
                    highlightedSeriesIndex = i;
                    highlightedPointIndex = j;
                    isDraggingPoint = true;
                    return true;
                }
            }
        }
        highlightedSeriesIndex = -1;
        highlightedPointIndex = -1;
        return false;
    }

    private void applyZoomToRect() {
        Rectangle r = zoomRect.getBounds();
        if (r.width < 0) {
            r.x += r.width;
            r.width = -r.width;
        }
        if (r.height < 0) {
            r.y += r.height;
            r.height = -r.height;
        }
        int plotWidth = getWidth() - PADDING_LEFT - PADDING_RIGHT;
        int plotHeight = getHeight() - PADDING_TOP - PADDING_BOTTOM;
        float worldX1 = screenToWorldX(r.x);
        float worldY1 = screenToWorldY(r.y);
        float worldX2 = screenToWorldX(r.x + r.width);
        float worldY2 = screenToWorldY(r.y + r.height);
        float newScaleX = scaleX * plotWidth / Math.abs(worldToScreenX(worldX2) - worldToScreenX(worldX1));
        float newScaleY = scaleY * plotHeight / Math.abs(worldToScreenY(worldY2) - worldToScreenY(worldY1));
        translateX = translateX * (newScaleX / scaleX) - (worldToScreenX(worldX1) - PADDING_LEFT) * (newScaleX / scaleX - 1);
        translateY = translateY * (newScaleY / scaleY) - (worldToScreenY(worldY1) - PADDING_TOP) * (newScaleY / scaleY - 1);
        scaleX = newScaleX;
        scaleY = newScaleY;
    }

    private void handleDoubleClick(Point p) {
        if (rectTitle.contains(p)) {
            String newTitle = JOptionPane.showInputDialog(this, "Enter new title:", figureAttribute.title);
            if (newTitle != null) {
                figureAttribute.title = newTitle;
            }
            editFont(figureAttribute.fontTitle, newFont -> figureAttribute.fontTitle = newFont);
        } else if (rectAxisX.contains(p)) {
            String newLabel = JOptionPane.showInputDialog(this, "Enter new X-axis label:", figureAttribute.axis_names[1]);
            if (newLabel != null) {
                figureAttribute.axis_names[1] = newLabel;
            }
            editFont(figureAttribute.fontAxisX, newFont -> figureAttribute.fontAxisX = newFont);
        } else if (rectAxisY.contains(p)) {
            String newLabel = JOptionPane.showInputDialog(this, "Enter new Y-axis label:", figureAttribute.axis_names[0]);
            if (newLabel != null) {
                figureAttribute.axis_names[0] = newLabel;
            }
            editFont(figureAttribute.fontAxisY, newFont -> figureAttribute.fontAxisY = newFont);
        } else if (rectLegend.contains(p)) {
            int itemHeight = 20;
            int index = (p.y - (rectLegend.y + 10)) / itemHeight;
            if (index >= 0 && index < figureAttribute.items.length) {
                String newText = JOptionPane.showInputDialog(this, "Enter new legend text:", figureAttribute.items[index]);
                if (newText != null) {
                    figureAttribute.items[index] = newText;
                }
                Color newColor = JColorChooser.showDialog(this, "Choose color for " + figureAttribute.items[index], colors[index]);
                if (newColor != null) {
                    colors[index] = newColor;
                }
            }
        } else {
            resetView();
        }
        repaint();
    }

    private interface FontConsumer {

        void accept(Font font);
    }

    private void editFont(Font currentFont, FontConsumer consumer) {
        JLabel label = new JLabel();
        label.setFont(currentFont);
        FontDialog.showDialog(label);
        if (!label.getFont().equals(currentFont)) {
            consumer.accept(label.getFont());
        }
    }

    private void drawGrid(Graphics2D g2d, int plotWidth, int plotHeight) {
        g2d.setColor(colorGrid);
        g2d.setStroke(new BasicStroke(1.0f));
        float visibleMinX = screenToWorldX(PADDING_LEFT), visibleMaxX = screenToWorldX(PADDING_LEFT + plotWidth);
        float visibleMinY = screenToWorldY(PADDING_TOP + plotHeight), visibleMaxY = screenToWorldY(PADDING_TOP);
        float xStep = calculateNiceStep(visibleMaxX - visibleMinX), yStep = calculateNiceStep(visibleMaxY - visibleMinY);
        if (isGridX) {
            for (float x = (float) (Math.floor(visibleMinX / xStep) * xStep); x <= visibleMaxX; x += xStep) {
                g2d.drawLine(worldToScreenX(x), PADDING_TOP, worldToScreenX(x), PADDING_TOP + plotHeight);
            }
        }
        if (isGridY) {
            for (float y = (float) (Math.floor(visibleMinY / yStep) * yStep); y <= visibleMaxY; y += yStep) {
                g2d.drawLine(PADDING_LEFT, worldToScreenY(y), PADDING_LEFT + plotWidth, worldToScreenY(y));
            }
        }
    }

    private void drawLines(Graphics2D g2d, int plotWidth, int plotHeight) {
        for (int i = 0; i < data.length; i++) {
            g2d.setColor(colors[i]);
            g2d.setStroke(getStrokeForLine(i));
            int[] xPoints = new int[data[i].length], yPoints = new int[data[i].length];
            for (int j = 0; j < data[i].length; j++) {
                float xVal = (xAxis != null) ? xAxis[j] : j;
                xPoints[j] = worldToScreenX(xVal);
                yPoints[j] = worldToScreenY(data[i][j]);
            }
            if (figureAttribute.pointType.equals("-")) {
                g2d.drawPolyline(xPoints, yPoints, xPoints.length);
            } else {
                g2d.drawPolyline(xPoints, yPoints, xPoints.length);
                drawPoints(g2d, xPoints, yPoints, figureAttribute.pointType);
            }
        }
    }

    private void drawHighlight(Graphics2D g2d) {
        if (highlightedPointIndex != -1) {
            float xVal = (xAxis != null) ? xAxis[highlightedPointIndex] : highlightedPointIndex;
            float yVal = data[highlightedSeriesIndex][highlightedPointIndex];
            int x = worldToScreenX(xVal);
            int y = worldToScreenY(yVal);
            g2d.setColor(COLOR_HIGHLIGHT);
            g2d.fillOval(x - 8, y - 8, 16, 16);
            g2d.setColor(colors[highlightedSeriesIndex]);
            g2d.setStroke(new BasicStroke(2.0f));
            drawPoints(g2d, new int[]{x}, new int[]{y}, "o");
        }
    }

    private void drawZoomRect(Graphics2D g2d) {
        if (zoomRect != null) {
            g2d.setColor(COLOR_ZOOM_RECT);
            g2d.fill(zoomRect);
            g2d.setColor(COLOR_ZOOM_RECT_BORDER);
            g2d.draw(zoomRect);
        }
    }

    private void drawTitles(Graphics2D g2d, int plotWidth, int plotHeight) {
        g2d.setColor(colorText);
        FontMetrics fm;
        g2d.setFont(figureAttribute.fontTitle != null ? figureAttribute.fontTitle : FONT_TITLE);
        fm = g2d.getFontMetrics();
        String title = figureAttribute.title;
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        int titleY = PADDING_TOP - 20;
        g2d.drawString(title, titleX, titleY);
        rectTitle.setBounds(titleX, titleY - fm.getAscent(), fm.stringWidth(title), fm.getHeight());
        g2d.setFont(figureAttribute.fontAxisX != null ? figureAttribute.fontAxisX : FONT_AXIS_TITLE);
        fm = g2d.getFontMetrics();
        String xLabel = figureAttribute.axis_names[1];
        int xLabelX = (getWidth() - fm.stringWidth(xLabel)) / 2;
        int xLabelY = getHeight() - 15;
        g2d.drawString(xLabel, xLabelX, xLabelY);
        rectAxisX.setBounds(xLabelX, xLabelY - fm.getAscent(), fm.stringWidth(xLabel), fm.getHeight());
        g2d.setFont(figureAttribute.fontAxisY != null ? figureAttribute.fontAxisY : FONT_AXIS_TITLE);
        fm = g2d.getFontMetrics();
        String yLabel = figureAttribute.axis_names[0];
        AffineTransform old = g2d.getTransform();
        g2d.rotate(-Math.PI / 2);
        int yLabelX = -(getHeight() + fm.stringWidth(yLabel)) / 2;
        int yLabelY = 20;
        g2d.drawString(yLabel, yLabelX, yLabelY);
        g2d.setTransform(old);
        rectAxisY.setBounds(0, PADDING_TOP, PADDING_LEFT - 10, plotHeight);
    }

    private void drawPoints(Graphics2D g2d, int[] xPoints, int[] yPoints, String type) {
        for (int i = 0; i < xPoints.length; i++) {
            int x = xPoints[i], y = yPoints[i];
            switch (type) {
                case ".":
                    g2d.fillOval(x - 2, y - 2, 4, 4);
                    break;
                case "o":
                    g2d.drawOval(x - 4, y - 4, 8, 8);
                    break;
                case "*":
                    g2d.drawLine(x - 4, y, x + 4, y);
                    g2d.drawLine(x, y - 4, x, y + 4);
                    g2d.drawLine(x - 3, y - 3, x + 3, y + 3);
                    g2d.drawLine(x - 3, y + 3, x + 3, y - 3);
                    break;
            }
        }
    }

    private Stroke getStrokeForLine(int index) {
        if (figureAttribute.isStroke && figureAttribute.stroke != null && index < figureAttribute.stroke.size()) {
            return figureAttribute.stroke.get(index);
        }
        return new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    }

    private void drawAxes(Graphics2D g2d, int plotWidth, int plotHeight) {
        g2d.setColor(colorAxis);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(PADDING_LEFT, PADDING_TOP + plotHeight, PADDING_LEFT + plotWidth, PADDING_TOP + plotHeight);
        g2d.drawLine(PADDING_LEFT, PADDING_TOP, PADDING_LEFT, PADDING_TOP + plotHeight);
    }

    private void drawAxisLabels(Graphics2D g2d, int plotWidth, int plotHeight) {
        g2d.setColor(colorText);
        g2d.setFont(FONT_AXIS_LABELS);
        FontMetrics fm = g2d.getFontMetrics();
        float visibleMinX = screenToWorldX(PADDING_LEFT), visibleMaxX = screenToWorldX(PADDING_LEFT + plotWidth);
        float visibleMinY = screenToWorldY(PADDING_TOP + plotHeight), visibleMaxY = screenToWorldY(PADDING_TOP);
        float xStep = calculateNiceStep(visibleMaxX - visibleMinX), yStep = calculateNiceStep(visibleMaxY - visibleMinY);
        DecimalFormat xFmt = createDynamicFormatter(xStep);
        for (float x = (float) (Math.floor(visibleMinX / xStep) * xStep); x <= visibleMaxX; x += xStep) {
            int screenX = worldToScreenX(x);
            String label = xFmt.format(x);
            g2d.drawString(label, screenX - fm.stringWidth(label) / 2, PADDING_TOP + plotHeight + 20);
        }
        DecimalFormat yFmt = createDynamicFormatter(yStep);
        for (float y = (float) (Math.floor(visibleMinY / yStep) * yStep); y <= visibleMaxY; y += yStep) {
            int screenY = worldToScreenY(y);
            String label = yFmt.format(y);
            g2d.drawString(label, PADDING_LEFT - fm.stringWidth(label) - 5, screenY + fm.getAscent() / 2);
        }
    }

    private int worldToScreenX(float worldX) {
        int plotWidth = getWidth() - PADDING_LEFT - PADDING_RIGHT;
        return (int) (PADDING_LEFT + ((worldX - minX) / (maxX - minX)) * plotWidth * scaleX + translateX);
    }

    private int worldToScreenY(float worldY) {
        int plotHeight = getHeight() - PADDING_TOP - PADDING_BOTTOM;
        return (int) (PADDING_TOP + plotHeight - ((worldY - minY) / (maxY - minY)) * plotHeight * scaleY + translateY);
    }

    private float screenToWorldX(int screenX) {
        int plotWidth = getWidth() - PADDING_LEFT - PADDING_RIGHT;
        if (plotWidth * scaleX == 0) {
            return 0;
        }
        return minX + ((screenX - PADDING_LEFT - translateX) / (plotWidth * scaleX)) * (maxX - minX);
    }

    private float screenToWorldY(int screenY) {
        int plotHeight = getHeight() - PADDING_TOP - PADDING_BOTTOM;
        if (plotHeight * scaleY == 0) {
            return 0;
        }
        return minY + ((PADDING_TOP + plotHeight - screenY + translateY) / (plotHeight * scaleY)) * (maxY - minY);
    }

    private float calculateNiceStep(float range) {
        if (range <= 0) {
            return 1;
        }
        float roughStep = range / 10.0f;
        float exponent = (float) Math.floor(Math.log10(roughStep));
        float fraction = roughStep / (float) Math.pow(10, exponent);
        float niceFraction;
        if (fraction < 1.5) {
            niceFraction = 1;
        } else if (fraction < 3) {
            niceFraction = 2;
        } else if (fraction < 7) {
            niceFraction = 5;
        } else {
            niceFraction = 10;
        }
        return niceFraction * (float) Math.pow(10, exponent);
    }

    private DecimalFormat createDynamicFormatter(float step) {
        int decimals = Math.max(0, -(int) Math.floor(Math.log10(step)));
        StringBuilder pattern = new StringBuilder("0");
        if (decimals > 0) {
            pattern.append(".");
            for (int i = 0; i < decimals; i++) {
                pattern.append("0");
            }
        }
        return new DecimalFormat(pattern.toString());
    }

    private String[] generateDefaultItems(int count) {
        String[] items = new String[count];
        for (int i = 0; i < count; i++) {
            items[i] = "Line " + (i + 1);
        }
        return items;
    }

    public void setMatrix(float[][] data) {
        this.data = data;
        this.paletteOffset = 0;
        assignColorsFromPalette(data.length);
        calculateBounds();
        repaint();
    }

    public void setMatrix(float[][] data, boolean isColorPersist) {
        this.data = data;
        if (!isColorPersist) {
            this.paletteOffset = 0;
        }
        assignColorsFromPalette(data.length);
        calculateBounds();
        repaint();
    }

    private void assignColorsFromPalette(int numSeries) {
        if (numSeries == 0) {
            this.colors = new Color[0];
            return;
        }
        this.colors = new Color[numSeries];
        for (int i = 0; i < numSeries; i++) {
            int colorIndex = (i + this.paletteOffset) % HIGH_CONTRAST_PALETTE.length;
            this.colors[i] = HIGH_CONTRAST_PALETTE[colorIndex];
        }
    }

    public void setRandomSeed(long seed) {
        if (data != null) {
            this.paletteOffset = (int) (seed % HIGH_CONTRAST_PALETTE.length);
            assignColorsFromPalette(data.length);
        }
        repaint();
    }

    public void setDarkMode(boolean isDarkMode) {
        this.isDarkMode = isDarkMode;
        updateColors();
        repaint();
    }

    public void setLegend(boolean isLegend) {
        this.figureAttribute.isLegend = isLegend;
        repaint();
    }

    public void setGridx(boolean isGridX) {
        this.isGridX = isGridX;
        repaint();
    }

    public void setGridy(boolean isGridY) {
        this.isGridY = isGridY;
        repaint();
    }

    public void setPointType(String type) {
        this.figureAttribute.pointType = type;
        repaint();
    }

    public void setPlotType(String type) {
        setPointType(type);
    }

    public void setFigureAttribute(TFigureAttribute attr) {
        this.figureAttribute = attr;
        repaint();
    }

    public void setXAxis(float[] xAxis) {
        this.xAxis = xAxis;
        calculateBounds();
        repaint();
    }

    public float[][] getMatrix() {
        return data;
    }
}
