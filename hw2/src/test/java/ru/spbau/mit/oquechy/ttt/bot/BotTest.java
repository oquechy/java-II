package ru.spbau.mit.oquechy.ttt.bot;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.MockitoAnnotations;
import ru.spbau.mit.oquechy.ttt.logic.Model;
import ru.spbau.mit.oquechy.ttt.logic.Position;

import java.io.InputStream;
import java.util.Scanner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class BotTest {

    private Bot[] bots;
    private Model model;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        model = new Model();
        bots = new Bot[]{new RandomBot(model), new BruteForceBot(model)};
    }

    @RepeatedTest(100)
    void newMove() {
        for (@NotNull Bot bot : bots) {
            testGame(bot);
        }
    }

    private void testGame(@NotNull Bot bot) {
        InputStream resourceAsStream = getClass().getResourceAsStream("test.txt");
        Scanner scanner = new Scanner(resourceAsStream);
        while (model.getResult() == null) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            Position position = new Position(x, y);

            if (model.isBusy(position)) {
                continue;
            }
            model.checkAndSetMove(position);

            if (model.getResult() == null) {
                Position m = bot.newMove();
                assertThat(model.checkAndSetMove(m), is(true));
            }
        }
    }
}