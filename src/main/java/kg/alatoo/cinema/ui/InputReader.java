package kg.alatoo.cinema.ui;

import kg.alatoo.cinema.model.Screening;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Чтение ввода с консоли с проверкой. Повторяет вопрос, пока ввод неверный. */
public class InputReader {
    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("Ввод закрыт. Программа завершена.");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    public String readNonEmpty(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  Поле не может быть пустым.");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println("  Введите число от " + min + " до " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Это не целое число: «" + value + "».");
            }
        }
    }

    public double readDouble(String prompt, double min, double max) {
        while (true) {
            String value = readLine(prompt).replace(',', '.');
            try {
                double number = Double.parseDouble(value);
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println("  Введите число от " + min + " до " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Это не число: «" + value + "».");
            }
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            String value = readLine(prompt + " (да/нет): ").toLowerCase();
            if (value.equals("да") || value.equals("д") || value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("нет") || value.equals("н") || value.equals("n") || value.equals("no")) {
                return false;
            }
            System.out.println("  Ответьте «да» или «нет».");
        }
    }

    public LocalDateTime readDateTime(String prompt) {
        while (true) {
            String value = readLine(prompt + " (дд.мм.гггг чч:мм): ");
            try {
                return LocalDateTime.parse(value, Screening.FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("  Неверный формат. Пример: 25.12.2026 18:30");
            }
        }
    }
}
