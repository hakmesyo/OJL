package jazari.data_analytics.core;

import java.awt.*;

public abstract class AbstractAxis implements Axis {
    protected float min;
    protected float max;
    protected String label;
    protected Style labelStyle;
    protected static final int PADDING = 50;
    protected static final int LEGEND_WIDTH = 120;
    protected static final int TARGET_GRID_COUNT = 10;

    public AbstractAxis(float min, float max, String label) {
        this.min = min;
        this.max = max;
        this.label = label;
//        this.labelStyle = new Style();
//        this.labelStyle.setFont(new Font("SansSerif", Font.PLAIN, 12));
    }

    public float calculateNiceStep(float range) {
        // Hedeflenen aralık sayısına göre yaklaşık step büyüklüğünü hesapla
        float roughStep = range / TARGET_GRID_COUNT;

        // En yakın "güzel" sayıyı bul (1, 2, 5 veya bunların 10'un katları)
        float exponent = (float) Math.floor(Math.log10(roughStep));
        float fraction = roughStep / (float) Math.pow(10, exponent);

        float niceFraction;
        if (fraction < 1.5) {
            niceFraction = 1;
        } else if (fraction < 3) {
            niceFraction = 2;
        } else if (fraction < 7.5) {
            niceFraction = 5;
        } else {
            niceFraction = 10;
        }

        return niceFraction * (float) Math.pow(10, exponent);
    }

    protected String formatAxisValue(float value) {
        // Eğer değer tam sayıya çok yakınsa, tam sayı olarak göster
        if (Math.abs(value - Math.round(value)) < 0.00001f) {
            return String.format("%d", Math.round(value));
        }
        // Değilse en fazla 2 ondalık basamak göster
        return String.format("%.1f", value).replaceAll("\\.0$", "");
    }

    @Override
    public float getMin() {
        return min;
    }

    @Override
    public float getMax() {
        return max;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void setLabelStyle(Style style) {
        this.labelStyle = style;
    }

    @Override
    public Style getLabelStyle() {
        return labelStyle;
    }
}