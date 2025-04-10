/**
 * TODO: ImageProcess ve diğer core classlardaki tüm işlemler parametre olarak
 * float[][] almalı.
 *
 * ****************************************************************************
 * OpenCV komutlarını çağıran harici uygulamalarda
 * System.loadLibrary(Core.NATIVE_LIBRARY_NAME); eklenmeli ve 64 bit jar add jar
 * ile eklendikten sonra dll dosyası da kök dizinde bulunmalı
 * ****************************************************************************
 */
package jazari.image_processing;

import jazari.factory.FactoryNormalization;
import jazari.factory.FactoryStatistic;
import jazari.factory.FactorySimilarityDistance;
import jazari.factory.FactoryUtils;
import jazari.types.TRoi;
import jazari.types.TGrayPixel;
import jazari.types.TWord;
import jazari.machine_learning.extraction.FeatureExtractionLBP;
import jazari.matrix.CMatrix;
import jazari.matrix.CPoint;
import jazari.matrix.CRectangle;
import jazari.factory.FactoryMatrix;
import com.jhlabs.composite.ColorDodgeComposite;
import com.jhlabs.image.AverageFilter;
import com.jhlabs.image.ContrastFilter;
import com.jhlabs.image.GaussianFilter;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.MotionBlurOp;
import com.jhlabs.image.PointFilter;
import com.luciad.imageio.webp.WebPImageWriterSpi;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import static jazari.factory.FactoryUtils.getDefaultDirectory;
import org.opencv.core.Core;

/**
 *
 * @author venap3
 */
public final class ImageProcess {

    private static boolean isOpenCVLoaded;

//    static {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }
    /**
     * Conversion from RGB space to LAB space (CIE)
     *
     * @param R
     * @param G
     * @param B
     * @return
     */
    public static float[] rgbToLab(int R, int G, int B) {

        float r, g, b, X, Y, Z, xr, yr, zr;

        // D65/2°
        float Xr = 95.047f;
        float Yr = 100.0f;
        float Zr = 108.883f;

        // --------- RGB to XYZ ---------//
        r = R / 255.0f;
        g = G / 255.0f;
        b = B / 255.0f;

        if (r > 0.04045) {
            r = (float) Math.pow((r + 0.055) / 1.055, 2.4);
        } else {
            r = r / 12.92f;
        }

        if (g > 0.04045) {
            g = (float) Math.pow((g + 0.055) / 1.055, 2.4);
        } else {
            g = g / 12.92f;
        }

        if (b > 0.04045) {
            b = (float) Math.pow((b + 0.055) / 1.055, 2.4);
        } else {
            b = b / 12.92f;
        }

        r *= 100;
        g *= 100;
        b *= 100;

        X = 0.4124f * r + 0.3576f * g + 0.1805f * b;
        Y = 0.2126f * r + 0.7152f * g + 0.0722f * b;
        Z = 0.0193f * r + 0.1192f * g + 0.9505f * b;

        // --------- XYZ to Lab --------- //
        xr = X / Xr;
        yr = Y / Yr;
        zr = Z / Zr;

        if (xr > 0.008856) {
            xr = (float) Math.pow(xr, 1 / 3.);
        } else {
            xr = (float) ((7.787 * xr) + 16 / 116.0);
        }

        if (yr > 0.008856) {
            yr = (float) Math.pow(yr, 1 / 3.);
        } else {
            yr = (float) ((7.787 * yr) + 16 / 116.0);
        }

        if (zr > 0.008856) {
            zr = (float) Math.pow(zr, 1 / 3.);
        } else {
            zr = (float) ((7.787 * zr) + 16 / 116.0);
        }

        float[] lab = new float[3];

        lab[0] = (116 * yr) - 16;
        lab[1] = 500 * (xr - yr);
        lab[2] = 200 * (yr - zr);
        return lab;

    }

    /**
     * Canny Edge Detector
     *
     * @param d : inputImage
     * @return BufferedImage
     */
    public static float[][] edgeDetectionCanny(float[][] d) {
        float lowThreshold = 0.3f;
        float highTreshold = 1.0f;
        float gaussianKernelRadious = 2.5f;
        int guassianKernelWidth = 3;
        boolean isContrastNormalized = false;
        BufferedImage currBufferedImage = ImageProcess.pixelsToImageGray(d);
        currBufferedImage = edgeDetectionCanny(currBufferedImage, lowThreshold, highTreshold, gaussianKernelRadious, guassianKernelWidth, isContrastNormalized);
        currBufferedImage = toGrayLevel(currBufferedImage);
        return imageToPixelsFloat(currBufferedImage);
    }

    public static BufferedImage edgeDetectionCannyAsImage(float[][] d) {
        float lowThreshold = 0.3f;
        float highTreshold = 1.0f;
        float gaussianKernelRadious = 2.5f;
        int guassianKernelWidth = 3;
        boolean isContrastNormalized = false;
        BufferedImage currBufferedImage = ImageProcess.pixelsToImageGray(d);
        currBufferedImage = edgeDetectionCanny(currBufferedImage, lowThreshold, highTreshold, gaussianKernelRadious, guassianKernelWidth, isContrastNormalized);
        currBufferedImage = toGrayLevel(currBufferedImage);
        return currBufferedImage;
    }

    public static float[][] edgeDetectionCanny(BufferedImage img) {
        float lowThreshold = 0.3f;
        float highTreshold = 1.0f;
        float gaussianKernelRadious = 2.5f;
        int guassianKernelWidth = 3;
        boolean isContrastNormalized = false;
        BufferedImage currBufferedImage = edgeDetectionCanny(img, lowThreshold, highTreshold, gaussianKernelRadious, guassianKernelWidth, isContrastNormalized);
        return imageToPixelsFloat(currBufferedImage);
    }

    /**
     * Canny Edge Detection - BufferedImage alıp BufferedImage döndüren versiyon
     *
     * @param img : Kaynak görüntü
     * @return BufferedImage olarak kenar tespit sonucu
     */
    public static BufferedImage edgeDetectionCannyAsImage(BufferedImage img) {
        float lowThreshold = 0.3f;
        float highTreshold = 1.0f;
        float gaussianKernelRadious = 2.5f;
        int guassianKernelWidth = 3;
        boolean isContrastNormalized = false;

        BufferedImage currBufferedImage = edgeDetectionCanny(img, lowThreshold, highTreshold,
                gaussianKernelRadious, guassianKernelWidth, isContrastNormalized);
        currBufferedImage = toGrayLevel(currBufferedImage);
        return currBufferedImage;
    }

    /**
     * Canny Edge Detection - Parametreleri ayarlanabilir versiyon
     *
     * @param img : Kaynak görüntü
     * @param lowThreshold : Düşük eşik değeri (0.0f-1.0f)
     * @param highThreshold : Yüksek eşik değeri (0.0f-1.0f)
     * @param gaussianRadius : Gaussian yumuşatma yarıçapı
     * @param gaussianWidth : Gaussian kernel genişliği (3, 5, 7 vb. tek sayı
     * olmalı)
     * @param normalizeContrast : Kontrast normalizasyonu yapılıp yapılmayacağı
     * @return BufferedImage olarak kenar tespit sonucu
     */
    public static BufferedImage edgeDetectionCannyAsImage(BufferedImage img,
            float lowThreshold,
            float highThreshold,
            float gaussianRadius,
            int gaussianWidth,
            boolean normalizeContrast) {

        BufferedImage currBufferedImage = edgeDetectionCanny(img, lowThreshold, highThreshold,
                gaussianRadius, gaussianWidth, normalizeContrast);
        currBufferedImage = toGrayLevel(currBufferedImage);
        return currBufferedImage;
    }

    /*
    public static Mat ocv_edgeDetectionCanny(Mat imageGray) {
        Mat imageCanny = new Mat();
        Imgproc.Canny(imageGray, imageCanny, 10, 100, 3, true);
        return imageCanny;
    }

    public static BufferedImage ocv_edgeDetectionCanny(BufferedImage img) {
        Mat imageGray = ocv_img2Mat(img);
        Mat imageCanny = new Mat();
        Imgproc.Canny(imageGray, imageCanny, 10, 100, 3, true);
        return ocv_mat2Img(imageCanny);
    }

    public static BufferedImage ocv_equalizeHistogram(BufferedImage img) {
        img = ocv_rgb2gray(img);
        Mat src = ocv_img2Mat(img);
        List<Mat> images = new ArrayList();
        images.add(src);
        MatOfInt channels = new MatOfInt(0);
        Mat mask = new Mat();
        Mat hist = new Mat();
        MatOfInt mHistSize = new MatOfInt(256);
        MatOfFloat mRanges = new MatOfFloat(0f, 256f);
        Imgproc.calcHist(images, channels, mask, hist, mHistSize, mRanges);
        return ocv_mat2Img(hist);
    }

    public static BufferedImage ocv_hist(BufferedImage img) {
        img = ocv_rgb2gray(img);
        Mat src = ocv_img2Mat(img);
        Mat dst = new Mat();
        Imgproc.equalizeHist(src, dst);
        return ocv_mat2Img(dst);
    }

    public static Mat ocv_blendImagesMat(Mat img1, Mat img2) {
        Mat dst = new Mat();
        Core.addWeighted(img1, 0.5, img2, 0.5, 0.0, dst);
        return dst;
    }

    public static Mat ocv_blendImagesMat(BufferedImage img1, BufferedImage img2) {
        Mat src1 = ImageProcess.ocv_img2Mat(img1);
        Mat src2 = ImageProcess.ocv_img2Mat(img2);
        Mat dst = new Mat();
        Core.addWeighted(src1, 0.5, src2, 0.5, 0.0, dst);
        return dst;
    }

    public static BufferedImage ocv_blendImagesBuffered(BufferedImage img1, BufferedImage img2) {
        Mat src1 = ImageProcess.ocv_img2Mat(img1);
        Mat src2 = ImageProcess.ocv_img2Mat(img2);
        Mat dst = new Mat();
        Core.addWeighted(src1, 0.5, src2, 0.5, 0.0, dst);
        BufferedImage bf_3 = ImageProcess.ocv_mat2Img(dst);
        return bf_3;
    }

    public static BufferedImage ocv_edgeDetectionCanny(float[][] d) {
        BufferedImage img = pixelsToImageGray(d);
        Mat imageGray = ocv_img2Mat(img);
        Mat imageCanny = new Mat();
//        Imgproc.Canny(imageGray, imageCanny, 10, 100, 3, true);
        Imgproc.Canny(imageGray, imageCanny, 10, 150, 3, true);
        return ocv_mat2Img(imageCanny);
    }

    public static float[][] ocv_edgeDetectionCanny2D(BufferedImage img) {
        Mat imageGray = ocv_img2Mat(img);
        Mat imageCanny = new Mat();
//        Imgproc.Canny(imageGray, imageCanny, 50, 200, 3, true);
//        Imgproc.Canny(imageGray, imageCanny, 20, 150, 3, true);
//        Imgproc.Canny(imageGray, imageCanny, 20, 100, 3, true);
        Imgproc.Canny(imageGray, imageCanny, 50, 100, 3, true);
        float[][] ret = imageToPixelsFloat(ocv_mat2Img(imageCanny));
        return ret;
    }
     */
    /**
     * Musa Edge Detector
     *
     * @param d: inputImage
     * @param thr: threshold float value
     * @return
     */
    public static float[][] edgeDetectionMusa(float[][] d, int thr) {
//        float thr=30;
        float[][] a1 = FactoryUtils.shiftOnRow(d, 1);
        float[][] a2 = FactoryUtils.shiftOnRow(d, -1);
        float[][] a3 = FactoryUtils.shiftOnColumn(d, 1);
        float[][] a4 = FactoryUtils.shiftOnColumn(d, -1);

        float[][] ret1 = FactoryUtils.subtractWithThreshold(d, a1, thr);
        float[][] ret2 = FactoryUtils.subtractWithThreshold(d, a2, thr);
        float[][] ret3 = FactoryUtils.subtractWithThreshold(d, a3, thr);
        float[][] ret4 = FactoryUtils.subtractWithThreshold(d, a4, thr);

        float[][] retx = FactoryUtils.add(ret1, ret2);
        float[][] rety = FactoryUtils.add(ret3, ret4);
        float[][] ret = FactoryUtils.add(retx, rety);
//        float[][] ret=FactoryUtils.add(ret1,ret3);
        return ret;
    }

    /**
     * Canny Edge Detector
     *
     * @param d : inputImage
     * @param lowThreshold: default value is 0.3f
     * @param highTreshold:default value is 1.0f
     * @param gaussianKernelRadious:default value is 2.5f
     * @param guassianKernelWidth:default value is 3
     * @param isContrastNormalized : default false
     * @return BufferedImage
     */
    public static int[][] edgeDetectionCanny(
            float[][] d,
            float lowThreshold,
            float highTreshold,
            float gaussianKernelRadious,
            int guassianKernelWidth,
            boolean isContrastNormalized) {
        BufferedImage currBufferedImage = ImageProcess.pixelsToImageGray(d);
        currBufferedImage = edgeDetectionCanny(currBufferedImage, lowThreshold, highTreshold, gaussianKernelRadious, guassianKernelWidth, isContrastNormalized);
        return imageToPixelsInt(currBufferedImage);
    }

    /**
     * Canny Edge Detector
     *
     * @param img : inputImage
     * @param lowThreshold: default value is 0.3f
     * @param highTreshold:default value is 1.0f
     * @param gaussianKernelRadious:default value is 2.5f
     * @param guassianKernelWidth:default value is 3
     * @param isContrastNormalized : default false
     * @return BufferedImage
     */
    public static BufferedImage edgeDetectionCanny(
            BufferedImage img,
            float lowThreshold,
            float highTreshold,
            float gaussianKernelRadious,
            int guassianKernelWidth,
            boolean isContrastNormalized) {
        BufferedImage currBufferedImage = img;
        CannyEdgeDetector detector = new CannyEdgeDetector();

        detector.setLowThreshold(lowThreshold);
        detector.setHighThreshold(highTreshold);
        detector.setGaussianKernelRadius(gaussianKernelRadious);
        detector.setGaussianKernelWidth(guassianKernelWidth);
        detector.setContrastNormalized(isContrastNormalized);

        detector.setSourceImage(currBufferedImage);
        detector.process();
        currBufferedImage = detector.getEdgesImage();
        return ImageProcess.rgb2gray(currBufferedImage);
    }

    public static BufferedImage isolateChannel(BufferedImage image, String channel) {

        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        int iAlpha = 0;
        int iRed = 0;
        int iGreen = 0;
        int iBlue = 0;
        int newPixel = 0;

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                int rgb = image.getRGB(i, j);
                newPixel = 0;

                iAlpha = rgb >> 24 & 0xff;
                iRed = rgb >> 16 & 0xff;
                iGreen = rgb >> 8 & 0xff;
                iBlue = rgb & 0xff;
                if (channel.equals("red")) {
//                    Tevafuk için eklenmişti
//                    if (iRed > 110 && iRed < 220 && iGreen > 20 && iGreen < 110 && iBlue > 20 && iBlue < 110) {
//                        newPixel = 0 | 170 << 16;
//                    } else {
//                        newPixel = 0;
//                    }
                    newPixel = newPixel | iRed << 16;
                }

                if (channel.equals("green")) {
                    newPixel = newPixel | iGreen << 8;
                }

                if (channel.equals("blue")) {
                    newPixel = newPixel | iBlue;
                }

                result.setRGB(i, j, newPixel);
            }
        }

        return result;
    }

    public static BufferedImage showRedPixels(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage redImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Color color;
        int t = 140;
        //int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        //yaz(pixels);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                //int pp = img.getRGB(x, y);

//                printPixelARGB(pp);
                Color cl = new Color(img.getRGB(x, y));
                int r = cl.getRed();
                int g = cl.getGreen();
                int b = cl.getBlue();
//                float[] hsv = new float[3];
//                hsv = cl.RGBtoHSB(r, g, b, hsv);
//                int q = 15;
//                if (hsv[0] * 360 > 345 || hsv[0] * 360 < 15) {
//                    //yaz("red:" + (hsv[0] * 360));
//                }else{
//                    yaz("bulmadi");
//                }
//

                yaz("rgb:" + r + "," + g + "," + b);
//                if (red == green && red == blue) {
//                    color = new Color(0, 0, 0);
//                } else if (red > t && green < t && blue < t) {
//                    color = new Color(255, 0, 0);
//                } else {
//                    color = new Color(0, 0, 0);
//                }
//
//                redImage.setRGB(x, y, color.getRGB());
            }
        }
        return redImage;
    }

    public static void printPixelARGB(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        System.out.println("argb: " + alpha + ", " + red + ", " + green + ", " + blue);
    }

    public static BufferedImage changeToRedMonochrome(BufferedImage grayImage) {

        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        BufferedImage redImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color grayColor = new Color(grayImage.getRGB(x, y));
                int gray = grayColor.getRed();

                int red = (gray > 127) ? 255 : gray / 2;
                int blue = (gray > 127) ? gray / 2 : 0;
                int green = (gray > 127) ? gray / 2 : 0;

                Color redColor = new Color(red, blue, green);
                redImage.setRGB(x, y, redColor.getRGB());
            }
        }
        return redImage;
    }

    public static BufferedImage DCT(BufferedImage bimg) {
        final int N = 8;  // Block size
        int nrows, ncols, m, n, x, y, u, v, img[][], in[][];
        float dct[][], sum, au, av;
        float n1 = (float) Math.sqrt(1.0 / N), n2 = (float) Math.sqrt(2.0 / N);
        img = imageToPixelsInt(bimg);
        nrows = img.length;
        ncols = img[0].length;
        if (nrows % N != 0 || ncols % N != 0) {
            System.out.println("Nrows and ncols should be 8's power");
            return bimg;
//            System.exit(0);
        }
        in = new int[nrows][ncols];
        dct = new float[nrows][ncols];
        // For each NxN block[m,n]
        for (m = 0; m < nrows; m += N) {
            for (n = 0; n < ncols; n += N) {
                // For each pixel[u,v] in block[m,n]
                for (u = m; u < m + N; u++) {
                    au = (u == m) ? n1 : n2;
                    for (v = n; v < n + N; v++) {
                        av = (v == n) ? n1 : n2;

                        // Sum up all pixels in the block
                        for (x = m, sum = 0; x < m + N; x++) {
                            for (y = n; y < n + N; y++) {
                                in[x][y] = img[x][y] - 128;  // Subtract by 128
                                sum += in[x][y] * Math.cos((2 * (x - m) + 1) * (u - m) * Math.PI / (2 * N))
                                        * Math.cos((2 * (y - n) + 1) * (v - n) * Math.PI / (2 * N));
                            }
                        }
                        dct[u][v] = au * av * sum;
                    } // for v
                } // for u
            }  // for n
        }  // for m
        return ImageProcess.pixelsToImageGray(FactoryUtils.toIntArray2D(dct));
    }

    public static BufferedImage cropImage(BufferedImage src, CRectangle rect) {
        return cropImage(src, rect.column, rect.row, rect.width, rect.height);
    }

    public static BufferedImage cropImage(BufferedImage src, Rectangle rect) {
        return cropImage(src, rect.x, rect.y, rect.width, rect.height);
    }

    public static BufferedImage cropImage(BufferedImage src, int px, int py, int width, int height) {
        if (src.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            return src.getSubimage(px, py, width, height);
        } else if (src.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            return src.getSubimage(px, py, width, height);
        } else {
            return toBGR(src.getSubimage(px, py, width, height));
        }
    }

    public static BufferedImage convertImageToPencilSketch(BufferedImage src) {
        PointFilter grayScaleFilter = new GrayscaleFilter();
        BufferedImage grayScale = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        grayScaleFilter.filter(src, grayScale);

//inverted gray scale
        BufferedImage inverted = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        PointFilter invertFilter = new InvertFilter();
        invertFilter.filter(grayScale, inverted);

//gaussian blurr
        GaussianFilter gaussianFilter = new GaussianFilter(20);
        BufferedImage gaussianFiltered = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        gaussianFilter.filter(inverted, gaussianFiltered);

//color dodge
        ColorDodgeComposite cdc = new ColorDodgeComposite(1.0f);
        CompositeContext cc = cdc.createContext(inverted.getColorModel(), grayScale.getColorModel(), null);
        Raster invertedR = gaussianFiltered.getRaster();
        Raster grayScaleR = grayScale.getRaster();
        BufferedImage composite = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        WritableRaster colorDodgedR = composite.getRaster();
        cc.compose(invertedR, grayScaleR, colorDodgedR);

//==================================
        return composite;
    }

    public static boolean lookNeighbourPixels(int[][] img, Point p, int r) {
        ArrayList<Point> lst = getWindowEdgePixelPositions(img, p, r);
        for (Point pt : lst) {
            if (img[pt.x][pt.y] != 127 && img[pt.x][pt.y] != 0) {
                p.x = pt.x;
                p.y = pt.y;
                //yaz("konumlandı: p.x:"+p.x+" p.y:"+p.y);
                return true;
            }
        }
        return false;
    }

    public static ArrayList<Point> getWindowEdgePixelPositions(int[][] img, Point p, int r) {
        Point m = new Point();
        m.x = p.x - r;
        m.y = p.y - r;
        Point pt = null;
        ArrayList<Point> lst = new ArrayList<Point>();
        if (m.x <= 0 || m.y <= 0 || m.x + 2 * r >= img.length || m.y + 2 * r >= img[0].length) {
            return lst;
        }
        for (int i = 0; i < 2 * r + 1; i++) {
            for (int j = 0; j < 2 * r + 1; j++) {
                pt = new Point(m.x + j, m.y + i);
                lst.add(pt);
            }
        }
        lst = FactoryUtils.shuffleList(lst);
        return lst;
    }

    /**
     * return Alpha, Red, Green and Blue values of original RGB image
     *
     * @param image
     * @return
     */
    public static int[][][] imageToPixelsColorInt(BufferedImage image) {
        int numRows = image.getHeight();
        int numCols = image.getWidth();
        // Now we make our array.
        int[][][] pixels = new int[numRows][numCols][4];
//        int[] outputChannels=new int[4];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
//                image.getRaster().getPixel(col,row,outputChannels);
//                pixels[row][col]=outputChannels;
                Color c = new Color(image.getRGB(col, row));
                pixels[row][col][0] = c.getAlpha();  // Alpha
                pixels[row][col][1] = c.getRed();  // Red
                pixels[row][col][2] = c.getGreen();  // Green
                pixels[row][col][3] = c.getBlue();        // Blue
            }
        }
        return pixels;
    }

    /**
     * return Alpha, Red, Green and Blue values of original RGB image
     *
     * @param image
     * @return
     */
    public static float[][][] imageToPixelsColorDouble(BufferedImage image) {
        int numRows = image.getHeight();
        int numCols = image.getWidth();
        // Now we make our array.
        float[][][] pixels = new float[numRows][numCols][4];
//        float[] outputChannels=new float[4];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
//                image.getRaster().getPixel(col,row,outputChannels);
//                pixels[row][col]=outputChannels;
                Color c = new Color(image.getRGB(col, row));
                pixels[row][col][0] = c.getAlpha();  // Alpha
                pixels[row][col][1] = c.getRed();  // Red
                pixels[row][col][2] = c.getGreen();  // Green
                pixels[row][col][3] = c.getBlue();        // Blue
            }
        }
        return pixels;
    }

    /**
     * return ARGB format Alpha, Red, Green and Blue values of original RGB
     * image
     *
     * @param image
     * @return
     */
    public static int[][][] imageToPixelsColorIntFaster(BufferedImage image) {
        // Java's peculiar way of extracting pixels is to give them
        // back as a one-dimensional array from which we will construct
        // our version.

        int numRows = image.getHeight();
        int numCols = image.getWidth();
        int[] oneDPixels = new int[numRows * numCols];

        // This will place the pixels in oneDPixels[]. Each int in
        // oneDPixels has 4 bytes containing the 4 pieces we need.
        PixelGrabber grabber = new PixelGrabber(image, 0, 0, numCols, numRows,
                oneDPixels, 0, numCols);
        try {
            grabber.grabPixels(0);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        // Now we make our array.
        int[][][] pixels = new int[4][numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            // First extract a row of int's from the right place.
            int[] aRow = new int[numCols];
            for (int col = 0; col < numCols; col++) {
                int element = row * numCols + col;
                aRow[col] = oneDPixels[element];
            }

            // In Java, the most significant byte is the alpha value,
            // followed by R, then G, then B. Thus, to extract the alpha
            // value, we shift by 24 and make sure we extract only that byte.
            for (int col = 0; col < numCols; col++) {
                pixels[0][row][col] = (aRow[col] >> 24) & 0xFF;  // Alpha
                pixels[1][row][col] = (aRow[col] >> 16) & 0xFF;  // Red
                pixels[2][row][col] = (aRow[col] >> 8) & 0xFF;  // Green
                pixels[3][row][col] = (aRow[col]) & 0xFF;        // Blue
            }
        }

        return pixels;
    }

    /**
     * return ARGB format Alpha, Red, Green and Blue values of original RGB
     * image
     *
     * @param image
     * @return
     */
    public static float[][][] imageToPixelsColorFloatFaster(BufferedImage image) {
        // Java's peculiar way of extracting pixels is to give them
        // back as a one-dimensional array from which we will construct
        // our version.

        int numRows = image.getHeight();
        int numCols = image.getWidth();
        int[] oneDPixels = new int[numRows * numCols];

        // This will place the pixels in oneDPixels[]. Each int in
        // oneDPixels has 4 bytes containing the 4 pieces we need.
        PixelGrabber grabber = new PixelGrabber(image, 0, 0, numCols, numRows,
                oneDPixels, 0, numCols);
        try {
            grabber.grabPixels(0);
        } catch (InterruptedException e) {
            System.out.println(e);
        }

        // Now we make our array.
        float[][][] pixels = new float[4][numRows][numCols];
        for (int row = 0; row < numRows; row++) {
            // First extract a row of int's from the right place.
            int[] aRow = new int[numCols];
            for (int col = 0; col < numCols; col++) {
                int element = row * numCols + col;
                aRow[col] = oneDPixels[element];
            }

            // In Java, the most significant byte is the alpha value,
            // followed by R, then G, then B. Thus, to extract the alpha
            // value, we shift by 24 and make sure we extract only that byte.
            for (int col = 0; col < numCols; col++) {
                pixels[0][row][col] = (aRow[col] >> 24) & 0xFF;  // Alpha
                pixels[1][row][col] = (aRow[col] >> 16) & 0xFF;  // Red
                pixels[2][row][col] = (aRow[col] >> 8) & 0xFF;  // Green
                pixels[3][row][col] = (aRow[col]) & 0xFF;        // Blue
            }
        }
        return pixels;
    }

    public static BufferedImage pixelsToImageColor(int[][][] pixels) {
        int numRows = pixels.length;
        int numCols = pixels[0].length;
        int[] oneDPixels = new int[numRows * numCols];

        int index = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                oneDPixels[index] = ((pixels[row][col][0] << 24) & 0xFF000000)
                        | ((pixels[row][col][1] << 16) & 0x00FF0000)
                        | ((pixels[row][col][2] << 8) & 0x0000FF00)
                        | ((pixels[row][col][3]) & 0x000000FF);
                index++;
            }
        }

        // The MemoryImageSource class is an ImageProducer that can
        // build an image out of 1D pixels. Then, rather confusingly,
        // the createImage() method, inherited from Component, is used
        // to make the actual Image instance. This is simply Java's
        // confusing, roundabout way. An alternative is to use the
        // Raster models provided in BufferedImage.
        MemoryImageSource imSource = new MemoryImageSource(numCols, numRows, oneDPixels, 0, numCols);
        Image imG = Toolkit.getDefaultToolkit().createImage(imSource);
        BufferedImage I = imageToBufferedImage(imG);
        return I;

    }

    public static BufferedImage pixelsToImageColor(float[][][] pixels) {
        int numRows = pixels.length;
        int numCols = pixels[0].length;
        int[] oneDPixels = new int[numRows * numCols];

        int index = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                oneDPixels[index] = (((int) pixels[row][col][0] << 24) & 0xFF000000)
                        | (((int) pixels[row][col][1] << 16) & 0x00FF0000)
                        | (((int) pixels[row][col][2] << 8) & 0x0000FF00)
                        | (((int) pixels[row][col][3]) & 0x000000FF);
                index++;
            }
        }

        // The MemoryImageSource class is an ImageProducer that can
        // build an image out of 1D pixels. Then, rather confusingly,
        // the createImage() method, inherited from Component, is used
        // to make the actual Image instance. This is simply Java's
        // confusing, roundabout way. An alternative is to use the
        // Raster models provided in BufferedImage.
        MemoryImageSource imSource = new MemoryImageSource(numCols, numRows, oneDPixels, 0, numCols);
        Image imG = Toolkit.getDefaultToolkit().createImage(imSource);
        BufferedImage I = imageToBufferedImage(imG);
        return I;

    }

    public static BufferedImage pixelsToImageColor(float[][] pixels) {
        int numRows = pixels.length;
        int numCols = pixels[0].length;
        int[] oneDPixels = new int[numRows * numCols];

        int index = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                oneDPixels[index] = (int) pixels[row][col];
                index++;
            }
        }

        // The MemoryImageSource class is an ImageProducer that can
        // build an image out of 1D pixels. Then, rather confusingly,
        // the createImage() method, inherited from Component, is used
        // to make the actual Image instance. This is simply Java's
        // confusing, roundabout way. An alternative is to use the
        // Raster models provided in BufferedImage.
        MemoryImageSource imSource = new MemoryImageSource(numCols, numRows, oneDPixels, 0, numCols);
        Image imG = Toolkit.getDefaultToolkit().createImage(imSource);
        BufferedImage I = imageToBufferedImage(imG);
        return I;

    }

    public static BufferedImage pixelsToImageColorArgbFormat(float[][][] pixels) {
        int numRows = pixels[0].length;
        int numCols = pixels[0][0].length;
        int[] oneDPixels = new int[numRows * numCols];

        int index = 0;
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                oneDPixels[index] = (((int) pixels[0][row][col] << 24) & 0xFF000000)
                        | (((int) pixels[1][row][col] << 16) & 0x00FF0000)
                        | (((int) pixels[2][row][col] << 8) & 0x0000FF00)
                        | (((int) pixels[3][row][col]) & 0x000000FF);
                index++;
            }
        }

        // The MemoryImageSource class is an ImageProducer that can
        // build an image out of 1D pixels. Then, rather confusingly,
        // the createImage() method, inherited from Component, is used
        // to make the actual Image instance. This is simply Java's
        // confusing, roundabout way. An alternative is to use the
        // Raster models provided in BufferedImage.
        MemoryImageSource imSource = new MemoryImageSource(numCols, numRows, oneDPixels, 0, numCols);
        Image imG = Toolkit.getDefaultToolkit().createImage(imSource);
        BufferedImage I = imageToBufferedImage(imG);
        return I;

    }

    public static byte[] getBytes(BufferedImage img) {
        return ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
    }

    public static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

    public static float[][] convertBufferedImageTo2D(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        float[][] result = new float[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

    public static BufferedImage imread() {
        return readImage();
    }

    public static BufferedImage imread(String path) {
        return readImage(path);
    }

    public static BufferedImage imread(File file) {
        return readImage(file);
    }

    public static BufferedImage readImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("images"));
        chooser.setDialogTitle("select Data Set file");
        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
        File file;
        BufferedImage ret = null;
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            return readImage(file.getAbsolutePath());
        }
        return ret;
    }

    public static BufferedImage readImage(File file) {
        return readImage(file.getAbsolutePath());
    }

//    public static BufferedImage readImage(String fileName) {
//        try {
//            return ImageIO.read(new File(fileName));
    ////        try {
////            File file = new File(fileName);
////
////            // Dosyanın gerçek MIME tipini kontrol et
////            Tika tika = new Tika();
////            String mimeType = tika.detect(file);
////
////            if (mimeType.equals("image/webp")) {
////                // WebP için TwelveMonkeys okuyucusunu kullan
////                IIORegistry registry = IIORegistry.getDefaultInstance();
////                registry.registerServiceProvider(new WebPImageReaderSpi());
////
////                try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
////                    Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
////                    if (readers.hasNext()) {
////                        ImageReader reader = readers.next();
////                        reader.setInput(input);
////                        return reader.read(0);
////                    }
////                }
////                throw new IOException("No suitable reader found for WebP image");
////            } else if (mimeType.equals("application/dicom")) {
////                return IJ.openImage(fileName).getBufferedImage();
////            } else {
////                // Diğer formatlar için standart ImageIO kullan
////                return ImageIO.read(file);
////            }
////        } catch (IOException ex) {
////            Logger.getLogger(ImageProcess.class.getName()).log(Level.SEVERE, null, ex);
////            return null;
////        }
//        } catch (IOException ex) {
//            Logger.getLogger(ImageProcess.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
//    public static BufferedImage readImage(String fileName) {
//        try {
//            File file = new File(fileName);
//
//            // Dosyanın gerçek MIME tipini kontrol et
//            Tika tika = new Tika();
//            String mimeType = tika.detect(file);
//
//            if (mimeType.equals("image/webp")) {
//                // WebP için TwelveMonkeys okuyucusunu kullan
//                IIORegistry registry = IIORegistry.getDefaultInstance();
//                registry.registerServiceProvider(new WebPImageReaderSpi());
//
//                try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
//                    Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
//                    if (readers.hasNext()) {
//                        ImageReader reader = readers.next();
//                        reader.setInput(input);
//                        return reader.read(0);
//                    }
//                }
//                throw new IOException("No suitable reader found for WebP image");
//            } else if (mimeType.equals("application/dicom")) {
//                return IJ.openImage(fileName).getBufferedImage();
//            } else {
//                // Diğer formatlar için standart ImageIO kullan
//                
//                
//                return ImageIO.read(file);
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(ImageProcess.class.getName()).log(Level.SEVERE, null, ex);
//            return null;
//        }
//    }
    public static BufferedImage readImage(String fileName) {
        try {
            File file = new File(fileName);
            return ImageIO.read(file);
        } catch (IOException ex) {
            Logger.getLogger(ImageProcess.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static BufferedImage readImage2(String fileName) {
        try {
            if (fileName.contains("http")) {
                URL url = new URL(fileName);
                URLConnection conn = url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla");
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);

                conn.connect();
                InputStream urlStream = conn.getInputStream();
                BufferedImage bimg = ImageIO.read(urlStream);
                return ImageIO.read(new URL(fileName));
            } else if (FactoryUtils.getFileExtension(new File(fileName).getName()).equals("dcm")) {
                return IJ.openImage(fileName).getBufferedImage();
            } else if (FactoryUtils.getFileExtension(new File(fileName).getName()).equals("tif")) {
                return javax.imageio.ImageIO.read(new File(fileName));
            } else {
                BufferedImage ret = ImageIO.read(new File(fileName));
                if (ret == null) {
                    return javax.imageio.ImageIO.read(new File(fileName));
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(ImageProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static float[][] bufferedImageToArray2D(BufferedImage img) {
        float[][] ret = null;
        if (img.getColorModel().getNumComponents() == 4) {
            img = convertToBufferedImageTypes(img, 5);
            ret = convertBufferedImageTo2D(img);
        } else if (img.getColorModel().getNumComponents() == 1 && !img.getColorModel().hasAlpha()) {
            float[] pixelValues = new float[img.getWidth() * img.getHeight()];
            int idx = 0;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int grayValue = img.getRaster().getSample(x, y, 0);
                    pixelValues[idx++] = grayValue;
                }
            }
            ret = FactoryMatrix.reshapeBasedOnRows(pixelValues, img.getHeight(), img.getWidth());
            return ret;
        } else if (img.getColorModel().getNumComponents() == 3 && !img.getColorModel().hasAlpha()) {
            img = convertToBufferedImageTypes(img, 5);
            ret = convertBufferedImageTo2D(img);
            return ret;
        }
        return ret;
    }

    public static float[][][] bufferedImageToArray3D(BufferedImage img) {
        float[][][] ret = null;
        if (img.getColorModel().getNumComponents() == 1 && !img.getColorModel().hasAlpha()) {
            throw new ArithmeticException("BufferedImage has not rgba channels");
        } else if (img.getColorModel().getNumComponents() == 3 && !img.getColorModel().hasAlpha()) {
            WritableRaster raster = img.getRaster();
            DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
            byte[] d = data.getData();
            float[] q = FactoryUtils.byte2Float(d);
            float[] r = new float[q.length / 3];
            float[] g = new float[q.length / 3];
            float[] b = new float[q.length / 3];
            int size = r.length;
            for (int i = 0; i < size; i++) {
                r[i] = q[3 * i];
                g[i] = q[3 * i + 1];
                b[i] = q[3 * i + 2];
            }
            float[][] rr = FactoryMatrix.reshape(r, img.getHeight(), img.getWidth());
            float[][] gg = FactoryMatrix.reshape(g, img.getHeight(), img.getWidth());
            float[][] bb = FactoryMatrix.reshape(b, img.getHeight(), img.getWidth());
            ret = new float[3][][];
            ret[0] = rr;
            ret[1] = gg;
            ret[2] = bb;
            return ret;
        }
        return ret;
    }

    public static int[][] imageToPixelsInt(BufferedImage img) {
        float[][] d = imageToPixelsFloat(img);
        int[][] original = FactoryUtils.toIntArray2D(d);
        return original;
    }

    public static float[][] to2DFloat(BufferedImage img) {
        return imageToPixelsFloat(img);
    }

    public static float[][] imageToPixelsFloat(BufferedImage img) {
//        if (img == null) {
//            return null;
//        }
//        float[][] original = new float[img.getHeight()][img.getWidth()]; // where we'll put the image
//        if ((img.getType() == BufferedImage.TYPE_CUSTOM)
//                || (img.getType() == BufferedImage.TYPE_INT_RGB)
//                || (img.getType() == BufferedImage.TYPE_INT_ARGB)
//                || (img.getType() == BufferedImage.TYPE_3BYTE_BGR)
//                || (img.getType() == BufferedImage.TYPE_4BYTE_ABGR)) {
//            for (int i = 0; i < img.getHeight(); i++) {
//                for (int j = 0; j < img.getWidth(); j++) {
//                    original[i][j] = img.getRGB(j, i);
//                }
//            }
//        } else {
//            Raster image_raster = img.getData();
//            //get pixel by pixel
//            int[] pixel = new int[1];
//            int[] buffer = new int[1];
//
//            // declaring the size of arrays
//            original = new float[img.getHeight()][img.getWidth()];
//
//            //get the image in the array
//            for (int i = 0; i < img.getHeight(); i++) {
//                for (int j = 0; j < img.getWidth(); j++) {
//                    pixel = image_raster.getPixel(j, i, buffer);
//                    original[i][j] = pixel[0];
//                }
//            }
//        }
//        return original;
        return bufferedImageToArray2D(img);
    }

//    public static float[][] imageToPixelsFloat(BufferedImage img) {
//        return bufferedImageToArray2D(img);
//    }
    public static int[][] imageToPixels255_CIZ(BufferedImage img) {
        return imageToPixelsInt(img);
//        MediaTracker mt = new MediaTracker(null);
//        mt.addImage(img, 0);
//        try {
//            mt.waitForID(0);
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        int w = img.getWidth();
//        int h = img.getHeight();
//        //System.out.println("w:"+w+" h:"+h);
//        int pixels[] = new int[w * h];
//        int fpixels[] = new int[w * h];
//        int dpixel[][] = new int[w][h];
//        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
//        try {
//            pg.grabPixels();
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//        int red = (pixels[0] >> 16 & 0xff);
//        int green = pixels[0] >> 8 & 0xff;
//        int blue = pixels[0] & 0xff;
//
//        if (red == green && red == blue) {
//            for (int i = 0; i < pixels.length; i++) {
//                fpixels[i] = pixels[i] & 0xff;
//            }
//        } else {
//            for (int i = 0; i < pixels.length; i++) {
//                int r = pixels[i] >> 16 & 0xff;
//                int g = pixels[i] >> 8 & 0xff;
//                int b = pixels[i] & 0xff;
//                int y = (int) (0.33000000000000002D * (float) r + 0.56000000000000005D * (float) g + 0.11D * (float) b);
//                fpixels[i] = y;

    

    ////            fpixels[i] = pixels[i];
//            }
//        }
//        int k = 0;
//        for (int i = 0; i < h; i++) {
//            for (int j = 0; j < w; j++) {
//                dpixel[j][i] = fpixels[k];
//                k++;
//            }
//        }
//
//        return dpixel;
    }

    public static int[] imageToPixelsTo1D(BufferedImage img) {
        return FactoryUtils.toIntArray1D(imageToPixelsInt(img));
    }

    public static int[][] imageToPixelsROI(BufferedImage img, Rectangle roi) {

        MediaTracker mt = new MediaTracker(null);
        mt.addImage(img, 0);
        try {
            mt.waitForID(0);
        } catch (Exception e) {
            // TODO: handle exception
        }

        int w = img.getWidth();
        int h = img.getHeight();
        //System.out.println("w:"+w+" h:"+h);
        int pixels[] = new int[w * h];
        int fpixels[] = new int[w * h];
        int dpixel[][] = new int[roi.width][roi.height];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (Exception e) {
            // TODO: handle exception
        }
        int cnt = 0;
        int py = 0;
        for (int i = roi.y * w + roi.x; i < (roi.y + roi.height) * w + roi.x; i++) {
            if (cnt < roi.width) {
                int r = pixels[i] >> 16 & 0xff;
                int g = pixels[i] >> 8 & 0xff;
                int b = pixels[i] >> 0 & 0xff;
                int y = (int) (0.33000000000000002D * (float) r + 0.56000000000000005D * (float) g + 0.11D * (float) b);
                dpixel[cnt][py] = y;
                cnt++;
            } else {
                cnt = 0;
                i = i + w - roi.width;
                py++;
            }
        }
        return dpixel;
    }

    public static BufferedImage imageToBufferedImage(Image im) {
        BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics bg = bi.getGraphics();
        bg.drawImage(im, 0, 0, null);
        bg.dispose();
        return bi;
    }

    public static BufferedImage pixelsToImageGray(int dizi[][]) {
        int[] pixels = FactoryUtils.toIntArray1D(dizi);
        int h = dizi.length;
        int w = dizi[0].length;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();
        raster.setPixels(0, 0, w, h, pixels);
        return image;
    }

    public static BufferedImage pixelsToImageGray(float dizi[][]) {
        int[] pixels = FactoryUtils.toIntArray1D(dizi);
        int h = dizi.length;
        int w = dizi[0].length;
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster raster = image.getRaster();
        raster.setPixels(0, 0, w, h, pixels);
        return image;
    }

    public static BufferedImage pixelsToBufferedImage255_CIZ(int dizi[][]) {
        int w = dizi.length;
        int h = dizi[0].length;
        int ai[] = new int[w * h];
        int aix[] = new int[w * h];
        int k = 0;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                ai[k] = dizi[j][i];
                aix[k] = (0xff000000 | ai[k] << 16 | ai[k] << 8 | ai[k]);
                k++;
            }
        }
        Image imG = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h, aix, 0, w));
        BufferedImage myImage = imageToBufferedImage(imG);
        return myImage;
    }

//    public static BufferedImage toGrayLevel(BufferedImage img) {
//        return rgb2gray(img);
//    }
    public static BufferedImage rgb2hsv(BufferedImage img) {
        int[][][] ret = convertHSV(img);
        BufferedImage rimg = pixelsToImageColor(ret);
        return rimg;
    }

    public static BufferedImage hsv2rgb(BufferedImage img) {
        int[][][] d = imageToPixelsColorInt(img);
        int nr = d.length;
        int nc = d[0].length;
        int ch = d[0][0].length;
        int[][][] ret = new int[nr][nc][ch];

        int red, green, blue;
        float hue, saturation, brightness;

        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                hue = d[i][j][1] / 255.0f;
                saturation = d[i][j][2] / 255.0f;
                brightness = d[i][j][3] / 255.0f;
                int rgb = Color.HSBtoRGB(hue, saturation, brightness);
                red = (rgb >> 16) & 0xFF;
                green = (rgb >> 8) & 0xFF;
                blue = rgb & 0xFF;
                ret[i][j][0] = 255;//alpha channel
                ret[i][j][1] = red;
                ret[i][j][2] = green;
                ret[i][j][3] = blue;
            }
        }

        BufferedImage rimg = pixelsToImageColor(ret);
        return rimg;
    }

    public static BufferedImage toHSVColorSpace(BufferedImage img) {
        return rgb2hsv(img);
    }

    public static BufferedImage getHueChannel(BufferedImage img) {
        int[][][] ret = convertHSV(img);
        int[][] d = new int[ret.length][ret[0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[0].length; j++) {
                d[i][j] = ret[i][j][1];
            }
        }
        BufferedImage bf = ImageProcess.pixelsToImageGray(d);
        return bf;
    }

    public static BufferedImage getSaturationChannel(BufferedImage img) {
        int[][][] ret = convertHSV(img);
        int[][] d = new int[ret.length][ret[0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[0].length; j++) {
                d[i][j] = ret[i][j][2];
            }
        }
        BufferedImage bf = ImageProcess.pixelsToImageGray(d);
        return bf;
    }

    public static BufferedImage getValueChannel(BufferedImage img) {
        int[][][] ret = convertHSV(img);
        int[][] d = new int[ret.length][ret[0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[0].length; j++) {
                d[i][j] = ret[i][j][3];
            }
        }
        BufferedImage bf = ImageProcess.pixelsToImageGray(d);
        return bf;
    }

    public static int[][][] convertHSV(BufferedImage img) {
        int[][][] d = imageToPixelsColorInt(img);
        int[][][] ret = new int[d.length][d[0].length][d[0][0].length];
        int n = 255;
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                int[] val = FactoryMatrix.clone(d[i][j]);
                float[] q = toHSV(val[1], val[2], val[3]);
                val[1] = (int) (q[0] * n);
                val[2] = (int) (q[1] * n);
                val[3] = (int) (q[2] * n);
                ret[i][j] = val;
            }
        }
        return ret;
    }

    public static float[] toHSV(int red, int green, int blue) {
        float[] hsv = new float[3];
        hsv = Color.RGBtoHSB(red, green, blue, null);
        return hsv;
    }

    public static float[] rgb2hsv(int red, int green, int blue) {
        return toHSV(red, green, blue);
    }

    public static float[] toRGB(int hue, int sat, int val) {
        float[] rgb = new float[3];
        int n = Color.HSBtoRGB(hue, sat, val);
        return rgb;
    }

    public static BufferedImage rgb2gray(BufferedImage img) {
        return toGrayLevel(img);
    }

    /*
    public static BufferedImage ocv_rgb2gray(BufferedImage img) {
        Mat rgbImage = ocv_img2Mat(img);
        Mat imageGray = new Mat();
        if (img.getType() == 0 || img.getType() == 1) {
            Imgproc.cvtColor(rgbImage, imageGray, Imgproc.COLOR_RGB2GRAY);
        } else if (img.getType() == 4 || img.getType() == 5) {
            Imgproc.cvtColor(rgbImage, imageGray, Imgproc.COLOR_BGR2GRAY);
        }
        return ocv_mat2Img(imageGray);
    }

    public static BufferedImage ocv_rgb2RedChannel(BufferedImage img) {
        Mat rgbImage = ocv_img2Mat(img);
        ArrayList<Mat> bgr = new ArrayList();
        Core.split(rgbImage, bgr);
        int type = bgr.get(2).type();
        System.out.println("type = " + type);
        Mat imageRed = new Mat();
        if (img.getType() == 0 || img.getType() == 1) {
            Imgproc.cvtColor(bgr.get(2), imageRed, Imgproc.COLOR_GRAY2RGB);
        } else if (img.getType() == 4) {
            Imgproc.cvtColor(bgr.get(2), imageRed, Imgproc.COLOR_GRAY2BGR);
        }
        return ocv_mat2Img(imageRed);
    }

    public static BufferedImage ocv_rgb2gray(Mat rgbImage) {
        Mat imageGray = new Mat();
        Imgproc.cvtColor(rgbImage, imageGray, Imgproc.COLOR_BGR2GRAY);
        return ocv_mat2Img(imageGray);
    }

    public static Mat ocv_rgb2grayMat(Mat rgbImage) {
        Mat imageGray = new Mat();
        Imgproc.cvtColor(rgbImage, imageGray, Imgproc.COLOR_BGR2GRAY);
        return imageGray;
    }

    public static BufferedImage ocv_mat2Img(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    public static Mat ocv_img2Mat(BufferedImage in) {
        if (in.getType() == BufferedImage.TYPE_INT_RGB) {
            Mat out;
            byte[] data;
            int r, g, b;
            out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
            data = new byte[in.getWidth() * in.getHeight() * (int) out.elemSize()];
            int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
            for (int i = 0; i < dataBuff.length; i++) {
                data[i * 3] = (byte) ((dataBuff[i] >> 0) & 0xFF);
                data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
                data[i * 3 + 2] = (byte) ((dataBuff[i] >> 16) & 0xFF);
            }
            return out;
        }

        byte[] pixels = ((DataBufferByte) in.getRaster().getDataBuffer()).getData();
        Mat mat = null;
        if (in.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            mat = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC1);
        } else {
            mat = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
        }
        mat.put(0, 0, pixels);
        return mat;
    }
     */
    public static float[][] rgb2gray2D(BufferedImage img) {
//        BufferedImage retImg = toNewColorSpace(img, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage retImg = toGrayLevel(img);
        float[][] ret = imageToPixelsFloat(retImg);
        return ret;
    }

    private static void yaz(int[] p) {
        for (int i = 0; i < p.length; i++) {
            System.out.println(p[i]);
        }
    }

    private static void yaz(String s) {
        System.out.println(s);
    }

    public static BufferedImage getHistogramImage(int[] lbp) {
        lbp = FactoryNormalization.normalizeMinMax(lbp);
        BufferedImage img = new BufferedImage(lbp.length * 10, 300, BufferedImage.TYPE_BYTE_GRAY);
        Graphics gr = img.getGraphics();
        gr.setColor(Color.white);
        int w = img.getWidth();
        int h = img.getHeight();
        int a = 20;
        gr.drawRect(a, a, w - 2 * a, h - 2 * a);
        return img;
    }

//    public static BufferedImage getHistogramImage(BufferedImage imgx) {
//        int[] hist=getHistogramData(imgx);
//        BufferedImage img = new BufferedImage(hist.length * 10, 300, BufferedImage.TYPE_BYTE_GRAY);
//        Graphics gr = img.getGraphics();
//        gr.setColor(Color.white);
//        int w = img.getWidth();
//        int h = img.getHeight();
//        int a = 20;
//        gr.drawRect(a, a, w - 2 * a, h - 2 * a);
//        return img;
//    }
    public static int[] getHistogram(BufferedImage imgx) {
        int[] d = imageToPixelsTo1D(imgx);
        int[] ret = new int[256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < d.length; j++) {
                if (d[j] == i) {
                    ret[i]++;
                }
            }
        }
        return ret;
    }

//    public static CMatrix getHistogram(BufferedImage imgx) {
//        CMatrix cm;
//        int[] d = imageToPixels255_1D(imgx);
//        int[] returnedValue = getHistogram(d);
//        cm = CMatrix.getInstance(returnedValue);
//        return cm;
//    }
    /**
     * 8 bit gray level histogram
     *
     * @param d
     * @return
     */
    public static int[] getHistogram(int[] d) {
        int[] ret = new int[256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < d.length; j++) {
                if (d[j] == i) {
                    ret[i]++;
                }
            }
        }
        return ret;
    }

    /**
     * N bins of histogram
     *
     * @param d
     * @return
     */
    public static int[] getHistogram(int[] d, int N) {
        int[] ret = new int[N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < d.length; j++) {
                if (d[j] == i) {
                    ret[i]++;
                }
            }
        }
        return ret;
    }

    public static int[] getHistogram(float[][] p) {
        float[] d = FactoryUtils.toFloatArray1D(p);
        int[] ret = new int[256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < d.length; j++) {
                if ((int) d[j] == i) {
                    ret[i]++;
                }
            }
        }
        return ret;
    }

    public static int[] getHistogram(int[][] p) {
        int[] d = FactoryUtils.toIntArray1D(p);
        int[] ret = new int[256];
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < d.length; j++) {
                if ((int) d[j] == i) {
                    ret[i]++;
                }
            }
        }
        return ret;
    }

    public static CMatrix getHistogramRed(CMatrix cm) {
        int[] d = cm.getRedChannelColor().toIntArray1D();
        int[] ret = ImageProcess.getHistogram(d);
        cm = CMatrix.getInstance(ret).transpose();
        return cm;
    }

    public static CMatrix getHistogramGreen(CMatrix cm) {
        int[] d = cm.getGreenChannelColor().toIntArray1D();
        int[] ret = ImageProcess.getHistogram(d);
        cm = CMatrix.getInstance(ret).transpose();
        return cm;
    }

    public static CMatrix getHistogramBlue(CMatrix cm) {
        int[] d = cm.getBlueChannelColor().toIntArray1D();
        int[] ret = ImageProcess.getHistogram(d);
        cm = CMatrix.getInstance(ret).transpose();
        return cm;
    }

    public static CMatrix getHistogramAlpha(CMatrix cm) {
        int[] d = cm.getAlphaChannelColor().toIntArray1D();
        int[] ret = ImageProcess.getHistogram(d);
        cm = CMatrix.getInstance(ret).transpose();
        return cm;
    }

    public static CMatrix getHistogram(CMatrix cm) {
        if (cm.getImage().getType() == BufferedImage.TYPE_BYTE_GRAY) {
            short[] d = cm.toShortArray1D();
            float[] ret = FactoryMatrix.getHistogram(d, 256);
            cm.setArray(ret);
            cm = cm.transpose();
        } else {
            int[][][] d = imageToPixelsColorInt(cm.getImage());
            float[][] ret = FactoryMatrix.getHistogram(d, 256);
            cm.setArray(ret);
        }
        cm.name += "|" + "Histogram";
        return cm;
    }

    public static BufferedImage revert(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = img.getRGB(x, y);
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                p = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(x, y, p);
            }
        }
        return img;
//        BufferedImage ret = null;
//        int[][] d = imageToPixelsInt(img);
//        int[][] q = new int[d.length][d[0].length];
//        for (int i = 0; i < d.length; i++) {
//            for (int j = 0; j < d[0].length; j++) {
//                q[i][j] = Math.abs(255 - d[i][j]);
//            }
//        }
//        ret = ImageProcess.pixelsToImageGray(q);
//        return ret;
    }

    /**
     * BufferedImage'i yatay yönde n kez çoğaltır
     *
     * @param originalImage çoğaltılacak orijinal görüntü
     * @param n kaç kez çoğaltılacağı
     * @return çoğaltılmış yeni BufferedImage
     */
    public static BufferedImage replicateImageColumn(BufferedImage originalImage, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Çoğaltma sayısı pozitif bir değer olmalıdır");
        }

        if (n == 1) {
            return deepCopy(originalImage);
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int newWidth = originalWidth * n;

        // Yeni, geniş görüntü oluştur
        BufferedImage resultImage = new BufferedImage(newWidth, originalHeight, originalImage.getType());

        // Grafik nesnesi oluştur
        Graphics2D g2d = resultImage.createGraphics();

        // Orijinal görüntüyü n kez yan yana çiz
        for (int i = 0; i < n; i++) {
            g2d.drawImage(originalImage, i * originalWidth, 0, null);
        }

        g2d.dispose();

        return resultImage;
    }

    /**
     * BufferedImage'i dikey yönde (satır) n kez çoğaltır
     *
     * @param originalImage çoğaltılacak orijinal görüntü
     * @param n kaç kez çoğaltılacağı
     * @return çoğaltılmış yeni BufferedImage
     */
    public static BufferedImage replicateImageRow(BufferedImage originalImage, int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Çoğaltma sayısı pozitif bir değer olmalıdır");
        }

        if (n == 1) {
            return deepCopy(originalImage);
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int newHeight = originalHeight * n;

        // Yeni, uzun görüntü oluştur
        BufferedImage resultImage = new BufferedImage(originalWidth, newHeight, originalImage.getType());

        // Grafik nesnesi oluştur
        Graphics2D g2d = resultImage.createGraphics();

        // Orijinal görüntüyü n kez alt alta çiz
        for (int i = 0; i < n; i++) {
            g2d.drawImage(originalImage, 0, i * originalHeight, null);
        }

        g2d.dispose();

        return resultImage;
    }

    /**
     * BufferedImage'i yatay ve dikey yönde çoğaltır
     *
     * @param originalImage çoğaltılacak orijinal görüntü
     * @param numRows dikey yönde (satır) kaç kez çoğaltılacağı
     * @param numColumns yatay yönde (sütun) kaç kez çoğaltılacağı
     * @return çoğaltılmış yeni BufferedImage
     */
    public static BufferedImage replicateImage(BufferedImage originalImage, int numRows, int numColumns) {
        if (numRows <= 0 || numColumns <= 0) {
            throw new IllegalArgumentException("Çoğaltma sayıları pozitif değerler olmalıdır");
        }

        if (numRows == 1 && numColumns == 1) {
            return deepCopy(originalImage);
        }

        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int newWidth = originalWidth * numColumns;
        int newHeight = originalHeight * numRows;

        // Yeni, genişletilmiş görüntü oluştur
        BufferedImage resultImage = new BufferedImage(newWidth, newHeight, originalImage.getType());

        // Grafik nesnesi oluştur
        Graphics2D g2d = resultImage.createGraphics();

        // İsteğe bağlı: Daha iyi kalite için render ipuçları
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Orijinal görüntüyü hem satır hem de sütun olarak çoğalt
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                int x = col * originalWidth;
                int y = row * originalHeight;
                g2d.drawImage(originalImage, x, y, null);
            }
        }

        g2d.dispose();

        return resultImage;
    }

    /**
     * BufferedImage'in derin kopyasını oluşturur
     */
    public static BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    /**
     * resize the image with resize ratio Ratio can be 0.5f or 2f ratio between
     * 0..1 reduces the image size ratio larger than 1 enlarges the image size
     * Image.SCALE_SMOOTH format
     *
     * @param img
     * @param ratio
     * @return
     */
    public static BufferedImage resize(BufferedImage img, float ratio) {
        int w = Math.round(img.getWidth() * ratio);
        int h = Math.round(img.getHeight() * ratio);
        return resize(img, w, h);
    }

    /**
     * resize the image to desired width and height value by using
     * Image.SCALE_SMOOTH format
     *
     * @param src:BufferedImage
     * @param w:width
     * @param h:height
     * @return
     */
    public static BufferedImage resize(BufferedImage src, int w, int h) {
        ////        Image tmp = img.getScaledInstance(w, h, Image.SCALE_FAST);
//        Image tmp = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
////        Image tmp = img.getScaledInstance(w, h, Image.SCALE_REPLICATE);
//        BufferedImage dimg = new BufferedImage(w, h, img.getType());
//        Graphics2D g2d = dimg.createGraphics();
//        g2d.drawImage(tmp, 0, 0, null);
//        g2d.dispose();
//        return dimg;

        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
//        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
//        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();

        BufferedImage img = new BufferedImage(w, h, src.getType());
        g2 = img.createGraphics();
        g2.drawImage(resizedImg, 0, 0, w, h, null);
        g2.dispose();
        return img;
    }

    public static BufferedImage resize(BufferedImage src, int w, int h, Object rh) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, rh);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();

        BufferedImage img = new BufferedImage(w, h, src.getType());
        g2 = img.createGraphics();
        g2.drawImage(resizedImg, 0, 0, w, h, null);
        g2.dispose();
        return img;
    }

    public static BufferedImage resizeSmooth(BufferedImage src, int w, int h) {
        Image img = src.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH);
        BufferedImage ret = toBufferedImage(img);
        return ret;
    }

    /**
     * Resizes an image using a Graphics2D BufferedImage. Width or Height might
     * w and/or h
     *
     * @param src - source image to scale
     * @param w - desired width
     * @param h - desired height
     * @return - the new resized image
     */
    public static BufferedImage resizeAspectRatio(BufferedImage src, int w, int h) {
        int finalw = w;
        int finalh = h;
        float factor = 1.0f;
        if (src.getWidth() > src.getHeight()) {
            factor = ((float) src.getHeight() / (float) src.getWidth());
            finalh = (int) (finalw * factor);
        } else {
            factor = ((float) src.getWidth() / (float) src.getHeight());
            finalw = (int) (finalh * factor);
        }

        BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, finalw, finalh, null);
        g2.dispose();

//        BufferedImage img = new BufferedImage(finalw, finalh, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage img = new BufferedImage(finalw, finalh, src.getType());
        g2 = img.createGraphics();
        g2.drawImage(resizedImg, 0, 0, finalw, finalh, null);
        g2.dispose();

//        return resizedImg;
        return img;
    }

    /**
     * Resizes an image using a Graphics2D BufferedImage.
     *
     * @param src - source image to scale
     * @param minimumSize - desired width
     * @return - the new resized image
     */
    public static BufferedImage resizeAspectRatio(BufferedImage src, int minimumSize) {
        int finalw = minimumSize;
        int finalh = minimumSize;
        float factor = 1.0f;
        if (src.getWidth() > src.getHeight()) {
            factor = ((float) src.getHeight() / (float) src.getWidth());
            finalh = (int) (finalw * factor);
        } else {
            factor = ((float) src.getWidth() / (float) src.getHeight());
            finalw = (int) (finalh * factor);
        }

        BufferedImage resizedImg = new BufferedImage(finalw, finalh, BufferedImage.TRANSLUCENT);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, finalw, finalh, null);
        g2.dispose();

        BufferedImage img = new BufferedImage(finalw, finalh, src.getType());
        g2 = img.createGraphics();
        g2.drawImage(resizedImg, 0, 0, finalw, finalh, null);
        g2.dispose();

        return img;
    }

    public static BufferedImage rotateImage(BufferedImage img, float theta) {
        float radians = theta * (float) Math.PI / 180;
        AffineTransform transform = new AffineTransform();
        transform.rotate(radians, img.getWidth() / 2, img.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        img = op.filter(img, newImage);
        return newImage;
    }

    public static BufferedImage rotateImage(BufferedImage img, CPoint cp, float theta) {
        float radians = theta * (float) Math.PI / 180;
        AffineTransform transform = new AffineTransform();
        transform.rotate(radians, cp.column, cp.row);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        img = op.filter(img, newImage);
        return newImage;
    }

    public static ArrayList<TRoi> getSpecialROIPositions(BufferedImage img) {
        ArrayList<TRoi> pos = new ArrayList<>();
        int[][] m = imageToPixelsInt(img);
        m = binarizeImage(m);
        m = FactoryUtils.transpose(m);
        int hImg = m.length;
        //each page of Quran is made up of 15 rows
        int hRow = hImg / 15;
        for (int i = 0; i < 15; i++) {
            addPosForEachRow(m, pos, i, hRow);
        }
        return pos;
    }

    private static int[][] binarizeImage(int[][] m) {
        int[][] ret = new int[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                if (m[i][j] > 0) {
                    ret[i][j] = 255;
                }
            }
        }
        return ret;
    }

    private static void addPosForEachRow(int[][] m, ArrayList<TRoi> pl, int n, int hRow) {
        CPoint p1 = new CPoint(n * hRow, 0);
        CPoint p2 = new CPoint(n * hRow + hRow, m[0].length);
        int[][] subMatrix = FactoryUtils.getSubMatrix(m, p1, p2);
        int[] prjMatrix = FactoryUtils.getProjectedMatrixOnX(subMatrix);
        TWord[] points = FactoryUtils.getPoints(prjMatrix);
        for (int i = 0; i < points.length; i++) {
            Point p = new Point(n * hRow + hRow / 2, points[i].centerPos);
            TRoi roi = new TRoi();
            roi.cp = p;
            roi.width = points[i].width;
            pl.add(roi);
            //yaz((n + 1) + ".satır da "+(i+1)+". yer"+" pos:"+p.x+","+p.y);
        }
    }

    private static float checkWindowDensity(int[][] m, int x, int y, int dx) {
        int w = 30;
        int h = 30 - dx;
        CPoint p1 = new CPoint(x, y);
        CPoint p2 = new CPoint(x + h, y + w);
        int[][] subMatrix = FactoryUtils.getSubMatrix(m, p1, p2);
//        float mean = Utils.getMeanValue(subMatrix);
        float mean = FactoryUtils.getPixelCount(subMatrix);
        yaz("roi pos:" + x + "," + y + " mean:" + mean);
        return mean;
    }

    /**
     * 16.04.2014 Musa Ataş Bir imgenin çerisinde küçük bir imgeyi correlation
     * coefficient tabanlı arar, en iyi correlation coefficient değerine sahip
     * koordinatı geri gönderir.
     */
    public static Point getPositionOfSubImageFromParentImage(int[][] parentImg, int[][] subImg, String filterType) {
        Point ret = new Point();
        ArrayList<TGrayPixel> cr = null;
        if (filterType.equals("DirectCorrelationBased")) {
            cr = performConvolveOperationForCorrelation(parentImg, subImg);
        }
        if (filterType.equals("LBP")) {
            cr = performConvolveOperationForLBP(parentImg, subImg);
        }
        for (int i = 0; i < 10; i++) {
            FactoryUtils.yaz(cr.get(i).toString());
        }
        return ret;
    }

    private static float[] to1D(int[][] parentImg) {
        int index = -1;
        float[] d = new float[parentImg.length * parentImg[0].length];
        for (int i = 0; i < parentImg.length; i++) {
            for (int j = 0; j < parentImg[0].length; j++) {
                d[++index] = parentImg[i][j] * 1.0f;
            }
        }
        return d;
    }

    private static ArrayList<TGrayPixel> performConvolveOperationForCorrelation(int[][] parentImg, int[][] subImg) {
        return getPossiblePixelsAfterConvolution(parentImg, subImg, "PearsonCorrelationCoefficient");
    }

    private static ArrayList<TGrayPixel> performConvolveOperationForLBP(int[][] parentImg, int[][] subImg) {
        return getPossiblePixelsAfterConvolution(parentImg, subImg, "LBP");
    }

    public static ArrayList<TGrayPixel> getPossiblePixelsAfterConvolution(int[][] parentImg, int[][] subImg, String filterType) {
        ArrayList<TGrayPixel> ret = new ArrayList<TGrayPixel>();
        int pw = parentImg.length;
        int ph = parentImg[0].length;

        int sw = subImg.length;
        int sh = subImg[0].length;
        FactoryUtils.yazln("pw:" + pw + " ph:" + ph + " sw:" + sw + " sh:" + sh);
        float[] m2 = to1D(subImg);
        float[] m1 = null;
        for (int i = sw / 2; i < pw - sw; i++) {
            for (int j = sh / 2; j < ph - sh; j++) {
                //Utils.yaz("i:"+i+" j:"+j);
                int[][] d = FactoryUtils.getSubMatrix(parentImg, new CPoint(i, j), new CPoint(i + sw, j + sh));
                m1 = to1D(d);
                float cor = 0;
                if (filterType.equals("PearsonCorrelationCoefficient")) {
                    cor = FactoryStatistic.PEARSON(m1, m2);
                }
                if (filterType.equals("LBP")) {
                    int[] subImgLBP = FeatureExtractionLBP.getLBP(subImg, true);
                    int[] parentImgLBP = FeatureExtractionLBP.getLBP(d, true);
                    //cor = Distance.getCorrelationCoefficientDistance(Utils.to2DArrayDouble(subImgLBP),Utilto2DArrayDoubleay(parentImgLBP));
                    cor = FactorySimilarityDistance.getEuclideanDistance(FactoryUtils.toFloatArray1D(subImgLBP), FactoryUtils.toFloatArray1D(parentImgLBP));
                }
//                if (cor > 0.85) {
//                    Utils.yazln("high Correlation:" + cor + " coordinates:" + i + ":" + j);
//                    Utils.yaz("M1=");
//                    Utils.yaz(m1);
//                    Utils.yaz("M2=");
//                    Utils.yaz(m2);
//                }

                TGrayPixel gp = new TGrayPixel();
                gp.corValue = cor;
                gp.x = i;
                gp.y = j;
                ret.add(gp);
            }
        }
//        Collections.sort(ret, new CustomComparatorForGrayPixelCorrelation());
        return ret;
    }

    public static int[][] getAbsoluteMatrixDifferenceWithROI(int[][] m_prev, int[][] m_curr, Rectangle r) {
        int[][] ret = new int[r.width][r.height];
        int k = 0;
        int l = 0;
        for (int i = r.x; i < r.x + r.width - 1; i++) {
            l = 0;
            for (int j = r.y; j < r.y + r.height - 1; j++) {
                int a = Math.abs(m_prev[i][j] - m_curr[i][j]);
                ret[k][l++] = (a < 20) ? 0 : a;
            }
            k++;
        }
        return ret;
    }

    public static int[][] segmentImageDifference(int[][] m, int size) {
        int w = m.length;
        int h = m[0].length;
        int nx = w / size;
        int ny = h / size;
        int[][] segM = new int[nx][ny];
        int[][] v = new int[size][size];
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                v = cropMatrix(m, new Rectangle(i * size, j * size, size, size));
                segM[i][j] = (int) FactoryUtils.getMean(v);
            }
        }
        return segM;
    }

    public static int[][] cropMatrix(int[][] m, Rectangle r) {
        int[][] ret = new int[r.width][r.height];
        int k = 0;
        int l = 0;
        for (int i = r.x; i < r.x + r.width - 1; i++) {
            l = 0;
            for (int j = r.y; j < r.y + r.height - 1; j++) {
                ret[k][l++] = m[i][j];
            }
            k++;
        }
        return ret;
    }

    public static CPoint getCenterOfGravityGray(BufferedImage img) {
        int[][] m = imageToPixelsInt(img);
        return ImageProcess.getCenterOfGravityGray(m);
    }

    public static CPoint getCenterOfGravityGray(BufferedImage img, boolean isShowCenter) {
        int[][] m = null;
        if (img.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            BufferedImage temp = rgb2gray(img);
            m = imageToPixelsInt(temp);
        } else {
            m = imageToPixelsInt(img);
        }
        CPoint cp = ImageProcess.getCenterOfGravityGray(m);
        if (isShowCenter) {
            img = fillRectangle(img, cp.row - 2, cp.column - 2, 5, 5, Color.black);
        }
        return cp;
    }

    public static CPoint getCenterOfGravityColor(BufferedImage img, boolean isShowCenter) {
        int[][][] m = null;
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            System.err.println("You should not use gray level image for this particular case");
            return new CPoint();
        } else {
            m = imageToPixelsColorInt(img);
        }

        BufferedImage red = ImageProcess.getRedChannelGray(img);
        BufferedImage green = ImageProcess.getGreenChannelGray(img);
        BufferedImage blue = ImageProcess.getRedChannelGray(img);
        CPoint cpRed = ImageProcess.getCenterOfGravityGray(red);
        CPoint cpGreen = ImageProcess.getCenterOfGravityGray(green);
        CPoint cpBlue = ImageProcess.getCenterOfGravityGray(blue);
        CPoint cp = new CPoint();
        cp.row = (int) ((cpRed.row + cpGreen.row + cpBlue.row) / 3.0);
        cp.column = (int) ((cpRed.column + cpGreen.column + cpBlue.column) / 3.0);
        if (isShowCenter) {
            img = fillRectangle(img, cp.row - 2, cp.column - 2, 5, 5, Color.red);
        }
        return cp;
    }

    public static CPoint getCenterOfGravityGray(int[][] m) {
        int w = m.length;
        int h = m[0].length;
        int sumX = 0;
        int sumY = 0;
        int np = 0;
        CPoint cp = new CPoint();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (m[i][j] > 0) {
                    sumX += i;
                    sumY += j;
                    np++;
                }
            }
        }
        float pr = 0;
        float pc = 0;
        if (np != 0) {
            pr = sumX * 1.0f / np;
            pc = sumY * 1.0f / np;
            cp.row = (int) pr;
            cp.column = (int) pc;
        }
        return cp;
    }

    public static int[][] getCenterOfGravityCiz(int[][] m) {
        int w = m.length;
        int h = m[0].length;
        int[][] ret = new int[w][h];
        int sumX = 0;
        int sumY = 0;
        int np = 0;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (m[i][j] > 0) {
                    sumX += i;
                    sumY += j;
                    np++;
                }
            }
        }
        //avoid division by zero
        float px = 0;
        float py = 0;
        if (np != 0) {
            px = sumX * 1.0f / np;
            py = sumY * 1.0f / np;
            ret[(int) px][(int) py] = 255;
            //writeToFile((int) px * zoom, 400 - (int) py * zoom, ++ID);
        }
        return ret;
    }

    public static CPoint getCenterOfGravityGray(float[][] m) {
        int w = m.length;
        int h = m[0].length;
        int sumX = 0;
        int sumY = 0;
        int np = 0;
        CPoint cp = new CPoint();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (m[i][j] > 0) {
                    sumX += i;
                    sumY += j;
                    np++;
                }
            }
        }
        float pr = 0;
        float pc = 0;
        if (np != 0) {
            pr = sumX * 1.0f / np;
            pc = sumY * 1.0f / np;
            cp.row = (int) pr;
            cp.column = (int) pc;
        }
        return cp;
    }

    public static float getInverseDiffMoment(int[][] img) {
        int col = img.length;
        int row = img[0].length;
        float IDF = 0.0f;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                IDF += img[i][j] / (1 + (i - j) * (i - j));
            }
        }
        return IDF;
    }

    public static float getInverseDiffMoment(BufferedImage img) {
        int[][] d = imageToPixelsInt(img);
        return getInverseDiffMoment(d);
    }

    public static float getInverseDiffMoment(float[][] img) {
        int col = img.length;
        int row = img[0].length;
        float IDF = 0.0f;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                IDF += img[i][j] / (1 + (i - j) * (i - j));
            }
        }
        return IDF;
    }

    public static float getContrast(BufferedImage img) {
        int[][] d = imageToPixelsInt(img);
        return getContrast(d);
    }

    public static float getContrast(int[][] img) {
        int col = img.length;
        int row = img[0].length;
        float contrast = 0.0f;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                contrast += img[i][j] * ((i - j) * (i - j));
            }
        }
        return contrast;
    }

    public static float getContrast(float[][] img) {
        int col = img.length;
        int row = img[0].length;
        float contrast = 0.0f;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                contrast += img[i][j] * ((i - j) * (i - j));
            }
        }
        return contrast;
    }

    public static float getEntropy(int[][] img) {
        int col = img.length;
        int row = img[0].length;
        float entropy = 0.0f;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                entropy += img[i][j] * Math.log(img[i][j]);
            }
        }
        return entropy;
    }

    public static float getEntropy(float[][] img) {
        int col = img[0].length;
        int row = img.length;
        float entropy = 0.0f;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                entropy += img[i][j] * Math.log(img[i][j] + 1);
            }
        }
        return entropy;
    }

    public static float getEntropy(BufferedImage actualImage) {
        ArrayList<String> values = new ArrayList<String>();
        int n = 0;
        Map<Integer, Integer> occ = new HashMap<>();
        for (int i = 0; i < actualImage.getHeight(); i++) {
            for (int j = 0; j < actualImage.getWidth(); j++) {
                int pixel = actualImage.getRGB(j, i);
                int alpha = (pixel >> 24) & 0xff;
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;
                //0.2989 * R + 0.5870 * G + 0.1140 * B greyscale conversion
//System.out.println("i="+i+" j="+j+" argb: " + alpha + ", " + red + ", " + green + ", " + blue);
                int d = (int) Math.round(0.2989 * red + 0.5870 * green + 0.1140 * blue);
                if (!values.contains(String.valueOf(d))) {
                    values.add(String.valueOf(d));
                }
                if (occ.containsKey(d)) {
                    occ.put(d, occ.get(d) + 1);
                } else {
                    occ.put(d, 1);
                }
                ++n;
            }
        }
        float e = 0.0f;
        for (Map.Entry<Integer, Integer> entry : occ.entrySet()) {
            int cx = entry.getKey();
            float p = (float) entry.getValue() / n;
            e += p * Math.log(p);
        }
        return -e;
    }

    public static float getEntropyWithHistogram(float[][] d) {
        int[] hist = ImageProcess.getHistogram(d);
        float sum = FactoryUtils.sum(hist);
        float e = 0.0f;
        for (int h : hist) {
            float p = h / sum;
            if (p > 0) {
                e += p * Math.log(p);
            }
        }
        return -e;
    }

    /**
     * yanlış hesaplıyor güncellenmesi gerekir
     *
     * @param d
     * @return
     */
    public static float getHomogeneity(float[][] d) {
        //aşağıdaki kod yanlış update edilmesi gerekiyor
        int[] hist = ImageProcess.getHistogram(d);
        float sum = FactoryUtils.sum(hist);
        float e = 0.0f;
        for (int h : hist) {
            float p = h / sum;
            if (p > 0) {
                e += p * Math.log(p);
            }
        }
        return -e;
    }

    public static float getEnergy(int[][] img) {
        int col = img.length;
        int row = img[0].length;
        float energy = 0.0f;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                energy += img[i][j] * img[i][j];
            }
        }
        return energy;
    }

    public static float getKurtosis(int[][] img) {
        int[] nums = FactoryUtils.toIntArray1D(img);
        int n = nums.length;
        float mean = FactoryUtils.getMean(nums);
        float deviation = 0.0f;
        float variance = 0.0f;
        float k = 0.0f;

        for (int i = 0; i < n; i++) {
            deviation = nums[i] - mean;
            variance += Math.pow(deviation, 2);
            k += Math.pow(deviation, 4);
        }
        //variance /= (n - 1);
        variance = variance / n;
        if (variance != 0.0) {
            //k = k / (n * variance * variance) - 3.0;
            k = k / (n * variance * variance);
        }
        return k;
    }

    public static float getSkewness(int[][] img) {
        int[] nums = FactoryUtils.toIntArray1D(img);
        int n = nums.length;
        float mean = FactoryUtils.getMean(nums);
        float deviation = 0.0f;
        float variance = 0.0f;
        float skew = 0.0f;

        for (int i = 0; i < n; i++) {
            deviation = nums[i] - mean;
            variance += Math.pow(deviation, 2);
            skew += Math.pow(deviation, 3);
        }
        //variance /= (n - 1);
        variance /= n;
        float standard_deviation = (float) Math.sqrt(variance);
        if (variance != 0.0) {
            skew /= (n * variance * standard_deviation);
        }
        return skew;
    }

    public static BufferedImage clone(BufferedImage img) {
        ColorModel cm = img.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = img.copyData(img.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

//    public static BufferedImage clone_deep(BufferedImage bi) {
//        ColorModel cm = bi.getColorModel();
//        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
//        WritableRaster raster = bi.copyData(null);
//        BufferedImage copy = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
//
//        // Copy properties if any
//        String[] propertyNames = bi.getPropertyNames();
//        if (propertyNames != null) {
//            for (String name : propertyNames) {
//                Object value = bi.getProperty(name);
//                if (value != null) {
//                    copy.getProperty(name);  // This doesn't set the property, but creates it if it doesn't exist
//                }
//            }
//        }
//
//        // Force a new data buffer
//        int width = bi.getWidth();
//        int height = bi.getHeight();
//        int[] pixels = new int[width * height];
//        bi.getRGB(0, 0, width, height, pixels, 0, width);
//        copy.setRGB(0, 0, width, height, pixels, 0, width);
//
//        return copy;
//    }
//    public static Image clone(Image img) {
//        BufferedImage bf = ImageProcess.toBufferedImage(img);
//        BufferedImage ret = new BufferedImage(bf.getWidth(), bf.getHeight(), bf.getType());
//        ret.getGraphics().drawImage(img, 0, 0, null);
//        return ret;
//    }
//
    public static int[] getImagePixels(BufferedImage image) {
        int[] dummy = null;
        int wid, hgt;

        // compute size of the array
        wid = image.getWidth();
        hgt = image.getHeight();

        // start getting the pixels
        Raster pixelData;
        pixelData = image.getData();

        System.out.println("wid:" + wid);
        System.out.println("hgt:" + hgt);
        System.out.println("Channels:" + pixelData.getNumDataElements());
        return pixelData.getPixels(0, 0, wid, hgt, dummy);
    }

    public static int[][] to2DRGB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] result = new int[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                result[row][col] = image.getRGB(col, row);
            }
        }

        return result;
    }

    public static int[][] to2D(BufferedImage image) {
        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // This code ensures that all the pixels in the image are loaded
        //image = new ImageIcon(image).getImage();
        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent Pixels
        //boolean hasAlpha = hasAlpha(image);
        boolean hasAlpha = false;

        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }

            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }

        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }

        // Copy image to buffered image
        Graphics g = bimage.createGraphics();

        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return bimage;
    }

//
//    public static BufferedImage readImageFromFile(File file) {
//        if (FactoryUtils.getFileExtension(file.getName()).equals("dcm")) {
//            return IJ.openImage(file.getAbsolutePath()).getBufferedImage();
//        }
//        BufferedImage ret = null;
//        try {
//            ret = ImageIO.read(file);
//        } catch (IOException ex) {
//            //Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
//        }
//        return ret;
//    }
//    public static File readImage() {
//        JFileChooser chooser = new JFileChooser();
//        //chooser.setCurrentDirectory(new java.io.File("images"));
//        chooser.setCurrentDirectory(new java.io.File(FactoryUtils.getWorkingDirectory()));
//        chooser.setDialogTitle("select image file");
//        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
//        File file = null;
//        BufferedImage ret = null;
//        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//            file = chooser.getSelectedFile();
//            if (FactoryUtils.getFileExtension(file.getName()).equals("dcm")) {
//                return file;
//            }
//            try {
//                ret = ImageIO.read(file);
//            } catch (IOException ex) {
//                //Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return file;
//    }
//    public static File readImageFileFromFolder() {
//        JFileChooser chooser = new JFileChooser();
//        chooser.setCurrentDirectory(new java.io.File("images"));
//        chooser.setDialogTitle("select Data Set file");
//        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
//        File file = null;
//        BufferedImage ret = null;
//        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//            file = chooser.getSelectedFile();
//        }
//        return file;
//    }
//    public static File readImageFileFromFolderWithDirectoryPath(String directoryPath) {
//        if (directoryPath == null || directoryPath.isEmpty()) {
//            return readImageFileFromFolder();
//        }
//        JFileChooser chooser = new JFileChooser();
//        chooser.setCurrentDirectory(new java.io.File(directoryPath));
//        chooser.setDialogTitle("select Data Set file");
//        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
//        File file = null;
//        BufferedImage ret = null;
//        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//            file = chooser.getSelectedFile();
//        }
//        return file;
//    }
//
//    public static BufferedImage readImageFromFileWithDirectoryPath(String directoryPath) {
//        JFileChooser chooser = new JFileChooser();
//        chooser.setCurrentDirectory(new java.io.File(directoryPath));
//        chooser.setDialogTitle("select Data Set file");
//        chooser.setSize(new java.awt.Dimension(45, 37)); // Generated
//        File file;
//        BufferedImage ret = null;
//        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//            file = chooser.getSelectedFile();
//            try {
//                ret = ImageIO.read(file);
//            } catch (IOException ex) {
//                //Logger.getLogger(this.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return ret;
//    }
//    public static BufferedImage readImageFromFile(String fileName) {
//        File file = new File(fileName);
//        if (!file.exists()) {
//            System.err.println("Fatal Exception: Image File not found at specified path");
//            System.exit(-1);
//            return null;
//        }
//        BufferedImage ret = null;
//        if (FactoryUtils.getFileExtension(new File(fileName).getName()).equals("dcm")) {
//            return IJ.openImage(fileName).getBufferedImage();
//        }
//        try {
//            ret = ImageIO.read(file);
//        } catch (IOException ex) {
//            Logger.getLogger(FactoryUtils.class.getName()).log(Level.SEVERE, null, "Problem reading " + fileName + "\n" + ex);
//        }
//        return ret;
//    }
//    public static CMatrix getMatrix(BufferedImage img) {
//        CMatrix cm = CMatrix.getInstance(imageToPixelsInt(img));
//        return cm;
//    }
    public static boolean writeImage(BufferedImage img) {
        return saveImage(img);
    }

    public static boolean writeImage(BufferedImage img, String fileName) {
        return saveImage(img, fileName);
    }

    public static boolean imwrite(BufferedImage img) {
        return saveImage(img);
    }

    public static boolean imwrite(BufferedImage img, String fileName) {
        return saveImage(img, fileName);
    }

    public static boolean saveImage(BufferedImage img) {
        JFileChooser FC = new JFileChooser(getDefaultDirectory());
        int retrival = FC.showSaveDialog(null);
        if (retrival == FC.APPROVE_OPTION) {
            File fileToSave = FC.getSelectedFile();
            String extension = FactoryUtils.getFileExtension(fileToSave);
            try {
                boolean ret = ImageIO.write(img, extension, fileToSave);
                return ret;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean saveImageAtFolder(BufferedImage img, String folderPath) {
        JFileChooser FC = new JFileChooser(folderPath);
        int retrival = FC.showSaveDialog(null);
        if (retrival == FC.APPROVE_OPTION) {
            File fileToSave = FC.getSelectedFile();
            String extension = FactoryUtils.getFileExtension(fileToSave);
            try {
                boolean ret = ImageIO.write(img, extension, fileToSave);
                return ret;

            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean saveImage(BufferedImage img, String fileName) {
        File file = new File(fileName);
        String extension = FactoryUtils.getFileExtension(fileName).toLowerCase();
        boolean ret = false;

        try {
            // PNG'den diğer formatlara dönüşüm için
            if ((img.getType() == BufferedImage.TYPE_INT_ARGB || img.getType() == BufferedImage.TYPE_4BYTE_ABGR) && !extension.equals("png")) {
                BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = newImage.createGraphics();
                g.setColor(Color.WHITE); // Arka plan rengi
                g.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());
                g.drawImage(img, 0, 0, null);
                g.dispose();
                img = newImage;
            }

            // WebP için özel işlem
            if (extension.equals("webp")) {
                // TwelveMonkeys WebP yazıcısını kaydet
                IIORegistry registry = IIORegistry.getDefaultInstance();
                registry.registerServiceProvider(new WebPImageWriterSpi());
            }

            ret = ImageIO.write(img, extension, file);

            // Eğer kaydetme başarısız olursa, PNG olarak kaydetmeyi dene
            if (!ret) {
                Logger.getLogger(ImageProcess.class.getName()).log(Level.WARNING,
                        "Failed to save as " + extension + ". Attempting to save as PNG.");
                ret = ImageIO.write(img, "png", new File(fileName + ".png"));
            }
        } catch (IOException ex) {
            Logger.getLogger(ImageProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static boolean saveImage2(BufferedImage img, String fileName) {
        File file = new File(fileName);
        String extension = FactoryUtils.getFileExtension(fileName);
        boolean ret = false;
        try {
            ret = ImageIO.write(img, extension, file);
        } catch (IOException ex) {
            Logger.getLogger(ImageProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static boolean saveImageTransparentBackGround(BufferedImage img, String fileName) {
        File file = new File(fileName);
        String extension = FactoryUtils.getFileExtension(fileName);
        boolean ret = false;
        try {
            ret = ImageIO.write(img, extension, file);
        } catch (IOException ex) {
            Logger.getLogger(ImageProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static void saveImageSVG(BufferedImage img, String dest_path) {
        String path = dest_path.replace("." + FactoryUtils.getFileExtension(dest_path), ".png");
        saveImage(img, path);
        RasterToVector rtv = new RasterToVector(path);
        rtv.convertToSVG(dest_path);
    }

    public static void saveImageSVG(String source_path, String dest_path) {
        RasterToVector rtv = new RasterToVector(source_path);
        rtv.convertToSVG(dest_path);
    }

//    public static BufferedImage convertDicomToBufferedImage(String filePath) {
//        File file = new File(filePath);
//        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName("DICOM");
//        BufferedImage img = null;
//        while (iterator.hasNext()) {
//            ImageReader imageReader = (ImageReader) iterator.next();
//            DicomImageReadParam dicomImageReadParam = (DicomImageReadParam) imageReader.getDefaultReadParam();
//            try {
//                ImageInputStream iis = ImageIO.createImageInputStream(file);
//                imageReader.setInput(iis, false);
//                img = imageReader.read(0, dicomImageReadParam);
//                iis.close();
//                if (img == null) {
//                    System.out.println("Could not read image!!");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return img;
//    }

    /*
    public static boolean ocv_saveImageWithFormat(BufferedImage img, String path) {
        Mat imgM = ocv_img2Mat(img);
        return Imgcodecs.imwrite(path, imgM);
    }

    public static Mat ocv_saveImage(BufferedImage img, String path) {
        Mat imgM = ocv_img2Mat(img);
        Imgcodecs.imwrite(path, imgM);
        return imgM;
    }

    public static BufferedImage ocv_imRead(String path) {
        Mat imgM = Imgcodecs.imread(path);
        return ocv_mat2Img(imgM);
    }
     */
    public static void saveGridImage(BufferedImage gridImage, String filePath) {
        File output = new File(filePath);
        output.delete();

        final String formatName = "png";

        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
            try {
                ImageWriter writer = iw.next();
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
                IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
                if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                    continue;
                }

                try {
                    setDPI(metadata, 300);

                } catch (IIOInvalidTreeException ex) {
                    Logger.getLogger(FactoryUtils.class
                            .getName()).log(Level.SEVERE, null, ex);
                }

                final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
                try {
                    writer.setOutput(stream);
                    writer.write(metadata, new IIOImage(gridImage, null, metadata), writeParam);
                } finally {
                    stream.close();
                }
                break;

            } catch (IOException ex) {
                Logger.getLogger(FactoryUtils.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void setDPI(IIOMetadata metadata, int DPI) throws IIOInvalidTreeException {

        // for PMG, it's dots per millimeter inc to cm=2.54
//        float dotsPerMilli = 1.0 * DPI / 10 / 2.54;
        float dotsPerMilli = 1.0f * DPI;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

    public static BufferedImage getBufferedImage(JPanel panel) {
        int w = panel.getWidth();
        int h = panel.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        panel.paint(g);
        return bi;
    }

    public static BufferedImage saveImageAsJPEG(String filePath, BufferedImage currBufferedImage, int k) {
        //System.out.println("New Image Captured and jpg file saved");
        try {
            if (currBufferedImage == null) {
                return null;
            }
            File file = new File(filePath + "\\img_" + k + ".jpg");
            ImageIO.write(currBufferedImage, "jpg", file);
            BufferedImage myImage = ImageIO.read(file);
            currBufferedImage = myImage;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return currBufferedImage;
    }

    public static BufferedImage getAlphaChannelGray(BufferedImage bf) {
        return rgb2gray(getAlphaChannelColor(bf));
    }

    public static BufferedImage getRedChannelGray(BufferedImage bf) {
        return rgb2gray(getRedChannelColor(bf));
    }

    public static BufferedImage getGreenChannelGray(BufferedImage bf) {
        return rgb2gray(getGreenChannelColor(bf));
    }

    public static BufferedImage getBlueChannelGray(BufferedImage bf) {
        return rgb2gray(getBlueChannelColor(bf));
    }

    public static BufferedImage getAlphaChannelColor(BufferedImage bf) {
        int[][][] d = imageToPixelsColorInt(bf);
        int[][] ret = new int[d.length][d[0].length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[i][j] = d[i][j][0];
            }
        }
        return pixelsToImageColor(d);
    }

    public static BufferedImage getRedChannelColor(BufferedImage bf) {
        int[][][] d = imageToPixelsColorInt(bf);
        int[][][] ret = new int[d.length][d[0].length][4];
        int nr = d.length;
        int nc = d[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j][0] = d[i][j][0];
                ret[i][j][1] = d[i][j][1];
            }
        }
        return pixelsToImageColor(ret);
    }

    public static BufferedImage getGreenChannelColor(BufferedImage bf) {
        int[][][] d = imageToPixelsColorInt(bf);
        int[][][] ret = new int[d.length][d[0].length][4];
        int nr = d.length;
        int nc = d[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j][0] = d[i][j][0];
                ret[i][j][2] = d[i][j][2];
            }
        }
        return pixelsToImageColor(ret);
    }

    public static BufferedImage getBlueChannelColor(BufferedImage bf) {
        int[][][] d = imageToPixelsColorInt(bf);
        int[][][] ret = new int[d.length][d[0].length][4];
        int nr = d.length;
        int nc = d[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                ret[i][j][0] = d[i][j][0];
                ret[i][j][3] = d[i][j][3];
            }
        }
        return pixelsToImageColor(ret);
    }

    public static float[][] getRedChannelDouble(float[][][] d) {
        float[][] ret = new float[d.length][d[0].length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[i][j] = d[i][j][1];
            }
        }
        return ret;
    }

    public static float[][] getGreenChannelDouble(float[][][] d) {
        float[][] ret = new float[d.length][d[0].length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[i][j] = d[i][j][2];
            }
        }
        return ret;
    }

    public static float[][] getBlueChannelDouble(float[][][] d) {
        float[][] ret = new float[d.length][d[0].length];
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                ret[i][j] = d[i][j][3];
            }
        }
        return ret;
    }

    public static BufferedImage filterMedian(BufferedImage imgx) {
        ImagePlus imagePlus = new ImagePlus("", imgx);
        ImageProcessor ip = imagePlus.getProcessor();
        ip.medianFilter();
        return ip.getBufferedImage();
        //return filterMedian(imgx, 3);
    }

    public static BufferedImage filterGaussian(BufferedImage imgx, int size) {
        GaussianFilter gaussianFilter = new GaussianFilter(size);
        BufferedImage gaussianFiltered = clone(imgx);
        gaussianFilter.filter(imgx, gaussianFiltered);
        return gaussianFiltered;
    }

    public static BufferedImage filterMedian(BufferedImage imgx, int size) {
        int w = imgx.getHeight(null);
        int h = imgx.getWidth(null);
        int[] kernel = new int[size * size];
        int dizi[][] = new int[w][h];
        int temp[][] = new int[w][h];
        dizi = imageToPixelsInt(imgx);
        temp = (int[][]) dizi.clone();
        for (int i = 1; i < w - 1; i++) {
            for (int j = 1; j < h - 1; j++) {
                int f = 0;
                for (int k = -1; k <= 1; k++) {
                    for (int t = -1; t <= 1; t++) {
                        kernel[f] = dizi[i + k][j + t];
                        f++;
                    }
                }
                temp[i][j] = medianKernel(kernel);
            }
        }
        dizi = (int[][]) temp.clone();
        BufferedImage ret_img = ImageProcess.pixelsToImageGray(dizi);
        return ret_img;
    }

    public static int medianKernel(int[] kernel) {
        int buffer;
        for (int i = 0; i < kernel.length; i++) {
            for (int j = i; j < kernel.length; j++) {
                if (kernel[j] > kernel[i]) {
                    buffer = kernel[j];
                    kernel[j] = kernel[i];
                    kernel[i] = buffer;
                }
            }
        }
        int mid = kernel.length / 2;
        return kernel[mid];
    }

    public static BufferedImage filterMean(BufferedImage imgx) {
        AverageFilter avgFilter = new AverageFilter();
        BufferedImage dest = clone(imgx);
        avgFilter.filter(dest, imgx);
        return dest;
//        return ImageProcess.filterMean(imgx, 3);
    }

    public static BufferedImage filterMotionBlur(BufferedImage imgx) {
        MotionBlurOp op = new MotionBlurOp(5, 45, 13, 3);
//        MotionBlurFilter motionFilter=new MotionBlurFilter();
//        motionFilter.setAngle(20);
//        motionFilter.setDistance(15);
        //motionFilter.setRotation(10);
        BufferedImage dest = clone(imgx);
        op.filter(dest, imgx);
        return dest;
    }

    public static float[][] filterMean(float[][] d) {
        float[][] ret = imageToPixelsFloat(filterMean(pixelsToImageGray(d), 3));
        return ret;
    }

    public static BufferedImage filterMean(BufferedImage imgx, int size) {
        int w = imgx.getWidth(null);
        int h = imgx.getHeight(null);
        int[] kernel = new int[size * size];
        int dizi[][] = new int[w][h];
        int temp[][] = new int[w][h];
        dizi = imageToPixelsInt(imgx);
        temp = FactoryMatrix.clone(dizi);
        int sum = 0;
        for (int i = 1; i < w - 1; i++) {
            for (int j = 1; j < h - 1; j++) {
                int f = 0;
                for (int k = -1; k <= 1; k++) {
                    for (int t = -1; t <= 1; t++) {
                        kernel[f] = dizi[i + k][j + t];
                        sum += kernel[f];
                        f++;
                    }
                }
//                if (sum / 9 > 0) {
//                    System.out.println("org:" + temp[i][j] + " avg:" + (sum / (size * size)) + " r,c:" + j + ":" + i);
//                }
                temp[i][j] = sum / (size * size);
                sum = 0;
            }
        }
        int[][] d = FactoryMatrix.clone(temp);
        BufferedImage ret_img = ImageProcess.pixelsToImageGray(d);
        return ret_img;
    }

//    public static float[][] meanFilter(float[][] imgx) {
//        int r = imgx.length;
//        int c = imgx[0].length;
//        int[] kernel = new int[9];
//        float temp[][] = new float[r][c];
//        temp = FactoryMatrix.clone(imgx);
//        int sum = 0;
//        for (int row = 1; row < r - 1; row++) {
//            for (int column = 1; column < c - 1; column++) {
//                int f = 0;
//                for (int k = -1; k <= 1; k++) {
//                    for (int t = -1; t <= 1; t++) {
//                        kernel[f] = (int) imgx[row + k][column + t];
//                        sum += kernel[f];
//                        f++;
//                    }
//                }
//                temp[row][column] = (int) (sum / 9.0);
//                sum = 0;
//            }
//        }
//        return temp;
//    }
    public static BufferedImage erode(BufferedImage img, int[][] kernel) {
        BufferedImage imgx = clone(img);
        int w = imgx.getWidth(null);
        int h = imgx.getHeight(null);
        int dizi[][] = new int[h][w];
        int temp[][] = new int[h][w];
        dizi = imageToPixelsInt(imgx);
        for (int i = 1; i < h - 1; i++) {
            for (int j = 1; j < w - 1; j++) {
                if (dizi[i - 1][j - 1] == kernel[0][0] && dizi[i - 1][j] == kernel[0][1] && dizi[i - 1][j + 1] == kernel[0][2]
                        && dizi[i][j - 1] == kernel[1][0] && dizi[i][j] == kernel[1][1] && dizi[i][j + 1] == kernel[1][2]
                        && dizi[i + 1][j - 1] == kernel[2][0] && dizi[i + 1][j] == kernel[2][1] && dizi[i + 1][j + 1] == kernel[2][2]) {

                    temp[i][j] = 0;
                } else {
                    temp[i][j] = 255;
                }
            }
        }
        dizi = temp;
        BufferedImage ret_img = ImageProcess.pixelsToImageGray(dizi);
        return ret_img;
    }

    public static BufferedImage erode(BufferedImage imgx) {
        int[][] kernel = {{255, 255, 255}, {255, 255, 255}, {255, 255, 255}};
        return dilate(imgx, kernel);
    }

    public static BufferedImage dilate(BufferedImage img, int[][] kernel) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        int dizi[][] = new int[h][w];
        int temp[][] = new int[h][w];
        BufferedImage imgx = clone(img);
        dizi = imageToPixelsInt(imgx);
        for (int i = 1; i < h - 1; i++) {
            for (int j = 1; j < w - 1; j++) {
                if (dizi[i - 1][j - 1] == kernel[0][0] || dizi[i - 1][j] == kernel[0][1] || dizi[i - 1][j + 1] == kernel[0][2]
                        || dizi[i][j - 1] == kernel[1][0] || dizi[i][j] == kernel[1][1] || dizi[i][j + 1] == kernel[1][2]
                        || dizi[i + 1][j - 1] == kernel[2][0] || dizi[i + 1][j] == kernel[2][1] || dizi[i + 1][j + 1] == kernel[2][2]) {
                    temp[i][j] = 0;
                } else {
                    temp[i][j] = 255;
                }
            }
        }
        dizi = temp;
        BufferedImage ret_img = ImageProcess.pixelsToImageGray(dizi);
        return ret_img;
    }

    public static BufferedImage dilate(BufferedImage imgx) {
        int[][] kernel = {{255, 255, 255}, {255, 255, 255}, {255, 255, 255}};
        return dilate(imgx, kernel);
    }

    public static BufferedImage kernelFilter(BufferedImage imgx) {
        int w = imgx.getWidth(null);
        int h = imgx.getHeight(null);
        int dizi[][] = new int[w][h];
        int kw = 15;
        int kh = 15;
        int med = kw / 2;

        int[][] kernel = new int[kw][kh];
        for (int i = 2; i < kw - 2; i++) {
            for (int j = 2; j < kh - 2; j++) {
                kernel[i][j] = 255;
            }
        }

        dizi = imageToPixelsInt(imgx);
        for (int i = med; i < w - med; i++) {
            for (int j = med; j < h - med; j++) {
                if (dizi[i - med][j - med] == 0 && dizi[i - med][j + med] == 0 && dizi[i + med][j - med] == 0 && dizi[i + med][j + med] == 0
                        && dizi[i][j] == 255 && dizi[i - 1][j - 1] == 255 && dizi[i - 1][j + 1] == 255 && dizi[i + 1][j - 1] == 255 && dizi[i + 1][j + 1] == 255) {
                    for (int k = 0; k < med; k++) {
                        for (int m = 0; m < med; m++) {
                            if (dizi[i + k][j + m] == 255) {
                                dizi[i + k][j + m] = 0;
                            }

                        }
                    }
                    //writeln("bulundu:"+i+","+j);
                }
            }
        }
        //dizi=temp;
        BufferedImage ret_img = ImageProcess.pixelsToImageGray(dizi);
        return ret_img;
    }

    /**
     * Get binary int treshold using Otsu's method
     */
    public static int getOtsuTresholdValue(BufferedImage original) {
        float[][] d = ImageProcess.imageToPixelsFloat(original);
        return getOtsuTresholdValue(d);
    }

    /**
     * Get binary int treshold using Otsu's method
     */
    public static int getOtsuTresholdValue(float[][] d) {
        int[] histogram = ImageProcess.getHistogram(d);
        int total = d.length * d[0].length;

        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) {
                continue;
            }
            wF = total - wB;

            if (wF == 0) {
                break;
            }

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }
        return threshold;
    }

    /**
     * Binarize Color Image with a given threshold value i.e. otsuThreshold
     *
     * @param original
     * @return
     */
    public static BufferedImage binarizeColorImage(BufferedImage original, int threshold) {
        int red;
        int newPixel;
        BufferedImage binarized = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());

        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {

                // Get pixels
                red = new Color(original.getRGB(i, j)).getRed();
                int alpha = new Color(original.getRGB(i, j)).getAlpha();
                if (red > threshold) {
                    newPixel = 255;
                } else {
                    newPixel = 0;
                }
                newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
                binarized.setRGB(i, j, newPixel);

            }
        }
        return binarized;
    }

    /**
     * Binarize Color Image with a given threshold value i.e. otsuThreshold
     *
     * @param original
     * @return
     */
    public static BufferedImage binarizeColorImage(BufferedImage original) {
        original = rgb2gray(original);
        int threshold = getOtsuTresholdValue(original);
        return binarizeColorImage(original, threshold);
    }

    /**
     * Binarize Image with a given threshold value hint: you can determine
     * threshold value from otsu approach and then pass as an argument
     *
     * @param original
     * @return
     */
    public static BufferedImage binarizeGrayScaleImage(BufferedImage original, int threshold) {
        int[][] d = imageToPixelsInt(original);
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (d[i][j] > threshold) {
                    d[i][j] = 255;
                } else {
                    d[i][j] = 0;
                }
            }
        }
        BufferedImage binarized = pixelsToImageGray(d);
        return binarized;
    }

    /**
     * Binarize Image with a given threshold value you can determine threshold
     * value from otsu approach and then pass as an argument
     *
     * @param original
     * @return
     */
    public static BufferedImage binarizeGrayScaleImage(BufferedImage original) {
        int threshold = getOtsuTresholdValue(original);
        return binarizeGrayScaleImage(original, threshold);
    }

    /**
     * Binarize Image with a given threshold value you can determine threshold
     * value from otsu approach
     *
     * @param d
     * @return
     */
    public static BufferedImage binarizeGrayScaleImage(float[][] d, int threshold) {
        int nr = d.length;
        int nc = d[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                if (d[i][j] > threshold) {
                    d[i][j] = 255;
                } else {
                    d[i][j] = 0;
                }
            }
        }

        BufferedImage binarized = pixelsToImageGray(d);
        return binarized;
    }

    // Convert R, G, B, Alpha to standard 8 bit
    public static int colorToRGB(int alpha, int red, int green, int blue) {
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;
        return newPixel;
    }

//    /**
//     * convert let say gray scale image to RGB or any other types
//     *
//     * @param img
//     * @param newType
//     * @return
//     */
//    public static BufferedImage toNewColorSpace(BufferedImage img, int newType) {
//        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), newType);
//        ret.getGraphics().drawImage(img, 0, 0, null);
//        return ret;
//    }
//
    /**
     * convert let say gray scale image to RGB or any other types
     *
     * @param img
     * @param newType
     * @return
     */
    public static BufferedImage toBufferedImage(BufferedImage img, int newType) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), newType);
        ret.getGraphics().drawImage(img, 0, 0, null);
        return ret;
    }

    public static float[][] highPassFilter(float[][] m, int t) {
        float[][] d = FactoryMatrix.clone(m);
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (d[i][j] < t) {
                    d[i][j] = 0;
                }
            }
        }
        return d;
    }

    public static float[][] lowPassFilter(float[][] m, int t) {
        float[][] d = FactoryMatrix.clone(m);
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (d[i][j] > t) {
                    d[i][j] = 0;
                }
            }
        }
        return d;
    }

    public static float[][] swapColor(float[][] m, int c1, int c2) {
        float[][] d = FactoryMatrix.clone(m);
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (d[i][j] == c1) {
                    d[i][j] = c2;
                }
            }
        }
        return d;
    }

    /*
    public static float[][] imageToPixels2DFromOpenCV(Mat m) {
        float[][] ret = new float[m.height()][m.width()];
        for (int i = 0; i < m.height(); i++) {
            for (int j = 0; j < m.width(); j++) {
                ret[i][j] = (float) m.get(i, j)[0];
            }
        }
        return ret;
    }
     */
//    public static Rectangle[] detectFacesRectangles(String type, BufferedImage img) {
//        
//        String xml = "";
//        if (type.equals("haar")) {
//            xml = "etc\\haarcascades\\haarcascade_frontalface_alt.xml";
    ////            xml = "etc\\haarcascades\\haarcascade_frontalface_alt_tree.xml";
//        }
//        if (type.equals("lbp")) {
//            xml = "etc\\lbpcascades\\lbpcascade_frontalface.xml";
//        }
////        System.out.println("xml_file = " + xml);
//        CascadeClassifier faceDetector = new CascadeClassifier(xml);
//        Mat imageGray = ocv_img2Mat(img);
//
//        MatOfRect faceDetections = new MatOfRect();
//        faceDetector.detectMultiScale(imageGray, faceDetections);
////        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));
//        for (Rect rect : faceDetections.toArray()) {
//            Imgproc.rectangle(imageGray, new org.opencv.core.Point(rect.x, rect.y), new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height),
//                    new Scalar(0, 255, 255), 2);
//
//        }
//        return ocv_mat2Img(imageGray);
//    }
    public static BufferedImage drawRectangle(BufferedImage img, int x, int y, int w, int h, int thickness, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            //img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setColor(color);
        g2d.drawRect(y, x, w, h);
        g2d.dispose();
        return img;
    }

    public static BufferedImage drawRectangle(BufferedImage img, Rectangle rect, int thickness, Color color) {
        img = convertToBufferedImageTypes(img, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setColor(color);
        g2d.drawRect(rect.x, rect.y, rect.width, rect.height);
        g2d.dispose();
        return img;
    }

    public static BufferedImage drawLine(BufferedImage img, int r1, int c1, int r2, int c2, int thickness, Color color) {
        BufferedImage ret = null;
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawLine(c1, r1, c2, r2);
        g2d.dispose();
        return img;
    }

    final public static BufferedImage toNewColorSpace(BufferedImage image, int newType) {
        BufferedImage ret = null;
        try {
            ret = new BufferedImage(
                    image.getWidth(),
                    image.getHeight(),
                    newType);
            ColorConvertOp xformOp = new ColorConvertOp(null);
            xformOp.filter(image, ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static BufferedImage fillRectangle(BufferedImage img, int x, int y, int w, int h, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setColor(color);
        g2d.fillRect(y, x, w, h);
        g2d.dispose();
        return img;
    }

    public static BufferedImage draw3DRectangle(BufferedImage img, int x, int y, int w, int h, int thickness, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setColor(color);
        g2d.draw3DRect(y, x, w, h, true);
        g2d.dispose();
        return img;
    }

    public static BufferedImage fill3DRectangle(BufferedImage img, int x, int y, int w, int h, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setColor(color);
        g2d.fill3DRect(y, x, w, h, true);
        g2d.dispose();
        return img;
    }

    public static BufferedImage drawRoundRectangle(BufferedImage img, int x, int y, int w, int h, int arcw, int arch, int thickness, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setColor(color);
        g2d.drawRoundRect(y, x, w, h, arcw, arch);
        g2d.dispose();
        return img;
    }

    public static BufferedImage fillRoundRectangle(BufferedImage img, int x, int y, int w, int h, int arcw, int arch, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setColor(color);
        g2d.fillRoundRect(y, x, w, h, arcw, arch);
        g2d.dispose();
        return img;
    }

    public static BufferedImage drawOval(BufferedImage img, int x, int y, int w, int h, int thickness, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setColor(color);
        g2d.drawOval(y, x, w, h);
        g2d.dispose();
        return img;
    }

    public static BufferedImage drawShape(BufferedImage img, Shape p, int thickness, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setColor(color);
        g2d.draw(p);
        g2d.dispose();
        return img;
    }

    public static BufferedImage fillShape(BufferedImage img, Shape p, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setColor(color);
        g2d.fill(p);
        g2d.dispose();
        return img;
    }

    public static BufferedImage drawPolygon(BufferedImage img, Polygon p, int thickness, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setColor(color);
        g2d.drawPolygon(p);
        g2d.dispose();
        return img;
    }

    public static BufferedImage fillPolygon(BufferedImage img, Polygon p, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setColor(color);
        g2d.fillPolygon(p);
        g2d.dispose();
        return img;
    }

    public static BufferedImage drawArc(BufferedImage img, int x, int y, int w, int h, int startAngle, int arcAngle, int thickness, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setColor(color);
        g2d.drawArc(y, x, w, h, startAngle, arcAngle);
        g2d.dispose();
        return img;
    }

    public static BufferedImage drawPolyLine(BufferedImage img, int[] xPoints, int[] yPoints, int nPoints, int thickness, Color color) {
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setColor(color);
        g2d.drawPolyline(yPoints, xPoints, nPoints);
        g2d.dispose();
        return img;
    }

    public static BufferedImage fillOval(BufferedImage img, int x, int y, int w, int h, Color color) {
        BufferedImage ret = null;
        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img = toNewColorSpace(img, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setColor(color);
        g2d.fillOval(y, x, w, h);
        g2d.dispose();
        return img;
    }

    /*
    public static BufferedImage detectFaces(String type, BufferedImage img) {
        String xml = "";
        if (type.equals("haar")) {
            xml = "etc\\haarcascades\\haarcascade_frontalface_alt.xml";
        }
        if (type.equals("lbp")) {
            xml = "etc\\lbpcascades\\lbpcascade_frontalface.xml";
        }
        BufferedImage img2 = ImageProcess.clone(img);
        CascadeClassifier faceDetector = new CascadeClassifier(xml);
        Mat imageGray = ocv_img2Mat(img2);

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(imageGray, faceDetections);
        for (Rect rect : faceDetections.toArray()) {
//            Imgproc.rectangle(imageGray, new org.opencv.core.Point(rect.x, rect.y), new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height),
//                    new Scalar(0, 255, 255), 2);
            drawRectangle(img, rect.y - (int) (rect.height * 0.1), rect.x, rect.width, rect.height + (int) (rect.height * 0.2), 1, Color.yellow);

        }
//        return ocv_mat2Img(imageGray);
        return img;
    }

    public static BufferedImage detectFaces(String type, BufferedImage img, CRectangle r, boolean showRect) {
        String xml = "";
        if (type.equals("haar")) {
            xml = "etc\\haarcascades\\haarcascade_frontalface_alt.xml";
        }
        if (type.equals("lbp")) {
            xml = "etc\\lbpcascades\\lbpcascade_frontalface.xml";
        }
        BufferedImage img2 = ImageProcess.clone(img);
        CascadeClassifier faceDetector = new CascadeClassifier(xml);
        Mat imageGray = ocv_img2Mat(img2);

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(imageGray, faceDetections);
        for (Rect rect : faceDetections.toArray()) {
//            Imgproc.rectangle(imageGray, new org.opencv.core.Point(rect.x, rect.y), new org.opencv.core.Point(rect.x + rect.width, rect.y + rect.height),
//                    new Scalar(0, 255, 255), 2);
            if (showRect) {
                drawRectangle(img, rect.y - (int) (rect.height * 0.1), rect.x, rect.width, rect.height + (int) (rect.height * 0.2), 1, Color.yellow);
            }
            r.row = rect.y - (int) (rect.height * 0.1);
            r.column = rect.x;
            r.width = rect.width;
            r.height = rect.height + (int) (rect.height * 0.2);

        }
//        return ocv_mat2Img(imageGray);
        return img;
    }

    public static Rectangle[] getFacesRectangles(String type, BufferedImage img) {
        String xml = "";
        if (type.equals("haar")) {
            xml = "etc\\haarcascades\\haarcascade_frontalface_alt.xml";
//            xml = "etc\\haarcascades\\haarcascade_frontalface_alt_tree.xml";
        }
        if (type.equals("lbp")) {
            xml = "etc\\lbpcascades\\lbpcascade_frontalface.xml";
        }
        BufferedImage img2 = ImageProcess.clone(img);
        CascadeClassifier faceDetector = new CascadeClassifier(xml);
        Mat imageGray = ocv_img2Mat(img2);

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(imageGray, faceDetections);
        Rect[] rects = faceDetections.toArray();
        Rectangle[] ret = new Rectangle[faceDetections.toArray().length];
        for (int i = 0; i < rects.length; i++) {
            Rect r = rects[i];
            ret[i] = new Rectangle(r.x, r.y, r.width, r.height);
        }
        return ret;
    }

    public static CRectangle[] getFacesRectanglesAsCRectangle(String type, BufferedImage img) {
        Rectangle[] rects = getFacesRectangles(type, img);
        CRectangle[] ret = new CRectangle[rects.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new CRectangle(rects[i]);
        }
        return ret;
    }
     */
    public static BufferedImage toARGB(BufferedImage image) {
        return toNewColorSpace(image, BufferedImage.TYPE_INT_ARGB);
    }

    public static BufferedImage toBGR(BufferedImage image) {
        return toNewColorSpace(image, BufferedImage.TYPE_3BYTE_BGR);
    }

    public static BufferedImage toGrayLevel(BufferedImage img) {
        BufferedImage image = new BufferedImage(img.getWidth(), img.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = image.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return image;
//        return toNewColorSpace(image, BufferedImage.TYPE_BYTE_GRAY);
//        return pixelsToImageGray(imageToPixelsDouble(image));
    }

    public static BufferedImage toBinary(BufferedImage image) {
        return toNewColorSpace(image, BufferedImage.TYPE_BYTE_BINARY);
    }

    public static BufferedImage flipImageLeft2Right(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Güvenli tip kontrolü
        int type = BufferedImage.TYPE_INT_RGB;  // varsayılan
        if (image.getType() != 0) {  // TYPE_CUSTOM = 0
            type = image.getType();
        }

        try {
            BufferedImage flipped = new BufferedImage(width, height, type);
            Graphics2D g = flipped.createGraphics();
            g.drawImage(image, width, 0, -width, height, null);
            g.dispose();
            return flipped;
        } catch (Exception e) {
            // Hata durumunda en güvenli tip ile tekrar dene
            BufferedImage flipped = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = flipped.createGraphics();
            g.drawImage(image, width, 0, -width, height, null);
            g.dispose();
            return flipped;
        }
    }

    public static BufferedImage flipImageTop2Bottom(BufferedImage image) {
        // Dikey flip için (1, -1) kullan
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        // Y ekseninde translate
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    public static BufferedImage flipHorizontal(BufferedImage image) {
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(-1, 1));
        at.concatenate(AffineTransform.getTranslateInstance(-image.getWidth(), 0));
        BufferedImage bf = buildTransformed(image, at);

//        BufferedImage bf = new BufferedImage(
//                image.getWidth(), image.getHeight(),
//                BufferedImage.TYPE_3BYTE_BGR);
//        Graphics2D g = image.createGraphics();
//        g.drawImage(image , 0,0,-image.getWidth(),image.getHeight(),null);
        return bf;
    }

    public static BufferedImage flipVertical(BufferedImage image) {
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1, -1));
        at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
        return buildTransformed(image, at);
    }

    public static BufferedImage buildTransformed(BufferedImage image, AffineTransform at) {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = newImage.createGraphics();
        g.transform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    public static BufferedImage invertImage(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            image = toARGB(image);
        }
        LookupTable lookup = new LookupTable(0, 4) {
            @Override
            public int[] lookupPixel(int[] src, int[] dest) {
                dest[0] = (int) (255 - src[0]);
                dest[1] = (int) (255 - src[1]);
                dest[2] = (int) (255 - src[2]);
                return dest;
            }
        };
        LookupOp op = new LookupOp(lookup, new RenderingHints(null));
        return op.filter(image, null);
    }

    /**
     * if location and size are not given overlayed image is positioned at 0,0
     * white pixels are ignored through overlaying process
     *
     * @param bf
     * @param overlay
     * @return
     */
    public static BufferedImage overlayImage(BufferedImage bf, BufferedImage overlay) {
        int[][][] bfp = imageToPixelsColorInt(bf);
        int[][][] ovp = imageToPixelsColorInt(overlay);
        int r = ovp.length;
        int c = ovp[0].length;
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (ovp[i][j][0] != 255 || ovp[i][j][1] != 255 || ovp[i][j][2] != 255 || ovp[i][j][3] != 255) {
                    for (int k = 0; k < 4; k++) {
                        bfp[i][j][k] = ovp[i][j][k];
                    }
                }
            }
        }
        return pixelsToImageColor(bfp);
    }

    /**
     * overlay image onto original image
     *
     * @param bf
     * @param overlay
     * @param rect
     * @param bkg
     * @return
     */
    public static BufferedImage overlayImage(BufferedImage bf, BufferedImage overlay, CRectangle rect, int bkg) {
        int[][][] bfp = imageToPixelsColorInt(bf);
        int[][][] ovp = imageToPixelsColorInt(overlay);
        int r1 = bfp.length;
        int c1 = bfp[0].length;
        int r = ovp.length;
        int c = ovp[0].length;
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
//                if (ovp[i][j][0] != bkg || ovp[i][j][1] != bkg || ovp[i][j][2] != bkg || ovp[i][j][3] != bkg) {
                if (ovp[i][j][1] != bkg || ovp[i][j][2] != bkg || ovp[i][j][3] != bkg) {
                    for (int k = 1; k < 4; k++) {
                        if (i + rect.row > 0 && j + rect.column > 0 && i + rect.row < r1 && j + rect.column < c1) {
                            bfp[i + rect.row][j + rect.column][k] = ovp[i][j][k];
                        }
                    }
                }
            }
        }
        return pixelsToImageColor(bfp);
    }

    public static BufferedImage overlayImage(BufferedImage bf, BufferedImage overlay, int x, int y) {
        int w = x + overlay.getWidth();
        int h = y + overlay.getHeight();
        if (bf.getWidth() > w) {
            w = bf.getWidth();
        }
        if (bf.getHeight() > h) {
            h = bf.getHeight();
        }
        BufferedImage ret = new BufferedImage(w, h, bf.getType());
        Graphics2D gr = ret.createGraphics();
        gr.drawImage(bf, 0, 0, null);
        gr.drawImage(overlay, x, y, null);
        gr.dispose();
        return ret;
    }

    public static BufferedImage overlayImage(BufferedImage bf, BufferedImage overlay, CPoint cp, int bkg) {
        int[][][] bfp = imageToPixelsColorInt(bf);
        int[][][] ovp = imageToPixelsColorInt(overlay);
        int r1 = bfp.length;
        int c1 = bfp[0].length;
        int r = ovp.length;
        int c = ovp[0].length;
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                if (ovp[i][j][1] != bkg || ovp[i][j][2] != bkg || ovp[i][j][3] != bkg) {
                    for (int k = 1; k < 4; k++) {
                        if (i + cp.row > 0 && j + cp.column > 0 && i + cp.row < r1 && j + cp.column < c1) {
                            bfp[i + cp.row][j + cp.column][k] = ovp[i][j][k];
                        }
                    }
                }
            }
        }
        return pixelsToImageColor(bfp);
    }

    public static BufferedImage overlayImage(BufferedImage bgImage, BufferedImage fgImage, float alpha) {
        int w = bgImage.getWidth();
        int h = bgImage.getHeight();
        BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImg.createGraphics();

// Clear the image (optional)
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, w, h);

// Draw the background image
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(bgImage, 0, 0, null);

// Draw the overlay image
        g.setComposite(AlphaComposite.SrcOver.derive((float) alpha));
        g.drawImage(fgImage, 0, 0, null);

        g.dispose();
        return newImg;
    }

    public static BufferedImage overlayImage(BufferedImage bgImage, BufferedImage fgImage, Point p, float alpha) {
        BufferedImage combinedImage = new BufferedImage(bgImage.getWidth(), bgImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combinedImage.createGraphics();
        g2d.drawImage(bgImage, 0, 0, null);
        g2d.drawImage(fgImage, p.x, p.y, null);
        g2d.dispose();
        return combinedImage;
    }

    public static BufferedImage drawText(BufferedImage bf, String str, int x, int y, Color col) {
        BufferedImage newImage = new BufferedImage(
                bf.getWidth(), bf.getHeight(),
                BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D gr = newImage.createGraphics();
        gr.drawImage(bf, 0, 0, null);
        gr.setColor(col);
        gr.drawString(str, x, y);
        gr.dispose();
        return newImage;
    }

    public static BufferedImage adaptiveThreshold(float[][] d, int t1, int t2) {
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (d[i][j] >= t1 && d[i][j] <= t2) {
                    d[i][j] = 255;
                } else {
                    d[i][j] = 0;
                }
            }
        }
        BufferedImage binarized = ImageProcess.pixelsToImageGray(d);
        return binarized;
    }

    public static BufferedImage thresholdGray(BufferedImage img, int t1, int t2) {
        float[][] d = imageToPixelsFloat(img);
        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[0].length; j++) {
                if (d[i][j] >= t1 && d[i][j] < t2) {
                    d[i][j] = 255;
                } else {
                    d[i][j] = 0;
                }
            }
        }
        BufferedImage binarized = ImageProcess.pixelsToImageGray(d);
        return binarized;
    }

    public static BufferedImage thresholdHSV(BufferedImage bf, int h1, int h2, int s1, int s2, int v1, int v2) {
        BufferedImage ret = null;
        int[][] dh = imageToPixelsInt(rgb2gray(getHueChannel(bf)));
        int[][] ds = imageToPixelsInt(rgb2gray(getSaturationChannel(bf)));
        int[][] dv = imageToPixelsInt(rgb2gray(getValueChannel(bf)));
        int nr = dh.length;
        int nc = dh[0].length;
        int[][] d = new int[nr][nc];
        int h, s, v;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                h = dh[i][j];
                s = ds[i][j];
                v = dv[i][j];
                if (h >= h1 && h <= h2 && s >= s1 && s <= s2 && v >= v1 && v <= v2) {
                    d[i][j] = 255;
                }
            }
        }
        ret = pixelsToImageGray(d);
        return ret;
    }

    public static BufferedImage ocv_img2hsv(BufferedImage bf) {
//        Mat frame = ImageProcess.ocv_img2Mat(bf);
//        Mat hsvImage = new Mat();
//        Imgproc.cvtColor(frame, hsvImage, Imgproc.COLOR_BGR2HSV);
//        BufferedImage img = ocv_mat2Img(hsvImage);
//        return img;
//        return ocv_rgb2hsv(bf);
//        return ocv_2_hsv(bf);
        return bf;
    }

    public static BufferedImage ocv_rgb2hsv(BufferedImage bf) {
//        Mat frame = ImageProcess.ocv_img2Mat(bf);
//        Mat hsvImage = new Mat();
//        // convert the frame to HSV
//        Imgproc.cvtColor(frame, hsvImage, Imgproc.COLOR_BGR2HSV);
//        BufferedImage img = ocv_mat2Img(hsvImage);
        //BufferedImage img = ocv_2_hsv(bf);
        return bf;
    }

    /*
    public static BufferedImage ocv_hsvThreshold(BufferedImage bf, int h1, int h2, int s1, int s2, int v1, int v2) {
        Mat frame = ImageProcess.ocv_img2Mat(bf);
//        Mat hsvImage = new Mat();
        Mat mask = new Mat();

        // convert the frame to HSV
//        Imgproc.cvtColor(frame, hsvImage, Imgproc.COLOR_BGR2HSV);
        // get thresholding values from the UI
        Scalar minValues = new Scalar(h1, s1, v1);
        Scalar maxValues = new Scalar(h2, s2, v2);

        Core.inRange(frame, minValues, maxValues, mask);
        BufferedImage img = ocv_mat2Img(mask);
        return img;
    }

    public static BufferedImage ocv_2_hsv(BufferedImage img) {
        Mat blurredImage = new Mat();
        Mat hsvImage = new Mat();
        Mat mask = new Mat();
        Mat morphOutput = new Mat();
        Mat frame = ImageProcess.ocv_img2Mat(img);

        // remove some noise
        Imgproc.blur(frame, blurredImage, new Size(7, 7));

        // convert the frame to HSV
        Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        // get thresholding values from the UI
        // remember: H ranges 0-180, S and V range 0-255
        Scalar minValues = new Scalar(0, 150, 150);
        Scalar maxValues = new Scalar(50, 255, 255);

        // show the current selected HSV range
//        String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0]
//                + "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: "
//                + minValues.val[2] + "-" + maxValues.val[2];
        // threshold HSV image to select tennis balls
        Core.inRange(hsvImage, minValues, maxValues, mask);
        BufferedImage bf = ImageProcess.ocv_mat2Img(hsvImage);
        return bf;
    }

    public static BufferedImage ocv_medianFilter(BufferedImage bf) {
        Mat frame = ImageProcess.ocv_img2Mat(bf);
        Mat blurredImage = new Mat();
        Imgproc.medianBlur(frame, blurredImage, 5);
        BufferedImage out = ocv_mat2Img(blurredImage);
        return out;
    }

    public static BufferedImage ocv_negativeImage(BufferedImage bf) {
        Mat frame = ImageProcess.ocv_img2Mat(bf);
        Mat negativeImage = new Mat();
        Core.bitwise_not(frame, negativeImage);
        BufferedImage out = ocv_mat2Img(negativeImage);
        return out;
    }

    public static BufferedImage ocv_cloneImage(BufferedImage bf) {
        Mat frame = ImageProcess.ocv_img2Mat(bf);
        Mat cloneImage = frame.clone();
        BufferedImage out = ocv_mat2Img(cloneImage);
        return out;
    }
     */
    public static BufferedImage cropBoundingBox(BufferedImage img) {
        BufferedImage bf = clone(img);
        int[][] d = imageToPixelsInt(img);
        int nr = d.length;
        int nc = d[0].length;
        int left = nc, right = 0;
        int top = nr, bottom = 0;

        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                if (d[i][j] > 0) {
                    if (left > j) {
                        left = j;
                    }
                    if (right < j) {
                        right = j;
                    }
                    if (top > i) {
                        top = i;
                    }
                    if (bottom < i) {
                        bottom = i;
                    }
                }
            }
        }
        bf = cropImage(bf, new CRectangle(top, left, right - left, bottom - top));
        return bf;
    }

    public static CPoint getCenterPoint(BufferedImage img) {
        CPoint cp = new CPoint();
        cp.row = img.getHeight() / 2;
        cp.column = img.getWidth() / 2;
        return cp;
    }

    public static BufferedImage changeQuantizationLevel(BufferedImage image, int n) {
        float[][] d = imageToPixelsFloat(image);
        int nr = d.length;
        int nc = d[0].length;

        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {

            }
        }
        return image;
    }

    public static BufferedImage equalizeHistogram(BufferedImage bi) {
        int width = bi.getWidth();
        int height = bi.getHeight();
        int anzpixel = width * height;
        int[] histogram = new int[256];
        int[] iarray = new int[1];
        int i = 0;

        //read pixel intensities into histogram
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int valueBefore = bi.getRaster().getPixel(x, y, iarray)[0];
                histogram[valueBefore]++;
            }
        }

        int sum = 0;
        // build a Lookup table LUT containing scale factor
        float[] lut = new float[anzpixel];
        for (i = 0; i < 256; ++i) {
            sum += histogram[i];
            lut[i] = sum * 255 / anzpixel;
        }

        // transform image using sum histogram as a Lookup table
        for (int x = 1; x < width; x++) {
            for (int y = 1; y < height; y++) {
                int valueBefore = bi.getRaster().getPixel(x, y, iarray)[0];
                int valueAfter = (int) lut[valueBefore];
                iarray[0] = valueAfter;
                bi.getRaster().setPixel(x, y, iarray);
            }
        }
        return bi;
    }

    /*
    public static float[] getHuMoments(BufferedImage img) {
        double[] moments = new double[7];
        Mat imagenOriginal;
        imagenOriginal = new Mat();
        Mat binario;
        binario = new Mat();
        Mat Canny;
        Canny = new Mat();

        imagenOriginal = ImageProcess.ocv_img2Mat(img);
        Mat gris = new Mat(imagenOriginal.width(), imagenOriginal.height(), imagenOriginal.type());
        Imgproc.cvtColor(imagenOriginal, gris, Imgproc.COLOR_RGB2GRAY);
        org.opencv.core.Size s = new Size(3, 3);
        Imgproc.GaussianBlur(gris, gris, s, 2);

        Imgproc.threshold(gris, binario, 100, 255, Imgproc.THRESH_BINARY);
        Imgproc.Canny(gris, Canny, 50, 50 * 3);

        java.util.List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        Mat hierarcy = new Mat();

        Imgproc.findContours(Canny, contours, hierarcy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.drawContours(Canny, contours, -1, new Scalar(Math.random() * 255, Math.random() * 255, Math.random() * 255));
        Moments p = new Moments();
        java.util.List<Moments> nu = new ArrayList<Moments>(contours.size());

        for (int i = 0; i < contours.size(); i++) {
            nu.add(i, Imgproc.moments(contours.get(i), false));
            p = nu.get(i);
        }
        double n20 = p.get_nu20(),
                n02 = p.get_nu02(),
                n30 = p.get_nu30(),
                n12 = p.get_nu12(),
                n21 = p.get_nu21(),
                n03 = p.get_nu03(),
                n11 = p.get_nu11();

        //First moment
        moments[0] = n20 + n02;

        //Second moment
        moments[1] = Math.pow((n20 - 02), 2) + Math.pow(2 * n11, 2);

        //Third moment
        moments[2] = Math.pow(n30 - (3 * (n12)), 2)
                + Math.pow((3 * n21 - n03), 2);

        //Fourth moment
        moments[3] = Math.pow((n30 + n12), 2) + Math.pow((n12 + n03), 2);

        //Fifth moment
        moments[4] = (n30 - 3 * n12) * (n30 + n12)
                * (Math.pow((n30 + n12), 2) - 3 * Math.pow((n21 + n03), 2))
                + (3 * n21 - n03) * (n21 + n03)
                * (3 * Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2));

        //Sixth moment
        moments[5] = (n20 - n02)
                * (Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2))
                + 4 * n11 * (n30 + n12) * (n21 + n03);

        //Seventh moment
        moments[6] = (3 * n21 - n03) * (n30 + n12)
                * (Math.pow((n30 + n12), 2) - 3 * Math.pow((n21 + n03), 2))
                + (n30 - 3 * n12) * (n21 + n03)
                * (3 * Math.pow((n30 + n12), 2) - Math.pow((n21 + n03), 2));

//        //Eighth moment
//        moments[7] = n11 * (Math.pow((n30 + n12), 2) - Math.pow((n03 + n21), 2))
//                - (n20 - n02) * (n30 + n12) * (n03 + n21);
        return FactoryUtils.toFloatArray1D(moments);
    }
     */
    public static int[][] rgb2lab(int[][] img) {
        int r = img.length;
        int c = img[0].length;
        int[][] ret = new int[r][c];
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                int val = img[i][j];
                Color col = new Color(val, true);
                int red = col.getRed();
                int green = col.getGreen();
                int blue = col.getBlue();
                ColorSpaceLAB lab = ColorSpaceLAB.fromRGB(red, green, blue, 255);
            }
        }
        return ret;
    }

    public static BufferedImage convertToBufferedImageTypes(BufferedImage img, int TYPE) {
        BufferedImage convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), TYPE);
        convertedImg.getGraphics().drawImage(img, 0, 0, null);
        return convertedImg;
    }

    public static List<BufferedImage> dataAugFlipHorizontal(List<BufferedImage> imgs, int n, double probability) {
        List<BufferedImage> ret = new ArrayList();
        Random k = new Random();
        for (int i = 0; i < n; i++) {
            if (Math.random() > probability) {
                ret.add(ImageProcess.flipHorizontal(ImageProcess.clone(imgs.get(k.nextInt(imgs.size())))));
            } else {
                ret.add(ImageProcess.clone(imgs.get(k.nextInt(imgs.size()))));
            }
        }
        return ret;
    }

    public static List<BufferedImage> dataAugFlipVertical(List<BufferedImage> imgs, int n, double probability) {
        List<BufferedImage> ret = new ArrayList();
        Random k = new Random();
        for (int i = 0; i < n; i++) {
            if (Math.random() > probability) {
                ret.add(ImageProcess.flipVertical(ImageProcess.clone(imgs.get(k.nextInt(imgs.size())))));
            } else {
                ret.add(ImageProcess.clone(imgs.get(k.nextInt(imgs.size()))));
            }
        }
        return ret;
    }

    public static List<BufferedImage> dataAugBrightnessRange(List<BufferedImage> imgs, int n, float[] range) {
        List<BufferedImage> ret = new ArrayList();
        Random k = new Random();
        ContrastFilter cf = new ContrastFilter();

        for (int i = 0; i < n; i++) {
            float d = (float) (range[0] + Math.random() * (range[1] - range[0]));
            cf.setBrightness(d);
            BufferedImage src = imgs.get(k.nextInt(imgs.size()));
            BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
            cf.filter(src, dst);
            ret.add(dst);
        }
        return ret;
    }

    public static List<BufferedImage> dataAugZoomRange(List<BufferedImage> imgs, int n, float[] range) {
        List<BufferedImage> ret = new ArrayList();
        Random k = new Random();
        BufferedImage src = ImageProcess.clone(imgs.get(k.nextInt(imgs.size())));
        int w = src.getWidth();
        int h = src.getHeight();
        for (int i = 0; i < n; i++) {
            float d = (float) (range[0] + Math.random() * (range[1] - range[0]));
            src = ImageProcess.clone(imgs.get(k.nextInt(imgs.size())));
            BufferedImage temp = ImageProcess.resizeAspectRatio(src, (int) (w * d), (int) (h * d));
            int w2 = temp.getWidth();
            int h2 = temp.getHeight();
            if (w2 > w && h2 > h) {
                temp = ImageProcess.cropImage(temp, new CRectangle((h2 - h) / 2, (w2 - w) / 2, w, h));
            } else {
                //temp = ImageProcess.overlayImage(src, temp, new CPoint((h - h2) / 2, (w - w2) / 2), 1);
                temp = ImageProcess.overlayImage(src, temp, (w - w2) / 2, (h - h2) / 2);
            }

            ret.add(temp);
        }
        return ret;
    }

    public static BufferedImage concatImage(BufferedImage img1, BufferedImage img2, String direction) {
        if (direction.equals("horizontal")) {
            if (img1.getHeight() != img2.getHeight()) {
                System.err.println("image heights are not identical");
                return null;
            }
            BufferedImage ret = new BufferedImage(img1.getWidth() + img2.getWidth(), img1.getHeight(), img1.getType());
            Graphics2D gr = ret.createGraphics();
            gr.drawImage(img1, 0, 0, null);
            gr.drawImage(img2, img1.getWidth(), 0, null);
            gr.dispose();
            return ret;
        } else if (direction.equals("vertical")) {
            if (img1.getWidth() != img2.getWidth()) {
                System.err.println("image widths are not identical");
                return null;
            }
            BufferedImage ret = new BufferedImage(img1.getWidth(), img1.getHeight() + img2.getHeight(), img1.getType());
            Graphics2D gr = ret.createGraphics();
            gr.drawImage(img1, 0, 0, null);
            gr.drawImage(img2, 0, img1.getHeight(), null);
            gr.dispose();
            return ret;
        }
        return null;
    }

    public static List<BufferedImage> dataAugWidthShiftRange(List<BufferedImage> imgs, int n, float[] range) {
        List<BufferedImage> ret = new ArrayList();
        Random k = new Random();
        BufferedImage src = ImageProcess.clone(imgs.get(k.nextInt(imgs.size())));
        int w = src.getWidth();
        int h = src.getHeight();
        for (int i = 0; i < n; i++) {
            int d = (int) (range[0] + Math.random() * (range[1] - range[0]));
            src = ImageProcess.clone(imgs.get(k.nextInt(imgs.size())));
            if (d > 0) { //means if you shift the image to right direction
                BufferedImage img1 = ImageProcess.cropImage(src, new CRectangle(0, 0, w - d, h));
                BufferedImage img2 = ImageProcess.cropImage(src, new CRectangle(0, 0, 1, h));
                BufferedImage temp = ImageProcess.cropImage(src, new CRectangle(0, 0, 1, h));
                for (int j = 0; j < d - 1; j++) {
                    temp = concatImage(temp, img2, "horizontal");
                }
                src = concatImage(temp, img1, "horizontal");
            } else if (d < 0) {
                BufferedImage img1 = ImageProcess.cropImage(src, new CRectangle(0, -d, w + d, h));
                BufferedImage img2 = ImageProcess.cropImage(src, new CRectangle(0, w - 1, 1, h));
                BufferedImage temp = ImageProcess.cropImage(src, new CRectangle(0, w - 1, 1, h));
                for (int j = 0; j < Math.abs(d) - 1; j++) {
                    temp = concatImage(temp, img2, "horizontal");
                }
                src = concatImage(img1, temp, "horizontal");
            }
            ret.add(src);
        }
        return ret;
    }

    public static List<BufferedImage> dataAugHeightShiftRange(List<BufferedImage> imgs, int n, float[] range) {
        List<BufferedImage> ret = new ArrayList();
        Random k = new Random();
        BufferedImage src = ImageProcess.clone(imgs.get(k.nextInt(imgs.size())));
        int w = src.getWidth();
        int h = src.getHeight();
        for (int i = 0; i < n; i++) {
            int d = (int) (range[0] + Math.random() * (range[1] - range[0]));
            src = ImageProcess.clone(imgs.get(k.nextInt(imgs.size())));
            if (d > 0) { //means if you shift the image to bottom direction
                BufferedImage img1 = ImageProcess.cropImage(src, new CRectangle(0, 0, w, h - d));
                BufferedImage img2 = ImageProcess.cropImage(src, new CRectangle(0, 0, w, 1));
                BufferedImage temp = ImageProcess.cropImage(src, new CRectangle(0, 0, w, 1));
                for (int j = 0; j < d - 1; j++) {
                    temp = concatImage(temp, img2, "vertical");
                }
                src = concatImage(temp, img1, "vertical");
            } else if (d < 0) {
                BufferedImage img1 = ImageProcess.cropImage(src, new CRectangle(-d, 0, w, h + d));
                BufferedImage img2 = ImageProcess.cropImage(src, new CRectangle(h - 1, 0, w, 1));
                BufferedImage temp = ImageProcess.cropImage(src, new CRectangle(h - 1, 0, w, 1));
                for (int j = 0; j < Math.abs(d) - 1; j++) {
                    temp = concatImage(temp, img2, "vertical");
                }
                src = concatImage(img1, temp, "vertical");
            }
            ret.add(src);
        }
        return ret;
    }

    public static BufferedImage adaptiveThresholdColorLimits(int r1, int r2, int g1, int g2, int b1, int b2, float[][][] argb) {
        int nr = argb[0].length;
        int nc = argb[0][0].length;
        float[][] ret = new float[nr][nc];
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                float red = argb[1][i][j];
                float green = argb[2][i][j];
                float blue = argb[3][i][j];
                if (red > r1 && red < r2 && green > g1 && green < g2 && blue > b1 && blue < b2) {
                    ret[i][j] = 255;
                }
            }
        }
        return ImageProcess.pixelsToImageGray(ret);
    }

    public static BufferedImage adaptiveThresholdColorAdaptive(int r, int dr, int g, int dg, int b, int db, float[][][] argb) {
        return adaptiveThresholdColorLimits(r - dr, r + dr, g - dg, g + dg, b - db, b + db, argb);
    }

    public static BufferedImage cropImageWithCentered224(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        return cropImage(img, (w - 224) / 2, (h - 224) / 2, 224, 224);
    }

    public static BufferedImage[][] tileImage(BufferedImage img, int nr, int nc) {
        BufferedImage[][] ret = new BufferedImage[nr][nc];
        int w = img.getWidth() / nc;
        int h = img.getHeight() / nr;
        for (int i = 0; i < nc; i++) {
            System.out.println("");
            for (int j = 0; j < nr; j++) {
                ret[j][i] = ImageProcess.cropImage(img, i * w, j * h, w, h);
                //System.out.println((i*w)+":"+(j*h)+":"+ret[i][j].getWidth()+":"+ret[i][j].getHeight()+"");
            }
        }
        return ret;
    }

    public static BufferedImage addNoise2D(BufferedImage image, float range) {
        float[][] data = imageToPixelsFloat(image);
        data = addNoise2D(data, range);
        return pixelsToImageGray(data);
    }

    public static float[][] addNoise2D(float[][] data, float range) {
        int nr = data.length;
        int nc = data[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                float n = (float) (new Random().nextGaussian() * range);
                data[i][j] = data[i][j] + n;
            }
        }
        return data;
    }

    public static BufferedImage addNoisePartial2D(BufferedImage image, float range, float probability) {
        float[][] data = imageToPixelsFloat(image);
        data = addNoisePartial2D(data, range, probability);
        return pixelsToImageGray(data);
    }

    public static float[][] addNoisePartial2D(float[][] data, float range, float probability) {
        int nr = data.length;
        int nc = data[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                if (Math.random() < probability) {
                    float n = (float) (new Random().nextGaussian() * range);
                    data[i][j] = data[i][j] + n;
                }
            }
        }
        return data;
    }

    public static BufferedImage addNoise3D(BufferedImage image, float range) {
        float[][][] data = imageToPixelsColorFloatFaster(image);
        data = addNoise3D(data, range);
        return pixelsToImageColor(data);
    }

    public static float[][][] addNoise3D(float[][][] data, float range) {
        int nch = data.length;
        int nr = data[0].length;
        int nc = data[0][0].length;
        for (int i = 1; i < nch; i++) {  //first element is alpha so skip it
            for (int j = 0; j < nr; j++) {
                for (int k = 0; k < nc; k++) {
                    float n = (float) (new Random().nextGaussian() * range);
                    data[i][j][k] = data[i][j][k] + n;
                }
            }
        }
        return data;
    }

    public static BufferedImage addNoisePartial3D(BufferedImage image, float range, float probability) {
        float[][][] data = imageToPixelsColorFloatFaster(image);
        data = addNoisePartial3D(data, range, probability);
        return pixelsToImageColor(data);
    }

    public static float[][][] addNoisePartial3D(float[][][] data, float range, float probability) {
        int nch = data.length;
        int nr = data[0].length;
        int nc = data[0][0].length;
        for (int i = 1; i < nch; i++) {  //first element is alpha so skip it
            for (int j = 0; j < nr; j++) {
                for (int k = 0; k < nc; k++) {
                    if (Math.random() < probability) {
                        float n = (float) (new Random().nextGaussian() * range);
                        data[i][j][k] = data[i][j][k] + n;
                    }
                }
            }
        }
        return data;
    }

    public static BufferedImage equalizeHistogramAdaptiveClahe(BufferedImage img) {
        if (!isOpenCVLoaded) {
            loadOpenCVLibrary();
        }
        AdaptiveConcurrentClahe clahe = new AdaptiveConcurrentClahe();
        BufferedImage ret = clahe.process(img);
        return ret;
    }

    public static void loadOpenCVLibrary() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        isOpenCVLoaded = true;
    }

    public static boolean isMaskImage(BufferedImage img) {
        BufferedImage copy = clone(img);
        copy = ImageProcess.rgb2gray(copy);
        float[][] df = ImageProcess.imageToPixelsFloat(copy);
        float[] d = FactoryUtils.toFloatArray1D(df);
        double[] dd = FactoryUtils.toDoubleArray1D(d);
//        int nr=d.length;
//        int nc=d[0].length;
//        for (int i = 0; i < nr; i++) {
//            for (int j = 0; j < nc; j++) {
//                if (!(d[i][j]==0 || d[i][j]==255)) {
//                    return false;
//                }
//            }
//        }
//        return true;
// 2. Functional API ile filtreleme
        return !Arrays.stream(dd).anyMatch(pixel -> pixel >= 0 && pixel <= 255);//allMatch(pixel -> pixel == 0 || pixel == 255);
    }

    public static boolean isMaskImage(float[][] data) {
        int nr = data.length;
        int nc = data[0].length;
        for (int i = 0; i < nr; i++) {
            for (int j = 0; j < nc; j++) {
                if (!(data[i][j] == 0 || data[i][j] == 255)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isBinarizedImage(BufferedImage img) {
        return isMaskImage(img);
    }

    public static boolean isBinarizedImage(float[][] data) {
        return isMaskImage(data);
    }

    /**
     * BufferedImage'i yatay veya dikey yönde aynalar
     *
     * @param originalImage aynalama yapılacak orijinal görüntü
     * @param horizontalFlip yatay aynalama yapılıp yapılmayacağı
     * @param verticalFlip dikey aynalama yapılıp yapılmayacağı
     * @return aynalanmış görüntü
     */
    public static BufferedImage mirrorImage(BufferedImage originalImage, boolean horizontalFlip, boolean verticalFlip) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Eğer hiçbir aynalama yapılmayacaksa orijinal görüntünün kopyasını döndür
        if (!horizontalFlip && !verticalFlip) {
            return deepCopy(originalImage);
        }

        // Yeni görüntü oluştur
        BufferedImage mirroredImage = new BufferedImage(width, height, originalImage.getType());

        // Grafik nesnesi oluştur
        Graphics2D g2d = mirroredImage.createGraphics();

        // Dönüşüm matrisini ayarla
        int x = horizontalFlip ? width : 0;
        int y = verticalFlip ? height : 0;
        int scaleX = horizontalFlip ? -1 : 1;
        int scaleY = verticalFlip ? -1 : 1;

        // AffineTransform kullanarak görüntüyü aynala
        AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.scale(scaleX, scaleY);

        g2d.setTransform(transform);
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        return mirroredImage;
    }

    /**
     * Sadece yatay aynalama yapar
     */
    public static BufferedImage mirrorHorizontal(BufferedImage originalImage) {
        return mirrorImage(originalImage, true, false);
    }

    /**
     * Sadece dikey aynalama yapar
     */
    public static BufferedImage mirrorVertical(BufferedImage originalImage) {
        return mirrorImage(originalImage, false, true);
    }

    /**
     * Görüntüyü mozaikleştirir (pikselleştirir)
     *
     * @param image Mozaikleştirilecek görüntü
     * @param blockSize Mozaik blok boyutu (pixel)
     * @return Mozaikleştirilmiş görüntü
     */
    public static BufferedImage mosaicImage(BufferedImage image, int blockSize) {
        if (blockSize <= 1) {
            return image; // Blok boyutu 1 veya daha küçükse orjinal görüntüyü döndür
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // Çıktı görüntüsünü oluştur
        BufferedImage output = new BufferedImage(width, height, image.getType());

        // Görüntüyü blok blok işle
        for (int y = 0; y < height; y += blockSize) {
            for (int x = 0; x < width; x += blockSize) {
                // Blok boyutlarını hesapla (görüntü kenarında eksik bloklar için)
                int blockWidth = Math.min(blockSize, width - x);
                int blockHeight = Math.min(blockSize, height - y);

                // Blok içindeki piksellerin renklerini topla
                int sumR = 0, sumG = 0, sumB = 0;
                int count = 0;

                for (int by = 0; by < blockHeight; by++) {
                    for (int bx = 0; bx < blockWidth; bx++) {
                        int rgb = image.getRGB(x + bx, y + by);
                        sumR += (rgb >> 16) & 0xff;
                        sumG += (rgb >> 8) & 0xff;
                        sumB += rgb & 0xff;
                        count++;
                    }
                }

                // Ortalama rengi hesapla
                int avgR = sumR / count;
                int avgG = sumG / count;
                int avgB = sumB / count;
                int avgRGB = (avgR << 16) | (avgG << 8) | avgB;

                // Bloğu ortalama renkle doldur
                for (int by = 0; by < blockHeight; by++) {
                    for (int bx = 0; bx < blockWidth; bx++) {
                        output.setRGB(x + bx, y + by, avgRGB);
                    }
                }
            }
        }

        return output;
    }

    /**
     * Görüntünün belirli bir bölgesini mozaikleştirir
     *
     * @param image Mozaikleştirilecek görüntü
     * @param roi Mozaikleştirilecek bölge (Rectangle)
     * @param blockSize Mozaik blok boyutu (pixel)
     * @return Belirli bölgesi mozaikleştirilmiş görüntü
     */
    public static BufferedImage mosaicRegion(BufferedImage image, Rectangle roi, int blockSize) {
        if (blockSize <= 1) {
            return image; // Blok boyutu 1 veya daha küçükse orjinal görüntüyü döndür
        }

        // Görüntü boyutlarını al
        int width = image.getWidth();
        int height = image.getHeight();

        // ROI'nin görüntü sınırları içinde olduğunu garantile
        int x = Math.max(0, roi.x);
        int y = Math.max(0, roi.y);
        int roiWidth = Math.min(roi.width, width - x);
        int roiHeight = Math.min(roi.height, height - y);

        if (roiWidth <= 0 || roiHeight <= 0) {
            return image; // Geçerli bir ROI değilse orijinal görüntüyü döndür
        }

        // Çıktı görüntüsünü oluştur (orijinal görüntünün kopyası)
        BufferedImage output = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // Sadece ROI bölgesini blok blok işle
        for (int blockY = y; blockY < y + roiHeight; blockY += blockSize) {
            for (int blockX = x; blockX < x + roiWidth; blockX += blockSize) {
                // Blok boyutlarını hesapla (ROI kenarında eksik bloklar için)
                int blockW = Math.min(blockSize, x + roiWidth - blockX);
                int blockH = Math.min(blockSize, y + roiHeight - blockY);

                // Blok içindeki piksellerin renklerini topla
                int sumR = 0, sumG = 0, sumB = 0;
                int count = 0;

                for (int by = 0; by < blockH; by++) {
                    for (int bx = 0; bx < blockW; bx++) {
                        int rgb = image.getRGB(blockX + bx, blockY + by);
                        sumR += (rgb >> 16) & 0xff;
                        sumG += (rgb >> 8) & 0xff;
                        sumB += rgb & 0xff;
                        count++;
                    }
                }

                // Ortalama rengi hesapla
                int avgR = sumR / count;
                int avgG = sumG / count;
                int avgB = sumB / count;
                int avgRGB = (avgR << 16) | (avgG << 8) | avgB;

                // Bloğu ortalama renkle doldur
                for (int by = 0; by < blockH; by++) {
                    for (int bx = 0; bx < blockW; bx++) {
                        output.setRGB(blockX + bx, blockY + by, avgRGB);
                    }
                }
            }
        }

        return output;
    }

    /**
     * Daha hızlı versiyonu - BufferedImage Graphics2D kullanarak (Büyük
     * resimler için daha verimli)
     *
     * @param image Mozaikleştirilecek görüntü
     * @param blockSize Mozaik blok boyutu (pixel)
     * @return Mozaikleştirilmiş görüntü
     */
    public static BufferedImage mosaicImageFast(BufferedImage image, int blockSize) {
        if (blockSize <= 1) {
            return image;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // Küçültülmüş görüntüyü oluştur
        int smallWidth = Math.max(1, width / blockSize);
        int smallHeight = Math.max(1, height / blockSize);

        // Görüntüyü küçült (her blok tek piksel olacak şekilde)
        BufferedImage smallImage = new BufferedImage(smallWidth, smallHeight, image.getType());
        Graphics2D g2d = smallImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, smallWidth, smallHeight, null);
        g2d.dispose();

        // Küçültülmüş görüntüyü orijinal boyuta geri büyüt (bu aşamada pikseller büyük bloklar halinde görünecek)
        BufferedImage output = new BufferedImage(width, height, image.getType());
        g2d = output.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(smallImage, 0, 0, width, height, null);
        g2d.dispose();

        return output;
    }

    /**
     * Yüzleri veya özel bölgeleri bulanıklaştırmak için mozaik uygular
     *
     * @param image Ana görüntü
     * @param regions Mozaiklenecek bölgeler listesi (Rectangle)
     * @param blockSize Mozaik blok boyutu
     * @return Bölgeleri mozaiklenmiş görüntü
     */
    public static BufferedImage mosaicRegions(BufferedImage image, List<Rectangle> regions, int blockSize) {
        // Orijinal görüntünün bir kopyasını oluştur
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // Her bir bölgeyi mozaikle
        for (Rectangle region : regions) {
            result = mosaicRegion(result, region, blockSize);
        }

        return result;
    }

    /**
     * Yüz bölgesini (veya herhangi bir bölgeyi) otomatik tespit edip
     * mozaikleyebilecek bir örnek metod NOT: Bu metod tam implementasyon
     * değildir, sadece örnek bir çerçevedir. Gerçek yüz tespiti için OpenCV
     * veya başka bir kütüphane kullanmanız gerekir.
     */
    public static BufferedImage mosaicFaces(BufferedImage image, int blockSize) {
        // TODO: Burada OpenCV veya başka bir yüz tespit algoritması kullanılmalı
        // Örnek olarak manuel bir bölge tanımlıyoruz
        List<Rectangle> faceRegions = new ArrayList<>();

        // Örnek: Görüntünün ortasında varsayımsal bir yüz bölgesi
        int faceWidth = image.getWidth() / 4;
        int faceHeight = image.getHeight() / 4;
        int faceX = (image.getWidth() - faceWidth) / 2;
        int faceY = (image.getHeight() - faceHeight) / 2;

        faceRegions.add(new Rectangle(faceX, faceY, faceWidth, faceHeight));

        return mosaicRegions(image, faceRegions, blockSize);
    }
}
