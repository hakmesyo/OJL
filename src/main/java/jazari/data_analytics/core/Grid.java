/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.data_analytics.core;

/**
 *
 * @author cezerilab
 */
import java.awt.*;

public class Grid {

    private Color color;
    private float strokeWidth;

    public Grid(Color color, float strokeWidth) {
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    public Color getColor() {
        return color;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void drawGrid(Graphics2D g2, AbstractPlot plot, float stepX, float stepY) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(strokeWidth));

        // Görünür alan sınırlarını hesapla
        float visibleMinX = plot.screenToWorldX(plot.PADDING);
        float visibleMaxX = plot.screenToWorldX(plot.getWidth() - plot.LEGEND_WIDTH - 10);
        float visibleMinY = plot.screenToWorldY(plot.getHeight() - plot.PADDING);
        float visibleMaxY = plot.screenToWorldY(plot.PADDING);

        // İlk grid çizgisinin konumunu hesapla
        float xStart = (float) Math.floor(visibleMinX / stepX) * stepX;
        float yStart = (float) Math.floor(visibleMinY / stepY) * stepY;

        // Grid çizgilerini çiz
        for (float x = xStart; x <= visibleMaxX + stepX / 2; x += stepX) {
            int screenX = plot.worldToScreenX(x);
            g2.drawLine(screenX, plot.PADDING, screenX, plot.getHeight() - plot.PADDING);
        }

        for (float y = yStart; y <= visibleMaxY + stepY / 2; y += stepY) {
            int screenY = plot.worldToScreenY(y);
            g2.drawLine(plot.PADDING, screenY, plot.getWidth() - plot.LEGEND_WIDTH - 10, screenY);
        }
    }
}
