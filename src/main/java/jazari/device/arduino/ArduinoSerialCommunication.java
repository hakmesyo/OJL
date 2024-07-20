/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.device.arduino;

import com.fazecast.jSerialComm.*;

public class ArduinoSerialCommunication {
    public static void main(String[] args) {
        SerialPort serialPort = SerialPort.getCommPort("com10"); 
        
        if (serialPort.openPort()) {
            System.out.println(serialPort.getSystemPortName() + " portuna bağlanıldı.");
            
            serialPort.setComPortParameters(115200, 8, 1, 0);
            //serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 100);

            while (true) {
                if (serialPort.bytesAvailable() > 0) {
                    byte[] readBuffer = new byte[serialPort.bytesAvailable()];
                    int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
                    String message = new String(readBuffer);
                    System.out.println("Arduino'dan alınan mesaj: " + message.trim());
                }
            }
        } else {
            System.out.println("Port açılamadı.");
        }
    }
}