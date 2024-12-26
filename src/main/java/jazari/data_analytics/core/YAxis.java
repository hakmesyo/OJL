package jazari.data_analytics.core;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class YAxis extends AbstractAxis {

    public YAxis(float min, float max, String label) {
        super(min, max, label);
    }

    @Override
    public void drawAxis(Graphics2D g2, AbstractPlot plot) {
        g2.setColor(labelStyle.getLineColor());
        g2.setStroke(labelStyle.getStroke());
        int x = PADDING;
        g2.drawLine(x, PADDING, x, plot.getHeight() - PADDING);
    }

    @Override
    public void drawAxisLabel(Graphics2D g2, AbstractPlot plot) {
        if (label == null) {
            return;
        }

        // Rendering kalitesini artır
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2.setFont(labelStyle.getFont());
        FontMetrics metrics = g2.getFontMetrics();
        AffineTransform original = g2.getTransform();
        g2.rotate(-Math.PI / 2);
        int labelWidth = metrics.stringWidth(label);

        // Hafif gölge efekti
        g2.setColor(new Color(0, 0, 0, 20));
        g2.drawString(label,
                -plot.getHeight() / 2 - labelWidth / 2 + 1, // Gölge offset x
                PADDING / 2 + 1);                            // Gölge offset y

        // Ana metni çiz
        g2.setColor(labelStyle.getTextColor());
        g2.drawString(label,
                -plot.getHeight() / 2 - labelWidth / 2,
                PADDING / 2);

        g2.setTransform(original);
    }

    @Override
    public void drawAxisLabels(Graphics2D g2, AbstractPlot plot, float step) {
        // Rendering kalitesini artır
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2.setFont(labelStyle.getFont());
        FontMetrics metrics = g2.getFontMetrics();
        float niceStep = calculateNiceStep(Math.abs(max - min));
        float start = (float) (Math.ceil(min / niceStep) * niceStep);

        for (float value = start; value <= max; value += niceStep) {
            int y = plot.worldToScreenY(value);
            String label = formatAxisValue(value);
            int labelWidth = metrics.stringWidth(label);

            // Hafif gölge efekti
            g2.setColor(new Color(0, 0, 0, 15));
            g2.drawString(label,
                    PADDING - labelWidth - 4, // Gölge biraz sağa
                    y + metrics.getHeight() / 2 + 1);  // Gölge biraz aşağı

            // Ana metni çiz
            g2.setColor(labelStyle.getTextColor());
            g2.drawString(label,
                    PADDING - labelWidth - 5,
                    y + metrics.getHeight() / 2);
        }
    }

    @Override
    public boolean isInsideLabel(int mouseX, int mouseY, AbstractPlot plot) {
        FontMetrics metrics = plot.getGraphics().getFontMetrics(labelStyle.getFont());
        int labelWidth = metrics.stringWidth(label);

        int labelX = PADDING / 2;
        int labelY = plot.getHeight() / 2;

        return mouseX >= 0 && mouseX <= PADDING
                && mouseY >= labelY - labelWidth / 2 && mouseY <= labelY + labelWidth / 2;
    }
}
