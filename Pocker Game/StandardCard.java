public class StandardCard {
    private String value;
    private String suit;

    public StandardCard(String value, String suit) {
        this.value = value;
        this.suit = suit;
    }

    public String getValue() { return value; }
    public String getSuit() { return suit; }

    public int getRank() {
        String[] vals = {"2","3","4","5","6","7","8","9","10","J","Q","K","A"};
        for (int i = 0; i < vals.length; i++)
            if (vals[i].equals(value)) return i;
        return 0;
    }

    public String getImagePath() {
        return "file:cards/" + value + "-" + suit + ".png";
    }
}