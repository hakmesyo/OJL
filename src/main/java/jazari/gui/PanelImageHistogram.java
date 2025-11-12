package jazari.gui;

import jazari.matrix.CMatrix;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape; // YENİ: Gerekli import
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

/**
 * Matplotlib'den ilham alan, modern, sağlam ve İNTERAKTİF bir histogram çizim paneli.
 * Bu panel, zoom ve pan özellikleriyle ve KIRPMA (clipping) desteğiyle çalışır.
 *
 * @author BAP1 (Orijinal Yazar), Gemini (Modernizasyon ve İnteraktivite)
 */
public class PanelImageHistogram extends JPanel {

    private CMatrix matrix;

    // --- STİL SABİTLERİ ---
    private static final Color COLOR_BACKGROUND = Color.WHITE;
    private static final Color COLOR_AXIS = Color.BLACK;
    private static final Color COLOR_GRID = new Color(220, 220, 220);
    private static final Color COLOR_BAR_FILL = new Color(70, 130, 180);
    private static final Color COLOR_BAR_BORDER = new Color(30, 80, 130);
    private static final Color COLOR_TEXT = Color.BLACK;
    private static final Color COLOR_NO_DATA = Color.GRAY;
    private static final Font FONT_AXIS_LABELS = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_NO_DATA = new Font("SansSerif", Font.BOLD, 16);
    private static final Font FONT_INFO = new Font("SansSerif", Font.PLAIN, 12);

    // --- PADDING ---
    private static final int PADDING_TOP = 20;
    private static final int PADDING_BOTTOM = 40;
    private static final int PADDING_LEFT = 50;
    private static final int PADDING_RIGHT = 20;

    // --- ETKİLEŞİM DEĞİŞKENLERİ ---
    private float scale = 1.0f;
    private float translateX = 0;
    private Point lastPoint;

    public PanelImageHistogram(CMatrix cm) {
        this.matrix = cm;
        setBackground(COLOR_BACKGROUND);
        setupInteractions();
    }

    private void setupInteractions() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    resetView();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                float oldScale = scale;
                scale *= Math.pow(1.05, -e.getWheelRotation());
                scale = Math.max(0.1f, Math.min(100.0f, scale));

                translateX = e.getX() - (e.getX() - translateX) * scale / oldScale;
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    translateX += e.getX() - lastPoint.x;
                    lastPoint = e.getPoint();
                    repaint();
                }
            }
        };

        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    public void resetView() {
        scale = 1.0f;
        translateX = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

        if (matrix == null || matrix.toFloatArray1D() == null || matrix.toFloatArray1D().length == 0) {
            drawNoDataMessage(g2d);
            return;
        }
        float[] data = matrix.toFloatArray1D();

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int plotWidth = panelWidth - PADDING_LEFT - PADDING_RIGHT;
        int plotHeight = panelHeight - PADDING_TOP - PADDING_BOTTOM;

        int startBin = Math.max(0, (int) screenToWorldX(-translateX, plotWidth, data.length));
        int endBin = Math.min(data.length - 1, (int) screenToWorldX(plotWidth - translateX, plotWidth, data.length));

        float visibleYMax = 0;
        for (int i = startBin; i <= endBin; i++) {
            if (data[i] > visibleYMax) {
                visibleYMax = data[i];
            }
        }
        visibleYMax *= 1.1f;
        if (visibleYMax == 0) visibleYMax = 1;

        // --- YENİ: KIRPMA (CLIPPING) İŞLEMİ ---
        // 1. Mevcut kırpma alanını sakla (iyi bir pratiktir)
        Shape oldClip = g2d.getClip();

        // 2. Kırpma alanını sadece çizim bölgesi olarak ayarla
        g2d.setClip(PADDING_LEFT, PADDING_TOP, plotWidth, plotHeight);

        // 3. Sadece bu bölge içinde çizilecek olan metotları çağır
        drawGrid(g2d, plotWidth, plotHeight, visibleYMax);
        drawBars(g2d, data, plotWidth, plotHeight, visibleYMax, startBin, endBin);

        // 4. Kırpma alanını eski haline getir ki eksenler ve etiketler çizilebilsin
        g2d.setClip(oldClip);
        // --- KIRPMA İŞLEMİ SONU ---

        // Bu metotlar artık kırpma alanı dışındadır ve normal şekilde çizilirler
        drawAxes(g2d, plotWidth, plotHeight);
        drawAxisLabels(g2d, plotWidth, plotHeight, data.length, visibleYMax, startBin, endBin);
        drawInfoText(g2d);
    }

    private void drawBars(Graphics2D g2d, float[] data, int plotWidth, int plotHeight, float yMax, int startBin, int endBin) {
        float barSlotWidth = (float) plotWidth / data.length * scale;
        float barActualWidth = Math.max(1.0f, barSlotWidth * 0.8f);

        for (int i = startBin; i <= endBin; i++) {
            float value = data[i];
            int barHeight = (int) ((value / yMax) * plotHeight);
            int barX = PADDING_LEFT + (int) (i * barSlotWidth + translateX + (barSlotWidth - barActualWidth) / 2);
            int barY = PADDING_TOP + plotHeight - barHeight;

            g2d.setColor(COLOR_BAR_FILL);
            g2d.fillRect(barX, barY, (int) barActualWidth, barHeight);
            g2d.setColor(COLOR_BAR_BORDER);
            g2d.drawRect(barX, barY, (int) barActualWidth, barHeight);
        }
    }
    
    private void drawAxisLabels(Graphics2D g2d, int plotWidth, int plotHeight, int numBins, float yMax, int startBin, int endBin) {
        g2d.setColor(COLOR_TEXT);
        g2d.setFont(FONT_AXIS_LABELS);
        FontMetrics fm = g2d.getFontMetrics();

        int numYLabels = 5;
        for (int i = 0; i <= numYLabels; i++) {
            float value = (yMax / numYLabels) * i;
            String label = String.format("%,.0f", value);
            int x = PADDING_LEFT - fm.stringWidth(label) - 5;
            int y = PADDING_TOP + plotHeight - (int) ((value / yMax) * plotHeight) + (fm.getAscent() / 2);
            g2d.drawString(label, x, y);
        }

        if (numBins == 0) return;
        float barSlotWidth = (float) plotWidth / numBins * scale;
        int step = 1;
        if (barSlotWidth < 20) {
            step = (int) Math.ceil(20 / barSlotWidth);
        }

        for (int i = startBin; i <= endBin; i++) {
            if (i % step == 0) {
                String label = String.valueOf(i);
                int x = PADDING_LEFT + (int) (i * barSlotWidth + translateX + barSlotWidth / 2) - (fm.stringWidth(label) / 2);
                int y = PADDING_TOP + plotHeight + 15;
                if (x > PADDING_LEFT - 20 && x < PADDING_LEFT + plotWidth + 20) {
                    g2d.drawString(label, x, y);
                }
            }
        }
    }

    private float screenToWorldX(float screenX, int plotWidth, int numBins) {
        return (screenX / scale) * numBins / plotWidth;
    }

    private void drawInfoText(Graphics2D g2d) {
        g2d.setFont(FONT_INFO);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString(String.format("Zoom: %.2fx", scale), 10, 15);
    }

    public CMatrix getMatrix() { return matrix; }
    public void setMatrix(CMatrix cm) { this.matrix = cm; repaint(); }
    private void drawGrid(Graphics2D g2d, int plotWidth, int plotHeight, float yMax) {
        g2d.setColor(COLOR_GRID);
        g2d.setStroke(new BasicStroke(1.0f));
        int numGridLines = 5;
        for (int i = 1; i <= numGridLines; i++) {
            float value = (yMax / numGridLines) * i;
            int y = PADDING_TOP + plotHeight - (int) ((value / yMax) * plotHeight);
            g2d.drawLine(PADDING_LEFT, y, PADDING_LEFT + plotWidth, y);
        }
    }
    private void drawAxes(Graphics2D g2d, int plotWidth, int plotHeight) {
        g2d.setColor(COLOR_AXIS);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(PADDING_LEFT, PADDING_TOP, PADDING_LEFT, PADDING_TOP + plotHeight);
        g2d.drawLine(PADDING_LEFT, PADDING_TOP + plotHeight, PADDING_LEFT + plotWidth, PADDING_TOP + plotHeight);
    }
    private void drawNoDataMessage(Graphics2D g2d) {
        g2d.setColor(COLOR_NO_DATA);
        g2d.setFont(FONT_NO_DATA);
        String msg = "Görüntülenecek Veri Yok";
        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D bounds = fm.getStringBounds(msg, g2d);
        int x = (getWidth() - (int) bounds.getWidth()) / 2;
        int y = (getHeight() - (int) bounds.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(msg, x, y);
    }
}