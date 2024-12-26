package jazari.machine_learning.analysis.roc_curve;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;

public class ROCCurvePlot extends JFrame {

    private List<List<Double>> probsPerClass;
    private List<List<Integer>> actualsPerClass;
    private String[] classNames;
    private List<Double> aucScores;
    private JPanel rocPanel;

    private static final Color[] COLORS = {
        new Color(31, 119, 180), // Mavi
        new Color(255, 127, 14), // Turuncu
        new Color(44, 160, 44), // Yeşil
        new Color(214, 39, 40), // Kırmızı
        new Color(148, 103, 189), // Mor
        new Color(140, 86, 75), // Kahverengi
        new Color(227, 119, 194), // Pembe
        new Color(127, 127, 127), // Gri
        new Color(188, 189, 34), // Sarı-yeşil
        new Color(23, 190, 207) // Turkuaz
    };

    public ROCCurvePlot(List<List<Double>> probsPerClass, List<List<Integer>> actualsPerClass, String[] classNames) {
        this.probsPerClass = probsPerClass;
        this.actualsPerClass = actualsPerClass;
        this.classNames = classNames;
        this.aucScores = new ArrayList<>();

        // Her sınıf için AUC hesapla
        for (int i = 0; i < probsPerClass.size(); i++) {
            double auc = calculateAUC(probsPerClass.get(i), actualsPerClass.get(i));
            aucScores.add(auc);
        }

        initializeFrame();
    }

    private void initializeFrame() {
        setTitle("ROC Curves");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        rocPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawROCCurves((Graphics2D) g);
            }
        };
        rocPanel.setBackground(Color.WHITE);

        setupPopupMenu();

        setLayout(new BorderLayout());
        add(rocPanel, BorderLayout.CENTER);

        // Pencere boyutu ve pozisyonu
        setSize(800, 800);
        setMinimumSize(new Dimension(600, 600));
        setLocationRelativeTo(null);

        // Aspect ratio'yu koru
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int width = getWidth();
                int height = getWidth();  // Kare şeklinde tut
                setSize(width, height);
            }
        });
    }

    private void drawROCCurves(Graphics2D g2d) {
        setupGraphics(g2d);

        // Plot alanı hesapla
        int padding = 70;  // Kenar boşluklarını artır
        int width = getWidth() - 2 * padding;
        int height = getHeight() - 2 * padding;

        // Grid ve eksenler
        drawGridAndAxes(g2d, padding, width, height);

        // ROC eğrileri
        drawCurves(g2d, padding, width, height);

        // Lejant
        drawLegend(g2d, padding, width);

        // Başlık
        drawTitle(g2d, width);
    }

    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private void drawGridAndAxes(Graphics2D g2d, int padding, int width, int height) {
        // Grid çizgileri
        g2d.setColor(new Color(220, 220, 220));
        g2d.setStroke(new BasicStroke(1.0f));

        DecimalFormat df = new DecimalFormat("0.0");
        Font tickFont = new Font("DejaVu Sans", Font.PLAIN, 11);
        g2d.setFont(tickFont);

        for (int i = 0; i <= 10; i++) {
            int x = padding + (width * i) / 10;
            int y = getHeight() - padding - (height * i) / 10;

            // Grid çizgileri
            g2d.drawLine(padding, y, padding + width, y);
            g2d.drawLine(x, getHeight() - padding, x, padding);

            // Eksen değerleri
            g2d.setColor(Color.BLACK);
            g2d.drawString(df.format(i / 10.0), x - 12, getHeight() - padding + 20);
            g2d.drawString(df.format(i / 10.0), padding - 35, y + 5);
            g2d.setColor(new Color(220, 220, 220));
        }

        // Ana eksenler
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(padding, getHeight() - padding, padding + width, getHeight() - padding);  // x-axis
        g2d.drawLine(padding, getHeight() - padding, padding, padding);  // y-axis

        // Eksen etiketleri için font ve renk ayarla
        g2d.setColor(Color.BLACK);
        Font labelFont = new Font("DejaVu Sans", Font.BOLD, 14);
        g2d.setFont(labelFont);
        FontMetrics fm = g2d.getFontMetrics();

        // X ekseni etiketi
        String xLabel = "False Positive Rate";
        int xLabelWidth = fm.stringWidth(xLabel);
        g2d.drawString(xLabel,
                padding + (width - xLabelWidth) / 2, // Ortalanmış X pozisyonu
                getHeight() - padding / 2);             // Y pozisyonu biraz yukarıda

        // Y ekseni etiketi
        // Mevcut Y ekseni çizimi kodunu güncelle
        AffineTransform original = g2d.getTransform();
        g2d.rotate(-Math.PI / 2);
        String yLabel = "True Positive Rate";
        int yLabelWidth = fm.stringWidth(yLabel);
        g2d.drawString(yLabel,
                -(padding + (height + yLabelWidth) / 2), // Ortalanmış pozisyon
                padding / 2);                               // Sol kenara yakın
        g2d.setTransform(original);
    }

    private void drawCurves(Graphics2D g2d, int padding, int width, int height) {
        for (int classIdx = 0; classIdx < probsPerClass.size(); classIdx++) {
            List<Double> probs = probsPerClass.get(classIdx);
            List<Integer> actuals = actualsPerClass.get(classIdx);

            // ROC noktalarını hesapla
            List<Point2D.Double> rocPoints = calculateROCPoints(probs, actuals);

            // Eğriyi çiz
            g2d.setColor(COLORS[classIdx % COLORS.length]);
            g2d.setStroke(new BasicStroke(2.5f));

            for (int i = 0; i < rocPoints.size() - 1; i++) {
                int x1 = padding + (int) (rocPoints.get(i).x * width);
                int y1 = getHeight() - padding - (int) (rocPoints.get(i).y * height);
                int x2 = padding + (int) (rocPoints.get(i + 1).x * width);
                int y2 = getHeight() - padding - (int) (rocPoints.get(i + 1).y * height);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }

    private void drawLegend(Graphics2D g2d, int padding, int width) {
        int legendX = padding + width - 200;  // Sağ tarafta
        int legendY = padding + 20;
        int boxWidth = 20;
        int boxHeight = 12;
        int spacing = 5;

        // Lejant arka planı
        g2d.setColor(new Color(255, 255, 255, 220));
        int legendWidth = 180;
        int legendHeight = (boxHeight + spacing) * probsPerClass.size() + 10;
        g2d.fillRect(legendX - 5, legendY - 5, legendWidth, legendHeight);
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawRect(legendX - 5, legendY - 5, legendWidth, legendHeight);

        Font legendFont = new Font("DejaVu Sans", Font.PLAIN, 12);
        g2d.setFont(legendFont);
        DecimalFormat df = new DecimalFormat("0.000");

        for (int i = 0; i < classNames.length; i++) {
            g2d.setColor(COLORS[i % COLORS.length]);
            g2d.fillRect(legendX, legendY + i * (boxHeight + spacing), boxWidth, boxHeight);

            g2d.setColor(Color.BLACK);
            String label = String.format("%s (AUC: %s)",
                    classNames[i],
                    df.format(aucScores.get(i)));
            g2d.drawString(label, legendX + boxWidth + 5,
                    legendY + i * (boxHeight + spacing) + boxHeight - 2);
        }
    }

    private void drawTitle(Graphics2D g2d, int width) {
        g2d.setColor(Color.BLACK);
        Font titleFont = new Font("DejaVu Sans", Font.BOLD, 16);
        g2d.setFont(titleFont);
        String title = "Receiver Operating Characteristic (ROC) Curves";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, 30);
    }

    private List<Point2D.Double> calculateROCPoints(List<Double> probs, List<Integer> actuals) {
        List<Point2D.Double> points = new ArrayList<>();
        points.add(new Point2D.Double(0, 0));

        // Olasılıkları sırala
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < probs.size(); i++) {
            indices.add(i);
        }
        indices.sort((a, b) -> Double.compare(probs.get(b), probs.get(a)));

        int totalPositives = actuals.stream().mapToInt(Integer::intValue).sum();
        int totalNegatives = actuals.size() - totalPositives;

        if (totalPositives == 0 || totalNegatives == 0) {
            return points;
        }

        double tp = 0, fp = 0;
        double prevProb = 1.0;

        for (int idx : indices) {
            double prob = probs.get(idx);
            if (prob != prevProb) {
                points.add(new Point2D.Double(fp / totalNegatives, tp / totalPositives));
                prevProb = prob;
            }
            if (actuals.get(idx) == 1) {
                tp++;
            } else {
                fp++;
            }
        }

        points.add(new Point2D.Double(fp / totalNegatives, tp / totalPositives));
        points.add(new Point2D.Double(1, 1));

        return points;
    }

    private double calculateAUC(List<Double> probs, List<Integer> actuals) {
        List<Point2D.Double> points = calculateROCPoints(probs, actuals);
        double auc = 0.0;

        for (int i = 1; i < points.size(); i++) {
            double width = points.get(i).x - points.get(i - 1).x;
            double height = (points.get(i).y + points.get(i - 1).y) / 2;
            auc += width * height;
        }

        return auc;
    }

    private void setupPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem saveItem = new JMenuItem("Save as PNG");
        saveItem.addActionListener(e -> saveImage());
        popup.add(saveItem);

        rocPanel.setComponentPopupMenu(popup);
    }

    private void saveImage() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        paint(g2d);
        g2d.dispose();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("roc_curves.png"));

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
