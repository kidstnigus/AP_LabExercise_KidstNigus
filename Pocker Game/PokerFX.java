import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PokerFX extends Application {

    PokerGame game;

    HBox dealerBox = new HBox(10);
    HBox communityBox = new HBox(10);
    HBox playerBox = new HBox(10);

    Label statusLabel = new Label("Press Deal To Start");

    int index = 0;

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: darkgreen;");

        dealerBox.setAlignment(Pos.CENTER);
        communityBox.setAlignment(Pos.CENTER);
        playerBox.setAlignment(Pos.CENTER);

        VBox center = new VBox(20);
        center.setAlignment(Pos.CENTER);

        Label dealerLabel = new Label("Dealer Cards");
        dealerLabel.setTextFill(Color.WHITE);

        Label tableLabel = new Label("Community Cards");
        tableLabel.setTextFill(Color.WHITE);

        Label playerLabel = new Label("Your Cards");
        playerLabel.setTextFill(Color.WHITE);

        statusLabel.setTextFill(Color.WHITE);

        center.getChildren().addAll(
                dealerLabel, dealerBox,
                tableLabel, communityBox,
                playerLabel, playerBox,
                statusLabel
        );

        Button dealBtn = new Button("Deal");
        Button nextBtn = new Button("Next");
        Button resultBtn = new Button("Show Winner");

        HBox buttons = new HBox(20, dealBtn, nextBtn, resultBtn);
        buttons.setAlignment(Pos.CENTER);

        root.setCenter(center);
        root.setBottom(buttons);

        dealBtn.setOnAction(e -> startGame());
        nextBtn.setOnAction(e -> nextCard());
        resultBtn.setOnAction(e -> showWinner());

        stage.setScene(new Scene(root, 1000, 700));
        stage.setTitle("Poker Game");
        stage.show();
    }

    void startGame() {

        game = new PokerGame();

        dealerBox.getChildren().clear();
        playerBox.getChildren().clear();
        communityBox.getChildren().clear();

        index = 0;

       
        for (StandardCard c : game.getPlayer().getHoleCards()) {
            playerBox.getChildren().add(createCardView(c));
        }
        
        for (int i = 0; i < 2; i++) {
            dealerBox.getChildren().add(createBackCard());
        }

        statusLabel.setText("Game Started");
    }

    void nextCard() {
        if (index < 5) {
            communityBox.getChildren().add(
                    createCardView(game.getCommunityCards()[index])
            );
            index++;
        }
    }

    void showWinner() {

        dealerBox.getChildren().clear();

        for (StandardCard c : game.getDealer().getHoleCards()) {
            dealerBox.getChildren().add(createCardView(c));
        }

        int playerScore = evaluate(game.getPlayer());
        int dealerScore = evaluate(game.getDealer());

        if (playerScore > dealerScore) {
            statusLabel.setText("You WIN 🎉");
        } else if (dealerScore > playerScore) {
            statusLabel.setText("You LOST ❌");
        } else {
            statusLabel.setText("DRAW");
        }
    }

    int evaluate(Player player) {

        StandardCard[] all = new StandardCard[7];

        System.arraycopy(player.getHoleCards(), 0, all, 0, 2);
        System.arraycopy(game.getCommunityCards(), 0, all, 2, 5);

        int pairs = 0;

        for (int i = 0; i < all.length; i++) {
            for (int j = i + 1; j < all.length; j++) {
                if (all[i].getValue().equals(all[j].getValue())) {
                    pairs++;
                }
            }
        }

        return pairs;
    }

    ImageView createCardView(StandardCard card) {
        ImageView iv = new ImageView(new Image(card.getImagePath()));
        iv.setFitWidth(90);
        iv.setFitHeight(130);
        return iv;
    }

    ImageView createBackCard() {
        ImageView iv = new ImageView(new Image("file:cards/back.png"));
        iv.setFitWidth(90);
        iv.setFitHeight(130);
        return iv;
    }

    public static void main(String[] args) {
        launch(args);
    }
}