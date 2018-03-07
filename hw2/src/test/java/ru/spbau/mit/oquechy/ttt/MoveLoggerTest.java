package ru.spbau.mit.oquechy.ttt;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Sign;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

class MoveLoggerTest {

    private final static SimpleDateFormat DATA_FORMAT = new SimpleDateFormat("HH:mm:ss");

    @Mock
    private Model model;
    private MoveLogger logger;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        logger = new MoveLogger(model);
    }

    @Test
    void getLog() throws ParseException {
        when(model.getMoveCounter()).thenReturn(0).thenReturn(4);

        {
            @NotNull MoveLogger.Log log = logger.getLog(Sign.X, 0, 0);
            assertThat(log.getSign(), is("X"));
            assertThat(log.getPosition(), is("(0, 0)"));
            assertThat(isValidDate(log.getTime()), is(true));
            assertThat(log.getMoveNumber(), is(0));
        }

        {
            @NotNull MoveLogger.Log log = logger.getLog(Sign.O, 2, 1);
            assertThat(log.getSign(), is("O"));
            assertThat(log.getPosition(), is("(1, 2)"));
            assertThat(isValidDate(log.getTime()), is(true));
            assertThat(log.getMoveNumber(), is(4));
        }

        //noinspection ResultOfMethodCallIgnored
        verify(model, times(2)).getMoveCounter();
        verifyNoMoreInteractions(model);
    }

    private static boolean isValidDate(@NotNull String value) throws ParseException {
        Date date;
        date = DATA_FORMAT.parse(value);
        return value.equals(DATA_FORMAT.format(date));
    }
}
