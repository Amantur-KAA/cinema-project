package kg.alatoo.cinema.model;

/** IMAX-зал: большой экран и звук (наценка x1.3). */
public class ImaxHall extends Hall {

    public ImaxHall(int id, String name, int seats) {
        super(id, name, seats);
    }

    @Override
    public double getPriceMultiplier() {
        return 1.3;
    }

    @Override
    public String getType() {
        return "IMAX";
    }
}
