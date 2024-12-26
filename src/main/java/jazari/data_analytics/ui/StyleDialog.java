package jazari.data_analytics.ui;

import jazari.data_analytics.core.Style;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class StyleDialog extends JDialog {

    private Style style;
    private JComboBox<String> fontComboBox;
    private JSpinner fontSizeSpinner;
    private JCheckBox boldCheckBox;
    private JCheckBox italicCheckBox;
    private JButton okButton;
    private boolean isCancelled = true;
    private JTextField textInput;

    public StyleDialog(Frame owner, String title, Style style, String text) {
        super(owner, title, true);
        this.style = style;
        initUI(text);
    }

    private void initUI(String text) {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Text Input Label
        add(new JLabel("Text:"), gbc);
        gbc.gridx++;
        // Text Input Field
        textInput = new JTextField(text, 20);
        add(textInput, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        // Font Label
        add(new JLabel("Font:"), gbc);
        gbc.gridx++;

        // Font Dropdown
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        fontComboBox = new JComboBox<>(fontNames);
        fontComboBox.setSelectedItem(style.getFont().getFamily());
        add(fontComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Font Size Label
        add(new JLabel("Font Size:"), gbc);
        gbc.gridx++;

        // Font Size Spinner
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(style.getFontSize(), 8, 72, 1));
        add(fontSizeSpinner, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Bold Checkbox
        add(new JLabel("Bold:"), gbc);
        gbc.gridx++;
        boldCheckBox = new JCheckBox();
        boldCheckBox.setSelected(style.getFont().isBold());
        add(boldCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // Italic Checkbox
        add(new JLabel("Italic:"), gbc);
        gbc.gridx++;
        italicCheckBox = new JCheckBox();
        italicCheckBox.setSelected(style.getFont().isItalic());
        add(italicCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        // OK button
        okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            applyChanges();
            isCancelled = false;
            dispose();
        });
        gbc.gridx = 1;
        add(okButton, gbc);
        gbc.gridx++;
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            dispose();
        });
        add(cancelButton, gbc);
        pack();
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void applyChanges() {
        int fontStyle = Font.PLAIN;
        if (boldCheckBox.isSelected()) {
            fontStyle |= Font.BOLD;
        }
        if (italicCheckBox.isSelected()) {
            fontStyle |= Font.ITALIC;
        }

        Font selectedFont = new Font((String) fontComboBox.getSelectedItem(), fontStyle, (Integer) fontSizeSpinner.getValue());
        style.setFont(selectedFont);
        style.setFontSize((Integer) fontSizeSpinner.getValue());

    }

    public Style getStyle() {
        if (isCancelled) {
            return null;
        }
        return style;
    }

    public String getEditedText() {
        if (isCancelled) {
            return null;
        }
        return textInput.getText();
    }
}
