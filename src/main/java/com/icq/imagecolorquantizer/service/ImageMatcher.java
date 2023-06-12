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

                if (size == -1 && date == null) {
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
    public static Map<BufferedImage, Set<Color>> extractColorPalette(List<BufferedImage> images) {
        Map<BufferedImage, Set<Color>> colorPaletteMap = new HashMap<>();
        for (BufferedImage image : images) {
            Set<Color> colorPalette = UTIL.extractColorPalette(image);
            colorPaletteMap.put(image, colorPalette);
        }
        return colorPaletteMap;
    }

    /*
     * this function is used to extract the list of colorPalette
     * that contains the given color by 70% or more for the given
     * list of colorPalette.
     */
    public List<BufferedImage> extractMatchingColorPalette(Color color, Map<BufferedImage, Set<Color>> entries) {
        List<BufferedImage> matchingColorPaletteList = new ArrayList<>();
        entries.forEach((image, colorPalette) -> {
            if (containsColor(color, colorPalette)) {
                matchingColorPaletteList.add(image);
            }
        });
        return matchingColorPaletteList;
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
}
