package com.icq.imagecolorquantizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
public class App extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // create root node
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = loader.load();

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

        stage.show();
    }
}