package com.comp2042;

/**
 * The GameController class handles the game logic for Tetris,
 * including user input, brick movement, collision checking,
 * ghost piece calculation, scoring, and updating the GUI.
 * It implements the InputEventListener interface to respond
 * to user and thread events.
 */
public class GameController implements InputEventListener {

    private final Board board = new SimpleBoard(25, 10);
    private final GuiController viewGuiController;

    /**
     * Constructs a GameController with a specified GUI controller.
     * Initializes the first brick, binds scores, and updates the next block preview.
     *
     * @param guiController the GUI controller managing the game's visual elements
     */
    public GameController(GuiController guiController) {
        this.viewGuiController = guiController;
        viewGuiController.setEventListener(this);

        // Initialize the first brick and game view
        board.createNewBrick();
        viewGuiController.initGameView(board.getBoardMatrix(), board.getViewData());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.lineScore(board.getScore().lineProperty());

        // Show the first next block preview
        updateNextBlock();
        // Show held block (will be empty initially)
        updateHeldBlock();
    }

    /**
     * Handles the "down" move event.
     * Moves the current brick down if possible, merges it if blocked,
     * clears completed rows, updates score, and refreshes the GUI.
     *
     * @param event the move event containing event type and source
     * @return DownData object containing clear row info, updated view data, and lock status
     */
    @Override
    public DownData onDownEvent(MoveEvent event) {
        boolean canMove = board.moveBrickDown();
        ClearRow clearRow = null;
        boolean locked = false;

        if (!canMove) {
            board.mergeBrickToBackground();
            viewGuiController.clearGhostPiece();
            int[][] boardBeforeClear = board.getBoardMatrix();
            clearRow = board.clearRows();
            locked = true;

            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
                int[][] newBoardMatrix = board.getBoardMatrix();
                viewGuiController.showLineClearEffectWithBoard(boardBeforeClear, clearRow.getLinesRemoved(), newBoardMatrix);
            } else {
                viewGuiController.refreshGameBackground(board.getBoardMatrix());
            }
            if (board.createNewBrick()) {
                viewGuiController.gameOver();
            }

            updateNextBlock();
        }

        updateGhostPiece();
        return new DownData(clearRow, board.getViewData(), locked);
    }

    /**
     * Handles the "left" move event.
     * Moves the current brick left if possible and updates the ghost piece.
     *
     * @param event the move event containing event type and source
     * @return updated ViewData of the brick
     */
    @Override
    public ViewData onLeftEvent(MoveEvent event) {
        board.moveBrickLeft();
        updateGhostPiece();
        return board.getViewData();
    }

    /**
     * Handles the "right" move event.
     * Moves the current brick right if possible and updates the ghost piece.
     *
     * @param event the move event containing event type and source
     * @return updated ViewData of the brick
     */
    @Override
    public ViewData onRightEvent(MoveEvent event) {
        board.moveBrickRight();
        updateGhostPiece();
        return board.getViewData();
    }

    /**
     * Handles the "rotate" event.
     * Rotates the current brick if possible and updates the ghost piece.
     *
     * @param event the move event containing event type and source
     * @return updated ViewData of the brick
     */
    @Override
    public ViewData onRotateEvent(MoveEvent event) {
        board.rotateLeftBrick();
        updateGhostPiece();
        return board.getViewData();
    }

    /**
     * New game after restart
     */
    @Override
    public void createNewGame() {
        board.newGame();
        board.createNewBrick();
        viewGuiController.refreshGameBackground(board.getBoardMatrix());
        viewGuiController.bindScore(board.getScore().scoreProperty());
        viewGuiController.lineScore(board.getScore().lineProperty());

        // Show next block
        updateNextBlock();
        // Show held block (will be empty initially)
        updateHeldBlock();
    }

    /**
     * Updates the Held Block preview in the GUI
     */
    private void updateHeldBlock() {
        HoldShapeInfo heldShape = board.getHeldShape();
        if (heldShape != null) {
            viewGuiController.showHoldBlock(heldShape);
        } else {
            viewGuiController.clearHoldBlock();
        }
    }

    /**
     * Checks whether the current brick can move down to a new Y position without
     * colliding with existing blocks or going out of the board's boundaries.
     *
     *
     * @param brick the current brick's view data, including shape and position
     * @param newY the proposed new Y-coordinate after moving down
     * @return true if the brick can safely move down; false if a collision would occur
     */

    @Override
    public boolean canMoveDown(ViewData brick, int newY) {
        final int HIDDEN_ROWS = 2;
        int[][] shape = brick.getBrickData();
        int xPos = brick.getxPosition();
        int[][] boardMatrix = board.getBoardMatrix();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int boardY = newY + i - HIDDEN_ROWS;
                    int boardX = xPos + j;

                    if (boardY >= boardMatrix.length || boardX >= boardMatrix[0].length) return false;
                    if (boardMatrix[boardY][boardX] != 0) return false;
                }
            }
        }
        return true;
    }

    /**
     * Updates the Next Block preview in the GUI
     */
    private void updateNextBlock() {
        NextShapeInfo nextShape = board.getNextShape();
        if (nextShape != null) {
            viewGuiController.showNextBlock(nextShape);
        }
    }

    /**
     * Update the ghost piece landing at
     */
    private void updateGhostPiece() {
        int[][] ghostData = getGhostPiecePosition();
        viewGuiController.drawGhostPiece(ghostData);
    }

    /**
     * Finds how far down the brick can fall before colliding
     * @return
     */
    private int[][] getGhostPiecePosition() {
        int[][] currentShape = board.getViewData().getBrickData();
        int currentX = board.getViewData().getxPosition();
        int currentY = board.getViewData().getyPosition();

        int ghostY = currentY;

        // Move down until collision
        while (!checkCollision(currentShape, currentX, ghostY + 1)) {
            ghostY++;
        }

        // Return shape + ghost position
        return new int[][] {
                {currentShape[0][0], currentShape[0][1], currentShape[0][2], currentShape[0][3]},
                {currentShape[1][0], currentShape[1][1], currentShape[1][2], currentShape[1][3]},
                {currentShape[2][0], currentShape[2][1], currentShape[2][2], currentShape[2][3]},
                {currentShape[3][0], currentShape[3][1], currentShape[3][2], currentShape[3][3]},
                {currentX, ghostY}
        };
    }

    /**
     * Checks whether it will collide with other block
     *
     * @param shape the 2D matrix representing the current brick's shape
     * @param x the x-coordinate where the shape will be placed
     * @param y the y-coordinate where the shape will be placed
     * @return true if a collision will occur; false otherwise
     */

    private boolean checkCollision(int[][] shape, int x,int y) {
        int[][] boardMatrix = board.getBoardMatrix();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int boardX = x + j;
                    int boardY = y + i;

                    if (boardX < 0 || boardX >= boardMatrix[0].length || boardY >= boardMatrix.length || (boardY >= 0 && boardMatrix[boardY][boardX] != 0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Handles the "hard drop" event.
     * Drops the current brick instantly to the bottom and returns the resulting DownData.
     *
     * @param event the move event containing event type and source
     * @return DownData object containing clear row info, updated view data, and lock status
     */
    @Override
    public DownData onHardDropEvent(MoveEvent event) {
        DownData downData;
        do {
            downData = onDownEvent(new MoveEvent(EventType.DOWN, EventSource.USER));
        } while (!downData.isLocked());
        return downData;
    }

    /**
     * Handles the "hold" event.
     * Swaps the current brick with the held brick (if any).
     *
     * @return HoldEvent containing the new current piece and held piece information, or null if hold was already used
     */
    @Override
    public HoldEvent onHoldEvent() {
        HoldShapeInfo holdInfo = board.holdBrick();
        if (holdInfo == null) {
            return null; // Hold was already used for this piece
        }

        // Update the view with the new current piece
        ViewData newViewData = board.getViewData();
        updateGhostPiece();

        // Update next block preview
        updateNextBlock();
        // Update held block preview
        updateHeldBlock();

        return new HoldEvent(newViewData, holdInfo);
    }




}
