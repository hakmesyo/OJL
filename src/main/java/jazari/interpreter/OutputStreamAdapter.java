/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.interpreter;

import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author cezerilab
 */
public class OutputStreamAdapter extends OutputStream {

    private JTextArea textArea;
    private StringBuilder buffer;

    public OutputStreamAdapter(JTextArea textArea) {
        this.textArea = textArea;
        this.buffer = new StringBuilder();
    }

    @Override
    public void write(int b) {
        buffer.append((char) b);
        if (b == '\n') {
            SwingUtilities.invokeLater(() -> {
                textArea.append(buffer.toString());
                buffer.setLength(0);
            });
        }
    }

    @Override
    public void flush() {
        if (buffer.length() > 0) {
            SwingUtilities.invokeLater(() -> {
                textArea.append(buffer.toString());
                buffer.setLength(0);
            });
        }
    }
}
