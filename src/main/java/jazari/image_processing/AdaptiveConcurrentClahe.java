/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jazari.image_processing;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.stream.IntStream;
import static org.opencv.core.Core.BORDER_REFLECT_101;
import static org.opencv.core.Core.copyMakeBorder;

/**
 * Very fast Contrast Limited Adaptive Histogram Equalization implementation
 * based on OpenCV with option for adaptive check if image to process is
 * suitable for CLAHE. Implementation is threadsafe, fully multithreaded and
 * much faster than the original C++ port.
 * <p>
 * Benchmarks with 4000x6000 pixel images and default settings shows up to 2.66x
 * speed increase.
 *
 * @author JBellic Softworks
 */
public class AdaptiveConcurrentClahe {

    // ** Adaptive Check Settings **
    private static final int PIXEL_BRIGHTNESS_THRESHOLD = 60;
    private static final float DARK_PIXELS_PERCENTAGE_THRESHOLD = 0.33F;

    // ** Clahe Settings **
    private static final int CLAHE_CLIP_LIMIT = 2;
    private static final int CLAHE_BUFFER_SIZE = 256;
    private static final int CLAHE_TILES_X = 8;
    private static final int CLAHE_TILES_Y = 8;
    private static final boolean CLAHE_COLOR_OUTPUT = false;

    /**
     * Process image without adaptive suitability check.
     *
     * @param bufferedImage image to apply CLAHE
     * @return processed image
     */
    public BufferedImage process(BufferedImage bufferedImage) {
        return applyOnLuminance(bufferedImage);
    }

    /**
     * Process image with adaptive suitability check.
     *
     * @param bufferedImage image to analyse and apply CLAHE
     * @return processed image if suitable, else original image
     */
    public BufferedImage processAdaptive(BufferedImage bufferedImage) {
        if (suitableForClahe(bufferedImage)) {
            return applyOnLuminance(bufferedImage);
        }
        return bufferedImage;
    }

    /**
     * Checks if image is suitable for CLAHE by calculating amount of too dark
     * pixels. If pixel brightness value is lower than defined brightness
     * threshold, it will be marked as too dark. If percentage amount of overall
     * too dark pixels exceeds defined dark pixels threshold, check returns
     * true.
     *
     * @param bufferedImage image to analyse
     * @return true if image is too dark, else false
     */
    private boolean suitableForClahe(BufferedImage bufferedImage) {
        int[] brightnessHistogram = new int[256];

        for (int i = 0; i < bufferedImage.getHeight(); i++) {
            for (int j = 0; j < bufferedImage.getWidth(); j++) {
                int pixel = bufferedImage.getRGB(j, i);
                int r = red(pixel);
                int g = green(pixel);
                int b = blue(pixel);

                int brightness = (int) (0.2126F * r + 0.7152F * g + 0.0722F * b);
                brightnessHistogram[brightness]++;
            }
        }
        int allPixelsCount = bufferedImage.getWidth() * bufferedImage.getHeight();
        int darkPixelCount = countPixelsWithBrightnessLessThanThreshold(brightnessHistogram);

        return darkPixelCount > allPixelsCount * DARK_PIXELS_PERCENTAGE_THRESHOLD;
    }

    /**
     * Returns count of dark pixels in image.
     */
    private int countPixelsWithBrightnessLessThanThreshold(int[] brightnessHistogram) {
        int darkPixelCount = 0;
        for (int i = 0; i < PIXEL_BRIGHTNESS_THRESHOLD; i++) {
            darkPixelCount += brightnessHistogram[i];
        }
        return darkPixelCount;
    }

    /**
     * Returns the red component of a color int.
     */
    private int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Returns the green component of a color int.
     */
    private int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Returns the blue component of a color int.
     */
    private int blue(int color) {
        return color & 0xFF;
    }

    /**
     * Applies CLAHE on lab channel of image and converts back to BGR.
     */
    private BufferedImage applyOnLuminance(BufferedImage bufferedImage) {
        Mat image = bufferedImageToMat(bufferedImage);
        if (image.channels() == 1) {
            Mat result = apply(image);
            return mat2BufferedImage(result);
        }
        Mat channel = new Mat();
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2Lab);
        Core.extractChannel(image, channel, 0);
        channel = apply(channel);
        Core.insertChannel(channel, image, 0);
        channel.release();
        Imgproc.cvtColor(image, image, Imgproc.COLOR_Lab2BGR);
        return mat2BufferedImage(image);
    }

    /**
     * Returns converted BufferedImage from mat.
     */
    public static BufferedImage mat2BufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }

        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] buffer = new byte[bufferSize];
        mat.get(0, 0, buffer);

        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);

        return image;
    }

    /**
     * Returns converted Mat from BufferedImage.
     */
    private Mat bufferedImageToMat(BufferedImage image) {
        if (image.getType()==10) {
            Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, data);
            return mat;
        } else {
            Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, data);
            return mat;
        }
    }

    /**
     * Returns Mat with applied CLAHE.
     */
    private Mat apply(Mat src) {
        Mat dst = new Mat(src.size(), src.type());
        Mat lut = new Mat();
        lut.create(CLAHE_TILES_X * CLAHE_TILES_Y, CLAHE_BUFFER_SIZE, CvType.CV_8UC1);

        Size tileSize;
        Mat srcForLut;

        if (src.cols() % CLAHE_TILES_X == 0 && src.rows() % CLAHE_TILES_Y == 0) {
            tileSize = new Size((double) src.cols() / CLAHE_TILES_X, (double) src.rows() / CLAHE_TILES_Y);
            srcForLut = src;
            System.out.println("w:" + src.dump());
        } else {
            Mat srcExt = new Mat();
            copyMakeBorder(src, srcExt, 0, CLAHE_TILES_Y - (src.rows() % CLAHE_TILES_Y), 0, CLAHE_TILES_X - (src.cols() % CLAHE_TILES_X), BORDER_REFLECT_101);
            tileSize = new Size((double) srcExt.cols() / CLAHE_TILES_X, (double) srcExt.rows() / CLAHE_TILES_Y);
            srcForLut = srcExt;
            //srcExt.release();
        }

        double tileSizeTotal = tileSize.area();
        float lutScale = (float) ((CLAHE_BUFFER_SIZE - 1) / tileSizeTotal);
        int clipLimit = (int) (CLAHE_CLIP_LIMIT * tileSizeTotal / CLAHE_BUFFER_SIZE);

        calcLutBodyParallel(new Range(0, CLAHE_TILES_X * CLAHE_TILES_Y), srcForLut, lut, tileSize, clipLimit, lutScale);
        calculateInterpolationBodyParallel(new Range(0, src.rows()), src, dst, lut, tileSize);

        lut.release();
        srcForLut.release();

        return dst;
    }

    /**
     * Calculates Intepolation Body concurrently for CLAHE.
     */
    private void calculateInterpolationBodyParallel(Range range, Mat src, Mat dst, Mat lut, Size tileSize) {
        int lutStep = (int) lut.step1();
        int lutBreak = CLAHE_TILES_X * lutStep;
        IntStream.range(range.start, range.end).parallel().forEach(i -> calculateInterpolationBody(i, lutStep, lutBreak, src, dst, lut, tileSize));
    }

    /**
     * Intepolation Body calculation algorithm.
     */
    private void calculateInterpolationBody(int y, int lutStep, int lutBreak, Mat src, Mat dst, Mat lut, Size tileSize) {
        float tyf = (y / (float) tileSize.height) - 0.5f;
        int ty1 = (int) Math.floor(tyf);
        int ty2 = ty1 + 1;
        float ya = tyf - ty1;

        // keep largest
        if (ty1 < 0) {
            ty1 = 0;
        }

        // keep smallest
        if (ty2 > CLAHE_TILES_Y - 1) {
            ty2 = CLAHE_TILES_Y - 1;
        }

        for (int x = 0; x < src.cols(); x++) {

            float txf = (x / (float) tileSize.width) - 0.5f;
            int tx1 = (int) Math.floor(txf);
            int tx2 = tx1 + 1;

            // keep largest
            float xa = txf - tx1;
            if (tx1 < 0) {
                tx1 = 0;
            }

            // keep smallest
            if (tx2 > CLAHE_TILES_X - 1) {
                tx2 = CLAHE_TILES_X - 1;
            }

            // original pixel value
            double[] ptr = src.get(y, x);
            int srcVal = (int) ptr[0];

            int ind1 = tx1 * lutStep + srcVal;
            int ind2 = tx2 * lutStep + srcVal;

            int column1 = (ind1 + (ty1 * lutBreak)) % lutStep;
            int row1 = (ind1 + (ty1 * lutBreak)) / lutStep;

            int column2 = (ind2 + (ty1 * lutBreak)) % lutStep;
            int row2 = (ind2 + (ty1 * lutBreak)) / lutStep;

            int column3 = (ind1 + (ty2 * lutBreak)) % lutStep;
            int row3 = (ind1 + (ty2 * lutBreak)) / lutStep;

            int column4 = (ind2 + (ty2 * lutBreak)) % lutStep;
            int row4 = (ind2 + (ty2 * lutBreak)) / lutStep;

            float res = 0;

            double[] lutPtr1 = lut.get(row1, column1);
            res += ((byte) lutPtr1[0] & 0xFF) * ((1.0f - xa) * (1.0f - ya));

            double[] lutPtr2 = lut.get(row2, column2);
            res += ((byte) lutPtr2[0] & 0xFF) * ((xa) * (1.0f - ya));

            double[] lutPtr3 = lut.get(row3, column3);
            res += ((byte) lutPtr3[0] & 0xFF) * ((1.0f - xa) * (ya));

            double[] lutPtr4 = lut.get(row4, column4);
            res += ((byte) lutPtr4[0] & 0xFF) * ((xa) * (ya));

            dst.put(y, x, (int) (res > CLAHE_BUFFER_SIZE - 1 ? CLAHE_BUFFER_SIZE - 1 : (res < 0 ? 0 : res)));
        }
    }

    /**
     * Calculates Lut Body concurrently for CLAHE.
     */
    private void calcLutBodyParallel(Range range, Mat src, Mat lut, Size tileSize, int clipLimit, float lutScale) {
        IntStream.range(range.start, range.end).parallel().forEach(e -> calcLutBody(e, src, lut, tileSize, clipLimit, lutScale));
    }

    /**
     * Lut Body calculation algorithm.
     */
    private void calcLutBody(int k, Mat src, Mat lut, Size tileSize, int clipLimit, float lutScale) {
        int ty = k / CLAHE_TILES_X;
        int tx = k % CLAHE_TILES_X;

        // retrieve tile subMatrix
        Rect tileROI = new Rect();
        tileROI.x = (int) (tx * tileSize.width);
        tileROI.y = (int) (ty * tileSize.height);
        tileROI.width = (int) tileSize.width;
        tileROI.height = (int) tileSize.height;
        Mat tile = src.submat(tileROI);

        // calculate histogram
        int[] tileHist = new int[CLAHE_BUFFER_SIZE];
        int height = tileROI.height;

        for (int h = height; h > 0; h--) {
            double[] ptr;
            for (int w = 0; w < tileROI.width; w++) {
                ptr = tile.get(h - 1, w);
                tileHist[(int) ptr[0]]++;
            }
        }
        tile.release();

        // clip histogram
        if (clipLimit > 0) {
            int clipped = 0;
            for (int i = 0; i < CLAHE_BUFFER_SIZE; ++i) {
                if (tileHist[i] > clipLimit) {
                    clipped += tileHist[i] - clipLimit;
                    tileHist[i] = clipLimit;
                }
            }

            // redistribute clipped pixels
            int redistBatch = clipped / CLAHE_BUFFER_SIZE;
            int residual = clipped - redistBatch * CLAHE_BUFFER_SIZE;

            for (int i = 0; i < CLAHE_BUFFER_SIZE; ++i) {
                tileHist[i] += redistBatch;
            }
            for (int i = 0; i < residual; ++i) {
                tileHist[i]++;
            }
        }

        // calculate lut
        int sum = 0;
        for (int i = 0; i < CLAHE_BUFFER_SIZE; ++i) {
            sum += tileHist[i];

            int x = Math.round(sum * lutScale);
            lut.put(k, i, x > CLAHE_BUFFER_SIZE - 1 ? CLAHE_BUFFER_SIZE - 1 : (x < 0 ? 0 : x));
        }
    }
}
