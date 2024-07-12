/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.device.arduino;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jazari.factory.FactorySerialLib;
import org.nd4j.shade.jackson.databind.JsonNode;
import org.nd4j.shade.jackson.databind.ObjectMapper;

/**
 *
 * @author cezerilab
 */

/*
const int LED_PIN = 13;  // Arduino üzerindeki dahili LED'in pin numarası

String inputString = "";      // Gelen seri verileri tutmak için string
bool stringComplete = false;  // String'in tamamlanıp tamamlanmadığını kontrol etmek için

void setup() {
  Serial.begin(115200);  // Seri iletişimi 115200 baud hızında başlat
  inputString.reserve(200);  // inputString için 200 byte ayır
  pinMode(LED_PIN, OUTPUT);  // LED pinini çıkış olarak ayarla
}

void loop() {
  // Eğer tam bir string alındıysa
  if (stringComplete) {
    processCommand(inputString);
    
    // String'i sıfırla
    inputString = "";
    stringComplete = false;
  }
  
  // Periyodik olarak sensor verilerini gönder (örneğin, her 1 saniyede bir)
  static unsigned long lastSendTime = 0;
  if (millis() - lastSendTime > 1000) {
    sendSensorData();
    lastSendTime = millis();
  }
}

void serialEvent() {
  while (Serial.available()) {
    char inChar = (char)Serial.read();
    inputString += inChar;
    if (inChar == '\n') {
      stringComplete = true;
    }
  }
}

void processCommand(String command) {
  command.trim();  // Baştaki ve sondaki boşlukları kaldır
  
  if (command == "LED_ON") {
    digitalWrite(LED_PIN, HIGH);
    Serial.println("LED turned ON");
  } 
  else if (command == "LED_OFF") {
    digitalWrite(LED_PIN, LOW);
    Serial.println("LED turned OFF");
  }
  else if (command == "GET_DATA") {
    sendSensorData();
  }
  else {
    Serial.println("Unknown command: " + command);
  }
}

void sendSensorData() {
  // Örnek sensor verileri (gerçek uygulamada bunları sensor okumalarıyla değiştirin)
  int temperature = random(20, 30);  // 20-30 arası rastgele sıcaklık
  int humidity = random(40, 60);     // 40-60 arası rastgele nem
  
  // JSON benzeri bir format kullanarak verileri gönder
  Serial.print("{\"temp\":");
  Serial.print(temperature);
  Serial.print(",\"hum\":");
  Serial.print(humidity);
  Serial.println("}");
}
 */
public class TestArduinoCodeEfficient {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Pattern DATA_PATTERN = Pattern.compile("Heading = (\\d+\\.\\d+) Degress = (\\d+\\.\\d+)");

    public static void main(String[] args) {
        FactorySerialLib serialLib = new FactorySerialLib();

        if (serialLib.openSerialPort("COM9", 115200)) {  // Port adını ve baud hızını doğru ayarlayın
            serialLib.setDataCallback(TestArduinoCodeEfficient::processArduinoData);
            serialLib.startListening();

            // Arduino'ya komutlar gönder
            serialLib.sendData("LED_ON\n");

            try {
                Thread.sleep(2000);  // 2 saniye bekle
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            serialLib.sendData("GET_DATA\n");

            // Programı çalışır durumda tutmak için
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            serialLib.closePort();
        }
    }

    private static void processArduinoData(String data) {
        System.out.println("Arduino'dan gelen ham veri: " + data);

        Matcher matcher = DATA_PATTERN.matcher(data);
        if (matcher.find()) {
            double heading = Double.parseDouble(matcher.group(1));
            double degrees = Double.parseDouble(matcher.group(2));

            System.out.printf("Heading: %.2f%n", heading);
            System.out.printf("Degrees: %.2f%n", degrees);

            // Burada heading ve degrees değerleriyle istediğiniz işlemleri yapabilirsiniz
            // Örnek:
            if (heading > 2.0 && degrees > 200.0) {
                System.out.println("Belirli bir eşik aşıldı!");
            }
        } else {
            System.out.println("Veri formatı tanınmadı: " + data);
        }
    }

    private static void processArduinoDataV2(String data) {
        System.out.println("Arduino'dan gelen ham veri: " + data);

        try {
            if (data.startsWith("{") && data.endsWith("}")) {
                // JSON verisi
                JsonNode jsonNode = objectMapper.readTree(data);

                if (jsonNode.has("temp") && jsonNode.has("hum")) {
                    int temperature = jsonNode.get("temp").asInt();
                    int humidity = jsonNode.get("hum").asInt();

                    System.out.println("Sıcaklık: " + temperature + "°C");
                    System.out.println("Nem: " + humidity + "%");

                    // Burada sıcaklık ve nem değerleriyle istediğiniz işlemleri yapabilirsiniz
                }
            } else {
                // JSON olmayan veri (örneğin, "LED turned ON" gibi mesajlar)
                System.out.println("Mesaj: " + data);
            }
        } catch (Exception e) {
            System.err.println("Veri ayrıştırma hatası: " + e.getMessage());
        }
    }

}
