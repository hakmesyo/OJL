/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.utils.pascalvoc;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Objects;
import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class PascalVocLane {
    public String name;
    public Color color = Color.yellow;
    public ArrayList<Point> spline;

    public PascalVocLane(String name, ArrayList<Point> spline, Color color) { 
        this.spline=spline;
        this.name = name;
        this.color = (color != null) ? color : FactoryUtils.getColorForLaneDetection(Integer.parseInt(name));
    }
    
    public PascalVocLane clone(){
        ArrayList<Point> p=new ArrayList();
        for (Point pnt : spline) {
            p.add(new Point(pnt.x,pnt.y));
        }
        Color cl=new Color(color.getRGB());
        PascalVocLane ret=new PascalVocLane(name, p, cl);
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.name);
        hash = 83 * hash + Objects.hashCode(this.color);
        hash = 83 * hash + Objects.hashCode(this.spline);
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
        final PascalVocLane other = (PascalVocLane) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        return Objects.equals(this.spline, other.spline);
    }


    @Override
    public String toString() {
        int k=0;
        String str_spline="\t\t<spline>\n";
        for (int i=0;i<spline.size();i++) {
            k++;
            str_spline+="\t\t\t<x"+k+">"+spline.get(i).x+"</x"+k+">\n"+"\t\t\t<y"+k+">"+spline.get(i).y+"</y"+k+">\n";
        }
        str_spline+="\t\t</spline>\n";
        String ret=str_spline;
        return ret;
    }

}

