package ru.spbau.mit.oquechy.ttt.bot;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.spbau.mit.oquechy.ttt.Controller;
import ru.spbau.mit.oquechy.ttt.logic.Model;

import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

class BotTest {

    private Bot[] bots;
    private Model model;
    @Mock private Controller controller;

    private boolean endOfGame;


    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        model = new Model(controller);
        bots = new Bot[]{new RandomBot(model), new BruteForceBot(model)};
        endOfGame = false;
    }

    @RepeatedTest(100)
    void newMove() {
        for (@NotNull Bot bot : bots) {
            testGame(bot);
        }
    }

    private void testGame(@NotNull Bot bot) {
        @NotNull Random random = new Random();

        doAnswer(invocation -> endOfGame = true).when(controller).writeWinner(any());

        while (!endOfGame){
            int i = random.nextInt(9);

            if (model.isBusy(i)) {
                continue;
            }
            model.checkMove(i);

            if (!endOfGame) {
                int m = bot.newMove();
                assertThat(model.checkMove(m), is(true));
            }
        }
    }
}