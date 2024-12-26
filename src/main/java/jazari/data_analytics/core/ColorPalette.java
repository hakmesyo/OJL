package jazari.data_analytics.core;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ColorPalette {

    private Map<String, Color> colors = new HashMap<>();
    private int nClass = 0;

    public ColorPalette(String[] labels) {
        java.util.Set<String> my_set = new java.util.HashSet<>();
        for (String label : labels) {
            my_set.add(label);
        }
        nClass = my_set.size();
        initColors(labels);

    }

    public ColorPalette(Map<String, Color> colorMap) {
        this.colors = colorMap;
        nClass = colorMap.size();
    }

    private void initColors(String[] labels) {
        float hue = 0.0f;
        float saturation = 0.8f;
        float brightness = 0.9f;
        float hueStep = 1.0f / nClass;

        for (String label : labels) {
            if (!colors.containsKey(label)) {  // Eğer key yoksa
                colors.put(label, Color.getHSBColor(hue, saturation, brightness));
                hue += hueStep;
            }
        }
    }

    public Color getColor(String label) {
        return colors.get(label);
    }

    public Map<String, Color> getColors() {
        return colors;
    }

    public int getClassSize() {
        return nClass;
    }
}
