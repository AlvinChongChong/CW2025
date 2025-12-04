package com.comp2042;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MatrixOperations}.
 */
class MatrixOperationsTest {

    @Test
    void copy_shouldCreateDeepCopyOfMatrix() {
        int[][] original = {
                {1, 2, 3},
                {4, 5, 6}
        };

        int[][] copy = MatrixOperations.copy(original);

        assertNotSame(original, copy, "Copy should be a different array instance");
        assertArrayEquals(original[0], copy[0]);
        assertArrayEquals(original[1], copy[1]);

        // Mutate original and ensure copy is not affected
        original[0][0] = 99;
        assertEquals(1, copy[0][0], "Copy should not change when original is modified");
    }

    @Test
    void merge_shouldOverlayBrickOnMatrixAtGivenPosition() {
        int[][] board = new int[4][4];
        int[][] brick = {
                {1, 1},
                {0, 1}
        };

        int[][] merged = MatrixOperations.merge(board, brick, 1, 1);

        assertEquals(1, merged[1][1]);
        assertEquals(1, merged[1][2]);
        assertEquals(1, merged[2][2]);
    }

    @Test
    void intersect_shouldDetectCollisionAndOutOfBounds() {
        int[][] board = new int[4][4];
        int[][] brick = {
                {1}
        };

        // Place inside bounds - no collision
        assertFalse(MatrixOperations.intersect(board, brick, 1, 1));

        // Place out of bounds horizontally
        assertTrue(MatrixOperations.intersect(board, brick, -1, 1));

        // Mark a filled cell in the board and test collision
        board[2][2] = 1;
        assertTrue(MatrixOperations.intersect(board, brick, 2, 2));
    }

    @Test
    void checkRemoving_shouldClearFullRowsAndCalculateScoreBonus() {
        int[][] matrix = {
                {1, 1, 1, 1}, // full
                {1, 0, 1, 1}, // not full
                {1, 1, 1, 1}  // full
        };

        ClearRow clearRow = MatrixOperations.checkRemoving(matrix);

        assertEquals(2, clearRow.getLinesRemoved());
        assertEquals(50 * 2 * 2, clearRow.getScoreBonus());

        int[][] newMatrix = clearRow.getNewMatrix();
        // top rows should now be empty
        assertArrayEquals(new int[]{0, 0, 0, 0}, newMatrix[0]);
        assertArrayEquals(new int[]{0, 0, 0, 0}, newMatrix[1]);
    }

    @Test
    void deepCopyList_shouldCopyEachMatrixIndependently() {
        List<int[][]> list = new ArrayList<>();
        list.add(new int[][]{{1, 2}, {3, 4}});
        list.add(new int[][]{{5, 6}});

        List<int[][]> copy = MatrixOperations.deepCopyList(list);

        assertEquals(2, copy.size());
        assertNotSame(list.get(0), copy.get(0));
        assertNotSame(list.get(1), copy.get(1));
        assertArrayEquals(list.get(0)[0], copy.get(0)[0]);
        assertArrayEquals(list.get(1)[0], copy.get(1)[0]);

        // Mutate original
        list.get(0)[0][0] = 99;
        assertEquals(1, copy.get(0)[0][0], "Copied list should not change when original list is modified");
    }
}


