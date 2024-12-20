package jazari.machine_learning.analysis.confusion_matrix;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.border.TitledBorder;

public class ConfusionMatrixPlot extends JFrame {

    private int[][] matrix;
    private String[] labels;
    private int numClasses;
    private JPanel plotPanel;
    private JPanel metricsPanel;

    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color GRID_COLOR = new Color(220, 220, 220);

    public ConfusionMatrixPlot(int[][] matrix, String[] labels) {
        this.matrix = matrix;
        this.labels = labels;
        this.numClasses = matrix.length;
        initializeFrame();
    }

    private Font getTitleFont(int panelSize) {
        return new Font("DejaVu Sans", Font.BOLD, panelSize / 30);
    }

    private Font getAxisLabelFont(int panelSize) {
        return new Font("DejaVu Sans", Font.BOLD, panelSize / 40);
    }

    private Font getLabelFont(int panelSize) {
        return new Font("DejaVu Sans", Font.PLAIN, panelSize / 45);
    }

    private Font getValueFont(int panelSize) {
        return new Font("DejaVu Sans", Font.PLAIN, panelSize / 45);
    }

    private void initializeFrame() {
        setTitle("Confusion Matrix");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        plotPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawConfusionMatrix((Graphics2D) g);
            }
        };
        plotPanel.setBackground(BACKGROUND_COLOR);

        // Metrikler paneli
        metricsPanel = createMetricsPanel();

        setupPopupMenu();

        // Layout düzenleme
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Plot paneli için minimum boyut - arttırıldı
        plotPanel.setPreferredSize(new Dimension(800, 700));
        mainPanel.add(plotPanel, BorderLayout.CENTER);
        mainPanel.add(metricsPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Pencere boyutu - arttırıldı
        setSize(800, 900);  // 600x700'den 800x900'e çıkarıldı
        setMinimumSize(new Dimension(600, 700)); // Minimum boyut eklendi
        setLocationRelativeTo(null);

        // Pencere yeniden boyutlandırıldığında kare oranını koru
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                setSize(width, width + 100); // Metrikler için ekstra alan
                updateMetricsFontSizes();
            }
        });
    }

    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel() {
            @Override
            public void doLayout() {
                super.doLayout();
                // Panel boyutu değiştiğinde font boyutlarını güncelle
                updateMetricsFontSizes();
            }
        };
        panel.setBackground(BACKGROUND_COLOR);

        // Border title font boyutu da dinamik olacak
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Performance Metrics",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                getMetricsTitleFont()
        ));

        panel.setLayout(new GridLayout(3, 2, 10, 5));
        panel.setPreferredSize(new Dimension(600, 100));

        // Metrikleri hesapla ve göster
        double accuracy = calculateAccuracy();
        Map<String, Double> metrics = calculateClassMetrics();

        // Metrik etiketlerini saklamak için map kullanıyoruz
        metricsLabels = new HashMap<>();
        metricsValues = new HashMap<>();

        addMetricRow(panel, "Accuracy", String.format("%.2f%%", accuracy * 100));
        addMetricRow(panel, "Precision", String.format("%.2f%%", metrics.get("precision") * 100));
        addMetricRow(panel, "Recall", String.format("%.2f%%", metrics.get("recall") * 100));
        addMetricRow(panel, "F1 Score", String.format("%.2f%%", metrics.get("f1") * 100));
        addMetricRow(panel, "Weighted F1", String.format("%.2f%%", metrics.get("weightedF1") * 100));

        return panel;
    }

    // Metrik label ve value referanslarını tutmak için
    private Map<String, JLabel> metricsLabels;
    private Map<String, JLabel> metricsValues;

    private void addMetricRow(JPanel panel, String name, String value) {
        JLabel nameLabel = new JLabel(name + ":", SwingConstants.RIGHT);
        JLabel valueLabel = new JLabel(" " + value, SwingConstants.LEFT);

        // Referansları sakla
        metricsLabels.put(name, nameLabel);
        metricsValues.put(name, valueLabel);

        panel.add(nameLabel);
        panel.add(valueLabel);
    }

    private Font getMetricsTitleFont() {
        int panelWidth = getWidth();
        return new Font("DejaVu Sans", Font.BOLD, panelWidth / 40);
    }

    private Font getMetricsLabelFont() {
        int panelWidth = getWidth();
        return new Font("DejaVu Sans", Font.BOLD, panelWidth / 45);
    }

    private Font getMetricsValueFont() {
        int panelWidth = getWidth();
        return new Font("DejaVu Sans", Font.PLAIN, panelWidth / 45);
    }

    private void updateMetricsFontSizes() {
        if (metricsLabels == null || metricsValues == null) {
            return;
        }

        // Border title font güncelleme
        TitledBorder border = (TitledBorder) metricsPanel.getBorder();
        border.setTitleFont(getMetricsTitleFont());

        // Metrik etiketleri ve değerleri için font güncelleme
        Font labelFont = getMetricsLabelFont();
        Font valueFont = getMetricsValueFont();

        for (JLabel label : metricsLabels.values()) {
            label.setFont(labelFont);
        }

        for (JLabel value : metricsValues.values()) {
            value.setFont(valueFont);
        }

        metricsPanel.repaint();
    }

    private Map<String, Double> calculateClassMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        int numClasses = matrix.length;

        double[] precision = new double[numClasses];
        double[] recall = new double[numClasses];
        double[] f1 = new double[numClasses];
        int[] classTotals = new int[numClasses];

        // Her sınıf için metrik hesaplama
        for (int i = 0; i < numClasses; i++) {
            int truePositive = matrix[i][i];
            int falsePositive = 0;
            int falseNegative = 0;

            for (int j = 0; j < numClasses; j++) {
                if (i != j) {
                    falsePositive += matrix[j][i];
                    falseNegative += matrix[i][j];
                }
                classTotals[i] += matrix[i][j];
            }

            precision[i] = truePositive + falsePositive > 0
                    ? (double) truePositive / (truePositive + falsePositive) : 0.0;

            recall[i] = truePositive + falseNegative > 0
                    ? (double) truePositive / (truePositive + falseNegative) : 0.0;

            f1[i] = precision[i] + recall[i] > 0
                    ? 2 * (precision[i] * recall[i]) / (precision[i] + recall[i]) : 0.0;
        }

        // Ortalamalar
        metrics.put("precision", Arrays.stream(precision).average().orElse(0.0));
        metrics.put("recall", Arrays.stream(recall).average().orElse(0.0));
        metrics.put("f1", Arrays.stream(f1).average().orElse(0.0));

        // Weighted F1-score
        double totalSamples = Arrays.stream(classTotals).sum();
        double weightedF1 = 0.0;
        for (int i = 0; i < numClasses; i++) {
            weightedF1 += f1[i] * classTotals[i] / totalSamples;
        }
        metrics.put("weightedF1", weightedF1);

        return metrics;
    }

    private void addMetricLabel(JPanel panel, String name, String value) {
        JLabel nameLabel = new JLabel(name + ":", SwingConstants.RIGHT);
        nameLabel.setFont(new Font("DejaVu Sans", Font.BOLD, 12));

        JLabel valueLabel = new JLabel(" " + value, SwingConstants.LEFT);
        valueLabel.setFont(new Font("DejaVu Sans", Font.PLAIN, 12));

        panel.add(nameLabel);
        panel.add(valueLabel);
    }

    private void drawConfusionMatrix(Graphics2D g2d) {
        setupGraphics(g2d);

        int width = plotPanel.getWidth();
        int height = plotPanel.getHeight();
        int panelSize = Math.min(width, height);

        int horizontalMargin = width / 8;
        int verticalMargin = height / 8;
        int matrixSize = Math.min(width - 2 * horizontalMargin, height - 2 * verticalMargin);
        int cellSize = matrixSize / numClasses;

        int startX = (width - matrixSize) / 2;
        int startY = verticalMargin + 40;

        g2d.setFont(getTitleFont(panelSize));
        drawTitle(g2d, width, startY - 60);

        drawColorBar(g2d, startX + matrixSize + 20, startY, 20, matrixSize, panelSize);
        drawMatrix(g2d, startX, startY, cellSize, panelSize);
        drawLabels(g2d, startX, startY, cellSize, matrixSize, panelSize);
    }

    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private void drawTitle(Graphics2D g2d, int width, int y) {
        String title = "Confusion Matrix";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        g2d.drawString(title, (width - fm.stringWidth(title)) / 2, y);
    }

    private void drawMatrix(Graphics2D g2d, int startX, int startY, int cellSize, int panelSize) {
        int maxValue = 0;
        int totalSum = 0;
        for (int[] row : matrix) {
            for (int val : row) {
                maxValue = Math.max(maxValue, val);
                totalSum += val;
            }
        }

        Font valueFont = getValueFont(panelSize);
        Font percentFont = new Font(valueFont.getFontName(), valueFont.getStyle(), (int) (valueFont.getSize() * 0.9));

        for (int i = 0; i < numClasses; i++) {
            for (int j = 0; j < numClasses; j++) {
                int x = startX + j * cellSize;
                int y = startY + i * cellSize;

                float value = (float) matrix[i][j] / maxValue;
                Color cellColor = getColorForValue(value);
                g2d.setColor(cellColor);
                g2d.fillRect(x, y, cellSize, cellSize);

                g2d.setColor(GRID_COLOR);
                g2d.drawRect(x, y, cellSize, cellSize);

                double percentage = (matrix[i][j] * 100.0) / totalSum;

                // Değer
                g2d.setFont(valueFont);
                String valueText = String.valueOf(matrix[i][j]);
                FontMetrics fm = g2d.getFontMetrics();
                g2d.setColor(value > 0.5 ? Color.WHITE : Color.BLACK);
                g2d.drawString(valueText,
                        x + (cellSize - fm.stringWidth(valueText)) / 2,
                        y + (cellSize / 2) - fm.getHeight() / 3);

                // Yüzde
                g2d.setFont(percentFont);
                String percentText = String.format("%.1f%%", percentage);
                FontMetrics pfm = g2d.getFontMetrics();
                g2d.drawString(percentText,
                        x + (cellSize - pfm.stringWidth(percentText)) / 2,
                        y + (cellSize / 2) + pfm.getHeight() / 2);
            }
        }
    }

    private Color getColorForValue(float value) {
        float r = (float) (1 - 0.8 * Math.pow(value, 0.5));
        float g = (float) (1 - 0.5 * Math.pow(value, 0.7));
        float b = (float) (1 - 0.3 * Math.pow(value, 0.1));
        return new Color(r, g, b);
    }

    private double calculateAccuracy() {
        int correct = 0;
        int total = 0;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (i == j) {
                    correct += matrix[i][j];
                }
                total += matrix[i][j];
            }
        }

        return total > 0 ? (double) correct / total : 0.0;
    }

    private void drawLabels(Graphics2D g2d, int startX, int startY, int cellSize, int matrixSize, int panelSize) {
        g2d.setFont(getAxisLabelFont(panelSize));
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();

        // Prediction etiketi
        String predLabel = "Prediction";
        g2d.drawString(predLabel,
                startX + (matrixSize - fm.stringWidth(predLabel)) / 2,
                startY - 35);

        // Actual etiketi
        Graphics2D g2dRotated = (Graphics2D) g2d.create();
        g2dRotated.translate(startX - 60, startY + matrixSize / 2);
        g2dRotated.rotate(-Math.PI / 2);
        g2dRotated.drawString("Actual", -fm.stringWidth("Actual") / 2, 0);
        g2dRotated.dispose();

        // Sınıf etiketleri
        g2d.setFont(getLabelFont(panelSize));
        fm = g2d.getFontMetrics();

        for (int i = 0; i < numClasses; i++) {
            g2d.drawString(labels[i],
                    startX + i * cellSize + (cellSize - fm.stringWidth(labels[i])) / 2,
                    startY - 10);

            g2d.drawString(labels[i],
                    startX - fm.stringWidth(labels[i]) - 15,
                    startY + i * cellSize + cellSize / 2 + fm.getAscent() / 2);
        }
    }

    private void drawColorBar(Graphics2D g2d, int x, int y, int width, int height, int panelSize) {
        // Renk skalası çizimi
        for (int i = 0; i <= height; i++) {
            float value = 1.0f - (float) i / height;
            g2d.setColor(getColorForValue(value));
            g2d.fillRect(x, y + i, width, 1);
        }

        // Değer etiketleri
        g2d.setFont(getLabelFont(panelSize));
        g2d.setColor(Color.BLACK);
        FontMetrics fm = g2d.getFontMetrics();

        // Maximum değeri bul
        int maxValue = 0;
        for (int[] row : matrix) {
            for (int val : row) {
                maxValue = Math.max(maxValue, val);
            }
        }

        // Min ve max değerleri yaz
        g2d.drawString("0", x + width + 5, y + height + fm.getAscent() / 2);
        g2d.drawString(String.valueOf(maxValue), x + width + 5, y + fm.getAscent() / 2);
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
        // Test için örnek matrix ve etiketler
        int[][] matrix = {
            {8, 1, 1},
            {2, 10, 0},
            {0, 2, 8}
        };
        String[] labels = {"Cat", "Dog", "Horse"};

        SwingUtilities.invokeLater(() -> {
            ConfusionMatrixPlot plot = new ConfusionMatrixPlot(matrix, labels);
            plot.setVisible(true);
        });
    }
}
