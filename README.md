# CW2025 — Tetris Game

[Repository on GitHub](https://github.com/AlvinChongChong/CW2025)

---

## Table of Contents

1. [Github Link](#github-link)
2. [Compilation Instructions](#compilation--run-instructions)
3. [Implemented and Working Properly](#implemented--working-properly)
4. [Implemented but Not Working Properly](#implemented-but-not-working-properly)
5. [Features Not Implemented](#features-not-implemented)
6. [New Java Classes](#new-java-classes)
7. [Modified Java Classes](#modified-java-classes)
8. [Unexpected Problems](#unexpected-problems)

---

## 1. Github Link

[https://github.com/AlvinChongChong/CW2025](https://github.com/AlvinChongChong/CW2025)

---

## 2. Compilation & Run Instructions

### Option 1 — Run from IntelliJ IDEA 

1. Download / clone the source code.

```bash
git clone https://github.com/AlvinChongChong/CW2025.git
```

2. Open IntelliJ IDEA → **File → Open** → select the `CW2025` project folder.
3. Make sure Java 17 and JavaFX SDK (version 25) are installed and configured.

**Create Run Configuration**

* Run → Edit Configurations → `+` → **Application**
* Main class: `com.comp2042.Main`
* VM options 

```
--module-path "C:\path\to\javafx-sdk-25\lib" --add-modules javafx.controls,javafx.fxml
```

4. Build and run.

* Right-click `Main.java` → Run 'Main'

---

### Option 2 — Run the packaged artifact (run `.bat`)

1. Download the whole `CW2025` folder (artifact included).
2. Open `out/artifacts/CW2025_jar/CW2025_jar` (or the folder containing `run.bat` and `CW2025.jar`).
3. Double-click `run.bat` to launch the game.

---

## 3. Implemented and Working Properly

| Feature              | What it does                                     |
| -------------------- | ------------------------------------------------ |
| Hard Drop            | Instantly drop the brick to the bottom           |
| Line Clearing        | Complete rows removed; score updates accordingly |
| Score and Line Counter | Tracks current score and lines cleared           |
| Next Block Preview   | Shows the upcoming brick                         |
| Pause / Resume       | Pauses gameplay and UI                           |
| Background Music     | Plays looping music during gameplay              |
| Restart              | Restart the current game                         |
| Ghost Brick          | Shows where the falling brick will land          |
| Main Menu            | Options to play Solo or Local Versus             |
| Versus Mode          | Local multiplayer: compare scores                |
| High Score           | Records high score for solo game                 |
| Line Clear Effect    | Visual effect when lines clear                   |
| Music On/Off         | Toggle music in pause menu                       |
| Leveling             | Increase fall speed as level increases           |
| Control Instructions | On-screen instructions for controls              |

---

## 4. Implemented but Not Working Properly

All features currently work as expected. 

---

## 5. Features Not Implemented

* Alternative game modes (e.g., unlimited time mode) — Not enough time to implement.
* Full Settings screen (adjust volume/brightness) — Not enough time to implement.

---

## 6. New Java Classes

|                     Class | Function                              |
| ------------------------: | ------------------------------------- |
|     `MusicPlayerWav.java` | Adds background music support         |
| `MainMenuController.java` | Main menu UI and navigation           |
|   `VersusController.java` | Versus mode                           |
|   `HighScoreManager.java` | Record and retrieve high scores       |
|          `HoldEvent.java` | Manage hold-piece events              |
|      `HoldShapeInfo.java` | Store shape and color for hold mechanic |

---

## 7. Modified Java Classes

### **GuiController.java**

* Added score label
* Added timer
* Added pause and resume functionality
* Added restart function
* Implemented ghost brick
* Used images for timer, score, and line counter
* Added next brick preview
* Added exit game and exit to main menu
* Updated high score record
* Added hold brick feature
* Added level
* Added line clear effect

### **GameController.java**

* Display line-clear score
* Prevent falling brick collision
* Handle and display hold brick
* Added line clear effect

### **Score.java**

* Added score calculation logic
* Added line-clearing logic

### **SimpleBoard.java**

* Adjusted brick spawn height
* Added score and line clear tracking
* Enabled hold brick functionality

### **Main.java**

* Play background music on launch
* Maximize screen automatically
* Load main menu on startup
* On/Off for music 

### **InputEventListener.java**

* Added `canMoveDown()` check
* Added hold brick handling

### **EventType.java**

* Added new event type: `HARD_DROP`

---

## 8. Unexpected Problems (and fixes)

1. **Ghost bricks stacking** — solved by rewriting ghost brick code.
2. **Ghost bricks and static blocks floating above frame** — fixed by adjusting ghost logic and coordinates.
3. **Falling brick dropped too early** — caused by `HIDDEN_ROWS`; removing it corrected the drop timing.
4. **Static bricks and frame movement when moving falling brick** — fixed by coordinate adjustments.
5. **Falling brick alignment and weird white pixel on top left** — fixed by coordinate adjustments.

---


