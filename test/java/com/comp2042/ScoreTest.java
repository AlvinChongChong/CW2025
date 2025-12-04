package com.comp2042;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link Score}.
 */
class ScoreTest {

    @Test
    void add_shouldIncreaseScoreByGivenValue() {
        Score score = new Score();

        score.add(50);
        score.add(25);

        assertEquals(75, score.scoreProperty().get());
    }

    @Test
    void addLine_shouldIncreaseLineCountByGivenValue() {
        Score score = new Score();

        score.addLine(2);
        score.addLine(3);

        assertEquals(5, score.lineProperty().get());
    }

    @Test
    void reset_shouldSetScoreAndLinesBackToZero() {
        Score score = new Score();
        score.add(100);
        score.addLine(4);

        score.reset();

        assertEquals(0, score.scoreProperty().get());
        assertEquals(0, score.lineProperty().get());
    }
}


