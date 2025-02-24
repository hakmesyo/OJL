/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.utils;

import java.time.LocalDateTime;

/**
 *
 * @author cezerilab
 */
/**
 * Calculates solar position based on time and location.
 * This class handles all astronomical calculations for solar tracking.
 */
public class SolarCalculator {
    private double latitude;  // in radians
    private double longitude; // in radians
    private double altitude; // in meters
    
    private static final double SOLAR_CONSTANT = 1361.0; // W/m²
    
    public SolarCalculator(double latitudeDegrees, double longitudeDegrees, double altitude) {
        updateLocation(latitudeDegrees, longitudeDegrees);
        this.altitude = altitude;
    }
    
    public void updateLocation(double latitudeDegrees, double longitudeDegrees) {
        this.latitude = Math.toRadians(latitudeDegrees);
        this.longitude = Math.toRadians(longitudeDegrees);
    }
    
    public SolarPosition calculateSolarPosition(LocalDateTime dateTime) {
        //System.out.println("Calculating solar position for: " + dateTime);
        int dayOfYear = dateTime.getDayOfYear();
        
        // Calculate solar declination angle (Spencer formula)
        double B = 2 * Math.PI * (dayOfYear - 1) / 365.0;
        double declination = Math.toDegrees(0.006918 - 0.399912 * Math.cos(B) + 0.070257 * Math.sin(B) 
                            - 0.006758 * Math.cos(2*B) + 0.000907 * Math.sin(2*B) 
                            - 0.002697 * Math.cos(3*B) + 0.001480 * Math.sin(3*B));
        
        // Calculate equation of time
        double E = 229.18 * (0.000075 + 0.001868 * Math.cos(B) - 0.032077 * Math.sin(B) 
                            - 0.014615 * Math.cos(2*B) - 0.040849 * Math.sin(2*B));
        
        // Calculate hour angle
        double hour = dateTime.getHour() + dateTime.getMinute()/60.0;
        double localLongitude = 45.0; // UTC+3 meridian
        double timeCorrection = 4 * (Math.toDegrees(longitude) - localLongitude) + E;
        double solarTime = hour + timeCorrection/60.0;
        double hourAngle = 15.0 * (solarTime - 12.0);
        
        // Calculate altitude angle
        double sinDeclination = Math.sin(Math.toRadians(declination));
        double cosDeclination = Math.cos(Math.toRadians(declination));
        double sinLatitude = Math.sin(latitude);
        double cosLatitude = Math.cos(latitude);
        double sinHourAngle = Math.sin(Math.toRadians(hourAngle));
        double cosHourAngle = Math.cos(Math.toRadians(hourAngle));
        
        double sinAltitude = sinLatitude * sinDeclination + 
                            cosLatitude * cosDeclination * cosHourAngle;
        double altitudeAngle = Math.toDegrees(Math.asin(sinAltitude));
        
        // Calculate azimuth angle
        double cosAzimuth = (sinDeclination * cosLatitude - 
                            cosDeclination * sinLatitude * cosHourAngle) / 
                            Math.cos(Math.toRadians(altitudeAngle));
        double azimuthAngle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, cosAzimuth))));
        
        if (hourAngle > 0) {
            azimuthAngle = 360 - azimuthAngle;
        }
        
        // Apply atmospheric corrections
        double airMass = calculateAirMass(altitudeAngle);
        double atmosphericRefraction = calculateAtmosphericRefraction(altitudeAngle);
        altitudeAngle += atmosphericRefraction / 3600.0; // Convert arcseconds to degrees
        
        // Calculate solar intensity with atmospheric effects
        double solarIntensity = SOLAR_CONSTANT * Math.pow(0.7, Math.pow(airMass, 0.678));
        
        return new SolarPosition(altitudeAngle, azimuthAngle, solarIntensity);
    }
    
    private double calculateAirMass(double altitude) {
        double zenith = 90 - altitude;
        double cosZenith = Math.cos(Math.toRadians(zenith));
        return 1 / (cosZenith + 0.50572 * Math.pow(96.07995 - zenith, -1.6364));
    }
    
    private double calculateAtmosphericRefraction(double altitude) {
        if (altitude > 85.0) return 0;
        
        double te = Math.tan(Math.toRadians(altitude));
        if (altitude > 5.0) {
            return 58.1 / te - 0.07 / (te * te * te) + 0.000086 / Math.pow(te, 5);
        }
        if (altitude > -0.575) {
            return 1735.0 + altitude * (-518.2 + altitude * (103.4 + altitude * (-12.79 + altitude * 0.711)));
        }
        return -20.774 / te;
    }
}
