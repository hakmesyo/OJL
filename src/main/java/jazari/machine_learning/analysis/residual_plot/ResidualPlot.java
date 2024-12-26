package jazari.machine_learning.analysis.residual_plot;

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

public class ResidualPlot extends JFrame {

    private List<Double> actuals;
    private List<Double> residuals;
    private JPanel plotPanel;
    private Timer resizeTimer;

    private static final int PADDING = 70;
    private static final Font LABEL_FONT = new Font("DejaVu Sans", Font.BOLD, 14);
    private static final Font TICK_FONT = new Font("DejaVu Sans", Font.PLAIN, 11);
    private static final Font TITLE_FONT = new Font("DejaVu Sans", Font.BOLD, 16);

    public ResidualPlot(List<Double> actuals, List<Double> residuals) {
        this.actuals = new ArrayList<>(actuals);
        this.residuals = new ArrayList<>(residuals);
        initializeFrame();
    }

    private void initializeFrame() {
        setTitle("Residual Plot");  // veya "Residual Plot" için uygun başlık
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
        double minResidual = Collections.min(residuals);
        double maxResidual = Collections.max(residuals);

        // Margin ekle
        double rangeActual = maxActual - minActual;
        double rangeResidual = maxResidual - minResidual;
        minActual -= rangeActual * 0.05;
        maxActual += rangeActual * 0.05;
        minResidual -= rangeResidual * 0.05;
        maxResidual += rangeResidual * 0.05;

        // Kalıntı aralığını simetrik yap (sıfır merkezli)
        double maxAbsResidual = Math.max(Math.abs(minResidual), Math.abs(maxResidual));
        minResidual = -maxAbsResidual;
        maxResidual = maxAbsResidual;

        drawGridAndAxes(g2d, width, height, minActual, maxActual, minResidual, maxResidual);
        drawZeroLine(g2d, width, height, minResidual, maxResidual);
        drawDataPoints(g2d, width, height, minActual, maxActual, minResidual, maxResidual);
        drawTitle(g2d);
        drawStatistics(g2d);
    }

    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, getWidth(), getHeight());
    }

    private void drawGridAndAxes(Graphics2D g2d, int width, int height,
            double minActual, double maxActual,
            double minResidual, double maxResidual) {
        // Grid çizgileri
        g2d.setColor(new Color(220, 220, 220));
        g2d.setStroke(new BasicStroke(1.0f));

        DecimalFormat df = new DecimalFormat("0.00");
        g2d.setFont(TICK_FONT);

        // Yatay grid
        for (int i = 0; i <= 10; i++) {
            int y = getHeight() - PADDING - (height * i) / 10;
            g2d.drawLine(PADDING, y, PADDING + width, y);

            double residualValue = minResidual + (maxResidual - minResidual) * i / 10;
            g2d.setColor(Color.BLACK);
            g2d.drawString(df.format(residualValue), PADDING - 60, y + 5);
            g2d.setColor(new Color(220, 220, 220));
        }

        // Dikey grid
        for (int i = 0; i <= 10; i++) {
            int x = PADDING + (width * i) / 10;
            g2d.drawLine(x, getHeight() - PADDING, x, PADDING);

            double actualValue = minActual + (maxActual - minActual) * i / 10;
            g2d.setColor(Color.BLACK);
            g2d.drawString(df.format(actualValue), x - 25, getHeight() - PADDING + 20);
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
        g2d.drawString("Residuals", -getHeight() / 2 - 30, PADDING / 2);
        g2d.setTransform(original);
    }

    private void drawZeroLine(Graphics2D g2d, int width, int height,
            double minResidual, double maxResidual) {
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{9}, 0));

        int zeroY = getHeight() - PADDING
                - (int) ((0 - minResidual) * height / (maxResidual - minResidual));
        g2d.drawLine(PADDING, zeroY, PADDING + width, zeroY);
    }

    private void drawDataPoints(Graphics2D g2d, int width, int height,
            double minActual, double maxActual,
            double minResidual, double maxResidual) {
        g2d.setColor(new Color(214, 39, 40, 150));  // Kırmızımsı

        for (int i = 0; i < actuals.size(); i++) {
            int x = PADDING + (int) ((actuals.get(i) - minActual) * width / (maxActual - minActual));
            int y = getHeight() - PADDING
                    - (int) ((residuals.get(i) - minResidual) * height / (maxResidual - minResidual));
            g2d.fillOval(x - 3, y - 3, 6, 6);
        }
    }

    private void drawTitle(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(TITLE_FONT);
        String title = "Residual Plot";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, PADDING / 2);
    }

    private void drawStatistics(Graphics2D g2d) {
        // İstatistikleri hesapla
        double meanResidual = calculateMean(residuals);
        double stdResidual = calculateStd(residuals);
        double maxAbsResidual = calculateMaxAbsResidual();

        // İstatistikleri göster
        g2d.setColor(Color.BLACK);
        g2d.setFont(TICK_FONT);
        int y = PADDING + 20;
        g2d.drawString(String.format("Mean Residual: %.4f", meanResidual), PADDING + 10, y);
        g2d.drawString(String.format("Std Residual: %.4f", stdResidual), PADDING + 10, y + 20);
        g2d.drawString(String.format("Max Abs Residual: %.4f", maxAbsResidual), PADDING + 10, y + 40);
    }

    private double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(d -> d).average().orElse(0);
    }

    private double calculateStd(List<Double> values) {
        double mean = calculateMean(values);
        return Math.sqrt(values.stream()
                .mapToDouble(d -> Math.pow(d - mean, 2))
                .average()
                .orElse(0));
    }

    private double calculateMaxAbsResidual() {
        return residuals.stream()
                .mapToDouble(Math::abs)
                .max()
                .orElse(0);
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
        fileChooser.setSelectedFile(new File("residual_plot.png"));

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
