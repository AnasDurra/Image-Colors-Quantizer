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
    public static List<File> filterImages(
            File folder,
            int minSizeKB,
            int maxSizeKB,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        List<File> result = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {


                // check each of the parameters whether they are passed or not.
                boolean isSizeMatch = isSizeInRange(file, minSizeKB, maxSizeKB);
                boolean isDateMatch = isCreationDateInRange(file, fromDate, toDate);
                boolean isImage = isImage(file);

                if (isImage && isSizeMatch && isDateMatch) {
                    result.add(file);
                }
            }
        }
        return result;
    }

    private static boolean isImage(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".png")
                || name.endsWith(".gif")
                || name.endsWith(".bmp");
    }

    private static boolean isCreationDateInRange(File file, LocalDate fromDate, LocalDate toDate) {

        try {

            // check which of the parameters are passed and which are not.
            boolean isFromDatePassed = fromDate != null;
            boolean isToDatePassed = toDate != null;

            // if both of the parameters are not passed, then return true
            if (!isFromDatePassed && !isToDatePassed) {
                return true;
            }

            // get the creation date of the file
            Path path = file.toPath();
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            LocalDate creationDate = attr.creationTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // check whether the creation date is in the range or not
            return isFromDatePassed && isToDatePassed && creationDate.isAfter(fromDate.minusDays(1)) && creationDate.isBefore(toDate.plusDays(1))
                    || isFromDatePassed && !isToDatePassed && creationDate.isAfter(fromDate.minusDays(1))
                    || !isFromDatePassed && creationDate.isBefore(toDate.plusDays(1));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private static boolean isSizeInRange(File file, int minSizeKB, int maxSizeKB) {
        long fileSize = file.length() / 1024; // convert bytes to KB

        // check whether the file size is in the range or not (with a margin of 10KB)
        // and if any of the sizes is -1, then it means that the size is not specified.
        return minSizeKB == -1 && maxSizeKB == -1
                || minSizeKB == -1 && fileSize <= maxSizeKB + 10
                || maxSizeKB == -1 && fileSize >= minSizeKB - 10
                || fileSize >= minSizeKB - 10 && fileSize <= maxSizeKB + 10;
    }
}
