package com.icq.imagecolorquantizer.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.util.HashSet;
import java.util.Set;

public class UTIL {

    public static double getColorDistance(Color c1, Color c2) {
        double rDiff = c1.getRed() - c2.getRed();
        double gDiff = c1.getGreen() - c2.getGreen();
        double bDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }

    public static Set<Color> extractColorPalette(BufferedImage image){
        // Get the dimensions of the image
        int width = image.getWidth();
        int height = image.getHeight();

        Set<Color> colorPalette = new HashSet<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the color at the current pixel
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb);
                colorPalette.add(color);
            }
        }
        return colorPalette;
    }

    public static BufferedImage createIndexedImage(BufferedImage inputImage, Color[] quantizedColors, int numColors) {
        // Create a new BufferedImage object with the same dimensions as the input image

        // Draw the input image onto the BufferedImage object
        Graphics2D g2d = inputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, null);
        g2d.dispose();

        // Convert the Color array to an int array
        int[] quantizedColorsInt = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            quantizedColorsInt[i] = quantizedColors[i].getRGB();
        }

        // Create a new IndexColorModel object with the quantized colors
        IndexColorModel colorModel = new IndexColorModel(8, numColors, quantizedColorsInt, 0, false, -1, DataBuffer.TYPE_BYTE);

        // Create a new BufferedImage object with the specified colormodel
        BufferedImage outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, colorModel);

        // Copy the pixel data from the input image to the output image
        outputImage.getGraphics().drawImage(inputImage, 0, 0, null);

//        // Print the colors in the indexed image
//        for (int i = 0; i < numColors; i++) {
//            int color = colorModel.getRGB(i);
//            System.out.printf("Color %d: R=%d, G=%d, B=%d\n", i, (color >> 16)& 0xFF, (color >> 8) & 0xFF, color & 0xFF);
//        }

        return outputImage;
    }
}