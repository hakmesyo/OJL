/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jazari.factory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import jazari.gui.FrameBasicImage;
import jazari.interfaces.call_back_interface.CallBackCamera;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author cezerilab
 */
public class FactoryWebCam {

    public Webcam webCam;
    public WebcamPanel panel;
    private static Dimension size;
    private static boolean isFlipped = true;
    public static BufferedImage currentImage;
    private CallBackCamera callback;
    public FrameBasicImage frm;
    private Thread callbackThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private static volatile FactoryWebCam instance;
    private JFrame window;

    private FactoryWebCam() {
        // Yeni bir frame oluşturmak yerine lazily initialization
    }

    public static FactoryWebCam getInstance() {
        if (instance == null) {
            synchronized (FactoryWebCam.class) {
                if (instance == null) {
                    instance = new FactoryWebCam();
                }
            }
        }
        return instance;
    }

    // Singleton instance'ı sıfırlamak için static metod
    public static void resetInstance() {
        if (instance != null) {
            instance.releaseResources();
            instance = null;
        }
    }

    /**
     * Tüm kaynakları serbest bırak
     */
    public void releaseResources() {
        // Thread'i durdur
        stopCallbackThread();

        // Webcam'i kapat
        if (webCam != null && webCam.isOpen()) {
            webCam.removeWebcamListener(null); // Tüm listener'ları kaldır
            webCam.close();
            webCam = null;
        }

        // Panel'i temizle
        if (panel != null) {
            panel.stop();
            panel = null;
        }

        // Frame'leri kapat
        if (frm != null) {
            frm.dispose();
            frm = null;
        }

        if (window != null) {
            window.dispose();
            window = null;
        }

        // Callback ve diğer referansları temizle
        callback = null;
        currentImage = null;
    }

    /**
     * Callback thread'ini güvenli bir şekilde durdur
     */
    private void stopCallbackThread() {
        if (callbackThread != null && callbackThread.isAlive()) {
            running.set(false);
            callbackThread.interrupt();
            try {
                callbackThread.join(1000); // Thread'in kapanmasını bekle (max 1 saniye)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Logger.getLogger(FactoryWebCam.class.getName()).log(Level.WARNING, "Thread interrupt edilirken hata", e);
            }
            callbackThread = null;
        }
    }

    public FactoryWebCam openWebCam() {
        webCam = Webcam.getDefault();
        if (webCam != null) {
            size = WebcamResolution.VGA.getSize();
            webCam.setViewSize(size);
            webCam.open(true);
        } else {
            Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, "Webcam bulunamadı");
        }
        return this;
    }

    public FactoryWebCam openWebCam(int cameraIndex) {
        try {
            if (Webcam.getWebcams().size() <= cameraIndex) {
                Logger.getLogger(FactoryWebCam.class.getName()).log(Level.WARNING,
                        "Belirtilen indekste kamera bulunamadı. Varsayılan kamera kullanılacak.");
                webCam = Webcam.getDefault();
            } else {
                webCam = Webcam.getWebcams().get(cameraIndex);
            }

            if (webCam != null) {
                size = WebcamResolution.VGA.getSize();
                webCam.setViewSize(size);
                webCam.open(true);
            } else {
                Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, "Webcam bulunamadı");
            }
        } catch (Exception e) {
            Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, "Kamera açılırken hata", e);
        }
        return this;
    }

    public FactoryWebCam openWebCam(Dimension size) {
        webCam = Webcam.getDefault();
        if (webCam != null) {
            webCam.setCustomViewSizes(size); // register custom resolutions
            webCam.setViewSize(size);
            webCam.open(true);
        } else {
            Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, "Webcam bulunamadı");
        }
        return this;
    }

    public FactoryWebCam openWebCam(int cameraIndex, Dimension size) {
        try {
            if (Webcam.getWebcams().size() <= cameraIndex) {
                Logger.getLogger(FactoryWebCam.class.getName()).log(Level.WARNING,
                        "Belirtilen indekste kamera bulunamadı. Varsayılan kamera kullanılacak.");
                webCam = Webcam.getDefault();
            } else {
                webCam = Webcam.getWebcams().get(cameraIndex);
            }

            if (webCam != null) {
                webCam.setCustomViewSizes(size); // register custom resolutions
                webCam.setViewSize(size);
                webCam.open(true);
            } else {
                Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, "Webcam bulunamadı");
            }
        } catch (Exception e) {
            Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, "Kamera açılırken hata", e);
        }
        return this;
    }

    public FactoryWebCam setCallback(boolean isCameraVisible, CallBackCamera callback) {
        // Önce mevcut thread'i durdur
        stopCallbackThread();

        this.callback = callback;
        running.set(true);

        // Sınırlı boyutlu kuyruk tanımla - sadece son 2 frame'i tutacak
        final ArrayBlockingQueue<BufferedImage> imageQueue = new ArrayBlockingQueue<>(2);
        final AtomicLong lastProcessTime = new AtomicLong(0);
        final long frameInterval = 10; // ~30 FPS

        callbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // WebCam listener'ı ekle
                    if (webCam != null) {
                        if (isCameraVisible) {
                            if (frm == null) {
                                frm = new FrameBasicImage();
                                frm.addWindowListener(new WindowAdapter() {
                                    @Override
                                    public void windowClosing(WindowEvent e) {
                                        stopCallbackThread();
                                    }
                                });
                            }
                            frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            frm.setVisible(true);
                        }

                        // WebCam listener ekle
                        webCam.addWebcamListener(new WebcamListener() {
                            @Override
                            public void webcamImageObtained(WebcamEvent we) {
                                // Kuyruğu yönet - dolu ise eski frame'i çıkar
                                if (imageQueue.remainingCapacity() == 0) {
                                    imageQueue.poll();
                                }
                                imageQueue.offer(we.getImage()); // Yeni görüntüyü ekle
                            }

                            @Override
                            public void webcamOpen(WebcamEvent we) {
                                System.err.println("Camera is opened");
                            }

                            @Override
                            public void webcamClosed(WebcamEvent we) {
                                System.err.println("Camera is closed");
                            }

                            @Override
                            public void webcamDisposed(WebcamEvent we) {
                                System.err.println("Camera is disposed");
                            }
                        });
                    }

                    // İşleme döngüsü - ayrı bir thread içinde çalışıyor
                    while (running.get() && !Thread.currentThread().isInterrupted()) {
                        try {
                            // Kuyruktan görüntü al, 100ms bekle yoksa devam et
                            BufferedImage img = imageQueue.poll(100, TimeUnit.MILLISECONDS);
                            if (img != null) {
                                long currentTime = System.currentTimeMillis();
                                // Frame işleme oranını kontrol et
                                if (currentTime - lastProcessTime.get() >= frameInterval) {
                                    if (callback != null) {
                                        // İşleme için callback'i çağır
                                        BufferedImage processedImage = callback.onFrame(img);
                                        currentImage = processedImage;

                                        // Görüntüyü göster
                                        if (isCameraVisible && frm != null && frm.isVisible()) {
                                            frm.setImage(currentImage);
                                        }

                                        lastProcessTime.set(currentTime);
                                    }
                                }
                            }
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                } catch (Exception e) {
                    Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, "Callback thread'inde hata", e);
                } finally {
                    System.err.println("Callback thread stopped");
                }
            }
        });

        callbackThread.setDaemon(true);
        callbackThread.start();

        return this;
    }

//    public FactoryWebCam setCallback(boolean isCameraVisible, CallBackCamera callback) {
//        // Önce mevcut thread'i durdur
//        stopCallbackThread();
//        
//        this.callback = callback;
//        running.set(true);
//        
//        callbackThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    // Webcam listener'ı ekle
//                    if (webCam != null) {
//                        if (isCameraVisible) {
//                            if (frm == null) {
//                                frm = new FrameBasicImage();
//                                // Frame kapanınca kaynakları temizle
//                                frm.addWindowListener(new WindowAdapter() {
//                                    @Override
//                                    public void windowClosing(WindowEvent e) {
//                                        stopCallbackThread();
//                                    }
//                                });
//                            }
//                            frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//                            frm.setVisible(true);
//                        }
//                        
//                        // Listener eklemeden önce mevcut listener'ları temizle
//                        for (WebcamListener listener : webCam.getWebcamListeners()) {
//                            webCam.removeWebcamListener(listener);
//                        }
//                        
//                        webCam.addWebcamListener(new WebcamListener() {
//                            @Override
//                            public void webcamImageObtained(WebcamEvent we) {
//                                // Her yeni görüntü alındığında callback'i çağır
//                                if (callback != null && running.get() && webCam != null && webCam.isOpen()) {
//                                    BufferedImage processedImage = callback.onFrame(we.getImage());
//                                    currentImage = processedImage;
//                                    if (isCameraVisible && frm != null && frm.isVisible()) {
//                                        frm.setImage(currentImage);
//                                    }
//                                }
//                            }
//
//                            @Override
//                            public void webcamOpen(WebcamEvent we) {
//                                System.err.println("Camera is opened");
//                            }
//
//                            @Override
//                            public void webcamClosed(WebcamEvent we) {
//                                System.err.println("Camera is closed");
//                                running.set(false);
//                            }
//
//                            @Override
//                            public void webcamDisposed(WebcamEvent we) {
//                                System.err.println("Camera is disposed");
//                                running.set(false);
//                            }
//                        });
//                    }
//                    
//                    // Kamera görüntülenmiyorsa, thread'i sürekli çalışır durumda tut
//                    // ama düzgün bir şekilde kapatılabilir olmasını sağla
//                    while (running.get() && !Thread.currentThread().isInterrupted()) {
//                        if (!isCameraVisible) {
//                            try {
//                                Thread.sleep(100); // Daha kısa sleep süresi, daha hızlı kapatılabilir
//                            } catch (InterruptedException ex) {
//                                Thread.currentThread().interrupt();
//                                break;
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    Logger.getLogger(FactoryWebCam.class.getName()).log(Level.SEVERE, "Callback thread'inde hata", e);
//                } finally {
//                    System.err.println("Callback thread stopped");
//                }
//            }
//        });
//        
//        callbackThread.setDaemon(true); // Daemon thread olarak işaretle, JVM kapanırken beklemez
//        callbackThread.start();
//        
//        return this;
//    }
    public FactoryWebCam showWebCAM() {
        if (webCam == null || !webCam.isOpen()) {
            Logger.getLogger(FactoryWebCam.class.getName()).log(Level.WARNING,
                    "WebCam açık değil, önce openWebCam metodunu çağırın.");
            return this;
        }

        // Eğer zaten bir panel varsa, önce temizle
        if (panel != null) {
            panel.stop();
        }

        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
        panel.setFPSDisplayed(true);

        // Eğer zaten bir pencere varsa, önce kapat
        if (window != null) {
            window.dispose();
        }

        window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (panel != null) {
                    panel.stop();
                }
            }
        });
        window.pack();
        window.setVisible(true);
        System.err.println("showwebcam is triggered");

        return this;
    }

    public FactoryWebCam showWebCAM(Dimension dim) {
        if (webCam == null || !webCam.isOpen()) {
            Logger.getLogger(FactoryWebCam.class.getName()).log(Level.WARNING,
                    "WebCam açık değil, önce openWebCam metodunu çağırın.");
            return this;
        }

        // Eğer zaten bir panel varsa, önce temizle
        if (panel != null) {
            panel.stop();
        }

        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
        panel.setFPSDisplayed(true);

        // Eğer zaten bir pencere varsa, önce kapat
        if (window != null) {
            window.dispose();
        }

        window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (panel != null) {
                    panel.stop();
                }
            }
        });
        window.setPreferredSize(dim);
        window.pack();
        window.setVisible(true);

        return this;
    }

    public FactoryWebCam showWebCAM(int fps) {
        if (webCam == null || !webCam.isOpen()) {
            Logger.getLogger(FactoryWebCam.class.getName()).log(Level.WARNING,
                    "WebCam açık değil, önce openWebCam metodunu çağırın.");
            return this;
        }

        // Eğer zaten bir panel varsa, önce temizle
        if (panel != null) {
            panel.stop();
        }

        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
        panel.setFPSLimited(true);
        panel.setFPSLimit(fps);
        panel.setFPSDisplayed(true);

        // Eğer zaten bir pencere varsa, önce kapat
        if (window != null) {
            window.dispose();
        }

        window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (panel != null) {
                    panel.stop();
                }
            }
        });
        window.pack();
        window.setVisible(true);

        return this;
    }

    public FactoryWebCam showWebCAM(Dimension resize, int fps) {
        if (webCam == null || !webCam.isOpen()) {
            Logger.getLogger(FactoryWebCam.class.getName()).log(Level.WARNING,
                    "WebCam açık değil, önce openWebCam metodunu çağırın.");
            return this;
        }

        // Eğer zaten bir panel varsa, önce temizle
        if (panel != null) {
            panel.stop();
        }

        panel = new WebcamPanel(webCam);
        panel.setImageSizeDisplayed(true);
        panel.setFPSDisplayed(true);
        panel.setFPSLimited(true);
        panel.setFPSLimit(fps);

        // Eğer zaten bir pencere varsa, önce kapat
        if (window != null) {
            window.dispose();
        }

        window = new JFrame("Webcam");
        window.add(panel);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (panel != null) {
                    panel.stop();
                }
            }
        });
        window.setPreferredSize(resize);
        window.pack();
        window.setVisible(true);

        return this;
    }

    public BufferedImage getImage() {
        return webCam != null ? webCam.getImage() : null;
    }

    public FactoryWebCam flipImageAlongVerticalAxis() {
        isFlipped = !isFlipped;
        return this;
    }

    /**
     * WebCam'i kapatır ve tüm kaynakları serbest bırakır
     */
    public void close() {
        releaseResources();
    }

    /**
     * JVM kapanmadan önce çağrılması gereken metod. static blok ile de
     * çağrılabilir.
     */
    public static void shutdownAllWebcams() {
        resetInstance();
        Webcam.getWebcams().forEach(webcam -> {
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
            }
        });
    }

    // JVM kapanmadan önce tüm kaynakları temizle
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdownAllWebcams();
            }
        });
    }
}
