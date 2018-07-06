package ru.spbau.mit.oquechy.ttt;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.mit.oquechy.ttt.bot.Bot;
import ru.spbau.mit.oquechy.ttt.bot.BruteForceBot;
import ru.spbau.mit.oquechy.ttt.bot.RandomBot;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Position;
import ru.spbau.mit.oquechy.ttt.logic.Sign;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Connections between logic and ui.
 */
public class Controller {

    @FXML
    private PieChart pieChart;
    @FXML
    private Button logButton;
    @FXML
    private Label message;
    @FXML
    private RadioButton hardBot;
    @FXML
    private RadioButton easyBot;
    @FXML
    private GridPane grid;

    private Model model;

    @NotNull
    private ViewMode mode = ViewMode.FIELD;

    @NotNull
    private State state = State.INACTIVE;

    @Nullable
    private Bot bot;

    private ToggleGroup botLevel;
    private long oCount;
    private long drawCount;
    private long xCount;

    /**
     * Assigns actions to javaFX nodes.
     */
    public void initialize() {
        botLevel = new ToggleGroup();
        easyBot.setToggleGroup(botLevel);
        hardBot.setToggleGroup(botLevel);
        uploadStatistics();
        welcome(true);

        ObservableList<Node> children = grid.getChildren();
        for (int i = 0, n = Model.ROW * Model.ROW; i < n; i++) {
            setCellActions(new Position(i / Model.ROW, i % Model.ROW), children.get(i));
        }

        setGridActions();
        // text-cursor appears when enter the grid without this line
        clearGrid();

        setLogActions();
    }

    private void uploadStatistics() {
        try (@NotNull DataInputStream statistics = new DataInputStream(getClass().getResourceAsStream("/ttt.db"))) {
            xCount = statistics.readLong();
            oCount = statistics.readLong();
            drawCount = statistics.readLong();
            pieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("X " + xCount, xCount),
                    new PieChart.Data("O " + oCount, oCount),
                    new PieChart.Data("Draw " + drawCount, drawCount)
            ));
        } catch (IOException e) {
            xCount = oCount = drawCount = 0;
            pieChart.setData(FXCollections.emptyObservableList());
            pieChart.setTitle("No statistics");
        }
    }

    private void setLogActions() {
        logButton.setOnMouseClicked(event -> {
            boolean needGrid = mode == ViewMode.LOG;
            grid.setDisable(!needGrid);
            grid.setVisible(needGrid);
            pieChart.setDisable(needGrid);
            pieChart.setVisible(!needGrid);
            logButton.setText(needGrid ? "Log" : "Field");
            mode = mode.flip();
        });
    }

    private void setCellActions(@NotNull Position position, Node cell) {
        cell.getStyleClass().add("cell");

        cell.setOnMouseClicked(event -> {
            if (state == State.ACTIVE && model.checkAndSetMove(position)) {
                moveApproved(position);
            } else if (state == State.ACTIVE) {
                cellIsBusy();
            } else {
                welcome(false);
            }
        });
    }

    private void setGridActions() {
        grid.setOnMouseEntered(event -> {
            if (state == State.ACTIVE) {
                grid.getScene().setCursor(Cursor.HAND);
            } else {
                grid.getScene().setCursor(Cursor.DEFAULT);
                welcome(false);
            }
        });
        grid.setOnMouseExited(event -> grid.getScene().setCursor(Cursor.DEFAULT));
    }

    /**
     * Handler for choosing single player mode.
     */
    public void newGameOnePlayer() {
        newGame();

        String id = ((RadioButton) botLevel.getSelectedToggle()).getId();
        bot = id.equals("easyBot") ? new RandomBot(model) : new BruteForceBot(model);
    }

    /**
     * Handler for choosing multi player mode.
     */
    public void newGameTwoPlayers() {
        bot = null;
        newGame();
    }

    private void newGame() {
        clearGrid();

        model = new Model();
        state = State.ACTIVE;
        message.setText("Cross begins.");
    }

    private void clearGrid() {
        for (Node node : grid.getChildren()) {
            if (node instanceof TextField) {
                ((TextField) node).clear();
                node.getStyleClass().clear();
                node.getStyleClass().add("cell");
            }
        }
    }

    private void welcome(boolean firstTime) {
        message.setText((firstTime ? "Hello! " : "") + "Choose the game mode.");
    }

    private void cellIsBusy() {
        message.setText("This cell is busy.");
    }

    /**
     * Puts sign on ui field.
     *
     * @param y    first coordinate
     * @param x    second coordinate
     * @param sign sign to be put
     */
    private void writeSign(int y, int x, Sign sign) {
        @NotNull TextField textField = (TextField) grid.getChildren().get(y * Model.ROW + x);
        textField.getStyleClass().clear();

        if (sign == Sign.X) {
            textField.getStyleClass().add("cross");
            textField.setText("X");
        } else if (sign == Sign.O) {
            textField.getStyleClass().add("nought");
            textField.setText("O");
        }

        message.setText("");
    }

    private void moveApproved(Position position) {
        writeSign(position.getX(), position.getY(), model.getSign(position));
        if (model.checkWin()) {
            writeWinner(model.getResult());
            return;
        }
        if (bot != null && state == Controller.State.ACTIVE) {
            position = bot.newMove();
            model.checkAndSetMove(position);
            writeSign(position.getX(), position.getY(), model.getSign(position));
            if (model.checkWin()) {
                writeWinner(model.getResult());
            }
        }
    }

    /**
     * Puts the result of the round on screen.
     *
     * @param sign winner
     */
    private void writeWinner(Sign sign) {
        state = State.INACTIVE;
        message.setText(sign == Sign.X ? "Cross won!" : sign == Sign.O ? "Nought won!" : "Draw!");
        updateStatistics(sign);
    }

    private void updateStatistics(Sign sign) {
        xCount += sign == Sign.X ? 1 : 0;
        oCount += sign == Sign.O ? 1 : 0;
        drawCount += sign == Sign.N ? 1 : 0;

        try (@NotNull FileOutputStream out = new FileOutputStream(getClass().getResource("/ttt.db").getPath());
             @NotNull DataOutputStream statistics = new DataOutputStream(out)) {
            statistics.writeLong(xCount);
            statistics.writeLong(oCount);
            statistics.writeLong(drawCount);
        } catch (IOException e) {
            pieChart.setTitle("No statistics");
        }
        pieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("X " + xCount, xCount),
                new PieChart.Data("O " + oCount, oCount),
                new PieChart.Data("Draw " + drawCount, drawCount)
        ));
    }

    private enum ViewMode {
        LOG,
        FIELD;

        @NotNull
        public ViewMode flip() {
            return this == LOG ? FIELD : LOG;
        }
    }

    private enum State {
        ACTIVE,
        INACTIVE
    }
}
