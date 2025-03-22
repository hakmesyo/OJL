package jazari.llm_forge;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventLogger {
    
    private JFrame logFrame;
    private JTextArea logArea;
    private SimpleDateFormat timeFormat;
    private boolean isVisible = false;
    
    public EventLogger() {
        timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        initializeUI();
    }
    
    private void initializeUI() {
        logFrame = new JFrame("Event Logger");
        logFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        logFrame.setSize(800, 400);
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(200, 200, 200));
        
        // Set auto-scroll behavior
        DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        
        // Create toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearLog());
        
        JButton exportButton = new JButton("Export");
        exportButton.addActionListener(e -> exportLog());
        
        toolbar.add(clearButton);
        toolbar.add(exportButton);
        
        // Add components to frame
        logFrame.getContentPane().add(toolbar, BorderLayout.NORTH);
        logFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        // Center on screen
        logFrame.setLocationRelativeTo(null);
    }
    
    public void log(String message) {
        String timestamp = timeFormat.format(new Date());
        String logEntry = timestamp + " | " + message + "\n";
        
        // Add to log area (thread-safe)
        SwingUtilities.invokeLater(() -> {
            Document doc = logArea.getDocument();
            try {
                doc.insertString(doc.getLength(), logEntry, null);
                
                // Limit log size to prevent memory issues
                if (doc.getLength() > 1000000) {  // ~1MB of text
                    doc.remove(0, doc.getLength() - 500000);  // Keep last 500K
                    logArea.setCaretPosition(doc.getLength());
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
        
        // Also print to console for debugging
        System.out.println("[LOG] " + logEntry.trim());
    }
    
    public void toggleVisibility() {
        isVisible = !isVisible;
        logFrame.setVisible(isVisible);
        
        if (isVisible) {
            logFrame.toFront();
        }
    }
    
    private void clearLog() {
        logArea.setText("");
        log("Log cleared");
    }
    
    private void exportLog() {
        // Create log file save dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Log File");
        
        // Set default filename
        SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String defaultName = "chatforge_log_" + fileFormat.format(new Date()) + ".txt";
        fileChooser.setSelectedFile(new java.io.File(defaultName));
        
        int userChoice = fileChooser.showSaveDialog(logFrame);
        
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                
                // Append .txt if not present
                if (!file.getName().toLowerCase().endsWith(".txt")) {
                    file = new java.io.File(file.getAbsolutePath() + ".txt");
                }
                
                // Write log content to file
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(logArea.getText());
                writer.close();
                
                log("Log exported to: " + file.getAbsolutePath());
                
                // Show success message
                JOptionPane.showMessageDialog(
                        logFrame,
                        "Log saved to:\n" + file.getAbsolutePath(),
                        "Log Saved",
                        JOptionPane.INFORMATION_MESSAGE);
                
            } catch (Exception e) {
                log("Error exporting log: " + e.getMessage());
                JOptionPane.showMessageDialog(
                        logFrame,
                        "Error saving log: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void showAndPosition(Component relativeTo) {
        if (relativeTo != null) {
            // Position dialog relative to parent component
            Point location = relativeTo.getLocationOnScreen();
            int x = location.x + 50;
            int y = location.y + 50;
            logFrame.setLocation(x, y);
        }
        
        logFrame.setVisible(true);
        isVisible = true;
        logFrame.toFront();
    }
    
    public boolean isVisible() {
        return isVisible;
    }
}