package jazari.llm_forge;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class CustomProviderDialog extends JDialog {
    private JTextField providerNameField;
    private JTextField apiUrlField;
    private JTextArea modelsTextArea;
    private JButton saveButton;
    private JButton cancelButton;
    private boolean confirmed = false;
    
    public CustomProviderDialog(JFrame parent) {
        super(parent, "Add Custom Provider", true);
        initializeUI();
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Provider name
        JPanel namePanel = new JPanel(new BorderLayout(10, 0));
        JLabel nameLabel = new JLabel("Provider Name:");
        providerNameField = new JTextField(20);
        namePanel.add(nameLabel, BorderLayout.WEST);
        namePanel.add(providerNameField, BorderLayout.CENTER);
        
        // API URL
        JPanel urlPanel = new JPanel(new BorderLayout(10, 0));
        JLabel urlLabel = new JLabel("API URL:");
        apiUrlField = new JTextField(20);
        urlPanel.add(urlLabel, BorderLayout.WEST);
        urlPanel.add(apiUrlField, BorderLayout.CENTER);
        
        // Models list
        JPanel modelsPanel = new JPanel(new BorderLayout(10, 0));
        JLabel modelsLabel = new JLabel("Models (one per line):");
        modelsTextArea = new JTextArea(5, 20);
        JScrollPane modelsScroll = new JScrollPane(modelsTextArea);
        modelsPanel.add(modelsLabel, BorderLayout.NORTH);
        modelsPanel.add(modelsScroll, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> saveAndClose());
        cancelButton.addActionListener(e -> dispose());
        
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);
        
        // Add panels
        mainPanel.add(namePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(urlPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(modelsPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        pack();
        setMinimumSize(new Dimension(400, 300));
    }
    
    private void saveAndClose() {
        // Validate
        if (providerNameField.getText().trim().isEmpty() ||
            apiUrlField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Provider name and API URL cannot be empty.", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        confirmed = true;
        dispose();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    public String getProviderName() {
        return providerNameField.getText().trim();
    }
    
    public String getApiUrl() {
        return apiUrlField.getText().trim();
    }
    
    public List<String> getModels() {
        List<String> models = new ArrayList<>();
        String[] lines = modelsTextArea.getText().split("\\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                models.add(line.trim());
            }
        }
        return models;
    }
    
    public static CustomProviderDialog showDialog(JFrame parent) {
        CustomProviderDialog dialog = new CustomProviderDialog(parent);
        dialog.setVisible(true);
        return dialog;
    }
}