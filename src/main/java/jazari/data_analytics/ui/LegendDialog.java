package jazari.data_analytics.ui;

import jazari.data_analytics.core.LegendItem;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

public class LegendDialog extends JDialog {

    private JTable legendTable;
    private DefaultTableModel tableModel;
    private List<LegendItem> legendItems;
    private boolean isCancelled = true;
    private Map<LegendItem, LegendItem> changes = new HashMap<>();

    public LegendDialog(Frame owner, String title, List<LegendItem> legendItems) {
        super(owner, title, true);
        this.legendItems = new ArrayList<>(legendItems); // Deep copy for safety
        initUI();

    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Table Model
        String[] columnNames = {"Label", "Color"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Legend items'ı tabloya ekle
        for (LegendItem item : legendItems) {
            tableModel.addRow(new Object[]{item.getLabel(), item.getColor()});
        }

        // Table
        legendTable = new JTable(tableModel);
        legendTable.getColumnModel().getColumn(1).setCellRenderer(new ColorRenderer());
        legendTable.getColumnModel().getColumn(1).setCellEditor(new ColorEditor());
        legendTable.getColumnModel().getColumn(0).setCellEditor(new LabelEditor());
        add(new JScrollPane(legendTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        // OK Button
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            updateLegendItems();
            isCancelled = false;
            dispose();
        });
        buttonPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            dispose();
        });
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(400, 300));
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();

    }

    private void updateLegendItems() {
        legendItems.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String label = (String) tableModel.getValueAt(i, 0);
            Color color = (Color) tableModel.getValueAt(i, 1);
            legendItems.add(new LegendItem(label, color));
        }
    }

    public List<LegendItem> getLegendItems() {
        if (isCancelled) {
            return null;
        }
        return legendItems;
    }

    public Map<LegendItem, LegendItem> getChanges() {
        if (isCancelled) {
            return null;
        }
        return changes;
    }

    // Custom renderer
    class ColorRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color color = (Color) value;
            setBackground(color);
            setOpaque(true);
            setText(" "); // Renk hücresinin metin içermemesini sağla
            return this;
        }
    }

    // Custom editor
    class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

        private Color currentColor;
        private JButton button;
        private JColorChooser colorChooser;
        private JDialog colorDialog;
        private LegendItem originalItem;

        @Override
        public Object getCellEditorValue() {
            return currentColor;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentColor = (Color) value;
            button = new JButton();
            button.setBackground(currentColor);
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.addActionListener(this);
            originalItem = legendItems.get(row);
            return button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            button = (JButton) e.getSource();
            colorChooser = new JColorChooser(currentColor);
            colorDialog = JColorChooser.createDialog(button, "Choose a Color", true, colorChooser,
                    (actionEvent) -> {
                        if (actionEvent.getActionCommand().equals("OK")) {
                            Color newColor = colorChooser.getColor();
                            if (!newColor.equals(originalItem.getColor())) {
                                changes.put(originalItem, new LegendItem(originalItem.getLabel(), newColor));
                            }
                            currentColor = newColor;
                            fireEditingStopped();
                        } else {
                            fireEditingCanceled();
                        }

                    }, null);

            colorDialog.setVisible(true);

        }
    }

    class LabelEditor extends DefaultCellEditor {

        private JTextField textField;
        private LegendItem originalItem;

        public LabelEditor() {
            super(new JTextField());
            this.textField = (JTextField) getComponent();
            textField.addActionListener(e -> {
                fireEditingStopped();
            });
            addCellEditorListener(new CellEditorListener() {
                @Override
                public void editingStopped(ChangeEvent e) {
                    String newLabel = textField.getText();
                    int row = legendTable.getSelectedRow();
                    LegendItem item = legendItems.get(row);
                    if (!item.getLabel().equals(newLabel)) {
                        changes.put(item, new LegendItem(newLabel, item.getColor()));
                    }
                }

                @Override
                public void editingCanceled(ChangeEvent e) {

                }
            });

        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            originalItem = legendItems.get(row);
            textField.setText(value.toString());
            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

        @Override
        public Object getCellEditorValue() {
            return textField.getText();
        }
    }
}
