package com.icq.imagecolorquantizer.utils;

import javafx.scene.image.*;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    // convert buffer image to javafx image
    public static Image convertBufferedImageToJavaFXImage(BufferedImage bufferedImage) {
        WritableImage writableImage = null;
        if (bufferedImage != null) {
            writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
            PixelWriter pixelWriter = writableImage.getPixelWriter();
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                for (int y = 0; y < bufferedImage.getHeight(); y++) {
                    pixelWriter.setArgb(x, y, bufferedImage.getRGB(x, y));
                }
            }
        }
        return writableImage;
    }

    public static void centerImage(ImageView imageView) {
        Image img = imageView.getImage();
        if (img != null) {
            double w;
            double h;

            double ratioX = imageView.getFitWidth() / img.getWidth();
            double ratioY = imageView.getFitHeight() / img.getHeight();

            double reducCoeff = Math.min(ratioX, ratioY);

            w = img.getWidth() * reducCoeff;
            h = img.getHeight() * reducCoeff;

            imageView.setX((imageView.getFitWidth() - w) / 2);
            imageView.setY((imageView.getFitHeight() - h) / 2);

        }
    }

    // convert javafx image to buffer image
    public static BufferedImage convertJavaFXImageToBufferedImage(Image image) {
        WritableImage writableImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        pixelWriter.setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), image.getPixelReader(), 0, 0);

        BufferedImage bufferedImage = new BufferedImage((int) writableImage.getWidth(), (int) writableImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferInt dataBuffer = (DataBufferInt) raster.getDataBuffer();
        int[] data = dataBuffer.getData();
        writableImage.getPixelReader().getPixels(0, 0, (int) writableImage.getWidth(), (int) writableImage.getHeight(), PixelFormat.getIntArgbInstance(), data, 0, (int) writableImage.getWidth());


        return bufferedImage;
    }

    public static BufferedImage loadImageFromPath(String imagePath) {
        try {
            File file = new File(imagePath);
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());

        // AffineTransform is used to perform scaling operation in Java
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.scale((double) targetWidth / originalImage.getWidth(), (double) targetHeight / originalImage.getHeight());

        // AffineTransformOp.TYPE_BILINEAR is used for high-quality image
        AffineTransformOp affineTransformOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);

        return affineTransformOp.filter(originalImage, resizedImage);
    }

}
