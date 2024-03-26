/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jazari.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jazari.app.Jazo;
import jazari.factory.FactoryUtils;
import jazari.utils.DataAnalytics;

/**
 *
 * @author cezerilab
 */
public class FrameObjectProperties extends javax.swing.JFrame {

    GridBagLayout layout;
    GridBagConstraints gbc;
    String[] objectNames;
    String[] objectColors;
    String[] objects;
    String folderPath = "C:\\Users\\cezerilab\\Desktop\\trafik_dataset\\train_object_detection";
    FrameImage frm;
    String objName;
    JList colorList;
    JList classNameList;
    private String selectedColorName = "Color 255 255 0";
    private String selectedObjectName;
    private int selectedIndex = -1;
    List<DataAnalytics> listDA = null;
    float totalNumberOfObjects = 0;
    private String objectType = "bbox"; //or polygon

    /**
     * Creates new form FrameObjectProperties
     *
     * @param frame
     * @param objName
     * @param objectType
     */
    public FrameObjectProperties(FrameImage frame, String objName, String objectType) {
        this.objectType = objectType;
        this.frm = frame;
        this.folderPath = frame.imageFolderPath;
        this.listDA = FactoryUtils.getDataAnalytics(folderPath);
        this.totalNumberOfObjects = getTotalNumberOfObjects();
        if (objName == null || objName.equals("")) {
            this.objName = "class_name";
            objects = FactoryUtils.readFile(folderPath + "/class_labels.txt").split("\n");
           selectedIndex = -1;
        } else {
            this.objName = objName;
            objects = FactoryUtils.readFile(folderPath + "/class_labels.txt").split("\n");
            selectedIndex = getSelectedIndex(objects, objName);
            if (selectedIndex == -1) {
                System.err.println("böyle bir nesne adı class_labels.txt dosyasında yok");
                dispose();
                return;
            }
        }
        initComponents();

        setTitle("Object Properties Frame");
        setAlwaysOnTop(true);
        setLocation(200, 80);
        txt_obj_name.setText(this.objName);

        init();
        txt_obj_name.requestFocus();
        lbl_tot_object.setText("" + totalNumberOfObjects);
    }

    private void addobjects(Component component, JPanel yourcontainer, GridBagLayout layout, GridBagConstraints gbc, int gridx, int gridy, int gridwidth, int gridheigth) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;

        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheigth;

        layout.setConstraints(component, gbc);
        yourcontainer.add(component);
    }

    private String[] buildClassNameList(String[] cs) {
        String[] ret = new String[cs.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = cs[i].split(":")[0];
        }
        return ret;
    }

    private Color[] buildColorList(String[] cs) {
        Color[] ret = new Color[cs.length];
        objectColors = new String[cs.length];
        if (cs.length == 1 && cs[0].isEmpty()) {
            ret[0] = Color.yellow;
            return ret;
        }

        for (int i = 0; i < cs.length; i++) {
            String[] str = cs[i].split(":");
            if (str.length > 0) {
                String className = str[0];
                String cls[] = str[1].split(" ");
                int red = Integer.parseInt(cls[1]);
                int green = Integer.parseInt(cls[2]);
                int blue = Integer.parseInt(cls[3]);
                ret[i] = new Color(red, green, blue);
                objectColors[i] = str[1];
            }
        }
        return ret;
    }

    private String[] getCounts() {
        String[] ret = new String[listDA.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = "" + listDA.get(i).frequency;
        }
        //ret[ret.length - 1] = "" + totalNumberOfObjects;
        return ret;
    }

    private String[] getRatios() {
        String[] ret = new String[listDA.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = listDA.get(i).ratio + " %";
        }
        //ret[ret.length - 1] = "100 %";
        return ret;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txt_obj_name = new javax.swing.JTextField();
        btn_color = new javax.swing.JButton();
        btn_add_new = new javax.swing.JButton();
        lbl_color = new javax.swing.JLabel();
        btn_ok = new javax.swing.JButton();
        btn_cancel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        container = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        lbl_tot_object = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Class Name:");

        txt_obj_name.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_obj_nameActionPerformed(evt);
            }
        });

        btn_color.setText("Color");
        btn_color.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_colorActionPerformed(evt);
            }
        });

        btn_add_new.setText("Add New");
        btn_add_new.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_add_newActionPerformed(evt);
            }
        });

        lbl_color.setBackground(new java.awt.Color(255, 255, 102));
        lbl_color.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        lbl_color.setOpaque(true);
        lbl_color.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lbl_colorMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_add_new)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_obj_name, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_color, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btn_color, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(txt_obj_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btn_color)
                        .addComponent(btn_add_new))
                    .addComponent(lbl_color, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        btn_ok.setText("OK");
        btn_ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_okActionPerformed(evt);
            }
        });

        btn_cancel.setText("CANCEL");
        btn_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cancelActionPerformed(evt);
            }
        });

        container.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout containerLayout = new javax.swing.GroupLayout(container);
        container.setLayout(containerLayout);
        containerLayout.setHorizontalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 454, Short.MAX_VALUE)
        );
        containerLayout.setVerticalGroup(
            containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 328, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(container);

        jLabel2.setText("Total Number of Objects:");

        lbl_tot_object.setText("100");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btn_ok, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addGap(4, 4, 4)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_tot_object, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btn_ok, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                            .addComponent(btn_cancel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(lbl_tot_object))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cancelActionPerformed
        frm.getPicturePanel().isBBoxCancelled = true;
        dispose();
    }//GEN-LAST:event_btn_cancelActionPerformed

    private void btn_okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_okActionPerformed
        if (selectedIndex != -1) {
            String[] s = objects[selectedIndex].split(":");
            objects[selectedIndex] = txt_obj_name.getText() + ":" + s[1];
            FactoryUtils.writeToFile(frm.imageFolderPath + "/class_labels.txt", objects);
            frm.getPicturePanel().updateObjectProperties(txt_obj_name.getText() + ":" + objectColors[selectedIndex], this.objectType);
        }
        dispose();
    }//GEN-LAST:event_btn_okActionPerformed

    private void btn_colorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_colorActionPerformed
        Color color = JColorChooser.showDialog(null, "Choose Color", Color.yellow);
        if (color != null) {
            selectedColorName = "Color " + color.getRed() + " " + color.getGreen() + " " + color.getBlue();
            lbl_color.setBackground(color);
            if (selectedObjectName != null && selectedObjectName.equals(txt_obj_name.getText())) {
                updateObjects(selectedColorName);
                init();
            }
        }

    }//GEN-LAST:event_btn_colorActionPerformed

    private void txt_obj_nameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_obj_nameActionPerformed
        btn_ok.doClick();
    }//GEN-LAST:event_txt_obj_nameActionPerformed

    private void btn_add_newActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_add_newActionPerformed
        if (!checkSimilar(txt_obj_name.getText())) {
            List<String> list = getArrayList(objects);
            if (list.contains("")){
                list.remove("");
            }
            list.add(txt_obj_name.getText() + ":" + selectedColorName);
            objects = list.toArray(objects);
            System.out.println(Arrays.asList(objects));
            init();
            selectedIndex = objects.length - 1;
            colorList.setSelectedIndex(selectedIndex);
            classNameList.setSelectedIndex(selectedIndex);
            jScrollPane1.getVerticalScrollBar().setValue(objects.length * 15);
        } else {
            FactoryUtils.showMessage("You have same class name in the list");
        }
    }//GEN-LAST:event_btn_add_newActionPerformed

    private void lbl_colorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lbl_colorMouseClicked
        btn_color.doClick();
    }//GEN-LAST:event_lbl_colorMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(FrameObjectProperties.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(FrameObjectProperties.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(FrameObjectProperties.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(FrameObjectProperties.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        //</editor-fold>

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Jazo.class.getName()).log(Level.SEVERE, null, ex);
        }


        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrameObjectProperties(null, "", "bbox").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_add_new;
    private javax.swing.JButton btn_cancel;
    private javax.swing.JButton btn_color;
    private javax.swing.JButton btn_ok;
    private javax.swing.JPanel container;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_color;
    private javax.swing.JLabel lbl_tot_object;
    private javax.swing.JTextField txt_obj_name;
    // End of variables declaration//GEN-END:variables

    private int getSelectedIndex(String[] objects, String objName) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i].split(":")[0].equals(objName)) {
                return i;
            }
        }
        return -1;
    }

    private void init() {
        container.removeAll();
        container.revalidate();
        container.repaint();

        layout = new GridBagLayout();
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        container.setLayout(layout);

        addobjects(new JLabel("Class Name"), container, layout, gbc, 0, 0, 1, 1);
        addobjects(new JLabel("Color"), container, layout, gbc, 1, 0, 1, 1);
        addobjects(new JLabel("Count"), container, layout, gbc, 2, 0, 1, 1);
        addobjects(new JLabel("Ratio"), container, layout, gbc, 3, 0, 1, 1);

        Color[] itemColorList = buildColorList(objects);
        colorList = new JList(itemColorList);
        colorList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Color item = (Color) value;
                setText("                                                         ");
                setBackground(item);
                if (isSelected) {
                    setBackground(getBackground().darker());
                }
                return c;
            }

        });
        colorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    selectedIndex = list.locationToIndex(evt.getPoint());
                    txt_obj_name.setText(objectNames[selectedIndex]);
                    classNameList.setSelectedIndex(selectedIndex);
                    selectedObjectName = txt_obj_name.getText();
                    selectedColorName = objectColors[selectedIndex];
                    lbl_color.setBackground(getColor(selectedColorName));
                    btn_color.doClick();
                }
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                selectedIndex = list.locationToIndex(evt.getPoint());
                txt_obj_name.setText(objectNames[selectedIndex]);
                classNameList.setSelectedIndex(selectedIndex);
                selectedObjectName = txt_obj_name.getText();
                selectedColorName = objectColors[selectedIndex];
                lbl_color.setBackground(getColor(selectedColorName));
            }

        });
        addobjects(colorList, container, layout, gbc, 1, 1, 1, 1);

        objectNames = buildClassNameList(objects);
        classNameList = new JList(objectNames);
        classNameList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    selectedIndex = list.locationToIndex(evt.getPoint());
                    txt_obj_name.setText(objectNames[selectedIndex]);
                    colorList.setSelectedIndex(selectedIndex);
                    selectedObjectName = txt_obj_name.getText();
                    selectedColorName = objectColors[selectedIndex];
                    lbl_color.setBackground(getColor(selectedColorName));
                    btn_ok.doClick();
                }
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                selectedIndex = list.locationToIndex(evt.getPoint());
                txt_obj_name.setText(objectNames[selectedIndex]);
                colorList.setSelectedIndex(selectedIndex);
                selectedObjectName = txt_obj_name.getText();
                selectedColorName = objectColors[selectedIndex];
                lbl_color.setBackground(getColor(selectedColorName));
            }
        });

        addobjects(classNameList, container, layout, gbc, 0, 1, 1, 1);

        if (listDA != null) {
            String[] counts = getCounts();
            JList countList = new JList(counts);
            addobjects(countList, container, layout, gbc, 2, 1, 1, 1);

            String[] ratios = getRatios();
            JList ratioList = new JList(ratios);
            addobjects(ratioList, container, layout, gbc, 3, 1, 1, 1);

            if (selectedIndex != -1) {
                classNameList.setSelectedValue(objName, true);
                colorList.setSelectedIndex(selectedIndex);
                jScrollPane1.getVerticalScrollBar().setValue(selectedIndex * 15);
                txt_obj_name.setText(objName);
                selectedObjectName = objName;
                selectedColorName = objectColors[selectedIndex];
                lbl_color.setBackground(getColor(selectedColorName));
            }
        }
        pack();
    }

    private void updateObjects(String selectedColorName) {
        String[] s = objects[selectedIndex].split(":");
        objects[selectedIndex] = s[0] + ":" + selectedColorName;
    }

    private Color getColor(String name) {
        String[] s = name.split(" ");
        Color ret = new Color(Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]));
        return ret;
    }

    private List<String> getArrayList(String[] d) {
        List<String> ret = new ArrayList();
        ret.addAll(Arrays.asList(d));
        return ret;
    }

    private float getTotalNumberOfObjects() {
        if (listDA == null) {
            return -1;
        }
        float ret = 0;
        for (int i = 0; i < listDA.size(); i++) {
            ret += listDA.get(i).frequency;
        }
        return ret;
    }

    private boolean checkSimilar(String s) {
        boolean ret = false;
        for (String object : objects) {
            String str = object.split(":")[0];
            if (str.equals(s)) {
                return true;
            }
        }
        return ret;
    }
}
