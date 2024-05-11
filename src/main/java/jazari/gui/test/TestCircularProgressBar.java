/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.gui.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import jazari.gui.FrameCircularProgressBar;


/**
 *
 * @author cezerilab
 */
public class TestCircularProgressBar {
    public static void main(String[] args) {
        FrameCircularProgressBar frm=new FrameCircularProgressBar();
        frm.setVisible(true);
        for (int i = 0; i <= 100; i++) {
            frm.setValue(i);
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(TestCircularProgressBar.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //frm.dispose();
        
    }
}
