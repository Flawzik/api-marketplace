package ru.mirea.apimarketplace.ui;

import ru.mirea.apimarketplace.model.ApiCatalogItem;
import ru.mirea.apimarketplace.model.ApiClient;
import ru.mirea.apimarketplace.model.ApiConnectionRequest;
import ru.mirea.apimarketplace.model.ApiKey;
import ru.mirea.apimarketplace.model.TableDump;
import ru.mirea.apimarketplace.util.Formats;

import java.util.Arrays;
import java.util.List;

/** Форматированный вывод объектов в консоль. */
public final class ConsolePrinter {

    private ConsolePrinter() {
    }

    // ------------------------------------------------------------- клиенты

    public static void printClients(List<ApiClient> clients) {
        if (clients.isEmpty()) {
            System.out.println("Клиентов не найдено.");
            return;
        }
        String format = "%-5s %-24s %-26s %-20s %-14s %-16s%n";
        System.out.printf(format, "ID", "ФИО", "Email", "Компания", "Тип", "Регистрация");
        System.out.println("-".repeat(110));
        for (ApiClient c : clients) {
            System.out.printf(format, c.getId(), cut(c.getFullName(), 24), cut(c.getEmail(), 26),
                    cut(c.getCompanyName(), 20), cut(c.getType().getTitle(), 14), Formats.dateTime(c.getCreatedAt()));
        }
        System.out.println("Всего: " + clients.size());
    }

    public static void printClientDetails(ApiClient c) {
        printCard("Клиент №" + c.getId(),
                "ФИО", c.getFullName(),
                "Email", c.getEmail(),
                "Компания", c.getCompanyName(),
                "Тип клиента", String.valueOf(c.getType()),
                "Регистрация", Formats.dateTime(c.getCreatedAt()));
    }

    // -------------------------------------------------------------- заявки

    public static void printRequests(List<ApiConnectionRequest> requests) {
        if (requests.isEmpty()) {
            System.out.println("Заявок не найдено.");
            return;
        }
        String format = "%-5s %-26s %-18s %-16s %-10s %-16s %9s  %-16s%n";
        System.out.printf(format, "ID", "Название", "Клиент", "Тип API", "Приоритет", "Статус", "Запр./сут", "Создана");
        System.out.println("-".repeat(125));
        for (ApiConnectionRequest r : requests) {
            System.out.printf(format, r.getId(), cut(r.getTitle(), 26), cut(r.getClientName(), 18),
                    cut(r.getApiType().getTitle(), 16), r.getPriority(), cut(r.getStatus().getTitle(), 16),
                    r.getExpectedDailyRequests(), Formats.dateTime(r.getCreatedAt()));
        }
        System.out.println("Всего: " + requests.size());
    }

    public static void printRequestDetails(ApiConnectionRequest r) {
        printCard("Заявка №" + r.getId(),
                "Клиент", r.getClientName() + " (ID " + r.getClientId() + ")",
                "Название", r.getTitle(),
                "Описание", r.getDescription(),
                "Тип API", String.valueOf(r.getApiType()),
                "Приоритет", String.valueOf(r.getPriority()),
                "Статус", String.valueOf(r.getStatus()),
                "Запросов/сут.", String.valueOf(r.getExpectedDailyRequests()),
                "Создана", Formats.dateTime(r.getCreatedAt()),
                "Обновлена", Formats.dateTime(r.getUpdatedAt()));
    }

    // ------------------------------------------------------------- каталог

    public static void printCatalog(List<ApiCatalogItem> items) {
        if (items.isEmpty()) {
            System.out.println("API в каталоге не найдено.");
            return;
        }
        String format = "%-5s %-26s %-8s %-16s %-12s %12s %12s%n";
        System.out.printf(format, "ID", "Название", "Версия", "Тип API", "Статус", "Лимит/мин", "Руб./мес.");
        System.out.println("-".repeat(100));
        for (ApiCatalogItem i : items) {
            System.out.printf(format, i.getId(), cut(i.getName(), 26), i.getVersion(),
                    cut(i.getApiType().getTitle(), 16), cut(i.getStatus().getTitle(), 12),
                    i.getRateLimitPerMinute(), i.getMonthlyPrice());
        }
        System.out.println("Всего: " + items.size());
    }

    public static void printCatalogDetails(ApiCatalogItem i) {
        printCard("API №" + i.getId(),
                "Название", i.getName(),
                "Версия", i.getVersion(),
                "Описание", i.getDescription(),
                "Тип API", String.valueOf(i.getApiType()),
                "Статус", String.valueOf(i.getStatus()),
                "Лимит", i.getRateLimitPerMinute() + " запросов в минуту",
                "Цена", i.getMonthlyPrice() + " руб./мес.",
                "Добавлен", Formats.dateTime(i.getCreatedAt()));
    }

    // -------------------------------------------------------------- ключи

    public static void printKeys(List<ApiKey> keys) {
        if (keys.isEmpty()) {
            System.out.println("API-ключей не найдено.");
            return;
        }
        String format = "%-5s %-22s %-10s %-14s %-12s %-16s %-16s%n";
        System.out.printf(format, "ID", "Клиент", "Заявка", "Ключ", "Статус", "Выдан", "Действует до");
        System.out.println("-".repeat(105));
        for (ApiKey k : keys) {
            System.out.printf(format, k.getId(), cut(k.getClientName(), 22), k.getRequestId(),
                    cut(k.getKeyValue(), 12), cut(k.getStatus().getTitle(), 12),
                    Formats.dateTime(k.getIssuedAt()), Formats.dateTime(k.getExpiresAt()));
        }
        System.out.println("Всего: " + keys.size());
    }

    public static void printKeyDetails(ApiKey k) {
        printCard("API-ключ №" + k.getId(),
                "Клиент", k.getClientName() + " (ID " + k.getClientId() + ")",
                "Заявка", "№" + k.getRequestId(),
                "Ключ", k.getKeyValue(),
                "Статус", String.valueOf(k.getStatus()),
                "Выдан", Formats.dateTime(k.getIssuedAt()),
                "Действует до", Formats.dateTime(k.getExpiresAt()));
    }

    // ------------------------------------------------------ таблицы из БД

    public static void printTable(TableDump table) {
        System.out.println();
        System.out.println("Таблица: " + table.tableName() + " (строк: " + table.rows().size() + ")");
        int cols = table.columns().size();
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) {
            widths[i] = table.columns().get(i).length();
            for (List<String> row : table.rows()) {
                widths[i] = Math.min(30, Math.max(widths[i], row.get(i).length()));
            }
        }
        printRow(table.columns(), widths);
        System.out.println("-".repeat(Arrays.stream(widths).sum() + 3 * cols));
        for (List<String> row : table.rows()) {
            printRow(row, widths);
        }
    }

    private static void printRow(List<String> values, int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            sb.append(String.format("%-" + widths[i] + "s", cut(values.get(i), widths[i]))).append(" | ");
        }
        System.out.println(sb);
    }

    // -------------------------------------------------------------- общее

    /** Карточка: заголовок и пары «название поля — значение». */
    private static void printCard(String title, String... labelsAndValues) {
        System.out.println("----------------------------------------");
        System.out.println(title);
        for (int i = 0; i < labelsAndValues.length; i += 2) {
            System.out.printf("%-14s %s%n", labelsAndValues[i] + ":", labelsAndValues[i + 1]);
        }
        System.out.println("----------------------------------------");
    }

    /** Обрезает длинную строку, чтобы таблица не «ехала». */
    static String cut(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }
}
