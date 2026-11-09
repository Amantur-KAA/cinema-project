package kg.alatoo.cinema.report;

/** Строка отчёта «Выручка по фильмам». */
public class FilmRevenue {
    private final String title;
    private final int ticketsSold;
    private final double revenue;

    public FilmRevenue(String title, int ticketsSold, double revenue) {
        this.title = title;
        this.ticketsSold = ticketsSold;
        this.revenue = revenue;
    }

    public String getTitle() {
        return title;
    }

    public int getTicketsSold() {
        return ticketsSold;
    }

    public double getRevenue() {
        return revenue;
    }

    @Override
    public String toString() {
        return String.format("%-25s билетов: %3d   выручка: %10.2f сом", title, ticketsSold, revenue);
    }
}
