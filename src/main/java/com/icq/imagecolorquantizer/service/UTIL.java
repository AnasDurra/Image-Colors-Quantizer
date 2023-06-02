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

    public static Set<Color> extractColorPalette(BufferedImage image) {
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

    public static BufferedImage createIndexedImage(BufferedImage image) {
       Set<Color> quantizedColors = extractColorPalette(image);
        // Create a new IndexColorModel with the colors from the quantized image
        System.out.println(quantizedColors.size());
        byte[] reds = new byte[quantizedColors.size()];
        byte[] greens = new byte[quantizedColors.size()];
        byte[] blues = new byte[quantizedColors.size()];
        int idx = 0;
        for (Color i : quantizedColors) {
            int color = i.getRGB();
            reds[idx] = (byte) ((color >> 16) & 0xFF);
            greens[idx] = (byte) ((color >> 8) & 0xFF);
            blues[idx] = (byte) (color & 0xFF);
            idx++;
        }
        IndexColorModel colorModel = new IndexColorModel(8,quantizedColors.size(), reds, greens, blues);

        // Create a new BufferedImage with the same dimensions as the original image, but with the TYPE_BYTE_INDEXED image type and the new IndexColorModel
        BufferedImage indexedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, colorModel);

        // Draw the original image onto the new indexed image
        indexedImage.getGraphics().drawImage(image, 0, 0, null);
System.out.println(extractColorPalette(indexedImage).size());
        return indexedImage;
    }
}