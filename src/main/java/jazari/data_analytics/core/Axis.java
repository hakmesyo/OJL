package jazari.data_analytics.core;

import java.awt.Graphics2D;

public interface Axis {
    float getMin();
    float getMax();
    String getLabel();
    void setLabel(String label);
    void setLabelStyle(Style style);
    Style getLabelStyle();
    void drawAxis(Graphics2D g2, AbstractPlot plot);
    void drawAxisLabel(Graphics2D g2, AbstractPlot plot);
    void drawAxisLabels(Graphics2D g2, AbstractPlot plot, float step);
    boolean isInsideLabel(int mouseX, int mouseY, AbstractPlot plot);
}