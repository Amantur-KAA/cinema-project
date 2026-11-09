package kg.alatoo.cinema.model;

/** VIP-зал: кресла-диваны, обслуживание (наценка x1.5). */
public class VipHall extends Hall {

    public VipHall(int id, String name, int seats) {
        super(id, name, seats);
    }

    @Override
    public double getPriceMultiplier() {
        return 1.5;
    }

    @Override
    public String getType() {
        return "VIP";
    }
}
