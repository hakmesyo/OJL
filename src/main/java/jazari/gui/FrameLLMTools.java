/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package jazari.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main frame for LLM-related tools including Java source code collector.
 *
 * @author cezerilab
 */
public class FrameLLMTools extends javax.swing.JFrame {

    static {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(FrameLLMTools.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates new form FrameMainLLM
     */
    public FrameLLMTools() {
        initComponents();
        logArea.setDoubleBuffered(true);
    }

    /**
     * Enable or disable UI components
     *
     * @param enabled true to enable components, false to disable
     */
    private void setComponentsEnabled(boolean enabled) {
        browseProjectButton.setEnabled(enabled);
        browseOutputButton.setEnabled(enabled);
        startButton.setEnabled(enabled);
        projectPathField.setEnabled(enabled);
        outputFileField.setEnabled(enabled);
    }

    /**
     * Main code collection logic
     *
     * @param projectPath Path to the project directory
     * @param outputFile Path to the output file
     * @throws IOException If there are issues reading/writing files
     */
    private void collectJavaCode(String projectPath, String outputFile) throws IOException {
        logToUI("Scanning for Java files: " + projectPath);

        // Find Java files
        List<Path> javaFiles = findJavaFiles(projectPath);

        if (javaFiles.isEmpty()) {
            logToUI("No Java files found in the specified directory!");
            return;
        }

        logToUI("Found " + javaFiles.size() + " Java files in total.");

        // Combine files
        logToUI("Combining files...");
        combineJavaFiles(javaFiles, outputFile);

        logToUI("Operation completed! Output file: " + outputFile);

        SwingUtilities.invokeLater(() -> {
            int result = JOptionPane.showConfirmDialog(this,
                    "Operation completed successfully!\nDo you want to open the output file now?",
                    "Operation Completed", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                try {
                    Desktop.getDesktop().open(new File(outputFile));
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(this, "Could not open file: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /**
     * Log message to the UI
     *
     * @param message Message to display
     */
    private void logToUI(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto scroll
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Find all Java files in the given directory and its subdirectories
     *
     * @param directory Root directory to search from
     * @return List of Java file paths
     * @throws IOException If directory cannot be read
     */
    private List<Path> findJavaFiles(String directory) throws IOException {
        Path startPath = Paths.get(directory);

        if (!Files.exists(startPath)) {
            throw new IOException("Specified directory not found: " + directory);
        }

        try (Stream<Path> pathStream = Files.walk(startPath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".java"))
                    .filter(path -> !isInBuildOrTarget(path)) // Exclude build and target folders
                    .collect(Collectors.toList());
        }
    }

    /**
     * Check if a path is within build or target directories
     *
     * @param path Path to check
     * @return true if path is in build or target directory
     */
    private boolean isInBuildOrTarget(Path path) {
        String pathStr = path.toString();
        return pathStr.contains(File.separator + "build" + File.separator)
                || pathStr.contains(File.separator + "target" + File.separator);
    }

    /**
     * Combine multiple Java files into one output file
     *
     * @param javaFiles List of Java file paths
     * @param outputFilePath Path to the output file
     * @throws IOException If there are issues reading/writing files
     */
    private void combineJavaFiles(List<Path> javaFiles, String outputFilePath) throws IOException {
        // Create a StringBuilder to store content for both file and UI
        StringBuilder contentBuilder = new StringBuilder();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            int count = 0;
            for (Path javaFile : javaFiles) {
                count++;
                if (count % 10 == 0) {
                    logToUI("Processing: " + count + "/" + javaFiles.size() + " files");
                }

                // Add file info to both StringBuilder and output file
                String fileHeader = "============================================================";
                String filePath = "FILE: " + javaFile.toString();
                String fileFooter = "============================================================";

                writer.write(fileHeader);
                writer.newLine();
                writer.write(filePath);
                writer.newLine();
                writer.write(fileFooter);
                writer.newLine();

                contentBuilder.append(fileHeader).append("\n");
                contentBuilder.append(filePath).append("\n");
                contentBuilder.append(fileFooter).append("\n");

                // Read and write file content
                try (BufferedReader reader = new BufferedReader(new FileReader(javaFile.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                        contentBuilder.append(line).append("\n");
                    }
                }

                // Add space between files
                writer.newLine();
                writer.newLine();
                contentBuilder.append("\n\n");
            }
        }

        // Display content in the logArea after all processing is complete
        logToUI("\n--- CREATED FILE CONTENT ---\n");
        logToUI(contentBuilder.toString());
        logToUI("\n--- END OF FILE CONTENT ---\n");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        projectPathField = new javax.swing.JTextField();
        browseProjectButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        outputFileField = new javax.swing.JTextField();
        browseOutputButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        logArea = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("LLM Tools");

        jLabel1.setText("Project Directory:");

        browseProjectButton.setText("Browse");
        browseProjectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseProjectButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Output File:");

        browseOutputButton.setText("Browse");
        browseOutputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseOutputButtonActionPerformed(evt);
            }
        });

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        logArea.setEditable(false);
        logArea.setColumns(20);
        logArea.setRows(5);
        jScrollPane1.setViewportView(logArea);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(projectPathField)
                                    .addComponent(outputFileField))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(browseOutputButton)
                                    .addComponent(browseProjectButton))))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(projectPathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseProjectButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(outputFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseOutputButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Source Code Collector", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 686, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 431, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Other Features", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void browseProjectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseProjectButtonActionPerformed
        // Start from current working directory
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Project Directory");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            projectPathField.setText(selectedFile.getAbsolutePath());

            // Suggest automatic output file name
            if (outputFileField.getText().isEmpty()) {
                String defaultOutput = selectedFile.getAbsolutePath() + File.separator
                        + "collected_java_code.txt";
                outputFileField.setText(defaultOutput);
            }
        }
    }//GEN-LAST:event_browseProjectButtonActionPerformed

    private void browseOutputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseOutputButtonActionPerformed
        // Start from current working directory
        JFileChooser fileChooser = new JFileChooser(new File("."));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select Output File");

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".txt")) {
                path += ".txt";
            }
            outputFileField.setText(path);
        }
    }//GEN-LAST:event_browseOutputButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        String projectPath = projectPathField.getText().trim();
        String outputFile = outputFileField.getText().trim();

        if (projectPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select project directory!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (outputFile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify output file!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable interface
        setComponentsEnabled(false);
        logArea.setText("");

        // Run in background
        new Thread(() -> {
            try {
                collectJavaCode(projectPath, outputFile);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("ERROR: " + e.getMessage() + "\n");
                    JOptionPane.showMessageDialog(this, "An error occurred during operation: "
                            + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                SwingUtilities.invokeLater(() -> setComponentsEnabled(true));
            }
        }).start();
    }//GEN-LAST:event_startButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrameLLMTools frm = new FrameLLMTools();
                frm.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseOutputButton;
    private javax.swing.JButton browseProjectButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea logArea;
    private javax.swing.JTextField outputFileField;
    private javax.swing.JTextField projectPathField;
    private javax.swing.JButton startButton;
    // End of variables declaration//GEN-END:variables
}
