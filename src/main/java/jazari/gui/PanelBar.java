/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.gui;

import jazari.types.TPanelData;
import jazari.matrix.CMatrix;
import jazari.factory.FactoryUtils;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.geom.AffineTransform;
import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryNormalization;

/**
 *
 * @author BAP1
 */
public class PanelBar extends TPanelData {

    private float[][] data;
    private float[][] originalData;
    private float scale = 1;
    private Color[] color;
    private String[] labels;
    int max_label_index = -1;
    private boolean isValueVisible = true;
    private FrameBar frm;

    public PanelBar(FrameBar frm, CMatrix cm, String[] labels, boolean isValueVisible) {
        super(cm);
        this.frm = frm;
        this.isValueVisible = isValueVisible;
        this.originalData = FactoryMatrix.transpose(FactoryMatrix.clone(getMatrix().getArray2Dfloat()));
        this.labels = labels;
        if (labels != null) {
            max_label_index = getMaximumLengthIndex(labels);
        } else {
            this.labels = generateArtificialLabels(originalData[0]);
        }
        //this.data = FactoryNormalization.normalizeMinMax(FactoryMatrix.clone(getMatrix().getArray2Dfloat()));
        this.data = FactoryNormalization.normalizeMax(FactoryMatrix.clone(getMatrix().getArray2Dfloat()));
        this.data = FactoryMatrix.transpose(this.data);
        color = FactoryUtils.generateColor(data.length);
        initialize();
        repaint();
    }

    public float[][] getData() {
        return getMatrix().getArray2Dfloat();
    }

    public void setData(CMatrix cm) {
        getMatrix().setArray(cm.array);
        this.data = FactoryNormalization.normalizeMax(FactoryMatrix.clone(getMatrix().getArray2Dfloat()));
        this.data = FactoryMatrix.transpose(this.data);
        this.originalData = FactoryMatrix.transpose(FactoryMatrix.clone(getMatrix().getArray2Dfloat()));
        repaint();
    }

    private void initialize() {
        this.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        this.updateUI();
        addMouseListener(new MouseAdapter() {
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                mousePos = e.getPoint();
                repaint();
            }
        });
    }

    @Override
    public void paint(Graphics gr1) {
        Graphics2D gr = (Graphics2D) gr1;
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        gr.setRenderingHint(
//                RenderingHints.KEY_TEXT_ANTIALIASING,
//                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
//        Font fnt = gr.getFont();
//        gr.setFont(new Font(fnt.getFontName(), 1, 15));
        if (!frm.chk_darkMode.isSelected()) {
            gr.setColor(Color.white);
        }

        int w = getWidth();
        int h = getHeight();
        gr.fillRect(0, 0, w, h);
        int px = 70;
        int py = 70;
        int dx = w - 2 * px;
        int dy = h - 2 * py;
        if (max_label_index != -1) {
            dy = h - 3 * gr.getFontMetrics().stringWidth(labels[max_label_index]);
        }

        Point[][] mp = mappingDataToScreenCoordinates(getMatrix().toDoubleArray2D(), dx, dy, px, py);

        if (isActivateDataCursor()) {
            gr.setColor(Color.red);
            checkDataPoints(gr1, mp);
        }

        gr.setColor(Color.black);
        gr.drawRect(px, py, dx, dy);
        drawAxisX(gr, px, py, dx, dy);
        drawAxisY(gr, px, py, dx, dy);
        gr.setColor(Color.red);
        gr.drawRect(0, 0, w - 1, h - 1);
        gr.drawRect(1, 1, w - 3, h - 3);
        //this.paintComponents(gr);
    }

    private void drawAxisX(Graphics2D gr, int px, int py, int dx, int dy) {
        float[] d = data[0];
        float n = 5;
        float deltaValX = d.length / n;
        float deltaX = dx / n;

        if (labels == null || labels.length == 0) {
            for (int i = 0; i <= n; i++) {
                int x = (int) (px + deltaX * i);
                gr.drawString(FactoryUtils.formatFloat(i * deltaValX, 0) + "", x - 5, dy + py + 30);
                gr.drawLine(x, dy + py + 2, x, dy + py + 12);
            }
        }
        float delta_x = 1.0f * dx / data[0].length;
        String maxLabel=FactoryUtils.getMaximum(labels);
        int length = FactoryUtils.getGraphicsTextWidth(gr, maxLabel);
        for (int i = 0; i < d.length; i++) {
            int x1 = (int) (px + i * delta_x+delta_x/2);            
            FactoryUtils.drawRotatedString(gr, labels[i], x1, py+dy+3*length, -Math.PI / 2);
        }
    }
    

    private void drawAxisY(Graphics2D gr, int px, int py, int dx, int dy) {
//        float maxY = FactoryUtils.getMaximum(data);
        float minY = FactoryUtils.getMinimum(originalData);
        float normY = dy;

        int n = 5;
        int deltaY = dy / n;
        float originalMaxY = FactoryUtils.getMaximum(originalData);
        //float originalMinY = FactoryUtils.getMinimum(originalData);
        float originalMinY = 0;
        float deltaValY = (originalMaxY - originalMinY) / n;
        //float deltaValY = (originalMaxY) / n;

        for (int i = 0; i <= n; i++) {
            int distY = (py + dy) - (i * deltaY);
            gr.drawString(FactoryUtils.formatFloat(originalMinY + i * deltaValY, 2) + "", 10, distY + 3);
            gr.drawLine(px - 12, distY, px - 2, distY);
        }

        for (int k = 0; k < data.length; k++) {
            float normX = dx * 1.0f / data[k].length;
            int x = 0;
            int x2 = 0;
            int y = 0;
            for (int i = 0; i < data[k].length; i++) {
                x = (int) (i * normX);
                x2 = (int) ((i + 1) * normX);
                y = (int) (data[k][i] * normY);
                float alpha = 0.5f;
                AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
                gr.setComposite(alcom);
                gr.setColor(color[k]);
                gr.fillRect(px + x, py + dy - y, (x2 - x), y);
                gr.setColor(Color.black);
                if (isValueVisible) {
                    int titleWidth = FactoryUtils.getGraphicsTextWidth(gr, originalData[k][i] + "");
                    gr.drawString(originalData[k][i] + "", px + x + (normX - titleWidth) / 2, dy + py - y - 3);
                }
                gr.drawRect(px + x, py + dy - y, (x2 - x), y);
            }
        }
        Rectangle rect = new Rectangle(px, py, dx, dy);
        float delta_x = 1.0f * dx / data[0].length;
        //BasicStroke stroke = new BasicStroke(3);
        float alpha = 0.85f;
        int qy = 65;
        int qx = 45;
        if (rect.contains(mousePos)) {
            AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            gr.setComposite(alcom);
            int nX = (int) ((mousePos.x - px) / delta_x);
            int y = (int) (data[0][nX] * normY);
            int x1 = (int) (px + nX * delta_x);

            gr.setColor(Color.red);
            gr.fillRect(x1, py + dy - y, (int) (delta_x * 1f), (int) (y));
            gr.setColor(Color.black);
            //gr.setStroke(stroke);
            gr.drawRect(x1, py + dy - y, (int) (delta_x * 1f), (int) (y));
            int titleWidth = FactoryUtils.getGraphicsTextWidth(gr, originalData[0][nX] + "");
            //gr.drawString(originalData[0][nX] + "", x1 + (((int)(delta_x * 2f)-titleWidth)/2) , dy + py - y - 3);
            //gr.setColor(Color.black);
            //gr.drawString(originalData[0][nX] + "", x1 + (((int)(delta_x * 2f)-titleWidth)/2) , dy + py - y + 20);
            gr.drawString(originalData[0][nX] + "", x1 + (((int) (delta_x) - titleWidth) / 2), dy + py - y - 3);
            //gr.drawString(originalData[0][nX] + "", px + x + (normX - titleWidth) / 2, dy + py - y - 3);
            x1 = (int) (px + nX * delta_x + delta_x);
            gr.rotate(Math.toRadians(-90), x1 + qx, py + dy + qy);
            //gr.setFont(new Font("Dialog", 0, 12));
            gr.setColor(Color.white);
            gr.drawString(labels[nX], x1, dy + py + 12);
            //gr.setFont(new Font("Dialog", 1, 12));
            gr.setColor(Color.red);
            gr.drawString(labels[nX], x1, dy + py + 12);
            gr.rotate(Math.toRadians(90), x1 + qx, py + dy + qy);
            //gr.setFont(new Font("Dialog", 0, 12));
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private int getMaximumLengthIndex(String[] labels) {
        int max = 0;
        int ret = 0;
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].length() > max) {
                max = labels[i].length();
                ret = i;
            }
        }
        return ret;
    }

    private String[] generateArtificialLabels(float[] d) {
        String[] ret = new String[d.length];
        for (int i = 0; i < d.length; i++) {
            ret[i] = "" + d[i];
        }
        return ret;
    }

    void setValueVisible(boolean valueVisible) {
        this.isValueVisible = valueVisible;
        repaint();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
