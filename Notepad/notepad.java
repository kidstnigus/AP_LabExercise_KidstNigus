import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class notepad extends Application {
    TextArea textArea = new TextArea();
    Label statusBar = new Label("Ln 1, Col 1");
    Label wordCountLabel = new Label("Words: 0");
    File currentFile = null;
    boolean isModified = false;
    int fontSize = 14;
    @Override
    public void start(Stage primaryStage) {

        BorderPane root = new BorderPane();
    
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        Menu editMenu = new Menu("Edit");
        Menu viewMenu = new Menu("View");
        Menu helpMenu = new Menu("Help");
        
        MenuItem newFile = new MenuItem("New");
        MenuItem openFile = new MenuItem("Open");
        MenuItem saveFile = new MenuItem("Save");
        MenuItem saveAs = new MenuItem("Save As");
        MenuItem exit = new MenuItem("Exit");

        MenuItem undo = new MenuItem("Undo");
        MenuItem redo = new MenuItem("Redo");
        MenuItem cut = new MenuItem("Cut");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        MenuItem find = new MenuItem("Find");

        MenuItem wrapText = new MenuItem("Toggle Word Wrap");

        MenuItem increaseFont = new MenuItem("Increase Font Size");
        MenuItem decreaseFont = new MenuItem("Decrease Font Size");

        MenuItem about = new MenuItem("About");

        newFile.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        openFile.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        saveFile.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveAs.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
        find.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));

        fileMenu.getItems().addAll(newFile, openFile, saveFile, saveAs, new SeparatorMenuItem(), exit);
        editMenu.getItems().addAll( undo, redo, new SeparatorMenuItem(), cut, copy, paste, new SeparatorMenuItem(), find );
        viewMenu.getItems().addAll( wrapText, increaseFont, decreaseFont);
        helpMenu.getItems().add(about);
        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);
        textArea.setWrapText(true);

        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!isModified) {
                isModified = true;
                updateTitle(primaryStage);
            }
            updateWordCount();
        });

        textArea.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
            int caretPos = newVal.intValue();
            String text = textArea.getText();

            int line = text.substring(0, Math.min(caretPos, text.length())).split("\\n", -1).length;
            int lastNewLine = text.substring(0, Math.min(caretPos, text.length())).lastIndexOf("\n");
            int col = caretPos - lastNewLine;

            statusBar.setText("Ln " + line + ", Col " + col);
        });

        HBox statusBox = new HBox(25, statusBar, wordCountLabel);
        statusBox.setPadding(new Insets(6, 12, 6, 12));

        newFile.setOnAction(e -> {
            textArea.clear();
            currentFile = null;
            isModified = false;
            updateTitle(primaryStage);
        });

        openFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );

            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    textArea.setText(Files.readString(file.toPath()));
                    currentFile = file;
                    isModified = false;
                    updateTitle(primaryStage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        saveFile.setOnAction(e -> save(primaryStage, false));
        saveAs.setOnAction(e -> save(primaryStage, true));
        
        undo.setOnAction(e -> textArea.undo());
        redo.setOnAction(e -> textArea.redo());
        cut.setOnAction(e -> textArea.cut());
        copy.setOnAction(e -> textArea.copy());
        paste.setOnAction(e -> textArea.paste());
        
        find.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Find");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter search term:");
            dialog.showAndWait().ifPresent(word -> {
                String text = textArea.getText();
                int index = text.indexOf(word);

                if (index >= 0) {
                    textArea.requestFocus();
                    textArea.selectRange(index, index + word.length());
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Not Found");
                    alert.setHeaderText(null);
                    alert.setContentText("Word '" + word + "' could not be found.");
                    alert.showAndWait();
                }
            });
        });
        wrapText.setOnAction(e -> textArea.setWrapText(!textArea.isWrapText()));
        increaseFont.setOnAction(e -> {
            fontSize = Math.min(fontSize + 2, 72);
            applyTheme(root, menuBar, statusBox, statusBar, wordCountLabel);
        });

        decreaseFont.setOnAction(e -> {
            fontSize = Math.max(fontSize - 2, 8);
            applyTheme(root, menuBar, statusBox, statusBar, wordCountLabel);
        });
 
        about.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("JavaFX Notepad Professional");
            alert.setContentText(
                    "An optimized, high-performance workspace engineered for text processing.\n\n" +
                    "Architectural Elements:\n" +
                    "• Streamlined Thread Autosave\n" +
                    "• Monospaced Typography Layout\n" +
                    "• Adaptive Workspace Contrast Engine"
            );
            alert.showAndWait();
        });

        exit.setOnAction(e -> handleExit(primaryStage));

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            handleExit(primaryStage);
        });
        
        Timeline autoSave = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    if (currentFile != null && isModified) {
                        save(primaryStage, false);
                    }
                })
        );
        autoSave.setCycleCount(Animation.INDEFINITE);
        autoSave.play();

        root.setTop(menuBar);
        root.setCenter(textArea);
        root.setBottom(statusBox);

        applyTheme(root, menuBar, statusBox, statusBar, wordCountLabel);
        Scene scene = new Scene(root, 950, 650);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        updateTitle(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void updateTitle(Stage stage) {
        String fileName = (currentFile == null) ? "Untitled" : currentFile.getName();
        String indicator = isModified ? " •" : ""; 
        stage.setTitle(fileName + indicator + " — NotepadPro");
    }

    private void save(Stage stage, boolean saveAs) {
        try {
            if (currentFile == null || saveAs) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Text Files", "*.txt")
                );

                File file = fileChooser.showSaveDialog(stage);
                if (file != null) {
                    currentFile = file;
                }
            }

            if (currentFile != null) {
                Files.writeString(currentFile.toPath(), textArea.getText());
                isModified = false;
                updateTitle(stage);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void handleExit(Stage stage) {
        if (!isModified) {
            stage.close();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Project Context");
        alert.setHeaderText("You have unsaved changes.");
        alert.setContentText("Would you like to preserve modifications before termination?");

        ButtonType saveBtn = new ButtonType("Save");
        ButtonType dontSaveBtn = new ButtonType("Don't Save");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveBtn, dontSaveBtn, cancelBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == saveBtn) {
                save(stage, false);
                if (!isModified) stage.close();
            } else if (response == dontSaveBtn) {
                stage.close();
            }
        });
    }
    private void updateWordCount() {
        String text = textArea.getText().trim();
        if (text.isEmpty()) {
            wordCountLabel.setText("Words: 0");
            return;
        }
        int words = text.split("\\s+").length;
        wordCountLabel.setText("Words: " + words);
    }

private void applyTheme(BorderPane root, MenuBar menuBar, HBox statusBox, Label statusBar, Label wordCount) {

    root.setStyle("-fx-background-color: #1e1e1e;");
    statusBox.setStyle("-fx-background-color: #007acc; -fx-border-color: #2d2d2d; -fx-border-width: 1 0 0 0;");
    statusBar.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");
    wordCount.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px;");

    textArea.setStyle( "-fx-control-inner-background: #1e1e1e;" + "-fx-text-fill: #d4d4d4;" + "-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace;" + "-fx-font-size: " + fontSize + "px;" +
        "-fx-background-color: transparent;" + "-fx-padding: 8;");
}
    public static void main(String[] args) {
        launch();
    }
}