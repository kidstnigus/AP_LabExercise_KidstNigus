public class PokerGame {

    private StandardDeck deck;
    private Player player;
    private Player dealer;
    private StandardCard[] communityCards;

    public PokerGame() {
        deck = new StandardDeck();

        player = new Player("Player");
        dealer = new Player("Dealer");

        communityCards = new StandardCard[5];

        dealCards();
    }

    private void dealCards() {

        player.setHoleCards(new StandardCard[]{
                deck.drawCard(),
                deck.drawCard()
        });

        dealer.setHoleCards(new StandardCard[]{
                deck.drawCard(),
                deck.drawCard()
        });

        for (int i = 0; i < 5; i++) {
            communityCards[i] = deck.drawCard();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Player getDealer() {
        return dealer;
    }

    public StandardCard[] getCommunityCards() {
        return communityCards;
    }
}