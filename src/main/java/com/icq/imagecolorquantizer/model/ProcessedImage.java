package com.icq.imagecolorquantizer.model;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

public record ProcessedImage(BufferedImage image, Set<Color> colorPalette) {

    public int getNumberOfColors() {
        return colorPalette.size();
    }

    /**
     * TODO: implement this method
     *  a method that takes the buffered image,
     *  and return it's size in kilobytes
     */
    public int getImageSize() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "png", baos);
            byte[] imageData = baos.toByteArray();
            int imageSizeInBytes = imageData.length;
            return imageSizeInBytes / 1024;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if there was an error
    }
}
