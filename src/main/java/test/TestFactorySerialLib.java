/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import com.fazecast.jSerialComm.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import jazari.factory.FactorySerialLib;

public class TestFactorySerialLib {

    public static void main(String[] args) {
        FactorySerialLib serialLib = new FactorySerialLib();

        if (serialLib.openSerialPort("COM9", 115200)) {
            serialLib.setDataCallback(data -> {
                System.out.println("Alınan veri: " + data);
                // Burada gelen veriyi işleyebilirsiniz
            });

            serialLib.startListening();

            // Veri gönderme örneği
            serialLib.sendData("Merhaba, Arduino!");

            // Programı çalışır durumda tutmak için
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Program sonlandığında portu kapatın
            serialLib.closePort();
        }
    }
//    private static SerialPort port;
//    private static BufferedReader reader;
//
//    public static void main(String[] args) {
//        connectToPort();
//    }
//
//    private static void connectToPort() {
//        port = SerialPort.getCommPort("com9");
//        port.setComPortParameters(115200, 8, 1, SerialPort.NO_PARITY);
//        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);
//
//        if (port.openPort()) {
//            System.out.println("Port başarıyla açıldı.");
//            reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
//            addDataListener();
//        } else {
//            System.out.println("Port açılamadı. 5 saniye sonra yeniden denenecek.");
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            connectToPort();
//        }
//    }
//
//    private static void addDataListener() {
//        port.addDataListener(new SerialPortDataListener() {
//            @Override
//            public int getListeningEvents() {
//                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
//            }
//            
//            @Override
//            public void serialEvent(SerialPortEvent event) {
//                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
//                    return;
//                }
//                try {
//                    String line = reader.readLine();
//                    if (line != null && !line.trim().isEmpty()) {
//                        System.out.println("Alınan veri: " + line.trim());
//                    }
//                } catch (Exception e) {
//                    System.out.println("Veri okuma hatası: " + e.getMessage());
//                    reconnect();
//                }
//            }
//        });
//    }
//
//    private static void reconnect() {
//        System.out.println("Bağlantı koptu. Yeniden bağlanılıyor...");
//        port.closePort();
//        connectToPort();
//    }
}
