package com.cheemsquest.console;

import com.cheemsquest.engine.ConstantRandom;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleGameTest {

    private static String play(String input) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        var in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        var out = new PrintStream(output, true, StandardCharsets.UTF_8);

        new ConsoleGame(in, out, ConstantRandom.NEUTRAL).run();

        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void scriptedPlaythroughEndsInVictory() {
        // Bonk the small doge twice, then three Bonks finish Karen (target auto-picked).
        String output = play("""
                1
                2
                1
                2
                1
                1
                1
                """);

        assertThat(output)
                .contains("Neighbor's Lil Doge is down!")
                .contains("Wild Karen Doge is down!")
                .contains("VICTORY!");
    }

    @Test
    void invalidInputAsksAgain() {
        String output = play("""
                wow
                7
                1
                2
                """);

        assertThat(output).contains("Much confuse. Pick a number from 1 to 3.");
        assertThat(output).contains("Neighbor's Lil Doge loses 14 HP.");
    }

    @Test
    void endOfInputExitsGracefully() {
        assertThat(play("")).contains("Cheems runs away from the fight");
    }
}
