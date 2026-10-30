package ru.mirea.apimarketplace.ui;

import ru.mirea.apimarketplace.model.ApiCatalogItem;
import ru.mirea.apimarketplace.model.ApiStatus;
import ru.mirea.apimarketplace.model.ApiType;
import ru.mirea.apimarketplace.service.CatalogService;
import ru.mirea.apimarketplace.service.CatalogSort;
import ru.mirea.apimarketplace.service.ExportService;

import java.util.List;

public class CatalogMenu extends EntityMenu {

    private static final String RATE_ERROR = "лимит должен быть целым числом.";
    private static final String PRICE_ERROR = "цена должна быть целым числом.";

    private final CatalogService service;

    public CatalogMenu(ConsoleInput in, CatalogService service, ExportService exportService) {
        super(in, exportService);
        this.service = service;
    }

    @Override
    protected String title() {
        return "КАТАЛОГ API";
    }

    @Override
    protected List<String> extraItems() {
        return List.of("Изменить статус API");
    }

    @Override
    protected void handleExtra(int number) {
        if (number == 1) {
            changeStatus();
        }
    }

    @Override
    protected void create() {
        String name = in.readRequired("Название API: ");
        String description = in.readRequired("Описание: ");
        String version = in.readRequired("Версия (например, 1.0): ");
        ApiType type = in.readEnum("Тип API", ApiType.values());
        int rate = in.readInt("Лимит запросов в минуту: ", RATE_ERROR);
        int price = in.readInt("Цена, руб./мес.: ", PRICE_ERROR);
        ApiCatalogItem item = service.create(name, description, version, type, rate, price);
        System.out.println("API добавлен в каталог, ID = " + item.getId());
    }

    @Override
    protected void showAll() {
        ConsolePrinter.printCatalog(service.getAll());
    }

    @Override
    protected void showById() {
        ConsolePrinter.printCatalogDetails(service.getById(in.readId("Введите ID API: ")));
    }

    @Override
    protected void edit() {
        ApiCatalogItem i = service.getById(in.readId("Введите ID API: "));
        String name = in.readOrKeep("Название", i.getName());
        String description = in.readOrKeep("Описание", i.getDescription());
        String version = in.readOrKeep("Версия", i.getVersion());
        ApiType type = in.readEnum("Тип API", ApiType.values(), i.getApiType());
        int rate = in.readIntOrKeep("Лимит запросов в минуту", RATE_ERROR, i.getRateLimitPerMinute());
        int price = in.readIntOrKeep("Цена, руб./мес.", PRICE_ERROR, i.getMonthlyPrice());
        service.update(i.getId(), name, description, version, type, rate, price);
        System.out.println("API обновлён.");
    }

    @Override
    protected void delete() {
        long id = in.readId("Введите ID API: ");
        ApiCatalogItem i = service.getById(id);
        if (in.confirm("Удалить API «" + i.getDisplayName() + "»?")) {
            service.delete(id);
            System.out.println("API удалён.");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    private void changeStatus() {
        ApiCatalogItem i = service.getById(in.readId("Введите ID API: "));
        System.out.println("Текущий статус: " + i.getStatus());
        System.out.println("Допустимые переходы: "
                + (i.getStatus().allowedNext().isEmpty() ? "нет" : i.getStatus().allowedNext()));
        ApiStatus next = in.readEnum("Новый статус", ApiStatus.values());
        service.changeStatus(i.getId(), next);
        System.out.println("Статус изменён на «" + next + "».");
    }

    @Override
    protected void sort() {
        CatalogSort sort = in.readEnum("Сортировать", CatalogSort.values());
        boolean descending = in.readDescending();
        ConsolePrinter.printCatalog(service.sorted(sort, descending));
    }

    @Override
    protected void filter() {
        int choice = in.readOption("Фильтр", List.of("По статусу", "По типу API", "По диапазону цен"));
        switch (choice) {
            case 1 -> ConsolePrinter.printCatalog(
                    service.filterByStatus(in.readEnum("Статус", ApiStatus.values())));
            case 2 -> ConsolePrinter.printCatalog(
                    service.filterByApiType(in.readEnum("Тип API", ApiType.values())));
            case 3 -> {
                int min = in.readInt("Цена от, руб./мес.: ", PRICE_ERROR);
                int max = in.readInt("Цена до, руб./мес.: ", PRICE_ERROR);
                ConsolePrinter.printCatalog(service.filterByPriceRange(min, max));
            }
            default -> System.out.println("Неизвестный фильтр.");
        }
    }

    @Override
    protected ExportService.TableData exportData() {
        return ExportService.tableOf("Каталог API", ApiCatalogItem.EXPORT_HEADERS, service.getAll());
    }
}
