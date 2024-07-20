package jazari.device.arduino;

import jazari.factory.FactorySerialLib;

public class TestSerialComunication {

    public static void main(String[] args) {
        FactorySerialLib serialLib = new FactorySerialLib("COM9", 115200, TestSerialComunication::processData);

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

    private static void processData(String data) {
        System.out.println("Alınan veri: " + data);
    }
}
