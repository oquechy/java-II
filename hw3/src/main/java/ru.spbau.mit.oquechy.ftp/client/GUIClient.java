package ru.spbau.mit.oquechy.ftp.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class GUIClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("GUIClient.fxml"));
        primaryStage.setTitle("Getty");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIClient.fxml"));
        GUIController controller = loader.getController();
        if (controller != null) {
            controller.closeConnection();
        }
    }
}
