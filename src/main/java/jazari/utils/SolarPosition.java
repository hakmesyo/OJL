/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.utils;

/**
 *
 * @author cezerilab
 */
/**
 * Represents the position of the sun in the sky using altitude and azimuth angles.
 * This class is immutable to ensure thread safety and prevent unwanted modifications.
 */
public final class SolarPosition {
    private final double altitudeAngle;
    private final double azimuthAngle;
    private final double solarIntensity;

    public SolarPosition(double altitudeAngle, double azimuthAngle, double solarIntensity) {
        this.altitudeAngle = altitudeAngle;
        this.azimuthAngle = azimuthAngle;
        this.solarIntensity = solarIntensity;
    }

    public double getAltitudeAngle() {
        return altitudeAngle;
    }

    public double getAzimuthAngle() {
        return azimuthAngle;
    }

    public double getSolarIntensity() {
        return solarIntensity;
    }

    @Override
    public String toString() {
        return String.format("SolarPosition[altitude=%.2f°, azimuth=%.2f°, intensity=%.1f W/m²]",
                           altitudeAngle, azimuthAngle, solarIntensity);
    }
}
