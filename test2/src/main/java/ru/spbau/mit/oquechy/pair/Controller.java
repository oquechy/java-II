package ru.spbau.mit.oquechy.pair;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Double.MAX_VALUE;

/**
 * Implementation of handlers for UI elements.
 */
public class Controller {
    @FXML
    private GridPane grid;

    private int[][] secretField;
    private int size;
    @Nullable
    private Button selectedButton = null;
    @NotNull
    private List<Button> toClear = new ArrayList<>();
    private int pairsCount;
    private int pairsFound;

    @FXML
    private void setUpField() {
        pairsFound = 0;
        toClear.clear();
        selectedButton = null;
        grid.getRowConstraints().clear();
        grid.getColumnConstraints().clear();
        grid.getChildren().clear();
        for (int i = 0; i < size; i++) {
            @NotNull ColumnConstraints column = new ColumnConstraints(-1, -1, Double.MAX_VALUE);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
            @NotNull RowConstraints row = new RowConstraints(-1, -1, Double.MAX_VALUE);
            row.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(row);
        }
        setUpSecretField(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                @NotNull Button button = new Button();
                button.setUserData(new Position(i, j));
                button.setOnMouseClicked(event -> {
                    for (@NotNull Button b : toClear) {
                        clearButton(b);
                    }
                    toClear.clear();

                    if (button.isDisabled()) {
                        return;
                    }

                    showNumber(button);
                    if (selectedButton == null) {
                        selectedButton = button;
                        return;
                    }

                    if (selectedButton.equals(button)) {
                        return;
                    }

                    if (getNumber(selectedButton) == getNumber(button)) {
                        showNumber(button);
                        selectedButton.setDisable(true);
                        button.setDisable(true);
                        pairsFound++;
                        if (pairsCount == pairsFound) {
                            pairsFound = 0;
                            showEndOfGameAlert();
                            setUpField();
                            return;
                        }
                    } else {
                        toClear.add(selectedButton);
                        toClear.add(button);
                    }

                    selectedButton = null;

                });
                button.setPrefSize(100, 100);
                button.setMaxSize(MAX_VALUE, MAX_VALUE);
                grid.add(button, i, j);
            }
        }
    }

    private void clearButton(Button button) {
        button.setStyle(null);
        button.setText("");
    }

    private void showNumber(Button selectedButton) {
        selectedButton.setStyle("-fx-background-color: mediumturquoise");
        selectedButton.setText(String.valueOf(getNumber(selectedButton)));
    }

    private int getNumber(Button button) {
        @NotNull Position userData = (Position) button.getUserData();
        return secretField[userData.getX()][userData.getY()];
    }

    private void setUpSecretField(int size) {
        secretField = new int[size][size];
        @NotNull Random random = new Random();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                secretField[i][j] = (i * size + j) % pairsCount;
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int x = random.nextInt(size);
                int y = random.nextInt(size);
                int tmp = secretField[i][j];
                secretField[i][j] = secretField[x][y];
                secretField[x][y] = tmp;
            }
        }
    }

    private void showInvalidArgumentsAlert() {
        @NotNull Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid arguments");
        alert.setHeaderText(null);
        alert.setContentText("Rerun application with one even integer between 2 and 16 as argument.");
        alert.showAndWait();
    }

    private void showEndOfGameAlert() {
        @NotNull Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("End of game");
        alert.setHeaderText(null);
        alert.setContentText("Congratulations! You have matched them all!");
        alert.showAndWait();
    }

    private void finish() {
        Scene scene = grid.getScene();
        if (scene != null && scene.getWindow() != null) {
            scene.getWindow().hide();
        }
    }

    /**
     * Accepts and validates parameter of game.
     * Given size should be even number between 2 and 12.
     *
     * @param parameters console arguments
     */
    public void setSize(@NotNull Application.Parameters parameters) {
        List<String> parametersRaw = parameters.getRaw();
        if (parametersRaw.size() != 1) {
            showInvalidArgumentsAlert();
            finish();
            return;
        }
        size = Integer.parseInt(parametersRaw.get(0));
        if (size < 2 || 12 < size || size % 2 == 1) {
            showInvalidArgumentsAlert();
            finish();
        }
        pairsCount = size * size / 2;
    }

    @Data
    private static class Position {
        final private int x;
        final private int y;

        /**
         * Initializes new {@link Position} with coordinates of a button.
         *
         * @param x row
         * @param y column
         */
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
