package jazari.data_analytics.plots;

import jazari.data_analytics.core.AbstractPlot;
import jazari.data_analytics.core.Axis;
import jazari.data_analytics.core.ColorPalette;
import jazari.data_analytics.core.Grid;
import jazari.data_analytics.core.LegendItem;
import jazari.data_analytics.core.Style;
import jazari.data_analytics.ui.LegendDialog;
import jazari.data_analytics.ui.StyleDialog;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.JFrame;
import jazari.data_analytics.core.AbstractAxis;
import jazari.data_analytics.core.XAxis;
import jazari.data_analytics.core.YAxis;

public class ScatterPlot extends AbstractPlot {

    // Data variables
    private float[][] points;
    private String[] labels;

    // Bounds
    private float minX, maxX, minY, maxY;

    // Display settings
    private static final int POINT_SIZE = 8;
    private static final int TOOLTIP_EXTRA_PIXELS = 8;
//    private Point mousePosition = null;
//    private String tooltipText = null;
    private List<LegendItem> tempLegendItems;

    private boolean isAxisLabelClicked = false;
    private XAxis xAxis;
    private YAxis yAxis;

    public ScatterPlot(float[][] points, String[] labels) {
        super(true);
        this.points = points;
        this.labels = labels;
        this.titleText = "Scatter Plot";
        this.colorPalette = new ColorPalette(labels);
        Set<String> my_set = new HashSet<>();
        for (String label : labels) {
            my_set.add(label);
        }
        setnClass(my_set.size());
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

        // Add margins
        float xMargin = (maxX - minX) * 0.1f;
        float yMargin = (maxY - minY) * 0.1f;
        minX -= xMargin;
        maxX += xMargin;
        minY -= yMargin;
        maxY += yMargin;

        updateAxes(minX, maxX, minY, maxY); // Eksenleri güncelle    }

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

                // Eksenlerdeki değerleri güncelle
                updateAxisLabels();

                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    translateX += e.getX() - lastPoint.x;
                    translateY += e.getY() - lastPoint.y;
                    lastPoint = e.getPoint();

                    // Eksenlerdeki değerleri güncelle
                    updateAxisLabels();

                    repaint();
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
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

    private boolean isInsideXAxisLabel(int mouseX, int mouseY) {
        FontMetrics metrics = getGraphics().getFontMetrics(xAxisStyle.getFont());
        int labelWidth = metrics.stringWidth(xAxisLabel);
        int labelHeight = metrics.getHeight();

        // X ekseni etiketi için merkezi konumu hesapla
        int labelX = (getWidth() - LEGEND_WIDTH - labelWidth) / 2 + PADDING;  // Ortalı pozisyon
        int labelY = getHeight() - PADDING / 5;

        return mouseX >= labelX && mouseX <= labelX + labelWidth
                && mouseY >= labelY - labelHeight && mouseY <= labelY;
    }

    private boolean isInsideYAxisLabel(int mouseX, int mouseY) {
        FontMetrics metrics = getGraphics().getFontMetrics(yAxisStyle.getFont());
        int labelWidth = metrics.stringWidth(yAxisLabel);
        int labelHeight = metrics.getHeight();

        // Y ekseni etiketi için döndürülmüş konumu hesapla
        int labelX = PADDING / 3;
        int labelY = getHeight() / 2 + labelWidth / 2;  // Ortalı pozisyon

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

    private void updateTooltip(int mouseX, int mouseY) {
        tooltipText = null;
        for (int i = 0; i < points.length; i++) {
            int pointScreenX = worldToScreenX(points[i][0]);
            int pointScreenY = worldToScreenY(points[i][1]);
            if (Math.abs(mouseX - pointScreenX) <= TOOLTIP_EXTRA_PIXELS
                    && Math.abs(mouseY - pointScreenY) <= TOOLTIP_EXTRA_PIXELS) {
                tooltipText = String.format("<html>Cluster: %s<br>X: %.2f<br>Y: %.2f</html>",
                        labels[i], points[i][0], points[i][1]);
                setToolTipText(tooltipText);
                return;
            }
        }
        setToolTipText(null);
    }

    @Override
    public void draw(Graphics2D g2) {
        // Başlığı çiz
        drawTitle(g2);

        // Nice step'leri hesapla
        float xStep = ((AbstractAxis) xAxis).calculateNiceStep(Math.abs(xAxis.getMax() - xAxis.getMin()));
        float yStep = ((AbstractAxis) yAxis).calculateNiceStep(Math.abs(yAxis.getMax() - yAxis.getMin()));

        // Grid çiz
        grid.drawGrid(g2, this, xStep, yStep);

        // Eksenleri çiz
        xAxis.drawAxis(g2, this);
        yAxis.drawAxis(g2, this);

        // Eksenlerin sayısal değerlerini çiz
        xAxis.drawAxisLabels(g2, this, xStep);
        yAxis.drawAxisLabels(g2, this, yStep);

        // Eksenlerin etiketlerini çiz
        xAxis.drawAxisLabel(g2, this);
        yAxis.drawAxisLabel(g2, this);

        // Noktaları çiz
        drawPoints(g2);

        // Eğer grid visible ise
        if (grid != null) {
            grid.drawGrid(g2, this, xStep, yStep);
        }

        // Eğer legend visible ise
        if (isLegendVisible) {
            drawLegend(g2);
        }
    }

    protected void updateAxes(float minX, float maxX, float minY, float maxY) {
        this.xAxis = new XAxis(minX, maxX, xAxisLabel);
        this.yAxis = new YAxis(minY, maxY, yAxisLabel);

        xAxis.setLabelStyle(xAxisStyle);
        yAxis.setLabelStyle(yAxisStyle);
    }

    private float calculateStep(float range) {
        float roughStep = range / TARGET_GRID_COUNT;
        float exponent = (float) Math.floor(Math.log10(roughStep));
        float fraction = roughStep / (float) Math.pow(10, exponent);

        float niceFraction;
        if (fraction < 1.5) {
            niceFraction = 1;
        } else if (fraction < 3) {
            niceFraction = 2;
        } else if (fraction < 7.5) {
            niceFraction = 5;
        } else {
            niceFraction = 10;
        }

        return niceFraction * (float) Math.pow(10, exponent);
    }

    private void drawPoints(Graphics2D g2) {
        // Noktaları çiz
        for (int i = 0; i < points.length; i++) {
            g2.setColor(colorPalette.getColor(labels[i]));
            int x = worldToScreenX(points[i][0]) - POINT_SIZE / 2;
            int y = worldToScreenY(points[i][1]) - POINT_SIZE / 2;

            // Ekran sınırları içinde olan noktaları çiz
            if (x > PADDING - POINT_SIZE && x < getWidth() - LEGEND_WIDTH - 10 + POINT_SIZE
                    && y > PADDING - POINT_SIZE && y < getHeight() - PADDING + POINT_SIZE) {
                g2.fillOval(x, y, POINT_SIZE, POINT_SIZE);
            }
        }
    }

    public void show() {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Örnek veri oluştur
        int numPoints = 100;
        float[][] points = new float[numPoints][2];
        String[] labels = new String[numPoints];
        Random random = new Random();

        for (int i = 0; i < numPoints; i++) {
            points[i][0] = random.nextFloat() * 100; // X değerleri
            points[i][1] = random.nextFloat() * 100; // Y değerleri
            if (i < 30) {
                labels[i] = "A";
            } else if (i < 60) {
                labels[i] = "B";
            } else {
                labels[i] = "C";
            }
        }
        ScatterPlot scatterPlot = new ScatterPlot(points, labels);
        scatterPlot.setTitle("Test Scatter Plot");
        scatterPlot.show();
    }
}
