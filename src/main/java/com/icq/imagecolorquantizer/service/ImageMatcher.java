package com.icq.imagecolorquantizer.service;

import com.icq.imagecolorquantizer.utils.ImageSearcher;
import com.icq.imagecolorquantizer.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

import static com.icq.imagecolorquantizer.service.UTIL.extractColorPalette;

/**
 * This class is used to compare an image
 * with a set of images based on the selected
 * colors from the input image color palette.
 */
public class ImageMatcher {

    /*
     * this function is used to load the images
     * from the given folders list, and only return
     * the list of images that have the same
     * date or size that equal to LocalDate and Size(KB) parameters.
     */
    public static List<BufferedImage> loadMatchingImages(int size, LocalDate date, List<File> foldersList) {

        // initialize an empty set of images
        List<BufferedImage> images = new ArrayList<>();

        // iterate over the folder list
        for (File folder : foldersList) {

            // get the list of images from the folder
            List<File> files = ImageSearcher.filterImages(folder, date, size);

            // iterate over the image list
            for (File file : files) {
                // read the image
                BufferedImage image = ImageUtils.loadImageFromPath(file.getAbsolutePath());

                // check if the image is indexed
                if (isIndexedImage(image)) {
                    // add the image to the image list
                    images.add(image);
                }

            }

        }
        return images;
    }

    /*
     * this function is used to extract the list of colorPalette
     * for the given list of buffered image, and return a Map<BufferedImage, Set<Color>>
     * of colors for each image.
     */
//    public static Map<BufferedImage, Set<Color>> extractColorPalette(List<BufferedImage> images) {
//        Map<BufferedImage, Set<Color>> colorPaletteMap = new HashMap<>();
//        for (BufferedImage image : images) {
//            Set<Color> colorPalette = UTIL.extractColorPalette(image);
//            colorPaletteMap.put(image, colorPalette);
//        }
//        return colorPaletteMap;
//    }

    /*
     * this function is used to extract the list of colorPalette
     * that contains the given color by 70% or more for the given
     * list of colorPalette.
     */
    public static List<BufferedImage> extractMatchingColorPalette(Map<BufferedImage, Set<Color>> entries) {
        List<BufferedImage> matchingColorPaletteList = new ArrayList<>();
        entries.forEach((image, colorPalette) -> {

            if (containsColor(getDominantColor(image), colorPalette)) {
                matchingColorPaletteList.add(image);
            }
        });
        return matchingColorPaletteList;
    }

    private static Color getDominantColor(BufferedImage image) {
        if (image == null) return null;

        // get the image width and height
        int width = image.getWidth();
        int height = image.getHeight();

        // get the RGB values of the image
        int[] rgb = image.getRGB(0, 0, width, height, null, 0, width);

        // initialize the RGB values
        int r = 0;
        int g = 0;
        int b = 0;

        // iterate over the RGB values
        for (int color : rgb) {
            // get the RGB value
            // to get the red, green, and blue values
            r += (color >> 16) & 0xFF;
            g += (color >> 8) & 0xFF;
            b += color & 0xFF;
        }

        // calculate the average of the RGB values
        r /= rgb.length;
        g /= rgb.length;
        b /= rgb.length;

        // return the dominant color
        return new Color(r, g, b);
    }

    private static boolean containsColor(Color color, Set<Color> colorPalette) {
        if (color == null || colorPalette == null) return false;

        // compare if the colors are similar by 70% or more
        for (Color c : colorPalette) {
            if (isSimilarColor(color, c)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSimilarColor(Color color1, Color color2) {
        if (color1 == null || color2 == null) return false;

        // get the RGB values of the given colors
        int r1 = color1.getRed();
        int g1 = color1.getGreen();
        int b1 = color1.getBlue();

        int r2 = color2.getRed();
        int g2 = color2.getGreen();
        int b2 = color2.getBlue();

        // calculate the difference between the RGB values
        int rDiff = Math.abs(r1 - r2);
        int gDiff = Math.abs(g1 - g2);
        int bDiff = Math.abs(b1 - b2);

        // calculate the average difference
        int avgDiff = (rDiff + gDiff + bDiff) / 3;

        // check if the average difference is less than 30
        return avgDiff < 30;
    }

    private static boolean isIndexedImage(BufferedImage image) {
        if (image == null) return false;
        return image.getType() == BufferedImage.TYPE_BYTE_INDEXED;
    }

    public static Map<BufferedImage, Double> searchForImage(Set<Color> colorSet, List<BufferedImage> images, double threshold) {
        return searchByImage(colorSet, images, threshold);
    }

    private static Map<BufferedImage, Double> searchByImage(Set<Color> colorSet, List<BufferedImage> images, double threshold) {

        Map<BufferedImage, Double> similarImages = new HashMap<>();

        for (BufferedImage image : images) {

            // Calculate the similarity score
            double result = compareColorPalettes(colorSet, extractColorPalette(image), threshold);
            if (result >= 51) {
                System.out.println("imagePath= " + image);
                System.out.println("similarity ratio= " + result + "\n");
                similarImages.put(image, result);
            }

        }

        System.out.println("Total similar images found: " + similarImages.size());
        return similarImages;
    }
    //! DON'T REMOVE THIS CODE

    // Compare similarity between color palettes using Euclidean distance
//    private static double compareColorPalettes(Set<Color> palette1, Set<Color> palette2) {
//        double similarityThreshold = 18;
//        int similarColors = 0;
//
//        for (Color color1 : palette1) {
//            double minDistance = Double.MAX_VALUE;
//
//            for (Color color2 : palette2) {
//                // Calculate Euclidean distance between RGB values
//                int rDiff = color1.getRed() - color2.getRed();
//                int gDiff = color1.getGreen() - color2.getGreen();
//                int bDiff = color1.getBlue() - color2.getBlue();
//
//                double distance = Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
//
//                if (distance < minDistance) {
//                    minDistance = distance;
//                }
//            }
//
//            if (minDistance <= similarityThreshold) {
//                similarColors++;
//            }
//        }
//
//        // Calculate the similarity score as a percentage
//        return (double) similarColors / palette1.size() * 100.0;
//    }

    static double compareColorPalettes(Set<Color> palette1, Set<Color> palette2, double threshold) {
        int similarColors = 0;

        for (Color color1 : palette1) {
            double minDistance = Double.MAX_VALUE;

            for (Color color2 : palette2) {
                // Convert the RGB values to Lab values
                double[] lab1 = rgbToLab(color1.getRed(), color1.getGreen(), color1.getBlue());
                double[] lab2 = rgbToLab(color2.getRed(), color2.getGreen(), color2.getBlue());

                // Calculate the CIEDE2000 color difference between the two Lab values
                double distance = ciede2000(lab1[0], lab1[1], lab1[2], lab2[0], lab2[1], lab2[2]);

                if (distance < minDistance) {
                    minDistance = distance;
                }
            }

            if (minDistance <= threshold) {
                similarColors++;
            }
        }

        // Calculate the similarity score as a percentage
        return (double) similarColors / palette1.size() * 100.0;
    }

    /**
     * this method converts RGB values to Lab values
     * where Lab is a color-opponent space with dimension L for lightness
     * and a and b for the color-opponent dimensions,
     * based on non-linearly compressed CIE XYZ color space coordinates.
     */
    private static double[] rgbToLab(double R, double G, double B) {
        // Normalize RGB values to [0, 1] double R = r / 255.0; double G = g / 255.0; double B = b / 255.0;

        // Apply inverse sRGB gamma correction double
        R = R <= 0.04045 ? R / 12.92 : Math.pow((R + 0.055) / 1.055, 2.4);
        G = G <= 0.04045 ? G / 12.92 : Math.pow((G + 0.055) / 1.055, 2.4);
        B = B <= 0.04045 ? B / 12.92 : Math.pow((B + 0.055) / 1.055, 2.4);

        // Convert RGB to XYZ using D65 standard illuminant
        double X = 0.4124 * R + 0.3576 * G + 0.1805 * B;
        double Y = 0.2126 * R + 0.7152 * G + 0.0722 * B;
        double Z = 0.0193 * R + 0.1192 * G + 0.9505 * B;

        // Normalize XYZ values to [0, 100]
        X *= 100;
        Y *= 100;
        Z *= 100;

        // Convert XYZ to Lab
        X /= 95.047; // Xn
        Y /= 100; // Yn
        Z /= 108.883; // Zn

        X = X > Math.pow((double) 6 / 29, 3) ? Math.cbrt(X) : (X * ((double) 29 / 6) * ((double) 29 / 6) + ((double) 4 / 29));
        Y = Y > Math.pow((double) 6 / 29, 3) ? Math.cbrt(Y) : (Y * ((double) 29 / 6) * ((double) 29 / 6) + ((double) 4 / 29));
        Z = Z > Math.pow((double) 6 / 29, 3) ? Math.cbrt(Z) : (Z * ((double) 29 / 6) * ((double) 29 / 6) + ((double) 4 / 29));

        double L = (116 * Y) - 16;
        double a = 500 * (X - Y);
        double b = 200 * (Y - Z);

        return new double[]{L, a, b};
    }


    /**
     * This method calculates the CIEDE2000 color difference between two Lab values
     * where CIEDE2000 is a color-difference formula based on CIELAB delta E*00.
     * It has been proposed as a more robust metric than delta E*94.
     * The measure incorporates the corrections from CIE94 (LCh) and takes into account
     * perceptual non-uniformities.
     */
    private static double ciede2000(double L1, double a1, double b1, double L2, double a2, double b2) {
        // Calculate Cprime1, Cprime2, Cabbar
        double Cstar1ab = Math.sqrt(a1 * a1 + b1 * b1);
        double Cstar2ab = Math.sqrt(a2 * a2 + b2 * b2);
        double Cstarabmean = (Cstar1ab + Cstar2ab) / 2;

        double G = 0.5 * (1 - Math.sqrt(Math.pow(Cstarabmean, 7) / (Math.pow(Cstarabmean, 7) + Math.pow(25, 7))));

        double aprime1 = (1 + G) * a1;
        double aprime2 = (1 + G) * a2;

        double Cprime1 = Math.sqrt(aprime1 * aprime1 + b1 * b1);
        double Cprime2 = Math.sqrt(aprime2 * aprime2 + b2 * b2);

        // Calculate hprime1, hprime2
        double hprime1 = Math.atan2(b1, aprime1);
        if (hprime1 < 0) {
            hprime1 += 2 * Math.PI;
        }

        double hprime2 = Math.atan2(b2, aprime2);
        if (hprime2 < 0) {
            hprime2 += 2 * Math.PI;
        }

        // Calculate dLprime, dCprime, dHprime
        double dLprime = L2 - L1;
        double dCprime = Cprime2 - Cprime1;

        double dHprime = 2 * Math.sqrt(Cprime1 * Cprime2) * Math.sin((hprime2 - hprime1) / 2);

        // Calculate CIEDE2000 color difference
        double LprimeMean = (L1 + L2) / 2;
        double CprimeMean = (Cprime1 + Cprime2) / 2;

        // Calculate hprimeMean differently depending on the chroma values
        double hprimeMean;
        if (Math.abs(hprime1 - hprime2) > Math.PI) {
            hprimeMean = (hprime1 + hprime2 + 2 * Math.PI) / 2;
        } else if (Cprime1 * Cprime2 == 0) {
            hprimeMean = hprime1 + hprime2;
        } else {
            hprimeMean = (hprime1 + hprime2) / 2;
        }

        double LprimeMeanMinus50squared = Math.pow(LprimeMean - 50, 2);
        double S_L = 1 + ((0.015 * LprimeMeanMinus50squared) / Math.sqrt(20 + LprimeMeanMinus50squared));
        double S_C = 1 + 0.045 * CprimeMean;
        double T = 1 - 0.17 * Math.cos(hprimeMean - Math.PI / 6) + 0.24 * Math.cos(2 * hprimeMean) + 0.32 * Math.cos(3 * hprimeMean + Math.PI / 30) - 0.2 * Math.cos(4 * hprimeMean - 63 * Math.PI / 180);
        double S_H = 1 + 0.015 * T * CprimeMean;
        double hprimeMeanMinus275div25squared = Math.pow(hprimeMean - 275 * Math.PI / 180, 2) / Math.pow(25 * Math.PI / 180, 2);
        double deltaTheta = 30 * Math.exp(-hprimeMeanMinus275div25squared);
        double CprimeMean7 = Math.pow(CprimeMean, 7);
        double R_C = 2 * Math.sqrt(CprimeMean7 / (CprimeMean7 + Math.pow(25, 7)));
        double R_T = -Math.sin(2 * deltaTheta) * R_C;

        return Math.sqrt(Math.pow(dLprime / (S_L * 1), 2) + Math.pow(dCprime / (S_C * 1), 2) + Math.pow(dHprime / (S_H * 1), 2) + R_T * (dCprime / (S_C * 1)) * (dHprime / (S_H * 1)));
    }
}
