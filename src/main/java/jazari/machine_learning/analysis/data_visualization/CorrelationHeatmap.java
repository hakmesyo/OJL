package jazari.machine_learning.analysis.data_visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

public class CorrelationHeatmap extends JPanel {

    private double[][] correlations;
    private String[] labels;
    private static final int MIN_CELL_SIZE = 40;  // Minimum hücre boyutu
    private static final int PADDING = 100;
    private static final Font LABEL_FONT = new Font("DejaVu Sans", Font.PLAIN, 12);
    private static final Font TITLE_FONT = new Font("DejaVu Sans", Font.BOLD, 16);

    public CorrelationHeatmap(double[][] correlations, String[] labels) {
        this.correlations = correlations;
        // Feature isimlerini kısalt
        this.labels = new String[labels.length];
        for (int i = 0; i < labels.length - 1; i++) {
            this.labels[i] = "F" + i;
        }
        this.labels[labels.length - 1] = "Out";  // Output için özel etiket

        setPreferredSize(new Dimension(600, 600));  // Başlangıç boyutu
        setBackground(Color.WHITE);
        setDoubleBuffered(true);

        // Resize listener ekle
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        setupGraphics(g2d);

        // Dinamik hücre boyutu hesapla
        int cellSize = calculateCellSize();

        drawTitle(g2d);
        drawHeatmap(g2d, cellSize);
        drawLabels(g2d, cellSize);
        drawColorbar(g2d);
    }

    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, getWidth(), getHeight());
    }

    private int calculateCellSize() {
        int availableWidth = getWidth() - (2 * PADDING);
        int availableHeight = getHeight() - (2 * PADDING);
        int size = Math.min(availableWidth, availableHeight) / correlations.length;
        return Math.max(size, MIN_CELL_SIZE);  // Minimum boyuttan küçük olmasın
    }

    private void drawHeatmap(Graphics2D g2d, int cellSize) {
        for (int i = 0; i < correlations.length; i++) {
            for (int j = 0; j < correlations[i].length; j++) {
                int x = PADDING + j * cellSize;
                int y = PADDING + i * cellSize;

                // Hücre rengi
                Color color = getCorrelationColor(correlations[i][j]);
                g2d.setColor(color);
                g2d.fillRect(x, y, cellSize, cellSize);

                // Hücre çerçevesi
                g2d.setColor(Color.WHITE);
                g2d.drawRect(x, y, cellSize, cellSize);

                // Korelasyon değeri
                g2d.setColor(Color.BLACK);
                g2d.setFont(LABEL_FONT);
                String value = new DecimalFormat("0.00").format(correlations[i][j]);
                FontMetrics fm = g2d.getFontMetrics();
                int valueWidth = fm.stringWidth(value);
                g2d.drawString(value,
                        x + (cellSize - valueWidth) / 2,
                        y + cellSize / 2 + fm.getAscent() / 2);
            }
        }
    }

    private Color getCorrelationColor(double correlation) {
        // Kırmızı (negatif) -> Beyaz (0) -> Mavi (pozitif)
        if (correlation < 0) {
            int red = 255;
            int blue = 255 + (int) (correlation * 255);
            int green = blue;
            return new Color(red, green, blue);
        } else {
            int blue = 255;
            int red = 255 - (int) (correlation * 255);
            int green = red;
            return new Color(red, green, blue);
        }
    }

    private void drawLabels(Graphics2D g2d, int cellSize) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(LABEL_FONT);
        FontMetrics fm = g2d.getFontMetrics();

        for (int i = 0; i < labels.length; i++) {
            // Yatay etiketler
            g2d.drawString(labels[i],
                    PADDING + i * cellSize + (cellSize - fm.stringWidth(labels[i])) / 2,
                    PADDING - 10);

            // Dikey etiketler
            Graphics2D g2 = (Graphics2D) g2d.create();
            g2.translate(PADDING - 10,
                    PADDING + i * cellSize + (cellSize + fm.stringWidth(labels[i])) / 2);
            g2.rotate(-Math.PI / 2);
            g2.drawString(labels[i], 0, 0);
            g2.dispose();
        }
    }

    private void drawTitle(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(TITLE_FONT);
        String title = "Feature Correlation Matrix";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, PADDING / 2);
    }

    private void drawColorbar(Graphics2D g2d) {
        // Color bar boyutlarını pencere boyutuna göre ayarla
        int barWidth = Math.max(getWidth() / 40, 15);  // Minimum 15px
        int barHeight = Math.min(getHeight() - PADDING * 2, getHeight() * 2/3);  // Maksimum 2/3 pencere yüksekliği
        int x = getWidth() - PADDING / 2 - barWidth;  // Sağ kenardan biraz içeride
        int y = (getHeight() - barHeight) / 2;

        // Gradient çiz
        for (int i = 0; i < barHeight; i++) {
            double correlation = 1.0 - 2.0 * i / (double)barHeight;
            g2d.setColor(getCorrelationColor(correlation));
            g2d.fillRect(x, y + i, barWidth, 1);
        }

        // Çerçeve
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, barWidth, barHeight);

        // Değerler için font boyutunu ayarla
        int fontSize = Math.max(Math.min(barWidth / 2, 12), 8);  // 8-12 px arası
        Font legendFont = new Font(LABEL_FONT.getFontName(), Font.PLAIN, fontSize);
        g2d.setFont(legendFont);
        FontMetrics fm = g2d.getFontMetrics();

        // Değerleri yaz
        g2d.setColor(Color.BLACK);
        String[] values = {"1.0", "0.5", "0.0", "-0.5", "-1.0"};
        for (int i = 0; i < values.length; i++) {
            int textY = y + (barHeight * i) / (values.length - 1);
            int textWidth = fm.stringWidth(values[i]);
            g2d.drawString(values[i], x + barWidth + 5, textY + fm.getAscent() / 2);
        }
    }
}