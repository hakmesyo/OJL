/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.ArrayList;
import java.util.List;
import jazari.matrix.CMatrix;

/**
 *
 * @author cezerilab
 */
public class TestSuperResolutionV2 {

    public static void main(String[] args) {
// Load a low-resolution image
        CMatrix lowRes = CMatrix.getInstance()
                .imread("images/fox.png")
                //.rgb2gray()
                .imshow("Low Resolution Image");

// Scale factor for super-resolution
        int scaleFactor = 4;

// Create initial high-resolution image using bicubic interpolation
        CMatrix initialHighRes = lowRes.clone()
                .imresize(lowRes.getColumnNumber() * scaleFactor,
                        lowRes.getRowNumber() * scaleFactor)
                .imshow("Initial Bicubic Upscaled");

// Parameters for patch-based super-resolution
        int lowPatchSize = 5;
        int highPatchSize = lowPatchSize * scaleFactor;
        int overlap = 2;
        int lowStep = lowPatchSize - overlap;
        int highStep = lowStep * scaleFactor;

// Create a training set from the image itself (self-similarity)
// In a real implementation, you might use an external database of patches
        List<CMatrix> lowPatches = new ArrayList<>();
        List<CMatrix> highPatches = new ArrayList<>();

// Extract training patches from different scales of the original image
        CMatrix trainLow = lowRes.clone()
                .imresize((int) (lowRes.getColumnNumber() * 0.5f), (int) (lowRes.getRowNumber() * 0.5f));

        CMatrix trainHigh = lowRes.clone(); // Original becomes the high-res for training

// Extract patch pairs
        for (int y = 0; y < trainLow.getRowNumber() - lowPatchSize; y += lowStep) {
            for (int x = 0; x < trainLow.getColumnNumber() - lowPatchSize; x += lowStep) {
                // Extract low-res patch
                CMatrix lowPatch = trainLow.clone().cmd(
                        y + ":" + (y + lowPatchSize),
                        x + ":" + (x + lowPatchSize)
                );

                // Extract corresponding high-res patch (at 2x scale)
                int hy = y * 2;
                int hx = x * 2;
                if (hy + highPatchSize <= trainHigh.getRowNumber()
                        && hx + highPatchSize <= trainHigh.getColumnNumber()) {

                    CMatrix highPatch = trainHigh.clone().cmd(
                            hy + ":" + (hy + highPatchSize),
                            hx + ":" + (hx + highPatchSize)
                    );

                    lowPatches.add(lowPatch);
                    highPatches.add(highPatch);
                }
            }
        }

        System.out.println("Collected " + lowPatches.size() + " training patch pairs");

// Create the final high-resolution image
        CMatrix superResImage = initialHighRes.clone();

// Process each patch in the low-resolution input
        for (int y = 0; y < lowRes.getRowNumber() - lowPatchSize; y += lowStep) {
            for (int x = 0; x < lowRes.getColumnNumber() - lowPatchSize; x += lowStep) {
                // Extract query patch from low-res image
                CMatrix queryPatch = lowRes.clone().cmd(
                        y + ":" + (y + lowPatchSize),
                        x + ":" + (x + lowPatchSize)
                );

                // Find the most similar patch in the training set
                int bestMatch = -1;
                float minDistance = Float.MAX_VALUE;

                for (int i = 0; i < lowPatches.size(); i++) {
                    CMatrix diff = queryPatch.clone().minus(lowPatches.get(i));
                    float distance = diff.clone().pow(2).sumTotal();

                    if (distance < minDistance) {
                        minDistance = distance;
                        bestMatch = i;
                    }
                }

                // If we found a match, replace the patch in the high-res image
                if (bestMatch != -1) {
                    // Coordinates in high-res image
                    int hy = y * scaleFactor;
                    int hx = x * scaleFactor;

                    // High-res patch dimensions
                    int patchHeight = Math.min(highPatchSize, superResImage.getRowNumber() - hy);
                    int patchWidth = Math.min(highPatchSize, superResImage.getColumnNumber() - hx);

                    // Extract corresponding high-res patch from the match
                    CMatrix highPatch = highPatches.get(bestMatch).clone().cmd(
                            "0:" + patchHeight,
                            "0:" + patchWidth
                    );

                    // Replace the patch in the super-res image
                    for (int py = 0; py < patchHeight; py++) {
                        for (int px = 0; px < patchWidth; px++) {
                            superResImage.setValue(hy + py, hx + px, highPatch.getValue(py, px));
                        }
                    }
                }
            }
        }

// Display the final super-resolution result
        superResImage.imshow("Patch-based Super Resolution");

// If a ground truth high-resolution image is available, evaluate quality
        CMatrix groundTruth = CMatrix.getInstance()
                .imread("images/fox_1.png")
                //.rgb2gray()
                .imshow("Ground Truth (if available)");

// Calculate PSNR improvement if ground truth is available
        if (groundTruth != null) {
            CMatrix bicubicError = groundTruth.clone().minus(initialHighRes).pow(2);
            float bicubicMSE = bicubicError.meanTotal();
            float bicubicPSNR = (float) (10 * Math.log10(255 * 255 / bicubicMSE));

            CMatrix superResError = groundTruth.clone().minus(superResImage).pow(2);
            float superResMSE = superResError.meanTotal();
            float superResPSNR = (float) (10 * Math.log10(255 * 255 / superResMSE));

            System.out.println("Bicubic upscaling PSNR: " + bicubicPSNR + " dB");
            System.out.println("Super-resolution PSNR: " + superResPSNR + " dB");
            System.out.println("Improvement: " + (superResPSNR - bicubicPSNR) + " dB");
        }

    }

}
