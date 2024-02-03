/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.types;

import java.awt.Font;
import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryUtils;
import java.awt.Stroke;
import java.util.ArrayList;
import javax.swing.JLabel;

/**
 *
 * @author BAP1
 */
public class TFigureAttribute {

    public String figureCaption = "Default Caption";
    public String title = "Plot";
    public Font fontTitle = null;//new JLabel().getFont();
    public Font[] fontCategories = null;//new JLabel().getFont();
    public Font fontAxisX = null;//new JLabel().getFont();
    public Font fontAxisY = null;//new JLabel().getFont();
    public String[] axis_names = new String[]{"y - axis", "x - axis"};
    public String[] items = new String[]{"X1", "X2"};
    public String[] categories = null;
    public String pointType = "-";
    public ArrayList<String> perfMetricStr = new ArrayList();
    public ArrayList<Float> perfMetricVal = new ArrayList();
    public boolean isStroke = false;
    public ArrayList<Stroke> stroke = new ArrayList<>();

    public TFigureAttribute() { 
        int a=3;
    }

    public TFigureAttribute(String title,String[] axisNames, String[] items, String[] categories) {
        this.title=title;
        this.axis_names=axisNames;
        this.items=items;
        this.categories=categories;
        this.fontCategories=new Font[this.categories.length];
        setFontCategories(new JLabel().getFont());
    }

    public TFigureAttribute(String figureCaption, String title, String axisNames, String items) {
        this.figureCaption = figureCaption;
        this.title = title;
        this.axis_names = axisNames.split(",");
        this.items = items.split(",");
    }

    public TFigureAttribute clone() {
        TFigureAttribute ret = new TFigureAttribute();
        ret.figureCaption = this.figureCaption;
        ret.title = this.title;
        ret.isStroke = this.isStroke;
        ret.stroke = this.stroke;
        ret.axis_names = FactoryMatrix.clone(this.axis_names);
        ret.items = FactoryMatrix.clone(this.items);
        ret.perfMetricStr = (ArrayList<String>) this.perfMetricStr.clone();
        ret.perfMetricVal = (ArrayList<Float>) this.perfMetricVal.clone();
        ret.fontCategories=fontCategories;
        return ret;
    }

    public void setFontCategories(Font font) {
        for (int i = 0; i < fontCategories.length; i++) {
            fontCategories[i]=font;
        }
    }
}
