package com.icq.imagecolorquantizer.service;

import com.icq.imagecolorquantizer.model.ProcessedImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;


public class ColorQuantizer {

    // Method 1 ,
    // Input imageName , name of the image in path => src/imageName,
    // And k => Number of possible colors in the result = k^3
    public static ProcessedImage uniformQuantization(BufferedImage image, int k) {
        BufferedImage quantizedImage = null;
        try {
            // Get the dimensions of the image
            int width = image.getWidth();
            int height = image.getHeight();

            // Quantized image
            quantizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            int channelRange = (256 + k - 1) / k;
            int[] minValues = new int[k]; // Array to store the minimum values for each channel (R, G, B)
            int[] maxValues = new int[k]; // Array to store the maximum values for each channel (R, G, B)
            int[] representative_color = new int[k]; // Representative color array

            // Calculate the range for each channel
            for (int channel = 0; channel < k; channel++) {
                minValues[channel] = channel * channelRange; // Minimum value for the channel
                maxValues[channel] = (channel + 1) * channelRange - 1; // Maximum value for the channel
            }
            // Calculate the representative colors for ranges
            for (int i = 0; i < k; i++) {
                representative_color[i] = (maxValues[i] + minValues[i]) / 2;
            }

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Extract the RGB values of the current pixel
                    int pixel = image.getRGB(x, y);
                    int red = (pixel >> 16) & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue = pixel & 0xFF;

                    // Calculate the interval index for each channel
                    int redIndex = red / channelRange;
                    int greenIndex = green / channelRange;
                    int blueIndex = blue / channelRange;

                    // Set the RGB values of the corresponding pixel in the quantized image
                    int quantizedPixel = (representative_color[redIndex] << 16) | (representative_color[greenIndex] << 8) | representative_color[blueIndex];
                    quantizedImage.setRGB(x, y, quantizedPixel);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (quantizedImage != null) {
            return new ProcessedImage(quantizedImage, UTIL.extractColorPalette(quantizedImage));
        } else {
            return null;
        }
    }

    // Extract Color Palette
    // Input image , the result of uniform_quantization method

    // Method 2
    public static ProcessedImage kMeans(BufferedImage inputImage, int numClusters) throws IOException {
        // Convert the input image to a BufferedImage object

        // Initialize the centroids randomly
        Color[] centroids = new Color[numClusters];
        for (int i = 0; i < numClusters; i++) {
            int x = (int) (Math.random() * inputImage.getWidth());
            int y = (int) (Math.random() * inputImage.getHeight());
            centroids[i] = new Color(inputImage.getRGB(x, y));
        }

        // Iterate until convergence
        boolean converged = false;
        while (!converged) {
            // Assign each pixel to the nearest centroid
            int[][] assignments = new int[inputImage.getWidth()][inputImage.getHeight()];
            for (int x = 0; x < inputImage.getWidth(); x++) {
                for (int y = 0; y < inputImage.getHeight(); y++) {
                    Color pixel = new Color(inputImage.getRGB(x, y));
                    int bestCluster = 0;
                    double bestDistance = Double.MAX_VALUE;
                    for (int i = 0; i < numClusters; i++) {
                        double distance = UTIL.getColorDistance(pixel, centroids[i]);
                        if (distance < bestDistance) {
                            bestCluster = i;
                            bestDistance = distance;
                        }
                    }
                    assignments[x][y] = bestCluster;
                }
            }
            // Update the centroids
            int[] counts = new int[numClusters];
            int[][] sums = new int[numClusters][3];
            for (int x = 0; x < inputImage.getWidth(); x++) {
                for (int y = 0; y < inputImage.getHeight(); y++) {
                    Color pixel = new Color(inputImage.getRGB(x, y));
                    int cluster = assignments[x][y];
                    counts[cluster]++;
                    sums[cluster][0] += pixel.getRed();
                    sums[cluster][1] += pixel.getGreen();
                    sums[cluster][2] += pixel.getBlue();
                }
            }
            converged = true;
            for (int i = 0; i < numClusters; i++) {
                if (counts[i] > 0) {
                    int r = sums[i][0] / counts[i];
                    int g = sums[i][1] / counts[i];
                    int b = sums[i][2] / counts[i];
                    Color newCentroid = new Color(r, g, b);
                    if (!newCentroid.equals(centroids[i])) {
                        centroids[i] = newCentroid;
                        converged = false;
                    }
                }
            }
        }

        // Replace each pixel with the nearest centroid
        for (int x = 0; x < inputImage.getWidth(); x++) {
            for (int y = 0; y < inputImage.getHeight(); y++) {
                Color pixel = new Color(inputImage.getRGB(x, y));
                int bestCluster = 0;
                double bestDistance = Double.MAX_VALUE;
                for (int i = 0; i < numClusters; i++) {
                    double distance = UTIL.getColorDistance(pixel, centroids[i]);
                    if (distance < bestDistance) {
                        bestCluster = i;
                        bestDistance = distance;
                    }
                }
                inputImage.setRGB(x, y, centroids[bestCluster].getRGB());
            }
        }
        //BufferedImage indexedImage = UTIL.createIndexedImage(inputImage, centroids, numClusters);
        return new ProcessedImage(inputImage, UTIL.extractColorPalette(inputImage));
    }

    // Method 3
    public static ProcessedImage medianCut(BufferedImage image, int depth) throws IOException {
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        int[][] flattened_img_array = new int[image.getWidth() * image.getHeight()][5];
        int counter = 0;

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                flattened_img_array[counter][0] = color.getRed();
                flattened_img_array[counter][1] = color.getGreen();
                flattened_img_array[counter][2] = color.getBlue();
                flattened_img_array[counter][3] = x;
                flattened_img_array[counter][4] = y;

                counter++;
            }
        }

        splitIntoBuckets(image, output, flattened_img_array, depth);

        return new ProcessedImage(output, UTIL.extractColorPalette(output));
    }

    private static void splitIntoBuckets(BufferedImage image, BufferedImage output, int[][] flattened_img_array, int depth) {
        if (flattened_img_array.length == 0) {
            return;
        }

        if (depth == 0) {
            medianCutQuantize(image, output, flattened_img_array);
            return;
        }

        int r_range = getMaxColumnValue(image, flattened_img_array, 0) - getMinColumnValue(image, flattened_img_array, 0);
        int g_range = getMaxColumnValue(image, flattened_img_array, 1) - getMinColumnValue(image, flattened_img_array, 1);
        int b_range = getMaxColumnValue(image, flattened_img_array, 2) - getMinColumnValue(image, flattened_img_array, 2);

        int space_with_highest_range = 0;

        if (g_range >= r_range && g_range >= b_range) space_with_highest_range = 1;
        else if (b_range >= r_range) space_with_highest_range = 2;


        int finalSpace_with_highest_range = space_with_highest_range;
        Arrays.sort(flattened_img_array, Comparator.comparingInt(o -> o[finalSpace_with_highest_range]));

        int median_index = (flattened_img_array.length + 1) / 2;

        splitIntoBuckets(image, output, Arrays.copyOfRange(flattened_img_array, 0, median_index), depth - 1);
        splitIntoBuckets(image, output, Arrays.copyOfRange(flattened_img_array, median_index, flattened_img_array.length), depth - 1);

    }

    private static int getMaxColumnValue(BufferedImage image, int[][] array, int column) {
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < array.length; i++) {
            max = Math.max(max, array[i][column]);
        }

        return max;
    }

    private static int getMinColumnValue(BufferedImage image, int[][] array, int column) {
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < array.length; i++) {
            min = Math.min(min, array[i][column]);
        }

        return min;
    }

    private static int getAvgColumnValue(int[][] array, int column) {
        int avg = 0;

        for (int i = 0; i < array.length; i++) {
            avg += array[i][column];
        }

        return avg / array.length;
    }


    private static void medianCutQuantize(BufferedImage image, BufferedImage output, int[][] array) {
        int r_avg = getAvgColumnValue(array, 0);
        int g_avg = getAvgColumnValue(array, 1);
        int b_avg = getAvgColumnValue(array, 2);

        for (int i = 0; i < array.length; i++) {
            Color color = new Color(r_avg, g_avg, b_avg);
            output.setRGB(array[i][3], array[i][4], color.getRGB());
        }

    }
}
