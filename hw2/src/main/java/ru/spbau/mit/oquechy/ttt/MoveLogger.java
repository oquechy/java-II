package ru.spbau.mit.oquechy.ttt;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Sign;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MoveLogger {

    private Model model;

    public MoveLogger(Model model) {
        this.model = model;
    }

    public static class Log {
        private final SimpleStringProperty sign;
        private final SimpleStringProperty position;
        private final SimpleStringProperty time;
        private final SimpleIntegerProperty moveNumber;

        public String getPosition() {
            return position.get();
        }

        public void setPosition(String position) {
            this.position.set(position);
        }

        public void setSign(String sign) {
            this.sign.set(sign);
        }

        public void setTime(String time) {
            this.time.set(time);
        }

        public void setMoveNumber(int moveNumber) {
            this.moveNumber.set(moveNumber);
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

        private Log(Sign sign, String position, String time, int moveNumber) {
            this.sign = new SimpleStringProperty(sign.name());
            this.position = new SimpleStringProperty(position);
            this.time = new SimpleStringProperty(time);
            this.moveNumber = new SimpleIntegerProperty(moveNumber);
        }
    }

    public MoveLogger.Log getLog(Sign sign, int y, int x) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String position = "(" + x + ", " + y + ")";
        return new MoveLogger.Log(sign, position, format.format(new Date()), model.getMoveCounter());
    }
}
