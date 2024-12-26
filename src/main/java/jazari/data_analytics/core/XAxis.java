package jazari.data_analytics.core;

import java.awt.*;

public class XAxis extends AbstractAxis {

    public XAxis(float min, float max, String label) {
        super(min, max, label);

    }

    @Override
    public void drawAxis(Graphics2D g2, AbstractPlot plot) {
        g2.setColor(labelStyle.getLineColor());
        g2.setStroke(labelStyle.getStroke());
        int y = plot.getHeight() - PADDING;
        g2.drawLine(PADDING, y, plot.getWidth() - LEGEND_WIDTH - 10, y);
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

        FontMetrics metrics = g2.getFontMetrics(labelStyle.getFont());
        int labelWidth = metrics.stringWidth(label);
        int x = plot.getWidth() / 2 - labelWidth / 2;
        int y = plot.getHeight() - PADDING / 4;

        // Hafif gölge efekti
        g2.setFont(labelStyle.getFont());
        g2.setColor(new Color(0, 0, 0, 20));
        g2.drawString(label, x + 1, y + 1);

        // Ana metni çiz
        g2.setColor(labelStyle.getTextColor());
        g2.drawString(label, x, y);
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
            int x = plot.worldToScreenX(value);
            String label = formatAxisValue(value);
            int labelWidth = metrics.stringWidth(label);

            // Hafif gölge efekti
            g2.setColor(new Color(0, 0, 0, 15));
            g2.drawString(label, x - labelWidth / 2 + 1,
                    plot.getHeight() - PADDING + metrics.getHeight() + 6);

            // Ana metni çiz
            g2.setColor(labelStyle.getTextColor());
            g2.drawString(label, x - labelWidth / 2,
                    plot.getHeight() - PADDING + metrics.getHeight() + 5);
        }
    }

    @Override
    public boolean isInsideLabel(int mouseX, int mouseY, AbstractPlot plot) {
        FontMetrics metrics = plot.getGraphics().getFontMetrics(labelStyle.getFont());
        int labelWidth = metrics.stringWidth(label);
        int labelHeight = metrics.getHeight();

        int labelX = plot.getWidth() / 2 - labelWidth / 2;
        int labelY = plot.getHeight() - PADDING / 4;

        return mouseX >= labelX && mouseX <= labelX + labelWidth
                && mouseY >= labelY - labelHeight && mouseY <= labelY;
    }
}
