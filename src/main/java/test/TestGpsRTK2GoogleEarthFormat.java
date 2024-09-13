/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import jazari.factory.FactoryUtils;

/**
 *
 * @author cezerilab
 */
public class TestGpsRTK2GoogleEarthFormat {
    public static void main(String[] args) {
        String s=FactoryUtils.gpsConvertRtk2LatLongString("3757.8019432,N,04151.0025488,E");
        System.out.println("s = " + s);
    }
}
