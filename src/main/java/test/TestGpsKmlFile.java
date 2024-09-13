/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.Arrays;
import java.util.List;
import jazari.factory.FactoryUtils;

/**
 *
 * @author Teknofest
 */
public class TestGpsKmlFile {

    public static void main(String[] args) {
        List<double[]> coordinates = List.of(
                new double[]{37.96327558660128, 41.8501618702164},
                new double[]{37.96329006242495, 41.8501530187302},
                new double[]{37.96330404854646, 41.85014398695938},
                new double[]{37.96331128878102, 41.85013339225093},
                new double[]{37.96331543272915, 41.85012029102375}
        );

        FactoryUtils.gpsGenerateKMLFromDoubleLatLong(coordinates, "dataset/output_jak.kml");
        
        List<double[]> map_1=FactoryUtils.gpsGetGPSPointsFromKMLAsDouble("dataset/output_jak.kml");
        for (double[] ds : map_1) {
            System.out.println("ds = " + Arrays.toString(ds));
        }
    }
}
