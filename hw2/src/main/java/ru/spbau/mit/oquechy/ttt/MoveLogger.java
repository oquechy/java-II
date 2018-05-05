package ru.spbau.mit.oquechy.ttt;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.jetbrains.annotations.NotNull;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Sign;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Log table rows factory.
 */
public class MoveLogger {

    private Model model;

    public MoveLogger(Model model) {
        this.model = model;
    }

    @NotNull
    public MoveLogger.Log getLog(@NotNull Sign sign, int y, int x) {
        @NotNull SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        @NotNull String position = "(" + x + ", " + y + ")";
        return new MoveLogger.Log(sign, position, format.format(new Date()), model.getMoveCounter());
    }

    public static class Log {
        @NotNull
        private final SimpleStringProperty sign;
        @NotNull
        private final SimpleStringProperty position;
        @NotNull
        private final SimpleStringProperty time;
        @NotNull
        private final SimpleIntegerProperty moveNumber;

        private Log(Sign sign, String position, String time, int moveNumber) {
            this.sign = new SimpleStringProperty(sign.name());
            this.position = new SimpleStringProperty(position);
            this.time = new SimpleStringProperty(time);
            this.moveNumber = new SimpleIntegerProperty(moveNumber);
        }

        public String getPosition() {
            return position.get();
        }

        public String getSign() {
            return sign.get();
        }

        public String getTime() {
            return time.get();
        }

        public int getMoveNumber() {
            return moveNumber.get();
        }
    }
}
