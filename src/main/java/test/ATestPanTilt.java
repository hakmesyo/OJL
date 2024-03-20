/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.Arrays;

/**
 *
 * @author cezerilab
 */
public class ATestPanTilt {
    public static void main(String[] args) {
//        double[] panTiltAngles = getPanTiltAngles(40, 20, 20, 10, 10);
//        System.out.println("(" + 10 + ":" + 10 + ") pan,tilt:" + Arrays.toString(panTiltAngles));
        for (int i = 0; i <= 20; i++) {
            for (int j = 0; j <= 20; j++) {
                double[] panTiltAngles = getPanTiltAngles(20, 20, 20, i, j);
                System.out.println("(" + i + ":" + j + ") pan,tilt:" + Arrays.toString(panTiltAngles));
            }
        }
    }

    private static double[] getPanTiltAngles(
            int height,
            int regionWidth,
            int regionHeight,
            int targetX,
            int targetY) {
        double panAngle = Math.atan2(targetX - regionWidth / 2, height);
        double tiltAngle = Math.atan2(targetY - regionHeight / 2, regionWidth / 2);
        panAngle = panAngle * 180.0 / Math.PI;
        tiltAngle = tiltAngle * 180.0 / Math.PI;
        return new double[]{panAngle, tiltAngle};
    }
}
