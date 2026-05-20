import java.util.ArrayList;
import java.util.Collections;

public class StandardDeck {

    private ArrayList<StandardCard> deck;

    public StandardDeck() {
        deck = new ArrayList<>();

        String[] suits = {"C","D","H","S"};
        String[] values = {
                "2","3","4","5","6","7","8","9","10",
                "J","Q","K","A"
        };

        for (String s : suits) {
            for (String v : values) {
                deck.add(new StandardCard(v, s));
            }
        }

        Collections.shuffle(deck);
    }

    public StandardCard drawCard() {
        return deck.remove(deck.size() - 1);
    }
}