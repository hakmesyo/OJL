/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.data_analytics.core;

/**
 *
 * @author cezerilab
 */
import java.text.DecimalFormat;
import java.awt.*;
import java.awt.geom.AffineTransform;

public class Axis_eski {

    private float min;
    private float max;
    private String label;

    public Axis_eski(float min, float max, String label) {
        this.min = min;
        this.max = max;
        this.label = label;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public String getLabel() {
        return label;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DecimalFormat createDynamicFormatter(float step) {
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

    public void drawAxis(Graphics2D g2, AbstractPlot plot) {
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));
        // Draw X axis
        g2.drawLine(plot.PADDING, plot.getHeight() - plot.PADDING,
                plot.getWidth() - plot.LEGEND_WIDTH - 10, plot.getHeight() - plot.PADDING);

        // Draw Y axis
        g2.drawLine(plot.PADDING, plot.PADDING, plot.PADDING, plot.getHeight() - plot.PADDING);
    }

    public void drawAxisLabels(Graphics2D g2, AbstractPlot plot, float stepX, float stepY) {
        g2.setFont(plot.AXIS_FONT);
        FontMetrics metrics = g2.getFontMetrics();

        // Görünür alan sınırlarını hesapla
        float visibleMinX = plot.screenToWorldX(plot.PADDING);
        float visibleMaxX = plot.screenToWorldX(plot.getWidth() - plot.LEGEND_WIDTH - 10);
        float visibleMinY = plot.screenToWorldY(plot.getHeight() - plot.PADDING);
        float visibleMaxY = plot.screenToWorldY(plot.PADDING);

        // İlk grid çizgisinin konumunu hesapla
        float xStart = (float) Math.floor(visibleMinX / stepX) * stepX;
        float yStart = (float) Math.floor(visibleMinY / stepY) * stepY;

        // Etiketleri formatla ve çiz
        DecimalFormat labelFormat = createDynamicFormatter(stepX);

        // X ekseni etiketleri
        for (float x = xStart; x <= visibleMaxX + stepX / 2; x += stepX) {
            int screenX = plot.worldToScreenX(x);
            String label = labelFormat.format(x);
            g2.setColor(plot.getxAxisStyle().getTextColor());
            g2.setFont(plot.getxAxisStyle().getFont());
            g2.drawString(label, screenX - metrics.stringWidth(label) / 2,
                    plot.getHeight() - plot.PADDING + metrics.getHeight() + 5);
        }

        // Y ekseni etiketleri
        labelFormat = createDynamicFormatter(stepY);
        for (float y = yStart; y <= visibleMaxY + stepY / 2; y += stepY) {
            int screenY = plot.worldToScreenY(y);
            String label = labelFormat.format(y);
            g2.setColor(plot.getyAxisStyle().getTextColor());
            g2.setFont(plot.getyAxisStyle().getFont());
            g2.drawString(label, plot.PADDING - metrics.stringWidth(label) - 5,
                    screenY + metrics.getHeight() / 2 - 2);
        }

        // Eksen başlıkları
        g2.setColor(Color.BLACK);
        g2.setFont(plot.AXIS_FONT);
        g2.drawString(label, plot.getWidth() - plot.LEGEND_WIDTH - 30, plot.getHeight() - plot.PADDING / 3);
        AffineTransform original = g2.getTransform();
        g2.rotate(-Math.PI / 2);
        g2.drawString(label, -plot.getHeight() / 2, plot.PADDING / 2);
        g2.setTransform(original);
    }

}
