package ru.spbau.mit.oquechy.ttt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

/**
 * Tic-Tac-Toe desktop application.
 * Supports single and multi player modes.
 * Saves history of moves for current round.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ttt.fxml"));
        primaryStage.setMinHeight(300);
        primaryStage.setMinWidth(300);
        primaryStage.setTitle("Tic-Tac-Toe");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        @NotNull Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
