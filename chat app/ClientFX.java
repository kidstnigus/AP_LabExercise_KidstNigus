import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientFX extends Application {

    private BufferedReader in;
    private PrintWriter out;

    private ListView<String> usersList = new ListView<>();
    private ScrollPane chatScrollPane = new ScrollPane();
    private VBox messageContainer = new VBox(12); 
    private TextField messageField = new TextField();

    private String username;
    private String selectedUser;

    @Override
    public void start(Stage stage) {
        showLogin(stage);
    }

    private void showLogin(Stage stage) {
        TextField name = new TextField();
        Button login = new Button("Enter ChatIT");

        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-text-fill: #b9bbbe; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: bold;");
        
        name.setPromptText("Type your username...");
        name.setStyle("-fx-background-color: #2f3136; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 8;");
        
        login.setStyle("-fx-background-color: #31d266; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 16; -fx-cursor: hand;");

        VBox box = new VBox(10, userLabel, name, login);
        box.setStyle("-fx-padding:25; -fx-alignment:center; -fx-background-color: #101010;");

        login.setOnAction(e -> {
            username = name.getText().trim();

            if (username.isEmpty()) {
                showAlert("Username required!");
                return;
            }

            login.setDisable(true);
            login.setText("Connecting...");

            new Thread(() -> {
                try {
                    connect(stage);
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        showAlert("Cannot connect to server!");
                        login.setDisable(false);
                        login.setText("Enter ChatIT");
                    });
                }
            }).start();
        });

        stage.setScene(new Scene(box, 300, 180));
        stage.setTitle("ChatIT Login");
        stage.show();
    }

    private void connect(Stage stage) {
        try {
            Socket socket = new Socket("localhost", 1234);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(username);

            Thread listener = new Thread(this::listen);
            listener.setDaemon(true);
            listener.start();

            Platform.runLater(() -> buildUI(stage));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void buildUI(Stage stage) {
        messageContainer.setPadding(new Insets(20));
        messageContainer.setStyle("-fx-background-color: #000000;");
        
        chatScrollPane.setContent(messageContainer);
        chatScrollPane.setFitToWidth(true);
        chatScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        chatScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        chatScrollPane.setStyle("-fx-background: #000000; -fx-background-color: #000000; -fx-border-color: transparent;");

        usersList.setStyle("""
            -fx-control-inner-background: #1a1a1a;
            -fx-background-color: #1a1a1a;
            -fx-text-fill: #b9bbbe;
            -fx-font-family: 'Segoe UI', sans-serif;
            -fx-font-size: 14;
        """);
        
        usersList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText("●  " + item);
                    if (isSelected()) {
                        setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-padding: 8 12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: transparent; -fx-text-fill: #b9bbbe; -fx-padding: 8 12;");
                    }
                }
            }
        });

        usersList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedUser = newVal;
        });

        Button send = new Button("➤");
        send.setStyle("-fx-background-color: #31d266; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-size: 14; -fx-padding: 10 16; -fx-cursor: hand;");
        send.setOnAction(e -> sendMessage());
        
        messageField.setPromptText("Message...");
        messageField.setStyle("""
            -fx-background-color: #262626;
            -fx-text-fill: white;
            -fx-background-radius: 20;
            -fx-padding: 10;
            -fx-font-size: 14;
        """);
        messageField.setOnAction(e -> sendMessage());

        HBox bottom = new HBox(10, messageField, send);
        HBox.setHgrow(messageField, Priority.ALWAYS);
        bottom.setStyle("-fx-padding: 15; -fx-background-color: #101010; -fx-border-color: #202225; -fx-border-width: 1 0 0 0;");
        bottom.setAlignment(Pos.CENTER);

        Label onlineLabel = new Label("Online Users");
        onlineLabel.setStyle("-fx-text-fill: #72767d; -fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: bold; -fx-font-size: 12;");

        VBox left = new VBox(8, onlineLabel, usersList);
        left.setStyle("-fx-padding: 15; -fx-background-color: #1a1a1a; -fx-border-color: #202225; -fx-border-width: 0 1 0 0;");
        left.setPrefWidth(200);
        VBox.setVgrow(usersList, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setLeft(left);
        root.setCenter(chatScrollPane);
        root.setBottom(bottom);
        root.setStyle("-fx-background-color: #000000;");

        stage.setScene(new Scene(root, 750, 500));
        stage.setTitle("ChatIT - " + username);
        stage.show();
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (selectedUser == null) {
            showAlert("Please select a user from the 'Online Users' list to message!");
            return;
        }
        if (msg.isEmpty()) return;

        out.println(selectedUser + ":" + msg);

        appendBubbleMessage("Me → " + selectedUser, msg, true);
        messageField.clear();
        
        // Return active cursor focus back to input field automatically so you can keep typing seamlessly
        Platform.runLater(() -> messageField.requestFocus());
    }

    private void listen() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {

                if (msg.startsWith("USERS:")) {
                    String users = msg.substring(6);
                    Platform.runLater(() -> {
                        String currentSelection = selectedUser;
                        usersList.getItems().setAll(users.split(","));
                        if (usersList.getItems().contains(currentSelection)) {
                            usersList.getSelectionModel().select(currentSelection);
                            selectedUser = currentSelection;
                        } else {
                            selectedUser = null;
                        }
                    });
                } 
                else if (msg.startsWith("MSG:")) {
                    String[] parts = msg.split(":", 3);
                    if (parts.length >= 3) {
                        String from = parts[1];
                        String text = parts[2];

                        Platform.runLater(() -> {
                            appendBubbleMessage(from, text, false);
                        });
                    }
                }
            }
        } catch (Exception e) {
            Platform.runLater(() -> showAlert("Connection lost to server."));
        }
    }

    private void appendBubbleMessage(String sender, String body, boolean isMe) {
        Label senderLabel = new Label(isMe ? "Me" : sender);
        senderLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px; -fx-font-family: 'Segoe UI', sans-serif; -fx-padding: 0 4 0 4;");

        Label bodyLabel = new Label(body);
        bodyLabel.setWrapText(true);
        bodyLabel.setMaxWidth(450);
        
        if (isMe) {
            bodyLabel.setStyle("""
                -fx-background-color: #31d266;
                -fx-text-fill: white;
                -fx-background-radius: 18 18 2 18;
                -fx-padding: 10 16;
                -fx-font-family: 'Segoe UI', Helvetica, sans-serif;
                -fx-font-size: 14px;
            """);
        } else {
            bodyLabel.setStyle("""
                -fx-background-color: #26262b;
                -fx-text-fill: white;
                -fx-background-radius: 18 18 18 2;
                -fx-padding: 10 16;
                -fx-font-family: 'Segoe UI', Helvetica, sans-serif;
                -fx-font-size: 14px;
            """);
        }

        VBox cellLayout = new VBox(3, senderLabel, bodyLabel);
        HBox alignmentWrapper = new HBox(cellLayout);
        
        if (isMe) {
            alignmentWrapper.setAlignment(Pos.CENTER_RIGHT);
            cellLayout.setAlignment(Pos.TOP_RIGHT);
        } else {
            alignmentWrapper.setAlignment(Pos.CENTER_LEFT);
            cellLayout.setAlignment(Pos.TOP_LEFT);
        }

        messageContainer.getChildren().add(alignmentWrapper);
        
        // FIXED: Force JavaFX engine to recalculate layout dimensions before performing scroll jumps
        messageContainer.layout();
        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ChatIT Notification");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a1a;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #dcddde; -fx-font-family: 'Segoe UI';");
        
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}