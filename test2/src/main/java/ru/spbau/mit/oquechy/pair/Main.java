package ru.spbau.mit.oquechy.pair;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

/**
 * Application for playing "Pairs in Paris" game.
 * Accepts one integer parameter as command line argument.
 * Given integer should be even number between 2 and 12 and
 * it defines number of rows and columns on the field.
 */
public class Main extends Application {

    /**
     * Initializes the window and passes size of the filed to {@link Controller}
     * @param args command line arguments should contain one even number between 2 and 12
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the application.
     *
     * @param primaryStage application window
     * @throws Exception then loading of FXML fails
     */
    @Override
    public void start(@NotNull Stage primaryStage) throws Exception {
        @NotNull FXMLLoader loader = new FXMLLoader(getClass().getResource("pair.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Pairs in Paris");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(600);
        primaryStage.show();
        Controller cont = loader.getController();
        cont.setSize(getParameters());
    }
}
