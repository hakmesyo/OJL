/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.utils.pascalvoc;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;

/**
 *
 * @author cezerilab
 */
public class PascalVocPolygon {

    private int xmin;
    private int ymin;
    private int xmax;
    private int ymax;
    private Rectangle rect;
    public int fromLeft = 0;
    public int fromTop = 0;
    public String name;
    public Color color = Color.yellow;
    public Polygon polygon;

    public PascalVocPolygon(String name, Polygon polygon, int fromLeft, int fromTop, Color color) { 
        this.polygon=polygon;
        this.rect = polygon.getBounds();
        this.xmin = rect.x;
        this.ymin = rect.y;
        this.xmax = rect.x + rect.width;
        this.ymax = rect.y + rect.height;
        this.fromLeft = fromLeft;
        this.fromTop = fromTop;
        this.name = name;
        this.color = (color != null) ? color : Color.yellow;
    }

    public Rectangle getRectangle(int padding) {
        rect.x = xmin - padding;
        rect.y = ymin - padding;
        rect.width = xmax - xmin + 2 * padding;
        rect.height = ymax - ymin + 2 * padding;
        return rect;
    }
    
    public int getXMin(){
        return polygon.getBounds().x;
    }

    public int getYMin(){
        return polygon.getBounds().y;
    }
    
    public int getXMax(){
        return polygon.getBounds().x+polygon.getBounds().width;
    }

    public int getYMax(){
        return polygon.getBounds().y+polygon.getBounds().height;
    }

    @Override
    public int hashCode() {
        int hash = 13;
        hash = 31 * hash + this.xmin;
        hash = 31 * hash + this.ymin;
        hash = 31 * hash + this.xmax;
        hash = 31 * hash + this.ymax;
        hash = 31 * hash + this.fromLeft;
        hash = 31 * hash + this.fromTop;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PascalVocPolygon other = (PascalVocPolygon) obj;
        if (this.xmin != other.xmin) {
            return false;
        }
        if (this.ymin != other.ymin) {
            return false;
        }
        if (this.xmax != other.xmax) {
            return false;
        }
        if (this.ymax != other.ymax) {
            return false;
        }
        if (this.fromLeft != other.fromLeft) {
            return false;
        }
        if (this.fromTop != other.fromTop) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        int k=0;
        String str_polygons="\t\t<polygon>\n";
        for (int i=0;i<polygon.npoints;i++) {
            k++;
            str_polygons+="\t\t\t<x"+k+">"+polygon.xpoints[i]+"</x"+k+">\n"+"\t\t\t<y"+k+">"+polygon.ypoints[i]+"</y"+k+">\n";
        }
        str_polygons+="\t\t</polygon>\n";
        String str_bndBox=
                "\t\t<bndbox>\n"
                + "\t\t\t<xmin>" + (xmin - fromLeft) + "</xmin>\n"
                + "\t\t\t<ymin>" + (ymin - fromTop) + "</ymin>\n"
                + "\t\t\t<xmax>" + (xmax - fromLeft) + "</xmax>\n"
                + "\t\t\t<ymax>" + (ymax - fromTop) + "</ymax>\n"
                + "\t\t</bndbox>\n";
        String ret=str_polygons+str_bndBox;
        return ret;
    }

    public int getWidth() {
        return Math.abs(xmax - xmin);
    }

    public int getHeight() {
        return Math.abs(ymax - ymin);
    }

    public void translate(int dx, int dy) {
        polygon.translate(dx, dy);
    }
    
    public void setLocationTopLeft(int px,int py){
        int dx=px-getXMin();
        int dy=py-getYMin();
        translate(dx, dy);
    }

}
