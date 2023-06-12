package com.icq.imagecolorquantizer.service;

import com.icq.imagecolorquantizer.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
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

            // get the list of images in the current folder
            List<File> files = getFiles(folder);

            // iterate over the image list
            for (File file : files) {

                if (size == -1 || date == null) {
                    BufferedImage image = ImageUtils.loadImageFromPath(file.getAbsolutePath());

                    if (isIndexedImage(image)) {
                        images.add(image);
                    }
                    continue;
                }

                // get the image size in KB
                int imageSize = (int) (file.length() / 1024);

                // get the image date in milliseconds
                long imageDate = getImageDate(file);

                // compare the LocalDate with FileTime
                LocalDate localDate = dateFromMillis(imageDate);

                // check if the image size and date are equal to the given parameters
                if (imageSize == size && localDate.toEpochDay() == date.toEpochDay()) {

                    BufferedImage image = ImageUtils.loadImageFromPath(file.getAbsolutePath());

                    if (isIndexedImage(image)) {
                        images.add(image);
                    }
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

    private static List<File> getFiles(File folder) {
        List<File> files = new ArrayList<>();
        if (!folder.isDirectory()) return files;
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile() && isImageFile(file)) {
                files.add(file);
            }
        }
        return files;
    }

    private static boolean isImageFile(File file) {
        if (file == null) return false;
        String name = file.getName();
        String extension = name.substring(name.lastIndexOf(".") + 1);
        return extension.equalsIgnoreCase("gif") ||
                extension.equalsIgnoreCase("png");
    }

    private static boolean isIndexedImage(BufferedImage image) {
        if (image == null) return false;
        return image.getType() == BufferedImage.TYPE_BYTE_INDEXED;
    }

    private static long getImageDate(File file) {
        // read the basic file attributes
        try {
            // return the creation date in milliseconds
            FileTime fileTime = Files.getLastModifiedTime(file.toPath());
            return fileTime.toMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static LocalDate dateFromMillis(long millis) {
        return LocalDate.ofEpochDay(millis);
    }

    public static List<BufferedImage> searchForImage(BufferedImage queryImage, List<BufferedImage> images) {
        return searchByImage(queryImage, images);
    }

    private static List<BufferedImage> searchByImage(BufferedImage queryImage, List<BufferedImage> images) {

        List<BufferedImage> similarImages = new ArrayList<>();

        for (BufferedImage image : images) {

            // Calculate the similarity score
            double result = compareColorPalettes(extractColorPalette(queryImage), extractColorPalette(image));
            if (result >= 60) {
                System.out.println("imagePath= " + image);
                System.out.println("similarity ratio= " + result + "\n");
                similarImages.add(image);
            }

        }

        System.out.println("Total similar images found: " + similarImages.size());
        return similarImages;
    }

    private static List<BufferedImage> searchByClustering(BufferedImage queryImage, List<BufferedImage> images) {

        // Perform color quantization using k-means clustering
        int k = 20; // Number of clusters
        List<Color> colors = kMeans(queryImage, k);

        // Compute the color frequencies for the input image
        int[] frequencies = computeColorFrequencies(queryImage, colors);

        // Compute the color frequencies for each image in the set
        List<int[]> imageFrequencies = new ArrayList<>();
        for (BufferedImage image : images) {
            int[] imageFreq = computeColorFrequencies(image, colors);
            imageFrequencies.add(imageFreq);
        }
        // Compute the Euclidean distance between the input image and each image in the set
        double[] distances = new double[images.size()];
        for (int i = 0; i < images.size(); i++) {
            int[] imageFreq = imageFrequencies.get(i);
            double distance = 0;
            for (int j = 0; j < frequencies.length; j++) {
                double diff = frequencies[j] - imageFreq[j];
                distance += diff * diff;
            }
            distances[i] = Math.sqrt(distance);
        }

        // Find the index of the image with the smallest distance
        int minIndex = 0;
        double minDistance = distances[0];
        for (int i = 1; i < distances.length; i++) {
            if (distances[i] < minDistance) {
                minIndex = i;
                minDistance = distances[i];
            }
        }


        // Display the most similar image (top5)
        List<BufferedImage> similarImages = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            BufferedImage image = images.get(i);
            similarImages.add(image);
        }
        return similarImages;
    }

    private static List<Color> kMeans(BufferedImage image, int k) {
        // Convert the image to an array of RGB values
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

        // Initialize the centroids randomly
        List<Color> centroids = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            int index = (int) (Math.random() * pixels.length);
            Color color = new Color(pixels[index]);
            centroids.add(color);
        }

        // Assign each pixel to the closest centroid
        int[] assignments = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            Color pixelColor = new Color(pixels[i]);
            int closestIndex = findClosestColorIndex(pixelColor, centroids);
            assignments[i] = closestIndex;
        }

        // Update the centroids until convergence
        boolean converged = false;
        while (!converged) {
            // Compute the mean color for each cluster
            List<Color> means = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                int rSum = 0, gSum = 0, bSum = 0, count = 0;
                for (int j = 0; j < pixels.length; j++) {
                    if (assignments[j] == i) {
                        Color pixelColor = new Color(pixels[j]);
                        rSum += pixelColor.getRed();
                        gSum += pixelColor.getGreen();
                        bSum += pixelColor.getBlue();
                        count++;
                    }
                }
                if (count > 0) {
                    int rMean = rSum / count;
                    int gMean = gSum / count;
                    int bMean = bSum / count;
                    Color meanColor = new Color(rMean, gMean, bMean);
                    means.add(meanColor);
                } else {
                    means.add(centroids.get(i));
                }
            }

            // Check for convergence
            converged = true;
            for (int i = 0; i < k; i++) {
                if (!means.get(i).equals(centroids.get(i))) {
                    converged = false;
                    break;
                }
            }

            // Update the centroids and assignments
            centroids = means;
            for (int i = 0; i < pixels.length; i++) {
                Color pixelColor = new Color(pixels[i]);
                int closestIndex = findClosestColorIndex(pixelColor, centroids);
                assignments[i] = closestIndex;
            }
        }

        return centroids;
    }

    private static int findClosestColorIndex(Color color, List<Color> colors) {
        int closestIndex = 0;
        double minDistance = distance(color, colors.get(0));
        for (int i = 1; i < colors.size(); i++) {
            double distance = distance(color, colors.get(i));
            if (distance < minDistance) {
                closestIndex = i;
                minDistance = distance;
            }
        }
        return closestIndex;
    }

    private static double distance(Color c1, Color c2) {
        double rDiff = c1.getRed() - c2.getRed();
        double gDiff = c1.getGreen() - c2.getGreen();
        double bDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }

    private static int[] computeColorFrequencies(BufferedImage image, List<Color> colors) {
        int[] frequencies = new int[colors.size()];
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color pixelColor = new Color(image.getRGB(i, j));
                int closestIndex = findClosestColorIndex(pixelColor, colors);
                frequencies[closestIndex]++;
            }
        }
        return frequencies;
    }


    // Compare similarity between color palettes using Euclidean distance
    private static double compareColorPalettes(Set<Color> palette1, Set<Color> palette2) {
        double similarityThreshold = 15;
        int similarColors = 0;

        for (Color color1 : palette1) {
            double minDistance = Double.MAX_VALUE;

            for (Color color2 : palette2) {
                // Calculate Euclidean distance between RGB values
                int rDiff = color1.getRed() - color2.getRed();
                int gDiff = color1.getGreen() - color2.getGreen();
                int bDiff = color1.getBlue() - color2.getBlue();

                double distance = Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);

                if (distance < minDistance) {
                    minDistance = distance;
                }
            }

            if (minDistance <= similarityThreshold) {
                similarColors++;
            }
        }

        // Calculate the similarity score as a percentage
        return (double) similarColors / palette1.size() * 100.0;
    }
}
