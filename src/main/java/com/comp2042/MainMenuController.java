package com.comp2042;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main menu screen.
 * Handles navigation to Solo mode, Versus mode, and exiting the game.
 */
public class MainMenuController implements Initializable {

    @FXML
    private Button soloButton;

    @FXML
    private Button versusButton;

    @FXML
    private Button exitButton;

    private Stage primaryStage;

    /**
     * Sets the primary stage for scene navigation.
     *
     * @param stage the primary stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization if needed
    }

    /**
     * Starts the Solo game mode.
     * Loads the game layout and initializes the game controller.
     *
     * @param event the action event
     */
    @FXML
    private void startSolo(ActionEvent event) {
        try {
            URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location);
            Parent root = fxmlLoader.load();
            GuiController guiController = fxmlLoader.getController();

            // Get current stage dimensions to maintain full screen
            double width = primaryStage.getWidth();
            double height = primaryStage.getHeight();
            
            // If stage is not yet shown or dimensions are invalid, use screen dimensions
            if (width <= 0 || height <= 0) {
                javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                width = bounds.getWidth();
                height = bounds.getHeight();
            }
            
            Scene gameScene = new Scene(root, width, height);
            primaryStage.setScene(gameScene);
            primaryStage.setMaximized(true);
            primaryStage.setTitle("TetrisJFX - Solo");

            // Initialize the game
            new GameController(guiController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the Versus game mode (local two-player).
     *
     * @param event the action event
     */
    @FXML
    private void startVersus(ActionEvent event) {
        try {
            URL location = getClass().getClassLoader().getResource("versusLayout.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(location);
            Parent root = fxmlLoader.load();
            VersusController versusController = fxmlLoader.getController();
            versusController.setPrimaryStage(primaryStage);

            // Maintain full screen
            double width = primaryStage.getWidth();
            double height = primaryStage.getHeight();
            

            if (width <= 0 || height <= 0) {
                javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                width = bounds.getWidth();
                height = bounds.getHeight();
            }
            
            Scene versusScene = new Scene(root, width, height);
            primaryStage.setScene(versusScene);
            primaryStage.setMaximized(true);
            primaryStage.setTitle("TetrisJFX - Versus");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Exits the game application.
     *
     * @param event the action event
     */
    @FXML
    private void exitGame(ActionEvent event) {
        System.exit(0);
    }

    /**
     * Adds hover effect to buttons.
     *
     * @param event the mouse event
     */
    @FXML
    private void onButtonHover(MouseEvent event) {
        Button button = (Button) event.getSource();
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.CYAN);
        shadow.setRadius(15);
        button.setEffect(shadow);
        button.setScaleX(1.05);
        button.setScaleY(1.05);
    }

    /**
     * Removes hover effect from buttons.
     *
     * @param event the mouse event
     */
    @FXML
    private void onButtonExit(MouseEvent event) {
        Button button = (Button) event.getSource();
        button.setEffect(null);
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }
}

