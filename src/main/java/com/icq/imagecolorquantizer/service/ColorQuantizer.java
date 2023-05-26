package com.icq.imagecolorquantizer.service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class ColorQuantizer {

    // Method 1 ,
    // Input imageName , name of the image in path => src/imageName,
    // And k => Number of possible colors in the result = k^3
    static BufferedImage uniform_quantization(String imageName, int k) {  // Number of possible colors in the result = k^3
        BufferedImage image;
        BufferedImage quantizedImage = null;
        try {
            // Load the image file
            File imageFile = new File("src/" + imageName);
            image = ImageIO.read(imageFile);
            // Extract the file extension from the file path
            String imageExtension = imageFile.getPath().substring(imageFile.getPath().lastIndexOf(".") + 1);

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

            // TODO delete this just to printing the ranges
            for (int channel = 0; channel < k; channel++) {
                System.out.println(minValues[channel] + " - " + maxValues[channel]);
            }

            // Calculate the representative colors for ranges
            for (int i = 0; i < k; i++) {
                representative_color[i] = (maxValues[i] + minValues[i]) / 2;
            }

            // TODO delete this
            for (int i = 0; i < k; i++) {
                System.out.println(representative_color[i]);
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

            // Define the file path and name for saving the quantized image
            String outputPath = "src/(" + imageName + ")_Quantized" + new Date().getTime() + "." + imageExtension;

            // Save the quantized image
            File outputFile = new File(outputPath);
            ImageIO.write(quantizedImage, imageExtension, outputFile);
            System.out.println("Quantized image saved successfully.");
        } catch (Exception e) {
            System.out.println("image not found");
            e.printStackTrace();
        }
        return quantizedImage;
    }

    // Method 2
    public static Image kMeans(Image inputImage, int numClusters) throws IOException {
        // Convert the input image to a BufferedImage object
        BufferedImage bufferedImage = new BufferedImage(inputImage.getWidth(null), inputImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        bufferedImage.getGraphics().drawImage(inputImage, 0, 0, null);

        // Initialize the centroids randomly
        Color[] centroids = new Color[numClusters];
        for (int i = 0; i < numClusters; i++) {
            int x = (int) (Math.random() * bufferedImage.getWidth());
            int y = (int) (Math.random() * bufferedImage.getHeight());
            centroids[i] = new Color(bufferedImage.getRGB(x, y));
        }

        // Iterate until convergence
        boolean converged = false;
        while (!converged) {
            // Assign each pixel to the nearest centroid
            int[][] assignments = new int[bufferedImage.getWidth()][bufferedImage.getHeight()];
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                for (int y = 0; y < bufferedImage.getHeight(); y++) {
                    Color pixel = new Color(bufferedImage.getRGB(x, y));
                    int bestCluster = 0;
                    double bestDistance = Double.MAX_VALUE;
                    for (int i = 0; i < numClusters; i++) {
                        double distance = getDistance(pixel, centroids[i]);
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
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                for (int y = 0; y < bufferedImage.getHeight(); y++) {
                    Color pixel = new Color(bufferedImage.getRGB(x, y));
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
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                Color pixel = new Color(bufferedImage.getRGB(x, y));
                int bestCluster = 0;
                double bestDistance = Double.MAX_VALUE;
                for (int i = 0; i < numClusters; i++) {
                    double distance = getDistance(pixel, centroids[i]);
                    if (distance < bestDistance) {
                        bestCluster = i;
                        bestDistance = distance;
                    }
                }
                bufferedImage.setRGB(x, y, centroids[bestCluster].getRGB());
            }
        }
        // Convert the BufferedImage object back to an Image object then into indexed image
        return createIndexedImage(bufferedImage
                .getScaledInstance(inputImage.getWidth(null), inputImage.getHeight(null), Image.SCALE_DEFAULT), centroids, 8);
    }

    // Method 3
}
