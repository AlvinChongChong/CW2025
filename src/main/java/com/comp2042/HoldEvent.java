package com.comp2042;

/**
 * Represents an event where a Tetris piece is held.
 * <p>
 * This class stores information about the newly active piece after a hold
 * operation and the piece currently held in the hold slot.
 * </p>
 */
public class HoldEvent {

    /** The new piece that becomes active after holding the current piece. */
    private final ViewData newCurrentPiece;

    /** The piece that is placed in the hold slot. */
    private final HoldShapeInfo holdPiece;

    /**
     * Constructs a new {@code HoldEvent} with the specified current and hold pieces.
     *
     * @param newCurrentPiece the piece that becomes active after holding
     * @param holdPiece       the piece that is now in the hold slot
     */
    public HoldEvent(ViewData newCurrentPiece, HoldShapeInfo holdPiece) {
        this.newCurrentPiece = newCurrentPiece;
        this.holdPiece = holdPiece;
    }

    /**
     * Returns the piece that becomes the current active piece after the hold.
     *
     * @return the new current piece
     */
    public ViewData getNewCurrentPiece() {
        return newCurrentPiece;
    }

    /**
     * Returns the piece that is now held in the hold slot.
     *
     * @return the held piece
     */
    public HoldShapeInfo getHoldPiece() {
        return holdPiece;
    }
}
