/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui.test;

import jazari.matrix.CMatrix;
import jazari.types.TFigureAttribute;

/**
 *
 * @author cezerilab
 */
public class TestBar {
    public static void main(String[] args) {
//        CMatrix cm1 = CMatrix.getInstance().imread("images/pullar.png").imhist();
//        callWithTFigureAttribute();
        //callWithLabelsAndItems();
    }

    private static void callWithTFigureAttribute() {
        TFigureAttribute attr=new TFigureAttribute(
                "Lane Detection Performance Evaluation",
                new String[]{"Accuracy","Groups"},
                new String[]{"Epoch-10","Epoch-20","Epoch-30","Epoch-40","Epoch-50"},
                new String[]{"SCNN","U-Net","ENet","ENet-SAD"}
        );
        CMatrix cm = CMatrix.getInstance()
                .rand(4, 5, -150f, 151f)
                .bar(attr)
                ;
    }

    private static void callWithLabelsAndItems() {
        float[] f={12.3f,-34.57f,17.91f,21.21f};
        String[] labels={"SCNN","U-Net","ENet","ENet-SAD"};
        String[] items={"Epoch-10","Epoch-20","Epoch-30","Epoch-40","Epoch-50"};
        CMatrix cm = CMatrix.getInstance(f)
                //.bar()
                .rand(4, 5, -150f, 151f)
                .shape()
                .bar(labels,items)                
                ;
    }
}
