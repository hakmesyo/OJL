package jazari.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

public class StuckPixelFix {

    public static void main(String[] args) {
        process();
    }

    public static void process() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Stuck Pixel Fixer (Random) - Kapatmak için ESC tuşuna basın");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setUndecorated(true);
            frame.setResizable(false);

            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();
            device.setFullScreenWindow(frame);

            PixelPanel pixelPanel = new PixelPanel();
            frame.add(pixelPanel);

            frame.setVisible(true);

            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        device.setFullScreenWindow(null);
                        frame.dispose();
                    }
                }
            });

            frame.setFocusable(true);
            frame.requestFocusInWindow();
        });
    }
}

class PixelPanel extends JPanel {

    private final Random random = new Random();
    private BufferedImage pixelImage;
    private final Timer timer;

    // --- YENİ EKLENEN BÖLÜM BAŞLANGICI ---
    // Bilgilendirme metni için değişkenler
    private final String infoText = "Press ESC to exit";
    private int textX, textY;
    private final Font infoFont = new Font("SansSerif", Font.BOLD, 36);
    private int positionChangeCounter = 0;
    // Her 125 karede bir pozisyonu değiştir (125 * 40ms = 5000ms = 5 saniye)
    private static final int POSITION_CHANGE_INTERVAL = 25;
    // --- YENİ EKLENEN BÖLÜM SONU ---

    public PixelPanel() {
        timer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updatePixelData();
                updateTextPositionIfNeeded(); // Metin pozisyonunu kontrol et ve gerekirse güncelle
                repaint();
            }
        });
        timer.start();
    }

    private void updatePixelData() {
        int width = getWidth();
        int height = getHeight();

        if (pixelImage == null || pixelImage.getWidth() != width || pixelImage.getHeight() != height) {
            if (width > 0 && height > 0) {
                pixelImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                calculateNewTextPosition(); // Metnin ilk pozisyonunu hesapla
            } else {
                return;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = random.nextInt(0xFFFFFF + 1);
                pixelImage.setRGB(x, y, rgb);
            }
        }
    }

    // --- YENİ EKLENEN METOTLAR BAŞLANGICI ---
    /**
     * Sayaç belirlenen aralığa ulaştığında metnin pozisyonunu günceller.
     */
    private void updateTextPositionIfNeeded() {
        positionChangeCounter++;
        if (positionChangeCounter >= POSITION_CHANGE_INTERVAL) {
            positionChangeCounter = 0;
            calculateNewTextPosition();
        }
    }

    /**
     * Metnin ekrandan taşmayacak şekilde yeni bir rastgele pozisyonunu
     * hesaplar.
     */
    private void calculateNewTextPosition() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        FontMetrics fm = this.getFontMetrics(infoFont);
        int stringWidth = fm.stringWidth(infoText);
        int stringHeight = fm.getAscent();

        int maxX = getWidth() - stringWidth;
        int maxY = getHeight() - stringHeight;

        if (maxX > 0) {
            textX = random.nextInt(maxX);
        } else {
            textX = 0;
        }

        if (maxY > 0) {
            textY = random.nextInt(maxY) + stringHeight;
        } else {
            textY = stringHeight;
        }
    }
    // --- YENİ EKLENEN METOTLAR SONU ---

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (pixelImage != null) {
            // 1. Rastgele piksellerden oluşan arka planı çiz
            g.drawImage(pixelImage, 0, 0, this);

            // 2. Bilgilendirme metnini ve gölgesini çiz
            g.setFont(infoFont);
            // Okunabilirliği artırmak için metne siyah bir gölge ekle
            g.setColor(Color.BLACK);
            g.drawString(infoText, textX + 2, textY + 2);
            // Metnin kendisini beyaz renkte çiz
            g.setColor(Color.WHITE);
            g.drawString(infoText, textX, textY);
        }
    }
}
