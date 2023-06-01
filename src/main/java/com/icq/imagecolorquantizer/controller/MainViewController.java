package com.icq.imagecolorquantizer.controller;

import com.icq.imagecolorquantizer.model.ProcessedImage;
import com.icq.imagecolorquantizer.service.ColorQuantizer;
import com.icq.imagecolorquantizer.service.Histogram;
import com.icq.imagecolorquantizer.service.UTIL;
import com.icq.imagecolorquantizer.utils.ColorUtils;
import com.icq.imagecolorquantizer.utils.ImageUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javafx.scene.shape.Rectangle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

public class MainViewController {


    private ProcessedImage processedImage;

    private ProcessedImage originalImage;

    private ProcessedImage histogram;
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

    @FXML
    private Text quantizedImageNumOfColorsTextView;

    @FXML
    private TextField valueTextField;

    @FXML
    private GridPane gridColorPalette;

    @FXML
    private Button quantizedColorPalette;

    @FXML
    private Button quantizedHistogram;

    @FXML
    private Button originalColorPalette;

    @FXML
    private Button originalHistogram;

    private int textValue;

    // selected algorithm
    private String selectedAlgorithm;

    // initialize method
    @FXML
    public void initialize() {
        // disable quantize button
        quantizeButton.setDisable(true);
        imageQuantizationAlgorithmHBox.setDisable(true);
        valueTextField.setDisable(true);
        quantizedColorPalette.setDisable(true);
        quantizedHistogram.setDisable(true);
        originalColorPalette.setDisable(true);
        originalHistogram.setDisable(true);

        // add items to imageQuantizationAlgorithmComboBox
        imageQuantizationAlgorithmComboBox.getItems().addAll("Uniform", "k Means", "Median Cut");


        // add event listener to imageQuantizationAlgorithmComboBox
        imageQuantizationAlgorithmComboBox.setOnAction(event -> {

            // get selected algorithm
            selectedAlgorithm = imageQuantizationAlgorithmComboBox.getSelectionModel().getSelectedItem();

            //enable text filed message
            valueTextField.setDisable(false);

        });

        valueTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!isNotInteger(valueTextField.getText())) {
                //get the integer number
                textValue = Integer.parseInt(valueTextField.getText());
                // enable quantize button
                quantizeButton.setDisable(false);
            } else {

                // disable quantize button
                quantizeButton.setDisable(true);

                if (valueTextField.getText().isEmpty())
                    return;

                //show alert
                Platform.runLater(() -> {
                    valueTextField.clear();
                });
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Invalid Input");
                alert.setHeaderText("Please enter a valid integer value.");
                alert.show();
            }
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

        ProcessedImage processedImage;
        // if image path is not null
        if (imagePath != null) {
            // assign original image to originalImage
            BufferedImage convertedImage = ImageUtils.convertStringToBufferedImage(imagePath);
            assert convertedImage != null;
            originalImage = new ProcessedImage(convertedImage, UTIL.extractColorPalette(convertedImage));

            // set image path to imageFilePath text field
            imageFilePath.setText(imagePath);

            // enable quantization algorithm combo box
            imageQuantizationAlgorithmHBox.setDisable(false);

            // create image object
            Image image = new Image("file:" + imagePath);

            // set image to original image view
            originalImageView.setImage(image);

            BufferedImage originalBufferedImage = ImageUtils.convertStringToBufferedImage(imagePath);
            processedImage = new ProcessedImage(originalBufferedImage, UTIL.extractColorPalette(Objects.requireNonNull(originalBufferedImage)));
            // center the image inside the image view
            ImageUtils.centerImage(originalImageView);

            // set original image size
            originalImageSizeTextView.setText("Size (KB): " + (int) (new File(imagePath).length() / 1024));

            //set original image num of color
            originalImageNumOfColorsTextView.setText("Num of color: " + processedImage.getNumberOfColors());

            // make the image centered in the imageView
            originalImageView.setPreserveRatio(true);

            //enable originalColorPalette & originalHistogram
            originalColorPalette.setDisable(false);
            originalHistogram.setDisable(false);

        }

    }

    // quantize image action event handler
    @FXML
    public void quantizeImageAction(ActionEvent event) throws IOException {

        // make sure quantization algorithm is selected
        if (selectedAlgorithm != null) {
            // get image path
            String imagePath = imageFilePath.getText();

            // create image object
            Image image;

            switch (selectedAlgorithm) {

                case "Uniform" -> {
                    image = ImageUtils.convertBufferedImageToJavaFXImage(uniformQuantizeImage(imagePath, textValue));
                    processedImage = ColorQuantizer.uniformQuantization(imagePath, textValue);
                    quantizedImageView.setImage(image);
                    quantizedImageSizeTextView.setText("Size (KB): " + Objects.requireNonNull(processedImage).getImageSize());
                    quantizedImageNumOfColorsTextView.setText("Num of color: " + Objects.requireNonNull(processedImage).getNumberOfColors());
                }
                case "k Means" -> {
                    image = ImageUtils.convertBufferedImageToJavaFXImage(KMeansQuantizeImage(ImageUtils.convertStringToBufferedImage(imagePath), textValue));
                    processedImage = ColorQuantizer.kMeans(ImageUtils.convertStringToBufferedImage(imagePath), textValue);
                    quantizedImageView.setImage(image);
//                    quantizedImageSizeTextView.setText("Size (KB): " + Objects.requireNonNull(processedImage).getImageSize());
                    quantizedImageNumOfColorsTextView.setText("Num of color: " + Objects.requireNonNull(processedImage).getNumberOfColors());
                }
                case "Median Cut" -> {
                    image = ImageUtils.convertBufferedImageToJavaFXImage(medianCutQuantizeImage(imagePath, textValue));
                    quantizedImageView.setImage(image);
//                    quantizedImageSizeTextView.setText("Size (KB): " + Objects.requireNonNull(ColorQuantizer.medianCut(imagePath, textValue)).getImageSize());
                    quantizedImageNumOfColorsTextView.setText("Num of color: " + Objects.requireNonNull(ColorQuantizer.medianCut(imagePath, textValue)).getNumberOfColors());
                    processedImage = ColorQuantizer.medianCut(imagePath, textValue);
                }
            }


            // TODO: quantize image

            // set image to quantized image view
//            quantizedImageView.setImage(image);

            // make the image centered in the imageView
            quantizedImageView.setPreserveRatio(true);

            // center the image inside the image view
            ImageUtils.centerImage(quantizedImageView);

            //enable show color palette & show histogram for quantized image
            quantizedColorPalette.setDisable(false);
            quantizedHistogram.setDisable(false);

        } else {
            // show alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Please Select a Quantization Algorithm");
        }
    }


    private BufferedImage uniformQuantizeImage(String imagePath, int value) {
        return ColorQuantizer.uniformQuantization(imagePath, value).getImage();
    }

    private BufferedImage medianCutQuantizeImage(String inputPath, int depth) throws IOException {
        return ColorQuantizer.medianCut(inputPath, depth).getImage();
    }

    private BufferedImage KMeansQuantizeImage(BufferedImage inputPath, int depth) throws IOException {
        return ColorQuantizer.kMeans(inputPath, depth).getImage();
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
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));

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

    public static boolean isNotInteger(String input) {
        try {
            Integer.parseInt(input);
            return false; // Input is a valid integer
        } catch (Exception e) {
            return true; // Input is not a valid integer
        }
    }


    /**
     * Color palette action event handler
     */
    public void colorPalette(Set<Color> colors) {
        int GRID_SIZE = (int) Math.sqrt(colors.size());  // Number of columns and rows in the grid
        int RECTANGLE_SIZE = 15;  // Size of each color rectangle

        Stage colorPaletteStage = new Stage();
        GridPane gridPane = new GridPane();
        gridPane.setHgap(1);
        gridPane.setVgap(1);
//        gridPane.setPadding(new Insets(10, 10, 10, 10));

        int rowIndex = 0;
        int colIndex = 0;
        for (Color color : colors) {
            javafx.scene.paint.Color convertedColor = ColorUtils.convertColorToPaint(color);
            Rectangle rectangle = new Rectangle(RECTANGLE_SIZE, RECTANGLE_SIZE, convertedColor);
            gridPane.add(rectangle, colIndex, rowIndex);

            colIndex++;
            if (colIndex >= GRID_SIZE) {
                colIndex = 0;
                rowIndex++;
            }
        }

        Scene scene = new Scene(gridPane);
        colorPaletteStage.setScene(scene);
        colorPaletteStage.setTitle("Color Palette");
        colorPaletteStage.show();
    }

    public void displayColorPalette(ActionEvent event) throws IOException {
        colorPalette(processedImage.getColorPalette());
    }

    public void displayOriginalColorPalette(ActionEvent event) throws IOException {
        colorPalette(originalImage.getColorPalette());
    }


    /**
     * Histogram action event handler
     */
    public void histogram(BufferedImage bufferedImage) {
        Histogram histogram = new Histogram();
        histogram.setImage(bufferedImage);
        histogram.display();
    }

    public void displayQuantizedHistogram(ActionEvent event) throws IOException {
        histogram(processedImage.getImage());
    }

    public void displayOriginalHistogram(ActionEvent event) throws IOException {
        histogram(originalImage.getImage());
    }
}

