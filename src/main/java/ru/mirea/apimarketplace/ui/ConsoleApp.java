package ru.mirea.apimarketplace.ui;

import ru.mirea.apimarketplace.model.RequestStatus;
import ru.mirea.apimarketplace.model.Statistics;
import ru.mirea.apimarketplace.model.TableDump;
import ru.mirea.apimarketplace.service.CatalogService;
import ru.mirea.apimarketplace.service.ClientService;
import ru.mirea.apimarketplace.service.DatabaseInfoService;
import ru.mirea.apimarketplace.service.ExportService;
import ru.mirea.apimarketplace.service.KeyService;
import ru.mirea.apimarketplace.service.RequestService;
import ru.mirea.apimarketplace.service.StatisticsService;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Главное меню системы. Остальные меню вызываются из него. */
public class ConsoleApp extends Menu {

    private static final Path EXPORT_DIR = Path.of("exports");

    private final ClientMenu clientMenu;
    private final RequestMenu requestMenu;
    private final CatalogMenu catalogMenu;
    private final KeyMenu keyMenu;
    private final SearchMenu searchMenu;
    private final StatisticsService statisticsService;
    private final ExportService exportService;
    private final DatabaseInfoService databaseInfoService;

    public ConsoleApp(ConsoleInput in, ClientService clientService, RequestService requestService,
                      CatalogService catalogService, KeyService keyService,
                      StatisticsService statisticsService, ExportService exportService,
                      DatabaseInfoService databaseInfoService) {
        super(in);
        this.clientMenu = new ClientMenu(in, clientService, exportService);
        this.requestMenu = new RequestMenu(in, requestService, exportService);
        this.catalogMenu = new CatalogMenu(in, catalogService, exportService);
        this.keyMenu = new KeyMenu(in, keyService, exportService);
        this.searchMenu = new SearchMenu(in, requestService);
        this.statisticsService = statisticsService;
        this.exportService = exportService;
        this.databaseInfoService = databaseInfoService;
    }

    @Override
    protected String title() {
        return "МАРКЕТПЛЕЙС API — ЗАЯВКИ НА ПОДКЛЮЧЕНИЕ";
    }

    @Override
    protected List<String> items() {
        return List.of("Клиенты", "Заявки на подключение API", "Каталог API", "API-ключи",
                "Поиск заявок", "Статистика", "Экспорт всех данных", "Вывести таблицы базы данных");
    }

    @Override
    protected String exitLabel() {
        return "Выход";
    }

    @Override
    protected void handle(int choice) {
        switch (choice) {
            case 1 -> clientMenu.run();
            case 2 -> requestMenu.run();
            case 3 -> catalogMenu.run();
            case 4 -> keyMenu.run();
            case 5 -> searchMenu.run();
            case 6 -> printStatistics();
            case 7 -> exportMenu();
            case 8 -> printTables();
            default -> System.out.println("Неизвестный пункт меню.");
        }
    }

    private void printStatistics() {
        Statistics s = statisticsService.collect();
        System.out.println();
        System.out.println("---------- СТАТИСТИКА ----------");
        System.out.println("Всего клиентов:               " + s.totalClients());
        System.out.println("Всего заявок:                 " + s.totalRequests());
        for (RequestStatus status : RequestStatus.values()) {
            System.out.printf("  %-28s %d%n", status.getTitle() + ":", s.byStatus().get(status));
        }
        System.out.println("Заявок с высоким приоритетом: " + s.highPriorityCount());
        System.out.println("Суммарная нагрузка:           " + s.totalDailyRequests() + " запросов/сут.");
        System.out.println("Средняя нагрузка на заявку:   "
                + String.format(Locale.US, "%.1f", s.avgDailyRequests()) + " запросов/сут.");
        System.out.println("Самый востребованный API:     "
                + (s.mostPopularType() == null ? "нет данных" : s.mostPopularType()));
        System.out.println("API в каталоге:               " + s.totalCatalogItems()
                + " (активных: " + s.activeCatalogItems() + ")");
        System.out.println("Выдано API-ключей:            " + s.totalKeys()
                + " (действующих: " + s.activeKeys() + ")");
    }

    private void exportMenu() {
        int choice = in.readOption("Экспорт всех данных (клиенты, заявки, каталог, ключи)",
                List.of("Excel (.xlsx), один файл", "CSV, по файлу на таблицу"));
        if (choice == 1) {
            System.out.println("Файл сохранён: " + exportService.exportAllToExcel(EXPORT_DIR).toAbsolutePath());
        } else {
            exportService.exportAllToCsv(EXPORT_DIR)
                    .forEach(file -> System.out.println("Файл сохранён: " + file.toAbsolutePath()));
        }
    }

    private void printTables() {
        for (TableDump table : databaseInfoService.dumpTables()) {
            ConsolePrinter.printTable(table);
        }
    }
}
