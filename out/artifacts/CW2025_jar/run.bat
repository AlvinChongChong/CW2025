@echo off
REM Set path to JavaFX lib folder
set PATH_TO_FX="%~dp0javafx-sdk-25\lib"

REM Run the JavaFX program
java --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml -jar "%~dp0CW2025.jar"

pause
