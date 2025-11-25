package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the local versus mode (two-player Tetris).
 * Manages two separate game boards and handles input for both players.
 */
public class VersusController implements Initializable {

    private static final int BRICK_SIZE = 20;
    private static final int VGAP = 1;

    @FXML private StackPane rootPane;

    @FXML private StackPane pauseMenu;
    @FXML private Button pauseButton;
    @FXML private Button restartButton;
    @FXML private Button musicToggleButton;

    @FXML private GridPane p1GamePanel;
    @FXML private Pane p1BrickOverlay;
    @FXML private Pane p1GroupNotification;
    @FXML private GameOverPanel p1GameOverPanel;
    @FXML private Pane p1NextBlockPane;
    @FXML private Pane p1HoldBlockPane;
    @FXML private ImageView p1ScoreThousands, p1ScoreHundreds, p1ScoreTens, p1ScoreOnes;
    @FXML private ImageView p1LineHundreds, p1LineTens, p1LineOnes;

    @FXML private GridPane p2GamePanel;
    @FXML private Pane p2BrickOverlay;
    @FXML private Pane p2GroupNotification;
    @FXML private GameOverPanel p2GameOverPanel;
    @FXML private Pane p2NextBlockPane;
    @FXML private Pane p2HoldBlockPane;
    @FXML private ImageView p2ScoreThousands, p2ScoreHundreds, p2ScoreTens, p2ScoreOnes;
    @FXML private ImageView p2LineHundreds, p2LineTens, p2LineOnes;

    private Board p1Board;
    private Board p2Board;
    private Rectangle[][] p1DisplayMatrix;
    private Rectangle[][] p2DisplayMatrix;
    private Rectangle[][] p1ActiveBrick;
    private Rectangle[][] p2ActiveBrick;
    private Rectangle[][] p1GhostBrick;
    private Rectangle[][] p2GhostBrick;
    private Rectangle[][] p1GhostPieceRectangles;
    private Rectangle[][] p2GhostPieceRectangles;

    private Timeline p1Timeline;
    private Timeline p2Timeline;

    private boolean p1GameOver = false;
    private boolean p2GameOver = false;
    private boolean isPaused = false;
    private Image[] digits = new Image[10];

    private Stage primaryStage;
    private Rectangle[][] displayMatrix;
    private InputEventListener eventListener;


    /**
     * Sets the primary stage for this controller.
     *
     * @param stage the main Stage of the application
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Initializes the controller after the FXML has been loaded.
     * Sets up both players' boards, creates initial bricks, initializes UI,
     * binds score and line properties, sets key event handlers, and starts the game loops.
     *
     * @param location  URL location used for resolving relative paths (not used)
     * @param resources ResourceBundle for localization (not used)
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDigitImages();

        p1Board = new SimpleBoard(25, 10);
        p2Board = new SimpleBoard(25, 10);
        
        // Create bricks first
        p1Board.createNewBrick();
        p2Board.createNewBrick();

        initPlayerView(1);
        initPlayerView(2);
        
        updatePlayerView(1);
        updatePlayerView(2);
        
        // Scores
        p1Board.getScore().scoreProperty().addListener((obs, oldVal, newVal) -> 
            updateScoreImages(1, newVal.intValue()));
        p2Board.getScore().scoreProperty().addListener((obs, oldVal, newVal) -> 
            updateScoreImages(2, newVal.intValue()));
        p1Board.getScore().lineProperty().addListener((obs, oldVal, newVal) -> 
            updateLineImages(1, newVal.intValue()));
        p2Board.getScore().lineProperty().addListener((obs, oldVal, newVal) -> 
            updateLineImages(2, newVal.intValue()));

        updateNextBlock(1, p1Board.getNextShape());
        updateNextBlock(2, p2Board.getNextShape());

        p1GameOverPanel.setVisible(false);
        p2GameOverPanel.setVisible(false);

        rootPane.setFocusTraversable(true);
        rootPane.requestFocus();
        rootPane.setOnKeyPressed(this::handleKeyPress);

        javafx.application.Platform.runLater(() -> {
            if (rootPane.getScene() != null) {
                rootPane.getScene().setOnKeyPressed(this::handleKeyPress);
                rootPane.requestFocus();
            }
        });

        startGameLoops();

        updateScoreImages(1, 0);
        updateScoreImages(2, 0);
        updateLineImages(1, 0);
        updateLineImages(2, 0);
        updateMusicButtonState();
    }

    /**
     * Initializes the visual representation for a player.
     *
     * @param player 1 for Player 1, 2 for Player 2
     */
    private void initPlayerView(int player) {
        Board board = (player == 1) ? p1Board : p2Board;
        GridPane gamePanel = (player == 1) ? p1GamePanel : p2GamePanel;
        Pane brickOverlay = (player == 1) ? p1BrickOverlay : p2BrickOverlay;
        
        int[][] boardMatrix = board.getBoardMatrix();
        ViewData viewData = board.getViewData();

        Rectangle[][] displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 0; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i);
            }
        }
        
        if (player == 1) {
            p1DisplayMatrix = displayMatrix;
        } else {
            p2DisplayMatrix = displayMatrix;
        }

        Rectangle[][] activeBrick = new Rectangle[viewData.getBrickData().length][viewData.getBrickData()[0].length];
        Rectangle[][] ghostBrick = new Rectangle[viewData.getBrickData().length][viewData.getBrickData()[0].length];
        
        for (int i = 0; i < viewData.getBrickData().length; i++) {
            for (int j = 0; j < viewData.getBrickData()[i].length; j++) {
                Rectangle ghost = new Rectangle(BRICK_SIZE - 1, BRICK_SIZE - 1);
                ghost.setFill(Color.LIGHTGRAY);
                ghost.setOpacity(0.3);
                ghost.setArcWidth(9);
                ghost.setArcHeight(9);
                ghost.setVisible(false);
                ghostBrick[i][j] = ghost;
                brickOverlay.getChildren().add(ghost);
                
                Rectangle active = new Rectangle(BRICK_SIZE - 1, BRICK_SIZE - 1);
                active.setFill(getFillColor(viewData.getBrickData()[i][j]));
                active.setArcWidth(9);
                active.setArcHeight(9);
                active.setVisible(false);
                activeBrick[i][j] = active;
                brickOverlay.getChildren().add(active);
            }
        }
        
        if (player == 1) {
            p1ActiveBrick = activeBrick;
            p1GhostBrick = ghostBrick;
        } else {
            p2ActiveBrick = activeBrick;
            p2GhostBrick = ghostBrick;
        }

        updateBrickPositions(player, viewData);
    }

    private boolean showLineClearEffect(int player, int[][] boardBeforeClear) {
        Rectangle[][] displayMatrix = (player == 1) ? p1DisplayMatrix : p2DisplayMatrix;
        Pane overlay = (player == 1) ? p1BrickOverlay : p2BrickOverlay;
        if (displayMatrix == null || overlay == null || boardBeforeClear == null) return false;

        List<Integer> rowsToFlash = new ArrayList<>();
        for (int i = 0; i < boardBeforeClear.length; i++) {
            boolean rowFull = true;
            for (int j = 0; j < boardBeforeClear[i].length; j++) {
                if (boardBeforeClear[i][j] == 0) {
                    rowFull = false;
                    break;
                }
            }
            if (rowFull) {
                rowsToFlash.add(i);
            }
        }
        if (rowsToFlash.isEmpty()) {
            return false;
        }

        List<Rectangle> flashRects = new ArrayList<>();
        for (Integer row : rowsToFlash) {
            for (int col = 0; col < boardBeforeClear[row].length; col++) {
                if (boardBeforeClear[row][col] == 0) continue;
                Rectangle rect = new Rectangle(BRICK_SIZE - 1, BRICK_SIZE - 1);
                rect.setFill(Color.WHITE);
                rect.setOpacity(0.0);
                rect.setArcWidth(9);
                rect.setArcHeight(9);
                rect.setX(col * BRICK_SIZE);
                rect.setY(row * (BRICK_SIZE + VGAP) - 2);
                flashRects.add(rect);
            }
        }
        if (flashRects.isEmpty()) {
            return false;
        }
        overlay.getChildren().addAll(flashRects);

        Timeline flashTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> setFlashOpacity(flashRects, 0.9)),
                new KeyFrame(Duration.millis(80), e -> setFlashOpacity(flashRects, 0.2)),
                new KeyFrame(Duration.millis(160), e -> setFlashOpacity(flashRects, 0.9)),
                new KeyFrame(Duration.millis(240), e -> setFlashOpacity(flashRects, 0.2)),
                new KeyFrame(Duration.millis(320), e -> setFlashOpacity(flashRects, 0.9))
        );
        flashTimeline.setOnFinished(e -> {
            overlay.getChildren().removeAll(flashRects);
            updatePlayerView(player);
        });
        flashTimeline.play();
        return true;
    }

    private void setFlashOpacity(List<Rectangle> rectangles, double opacity) {
        for (Rectangle rect : rectangles) {
            rect.setOpacity(opacity);
        }
    }

    /**
     * Updates the brick positions for the active piece of a player.
     *
     * @param player   Player number (1 or 2)
     * @param viewData Current brick view data
     */
    private void updateBrickPositions(int player, ViewData viewData) {
        Rectangle[][] activeBrick = (player == 1) ? p1ActiveBrick : p2ActiveBrick;
        
        if (activeBrick == null || viewData == null) return;
        
        for (int i = 0; i < viewData.getBrickData().length && i < activeBrick.length; i++) {
            for (int j = 0; j < viewData.getBrickData()[i].length && j < activeBrick[i].length; j++) {
                Rectangle r = activeBrick[i][j];
                if (r == null) continue;
                
                int value = viewData.getBrickData()[i][j];
                r.setVisible(value != 0);
                if (value != 0) {
                    r.setFill(getFillColor(value));
                    r.setX((viewData.getxPosition() + j) * BRICK_SIZE);
                    r.setY((viewData.getyPosition() + i) * (BRICK_SIZE + VGAP) - 2);
                } else {
                    r.setVisible(false);
                }
            }
        }
    }

    /**
     * Updates the entire player view, including background, active brick, and ghost piece.
     *
     * @param player Player number (1 or 2)
     */
    private void updatePlayerView(int player) {
        Board board = (player == 1) ? p1Board : p2Board;
        ViewData viewData = board.getViewData();
        Rectangle[][] displayMatrix = (player == 1) ? p1DisplayMatrix : p2DisplayMatrix;
        Rectangle[][] activeBrick = (player == 1) ? p1ActiveBrick : p2ActiveBrick;
        
        // Update background
        int[][] boardMatrix = board.getBoardMatrix();
        for (int i = 0; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle r = displayMatrix[i][j];
                r.setFill(getFillColor(boardMatrix[i][j]));
                r.setArcHeight(9);
                r.setArcWidth(9);
            }
        }

        updateBrickPositions(player, viewData);

        updateGhostPiece(player);
    }

    /**
     * Handles all key press events for both players.
     *
     * @param event KeyEvent representing the pressed key
     */
    private void handleKeyPress(KeyEvent event) {
        if (p1GameOver && p2GameOver) return;
        if (isPaused) return;
        if (!p1GameOver) {
            if (event.getCode() == KeyCode.LEFT) {
                p1Board.moveBrickLeft();
                updatePlayerView(1);
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                p1Board.moveBrickRight();
                updatePlayerView(1);
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                p1Board.rotateLeftBrick();
                updatePlayerView(1);
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                handlePlayerDown(1);
                event.consume();
            } else if (event.getCode() == KeyCode.SPACE) {
                handlePlayerHardDrop(1);
                event.consume();
            } else if (event.getCode() == KeyCode.SHIFT) {
                handlePlayerHold(1);
                updatePlayerView(1);
                event.consume();
            }
        }

        if (!p2GameOver) {
            if (event.getCode() == KeyCode.A) {
                p2Board.moveBrickLeft();
                updatePlayerView(2);
                event.consume();
            } else if (event.getCode() == KeyCode.D) {
                p2Board.moveBrickRight();
                updatePlayerView(2);
                event.consume();
            } else if (event.getCode() == KeyCode.W) {
                p2Board.rotateLeftBrick();
                updatePlayerView(2);
                event.consume();
            } else if (event.getCode() == KeyCode.S) {
                handlePlayerDown(2);
                event.consume();
            } else if (event.getCode() == KeyCode.Q) {
                handlePlayerHardDrop(2);
                event.consume();
            } else if (event.getCode() == KeyCode.E) {
                handlePlayerHold(2);
                updatePlayerView(2);
                event.consume();
            }
        }

        rootPane.requestFocus();
    }


    /**
     * Handles the downward movement of a player's active brick by one step.
     * <p>
     * If the brick cannot move further, merges it into the board, clears any
     * completed rows, awards score, and generates a new brick.
     * If a new brick cannot be created, the player loses (game over).
     * Updates the next block preview and player view accordingly.
     * </p>
     *
     * @param player Player number (1 or 2)
     */
    private void handlePlayerDown(int player) {
        Board board = (player == 1) ? p1Board : p2Board;
        boolean canMove = board.moveBrickDown();
        boolean delayedRefresh = false;

        if (!canMove) {
            board.mergeBrickToBackground();
            int[][] boardBeforeClear = MatrixOperations.copy(board.getBoardMatrix());
            ClearRow clearRow = board.clearRows();
            if (clearRow.getLinesRemoved() > 0) {
                board.getScore().add(clearRow.getScoreBonus());
                delayedRefresh = showLineClearEffect(player, boardBeforeClear);
            }
            if (board.createNewBrick()) {
                handleGameOver(player);
            }
            updateNextBlock(player, board.getNextShape());
        }

        if (!delayedRefresh) {
            updatePlayerView(player);
        }
    }

    /**
     * Performs a hard drop for a player's active brick.
     * <p>
     * Moves the brick down until it cannot move further, then merges it into the board
     * and handles scoring and next brick creation.
     * </p>
     *
     * @param player Player number (1 or 2)
     */
    private void handlePlayerHardDrop(int player) {
        Board board = (player == 1) ? p1Board : p2Board;
        while (board.moveBrickDown()) {
        }
        handlePlayerDown(player);
    }


    /**
     * Handles holding a player's active brick.
     * <p>
     * Swaps the current brick with the held brick (if any), and updates the hold block preview.
     * Also updates the next block if a new brick is created as a result.
     * </p>
     *
     * @param player Player number (1 or 2)
     */
    private void handlePlayerHold(int player) {
        Board board = (player == 1) ? p1Board : p2Board;
        HoldShapeInfo holdInfo = board.holdBrick();
        if (holdInfo != null) {
            updateHoldBlock(player, holdInfo);
            updateNextBlock(player, board.getNextShape());
        }
    }

    /**
     * Updates the ghost piece for a player.
     * <p>
     * Calculates where the current brick would land if dropped immediately
     * and renders a translucent ghost piece at that location.
     * </p>
     *
     * @param player Player number (1 or 2)
     */
    private void updateGhostPiece(int player) {
        Board board = (player == 1) ? p1Board : p2Board;
        GridPane gamePanel = (player == 1) ? p1GamePanel : p2GamePanel;
        
        int[][] ghostData = getGhostPiecePosition(player);
        if (ghostData != null) {
            drawGhostPiece(player, ghostData, gamePanel);
        }
    }

    /**
     * Calculates the position of a player's ghost piece.
     * <p>
     * Returns a 2D array representing the brick shape and its x/y position
     * at the lowest valid location on the board.
     * </p>
     *
     * @param player Player number (1 or 2)
     * @return 2D array representing ghost brick shape and coordinates
     */
    private int[][] getGhostPiecePosition(int player) {
        Board board = (player == 1) ? p1Board : p2Board;
        ViewData viewData = board.getViewData();
        if (viewData == null) return null;
        
        int[][] currentShape = viewData.getBrickData();
        int currentX = viewData.getxPosition();
        int currentY = viewData.getyPosition();
        
        int ghostY = currentY;

        while (!checkCollision(player, currentShape, currentX, ghostY + 1)) {
            ghostY++;
        }

        int rows = Math.min(4, currentShape.length);
        int cols = Math.min(4, currentShape[0].length);
        int[][] result = new int[5][4];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = currentShape[i][j];
            }
        }
        result[4][0] = currentX;
        result[4][1] = ghostY;
        
        return result;
    }

    /**
     * Checks if a given brick shape collides with the board at the specified position.
     *
     * @param player Player number (1 or 2)
     * @param shape  2D array representing the brick shape
     * @param x      X-coordinate on the board
     * @param y      Y-coordinate on the board
     * @return true if collision occurs, false otherwise
     */
    private boolean checkCollision(int player, int[][] shape, int x, int y) {
        Board board = (player == 1) ? p1Board : p2Board;
        int[][] boardMatrix = board.getBoardMatrix();
        
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int boardX = x + j;
                    int boardY = y + i;

                    if (boardX < 0 || boardX >= boardMatrix[0].length ||
                            boardY >= boardMatrix.length ||
                            (boardY >= 0 && boardMatrix[boardY][boardX] != 0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Clears the current ghost piece from the player's board.
     *
     * @param player Player number (1 or 2)
     */
    private void clearGhostPiece(int player) {
        Rectangle[][] ghostPieceRectangles = (player == 1) ? p1GhostPieceRectangles : p2GhostPieceRectangles;
        GridPane gamePanel = (player == 1) ? p1GamePanel : p2GamePanel;
        
        if (ghostPieceRectangles != null) {
            for (int i = 0; i < ghostPieceRectangles.length; i++) {
                for (int j = 0; j < ghostPieceRectangles[i].length; j++) {
                    if (ghostPieceRectangles[i][j] != null) {
                        gamePanel.getChildren().remove(ghostPieceRectangles[i][j]);
                        ghostPieceRectangles[i][j] = null;
                    }
                }
            }
        }
    }

    /**
     * Draws the ghost piece at its calculated position.
     * <p>
     * Renders translucent rectangles representing the ghost brick at the given
     * coordinates on the player's grid pane.
     * </p>
     *
     * @param player     Player number (1 or 2)
     * @param ghostData  2D array containing ghost brick shape and coordinates
     * @param gamePanel  The GridPane to render the ghost piece on
     */
    private void drawGhostPiece(int player, int[][] ghostData, GridPane gamePanel) {
        clearGhostPiece(player);

        int ghostX = ghostData[4][0];
        int ghostY = ghostData[4][1];
        
        Rectangle[][] ghostPieceRectangles = new Rectangle[4][4];
        
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (i < ghostData.length - 1 && j < ghostData[i].length && ghostData[i][j] != 0) {
                    Rectangle ghostRect = new Rectangle(BRICK_SIZE - 1, BRICK_SIZE - 1);
                    
                    Color pieceColor = (Color) getFillColor(ghostData[i][j]);
                    Color ghostColor = new Color(pieceColor.getRed(), pieceColor.getGreen(), pieceColor.getBlue(), 0.3);
                    
                    ghostRect.setFill(ghostColor);
                    ghostRect.setStroke(Color.WHITE);
                    ghostRect.setStrokeWidth(1);
                    ghostRect.setArcHeight(9);
                    ghostRect.setArcWidth(9);
                    
                    gamePanel.add(ghostRect, ghostX + j, ghostY + i);
                    
                    ghostPieceRectangles[i][j] = ghostRect;
                }
            }
        }
        
        if (player == 1) {
            p1GhostPieceRectangles = ghostPieceRectangles;
        } else {
            p2GhostPieceRectangles = ghostPieceRectangles;
        }
    }

    /**
     * Handles the game over state for a player.
     * <p>
     * Stops the player's timeline, displays the game over panel, and sets the game over flag.
     * </p>
     *
     * @param player Player number (1 or 2)
     */
    private void handleGameOver(int player) {
        if (player == 1) {
            p1GameOver = true;
            p1GameOverPanel.setVisible(true);
            if (p1Timeline != null) p1Timeline.stop();
        } else {
            p2GameOver = true;
            p2GameOverPanel.setVisible(true);
            if (p2Timeline != null) p2Timeline.stop();
        }
    }

    /**
     * Starts the automatic game loops (falling bricks) for both players.
     * <p>
     * Creates and plays timelines that move the bricks down at fixed intervals.
     * Stops automatically when the game is over for a player.
     * </p>
     */
    private void startGameLoops() {
        p1Timeline = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            if (!p1GameOver) {
                handlePlayerDown(1);
            }
        }));
        p1Timeline.setCycleCount(Timeline.INDEFINITE);
        p1Timeline.play();
        
        p2Timeline = new Timeline(new KeyFrame(Duration.millis(400), e -> {
            if (!p2GameOver) {
                handlePlayerDown(2);
            }
        }));
        p2Timeline.setCycleCount(Timeline.INDEFINITE);
        p2Timeline.play();
    }

    /**
     * Loads digit images (0-9) from resources for score and line display.
     */
    private void loadDigitImages() {
        for (int i = 0; i <= 9; i++) {
            digits[i] = new Image(getClass().getResourceAsStream("/digits/" + i + ".png"));
        }
    }

    /**
     * Updates the score images for a player.
     *
     * @param player Player number (1 or 2)
     * @param score  Current score value
     */
    private void updateScoreImages(int player, int score) {
        ImageView[] imageViews = (player == 1) ?
                new ImageView[]{p1ScoreThousands, p1ScoreHundreds, p1ScoreTens, p1ScoreOnes} :
                new ImageView[]{p2ScoreThousands, p2ScoreHundreds, p2ScoreTens, p2ScoreOnes};

        int thousands = (score / 1000) % 10;
        int hundreds = (score / 100) % 10;
        int tens = (score / 10) % 10;
        int ones = score % 10;
        
        imageViews[0].setImage(digits[thousands]);
        imageViews[1].setImage(digits[hundreds]);
        imageViews[2].setImage(digits[tens]);
        imageViews[3].setImage(digits[ones]);
    }

    /**
     * Updates the lines cleared images for a player.
     *
     * @param player Player number (1 or 2)
     * @param lines  Current number of lines cleared
     */
    private void updateLineImages(int player, int lines) {
        ImageView[] imageViews = (player == 1) ?
                new ImageView[]{p1LineHundreds, p1LineTens, p1LineOnes} :
                new ImageView[]{p2LineHundreds, p2LineTens, p2LineOnes};

        int hundreds = (lines / 100) % 10;
        int tens = (lines / 10) % 10;
        int ones = lines % 10;
        
        imageViews[0].setImage(digits[hundreds]);
        imageViews[1].setImage(digits[tens]);
        imageViews[2].setImage(digits[ones]);
    }

    /**
     * Updates the next block preview for a player.
     *
     * @param player Player number (1 or 2)
     * @param next   NextShapeInfo object representing the upcoming brick
     */
    private void updateNextBlock(int player, NextShapeInfo next) {
        Pane pane = (player == 1) ? p1NextBlockPane : p2NextBlockPane;
        if (next == null || pane == null) return;
        
        pane.getChildren().clear();
        int[][] shape = next.getShape();
        renderBlockPreview(pane, shape);
    }

    /**
     * Updates the hold block preview for a player.
     *
     * @param player Player number (1 or 2)
     * @param hold   HoldShapeInfo object representing the held brick
     */
    private void updateHoldBlock(int player, HoldShapeInfo hold) {
        Pane pane = (player == 1) ? p1HoldBlockPane : p2HoldBlockPane;
        if (hold == null || pane == null) return;
        
        pane.getChildren().clear();
        int[][] shape = hold.getShape();
        renderBlockPreview(pane, shape);
    }


    /**
     * Renders a brick shape preview in a given pane.
     *
     * @param pane  Pane to render the shape
     * @param shape 2D array representing the brick shape
     */
    private void renderBlockPreview(Pane pane, int[][] shape) {
        if (shape == null || pane == null) return;
        
        double blockSize = 12;
        double offsetX = (pane.getPrefWidth() - shape[0].length * blockSize) / 2;
        double offsetY = (pane.getPrefHeight() - shape.length * blockSize) / 2;
        
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    Rectangle rect = new Rectangle(blockSize, blockSize);
                    rect.setFill(getFillColor(shape[i][j]));
                    rect.setArcWidth(blockSize / 4);
                    rect.setArcHeight(blockSize / 4);
                    rect.setLayoutX(j * blockSize + offsetX);
                    rect.setLayoutY(i * blockSize + offsetY);
                    pane.getChildren().add(rect);
                }
            }
        }
    }

    /**
     * Returns the fill color for a brick based on its value.
     *
     * @param i Brick type index
     * @return Paint color to use for the brick
     */
    private Paint getFillColor(int i) {
        switch (i) {
            case 0: return Color.TRANSPARENT;
            case 1: return Color.AQUA;
            case 2: return Color.BLUEVIOLET;
            case 3: return Color.DARKGREEN;
            case 4: return Color.YELLOW;
            case 5: return Color.RED;
            case 6: return Color.BEIGE;
            case 7: return Color.BURLYWOOD;
            default: return Color.WHITE;
        }
    }

    /**
     * Pauses the game when the Pause button is clicked.
     * <p>
     * Stops both players' timelines and shows the pause menu overlay.
     * </p>
     *
     * @param actionEvent The ActionEvent generated by clicking the button.
     */
    @FXML
    public void pauseGame(ActionEvent actionEvent) {
        if (isPaused) {
            resumeGame(actionEvent);
        } else {
            isPaused = true;
            pauseMenu.setVisible(true);
            if (p1Timeline != null) p1Timeline.pause();
            if (p2Timeline != null) p2Timeline.pause();
            rootPane.requestFocus();
        }
        updateMusicButtonState();
    }

    /**
     * Resumes the game from a paused state.
     * <p>
     * Hides the pause menu and resumes both players' timelines if the game is not over.
     * </p>
     *
     * @param actionEvent The ActionEvent generated by clicking the Resume button.
     */
    @FXML
    public void resumeGame(ActionEvent actionEvent) {
        pauseMenu.setVisible(false);
        isPaused = false;
        if (p1Timeline != null && !p1GameOver) p1Timeline.play();
        if (p2Timeline != null && !p2GameOver) p2Timeline.play();
        rootPane.requestFocus();
        updateMusicButtonState();
    }

    /**
     * Restarts the versus game.
     * <p>
     * Resets both boards, clears UI elements, resets scores and lines, and starts new game loops.
     * </p>
     */
    @FXML
    public void restartGame() {
        // Stop timelines
        if (p1Timeline != null) p1Timeline.stop();
        if (p2Timeline != null) p2Timeline.stop();
        
        // Reset game state
        p1GameOver = false;
        p2GameOver = false;
        isPaused = false;
        pauseMenu.setVisible(false);
        p1GameOverPanel.setVisible(false);
        p2GameOverPanel.setVisible(false);
        
        // Clear old UI elements
        clearGhostPiece(1);
        clearGhostPiece(2);
        p1GamePanel.getChildren().clear();
        p1BrickOverlay.getChildren().clear();
        p1GroupNotification.getChildren().clear();
        p2GamePanel.getChildren().clear();
        p2BrickOverlay.getChildren().clear();
        p2GroupNotification.getChildren().clear();
        
        // Reset display matrices to null so they get recreated
        p1DisplayMatrix = null;
        p2DisplayMatrix = null;
        p1ActiveBrick = null;
        p2ActiveBrick = null;
        p1GhostBrick = null;
        p2GhostBrick = null;
        p1GhostPieceRectangles = null;
        p2GhostPieceRectangles = null;
        
        // Reset boards
        p1Board.newGame();
        p2Board.newGame();
        p1Board.createNewBrick();
        p2Board.createNewBrick();
        
        // Reset views
        initPlayerView(1);
        initPlayerView(2);
        updatePlayerView(1);
        updatePlayerView(2);
        
        // Update UI
        updateScoreImages(1, p1Board.getScore().scoreProperty().get());
        updateScoreImages(2, p2Board.getScore().scoreProperty().get());
        updateLineImages(1, p1Board.getScore().lineProperty().get());
        updateLineImages(2, p2Board.getScore().lineProperty().get());
        updateNextBlock(1, p1Board.getNextShape());
        updateNextBlock(2, p2Board.getNextShape());
        
        HoldShapeInfo p1Hold = p1Board.getHeldShape();
        if (p1Hold != null) {
            updateHoldBlock(1, p1Hold);
        } else {
            p1HoldBlockPane.getChildren().clear();
        }
        
        HoldShapeInfo p2Hold = p2Board.getHeldShape();
        if (p2Hold != null) {
            updateHoldBlock(2, p2Hold);
        } else {
            p2HoldBlockPane.getChildren().clear();
        }

        startGameLoops();
        rootPane.requestFocus();
        updateMusicButtonState();
    }

    /**
     * Exits the game application completely.
     * <p>
     * Stops all timelines and closes the stage.
     * </p>
     *
     * @param actionEvent The ActionEvent generated by clicking the Exit button.
     */
    @FXML
    public void exitGame(ActionEvent actionEvent) {
        if (p1Timeline != null) p1Timeline.stop();
        if (p2Timeline != null) p2Timeline.stop();
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    /**
     * Exits to the main menu from the versus game screen.
     * <p>
     * Stops timelines, loads the main menu FXML, and switches the scene to the main menu.
     * </p>
     *
     * @param event The ActionEvent generated by clicking the Main Menu button.
     */
    @FXML
    private void exitToMainMenu(ActionEvent event) {
        if (p1Timeline != null) p1Timeline.stop();
        if (p2Timeline != null) p2Timeline.stop();

        try {
            Stage stage = (Stage) p1GamePanel.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getClassLoader().getResource("mainMenu.fxml"));
            Parent root = fxmlLoader.load();
            MainMenuController menuController = fxmlLoader.getController();
            menuController.setPrimaryStage(stage);
            
            double width = stage.getWidth();
            double height = stage.getHeight();
            if (width <= 0 || height <= 0) {
                Screen screen = Screen.getPrimary();
                Rectangle2D bounds = screen.getVisualBounds();
                width = bounds.getWidth();
                height = bounds.getHeight();
            }
            
            Scene menuScene = new Scene(root, width, height);
            stage.setScene(menuScene);
            stage.setMaximized(true);
            stage.setTitle("TetrisJFX - Main Menu");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleMusic(ActionEvent event) {
        MusicPlayerWav player = Main.getMusicPlayer();
        if (player == null) return;
        if (player.isPlaying()) {
            player.stopMusic();
        } else {
            player.playMusic(Main.getDefaultMusicTrack());
        }
        updateMusicButtonState();
    }

    private void updateMusicButtonState() {
        if (musicToggleButton == null) return;
        MusicPlayerWav player = Main.getMusicPlayer();
        boolean playing = player != null && player.isPlaying();
        musicToggleButton.setOpacity(playing ? 1.0 : 0.35);
        musicToggleButton.setTooltip(new Tooltip(playing ? "Mute Music" : "Play Music"));
    }
}
