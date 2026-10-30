package ru.mirea.apimarketplace.ui;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/** Безопасное чтение данных из консоли: при неверном вводе сообщает об ошибке и спрашивает снова. */
public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput(InputStream in) {
        this.scanner = new Scanner(in, StandardCharsets.UTF_8);
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            throw new InputClosedException();
        }
        return scanner.nextLine().trim();
    }

    /** Строка, которая не может быть пустой. */
    public String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Ошибка: значение не может быть пустым.");
        }
    }

    /** Строка; если ввести пустую — остаётся текущее значение. */
    public String readOrKeep(String prompt, String current) {
        String value = readLine(prompt + " [Enter — оставить: " + current + "]: ");
        return value.isEmpty() ? current : value;
    }

    public long readId(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: ID должен быть целым числом.");
            }
        }
    }

    public int readInt(String prompt, String errorMessage) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: " + errorMessage);
            }
        }
    }

    /** Как readInt, но пустой ввод оставляет текущее значение. */
    public int readIntOrKeep(String prompt, String errorMessage, int current) {
        while (true) {
            String value = readLine(prompt + " [Enter — оставить: " + current + "]: ");
            if (value.isEmpty()) {
                return current;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: " + errorMessage);
            }
        }
    }

    /** Номер пункта меню от 0 до max. */
    public int readMenuChoice(int max) {
        while (true) {
            String value = readLine("Выберите действие: ");
            try {
                int choice = Integer.parseInt(value);
                if (choice >= 0 && choice <= max) {
                    return choice;
                }
            } catch (NumberFormatException e) {
                // сообщение об ошибке ниже
            }
            System.out.println("Ошибка: введите число от 0 до " + max + ".");
        }
    }

    /** Выбор значения enum по номеру. */
    public <E extends Enum<E>> E readEnum(String title, E[] values) {
        return readEnum(title, values, null);
    }

    /** Выбор значения enum по номеру; если current не null, пустой ввод оставляет его. */
    public <E extends Enum<E>> E readEnum(String title, E[] values, E current) {
        System.out.println(title + ":");
        for (int i = 0; i < values.length; i++) {
            System.out.println("  " + (i + 1) + ". " + values[i]);
        }
        String hint = current == null ? "" : " [Enter — оставить: " + current + "]";
        while (true) {
            String value = readLine("Введите номер" + hint + ": ");
            if (value.isEmpty() && current != null) {
                return current;
            }
            try {
                int number = Integer.parseInt(value);
                if (number >= 1 && number <= values.length) {
                    return values[number - 1];
                }
            } catch (NumberFormatException e) {
                // сообщение об ошибке ниже
            }
            System.out.println("Ошибка: введите число от 1 до " + values.length + ".");
        }
    }

    /** Порядок сортировки. */
    public enum Order {
        ASC("По возрастанию"),
        DESC("По убыванию");

        private final String title;

        Order(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    /** Спрашивает порядок сортировки: true — обратный (по убыванию). */
    public boolean readDescending() {
        return readEnum("Порядок сортировки", Order.values()) == Order.DESC;
    }

    /** Выбор одного варианта из списка строк; возвращает номер от 1 до options.size(). */
    public int readOption(String title, java.util.List<String> options) {
        System.out.println(title + ":");
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + options.get(i));
        }
        while (true) {
            String value = readLine("Введите номер: ");
            try {
                int number = Integer.parseInt(value);
                if (number >= 1 && number <= options.size()) {
                    return number;
                }
            } catch (NumberFormatException e) {
                // сообщение об ошибке ниже
            }
            System.out.println("Ошибка: введите число от 1 до " + options.size() + ".");
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                System.out.println("Ошибка: дата должна быть в формате ГГГГ-ММ-ДД (например, 2026-10-04).");
            }
        }
    }

    public boolean confirm(String question) {
        String answer = readLine(question + " (да/нет): ").toLowerCase();
        return answer.equals("да") || answer.equals("д") || answer.equals("y") || answer.equals("yes");
    }
}
