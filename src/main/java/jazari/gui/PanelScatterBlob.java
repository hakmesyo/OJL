package jazari.gui;

import jazari.types.TFigureAttribute;
import jazari.matrix.CMatrix;
import jazari.factory.FactoryUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

/**
 *
 * @author BAP1 - Refactored by Gemini for Performance and Modern UI
 */
public final class PanelScatterBlob extends javax.swing.JPanel {

    // Helper Inner Class - Implementing Comparable for sorting
    private static class BlobPoint implements Comparable<BlobPoint> {
        double xData, yData;
        Color color;
        int itemIndex;

        @Override
        public int compareTo(BlobPoint other) {
            return Double.compare(this.xData, other.xData);
        }
    }

    // == 1. DATA and ATTRIBUTES ==
    private CMatrix cm = null;
    private TFigureAttribute figureAttribute;
    private String[] axis = new String[]{"Dimension-1", "Dimension-2"};
    private String[] items;
    private String title = "Scatter Plot";
    private BlobPoint[] blobPoints; // Sorted array of blob points

    // == 2. VIEWPORT STATE for ZOOM/PAN ==
    private double viewXMin, viewXMax, viewYMin, viewYMax;
    private double dataXMin, dataXMax, dataYMin, dataYMax;

    // == 3. INTERACTION STATE ==
    private boolean isInteracting = false; // Flag for draft mode rendering
    private Point panStartPoint;
    private Rectangle zoomRect;
    private final Timer interactionTimer; // Timer to detect end of interaction

    // == 4. AESTHETIC and STYLE CONSTANTS ==
    private final Color COLOR_BACKGROUND = Color.WHITE;
    private final Color COLOR_PLOT_AREA_BACKGROUND = new Color(248, 248, 248);
    private final Color COLOR_GRID = new Color(220, 220, 220);
    private final Color COLOR_AXIS = new Color(50, 50, 50);
    private final Color COLOR_TEXT = new Color(20, 20, 20);
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private final Font FONT_AXIS_LABEL = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_TICK_LABEL = new Font("Segoe UI", Font.PLAIN, 11);
    private final Color[] MODERN_PALETTE = {
        new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44),
        new Color(214, 39, 40), new Color(148, 103, 189), new Color(140, 86, 75),
        new Color(227, 119, 194), new Color(127, 127, 127), new Color(188, 189, 34),
        new Color(23, 190, 207)
    };
    private Color[] distinctColors;

    // == 5. UI COMPONENTS ==
    private JLabel tooltipLabel;
    private long rand_seed;

    public PanelScatterBlob() {
        initialize();
        // Timer fires once, 50ms after the last interaction, to trigger a high-quality repaint
        interactionTimer = new Timer(50, e -> {
            isInteracting = false;
            repaint();
        });
        interactionTimer.setRepeats(false);
    }
    
    public PanelScatterBlob(CMatrix cm) {
        this();
        this.cm = cm;
        processData();
    }

    public PanelScatterBlob(CMatrix cm, TFigureAttribute attr) {
        this();
        this.cm = cm;
        this.figureAttribute = attr;
        this.axis = attr.axis_names;
        this.items = attr.items;
        this.title = attr.title;
        processData();
    }

    private void initialize() {
        this.setBackground(COLOR_BACKGROUND);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setupTooltip();
        addInteractionListeners();
    }

    private void processData() {
        if (cm == null || cm.getRowNumber() < 2) {
            blobPoints = null; // Clear data
            return;
        }
        float[][] dm = cm.toFloatArray2D();
        dataXMin = FactoryUtils.getMinimum(dm[0]);
        dataXMax = FactoryUtils.getMaximum(dm[0]);
        dataYMin = FactoryUtils.getMinimum(dm[1]);
        dataYMax = FactoryUtils.getMaximum(dm[1]);

        double xPadding = (dataXMax - dataXMin) * 0.05;
        double yPadding = (dataYMax - dataYMin) * 0.05;
        if (xPadding == 0) xPadding = 1; // Avoid zero range
        if (yPadding == 0) yPadding = 1;
        dataXMin -= xPadding; dataXMax += xPadding;
        dataYMin -= yPadding; dataYMax += yPadding;

        resetView();

        int lastIndex = dm.length - 1;
        float[] distinct_color_indexes = FactoryUtils.getDistinctValues(dm[lastIndex]);
        distinctColors = new Color[distinct_color_indexes.length];
        for (int i = 0; i < distinct_color_indexes.length; i++) {
            distinctColors[i] = MODERN_PALETTE[i % MODERN_PALETTE.length];
        }

        if (this.items == null || this.items.length != distinct_color_indexes.length) {
            this.items = new String[distinct_color_indexes.length];
            for (int i = 0; i < distinct_color_indexes.length; i++) {
                this.items[i] = "Blob-" + (i + 1);
            }
        }
        
        blobPoints = new BlobPoint[dm[0].length];
        for (int j = 0; j < dm[0].length; j++) {
            BlobPoint p = new BlobPoint();
            p.xData = dm[0][j];
            p.yData = dm[1][j];
            p.itemIndex = (int) dm[lastIndex][j];
            p.color = distinctColors[p.itemIndex];
            blobPoints[j] = p;
        }
        
        // *** PERFORMANCE OPTIMIZATION 1: Sort the data by X-coordinate ***
        Arrays.sort(blobPoints);
        
        repaint();
    }

    private void resetView() {
        viewXMin = dataXMin;
        viewXMax = dataXMax;
        viewYMin = dataYMin;
        viewYMax = dataYMax;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // *** PERFORMANCE OPTIMIZATION 2: Use draft quality during interaction ***
        if (isInteracting) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        } else {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        }

        if (blobPoints == null || blobPoints.length == 0) {
            g2d.setColor(COLOR_TEXT);
            g2d.setFont(FONT_AXIS_LABEL);
            String msg = "No data to display.";
            FontMetrics fm = g2d.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            g2d.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
            return;
        }

        Rectangle plotArea = calculatePlotArea(g2d);

        drawGrid(g2d, plotArea);
        drawAxes(g2d, plotArea);
        drawDataPoints(g2d, plotArea);
        drawLegend(g2d, plotArea);
        drawTitle(g2d);

        if (zoomRect != null) {
            g2d.setColor(new Color(0, 100, 255, 50));
            g2d.fill(zoomRect);
            g2d.setColor(new Color(0, 100, 255, 200));
            g2d.draw(zoomRect);
        }
    }
    
    private void drawDataPoints(Graphics2D g2d, Rectangle plotArea) {
        // *** PERFORMANCE OPTIMIZATION 1: Find start/end index of visible points ***
        BlobPoint searchStart = new BlobPoint(); searchStart.xData = viewXMin;
        BlobPoint searchEnd = new BlobPoint(); searchEnd.xData = viewXMax;
        
        int startIndex = Arrays.binarySearch(blobPoints, searchStart);
        if (startIndex < 0) {
            startIndex = -(startIndex + 1);
        }
        
        int endIndex = Arrays.binarySearch(blobPoints, searchEnd);
        if (endIndex < 0) {
            endIndex = -(endIndex + 1);
        }

        endIndex = Math.min(endIndex, blobPoints.length);

        int r = 8;
        // Now, loop only over the potentially visible points
        for (int i = startIndex; i < endIndex; i++) {
            BlobPoint p = blobPoints[i];
            // We still need to check Y-axis
            if (p.yData >= viewYMin && p.yData <= viewYMax) {
                int screenX = mapDataToScreenX(p.xData, plotArea);
                int screenY = mapDataToScreenY(p.yData, plotArea);
                
                g2d.setColor(p.color);
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.fillOval(screenX - r / 2, screenY - r / 2, r, r);
                
                // *** PERFORMANCE OPTIMIZATION 2: Skip drawing outline in draft mode ***
                if (!isInteracting) {
                    g2d.setColor(p.color.darker());
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
                    g2d.drawOval(screenX - r / 2, screenY - r / 2, r, r);
                }
            }
        }
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void addInteractionListeners() {
        addMouseWheelListener(e -> {
            isInteracting = true;
            handleZoom(e);
            interactionTimer.restart(); // Reset timer for high-quality repaint
        });
        
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    isInteracting = true;
                    panStartPoint = e.getPoint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    isInteracting = true;
                    zoomRect = new Rectangle(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                boolean needsRepaint = (panStartPoint != null || zoomRect != null);
                isInteracting = false; 
                
                if (panStartPoint != null) {
                    panStartPoint = null;
                }
                if (zoomRect != null) {
                    handleBoxZoom();
                    zoomRect = null;
                }
                if (needsRepaint) {
                    repaint(); // Trigger final high-quality repaint
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    resetView();
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!isInteracting) {
                    updateTooltip(e.getPoint());
                }
            }
        };
        addMouseListener(mouseAdapter);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (panStartPoint != null) {
                    handlePan(e);
                } else if (zoomRect != null) {
                    zoomRect.setSize(e.getX() - zoomRect.x, e.getY() - zoomRect.y);
                    repaint();
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!isInteracting) {
                    updateTooltip(e.getPoint());
                }
            }
        });
    }

    private Rectangle calculatePlotArea(Graphics2D g2d) {
        Insets insets = getInsets();
        int paddingLeft = 60;
        int paddingRight = 150; // For legend
        int paddingTop = 50; // For title
        int paddingBottom = 50;
        
        int x = insets.left + paddingLeft;
        int y = insets.top + paddingTop;
        int width = getWidth() - insets.left - insets.right - paddingLeft - paddingRight;
        int height = getHeight() - insets.top - insets.bottom - paddingTop - paddingBottom;
        
        return new Rectangle(x, y, width, height);
    }

    private void drawGrid(Graphics2D g2d, Rectangle plotArea) {
        g2d.setColor(COLOR_PLOT_AREA_BACKGROUND);
        g2d.fill(plotArea);
        g2d.setColor(COLOR_GRID);

        int numTicks = 10;
        for (int i = 0; i <= numTicks; i++) {
            double dataX = viewXMin + (i * (viewXMax - viewXMin) / numTicks);
            int screenX = mapDataToScreenX(dataX, plotArea);
            g2d.drawLine(screenX, plotArea.y, screenX, plotArea.y + plotArea.height);
        }
        for (int i = 0; i <= numTicks; i++) {
            double dataY = viewYMin + (i * (viewYMax - viewYMin) / numTicks);
            int screenY = mapDataToScreenY(dataY, plotArea);
            g2d.drawLine(plotArea.x, screenY, plotArea.x + plotArea.width, screenY);
        }
    }

    private void drawAxes(Graphics2D g2d, Rectangle plotArea) {
        g2d.setColor(COLOR_AXIS);
        g2d.setStroke(new BasicStroke(2f));

        g2d.drawLine(plotArea.x, plotArea.y + plotArea.height, plotArea.x + plotArea.width, plotArea.y + plotArea.height); // X-axis
        g2d.drawLine(plotArea.x, plotArea.y, plotArea.x, plotArea.y + plotArea.height); // Y-axis

        g2d.setFont(FONT_TICK_LABEL);
        int numTicks = 10;

        for (int i = 0; i <= numTicks; i++) {
            double dataX = viewXMin + (i * (viewXMax - viewXMin) / numTicks);
            int screenX = mapDataToScreenX(dataX, plotArea);
            String label = String.format("%.1f", dataX);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawLine(screenX, plotArea.y + plotArea.height, screenX, plotArea.y + plotArea.height + 5);
            g2d.drawString(label, screenX - labelWidth / 2, plotArea.y + plotArea.height + 20);
        }

        for (int i = 0; i <= numTicks; i++) {
            double dataY = viewYMin + (i * (viewYMax - viewYMin) / numTicks);
            int screenY = mapDataToScreenY(dataY, plotArea);
            String label = String.format("%.1f", dataY);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.drawLine(plotArea.x, screenY, plotArea.x - 5, screenY);
            g2d.drawString(label, plotArea.x - labelWidth - 10, screenY + fm.getAscent() / 2);
        }
        
        g2d.setFont(FONT_AXIS_LABEL);
        FontMetrics fm = g2d.getFontMetrics();
        int xLabelWidth = fm.stringWidth(axis[0]);
        g2d.drawString(axis[0], plotArea.x + (plotArea.width - xLabelWidth) / 2, getHeight() - getInsets().bottom - 5);
        
        int yLabelWidth = fm.stringWidth(axis[1]);
        g2d.translate(getInsets().left + 20, plotArea.y + (plotArea.height + yLabelWidth) / 2);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(axis[1], 0, 0);
        g2d.rotate(Math.PI / 2);
        g2d.translate(-(getInsets().left + 20), -(plotArea.y + (plotArea.height + yLabelWidth) / 2));
    }
    
    private void drawLegend(Graphics2D g2d, Rectangle plotArea) {
        g2d.setFont(FONT_AXIS_LABEL);
        g2d.setColor(COLOR_TEXT);
        int legendX = plotArea.x + plotArea.width + 30;
        int legendY = plotArea.y;
        g2d.drawString("Items", legendX, legendY);
        
        g2d.setFont(FONT_TICK_LABEL);
        int itemHeight = 20;
        for (int i = 0; i < items.length; i++) {
            g2d.setColor(distinctColors[i]);
            g2d.fillRect(legendX, legendY + 15 + (i * itemHeight), 10, 10);
            g2d.setColor(COLOR_TEXT);
            g2d.drawString(items[i], legendX + 20, legendY + 25 + (i * itemHeight));
        }
    }
    
    private void drawTitle(Graphics2D g2d) {
        g2d.setFont(FONT_TITLE);
        g2d.setColor(COLOR_TEXT);
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, getInsets().top + 20);
    }

    private void setupTooltip() {
        tooltipLabel = new JLabel();
        this.setLayout(null);
        this.add(tooltipLabel);
        tooltipLabel.setOpaque(true);
        tooltipLabel.setBackground(new Color(255, 255, 225));
        tooltipLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        tooltipLabel.setFont(FONT_TICK_LABEL);
        tooltipLabel.setVisible(false);
    }

    private void handleZoom(MouseWheelEvent e) {
        double zoomFactor = e.getWheelRotation() < 0 ? 0.9 : 1.1;
        Point mousePoint = e.getPoint();
        Rectangle plotArea = calculatePlotArea((Graphics2D) getGraphics());

        if (!plotArea.contains(mousePoint)) return;

        double mouseDataX = mapScreenToDataX(mousePoint.x, plotArea);
        double mouseDataY = mapScreenToDataY(mousePoint.y, plotArea);

        viewXMin = mouseDataX - (mouseDataX - viewXMin) * zoomFactor;
        viewXMax = mouseDataX + (viewXMax - mouseDataX) * zoomFactor;
        viewYMin = mouseDataY - (mouseDataY - viewYMin) * zoomFactor;
        viewYMax = mouseDataY + (viewYMax - mouseDataY) * zoomFactor;

        repaint();
    }

    private void handlePan(MouseEvent e) {
        if (panStartPoint == null) return;
        int dx = e.getX() - panStartPoint.x;
        int dy = e.getY() - panStartPoint.y;

        Rectangle plotArea = calculatePlotArea((Graphics2D) getGraphics());
        double dataDx = dx * (viewXMax - viewXMin) / plotArea.width;
        double dataDy = dy * (viewYMax - viewYMin) / plotArea.height;

        viewXMin -= dataDx;
        viewXMax -= dataDx;
        viewYMin += dataDy;
        viewYMax += dataDy;

        panStartPoint = e.getPoint();
        repaint();
    }
    
    private void handleBoxZoom() {
        if (zoomRect == null || zoomRect.width == 0 || zoomRect.height == 0) return;
        
        Rectangle plotArea = calculatePlotArea((Graphics2D) getGraphics());
        
        int x1 = Math.min(zoomRect.x, zoomRect.x + zoomRect.width);
        int y1 = Math.min(zoomRect.y, zoomRect.y + zoomRect.height);
        int x2 = Math.max(zoomRect.x, zoomRect.x + zoomRect.width);
        int y2 = Math.max(zoomRect.y, zoomRect.y + zoomRect.height);

        viewXMin = mapScreenToDataX(x1, plotArea);
        viewXMax = mapScreenToDataX(x2, plotArea);
        viewYMin = mapScreenToDataY(y2, plotArea); // Y is inverted
        viewYMax = mapScreenToDataY(y1, plotArea);
    }
    
    private void updateTooltip(Point mousePos) {
        if (blobPoints == null) return;
        Rectangle plotArea = calculatePlotArea((Graphics2D) getGraphics());
        if (!plotArea.contains(mousePos)) {
            tooltipLabel.setVisible(false);
            return;
        }

        // A simple linear search for tooltips is generally fast enough, 
        // as it only happens on mouseMove, not drag.
        for (BlobPoint p : blobPoints) {
            if (p.xData >= viewXMin && p.xData <= viewXMax && p.yData >= viewYMin && p.yData <= viewYMax) {
                int screenX = mapDataToScreenX(p.xData, plotArea);
                int screenY = mapDataToScreenY(p.yData, plotArea);

                if (mousePos.distance(screenX, screenY) < 5) {
                    String msg = String.format("<html><b>%s</b><br>X: %.2f<br>Y: %.2f</html>",
                            items[p.itemIndex], p.xData, p.yData);
                    tooltipLabel.setText(msg);
                    tooltipLabel.setSize(tooltipLabel.getPreferredSize());
                    tooltipLabel.setLocation(mousePos.x + 15, mousePos.y - 15);
                    tooltipLabel.setVisible(true);
                    return;
                }
            }
        }
        tooltipLabel.setVisible(false);
    }

    private int mapDataToScreenX(double dataX, Rectangle plotArea) {
        return plotArea.x + (int) ((dataX - viewXMin) * plotArea.width / (viewXMax - viewXMin));
    }

    private int mapDataToScreenY(double dataY, Rectangle plotArea) {
        return plotArea.y + plotArea.height - (int) ((dataY - viewYMin) * plotArea.height / (viewYMax - viewYMin));
    }

    private double mapScreenToDataX(int screenX, Rectangle plotArea) {
        return viewXMin + ((double) (screenX - plotArea.x) * (viewXMax - viewXMin) / plotArea.width);
    }

    private double mapScreenToDataY(int screenY, Rectangle plotArea) {
        return viewYMin + ((double) (plotArea.y + plotArea.height - screenY) * (viewYMax - viewYMin) / plotArea.height);
    }

    public void setFigureAttribute(TFigureAttribute attr) {
        this.figureAttribute = attr;
        this.axis = attr.axis_names;
        this.items = attr.items;
        this.title = attr.title;
        processData();
    }

    public void setMatrix(CMatrix cm) {
        this.cm = cm.transpose();
        processData();
    }

    public CMatrix getMatrix() {
        return this.cm;
    }
    
    public void setRandomSeed(long seed) {
        this.rand_seed = seed;
        // Note: Currently, random seed is not used as we have a fixed modern palette.
        // If you want to re-enable random colors, you would use it here.
    }
    
    @SuppressWarnings("unchecked")
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
    }
}