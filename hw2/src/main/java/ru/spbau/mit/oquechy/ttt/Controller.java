package ru.spbau.mit.oquechy.ttt;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import ru.spbau.mit.oquechy.ttt.bot.Bot;
import ru.spbau.mit.oquechy.ttt.bot.BruteForceBot;
import ru.spbau.mit.oquechy.ttt.bot.RandomBot;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Sign;

public class Controller {
    private enum ViewMode {
        STATISTICS,
        FIELD;

        public ViewMode flip() {
            return this == STATISTICS ? FIELD : STATISTICS;
        }
    }

    private enum State {
        ACTIVE,
        INACTIVE
    }

    @FXML
    private Button statisticsButton;
    @FXML
    private ScrollPane scroll;
    @FXML
    private Label message;
    @FXML
    private RadioButton hardBot;
    @FXML
    private RadioButton easyBot;
    @FXML
    private GridPane grid;
    @FXML
    private TableColumn winner;
    @FXML
    private TableColumn moves;
    @FXML
    private TableColumn date;
    @FXML
    private TableView table;

    private Model model;
    private ViewMode mode = ViewMode.FIELD;
    private State state = State.INACTIVE;
    private Bot bot;
    private ToggleGroup botLevel;

    public void initialize() {
        botLevel = new ToggleGroup();
        easyBot.setToggleGroup(botLevel);
        hardBot.setToggleGroup(botLevel);

        welcome(true);

        setColumnsWidth();

        ObservableList<Node> children = grid.getChildren();
        for (int i = 0; i < children.size(); i++) {
            setCellActions(i, children.get(i));
        }

        setGridActions();
        // text-cursor appears when enter the grid without this line
        clearGrid();

        setStatisticsActions();
    }

    private void setColumnsWidth() {
        winner.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        moves.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
        date.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
    }

    private void setStatisticsActions() {
        statisticsButton.setOnMouseClicked(event -> {
            boolean needGrid = mode == ViewMode.STATISTICS;
                grid.setDisable(!needGrid);
                grid.setVisible(needGrid);
                scroll.setDisable(needGrid);
                scroll.setVisible(!needGrid);
                statisticsButton.setText(needGrid ? "Statistics" : "Field");
                mode = mode.flip();
        });
    }

    private void setCellActions(int i, Node cell) {
        cell.getStyleClass().add("cell");

        cell.setOnMouseClicked(event -> {
            if (state == State.ACTIVE) {
                model.checkMove(i);
                if (bot != null && state == State.ACTIVE) {
                    model.checkMove(bot.newMove());
                }
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

    public void newGameOnePlayer(MouseEvent mouseEvent) {
        clearGrid();

        model = new Model(this);
        state = State.ACTIVE;
        message.setText("Cross begins.");

        String id = ((RadioButton) botLevel.getSelectedToggle()).getId();
        bot = id.equals("easyBot") ? new RandomBot(model) : new BruteForceBot(model);
    }

    public void newGameTwoPlayers(MouseEvent mouseEvent) {
        bot = null;
        clearGrid();

        model = new Model(this);
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

    public void cellIsBusy() {
        message.setText("This cell is busy.");
    }

    public void writeSign(int y, int x, Sign sign) {
        TextField textField = (TextField) grid.getChildren().get(y * Model.SIZE + x);
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

    public void writeWinner(Sign sign) {
        state = State.INACTIVE;
        message.setText(sign == Sign.X ? "Cross won!" : sign == Sign.O ? "Nought won!" : "Draw!");
    }
}
