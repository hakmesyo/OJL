/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

/**
 *
 * @author cezerilab
 */
import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

public class TestLatLongToUTM {

    private static final double SEMI_MAJOR_AXIS = 6378137.0; // Dünya'nın yarı büyük ekseni (metre cinsinden)
    private static final double ECCENTRICITY_SQUARED = 0.00669437999014; // Dünya'nın dış merkezlik karesi

    public static void main(String[] args) {
        // Giriş LatLong koordinatları
        double latitude = 37.9633970700379;
        double longitude = 41.85015192791282;

        // Hedef UTM bölgesi Siirt
        int utmZone = 37;
        // Hedef UTM bölgesi Gebze
        //int utmZone = 35;
        char utmHemisphere = 'N';

        // Koordinat dönüşümü
        double[] utmCoordinates = convertLatLongToUTM(latitude, longitude, utmZone, utmHemisphere);

        // Dönüştürülmüş UTM koordinatları
        System.out.println("UTM Koordinatları: " + utmCoordinates[0] + " " + utmCoordinates[1]);
    }

    public static double[] convertLatLongToUTM(double latitude, double longitude, int utmZone, char utmHemisphere) {
        // Meridyen konumu
        double centralMeridian = (utmZone * 6 - 183) * Math.PI / 180;

        // Boylamı merkezi meridyenle eşle
        double longitudeRad = longitude * Math.PI / 180;
        double lambda0Rad = centralMeridian;
        double lambdaRad = longitudeRad - lambda0Rad;

        // Geodesi hesaplamaları
        double e2 = ECCENTRICITY_SQUARED;
        double k0 = 0.9996; // UTM ölçek faktörü
        double a = SEMI_MAJOR_AXIS;
        double eccSquared = e2;
        double eccPrimeSquared = (eccSquared) / (1 - eccSquared);

        // Enlem radyan cinsinden
        double phiRad = latitude * Math.PI / 180;
        double N = a / Math.sqrt(1 - eccSquared * Math.sin(phiRad) * Math.sin(phiRad));
        double T = Math.tan(phiRad) * Math.tan(phiRad);
        double C = eccPrimeSquared * Math.cos(phiRad) * Math.cos(phiRad);
        double A = (lambdaRad * Math.cos(phiRad)) / (1 + C);
        double M = a * ((1 - eccSquared / 4 - 3 * eccSquared * eccSquared / 64 - 5 * eccSquared * eccSquared * eccSquared / 256) * phiRad
                - (3 * eccSquared / 8 + 3 * eccSquared * eccSquared / 32 + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(2 * phiRad)
                + (15 * eccSquared * eccSquared / 256 + 45 * eccSquared * eccSquared * eccSquared / 1024) * Math.sin(4 * phiRad)
                - (35 * eccSquared * eccSquared * eccSquared / 3072) * Math.sin(6 * phiRad));

        // UTM koordinatlarını hesapla
        double utmEasting = k0 * (N * Math.sin(A) + A * N * T * Math.cos(A) * Math.cos(A) / 2 + (A * A * A * N * T * T * Math.sin(A) * Math.cos(A) * Math.cos(A) / 24)
                - (A * A * A * A * N * T * T * T * Math.cos(A) * Math.cos(A) * Math.cos(A) * Math.cos(A) / 72));
        double utmNorthing = k0 * (M + N * Math.cos(A) * Math.cos(A) / 2 + (A * A * N * T * Math.sin(A) * Math.cos(A) / 24)
                - (A * A * A * A * N * T * T * Math.sin(A) * Math.sin(A) * Math.cos(A) * Math.cos(A) / 72));

        // Kuzey yarımkürede, UTM Kuzey için 5000000 ekle
        if (utmHemisphere == 'N') {
            utmNorthing += 5000000;
        }

        // UTM Bölge numarasını ekle
        double utmZoneNumber = utmZone;
        double utmEastingZone = utmEasting + utmZoneNumber * 100000;

        // Dönüştürülmüş UTM koordinatlarını döndür
        return new double[] { utmEastingZone, utmNorthing };
    }
}