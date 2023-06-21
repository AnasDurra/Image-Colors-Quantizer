package com.icq.imagecolorquantizer.controller;

import com.icq.imagecolorquantizer.utils.ImageUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import java.awt.image.BufferedImage;

public class CroppingViewController {

    @FXML
    private ScrollPane scp;

    @FXML
    private HBox root;

    @FXML
    private ImageView imageView;

    @FXML
    private Pane imageViewParent;

    @FXML
    private Button cropButton;

    private Rectangle rectBound;

    private BufferedImage bufferedImage;


    public void initialize(BufferedImage bufferedImage) {

        this.bufferedImage = bufferedImage;

        // set border for image view
//        imageViewParent.setStyle("-fx-border-color: black; -fx-border-width: 2;");

        // set image to image view
        imageView.setImage(ImageUtils.convertBufferedImageToJavaFXImage(bufferedImage));

        imageView.setPreserveRatio(true);

        // TODO
        imageView.fitWidthProperty().bind(imageViewParent.widthProperty());
        imageView.fitHeightProperty().bind(imageViewParent.heightProperty());


        // bind the image view with event listener
        imageViewParent.addEventFilter(MouseEvent.ANY, this::onMousePressed);

    }

    // add mouse event listener for the image view
    public void onMousePressed(MouseEvent event) {

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {

            // remove old rectangle
            if (rectBound != null && rectBound.getParent() != null) {
                imageViewParent.getChildren().remove(rectBound);
            }

            // create new rectangle for selection
            rectBound = new Rectangle();
            rectBound.setStyle("-fx-stroke: red; -fx-stroke-width: 1;");
            rectBound.setFill(Color.TRANSPARENT);
            rectBound.setStrokeType(StrokeType.INSIDE);
            if (rectBound.getParent() == null) {
                rectBound.setWidth(0.0);
                rectBound.setHeight(0.0);
                rectBound.setLayoutX(event.getX());
                rectBound.setLayoutY(event.getY()); // setX or setY
                imageViewParent.getChildren().add(rectBound);
            }
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            rectBound.setWidth(event.getX() - rectBound.getLayoutX());
            rectBound.setHeight(event.getY() - rectBound.getLayoutY());
        } else if (event.getEventType() == MouseEvent.MOUSE_CLICKED
                && event.getButton() == MouseButton.SECONDARY) {
            if (rectBound.getParent() != null) {
                imageViewParent.getChildren().remove(rectBound);
            }
        }
        // on mouse released enable crop button only if the rectangle is visible
        else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            cropButton.setDisable(!(rectBound.getWidth() > 0) || !(rectBound.getHeight() > 0));
        }

    }

    // crop button action
    @FXML
    public void cropClicked() {
        // crop the input image
        PixelReader reader = ImageUtils.convertBufferedImageToJavaFXImage(bufferedImage).getPixelReader();

        // convert the coordinates to the original image dimensions (after taking the ratio into account)
        double x = rectBound.getLayoutX() * bufferedImage.getWidth() / imageView.getBoundsInLocal().getWidth();
        double y = rectBound.getLayoutY() * bufferedImage.getHeight() / imageView.getBoundsInLocal().getHeight();
        double width = rectBound.getWidth() * bufferedImage.getWidth() / imageView.getBoundsInLocal().getWidth();
        double height = rectBound.getHeight() * bufferedImage.getHeight() / imageView.getBoundsInLocal().getHeight();

        // Crop the image
        WritableImage newImage = new WritableImage(
                reader,
                (int) x,
                (int) y,
                (int) width,
                (int) height
        );

        // remove the old image view
        imageViewParent.getChildren().remove(imageView);

        // add new image view
        imageViewParent.getChildren().add(new ImageView(newImage));

        // remove the rectangle
        imageViewParent.getChildren().remove(rectBound);
        cropButton.setDisable(true);

        // close the stage
        cropButton.getScene().getWindow().hide();

    }

    // getCroppedImage method
    public BufferedImage getCroppedImage() {

        // check that user has selected an area
        if (rectBound == null) {
            return null;
        }

        // crop the input image
        PixelReader reader = ImageUtils.convertBufferedImageToJavaFXImage(bufferedImage).getPixelReader();

        double x = rectBound.getLayoutX() * bufferedImage.getWidth() / imageView.getBoundsInLocal().getWidth();
        double y = rectBound.getLayoutY() * bufferedImage.getHeight() / imageView.getBoundsInLocal().getHeight();
        double width = rectBound.getWidth() * bufferedImage.getWidth() / imageView.getBoundsInLocal().getWidth();
        double height = rectBound.getHeight() * bufferedImage.getHeight() / imageView.getBoundsInLocal().getHeight();

        // Crop the image
        WritableImage newImage = new WritableImage(
                reader,
                (int) x,
                (int) y,
                (int) width,
                (int) height
        );
        return ImageUtils.convertJavaFXImageToBufferedImage(newImage);
    }


}
