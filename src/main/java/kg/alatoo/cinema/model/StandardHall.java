package kg.alatoo.cinema.model;

/** Обычный зал (наценка x1.0). */
public class StandardHall extends Hall {

    public StandardHall(int id, String name, int seats) {
        super(id, name, seats);
    }

    @Override
    public double getPriceMultiplier() {
        return 1.0;
    }

    @Override
    public String getType() {
        return "STANDARD";
    }
}
