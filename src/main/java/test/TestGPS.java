/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.google.maps.model.LatLng;
import java.awt.geom.Point2D;
import jazari.factory.FactoryUtils;

//https://coordinates-converter.com/en/decimal/51.000000,10.000000?karte=OpenStreetMap&zoom=8

/**
 *
 * @author dell_lab
 */
public class TestGPS {
    public static void main(String[] args) {
//        aşağıdaki data gebze parkur alanı kml si
        double[][] gpsData = {
            {29.50911482597872, 40.79043673267945},
            {29.50836199373552, 40.78993939708132},
            {29.5088691017504, 40.78949094745692},
            {29.50965094265705, 40.79000447472892}
        };
        LatLng from=new LatLng(gpsData[0][1],gpsData[0][0]);
        LatLng to=new LatLng(gpsData[1][1],gpsData[1][0]);
        double distance=FactoryUtils.gpsDistance(from, to);
        System.out.println("distance = " + distance);
        double angle=FactoryUtils.gpsHeadingAngle(from, to);
        System.out.println("angle = " + angle);
    }
}
