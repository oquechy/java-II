package ru.spbau.mit.oquechy.ftp.client.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

/**
 * Graphic application connects to server by hostname,
 * shows the file structure on server and allows to download
 * files from server.
 *
 * Server's structure is dynamically obtained from the server.
 * Each directory is queried once. List of children doesn't update
 * during one session.
 */
public class GUIClient extends Application {

    /**
     * Launches the application.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Opens the window and builds the scene.
     *
     * @param primaryStage window
     * @throws Exception if uploading of FXML fails
     */
    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("GUIClient.fxml"));
        primaryStage.setTitle("Getty");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
        @NotNull Scene scene = new Scene(root);
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(150);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Closes connection when application is stopped.
     */
    @Override
    public void stop() {
        @NotNull FXMLLoader loader = new FXMLLoader(getClass().getResource("GUIClient.fxml"));
        GUIController controller = loader.getController();
        if (controller != null) {
            controller.closeConnection();
        }
    }
}
