/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.device.serial;

/**
 *
 * @author cezerilab
 */
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SerialComExample {

    public static void main(String[] args) {
        // COM4 portunu bul
        SerialPort comPort = SerialPort.getCommPort("COM4");

        // Portu aç (9600 baud, 8 veri biti, hiçbir parite, 1 durma biti)
        comPort.openPort();
        comPort.setBaudRate(9600);
        comPort.setNumDataBits(8);
        comPort.setParity(SerialPort.NO_PARITY);
        comPort.setNumStopBits(1);
        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    return;
                }
                byte[] newData = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(newData, newData.length);
                // Baytları karakter dizisine dönüştür
                String receivedString = new String(newData, StandardCharsets.UTF_8);
                System.out.println(receivedString);
            }
        });

//        // Giriş ve çıkış akışlarını al
//        InputStream inputStream = comPort.getInputStream();
//        OutputStream outputStream = comPort.getOutputStream();
//
//        // Ayrı bir iş parçacığında gelen verileri oku
//        Thread readThread = new Thread(() -> {
//            Scanner scanner = new Scanner(inputStream);
//            while (true) {
//                while (scanner.hasNextLine()) {
//                    String line = scanner.nextLine();
//                    System.out.println("Gelen veri: " + line);
//                }
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(SerialComExample.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        });
//        readThread.start();
//
////        // Kullanıcıdan veri al ve COM4'e gönder
////        Scanner userInputScanner = new Scanner(System.in);
////        while (true) {
////            System.out.print("Gönderilecek veri: ");
////            String data = userInputScanner.nextLine();
////            try {
////                outputStream.write(data.getBytes());
////                outputStream.flush();
////            } catch (Exception e) {
////                System.err.println("Veri gönderme hatası: " + e.getMessage());
////            }
////        }
    }
}
