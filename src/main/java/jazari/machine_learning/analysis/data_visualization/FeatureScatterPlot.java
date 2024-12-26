package jazari.machine_learning.analysis.data_visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.ArrayList;

public class FeatureScatterPlot extends JPanel {

    private List<Double> featureValues;
    private List<Double> outputValues;
    private String featureName;
    private static final int LEFT_PADDING = 90;  // Sol taraf için daha fazla padding
    private static final int RIGHT_PADDING = 20;  // Sağ taraf için normal padding
    private static final int TOP_PADDING = 70;    // Üst taraf için padding
    private static final int BOTTOM_PADDING = 70; // Alt taraf için padding
    private static final Font LABEL_FONT = new Font("DejaVu Sans", Font.BOLD, 14);
    private static final Font TITLE_FONT = new Font("DejaVu Sans", Font.BOLD, 16);
    private static final Font VALUE_FONT = new Font("DejaVu Sans", Font.PLAIN, 12);

    // ... NiceScale sınıfı ve diğer yardımcı metodlar aynı ...

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        setupGraphics(g2d);

        int width = getWidth() - (LEFT_PADDING + RIGHT_PADDING);
        int height = getHeight() - (TOP_PADDING + BOTTOM_PADDING);

        // Veri aralıklarını bul ve margin ekle
        double minFeature = findMin(featureValues);
        double maxFeature = findMax(featureValues);
        double minOutput = findMin(outputValues);
        double maxOutput = findMax(outputValues);

        drawGridAndAxes(g2d, width, height, minFeature, maxFeature, minOutput, maxOutput);
        drawPoints(g2d, width, height, minFeature, maxFeature, minOutput, maxOutput);
        drawTitle(g2d);
        
        double correlation = calculateCorrelation();
        drawCorrelation(g2d, correlation);
    }

    private void drawGridAndAxes(Graphics2D g2d, int width, int height,
            double minFeature, double maxFeature,
            double minOutput, double maxOutput) {
        
        NiceScale xScale = new NiceScale(minFeature, maxFeature);
        NiceScale yScale = new NiceScale(minOutput, maxOutput);

        // Grid çizgileri
        g2d.setColor(new Color(220, 220, 220));
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.setFont(VALUE_FONT);

        // X ekseni için nice ticks
        for (double x = xScale.getNiceMin(); x <= xScale.getNiceMax(); x += xScale.getTickSpacing()) {
            int xPos = LEFT_PADDING + (int) ((x - xScale.getNiceMin()) * width 
                    / (xScale.getNiceMax() - xScale.getNiceMin()));
            
            // Dikey grid çizgisi
            g2d.drawLine(xPos, getHeight() - BOTTOM_PADDING, xPos, TOP_PADDING);
            
            // X ekseni değerleri
            String label = String.format("%.2f", x);
            FontMetrics fm = g2d.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, xPos - labelWidth/2, getHeight() - BOTTOM_PADDING + 20);
            g2d.setColor(new Color(220, 220, 220));
        }

        // Y ekseni için nice ticks
        for (double y = yScale.getNiceMin(); y <= yScale.getNiceMax(); y += yScale.getTickSpacing()) {
            int yPos = getHeight() - BOTTOM_PADDING - (int) ((y - yScale.getNiceMin()) * height 
                    / (yScale.getNiceMax() - yScale.getNiceMin()));
            
            // Yatay grid çizgisi
            g2d.drawLine(LEFT_PADDING, yPos, LEFT_PADDING + width, yPos);
            
            // Y ekseni değerleri
            String label = String.format("%.2f", y);
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, LEFT_PADDING - 55, yPos + 5);
            g2d.setColor(new Color(220, 220, 220));
        }

        // Eksenler
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawLine(LEFT_PADDING, getHeight() - BOTTOM_PADDING, LEFT_PADDING + width, getHeight() - BOTTOM_PADDING);
        g2d.drawLine(LEFT_PADDING, getHeight() - BOTTOM_PADDING, LEFT_PADDING, TOP_PADDING);

        // X ekseni etiketi
        g2d.setFont(LABEL_FONT);
        String xLabel = featureName;
        FontMetrics fm = g2d.getFontMetrics();
        int labelWidth = fm.stringWidth(xLabel);
        g2d.drawString(xLabel, LEFT_PADDING + (width - labelWidth)/2, getHeight() - 10);

        // Y ekseni etiketi
        AffineTransform original = g2d.getTransform();
        g2d.rotate(-Math.PI / 2);
        g2d.drawString("Output", -getHeight()/2 - 30, 30);  // Y ekseni etiketi pozisyonu
        g2d.setTransform(original);
    }

    private void drawPoints(Graphics2D g2d, int width, int height,
            double minFeature, double maxFeature,
            double minOutput, double maxOutput) {
            
        NiceScale xScale = new NiceScale(minFeature, maxFeature);
        NiceScale yScale = new NiceScale(minOutput, maxOutput);
        
        g2d.setColor(new Color(31, 119, 180, 150));

        for (int i = 0; i < featureValues.size(); i++) {
            int x = LEFT_PADDING + (int) ((featureValues.get(i) - xScale.getNiceMin()) * width 
                    / (xScale.getNiceMax() - xScale.getNiceMin()));
            int y = getHeight() - BOTTOM_PADDING - (int) ((outputValues.get(i) - yScale.getNiceMin()) * height 
                    / (yScale.getNiceMax() - yScale.getNiceMin()));
            g2d.fillOval(x - 3, y - 3, 6, 6);
        }
    }

    private void drawTitle(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(TITLE_FONT);
        String title = featureName + " vs Output";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (getWidth() - titleWidth) / 2, TOP_PADDING / 2);
    }

    private void drawCorrelation(Graphics2D g2d, double correlation) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(VALUE_FONT);
        String corrText = String.format("Correlation: %.4f", correlation);
        g2d.drawString(corrText, LEFT_PADDING + 10, TOP_PADDING + 20);
    }
    
    private static class NiceScale {
        private double minPoint;
        private double maxPoint;
        private double maxTicks = 10;
        private double tickSpacing;
        private double range;
        private double niceMin;
        private double niceMax;

        public NiceScale(double min, double max) {
            this.minPoint = min;
            this.maxPoint = max;
            calculate();
        }

        private void calculate() {
            this.range = niceNum(maxPoint - minPoint, false);
            this.tickSpacing = niceNum(range / (maxTicks - 1), true);
            this.niceMin = Math.floor(minPoint / tickSpacing) * tickSpacing;
            this.niceMax = Math.ceil(maxPoint / tickSpacing) * tickSpacing;
        }

        private double niceNum(double range, boolean round) {
            double exponent = Math.floor(Math.log10(range));
            double fraction = range / Math.pow(10, exponent);
            double niceFraction;

            if (round) {
                if (fraction < 1.5) niceFraction = 1;
                else if (fraction < 3) niceFraction = 2;
                else if (fraction < 7) niceFraction = 5;
                else niceFraction = 10;
            } else {
                if (fraction <= 1) niceFraction = 1;
                else if (fraction <= 2) niceFraction = 2;
                else if (fraction <= 5) niceFraction = 5;
                else niceFraction = 10;
            }

            return niceFraction * Math.pow(10, exponent);
        }

        public double getTickSpacing() { return tickSpacing; }
        public double getNiceMin() { return niceMin; }
        public double getNiceMax() { return niceMax; }
    }

    public FeatureScatterPlot(List<Double> featureValues, List<Double> outputValues, String featureName) {
        this.featureValues = new ArrayList<>(featureValues);
        this.outputValues = new ArrayList<>(outputValues);
        this.featureName = featureName;
        setPreferredSize(new Dimension(400, 400));
        setBackground(Color.WHITE);
        setDoubleBuffered(true);
    }

    private void setupGraphics(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, getWidth(), getHeight());
    }


    private double calculateCorrelation() {
        double meanFeature = featureValues.stream().mapToDouble(d -> d).average().orElse(0.0);
        double meanOutput = outputValues.stream().mapToDouble(d -> d).average().orElse(0.0);

        double numerator = 0;
        double denomFeature = 0;
        double denomOutput = 0;

        for (int i = 0; i < featureValues.size(); i++) {
            double diffFeature = featureValues.get(i) - meanFeature;
            double diffOutput = outputValues.get(i) - meanOutput;

            numerator += diffFeature * diffOutput;
            denomFeature += diffFeature * diffFeature;
            denomOutput += diffOutput * diffOutput;
        }

        return numerator / (Math.sqrt(denomFeature) * Math.sqrt(denomOutput));
    }

    private double findMin(List<Double> values) {
        double min = values.stream().mapToDouble(d -> d).min().orElse(0.0);
        double range = values.stream().mapToDouble(d -> d).max().orElse(0.0) - min;
        return min - range * 0.05;  // %5 margin ekle
    }

    private double findMax(List<Double> values) {
        double max = values.stream().mapToDouble(d -> d).max().orElse(0.0);
        double range = max - values.stream().mapToDouble(d -> d).min().orElse(0.0);
        return max + range * 0.05;  // %5 margin ekle
    }
}