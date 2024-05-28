/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.gui;

import jazari.matrix.CMatrix;
import jazari.utils.LineNumberTableRowHeader;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import jazari.factory.FactoryMatrix;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;

/**
 *
 * @author BAP1
 */
public class FrameDataGrid extends javax.swing.JFrame {

    public float[][] data2D;
    public float[][][] data3D;
    private String dataType = "2D";//or 3D
    public FrameImage frameImage;
    public boolean isInt;

    /**
     *
     * @param data
     * @param isInt
     */
    public FrameDataGrid(float[][] data,boolean isInt) {
        this.isInt=isInt;
        data2D = data;
        dataType = "2D";
        initComponents();
        setMatrix(data2D, ds_table,isInt);
        LineNumberTableRowHeader tableLineNumber = new LineNumberTableRowHeader(jScrollPane2, ds_table);
        jScrollPane2.setRowHeaderView(tableLineNumber);
    }

    /**
     *
     * @param data
     */
    public FrameDataGrid(float[][][] data) {
        dataType = "3D";
        this.isInt=true;
        data3D = data;
        initComponents();
        tabbedPane.removeAll();
        String[] tabTitles = {"Alpha Channel", "Red Channel", "Green Channel", "Blue Channel"};
        for (int i = 0; i < data3D.length; i++) {
            JScrollPane scroll = new JScrollPane();
            JTable table = new JTable();
            setMatrix(data3D[i], table,isInt);
            scroll.setViewportView(table);
            LineNumberTableRowHeader tableLineNumber = new LineNumberTableRowHeader(scroll, table);
            scroll.setRowHeaderView(tableLineNumber);
            tabbedPane.add(scroll);
            tabbedPane.setTitleAt(i, tabTitles[i]);
            this.data2D = data[i];
        }
        int selectedIndex = tabbedPane.getSelectedIndex();
        data2D = data3D[selectedIndex];
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = tabbedPane.getSelectedIndex();
                data2D = data3D[selectedIndex];
            }
        });
    }

    public void setMatrix(float[][] data, JTable table,boolean isInt) {
        this.data2D = data;
        table.setModel(getTableModelForArtificialData(data, isInt));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setColumnSelectionAllowed(true);
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int clickedIndex = table.convertColumnIndexToModel(table.columnAtPoint(e.getPoint()));
                table.setColumnSelectionInterval(clickedIndex, clickedIndex); //selects which column will have all its rows selected
                table.setRowSelectionInterval(0, table.getRowCount() - 1); //once column has been selected, select all rows from 0 to the end of that column
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    String newValue = JOptionPane.showInputDialog("Set pixel value:");
                    try {
                        int value = Integer.parseInt(newValue);
                        for (int row : table.getSelectedRows()) {
                            for (int col : table.getSelectedColumns()) {
                                table.setValueAt(value, row, col);
                                data2D[row][col] = value;
                            }
                        }
                        if (frameImage != null) {
                            if (dataType.equals("2D")) {
                                if (frameImage.getPicturePanel().selectionRectImage == null) {
                                    frameImage.getPicturePanel().setMatrixData(data2D);
                                } else {
                                    updateSelectedData2D(frameImage.getPicturePanel().imgData);
                                    frameImage.getPicturePanel().setMatrixData(frameImage.getPicturePanel().imgData);
                                }
                            } else {
                                if (frameImage.getPicturePanel().selectionRectImage == null) {
                                    frameImage.getPicturePanel().setMatrixData(data3D);
                                } else {
                                    float[][][] dt = updateSelectedData3D(ImageProcess.imageToPixelsColorFloatFaster(frameImage.getPicturePanel().getImage()));
                                    frameImage.getPicturePanel().setMatrixData(dt);
                                }
                            }

                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Geçersiz değer! Lütfen bir rakam giriniz.");
                    }
                }
            }

        });
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT); // Sağa dayalı hizalama
        for (int j = 0; j < table.getColumnCount(); j++) {
            table.getColumnModel().getColumn(j).setCellRenderer(rightRenderer);
            table.getColumnModel().getColumn(j).setPreferredWidth(40);
        }
        this.setTitle("DataGrid for image, format is [ " + data.length + " x " + data[0].length + " ]");
    }

    private float[][] updateSelectedData2D(float[][] orgData) {
        Point rect = frameImage.getPicturePanel().getRelativeSelectedRectangleTopLeft();
        for (int i = 0; i < frameImage.getPicturePanel().selectionRect.height; i++) {
            for (int j = 0; j < frameImage.getPicturePanel().selectionRect.width; j++) {
                orgData[i + rect.y][j + rect.x] = data2D[i][j];
            }
        }
        return orgData;
    }

    private float[][][] updateSelectedData3D(float[][][] orgData) {
        Point rect = frameImage.getPicturePanel().getRelativeSelectedRectangleTopLeft();
        for (int k = 0; k < data3D.length; k++) {
            for (int i = 0; i < frameImage.getPicturePanel().selectionRect.height; i++) {
                for (int j = 0; j < frameImage.getPicturePanel().selectionRect.width; j++) {
                    orgData[k][i + rect.y][j + rect.x] = data3D[k][i][j];
                }
            }
        }
        return orgData;
    }

    public float[][] getMatrix2D() {
        return data2D;
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
        btn_visualize = new javax.swing.JButton();
        btn_plot = new javax.swing.JButton();
        btn_scatter = new javax.swing.JButton();
        btn_transpose = new javax.swing.JButton();
        btn_from_text = new javax.swing.JButton();
        btn_bar_plot = new javax.swing.JButton();
        btn_heatmap = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        ds_table = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btn_visualize.setText("ImShow");
        btn_visualize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_visualizeActionPerformed(evt);
            }
        });

        btn_plot.setText("Plot");
        btn_plot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_plotActionPerformed(evt);
            }
        });

        btn_scatter.setText("Scatter");
        btn_scatter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_scatterActionPerformed(evt);
            }
        });

        btn_transpose.setText("Transpose");
        btn_transpose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_transposeActionPerformed(evt);
            }
        });

        btn_from_text.setText("DataSet Text Editor");
        btn_from_text.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_from_textActionPerformed(evt);
            }
        });

        btn_bar_plot.setText("Bar Plot");
        btn_bar_plot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_bar_plotActionPerformed(evt);
            }
        });

        btn_heatmap.setText("Heat Map");
        btn_heatmap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_heatmapActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btn_transpose, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_plot, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_scatter, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_bar_plot, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_visualize, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_heatmap, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_from_text)
                .addContainerGap(148, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(btn_plot, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_scatter, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_visualize, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_transpose, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_bar_plot, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_from_text, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btn_heatmap, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        ds_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        ds_table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        ds_table.setCellSelectionEnabled(true);
        jScrollPane2.setViewportView(ds_table);

        tabbedPane.addTab("Gray", jScrollPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public TableModel getTableModelForArtificialData(float[][] data, boolean isInt) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            int colNumber = data[0].length;
            String columnNames[] = new String[colNumber];
            for (int i = 0; i < colNumber; i++) {
                columnNames[i] = "" + (i);
            }
            if (!isInt) {
                String dataS[][] = FactoryUtils.toStringArray2D(data);
                TableModel model = new DefaultTableModel(dataS, columnNames);
                return model;
            } else {
                String dataS[][] = FactoryMatrix.toStringArray2DAsInt(data);
                TableModel model = new DefaultTableModel(dataS, columnNames);
                return model;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void btn_visualizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_visualizeActionPerformed
        if (isDataSelectedFromTable()) {
            if (dataType.equals("2D")) {
                float[][] f = getSelectedCells();
                CMatrix.getInstance(f).transpose().imshow();
            } else {
                float[][][] f = getSelected3DCells();
                CMatrix.getInstance(f).transpose().imshow();
            }
        } else {
            if (dataType.equals("2D")) {
                data2D = getSelectedTableData();
                CMatrix.getInstance(data2D).imshow();
            } else {
                data3D = getTablesData();
                CMatrix.getInstance(data3D).imshow();
            }

        }
    }//GEN-LAST:event_btn_visualizeActionPerformed

    private JTable getSelectedTable() {
        Component tabComponent = tabbedPane.getComponentAt(tabbedPane.getSelectedIndex());
        if (tabComponent instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) tabComponent;
            JViewport viewport = scrollPane.getViewport();
            if (viewport.getView() instanceof JTable) {
                return (JTable) viewport.getView();
            }
        }
        return null;
    }

    private JTable getTable(int index) {
        Component tabComponent = tabbedPane.getComponentAt(index);
        if (tabComponent instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) tabComponent;
            JViewport viewport = scrollPane.getViewport();
            if (viewport.getView() instanceof JTable) {
                return (JTable) viewport.getView();
            }
        }
        return null;
    }

    private void btn_plotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_plotActionPerformed
        if (isDataSelectedFromTable()) {
            float[][] f = getSelectedCells();
            CMatrix.getInstance(f).plot();
        } else {
            float[][] f = getSelectedCells();
            if (f.length == 1 && f[0].length == 1) {
                return;
            }
            if (data2D.length > 100 || data2D[0].length > 100) {
                if (FactoryUtils.confirmMessage("The matrix size is too big to visualize. Would you like to proceed?") == JOptionPane.YES_OPTION) {
                    CMatrix.getInstance(data2D).plot();
                }
            } else {
                CMatrix.getInstance(data2D).plot();
            }
        }
    }//GEN-LAST:event_btn_plotActionPerformed

    private void btn_scatterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_scatterActionPerformed
        if (isDataSelectedFromTable()) {
            float[][] f = getSelectedCells();
            CMatrix.getInstance(f).transpose().scatter();
        } else {
            float[][] f = getSelectedCells();
            if (f.length == 1 && f[0].length == 1) {
                return;
            }
            if (data2D.length > 100 || data2D[0].length > 100) {
                if (FactoryUtils.confirmMessage("The matrix size is too big to visualize. Would you like to proceed?") == JOptionPane.YES_OPTION) {
                    CMatrix.getInstance(data2D).transpose().scatter();
                }
            } else {
                CMatrix.getInstance(data2D).transpose().scatter();
            }
        }
    }//GEN-LAST:event_btn_scatterActionPerformed

    private void btn_transposeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_transposeActionPerformed
        float[][] f = null;
        if (isDataSelectedFromTable()) {
            f = getSelectedCells();
        } else {
            f = getSelectedCells();
            if (f.length == 1 && f[0].length == 1) {
                return;
            }
            f = tableToArray(getSelectedTable());
        }
        f = FactoryMatrix.transpose(f);
        setMatrix(f, getSelectedTable(),isInt);
    }//GEN-LAST:event_btn_transposeActionPerformed

    private void btn_from_textActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_from_textActionPerformed
        new FrameDataSetTextEditor().setVisible(true);
    }//GEN-LAST:event_btn_from_textActionPerformed

    private void btn_bar_plotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_bar_plotActionPerformed
        if (isDataSelectedFromTable()) {
            float[][] f = getSelectedCells();
            CMatrix.getInstance(f).transpose().bar();
        } else {
            float[][] f = getSelectedCells();
            if (f.length == 1 && f[0].length == 1) {
                return;
            }
            if (data2D.length > 100 || data2D[0].length > 100) {
                if (FactoryUtils.confirmMessage("The matrix size is too big to visualize. Would you like to proceed?") == JOptionPane.YES_OPTION) {
                    CMatrix.getInstance(data2D).transpose().bar();
                }
            } else {
                CMatrix.getInstance(data2D).transpose().bar();
            }
        }
    }//GEN-LAST:event_btn_bar_plotActionPerformed

    private void btn_heatmapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_heatmapActionPerformed
        if (isDataSelectedFromTable()) {
            if (dataType.equals("2D")) {
                float[][] f = getSelectedCells();
                CMatrix.getInstance(f).transpose().heatmap();
            } else {
                float[][][] f = getSelected3DCells();
                CMatrix.getInstance(f).transpose().heatmap();
            }
        } else {
            if (dataType.equals("2D")) {
                data2D = getSelectedTableData();
                CMatrix.getInstance(data2D).heatmap();
            } else {
                data3D = getTablesData();
                CMatrix.getInstance(data3D).heatmap();
            }

        }
        
    }//GEN-LAST:event_btn_heatmapActionPerformed

    public static float[][] tableToArray(JTable table) {
        int m = table.getRowCount();
        int n = table.getColumnCount();
        float[][] ret = new float[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                ret[i][j] = Float.parseFloat(table.getValueAt(i, j).toString());
            }
        }
        return ret;
    }

    public static void clearTable(JTable table) {
        while (table.getRowCount() > 0) {
            ((DefaultTableModel) table.getModel()).removeRow(0);
        }
    }

    private boolean isDataSelectedFromTable() {
        JTable tbl = getSelectedTable();
        int[] selectedRows = tbl.getSelectedRows();
        int[] selectedColumns = tbl.getSelectedColumns();
        if ((selectedRows.length == 0 && selectedColumns.length == 0) || (selectedRows.length == 1 && selectedColumns.length == 1)) {
            return false;
        } else {
            return true;
        }
    }

    private float[][] getSelectedCells() {
        JTable tbl = getSelectedTable();
        int[] selectedRows = tbl.getSelectedRows();
        int[] selectedColumns = tbl.getSelectedColumns();
        float[][] f = new float[selectedRows.length][selectedColumns.length];
        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < f[0].length; j++) {
                f[i][j] = Float.parseFloat("" + tbl.getValueAt(selectedRows[i], selectedColumns[j]));
            }
        }
        return f;
    }

    private float[][][] getSelected3DCells() {
        JTable tblSelected = getSelectedTable();
        int[] selectedRows = tblSelected.getSelectedRows();
        int[] selectedColumns = tblSelected.getSelectedColumns();
        float[][][] ret = new float[4][selectedRows.length][selectedColumns.length];
        for (int index = 0; index < 4; index++) {
            JTable tbl = getTable(index);
            for (int i = 0; i < selectedRows.length; i++) {
                for (int j = 0; j < selectedColumns.length; j++) {
                    ret[index][i][j] = Float.parseFloat("" + tbl.getValueAt(selectedRows[i], selectedColumns[j]));
                }
            }
        }
        return ret;
    }

    private String[] getColumnNames(float[][] data) {
        int colNumber = data[0].length;
        String[] columnNames = new String[colNumber];
        for (int i = 0; i < colNumber; i++) {
            columnNames[i] = "" + (i);
        }
        return columnNames;
    }

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
            java.util.logging.Logger.getLogger(FrameDataGrid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrameDataGrid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrameDataGrid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrameDataGrid.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //new FrameDataGrid().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_bar_plot;
    private javax.swing.JButton btn_from_text;
    private javax.swing.JButton btn_heatmap;
    private javax.swing.JButton btn_plot;
    private javax.swing.JButton btn_scatter;
    private javax.swing.JButton btn_transpose;
    private javax.swing.JButton btn_visualize;
    private javax.swing.JTable ds_table;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    private float[][] getSelectedTableData() {
        JTable tbl = getSelectedTable();
        float[][] f = new float[data2D.length][data2D[0].length];
        for (int i = 0; i < f.length; i++) {
            for (int j = 0; j < f[0].length; j++) {
                f[i][j] = Float.parseFloat("" + tbl.getValueAt(i, j));
            }
        }
        return f;
    }

    private float[][][] getTablesData() {
        float[][][] ret = new float[data3D.length][data3D[0].length][data3D[0][0].length];
        //ret[0]=FactoryMatrix.values(ret[0].length, ret[0][0].length, 255);
        for (int index = 0; index < ret.length; index++) {
            JTable tbl = getTable(index);
            for (int i = 0; i < ret[0].length; i++) {
                for (int j = 0; j < ret[0][0].length; j++) {
                    ret[index][i][j] = Float.parseFloat("" + tbl.getValueAt(i, j));
                }
            }
        }
        return ret;
    }

}
