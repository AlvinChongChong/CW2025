package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
/**
 * Main entry point for the Tetris JavaFX application.
 *
 * This class extends {@link javafx.application.Application} and initializes
 * the JavaFX stage, loads the FXML layout, sets up the scene, and starts
 * the game controller. It also plays background music on launch.
 */
public class Main extends Application {

    private static final String DEFAULT_MUSIC = "/chill_music.mp3";
    private static final MusicPlayerWav MUSIC_PLAYER = new MusicPlayerWav();

    public static MusicPlayerWav getMusicPlayer() {
        return MUSIC_PLAYER;
    }

    public static String getDefaultMusicTrack() {
        return DEFAULT_MUSIC;
    }

    /**
     * Starts the JavaFX application.
     * Loads the main menu first, from which users can navigate to Solo, Versus, or exit.
     *
     * @param primaryStage the primary stage for this application
     * @throws Exception if loading the FXML or initializing the scene fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        MUSIC_PLAYER.playMusic(DEFAULT_MUSIC);   // play background music

        // Load main menu
        URL location = getClass().getClassLoader().getResource("mainMenu.fxml");
        ResourceBundle resources = null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, resources);
        Parent root = fxmlLoader.load();
        MainMenuController menuController = fxmlLoader.getController();
        menuController.setPrimaryStage(primaryStage);

        primaryStage.setTitle("TetrisJFX - Main Menu");
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);    // Full screen
        primaryStage.show();
    }
}
