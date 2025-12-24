package jazari.utils.pascalvoc;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;

public class PascalVocLine implements Serializable {

    public String name;
    public Point startPoint;
    public Point endPoint;
    public Color color;
    public int thickness = 2; // Çizgi kalınlığı varsayılan

    /**
     * Boş Constructor
     */
    public PascalVocLine() {
        this.startPoint = new Point(0, 0);
        this.endPoint = new Point(0, 0);
        this.name = "line";
        this.color = Color.YELLOW;
    }

    /**
     * Dolu Constructor
     *
     * @param name Sınıf adı
     * @param startPoint Başlangıç noktası
     * @param endPoint Bitiş noktası
     * @param color Çizgi rengi
     */
    public PascalVocLine(String name, Point startPoint, Point endPoint, Color color) {
        this.name = name;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.color = color;
    }

    /**
     * Klonlama (Deep Copy) için yardımcı metod
     */
    public PascalVocLine clone() {
        return new PascalVocLine(
                this.name,
                new Point(this.startPoint),
                new Point(this.endPoint),
                new Color(this.color.getRGB())
        );
    }

    @Override
    public String toString() {
        return "Line: " + name + " [" + startPoint.x + "," + startPoint.y + "] -> [" + endPoint.x + "," + endPoint.y + "]";
    }
}
