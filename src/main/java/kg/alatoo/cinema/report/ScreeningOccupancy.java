package kg.alatoo.cinema.report;

/** Строка отчёта «Заполненность сеансов». */
public class ScreeningOccupancy {
    private final int screeningId;
    private final String title;
    private final String hallName;
    private final int seats;
    private final int sold;

    public ScreeningOccupancy(int screeningId, String title, String hallName, int seats, int sold) {
        this.screeningId = screeningId;
        this.title = title;
        this.hallName = hallName;
        this.seats = seats;
        this.sold = sold;
    }

    public double getPercent() {
        return seats == 0 ? 0 : sold * 100.0 / seats;
    }

    public int getScreeningId() {
        return screeningId;
    }

    public int getSold() {
        return sold;
    }

    @Override
    public String toString() {
        return String.format("Сеанс #%-3d %-22s %-12s занято %3d из %3d (%.1f%%)",
                screeningId, title, hallName, sold, seats, getPercent());
    }
}
