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
    public static BufferedImage uniformQuantization(String imageName, int k){  // Number of possible colors in the result = k^3
        BufferedImage image;
        BufferedImage quantizedImage = null;
        try {
            // Load the image file
            File imageFile = new File( imageName);
            image = ImageIO.read(imageFile);
            // Extract the file extension from the file path
            String imageExtension = imageFile.getPath().substring(imageFile.getPath().lastIndexOf(".") + 1);

            // Get the dimensions of the image
            int width = image.getWidth();
            int height = image.getHeight();

            // Quantized image
            quantizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            int channelRange = (256 + k - 1)/k;
            int[] minValues = new int[k]; // Array to store the minimum values for each channel (R, G, B)
            int[] maxValues = new int[k]; // Array to store the maximum values for each channel (R, G, B)
            int[] representative_color = new int [k]; // Representative color array

            // Calculate the range for each channel
            for (int channel = 0; channel < k; channel++) {
                minValues[channel] = channel * channelRange; // Minimum value for the channel
                maxValues[channel] = (channel + 1) * channelRange - 1; // Maximum value for the channel
            }

            // TODO delete this just to printing the ranges
            for (int channel = 0; channel < k; channel++) {
                System.out.println(minValues[channel] +" - "+maxValues[channel]);
            }

            // Calculate the representative colors for ranges
            for (int i = 0; i < k; i++) {
                representative_color[i] =(maxValues[i] + minValues[i])/2;
            }

            // TODO delete this
            for (int i = 0; i < k; i++) {
                System.out.println(representative_color[i]);
            }

            for(int y=0 ;y<height ;y++){
                for(int x=0 ; x<width ;x++){
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

        }catch (Exception e) {
            System.out.println("image not found");
            e.printStackTrace();
        }
        return quantizedImage;
    }

    // Extract Color Palette
    // Input image , the reslut of uniform_quantization method
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

    // Method 2

    // Method 3
}
