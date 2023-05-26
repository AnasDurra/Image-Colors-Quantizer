package com.icq.imagecolorquantizer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class MainViewController {
    @FXML
    private Button selectImageButton;

    @FXML
    private TextField imageFilePath;

    @FXML
    private Button quantizeButton;

    @FXML
    private ImageView originalImageView;

    @FXML
    private ImageView quantizedImageView;

    @FXML
    private HBox imageQuantizationAlgorithmHBox;

    @FXML
    private ComboBox<String> imageQuantizationAlgorithmComboBox;

    // initialize method
    @FXML
    public void initialize() {
        // disable quantize button
        quantizeButton.setDisable(true);
        imageQuantizationAlgorithmHBox.setDisable(true);

        // add items to imageQuantizationAlgorithmComboBox
        imageQuantizationAlgorithmComboBox.getItems().addAll(
                "K-Means",
                "Median Cut",
                "Octree"
        );

    }


    // select image action event handler
    @FXML
    public void selectImageAction(ActionEvent event) {
        String imagePath = chooseFile();

        // if image path is not null
        if (imagePath != null) {
            // set image path to imageFilePath text field
            imageFilePath.setText(imagePath);

            // enable quantize button
            quantizeButton.setDisable(false);
            imageQuantizationAlgorithmHBox.setDisable(false);

            // create image object
            Image image = new Image("file:" + imagePath);

            // set image to original image view
            originalImageView.setImage(image);

            // make the image centered in the imageView
            originalImageView.setPreserveRatio(true);

        }

    }

    // quantize image action event handler
    @FXML
    public void quantizeImageAction(ActionEvent event) {

    }


    /**
     * Choose file from file system
     * and return the path of the file
     */
    private String chooseFile() {
        // create file chooser
        FileChooser chooser = new FileChooser();

        // set the initial directory to desktop
        chooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));

        // set title
        chooser.setTitle("Open File");

        // set filter to only show images
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );

        File file = chooser.showOpenDialog(new Stage());
        if (file != null) {
            return file.getPath();
        } else {
            if (imageFilePath.getText().isEmpty()) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Please Select a File");
//            alert.setContentText("You didn't select a file!");
                alert.showAndWait();
            }
        }
        return null;
    }
}

