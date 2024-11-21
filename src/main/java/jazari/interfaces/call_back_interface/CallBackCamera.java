/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package jazari.interfaces.call_back_interface;

import java.awt.image.BufferedImage;

/**
 *
 * @author cezerilab
 */
public interface CallBackCamera {
    BufferedImage onFrame(BufferedImage image);
}

