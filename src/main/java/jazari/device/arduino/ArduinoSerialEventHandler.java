/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.device.arduino;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class ArduinoSerialEventHandler {

    public static void main(String[] args) {
        // Arduino'nun bağlı olduğu seri portun adını belirleyin 
        String portName = "COM10"; 

        // Seri portu açın
        SerialPort serialPort = SerialPort.getCommPort(portName);
        serialPort.openPort();

        // Seri portun iletişim hızını ayarlayın
        serialPort.setComPortParameters(115200, 8, 1, 0);

        // Arduino'ya bir mesaj gönderin
        //serialPort.writeBytes("Merhaba Arduino!\n".getBytes());

        // Seri portta veri okuma eventi için listener ekleyin
        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; 
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                
                if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    byte[] buffer = new byte[serialPort.bytesAvailable()];
                    int bytesRead = serialPort.readBytes(buffer, buffer.length);
                    //System.out.println("exp = " + bytesRead);

                    // Gelen veriyi satır sonuna kadar oku
                    StringBuilder receivedString = new StringBuilder();
                    for (int i = 0; i < bytesRead; i++) {
                        receivedString.append((char) buffer[i]);
                        if (buffer[i] == '\n') {
                            // Satır sonu karakteri bulundu, verileri işleyin
                            if (receivedString.toString().length()==2) continue;
                            System.out.println("Arduino'dan gelen veri: " + receivedString.toString());
                            // Gerekli işlemleri yapın (örneğin, verileri bir dosyaya kaydedin)
                            receivedString.setLength(0); // StringBuilder'ı temizleyin
                            break; // Döngüyü sonlandırın
                        }
                    }
                }
            }
        });
    }
}