package jazari.data_analytics.plots;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jazari.data_analytics.core.*;
import javax.swing.JFrame;
import jazari.data_analytics.core.AbstractAxis;
import jazari.data_analytics.core.XAxis;
import jazari.data_analytics.core.YAxis;
import jazari.data_analytics.core.AbstractPlot;
import jazari.data_analytics.ui.LegendDialog;
import jazari.data_analytics.ui.StyleDialog;

public class LinePlot extends AbstractPlot {

    private float[][] points;
    private String[] labels;
    private List<LegendItem> tempLegendItems;

    // Yeni özellikler
    private static final int LINE_THICKNESS = 2;
    private static final int POINT_SIZE = 6;
    private static final int TOOLTIP_EXTRA_PIXELS = 8;
    private boolean showPoints = true;  // Noktaları göster/gizle
    private float lineAlpha = 1.0f;     // Çizgi transparanlığı
    private boolean showArea = false;    // Alan dolgusu
    private float areaAlpha = 0.2f;     // Alan dolgusu transparanlığı
    private LineStyle lineStyle = LineStyle.SOLID; // Çizgi stili

    private boolean isAxisLabelClicked = false;
    private float minX, maxX, minY, maxY;
    private XAxis xAxis;
    private YAxis yAxis;

    @Override
    public void draw(Graphics2D g2) {
        // Rendering kalitesini artır
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Başlığı çiz
        drawTitle(g2);

        // Nice step'leri hesapla
        float xStep = ((AbstractAxis) xAxis).calculateNiceStep(Math.abs(xAxis.getMax() - xAxis.getMin()));
        float yStep = ((AbstractAxis) yAxis).calculateNiceStep(Math.abs(yAxis.getMax() - yAxis.getMin()));

        // Grid çiz
        if (grid != null) {
            grid.drawGrid(g2, this, xStep, yStep);
        }

        // Eksenleri çiz
        xAxis.drawAxis(g2, this);
        yAxis.drawAxis(g2, this);

        // Eksenlerin sayısal değerlerini çiz
        xAxis.drawAxisLabels(g2, this, xStep);
        yAxis.drawAxisLabels(g2, this, yStep);

        // Eksenlerin etiketlerini çiz
        xAxis.drawAxisLabel(g2, this);
        yAxis.drawAxisLabel(g2, this);

        // Çizim alanını kırp
        Shape oldClip = g2.getClip();
        g2.setClip(PADDING, PADDING,
                getWidth() - PADDING - LEGEND_WIDTH - 10,
                getHeight() - 2 * PADDING);

        // Noktaları ve çizgileri çiz
        drawPoints(g2);

        // Kırpmayı geri al
        g2.setClip(oldClip);

        // Legend'ı çiz
        if (isLegendVisible) {
            drawLegend(g2);
        }

        // Mouse tooltip'ini çiz
        if (mousePosition != null && tooltipText != null) {
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            int tooltipX = mousePosition.x + 10;
            int tooltipY = mousePosition.y - 10;

            // Tooltip'in ekran dışına çıkmasını önle
            if (tooltipX + fm.stringWidth(tooltipText) > getWidth()) {
                tooltipX = mousePosition.x - fm.stringWidth(tooltipText) - 10;
            }
            if (tooltipY - fm.getHeight() < 0) {
                tooltipY = mousePosition.y + 20;
            }

            g2.drawString(tooltipText, tooltipX, tooltipY);
        }
    }

    // Çizgi stilleri için enum
    public enum LineStyle {
        SOLID,
        DASHED,
        DOTTED,
        DASH_DOT
    }

    public LinePlot(float[][] points, String[] labels) {
        super(true);
        this.points = points;
        this.labels = labels;
        this.titleText = "Line Plot";
        this.colorPalette = new ColorPalette(labels);

        Set<String> uniqueLabels = new HashSet<>();
        for (String label : labels) {
            uniqueLabels.add(label);
        }
        setnClass(uniqueLabels.size());

        calculateBounds();
        initAxes();
        this.grid = new Grid(new Color(220, 220, 220), 0.5f);
        initLegend();
        setupInteractions();
    }

    private void initLegend() {
        Map<String, Color> colors = colorPalette.getColors();
        List<LegendItem> items = new ArrayList<>();
        String[] keys = colors.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++) {
            items.add(new LegendItem(keys[i], colors.get(keys[i])));
        }
        tempLegendItems = items;
        setLegendItems(items);
    }

    private void initAxes() {
        // Initialize styles
        xAxisStyle = new Style();
        yAxisStyle = new Style();
        titleStyle = new Style();

        xAxisStyle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        yAxisStyle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleStyle.setFont(new Font("SansSerif", Font.BOLD, 16));

        this.xAxis = new XAxis(minX, maxX, xAxisLabel);
        this.yAxis = new YAxis(minY, maxY, yAxisLabel);

        xAxis.setLabelStyle(xAxisStyle);
        yAxis.setLabelStyle(yAxisStyle);
    }

    private void setupInteractions() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                } else {
                    lastPoint = e.getPoint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    resetView();
                    return;
                }
                int x = e.getX();
                int y = e.getY();

                if (isInsideXAxisLabel(x, y)) {
                    if (!isAxisLabelClicked) {
                        isAxisLabelClicked = true;
                        StyleDialog dialog = new StyleDialog(null, "X Axis Label Style", xAxisStyle, xAxisLabel);
                        dialog.setVisible(true);
                        if (dialog.getStyle() != null) {
                            xAxisStyle = dialog.getStyle();
                            if (dialog.getEditedText() != null) {
                                xAxisLabel = dialog.getEditedText();
                                xAxis.setLabel(xAxisLabel);
                            }
                            repaint();
                        }
                        isAxisLabelClicked = false;
                    }
                } else if (isInsideYAxisLabel(x, y)) {
                    if (!isAxisLabelClicked) {
                        isAxisLabelClicked = true;
                        StyleDialog dialog = new StyleDialog(null, "Y Axis Label Style", yAxisStyle, yAxisLabel);
                        dialog.setVisible(true);
                        if (dialog.getStyle() != null) {
                            yAxisStyle = dialog.getStyle();
                            if (dialog.getEditedText() != null) {
                                yAxisLabel = dialog.getEditedText();
                                yAxis.setLabel(yAxisLabel);
                            }
                            repaint();
                        }
                        isAxisLabelClicked = false;
                    }
                } else if (isInsideTitle(x, y)) {
                    StyleDialog dialog = new StyleDialog(null, "Title Style", titleStyle, titleText);
                    dialog.setVisible(true);
                    if (dialog.getStyle() != null) {
                        titleStyle = dialog.getStyle();
                        if (dialog.getEditedText() != null) {
                            titleText = dialog.getEditedText();
                        }
                        repaint();
                    }
                } else if (isInsideLegend(x, y)) {
                    LegendDialog dialog = new LegendDialog(null, "Edit Legend", tempLegendItems);
                    dialog.setVisible(true);
                    if (dialog.getLegendItems() != null) {
                        Map<LegendItem, LegendItem> changes = dialog.getChanges();
                        tempLegendItems = dialog.getLegendItems();
                        setLegendItems(tempLegendItems);
                        updateColorPalette(changes);
                        repaint();
                    }
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePosition = e.getPoint();
                updateTooltip(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getX() < PADDING || e.getX() > getWidth() - LEGEND_WIDTH - 10
                        || e.getY() < PADDING || e.getY() > getHeight() - PADDING) {
                    return;
                }

                float oldScale = scale;
                scale *= Math.pow(1.05, -e.getWheelRotation());
                scale = Math.max(0.1f, Math.min(10.0f, scale));

                translateX = e.getX() - (e.getX() - translateX) * scale / oldScale;
                translateY = e.getY() - (e.getY() - translateY) * scale / oldScale;

                updateAxisLabels();
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    translateX += e.getX() - lastPoint.x;
                    translateY += e.getY() - lastPoint.y;
                    lastPoint = e.getPoint();

                    updateAxisLabels();
                    repaint();
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    private boolean isInsideXAxisLabel(int mouseX, int mouseY) {
        FontMetrics metrics = getGraphics().getFontMetrics(xAxisStyle.getFont());
        int labelWidth = metrics.stringWidth(xAxisLabel);
        int labelHeight = metrics.getHeight();

        // X ekseni etiketi için merkezi konumu hesapla
        int labelX = (getWidth() - LEGEND_WIDTH - labelWidth) / 2 + PADDING;
        int labelY = getHeight() - PADDING / 3;

        return mouseX >= labelX && mouseX <= labelX + labelWidth
                && mouseY >= labelY - labelHeight && mouseY <= labelY;
    }

    private boolean isInsideYAxisLabel(int mouseX, int mouseY) {
        FontMetrics metrics = getGraphics().getFontMetrics(yAxisStyle.getFont());
        int labelWidth = metrics.stringWidth(yAxisLabel);
        int labelHeight = metrics.getHeight();

        int labelX = PADDING / 3;
        int labelY = getHeight() / 2 + labelWidth / 2;

        // Döndürülmüş metin için tıklama alanını ayarla
        return mouseX >= labelX - labelHeight && mouseX <= labelX + labelHeight
                && mouseY >= labelY - labelWidth && mouseY <= labelY;
    }

    private boolean isInsideTitle(int mouseX, int mouseY) {
        FontMetrics metrics = getGraphics().getFontMetrics(titleStyle.getFont());
        int titleWidth = metrics.stringWidth(titleText);
        int titleX = (getWidth() - titleWidth) / 2;
        int titleY = PADDING / 2;
        int labelHeight = metrics.getHeight();
        return mouseX >= titleX && mouseX <= titleX + titleWidth
                && mouseY >= titleY - labelHeight && mouseY <= titleY;
    }

    private boolean isInsideLegend(int mouseX, int mouseY) {
        int legendX = getWidth() - LEGEND_WIDTH + 10;
        int legendY = PADDING;
        int itemHeight = 20;
        return mouseX >= legendX - 5 && mouseX <= legendX - 5 + LEGEND_WIDTH - 15
                && mouseY >= legendY && mouseY <= legendY + nClass * itemHeight + 30;
    }

    protected void updateAxes(float minX, float maxX, float minY, float maxY) {
        this.xAxis = new XAxis(minX, maxX, xAxisLabel);
        this.yAxis = new YAxis(minY, maxY, yAxisLabel);

        xAxis.setLabelStyle(xAxisStyle);
        yAxis.setLabelStyle(yAxisStyle);
    }

    private void updateAxisLabels() {
        // Görünen alanın dünya koordinatlarındaki sınırlarını hesapla
        float visibleMinX = screenToWorldX(PADDING);
        float visibleMaxX = screenToWorldX(getWidth() - LEGEND_WIDTH - 10);
        float visibleMinY = screenToWorldY(getHeight() - PADDING);
        float visibleMaxY = screenToWorldY(PADDING);

        // Eksenleri güncelle
        xAxis = new XAxis(visibleMinX, visibleMaxX, xAxisLabel);
        yAxis = new YAxis(visibleMinY, visibleMaxY, yAxisLabel);

        // Stilleri yeniden ata
        xAxis.setLabelStyle(xAxisStyle);
        yAxis.setLabelStyle(yAxisStyle);
    }

    private void updateColorPalette(Map<LegendItem, LegendItem> changes) {
        String[] keys = new String[tempLegendItems.size()];
        Map<String, Color> colorMap = new HashMap<>();
        for (int i = 0; i < tempLegendItems.size(); i++) {
            keys[i] = tempLegendItems.get(i).getLabel();
            colorMap.put(tempLegendItems.get(i).getLabel(), tempLegendItems.get(i).getColor());
        }
        this.colorPalette = new ColorPalette(keys);
        for (int i = 0; i < labels.length; i++) {
            for (Map.Entry<LegendItem, LegendItem> entry : changes.entrySet()) {
                if (labels[i].equals(entry.getKey().getLabel())) {
                    labels[i] = entry.getValue().getLabel();
                    if (!entry.getKey().getColor().equals(entry.getValue().getColor())) {
                        colorMap.put(entry.getValue().getLabel(), entry.getValue().getColor());
                    }
                }
            }
        }
        this.colorPalette = new ColorPalette(colorMap);
    }

    private void calculateBounds() {
        minX = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        minY = Float.MAX_VALUE;
        maxY = Float.MIN_VALUE;

        for (float[] point : points) {
            minX = Math.min(minX, point[0]);
            maxX = Math.max(maxX, point[0]);
            minY = Math.min(minY, point[1]);
            maxY = Math.max(maxY, point[1]);
        }

        float xMargin = (maxX - minX) * 0.1f;
        float yMargin = (maxY - minY) * 0.1f;
        minX -= xMargin;
        maxX += xMargin;
        minY -= yMargin;
        maxY += yMargin;

        updateAxes(minX, maxX, minY, maxY);
    }

    protected void drawPoints(Graphics2D g2) {
        // Rendering kalitesi
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Her seri için path'leri hazırla
        Map<String, Path2D.Float> linePaths = new HashMap<>();
        Map<String, Path2D.Float> areaPaths = new HashMap<>();

        // Serileri oluştur
        for (int i = 0; i < points.length; i++) {
            String label = labels[i];
            float x = points[i][0];
            float y = points[i][1];
            int screenX = worldToScreenX(x);
            int screenY = worldToScreenY(y);

            // Çizgi path'i
            if (!linePaths.containsKey(label)) {
                Path2D.Float path = new Path2D.Float();
                path.moveTo(screenX, screenY);
                linePaths.put(label, path);

                if (showArea) {
                    Path2D.Float areaPath = new Path2D.Float();
                    areaPath.moveTo(screenX, getHeight() - PADDING);
                    areaPath.lineTo(screenX, screenY);
                    areaPaths.put(label, areaPath);
                }
            } else {
                linePaths.get(label).lineTo(screenX, screenY);
                if (showArea) {
                    areaPaths.get(label).lineTo(screenX, screenY);
                }
            }
        }

        // Alan dolgularını çiz
        if (showArea) {
            for (Map.Entry<String, Path2D.Float> entry : areaPaths.entrySet()) {
                String label = entry.getKey();
                Path2D.Float areaPath = entry.getValue();

                // Path'i kapat
                int lastX = worldToScreenX(points[points.length - 1][0]);
                areaPath.lineTo(lastX, getHeight() - PADDING);
                areaPath.closePath();

                // Dolguyu çiz
                Color color = colorPalette.getColor(label);
                g2.setColor(new Color(color.getRed(), color.getGreen(),
                        color.getBlue(), (int) (255 * areaAlpha)));
                g2.fill(areaPath);
            }
        }

        // Çizgileri çiz
        for (Map.Entry<String, Path2D.Float> entry : linePaths.entrySet()) {
            String label = entry.getKey();
            Path2D.Float path = entry.getValue();

            // Çizgi stilini ayarla
            Stroke stroke = getStrokeForStyle(lineStyle);
            g2.setStroke(stroke);

            // Çizgi rengini ayarla
            Color color = colorPalette.getColor(label);
            g2.setColor(new Color(color.getRed(), color.getGreen(),
                    color.getBlue(), (int) (255 * lineAlpha)));
            g2.draw(path);
        }

        // Noktaları çiz
        if (showPoints) {
            for (int i = 0; i < points.length; i++) {
                int x = worldToScreenX(points[i][0]) - POINT_SIZE / 2;
                int y = worldToScreenY(points[i][1]) - POINT_SIZE / 2;

                if (isPointVisible(x, y)) {
                    Color color = colorPalette.getColor(labels[i]);
                    g2.setColor(color);
                    g2.fillOval(x, y, POINT_SIZE, POINT_SIZE);
                    // Nokta kenarı
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.drawOval(x, y, POINT_SIZE, POINT_SIZE);
                }
            }
        }
    }

    private boolean isPointVisible(int x, int y) {
        return x > PADDING - POINT_SIZE && x < getWidth() - LEGEND_WIDTH - 10 + POINT_SIZE
                && y > PADDING - POINT_SIZE && y < getHeight() - PADDING + POINT_SIZE;
    }

    private Stroke getStrokeForStyle(LineStyle style) {
        float[] dash_pattern;
        switch (style) {
            case DASHED:
                dash_pattern = new float[]{10.0f, 10.0f};
                return new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 10.0f, dash_pattern, 0.0f);
            case DOTTED:
                dash_pattern = new float[]{2.0f, 5.0f};
                return new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 10.0f, dash_pattern, 0.0f);
            case DASH_DOT:
                dash_pattern = new float[]{10.0f, 5.0f, 2.0f, 5.0f};
                return new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 10.0f, dash_pattern, 0.0f);
            default:
                return new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND);
        }
    }

    // Yeni özellikler için setter metodları
    public void setShowPoints(boolean showPoints) {
        this.showPoints = showPoints;
        repaint();
    }

    public void setLineAlpha(float alpha) {
        this.lineAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
        repaint();
    }

    public void setShowArea(boolean showArea) {
        this.showArea = showArea;
        repaint();
    }

    public void setAreaAlpha(float alpha) {
        this.areaAlpha = Math.max(0.0f, Math.min(1.0f, alpha));
        repaint();
    }

    public void setLineStyle(LineStyle style) {
        this.lineStyle = style;
        repaint();
    }

    protected void updateTooltip(int mouseX, int mouseY) {
        tooltipText = null;
        for (int i = 0; i < points.length; i++) {
            int pointScreenX = worldToScreenX(points[i][0]);
            int pointScreenY = worldToScreenY(points[i][1]);
            if (Math.abs(mouseX - pointScreenX) <= TOOLTIP_EXTRA_PIXELS
                    && Math.abs(mouseY - pointScreenY) <= TOOLTIP_EXTRA_PIXELS) {
                tooltipText = String.format("<html>Series: %s<br>X: %.2f<br>Y: %.2f</html>",
                        labels[i], points[i][0], points[i][1]);
                setToolTipText(tooltipText);
                return;
            }
        }
        setToolTipText(null);
    }

    public static void main(String[] args) {
        int numPoints = 100;
        float[][] points = new float[numPoints * 2][2];
        String[] labels = new String[numPoints * 2];

        // X değerlerini 0'dan 100'e yay
        float xStep = 100.0f / (numPoints - 1);  // X değerlerini eşit aralıklarla dağıt

        // Birinci seri: sin dalgası
        for (int i = 0; i < numPoints; i++) {
            float x = i * xStep;  // 0'dan 100'e
            float radians = (float) (x * 2 * Math.PI / 100);  // X değerini radyana çevir
            points[i][0] = x;  // X değeri 0-100 arasında
            points[i][1] = (float) Math.sin(radians) * 50;  // Sin değerini 100 ile çarp
            labels[i] = "Sin Wave";
        }

        // İkinci seri: cos dalgası
        for (int i = 0; i < numPoints; i++) {
            float x = i * xStep;  // 0'dan 100'e
            float radians = (float) (x * 2 * Math.PI / 100);  // X değerini radyana çevir
            points[numPoints + i][0] = x;  // X değeri 0-100 arasında
            points[numPoints + i][1] = (float) Math.cos(radians) * 50;  // Cos değerini 100 ile çarp
            labels[numPoints + i] = "Cos Wave";
        }

        LinePlot linePlot = new LinePlot(points, labels);
        linePlot.setTitle("Trigonometric Functions");
        linePlot.setxAxisLabel("X");
        linePlot.setyAxisLabel("Y");

        // Özellikleri ayarla
        linePlot.setShowArea(true);
        linePlot.setAreaAlpha(0.1f);
        linePlot.setLineStyle(LineStyle.SOLID);
        linePlot.setShowPoints(false);

        // Plot'u göster
        JFrame frame = new JFrame("Line Plot Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(linePlot);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
