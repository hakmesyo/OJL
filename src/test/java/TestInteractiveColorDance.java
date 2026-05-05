import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import jazari.factory.FactoryUtils;
import jazari.matrix.CMatrix;
import jazari.gui.FrameImage;

public class TestInteractiveColorDance {

    private static int mouseX, mouseY;
    private static boolean isLeftPressed = false;
    private static boolean isRightPressed = false;
    private static float mouseVelocity = 0;
    private static int lastX, lastY;

    public static void main(String[] args) {
        int width = 800;
        int height = 600;

        // 1. Önce imshowRefresh çağırarak statik frame'i oluşturuyoruz
        CMatrix.getInstance(height, width).imshowRefresh("Interactive Perlin Dance");

        // 2. getCanvas yerine doğrudan frame'i alıyoruz
        FrameImage frame = CMatrix.frameImageRefresh;
        
        // Mouse hareketlerini yakala (Bozunum/Perturbation için)
        frame.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // Koordinatları alırken başlık çubuğu payını düşüyoruz
                mouseX = e.getX();
                mouseY = e.getY() - 30; 
                
                // Hız hesaplama
                mouseVelocity = (float) Math.sqrt(Math.pow(mouseX - lastX, 2) + Math.pow(mouseY - lastY, 2));
                lastX = mouseX;
                lastY = mouseY;
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY() - 30;
            }
        });

        // Tıklamaları yakala (Çekme/İtme için)
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) isLeftPressed = true;
                if (e.getButton() == MouseEvent.BUTTON3) isRightPressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isLeftPressed = false;
                isRightPressed = false;
            }
        });

        // 3. Animasyon Thread'i
        new Thread(() -> {
            float time = 0;
            float noiseScale = 0.01f;

            while (true) {
                // Mouse hızı sisteme enerji katar
                float perturbation = mouseVelocity * 0.015f;
                time += 0.3f + perturbation; 
                mouseVelocity *= 0.85f; // Enerji sönümlenir

                // Perlin + Sinus ile pürüzsüz köşe renkleri
                Color tl = Color.getHSBColor(getSmoothHue(time, 0, noiseScale), 0.9f, 0.9f);
                Color tr = Color.getHSBColor(getSmoothHue(time, 1000, noiseScale), 0.9f, 0.9f);
                Color bl = Color.getHSBColor(getSmoothHue(time, 2000, noiseScale), 0.9f, 0.9f);
                Color br = Color.getHSBColor(getSmoothHue(time, 3000, noiseScale), 0.9f, 0.9f);

                // İnteraktif bükülmüş (warped) görüntüyü render et
                BufferedImage img = renderInteractiveGradient(height, width, tl, tr, bl, br);
                
                // Görüntüyü ekrana bas
                CMatrix.getInstance(img).imshowRefresh("Interactive Color Dance - OJL");

                FactoryUtils.delay(15);
            }
        }).start();
    }

    private static BufferedImage renderInteractiveGradient(int rows, int cols, Color tl, Color tr, Color bl, Color br) {
        BufferedImage img = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);

        float mx = (float) mouseX / cols;
        float my = (float) mouseY / rows;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                float u = (float) j / (cols - 1);
                float v = (float) i / (rows - 1);

                // Mouse'a olan uzaklığa göre bir kuvvet alanı (Force Field) oluştur
                double dist = Math.sqrt(Math.pow(u - mx, 2) + Math.pow(v - my, 2));
                float force = (float) Math.exp(-dist * 4.5); // Etki yarıçapı

                if (isLeftPressed) { 
                    // Attraction: Koordinatları merkeze çek (Kara delik etkisi)
                    u += (mx - u) * force * 0.6f;
                    v += (my - v) * force * 0.6f;
                } else if (isRightPressed) { 
                    // Repulsion: Koordinatları merkezden it (Beyaz delik etkisi)
                    u -= (mx - u) * force * 0.6f;
                    v -= (my - v) * force * 0.6f;
                }

                // Sınırları koru
                u = Math.max(0, Math.min(1, u));
                v = Math.max(0, Math.min(1, v));

                // Bilineer interpolasyon
                int r = (int) ((1 - u) * (1 - v) * tl.getRed() + u * (1 - v) * tr.getRed() + (1 - u) * v * bl.getRed() + u * v * br.getRed());
                int g = (int) ((1 - u) * (1 - v) * tl.getGreen() + u * (1 - v) * tr.getGreen() + (1 - u) * v * bl.getGreen() + u * v * br.getGreen());
                int b = (int) ((1 - u) * (1 - v) * tl.getBlue() + u * (1 - v) * tr.getBlue() + (1 - u) * v * bl.getBlue() + u * v * br.getBlue());

                img.setRGB(j, i, (255 << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return img;
    }

    private static float getSmoothHue(float time, float offset, float scale) {
        float noise = FactoryUtils.perlinNoise(time + offset, scale);
        return (float) (Math.sin(noise * Math.PI) * 0.5 + 0.5);
    }
}