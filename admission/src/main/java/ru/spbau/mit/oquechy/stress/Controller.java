package ru.spbau.mit.oquechy.stress;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import ru.spbau.mit.oquechy.stress.server.Server;
import ru.spbau.mit.oquechy.stress.server.ServerNonBlocking;
import ru.spbau.mit.oquechy.stress.server.ServerSeparateThreads;
import ru.spbau.mit.oquechy.stress.server.ServerThreadPool;
import ru.spbau.mit.oquechy.stress.types.Architecture;
import ru.spbau.mit.oquechy.stress.types.Property;
import ru.spbau.mit.oquechy.stress.utils.Logger;
import ru.spbau.mit.oquechy.stress.utils.Statistic;

import java.io.IOException;

public class Controller {

    private static final String CSS_INVALID_NUMBER = "-fx-control-inner-background: pink";
    private static final String CSS_INVALID_CHOICE = "-fx-background-color: pink";
    private static final String CSS_VALID = "";

    @FXML
    private HBox parent;
    @FXML
    private LineChart<Integer, Number> chart;
    @FXML
    private ChoiceBox<Property> propertyCB;
    @FXML
    private ChoiceBox<Architecture> architectureCB;
    @FXML
    private TextField iterationsTF;
    @FXML
    private TextField stepTF;
    @FXML
    private TextField lengthTF;
    @FXML
    private TextField queriesTF;
    @FXML
    private TextField clientsTF;
    @FXML
    private TextField pauseTF;

    @FXML
    private void stress() throws IOException, InterruptedException {
        chart.getData().clear();
        Architecture architecture = getObject(architectureCB);
        Property property = getObject(propertyCB);

        int iterations = getInt(iterationsTF);
        int step = getInt(stepTF);
        int queries = getInt(queriesTF);
        int length = getInt(lengthTF);
        int clients = getInt(clientsTF);
        int pause = getInt(pauseTF);

        System.out.println("Checking");
        if (iterations != 0 && step != 0 && queries != 0 && length != 0
                && clients != 0 && pause != 0
                && architecture != null
                && property != null) {
            parent.setDisable(true);
            plot(architecture, property, iterations, step, queries, length, clients, pause);
            parent.setDisable(false);
        }
    }

    private void plot(Architecture architecture, Property property, int iterations, int step,
                      int queries, int length, int clients, int pause)
            throws IOException, InterruptedException {
        Server server;
        switch (architecture) {
            case SEPARATE_THREAD:
                server = new ServerSeparateThreads();
                break;
            case THREAD_POOL:
                server = new ServerThreadPool();
                break;
            case NON_BLOCKING:
                server = new ServerNonBlocking();
                break;
            default:
                throw new UnsupportedOperationException();
        }
        XYChart.Series<Integer, Number> sorting = new XYChart.Series<>();
        XYChart.Series<Integer, Number> transmitting = new XYChart.Series<>();
        XYChart.Series<Integer, Number> serving = new XYChart.Series<>();
        sorting.setName("Sorting time");
        transmitting.setName("Server time");
        serving.setName("Client time");
        try (Logger logger = new Logger(architecture, property, iterations, step, queries, length, clients, pause)) {
            for (int i = 1; i <= iterations; i++) {
                Statistic statistic = server.run(clients, queries);
                length += (property == Property.N ? 1 : 0) * step;
                clients += (property == Property.M ? 1 : 0) * step;
                pause += (property == Property.D ? 1 : 0) * step;

                sorting.getData().add(new XYChart.Data<>(i, statistic.getSortingTime()));
                transmitting.getData().add(new XYChart.Data<>(i, statistic.getTransmittingTime()));
                serving.getData().add(new XYChart.Data<>(i, statistic.getServingTime()));

                logger.add(statistic);
            }
        }
        chart.getData().add(sorting);
        chart.getData().add(transmitting);
        chart.getData().add(serving);
    }

    private <T> T getObject(ChoiceBox<T> choiceBox) {
        T value = choiceBox.getValue();
        if (value == null) {
            choiceBox.setStyle(CSS_INVALID_CHOICE);
        } else {
            choiceBox.setStyle(CSS_VALID);
        }
        return value;
    }

    private int getInt(TextField textField) {
        int i = 0;
        try {
            i = Integer.parseInt(textField.getText());
        } catch (NumberFormatException ignored) {
        }

        if (i > 0) {
            textField.setStyle(CSS_VALID);
            return i;
        } else {
            textField.setStyle(CSS_INVALID_NUMBER);
            return 0;
        }
    }
}
