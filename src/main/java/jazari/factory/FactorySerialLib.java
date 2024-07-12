/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.factory;

import com.fazecast.jSerialComm.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.function.Consumer;

public class FactorySerialLib {

    private SerialPort port;
    private BufferedReader reader;
    private OutputStream outputStream;
    private Consumer<String> dataCallback;
    private static final String PORT_NAMES[] = {
        "/dev/tty.usbserial-A9007UX1", // Mac OS X
        "/dev/ttyACM0", // Raspberry Pi
        "/dev/ttyUSB0", // Linux
        "COM6", // Windows for solenoid valve
        "COM7" // 
    };

    public FactorySerialLib() {
    }

    public boolean openSerialPort(String portName, int baudRate) {
        port = SerialPort.getCommPort(portName);
        port.setComPortParameters(baudRate, 8, 1, SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0);

        if (port.openPort()) {
            System.out.println("Port başarıyla açıldı: " + portName);
            reader = new BufferedReader(new InputStreamReader(port.getInputStream()));
            outputStream = port.getOutputStream();
            return true;
        } else {
            System.out.println("Port açılamadı: " + portName);
            return false;
        }
    }

    public void setDataCallback(Consumer<String> callback) {
        this.dataCallback = callback;
    }

    public void startListening() {
        if (port == null || !port.isOpen()) {
            throw new IllegalStateException("Seri port açık değil.");
        }

        port.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    return;
                }
                try {
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty() && dataCallback != null) {
                        dataCallback.accept(line.trim());
                    }
                } catch (Exception e) {
                    System.out.println("Veri okuma hatası: " + e.getMessage());
                    reconnect();
                }
            }
        });
    }

    public boolean sendData(String data) {
        if (port == null || !port.isOpen() || outputStream == null) {
            System.out.println("Port açık değil veya çıkış akışı yok.");
            return false;
        }
        try {
            outputStream.write(data.getBytes());
            outputStream.flush();
            System.out.println("Veri gönderildi: " + data);
            return true;
        } catch (Exception e) {
            System.out.println("Veri gönderme hatası: " + e.getMessage());
            return false;
        }
    }

    public void closePort() {
        if (port != null && port.isOpen()) {
            port.closePort();
            System.out.println("Port kapatıldı.");
        }
    }

    private void reconnect() {
        System.out.println("Bağlantı koptu. Yeniden bağlanılıyor...");
        closePort();
        if (openSerialPort(port.getSystemPortName(), port.getBaudRate())) {
            startListening();
        }
    }
}
