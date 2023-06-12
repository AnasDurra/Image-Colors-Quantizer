package com.icq.imagecolorquantizer.controller;

import com.icq.imagecolorquantizer.model.ProcessedImage;
import com.icq.imagecolorquantizer.service.ImageMatcher;
import com.icq.imagecolorquantizer.service.UTIL;
import com.icq.imagecolorquantizer.utils.ImageUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Set;

public class ImagesMatcherViewController {

    private ProcessedImage originalImage;

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
    private TextField similarityRatioValue;

    @FXML
    private GridPane searchResultGP;

    /**
     * local state variables
     */
    private final java.util.List<java.awt.Color> selectedColors = new java.util.ArrayList<>();

    private File selectedItem;

    @FXML
    public void initialize() {

        directoryListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> selectedItem = newValue);


    }

    @FXML
    public void selectImageAction(ActionEvent event) {

        String imagePath = UTIL.chooseFile(imageFilePath);

        // if the image path is not null
        if (imagePath != null) {
            // assign original image to originalImage
            BufferedImage convertedImage = ImageUtils.loadImageFromPath(imagePath);
            assert convertedImage != null;
            originalImage = new ProcessedImage(convertedImage, UTIL.extractColorPalette(convertedImage));

            // set the image path to imageFilePath text field
            imageFilePath.setText(imagePath);

            // create image object
            Image image = new Image("file:" + imagePath);

            // set image to original image view
            originalImageView.setImage(image);

//            BufferedImage originalBufferedImage = ImageUtils.loadImageFromPath(imagePath);
//            processedImage = new ProcessedImage(originalBufferedImage, UTIL.extractColorPalette(Objects.requireNonNull(originalBufferedImage)));
            // center the image inside the image view
            ImageUtils.centerImage(originalImageView);

            // make the image centered in the imageView
            originalImageView.setPreserveRatio(true);


            // load the image as buffered image
            BufferedImage bufferedImage = ImageUtils.loadImageFromPath(imagePath);

            if (bufferedImage != null) {

                // clear the previous color selection
                selectedColors.clear();
                selectedColorsGridPane.getChildren().clear();

                // get the color palette of the image
                Set<java.awt.Color> colorPalette = UTIL.extractColorPalette(bufferedImage);

                // size of color palette
                int colorPaletteSize = colorPalette.size();

                // num of columns in the color palette grid pane
                // equals to width of the color palette grid pane
                // divided by the size of each color rectangle (12)
                int numOfColumns = (int) selectedColorsGridPane.getWidth() / 12;
                int numOfRows = colorPaletteSize / numOfColumns;

                int i = 0;
                for (java.awt.Color color : colorPalette) {

                    // created a cell filled with a colo
                    Rectangle rectangle = new Rectangle(12, 12);
                    rectangle.setFill(Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));

                    // add the cell to the grid pane
                    selectedColorsGridPane.add(rectangle, i % numOfColumns, i / numOfColumns);

                    // on rectangle click, we should add the color to the selected colors list
                    // and make the stork of the rectangle red
                    // otherwise, if it's already selected, we should remove it from the selected colors list
                    // and make the stork of the rectangle transparent
                    rectangle.setOnMouseClicked(mouseEvent -> {
                        if (selectedColors.contains(color)) {
                            selectedColors.remove(color);
                            rectangle.setStroke(Color.TRANSPARENT);
                        } else {
                            selectedColors.add(color);
                            rectangle.setStroke(Color.RED);
                        }
                    });

                    i++;
                }

            }

            cropButton.setDisable(false);
            dimensionButton.setDisable(false);
            datePiker.setDisable(false);
            sizeTextField.setDisable(false);
            similarityRatioValue.setDisable(false);
            addDirectoryButton.setDisable(false);
            removeDirectoryButton.setDisable(false);


        }
    }

    @FXML
    public void addDirectoryAction(ActionEvent event) {
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
    public void removeDirectoryAction(ActionEvent event) {
        if (selectedItem != null) {
            directoryListView.getItems().remove(selectedItem);
            selectedItem = null;
        }
    }

    @FXML
    public void searchButtonClick(ActionEvent event) {

        // clear the grid
        searchResultGP.getChildren().clear();

        System.out.println("searchButtonClick");

        List<File> foldersList = directoryListView.getItems();
        List<BufferedImage> imagesList = ImageMatcher.loadMatchingImages(-1, null, foldersList);


        imagesList = ImageMatcher.searchForImage(originalImage.image(), imagesList);


        // show the result in the grid pane two images per row
        int numOfColumns = (int) searchResultGP.getWidth() / 180;
        int numOfRows = imagesList.size() / numOfColumns;

        int i = 0;
        for (BufferedImage bufferedImage : imagesList) {

            // create image object
            Image image = ImageUtils.convertBufferedImageToJavaFXImage(bufferedImage);

            // create image view
            ImageView imageView = new ImageView(image);

            // set the width and height of the image view
            imageView.setFitWidth(200);
            imageView.setFitHeight(200);

            // make the image centered in the imageView
            imageView.setPreserveRatio(true);

            // center the image inside the image view
            ImageUtils.centerImage(imageView);

            // add the image view to the grid pane
            searchResultGP.add(imageView, i % numOfColumns, i / numOfColumns);


            i++;
        }

        // change the width of the grid pane to fit the images
        searchResultGP.setPrefWidth(numOfColumns * 180);
    }

}
