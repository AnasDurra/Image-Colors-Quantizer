package com.icq.imagecolorquantizer.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ImageSearcher {
    public static List<File> filterImages(File folder, LocalDate creationDate, int sizeInKB) {
        List<File> result = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {

                // use date only if not null and use size only if not -1
                boolean isCreationDateMatch = creationDate == null || isCreationDateMatch(file, creationDate);
                boolean isSizeMatch = sizeInKB == -1 || isSizeMatch(file, sizeInKB);
                if (isImage(file) && isCreationDateMatch && isSizeMatch) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    private static boolean isImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".gif");
    }

    private static boolean isCreationDateMatch(File file, LocalDate creationDate) {
        try {
            Path path = file.toPath();
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            LocalDate fileCreationDate = attrs.creationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return fileCreationDate.equals(creationDate);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private static boolean isSizeMatch(File file, int sizeInKB) {
        long fileSize = file.length() / 1024; // convert bytes to KB
        return Math.abs(fileSize - sizeInKB) <= 10; // allow a margin of 10KB
    }
}
