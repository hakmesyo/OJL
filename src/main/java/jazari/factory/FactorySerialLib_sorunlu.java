/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.factory;

import com.fazecast.jSerialComm.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;
import java.util.Timer;
import java.util.TimerTask;

public class FactorySerialLib_sorunlu {

    public SerialPort port;
    public int baudRate = 115200;
    public String portName = "COM5";
    private OutputStream outputStream;
    private Consumer<String> dataCallback;
    private Timer connectionCheckTimer;

    public FactorySerialLib_sorunlu(String portName, int baudRate, Consumer<String> callback) {
        this.portName = portName;
        this.baudRate = baudRate;
        openSerialPort(portName, baudRate);
        setDataCallback(callback);
        startListening();
        startConnectionCheck();
    }

    public boolean openSerialPort(String portName, int baudRate) {
        port = SerialPort.getCommPort(portName);
        port.setComPortParameters(baudRate, 8, 1, SerialPort.NO_PARITY);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 100);

        if (port.openPort()) {
            System.out.println("Port başarıyla açıldı: " + portName);
            try {
                outputStream = port.getOutputStream();
                return true;
            } catch (Exception e) {
                System.err.println("OutputStream alınırken hata: " + e.toString());
                return false;
            }
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
                    byte[] buffer = new byte[1024];
                    int bytesRead = port.readBytes(buffer, buffer.length);
                    if (bytesRead > 0) {
                        String data = new String(buffer, 0, bytesRead).trim();
                        if (!data.isEmpty() && dataCallback != null) {
                            dataCallback.accept(data);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Veri okuma hatası: " + e.toString());
                    e.printStackTrace();
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
            System.err.println("Veri gönderme hatası: " + e.toString());
            e.printStackTrace();
            return false;
        }
    }

    public void closePort() {
        if (port != null && port.isOpen()) {
            port.closePort();
            System.out.println("Port kapatıldı.");
        }
        if (connectionCheckTimer != null) {
            connectionCheckTimer.cancel();
            connectionCheckTimer = null;
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                System.err.println("OutputStream kapatılırken hata: " + e.toString());
            }
            outputStream = null;
        }
    }

    private void reconnect() {
        System.out.println("Bağlantı koptu. Yeniden bağlanılıyor...");
        closePort(); // Mevcut bağlantıyı ve kaynakları temizle

        if (openSerialPort(portName, baudRate)) {
            try {
                outputStream = port.getOutputStream(); // outputStream'i yeniden ata
                startListening(); // Dinlemeyi yeniden başlat
                startConnectionCheck(); // Timer'ı yeniden başlat
                System.out.println("Yeniden bağlantı başarılı.");
            } catch (Exception e) {
                System.err.println("Yeniden bağlantı sırasında hata: " + e.toString());
                e.printStackTrace();
            }
        } else {
            System.err.println("Yeniden bağlantı başarısız.");
        }
    }

    private void startConnectionCheck() {
        if (connectionCheckTimer != null) {
            connectionCheckTimer.cancel();
        }
        connectionCheckTimer = new Timer();
        connectionCheckTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkConnection();
            }
        }, 0, 5000); // Her 5 saniyede bir kontrol et
    }

    private void checkConnection() {
        if (!port.isOpen()) {
            reconnect();
        }
    }
}