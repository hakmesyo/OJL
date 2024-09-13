package jazari.factory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactoryCamera {

    private Webcam webcam;
    private static final Logger log = LoggerFactory.getLogger(FactoryCamera.class);
    private JFrame frame;
    private JLabel label;
    private Timer timer;
    private long lastTime = 0;
    private int frameCount = 0;
    private double fps = 0;

    static {
        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "ERROR");
        Webcam.setDriver(new WebcamDefaultDriver());
        Webcam.getDiscoveryService().setEnabled(false);
        java.util.logging.Logger.getLogger("com.github.sarxos.webcam").setLevel(java.util.logging.Level.OFF);
    }

    public void start(int cameraIndex, int requestedWidth, int requestedHeight) {
        List<Webcam> webcams = Webcam.getWebcams();
        if (cameraIndex < 0 || cameraIndex >= webcams.size()) {
            throw new IllegalArgumentException("Invalid camera index");
        }

        webcam = webcams.get(cameraIndex);
        Dimension[] nonStandardResolutions = new Dimension[]{
            WebcamResolution.UHD4K.getSize(),
            new Dimension(requestedWidth, requestedHeight),
            WebcamResolution.FHD.getSize(),
            WebcamResolution.HD.getSize(),
            WebcamResolution.VGA.getSize(),};

        webcam.setCustomViewSizes(nonStandardResolutions);
        Dimension bestSize = chooseBestResolution(webcam.getViewSizes(), new Dimension(requestedWidth, requestedHeight));
        webcam.setViewSize(bestSize);
        

        if (!webcam.open()) {
            throw new RuntimeException("Failed to open camera");
        }

        log.info("Camera started. Resolution: {}x{}", webcam.getViewSize().width, webcam.getViewSize().height);
    }

    private Dimension chooseBestResolution(Dimension[] availableSizes, Dimension requestedSize) {
        return Arrays.stream(availableSizes)
                .min((d1, d2) -> {
                    long diff1 = Math.abs((long) d1.width * d1.height - (long) requestedSize.width * requestedSize.height);
                    long diff2 = Math.abs((long) d2.width * d2.height - (long) requestedSize.width * requestedSize.height);
                    return Long.compare(diff1, diff2);
                })
                .orElse(availableSizes[0]);
    }

    public BufferedImage captureImage() {
        if (webcam == null || !webcam.isOpen()) {
            throw new IllegalStateException("Camera is not initialized or open");
        }
        return webcam.getImage();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
        if (frame != null) {
            frame.dispose();
        }
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            log.info("Camera closed.");
        }
    }

    public JFrame openLiveCamera(int targetFps) {
        
        if (webcam == null || !webcam.isOpen()) {
            throw new IllegalStateException("Camera is not initialized");
        }

        if (targetFps<=0) {
            targetFps=30;
        }
        frame = new JFrame("Live Camera Feed");
        frame.setLocation(1000, 0);
        label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (g instanceof Graphics2D) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 18));
                g.drawString(String.format("FPS: %.2f", fps), 10, 30);
            }
        };
        frame.getContentPane().add(label);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(webcam.getViewSize());
        frame.setVisible(true);

        int delay = 1000 / targetFps;
        timer = new Timer(delay, e -> {
            long now = System.currentTimeMillis();
            frameCount++;
            if (now - lastTime >= 1000) {
                fps = frameCount / ((now - lastTime) / 1000.0);
                frameCount = 0;
                lastTime = now;
            }

            BufferedImage image = captureImage();
            if (image != null) {
                label.setIcon(new ImageIcon(image));
                label.repaint(); // Force repaint to update FPS overlay
            }
        });
        timer.start();

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                closeCamera();
            }
        });
        return frame;
    }

    private void closeCamera() {
        if (timer != null) {
            timer.stop();
        }
        if (frame != null) {
            frame.dispose();
        }
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        log.info("Camera closed and resources released.");
        System.exit(0); // Uygulamayı tamamen sonlandır
    }

    // Example usage
    public static void main(String[] args) {
        FactoryCamera camera = new FactoryCamera();

        try {
            camera.start(0, 640, 480);

            BufferedImage img = camera.captureImage();
            log.info("Image captured: {}x{}", img.getWidth(), img.getHeight());

            //webcam.openLiveCamera(30); // Open live view at 10 FPS

            // Keep main thread alive
            //Thread.sleep(30000); // Run for 30 seconds
        } catch (Exception e) {
            log.error("An error occurred", e);
        } finally {
            camera.stop();
        }
    }
}
