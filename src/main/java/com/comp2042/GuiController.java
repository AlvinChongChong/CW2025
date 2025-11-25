package com.comp2042;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.effect.Reflection;
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
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * GuiController manages the visual representation of the Tetris game.
 * It handles the game board, bricks (active and ghost pieces), timer, score display,
 * next block preview, pause functionality, and game over screen.
 *
 * <p>This controller communicates with an InputEventListener to handle game logic
 * while maintaining the user interface in sync with the underlying board state.</p>
 */
public class GuiController implements Initializable {

    /** The size of each brick in pixels. */
    private static final int BRICK_SIZE = 20;

    /** The Button for pausing the game. */
    public Button pauseButton;
    /** StackPane containing pause menu. */
    public StackPane pauseMenu;
    @FXML
    private Button musicToggleButton;

    @FXML
    private ImageView minTens, minOnes, secTens, secOnes;  //image for timer
    @FXML
    private ImageView lineHundreds, lineTens, lineOnes;

    @FXML
    private GridPane gamePanel;

    @FXML
    private Pane brickOverlay;

    @FXML
    private Pane groupNotification;

    @FXML
    private GridPane brickPanel;

    @FXML
    private GameOverPanel gameOverPanel;

    private Rectangle[][] displayMatrix;
    private Rectangle[][] activeBrick;
    private Rectangle[][] ghostBrick;

    private Timeline timeLine;
    private Timeline timerTimeline;

    private InputEventListener eventListener;

    private final BooleanProperty isPause = new SimpleBooleanProperty();
    private final BooleanProperty isGameOver = new SimpleBooleanProperty();

    @FXML
    private ImageView scoreThousands;
    @FXML
    private ImageView scoreHundreds;
    @FXML
    private ImageView scoreTens;
    @FXML
    private ImageView scoreOnes;

    @FXML
    private Pane nextBlockPane;

    @FXML
    private Pane holdBlockPane;

    @FXML
    private ImageView levelHundreds;

    @FXML
    private ImageView levelTens;
    @FXML
    private ImageView levelOnes;

    @FXML
    private Label highScoreLabel;


    private int timeRemaining = 180;
    private Image[] digits = new Image[10];
    private Rectangle[][] ghostPieceRectangles;
    private int totalLinesCleared = 0;
    private int highScore;

    private static final String HIGH_SCORE_DIR = ".tetris";
    private static final String HIGH_SCORE_FILE = "highscore.dat";
    private java.nio.file.Path highScoreFilePath;
    @FXML
    private ImageView musicIcon;



    /**
     * Initializes the GUI components, binds key events, loads digit images,
     * and prepares the game view.
     *
     * @param location  not used
     * @param resources not used
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gamePanel.setFocusTraversable(true);
        gamePanel.requestFocus();
        gamePanel.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (!isPause.get() && !isGameOver.get()) {
                    if (keyEvent.getCode() == KeyCode.LEFT || keyEvent.getCode() == KeyCode.A) {
                        refreshBrick(eventListener.onLeftEvent(new MoveEvent(EventType.LEFT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.RIGHT || keyEvent.getCode() == KeyCode.D) {
                        refreshBrick(eventListener.onRightEvent(new MoveEvent(EventType.RIGHT, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.W) {
                        refreshBrick(eventListener.onRotateEvent(new MoveEvent(EventType.ROTATE, EventSource.USER)));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.DOWN || keyEvent.getCode() == KeyCode.S) {
                        moveDown(new MoveEvent(EventType.DOWN, EventSource.USER));
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.SPACE) {
                        hardDrop();
                        keyEvent.consume();
                    }
                    if (keyEvent.getCode() == KeyCode.C) {
                        hold();
                        keyEvent.consume();
                    }
                }
                if (keyEvent.getCode() == KeyCode.N) {
                    newGame(null);
                }
            }
        });
        gameOverPanel.setVisible(false);

        final Reflection reflection = new Reflection();
        reflection.setFraction(0.8);
        reflection.setTopOpacity(0.9);
        reflection.setTopOffset(-12);

        loadDigitImages();
        updateScoreImages(0);
        updateLineImages(0);
        updateLevelImages(0);
        initializeHighScore();
        highScore = loadHighScore();
        updateHighScoreLabel();
        updateMusicButtonState();

    }

    /**
     * Initializes the game board view, creating rectangles for the grid,
     * active brick, and ghost brick.
     *
     * @param boardMatrix 2D array representing the board
     * @param brick       initial active brick data
     */
    public void initGameView(int[][] boardMatrix, ViewData brick) {
        displayMatrix = new Rectangle[boardMatrix.length][boardMatrix[0].length];
        for (int i = 0; i < boardMatrix.length; i++) {
            for (int j = 0; j < boardMatrix[i].length; j++) {
                Rectangle rectangle = new Rectangle(BRICK_SIZE, BRICK_SIZE);
                rectangle.setFill(Color.TRANSPARENT);
                displayMatrix[i][j] = rectangle;
                gamePanel.add(rectangle, j, i);
            }
        }

        activeBrick = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];
        ghostBrick = new Rectangle[brick.getBrickData().length][brick.getBrickData()[0].length];

        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                // ghost brick
                Rectangle ghost = new Rectangle(BRICK_SIZE - 1, BRICK_SIZE - 1);
                ghost.setFill(Color.LIGHTGRAY);
                ghost.setOpacity(0.3);
                ghost.setArcWidth(9);
                ghost.setArcHeight(9);
                ghost.setVisible(false); // Initially hidden
                ghostBrick[i][j] = ghost;
                brickOverlay.getChildren().add(ghost);

                // normal brick
                Rectangle active = new Rectangle(BRICK_SIZE - 1, BRICK_SIZE - 1);
                active.setFill(getFillColor(brick.getBrickData()[i][j]));
                active.setStrokeWidth(1.2);
                active.setStrokeType(StrokeType.CENTERED);
                active.setArcWidth(9);
                active.setArcHeight(9);
                active.setVisible(false); // Initially hidden until positioned
                activeBrick[i][j] = active;
                brickOverlay.getChildren().add(active);
            }
        }
        updateBrickPosition(brick);

        timeLine = new Timeline(new KeyFrame(
                Duration.millis(400),
                ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
        ));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
        startTimer();
    }
    /**
     * Returns the color associated with a given brick type ID.
     *
     * @param i the ID representing a brick type
     * @return the corresponding Paint color for the brick
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
     * Updates the positions and colors of the active brick's rectangles on the UI
     * based on the current brick view data.
     *
     * @param brick the ViewData object representing the brick's current shape and position
     */
    private void updateBrickPosition(ViewData brick) {
        // Normal brick
        final int VGAP = 1;
        for (int i = 0; i < brick.getBrickData().length; i++) {
            for (int j = 0; j < brick.getBrickData()[i].length; j++) {
                Rectangle r = activeBrick[i][j];
                int value = brick.getBrickData()[i][j];
                r.setVisible(value != 0);
                r.setFill(getFillColor(value));
                r.setX((brick.getxPosition() + j) * BRICK_SIZE);
                r.setY((brick.getyPosition() + i) * (BRICK_SIZE + VGAP) - 2);
            }
        }

        }
    /**
     * Refreshes the active brick on screen unless the game is paused.
     *
     * @param brick the updated brick view data
     */

    private void refreshBrick(ViewData brick) {
        if (!isPause.get()) {
            updateBrickPosition(brick);
        }
    }

    /**
     * Draws the background board matrix (landed tiles).
     *
     * @param board 2D matrix of board colors
     */

    public void refreshGameBackground(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                setRectangleData(board[i][j], displayMatrix[i][j]);
            }
        }
    }

    /**
     * Sets the visual appearance of a rectangle based on the brick color.
     * Applies fill color and rounded corners.
     *
     * @param color the integer ID representing the brick color
     * @param rectangle the Rectangle object to be updated
     */
    private void setRectangleData(int color, Rectangle rectangle) {
        rectangle.setFill(getFillColor(color));
        rectangle.setArcHeight(9);
        rectangle.setArcWidth(9);
    }

    /**
     * Handles both automatic and user-triggered downward movement of the brick.
     * Checks for row clears and updates score notifications.
     *
     * @param event type of downward movement (thread/user)
     */

    private void moveDown(MoveEvent event) {
        if (!isPause.get()) {
            DownData downData = eventListener.onDownEvent(event);
            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
            refreshBrick(downData.getViewData());
        }
        gamePanel.requestFocus();
    }
    
    /**
     * Shows a visual effect when lines are cleared.
     * Flashes the cleared rows white before they disappear.
     * Uses the board matrix before clearing to identify which rows to flash.
     *
     * @param boardBeforeClear the board matrix before rows were cleared
     * @param linesCleared number of lines that were cleared
     * @param newBoardMatrix the board matrix after rows were cleared (to refresh after effect)
     */
    public void showLineClearEffectWithBoard(int[][] boardBeforeClear, int linesCleared, int[][] newBoardMatrix) {
        if (displayMatrix == null || boardBeforeClear == null) return;

        if (eventListener != null) {
        }
        
        // Find which rows are full
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
        
        if (rowsToFlash.isEmpty()) return;

        Map<Integer, Paint[]> originalColors = new HashMap<>();
        for (Integer row : rowsToFlash) {
            Paint[] colors = new Paint[displayMatrix[row].length];
            for (int j = 0; j < displayMatrix[row].length; j++) {
                colors[j] = displayMatrix[row][j].getFill();
            }
            originalColors.put(row, colors);
        }

        Timeline flashTimeline = new Timeline();
        
        // Flash to white (immediate)
        KeyFrame flashOn1 = new KeyFrame(Duration.millis(0), e -> {
            for (Integer row : rowsToFlash) {
                for (int j = 0; j < displayMatrix[row].length; j++) {
                    displayMatrix[row][j].setFill(Color.WHITE);
                }
            }
        });
        
        // Flash back to original
        KeyFrame flashOff1 = new KeyFrame(Duration.millis(80), e -> {
            for (Integer row : rowsToFlash) {
                Paint[] colors = originalColors.get(row);
                for (int j = 0; j < displayMatrix[row].length; j++) {
                    displayMatrix[row][j].setFill(colors[j]);
                }
            }
        });
        
        // Flash to white again
        KeyFrame flashOn2 = new KeyFrame(Duration.millis(160), e -> {
            for (Integer row : rowsToFlash) {
                for (int j = 0; j < displayMatrix[row].length; j++) {
                    displayMatrix[row][j].setFill(Color.WHITE);
                }
            }
        });
        
        // Flash back to original again
        KeyFrame flashOff2 = new KeyFrame(Duration.millis(240), e -> {
            for (Integer row : rowsToFlash) {
                Paint[] colors = originalColors.get(row);
                for (int j = 0; j < displayMatrix[row].length; j++) {
                    displayMatrix[row][j].setFill(colors[j]);
                }
            }
        });
        
        // Flash to white one more time, then refresh board
        KeyFrame flashOn3 = new KeyFrame(Duration.millis(320), e -> {
            for (Integer row : rowsToFlash) {
                for (int j = 0; j < displayMatrix[row].length; j++) {
                    displayMatrix[row][j].setFill(Color.WHITE);
                }
            }
        });
        
        // Refresh board after effect completes
        KeyFrame refreshBoard = new KeyFrame(Duration.millis(400), e -> {
            if (newBoardMatrix != null) {
                refreshGameBackground(newBoardMatrix);
            }
        });
        
        flashTimeline.getKeyFrames().addAll(flashOn1, flashOff1, flashOn2, flashOff2, flashOn3, refreshBoard);
        flashTimeline.setCycleCount(1);
        flashTimeline.play();
    }

    /**
     * Sets the listener for input events, allowing the GUI to
     * communicate user actions (like key presses) to the game logic.
     *
     * @param eventListener the InputEventListener to handle user and thread events
     */
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Binds the score property to on-screen digit images.
     *
     * @param scoreProperty observable score value
     */

    public void bindScore(IntegerProperty scoreProperty) {
        scoreProperty.addListener((obs, oldVal, newVal) -> {
            int score = newVal.intValue();
            updateScoreImages(score);
            checkForHighScore(score);
        });
        checkForHighScore(scoreProperty.get());
    }

    /**
     * Binds the line counter property to digit images.
     *
     * @param lineProperty observable line count
     */

    public void lineScore(IntegerProperty lineProperty) {
        lineProperty.addListener((obs, oldVal, newVal) -> {
            int linesCleared = newVal.intValue();
            updateLineImages(linesCleared);
            updateLevelImages(linesCleared);

        });
    }

    /**
     * Stops all timelines, shows the game over screen,
     * and marks the game as finished.
     */

    public void gameOver() {
        timeLine.stop();
        gameOverPanel.setVisible(true);
        isGameOver.setValue(true);
        if (timerTimeline != null) timerTimeline.stop();
    }

    /**
     * Resets the entire game state and starts a new session.
     *
     * @param actionEvent button event (may be null)
     */

    public void newGame(ActionEvent actionEvent) {
        timeLine.stop();
        gameOverPanel.setVisible(false);
        eventListener.createNewGame();
        gamePanel.requestFocus();
        timeLine.play();
        isPause.set(false);
        isGameOver.set(false);
        timeRemaining = 180;
        startTimer();
        totalLinesCleared = 0;
        updateLineImages(0);
        updateLevelImages(0);
    }

    /**
     * Resumes the game after being paused.
     *
     * @param actionEvent button event
     */

    public void resumeGame(ActionEvent actionEvent) {
        pauseMenu.setVisible(false);
        isPause.set(false);
        timeLine.play();
        if (timerTimeline != null) timerTimeline.play();
        gamePanel.requestFocus();
        updateMusicButtonState();
    }

    /**
     * Toggles the pause state. Pauses/plays animations and timer.
     *
     * @param actionEvent button event
     */

    public void pauseGame(ActionEvent actionEvent) {
        if (isPause.get()) {
            timeLine.play();
            if (timerTimeline != null) timerTimeline.play();
            isPause.set(false);
            pauseMenu.setVisible(false);
        } else {
            if (timerTimeline != null) timerTimeline.pause();
            isPause.set(true);
            pauseMenu.setVisible(true);
        }
        gamePanel.requestFocus();
    }

    /**
     * Exits to the main menu, stopping the current game.
     *
     * @param actionEvent button event
     */
    public void exitToMainMenu(ActionEvent actionEvent) {
        // Stop all timelines
        if (timeLine != null) {
            timeLine.stop();
        }
        if (timerTimeline != null) {
            timerTimeline.stop();
        }

        try {
            // Get the stage from the current scene
            Stage stage = (Stage) gamePanel.getScene().getWindow();

            // Load main menu
            FXMLLoader fxmlLoader = new FXMLLoader(
                    getClass().getClassLoader().getResource("mainMenu.fxml"));
            Parent root = fxmlLoader.load();
            MainMenuController menuController = fxmlLoader.getController();
            menuController.setPrimaryStage(stage);

            // Get current stage dimensions to maintain full screen
            double width = stage.getWidth();
            double height = stage.getHeight();

            // If stage dimensions are invalid, use screen dimensions
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

    /**
     * Exits the application completely.
     *
     * @param actionEvent button event
     */

    public void exitGame(ActionEvent actionEvent) {
        System.exit(0);
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
        MusicPlayerWav player = Main.getMusicPlayer();
        boolean playing = player != null && player.isPlaying();

        musicIcon.setOpacity(playing ? 1.0 : 0.35);

        musicToggleButton.setTooltip(new Tooltip(
                playing ? "Mute Music" : "Play Music"
        ));

    }


    /**
     * Loads the digit images (0-9) from resources into the `digits` array.
     * These images are later used for displaying scores, lines, and timer digits.
     */
    private void loadDigitImages() {
        for (int i = 0; i <= 9; i++) {
            digits[i] = new Image(getClass().getResourceAsStream("/digits/" + i + ".png"));
        }
    }

    /**
     * Updates the timer digit images on the UI based on the remaining time in seconds.
     *
     * @param secondsRemaining the number of seconds left in the countdown timer
     */
    private void updateTimerImages(int secondsRemaining) {
        int minutes = secondsRemaining / 60;
        int seconds = secondsRemaining % 60;

        minTens.setImage(digits[minutes / 10]);
        minOnes.setImage(digits[minutes % 10]);
        secTens.setImage(digits[seconds / 10]);
        secOnes.setImage(digits[seconds % 10]);
    }

    /**
     * Initializes and starts the countdown timer.
     * When timer reaches zero, triggers game over.
     */

    private void startTimer() {
        loadDigitImages();
        updateTimerImages(timeRemaining);

        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            updateTimerImages(timeRemaining);

            if (timeRemaining <= 0) {
                timerTimeline.stop();
                gameOver();
            }
        }));
        timerTimeline.setCycleCount(Timeline.INDEFINITE);
        timerTimeline.play();
    }
    /**
     * Updates the score digit images on the UI based on the current score value.
     *
     * @param score the current score to display
     */
    private void updateScoreImages(int score) {
        int thousands = (score / 1000) % 10;
        int hundreds = (score / 100) % 10;
        int tens = (score / 10) % 10;
        int ones = score % 10;

        scoreThousands.setImage(digits[thousands]);
        scoreHundreds.setImage(digits[hundreds]);
        scoreTens.setImage(digits[tens]);
        scoreOnes.setImage(digits[ones]);
    }

    /**
     * Initializes the high score file path.
     */
    private void initializeHighScore() {
        String userHome = System.getProperty("user.home");
        java.nio.file.Path directory = java.nio.file.Paths.get(userHome, HIGH_SCORE_DIR);
        try {
            java.nio.file.Files.createDirectories(directory);
        } catch (java.io.IOException e) {
            directory = java.nio.file.Paths.get("").toAbsolutePath();
        }
        highScoreFilePath = directory.resolve(HIGH_SCORE_FILE);
    }

    /**
     * Loads the saved high score if available.
     *
     * @return the stored high score, or 0 if none exists or parsing fails
     */
    private int loadHighScore() {
        if (highScoreFilePath == null || !java.nio.file.Files.exists(highScoreFilePath)) {
            return 0;
        }
        try {
            String content = java.nio.file.Files.readString(highScoreFilePath, java.nio.charset.StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(content);
        } catch (java.io.IOException | NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Persists the provided high score value.
     *
     * @param newHighScore score to store
     */
    private void saveHighScore(int newHighScore) {
        if (highScoreFilePath == null) return;
        try {
            java.nio.file.Files.writeString(
                    highScoreFilePath,
                    String.valueOf(newHighScore),
                    java.nio.charset.StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (java.io.IOException e) {
            // Swallow the exception; inability to save shouldn't crash the game.
        }
    }

    /**
     * Updates the high score label text on screen.
     */
    private void updateHighScoreLabel() {
        if (highScoreLabel != null) {
            highScoreLabel.setText(String.format("%05d", Math.max(0, highScore)));
        }
    }

    /**
     * Checks whether the current score surpasses the stored high score.
     * If so, saves and updates the display.
     *
     * @param newScore latest score value
     */
    private void checkForHighScore(int newScore) {
        if (newScore > highScore) {
            highScore = newScore;
            saveHighScore(highScore);
            updateHighScoreLabel();
        } else {
            // ensure label is up to date when binding is first established
            updateHighScoreLabel();
        }
    }

    /**
     * Updates the line counter digit images on the UI based on the number of cleared lines.
     *
     * @param lines the number of lines cleared to display
     */
    private void updateLineImages(int lines) {
        int hundreds = (lines / 100) % 10;
        int tens = (lines / 10) % 10;
        int ones = lines % 10;

        lineHundreds.setImage(digits[hundreds]);
        lineTens.setImage(digits[tens]);
        lineOnes.setImage(digits[ones]);
    }

    //show next block
    private static final int BLOCK_SIZE = 20;

    /**
     * Renders a preview of the next block, scaling and centering it inside
     * the preview panel.
     *
     * @param next shape and color data of the next piece
     */

    public void showNextBlock(NextShapeInfo next) {
        nextBlockPane.getChildren().clear(); // clear previous preview

        int[][] shape = next.getShape();
        int shapeRows = shape.length;
        int shapeCols = shape[0].length;

        double paneWidth = nextBlockPane.getPrefWidth();
        double paneHeight = nextBlockPane.getPrefHeight();

        // Find topmost, bottommost, leftmost, rightmost filled blocks
        int top = shapeRows, bottom = -1, left = shapeCols, right = -1;
        for (int i = 0; i < shapeRows; i++) {
            for (int j = 0; j < shapeCols; j++) {
                if (shape[i][j] != 0) {
                    if (i < top) top = i;
                    if (i > bottom) bottom = i;
                    if (j < left) left = j;
                    if (j > right) right = j;
                }
            }
        }

        int usedRows = bottom - top + 1;
        int usedCols = right - left + 1;

        // Scale block size to fit the pane
        double padding = 10; // optional padding inside the preview box
        double blockWidth = (paneWidth - padding * 2) / usedCols;
        double blockHeight = (paneHeight - padding * 2) / usedRows;
        double blockSize = Math.min(blockWidth, blockHeight);

        // Make blocks smaller by multiplying by a factor (<1)
        blockSize *= 0.8; // 80% of available size

        // Calculate offsets to center the used blocks
        double offsetX = (paneWidth - usedCols * blockSize) / 2;
        double offsetY = (paneHeight - usedRows * blockSize) / 2;

        for (int i = 0; i < shapeRows; i++) {
            for (int j = 0; j < shapeCols; j++) {
                if (shape[i][j] != 0) {
                    Rectangle rect = new Rectangle(blockSize, blockSize);
                    rect.setFill(getFillColor(shape[i][j]));
                    rect.setArcWidth(blockSize / 4);
                    rect.setArcHeight(blockSize / 4);
                    rect.setStroke(Color.BLACK);
                    rect.setStrokeWidth(1);

                    rect.setLayoutX((j - left) * blockSize + offsetX);
                    rect.setLayoutY((i - top) * blockSize + offsetY);

                    nextBlockPane.getChildren().add(rect);
                }
            }
        }
    }

    /**
     * Fully restarts the game from the game over menu.
     * Resets timers, boards, state, and creates a new game session.
     */

//restart game
    public void restartGame() {
        if (eventListener != null) {
            if (timeLine != null) timeLine.stop();
            if (timerTimeline != null) timerTimeline.stop();

            gameOverPanel.setVisible(false);

            isPause.set(false);
            isGameOver.set(false);

            timeRemaining = 180;
            totalLinesCleared = 0;  // <-- reset here

            eventListener.createNewGame();

            gamePanel.requestFocus();

            if (timeLine != null) timeLine.play();
            startTimer();
        }
    }


    /**
     * Removes all previously drawn ghost piece rectangles from the grid.
     */

    //Remove old ghost piece
    public void clearGhostPiece() {
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
     * Draws the ghost piece using semi-transparent rectangles to show
     * where the active brick will land.
     *
     * @param ghostData 5x? array containing shape and final landing coordinates
     */

    public void drawGhostPiece(int[][] ghostData) {
        clearGhostPiece();  // remove previous ghost bricks

        // Extract ghost position from the last row of ghostData
        int ghostX = ghostData[4][0];
        int ghostY = ghostData[4][1];

        ghostPieceRectangles = new Rectangle[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (ghostData[i][j] != 0) {
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
    }

    /**
     * Instantly drops the brick to its lowest valid position.
     * Updates score notifications if lines are cleared.
     */

    private void hardDrop() {
        if (eventListener != null && !isPause.get() && !isGameOver.get()) {
            DownData downData = eventListener.onHardDropEvent(new MoveEvent(EventType.HARD_DROP, EventSource.USER));
            refreshBrick(downData.getViewData());

            if (downData.getClearRow() != null && downData.getClearRow().getLinesRemoved() > 0) {
                NotificationPanel notificationPanel = new NotificationPanel("+" + downData.getClearRow().getScoreBonus());
                groupNotification.getChildren().add(notificationPanel);
                notificationPanel.showScore(groupNotification.getChildren());
            }
        }
    }

    /**
     * Updates the level display based on total lines cleared and adjusts the drop speed.
     * Level starts at 1 and increases as 5 line cleared.
     *
     *
     */
    private void updateLevelImages(int totalLinesCleared) {

        int level = totalLinesCleared / 5 + 1;  // Level starts at 1

        int hundreds = (level / 100) % 10;
        int tens = (level / 10) % 10;
        int ones = level % 10;

        levelHundreds.setImage(digits[hundreds]);
        levelTens.setImage(digits[tens]);
        levelOnes.setImage(digits[ones]);

        if (timeLine != null) {
            timeLine.stop();
            timeLine.getKeyFrames().setAll(
                    new KeyFrame(
                            Duration.millis(getDropIntervalForLevel(level)),
                            ae -> moveDown(new MoveEvent(EventType.DOWN, EventSource.THREAD))
                    )
            );
            timeLine.play();
        }
    }



    /**
     * Returns the drop interval (ms) based on the current level.
     * Higher levels drop faster.
     *
     * @param level the current level
     * @return drop interval in milliseconds
     */
    private double getDropIntervalForLevel(int level) {
        // Base interval (level 1) is 400ms
        // Decrease by 20ms per level (example)
        double interval = 400 - (level - 1) * 40;
        return Math.max(interval, 100); // minimum 100ms
    }

    /**
     * Handles the hold brick action when the 'C' key is pressed.
     * Swaps the current brick with the held brick (if any).
     */
    private void hold() {
        if (eventListener != null && !isPause.get() && !isGameOver.get()) {
            HoldEvent holdEvent = eventListener.onHoldEvent();
            if (holdEvent != null) {
                // Update the current brick display
                refreshBrick(holdEvent.getNewCurrentPiece());
                // Update the held brick display
                showHoldBlock(holdEvent.getHoldPiece());
            }
        }
    }

    /**
     * Renders a preview of the held block, scaling and centering it inside
     * the hold preview panel.
     *
     * @param hold shape and color data of the held piece
     */
    public void showHoldBlock(HoldShapeInfo hold) {
        if (holdBlockPane == null) return;
        
        holdBlockPane.getChildren().clear(); // clear previous preview

        int[][] shape = hold.getShape();
        int shapeRows = shape.length;
        int shapeCols = shape[0].length;

        double paneWidth = holdBlockPane.getPrefWidth();
        double paneHeight = holdBlockPane.getPrefHeight();

        // Find topmost, bottommost, leftmost, rightmost filled blocks
        int top = shapeRows, bottom = -1, left = shapeCols, right = -1;
        for (int i = 0; i < shapeRows; i++) {
            for (int j = 0; j < shapeCols; j++) {
                if (shape[i][j] != 0) {
                    if (i < top) top = i;
                    if (i > bottom) bottom = i;
                    if (j < left) left = j;
                    if (j > right) right = j;
                }
            }
        }

        int usedRows = bottom - top + 1;
        int usedCols = right - left + 1;

        // Scale block size to fit the pane
        double padding = 10; // optional padding inside the preview box
        double blockWidth = (paneWidth - padding * 2) / usedCols;
        double blockHeight = (paneHeight - padding * 2) / usedRows;
        double blockSize = Math.min(blockWidth, blockHeight);

        // Make blocks smaller by multiplying by a factor (<1)
        blockSize *= 0.8; // 80% of available size

        // Calculate offsets to center the used blocks
        double offsetX = (paneWidth - usedCols * blockSize) / 2;
        double offsetY = (paneHeight - usedRows * blockSize) / 2;

        for (int i = 0; i < shapeRows; i++) {
            for (int j = 0; j < shapeCols; j++) {
                if (shape[i][j] != 0) {
                    Rectangle rect = new Rectangle(blockSize, blockSize);
                    rect.setFill(getFillColor(shape[i][j]));
                    rect.setArcWidth(blockSize / 4);
                    rect.setArcHeight(blockSize / 4);
                    rect.setStroke(Color.BLACK);
                    rect.setStrokeWidth(1);

                    rect.setLayoutX((j - left) * blockSize + offsetX);
                    rect.setLayoutY((i - top) * blockSize + offsetY);

                    holdBlockPane.getChildren().add(rect);
                }
            }
        }
    }

    /**
     * Clears the held block preview pane.
     */
    public void clearHoldBlock() {
        if (holdBlockPane != null) {
            holdBlockPane.getChildren().clear();
        }
    }


}





