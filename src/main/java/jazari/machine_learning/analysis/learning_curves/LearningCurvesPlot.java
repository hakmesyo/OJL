package jazari.machine_learning.analysis.learning_curves;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LearningCurvesPlot extends JFrame {

    private List<Double> trainLoss;
    private List<Double> valLoss;
    private List<Double> trainAccuracy;
    private List<Double> valAccuracy;
    private int epochs;
    private JPanel plotPanel;

    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color GRID_COLOR = new Color(220, 220, 220);
    private static final Color TRAIN_COLOR = new Color(31, 119, 180);  // Mavi
    private static final Color VAL_COLOR = new Color(255, 127, 14);    // Turuncu
    private static final int PADDING = 50;
    private static final int LEFT_PADDING = 100;   // Sol boşluğu artır
    private static final int RIGHT_PADDING = 30;  // Sağ tarafta legend için alan
    private static final int TOP_PADDING = 50;
    private static final int BOTTOM_PADDING = 70;
    private static final int PLOT_SPACING = 100;   // İki plot arası mesafeyi artır

    public LearningCurvesPlot(List<Double> trainLoss, List<Double> valLoss,
            List<Double> trainAccuracy, List<Double> valAccuracy) {
        this.trainLoss = trainLoss;
        this.valLoss = valLoss;
        this.trainAccuracy = trainAccuracy;
        this.valAccuracy = valAccuracy;
        this.epochs = trainLoss.size();

        initializeFrame();
    }

    private void initializeFrame() {
        setTitle("Learning Curves");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Arka plan rengini ayarla
        setBackground(Color.WHITE);

        plotPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(Color.WHITE);  // Panel arka planı
                drawPlot((Graphics2D) g);
            }
        };
        plotPanel.setBackground(Color.WHITE);  // Panel arka planı

        setupPopupMenu();

        setLayout(new BorderLayout());
        add(plotPanel, BorderLayout.CENTER);

        // Başlangıç boyutu ve minimum boyut
        setSize(1000, 800);  // Daha büyük başlangıç boyutu
        setMinimumSize(new Dimension(800, 600));  // Minimum boyut
        setLocationRelativeTo(null);  // Ekranın ortasında

        // Pencere boyutu değiştiğinde aspect ratio'yu koru
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                int height = (int) (width * 0.8);  // 4:5 aspect ratio
                setSize(width, height);
            }
        });
    }

    private void drawPlot(Graphics2D g2d) {
        setupGraphics(g2d);

        int width = plotPanel.getWidth();
        int height = plotPanel.getHeight();

        // Plot alanlarını hesapla
        int plotWidth = width - LEFT_PADDING - RIGHT_PADDING;
        int plotHeight = (height - TOP_PADDING - BOTTOM_PADDING - PLOT_SPACING) / 2;

        // Loss plot
        drawSubPlot(g2d, trainLoss, valLoss,
                LEFT_PADDING, TOP_PADDING,
                plotWidth, plotHeight,
                "Loss", findMinMax(trainLoss, valLoss));

        // Accuracy plot
        drawSubPlot(g2d, trainAccuracy, valAccuracy,
                LEFT_PADDING, TOP_PADDING + plotHeight + PLOT_SPACING,
                plotWidth, plotHeight,
                "Accuracy", findMinMax(trainAccuracy, valAccuracy));

        // Legend için önce boyutları hesapla
        g2d.setFont(getLegendFont());
        FontMetrics fm = g2d.getFontMetrics();
        int boxSize = 12;
        int spacing = 8;
        String trainLabel = "Train";
        String valLabel = "Validation";
        int legendWidth = Math.max(fm.stringWidth(trainLabel), fm.stringWidth(valLabel)) + boxSize + spacing * 4;

        // Legend'ın X pozisyonunu frame genişliğine göre hesapla
        // plotWidth + LEFT_PADDING, plot'un sağ kenarının X koordinatı
        // legendWidth kadar sola git ki legend frame içinde kalsın
        int legendX = LEFT_PADDING + plotWidth - legendWidth;

        drawLegend(g2d, legendX, TOP_PADDING + 30);
    }

    private void drawLegend(Graphics2D g2d, int x, int y) {
        // Legend arka planı
        g2d.setFont(getLegendFont());
        FontMetrics fm = g2d.getFontMetrics();

        int boxSize = 12;
        int spacing = 8;
        String trainLabel = "Train";
        String valLabel = "Validation";

        // Legend kutusu boyutlarını hesapla
        int legendWidth = Math.max(fm.stringWidth(trainLabel), fm.stringWidth(valLabel)) + boxSize + spacing * 2;
        int legendHeight = (fm.getHeight() + spacing) * 2 + spacing;

        // Legend kutusu arka planı
        g2d.setColor(new Color(255, 255, 255, 220));
        g2d.fillRect(x - spacing, y - spacing, legendWidth + spacing * 2, legendHeight);
        g2d.setColor(GRID_COLOR);
        g2d.drawRect(x - spacing, y - spacing, legendWidth + spacing * 2, legendHeight);

        // Train legend
        g2d.setColor(TRAIN_COLOR);
        g2d.fillRect(x, y, boxSize, boxSize);
        g2d.setColor(Color.BLACK);
        g2d.drawString(trainLabel, x + boxSize + spacing, y + boxSize);

        // Validation legend
        g2d.setColor(VAL_COLOR);
        g2d.fillRect(x, y + fm.getHeight() + spacing, boxSize, boxSize);
        g2d.setColor(Color.BLACK);
        g2d.drawString(valLabel, x + boxSize + spacing, y + fm.getHeight() + spacing + boxSize);
    }

    private void drawAxisValues(Graphics2D g2d, int x, int y, int width, int height, double[] minMax) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(getAxisFont());
        FontMetrics fm = g2d.getFontMetrics();

        // Y ekseni değerleri
        int numYTicks = 5;
        for (int i = 0; i <= numYTicks; i++) {
            double value = minMax[0] + (minMax[1] - minMax[0]) * i / numYTicks;
            String label = String.format("%.2f", value);
            int labelX = x - fm.stringWidth(label) - 15;  // Daha fazla boşluk
            int labelY = y + height - (height * i / numYTicks) + fm.getAscent() / 2;
            g2d.drawString(label, labelX, labelY);

            // Grid çizgileri
            g2d.setColor(GRID_COLOR);
            g2d.drawLine(x, labelY - fm.getAscent() / 2, x + width, labelY - fm.getAscent() / 2);
            g2d.setColor(Color.BLACK);
        }

        // X ekseni değerleri (epoch)
        int maxXTicks = width / 60;  // Daha az tick göster
        int step = Math.max(1, epochs / maxXTicks);

        for (int epoch = 0; epoch < epochs; epoch += step) {
            String label = String.valueOf(epoch);
            int labelX = x + (epoch * width / (epochs - 1)) - fm.stringWidth(label) / 2;
            int labelY = y + height + fm.getHeight() + 5;

            if (epoch == 0 || labelX > x + fm.stringWidth(String.valueOf(epoch - step)) + 25) {
                g2d.drawString(label, labelX, labelY);

                // Dikey grid çizgisi
                g2d.setColor(GRID_COLOR);
                g2d.drawLine(labelX + fm.stringWidth(label) / 2, y,
                        labelX + fm.stringWidth(label) / 2, y + height);
                g2d.setColor(Color.BLACK);
            }
        }

        // X ekseni label
        String xLabel = "Epoch";
        g2d.drawString(xLabel,
                x + (width - fm.stringWidth(xLabel)) / 2,
                y + height + fm.getHeight() * 2 + 5);
    }

    private void drawSubPlot(Graphics2D g2d, List<Double> trainData, List<Double> valData,
            int x, int y, int width, int height, String title, double[] minMax) {
        // Grid ve eksenler
        g2d.setColor(GRID_COLOR);
        g2d.drawRect(x, y, width, height);

        // Başlık
        g2d.setColor(Color.BLACK);
        g2d.setFont(getTitleFont());
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, x + (width - fm.stringWidth(title)) / 2, y - 10);

        // Eksen değerleri
        drawAxisValues(g2d, x, y, width, height, minMax);

        // Data çizgileri
        drawDataLine(g2d, trainData, x, y, width, height, minMax, TRAIN_COLOR);
        drawDataLine(g2d, valData, x, y, width, height, minMax, VAL_COLOR);
    }

    private void drawDataLine(Graphics2D g2d, List<Double> data, int x, int y,
            int width, int height, double[] minMax, Color color) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));

        int[] xPoints = new int[data.size()];
        int[] yPoints = new int[data.size()];

        for (int i = 0; i < data.size(); i++) {
            xPoints[i] = x + (i * width) / (epochs - 1);
            double normalizedY = (data.get(i) - minMax[0]) / (minMax[1] - minMax[0]);
            yPoints[i] = y + height - (int) (normalizedY * height);
        }

        for (int i = 0; i < xPoints.length - 1; i++) {
            g2d.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }
    }

    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private Font getTitleFont() {
        return new Font("DejaVu Sans", Font.BOLD, getWidth() / 40);
    }

    private Font getAxisFont() {
        return new Font("DejaVu Sans", Font.PLAIN, getWidth() / 50);
    }

    private Font getLegendFont() {
        return new Font("DejaVu Sans", Font.PLAIN, getWidth() / 45);
    }

    private double[] findMinMax(List<Double> data1, List<Double> data2) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (Double value : data1) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        for (Double value : data2) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }

        // Biraz margin ekle
        double range = max - min;
        min -= range * 0.05;
        max += range * 0.05;

        return new double[]{min, max};
    }

    private void setupPopupMenu() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem saveItem = new JMenuItem("Save as Image");
        saveItem.addActionListener(e -> saveImage());
        popup.add(saveItem);

        plotPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void saveImage() {
        try {
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            printAll(g2d);
            g2d.dispose();

            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
            fileChooser.setDialogTitle("Save Learning Curves");

            // Tüm desteklenen formatlar için filtre
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String name = f.getName().toLowerCase();
                    return name.endsWith(".png")
                            || name.endsWith(".jpg")
                            || name.endsWith(".jpeg")
                            || name.endsWith(".bmp")
                            || name.endsWith(".gif")
                            || name.endsWith(".tiff");
                }

                @Override
                public String getDescription() {
                    return "Image Files (*.png, *.jpg, *.jpeg, *.bmp, *.gif, *.tiff)";
                }
            });

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String name = file.getName().toLowerCase();
                String format = "png"; // varsayılan format

                // Dosya uzantısına göre formatı belirle
                if (name.endsWith(".png")) {
                    format = "png";
                } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                    format = "jpg";
                } else if (name.endsWith(".bmp")) {
                    format = "bmp";
                } else if (name.endsWith(".gif")) {
                    format = "gif";
                } else if (name.endsWith(".tiff")) {
                    format = "tiff";
                } else {
                    // Uzantı yoksa .png ekle
                    file = new File(file.getAbsolutePath() + ".png");
                }

                if (ImageIO.write(image, format, file)) {
                    System.out.println("Image saved to: " + file.getAbsolutePath());
                    JOptionPane.showMessageDialog(this,
                            "Image saved successfully to:\n" + file.getAbsolutePath(),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    throw new IOException("Could not save image - no appropriate writer found for " + format);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving image:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Test için örnek veri
        List<Double> trainLoss = Arrays.asList(0.5, 0.4, 0.3, 0.25, 0.2, 0.18, 0.15, 0.13, 0.12, 0.11);
        List<Double> valLoss = Arrays.asList(0.55, 0.45, 0.35, 0.3, 0.28, 0.27, 0.29, 0.31, 0.33, 0.35);
        List<Double> trainAcc = Arrays.asList(0.6, 0.7, 0.75, 0.8, 0.82, 0.85, 0.87, 0.89, 0.9, 0.91);
        List<Double> valAcc = Arrays.asList(0.55, 0.65, 0.7, 0.75, 0.77, 0.78, 0.77, 0.76, 0.75, 0.74);

        SwingUtilities.invokeLater(() -> {
            LearningCurvesPlot plot = new LearningCurvesPlot(trainLoss, valLoss, trainAcc, valAcc);
            plot.setVisible(true);
        });
    }

    /**
     * Öğrenme eğrilerini yeni verilerle günceller
     */
    public void updateCurves(List<Double> newTrainLoss, List<Double> newValLoss,
            List<Double> newTrainAccuracy, List<Double> newValAccuracy) {
        // Derin kopya oluştur
        this.trainLoss = new ArrayList<>(newTrainLoss);
        this.valLoss = new ArrayList<>(newValLoss);
        this.trainAccuracy = new ArrayList<>(newTrainAccuracy);
        this.valAccuracy = new ArrayList<>(newValAccuracy);
        this.epochs = trainLoss.size();

        // Panel'i yeniden çiz
        if (plotPanel != null) {
            plotPanel.repaint();
        }
    }
}
