public class Player {

    private String name;
    private StandardCard[] holeCards;

    public Player(String name) {
        this.name = name;
        this.holeCards = new StandardCard[2];
    }

    public String getName() {
        return name;
    }

    public StandardCard[] getHoleCards() {
        return holeCards;
    }

    public void setHoleCards(StandardCard[] cards) {
        this.holeCards = cards;
    }
}