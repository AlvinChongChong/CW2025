package com.comp2042;

/**
 * Represents the information of a Tetris piece that is held in the hold slot.
 * <p>
 * This includes the shape of the piece and its associated color code.
 * </p>
 */
public class HoldShapeInfo {

    /** The 2D array representing the shape of the Tetris piece. */
    private final int[][] shape;

    /** The color code associated with the piece. */
    private final int colorCode;

    /**
     * Constructs a new {@code HoldShapeInfo} with the specified shape and color code.
     *
     * @param shape the 2D array representing the piece's shape
     * @param colorCode the integer code representing the piece's color
     */
    public HoldShapeInfo(int[][] shape, int colorCode) {
        this.shape = shape;
        this.colorCode = colorCode;
    }

    /**
     * Returns the shape of the Tetris piece.
     *
     * @return a 2D array representing the piece's shape
     */
    public int[][] getShape() {
        return shape;
    }

    /**
     * Returns the color code of the Tetris piece.
     *
     * @return the color code as an integer
     */
    public int getColorCode() {
        return colorCode;
    }
}
