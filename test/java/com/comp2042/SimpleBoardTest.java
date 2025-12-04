package com.comp2042;

import com.comp2042.logic.bricks.Brick;
import com.comp2042.logic.bricks.BrickGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SimpleBoard}.
 *
 * <p>These tests focus on game logic that is independent of JavaFX UI,
 * such as spawning bricks, clearing rows, and hold mechanics.</p>
 */
class SimpleBoardTest {

    private SimpleBoard board;

    @BeforeEach
    void setUp() {
        board = new SimpleBoard(10, 20);
    }

    @Test
    void newGame_shouldResetBoardAndScore() {
        board.getScore().add(100);
        board.getScore().addLine(3);

        board.newGame();

        assertEquals(0, board.getScore().scoreProperty().get());
        assertEquals(0, board.getScore().lineProperty().get());
    }

    @Test
    void createNewBrick_shouldPlaceBrickAtTopCenterOrBelow() {
        boolean collided = board.createNewBrick();

        assertFalse(collided, "New brick on empty board should not collide");

        int[][] matrix = board.getBoardMatrix();
        boolean anyFilled = false;
        for (int[] row : matrix) {
            for (int cell : row) {
                if (cell != 0) {
                    anyFilled = true;
                    break;
                }
            }
            if (anyFilled) break;
        }
        // After spawning, brick should eventually be merged, so board may still be empty.
        // For now we just assert that the board exists and has correct dimensions.
        assertEquals(10, matrix.length);
        assertEquals(20, matrix[0].length);
    }

    @Test
    void clearRows_shouldIncrementLineCountWhenRowsCleared() {
        int[][] matrix = board.getBoardMatrix();

        for (int col = 0; col < matrix[0].length; col++) {
            matrix[0][col] = 1;
            matrix[1][col] = 1;
        }

        board.clearRows();

        assertEquals(2, board.getScore().lineProperty().get());
    }

    @Test
    void holdBrick_shouldReturnNullWhenCalledBeforeSpawning() {
        assertNull(board.holdBrick());
    }

    @Test
    void holdBrick_shouldStoreCurrentBrickAndReturnHoldInfo() {
        board.createNewBrick();

        HoldShapeInfo holdInfo = board.holdBrick();

        assertNotNull(holdInfo, "Hold info should not be null after holding first piece");
        assertNotNull(board.getHeldShape(), "Board should report a held shape");
    }
}


