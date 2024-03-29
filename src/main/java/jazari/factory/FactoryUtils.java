/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.factory;

import au.com.bytecode.opencsv.CSVReader;
import com.google.gson.Gson;
import java.awt.AWTException;
import jazari.interfaces.InterfaceCallBack;
import jazari.utils.SerialType;
import jazari.types.TDeviceState;
import jazari.types.TWord;
import jazari.types.TLearningType;
import jazari.matrix.CMatrix;
import jazari.matrix.CPoint;
import jazari.types.TRoi;
import jazari.utils.CustomComparatorForCPoint;
import jazari.utils.MersenneTwister;
import jazari.utils.ReaderCSV;
import jazari.utils.UniqueRandomNumbers;
import jazari.websocket.SocketServer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_BGR;
import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import jazari.utils.DataAnalytics;
import jazari.gui.FrameImage;
import jazari.image_processing.ImageProcess;
import jazari.interfaces.call_back_interface.CallBackTrigger;
import jazari.matrix.CRectangle;
import jazari.types.TBoundingBox;
import jazari.types.TGapIndex;
import jazari.utils.CopyImageToClipboard;
import jazari.utils.PerlinNoise;
import jazari.utils.WindowsLikeComparator;
import jazari.utils.YoloPolygonJson;
import jazari.utils.pascalvoc.PascalVocBoundingBox;
import jazari.utils.pascalvoc.PascalVocAttribute;
import jazari.utils.pascalvoc.AnnotationPascalVOCFormat;
import jazari.utils.pascalvoc.PascalVocObject;
import jazari.utils.pascalvoc.PascalVocPolygon;
import jazari.utils.pascalvoc.PascalVocSize;
import jazari.utils.pascalvoc.PascalVocSource;
import org.apache.commons.io.FileUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.w3c.dom.Element;
import test.Deneme;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author venap3
 */
public final class FactoryUtils {

    public static boolean stopServer = false;
    public static boolean isConnectPythonServer = false;
    public static WebSocketClient client;
    public static String currDir = System.getProperty("user.dir");
    public static final AtomicBoolean running = new AtomicBoolean(false);
    public static int nAttempts = 0;
    public static Robot robot;
    public static String saveImageFolder=FactoryUtils.getDefaultDirectory();

    static {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public static String listPorts() {
//        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
//        String str="";
//        while (portEnum.hasMoreElements()) {
//            CommPortIdentifier portIdentifier = portEnum.nextElement();
//            String s=portIdentifier.getName() + " - " + getPortTypeName(portIdentifier.getPortType());
//            System.out.println(s);
//            str+=s+"\n";
//        }
//        return str;
//    }
//
//    public static String getPortTypeName(int portType) {
//        switch (portType) {
//            case CommPortIdentifier.PORT_I2C:
//                return "I2C";
//            case CommPortIdentifier.PORT_PARALLEL:
//                return "Parallel";
//            case CommPortIdentifier.PORT_RAW:
//                return "Raw";
//            case CommPortIdentifier.PORT_RS485:
//                return "RS485";
//            case CommPortIdentifier.PORT_SERIAL:
//                return "Serial";
//            default:
//                return "unknown type";
//        }
//    }
    public static String getMacAddress() {
        InetAddress ip;
        StringBuilder sb = new StringBuilder();;
        try {
            ip = InetAddress.getLocalHost();
//            System.out.println("Current IP address : " + ip.getHostAddress());
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            System.out.print("Current MAC address : ");
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
//            System.out.println(sb.toString());
            return sb.toString();
        } catch (UnknownHostException | SocketException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getIPAddress() {
        InetAddress ip = null;
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ip.getHostAddress();
    }

    /**
     * deserialize to Object from given file.We use the general Object so as
     * that it can work for any Java Class.
     *
     * @param fileName
     * @return
     */
    public static Object deserialize(String fileName) {
        FileInputStream fis = null;
        Object obj = null;
        try {
            fis = new FileInputStream(fileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                obj = ois.readObject();
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return obj;
    }

    /**
     * serialize the given object and save it to given file
     *
     * @param obj
     * @param fileName
     */
    public static void serialize(Object obj, String fileName) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

//    private static String curr_file;
//    public static void plot(CMatrix cm) {
//        new FramePlot(cm).setVisible(true);
//    }
    public static ArrayList<Point> shuffleList(ArrayList<Point> lst) {
        long seed = System.nanoTime();
        Collections.shuffle(lst, new Random(seed));
        return lst;
    }
    // This method returns a buffered image with the contents of an image

//    public static Object readFromXML() {
//        Object out = null;
//        try {
//            JFileChooser chooser = new JFileChooser();
////            chooser.setCurrentDirectory(new java.io.File("C:\\Plotter\\GCODE"));
//            chooser.setDialogTitle("xml from file");
//            chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
//            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//                File file = chooser.getSelectedFile();
//                String modelFile = file.getAbsolutePath();
//                XStream xx = new XStream(new DomDriver());
//                FileReader fileReader;
//                fileReader = new FileReader(modelFile);
//                out = xx.fromXML(fileReader);
//                FactoryUtils.showMessage("B.loaded successfully");
//            }
//        } catch (FileNotFoundException e) {
//        }
//        return out;
//    }
//
//    public static void writeToXML(Object obj) {
//        JFileChooser chooser = new JFileChooser();
////        chooser.setCurrentDirectory(new java.io.File("C:\\Plotter\\GCODE"));
//        chooser.setDialogTitle("save xml as a file");
//        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
//        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
//            File file = chooser.getSelectedFile();
//            String fName = file.getAbsolutePath();
//            writeToFile(fName, toXML(obj));
//            showMessage("B. Object serialized to " + fName);
//        }
//    }
//
//    public static String toXML(Object obj) {
//        return new XStream().toXML(obj);
//    }
//
//    public static Object fromXML(String str) {
//        return new XStream(new DomDriver()).fromXML(str);
//    }
//
//    public static Object fromXMLFile() {
//        return readFromXML();
//    }
    public static File getFileFromChooserForPNG() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("save panel as a png file");
        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
        chooser.setFileFilter(new FileNameExtensionFilter("png", "png"));
        File file = new File("C:\\deneme.png");
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file.getName().indexOf(".png") == -1) {
                File file2 = new File(file.getParent() + "/" + file.getName() + ".png");
                file = file2;
            }
            return file;
        }
        return null;
    }

    public static File getFileFromChooserSave() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(getCurrentDirectory()));
        chooser.setDialogTitle("save as file");
        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
//        chooser.setFileFilter(new FileNameExtensionFilter("png", "png"));
        File file = null;
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            return file;
        }
        return file;
    }

    public static File getFileFromChooserSave(String folderPath) {
        JFileChooser chooser = new JFileChooser(folderPath);
        chooser.setDialogTitle("save as file");
        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
//        chooser.setFileFilter(new FileNameExtensionFilter("png", "png"));
        File file = null;
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            return file;
        }
        return file;
    }

    public static File getFileFromChooserOpen() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(getCurrentDirectory()));
        chooser.setDialogTitle("select file");
        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
        File file = null;
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            return file;
        }
        return file;
    }

    public static File getFileFromChooserOpenFilterImageFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(getCurrentDirectory()));
        chooser.setDialogTitle("select file");
        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "png", "gif", "jpeg");
        chooser.setFileFilter(filter);
        File file = null;
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            return file;
        }
        return file;
    }

    public static File getFileFromChooserOpen(String folderPath) {
        JFileChooser chooser = new JFileChooser(folderPath);
        chooser.setDialogTitle("select file");
        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
//        chooser.setFileFilter(new FileNameExtensionFilter("png", "png"));
        File file = null;
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            return file;
        }
        return file;
    }

    public static File browseDirectory() {
        return browseDirectory(getDefaultDirectory());
    }

    public static File browseDirectory(String path) {
        JFileChooser chooser = new JFileChooser(path);
        chooser.setDialogTitle("Browse Directory");
        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        File file = null;
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
            System.out.println("getSelectedFile() : " + chooser.getSelectedFile());
        } else {
            System.out.println("No Selection ");
        }
        return file;
    }

    public static File browseFile() {
        return getFileFromChooserOpen();
    }

    public static File browseFileForImage() {
        return getFileFromChooserOpenFilterImageFiles();
    }

    public static File browseFile(String path) {
        return getFileFromChooserOpen(path);
    }

    public static void writeOnFile(String file_name, String row) {

        File outFile = new File(file_name);
        if (!outFile.exists()) {
            try {
                if (!outFile.createNewFile()) {
                    System.out.println(file_name + " file was generated");
                    return;
                }
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        FileWriter out;
        try {
            out = new FileWriter(outFile, true);
            out.write(row);
            out.close();
        } catch (IOException e) {
        }
    }

    public static void writeOnFile(String file_name, List<String> rows) {
        String row = "";
        StringBuilder sb = new StringBuilder();
        for (String row1 : rows) {
            sb.append(row1 + "\n");
        }
        row = sb.toString();

        File outFile = new File(file_name);
        if (!outFile.exists()) {
            try {
                if (!outFile.createNewFile()) {
                    System.out.println(file_name + " file was generated");
                    return;
                }
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        FileWriter out;
        try {
            out = new FileWriter(outFile, true);
            out.write(row);
            out.close();
        } catch (IOException e) {
        }
    }

    public static void writeOnFile(String row) {
        File file = getFileFromChooserSave(getDefaultDirectory());
        if (file != null) {
            writeOnFile(file.getAbsolutePath(), row);
        }
    }

    public static void writeToFile(String row) {
        File file = getFileFromChooserSave(getDefaultDirectory());
        if (file != null) {
            writeToFile(file.getAbsolutePath(), row);
        }
    }

    public static void writeToFile(String path, List<String> rows) {
        String row = "";
        StringBuilder sb = new StringBuilder();
        for (String row1 : rows) {
            sb.append(row1 + "\n");
        }
        row = sb.toString();
        Writer out = null;
        try {
            try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(path), "UTF-8"));
                try {
                    try {
                        out.write(row);
                        Thread.sleep(5);
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } finally {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (FileNotFoundException ex) {
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writeToFile(String path, List<String> rows, int chunkSize) {
        int nr = rows.size();
        int nChunk = nr / chunkSize;
        int kalan = nr - nChunk * chunkSize;

        for (int i = 0; i < nChunk; i++) {
            String row = "";
            for (int j = 0; j < chunkSize; j++) {
                row += rows.get(i * chunkSize + j) + "\n";
            }
            writeOnFile(path, row);
        }
        if (kalan != 0) {
            String row = "";
            for (int j = 0; j < kalan; j++) {
                row += rows.get(nChunk * chunkSize + j) + "\n";
            }
            writeOnFile(path, row);
        }

    }

    public static void writeToFile(String path, String row) {
        Writer out = null;
        try {
            try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(path), "UTF-8"));
                try {
                    try {
                        if (!(row == null || row.equals(""))) {
                            out.write(row);
                        }
                        Thread.sleep(5);
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } finally {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (FileNotFoundException ex) {
//                new File(path).delete();
//                System.out.println("FactoryUtils.writeToFile(path,row) metodunda hata oldu");
//                writeToFile(path, row);
                //Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writeToFile(String path, String[] rows) {
        Writer out = null;
        String row = "";
        for (String row1 : rows) {
            if (row1 != null) {
                row += row1 + "\n";
            }
        }
        try {
            try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(path), "UTF-8"));
                try {
                    try {
                        out.write(row);
                        Thread.sleep(5);
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } finally {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (FileNotFoundException ex) {
//                new File(path).delete();
//                System.out.println("FactoryUtils.writeToFile(path,row) metodunda hata oldu");
//                writeToFile(path, row);
                //Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

//    public static void writeToFile(String file_name, String row) {
//        File outFile = new File(file_name);
//        FileWriter out;
//        if (outFile.exists()) {
////            showMessage(file_name + " isminde bir dosya zaten var üzerine yazılacak");
//        }
//        try {
//            out = new FileWriter(outFile, false);
//            out.write(row);
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    public static void writeToFile(String file_name, float[][] d) {
        File outFile = new File(file_name);
        FileWriter out;
        if (outFile.exists()) {
            showMessage(file_name + " isminde bir dosya zaten var üzerine yazılacak");
        }
        try {
            out = new FileWriter(outFile, false);
            String row = "";
            for (int i = 0; i < d.length; i++) {
                String r = "";
                for (int j = 0; j < d[0].length; j++) {
                    r += d[i][j] + ",";
                }
                r = r.substring(0, r.length() - 1);
                row += r + "\n";

            }
            out.write(row);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String file_name, String[][] d) {
        File outFile = new File(file_name);
        FileWriter out;
        if (outFile.exists()) {
            showMessage(file_name + " isminde bir dosya zaten var üzerine yazılacak");
        }
        try {
            out = new FileWriter(outFile, false);
            String row = "";
            for (int i = 0; i < d.length; i++) {
                String r = "";
                for (int j = 0; j < d[0].length; j++) {
                    r += d[i][j] + ",";
                }
                r = r.substring(0, r.length() - 1);
                row += r + "\n";

            }
            out.write(row);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(String file_name, int[][] d) {
        File outFile = new File(file_name);
        FileWriter out;
        if (outFile.exists()) {
            showMessage(file_name + " isminde bir dosya zaten var üzerine yazılacak");
        }
        try {
            out = new FileWriter(outFile, false);
            String row = "";
            for (int i = 0; i < d.length; i++) {
                String r = "";
                for (int j = 0; j < d[0].length; j++) {
                    r += d[i][j] + ",";
                }
                r = r.substring(0, r.length() - 1);
                row += r + "\n";

            }
            out.write(row);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * write data matrix as Weka ARFF file format note that feature names are
     * given f_1..f_n format
     *
     * @param file_path : path with desired file_name
     * @param d : data matrix
     * @param learning_type : TLearningType.REGRESSION or
     * TLearningType.CLASSIFICATION
     */
    public static void writeToArffFile(String file_path, float[][] d, int learning_type) {
        String s = "";
        String relationName = getFileName(file_path);
        s += "@relation '" + relationName + "'\n";
        int n = d[0].length;
        for (int i = 1; i < n; i++) {
            s += "@attribute 'f_" + i + "' real\n";
        }
        if (learning_type == TLearningType.REGRESSION) {
            s += "@attribute 'output' real\n";
        }
        if (learning_type == TLearningType.CLASSIFICATION) {
            float[][] tr = transpose(d);
            float[] distinct = getDistinctValues(tr[tr.length - 1]);
            s += "@attribute 'output' {";
            for (int i = 0; i < distinct.length; i++) {
                s += distinct[i] + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "}\n";
        }
        s += "@data\n";
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                s += d[i][j] + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "\n";
//            writeToFile(file_path, s);
//            s = "";
        }
        writeToFile(file_path, s);
    }

    /**
     * write data matrix as Weka ARFF file format note that feature names are
     * given f_1..f_n format
     *
     * @param file_path : path with desired file_name
     * @param learning_type : TLearningType.REGRESSION or
     * TLearningType.CLASSIFICATION
     */
    public static void writeToArffFile(String file_path, String lines, int learning_type) {
        String[] ss = lines.split("\n");
        String[] sss = ss[0].split(",");
        float[] dd = new float[ss.length];
        for (int i = 0; i < ss.length; i++) {
            sss = ss[i].split(",");
            dd[i] = Float.parseFloat(sss[sss.length - 1]);
        }
        String s = "";
        String relationName = getFileName(file_path);
        s += "@relation '" + relationName + "'\n";
        int n = sss.length;
        for (int i = 1; i < n; i++) {
            s += "@attribute 'f_" + i + "' real\n";
        }
        if (learning_type == TLearningType.REGRESSION) {
            s += "@attribute 'output' real\n";
        }
        if (learning_type == TLearningType.CLASSIFICATION) {
            float[] distinct = getDistinctValues(dd);
            s += "@attribute 'output' {";
            for (int i = 0; i < distinct.length; i++) {
                s += (int) distinct[i] + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "}\n";
        }
        s += "@data\n" + lines;
        writeToFile(file_path, s);
    }

    /**
     * write data matrix as Weka ARFF file format note that feature names are
     * given f_1..f_n format
     *
     * @param file_path : path with desired file_name
     * @param d : data matrix
     * @param learning_type : TLearningType.REGRESSION or
     * TLearningType.CLASSIFICATION
     */
    public static void writeOnArffFile(String file_path, float[][] d, int learning_type) {
        String s = "";
        String relationName = getFileName(file_path);
        s += "@relation '" + relationName + "'\n";
        int n = d[0].length;
        for (int i = 1; i < n; i++) {
            s += "@attribute 'f_" + i + "' real\n";
        }
        if (learning_type == TLearningType.REGRESSION) {
            s += "@attribute 'output' real\n";
        }
        if (learning_type == TLearningType.CLASSIFICATION) {
            float[][] tr = transpose(d);
            float[] distinct = getDistinctValues(tr[tr.length - 1]);
            s += "@attribute 'output' {";
            for (int i = 0; i < distinct.length; i++) {
                s += distinct[i] + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "}\n";
        }
        s += "@data\n";
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                s += d[i][j] + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "\n";
            writeOnFile(file_path, s);
            s = "";
        }
//        writeToFile(file_path, s);
    }

    /**
     * write data matrix as Weka ARFF file format note that feature names are
     * given f_1..f_n format
     *
     * @param file_path : path with desired file_name
     * @param features: names of the features
     * @param d : data matrix
     * @param learning_type : TLearningType.REGRESSION or
     * TLearningType.CLASSIFICATION
     */
    public static void writeToArffFile(String file_path, String[] features, float[][] d, int learning_type) {
        String s = "";
        String relationName = getFileName(file_path);
        s += "@relation '" + relationName + "'\n";
        int n = d[0].length;
        for (int i = 0; i < n - 1; i++) {
            s += "@attribute '" + features[i] + "' real\n";
        }
        if (learning_type == TLearningType.REGRESSION) {
            s += "@attribute 'output' real\n";
        }
        if (learning_type == TLearningType.CLASSIFICATION) {
            float[][] tr = transpose(d);
            float[] distinct = getDistinctValues(tr[tr.length - 1]);
            s += "@attribute 'output' {";
            for (int i = 0; i < distinct.length; i++) {
                s += distinct[i] + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "}\n";
        }
        s += "@data\n";
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                s += d[i][j] + ",";
            }
            s = s.substring(0, s.length() - 1);
            s += "\n";
        }
        writeToFile(file_path, s);
    }

    /**
     * get the distinct values from the array provided
     *
     * @param d searching array
     * @return distinct values array
     */
    public static float[] getDistinctValues(float[] d) {
        ArrayList<Float> lst = new ArrayList<>();
        for (int i = 0; i < d.length; i++) {
            if (!lst.contains(d[i])) {
                lst.add(d[i]);
            }
        }
        return toFloatArray(lst);
    }

    /**
     * get the unique/distinct values from the array provided
     *
     * @param d searching array
     * @return distinct values array
     */
    public static float[] getUniqueValues(float[] d) {
        return getDistinctValues(d);
    }

    /**
     * get the distinct values from the array provided
     *
     * @param d searching array
     * @return distinct values array
     */
    public static int[] getDistinctValues(int[] d) {
        ArrayList<Integer> lst = new ArrayList<>();
        for (int i = 0; i < d.length; i++) {
            if (!lst.contains(d[i])) {
                lst.add(d[i]);
            }
        }
        return toIntArray1D(lst);
    }

    /**
     * get the unique/distinct values from the array provided
     *
     * @param d searching array
     * @return distinct values array
     */
    public static int[] getUniqueValues(int[] d) {
        return getDistinctValues(d);
    }

    public static String readFromFileUTF8(String file_name) {
        File file = new File(file_name);
        if (!file.exists()) {
            showMessage(file_name + " isminde bir dosya yok");
            return null;
        }
        String ret = "";
        Charset charset = StandardCharsets.UTF_8;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            try {
                String s;
                while ((s = br.readLine()) != null) {
                    ret += s + "\n";
                }
                ret = ret.substring(0, ret.length() - 1);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static List<String> readFileAsList(String fileName) {
        List<String> ret = new ArrayList<>();
        String[] s = readFile(fileName).split("\n");
        int n = s.length;
        for (int i = 0; i < n; i++) {
            ret.add(s[i]);
        }
        return ret;
    }

    public static String readFile(String fileName) {
        String ret = "";
        StringBuilder builder = new StringBuilder("");
        File file = new File(fileName);
        if (!file.exists()) {
            showMessage(fileName + " isminde bir dosya yok");
            return ret;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String s;
            while ((s = br.readLine()) != null) {
                //ret = ret + s + "\n";
                builder.append(s).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ret;
        }

        return builder.toString();
    }

    public static String readFile() {
        String ret = "";
        File getFile = getFileFromChooserOpen();
        if (getFile == null) {
            return null;
        }
        String fileName = getFile.getAbsolutePath();
        return readFile(fileName);
    }

    public static String readFileUntil(String fileName, int index) {
        String ret = "";
        StringBuilder sBuilder = new StringBuilder();
        File file = new File(fileName);
        if (!file.exists()) {
            showMessage(fileName + " isminde bir dosya yok");
            return ret;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            char current;
            int k = 0;
            int val = 0;
            while (fis.available() > 0 && k++ < index) {
                val = fis.read();
                current = (char) val;
                sBuilder.append(current);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ret = sBuilder.toString();
        return ret;
    }

    /**
     * read matrix written as atext file with ; split token
     *
     * @return
     */
    public static float[][] readFromFile() {
        return readFromFile(getFileFromChooserOpen(getDefaultDirectory()).getAbsolutePath(), ";");
    }

    public static float[][] readFromFile(String token) {
        return readFromFile(getFileFromChooserOpen(getDefaultDirectory()).getAbsolutePath(), token);
    }

    public static float[][] readFromFile(String file_name, String token) {
        float[][] d = new float[1][1];
        ArrayList<float[]> lst = new ArrayList<>();
        File file = new File(file_name);
        if (!file.exists()) {
            showMessage(file_name + " isminde bir dosya yok");
            return d;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String s;
            while ((s = br.readLine()) != null) {
                if ((s.indexOf("@") != -1) || (s.indexOf("%") != -1)) {
                    continue;
                }
                float[] row = null;
                if (token.isEmpty()) {
                    row = new float[1];
                    row[0] = Float.parseFloat(s);
                } else {
                    String[] sd = s.split(token);
                    row = new float[sd.length];
                    for (int i = 0; i < sd.length; i++) {
//                        System.out.println("i = " + i+" val="+sd[i]);
                        row[i] = (float) Double.parseDouble(sd[i]);
                    }
                }
                lst.add(row);

            }
        } catch (IOException e) {
            e.printStackTrace();
            return d;
        }
        return lst.toArray(d);
    }

    public static ReaderCSV readFromCSVFile(String file_name) {
        ReaderCSV csv = new ReaderCSV();
        List<String> columnNames = new ArrayList<>();
        List<String> classLabels = new ArrayList<>();
        float[][] d = new float[1][1];
        ArrayList<float[]> lst = new ArrayList<>();
        File file = new File(file_name);
        if (!file.exists()) {
            showMessage(file_name + " does not exist please check it");
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String s = br.readLine().replace("\"", "");
            columnNames = Arrays.asList(s.split(","));
            while ((s = br.readLine()) != null) {
                float[] row = null;
                String[] sd = s.split(",");
                row = new float[sd.length];
                for (int i = 0; i < sd.length - 1; i++) {
                    row[i] = Float.parseFloat(sd[i]);
                }
                classLabels.add(sd[sd.length - 1].replace("\"", ""));
                lst.add(row);

            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        csv.data = toDoubleArray2D(lst.toArray(d));
        csv.columnNames = columnNames;
        csv.classLabels = classLabels;
        return csv;
    }

    public static String[][] readFromFileAsString(String file_name, String token) {
        String[][] d = new String[1][1];
        ArrayList<String[]> lst = new ArrayList<>();
        File file = new File(file_name);
        if (!file.exists()) {
            showMessage(file_name + " isminde bir dosya yok");
            return d;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String s;
            while ((s = br.readLine()) != null) {
                if (s.indexOf("@") != -1) {
                    continue;
                }
                String[] row = null;
                if (token.isEmpty()) {
                    row = new String[1];
                    row[0] = s;
                } else {
                    String[] sd = s.split(token);
                    row = new String[sd.length];
                    for (int i = 0; i < sd.length; i++) {
                        row[i] = sd[i];
                    }
                }
                lst.add(row);

            }
        } catch (IOException e) {
            e.printStackTrace();
            return d;
        }
        return lst.toArray(d);
    }

    public static String[] readFromFileAsString1D(String file_name) {
        ArrayList<String> lst = new ArrayList<>();
        File file = new File(file_name);
        if (!file.exists()) {
            showMessage(file_name + " isminde bir dosya yok");
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String s;
            while ((s = br.readLine()) != null) {
                if (s.indexOf("@") != -1) {
                    continue;
                }
                lst.add(s);

            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String[] d = {};
        return lst.toArray(d);
    }

    /**
     * Read sensor data from piezoelectric sensor file
     *
     * @param file_name
     * @param count
     * @return
     */
    public static float[][] readVibrationSensorDataFromFile(String file_name, int count) {
        float[][] d = new float[1][1];
        ArrayList<float[]> lst = new ArrayList<>();
        File file = new File(file_name);
        if (!file.exists()) {
            showMessage(file_name + " isminde bir dosya yok");
            return d;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file_name))) {
            String s;
            while ((s = br.readLine()) != null) {
                String[] sd = s.split("\\.");
                if (sd.length == 2) {
                    float[] q = new float[count];
                    for (int i = 0; i < count; i++) {
                        s = br.readLine();
                        try {
                            q[i] = Float.parseFloat(s);
                        } catch (Exception e) {
                            int a = 1;
                        }
                    }
                    lst.add(q);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return d;
        }
        d = lst.toArray(d);
        d = transpose(d);
        return d;
    }

    /**
     * Open Message Information Dialog Box
     * @param str:Message to show
     */
    public static void showMessage(String str) {
        JOptionPane.showMessageDialog(null, str);
//        System.out.println(str);
    }

    /**
     * Open Message Information Dialog Box for a specified time
     * @param str
     * @param delay : time to wait in ms
     * @param func
     */
    public static void showMessageTemp(String str, int delay, CallBackTrigger func) {
        JFrame frame = new JFrame("Custom Message");
        frame.setUndecorated(true);
        JLabel label = new JLabel(str, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(label);
        frame.pack();
        Dimension labelSize = label.getPreferredSize();
        frame.setSize(labelSize.width + 50, labelSize.height + 50); // İstediğiniz ekstra boşlukları ekleyebilirsiniz
        frame.setLocationRelativeTo(null); // Ekranın ortasına hizala
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Pencere kapatıldığında programı sonlandırma
        frame.setVisible(true);
        frame.repaint();
        Timer timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                func.trigger();
            }
        });
        timer.setRepeats(false);
        timer.setDelay(delay);
        timer.start();
    }

    public static String inputMessage(String message, String initialSelectionValue) {
        return JOptionPane.showInputDialog(message, initialSelectionValue);
    }

    public static String inputMessage(String str1) {
        return JOptionPane.showInputDialog(str1);
    }

    public static void yazln(String str) {
        System.out.println(str);
    }

    public static void yaz(String str) {
        System.out.print(str);
    }

    public static void yaz(float[] m) {
        yaz("[");
        for (int i = 0; i < m.length; i++) {
            yaz(m[i] + " ");
        }
        yaz("]\n");
    }

    public static void yaz(int[][] m) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                yaz(m[i][j] + " ");
            }
            yazln("");
        }
        yazln("");
    }

    private static float[][] getFloatMatrix(int[][] a) {
        float[][] d = new float[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                d[i][j] = (float) a[i][j];
            }
        }
        return d;
    }

    private static int[][] getIntMatrix(float[][] a) {
        int[][] d = new int[a.length][a[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                d[i][j] = (int) a[i][j];
            }
        }
        return d;
    }

    public static float[][] getSubMatrix(float[][] m, int p1x, int p1y, int p2x, int p2y) {
        int r = m.length;
        int c = m[0].length;
        if (p1x < 0 || p1y < 0 || p1x > p2x || p1y > p2y
                || p1x > r || p1y > c || p2x > r || p2y > c) {
            return m;
        } else {
            float[][] ret = new float[p2x - p1x + 1][p2y - p1y + 1];
            for (int i = p1x; i <= p2x; i++) {
                for (int j = p1y; j <= p2y; j++) {
                    ret[i - p1x][j - p1y] = m[i][j];
                }
            }
            return ret;
        }
    }

    public static int[][] getSubMatrix(int[][] m, CPoint p1, CPoint p2) {
        int r = m.length;
        int c = m[0].length;
        if (p1.row < 0 || p1.column < 0 || p1.row > p2.row || p1.column > p2.column
                || p1.row > r || p1.column > c || p2.row > r || p2.column > c) {
            return m;
        } else {
            int[][] ret = new int[p2.row - p1.row + 1][p2.column - p1.column + 1];
            for (int i = p1.row; i <= p2.row; i++) {
                for (int j = p1.column; j <= p2.column; j++) {
                    ret[i - p1.row][j - p1.column] = m[i][j];
                }
            }
            return ret;
        }
    }

    /**
     * Crop the matrix at the center point with the specified length from the
     * center as in the case of circle radious
     *
     * @param m:original matrix
     * @param cp:center point
     * @param r:window size
     * @return cropped matrix
     */
    public static float[][] crop(float[][] m, CPoint cp, int r) {
        int c1 = cp.column - r;
        int c2 = cp.column + r;
        int r1 = cp.row - r;
        int r2 = cp.row + r;
        if (c1 < 0 || r1 < 0 || c2 > m[0].length - 1 || r2 > m.length - 1) {
//            System.out.println("cropped matrix is outside from the matrix");
            return m;
        }
        return getSubMatrix(m, new CPoint(r1, c1), new CPoint(r2, c2));
    }

    /**
     * Crop the matrix from p1 to p2 position which are actually identical to
     * top-left and right-bottom positions
     *
     * @param m:original matrix
     * @param p1:top-left
     * @param p2:bottom-right
     * @return cropped matrix
     */
    public static float[][] crop(float[][] m, CPoint p1, CPoint p2) {
        return getSubMatrix(m, p1, p2);
    }

    /**
     * Crop the matrix from p1 to p2 position which are actually identical to
     * top-left and right-bottom positions
     *
     * @param m:original matrix
     * @param p1:top-left
     * @param p2:bottom-right
     * @return cropped matrix
     */
    public static float[][] getSubMatrix(float[][] m, CPoint p1, CPoint p2) {
        int r = m.length;
        int c = m[0].length;
        if (p1.row < 0 || p1.column < 0 || p1.row > p2.row || p1.column > p2.column
                || p1.row > r || p1.column > c || p2.row > r || p2.column > c) {
            return m;
        } else {
            float[][] ret = new float[p2.row - p1.row + 1][p2.column - p1.column + 1];
            for (int i = p1.row; i < p2.row; i++) {
                for (int j = p1.column; j < p2.column; j++) {
                    ret[i - p1.row][j - p1.column] = m[i][j];
                }
            }
            return ret;
        }
    }

    public static float getSum(float[] d) {
        float ret = 0;
        for (int i = 0; i < d.length; i++) {
            ret += d[i];
        }
        return ret;
    }

    public static int getSum(int[] d) {
        int ret = 0;
        for (int i = 0; i < d.length; i++) {
            ret += d[i];
        }
        return ret;
    }

//    public static float getSum(float[] d) {
//        float ret = 0;
//        for (int i = 0; i < d.length; i++) {
//            ret += d[i];
//        }
//        return ret;
//    }
    public static float getSum(float[][] d) {
        float ret = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret += d[i][j];
            }
        }
        return ret;
    }

    public static int getSum(int[][] d) {
        int ret = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret += d[i][j];
            }
        }
        return ret;
    }
//
//    public static float getSum(float[][] d) {
//        float ret = 0;
//        for (int i = 0; i < d.length; i++) {
//            for (int j = 0; j < d[0].length; j++) {
//                ret += d[i][j];
//            }
//        }
//        return ret;
//    }

    public static float getPixelCount(int[][] m) {
        if (m == null) {
            return 0.0f;
        }
        float ret = 0.0f;
        float toplam = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                if (m[i][j] != 0) {
                    toplam++;
                }
            }
        }
        return toplam;
    }

    public static ArrayList<Point> getROIPos(ArrayList lst, int threshold) {
        ArrayList<Point> ret = new ArrayList<>();
        ArrayList posList = new ArrayList();
        for (Object obj : lst) {
            ArrayList<Point> ps = (ArrayList<Point>) obj;
            ArrayList<Point> pos = new ArrayList<>();
            int delta;
            ArrayList<Integer> v = new ArrayList<>();
            for (int i = 0; i < ps.size() - 1; i++) {
                delta = Math.abs(ps.get(i).y - ps.get(i + 1).y);
                if (delta != 0 && delta < threshold) {
                    v.add(ps.get(i).y);
                } else {
                    int tp = 0;
                    for (Integer nt : v) {
                        tp += nt;
                    }
                    Point p = new Point();
                    p.x = ps.get(i).x;
                    p.y = tp / v.size();
                    pos.add(p);
                    v.clear();
                }
            }
            posList.add(pos);
        }
        return ret;
    }

    /**
     * Project matrix on X axis or row axis
     *
     * @param m
     * @return
     */
    public static int[] getProjectedMatrixOnX(int[][] m) {
        int[] d = new int[m[0].length];
        for (int j = 0; j < d.length; j++) {
            for (int i = 0; i < m.length; i++) {
                d[j] += m[i][j];
            }
        }
        return d;
    }

    /**
     * Project matrix on X axis or row axis
     *
     * @param m
     * @return
     */
    public static int[] getProjectedMatrixOnX(float[][] m) {
        int[] d = new int[m[0].length];
        for (int j = 0; j < m[0].length; j++) {
            for (int i = 0; i < m.length; i++) {
                d[j] += m[i][j];
            }
        }
        return d;
    }

    /**
     * Project matrix on Y axis or column axis
     *
     * @param m
     * @return
     */
    public static int[] getProjectedMatrixOnY(int[][] m) {
        int[] d = new int[m.length];
        for (int j = 0; j < m.length; j++) {
            for (int i = 0; i < m[0].length; i++) {
                d[j] += m[j][i];
            }
        }
        return d;
    }

    /**
     * Project matrix on Y axis or Column axis
     *
     * @param m
     * @return
     */
    public static int[] getProjectedMatrixOnY(float[][] m) {
        int[] d = new int[m.length];
        for (int j = 0; j < m.length; j++) {
            for (int i = 0; i < m[0].length; i++) {
                d[j] += m[j][i];
            }
        }
        return d;
    }

    public static TWord[] getPoints(int[] d) {
        ArrayList<Integer> lst = new ArrayList<>();
        ArrayList<Integer> lstW = new ArrayList<>();
        //binarize image
        for (int i = 0; i < d.length; i++) {
            if (d[i] != 0) {
                d[i] = 255;
            }
        }

        //1D chain algorithm threshold is 10 which means pixel is assigned at most 10 aparture or gap
        @SuppressWarnings("UseOfObsoleteCollectionType")
        Vector<Integer> v = new Vector<>();
        int gap = 0;
        for (int i = 0; i < d.length; i++) {
            if (d[i] != 0) {
                v.add(i);
                gap = 0;
            } else {
                gap++;
                if (gap < 10 && i < d.length - 1) {
                    continue;
                } else {
                    if (!v.isEmpty() && v.size() > 10 && v.size() < 80) {
                        int mid = (v.firstElement() + v.lastElement()) / 2;
                        int w = Math.abs(v.lastElement() - v.firstElement());
                        lst.add(mid);
                        lstW.add(w);
                        v.removeAllElements();
                        gap = 0;
                    } else {
                        v.removeAllElements();
                        gap = 0;
                        continue;
                    }
                }
            }
        }
        TWord[] ret = new TWord[lst.size()];
        for (int i = 0; i < lst.size(); i++) {
            ret[i] = new TWord();
            ret[i].centerPos = lst.get(i);
            ret[i].width = lstW.get(i);
        }
        return ret;
    }

    public static String reverseString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            str += s.charAt(s.length() - 1 - i);
        }
        return str;
    }

    public static String getDefaultDirectory() {
        String workingDir = System.getProperty("user.dir");
        return workingDir;
    }

    public static String getCurrentDirectory() {
        return getDefaultDirectory();
    }

    public static String getWorkingDirectory() {
        return getDefaultDirectory();
    }

    public static float[] toFloatArray1D(int[] m) {
        float[] ret = new float[m.length];
        for (int i = 0; i < m.length; i++) {
            ret[i] = m[i] * 1.0f;
        }
        return ret;
    }

    public static float[] toFloatArray1D(long[] m) {
        float[] ret = new float[m.length];
        for (int i = 0; i < m.length; i++) {
            ret[i] = m[i] * 1.0f;
        }
        return ret;
    }

    public static float[] toFloatArray1D(byte[] m) {
        float[] ret = new float[m.length];
        for (int i = 0; i < m.length; i++) {
            ret[i] = m[i] * 1.0f;
        }
        return ret;
    }

    public static float[] toFloatArray1D(double[] m) {
        float[] ret = new float[m.length];
        for (int i = 0; i < m.length; i++) {
            ret[i] = (float) m[i];
        }
        return ret;
    }

    public static float[] toFloatArray1D(String[] m) {
        float[] ret = new float[m.length];
        for (int i = 0; i < m.length; i++) {
            ret[i] = Float.parseFloat(m[i]);
        }
        return ret;
    }

    public static String UTM2DMS(int zone, double px, double py, char Letter) {
        String[] s = UTM2Deg(zone, px, py, Letter).split(" ");
        double d1 = Double.parseDouble(s[0]);
        double deg1 = (int) d1;
        double m1 = (int) ((d1 - deg1) * 60);
        double s1 = ((d1 - deg1) * 60 - m1) * 60;
        String str1 = deg1 + ":" + m1 + ":" + s1;

        double d2 = Double.parseDouble(s[1]);
        double deg2 = (int) d2;
        double m2 = (int) ((d2 - deg2) * 60);
        double s2 = ((d2 - deg2) * 60 - m2) * 60;
        String str2 = deg2 + ":" + m2 + ":" + s2;
        return str1 + " " + str2;
    }

    public static String UTM2Deg(int zone, double px, double py, char Letter) {
        double Easting = px;
        double Northing = py;
        double Hem;
        if (Letter > 'M') {
            Hem = 'N';
        } else {
            Hem = 'S';
        }
        double north;
        if (Hem == 'S') {
            north = Northing - 10000000;
        } else {
            north = Northing;
        }
        double latitude = (north / 6366197.724 / 0.9996 + (1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) - 0.006739496742 * Math.sin(north / 6366197.724 / 0.9996) * Math.cos(north / 6366197.724 / 0.9996) * (Math.atan(Math.cos(Math.atan((Math.exp((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3)) - Math.exp(-(Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3))) / 2 / Math.cos((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996))) * Math.tan((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996)) - north / 6366197.724 / 0.9996) * 3 / 2) * (Math.atan(Math.cos(Math.atan((Math.exp((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3)) - Math.exp(-(Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3))) / 2 / Math.cos((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996))) * Math.tan((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996)) - north / 6366197.724 / 0.9996)) * 180 / Math.PI;
        latitude = Math.round(latitude * 10000000);
        latitude = latitude / 10000000;
        double longitude = Math.atan((Math.exp((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3)) - Math.exp(-(Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) / 3))) / 2 / Math.cos((north - 0.9996 * 6399593.625 * (north / 6366197.724 / 0.9996 - 0.006739496742 * 3 / 4 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.pow(0.006739496742 * 3 / 4, 2) * 5 / 3 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 - Math.pow(0.006739496742 * 3 / 4, 3) * 35 / 27 * (5 * (3 * (north / 6366197.724 / 0.9996 + Math.sin(2 * north / 6366197.724 / 0.9996) / 2) + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 4 + Math.sin(2 * north / 6366197.724 / 0.9996) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2) * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) / 3)) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))) * (1 - 0.006739496742 * Math.pow((Easting - 500000) / (0.9996 * 6399593.625 / Math.sqrt((1 + 0.006739496742 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)))), 2) / 2 * Math.pow(Math.cos(north / 6366197.724 / 0.9996), 2)) + north / 6366197.724 / 0.9996)) * 180 / Math.PI + zone * 6 - 183;
        longitude = Math.round(longitude * 10000000);
        longitude = longitude / 10000000;
        return latitude + " " + longitude;
    }

    public static String degMS2UTM(String s1, String s2) {
        double lat = Double.parseDouble(s1.split(":")[0]) + Double.parseDouble(s1.split(":")[1]) / 60.0 + Double.parseDouble(s1.split(":")[2]) / 3600.0;
        double longt = Double.parseDouble(s2.split(":")[0]) + Double.parseDouble(s2.split(":")[1]) / 60.0 + Double.parseDouble(s2.split(":")[2]) / 3600.0;
        return deg2UTM(lat, longt);
    }

    public static String deg2UTM(double Lat, double Lon) {
        double Easting;
        double Northing;
        int Zone = (int) Math.floor(Lon / 6 + 31);
        char Letter;
        if (Lat < -72) {
            Letter = 'C';
        } else if (Lat < -64) {
            Letter = 'D';
        } else if (Lat < -56) {
            Letter = 'E';
        } else if (Lat < -48) {
            Letter = 'F';
        } else if (Lat < -40) {
            Letter = 'G';
        } else if (Lat < -32) {
            Letter = 'H';
        } else if (Lat < -24) {
            Letter = 'J';
        } else if (Lat < -16) {
            Letter = 'K';
        } else if (Lat < -8) {
            Letter = 'L';
        } else if (Lat < 0) {
            Letter = 'M';
        } else if (Lat < 8) {
            Letter = 'N';
        } else if (Lat < 16) {
            Letter = 'P';
        } else if (Lat < 24) {
            Letter = 'Q';
        } else if (Lat < 32) {
            Letter = 'R';
        } else if (Lat < 40) {
            Letter = 'S';
        } else if (Lat < 48) {
            Letter = 'T';
        } else if (Lat < 56) {
            Letter = 'U';
        } else if (Lat < 64) {
            Letter = 'V';
        } else if (Lat < 72) {
            Letter = 'W';
        } else {
            Letter = 'X';
        }
        Easting = 0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.62 / Math.pow((1 + Math.pow(0.0820944379, 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)), 0.5) * (1 + Math.pow(0.0820944379, 2) / 2 * Math.pow((0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) / 3) + 500000;
        Easting = Math.round(Easting * 100) * 0.01;
        Northing = (Math.atan(Math.tan(Lat * Math.PI / 180) / Math.cos((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) + 0.9996 * 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 3);
        if (Letter < 'M') {
            Northing = Northing + 10000000;
        }
        Northing = Math.round(Northing * 100) * 0.01;
        String ret = Easting + " " + Northing + " " + Zone;
        return ret;
    }

    public static int[] toIntArray1D(byte[] m) {
        int[] ret = new int[m.length];
        for (int i = 0; i < m.length; i++) {
            ret[i] = (((int) m[i]) & 0xFF);  //m[i]&0xFF;
        }
        return ret;
    }

    public static int[] toIntArray1D(short[] m) {
        int[] ret = new int[m.length];
        for (int i = 0; i < m.length; i++) {
            ret[i] = (short) m[i];
        }
        return ret;
    }

    public static int[] toIntArray1D(char[] m) {
        int[] ret = new int[m.length];
        for (int i = 0; i < m.length; i++) {
            ret[i] = m[i];
        }
        return ret;
    }

//    public static float[] toFloatArray1D(float[] m) {
//        float[] ret = new float[m.length];
//        for (int i = 0; i < m.length; i++) {
//            ret[i] = m[i];
//        }
//        return ret;
//    }
    public static float[][] toFloatArray2D(int[][] m) {
        int nr = m.length;
        int nc = m[0].length;
        float[][] ret = new float[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = m[i][j] * 1.0f;
            }
        }
        return ret;
    }

    public static float[] toFloatArray1D(float[][] m) {
        float[] ret = new float[m.length * m[0].length];
        int k = 0;
        for (int j = 0; j < m[0].length; j++) {
            for (int i = 0; i < m.length; i++) {
                ret[k++] = m[i][j];
            }
        }
        return ret;
    }

    public static float[] toFloatArray1D_eski(float[][] m) {
        float[] ret = new float[m.length * m[0].length];
        int k = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                ret[k++] = m[i][j];
            }
        }
        return ret;
    }

    public static Object[] toFloatArray1D(Object[][] m) {
        Object[] ret = new Object[m.length * m[0].length];
        int k = 0;
        for (int j = 0; j < m[0].length; j++) {
            for (int i = 0; i < m.length; i++) {
                ret[k++] = m[i][j];
            }
        }
        return ret;
    }

    public static float[][] toFloatArray2D(byte[][] m) {
        float[][] ret = new float[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                ret[i][j] = m[i][j] * 1.0f;
            }
        }
        return ret;
    }

    public static float[][] toFloatArray2D(double[][] m) {
        float[][] ret = new float[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                ret[i][j] = (float) m[i][j];
            }
        }
        return ret;
    }

    public static float[][] toFloatArray2D(double[] m, int nr, int nc) {
        float[][] ret = new float[nr][nc];
        int k = 0;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (float) m[k++];
            }
        }
        return ret;
    }

    public static float[][] toFloatArray2D(float[] m, int nr, int nc) {
        float[][] ret = new float[nr][nc];
        int k = 0;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (float) m[k++];
            }
        }
        return ret;
    }

    public static float[][][] toFloatArray3D(int[][][] m) {
        float[][][] ret = new float[m.length][m[0].length][m[0][0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                for (int k = 0; k < m[0][0].length; k++) {
                    ret[i][j][k] = m[i][j][k] * 1.0f;
                }
            }
        }
        return ret;
    }

    public static float[][][] toFloatArray3D(double[][][] m) {
        int d1 = m.length;
        int d2 = m[0].length;
        int d3 = m[0][0].length;

        float[][][] ret = new float[d1][d2][d3];
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2; j++) {
                for (int k = 0; k < d3; k++) {
                    ret[i][j][k] = (float) m[i][j][k];
                }
            }
        }
        return ret;
    }

    /**
     * encrypt some value with salt value by means of xor knowing that float xor
     * with salt yields original value
     *
     * @param original
     * @param salt
     * @return
     */
    public static int encrypt(int original, int salt) {
        return original ^ salt;
    }

    /**
     * decrypt encrypted value with salt value by means of xor knowing that
     * float xor with salt yields original value
     *
     * @param encrypted
     * @param salt
     * @return
     */
    public static int decrypt(int encrypted, int salt) {
        return encrypted ^ salt;
    }

    /**
     *
     * @param m
     * @return
     */
    public static float[] toFloatArray(Vector m) {
        float[] ret = new float[m.size()];
        for (int i = 0; i < m.size(); i++) {
            ret[i] = (float) m.get(i);
        }
        return ret;
    }

    public static float[] toFloatArray(ArrayList m) {
        float[] ret = new float[m.size()];
        for (int i = 0; i < m.size(); i++) {
            ret[i] = (float) m.get(i);
        }
        return ret;
    }

    public static int[] toIntArray1D(float[] m) {
        int[] ret = new int[m.length];
        for (int i = 0; i < m.length; i++) {
            ret[i] = (int) Math.round(m[i]);
        }
        return ret;
    }

    public static int[][] toIntArray2D(float[][] m) {
        int nr = m.length;
        int nc = m[0].length;
        int[][] ret = new int[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
//                ret[i][j] = (int) Math.round(m[i][j]);
                ret[i][j] = (int) m[i][j];
            }
        }
        return ret;
    }

    public static int[][] trunc(float[][] m) {
        int[][] ret = new int[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                ret[i][j] = (int) m[i][j];
            }
        }
        return ret;
    }

    public static short[][] toShortArray2D(float[][] m) {
        short[][] ret = new short[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                ret[i][j] = (short) Math.round(m[i][j]);
            }
        }
        return ret;
    }

    public static byte[][] toByteArray2D(float[][] m) {
        byte[][] ret = new byte[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                ret[i][j] = (byte) Math.round(m[i][j]);
            }
        }
        return ret;
    }

//    public static float[][] toFloatArray2D(float[][] m) {
//        float[][] ret = new float[m.length][m[0].length];
//        for (int i = 0; i < m.length; i++) {
//            for (int j = 0; j < m[0].length; j++) {
//                ret[i][j] = (float) Math.round(m[i][j]);
//            }
//        }
//        return ret;
//    }
    public static long[][] toLongArray2D(float[][] m) {
        long[][] ret = new long[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                ret[i][j] = (long) Math.round(m[i][j]);
            }
        }
        return ret;
    }

    public static String[][] toStringArray2D(float[][] m) {
        String[][] ret = new String[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
//                ret[i][j] = String.valueOf(Math.round(m[i][j]));
                ret[i][j] = String.valueOf(formatFloat(m[i][j], 3));
            }
        }
        return ret;
    }

//    public static int[] toIntArray1D(float[] m) {
//        int[] ret = new int[m.length];
//        for (int i = 0; i < m.length; i++) {
//            ret[i] = (int) Math.round(m[i]);
//        }
//        return ret;
//    }
//    public static int[][] toIntArray2D(float[][] m) {
//        int[][] ret = new int[m.length][m[0].length];
//        for (int i = 0; i < m.length; i++) {
//            for (int j = 0; j < m[0].length; j++) {
//                ret[i][j] = (int) Math.round(m[i][j]);
//            }
//        }
//        return ret;
//    }
    public static int[][] toIntArray2D(byte[][] m) {
        int[][] ret = new int[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                ret[i][j] = (int) m[i][j];
            }
        }
        return ret;
    }

    public static int[] toIntArray1D(Vector m) {
        int[] ret = new int[m.size()];
        for (int i = 0; i < m.size(); i++) {
            ret[i] = (int) m.get(i);
        }
        return ret;
    }

    public static int[] toIntArray1D(ArrayList m) {
        int[] ret = new int[m.size()];
        for (int i = 0; i < m.size(); i++) {
            ret[i] = (int) m.get(i);
        }
        return ret;
    }

    public static float formatFloat(float num) {
        float q = 0f;
        try {
            DecimalFormat df = new DecimalFormat("#.###");
            q = Float.parseFloat(df.format(num).replace(",", "."));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return q;
    }

    public static String getFileExtension(File file) {
        String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.') + 1);
        return extension;
    }

    public static String getFileExtension(String str) {
        String extension = str.substring(str.lastIndexOf('.') + 1);
        return extension;
    }

    /**
     * parse the file name from file path string
     *
     * @param str:file path contains filename
     * @return file name
     */
    public static String getFileNameFromPath(String str) {
        if (str == null || str.equals("") || str.isEmpty()) {
            return null;
        }
        Path p = Paths.get(str);
        String fileName = p.getFileName().toString();
        return fileName;
    }

    /**
     * parse the file name from file path string
     *
     * @param str:file path contains filename
     * @return file name
     */
    public static String getParentFromPath(String str) {
        if (str == null || str.equals("") || str.isEmpty()) {
            return null;
        }
        Path p = Paths.get(str);
        String fileName = p.getParent().toString();
        return fileName;
    }

    /**
     * parse the file name from composite file structure/name
     *
     * @param str:file path contains filename
     * @return file name
     */
    public static String getFileName(String str) {
        String extension = str.substring(str.lastIndexOf('.') + 1);
        String fileName = getFileNameFromPath(str);
        if (extension.equals(fileName)) {
            return fileName;
        } else {
            if (fileName.lastIndexOf(extension) == -1) {
                return null;
            }
            String ret = fileName.substring(0, fileName.lastIndexOf(extension) - 1);
            return ret;
        }
    }

    /**
     * b.try to format float number with 3 digit precision by default
     *
     * @param num
     * @return
     */
    public static float[] formatFloat(float[] num) {
        float[] q = new float[num.length];
        for (int i = 0; i < num.length; i++) {
            q[i] = formatFloat(num[i]);
        }
        return q;
    }

    /**
     * b.try to format float number with 3 digit precision by default
     *
     * @param num
     * @return
     */
    public static float[][] formatFloat(float[][] num) {
        float[][] q = new float[num.length][num[0].length];
        for (int i = 0; i < num.length; i++) {
            for (int j = 0; j < num[0].length; j++) {
                q[i][j] = formatFloat(num[i][j]);
            }
        }
        return q;
    }

    /**
     * b.try to format float number with precision number
     *
     * @param num
     * @return
     */
    public static float[][] formatFloat(float[][] num, int precision) {
        float[][] q = new float[num.length][num[0].length];
        for (int i = 0; i < num.length; i++) {
            for (int j = 0; j < num[0].length; j++) {
                q[i][j] = formatFloat(num[i][j], precision);
            }
        }
        return q;
    }

    public static double[][] formatDouble(double[][] num, int precision) {
        double[][] q = new double[num.length][num[0].length];
        for (int i = 0; i < num.length; i++) {
            for (int j = 0; j < num[0].length; j++) {
                q[i][j] = formatDouble(num[i][j], precision);
            }
        }
        return q;
    }

    /**
     * b.try to format float number with n digit precision by default
     *
     * @param num
     * @return
     */
    public static float[] formatFloat(float[] num, int n) {
        float[] q = new float[num.length];
        for (int i = 0; i < num.length; i++) {
            q[i] = formatFloat(num[i], n);
        }
        return q;
    }

    /**
     * b.try to format float number with 3 digit precision by default
     *
     * @param num
     * @return
     */
    public static double formatDouble(double num) {
        float q = 0;
        try {
            DecimalFormat df = new DecimalFormat("#.000");
            q = Float.parseFloat(df.format(num).replace(",", "."));
        } catch (Exception e) {
//            e.printStackTrace();
            return -10000000000000.0;
        }
        return q;
    }

    public static String formatFloatAsString(float num, int n) {
        float x = formatFloat(num, n);
        String ret = Float.toString(x);
        if (x % 1 == 0) {
            ret = ret.substring(0, ret.indexOf("."));
        }
        if (ret.length() > 10) {
            // çok büyük sayılar için bilimsel gösterim yap
            ret = String.format("%5.2e", Float.parseFloat(ret));
        }
        return ret;
    }

    public static float formatFloat(float num, int n) {
        float q = 0;
        try {
            DecimalFormat df = null;
            switch (n) {
                case 0:
                    return (float) Math.floor(num);
                case 1:
                    df = new DecimalFormat("#.0");
                    break;
                case 2:
                    df = new DecimalFormat("#.00");
                    break;
                case 3:
                    df = new DecimalFormat("#.000");
                    break;
                case 4:
                    df = new DecimalFormat("#.0000");
                    break;
                case 5:
                    df = new DecimalFormat("#.00000");
                    break;
                default:
                    df = new DecimalFormat("#.000");
                    break;

            }
//            DecimalFormat df = new DecimalFormat("#.###");
            q = Float.parseFloat(df.format(num).replace(",", "."));
        } catch (Exception e) {
//            e.printStackTrace();
            return 0;
        }
        return q;
    }

    public static double formatDouble(double num, int n) {
        float q = 0;
        try {
            DecimalFormat df = null;
            switch (n) {
                case 0:
                    return (float) Math.floor(num);
                case 1:
                    df = new DecimalFormat("#.0");
                    break;
                case 2:
                    df = new DecimalFormat("#.00");
                    break;
                case 3:
                    df = new DecimalFormat("#.000");
                    break;
                case 4:
                    df = new DecimalFormat("#.0000");
                    break;
                case 5:
                    df = new DecimalFormat("#.00000");
                    break;
                default:
                    df = new DecimalFormat("#.000");
                    break;

            }
//            DecimalFormat df = new DecimalFormat("#.###");
            q = Float.parseFloat(df.format(num).replace(",", "."));
        } catch (Exception e) {
//            e.printStackTrace();
            return 0;
        }
        return q;
    }

    /*
    public static float[] clone(float[] d) {
        float[] ret = new float[d.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = d[i];
        }
        return ret;
    }

    public static float[][] clone(float[][] d) {
        float[][] ret = new float[d.length][d[0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[0].length; j++) {
                ret[i][j] = d[i][j];
            }
        }
        return ret;
    }

    public static float[] clone(float[] d) {
        float[] ret = new float[d.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = d[i];
        }
        return ret;
    }

    public static float[][] clone(float[][] d) {
        float[][] ret = new float[d.length][d[0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < d[i].length; j++) {
                ret[i][j] = d[i][j];
            }
        }
        return ret;
    }

    public static float[][][] clone(float[][][] d) {
        if (d == null) {
            return null;
        }
        float[][][] ret = new float[d.length][d[0].length][d[0][0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[0].length; j++) {
                for (int k = 0; k < ret[0][0].length; k++) {
                    ret[i][j][k] = d[i][j][k];
                }
            }
        }
        return ret;
    }

    public static int[] clone(int[] d) {
        int[] ret = new int[d.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = d[i];
        }
        return ret;
    }

    public static String[] clone(String[] d) {
        String[] ret = new String[d.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = d[i];
        }
        return ret;
    }

    public static int[][] clone(int[][] d) {
        int[][] ret = new int[d.length][d[0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[0].length; j++) {
                ret[i][j] = d[i][j];
            }
        }
        return ret;
    }

    public static byte[][] clone(byte[][] d) {
        byte[][] ret = new byte[d.length][d[0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[0].length; j++) {
                ret[i][j] = d[i][j];
            }
        }
        return ret;
    }
     */
    public static int[] sortArrayAndReturnIndex(float[] p, String t) {
        int[] ret = new int[p.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = i;
        }
        if (t.equals("desc")) {
            //buyukten kucuge sirala
            float temp = p[0];
            int q = 0;
            for (int i = 0; i < p.length; i++) {
                for (int j = i; j < p.length; j++) {
                    if (p[j] > p[i]) {
                        temp = p[i];
                        p[i] = p[j];
                        p[j] = temp;

                        q = ret[i];
                        ret[i] = ret[j];
                        ret[j] = q;
                    }
                }
            }
        }
        if (t.equals("asc")) {
            //kucukten buyuge sirala
            float temp = 0;
            int q = 0;
            for (int i = 0; i < p.length; i++) {
                for (int j = i; j < p.length; j++) {
                    if (p[j] < p[i]) {
                        temp = p[i];
                        p[i] = p[j];
                        p[j] = temp;

                        q = ret[i];
                        ret[i] = ret[j];
                        ret[j] = q;
                    }
                }
            }
        }
        return ret;
    }

    public static String convertArrayToString(float[] p) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + ";";
        }
        return s;
    }

    public static String toString(float[] p, String token) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + token;
        }
        return s;
    }

//    public static String toString(float[] p, String token) {
//        String s = "";
//        for (int i = 0; i < p.length; i++) {
//            s += p[i] + token;
//        }
//        return s;
//    }
    public static String toString(long[] p, String token) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + token;
        }
        return s;
    }

    public static String toString(int[] p, String token) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + token;
        }
        return s;
    }

    public static String toString(short[] p, String token) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + token;
        }
        return s;
    }

    public static String toString(byte[] p, String token) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + token;
        }
        return s;
    }

    public static String toString(char[] p, String token) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + token;
        }
        return s;
    }

    public static String toString(boolean[] p, String token) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + token;
        }
        return s;
    }

    public static String toString(String[] p, String token) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + token;
        }
        return s;
    }

    public static String convertArrayToString(int[] p) {
        String s = "";
        for (int i = 0; i < p.length; i++) {
            s += p[i] + ";";
        }
        return s;
    }

    public static boolean isExistInTheList(int[] d, Vector<int[]> list) {
        int[] temp = null;
        boolean flag = false;
        for (int i = 0; i < list.size(); i++) {
            temp = list.get(i);
            flag = true;
            for (int j = 0; j < d.length; j++) {
                if (!containsArray(d[j], temp)) {
                    flag = false;
                }
            }
            if (flag) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsArray(int i, int[] temp) {
        for (int j = 0; j < temp.length; j++) {
            if (i == temp[j]) {
                return true;
            }
        }
        return false;
    }

    public static String getFolderPath(String path) {
        return new File(path).getParent();
    }

    public static int[] sortArray(int[] a) {
        int[] b = a.clone();
        int t = 0;
        for (int i = 0; i < a.length; i++) {
            for (int j = i; j < a.length; j++) {
                if (b[i] > b[j]) {
                    t = b[i];
                    b[i] = b[j];
                    b[j] = t;
                }
            }
        }
        return b;
    }

    public static float[] sortArrayAscend(float[] a) {
        Arrays.sort(a);
        return a;
    }

    public static float[] sortArrayDescend(float[] a) {
        float[] d = new float[a.length];
        Arrays.sort(a);
        int n = a.length;
        for (int i = 0; i < n; i++) {
            d[i] = a[n - 1 - i];
        }
        return d;
    }

    public static float[] sortArray(float[] a, int[] index) {
        float[] b = a.clone();
        for (int i = 0; i < a.length; i++) {
            b[i] = a[index[i]];
        }
        return b;
    }

    public static String getRandomSubset(String[] set) {
        Random rnd = new Random();
        int n = rnd.nextInt(set.length - 1);
        return set[n];
    }

    /**
     * Önce seçilen bir combinasyonun sağ ve solunda gaussian miktar kadar ötede
     * bir komşu kombinasyon araştırır. Eğer ötelenen yer pozitif yönde ise ve
     * shift miktarı ile konumlanan index set in uzunluğundan küçük ise shift
     * ekelenip ilgili kombinasyon split edilerek döndürülür değilse circle
     * mantığı kullanılarak dizinin başına taşar. Negatif için de durum
     * böyledir.
     *
     * @param prevComb
     * @param set
     * @return
     */
    public static String getNeighborSubset(String prevComb, String[] set) {
        int n = find(prevComb, set);
        String ret = set[0];
        if (n != -1) {
            int k = (int) (new Random().nextGaussian() * 5);
            if (k >= 0) {
                if (n + k > set.length - 1) {
                    ret = set[Math.abs((n + k) - (set.length - 1))];
                } else {
                    ret = set[n + k];
                }
            } else {
                if (n + k < 0) {
                    ret = set[Math.abs((set.length - 1) - (n + k))];
                } else {
                    ret = set[n + k];
                }

            }
        }
        return ret;
    }

    private static int find(String prev, String[] set) {
        for (int i = 0; i < set.length; i++) {
            if (prev.equals(set[i])) {
                return i;
            }
        }
        return -1;
    }

    public static void convertImageToDifferentDPI(BufferedImage sourceImage, String destFile, int dpi, float... maxValue) {
        try {
            File destinationFile = new File(destFile);
            ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(destinationFile);
            imageWriter.setOutput(ios);
            ImageWriteParam jpegParams = imageWriter.getDefaultWriteParam();

            IIOMetadata data = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(sourceImage), jpegParams);
            Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0f");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", Integer.toString(dpi));
            jfif.setAttribute("Ydensity", Integer.toString(dpi));
            jfif.setAttribute("resUnits", "1"); // density is dots per inch
            data.mergeTree("javax_imageio_jpeg_image_1.0f", tree); // Write and clean up

            imageWriter.write(data, new IIOImage(sourceImage, null, data), jpegParams);
            ios.close();
            imageWriter.dispose();

        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * bir matrisin bütün rowlarını index dizisine göre sıralar
     *
     * @param m
     * @param index
     * @return
     */
    public static CMatrix sortRows(CMatrix m, int[] index) {
        float[][] d = m.getArray2Dfloat();
        for (int i = 0; i < d.length; i++) {
            d[i] = sortArray(d[i], index);
        }
        m.setArray(d);
        return m.transpose();
    }

    public static float[][] transpose(float[][] d) {
        float[][] ret = new float[d[0].length][d.length];
        for (int i = 0; i < d[0].length; i++) {
            for (int j = 0; j < d.length; j++) {
                ret[i][j] = d[j][i];
            }
        }
        return ret;
    }

    public static int[][] transpose(int[][] d) {
        int[][] ret = new int[d[0].length][d.length];
        for (int i = 0; i < d[0].length; i++) {
            for (int j = 0; j < d.length; j++) {
                ret[i][j] = d[j][i];
            }
        }
        return ret;
    }

//    public static float[][] transpose(float[][] d) {
//        float[][] ret = new float[d[0].length][d.length];
//        for (int i = 0; i < d[0].length; i++) {
//            for (int j = 0; j < d.length; j++) {
//                ret[i][j] = d[j][i];
//            }
//        }
//        return ret;
//    }
    public static byte[][] transpose(byte[][] d) {
        byte[][] ret = new byte[d[0].length][d.length];
        for (int i = 0; i < d[0].length; i++) {
            for (int j = 0; j < d.length; j++) {
                ret[i][j] = d[j][i];
            }
        }
        return ret;
    }

    public static String[][] transpose(String[][] d) {
        String[][] ret = new String[d[0].length][d.length];
        for (int i = 0; i < d[0].length; i++) {
            for (int j = 0; j < d.length; j++) {
                ret[i][j] = d[j][i];
            }
        }
        return ret;
    }

    public static Object[][] transpose(Object[][] d) {
        Object[][] ret = new Object[d[0].length][d.length];
        for (int i = 0; i < d[0].length; i++) {
            for (int j = 0; j < d.length; j++) {
                ret[i][j] = d[j][i];
            }
        }
        return ret;
    }

//    public static float[] to1D(float[][] d) {
//        int r = d.length;
//        int c = d[0].length;
//        float[] ret = new float[r * c];
//        int k = 0;
//        for (int j = 0; j < c; j++) {
//            for (int i = 0; i < r; i++) {
//                ret[k++] = d[i][j];
//            }
//        }
//        return ret;
//    }
//
    public static float[][] reshape(float[][] d, int r, int c) {
        if (d.length * d[0].length != r * c) {
            System.err.println("size mismatch");
//            showMessage("size mismatch");
            return d;
        }
        float[][] ret = new float[r][c];
        float[] a = toFloatArray1D(d);
        int k = 0;
        for (int j = 0; j < c; j++) {
            for (int i = 0; i < r; i++) {
                ret[i][j] = a[k++];
            }
        }
        return ret;
    }

    public static float[][] reshape(float[] d, int r, int c) {
        float[][] ret = new float[r][c];
        if (d.length != r * c) {
            showMessage("size mismatch");
            return ret;
        }
        int k = 0;
        for (int j = 0; j < c; j++) {
            for (int i = 0; i < r; i++) {
                ret[i][j] = d[k++];
            }
        }
        return ret;
    }

    public static float[] flatten(float[][] d) {
        int n1 = d.length;
        int n2 = d[0].length;
        int n = n1 * n2;
        float[] ret = new float[n];
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                ret[i * n2 + j] = d[i][j];
            }
        }
        return ret;
    }

    public static float[] flatten(float[][][] d) {
        int n1 = d.length;
        int n2 = d[0].length;
        int n3 = d[0][0].length;
        int n = n1 * n2 * n3;
        float[] ret = new float[n];
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                for (int k = 0; k < n3; k++) {
                    ret[i * n2 * n3 + j * n3 + k] = d[i][j][k];
                }

            }
        }
        return ret;
    }

    public static float[] flatten(float[][][][] d) {
        int n1 = d.length;
        int n2 = d[0].length;
        int n3 = d[0][0].length;
        int n4 = d[0][0][0].length;
        int n = n1 * n2 * n3 * n4;
        float[] ret = new float[n];
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                for (int k = 0; k < n3; k++) {
                    for (int l = 0; l < n4; l++) {
                        ret[i * n2 * n3 * n4 + j * n3 * n4 + k * n4 + l] = d[i][j][k][l];
                    }
                }

            }
        }
        return ret;
    }

    /**
     * if we want to convert 1D column vector index to 2D coordinates i.e after
     * applying find command we get 1D column vector in which we want to find
     * the exact coordinate of the specified item of the column vector
     *
     * @param index:index of the column vector
     * @param r:original row number of 2D matrix
     * @param c:original column number of 2D matrix
     * @return CPoint(row,column)
     */
    public static CPoint to2D(int index, int r, int c) {
        CPoint ret = new CPoint(0, 0);
        int col = index / r;
        int row = index - col * r;
        ret.row = row;
        ret.column = col;
        return ret;
    }

    /**
     * return the row and column number of the input 2D array
     *
     * @param d:2D float array
     * @return
     */
    public static CPoint getSize(float[][] d) {
        CPoint ret = new CPoint(d.length, d[0].length);
        return ret;
    }

    /**
     * return the row and column number of the input 2D array
     *
     * @param d:2D int array
     * @return
     */
    public static CPoint getSize(int[][] d) {
        CPoint ret = new CPoint(d.length, d[0].length);
        return ret;
    }

    public static int[] toIntArray1D(float[][] d) {
        int[] ret = new int[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = (int) d[i][j];
            }
        }
        return ret;
    }

    public static short[] toShortArray1DBasedOnRows(float[][] d) {
        short[] ret = new short[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = (short) d[i][j];
            }
        }
        return ret;
    }

    public static short[] toShortArray1D(float[][] d) {
        short[] ret = new short[d.length * d[0].length];
        int k = 0;
        for (int j = 0; j < d[0].length; j++) {
            for (int i = 0; i < d.length; i++) {
                ret[k++] = (short) d[i][j];
            }
        }
        return ret;
    }

    public static short[] toShortArray1D(float[] d) {
        short[] ret = new short[d.length];
        for (int i = 0; i < d.length; i++) {
            ret[i] = (short) d[i];
        }
        return ret;
    }

    public static float[] toFloatArray1DBasedOnRows(float[][] d) {
        float[] ret = new float[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = (float) d[i][j];
            }
        }
        return ret;
    }

    public static long[] toLongArray1DBasedOnRows(float[][] d) {
        long[] ret = new long[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = (long) d[i][j];
            }
        }
        return ret;
    }

    public static long[] toLongArray1D(float[][] d) {
        long[] ret = new long[d.length * d[0].length];
        int k = 0;
        for (int j = 0; j < d[0].length; j++) {
            for (int i = 0; i < d.length; i++) {
                ret[k++] = (long) d[i][j];
            }
        }
        return ret;
    }

    public static long[] toLongArray1D(float[] d) {
        long[] ret = new long[d.length];
        for (int i = 0; i < d.length; i++) {
            ret[i] = (long) d[i];
        }
        return ret;
    }

    public static String[] toStringArray1DBasedOnRows(float[][] d) {
        String[] ret = new String[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = String.valueOf(d[i][j]);
            }
        }
        return ret;
    }

    public static String[] toStringArray1D(float[][] d) {
        String[] ret = new String[d.length * d[0].length];
        int k = 0;
        for (int j = 0; j < d[0].length; j++) {
            for (int i = 0; i < d.length; i++) {
                ret[k++] = String.valueOf(d[i][j]);
            }
        }
        return ret;
    }

    public static String[] toStringArray1D(float[] d) {
        String[] ret = new String[d.length];
        for (int i = 0; i < d.length; i++) {
            ret[i] = String.valueOf(d[i]);
        }
        return ret;
    }

    public static byte[] toByteArray1DBasedOnRows(float[][] d) {
        byte[] ret = new byte[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = (byte) d[i][j];
            }
        }
        return ret;
    }

    public static byte[] toByteArray1DBasedOnRows(int[][] d) {
        byte[] ret = new byte[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = (byte) d[i][j];
            }
        }
        return ret;
    }

    public static int[] toIntArray1D(int[][] d) {
        int[] ret = new int[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = d[i][j];
            }
        }
        return ret;
    }

    public static byte[] toByteArray1D(float[][] d) {
        byte[] ret = new byte[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = (byte) d[i][j];
            }
        }
        return ret;
    }

    public static byte[] toByteArray1DBasedOnRows(byte[][] d) {
        byte[] ret = new byte[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = d[i][j];
            }
        }
        return ret;
    }

    public static short[] toShortArray1DBasedOnRows(short[][] d) {
        short[] ret = new short[d.length * d[0].length];
        int k = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[k++] = d[i][j];
            }
        }
        return ret;
    }

    public static int getMinimum(int[] p) {
        int m = p[0];
        for (int i = 0; i < p.length; i++) {
            if (p[i] < m) {
                m = p[i];
            }
        }
        return m;
    }

    public static int getMaximum(int[] p) {
        int m = p[0];
        for (int i = 0; i < p.length; i++) {
            if (p[i] > m) {
                m = p[i];
            }
        }
        return m;
    }

    public static float getMaximum(float[][] p) {
        float m = p[0][0];
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[0].length; j++) {
                if (p[i][j] > m) {
                    m = p[i][j];
                }
            }
        }
        return m;
    }

    public static float getMinimum(float[][] p) {
        float m = p[0][0];
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[0].length; j++) {
                if (p[i][j] < m) {
                    m = p[i][j];
                }
            }
        }
        return m;
    }

//    public static float getMaximum(float[][] p) {
//        float m = p[0][0];
//        for (int i = 0; i < p.length; i++) {
//            for (int j = 0; j < p[0].length; j++) {
//                if (p[i][j] > m) {
//                    m = p[i][j];
//                }
//            }
//        }
//        return m;
//    }
//
//    public static float getMinimum(float[][] p) {
//        float m = p[0][0];
//        for (int i = 0; i < p.length; i++) {
//            for (int j = 0; j < p[0].length; j++) {
//                if (p[i][j] < m) {
//                    m = p[i][j];
//                }
//            }
//        }
//        return m;
//    }
    public static int getMaximum(int[][] p) {
        int m = p[0][0];
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[0].length; j++) {
                if (p[i][j] > m) {
                    m = p[i][j];
                }
            }
        }
        return m;
    }

    public static int getMinimum(int[][] p) {
        int m = p[0][0];
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[0].length; j++) {
                if (p[i][j] < m) {
                    m = p[i][j];
                }
            }
        }
        return m;
    }

    public static byte getMaximum(byte[][] p) {
        byte m = p[0][0];
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[0].length; j++) {
                if (p[i][j] > m) {
                    m = p[i][j];
                }
            }
        }
        return m;
    }

    public static byte getMinimum(byte[][] p) {
        byte m = p[0][0];
        for (int i = 0; i < p.length; i++) {
            for (int j = 0; j < p[0].length; j++) {
                if (p[i][j] < m) {
                    m = p[i][j];
                }
            }
        }
        return m;
    }

    public static float getMinimum(float[] p) {
        float m = p[0];
        for (int i = 0; i < p.length; i++) {
            if (p[i] < m) {
                m = p[i];
            }
        }
        return m;
    }

    public static int getMinimumIndex(float[] p) {
        float m = p[0];
        int min = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] < m) {
                m = p[i];
                min = i;
            }
        }
        return min;
    }

    public static float getMaximum(float[] p) {
        float m = p[0];
        for (int i = 0; i < p.length; i++) {
            if (p[i] > m) {
                m = p[i];
            }
        }
        return m;
    }

    public static int getMaximumIndex(float[] p) {
        float m = p[0];
        int max = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] > m) {
                m = p[i];
                max = i;
            }
        }
        return max;
    }

    public static byte getMinimum(byte[] p) {
        byte m = p[0];
        for (int i = 0; i < p.length; i++) {
            if (p[i] < m) {
                m = p[i];
            }
        }
        return m;
    }

    public static byte getMaximum(byte[] p) {
        byte m = p[0];
        for (int i = 0; i < p.length; i++) {
            if (p[i] > m) {
                m = p[i];
            }
        }
        return m;
    }

    public static float getMean(float[][] m) {
        float ret;
        float toplam = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                toplam += m[i][j];
            }
        }
        int pix = (m.length * m[0].length);
        ret = toplam / pix;
        return ret;
    }

    public static float getMean(int[][] m) {
        float ret;
        float toplam = 0;
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                toplam += m[i][j];
            }
        }
        int pix = (m.length * m[0].length);
        ret = toplam / pix;
        return ret;
    }

    public static float getMean(float[] d) {
        float t = getSum(d);
        return t / d.length;
    }

    public static float getMean(int[] d) {
        float t = getSum(d);
        return t / d.length;
    }

    public static float getMagnitude(float[] d) {
        float t = 0;
        for (int i = 0; i < d.length; i++) {
            t += d[i] * d[i];
        }
        return (float) Math.sqrt(t);
    }

    public static float getEnergy(float[] d) {
        float t = 0;
        for (int i = 0; i < d.length; i++) {
            t += d[i] * d[i];
        }
        return t;
    }

    public static float getEntropy(float[] d) {
        float entropy = 0.0f;
        float[] pdfArray = getPDFData(d);
        for (int i = 0; i < pdfArray.length; i++) {
            if (pdfArray[i] >= 0 && pdfArray[i] <= 0.000001) {
                continue;
            }
            entropy += pdfArray[i] * Math.log(pdfArray[i]);
        }
        entropy = -entropy;
        return entropy;
    }

    public static float[] getPDFData(float[] d) {
        float[] hst = FactoryMatrix.getHistogram(d, 255);
        float sum = sum(hst);
        float[] ret = new float[d.length];
        for (int i = 0; i < d.length; i++) {
            ret[i] = d[i] / sum;
        }
        return ret;
    }

    public static float getMagnitude(int[] d) {
        float t = 0;
        for (int i = 0; i < d.length; i++) {
            t += d[i] * d[i];
        }
        return (float) Math.sqrt(t);
    }

    public static float getMagnitude(byte[] d) {
        float t = 0;
        for (int i = 0; i < d.length; i++) {
            t += d[i] * d[i];
        }
        return (float) Math.sqrt(t);
    }

    public static ArrayList<Integer> toArrayList(int[] p) {
        ArrayList<Integer> ret = new ArrayList<>();
        for (int i = 0; i < p.length; i++) {
            ret.add(i);
        }
        return ret;
    }

    public static float sum(float[] d) {
        float ret = 0;
        for (int i = 0; i < d.length; i++) {
            ret += d[i];
        }
        return ret;
    }

    public static float prod(float[] d) {
        float ret = 1;
        for (int i = 0; i < d.length; i++) {
            ret *= d[i];
        }
        return ret;
    }

    public static float sum(float[][] d) {
        float ret = 0;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret += d[i][j];
            }
        }
        return ret;
    }

    public static float prod(float[][] d) {
        float ret = 1;
        for (float[] d1 : d) {
            for (int j = 0; j < d[0].length; j++) {
                ret *= d1[j];
            }
        }
        return ret;
    }

    public static float sum(float[] d, int n) {
        float ret = 0;
        for (int i = 0; i < n; i++) {
            ret += d[i];
        }
        return ret;
    }

    public static int sum(int[] d) {
        int ret = 0;
        for (int i = 0; i < d.length; i++) {
            ret += d[i];
        }
        return ret;
    }

    public static long sum(long[] d) {
        long ret = 0;
        for (int i = 0; i < d.length; i++) {
            ret += d[i];
        }
        return ret;
    }

    /**
     * average two identical float array
     *
     * @param a
     * @param b
     * @return
     */
    public static float[] mean(float[] a, float[] b) {
        int n = Math.min(a.length, b.length);
        float[] ret = new float[a.length];
        for (int i = 0; i < n; i++) {
            ret[i] = (a[i] + b[i]) / 2.0f;
        }
        return ret;
    }

    public static float mean(float[] d) {
        return sum(d) / d.length;
    }

    public static int mean(int[] d) {
        return sum(d) / d.length;
    }

    public static long mean(long[] d) {
        return sum(d) / d.length;
    }

    public static float std(float[] d) {
        return FactoryStatistic.getStandardDeviation(d);
    }

    public static float var(float[] d) {
        return FactoryStatistic.getVariance(d);
    }

    public static int[][] to2DInt(Integer[] bDizi, int m, int n) {
        int[][] ret = new int[m][n];
        int k = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                ret[i][j] = bDizi[k++];
//                if (bDizi[k]!=0) {
//                    System.out.println(k+".pixel:"+bDizi[k]);
//                }
            }
        }
        return ret;
    }

    public static int[][] to2DInt(int[] bDizi, int m, int n) {
        int[][] ret = new int[m][n];
        int k = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                ret[i][j] = bDizi[k++];
            }
        }
        return ret;
    }

    public static String getSubstringValueWithKeyFromBegin(String str, String key, TDeviceState ds) {
        String atla = "name=\"Value\">\n"
                + "                    <itf guid=\"{99B44940-BFE1-4083-ADA1-BE703F4B8E08}\" value=\"";
        int i = str.indexOf(key) + key.length();
        int j = str.indexOf("name=\"Value\"", i) + atla.length();
        ds.pos = j;
        ds.str = str.substring(0, j);
        return str.substring(0, j);
    }

    public static String getSubstringValueWithKeyToEnd(String str, TDeviceState ds) {
        int j = str.indexOf("\"", ds.pos);
        return str.substring(j, str.length());
    }

    public static String getSubstringValueWithKey(String str, String key) {
        String atla = "name=\"Value\">\n"
                + "                    <itf guid=\"{99B44940-BFE1-4083-ADA1-BE703F4B8E08}\" value=\"";
        int i = str.indexOf(key) + key.length();

        int j = str.indexOf("name=\"Value\"", i) + atla.length();
        int k = str.indexOf("\"", j);

        return str.substring(j, k);
    }

    public static float[] normalizeToCanvas(float[] votes, Rectangle rect) {
        float[] ret = new float[votes.length];
        int py = 50;
        int dh = rect.height - 2 * py;
        float maxH = FactoryUtils.getMaximum(votes);
        float scale = dh / maxH;
        for (int i = 0; i < ret.length; i++) {
            ret[i] = votes[i] * scale;
        }
        return ret;
    }

    public static int getDigitNumber(float p) {
        NumberFormat formatter = new DecimalFormat();
        formatter = new DecimalFormat("0.######E0");
        String s = formatter.format(p);
//        String s = p + "";
//        return s.length();
        String ss = s.substring(s.indexOf("E") + 1);
        return Integer.parseInt(ss);
    }

    public static File[] getFileListInFolder(String path) {
        ArrayList<File> results = new ArrayList<>();
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                results.add(file);
            }
        }
        File[] list = results.toArray(new File[0]);
        //Arrays.sort(list, Comparator.comparingLong(File::lastModified));
        List lst = Arrays.asList(list);
        Collections.sort(lst, new WindowsLikeComparator());
        lst.toArray(list);

        return list;
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

    public static File[] getDirectories(String path) {
        File[] directories = new File(path).listFiles(File::isDirectory);
        return directories;
    }

    public static File[] getFileListDataSetForImageClassification(String imageMainFolder) {
        final String[] EXTENSIONS = new String[]{
            "gif", "png", "bmp", "jpg", "PNG", "JPG", "BMP", "GIF", "jpeg", "JPEG" // and other formats you need
        };
        // filter to identify images based on their extensions
        FilenameFilter IMAGE_FILTER = new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                for (final String ext : EXTENSIONS) {
                    if (name.endsWith("." + ext)) {
                        return (true);
                    }
                }
                return (false);
            }
        };
        File[] dirs = getFolderListInFolder(imageMainFolder);
        List<File> lst = new ArrayList();
        if (dirs.length == 0) {
            File dir = new File(imageMainFolder);
            File[] files = dir.listFiles(IMAGE_FILTER);
            File[] ret = new File[files.length];
            for (int j = 0; j < files.length; j++) {
                ret[j] = files[j];
            }
            return ret;

        } else {
            for (int i = 0; i < dirs.length; i++) {
                File dir = new File(dirs[i].getAbsolutePath());
                File[] files = dir.listFiles(IMAGE_FILTER);
                for (int j = 0; j < files.length; j++) {
                    lst.add(files[j]);
                }
            }
            File[] ret = new File[lst.size()];
            for (int i = 0; i < lst.size(); i++) {
                ret[i] = lst.get(i);
            }
            Arrays.sort(ret, Comparator.comparingLong(File::lastModified));
            return ret;
        }

    }

    public static List<File> getFileListInFolderForImages(String imageFolder) {
        File[] ret = getFileArrayInFolderForImages(imageFolder);
        return new ArrayList(Arrays.asList(ret));
    }

    public static File[] getFileArrayInFolderForImages(String imageFolder) {
        File dir = new File(imageFolder);
        final String[] EXTENSIONS = new String[]{
            "gif", "png", "bmp", "jpg", "PNG", "JPG", "BMP", "GIF", "jpeg", "JPEG", "tif", "TIF" // and other formats you need
        };
        // filter to identify images based on their extensions
        FilenameFilter IMAGE_FILTER = new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                for (final String ext : EXTENSIONS) {
                    if (name.endsWith("." + ext)) {
                        return (true);
                    }
                }
                return (false);
            }
        };
        File[] list = dir.listFiles(IMAGE_FILTER);

        List lst = Arrays.asList(list);
        Collections.sort(lst, new WindowsLikeComparator());
        lst.toArray(list);
        return list;
    }

    public static File[] getFileArrayForImagesSubFoldersRecursively(String path) {
        File folder = new File(path);

        if (!folder.exists()) {
            System.out.println("Folder was not found : " + path);
            return new File[0];
        }

        File[] files = folder.listFiles();

        List<File> listImageFiles = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File[] subFiles = getFileArrayForImagesSubFoldersRecursively(file.getAbsolutePath());
                    listImageFiles.addAll(List.of(subFiles));
                } else {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".gif")) {
                        listImageFiles.add(file);
                    }
                }
            }
        }
        return listImageFiles.toArray(new File[0]);
    }

    /**
     * get File Array By Extension
     *
     * @param imageFolder
     * @param extensions : can be String array or for example "jpg","png","txt"
     * etc
     * @return
     */
    public static File[] getFileArrayInFolderByExtension(String imageFolder, String... extensions) {
        File dir = new File(imageFolder);
        // filter to identify images based on their extensions
        FilenameFilter FILE_FILTER = new FilenameFilter() {

            @Override
            public boolean accept(final File dir, final String name) {
                for (String extension : extensions) {
                    if (name.endsWith("." + extension)) {
                        return (true);
                    }
                }
                return (false);
            }
        };
        File[] list = dir.listFiles(FILE_FILTER);
        List lst = Arrays.asList(list);
        Collections.sort(lst, new WindowsLikeComparator());
        lst.toArray(list);
        //Arrays.sort(list, Comparator.comparing(File::getName));
        return list;
    }

    public static double[] getOneHotEncoding(int nClasses, String class_number) {
        double[] ret = new double[nClasses];
        double cln = Double.parseDouble(class_number);
        if (cln == 0) {
            ret[0] = 1;
        } else {
            ret[(int) cln] = 1;
        }
        return ret;
    }

    public static float[] getOneHotEncoding(int nClasses, int class_number) {
        float[] ret = new float[nClasses];
        ret[class_number] = 1;
        return ret;
    }

    /**
     * get File Array in folder
     *
     * @param imageFolder
     * @return
     */
    public static File[] getFileArrayInFolder(String imageFolder) {
        File dir = new File(imageFolder);
        File[] list = dir.listFiles();
        //Arrays.sort(list, Comparator.comparingLong(File::lastModified));
        //Arrays.sort(list, Comparator.comparing(File::getName));
        List lst = Arrays.asList(list);
        Collections.sort(lst, new WindowsLikeComparator());
        lst.toArray(list);

        return list;
    }

    public static List<File> getFileListInFolderByExtension(String imageFolder, String extension) {
        File[] list = getFileArrayInFolderByExtension(imageFolder, extension);
        return new ArrayList(Arrays.asList(list));
    }

    public static long fact(int n) {
        int t = 1;
        for (int i = 1; i < n; i++) {
            t *= i;
        }
        return t;
    }

    public static long fact(int m, int n) {
        int t = 1;
        for (int i = n + 1; i <= m; i++) {
            t *= i;
        }
        return t;
    }

    public static float[] ekle(float[] p1, float[] p2) {
        return concatenate(p1, p2);
    }

    public static int[] ekle(int[] p1, int[] p2) {
        return concatenate(p1, p2);
    }

    public static float[] concatenate(float[] p1, float[] p2) {
        float[] ret = new float[p1.length + p2.length];
        ArrayList<Float> lst = new ArrayList<>();
        for (int i = 0; i < p1.length; i++) {
            lst.add(p1[i]);
        }
        for (int i = 0; i < p2.length; i++) {
            lst.add(p2[i]);
        }
        Float[] d = new Float[p1.length + p2.length];
        d = lst.toArray(d);
        for (int i = 0; i < d.length; i++) {
            ret[i] = d[i];
        }
        return ret;
    }

    public static int[] concatenate(int[] p1, int[] p2) {
        int[] ret = new int[p1.length + p2.length];
        ArrayList<Integer> lst = new ArrayList<>();
        for (int i = 0; i < p1.length; i++) {
            lst.add(p1[i]);
        }
        for (int i = 0; i < p2.length; i++) {
            lst.add(p2[i]);
        }
        Integer[] d = new Integer[p1.length + p2.length];
        d = lst.toArray(d);
        for (int i = 0; i < d.length; i++) {
            ret[i] = d[i];
        }
        return ret;
    }

    public static int getGraphicsTextWidth(Graphics gr, String t) {
        Rectangle2D r1 = gr.getFont().getStringBounds(t, 0, t.length(), gr.getFontMetrics().getFontRenderContext());
        return (int) r1.getWidth();
    }

    public static int getMaxGraphicsTextWidth(Graphics gr, String[] t) {
        int ret = 0;
        for (int i = 0; i < t.length; i++) {
            Rectangle2D r1 = gr.getFont().getStringBounds(t[i], 0, t[i].length(), gr.getFontMetrics().getFontRenderContext());
            if (ret < r1.getWidth()) {
                ret = (int) r1.getWidth();
            }
        }
        return ret;
    }

    public static int getMaxGraphicsTextSeriesWidth(Graphics gr, String[] t) {
        int ret = 0;
        for (int i = 0; i < t.length; i++) {
            Rectangle2D r1 = gr.getFont().getStringBounds(t[i], 0, t[i].length(), gr.getFontMetrics().getFontRenderContext());
            ret += r1.getWidth() + 30;
        }
        return ret;
    }

    public static int getGraphicsTextHeight(Graphics gr, String t) {
        Rectangle2D r1 = gr.getFont().getStringBounds(t, 0, t.length(), gr.getFontMetrics().getFontRenderContext());
        return (int) r1.getHeight();
    }

    public static int getMaxGraphicsTextHeight(Graphics gr, String[] t) {
        int ret = 0;
        for (String t1 : t) {
            Rectangle2D r1 = gr.getFont().getStringBounds(t1, 0, t1.length(), gr.getFontMetrics().getFontRenderContext());
            if (ret < r1.getHeight()) {
                ret = (int) r1.getHeight();
            }
        }
        return ret;
    }

    public static float[] gaussian(float[] d, float sigma, float mean) {
        float[] ret = new float[d.length];
        for (int i = 0; i < d.length; i++) {
            ret[i] = gaussian(d[i], sigma, mean);
        }
        return ret;
    }

    public static float sigmoid(float x) {
        float ret = (float) (1 / (1 + Math.pow(Math.E, -1 * x)));
        return ret;
    }

    public static float[] sigmoid(float[] x) {
        int nr = x.length;
        float[] ret = new float[nr];
        for (int i = 0; i < nr; i++) {
            ret[i] = (float) (1 / (1 + Math.pow(Math.E, -1 * x[i])));
        }
        return ret;
    }

    public static float sigmoidDerivative(float x) {
        float ret = sigmoid(x) * (1 - sigmoid(x));
        return ret;
    }

    public static float[] sigmoidDerivative(float x[]) {
        int nr = x.length;
        float[] ret = new float[nr];
        for (int i = 0; i < nr; i++) {
            ret[i] = sigmoid(x[i]) * (1 - sigmoid(x[i]));
        }
        return ret;
    }

    public static float[][] gaussian(float[][] d, float sigma, float mean) {
        float[][] ret = new float[d.length][d[0].length];
        for (int i = 0; i < d.length; i++) {
            ret[i] = gaussian(d[i], sigma, mean);
        }
        return ret;
    }

    public static float gaussian(float x, float sigma, float mean) {
        float ret = (float) (Math.exp(-(Math.pow(x - mean, 2) / (2 * sigma * sigma))));
        return ret;
    }

    public static long tic() {
        long currentTime = System.nanoTime();
        return currentTime;
    }

    public static long toc(String msg, long tic) {
        long toc = System.nanoTime();
        float elapsed = (toc - tic) / (1000000.0f);
        System.out.println(msg + elapsed + " miliSecond");
        return toc;
    }

    public static long toc(long tic) {
        long toc = System.nanoTime();
        float elapsed = (toc - tic) / (1000000.0f);
        System.out.println("Elapsed Time:" + elapsed + " ms");
        return toc;
    }

    public static int fps(long t) {
        int ret = (int) Math.round(1E9 / (System.nanoTime() - t));
        return ret;
    }

    /**
     * it is used for detecting the single any object in the image matrix and
     * crop the roi of the object from the matrix it should be noted that
     * internally it is used nf=nearFactor which you should give as a parameter
     *
     * @param d:input image matrix
     * @param t:threshold value for background subtraction
     * @param nf:near factor how far object from the edge of the image matrix
     * @return cropped matrix related to the object itself
     */
    public static float[][] getWeightCenteredROI(float[][] d, int t, int nf) {
        float[][] ret = null;
//        CMatrix.getInstance(d).imshow();
        int[] px = getProjectedMatrixOnX(d);
        int[] py = getProjectedMatrixOnY(d);
//        CMatrix.getInstance(px).plot();
//        CMatrix.getInstance(py).plot();
        int thr = 10000;
        CPoint[] p_x = getPotentialObjects(px, thr);
        CPoint[] p_y = getPotentialObjects(py, thr);
        try {
            if (p_x.length == 0 || p_y.length == 0) {
                return null;
            }
            ret = getSubMatrix(d, new CPoint(p_y[0].row, p_x[0].row), new CPoint(p_y[0].column, p_x[0].column));
//            Thread.sleep(10);
        } catch (Exception ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

//        int[] p = getMinMaxPosition(d, t, 5);
//        int w = d[0].length;
//        int h = d.length;
////        Point center = new Point(Math.abs(p[0] + p[2]) / 2, Math.abs(p[1] + p[3]) / 2);
//        if (p[0] <= 1 || p[0] >= w - 1 || p[2] <= 1 || p[2] >= w - 1
//                || p[1] <= 1 || p[1] >= h - 1 || p[3] <= 1 || p[3] >= h - 1) {
//            ret = new float[1][1];
//        } else {
//            ret = getSubMatrix(d, new CPoint(p[1], p[0]), new CPoint(p[3], p[2]));
//        }
        return ret;
    }

    public static TRoi getRoiOfWeightCenteredObject(float[][] d, int t, int nf) {
        TRoi ret = new TRoi();
        int[] px = getProjectedMatrixOnX(d);
        int[] py = getProjectedMatrixOnY(d);
        int thr = 10000;
        CPoint[] p_x = getPotentialObjects(px, thr);
        CPoint[] p_y = getPotentialObjects(py, thr);
        try {
            if (p_x.length == 0 || p_y.length == 0) {
                return null;
            } else {
                ret.pr = p_y[0].row;
                ret.pc = p_x[0].row;
                ret.height = p_y[0].column - ret.pr;
                ret.width = p_x[0].column - ret.pc;
            }
            //ret = getSubMatrix(d, new CPoint(p_y[0].row, p_x[0].row), new CPoint(p_y[0].column, p_x[0].column));
        } catch (Exception ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static CPoint getWeightCenterPos(float[][] d) {
        //long t1=tic();
        float[][] ret = null;
        CPoint cp = new CPoint();
        int[] px = getProjectedMatrixOnX(d);
        int pxFrom = getFirstOccuranceOfData(px);
        int pxTo = getLastOccuranceOfData(px);
        cp.column = (pxFrom + pxTo) / 2;
        int[] py = getProjectedMatrixOnY(d);
        int pyFrom = getFirstOccuranceOfData(py);
        int pyTo = getLastOccuranceOfData(py);
        cp.row = (pyFrom + pyTo) / 2;
        //t1=toc("extra cost:",t1);
        return cp;
    }

    public static int getFirstOccuranceOfData(int[] p) {
        int ret = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] != 0) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    public static int getLastOccuranceOfData(int[] p) {
        int ret = 0;
        for (int i = p.length - 1; i > 0; i--) {
            if (p[i] != 0) {
                ret = i;
                break;
            }
        }
        return ret;
    }

    public static float[][] getWeightCenteredROI(float[][] d) {
        float[][] ret = null;
        int[] px = getProjectedMatrixOnX(d);
        int[] py = getProjectedMatrixOnY(d);
        int[] p_x = getPotentialObjects(px);
        int[] p_y = getPotentialObjects(py);
        ret = getSubMatrix(d, p_y[0], p_x[0], p_y[1], p_x[1]);
        return ret;
    }

    public static float[][] getWeightCenteredROI(float[][] d, CPoint[] cp) {
        float[][] ret = null;
        int[] px = getProjectedMatrixOnX(d);
        int[] py = getProjectedMatrixOnY(d);
        int[] p_x = getPotentialObjects(px);
        int[] p_y = getPotentialObjects(py);
        cp = new CPoint[2];
        cp[0] = new CPoint();
        cp[1] = new CPoint();
        cp[0].row = p_y[0];
        cp[0].column = p_x[0];
        cp[1].row = p_y[1];
        cp[1].column = p_x[1];

        ret = getSubMatrix(d, p_y[0], p_x[0], p_y[1], p_x[1]);
        return ret;
    }

    public static CPoint[] getWeightCenteredROI(float[][] d, boolean flag) {
        float[][] ret = null;
        int[] px = getProjectedMatrixOnX(d);
        int[] py = getProjectedMatrixOnY(d);
        int[] p_x = getPotentialObjects(px);
        int[] p_y = getPotentialObjects(py);
        CPoint[] cp = new CPoint[2];
        cp[0] = new CPoint();
        cp[1] = new CPoint();
        cp[0].row = p_y[0];
        cp[0].column = p_x[0];
        cp[1].row = p_y[1];
        cp[1].column = p_x[1];

        ret = getSubMatrix(d, p_y[0], p_x[0], p_y[1], p_x[1]);
        return cp;
    }

    private static int[] getPotentialObjects(int[] pr) {
        ArrayList<CPoint> lst = new ArrayList<>();
        for (int i = 0; i < pr.length; i++) {
            if (pr[i] > 0) {
                int p1 = i;
                int p2 = 0;
                int t = 0;
                while (i < pr.length) {
                    if (pr[i] == 0) {
                        p2 = i;
                        break;
                    }
                    p2 = i;
                    t += pr[i];
                    i++;
                }
                CPoint p = new CPoint(p1, p2);
                p.weight = t;
                if (t > 0) {
                    lst.add(p);
                }
            }
        }
        Collections.sort(lst, new CustomComparatorForCPoint());
        int[] ret = new int[2];
        ret[0] = lst.get(0).row;
        ret[1] = lst.get(0).column;
        return ret;
    }

    private static CPoint[] getPotentialObjects(int[] pr, int thr) {
        ArrayList<CPoint> lst = new ArrayList<>();
        for (int i = 0; i < pr.length; i++) {
            if (pr[i] > 0) {
                int p1 = i;
                int p2 = 0;
                int t = 0;
                while (i < pr.length) {
                    if (pr[i] == 0) {
                        p2 = i;
                        break;
                    }
                    p2 = i;
                    t += pr[i];
                    i++;
                }
                CPoint p = new CPoint(p1, p2);
                p.weight = t;
                if (t > thr) {
                    lst.add(p);
                }
            }
        }
        Collections.sort(lst, new CustomComparatorForCPoint());
        CPoint[] ret = new CPoint[lst.size()];
        ret = lst.toArray(ret);
        return ret;
    }

    private static int[] getMinMaxPosition(float[][] d, int t, int a) {
        int maxX = 0;
        int maxY = 0;
        int minX = d.length * 2;
        int minY = d[0].length * 2;
        int[] ret = new int[4];
        for (int i = 0; i < d[0].length; i++) {
            for (int j = 0; j < d.length; j++) {
                if (d[j][i] > t) {
                    if (minX > i) {
                        minX = i;
                    }
                    if (maxX < i) {
                        maxX = i;
                    }
                    if (minY > j) {
                        minY = j;
                    }
                    if (maxY < j) {
                        maxY = j;
                    }
                }
            }
        }
        ret[0] = ((minX - a) < 0) ? minX : (minX - a);
        ret[1] = ((minY - a) < 0) ? minY : (minY - a);
        ret[2] = ((maxX + a) > d[0].length) ? maxX : (maxX + a);
        ret[3] = ((maxY + a) > d.length) ? maxY : (maxY + a);
        return ret;
    }

    /**
     * herhangi bir matrisin içerisindeki geometrik şeklin en uzun boyutu ve
     * buna dik olan ikinci boyutun pixel olarak uzunluğunu hesaplar
     *
     * @param m=matrix
     * @param thr=threshold
     * @param isShow
     * @return
     */
    public static float[] getObjectDimensions(float[][] m, int thr, boolean isShow) {
        int cRow = m.length / 2;
        int cColumn = m[0].length / 2;
        float d1 = 0;
        float d2 = 0;
        int lr = 0;
        int lc = 0;
        int dim1R1 = cRow;
        int dim1C1 = cColumn;
        int dim1R2 = 0;
        int dim1C2 = 0;
        int dim2R2 = 0;
        int dim2C2 = 0;
        //en uzun dimension thresholddan büyük olan pixeller
        //arasında merkeze eucledian uzaklığı en büyük olan pixelin koordinattır.
        //en sonda bulunan lr ve lc normal grafikteki y,x değerlerini verirler.
        //bulunan koordinatın merkeze uzaklığının 2 katı principal dimensiondır.
        for (int c = 0; c < m[0].length; c++) {
            for (int r = 0; r < m.length; r++) {
                if (m[r][c] > thr) {
                    float ed = getEucledianDistance(cRow, cColumn, r, c);
                    if (ed > d1) {
                        lr = r;
                        lc = c;
                        d1 = ed;
                        dim1R2 = r;
                        dim1C2 = c;
                    }
                }
            }
        }
        d1 = FactoryUtils.formatFloat(2 * d1);
        float m1 = FactoryUtils.formatFloat(getSlope(lr, lc, cRow, cColumn));
        float m2 = FactoryUtils.formatFloat(-1.0f / m1);
        float dt = 0.1f;
        //en uzun dim bulunduktan sonra buna dik dim2 nin eğimini biliyoruz
        //bir de merkezi biliyoruz merkezden dışarıya çekilen doğrulardan en büyük ve 
        //eğimi m2 olan dim2 dir.
        for (int c = 0; c < m[0].length; c++) {
            for (int r = 0; r < m.length; r++) {
                if (m[r][c] > thr) {
                    float ed = getEucledianDistance(cRow, cColumn, r, c);
                    float slope = getSlope(r, c, cRow, cColumn);
                    float mm2 = Math.abs(m2 - slope);
                    if (ed > d2 && mm2 < dt) {
                        lr = r;
                        lc = c;
                        d2 = ed;
                        dim2R2 = r;
                        dim2C2 = c;
                    }
                }
            }
        }
        d2 = FactoryUtils.formatFloat(d2 * 2);
        float[] ret = new float[4];
        ret[0] = m1;
        ret[1] = d1;
        ret[2] = m2;
        ret[3] = d2;
        if (isShow) {
            CMatrix cm = CMatrix.getInstance(m).
                    drawLine(dim1R1, dim1C1, dim1R2, dim1C2, 2, Color.yellow).
                    drawLine(dim1R1, dim1C1, dim2R2, dim2C2, 2, Color.yellow).imshow(".fıstık");
        }
        return ret;
    }

    /**
     * herhangi bir matrisin içerisindeki geometrik şeklin en uzun boyutu ve
     * buna dik olan ikinci boyutun pixel olarak uzunluğunu hesaplar
     *
     * @param m=matrix
     * @param thr=threshold
     * @param isShow
     * @return
     */
    public static float[] getObjectDimensionsV2(float[][] m, int thr, boolean isShow) {
        int cRow = m.length / 2;
        int cColumn = m[0].length / 2;
        float d1 = 0;
        float d2 = 0;
        int lr = 0;
        int lc = 0;
        int dim1R1 = cRow;
        int dim1C1 = cColumn;
        int dim1R2 = 0;
        int dim1C2 = 0;
        int dim2R2 = 0;
        int dim2C2 = 0;
        //en uzun dimension thresholddan büyük olan pixeller
        //arasında merkeze eucledian uzaklığı en büyük olan pixelin koordinattır.
        //en sonda bulunan lr ve lc normal grafikteki y,x değerlerini verirler.
        //bulunan koordinatın merkeze uzaklığının 2 katı principal dimensiondır.
        for (int c = 0; c < m[0].length; c++) {
            for (int r = 0; r < m.length; r++) {
                if (m[r][c] > thr) {
                    float ed = getEucledianDistance(cRow, cColumn, r, c);
                    if (ed > d1) {
                        lr = r;
                        lc = c;
                        d1 = ed;
                        dim1R2 = r;
                        dim1C2 = c;
                    }
                }
            }
        }
        d1 = FactoryUtils.formatFloat(2 * d1);
        float m1 = FactoryUtils.formatFloat(getSlope(lr, lc, cRow, cColumn));
        float m2 = FactoryUtils.formatFloat(-1.0f / m1);
        float dt = 0.1f;
        //en uzun dim bulunduktan sonra buna dik dim2 nin eğimini biliyoruz
        //bir de merkezi biliyoruz merkezden dışarıya çekilen doğrulardan en büyük ve 
        //eğimi m2 olan dim2 dir.
        for (int c = 0; c < m[0].length; c++) {
            for (int r = 0; r < m.length; r++) {
                if (m[r][c] > thr) {
                    float ed = getEucledianDistance(cRow, cColumn, r, c);
                    float slope = getSlope(r, c, cRow, cColumn);
                    float mm2 = Math.abs(m2 - slope);
                    if (ed > d2 && mm2 < dt) {
                        lr = r;
                        lc = c;
                        d2 = ed;
                        dim2R2 = r;
                        dim2C2 = c;
                    }
                }
            }
        }
        d2 = FactoryUtils.formatFloat(d2 * 2);
        float[] ret = new float[4];
        ret[0] = m1;
        ret[1] = d1;
        ret[2] = m2;
        ret[3] = d2;
        if (isShow) {
            CMatrix cm = CMatrix.getInstance(m).
                    drawLine(dim1R1, dim1C1, dim1R2, dim1C2, 2, Color.yellow).
                    drawLine(dim1R1, dim1C1, dim2R2, dim2C2, 2, Color.yellow).imshow(".fıstık");
        }
        return ret;
    }

    public static float getSlope(int r1, int c1, int r2, int c2) {
        float ret = -(r1 - r2) * 1.0f / (c1 - c2);
        return ret;
    }

    public static float getEucledianDistance(float d1, float d2) {
        return (float) Math.sqrt(d1 * d1 + d2 * d2);
    }

    public static float getEucledianDistance(int x1, int y1, int x2, int y2) {
        float d = (float) (Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2)));
        return d;
    }

    public static float getEucledianDistance(Point p1, Point p2) {
        float d = (float) (Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2)));
        return d;
    }

    public static float getEucledianDistance(float x1, float y1, float x2, float y2) {
        float d = (float) (Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2)));
        return d;
    }

    /**
     * return current time as String for file name or other issues
     *
     * @return
     */
    public static String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");
        Calendar cal = Calendar.getInstance();
        String ret = dateFormat.format(cal.getTime());
        return ret;
    }

    /**
     * return current date as String for file name or other issues
     *
     * @return
     */
    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
        Calendar cal = Calendar.getInstance();
        String ret = dateFormat.format(cal.getTime());
        return ret;
    }

    /**
     * calculate number of peaks of each column
     *
     * @param d:two dimensional matrix
     * @return array of peaks numbers of columns
     */
    public static float[] getNumberOfPeaks(float[][] d) {
        float[] ret = new float[d[0].length];
        float[][] dt = transpose(d);
        for (int i = 0; i < ret.length; i++) {
            ret[i] = getNumberOfPeaks(dt[i]);
        }
        return ret;
    }

    public static float getNumberOfPeaks(float[] d) {
        float ret = 0;
        float y1, y2, y3;
        for (int i = 1; i < d.length - 1; i++) {
            y1 = d[i - 1];
            y2 = d[i];
            y3 = d[i + 1];
            if (y1 < y2 && y3 < y2) {
                ret++;
            }
        }
        return ret;
    }

    public static float getNumberOfWalleys(float[] d) {
        float ret = 0;
        float y1, y2, y3;
        for (int i = 1; i < d.length - 1; i++) {
            y1 = d[i - 1];
            y2 = d[i];
            y3 = d[i + 1];
            if (y1 > y2 && y3 > y2) {
                ret++;
            }
        }
        return ret;
    }

    public static float getNumberOfPeaks(float[] d, float sensitivity) {
        float ret = 0;
        float y1, y2, y3;
        for (int i = 1; i < d.length - 1; i++) {
            y1 = d[i - 1];
            y2 = d[i];
            y3 = d[i + 1];
            if ((y2 - y1) > sensitivity && (y2 - y1) > sensitivity) {
                ret++;
            }
        }
        return ret;
    }

    public static float getNumberOfWalleys(float[] d, float sensitivity) {
        float ret = 0;
        float y1, y2, y3;
        for (int i = 1; i < d.length - 1; i++) {
            y1 = d[i - 1];
            y2 = d[i];
            y3 = d[i + 1];
            if ((y1 - y2) > sensitivity && (y3 - y2) > sensitivity) {
                ret++;
            }
        }
        return ret;
    }

    public static void makeDirectory(String fn) {
        File file = new File(fn);
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("folder " + fn + " was generated successfully");
            } else {
                System.err.println(fn + " folder couldn't generated.");
            }
        }
    }

    public static float[] getTotalMovement(float[][] d) {
        float[] ret = new float[d[0].length];
        float[][] dt = transpose(d);
        for (int i = 0; i < ret.length; i++) {
            ret[i] = getTotalMovement(dt[i]);
        }
        return ret;
    }

    public static float getTotalMovement(float[] d) {
        float ret = 0;
        for (int i = 0; i < d.length; i++) {
            ret += Math.abs(d[i]);
        }
        return ret;
    }

    public static float[] scale(float[] dizi, float d) {
        float[] ret = new float[dizi.length];
        for (int i = 0; i < dizi.length; i++) {
            ret[i] = dizi[i] * d;
        }
        return ret;
    }

//    public static float[] scale(float[] dizi, float d) {
//        float[] ret = new float[dizi.length];
//        for (int i = 0; i < dizi.length; i++) {
//            ret[i] = dizi[i] * d;
//        }
//        return ret;
//    }
    public static String toWekaString(float[] d) {
        String ret = "";
        for (int i = 0; i < d.length; i++) {
            ret += d[i] + ",";
        }
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    public static String toCSVString(float[] d) {
        String ret = "";
        for (int i = 0; i < d.length; i++) {
            ret += d[i] + ";";
        }
        ret = ret.substring(0, ret.length() - 1);
        return ret;
    }

    /**
     * add binary edge image on the original image
     *
     * @param d : original image
     * @param dc: binary edge image
     * @return original image with edge information
     */
    public static float[][] overlayIdenticalMatrix(float[][] d, float[][] dc) {
        if (d.length != dc.length || d[0].length != dc[0].length) {
            return d;
        }
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (dc[i][j] == 255) {
                    d[i][j] = 255;
                }
            }
        }
        return d;
    }

    /**
     * add any matrix on the original matrix at specified point
     *
     * @param d : original image
     * @param dc: binary edge image
     * @return original image with edge information
     */
    public static float[][] overlayMatrix(float[][] d, float[][] dc, CPoint cp) {
        if (cp.row - dc.length < 0 || cp.row + dc.length > d.length - 1 || cp.column - dc[0].length < 0 || cp.column + dc[0].length > d[0].length - 1) {
            return d;
        }
        int pr = dc.length / 2;
        int pc = dc[0].length / 2;

        for (int i = 0; i < dc.length; i++) {
            for (int j = 0; j < dc[0].length; j++) {
                d[cp.row - pr + i][cp.column - pc + j] = dc[i][j];
            }
        }
        return d;
    }

    public static boolean deleteFile(File file) {
        return file.delete();
    }

    public static boolean deleteDirectory(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
            return true;
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean deleteDirectory(String path) {
        File dir = new File(path);
        return deleteDirectory(dir);
    }

    public static boolean removeDirectory(File dir) {
        return deleteDirectory(dir);
    }

    public static boolean removeDirectory(String path) {
        File dir = new File(path);
        return removeDirectory(dir);
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    public static boolean renameFile(String oldname, String newname) {
        // File (or directory) with old name
        File file = new File(oldname);

        // File (or directory) with new name
        File file2 = new File(newname);
        if (file2.exists()) {
            System.out.println("can not rename since target name is same as source name");
            return false;
        }

        // Rename file (or directory)
        boolean success = file.renameTo(file2);
        if (!success) {
            System.out.println("can not rename");
        }
        return true;
    }

    public static boolean renameFile(File oldFile, File newFile) {
        if (newFile.exists()) {
            System.out.println("can not rename since target name is same as source name");
            return false;
        }
        boolean success = oldFile.renameTo(newFile);
        if (!success) {
            System.out.println("can not rename");
        }
        return true;
    }

    public static float multiplyAndSum(float[] r1, float[] r2) {
        float ret = 0;
        for (int i = 0; i < r1.length; i++) {
            ret += r1[i] * r2[i];
        }
        return ret;
    }

    public static boolean newFolder(String dirName) {
        File theDir = new File(dirName);
        if (!theDir.exists()) {
            try {
                theDir.mkdir();
                return true;
            } catch (SecurityException se) {
                System.out.println("exception was thrown:" + se.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isFolderExist(String dirName) {
        File theDir = new File(dirName);
        if (theDir.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isFileExist(String path) {
        File fl = new File(path);
        if (fl.exists()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPointInROI(CPoint p, ArrayList<CPoint> lst) {
        CPoint[] plst = new CPoint[lst.size()];
        plst = lst.toArray(plst);
        return isPointInPolygon(p, plst);
    }

    public static boolean isPointInROI(CPoint p, CPoint[] roi) {
        return isPointInPolygon(p, roi);
    }

    public static TRoi getROI(float[][] d) {
        int nr = d.length;
        List<Integer> lstRowFirstOccurance = new ArrayList();
        List<Integer> lstRowLastOccurance = new ArrayList();
        List<Integer> lstColumnFirstOccurance = new ArrayList();
        List<Integer> lstColumnLastOccurance = new ArrayList();
        for (int i = 0; i < nr; i++) {
            lstRowFirstOccurance.add(getFirstIndex(d[i], 255));
            lstRowLastOccurance.add(getLastIndex(d[i], 255));
        }
        float[][] dd = FactoryMatrix.transpose(d);
        nr = dd.length;
        for (int i = 0; i < nr; i++) {
            lstColumnFirstOccurance.add(getFirstIndex(dd[i], 255));
            lstColumnLastOccurance.add(getLastIndex(dd[i], 255));
        }
        TRoi roi = new TRoi();
        Collections.sort(lstRowFirstOccurance);
        int r1 = lstRowFirstOccurance.get(0);
        Collections.sort(lstRowLastOccurance);
        int r2 = lstRowLastOccurance.get(lstRowLastOccurance.size() - 1);
        Collections.sort(lstColumnFirstOccurance);
        int c1 = lstColumnFirstOccurance.get(0);
        Collections.sort(lstColumnLastOccurance);
        int c2 = lstColumnLastOccurance.get(lstColumnLastOccurance.size() - 1);
        //System.out.println("r1,r2,c1,c2:"+r1+","+r2+","+c1+","+c2);
        int w = r2 - r1;
        int h = c2 - c1;
        CPoint cp = new CPoint(c1 + h / 2, r1 + w / 2);
        roi.centerPoint = cp;
        roi.pr = c1;
        roi.pc = r1;
        roi.width = w;
        roi.height = h;
        return roi;
    }

    public static int getFirstIndex(float[] d, int thr) {
        int n = d.length;
        for (int i = 0; i < n; i++) {
            if (d[i] == thr) {
                return i;
            }
        }
        return Integer.MAX_VALUE;
    }

    public static int getLastIndex(float[] d, int thr) {
        int n = d.length;
        for (int i = n - 1; i > 0; i--) {
            if (d[i] == thr) {
                return i;
            }
        }
        return -1;
    }

    public static CPoint[] getPointsInROI(CPoint[] roi) {
        ArrayList<CPoint> lst = new ArrayList<>();
        CPoint[] cp = extractOuterBoundryFromROI(roi);
        for (int i = cp[0].row; i < cp[1].row; i++) {
            for (int j = cp[0].column; j < cp[1].column; j++) {
                CPoint p = new CPoint(i, j);
                if (isPointInPolygon(p, roi)) {
                    lst.add(p);
                }
            }
        }
        CPoint[] cp_lst = new CPoint[lst.size()];
        return lst.toArray(cp_lst);
    }

    public static void savePointsInROI(CPoint[] roiPixels) {
        String s = "";
        for (CPoint rp : roiPixels) {
            s += rp.row + "," + rp.column + "\n";
        }
        writeToFile(s);
    }

    public static boolean isPointInROI(CPoint p, float[][] d) {
        CPoint[] lst = new CPoint[d.length];
        for (int i = 0; i < d.length; i++) {
            CPoint cp = new CPoint((int) d[i][0], (int) d[i][1]);
            lst[i] = cp;
        }
        return isPointInPolygon(p, lst);
    }

    public static CPoint[] getRoiBoundary(float[][] d) {
        CPoint[] polygon = new CPoint[d.length];
        for (int i = 0; i < d.length; i++) {
            polygon[i] = new CPoint();
            polygon[i].row = (int) d[i][0];
            polygon[i].column = (int) d[i][1];
        }
        return getRoiBoundary(polygon);
    }

    public static CPoint[] getRoiBoundary(CPoint[] polygon) {
        int minC = polygon[0].column;
        int maxC = polygon[0].column;
        int minR = polygon[0].row;
        int maxR = polygon[0].row;
        for (int i = 1; i < polygon.length; i++) {
            CPoint q = polygon[i];
            minC = Math.min(q.column, minC);
            maxC = Math.max(q.column, maxC);
            minR = Math.min(q.row, minR);
            maxR = Math.max(q.row, maxR);
        }
        CPoint[] ret = new CPoint[2];
        ret[0] = new CPoint(minR, minC);
        ret[1] = new CPoint(maxR, maxC);
        return ret;
    }

    private static CPoint[] extractOuterBoundryFromROI(CPoint[] polygon) {
        int minC = polygon[0].column;
        int maxC = polygon[0].column;
        int minR = polygon[0].row;
        int maxR = polygon[0].row;
        for (int i = 1; i < polygon.length; i++) {
            CPoint q = polygon[i];
            minC = Math.min(q.column, minC);
            maxC = Math.max(q.column, maxC);
            minR = Math.min(q.row, minR);
            maxR = Math.max(q.row, maxR);
        }
        CPoint[] ret = new CPoint[2];
        ret[0] = new CPoint(minR, minC);
        ret[1] = new CPoint(maxR, maxC);
        return ret;
    }

    private static boolean isPointInPolygon(Point p, Point[] polygon) {
        CPoint cp = convertPointToCPoint(p);
        CPoint[] cps = convertPointsToCPoints(polygon);
        return isPointInPolygon(cp, cps);
    }

    private static boolean isPointInPolygon(CPoint p, CPoint[] polygon) {
        CPoint[] cp = extractOuterBoundryFromROI(polygon);
        int minC = cp[0].column;
        int maxC = cp[1].column;
        int minR = cp[0].row;
        int maxR = cp[1].row;

        if (p.column < minC || p.column > maxC || p.row < minR || p.row > maxR) {
            return false;
        }

        // http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        boolean inside = false;
        for (int i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            if ((polygon[i].row > p.row) != (polygon[j].row > p.row)
                    && p.column < (polygon[j].column - polygon[i].column) * (p.row - polygon[i].row) / (polygon[j].row - polygon[i].row) + polygon[i].column) {
                inside = !inside;
            }
        }

        return inside;
    }

//    public static float[] getHistogram(float[] d, int nBins) {
//        float[] ret = new float[nBins];
//        float min = getMinimum(d);
//        boolean eksili = false;
//        if (min < 0) {
//            eksili = true;
//        }
//        float max = getMaximum(d);
//        float delta = 0;
//        if (min < 0 && max > 0) {
//            delta = Math.abs(Math.abs(max) + Math.abs(min)) / (nBins - 1);
//        }
//        if ((min <= 0 && max <= 0) || (min >= 0 && max >= 0)) {
//            delta = Math.abs(Math.abs(max) - Math.abs(min)) / (nBins - 1);
//        }
//        int index = 0;
//        for (int i = 0; i < d.length; i++) {
//            index = (int) Math.round((d[i] - min) / delta);
//            if (index >= ret.length) {
//                index = ret.length - 1;
//            }
//            if (index < 0) {
//                index = 0;
//            }
//            if (d[i] == min) {
//                int a = 1;
//            }
//            ret[index]++;
//        }
//        return ret;
//    }
    /**
     * generate n different colors
     *
     * @param n
     * @return
     */
    public static Color[] generateColor(int n) {
        Color[] cl = new Color[n];
        if (n == 1) {
            MersenneTwister r = new MersenneTwister();
            int red = new MersenneTwister(r.nextInt(1000)).nextInt(255);
            int green = new MersenneTwister(r.nextInt(10)).nextInt(255);
            int blue = new MersenneTwister(r.nextInt(100)).nextInt(255);
            cl[0] = new Color(red, green, blue);
        } else if (n == 2) {
            cl[0] = new Color(255, 0, 0);
            cl[1] = new Color(0, 0, 255);
        } else if (n == 3) {
            cl[0] = new Color(255, 0, 0);
            cl[1] = new Color(0, 255, 0);
            cl[2] = new Color(0, 0, 255);
        } else {
            Random r = new Random();
            for (int i = 0; i < n; i++) {
                int red = UniqueRandomNumbers.getUniqueNumber(0, 255);
                int green = UniqueRandomNumbers.getUniqueNumber(0, 255);
                int blue = UniqueRandomNumbers.getUniqueNumber(0, 255);
                cl[i] = new Color(red, green, blue);
            }
        }
        return cl;
    }

//    public static float[][] getHistogram(float[][] array) {
//        float[][] d = FactoryMatrix.clone(array);
//        d = transpose(d);
//        int nBins = (int) (getMaximum(array) - getMinimum(array)) + 1;
//        float[][] ret = new float[d.length][nBins];
//        for (int i = 0; i < d.length; i++) {
//            ret[i] = getHistogram(d[i], nBins);
//        }
//        return transpose(ret);
//    }
//
//    public static float[][] getHistogram(float[][] array, int nBins) {
//        float[][] d = FactoryMatrix.clone(array);
//        d = transpose(d);
//        float[][] ret = new float[d.length][nBins];
//        for (int i = 0; i < d.length; i++) {
//            ret[i] = getHistogram(d[i], nBins);
//        }
////        return transpose(ret);
//        return ret;
//    }
    public static float[] hist(float[][] array) {
        return FactoryMatrix.getHistogram(array, 256);
    }

    public static float[] hist(float[][] array, int nBins) {
        return FactoryMatrix.getHistogram(array, nBins);
    }

    public static float[] hist(float[] array, int nBins) {
        return FactoryMatrix.getHistogram(array, nBins);
    }

    public static int[] hist(int[] array, int nBins) {
        return FactoryUtils.toIntArray1D(FactoryMatrix.getHistogram(FactoryUtils.toFloatArray1D(array), nBins));
    }

    public static float[][] shiftOnRow(float[][] d, int q) {
        float[][] ret = new float[d.length][d[0].length];
        if (q >= 0) {
            for (int i = 0; i < d.length; i++) {
                for (int j = q; j < d[0].length; j++) {
                    ret[i][j] = d[i][j - q];
                }
            }
        } else {
            for (int i = 0; i < d.length; i++) {
                for (int j = 0; j < d[0].length + q; j++) {
                    ret[i][j] = d[i][j - q];
                }
            }
        }
        return ret;
    }

    public static float[][] shiftOnColumn(float[][] d, int q) {

        float[][] ret = new float[d.length][d[0].length];
        if (q >= 0) {
            for (int row = 0; row < d.length - q; row++) {
                for (int column = 0; column < d[0].length; column++) {
                    ret[row + q][column] = d[row][column];
                }
            }
        } else {
            for (int row = 0; row < d.length + q; row++) {
                for (int column = 0; column < d[0].length; column++) {
                    ret[row][column] = d[row - q][column];
                }
            }
        }
        return ret;
    }

    public static float[][] subtract(float[][] a, float[][] b) {
        float[][] d = new float[a.length][a[0].length];
        if (isIdenticalMatrix(a, b)) {
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a[0].length; j++) {
                    d[i][j] = a[i][j] - b[i][j];
                }
            }
        } else {

        }
        return d;
    }

    public static float[][] subtractWithThreshold(float[][] a, float[][] b, float thr) {
        float[][] d = new float[a.length][a[0].length];
        if (isIdenticalMatrix(a, b)) {
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a[0].length; j++) {
                    if (a[i][j] - b[i][j] < thr) {
                        d[i][j] = 0;
                    } else {
                        d[i][j] = 255;
                    }

                }
            }
        } else {

        }
        return d;
    }

    private static boolean isIdenticalMatrix(float[][] a, float[][] b) {
        if (a.length == b.length && a[0].length == b[0].length) {
            return true;
        } else {
            return false;
        }
    }

    public static float[][] add(float[][] a, float[][] b) {
        float[][] d = new float[a.length][a[0].length];
        if (isIdenticalMatrix(a, b)) {
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a[0].length; j++) {
                    d[i][j] = a[i][j] + b[i][j];
                }
            }
        } else {

        }
        return d;
    }

    /**
     * compute eucledian distance between two points in n dimensioanal space
     *
     * @param tr
     * @param test
     * @return distance
     */
    public static float getEucledianDistance(float[] tr, float[] test) {
        float ret = 0;
        for (int i = 0; i < tr.length; i++) {
            ret += Math.pow((tr[i] - test[i]), 2);
        }
        ret = (float) Math.sqrt(ret);
        return ret;
    }

    /**
     * compute eucledian distance between two points in n dimensioanal space
     * except last element since last element is used as class label or target
     * function
     *
     * @param tr
     * @param test
     * @return distance
     */
    public static float getEucledianDistanceExceptLastElement(float[] tr, float[] test) {
        float ret = 0;
        for (int i = 0; i < tr.length - 1; i++) {
            ret += Math.pow((tr[i] - test[i]), 2);
        }
        ret = (float) Math.sqrt(ret);
        return ret;
    }

    private static BigInteger getFactorial(int n) {
        BigInteger fact = BigInteger.ONE;
        for (int i = n; i > 1; i--) {
            fact = fact.multiply(new BigInteger(Integer.toString(i)));
        }
        return fact;
    }

    public static BigInteger combination(int m, int n) {
        BigInteger mFact = getFactorial(m);
        BigInteger nFact = getFactorial(n);
        BigInteger mminusnFact = getFactorial(m - n);
        BigInteger total = mFact.divide(nFact.multiply(mminusnFact));
        return total;
    }

    public static void readln() {
        try {
            System.out.println("press enter to proceed");
            System.in.read();
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static float[][] threshold(float[][] d, int t) {
        float[][] ret = new float[d.length][d[0].length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (d[i][j] > t) {
                    ret[i][j] = 255;
                } else {
                    ret[i][j] = 0;
                }
            }
        }
        return ret;
    }

    /**
     * Matlab compatible see code of imnoise in Matlab
     *
     * @param d
     * @param m
     * @param v
     * @return
     */
    public static float[][] addGaussianNoise(float[][] d, float m, float v) {
        float[][] r = new float[d.length][d[0].length];
        Random rnd = new Random();
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                r[i][j] = Math.round(m + d[i][j] + rnd.nextGaussian() * Math.sqrt(v));
            }
        }
        return r;
    }

    /**
     *
     * @param d
     * @param freq : 0..1
     * @return
     */
    public static float[][] addSaltAndPepperNoise(float[][] d, float freq) {
        float[][] r = clone(d);
        Random rnd = new Random();
        int nr = d.length;
        int nc = d[0].length;
        int max = (int) (nr * nc * freq);
        for (int i = 0; i < max; i++) {
            int pr = rnd.nextInt(nr);
            int pc = rnd.nextInt(nc);
            if (rnd.nextBoolean()) {
                r[pr][pc] = 255;
            } else {
                r[pr][pc] = 0;
            }

        }
        return r;
    }

    /**
     * Reads a CSV-file from disk into a 2D float array.
     *
     * @param filename
     * @param separator Separator character between values.
     * @param headerLines Number of header lines to skip before reading data.
     * @return 2D float array
     */
    public static float[][] readCSV(String filename, char separator, int headerLines) {
        BufferedReader br = null;
        java.util.List<String[]> values = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            CSVReader cr = new CSVReader(br, separator, '\"', '\\', headerLines);
            values = cr.readAll();
            cr.close();
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        int numRows = values.size();
        int numCols = values.get(0).length;
        float[][] ret = new float[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            String[] rowValues = values.get(row);
            for (int col = 0; col < numCols; col++) {
                ret[row][col] = Float.parseFloat(rowValues[col]);
            }
        }
        return ret;
    }

    public static float[][] readCSV_slow(String filename, char separator, int headerLines) {
        BufferedReader br = null;
        String[] strArray;
        List<float[]> lst = new ArrayList();
        try {
            br = new BufferedReader(new FileReader(filename));
            CSVReader cr = new CSVReader(br, separator, '\"', '\\', headerLines);
            int k = 0;
            while ((strArray = cr.readNext()) != null) {
                int nr = strArray.length;
                float[] f = new float[nr];
                for (int i = 0; i < nr; i++) {
                    f[i] = Float.parseFloat(strArray[i]);
                }
                lst.add(f);
                //System.out.println((k++)+".satır");
            }
            cr.close();
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        int numRows = lst.size();
        int numCols = lst.get(0).length;
        float[][] ret = new float[numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                ret[row][col] = lst.get(row)[col];
            }
        }
        return ret;
    }

    /**
     * Reads a CSV-file from disk into a 2D float array.
     *
     * @param filename
     * @param separator Separator character between values.
     * @param headerLines Number of header lines to skip before reading data.
     * @return 2D float array
     */
    public static List<String[]> readCSV_AsString(String filename, char separator, int headerLines) {
        BufferedReader br = null;
        java.util.List<String[]> values = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            CSVReader cr = new CSVReader(br, separator, '\"', '\\', headerLines);
            values = cr.readAll();
            cr.close();
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return values;
    }

//    public static float[][] toFloatArray2D(int[][] array) {
//        int nr = array.length;
//        int nc = array[0].length;
//        float[][] ret = new float[nr][nc];
//        for (int i = 0; i < nr; i++) {
//            for (int j = 0; j < nc; j++) {
//                ret[i][j] = (float) array[i][j];
//            }
//        }
//        return ret;
//    }
    public static float[][] toFloatArray2D(long[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        float[][] ret = new float[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (float) array[i][j];
            }
        }
        return ret;
    }

    public static float[][] toFloatArray2D(short[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        float[][] ret = new float[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (float) array[i][j];
            }
        }
        return ret;
    }

//    public static float[][] toFloatArray2D(byte[][] array) {
//        int nr = array.length;
//        int nc = array[0].length;
//        float[][] ret = new float[nr][nc];
//        for (int i = 0; i < nr; i++) {
//            for (int j = 0; j < nc; j++) {
//                ret[i][j] = (float) array[i][j];
//            }
//        }
//        return ret;
//    }
    public static float[][] toFloatArray2D(String[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        float[][] ret = new float[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = Float.parseFloat(array[i][j]);
            }
        }
        return ret;
    }

//    public static short[][] toShortArray2D(float[][] array) {
//        int nr = array.length;
//        int nc = array[0].length;
//        short[][] ret = new short[nr][nc];
//        for (int i = 0; i < nr; i++) {
//            for (int j = 0; j < nc; j++) {
//                ret[i][j] = (short) array[i][j];
//            }
//        }
//        return ret;
//    }
    public static short[][] toShortArray2D(int[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        short[][] ret = new short[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (short) array[i][j];
            }
        }
        return ret;
    }

    public static short[][] toShortArray2D(long[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        short[][] ret = new short[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (short) array[i][j];
            }
        }
        return ret;
    }

    public static short[][] toShortArray2D(byte[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        short[][] ret = new short[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (short) array[i][j];
            }
        }
        return ret;
    }

    public static short[][] toShortArray2D(String[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        short[][] ret = new short[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = Short.parseShort(array[i][j]);
            }
        }
        return ret;
    }

//    public static byte[][] toByteArray2D(float[][] array) {
//        int nr = array.length;
//        int nc = array[0].length;
//        byte[][] ret = new byte[nr][nc];
//        for (int i = 0; i < nr; i++) {
//            for (int j = 0; j < nc; j++) {
//                ret[i][j] = (byte) array[i][j];
//            }
//        }
//        return ret;
//    }
    public static byte[][] toByteArray2D(int[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        byte[][] ret = new byte[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (byte) array[i][j];
            }
        }
        return ret;
    }

    public static byte[][] toByteArray2D(long[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        byte[][] ret = new byte[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (byte) array[i][j];
            }
        }
        return ret;
    }

    public static byte[][] toByteArray2D(short[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        byte[][] ret = new byte[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (byte) array[i][j];
            }
        }
        return ret;
    }

    public static byte[][] toByteArray2D(String[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        byte[][] ret = new byte[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = Byte.parseByte(array[i][j]);
            }
        }
        return ret;
    }

    public static char[][] toCharArray2D(float[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        char[][] ret = new char[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (char) array[i][j];
            }
        }
        return ret;
    }

//    public static char[][] toCharArray2D(float[][] array) {
//        int nr = array.length;
//        int nc = array[0].length;
//        char[][] ret = new char[nr][nc];
//        for (int i = 0; i < nr; i++) {
//            for (int j = 0; j < nc; j++) {
//                ret[i][j] = (char) array[i][j];
//            }
//        }
//        return ret;
//    }
    public static char[][] toCharArray2D(int[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        char[][] ret = new char[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (char) array[i][j];
            }
        }
        return ret;
    }

    public static char[][] toCharArray2D(long[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        char[][] ret = new char[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (char) array[i][j];
            }
        }
        return ret;
    }

    public static char[][] toCharArray2D(short[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        char[][] ret = new char[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (char) array[i][j];
            }
        }
        return ret;
    }

    public static char[][] toCharArray2D(String[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        char[][] ret = new char[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (char) Byte.parseByte(array[i][j]);
            }
        }
        return ret;
    }

//    public static long[][] toLongArray2D(float[][] array) {
//        int nr = array.length;
//        int nc = array[0].length;
//        long[][] ret = new long[nr][nc];
//        for (int i = 0; i < nr; i++) {
//            for (int j = 0; j < nc; j++) {
//                ret[i][j] = (long) array[i][j];
//            }
//        }
//        return ret;
//    }
    public static long[][] toLongArray2D(int[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        long[][] ret = new long[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (long) array[i][j];
            }
        }
        return ret;
    }

    public static long[][] toLongArray2D(byte[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        long[][] ret = new long[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (long) array[i][j];
            }
        }
        return ret;
    }

    public static long[][] toLongArray2D(char[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        long[][] ret = new long[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = (long) array[i][j];
            }
        }
        return ret;
    }

    public static long[][] toLongArray2D(String[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        long[][] ret = new long[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = Long.parseLong(array[i][j]);
            }
        }
        return ret;
    }

//    public static String[][] toStringArray2D(float[][] array) {
//        int nr = array.length;
//        int nc = array[0].length;
//        String[][] ret = new String[nr][nc];
//        for (int i = 0; i < nr; i++) {
//            for (int j = 0; j < nc; j++) {
//                ret[i][j] = "" + array[i][j];
//            }
//        }
//        return ret;
//    }
    public static String[][] toStringArray2D(long[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        String[][] ret = new String[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = "" + array[i][j];
            }
        }
        return ret;
    }

    public static String[][] toStringArray2D(int[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        String[][] ret = new String[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = "" + array[i][j];
            }
        }
        return ret;
    }

    public static String[][] toStringArray2D(byte[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        String[][] ret = new String[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = "" + array[i][j];
            }
        }
        return ret;
    }

    public static String[][] toStringArray2D(char[][] array) {
        int nr = array.length;
        int nc = array[0].length;
        String[][] ret = new String[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = "" + array[i][j];
            }
        }
        return ret;
    }

    public static List clone(List lst) {
        if (lst == null) {
            return null;
        }
        return Arrays.asList(lst.toArray());
    }

    public static float[] clone(float[] p) {
        float[] ret = new float[p.length];
        System.arraycopy(p, 0, ret, 0, p.length);
        return ret;
    }

//    public static float[] clone(float[] p) {
//        float[] ret = new float[p.length];
//        System.arraycopy(p, 0, ret, 0, p.length);
//        return ret;
//    }
    public static int[] clone(int[] p) {
        int[] ret = new int[p.length];
        System.arraycopy(p, 0, ret, 0, p.length);
        return ret;
    }

    public static byte[] clone(byte[] p) {
        byte[] ret = new byte[p.length];
        System.arraycopy(p, 0, ret, 0, p.length);
        return ret;
    }

    public static char[] clone(char[] p) {
        char[] ret = new char[p.length];
        System.arraycopy(p, 0, ret, 0, p.length);
        return ret;
    }

    public static long[] clone(long[] p) {
        long[] ret = new long[p.length];
        System.arraycopy(p, 0, ret, 0, p.length);
        return ret;
    }

    public static short[] clone(short[] p) {
        short[] ret = new short[p.length];
        System.arraycopy(p, 0, ret, 0, p.length);
        return ret;
    }

    public static String[] clone(String[] p) {
        String[] ret = new String[p.length];
        System.arraycopy(p, 0, ret, 0, p.length);
        return ret;
    }

    public static boolean[] clone(boolean[] p) {
        boolean[] ret = new boolean[p.length];
        System.arraycopy(p, 0, ret, 0, p.length);
        return ret;
    }

    public static float[][] clone(float[][] p) {
        float[][] ret = new float[p.length][p[0].length];
        int nr = p.length;
        for (int i = 0; i < nr; i++) {
            System.arraycopy(p[i], 0, ret[i], 0, p[0].length);
        }
        return ret;
    }

//    public static float[][] clone(float[][] p) {
//        float[][] ret = new float[p.length][p[0].length];
//        int nr = p.length;
//        for (int i = 0; i < nr; i++) {
//            System.arraycopy(p[i], 0, ret[i], 0, p[0].length);
//        }
//        return ret;
//    }
    public static int[][] clone(int[][] p) {
        int[][] ret = new int[p.length][p[0].length];
        int nr = p.length;
        for (int i = 0; i < nr; i++) {
            System.arraycopy(p[i], 0, ret[i], 0, p[0].length);
        }
        return ret;
    }

    public static long[][] clone(long[][] p) {
        long[][] ret = new long[p.length][p[0].length];
        int nr = p.length;
        for (int i = 0; i < nr; i++) {
            System.arraycopy(p[i], 0, ret[i], 0, p[0].length);
        }
        return ret;
    }

    public static short[][] clone(short[][] p) {
        short[][] ret = new short[p.length][p[0].length];
        int nr = p.length;
        for (int i = 0; i < nr; i++) {
            System.arraycopy(p[i], 0, ret[i], 0, p[0].length);
        }
        return ret;
    }

    public static byte[][] clone(byte[][] p) {
        byte[][] ret = new byte[p.length][p[0].length];
        int nr = p.length;
        for (int i = 0; i < nr; i++) {
            System.arraycopy(p[i], 0, ret[i], 0, p[0].length);
        }
        return ret;
    }

    public static boolean[][] clone(boolean[][] p) {
        boolean[][] ret = new boolean[p.length][p[0].length];
        int nr = p.length;
        for (int i = 0; i < nr; i++) {
            System.arraycopy(p[i], 0, ret[i], 0, p[0].length);
        }
        return ret;
    }

    public static String[][] clone(String[][] p) {
        String[][] ret = new String[p.length][p[0].length];
        int nr = p.length;
        for (int i = 0; i < nr; i++) {
            System.arraycopy(p[i], 0, ret[i], 0, p[0].length);
        }
        return ret;
    }

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public static int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static float[] exp(float[] d) {
        int n = d.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = (float) Math.exp(d[i]);
        }
        return ret;
    }

    public static float[] divide(float[] d, float sum) {
        int n = d.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = d[i] / sum;
        }
        return ret;
    }

    public static float[] multiply(float[] d, float sum) {
        int n = d.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = d[i] * sum;
        }
        return ret;
    }

    public static float[] add(float[] d, float sum) {
        int n = d.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = d[i] + sum;
        }
        return ret;
    }

    public static float[] subtract(float[] a, float val) {
        int n = a.length;
        float[] d = new float[n];
        for (int i = 0; i < n; i++) {
            d[i] = a[i] - val;
        }
        return d;
    }

    public static float[] subtract(float[] s, float[] y) {
        float[] ret = new float[s.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = s[i] - y[i];
        }
        return ret;
    }

    public static String formatBinary(int p) {
        return formatBinary(8, p);
    }

    public static String formatBinary(int n, int p) {
        char[] chars = new char[n];
        for (int j = 0; j < n; j++) {
            chars[j] = (char) (((p >>> (n - j - 1)) & 1) + '0');
        }
        return String.valueOf(chars);
    }

    public static void copyDirectory(File sourceFile, File destFile) {
        try {
            FileUtils.copyDirectory(sourceFile, destFile);
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void copyFile(String sourceFilePath, String destFilePath) {
        copyFile(new File(sourceFilePath), new File(destFilePath));
    }

    public static void copyFile(File sourceFile, File destFile) {
        if (!destFile.exists()) {
            try {
                destFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            try {
                source = new RandomAccessFile(sourceFile, "rw").getChannel();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                destination = new RandomAccessFile(destFile, "rw").getChannel();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }

            long position = 0;
            long count = 0;
            try {
                count = source.size();
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                source.transferTo(position, count, destination);
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            if (source != null) {
                try {
                    source.close();
                } catch (IOException ex) {
                    Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (destination != null) {
                try {
                    destination.close();
                } catch (IOException ex) {
                    Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public static String[][] readArffString(String path, int classIndex) {
        String[][] str = null;
        try {
            Instances wekaInstance = ConverterUtils.DataSource.read(path);
            wekaInstance.setClassIndex(classIndex);
            Instance ins = wekaInstance.instance(0);
            str = new String[wekaInstance.numInstances()][ins.numAttributes()];
            for (int i = 0; i < wekaInstance.numInstances(); i++) {
                ins = wekaInstance.instance(i);
                for (int j = 0; j < ins.numAttributes(); j++) {
                    str[i][j] = ins.stringValue(j);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CMatrix.class.getName()).log(Level.SEVERE, null, ex);
        }
        return str;
    }

    public static float getStd(float[] d) {
        return std(d);
    }

    public static float[][] deleteRowsFrom(float[][] dimg, int index) {
        float[][] ret = new float[index][dimg[0].length];
        for (int i = 0; i < index; i++) {
            ret[i] = dimg[i];
        }
        return ret;
    }

//    public static int[] convertInt(List<Integer> lst) {
//        int[] ret = lst.stream().filter(i -> i != null).mapToInt(i -> i).toArray();
//        return ret;
//    }
//
//    public static float[] convertFloat(List<Float> lst) {
//        float[] ret = lst.stream().filter(i -> i != null).mapToDouble(i -> i).toArray();
//        return ret;
//    }
//
//    public static long[] convertLong(List<Long> lst) {
//        long[] ret = lst.stream().filter(i -> i != null).mapToLong(i -> i).toArray();
//        return ret;
//    }
    public static boolean isPointInsidePolygon(Point[] polygon, Point point) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            if ((polygon[i].y > point.y) != (polygon[j].y > point.y)
                    && (point.x < (polygon[j].x - polygon[i].x) * (point.y - polygon[i].y) / (polygon[j].y - polygon[i].y) + polygon[i].x)) {
                result = !result;
            }
        }
        return result;
    }

    public static float getDistanceFromLatLonInKm(float lat1, float lon1, float lat2, float lon2) {
        float R = 6371; // Radius of the earth in km
        float dLat = deg2rad(lat2 - lat1);  // deg2rad below
        float dLon = deg2rad(lon2 - lon1);
        float a
                = (float) (Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        float d = R * c; // Distance in km
        return d;
    }

    public static float deg2rad(float deg) {
        return (float) (deg * (Math.PI / 180));
    }

    public static float[] getCosineSimilarity(float[][] d1, float[][] d2) {
        int n = d1.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = getCosineSimilarity(d1[i], d2[i]);
        }
        return ret;
    }

    public static float getCosineSimilarity(float[] vectorA, float[] vectorB) {
        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return (float) (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    public static List toListFrom2DArray(float[][] p) {
        List ret = new ArrayList<>();
        int nr = p.length;
        for (int i = 0; i < nr; i++) {
            ret.add(p[i]);
        }
        return ret;
    }

    public static float[][] toFloatArray2D(List p) {
        float[][] ret = new float[p.size()][];
        int nr = ret.length;
        for (int i = 0; i < nr; i++) {
            ret[i] = (float[]) (p.get(i));
        }
        return ret;
    }

    public static float[] vector(int from, int to) {
        if (from < to) {
            return vector(from, to, 1);
        } else {
            return vector(from, to, -1);
        }
//        float[] ret = new float[to - from];
//        for (int i = 0; i < ret.length; i++) {
//            ret[i] = from + i;
//        }
//        return ret;
    }

    public static float[] vector(float from, float to, float incr) {
        if (from < to && incr < 0) {
            throw new UnsupportedOperationException("incr should be positive");
        }
        if (from > to && incr > 0) {
            throw new UnsupportedOperationException("incr should be negative");
        }
        float delta = Math.abs(to - from);
        int n = Math.abs((int) (delta / incr));
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = from + i * incr;
        }
        return ret;
    }

    public static boolean canBeDotProduct(float[][] p1, float[][] p2) {
        if (p1[0].length == p2.length) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isVector(float[][] array) {
        if (array.length == 1 || array[0].length == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSimilarShape(float[][] d1, float[][] d2) {
        if (d1.length == d2.length && d1[0].length == d2[0].length) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".bmp");
    }

    public static float dotVector(float[][] d1, float[][] d2) {
        float ret = 0;
        float[] d_1 = toFloatArray1D(d1);
        float[] d_2 = toFloatArray1D(d2);
        for (int i = 0; i < d_1.length; i++) {
            ret += d_1[i] * d_2[i];
        }
        return ret;
    }

    public static File[] shuffle(File[] files, int seed) {
        List<File> lst = Arrays.asList(files);
        Random rnd = new Random(seed);
        Collections.shuffle(lst, rnd);
        files = lst.toArray(files);
        return files;
    }

    public static File[] shuffle(File[] files) {
        List<File> lst = Arrays.asList(files);
        Random rnd = new Random();
        Collections.shuffle(lst, rnd);
        files = lst.toArray(files);
        return files;
    }

    public static List<File> shuffle(List<File> files, int seed) {
        Collections.shuffle(files, new Random(seed));
        return files;
    }

    public static List<File> shuffle(List<File> files) {
        Collections.shuffle(files, new Random());
        return files;
    }

    public static float[][] RemoveNaNToZero(float[][] d) {
        int nr = d.length;
        int nc = d[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                d[i][j] = Float.isNaN(d[i][j]) ? 0 : d[i][j];
            }
        }
        return d;
    }

    public static String[] splitPath(String path) {
        return path.split(":?\\\\");
        //path = path.replace("\\", "/");
        //String[] s = path.split("/");
        //return s;
    }

    public static String getLastItemPath(String path) {
        path = path.replace("\\", "/");
        String[] s = path.split("/");
        return s[s.length - 1];
    }

    public static String getItemPath(String path, int n) {
        path = path.replace("\\", "/");
        String[] s = path.split("/");
        if (s.length < 2) {
            return s[0];
        }
        if (n < 0) {
            return s[s.length + n];
        } else {
            return s[n];
        }

    }
    
    /**
     * generate color from known color set
     * @param colorIndex
     * @return
     */
    public static Color getColorForLaneDetection(int colorIndex){
        switch (colorIndex) {
            case 1:
                return Color.BLUE;
            case 2:
                return Color.CYAN;
            case 3:
                return Color.MAGENTA;
            case 4:
                return Color.ORANGE;
            case 5:
                return Color.PINK;
            case 6:
                return Color.RED;
            default: 
                return Color.BLACK;
        }
    }

    public static Color[] getRandomColors(int n, long random_seed) {
        Random rnd = new Random(random_seed);
        Color[] ret = new Color[n];
        for (int i = 0; i < n; i++) {
            float r = rnd.nextFloat();
            float g = rnd.nextFloat();
            float b = rnd.nextFloat();
            ret[i] = new Color(r, g, b);
        }
        return ret;
    }

    public static int getLongestStringIndex(String[] items) {
        int n = 0;
        int ret = 0;
        for (int i = 0; i < items.length; i++) {
            if (n < items[i].length()) {
                n = items[i].length();
                ret = i;
            }
        }
        return ret;
    }

    public static int getLongestStringIndex(float[] items) {
        int n = 0;
        int ret = 0;
        for (int i = 0; i < items.length; i++) {
            if (n < ("" + items[i]).length()) {
                n = ("" + items[i]).length();
                ret = i;
            }
        }
        return ret;
    }

    public static int getLongestStringLength(float[] items) {
        int n = 0;
        for (int i = 0; i < items.length; i++) {
            if (n < ("" + items[i]).length()) {
                n = ("" + items[i]).length();
            }
        }
        return n;
    }

    public static int getLongestStringLength(float[][] items) {
        int n = 0;
        for (int i = 0; i < items.length; i++) {
            for (int j = 0; j < items[0].length; j++) {
                if (n < ("" + items[i][j]).length()) {
                    n = ("" + items[i][j]).length();
                }
            }
        }
        return n;
    }

    public static double[][] toDoubleArray2D(float[][] d) {
        int nr = d.length;
        int nc = d[0].length;
        double[][] ret = new double[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j] = d[i][j];
            }
        }
        return ret;
    }

    public static double[][][] toDoubleArray3D(float[][][] d) {
        int d1 = d.length;
        int d2 = d[0].length;
        int d3 = d[0][0].length;
        double[][][] ret = new double[d1][d2][d3];
        for (int i = 0; i < d1; i++) {
            for (int j = 0; j < d2; j++) {
                for (int k = 0; k < d3; k++) {
                    ret[i][j][k] = d[i][j][k];
                }
            }
        }
        return ret;
    }

    public static double[] toDoubleArray1D(float[][] d) {
        int nr = d.length;
        int nc = d[0].length;
        double[] ret = new double[nr * nc];
        int k = 0;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[k++] = d[i][j];
            }
        }
        return ret;
    }

    public static double[] toDoubleArray1D(Vector d) {
        int nr = d.size();
        double[] ret = new double[nr];
        for (int i = 0; i < nr; i++) {
            ret[i] = (double) d.get(i);
        }
        return ret;
    }

    public static float[] toFloatArray1D(Vector d) {
        int nr = d.size();
        float[] ret = new float[nr];
        for (int i = 0; i < nr; i++) {
            ret[i] = (float) d.get(i);
        }
        return ret;
    }

    public static double[] toDoubleArray1D(float[] d) {
        int nr = d.length;
        double[] ret = new double[nr];
        for (int i = 0; i < nr; i++) {
            ret[i] = d[i];
        }
        return ret;
    }

    /**
     *
     * @param x
     * @param fromX1
     * @param fromX2
     * @param toX1
     * @param toX2
     * @return
     */
    public static float map(float x, float fromX1, float fromX2, float toX1, float toX2) {
        if (fromX2 <= fromX1 || toX2 <= toX1) {
            System.err.println("Mapping can only be performed when fromX2 > fromX1 and toX2 > toX1, and x should be in between fromX1 and fromX2.");
            System.exit(-1);
        }

        float result = (x - fromX1) * (toX2 - toX1) / (fromX2 - fromX1) + toX1;

        // Optional: Limit the result to the target range
        if (result < toX1) {
            return toX1;
        } else if (result > toX2) {
            return toX2;
        } else {
            return result;
        }

//        if (fromX2 <= fromX1 || toX2 <= toX1) {
//            System.err.println("mapping can only be x2>x1 and x4>x3 and x should be in between x1 and x2");
//            System.exit(-1);
//        }
//        return x * (toX2 - toX1) / (fromX2 - fromX1) + toX1;
    }

    public static String toJSON(List lst) {
        Gson gson = new Gson();
        String str = gson.toJson(lst);
        return str;
    }

    public static void savePanel(JPanel cp, String str_dpi) {
        int dpi = Integer.parseInt(str_dpi);
        float scale = dpi / 96.0f;
        cp.setSize((int) (cp.getWidth() * scale), (int) (cp.getHeight() * scale));
        File file = FactoryUtils.getFileFromChooserSave();
        if (file != null) {
            ImageProcess.saveGridImage(ImageProcess.getBufferedImage(cp), file.getAbsolutePath());
        } else {
            FactoryUtils.showMessage("kaydedilemedi CPlotFrame.savePanel()");
        }
        cp.setSize((int) (cp.getWidth() / scale), (int) (cp.getHeight() / scale));
    }

    public static String saveImageAs(BufferedImage img, String str_dpi) {
        int dpi = Integer.parseInt(str_dpi);
        float scale = dpi / 96.0f;
        img = ImageProcess.resizeAspectRatio(img, (int) (img.getWidth() * scale), (int) (img.getHeight() * scale));
        File file=FactoryUtils.getFileFromChooserSave(saveImageFolder);
        //File file = FactoryUtils.getFileFromChooserSave();
        if (file != null) {
            saveImageFolder=FactoryUtils.getFolderPath(file.getAbsolutePath());
            ImageProcess.saveImage(img, file.getAbsolutePath());
            return file.getAbsolutePath();
        } else {
            FactoryUtils.showMessage("kaydedilemedi CPlotFrame.savePanel()");
            return null;
        }
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    public static boolean isMousePosEqual(Point mp1, Point mp2) {
        return mp1.x == mp2.x && mp1.y == mp2.y;
    }

    public static boolean isPointInROI(Point p, Rectangle rect) {
        //Rectangle r=new Rectangle(rect.x-2, rect.y-2, rect.width+5, rect.height+5);
        return rect.contains(p);
    }

    public static String deleteLastNCharacters(String s, int n) {
        return s.substring(0, s.length() - n);
    }

    public static float[][] sumConsecutive(float[][] d) {
        int nr = d.length;
        int nc = d[0].length;
        float[][] ret = new float[nr][nc];
        if (nr == 1 && nc == 1) {
            return d;
        } else if (nr == 1) {
            for (int i = 1; i < nc; i++) {
                ret[0][i] = ret[0][i - 1] + d[0][i];
            }
        } else if (nc == 1) {
            for (int i = 1; i < nr; i++) {
                ret[i][0] = ret[i - 1][0] + d[i][0];
            }
        } else {
            for (int i = 0; i < nr; i++) {
                for (int j = 1; j < nc; j++) {
                    ret[i][j] = ret[i][j - 1] + d[i][j];
                }
            }
        }
        return ret;
    }

    public static float[][] prodConsecutive(float[][] d) {
        int nr = d.length;
        int nc = d[0].length;
        float[][] ret = new float[nr][nc];
        if (nr == 1 && nc == 1) {
            return d;
        } else if (nr == 1) {
            ret[0][0] = d[0][0];
            for (int i = 1; i < nc; i++) {
                ret[0][i] = ret[0][i - 1] * d[0][i];
            }
        } else if (nc == 1) {
            ret[0][0] = d[0][0];
            for (int i = 1; i < nr; i++) {
                ret[i][0] = ret[i - 1][0] * d[i][0];
            }
        } else {
            for (int i = 0; i < nr; i++) {
                ret[i][0] = d[i][0];
                for (int j = 1; j < nc; j++) {
                    ret[i][j] = ret[i][j - 1] * d[i][j];
                }
            }
        }
        return ret;
    }

    public static void renameFilesAsNanoTime(String pathFolder) {
        File[] files = FactoryUtils.getFileArrayInFolderForImages(pathFolder);
        for (File file : files) {
            FactoryUtils.renameFile(file, new File(pathFolder + "\\" + System.nanoTime() + "." + FactoryUtils.getFileExtension(file)));
        }
    }

    public static void reduceImageSize(String folderPath, int maxWidth, int maxHeight) {
        File[] files = FactoryUtils.getFileArrayInFolderForImages(folderPath);
        int k = -1;
        for (File file : files) {
            k++;
            System.out.println(k + "." + file.getAbsolutePath());
            BufferedImage img = ImageProcess.imread(file.getAbsolutePath());
            if (img.getWidth() > maxWidth || img.getHeight() > maxHeight) {
                if (img.getHeight() > maxHeight) {
                    System.out.println(k + ".image height çok büyük :" + img.getWidth() + ":" + img.getHeight());
                    //height is more important than width
                    double r = 1.0 * maxHeight / img.getHeight();
                    int desiredWidth = (int) (r * img.getWidth());
                    int desiredHeight = maxHeight;
                    img = ImageProcess.resize(img, desiredWidth, desiredHeight);
                    ImageProcess.saveImage(img, file.getAbsolutePath());
                } else if (img.getWidth() > maxWidth) {
                    System.out.println(k + ".image width çok büyük :" + img.getWidth() + ":" + img.getHeight());
                    double r = 1.0 * maxWidth / img.getWidth();
                    int desiredHeight = (int) (r * img.getHeight());
                    int desiredWidth = maxWidth;
                    img = ImageProcess.resize(img, desiredWidth, desiredHeight);
                    ImageProcess.saveImage(img, file.getAbsolutePath());
                }
            }
        }
    }

    public static void reduceDataSet(String path_source, String path_reduced, float reduceRatio) {
        FactoryUtils.makeDirectory(path_reduced);
        File[] dirs = FactoryUtils.getFolderListInFolder(path_source);
        for (File dir : dirs) {
            List<File> imgList = Arrays.asList(FactoryUtils.getFileArrayInFolderForImages(dir.getAbsolutePath()));
            Collections.shuffle(imgList, new Random(123));
            List<File> imgReducedList = imgList.subList(0, (int) (imgList.size() * reduceRatio));
            FactoryUtils.makeDirectory(path_reduced + "/" + dir.getName());
            for (File file : imgReducedList) {
                copyFile(file, new File(path_reduced + "/" + dir.getName() + "/" + file.getName()));
            }
        }
    }

    public static void generateObjectDetectionDataSetYolo(String pathSource, String pathTarget, float trainRatio, float valRatio, float testRatio, boolean shuffle, int seed, String extension, String[] classLabels) {
        //FactoryUtils.balanceDatasetBasedOnFileExt(pathSource,"txt","jpg","xml");
        FactoryUtils.convertPascalVoc2YoloFormatBndBox(pathSource, classLabels);
        FactoryUtils.deleteEmptyFiles(pathSource, new String[]{"txt", "xml", "jpg"});

        File[] imgFiles = getFileArrayInFolderByExtension(pathSource, extension);
        Map<String, File> mapImg = generateHashMapForFile(imgFiles);
        File[] xmlFiles = getFileArrayInFolderByExtension(pathSource, "xml");
        Map<String, File> mapXml = generateHashMapForFile(xmlFiles);
        File[] txtFiles = getFileArrayInFolderByExtension(pathSource, "txt");

        List<File> listTxtFiles = new ArrayList(Arrays.asList(txtFiles));
        listTxtFiles.removeIf(x -> x.getName().contains("class_labels"));
        txtFiles = new File[listTxtFiles.size()];
        listTxtFiles.toArray(txtFiles);

        removeDirectoryRecursively(pathTarget);
        makeDirectory(pathTarget);
        makeDirectory(pathTarget + "/images");
        makeDirectory(pathTarget + "/images/train");
        makeDirectory(pathTarget + "/images/val");
        makeDirectory(pathTarget + "/images/test");
        makeDirectory(pathTarget + "/labels");
        makeDirectory(pathTarget + "/labels/train");
        makeDirectory(pathTarget + "/labels/val");
        makeDirectory(pathTarget + "/labels/test");

        if (shuffle) {
//            FactoryUtils.shuffle(imgFiles, seed);
//            FactoryUtils.shuffle(xmlFiles, seed);
            FactoryUtils.shuffle(txtFiles, seed);
        }
        int n = txtFiles.length;
        for (int i = 0; i < n; i++) {
            if (i <= (int) (n * trainRatio)) {
                FactoryUtils.copyFile(mapImg.get(getFileName(txtFiles[i].getName())), new File(pathTarget + "/images/train/" + getFileName(txtFiles[i].getName()) + ".jpg"));
                //FactoryUtils.copyFile(mapXml.get(getFileName(txtFiles[i].getName())), new File(pathTarget + "/images/train/" + xmlFiles[i].getName()));
                FactoryUtils.copyFile(txtFiles[i], new File(pathTarget + "/labels/train/" + txtFiles[i].getName()));

            } else if (i > (int) (n * trainRatio) && i <= (int) (n * (trainRatio + valRatio))) {
                FactoryUtils.copyFile(mapImg.get(getFileName(txtFiles[i].getName())), new File(pathTarget + "/images/val/" + getFileName(txtFiles[i].getName()) + ".jpg"));
                //FactoryUtils.copyFile(mapXml.get(getFileName(txtFiles[i].getName())), new File(pathTarget + "/images/val/" + xmlFiles[i].getName()));
                FactoryUtils.copyFile(txtFiles[i], new File(pathTarget + "/labels/val/" + txtFiles[i].getName()));
            } else {
                FactoryUtils.copyFile(mapImg.get(getFileName(txtFiles[i].getName())), new File(pathTarget + "/images/test/" + getFileName(txtFiles[i].getName()) + ".jpg"));
                //FactoryUtils.copyFile(mapXml.get(getFileName(txtFiles[i].getName())), new File(pathTarget + "/images/test/" + xmlFiles[i].getName()));
                FactoryUtils.copyFile(txtFiles[i], new File(pathTarget + "/labels/test/" + txtFiles[i].getName()));
            }
            System.out.println(i + ".items copied from " + n + " of items");
        }
        generateCoCoYamlYolo(pathTarget, classLabels);

    }

    public static void removeDirectoryRecursively(String folderPath) {
        if (!isFolderExist(folderPath)) {
            System.err.println("folder path does'nt exist");
            return;
        }
        Path path = Paths.get(folderPath);
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void generateCoCoYamlYolo(String pathTarget, String[] classLabels) {
        String content = "# Train/val/test sets as 1) dir: path/to/imgs, 2) file: path/to/imgs.txt, or 3) list: [path/to/imgs1, path/to/imgs2, ..]\n"
                + "path:  # dataset root dir (leave empty for HUB)\n"
                + "train: images/train  # train images (relative to 'path') 8 images\n"
                + "val: images/val  # val images (relative to 'path') 8 images\n"
                + "test:  # test images (optional)\n"
                + "\n"
                + "# Classes\n"
                + "names:\n";
        for (String lbl : classLabels) {
            String[] s = lbl.split(":");
            content += "  " + s[0] + ": " + s[1] + "\n";
        }
        FactoryUtils.writeToFile(pathTarget + "/coco6.yaml", content);
        System.out.println("coco6.yaml file is written now");
    }

    /**
     *
     * @param path : Path contains items which would be processed
     * @param ratio : probability between 0..1 as floating number
     * @param seed : random seed point
     * @param extension : file extension in order to filter the contents of the
     * path
     */
    public static void diluteDataSet(String path, float ratio, int seed, String extension) {
        File[] listFile = FactoryUtils.getFileArrayInFolderByExtension(path, extension);
        File[] xmlFile = FactoryUtils.getFileArrayInFolderByExtension(path, "xml");
        FactoryUtils.shuffle(listFile, seed);
        FactoryUtils.shuffle(xmlFile, seed);
        FactoryUtils.removeDirectory(path + "/dilute");
        FactoryUtils.makeDirectory(path + "/dilute");
        Random rnd = new Random(seed);
        if (xmlFile.length == listFile.length && xmlFile.length > 0) {
            for (int i = 0; i < listFile.length; i++) {
                float v = rnd.nextFloat();
                if (v < ratio) {
                    FactoryUtils.copyFile(listFile[i], new File(path + "/dilute/" + listFile[i].getName()));
                    FactoryUtils.copyFile(xmlFile[i], new File(path + "/dilute/" + xmlFile[i].getName()));
                    //FactoryUtils.copyFile(listFile.get(i).getAbsolutePath(),path+"/dilute/"+listFile.get(i).getName());
                }
            }
        } else {
            for (int i = 0; i < listFile.length; i++) {
                float v = rnd.nextFloat();
                if (v < ratio) {
                    FactoryUtils.copyFile(listFile[i], new File(path + "/dilute/" + listFile[i].getName()));
                }
            }
        }

    }

    private static CPoint convertPointToCPoint(Point p) {
        CPoint ret = new CPoint(p.y, p.x);
        return ret;
    }

    private static CPoint[] convertPointsToCPoints(Point[] polygon) {
        CPoint[] ret = new CPoint[polygon.length];
        int k = 0;
        for (Point point : polygon) {
            ret[k++] = new CPoint(point.y, point.x);

        }
        return ret;
    }

    public static void removeFilesContains(String path, String key) {
        File[] files = getFileListInFolder(path);
        int k = 0;
        for (File file : files) {
            if (file.getName().contains(key)) {
                deleteFile(file);
                System.out.println(k + " --> " + file.getName() + " deleted");
                k++;
            }
        }
        System.out.println(k + " files were deleted successfully.");
    }

    public static String readJSONFile(String path) {
        try {
            return FileUtils.readFileToString(new File(path), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(Deneme.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static String updatePascalVocObjectNames(String pathXML, String... params) {
        String xml = FactoryUtils.readPascalVocXMLAsString(pathXML);
        for (int i = 0; i < params.length; i++) {
            String[] s = params[i].split(":");
            xml = xml.replaceAll(s[0], s[1]);
        }
        FactoryUtils.writeToFile(pathXML, xml);
        return xml;
    }

    public static String removePascalVocObjectNames(String pathXML, String... params) {
        if (pathXML.contains("frame_000004.xml")) {
            int a = 11;
        }
        AnnotationPascalVOCFormat apv = FactoryUtils.readPascalVocXML(pathXML);
        List<PascalVocObject> lstObj = apv.lstObjects;
        List<PascalVocObject> ret = new ArrayList();
        for (PascalVocObject obj : lstObj) {
            for (String param : params) {
                if (obj.name.equals(param)) {
                    ret.add(obj);
                }
            }
        }
        lstObj.removeAll(ret);
        String ext = (!apv.imagePath.equals("null")) ? FactoryUtils.getFileExtension(apv.imagePath) : "jpg";
        String imagePath = pathXML.replace("xml", ext);
        serializePascalVocXML(apv.folder, apv.fileName, imagePath, apv.source, lstObj);
        return readPascalVocXMLAsString(pathXML);
    }

    public static void updatePascalVocObjectNamesBatchProcess(String path, String... params) {
        File[] files = FactoryUtils.getFileArrayInFolderByExtension(path, "xml");
        for (File file : files) {
            updatePascalVocObjectNames(file.getAbsolutePath(), params);
            System.out.println(file.getName() + " updated");
        }
    }

    public static void removePascalVocObjectNamesBatchProcess(String path, String... params) {
        File[] files = FactoryUtils.getFileArrayInFolderByExtension(path, "xml");
        for (File file : files) {
            removePascalVocObjectNames(file.getAbsolutePath(), params);
            System.out.println(file.getName() + " updated");
        }
    }

    public static Point[] clone(Point[] ps) {
        Point[] ret = new Point[ps.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = clone(ps[i]);
        }
        return ret;
    }

    public static Point clone(Point p) {
        Point ret = new Point();
        ret.x = p.x;
        ret.y = p.y;
        return ret;
    }

    public static void reIndexFilesBasedOnPrefixAndTimeStamp(String pathFolder, String extensions) {
        String[] exts = extensions.split(",");
        File[] files = getFileArrayInFolderByExtension(pathFolder, exts[0]);
        for (File file : files) {
            String newName = System.nanoTime() + ".";
            String path = getFolderPath(file.getAbsolutePath());
            String fileName = getFileName(file.getName());
            renameFile(new File(path + "/" + fileName + "." + exts[0]), new File(path + "/" + newName + exts[0]));
            for (int i = 1; i < exts.length; i++) {
                if (isFileExist(path + "/" + fileName + "." + exts[i])) {
                    renameFile(new File(path + "/" + fileName + "." + exts[i]), new File(path + "/" + newName + exts[i]));
                }
            }
        }

    }

    private static Map<String, File> generateHashMapForFile(File[] imgFiles) {
        Map<String, File> ret = new HashMap();
        int n = imgFiles.length;
        for (int i = 0; i < n; i++) {
            ret.put(getFileName(imgFiles[i].getName()), imgFiles[i]);
        }
        return ret;
    }

    private static void deleteEmptyFiles(String pathSource, String[] extensions) {
        File[] files = getFileArrayInFolderByExtension(pathSource, extensions[0]);
        List<String> emptyFiles = new ArrayList();
        for (File f : files) {
            if (isFileEmpty(f)) {
                emptyFiles.add(f.getAbsolutePath());
            }
        }
        files = null;
        System.gc();
        for (String ef : emptyFiles) {
            for (int i = 0; i < extensions.length; i++) {
                try {
                    Files.deleteIfExists(Paths.get(getParentFromPath(ef) + "/" + getFileName(getFileNameFromPath(ef)) + "." + extensions[i]));
                } catch (IOException ex) {
                    Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    private static boolean isFileEmpty(File f) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(f.getAbsolutePath()));
            if (br.readLine() == null) {
                return true;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static void balanceDatasetBasedOnFileExt(String pathSource, String... ext) {
        String filter = ext[0];
        File[] files = getFileArrayInFolderByExtension(pathSource, filter);
        Map<String, File>[] maps = new HashMap[ext.length - 1];
        System.out.println("bu metod henuz sonlanmadı");
    }

    /**
     * try to divide image into 2D cropped images and save them on target folder
     * with specified image format
     *
     * @param img
     * @param nr : number of rows
     * @param nc : number of columns
     * @param destinationFolder : cropped images stored in that folder
     * @param fileCaption : cropped image prefix name
     * @param imageExtension : cropped images extension ie "jpg", "png"
     * @param isShow : boolean isVisibile
     */
    public static void cropImageArray2D(BufferedImage img, int nr, int nc, String destinationFolder, String fileCaption, String imageExtension, boolean isShow) {
        int w = img.getWidth() / nc;
        int h = img.getHeight() / nr;
        FactoryUtils.makeDirectory(destinationFolder);
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                BufferedImage temp = ImageProcess.cropImage(img, new CRectangle(i * h, j * w, w, h));
                ImageProcess.saveImage(temp, destinationFolder + "/" + fileCaption + "_" + i + "_" + j + "." + imageExtension);
                if (isShow) {
                    FrameImage frm = new FrameImage();
                    frm.setImage(temp, destinationFolder, fileCaption + "_" + i + "_" + j + "." + imageExtension);
                    frm.setVisible(isShow);
                }
            }
        }
    }

    /**
     * try to divide image into 2D cropped images and save them on target folder
     * with specified image format
     *
     * @param img
     * @param width
     * @param destinationFolder : cropped images stored in that folder
     * @param height
     * @param fileCaption : cropped image prefix name
     * @param imageExtension : cropped images extension ie "jpg", "png"
     * @param isShow : boolean isVisibile
     */
    public static void cropImageArray2DByCropSize(BufferedImage img, int width, int height, String destinationFolder, String fileCaption, String imageExtension, boolean isShow) {
        int nr = img.getWidth() / width;
        int nc = img.getHeight() / height;
        FactoryUtils.makeDirectory(destinationFolder);
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                BufferedImage temp = ImageProcess.cropImage(img, new CRectangle(i * height, j * width, width, height));
                ImageProcess.saveImage(temp, destinationFolder + "/" + fileCaption + "_" + i + "_" + j + "." + imageExtension);
                if (isShow) {
                    FrameImage frm = new FrameImage();
                    frm.setImage(temp, destinationFolder, fileCaption + "_" + i + "_" + j + "." + imageExtension);
                    frm.setVisible(isShow);
                }
            }
        }
    }

    public static boolean isNear(Point p1, Point p2, int d) {
        if (Math.abs(p1.x - p2.x) <= d && Math.abs(p1.y - p2.y) <= d) {
            return true;
        } else {
            return false;
        }
    }

    public static Polygon clone(Polygon p) {
        Polygon ret = new Polygon(clone(p.xpoints), clone(p.ypoints), p.npoints);
        return ret;
    }

    public static Polygon shiftPolygon(Polygon p, int dx, int dy) {
        int n = p.npoints;
        for (int i = 0; i < n; i++) {
            p.xpoints[i] += dx;
            p.ypoints[i] += dy;
        }
        return p;
    }

    public static float[] getDiagonalVector(float[][] d) {
        if (d.length != d[0].length) {
            throw new UnsupportedOperationException("matrix is not square");
        }
        int n = d.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = d[i][i];
        }
        return ret;
    }

    public static void generateSegmentationMasks(File[] imageFiles) {
        Map<String, Color> colorMap = getClassLabelHashMap(imageFiles[0].getParent() + "/class_labels.txt");
        String folderPath = imageFiles[0].getParent();
        for (File imageFile : imageFiles) {
            BufferedImage img = ImageProcess.imread(imageFile.getAbsolutePath());
            int w = img.getWidth();
            int h = img.getHeight();
            BufferedImage imageMask = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D gr = imageMask.createGraphics();
            gr.setColor(Color.BLACK);
            gr.fillRect(0, 0, w, h);
            String xmlFile = folderPath + "/" + getFileName(imageFile.getName()) + ".xml";
            if (isFileExist(xmlFile)) {
                AnnotationPascalVOCFormat apv = deserializePascalVocXML(xmlFile);
                for (PascalVocObject pvo : apv.lstObjects) {
                    gr.setColor(colorMap.get(pvo.polygonContainer.name));
                    gr.fillPolygon(pvo.polygonContainer.polygon);
                }
                ImageProcess.saveImage(imageMask, folderPath + "/" + getFileName(imageFile.getName()) + "_mask.jpg");
            }

        }
    }

    public static Map<String, Color> getClassLabelHashMap(String path) {
        String[] content = readFile(path).split("\n");
        Map<String, Color> ret = new HashMap<String, Color>();
        for (String str : content) {
            String[] s = str.split(":");
            String[] cols = s[1].split(" ");
            ret.put(s[0], new Color(toInt(cols[1]), toInt(cols[2]), toInt(cols[3])));
        }
        return ret;
    }

    public static int toInt(String s) {
        return Integer.parseInt(s);
    }

    public static String convertPascalVoc2YoloFormatPolygonSingle(String path, String[] refList) {
        AnnotationPascalVOCFormat bbp = deserializePascalVocXML(path);
        int w = bbp.size.width;
        int h = bbp.size.height;
        String ret = "";
        Map<String, Integer> map = new HashMap();
        for (String str : refList) {
            String s = str.split(":")[1];
            int i = Integer.parseInt(str.split(":")[0]);
            map.put(s, i);
        }
        for (PascalVocObject pv : bbp.lstObjects) {
            int np = pv.polygonContainer.polygon.npoints;
            String str = "";
            for (int i = 0; i < np; i++) {
                int px = pv.polygonContainer.polygon.xpoints[i];
                int py = pv.polygonContainer.polygon.ypoints[i];
                float fpx = 1.0f * px / w;
                float fpy = 1.0f * py / h;
                str += " " + fpx + " " + fpy;
            }
            if (!map.containsKey(pv.name)) {
                continue;
            }
            int class_index = map.get(pv.name);
            ret += class_index + str + "\n";
        }
        return ret;
    }

    /**
     * 
     * @param path
     * @param refList
     * @return
     */
    public static String convertPascalVoc2YoloFormatDetectionSingle(String path, String[] refList) {
        AnnotationPascalVOCFormat bbp = deserializePascalVocXML(path);
        int w = bbp.size.width;
        int h = bbp.size.height;
        String ret = "";
        int x1, x2, y1, y2, n, class_index;
        float px1, px2, py1, py2;
        Map<String, Integer> map = new HashMap();
        for (String str : refList) {
            String s = str.split(":")[1];
            int i = Integer.parseInt(str.split(":")[0]);
            map.put(s, i);
        }
        for (PascalVocObject pv : bbp.lstObjects) {
            x1 = pv.bndbox.xmin;
            y1 = pv.bndbox.ymin;
            x2 = pv.bndbox.xmax;
            y2 = pv.bndbox.ymax;
            px1 = (x1 + x2) / 2.0f / w;
            py1 = (y1 + y2) / 2.0f / h;
            px2 = (x2 - x1) * 1.0f / w;
            py2 = (y2 - y1) * 1.0f / h;
            if (!map.containsKey(pv.name)) {
                continue;
            }
            class_index = map.get(pv.name);
            ret += class_index + " " + px1 + " " + py1 + " " + px2 + " " + py2 + "\n";
        }
        return ret;
    }

    public static List<DataAnalytics> getDataAnalytics(String imageFolderPath) {
        List<String> classNames = getClassNamesFromClassLabelsTxtFile(imageFolderPath);
        if (classNames == null) {
            return null;
        }
        List<DataAnalytics> ret = new ArrayList<>();
        for (String className : classNames) {
            ret.add(new DataAnalytics(className));
        }

        File[] files = getFileArrayInFolderByExtension(imageFolderPath, "xml");
        if (files.length == 0) {
            return null;
        }
        int total = 0;
        for (File file : files) {
            if (checkIntegrityOfXmlFileWithCorrespondingImages(file)) {
                AnnotationPascalVOCFormat apv = FactoryUtils.deserializePascalVocXML(file.getAbsolutePath());
                List<PascalVocObject> lst = apv.lstObjects;
                for (PascalVocObject pvo : lst) {
                    if (classNames.contains(pvo.name)) {
                        DataAnalytics da = getDataAnalyticItem(ret, pvo.name);
                        da.frequency++;
                        total++;
                    }
                }
            }
        }
        for (DataAnalytics da : ret) {
            da.ratio = Math.round(da.frequency / total * 100);
        }
        return ret;
    }

    public static List<String> getClassNamesFromClassLabelsTxtFile(String folderPath) {
        if (isFileExist(folderPath + "/class_labels.txt")) {
            String[] str = readFile(folderPath + "/class_labels.txt").split("\n");
            List<String> ret = new ArrayList();
            for (int i = 0; i < str.length; i++) {
                ret.add(str[i].split(":")[0]);
            }
            return ret;
        } else {
            System.err.println("class_labels.txt file does not exist...");
            return null;
        }
    }

    public static boolean checkIntegrityOfXmlFileWithCorrespondingImages(File file) {
        String folderPath = file.getParent();
        String imageFileJpg = folderPath + "/" + getFileName(file.getName()) + ".jpg";
        String imageFilePng = folderPath + "/" + getFileName(file.getName()) + ".png";
        String imageFileJPEG = folderPath + "/" + getFileName(file.getName()) + ".JPEG";
        if (isFileExist(imageFileJpg) || isFileExist(imageFilePng) || isFileExist(imageFileJPEG)) {
            return true;
        } else {
            return false;
        }
    }

    public static DataAnalytics getDataAnalyticItem(List<DataAnalytics> ret, String name) {
        for (DataAnalytics da : ret) {
            if (da.dataName.equals(name)) {
                return da;
            }
        }
        return null;
    }

    public static String[] getStringTokens(String s) {
        StringTokenizer st = new StringTokenizer(s);
        int n = st.countTokens();
        String[] ret = new String[n];
        int i = 0;
        while (st.hasMoreTokens()) {
            ret[i++] = st.nextToken();
        }
        return ret;
    }

    public static float[][] parseData(String text) {
        String[] s = text.split("\n");
        String seperator = getSeperator(s);
        if (s.length == 0) {
            return null;
        }
        int nr = s.length;
        int nc = s[0].split(seperator).length;
        boolean isColumnNamesExist = checkColumnName(s[0], seperator);
        float[][] data = null;
        if (isColumnNamesExist) {
            data = new float[nr - 1][nc];
            for (int i = 1; i < nr; i++) {
                String[] row = s[i].split(seperator);
                for (int j = 0; j < nc; j++) {
                    data[i - 1][j] = Float.parseFloat(row[j]);
                }
            }
        } else {
            data = new float[nr][nc];
            for (int i = 0; i < nr; i++) {
                String[] row = s[i].split(seperator);
                for (int j = 0; j < nc; j++) {
                    data[i][j] = Float.parseFloat(row[j]);
                }
            }
        }
        return data;
    }

    public static String getSeperator(String[] s) {
        int n = s.length;
        int m = 0;
        String seperator = "\t";
        m = s[0].split("\t").length;

        if (m > 1) {
            seperator = "\t";
        } else {
            m = s[0].split(",").length;
            if (m > 1) {
                seperator = ",";
            } else {
                m = s[0].split(";").length;
                if (m > 1) {
                    seperator = ";";
                } else {
                    m = s[0].split(" ").length;
                    if (m > 1) {
                        seperator = " ";
                    } else {
                        System.err.println("Data should have csv format (ie: comma or blank seperated numeric values) \nOnly first row can be feature names with comma or blanks seperated String values..");
                        return seperator;
                    }
                }
            }
        }
        for (int i = 0; i < n; i++) {
            int q = s[i].split(seperator).length;
            if (m != q) {
                System.err.println("Column number of Dataset is not consistent");
                return null;
            }
        }
        return seperator;
    }

    private static boolean checkColumnName(String str, String sep) {
        String[] s = str.split(sep);
        try {
            float f = Float.parseFloat(s[0]);
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    public static float[][] timesScalar(float[][] d, float scale) {
        int nr = d.length;
        int nc = d[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                d[i][j] *= scale;
            }
        }
        return d;
    }

    public static float[] timesScalar(float[] d, float scale) {
        int nr = d.length;
        for (int i = 0; i < nr; i++) {
            d[i] *= scale;
        }
        return d;
    }

    public static float[][] timesScalarParallel(float[][] d, float scale) {
        int nr = d.length;
        int nc = d[0].length;

        // Convert the array to a parallel stream and modify elements in-place.
        Arrays.parallelSetAll(d, i -> {
            float[] row = new float[nc];
            for (int j = 0; j < nc; j++) {
                row[j] = d[i][j] * scale;
            }
            return row;
        });

        return d;
    }

    public static float[] logPlusScalar(float[] f, float val) {
        int n = f.length;
        for (int i = 0; i < n; i++) {
            f[i] = (float) Math.log(f[i]) + val;
        }
        return f;
    }

    public static float[][][][] loadDataSetAs4DFloatFromImages(String path_train, int nChannel) {
        float[][][][] ret = null;

        return ret;
    }

    public static float[][][] toARGB(float[][][] f) {
        int nr = f[0].length;
        int nc = f[0][0].length;
        float[][][] ret = new float[f.length + 1][nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[0][i][j] = 255;
            }
        }
        for (int i = 1; i < 4; i++) {
            ret[i] = f[i - 1];
        }
        return ret;
    }

    public static Map getHashMapHistogramByFileName(String path, String regex) {
        File[] imgs = FactoryUtils.getFileArrayInFolderForImages(path);
        Map<String, Integer> hist = new HashMap<>();
        for (File img : imgs) {
            String name = (regex != null && regex != "") ? img.getName().split(regex)[0] : img.getName();
            if (hist.containsKey(name)) {
                hist.replace(name, hist.get(name) + 1);
            } else {
                hist.put(name, 1);
            }
        }
        for (Map.Entry<String, Integer> entry : hist.entrySet()) {
            System.out.println(entry);
        }
        return hist;
    }

    public static Map getHashMapHistogramByFolderName(String path) {
        File[] dirs = FactoryUtils.getDirectories(path);
        Map<String, Integer> hist = new HashMap<>();
        for (File dir : dirs) {
            String name = dir.getName();
            if (!hist.containsKey(name)) {
                hist.put(name, 0);
                File[] imgs = FactoryUtils.getFileArrayForImagesSubFoldersRecursively(path + "/" + name);
                hist.replace(name, imgs.length);
            }
        }
        for (Map.Entry<String, Integer> entry : hist.entrySet()) {
            System.out.println(entry);
        }
        return hist;
    }

    public static String[] resolveHashMapToLabels(Map<String, Integer> map) {
        String[] label = new String[map.size()];
        int k = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            label[k++] = entry.getKey();
        }
        return label;
    }

    public static float[] resolveHashMapToArray(Map<String, Integer> map) {
        float[] ret = new float[map.size()];
        int k = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            ret[k++] = entry.getValue();
        }
        return ret;
    }

    public static String getMaximum(String[] str) {
        return Arrays.stream(str)
                .max(String::compareTo)
                .orElse(null);
    }
//    public static Rectangle getBoundingRectangle(Polygon polygon) {
//        if (polygon.npoints <= 0) {
//            return null;
//        }
//        Rectangle ret = new Rectangle();
//        Point p = listPolygon.get(0);
//        int minX = p.x;
//        int minY = p.y;
//        int maxX = p.x;
//        int maxY = p.y;
//        for (Point point : listPolygon) {
//            if (point.x < minX) {
//                minX = point.x;
//            }
//            if (point.y < minY) {
//                minY = point.y;
//            }
//            if (point.x > maxX) {
//                maxX = point.x;
//            }
//            if (point.y > maxY) {
//                maxY = point.y;
//            }
//        }
//        ret.x = minX;
//        ret.y = minY;
//        ret.width = maxX - minX;
//        ret.height = maxY - minY;
//        return ret;
//    }

    public static int confirmMessage(String msg) {
        return JOptionPane.showConfirmDialog(null, msg);
    }

    public static String removeLastChar(String content) {
        return content.substring(0, content.length()-1);
    }

    public static String buildJsonFileAsTuSimpleFormat(String imageFolder) {
        String ret="";
        File[] images=FactoryUtils.getFileArrayInFolderByExtension(imageFolder+"/seg_label", "png");
        int[] rowIndex=CMatrix.getInstance().range(240, 720, 10).toIntArray1D();
        
        for (File image : images) {
            BufferedImage img=ImageProcess.imread(image);
            img=ImageProcess.rgb2gray(img);
            int[][] m=ImageProcess.imageToPixelsInt(img);
            boolean firstNonZero=false;
            for (int i = 0; i < rowIndex.length; i++) {
                int[] row=m[rowIndex[i]];
                //ArrayList<TGapIndex>[] lanes=new ArrayList[4];
                HashMap<Integer,Integer> lanes=new HashMap();
                String[] strLane=new String[4];
                ArrayList<TGapIndex> lst=null;
                int z=0;
                for (int j = row.length-1; j > 0; j--) {
                    if (row[j]!=0) {
                        if (!firstNonZero){                            
                            firstNonZero=true;
                        }
                        lst=new ArrayList();
                        int k=1;
                        int gap=0;
                        while(true){
                            int top=m[i-k][j];
                            int bottom=m[i+k][j];
                            if (top!=0 && bottom!=0) {
                               gap=2*k; 
                            }else{
                               break; 
                            }
                            k++;
                        }
                        lst.add(new TGapIndex(j, gap));
                    }else{
                        if (firstNonZero) {
                            firstNonZero=false;
                            int max=0;
                            int index=0;
                            for (TGapIndex gp : lst) {
                                if (max<gp.gap) {
                                    max=gp.gap;
                                    index=gp.index;
                                }
                            }
                            //lanes.put(z++, index);
                            strLane[z++]+=index+",";
                        }
                    }
                }
                
            }
            
        }
        return ret;
    }
    

    public <T> List<T> toArrayList(T[][] twoDArray) {
        List<T> list = new ArrayList<T>();
        for (T[] array : twoDArray) {
            list.addAll(Arrays.asList(array));
        }
        return list;
    }

    public static void renameFilesAsAscIndex(String path, String extension) {
        File[] files = FactoryUtils.getFileListInFolder(path);
        for (int j = 0; j < files.length; j++) {
            files[j].renameTo(new File(path + "/" + j + "." + extension));
        }
    }

    public static float[] resolveParam(String s, int max) {
        float[] ret = null;
        if (s.contains(":")) {
            String[] ss = s.split(":");
            if (ss.length == 3) {
                float from = Float.parseFloat(ss[0]);
                float to = Float.parseFloat(ss[1]);
                float incr = Float.parseFloat(ss[2]);
                ret = vector(from, to, incr);
            } else if (ss.length <= 2 && !s.contains(",")) {
                if (ss.length == 1) {
                    s = ss[0] + ":end";
                    ss = s.split(":");
                    ss[1] = ss[1].replace("end", (max - 1) + "");
                } else if (ss[1].indexOf("end") != -1) {
                    ss[1] = ss[1].replace("end", (max - 1) + "");
                }
//                else {
                try {
                    if (ss[0].isEmpty() && ss[1].isEmpty()) {
                        ss[0] = "0";
                        ss[1] = (max - 1) + "";
                    } else if (ss[0].isEmpty()) {
                        ss[0] = "0";
                        int q = Integer.parseInt(ss[1]);
                        if (q < 0) {
                            ss[1] = (max + q) + "";
                        }
                    } else if (ss[1].isEmpty()) {
                        int q = Integer.parseInt(ss[0]);
                        if (q < 0) {
                            ss[0] = (max + q) + "";
                        }
                        ss[1] = (max - 1) + "";
                    } else {
                        int q = Integer.parseInt(ss[0]);
                        if (q < 0) {
                            ss[0] = (max + q) + "";
                        }
                        q = Integer.parseInt(ss[1]);
                        if (q < 0) {
                            ss[1] = (max + q) + "";
                        }
                    }
                } catch (Exception e) {
                }
//                }
                int from = Integer.parseInt(ss[0]);
                int to = Integer.parseInt(ss[1]);
                if (from >= max || to > max) {
                    System.out.println("range check error please correct from:to range index");
                    ret = null;
                } else if (from == to) {
                    ret = new float[]{from, from};
                } else if (from < to) {
                    ret = FactoryUtils.vector(from, to);
                } else if (from > to) {
                    ret = FactoryUtils.vector(to, from);
                }
            } //belki de gelen parametre 1,3,7:13,15,-2 gibi bir şeydir
            else {
                String[] p = s.split(",");
                //ilkönce negatif olanları işle
                List<Float> lst = new ArrayList();
                for (int i = 0; i < p.length; i++) {
                    if (!p[i].contains("-") && !p[i].contains(":")) {
                        lst.add(Float.parseFloat(p[i]));
                    } else if (p[i].contains("-") && !p[i].contains(":")) {
                        lst.add((max - 1) + Float.parseFloat(p[i]));
                    } else if (p[i].contains(":")) {
                        String[] q = p[i].split(":");
                        if (q[0].contains("-")) {
                            q[0] = (max - 1 + Integer.parseInt(q[0])) + "";
                        }
                        if (q[1].contains("-")) {
                            q[1] = (max - 1 + Integer.parseInt(q[1])) + "";
                        }
                        int from = Integer.parseInt(q[0]);
                        int to = Integer.parseInt(q[1]);
                        float[] d = FactoryUtils.vector(from, to);
                        for (int j = 0; j < d.length; j++) {
                            lst.add(d[j]);
                        }
                    }
                }
                ret = new float[lst.size()];
                for (int i = 0; i < ret.length; i++) {
                    ret[i] = lst.get(i);
                }
            }
        } else {
            String[] p = s.split(",");
            ret = new float[p.length];
            for (int i = 0; i < p.length; i++) {
                if (p[i].contains("-")) {
                    ret[i] = max + Integer.parseInt(p[i]);
                } else {
                    ret[i] = Integer.parseInt(p[i]);
                }

            }
        }
        return ret;
    }

    public static float[] resolveParamForRange(String s) {
        float[] ret = null;
        if (s.contains(",")) {
            throw new ArithmeticException("wrong parameters for range");
        }
        if (s.contains(":")) {
            String[] ss = s.split(":");
            if (ss.length == 3) {
                float from = Float.parseFloat(ss[0]);
                float to = Float.parseFloat(ss[1]);
                float incr = Float.parseFloat(ss[2]);
                ret = vector(from, to, incr);
            } else if (ss.length == 2) {
                float from = Float.parseFloat(ss[0]);
                float to = Float.parseFloat(ss[1]);
                if (from < to) {
                    ret = vector(from, to, 1);
                } else {
                    ret = vector(from, to, -1);
                }

            }
        }
        return ret;
    }

    public static int[] byte2Int(byte[] b) {
        int nr = b.length;
        int[] ret = new int[nr];
        for (int i = 0; i < nr; i++) {
            ret[i] = byte2Int(b[i]);
        }
        return ret;
    }

    public static float[] byte2Float(byte[] b) {
        int nr = b.length;
        float[] ret = new float[nr];
        for (int i = 0; i < nr; i++) {
            ret[i] = byte2Float(b[i]);
        }
        return ret;
    }

    public static int byte2Int(byte b) {
        return b & 0xFF;
    }

    public static long byte2Long(byte b) {
        return (long) (b & 0xFF);
    }

    public static short byte2Short(byte b) {
        return (short) (b & 0xFF);
    }

    public static float byte2Float(byte b) {
        return (float) (b & 0xFF);
    }

//    public static float byte2Float(byte b) {
//        return (float) (b & 0xFF);
//    }
    public static String getLocalIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "127.0.0.1";
    }

    public float distanceHausDorff(float[][] d1, float[][] d2) {
        float ret = 0;

        return ret;
    }

    public static void startJavaServer(final SocketServer server) {
        new Thread(() -> {
            try {
                //server = new SocketServer(port);
                //int port = 8887;
                server.start();
                System.out.println("Java WebSocket Server started on port: " + server.getPort());
                BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
                running.set(true);
                while (running.get()) {
                    try {
                        if (sysin.ready()) {
                            String in;
                            try {
                                in = sysin.readLine();
                                //server.broadcast(in);
                                if (in.equals("exit")) {
                                    try {
                                        server.stop(1000);
                                        System.out.println("Java Server is stopping");
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    break;
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        Thread.sleep(1);
                    } catch (IOException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                System.out.println("Java Server was stopped");
                server.stop();
            } catch (UnknownHostException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    }

    public static void stopWebsocketServer() {
        System.out.println("stop server command stop server flag is true");
        stopServer = true;
    }

    public static WebSocketClient connectToPythonServer(InterfaceCallBack cb) {
        //icbf = cb;
        isConnectPythonServer = true;
        try {
            client = new WebSocketClient(new URI("ws://localhost:8888"), new Draft_6455()) {

                @Override
                public void onMessage(String message) {
//                    System.out.println("incoming message from python server = " + message);
                    cb.onMessageReceived(message);
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("You connected to python server: " + getURI() + "\n");
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason + "\n");
                }

                @Override
                public void onError(Exception ex) {
                    System.out.println("Can't connect to python server:8888");
                }
            };
            client.connect();
        } catch (URISyntaxException ex) {
            System.out.println("ws://localhost:8888" + " is not a valid WebSocket URI\n");
            isConnectPythonServer = false;
        }
        return client;
    }

    public static void bekle(int milliSeconds) {
        delay(milliSeconds);
    }

    public static void delay(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void executeCmdCommand(String cmd) {
        executeCommand("cmd.exe", cmd, true);
    }

    public static void executeCmdCommand(String cmd, boolean isClosedAfter) {
        executeCommand("cmd.exe", cmd, isClosedAfter);
    }

    public static void executeCommand(String program, String command, boolean isClosed) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (isClosed) {
            processBuilder.command(program, "/c", command);
        } else {
            processBuilder.command(program, command);
        }
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            int exitCode = process.waitFor();
            System.out.println("\nExited with error code : " + exitCode);
            if (exitCode == 1) {
                BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errorString = error.readLine();
                while (errorString != null) {
                    System.out.println("errorString = " + errorString);
                    errorString = error.readLine();
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sendDataToSerialPort(SerialType st, String s1) {
        String s = s1 + "\n";
        try {
            st.output.write(s.getBytes());
            st.output.flush();
            System.out.println("message was sent to arduino:" + s1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getRelativePath(String absolutePath, String relativeTo) {
        return convertToRelativePath(absolutePath, relativeTo);
    }

    public static String convertToRelativePath(String absolutePath, String relativeTo) {
        StringBuilder relativePath = null;

        // Thanks to:
        // http://mrpmorris.blogspot.com/2007/05/convert-absolute-path-to-relative-path.html
        absolutePath = absolutePath.replaceAll("\\\\", "/");
        relativeTo = relativeTo.replaceAll("\\\\", "/");

        if (absolutePath.equals(relativeTo) == true) {

        } else {
            String[] absoluteDirectories = absolutePath.split("/");
            String[] relativeDirectories = relativeTo.split("/");

            //Get the shortest of the two paths
            int length = absoluteDirectories.length < relativeDirectories.length
                    ? absoluteDirectories.length : relativeDirectories.length;

            //Use to determine where in the loop we exited
            int lastCommonRoot = -1;
            int index;

            //Find common root
            for (index = 0; index < length; index++) {
                if (absoluteDirectories[index].equals(relativeDirectories[index])) {
                    lastCommonRoot = index;
                } else {
                    break;
                    //If we didn't find a common prefix then throw
                }
            }
            if (lastCommonRoot != -1) {
                //Build up the relative path
                relativePath = new StringBuilder();
                //Add on the ..
                for (index = lastCommonRoot + 1; index < absoluteDirectories.length; index++) {
                    if (absoluteDirectories[index].length() > 0) {
                        relativePath.append("../");
                    }
                }
                for (index = lastCommonRoot + 1; index < relativeDirectories.length - 1; index++) {
                    relativePath.append(relativeDirectories[index] + "/");
                }
                relativePath.append(relativeDirectories[relativeDirectories.length - 1]);
            }
        }
        return relativePath == null ? null : relativePath.toString();
    }

    public static void listSystemProperties() {
        System.getProperties().list(System.out);
    }

    public static String getOperatinSystemName() {
        return System.getProperty("os.name");
    }

    /**
     *
     * split base dataset as train, validation and test sets.
     *
     * @param base_path : Base path should contain class folders.
     * @param r_train : i.e 0.8 for train and 0.2 for test
     * @param r_valid : i.e 0.05 for validation
     * @param r_test : i.e 0.15 for test
     */
    public static void splitTrainValidTestFolder(String base_path, float r_train, float r_valid, float r_test) {
        FactoryUtils.removeDirectory(base_path + "/train");
        FactoryUtils.removeDirectory(base_path + "/valid");
        FactoryUtils.removeDirectory(base_path + "/test");
        File[] folders = FactoryUtils.getDirectories(base_path);
        FactoryUtils.makeDirectory(base_path + "/train");
        FactoryUtils.makeDirectory(base_path + "/valid");
        FactoryUtils.makeDirectory(base_path + "/test");
        for (File folder : folders) {
            File[] imgs = FactoryUtils.getFileListDataSetForImageClassification(folder.getAbsolutePath());
            List<File> lst = Arrays.asList(imgs);
            Collections.shuffle(lst);
            int n = imgs.length;
            FactoryUtils.makeDirectory(base_path + "/train/" + folder.getName());
            FactoryUtils.makeDirectory(base_path + "/valid/" + folder.getName());
            FactoryUtils.makeDirectory(base_path + "/test/" + folder.getName());
            for (int i = 0; i < n; i++) {
                if (i < (int) (n * r_train)) {
                    FactoryUtils.copyFile(lst.get(i), new File(base_path + "/train/" + folder.getName() + "/" + lst.get(i).getName()));
                } else if (i >= (int) (n * r_train) && i < (int) (n * (r_train + r_valid))) {
                    FactoryUtils.copyFile(lst.get(i), new File(base_path + "/valid/" + folder.getName() + "/" + lst.get(i).getName()));
                } else {
                    FactoryUtils.copyFile(lst.get(i), new File(base_path + "/test/" + folder.getName() + "/" + lst.get(i).getName()));
                }
            }

        }

    }

    /**
     * split base dataset as train and test sets.
     *
     * @param base_path : Base path should contain class folders.
     * @param r_train : i.e 0.8 for train and 0.2 for test
     * @param r_test : i.e 0.2 for test 0.8 for train
     */
    public static void splitTrainTestFolder(String base_path, float r_train, float r_test) {
        FactoryUtils.removeDirectory(base_path + "/train");
        FactoryUtils.removeDirectory(base_path + "/test");
        File[] folders = FactoryUtils.getDirectories(base_path);
        FactoryUtils.makeDirectory(base_path + "/train");
        FactoryUtils.makeDirectory(base_path + "/test");
        for (File folder : folders) {
            File[] imgs = FactoryUtils.getFileListDataSetForImageClassification(folder.getAbsolutePath());
            List<File> lst = Arrays.asList(imgs);
            Collections.shuffle(lst);
            int n = imgs.length;
            FactoryUtils.makeDirectory(base_path + "/train/" + folder.getName());
            FactoryUtils.makeDirectory(base_path + "/test/" + folder.getName());
            for (int i = 0; i < n; i++) {
                if (i <= (int) (n * r_train)) {
                    FactoryUtils.copyFile(lst.get(i), new File(base_path + "/train/" + folder.getName() + "/" + lst.get(i).getName()));
                } else {
                    FactoryUtils.copyFile(lst.get(i), new File(base_path + "/test/" + folder.getName() + "/" + lst.get(i).getName()));
                }
            }

        }

    }

    /**
     * split 0.7 train, 0.1 validation and 0.2 test folder as default
     *
     * @param path
     */
    public static void splitTrainValidTestFolder(String path) {
        splitTrainValidTestFolder(path, 0.7f, 0.1f, 0.2f);
    }

    public static int getMaxWidth(Graphics gr, String[] t) {
        Rectangle2D r1 = gr.getFont().getStringBounds(t[0], 0, t[0].length(), gr.getFontMetrics().getFontRenderContext());
        Rectangle2D r2 = gr.getFont().getStringBounds(t[1], 0, t[1].length(), gr.getFontMetrics().getFontRenderContext());
        int ret = (int) Math.max(r1.getWidth(), r2.getWidth()) + 20;
        return ret;
    }

    public static int getMaxHeight(Graphics gr, String[] t) {
        Rectangle2D r1 = gr.getFont().getStringBounds(t[0], 0, t[0].length(), gr.getFontMetrics().getFontRenderContext());
        Rectangle2D r2 = gr.getFont().getStringBounds(t[1], 0, t[1].length(), gr.getFontMetrics().getFontRenderContext());
        int ret = (int) Math.max(r1.getHeight(), r2.getHeight());
        return ret;
    }

    public static String serializePascalVocXML(String folder, String fileName, String imagePath, PascalVocSource source, List<PascalVocObject> lstObjects) {
        BufferedImage image = ImageProcess.imread(imagePath);
        int type = image.getType();
        int depth = 3;
        if (type == TYPE_INT_BGR) {
            depth = 3;
        }
        PascalVocSize size = new PascalVocSize(image.getWidth(), image.getHeight(), depth);
        String lst = "";
        for (PascalVocObject obj : lstObjects) {
            lst += obj.toString();
        }

        String ret = "<annotation>\n"
                + "\t<folder>" + folder + "</folder>\n"
                + "\t<filename>" + fileName + "</filename>\n"
                + "\t<path>" + imagePath + "</path>\n"
                + source.toString()
                + size.toString()
                + "\t<segmented>0</segmented>\n"
                + lst
                + "</annotation>\n";
        File file = new File(imagePath);
        FactoryUtils.writeToFile(file.getParent() + "/" + FactoryUtils.getFileName(file.getName()) + ".xml", ret);
        return ret;

    }

    public static AnnotationPascalVOCFormat readPascalVocXML(String filePath) {
        return deserializePascalVocXML(filePath);
    }

    public static String readPascalVocXMLAsString(String filePath) {
        return deserializePascalVocXML(filePath).toString();
    }

    public static Object deserializeJSON(String filePath, Object obj) {
        Gson gson = new Gson();
        try (Reader reader = new FileReader(filePath)) {
            obj = gson.fromJson(reader, obj.getClass());
            //System.out.println("obj = " + obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static AnnotationPascalVOCFormat convertJSONtoPascalVoc4Polygon(String filePath) {
        YoloPolygonJson obj = (YoloPolygonJson) deserializeJSON(filePath, new YoloPolygonJson());
        AnnotationPascalVOCFormat ret = new AnnotationPascalVOCFormat();

        ret.folder = obj.imagePath;
        ret.fileName = obj.imagePath;
        ret.imagePath = obj.imagePath;

        PascalVocSource source = new PascalVocSource();
        String database = "Unknown";
        String annotation = "Unknown";
        String image = "Unknown";
        source = new PascalVocSource(database, annotation, image);
        ret.source = source;

        int width = obj.imageWidth;
        int height = obj.imageHeight;
        int depth = 3;
        PascalVocSize size = new PascalVocSize(width, height, depth);
        ret.size = size;
        List<PascalVocObject> lstObjects = new ArrayList();

        int cnt = obj.shapes.size();
        for (int i = 0; i < cnt; i++) {
            Polygon poly = new Polygon();
            YoloPolygonJson.Shape shp = obj.shapes.get(i);
            int n = shp.points.length;
            for (int j = 0; j < n; j++) {
                poly.addPoint(Math.round(shp.points[j][0]), Math.round(shp.points[j][1]));
            }
            PascalVocBoundingBox bbox = new PascalVocBoundingBox(shp.label, poly.getBounds(), poly.getBounds().x, poly.getBounds().y, Color.yellow);
            PascalVocPolygon pvp = new PascalVocPolygon(shp.label, poly, poly.getBounds().x, poly.getBounds().y, Color.yellow);
            List<PascalVocAttribute> attributeList = null;
            PascalVocObject pvo = new PascalVocObject(shp.label, "Unspecified", 0, 0, 0, bbox, pvp, attributeList);
            lstObjects.add(pvo);
        }
        ret.lstObjects = lstObjects;
        return ret;
    }

    public static AnnotationPascalVOCFormat deserializePascalVocXML(String filepath) {
        String s = FactoryUtils.readFile(filepath);
        String folder = s.substring(s.indexOf("<folder>") + 8, s.indexOf("</folder>"));
        AnnotationPascalVOCFormat ret = new AnnotationPascalVOCFormat();
        ret.folder = folder;
        String fileName = s.substring(s.indexOf("<filename>") + 10, s.indexOf("</filename>"));
        ret.fileName = fileName;
        if (s.indexOf("<path>") != -1) {
            String path = s.substring(s.indexOf("<path>") + 6, s.indexOf("</path>"));
            ret.imagePath = path;
        }

        PascalVocSource source = new PascalVocSource();
        if (s.indexOf("<source>") != -1) {
            String database = "Unknown";
            if (s.indexOf("<database>") != -1) {
                database = s.substring(s.indexOf("<database>") + 10, s.indexOf("</database>"));
            }
            String annotation = "Unknown";
            if (s.indexOf("<annotation>", s.indexOf("<source>")) != -1) {
                annotation = s.substring(s.indexOf("<annotation>", s.indexOf("<source>")) + 12, s.indexOf("</annotation>", s.indexOf("<source>")));
            }
            String image = "Unknown";
            if (s.indexOf("<image>") != -1) {
                image = s.substring(s.indexOf("<image>") + 7, s.indexOf("</image>"));
            }
            source = new PascalVocSource(database, annotation, image);
        }
        ret.source = source;

        int width = Integer.parseInt(s.substring(s.indexOf("<width>") + 7, s.indexOf("</width>")));
        int height = Integer.parseInt(s.substring(s.indexOf("<height>") + 8, s.indexOf("</height>")));
        int depth = 0;
        if (s.indexOf("<depth>") != -1 && s.substring(s.indexOf("<depth>") + 7, s.indexOf("</depth>")).length() > 0) {
            depth = Integer.parseInt(s.substring(s.indexOf("<depth>") + 7, s.indexOf("</depth>")));
        }
        PascalVocSize size = new PascalVocSize(width, height, depth);
        ret.size = size;
        List<PascalVocObject> lstObjects = new ArrayList();
        long count = s.split("</object>").length - 1;
        int nameIndex1 = 0;
        int xminIndex1 = 0;
        int yminIndex1 = 0;
        int xmaxIndex1 = 0;
        int ymaxIndex1 = 0;

        int nameIndex2 = 0;
        int xminIndex2 = 0;
        int yminIndex2 = 0;
        int xmaxIndex2 = 0;
        int ymaxIndex2 = 0;
        char[] ch = s.toCharArray();
        int indexAttributes = 0;
        int indexAttributesEnd = 0;
        int indexObjectEnd = 0;

        int x_temp_index1 = 0;
        int x_index1 = 0; //for parsing polygon
        int x_index2 = 0; //for parsing polygon
        int y_index1 = 0; //for parsing polygon
        int y_index2 = 0; //for parsing polygon

        for (int i = 0; i < count; i++) {
            indexAttributes = s.indexOf("<attributes>", indexAttributes + 1);
            indexObjectEnd = s.indexOf("</object>", indexObjectEnd + 1);
            String name = s.substring(s.indexOf("<name>", nameIndex1 + 1) + 6, s.indexOf("</name>", nameIndex2 + 1));
            nameIndex1 = s.indexOf("<name>", indexObjectEnd) - 25;
            nameIndex2 = s.indexOf("</name>", indexObjectEnd) - 25;

            boolean isPolygonExist = (s.indexOf("<polygon>", xminIndex1 + 1) != -1) ? true : false;
            PascalVocPolygon polygon = null;
            if (isPolygonExist) {
                int k = 0;
                Polygon poly = new Polygon();
                int temp = x_temp_index1;
                int exit_polygon_index = s.indexOf("</polygon>", xminIndex1 + 1);
                while (true) {
                    if (i == 5) {
                        int dur = 0;
                    }
                    k++;
                    x_temp_index1 = s.indexOf("<x" + k + ">", x_temp_index1 + 1);
                    if (x_temp_index1 > exit_polygon_index) {
                        x_temp_index1 = x_index1;
                        break;
                    }

                    if (x_temp_index1 == -1) {
                        x_temp_index1 = x_index1;
                        break;
                    } else {
                        x_index1 = x_temp_index1;
                    }

                    x_index2 = s.indexOf("</x" + k + ">", x_index2 + 1);

                    int x = Math.round(Float.parseFloat(s.substring(x_index1 + ("<x" + k + ">").length(), x_index2)));

                    y_index1 = s.indexOf("<y" + k + ">", y_index1 + 1);
                    y_index2 = s.indexOf("</y" + k + ">", y_index2 + 1);
                    int y = Math.round(Float.parseFloat(s.substring(y_index1 + ("<y" + k + ">").length(), y_index2)));

                    poly.addPoint(x, y);
                    //System.out.println(p);
                }
                polygon = new PascalVocPolygon(name, poly, 0, 0, Color.yellow);
            }

            int xmin = (int) Math.round(Double.parseDouble(s.substring(s.indexOf("<xmin>", xminIndex1 + 1) + 6, s.indexOf("</xmin>", xminIndex2 + 1))));
            xminIndex1 = s.indexOf("<xmin>", xminIndex1 + 1);
            xminIndex2 = s.indexOf("</xmin>", xminIndex2 + 1);

            int ymin = (int) Math.round(Double.parseDouble(s.substring(s.indexOf("<ymin>", yminIndex1 + 1) + 6, s.indexOf("</ymin>", yminIndex2 + 1))));
            yminIndex1 = s.indexOf("<ymin>", yminIndex1 + 1);
            yminIndex2 = s.indexOf("</ymin>", yminIndex2 + 1);

            int xmax = (int) Math.round(Double.parseDouble(s.substring(s.indexOf("<xmax>", xmaxIndex1 + 1) + 6, s.indexOf("</xmax>", xmaxIndex2 + 1))));
            xmaxIndex1 = s.indexOf("<xmax>", xmaxIndex1 + 1);
            xmaxIndex2 = s.indexOf("</xmax>", xmaxIndex2 + 1);

            int ymax = (int) Math.round(Double.parseDouble(s.substring(s.indexOf("<ymax>", ymaxIndex1 + 1) + 6, s.indexOf("</ymax>", ymaxIndex2 + 1))));
            ymaxIndex1 = s.indexOf("<ymax>", ymaxIndex1 + 1);
            ymaxIndex2 = s.indexOf("</ymax>", ymaxIndex2 + 1);

            PascalVocBoundingBox bbox = new PascalVocBoundingBox(name, new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin), 0, 0, null);
            List<PascalVocAttribute> attributeList = null;
            String strAttributes = "";
            if (indexAttributes != -1) {
                attributeList = new ArrayList();
                strAttributes = s.substring(indexAttributes, s.indexOf("</attributes>", indexAttributes));
                int cntAttribute = strAttributes.split("</attribute>").length - 1;
                int indexAttrBegin = 0;
                int indexAttrEnd = 0;
                for (int j = 0; j < cntAttribute; j++) {
                    indexAttrBegin = strAttributes.indexOf("<attribute>", indexAttrBegin + 1);
                    indexAttrEnd = strAttributes.indexOf("</attribute>", indexAttrEnd + 1);
                    String sMid = strAttributes.substring(indexAttrBegin, indexAttrEnd);
                    int indexNameBegin = sMid.indexOf("<name>") + 6;
                    int indexNameEnd = sMid.indexOf("</name>");
                    String attrName = sMid.substring(indexNameBegin, indexNameEnd);
                    int indexValueBegin = sMid.indexOf("<value>") + 7;
                    int indexValueEnd = sMid.indexOf("</value>");
                    String attrValue = sMid.substring(indexValueBegin, indexValueEnd);
                    PascalVocAttribute bbA = new PascalVocAttribute(attrName, attrValue);
                    attributeList.add(bbA);
                }
            }
            PascalVocObject obj = null;
            if (isPolygonExist) {
                obj = new PascalVocObject(name, "Unspecified", 0, 0, 0, bbox, polygon, attributeList);
            } else {
                obj = new PascalVocObject(name, "Unspecified", 0, 0, 0, bbox, null, attributeList);
            }

            lstObjects.add(obj);
        }
        ret.lstObjects = lstObjects;
        return ret;
    }

    /**
     *
     * @param srcDirectory
     * @param labels
     * @return
     */
    public static String convertPascalVoc2DJLFormat(String srcDirectory, Map<String, Integer> labels) {
        String ret = "{";
        File[] xmls = FactoryUtils.getFileArrayInFolderByExtension(srcDirectory, "xml");
        for (File xml : xmls) {
            AnnotationPascalVOCFormat voc = deserializePascalVocXML(xml.getAbsolutePath());
            int n = voc.lstObjects.size();
            String s = "\"" + FactoryUtils.getFileName(xml.getName()) + ".jpg" + "\"" + ":[";
            float w = voc.size.width;
            float h = voc.size.height;
            for (int i = 0; i < n; i++) {
                PascalVocBoundingBox bbox = voc.lstObjects.get(i).bndbox;
                float lbl = labels.get(voc.lstObjects.get(i).name);
                //s=s+" [4.0, 5.0, 512.0, 512.0, "+lbl+", "+bbox.xmin/w+", "+bbox.ymin/h+", "+bbox.xmax/w+", "+bbox.ymax/h+" ],";
                s = s + "[4.0, 5.0, 512.0, 512.0, " + lbl + ", " + bbox.xmin / w + ", " + bbox.ymin / h + ", " + bbox.xmax / w + ", " + bbox.ymax / h + "], ";
            }
            s = FactoryUtils.deleteLastNCharacters(s, 2);
            s = s + " ],";
            ret = ret + s;
        }
        ret = FactoryUtils.deleteLastNCharacters(ret, 1);
        ret = ret + "}";
        return ret;
    }

    public static String convertPascalVoc2CsvFormatBndBox(String srcDirectory) {
        String ret = "filename,width,height,class,xmin,ymin,xmax,ymax\n";
        File[] xmls = FactoryUtils.getFileArrayInFolderByExtension(srcDirectory, "xml");
        for (File xml : xmls) {
            AnnotationPascalVOCFormat voc = deserializePascalVocXML(xml.getAbsolutePath());
            int n = voc.lstObjects.size();
            int w = voc.size.width;
            int h = voc.size.height;
            String s = "";
            for (int i = 0; i < n; i++) {
                PascalVocBoundingBox bbox = voc.lstObjects.get(i).bndbox;
                s += voc.fileName + "," + w + "," + h + "," + voc.lstObjects.get(i).name + "," + bbox.xmin + "," + bbox.ymin + "," + bbox.xmax + "," + bbox.ymax + "\n";
            }
            ret += s;
        }
        return ret;
    }

    /**
     *
     * @param xmlDirectory : note that sum of two ratios should be 1
     * @param trainSetRatio : should be less and equal to 1
     * @param testSetRatio : should be less and equal to 1
     * @return
     */
    public static String[] convertPascalVoc2CsvFormatBndBox(String xmlDirectory, float trainSetRatio, float testSetRatio) {
        String header = "filename,width,height,class,xmin,ymin,xmax,ymax";
        File[] xmls = FactoryUtils.getFileArrayInFolderByExtension(xmlDirectory, "xml");
        String[] ret = new String[2];
        List<String> lines = new ArrayList();
        for (File xml : xmls) {
            AnnotationPascalVOCFormat voc = deserializePascalVocXML(xml.getAbsolutePath());
            int n = voc.lstObjects.size();
            int w = voc.size.width;
            int h = voc.size.height;
            String s = "";
            for (int i = 0; i < n; i++) {
                PascalVocBoundingBox bbox = voc.lstObjects.get(i).bndbox;
                lines.add(voc.fileName + "," + w + "," + h + "," + voc.lstObjects.get(i).name + "," + bbox.xmin + "," + bbox.ymin + "," + bbox.xmax + "," + bbox.ymax);
            }
        }
        long seed = System.nanoTime();
        Collections.shuffle(lines, new Random(seed));
        if (trainSetRatio + testSetRatio != 1) {
            try {
                throw new Exception("summation of train and test set ratios must be 1");
            } catch (Exception ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        int size = lines.size();
        int nTrain = (int) (size * trainSetRatio);
        String sTrain = header + "\n";
        String sTest = header + "\n";
        int k = 0;
        for (String line : lines) {
            if (k < nTrain) {
                sTrain += line + "\n";
            } else {
                sTest += line + "\n";
            }
            k++;

        }
        ret[0] = sTrain;
        ret[1] = sTest;
        return ret;
    }

    public static String convertPascalVoc2CsvFormatBndBox(String srcDirectory, String csvFilePath) {
        String ret = convertPascalVoc2CsvFormatBndBox(srcDirectory);
        writeToFile(csvFilePath, ret);
        return ret;
    }

    /**
     * yolo format needs relative coordinate therefore this method convert real
     * boundingbox point position to image size relative position. It needs main
     * folder path of PascalVoc.xml files and class index and names
     *
     * @param mainFolderPath
     * @param refList : String array of class_index:class_name pairs
     */
    public static void convertPascalVoc2YoloFormatBndBox(String mainFolderPath, String[] refList) {
        File[] files = FactoryUtils.getFileArrayInFolderByExtension(mainFolderPath, "xml");
        int x1, x2, y1, y2, w, h, n, class_index;
        float px1, px2, py1, py2;
        //String[] refList = FactoryUtils.readFile(mainFolderPath + "/" + labels_map_file).split("\n");
        Map<String, Integer> map = new HashMap();
        for (String str : refList) {
            String s = str.split(":")[1];
            int i = Integer.parseInt(str.split(":")[0]);
            map.put(s, i);
        }
        String globalRet = "";
        int k = 0;
        for (File f : files) {
            if (f.isFile() && FactoryUtils.getFileExtension(f).equals("xml")) {
                System.out.println("k:" + k++);
                AnnotationPascalVOCFormat bbp = deserializePascalVocXML(f.getAbsolutePath());
                w = bbp.size.width;
                h = bbp.size.height;
                String ret = "";
                for (PascalVocObject pv : bbp.lstObjects) {
                    x1 = pv.bndbox.xmin;
                    y1 = pv.bndbox.ymin;
                    x2 = pv.bndbox.xmax;
                    y2 = pv.bndbox.ymax;
                    px1 = (x1 + x2) / 2.0f / w;
                    py1 = (y1 + y2) / 2.0f / h;
                    px2 = (x2 - x1) * 1.0f / w;
                    py2 = (y2 - y1) * 1.0f / h;
                    //System.out.println(pv.name);
                    if (!map.containsKey(pv.name)) {
                        continue;
                    }
                    class_index = map.get(pv.name);
                    ret += class_index + " " + px1 + " " + py1 + " " + px2 + " " + py2 + "\n";
                }
                globalRet += ret;
                //FactoryUtils.writeToFile(mainFolderPath + "/" + FactoryUtils.getFileName(bbp.fileName) + ".txt", ret);
                FactoryUtils.writeToFile(mainFolderPath + "/" + FactoryUtils.getFileName(f.getName()) + ".txt", ret);
            }
        }
        //FactoryUtils.writeToFile(mainFolderPath + "/" + "yolov7.txt", globalRet);
        System.out.println("convertPascalVoc2YoloFormatBndBox finished");
    }

    /**
     * convert all pascalvoc xml annotation files to yolo txt format based on
     * type (detection or segmentation) You only need to specify mainFolderPath and
     * subFolderName
     *
     * @param mainFolderPath
     * @param subFolderName
     * @param type : "detection" or "segmentation"
     * @return String mainFolderPath + "/" + subfolderName
     */
    public static String convertPascalVoc2YoloFormatBatch(String mainFolderPath, String subFolderName, String type) {
        String[] classIndex = getClassIndexArray(mainFolderPath + "/class_labels.txt");
        if (type.equals("detection")) {
            return convertPascalVoc2YoloFormatBatch(mainFolderPath, subFolderName, type, classIndex);
        } else if (type.equals("segmentation")) {
            return convertPascalVoc2YoloFormatBatch(mainFolderPath, subFolderName, type, classIndex);
        } else {
            return null;
        }
    }

    public static String[] getClassIndexArray(String mainFolderPath) {
        String[] s = FactoryUtils.readFile(mainFolderPath).split("\n");
        String[] ret = new String[s.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = i + ":" + s[i].split(":")[0];
        }
        return ret;
    }

    /**
     * convert all pascalvoc xml annotation files to yolo txt format based on
     * type (detection or segmentation)
     *
     * @param mainFolderPath
     * @param subfolderName
     * @param classIndex : String array of class_index:class_name pairs
     * @param detectionType
     * @return 
     */
    public static String convertPascalVoc2YoloFormatBatch(String mainFolderPath, String subfolderName, String detectionType, String[] classIndex) {
        File[] files = FactoryUtils.getFileArrayInFolderByExtension(mainFolderPath, "xml");
        //String[] refList = FactoryUtils.readFile(mainFolderPath + "/" + labels_map_file).split("\n");
        removeDirectoryRecursively(mainFolderPath + "/" + subfolderName);
        FactoryUtils.makeDirectory(mainFolderPath + "/" + subfolderName);
        FactoryUtils.makeDirectory(mainFolderPath + "/" + subfolderName + "/" + detectionType);
        FactoryUtils.makeDirectory(mainFolderPath + "/" + subfolderName + "/" + detectionType + "/images");
        FactoryUtils.makeDirectory(mainFolderPath + "/" + subfolderName + "/" + detectionType + "/labels");
        String str_yaml = "train: " + mainFolderPath + "/" + subfolderName + "/train/" + "\n"
                + "val: " + mainFolderPath + "/" + subfolderName + "/test/" + "\n"
                + "nc: " + classIndex.length + "\n"
                + "names: [";
        for (int i = 0; i < classIndex.length; i++) {
            str_yaml += "'" + classIndex[i].split(":")[1] + "',";
        }
        str_yaml = str_yaml.substring(0, str_yaml.length() - 1);
        str_yaml += "]\n";
        FactoryUtils.writeToFile(mainFolderPath + "/" + subfolderName + "/dataset.yaml", str_yaml);
        int k = 0;
        for (File f : files) {
            if (f.isFile() && FactoryUtils.getFileExtension(f).equals("xml")) {
                System.out.println("k:" + k++);
                String ret = "";
                if (detectionType.equals("detection")) {
                    ret = convertPascalVoc2YoloFormatDetectionSingle(f.getAbsolutePath(), classIndex);
                } else if (detectionType.equals("segmentation")) {
                    ret = convertPascalVoc2YoloFormatPolygonSingle(f.getAbsolutePath(), classIndex);
                }
                if (FactoryUtils.isFileExist(mainFolderPath + "/" + FactoryUtils.getFileName(f.getName()) + ".jpg")) {
                    FactoryUtils.copyFile(mainFolderPath + "/" + FactoryUtils.getFileName(f.getName()) + ".jpg", mainFolderPath + "/" + subfolderName + "/" + detectionType + "/images/" + FactoryUtils.getFileName(f.getName()) + ".jpg");
                } else if (FactoryUtils.isFileExist(mainFolderPath + "/" + FactoryUtils.getFileName(f.getName()) + ".png")) {
                    FactoryUtils.copyFile(mainFolderPath + "/" + FactoryUtils.getFileName(f.getName()) + ".png", mainFolderPath + "/" + subfolderName + "/" + detectionType + "/images/" + FactoryUtils.getFileName(f.getName()) + ".png");
                }

                FactoryUtils.writeToFile(mainFolderPath + "/" + subfolderName + "/" + detectionType + "/labels/" + FactoryUtils.getFileName(f.getName()) + ".txt", ret);
            }
        }
        System.out.println("convertPascalVoc2YoloFormatPolygon finished");
        return mainFolderPath + "\\" + subfolderName;
    }

    /**
     *
     * @param path
     * @param yoloVersion
     * @param detectionType
     * @param seed : seed of random function (if seed==-1 means random seed or
     * no seed)
     */
    public static void generateYoloSegmentationDataSet(String path, String yoloVersion, String detectionType, int seed) {
        String folderPath = FactoryUtils.convertPascalVoc2YoloFormatBatch(path, yoloVersion, detectionType);
        float trainRatio = 0.8f;
        float valRatio = 0.2f;
        File[] imgFiles = getFileArrayInFolder(folderPath + "/" + detectionType + "/images");
        File[] txtFiles = getFileArrayInFolder(folderPath + "/" + detectionType + "/labels");
        if (seed == -1) {
            FactoryUtils.shuffle(imgFiles);
            FactoryUtils.shuffle(txtFiles);
        } else {
            FactoryUtils.shuffle(imgFiles, seed);
            FactoryUtils.shuffle(txtFiles, seed);
        }
        removeDirectoryRecursively(folderPath + "/train");
        removeDirectoryRecursively(folderPath + "/test");
        makeDirectory(folderPath + "/train");
        makeDirectory(folderPath + "/train/images");
        makeDirectory(folderPath + "/train/labels");
        makeDirectory(folderPath + "/test");
        makeDirectory(folderPath + "/test/images");
        makeDirectory(folderPath + "/test/labels");
        int n = imgFiles.length;
        int nTrain = Math.round(n * trainRatio);
        int nTest = n - nTrain;
        for (int i = 0; i < nTrain; i++) {
            copyFile(imgFiles[i], new File(folderPath + "/train/images/" + imgFiles[i].getName()));
            copyFile(txtFiles[i], new File(folderPath + "/train/labels/" + txtFiles[i].getName()));
        }
        for (int i = 0; i < nTest; i++) {
            copyFile(imgFiles[i + nTrain], new File(folderPath + "/test/images/" + imgFiles[i + nTrain].getName()));
            copyFile(txtFiles[i + nTrain], new File(folderPath + "/test/labels/" + txtFiles[i + nTrain].getName()));
        }
        //zip(folderPath, path+"/ds.zip");
    }

    public static void generateYoloDetectionDataSet(String path, String yoloVersion, String detectionType, int seed) {
        String folderPath = FactoryUtils.convertPascalVoc2YoloFormatBatch(path, yoloVersion, detectionType);
        float trainRatio = 0.8f;
        float valRatio = 0.2f;
        File[] imgFiles = getFileArrayInFolder(folderPath + "/" + detectionType + "/images");
        File[] txtFiles = getFileArrayInFolder(folderPath + "/" + detectionType + "/labels");
        if (seed == -1) {
            FactoryUtils.shuffle(imgFiles);
            FactoryUtils.shuffle(txtFiles);
        } else {
            FactoryUtils.shuffle(imgFiles, seed);
            FactoryUtils.shuffle(txtFiles, seed);
        }
        removeDirectoryRecursively(folderPath + "/train");
        removeDirectoryRecursively(folderPath + "/test");
        makeDirectory(folderPath + "/train");
        makeDirectory(folderPath + "/train/images");
        makeDirectory(folderPath + "/train/labels");
        makeDirectory(folderPath + "/test");
        makeDirectory(folderPath + "/test/images");
        makeDirectory(folderPath + "/test/labels");
        int n = imgFiles.length;
        int nTrain = Math.round(n * trainRatio);
        int nTest = n - nTrain;
        for (int i = 0; i < nTrain; i++) {
            copyFile(imgFiles[i], new File(folderPath + "/train/images/" + imgFiles[i].getName()));
            copyFile(txtFiles[i], new File(folderPath + "/train/labels/" + txtFiles[i].getName()));
        }
        for (int i = 0; i < nTest; i++) {
            copyFile(imgFiles[i + nTrain], new File(folderPath + "/test/images/" + imgFiles[i + nTrain].getName()));
            copyFile(txtFiles[i + nTrain], new File(folderPath + "/test/labels/" + txtFiles[i + nTrain].getName()));
        }
        //zip(folderPath, path+"/ds.zip");
    }

    public static void getFileListRecursively(List<String> fileList, File directory) {
        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file.getAbsolutePath());
                } else {
                    getFileListRecursively(fileList, file);
                }
            }
        }
    }

    private static void zip(String dir, String zipFile) {
        File directory = new File(dir);
        File[] files = directory.listFiles();
        List<String> fileList = new ArrayList<>();

        getFileListRecursively(fileList, directory);

        try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String filePath : fileList) {
                System.out.println("Compressing: " + filePath);

                // Creates a zip entry.
                String name = filePath.substring(directory.getAbsolutePath().length() + 1);

                ZipEntry zipEntry = new ZipEntry(name);
                zos.putNextEntry(zipEntry);

                // Read file content and write to zip output stream.
                try (FileInputStream fis = new FileInputStream(filePath)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    // Close the zip entry.
                    zos.closeEntry();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateYoloSegmentationDataSet(String folderPath, String yoloVersion, String detectionType) {
        generateYoloSegmentationDataSet(folderPath, yoloVersion, detectionType, -1);
    }

    public static void generateYoloDetectionDataSet(String folderPath, String yoloVersion, String detectionType) {
        generateYoloDetectionDataSet(folderPath, yoloVersion, detectionType, -1);
    }

    public static String formatFloatDot2Comma(float p) {
        String s = "" + p;
        if (s.indexOf(".") != -1) {
            s = s.replace(".", ",");
        }
        return s;
    }

    public static int getScreenWidth() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        return width;
    }

    public static int getScreenHeight() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) screenSize.getHeight();
        return height;
    }

    public static Dimension getScreenSize() {
        DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension screenSize = new Dimension(mode.getWidth(), mode.getHeight());
        return screenSize;
    }

    public static BufferedImage captureWholeScreenWithRobot() {
        int width = getScreenWidth();
        int height = getScreenHeight();
        return robot.createScreenCapture(new Rectangle(0, 0, width, height));
    }

    public static BufferedImage captureScreenWithRobot(Robot robot, Rectangle rect) {
        if (rect.width > 0 && rect.height > 0) {
            return robot.createScreenCapture(rect);
        } else {
            return null;
        }
    }

    public static boolean isFileExist(File file) {
        return file.exists();
    }

    public static boolean isProcessRunning(String serviceName) {
        boolean ret = false;
        try {
            Process p = Runtime.getRuntime().exec("tasklist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {

                System.out.println(line);
                if (line.contains(serviceName)) {
                    return true;
                }
            }

            return false;
        } catch (IOException ex) {
            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static void killProcess(String serviceName) {
        if (isProcessRunning(serviceName)) {
            try {
                Runtime.getRuntime().exec("taskkill /F /IM " + serviceName);
            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void killProcess(long pid) {
        ProcessHandle.of(pid).ifPresent(ProcessHandle::destroyForcibly); // or ProcessHandle::destroy
//        try {
//            
//            Runtime.getRuntime().exec("taskkill /F /PID "+pid);
//        } catch (IOException ex) {
//            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public static Process openHttpServer(String OS, String PATH, int HTTP_PORT) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process ret = null;
                try {
                    if (OS.contains("WINDOWS")) {
                        ret = Runtime.getRuntime().exec("cmd /c start cmd.exe /K http-server " + PATH + " -p " + HTTP_PORT + " -c1 --cors");
                    } else if (OS.contains("LINUX")) {
                        ret = Runtime.getRuntime().exec("/bin/bash -c http-server " + PATH + "-p " + HTTP_PORT);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Deneme.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        return null;
    }

    public static BufferedImage copyImage2ClipBoard(BufferedImage img) {
        new CopyImageToClipboard(img);
        return img;
    }

    public static Point drawRotatedString(Graphics g, String text, int x, int y, double angle) {
        Point ret = new Point(x, y);
        Graphics2D g2d = (Graphics2D) g;
        g2d.rotate(angle, x, y);
        g2d.drawString(text, x, y);
        g2d.rotate(-angle, x, y);
        return ret;
    }

    public static Font getDefaultFont() {
        return new JLabel().getFont();
    }

    /**
     * generate single perlin noise based on the value
     *
     * @param value
     * @param scale
     * @return
     */
    public static float perlinNoise(float value, float scale) {
        return PerlinNoise.noise(value * scale, value * scale, 1.44f);
    }

    /**
     *
     * @param val
     * @return
     */
    public static int getDigitSensitivity(float val) {
        int ret = 0;
        if (val < 0.001) {
            ret = 6;
        } else if (val < 0.01) {
            ret = 5;
        } else if (val < 0.1) {
            ret = 4;
        } else if (val < 1) {
            ret = 3;
        } else if (val < 10) {
            ret = 2;
        } else if (val < 100) {
            ret = 1;
        } else {
            ret = 0;
        }
        return ret;
    }

    /**
     * convert gps coordinate to decimal (double)
     * <a href="https://coordinates-converter.com/en/decimal/37.965177,41.851559?karte=OpenStreetMap&zoom=16"> link </a>
     *
     * @param lat   : (Y axis, Parallel) 41°51'4.61"D --> 41:51:4.61:E
     * @param longt : (X axis, Meridian) 37°57'43.60"K --> 37:57:43.60:N
     * @return
     */
    public static Point2D.Double gpsToDecimalCoordinate(String lat, String longt) {
        Point2D.Double ret = new Point2D.Double();
        String[] x = longt.split(":");
        ret.x = Double.parseDouble(x[0]) + Double.parseDouble(x[1]) / 60.0 + Double.parseDouble(x[2]) / 3600.0;
        if (x[3].equals("S")) {
            ret.x=-ret.x;
        }
        String[] y = lat.split(":");
        ret.y = Double.parseDouble(y[0]) + Double.parseDouble(y[1]) / 60.0 + Double.parseDouble(y[2]) / 3600.0;
        if (y[3].equals("W")) {
            ret.y=-ret.y;
        }
        return ret;
    }
    
    /**
     *
     * @param from
     * @param to
     * @return
     */
    public static double getDirectionFromGPSPoints(Point2D.Double from, Point2D.Double to){
        double dLon = Math.toRadians(to.x - from.x);

        double lat1 = Math.toRadians(from.y);
        double lat2 = Math.toRadians(to.y);
        double lon1 = Math.toRadians(from.x);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));

        // Normalize the bearing to range from 0 to 360 degrees
        bearing = (bearing + 360) % 360;

        return bearing;
        
    }
    
    public static TBoundingBox getBoundingBox(int[][] maskImage) {
        return findBoundingBox(maskImage);
    }
    
    public static TBoundingBox findBoundingBox(int[][] maskImage) {
        int rows = maskImage.length;
        int cols = maskImage[0].length;

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (maskImage[i][j] == 255) { 
                    minX = Math.min(minX, j);
                    minY = Math.min(minY, i);
                    maxX = Math.max(maxX, j);
                    maxY = Math.max(maxY, i);
                }
            }
        }
        Point topLeft = new Point(minX, minY);
        Point bottomRight = new Point(maxX, maxY);
        TBoundingBox boundingBox = new TBoundingBox(topLeft, bottomRight);
        return boundingBox;
    }

}
