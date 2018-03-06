package ru.spbau.mit.oquechy.ttt;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import ru.spbau.mit.oquechy.ttt.bot.Bot;
import ru.spbau.mit.oquechy.ttt.bot.BruteForceBot;
import ru.spbau.mit.oquechy.ttt.bot.RandomBot;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Sign;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Controller {

    private enum ViewMode {
        LOG,
        FIELD;

        public ViewMode flip() {
            return this == LOG ? FIELD : LOG;
        }
    }

    private enum State {
        ACTIVE,
        INACTIVE
    }

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
    @FXML
    private TableColumn<MoveLogger.Log, String> sign;
    @FXML
    private TableColumn<MoveLogger.Log, String> position;
    @FXML
    private TableColumn<MoveLogger.Log, String> time;
    @FXML
    private TableColumn<MoveLogger.Log, Integer> moves;
    @FXML
    private TableView<MoveLogger.Log> table;

    private Model model;
    private ViewMode mode = ViewMode.FIELD;
    private State state = State.INACTIVE;
    private Bot bot;
    private ToggleGroup botLevel;
    private final ObservableList<MoveLogger.Log> log = FXCollections.observableArrayList();
    private MoveLogger logger;

    public void initialize() {
        botLevel = new ToggleGroup();
        easyBot.setToggleGroup(botLevel);
        hardBot.setToggleGroup(botLevel);

        welcome(true);

        setColumnsWidth();

        table.setItems(log);
        sign.setCellValueFactory(new PropertyValueFactory<>("sign"));
        position.setCellValueFactory(new PropertyValueFactory<>("position"));
        time.setCellValueFactory(new PropertyValueFactory<>("time"));
        moves.setCellValueFactory(new PropertyValueFactory<>("moveNumber"));

        ObservableList<Node> children = grid.getChildren();
        for (int i = 0; i < children.size(); i++) {
            setCellActions(i, children.get(i));
        }

        setGridActions();
        // text-cursor appears when enter the grid without this line
        clearGrid();

        setLogActions();
    }

    private void setColumnsWidth() {
        sign.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        position.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        time.prefWidthProperty().bind(table.widthProperty().multiply(0.3));
        moves.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
    }

    private void setLogActions() {
        logButton.setOnMouseClicked(event -> {
            boolean needGrid = mode == ViewMode.LOG;
                grid.setDisable(!needGrid);
                grid.setVisible(needGrid);
                table.setDisable(needGrid);
                table.setVisible(!needGrid);
                logButton.setText(needGrid ? "Log" : "Field");
                mode = mode.flip();
        });
    }

    private void setCellActions(int i, Node cell) {
        cell.getStyleClass().add("cell");

        cell.setOnMouseClicked(event -> {
            if (state == State.ACTIVE && model.checkMove(i)) {
                moveApproved();
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

    public void newGameOnePlayer(MouseEvent mouseEvent) {
        newGame();

        String id = ((RadioButton) botLevel.getSelectedToggle()).getId();
        bot = id.equals("easyBot") ? new RandomBot(model) : new BruteForceBot(model);
    }

    public void newGameTwoPlayers(MouseEvent mouseEvent) {
        bot = null;
        newGame();
    }

    private void newGame() {
        clearGrid();

        model = new Model(this);
        state = State.ACTIVE;
        message.setText("Cross begins.");
        logger = new MoveLogger(model);
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

        log.add(logger.getLog(sign, y, x));
        message.setText("");
    }

    private void moveApproved() {
        if (bot != null && state == Controller.State.ACTIVE) {
            model.checkMove(bot.newMove());
        }
    }

    public void writeWinner(Sign sign) {
        state = State.INACTIVE;
        message.setText(sign == Sign.X ? "Cross won!" : sign == Sign.O ? "Nought won!" : "Draw!");
    }

}
