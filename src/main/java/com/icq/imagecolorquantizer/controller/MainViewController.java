package com.icq.imagecolorquantizer.controller;

import com.icq.imagecolorquantizer.service.ColorQuantizer;
import com.icq.imagecolorquantizer.utils.ImageUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
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

    @FXML
    private Text originalImageSizeTextView;

    @FXML
    private Text originalImageNumOfColorsTextView;

    @FXML
    private Text quantizedImageSizeTextView;


    // selected algorithm
    private String selectedAlgorithm;

    // initialize method
    @FXML
    public void initialize() {
        // disable quantize button
        quantizeButton.setDisable(true);
        imageQuantizationAlgorithmHBox.setDisable(true);

        // add items to imageQuantizationAlgorithmComboBox
        imageQuantizationAlgorithmComboBox.getItems().addAll(
                "Uniform",
                "Median Cut",
                "Octree"
        );

        // add event listener to imageQuantizationAlgorithmComboBox
        imageQuantizationAlgorithmComboBox.setOnAction(event -> {
            // get selected algorithm
            selectedAlgorithm = imageQuantizationAlgorithmComboBox.getSelectionModel().getSelectedItem();

            // enable quantize button
            quantizeButton.setDisable(false);
        });

        // set image click event handler
        originalImageView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openZoomedInStage(originalImageView.getImage());
            }
        });
        quantizedImageView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openZoomedInStage(quantizedImageView.getImage());
            }
        });
    }


    private void openZoomedInStage(Image image) {
        Stage zoomedInStage = new Stage();
        ImageView zoomedInView = new ImageView();

        zoomedInStage.setScene(new Scene(new Pane(zoomedInView)));
        zoomedInStage.setTitle("Zoomed In Image");
        zoomedInView.setPreserveRatio(true);
        zoomedInView.setImage(image);

        // set stage position to center of screen
        zoomedInStage.setX((Screen.getPrimary().getBounds().getWidth() - zoomedInStage.getWidth()) / 2);
        zoomedInStage.setY((Screen.getPrimary().getBounds().getHeight() - zoomedInStage.getHeight()) / 2);


        // make image fit the stage
        zoomedInView.fitWidthProperty().bind(zoomedInStage.widthProperty());
        zoomedInView.fitHeightProperty().bind(zoomedInStage.heightProperty());

        zoomedInStage.show();
    }


    // select image action event handler
    @FXML
    public void selectImageAction(ActionEvent event) {
        String imagePath = chooseFile();

        // if image path is not null
        if (imagePath != null) {
            // set image path to imageFilePath text field
            imageFilePath.setText(imagePath);

            // enable quantization algorithm combo box
            imageQuantizationAlgorithmHBox.setDisable(false);

            // create image object
            Image image = new Image("file:" + imagePath);

            // set image to original image view
            originalImageView.setImage(image);

            // center the image inside the image view
            ImageUtils.centerImage(originalImageView);

            // set original image size
            originalImageSizeTextView.setText("Size (KB): " + (int) (new File(imagePath).length() / 1024));

            // make the image centered in the imageView
            originalImageView.setPreserveRatio(true);

        }

    }

    // quantize image action event handler
    @FXML
    public void quantizeImageAction(ActionEvent event) {

        // make sure quantization algorithm is selected
        if (selectedAlgorithm != null) {
            // get image path
            String imagePath = imageFilePath.getText();

            // create image object
            Image image;

            // TODO: quantize image
            image = ImageUtils.convertBufferedImageToJavaFXImage(quantizeImage(imagePath));

            // set image to quantized image view
            quantizedImageView.setImage(image);

            // make the image centered in the imageView
            quantizedImageView.setPreserveRatio(true);

            // center the image inside the image view
            ImageUtils.centerImage(quantizedImageView);

        } else {
            // show alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Please Select a Quantization Algorithm");
        }
    }


    private BufferedImage quantizeImage(String imagePath) {
        return ColorQuantizer.uniformQuantization(imagePath, 16);
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

