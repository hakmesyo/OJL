/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jazari.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import jazari.utils.DataAnalytics;

/**
 *
 * @author cezerilab
 */
public class FrameDataAnalytics extends javax.swing.JFrame {

    List<DataAnalytics> listDA;
    String folderPath="C:\\Users\\cezerilab\\Desktop\\trafik_dataset\\train_object_detection";
    GridBagLayout layout;
    GridBagConstraints gbc;
    FrameImage frm;
    float totalNumberOfObjects=0;
    String[] names;

    /**
     * Creates new form FrameDataAnalytics
     */
    public FrameDataAnalytics() {
        initComponents();
        initialize();
    }
    
    public FrameDataAnalytics(FrameImage frm,String folderPath, List<DataAnalytics> listDA) {
        this.frm=frm;
        this.folderPath = folderPath;
        this.listDA = listDA;
        initComponents();
        initialize();
    }

    
    private void initialize() {

        setTitle("Data Analaytics Frame");
        //Point loc = getLocation();
        //setLocation(loc.x + 120, loc.y + 120);
        setAlwaysOnTop(true);
        //setPreferredSize(new Dimension(700, 500));

        layout = new GridBagLayout();
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        container.setLayout(layout);

//        JLabel title = new JLabel("I like Cockatoos");
//        JButton button = new JButton("Me too");
//        JButton button2 = new JButton("Me too");
//        JTextField txt=new JTextField("merhaba looo");
//
//        addobjects(title, container, layout, gbc, 0, 0, 1, 1);
//        addobjects(button, container, layout, gbc, 0, 1, 1, 1);
//        addobjects(button2, container, layout, gbc, 1, 1, 1, 1);

        addobjects(new JLabel("Color"), container, layout, gbc, 0, 0, 1, 1);
        addobjects(new JLabel("Class Name"), container, layout, gbc, 1, 0, 1, 1);
        addobjects(new JLabel("Count"), container, layout, gbc, 2, 0, 1, 1);
        addobjects(new JLabel("Ratio"), container, layout, gbc, 3, 0, 1, 1);
        
        

        String[] cs = FactoryUtils.readFile(folderPath + "/class_labels.txt").split("\n");

        Color[] itemList = buildUrgentColorList();
        JList colorList = new JList(itemList);
        colorList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Color item = (Color) value;
                setText("           ");
                setBackground(item);
                if (isSelected) {
                    setBackground(getBackground().darker());
                }
                return c;
            }

        });
        addobjects(colorList, container, layout, gbc, 0, 1, 1, 1);

        names = buildClassNameList(cs);
        JList classNameList = new JList(names);
        addobjects(classNameList, container, layout, gbc, 1, 1, 1, 1);
        
        String[] counts=getCounts();
        JList countList = new JList(counts);
        addobjects(countList, container, layout, gbc, 2, 1, 1, 1);
        
        String[] ratios=getRatios();
        JList ratioList = new JList(ratios);
        addobjects(ratioList, container, layout, gbc, 3, 1, 1, 1);
        
        pack();
        setLocation(frm.getLocation().x+frm.getWidth()-getWidth()-10, frm.getLocation().y+83);
    }


    public void addobjects(Component component, JPanel yourcontainer, GridBagLayout layout, GridBagConstraints gbc, int gridx, int gridy, int gridwidth, int gridheigth) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;

        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheigth;

        layout.setConstraints(component, gbc);
        yourcontainer.add(component);
    }


    private String[] buildClassNameList(String[] cs) {
        String[] ret = new String[cs.length+1];
        for (int i = 0; i < ret.length-1; i++) {
            ret[i] = cs[i].split(":")[0];
        }
        ret[ret.length-1]="Total number of objects";
        return ret;
    }


    private String[] getCounts() {
        String[] ret=new String[listDA.size()+1];
        for (int i = 0; i < ret.length-1; i++) {
            ret[i]=""+listDA.get(i).frequency;
        }
        ret[ret.length-1]=""+totalNumberOfObjects;
        return ret;
    }

    private String[] getRatios() {
        String[] ret=new String[listDA.size()+1];
        for (int i = 0; i < ret.length-1; i++) {
            ret[i]=listDA.get(i).ratio+" %";
        }
        ret[ret.length-1]="100 %";
        return ret;
    }

    private Color[] buildUrgentColorList() {
        Color[] ret=new Color[listDA.size()+1];
        float max=0;
        for (int i = 0; i < ret.length-1; i++) {
            if (max<listDA.get(i).ratio/100){
                max=listDA.get(i).ratio/100;
            }
            totalNumberOfObjects+=listDA.get(i).frequency;
        }
        float coef=1.0f/max;
        for (int i = 0; i < ret.length-1; i++) {
            ret[i]=new Color(checkRange(coef*(max-listDA.get(i).ratio/100)), checkRange(coef*listDA.get(i).ratio/100), 0);
        }
        //ret[ret.length-1]=new Color(0,255,0);
        return ret;
    }
    
    private float checkRange(float val){
        if (val>1.0f) {
            return 1.0f;
        }else if(val<0){
            return 0;
        }else{
            return val;
        }
    }

    private float[][] getData() {
        String[] cnt=getCounts();
        float[][] ret=new float[cnt.length-1][1];
        for (int i = 0; i < cnt.length-1; i++) {
            ret[i][0]=Float.parseFloat(cnt[i]);
        }
        return ret;
    }

    public class ColorItem {

        public String className;
        public Color color;

        public ColorItem(String className, Color color) {
            this.className = className;
            this.color = color;
        }

        @Override
        public String toString() {
            return className + ":Color " + color.getRed() + " " + color.getGreen() + " " + color.getBlue();
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

        btn_bar_plot = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        container = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        btn_bar_plot.setText("Bar Plot");
        btn_bar_plot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_bar_plotActionPerformed(evt);
            }
        });

        jButton2.setText("Close");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        container.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout containerLayout = new javax.swing.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 460, Short.MAX_VALUE)
        );
        containerLayout.setVerticalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 403, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(container);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btn_bar_plot, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_bar_plot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 409, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        frm.getPicturePanel().requestFocus();
    }//GEN-LAST:event_formWindowClosed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void btn_bar_plotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_bar_plotActionPerformed
        float[][] data = getData();
        CMatrix cm = CMatrix.getInstance(data).bar(names);        
    }//GEN-LAST:event_btn_bar_plotActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrameDataAnalytics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrameDataAnalytics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrameDataAnalytics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrameDataAnalytics.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrameDataAnalytics().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_bar_plot;
    private javax.swing.JPanel container;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
