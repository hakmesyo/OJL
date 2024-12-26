package jazari.machine_learning.analysis.prediction_plot;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.text.DecimalFormat;

public class PredictionPlot extends JFrame {

    private List<Double> actuals;
    private List<Double> predictions;
    private JPanel plotPanel;
    private Timer resizeTimer;

    private static final int PADDING = 70;
    private static final Font LABEL_FONT = new Font("DejaVu Sans", Font.BOLD, 14);
    private static final Font TICK_FONT = new Font("DejaVu Sans", Font.PLAIN, 11);
    private static final Font TITLE_FONT = new Font("DejaVu Sans", Font.BOLD, 16);

    public PredictionPlot(List<Double> actuals, List<Double> predictions) {
        this.actuals = new ArrayList<>(actuals);      // Derin kopya
        this.predictions = new ArrayList<>(predictions);
        initializeFrame();
    }

    private void initializeFrame() {
        setTitle("Prediction vs Actual Plot");  // veya "Residual Plot" için uygun başlık
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        plotPanel = new JPanel() {
            {
                setDoubleBuffered(true);
                setPreferredSize(new Dimension(800, 800));  // Başlangıç boyutu
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, getWidth(), getHeight());  // Arka planı temizle
                drawPlot(g2d);
                this.revalidate();  // Panel'i yeniden doğrula
            }
        };
        plotPanel.setBackground(Color.WHITE);

        // Frame'e direkt ekle
        setLayout(new BorderLayout());
        add(plotPanel, BorderLayout.CENTER);

        // Frame boyutunu ayarla
        pack();  // Panel boyutuna göre frame'i ayarla
        setLocationRelativeTo(null);
        setVisible(true);  // Görünür yap

        setupPopupMenu();

        // Resize listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (resizeTimer != null && resizeTimer.isRunning()) {
                    resizeTimer.restart();
                } else {
                    resizeTimer = new Timer(100, evt -> {
                        int size = Math.min(getWidth(), getHeight());
                        setSize(size, size);
                        plotPanel.repaint();
                        ((Timer) evt.getSource()).stop();
                    });
                    resizeTimer.setRepeats(false);
                    resizeTimer.start();
                }
            }
        });
    }

    private void drawPlot(Graphics2D g2d) {
        setupGraphics(g2d);

        int width = getWidth() - 2 * PADDING;
        int height = getHeight() - 2 * PADDING;

        // Veri aralıklarını bul
        double minActual = Collections.min(actuals);
        double maxActual = Collections.max(actuals);
        double minPred = Collections.min(predictions);
        double maxPred = Collections.max(predictions);
        double minValue = Math.min(minActual, minPred);
        double maxValue = Math.max(maxActual, maxPred);

        // Margin ekle
        double range = maxValue - minValue;
        minValue -= range * 0.05;
        maxValue += range * 0.05;

        drawGridAndAxes(g2d, width, height, minValue, maxValue);
        drawPerfectLine(g2d, width, height, minValue, maxValue);
        drawDataPoints(g2d, width, height, minValue, maxValue);
        drawTitle(g2d);
        drawMetrics(g2d);
    }

    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, getWidth(), getHeight());
    }

    private void drawGridAndAxes(Graphics2D g2d, int width, int height, double minValue, double maxValue) {
        // Grid çizgileri
        g2d.setColor(new Color(220, 220, 220));
        g2d.setStroke(new BasicStroke(1.0f));

        DecimalFormat df = new DecimalFormat("0.00");
        g2d.setFont(TICK_FONT);

        for (int i = 0; i <= 10; i++) {
            int x = PADDING + (width * i) / 10;
            int y = getHeight() - PADDING - (height * i) / 10;

            // Grid çizgileri
            g2d.drawLine(PADDING, y, PADDING + width, y);
            g2d.drawLine(x, getHeight() - PADDING, x, PADDING);

            // Grid değerleri
            g2d.setColor(Color.BLACK);
            double value = minValue + (maxValue - minValue) * i / 10;
            g2d.drawString(df.format(value), x - 25, getHeight() - PADDING + 20);
            g2d.drawString(df.format(value), PADDING - 60, y + 5);
            g2d.setColor(new Color(220, 220, 220));
        }

        // Ana eksenler
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(PADDING, getHeight() - PADDING, PADDING + width, getHeight() - PADDING);
        g2d.drawLine(PADDING, getHeight() - PADDING, PADDING, PADDING);

        // Eksen etiketleri
        g2d.setFont(LABEL_FONT);
        g2d.drawString("Actual Values", PADDING + width / 2 - 40, getHeight() - PADDING / 3);

        AffineTransform original = g2d.getTransform();
        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Predicted Values", -getHeight() / 2 - 50, PADDING / 2);
        g2d.setTransform(original);
    }

    private void drawPerfectLine(Graphics2D g2d, int width, int height, double minValue, double maxValue) {
        // y=x çizgisi
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{9}, 0));
        g2d.drawLine(PADDING, getHeight() - PADDING, PADDING + width, PADDING);
    }

    private void drawDataPoints(Graphics2D g2d, int width, int height, double minValue, double maxValue) {
        g2d.setColor(new Color(31, 119, 180, 150));
        double range = maxValue - minValue;

        for (int i = 0; i < actuals.size(); i++) {
            int x = PADDING + (int) ((actuals.get(i) - minValue) * width / range);
            int y = getHeight() - PADDING - (int) ((predictions.get(i) - minValue) * height / range);
            g2d.fillOval(x - 3, y - 3, 6, 6);
        }
    }

    private void drawTitle(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(TITLE_FONT);
        String title = "Prediction vs Actual Plot";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, PADDING / 2);
    }

    private void drawMetrics(Graphics2D g2d) {
        // Metrikleri hesapla
        double mse = calculateMSE();
        double mae = calculateMAE();
        double r2 = calculateR2();

        // Metrikleri göster
        g2d.setColor(Color.BLACK);
        g2d.setFont(TICK_FONT);
        int y = PADDING + 20;
        g2d.drawString(String.format("MSE: %.4f", mse), PADDING + 10, y);
        g2d.drawString(String.format("MAE: %.4f", mae), PADDING + 10, y + 20);
        g2d.drawString(String.format("R²: %.4f", r2), PADDING + 10, y + 40);
    }

    private double calculateMSE() {
        double sum = 0;
        for (int i = 0; i < actuals.size(); i++) {
            double diff = predictions.get(i) - actuals.get(i);
            sum += diff * diff;
        }
        return sum / actuals.size();
    }

    private double calculateMAE() {
        double sum = 0;
        for (int i = 0; i < actuals.size(); i++) {
            sum += Math.abs(predictions.get(i) - actuals.get(i));
        }
        return sum / actuals.size();
    }

    private double calculateR2() {
        double meanActual = actuals.stream().mapToDouble(d -> d).average().orElse(0);
        double totalSS = actuals.stream()
                .mapToDouble(d -> Math.pow(d - meanActual, 2))
                .sum();
        double residualSS = 0;
        for (int i = 0; i < actuals.size(); i++) {
            residualSS += Math.pow(predictions.get(i) - actuals.get(i), 2);
        }
        return 1 - (residualSS / totalSS);
    }

    private void setupPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem saveItem = new JMenuItem("Save as PNG");
        saveItem.addActionListener(e -> saveImage());
        popup.add(saveItem);
        plotPanel.setComponentPopupMenu(popup);
    }

    private void saveImage() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        paint(g2d);
        g2d.dispose();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("prediction_plot.png"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".png")) {
                    file = new File(file.getAbsolutePath() + ".png");
                }
                ImageIO.write(image, "PNG", file);
                JOptionPane.showMessageDialog(this,
                        "Image saved successfully to:\n" + file.getAbsolutePath(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error saving image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
