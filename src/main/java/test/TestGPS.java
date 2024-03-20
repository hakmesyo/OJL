/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.awt.geom.Point2D;
import jazari.factory.FactoryUtils;

//https://coordinates-converter.com/en/decimal/51.000000,10.000000?karte=OpenStreetMap&zoom=8

/**
 *
 * @author dell_lab
 */
public class TestGPS {
    public static void main(String[] args) {
        Point2D.Double p=FactoryUtils.gpsToDecimalCoordinate("37:57:43.5852:N","41:51:4.5684:E");
        System.out.println("p = " + p);
        
        //iki gps noktasına göre yön tayini
        Double angle=FactoryUtils.getDirectionFromGPSPoints(
                FactoryUtils.gpsToDecimalCoordinate("37:57:42.38:N","41:51:11.54:E"), 
                //FactoryUtils.GpsToDecimalCoordinate("37:57:42.26:N","41:51:1.59:E"));
                //FactoryUtils.GpsToDecimalCoordinate("37:57:47.25:N","41:51:11.53:E")); //359 derece
                //FactoryUtils.GpsToDecimalCoordinate("37:57:42.38:N","41:51:11.55:E")); //90 derece
                FactoryUtils.gpsToDecimalCoordinate("37:57:42.38:N","41:51:11.53:E")); //270 derece
        System.out.println("angle = " + angle);
    }
}
