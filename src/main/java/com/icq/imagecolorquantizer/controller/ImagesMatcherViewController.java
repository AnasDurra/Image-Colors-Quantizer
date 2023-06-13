package com.icq.imagecolorquantizer.controller;

import com.icq.imagecolorquantizer.model.ProcessedImage;
import com.icq.imagecolorquantizer.service.ColorQuantizer;
import com.icq.imagecolorquantizer.service.ImageMatcher;
import com.icq.imagecolorquantizer.service.UTIL;
import com.icq.imagecolorquantizer.utils.ImageUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ImagesMatcherViewController {


    @FXML
    private Button cropButton;

    @FXML
    private DatePicker datePiker;

    @FXML
    private Button dimensionButton;

    @FXML
    private ListView<java.io.File> directoryListView;

    @FXML
    private TextField imageFilePath;

    @FXML
    private ImageView originalImageView;

    @FXML
    private Button searchButton;

    @FXML
    private Button addDirectoryButton;

    @FXML
    private Button removeDirectoryButton;

    @FXML
    private Button selectImageButton;

    @FXML
    private TextField sizeTextField;

    @FXML
    private GridPane selectedColorsGridPane;

    @FXML
    private TextField thresholdTF;

    @FXML
    private GridPane searchResultGP;

    @FXML
    private TextField newHeightTF;

    @FXML
    private TextField newWidthTF;

    @FXML
    private Button searchColorsBtn;

    /**
     * local state variables
     */
    private final java.util.List<java.awt.Color> selectedColors = new java.util.ArrayList<>();
    private ProcessedImage originalImage;
    private File selectedItem;
    private BufferedImage loadedImage;

    @FXML
    public void initialize() {

        directoryListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> selectedItem = newValue);


    }


    @FXML
    private void resizeButtonClick() {
        if (loadedImage != null) {

            // get the new width and height after validation
            // set the default value to image width and height
            int newWidth = loadedImage.getWidth();
            int newHeight = loadedImage.getHeight();

            //? 1. validate the new width
            try {
                newWidth = Integer.parseInt(newWidthTF.getText());
                if (newWidth < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                newWidthTF.setText("0");

                // show alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid width");
                alert.setContentText("Please enter a valid width");
                alert.showAndWait();
            }

            //? 2. validate the new height
            try {
                newHeight = Integer.parseInt(newHeightTF.getText());
                if (newHeight < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                newHeightTF.setText("0");

                // show alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid height");
                alert.setContentText("Please enter a valid height");
                alert.showAndWait();
            }

            loadedImage = ImageUtils.resizeImage(loadedImage, newWidth, newHeight);
            Image image = ImageUtils.convertBufferedImageToJavaFXImage(loadedImage);
            originalImageView.setImage(image);
            ImageUtils.centerImage(originalImageView);
        }
    }

    @FXML
    public void selectImageAction() {

        String imagePath = UTIL.chooseFile(imageFilePath);

        // if the image path is not null
        if (imagePath != null) {
            // assign original image to originalImage
            BufferedImage convertedImage = ImageUtils.loadImageFromPath(imagePath);
            assert convertedImage != null;

            //? image loaded
            loadedImage = convertedImage;

            // set the image path to imageFilePath text field
            imageFilePath.setText(imagePath);

            // create image object
            Image image = ImageUtils.convertBufferedImageToJavaFXImage(convertedImage);

            // set image to original image view
            originalImageView.setImage(image);

            // center the image inside the image view
            ImageUtils.centerImage(originalImageView);

            // make the image centered in the imageView
            originalImageView.setPreserveRatio(true);

            // clear the previous color selection
            selectedColors.clear();

            cropButton.setDisable(false);
            dimensionButton.setDisable(false);
            datePiker.setDisable(false);
            sizeTextField.setDisable(false);
            thresholdTF.setDisable(false);
            addDirectoryButton.setDisable(false);
            removeDirectoryButton.setDisable(false);
            searchButton.setDisable(false);
            newWidthTF.setDisable(false);
            newHeightTF.setDisable(false);
        }
    }

    @FXML
    public void addDirectoryAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Directories");

        // Open the directory chooser dialog
        File selectedDirectory = directoryChooser.showDialog(new Stage());

        if (selectedDirectory != null) {
//            // Clear the ListView
//            directoryListView.getItems().clear();
            if (directoryListView.getItems().contains(selectedDirectory)) {
                return;
            }
            // Add the selected directory path to the ListView
            directoryListView.getItems().add(selectedDirectory);
        }

        searchButton.setDisable(false);
    }

    @FXML
    public void removeDirectoryAction() {
        if (selectedItem != null) {
            directoryListView.getItems().remove(selectedItem);
            selectedItem = null;
        }
    }

    @FXML
    public void searchButtonClick() throws IOException {

        // TODO: FOR DEBUGGING
        System.out.println("searchButtonClick");

        // first of all, validate the user input

        //?validate the threshold value,
        // get the threshold value from the text field and validate it
        // in case there is any error, show an alert and return
        int threshold;
        try {

            // if empty, set to default value
            if (thresholdTF.getText().isEmpty()) {
                thresholdTF.setText("18");
            }

            threshold = Integer.parseInt(thresholdTF.getText());

            // check if the threshold is in the range [0, 100]
            if (threshold < 0 || threshold > 100) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            // show alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid threshold value");
            alert.setContentText("Please enter a valid threshold value");

            // clear the text field
            thresholdTF.setText("");

            alert.showAndWait();

            return;
        }


        // get the list of directories to search in
        List<File> foldersList = directoryListView.getItems();
        if (foldersList.isEmpty()) {

            // show alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No directories selected");
            alert.setContentText("Please select at least one directory to search in");
            alert.showAndWait();
            return;
        }

        // get the search filters
        // 1. image size
        int imageSize = -1;
        if (!sizeTextField.getText().isEmpty()) {
            try {
                imageSize = Integer.parseInt(sizeTextField.getText());
            } catch (NumberFormatException e) {
                // show alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid image size");
                alert.setContentText("Please enter a valid image size");
                alert.showAndWait();
                return;
            }
        }

        // 2. image date
        LocalDate imageDate = null;
        if (datePiker.getValue() != null) {
            imageDate = datePiker.getValue();
        }

        //?fetch the list of images that can be found in the selected directories
        //?after applying the filters (if any)
        List<BufferedImage> imagesList = ImageMatcher.loadMatchingImages(imageSize, imageDate, foldersList);


        //! quantize the input image
        originalImage = ColorQuantizer.medianCut(loadedImage, 8);
//        originalImage = ColorQuantizer.kMeans(loadedImage, 64);


        // get the color palette of the image
        Set<Color> colorPalette = originalImage.colorPalette();

        //? show the color palette in the grid pane


        // if the image changed, reset the color palette grid pane
        if (!originalImage.image().equals(loadedImage)) {
            // reset the search result grid pane
            selectedColorsGridPane.getChildren().clear();
        }

        showColorPaletteGrid(colorPalette);


        Map<BufferedImage, Double> imagesMap =
                ImageMatcher.searchForImage(
                        colorPalette,
                        imagesList,
                        threshold
                );

        // sort the images map by the similarity value
        imagesMap = imagesMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        //? clear the search result grid pane
        searchResultGP.getChildren().clear();
        showSearchResult(imagesMap);
        searchColorsBtn.setDisable(false);
    }

    @FXML
    public void searchForColorButtonClick() {

        // if the user selected colors, we should use them instead of the color palette
        if (selectedColors.isEmpty()) {
            // show alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No colors selected");
            alert.setContentText("Please select at least one color to search for");
            alert.showAndWait();
            return;
        }

        int threshold;
        try {

            // if empty, set to default value
            if (thresholdTF.getText().isEmpty()) {
                thresholdTF.setText("18");
            }

            threshold = Integer.parseInt(thresholdTF.getText());

            // check if the threshold is in the range [0, 100]
            if (threshold < 0 || threshold > 100) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            // show alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid threshold value");
            alert.setContentText("Please enter a valid threshold value");

            // clear the text field
            thresholdTF.setText("");

            alert.showAndWait();

            return;
        }


        // get the list of directories to search in
        List<File> foldersList = directoryListView.getItems();
        if (foldersList.isEmpty()) {

            // show alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No directories selected");
            alert.setContentText("Please select at least one directory to search in");
            alert.showAndWait();
            return;
        }

        // get the search filters
        // 1. image size
        int imageSize = -1;
        if (!sizeTextField.getText().isEmpty()) {
            try {
                imageSize = Integer.parseInt(sizeTextField.getText());
            } catch (NumberFormatException e) {
                // show alert
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid image size");
                alert.setContentText("Please enter a valid image size");
                alert.showAndWait();
                return;
            }
        }

        // 2. image date
        LocalDate imageDate = null;
        if (datePiker.getValue() != null) {
            imageDate = datePiker.getValue();
        }

        //?fetch the list of images that can be found in the selected directories
        //?after applying the filters (if any)
        List<BufferedImage> imagesList = ImageMatcher.loadMatchingImages(imageSize, imageDate, foldersList);


        Map<BufferedImage, Double> imagesMap =
                ImageMatcher.searchForImage(
                        new HashSet<>(selectedColors),
                        imagesList,
                        threshold
                );

        // sort the images map by the similarity value
        imagesMap = imagesMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        //? clear the search result grid pane
        searchResultGP.getChildren().clear();
        showSearchResult(imagesMap);

    }

    /*
     * this method shows the color palette of the image in the grid pane
     * each color is represented by a rectangle, the color of the rectangle
     * is the color of the color in the palette.
     */
    private void showColorPaletteGrid(Set<Color> colorPalette) {
        // size of color palette
        int colorPaletteSize = colorPalette.size();

        // num of columns in the color palette grid pane
        // equals to width of the color palette grid pane
        // divided by the size of each color rectangle (12)
        int numOfColumnPalette = (int) selectedColorsGridPane.getWidth() / 8;

        int index = 0;
        for (java.awt.Color color : colorPalette) {

            // created a cell filled with a colo
            javafx.scene.shape.Rectangle rectangle = new javafx.scene.shape.Rectangle(8, 8);
            rectangle.setFill(javafx.scene.paint.Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));

            // add the cell to the grid pane
            selectedColorsGridPane.add(rectangle, index % numOfColumnPalette, index / numOfColumnPalette);

            // on rectangle click, we should add the color to the selected colors list
            // and make the stork of the rectangle red
            // otherwise, if it's already selected, we should remove it from the selected colors list
            // and make the stork of the rectangle transparent
            rectangle.setOnMouseClicked(mouseEvent -> {
                if (selectedColors.contains(color)) {
                    selectedColors.remove(color);
                    rectangle.setStroke(javafx.scene.paint.Color.TRANSPARENT);
                } else {
                    selectedColors.add(color);
                    rectangle.setStroke(javafx.scene.paint.Color.RED);
                }
            });

            index++;
        }

    }


    /*
     * this method shows the search result in the grid pane
     * each image is represented by an image view, and below it
     * there is a label that shows the similarity ratio
     * between the image and the input image.
     */
    private void showSearchResult(Map<BufferedImage, Double> imagesMap) {

        // show the result in the grid pane two images per row
        int numOfColumns = (int) searchResultGP.getWidth() / 180;

        int i = 0;
        for (Map.Entry<BufferedImage, Double> entry : imagesMap.entrySet()) {

            /*
             * show the image in the grid pane
             * and the similarity ratio in a label below the image
             */

            // create VBox to hold the image and the label
            VBox vBox = new VBox();
            vBox.setSpacing(5);

            // create the image view
            ImageView imageView = new ImageView();

            // set the image view size
            imageView.setFitWidth(180);
            imageView.setFitHeight(180);

            // set the image view to show the image
            imageView.setImage(ImageUtils.convertBufferedImageToJavaFXImage(entry.getKey()));

            // preserve the image ratio
            imageView.setPreserveRatio(true);

            // on image double click, we should show the image color palette
            imageView.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2) {
                    UTIL.showColorPalette(entry.getKey());
                }
            });

            // add the image view to the VBox
            vBox.getChildren().add(imageView);

            // create the label
            Label label = new Label();

            // similarity ratio : __%
            label.setText("similarity ratio : " + String.format("%.2f", entry.getValue()) + "%");

            // CENTER the label text horizontally
            label.setAlignment(Pos.CENTER);

            // add the label to the VBox
            vBox.getChildren().add(label);

            // center the VBox content horizontally
            vBox.setAlignment(Pos.CENTER);

            // add the VBox to the grid pane
            searchResultGP.add(vBox, i % numOfColumns, i / numOfColumns);

            i++;
        }

        // change the width of the grid pane to fit the images
        searchResultGP.setPrefWidth(numOfColumns * 180);
    }
}

