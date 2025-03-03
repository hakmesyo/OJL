package jazari.machine_learning.analysis.distribution;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

public class ScatterPlotViewer extends JPanel {

    // Data variables
    private float[][] points;
    private String[] labels;
    private Map<String, Color> colors = new HashMap<>();
    private String title;

    // Bounds
    private float minX, maxX, minY, maxY;

    // Display settings
    private static final int PADDING = 60;
    private static final int LEGEND_WIDTH = 150;
    private static final int POINT_SIZE = 8;
    private static final int TARGET_GRID_COUNT = 10; // Hedeflenen grid çizgi sayısı
    private final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 11);
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private final Font AXIS_FONT = new Font("SansSerif", Font.PLAIN, 12);

    // Interactive features
    private float scale = 1.0f;
    private float translateX = 0;
    private float translateY = 0;
    private Point lastPoint;
    private Point mousePosition;
    private String tooltipText = null;
    private JPopupMenu popup;
    private int nClass = 0;

    public ScatterPlotViewer(float[][] points, String[] labels) {
        this.points = points;
        this.labels = labels;
        this.title = "Scatter Plot";
        
        calculateBounds();
        initColors();
        setupInteractions();
        setupPopupMenu();

        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.WHITE);

        ToolTipManager.sharedInstance().setInitialDelay(0);
        setToolTipText("");
    }
    
    // Varsayılan etiketlerle başlatmak için ek constructor
    public ScatterPlotViewer(float[][] points) {
        this(points, generateDefaultLabels(points.length));
    }
    
    // Varsayılan etiketleri oluşturan yardımcı metod
    private static String[] generateDefaultLabels(int count) {
        String[] defaultLabels = new String[count];
        for (int i = 0; i < count; i++) {
            defaultLabels[i] = "0";  // Varsayılan olarak tüm noktalar "0" kümesinde
        }
        return defaultLabels;
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

    private void resetView() {
        scale = 1.0f;
        translateX = 0;
        translateY = 0;
        repaint();
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
                paint(g2);
                g2.dispose();

                ImageIO.write(image, "png", file);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initColors() {
        // Benzersiz etiketleri topla
        Set<String> uniqueLabels = new HashSet<>();
        for (String label : labels) {
            uniqueLabels.add(label);
        }
        
        // nClass'ı güncelle
        nClass = uniqueLabels.size();
        
        float hue = 0.0f;
        float saturation = 0.8f;
        float brightness = 0.9f;
        float hueStep = 1.0f / Math.max(1, nClass);  // Sıfıra bölünmeyi önlemek için

        // Her benzersiz etiket için renk ata
        for (String label : uniqueLabels) {
            if (!colors.containsKey(label)) {  // Eğer key yoksa
                colors.put(label, Color.getHSBColor(hue, saturation, brightness));
                hue += hueStep;
            }
        }
        
        // Eğer colors haritası boşsa varsayılan bir renk ekle
        if (colors.isEmpty()) {
            colors.put("default", Color.BLUE);
        }
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
                // Mouse pozisyonunun plot içinde olup olmadığını kontrol et
                if (e.getX() < PADDING || e.getX() > getWidth() - LEGEND_WIDTH - 10
                        || e.getY() < PADDING || e.getY() > getHeight() - PADDING) {
                    return;
                }

                // Eski scale değerini sakla
                float oldScale = scale;

                // Zoom faktörünü hesapla (daha yumuşak zoom için)
                scale *= Math.pow(1.05, -e.getWheelRotation());
                scale = Math.max(0.1f, Math.min(10.0f, scale));

                translateX = e.getX() - (e.getX() - translateX) * scale / oldScale;
                translateY = e.getY() - (e.getY() - translateY) * scale / oldScale;

                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    translateX += e.getX() - lastPoint.x;
                    translateY += e.getY() - lastPoint.y;
                    lastPoint = e.getPoint();
                    repaint();
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    private void updateTooltip(int mouseX, int mouseY) {
        tooltipText = null;
        float worldX = screenToWorldX(mouseX);
        float worldY = screenToWorldY(mouseY);

        for (int i = 0; i < points.length; i++) {
            float dx = points[i][0] - worldX;
            float dy = points[i][1] - worldY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < 0.5 / scale) {  // Scale'e göre ayarlanmış mesafe kontrolü
                tooltipText = String.format("<html>Cluster: %s<br>X: %.2f<br>Y: %.2f</html>",
                        labels[i], points[i][0], points[i][1]);
                setToolTipText(tooltipText);
                return;
            }
        }
        setToolTipText(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Zoom bilgisini çiz
        g2.setColor(Color.BLACK);
        g2.setFont(LABEL_FONT);
        g2.drawString(String.format("Zoom: %.2fx", scale), 10, 20);

        drawTitle(g2);
        drawGrid(g2);
        drawAxes(g2);
        drawAxisLabels(g2);
        drawPoints(g2);
        drawLegend(g2);
    }

    private float calculateStep(float range) {
        // Hedeflenen aralık sayısına göre yaklaşık step büyüklüğünü hesapla
        float roughStep = range / TARGET_GRID_COUNT;

        // En yakın "güzel" sayıyı bul (1, 2, 5 veya bunların 10'un katları)
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

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(0.5f));

        // Görünür alan sınırlarını hesapla
        float visibleMinX = screenToWorldX(PADDING);
        float visibleMaxX = screenToWorldX(getWidth() - LEGEND_WIDTH - 10);
        float visibleMinY = screenToWorldY(getHeight() - PADDING);
        float visibleMaxY = screenToWorldY(PADDING);

        // Grid aralıklarını hesapla
        float xStep = calculateStep(Math.abs(visibleMaxX - visibleMinX));
        float yStep = calculateStep(Math.abs(visibleMaxY - visibleMinY));

        // İlk grid çizgisinin konumunu hesapla
        float xStart = (float) Math.floor(visibleMinX / xStep) * xStep;
        float yStart = (float) Math.floor(visibleMinY / yStep) * yStep;

        // Grid çizgilerini çiz
        for (float x = xStart; x <= visibleMaxX + xStep / 2; x += xStep) {
            int screenX = worldToScreenX(x);
            g2.drawLine(screenX, PADDING, screenX, getHeight() - PADDING);
        }

        for (float y = yStart; y <= visibleMaxY + yStep / 2; y += yStep) {
            int screenY = worldToScreenY(y);
            g2.drawLine(PADDING, screenY, getWidth() - LEGEND_WIDTH - 10, screenY);
        }
    }

    private void drawTitle(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.setFont(TITLE_FONT);
        FontMetrics metrics = g2.getFontMetrics();
        int titleWidth = metrics.stringWidth(title);
        g2.drawString(title, (getWidth() - titleWidth) / 2, PADDING / 2);
    }

    private void drawAxes(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));

        // Draw X axis
        g2.drawLine(PADDING, getHeight() - PADDING,
                getWidth() - LEGEND_WIDTH - 10, getHeight() - PADDING);

        // Draw Y axis
        g2.drawLine(PADDING, PADDING, PADDING, getHeight() - PADDING);
    }

    private DecimalFormat createDynamicFormatter(float step) {
        // Step değerine göre dinamik format oluştur
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

    private void drawAxisLabels(Graphics2D g2) {
        g2.setFont(AXIS_FONT);
        FontMetrics metrics = g2.getFontMetrics();

        // Görünür alan sınırlarını hesapla
        float visibleMinX = screenToWorldX(PADDING);
        float visibleMaxX = screenToWorldX(getWidth() - LEGEND_WIDTH - 10);
        float visibleMinY = screenToWorldY(getHeight() - PADDING);
        float visibleMaxY = screenToWorldY(PADDING);

        // Grid aralıklarını hesapla
        float xStep = calculateStep(Math.abs(visibleMaxX - visibleMinX));
        float yStep = calculateStep(Math.abs(visibleMaxY - visibleMinY));

        // İlk grid çizgisinin konumunu hesapla
        float xStart = (float) Math.floor(visibleMinX / xStep) * xStep;
        float yStart = (float) Math.floor(visibleMinY / yStep) * yStep;

        // Etiketleri formatla ve çiz
        DecimalFormat labelFormat = createDynamicFormatter(xStep);

        // X ekseni etiketleri
        for (float x = xStart; x <= visibleMaxX + xStep / 2; x += xStep) {
            int screenX = worldToScreenX(x);
            String label = labelFormat.format(x);
            g2.drawString(label, screenX - metrics.stringWidth(label) / 2,
                    getHeight() - PADDING + metrics.getHeight() + 5);
        }

        // Y ekseni etiketleri
        labelFormat = createDynamicFormatter(yStep);
        for (float y = yStart; y <= visibleMaxY + yStep / 2; y += yStep) {
            int screenY = worldToScreenY(y);
            String label = labelFormat.format(y);

            g2.drawString(label, PADDING - metrics.stringWidth(label) - 5,
                    screenY + metrics.getHeight() / 2 - 2);
        }

        // Eksen başlıkları
        g2.drawString("X", getWidth() - LEGEND_WIDTH - 30, getHeight() - PADDING / 3);

        AffineTransform original = g2.getTransform();
        g2.rotate(-Math.PI / 2);
        g2.drawString("Y", -getHeight() / 2, PADDING / 2);
        g2.setTransform(original);
    }

    private void drawPoints(Graphics2D g2) {
        // Noktaları çiz
        for (int i = 0; i < points.length; i++) {
            // labels[i] için renk bulunamadığında varsayılan renk kullan
            Color pointColor = colors.getOrDefault(labels[i], Color.BLUE);
            g2.setColor(pointColor);
            
            int x = worldToScreenX(points[i][0]) - POINT_SIZE / 2;
            int y = worldToScreenY(points[i][1]) - POINT_SIZE / 2;

            // Ekran sınırları içinde olan noktaları çiz
            if (x > PADDING - POINT_SIZE && x < getWidth() - LEGEND_WIDTH - 10 + POINT_SIZE
                    && y > PADDING - POINT_SIZE && y < getHeight() - PADDING + POINT_SIZE) {
                g2.fillOval(x, y, POINT_SIZE, POINT_SIZE);
            }
        }
    }

    private void drawLegend(Graphics2D g2) {
        // Renk haritası boşsa legend çizme
        if (colors.isEmpty()) {
            return;
        }
        
        int legendX = getWidth() - LEGEND_WIDTH + 10;
        int legendY = PADDING;
        int itemHeight = 20;

        // Legend arka planı
        g2.setColor(new Color(250, 250, 250, 240));
        g2.fillRoundRect(legendX - 5, legendY, LEGEND_WIDTH - 15,
                Math.max(1, nClass) * itemHeight + 30, 10, 10);
        g2.setColor(new Color(200, 200, 200));
        g2.drawRoundRect(legendX - 5, legendY, LEGEND_WIDTH - 15,
                Math.max(1, nClass) * itemHeight + 30, 10, 10);

        // Legend başlığı
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Clusters", legendX, legendY + 20);

        // Legend itemları
        g2.setFont(LABEL_FONT);
        String[] keys = colors.keySet().toArray(new String[0]);
        
        // Boş kontrol
        if (keys.length == 0) {
            return;
        }
        
        for (int i = 0; i < Math.min(nClass, keys.length); i++) {
            int y = legendY + 35 + i * itemHeight;

            // Renk kutusu
            String key = keys[i];
            Color color_value = colors.get(key);
            g2.setColor(color_value);
            g2.fillRect(legendX, y, 12, 12);

            // Etiket
            g2.setColor(Color.BLACK);
            g2.drawString("Cluster " + i, legendX + 20, y + 10);
        }
    }

    private int worldToScreenX(float x) {
        float plotWidth = getWidth() - PADDING - LEGEND_WIDTH - 10;
        return (int) (PADDING + (x - minX) * plotWidth * scale / (maxX - minX) + translateX);
    }

    private int worldToScreenY(float y) {
        float plotHeight = getHeight() - 2 * PADDING;
        return (int) (PADDING + (maxY - y) * plotHeight * scale / (maxY - minY) + translateY);
    }

    private float screenToWorldX(int screenX) {
        float plotWidth = getWidth() - PADDING - LEGEND_WIDTH - 10;
        return minX + (screenX - PADDING - translateX) * (maxX - minX) / (plotWidth * scale);
    }

    private float screenToWorldY(int screenY) {
        float plotHeight = getHeight() - 2 * PADDING;
        return maxY - (screenY - PADDING - translateY) * (maxY - minY) / (plotHeight * scale);
    }

    public void setTitle(String title) {
        this.title = title;
        repaint();
    }

    public void show() {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}