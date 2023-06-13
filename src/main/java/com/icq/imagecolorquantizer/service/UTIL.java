package com.icq.imagecolorquantizer.service;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class UTIL {

    public static double getColorDistance(Color c1, Color c2) {
        double rDiff = c1.getRed() - c2.getRed();
        double gDiff = c1.getGreen() - c2.getGreen();
        double bDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt(rDiff * rDiff + gDiff * gDiff + bDiff * bDiff);
    }

    public static Set<Color> extractColorPalette(BufferedImage image) {
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

    public static BufferedImage createIndexedImage(BufferedImage image) {
        Set<Color> quantizedColors = extractColorPalette(image);
        // Create a new IndexColorModel with the colors from the quantized image
        byte[] reds = new byte[quantizedColors.size()];
        byte[] greens = new byte[quantizedColors.size()];
        byte[] blues = new byte[quantizedColors.size()];
        int idx = 0;
        for (Color i : quantizedColors) {
            int color = i.getRGB();
            reds[idx] = (byte) ((color >> 16) & 0xFF);
            greens[idx] = (byte) ((color >> 8) & 0xFF);
            blues[idx] = (byte) (color & 0xFF);
            idx++;
        }
        IndexColorModel colorModel = new IndexColorModel(8, quantizedColors.size(), reds, greens, blues);

        // Create a new BufferedImage with the same dimensions as the original image, but with the TYPE_BYTE_INDEXED image type and the new IndexColorModel
        BufferedImage indexedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, colorModel);

        // Draw the original image onto the new indexed image
        indexedImage.getGraphics().drawImage(image, 0, 0, null);
        return indexedImage;
    }

    public static String chooseFile(TextField imageFilePath) {
        // create file chooser
        FileChooser chooser = new FileChooser();

        // set the initial directory to desktop
        chooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));

        // set title
        chooser.setTitle("Open File");

        // set filter to only show images
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"));

        File file = chooser.showOpenDialog(new Stage());
        if (file != null) {
            return file.getPath();
        } else {
            if (imageFilePath.getText().isEmpty()) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Please Select a File");
                alert.showAndWait();
            }
        }
        return null;
    }

    public static void showColorPalette(BufferedImage bufferedImage) {

        // create a new stage
        Stage stage = new Stage();

        // create a new grid pane
        GridPane gridPane = new GridPane();

        // set the padding of the grid pane
        gridPane.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));

        // set the vertical and horizontal gap between the cells
        gridPane.setVgap(1);
        gridPane.setHgap(1);

        // set the background color of the grid pane
        gridPane.setStyle("-fx-background-color: #ffffff;");

        // get the color palette of the image
        Set<Color> colorPalette1 = UTIL.extractColorPalette(bufferedImage);

        // num of columns in the color palette grid pane
        // equals to width of the color palette grid pane
        // divided by the size of each color rectangle (10)
        int numOfColumnPalette1 = 35;

        int index1 = 0;
        for (java.awt.Color color : colorPalette1) {

            // created a cell filled with a colo
            javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(10, 10);
            rectangle.setFill(javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));

            // add the cell to the grid pane
            gridPane.add(rectangle, index1 % numOfColumnPalette1, index1 / numOfColumnPalette1);

            index1++;
        }

        // create a new scene
        Scene scene = new Scene(gridPane);

        // set the scene to the stage
        stage.setScene(scene);

        // set the title of the stage
        stage.setTitle("Color Palette");

        // show the stage
        stage.show();
    }

}