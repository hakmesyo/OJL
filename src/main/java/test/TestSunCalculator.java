/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class TestSunCalculator {

    private static final double RAD = Math.PI / 180;

    // Yılın gününü hesapla (1-365)
    private int getDayOfYear(LocalDate date) {
        LocalDate startOfYear = LocalDate.of(date.getYear(), 1, 1);
        return (int) ChronoUnit.DAYS.between(startOfYear, date) + 1;
    }

    // Güneşin konumunu hesapla
    private double getSunPosition(int dayOfYear) {
        // Güneşin eğimi (declination)
        return -23.45 * Math.cos(360.0 / 365.0 * (dayOfYear + 10) * RAD);
    }

    // Saat açısını hesapla
    private double getHourAngle(double latitude, double declination) {
        double latRad = latitude * RAD;
        double decRad = declination * RAD;

        // Güneş yüksekliği -0.833 derece olduğunda doğuş/batış gerçekleşir
        return Math.toDegrees(Math.acos(
                (-Math.sin(-0.833 * RAD) - Math.sin(latRad) * Math.sin(decRad))
                / (Math.cos(latRad) * Math.cos(decRad))
        ));
    }

    // Güneş doğuş ve batış zamanlarını hesapla
    public SunTimes calculateSunTimes(LocalDate date, double latitude, double longitude) {
        int dayOfYear = getDayOfYear(date);
        double declination = getSunPosition(dayOfYear);
        double hourAngle = getHourAngle(latitude, declination);

        // Zaman düzeltmesi (equation of time)
        double B = 360.0 * (dayOfYear - 81) / 365.0;
        double EoT = 9.87 * Math.sin(2 * B * RAD) - 7.53 * Math.cos(B * RAD) - 1.5 * Math.sin(B * RAD);

        // UTC+3 için düzeltme (Türkiye saat dilimi)
        int zoneDiff = 3;
        double timeCorrection = EoT + (4 * longitude) - (60 * zoneDiff);

        // Güneş doğuş ve batış zamanları (dakika cinsinden)
        double sunriseMinutes = 720 - 4 * hourAngle - timeCorrection;
        double sunsetMinutes = 720 + 4 * hourAngle - timeCorrection;

        return new SunTimes(
                formatTime(sunriseMinutes),
                formatTime(sunsetMinutes)
        );
    }

    // Dakikaları saat:dakika formatına çevir
    private String formatTime(double minutes) {
        int hours = (int) (minutes / 60);
        int mins = (int) Math.round(minutes % 60);
        return String.format("%02d:%02d", hours, mins);
    }

    // Sonuçları tutmak için iç sınıf
    public static class SunTimes {

        public final String sunrise;
        public final String sunset;

        public SunTimes(String sunrise, String sunset) {
            this.sunrise = sunrise;
            this.sunset = sunset;
        }

        @Override
        public String toString() {
            return "Güneş doğuş: " + sunrise + ", Güneş batış: " + sunset;
        }
    }

    public static void main(String[] args) {
        TestSunCalculator calculator = new TestSunCalculator();

        // Bazı örnek şehirler ve koordinatları
        System.out.println("Örnek şehir koordinatları:");
        System.out.println("İstanbul: 41.0082, 28.9784");
        System.out.println("Siirt: 37.9333, 41.9500");
        System.out.println("Ankara: 39.9334, 32.8597");
        System.out.println("İzmir: 38.4237, 27.1428");
        System.out.println();

        // Koordinatları burada değiştirebilirsiniz
//        double latitude = 41.0082;  // İstanbul için örnek enlem
//        double longitude = 28.9784; // İstanbul için örnek boylam
        double latitude = 37.9333;  // siirt için örnek enlem
        double longitude = 41.9500; // siirt için örnek boylam
        //LocalDate date = LocalDate.of(2025, 1, 20); // Tarihi buradan değiştirebilirsiniz
        LocalDate date = LocalDate.of(2025, 6, 20); // Tarihi buradan değiştirebilirsiniz

        SunTimes times = calculator.calculateSunTimes(date, latitude, longitude);

        System.out.println("Seçilen koordinatlar için " + date + " tarihinde:");
        System.out.println("Enlem: " + latitude + "°, Boylam: " + longitude + "°");
        System.out.println(times);
    }
}
