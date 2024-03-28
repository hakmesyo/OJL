/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.types;

import java.awt.Point;

/**
 *
 * @author dell_lab
 */
    // Sınırlayıcı kutu sınıfı
public class TBoundingBox {

        Point topLeft;
        Point bottomRight;

        public TBoundingBox(Point topLeft, Point bottomRight) {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
        }

        @Override
        public String toString() {
            return "BoundingBox{"
                    + "topLeft=" + topLeft
                    + ", bottomRight=" + bottomRight
                    + '}';
        }
    }
