package kg.alatoo.cinema.report;

/** Строка отчёта «Постоянные клиенты». */
public class CustomerStats {
    private final String fullName;
    private final int tickets;
    private final double totalSpent;

    public CustomerStats(String fullName, int tickets, double totalSpent) {
        this.fullName = fullName;
        this.tickets = tickets;
        this.totalSpent = totalSpent;
    }

    public String getFullName() {
        return fullName;
    }

    public int getTickets() {
        return tickets;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    @Override
    public String toString() {
        return String.format("%-25s билетов: %3d   потратил: %10.2f сом", fullName, tickets, totalSpent);
    }
}
