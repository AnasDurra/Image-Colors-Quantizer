package com.icq.imagecolorquantizer.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ProcessedImage {
    private final BufferedImage image;
    private final Set<Color> colorPalette;

    public ProcessedImage(BufferedImage image, Set<Color> colorPalette) {
        this.image = image;
        this.colorPalette = colorPalette;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Set<Color> getColorPalette() {
        return colorPalette;
    }

    public int getNumberOfColors() {
        return colorPalette.size();
    }

    //writing the image to a temp folder to get the specific size and then delete the image
    //TODO change the path in your device
    public float getImageSize() throws IOException {
        String path = "C:\\Users\\ASUS\\Desktop\\multimedia\\temp\\saveTemp.tiff";
        File output = new File(path);
        ImageIO.write((RenderedImage) image, "tiff", output);

        float size = output.length();

        output.delete();

        return size;
    }
}
