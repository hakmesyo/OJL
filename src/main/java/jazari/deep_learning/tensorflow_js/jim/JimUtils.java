/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.deep_learning.tensorflow_js.jim;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jazari.interfaces.call_back_interface.CallBackString;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author cezerilab
 */
public class JimUtils {

    public WebSocketClient client;
    public boolean isConnected = false;
    public long t = System.nanoTime();
    public int hit = 0;
    public int err = 0;
    public double accuracy = 0;
    public int index_file = 0;
    public Socket socket = null;

    public static ByteBuffer toByteBuffer(String path) {
        long t = System.nanoTime();
        try {
            File f = new File(path);
            BufferedImage bi = ImageIO.read(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", out);
            ByteBuffer byteBuffer = ByteBuffer.wrap(out.toByteArray());
//            client.send(byteBuffer);
            out.close();
//            byteBuffer.clear();
            System.out.println("time elapsed:" + (System.nanoTime() - t) / 1000000.0d);
            return byteBuffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void sendImageByteBuffer(Socket socket, String imgPath) {
        long t1 = System.currentTimeMillis();
        try {
//            ServerSocket serverSocket = new ServerSocket(15123);
//            System.out.println("Server waits for connection...");
//            Socket socket = serverSocket.accept();
//            getImageByteBuffer(imgPath);
            System.out.println("Accepted connection : " + socket);
            File transferFile = new File(imgPath);
            byte[] bytearray = new byte[(int) transferFile.length()];
            FileInputStream fin = new FileInputStream(transferFile);
            BufferedInputStream bin = new BufferedInputStream(fin);
            bin.read(bytearray, 0, bytearray.length);
            OutputStream os = socket.getOutputStream();
            System.out.println("Sending Files...");
            os.write(bytearray, 0, bytearray.length);
            os.flush();
            socket.close();
            System.out.println("File transfer complete");
        } catch (IOException ex) {
            Logger.getLogger(JimUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("time elapsed:" + (System.currentTimeMillis() - t1));
    }

    public static void getImageByteBuffer(String imgPath) {
        InputStream is = null;
        try {
            //you can change localhost to target ip in the LAN, dont change port number
            long t1 = System.currentTimeMillis();
            Socket socket = new Socket("127.0.0.1", 15123);
            int filesize = 1022386;
            int bytesRead;
            int currentTot = 0;
            byte[] bytearray = new byte[filesize];
            is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(imgPath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bytesRead = is.read(bytearray, 0, bytearray.length);
            currentTot = bytesRead;
            do {
                bytesRead = is.read(bytearray, currentTot, (bytearray.length - currentTot));
                if (bytesRead >= 0) {
                    currentTot += bytesRead;
                }
            } while (bytesRead > -1);
            bos.write(bytearray, 0, currentTot);
            bos.flush();
            bos.close();
            socket.close();
            System.out.println("received side time elapsed:" + (System.currentTimeMillis() - t1));
        } catch (IOException ex) {
            Logger.getLogger(JimUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                Logger.getLogger(JimUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static File[] getFolderListInFolder(String path) {
        ArrayList<File> results = new ArrayList<>();
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                results.add(file);
            }
        }
        return results.toArray(new File[0]);
    }

    public WebSocketClient connectToJavaServer(String ip, String port, CallBackString cb) {
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
                    //System.exit(0);
                    //System.exit(0);
                    //System.exit(0);
                    //System.exit(0);
                }
            };
            client.connect();
        } catch (URISyntaxException ex) {
            System.out.println("ws://" + ip + ":" + port + " is not a valid WebSocket URI\n");
        }
        return client;
    }

    public static String[] getFolderNameAsLabels(String path) {
        File[] dirs = getFolderListInFolder(path);
        String[] ret = new String[dirs.length];
        for (int i = 0; i < dirs.length; i++) {
            ret[i] = dirs[i].getName();
        }
        return ret;
    }

    public static void sendData(String path,int port) {
        try {
            Socket socket = new Socket("localhost", port);
            File fileToSend = new File(path);
            // Create an input stream into the file you want to send.
            FileInputStream fileInputStream = new FileInputStream(path);
            // Create an output stream to write to write to the server over the socket connection.
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            // Get the name of the file you want to send and store it in filename.
            String fileName = fileToSend.getName();
            // Convert the name of the file into an array of bytes to be sent to the server.
            byte[] fileNameBytes = fileName.getBytes();
            // Create a byte array the size of the file so don't send too little or too much data to the server.
            byte[] fileBytes = new byte[(int) fileToSend.length()];
            // Put the contents of the file into the array of bytes to be sent so these bytes can be sent to the server.
            fileInputStream.read(fileBytes);
            // Send the length of the name of the file so server knows when to stop reading.
            dataOutputStream.writeInt(fileNameBytes.length);
            // Send the file name.
            dataOutputStream.write(fileNameBytes);
            // Send the length of the byte array so the server knows when to stop reading.
            dataOutputStream.writeInt(fileBytes.length);
            // Send the actual file.
            dataOutputStream.write(fileBytes);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendByteBufferData(String path,int thread_id) {
        try {
            File f = new File(path);
            BufferedImage bi = ImageIO.read(f);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", out);
            byte[] b=out.toByteArray();
            byte[] bb=new byte[b.length+1];
            bb[0]=(byte)thread_id;
            for (int i = 0; i < b.length; i++) {
               bb[i+1]=b[i]; 
            }
            ByteBuffer byteBuffer = ByteBuffer.wrap(bb);
            client.send(byteBuffer);
            out.close();
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void sendByteBufferData(BufferedImage bi,int thread_id) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", out);
            byte[] b=out.toByteArray();
            byte[] bb=new byte[b.length+1];
            bb[0]=(byte)thread_id;
            for (int i = 0; i < b.length; i++) {
               bb[i+1]=b[i]; 
            }
            ByteBuffer byteBuffer = ByteBuffer.wrap(bb);
            client.send(byteBuffer);
            out.close();
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
