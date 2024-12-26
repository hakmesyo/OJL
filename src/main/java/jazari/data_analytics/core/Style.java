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

public class Style {

    private Font font;
    private Color textColor;
    private int fontSize;
    private Color lineColor;
    private float lineWidth;
    private BasicStroke stroke;
    private Color fillColor;

    public Style() {
        this.font = new Font("SansSerif", Font.PLAIN, 12);
        this.textColor = Color.BLACK;
        this.fontSize = 12;
        this.lineColor = Color.BLACK;
        this.lineWidth = 1.0f;
        this.stroke = new BasicStroke(lineWidth);
        this.fillColor = Color.WHITE;
    }

    public Style(Font font, Color textColor, int fontSize, Color lineColor, float lineWidth, BasicStroke stroke, Color fillColor) {
        this.font = font;
        this.textColor = textColor;
        this.fontSize = fontSize;
        this.lineColor = lineColor;
        this.lineWidth = lineWidth;
        this.stroke = stroke;
        this.fillColor = fillColor;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        // Font nesnesini de güncelle
        this.font = new Font(this.font.getFamily(), this.font.getStyle(), fontSize);
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        this.stroke = new BasicStroke(lineWidth);
    }

    public BasicStroke getStroke() {
        return stroke;
    }

    public void setStroke(BasicStroke stroke) {
        this.stroke = stroke;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }
}
