package com.icq.imagecolorquantizer.controller;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class MenuViewController {

    @FXML
    private Button colorQuantizerButton;
    @FXML
    private Button imageMatcherButton;

    @FXML
    void colorQuantizerAction(ActionEvent event) throws IOException {


        // create root node
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/icq/imagecolorquantizer/main-view.fxml")));

        //create new stage
        Stage stage = new Stage();

        // create scene
        Scene scene = new Scene(root, Color.WHITE);

        // set title of stage
        stage.setTitle("Image Color Quantizer");

        // set app icon
        Image appIcon = new Image("images/app_logo.png");
        stage.getIcons().add(appIcon);


        // set stage size
        stage.setResizable(false);

        // set scene
        stage.setScene(scene);

        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(imageMatcherButton.getScene().getWindow());

        stage.show();

    }


    @FXML
    void imagesMatcherAction(ActionEvent event) throws IOException {
        // create root node
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/icq/imagecolorquantizer/images-matcher-view.fxml")));

        //create new stage
        Stage stage = new Stage();

        // create scene
        Scene scene = new Scene(root, Color.WHITE);

        // set title of stage
        stage.setTitle("Image Matcher");

        // set app icon
        Image appIcon = new Image("images/app_logo.png");
        stage.getIcons().add(appIcon);


        //set full screen
//        stage.setFullScreen(true);

        // set stage size
        stage.setResizable(false);

        // set scene
        stage.setScene(scene);

        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(imageMatcherButton.getScene().getWindow());

        stage.show();

    }

}


