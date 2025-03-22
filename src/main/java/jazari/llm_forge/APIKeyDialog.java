package jazari.llm_forge;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIKeyDialog extends JDialog {
    
    private JTextField apiKeyField;
    private JPasswordField apiKeyPasswordField;
    private JCheckBox showApiKeyCheckbox;
    private JButton saveButton;
    private JButton cancelButton;
    private boolean confirmed = false;
    private Map<String, String> result;
    
    public APIKeyDialog(JFrame parent, String providerName, List<String> requiredFields) {
        super(parent, "API Key for " + providerName, true);
        
        result = new HashMap<>();
        
        initializeUI(providerName, requiredFields);
        
        // Center dialog relative to parent
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI(String providerName, List<String> requiredFields) {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Add information label
        JLabel infoLabel = new JLabel("Please enter your " + providerName + " API key:");
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(infoLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Create a panel for each required field
        for (String field : requiredFields) {
            JPanel fieldPanel = createFieldPanel(field);
            fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            mainPanel.add(fieldPanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        // "Show API Key" checkbox
        showApiKeyCheckbox = new JCheckBox("Show API Key");
        showApiKeyCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showApiKeyCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePasswordVisibility();
            }
        });
        mainPanel.add(showApiKeyCheckbox);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAndClose();
            }
        });
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);
        
        // Add panels to dialog
        add(mainPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        // Set default button
        getRootPane().setDefaultButton(saveButton);
        
        // Set size and make visible
        pack();
        setMinimumSize(new Dimension(400, 200));
    }
    
    private JPanel createFieldPanel(String fieldName) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        
        JLabel label = new JLabel(fieldName + ":");
        panel.add(label, BorderLayout.WEST);
        
        if (fieldName.toLowerCase().contains("key") || 
            fieldName.toLowerCase().contains("password") || 
            fieldName.toLowerCase().contains("secret")) {
            
            // Use password field for sensitive information
            apiKeyPasswordField = new JPasswordField(30);
            panel.add(apiKeyPasswordField, BorderLayout.CENTER);
            
            // Store reference to the main API key field
            if (apiKeyField == null) {
                apiKeyField = new JTextField(30);
                apiKeyField.setVisible(false);
            }
        } else {
            // Use regular text field for non-sensitive information
            JTextField textField = new JTextField(30);
            panel.add(textField, BorderLayout.CENTER);
            
            // Add to result map on change
            textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    result.put(fieldName, textField.getText());
                }
                
                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    result.put(fieldName, textField.getText());
                }
                
                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    result.put(fieldName, textField.getText());
                }
            });
        }
        
        return panel;
    }
    
    private void togglePasswordVisibility() {
        if (showApiKeyCheckbox.isSelected()) {
            // Transfer content from password field to text field
            apiKeyField.setText(new String(apiKeyPasswordField.getPassword()));
            
            // Swap visibility
            apiKeyPasswordField.setVisible(false);
            apiKeyField.setVisible(true);
            
            // Replace password field with text field in UI
            Container parent = apiKeyPasswordField.getParent();
            parent.remove(apiKeyPasswordField);
            parent.add(apiKeyField, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();
        } else {
            // Transfer content from text field to password field
            apiKeyPasswordField.setText(apiKeyField.getText());
            
            // Swap visibility
            apiKeyField.setVisible(false);
            apiKeyPasswordField.setVisible(true);
            
            // Replace text field with password field in UI
            Container parent = apiKeyField.getParent();
            parent.remove(apiKeyField);
            parent.add(apiKeyPasswordField, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();
        }
    }
    
    private void saveAndClose() {
        // Get API key value
        String apiKey = showApiKeyCheckbox.isSelected() 
                ? apiKeyField.getText() 
                : new String(apiKeyPasswordField.getPassword());
        
        // Validate API key
        if (apiKey == null || apiKey.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "API key cannot be empty.", 
                    "Invalid API Key", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Add to result
        result.put("apiKey", apiKey);
        
        // Set confirmed flag and close
        confirmed = true;
        dispose();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public Map<String, String> getResult() {
        return result;
    }
    
    public static Map<String, String> showDialog(JFrame parent, String providerName, List<String> requiredFields) {
        APIKeyDialog dialog = new APIKeyDialog(parent, providerName, requiredFields);
        dialog.setVisible(true);
        
        // Wait for dialog to close
        if (dialog.isConfirmed()) {
            return dialog.getResult();
        }
        
        return null;
    }
}