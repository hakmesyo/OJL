/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.websocket;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * PYTHON SIDE
 * 
 * websocket_client.py
 * 
import asyncio
import websockets
import base64
import json
from io import BytesIO
from PIL import Image
import torch
from ultralytics import YOLO
import os

# Özel YOLOv8 modelinin yolu
# MODEL_PATH = r"C:\Users\Teknofest\Documents\NetBeansProjects\DJLPytorch\models\my_model/model_m.pt"
MODEL_PATH = r"C:\Users\Teknofest\Documents\NetBeansProjects\DJLPytorch\models\my_model/model_n.pt"

# synset.txt dosyasının yolu (model ile aynı dizinde olduğunu varsayıyoruz)
SYNSET_PATH = os.path.join(os.path.dirname(MODEL_PATH), "synset.txt")

# Özel YOLOv8 modelini yükle
model = YOLO(MODEL_PATH)

# Sınıf isimlerini synset.txt dosyasından oku
def read_class_names(synset_path):
    with open(synset_path, 'r', encoding='utf-8') as f:
        class_names = [line.strip() for line in f.readlines()]
    return class_names

class_names = read_class_names(SYNSET_PATH)

async def detect_objects(websocket, path):
    while True:
        try:
            # WebSocket'ten JSON mesajını al
            message = await websocket.recv()
            data = json.loads(message)
            
            image_id = data['image_id']
            base64_image = data['image_data']
            
            # Base64'ten görüntüyü decode et
            image_data = base64.b64decode(base64_image)
            image = Image.open(BytesIO(image_data))
            
            # Nesne algılama işlemini gerçekleştir
            results = model(image)
            
            # Sonuçları işle
            objects = []
            for r in results:
                for box in r.boxes:
                    class_id = int(box.cls)
                    obj = {
                        'class': class_id,
                        'class_name': class_names[class_id],
                        'confidence': float(box.conf),
                        'bbox': box.xyxy[0].tolist()
                    }
                    objects.append(obj)
            
            # Sonuçları JSON formatında gönder
            response = {
                'image_id': image_id,
                'objects': objects
            }
            await websocket.send(json.dumps(response))
            print(f"Processed image ID: {image_id}. Detected {len(objects)} objects.")
            
        except websockets.exceptions.ConnectionClosed:
            print("Bağlantı kapandı")
            break
        except Exception as e:
            print(f"Hata oluştu: {str(e)}")

async def main():
    server = await websockets.serve(detect_objects, "localhost", 8888)
    print("Python WebSocket sunucusu başlatıldı (port 8888)")
    await server.wait_closed()

if __name__ == "__main__":
    asyncio.get_event_loop().run_until_complete(main())
 *
 * yukarıdaki python kodu yolov8 pytorch modelini yükler ve javadan gönderilen imgelerde nesnelerin
 * yerlerini ve isimlerini tespit ederek javaya gönderir.
 * 
 * İlk önce anaconda yüklenecek
 * ardından conda env yapılacak
 * bunu active edip pytorch, websocket ve ultralytics bağımlılıkları pip ile yüklenecek
 * uygun cuda, cudnn yüklenecek
 * pytorchun gpuyu kullanabilip kullanamayacağı test edilecek
 * 
 * ilk önce komut satırından (conda env de iken) python websocket_client.py dosyası çalıştırılacak
 * ikinci olarak java programı çalıştırılacak.
 * 
 */

public class ObjectDetectionServer {

    private static final int PORT = 8887;
    private static final String PYTHON_WS_URL = "ws://localhost:8888";
    private static final String IMAGE_DIRECTORY = "dataset/ds_simulation/test";
    private static final CopyOnWriteArrayList<WebSocketConnection> connections = new CopyOnWriteArrayList<>();
    private static WebSocket pythonWebSocket;
    private static final AtomicInteger imageIdCounter = new AtomicInteger(0);
    private static final Map<Integer, String> imagePaths = new HashMap<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/objectdetection", new WebSocketHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + PORT);

        connectToPython();
        processImages();
    }

    private static StringBuilder messageBuffer = new StringBuilder();

    private static void connectToPython() throws InterruptedException {
        CompletableFuture<WebSocket> webSocketCompletableFuture = HttpClient
                .newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(PYTHON_WS_URL), new WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        messageBuffer.append(data);
                        if (last) {
                            try {
                                processCompleteMessage(messageBuffer.toString());
                            } catch (JSONException e) {
                                System.err.println("Error processing message: " + e.getMessage());
                                System.err.println("Problematic message: " + messageBuffer.toString());
                            } finally {
                                messageBuffer.setLength(0); // Clear the buffer
                            }
                        }
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }
                });

        try {
            pythonWebSocket = webSocketCompletableFuture.get();
        } catch (ExecutionException ex) {
            Logger.getLogger(ObjectDetectionServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Connected to Python WebSocket server");
    }

    private static void processCompleteMessage(String message) throws JSONException {
        JSONObject jsonResponse = new JSONObject(message);
        int imageId = jsonResponse.getInt("image_id");
        String imagePath = imagePaths.get(imageId);
        JSONArray objects = jsonResponse.getJSONArray("objects");

        System.out.println("Received result for image ID: " + imageId);
        System.out.println("Image path: " + imagePath);
        System.out.println("Number of detected objects: " + objects.length());

        for (int i = 0; i < objects.length(); i++) {
            JSONObject obj = objects.getJSONObject(i);
            String className = obj.getString("class_name");
            double confidence = obj.getDouble("confidence");
            JSONArray bbox = obj.getJSONArray("bbox");

            System.out.println("Detected: " + className + " with confidence: " + confidence);
            System.out.println("Bounding box: " + bbox);
        }
        System.out.println("--------------------");
    }

    private static void processImages() throws IOException, InterruptedException {
        List<Path> imagePaths = Files.walk(Paths.get(IMAGE_DIRECTORY))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".jpg")
                || path.toString().toLowerCase().endsWith(".png"))
                .collect(Collectors.toList());

        for (int i = 0; i < 1; i++) {
            for (Path imagePath : imagePaths) {
                byte[] imageData = Files.readAllBytes(imagePath);
                String base64Image = Base64.getEncoder().encodeToString(imageData);

                int imageId = imageIdCounter.getAndIncrement();
                ObjectDetectionServer.imagePaths.put(imageId, imagePath.toString());

                JSONObject jsonMessage = new JSONObject();
                try {
                    jsonMessage.put("image_id", imageId);
                    jsonMessage.put("image_data", base64Image);
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }

                pythonWebSocket.sendText(jsonMessage.toString(), true);
                System.out.println("Sent image: " + imagePath.getFileName() + " with ID: " + imageId);

                Thread.sleep(10); // 100ms bekleme
            }
        }
    }

    static class WebSocketHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleWebSocketHandshake(exchange);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handleWebSocketHandshake(HttpExchange exchange) throws IOException {
            String key = exchange.getRequestHeaders().getFirst("Sec-WebSocket-Key");
            String response = "HTTP/1.1 101 Switching Protocols\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Sec-WebSocket-Accept: " + generateWebSocketAccept(key) + "\r\n\r\n";
            exchange.sendResponseHeaders(101, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            WebSocketConnection connection = new WebSocketConnection(exchange);
            connections.add(connection);
            connection.startReading();
        }

        private String generateWebSocketAccept(String key) {
            String fullKey = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                return Base64.getEncoder().encodeToString(md.digest(fullKey.getBytes(StandardCharsets.UTF_8)));
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(ObjectDetectionServer.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
    }

    static class WebSocketConnection {

        private final HttpExchange exchange;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        WebSocketConnection(HttpExchange exchange) throws IOException {
            this.exchange = exchange;
            this.inputStream = exchange.getRequestBody();
            this.outputStream = exchange.getResponseBody();
        }

        void startReading() {
            new Thread(() -> {
                try {
                    while (true) {
                        WebSocketFrame frame = readFrame();
                        if (frame.opcode == 8) { // Close frame
                            close();
                            break;
                        } else if (frame.opcode == 1) { // Text frame
                            onMessage(new String(frame.payload, StandardCharsets.UTF_8));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        void send(String message) throws IOException {
            byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            WebSocketFrame frame = new WebSocketFrame(1, payload);
            writeFrame(frame);
        }

        private WebSocketFrame readFrame() throws IOException {
            byte[] header = new byte[2];
            inputStream.read(header);

            boolean fin = (header[0] & 0x80) != 0;
            int opcode = header[0] & 0x0F;
            boolean masked = (header[1] & 0x80) != 0;
            int length = header[1] & 0x7F;

            if (length == 126) {
                byte[] extendedLength = new byte[2];
                inputStream.read(extendedLength);
                length = ((extendedLength[0] & 0xFF) << 8) | (extendedLength[1] & 0xFF);
            } else if (length == 127) {
                byte[] extendedLength = new byte[8];
                inputStream.read(extendedLength);
                length = 0;
                for (int i = 0; i < 8; i++) {
                    length |= ((long) (extendedLength[i] & 0xFF)) << (56 - (i * 8));
                }
            }

            byte[] maskingKey = new byte[4];
            if (masked) {
                inputStream.read(maskingKey);
            }

            byte[] payload = new byte[length];
            inputStream.read(payload);

            if (masked) {
                for (int i = 0; i < payload.length; i++) {
                    payload[i] ^= maskingKey[i % 4];
                }
            }

            return new WebSocketFrame(opcode, payload);
        }

        private void writeFrame(WebSocketFrame frame) throws IOException {
            byte[] header = new byte[2];
            header[0] = (byte) (0x80 | frame.opcode);

            if (frame.payload.length < 126) {
                header[1] = (byte) frame.payload.length;
            } else if (frame.payload.length < 65536) {
                header[1] = 126;
            } else {
                header[1] = 127;
            }

            outputStream.write(header);

            if (frame.payload.length >= 126 && frame.payload.length < 65536) {
                outputStream.write((frame.payload.length >> 8) & 0xFF);
                outputStream.write(frame.payload.length & 0xFF);
            } else if (frame.payload.length >= 65536) {
                for (int i = 7; i >= 0; i--) {
                    outputStream.write((int) ((frame.payload.length >> (8 * i)) & 0xFF));
                }
            }

            outputStream.write(frame.payload);
            outputStream.flush();
        }

        private void close() throws IOException {
            connections.remove(this);
            exchange.close();
        }

        protected void onMessage(String message) {
            System.out.println("Received message: " + message);
        }

        private static class WebSocketFrame {

            final int opcode;
            final byte[] payload;

            WebSocketFrame(int opcode, byte[] payload) {
                this.opcode = opcode;
                this.payload = payload;
            }
        }
    }
}
