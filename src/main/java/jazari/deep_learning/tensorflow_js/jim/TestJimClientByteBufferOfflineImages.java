/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.deep_learning.tensorflow_js.jim;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jazari.factory.FactoryUtils;
import jazari.image_processing.ImageProcess;
import jazari.interfaces.call_back_interface.CallBackString;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author dell_lab
 */
public class TestJimClientByteBufferOfflineImages {

    public static long t1 = System.currentTimeMillis();
    private static WebSocketClient client;
    private static File[] files;
    private static int index = 0;
    private static boolean isConnected = false;
    private static String response="";

    private static WebSocketClient connectToJavaServer(String ip, String port, CallBackString cb) {
        try {
            client = new WebSocketClient(new URI("ws://" + ip + ":" + port), new Draft_6455()) {
                @Override
                public void onMessage(String message) {
                    cb.onMessageReceived(message);
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("You connected to Java server: " + getURI() + "\n");
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason + "\n");
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("error :" + ex.getMessage());
//                    client.close();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex1) {
                        Logger.getLogger(JimUtils.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            };
            client.connect();
        } catch (URISyntaxException ex) {
            System.out.println("ws://" + ip + ":" + port + " is not a valid WebSocket URI\n");
        }
        return client;
    }

    private static WebSocketClient startJimCommunication(String hostIP, String socketServerPort, int ms_wait) {
        System.out.println(hostIP + " " + socketServerPort + " triggered");
        
        client = connectToJavaServer(hostIP, socketServerPort, new CallBackString() {
            @Override
            public void onMessageReceived(String msg) {
                if (!isConnected && msg.contains("[new client connection]")) {
                    isConnected = true;
                    System.out.println("connection was accepted, now we send register request to the JIM in order to get ID.. ");
                    client.send("CJ:NTHR");
                } else if (msg.contains("JC:THR:")) {
                    int thread_no = Integer.parseInt(msg.split(":")[2]);
                    if (msg.split(":").length < 4) {
                        client.send("CJ:THR:" + thread_no + ":" + "real time test image" + ":./temp.jpg");
                    }else{
                        response = msg.split(":")[4];
                    }
                    iterateImageFile(thread_no, response);

                }

            }

        });
        return client;
    }

    private static void iterateImageFile(int thread_no, String response) {
        System.out.println("thread no:" + thread_no + " detection response = " + response+" elapsed time:"+(System.currentTimeMillis()-t1));
        t1=System.currentTimeMillis();        
        if (index < files.length - 1) {
            BufferedImage img = ImageProcess.imread(files[index++].getAbsolutePath());
            sendByteBufferData(img, thread_no);
        } else {
            System.out.println("finished");
        }
    }

    private static void sendByteBufferData(BufferedImage bi, int thread_id) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", out);
            byte[] b = out.toByteArray();
            byte[] bb = new byte[b.length + 1];
            bb[0] = (byte) thread_id;
            for (int i = 0; i < b.length; i++) {
                bb[i + 1] = b[i];
            }
            ByteBuffer byteBuffer = ByteBuffer.wrap(bb);
            client.send(byteBuffer);
            out.close();
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        t1=System.currentTimeMillis();
        files = FactoryUtils.getFileArrayInFolderByExtension("C:\\Users\\dell_lab\\Desktop\\fsm_tfjs\\dataset\\closed", "jpg");
        client = startJimCommunication("localhost", "8887", 1);
    }
}
