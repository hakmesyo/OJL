/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.data_analytics.core;

/**
 *
 * @author cezerilab
 */
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlot extends JPanel implements Plot {

    protected String title;
    protected static final int PADDING = 60;
    protected static final int LEGEND_WIDTH = 150;
    protected static final int TARGET_GRID_COUNT = 10;
    protected final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 11);
    protected final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    protected final Font AXIS_FONT = new Font("SansSerif", Font.PLAIN, 12);

    // Interactive features
    protected float scale = 1.0f;
    protected float translateX = 0;
    protected float translateY = 0;
    protected Point lastPoint;
    protected Point mousePosition;
    protected String tooltipText = null;
    protected JPopupMenu popup;
    private Dimension dimension = new Dimension(800, 600);
    protected XAxis xAxis;
    protected YAxis yAxis;
    protected Grid grid;
    protected boolean isLegendVisible;
    protected List<LegendItem> legendItems = new ArrayList<>();
    protected int nClass = 0;
    protected ColorPalette colorPalette;
    protected Style xAxisStyle;
    protected Style yAxisStyle;
    protected Style titleStyle;
    protected Style gridStyle;
    protected String titleText;
    protected String xAxisLabel;
    protected String yAxisLabel;

    public AbstractPlot(boolean isLegendVisible) {
        this.isLegendVisible = isLegendVisible;
        setupPopupMenu();
        ToolTipManager.sharedInstance().setInitialDelay(0);
        setToolTipText("");
        setPreferredSize(dimension);
        setBackground(Color.WHITE);

        // Stil nesnelerini initialize et
        xAxisStyle = new Style();
        yAxisStyle = new Style();
        titleStyle = new Style();
        gridStyle = new Style();

        // Varsayılan metinleri ayarla
        titleText = "Title";
        xAxisLabel = "X Axis";
        yAxisLabel = "Y Axis";

        // Eksenleri varsayılan değerlerle initialize et
        this.xAxis = new XAxis(0, 100, xAxisLabel);  // Varsayılan min-max değerleri
        this.yAxis = new YAxis(0, 100, yAxisLabel);  // Alt sınıflarda güncellenebilir

        // Eksenlere stilleri ata
        xAxis.setLabelStyle(xAxisStyle);
        yAxis.setLabelStyle(yAxisStyle);
    }

    public void setxAxisLabel(String xAxisLabel) {
        this.xAxisLabel = xAxisLabel;
    }

    public void setyAxisLabel(String yAxisLabel) {
        this.yAxisLabel = yAxisLabel;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    @Override
    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
        setPreferredSize(dimension);
    }

    public void setxAxisStyle(Style xAxisStyle) {
        this.xAxisStyle = xAxisStyle;
    }

    public void setyAxisStyle(Style yAxisStyle) {
        this.yAxisStyle = yAxisStyle;
    }

    public void setTitleStyle(Style titleStyle) {
        this.titleStyle = titleStyle;
    }

    public void setGridStyle(Style gridStyle) {
        this.gridStyle = gridStyle;
    }

    public Style getxAxisStyle() {
        return xAxisStyle;
    }

    public Style getyAxisStyle() {
        return yAxisStyle;
    }

    public Style getTitleStyle() {
        return titleStyle;
    }

    public Style getGridStyle() {
        return gridStyle;
    }

    public void setLegendItems(List<LegendItem> legendItems) {
        this.legendItems = legendItems;
    }

    @Override
    public Dimension getDimension() {
        return dimension;
    }

    @Override
    public void setBackgroundColor(Color color) {
        setBackground(color);
    }

    @Override
    public JPopupMenu getPopupMenu() {
        return popup;
    }

    @Override
    public void resetView() {
        scale = 1.0f;
        translateX = 0;
        translateY = 0;
        repaint();
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    private void setupPopupMenu() {
        popup = new JPopupMenu();
        JMenuItem saveItem = new JMenuItem("Save Image As...");
        saveItem.addActionListener(e -> saveImage());
        popup.add(saveItem);

        JMenuItem resetItem = new JMenuItem("Reset View");
        resetItem.addActionListener(e -> resetView());
        popup.add(resetItem);
    }

    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Plot As");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "PNG Images", "png");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                String path = file.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".png")) {
                    file = new File(path + ".png");
                }

                BufferedImage image = new BufferedImage(getWidth(), getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = image.createGraphics();
                paintComponent(g2);
                g2.dispose();

                ImageIO.write(image, "png", file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Zoom bilgisini çiz
        g2.setColor(Color.BLACK);
        g2.setFont(LABEL_FONT);
        g2.drawString(String.format("Zoom: %.2fx", scale), 10, 20);

        draw(g2);
        if (isLegendVisible) {
            drawLegend(g2);
        }
    }

    protected void drawTitle(Graphics2D g2) {
        // Rendering kalitesini artır
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Font için hafif bir gölge efekti
        FontMetrics metrics = g2.getFontMetrics(titleStyle.getFont());
        int titleWidth = metrics.stringWidth(titleText);
        int x = (getWidth() - titleWidth) / 2;
        int y = PADDING / 2 + metrics.getAscent() / 2;

        // Çok hafif gölge efekti (opsiyonel)
        Color shadowColor = new Color(0, 0, 0, 30);  // Yarı saydam siyah
        g2.setColor(shadowColor);
        g2.drawString(titleText, x + 1, y + 1);

        // Ana metni çiz
        g2.setColor(titleStyle.getTextColor());
        g2.setFont(titleStyle.getFont());
        g2.drawString(titleText, x, y);
    }

    protected int worldToScreenX(float x) {
        if (xAxis == null) {
            return PADDING;
        }
        float plotWidth = getWidth() - PADDING - LEGEND_WIDTH - 10;
        return (int) (PADDING + (x - xAxis.getMin()) * plotWidth * scale / (xAxis.getMax() - xAxis.getMin()) + translateX);
    }

    protected int worldToScreenY(float y) {
        if (yAxis == null) {
            return PADDING;
        }
        float plotHeight = getHeight() - 2 * PADDING;
        return (int) (PADDING + (yAxis.getMax() - y) * plotHeight * scale / (yAxis.getMax() - yAxis.getMin()) + translateY);
    }

    protected float screenToWorldX(int screenX) {
        float plotWidth = getWidth() - PADDING - LEGEND_WIDTH - 10;
        return xAxis.getMin() + (screenX - PADDING - translateX) * (xAxis.getMax() - xAxis.getMin()) / (plotWidth * scale);
    }

    protected float screenToWorldY(int screenY) {
        float plotHeight = getHeight() - 2 * PADDING;
        return yAxis.getMax() - (screenY - PADDING - translateY) * (yAxis.getMax() - yAxis.getMin()) / (plotHeight * scale);
    }

    protected void drawLegend(Graphics2D g2) {
        // Rendering kalitesini artır
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int legendX = getWidth() - LEGEND_WIDTH + 10;
        int legendY = PADDING;
        int itemHeight = 20;

        // Legend arka planı - hafif gölge efekti
        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillRoundRect(legendX - 4, legendY + 1, LEGEND_WIDTH - 15,
                nClass * itemHeight + 30, 10, 10);

        // Legend arka planı - ana panel
        g2.setColor(new Color(250, 250, 250, 240));
        g2.fillRoundRect(legendX - 5, legendY, LEGEND_WIDTH - 15,
                nClass * itemHeight + 30, 10, 10);

        // Legend border - daha soft
        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(legendX - 5, legendY, LEGEND_WIDTH - 15,
                nClass * itemHeight + 30, 10, 10);

        // Legend başlığı
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Başlık gölgesi
        g2.setColor(new Color(0, 0, 0, 20));
        g2.drawString("Clusters", legendX + 1, legendY + 21);

        // Başlık metni
        g2.setColor(new Color(60, 60, 60));
        g2.drawString("Clusters", legendX, legendY + 20);

        // Legend itemları
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        for (int i = 0; i < legendItems.size(); i++) {
            int y = legendY + 35 + i * itemHeight;
            LegendItem item = legendItems.get(i);

            // Renk kutusu gölgesi
            g2.setColor(new Color(0, 0, 0, 30));
            g2.fillRect(legendX + 1, y + 1, 12, 12);

            // Renk kutusu
            g2.setColor(item.getColor());
            g2.fillRect(legendX, y, 12, 12);

            // Renk kutusu border
            g2.setColor(new Color(210, 210, 210));
            g2.drawRect(legendX, y, 12, 12);

            // Etiket gölgesi
            g2.setColor(new Color(0, 0, 0, 15));
            g2.drawString(item.getLabel(), legendX + 21, y + 11);

            // Etiket
            g2.setColor(new Color(40, 40, 40));
            g2.drawString(item.getLabel(), legendX + 20, y + 10);
        }
    }

    public void setnClass(int nClass) {
        this.nClass = nClass;
    }

}
