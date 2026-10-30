package ru.mirea.apimarketplace.ui;

import ru.mirea.apimarketplace.model.ApiKey;
import ru.mirea.apimarketplace.model.KeyStatus;
import ru.mirea.apimarketplace.service.ExportService;
import ru.mirea.apimarketplace.service.KeyService;
import ru.mirea.apimarketplace.service.KeySort;

import java.util.List;

public class KeyMenu extends EntityMenu {

    private static final String DAYS_ERROR = "срок должен быть целым числом дней.";

    private final KeyService service;

    public KeyMenu(ConsoleInput in, KeyService service, ExportService exportService) {
        super(in, exportService);
        this.service = service;
    }

    @Override
    protected String title() {
        return "API-КЛЮЧИ";
    }

    @Override
    protected List<String> extraItems() {
        return List.of("Изменить статус ключа (отозвать / пометить истёкшим)");
    }

    @Override
    protected void handleExtra(int number) {
        if (number == 1) {
            changeStatus();
        }
    }

    @Override
    protected void create() {
        long requestId = in.readId("ID одобренной заявки: ");
        int days = in.readInt("Срок действия ключа, дней: ", DAYS_ERROR);
        ApiKey key = service.create(requestId, days);
        System.out.println("Ключ выдан, ID = " + key.getId());
        System.out.println("Значение ключа: " + key.getKeyValue());
    }

    @Override
    protected void showAll() {
        ConsolePrinter.printKeys(service.getAll());
    }

    @Override
    protected void showById() {
        ConsolePrinter.printKeyDetails(service.getById(in.readId("Введите ID ключа: ")));
    }

    /** У ключа можно менять только срок действия (само значение неизменно). */
    @Override
    protected void edit() {
        ApiKey key = service.getById(in.readId("Введите ID ключа: "));
        int days = in.readInt("Новый срок действия (дней от даты выдачи): ", DAYS_ERROR);
        service.updateValidity(key.getId(), days);
        System.out.println("Срок действия ключа обновлён.");
    }

    @Override
    protected void delete() {
        long id = in.readId("Введите ID ключа: ");
        ApiKey key = service.getById(id);
        if (in.confirm("Удалить " + key.getDisplayName() + "?")) {
            service.delete(id);
            System.out.println("Ключ удалён.");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    private void changeStatus() {
        ApiKey key = service.getById(in.readId("Введите ID ключа: "));
        System.out.println("Текущий статус: " + key.getStatus());
        KeyStatus next = in.readEnum("Новый статус", KeyStatus.values());
        service.changeStatus(key.getId(), next);
        System.out.println("Статус изменён на «" + next + "».");
    }

    @Override
    protected void sort() {
        KeySort sort = in.readEnum("Сортировать", KeySort.values());
        boolean descending = in.readDescending();
        ConsolePrinter.printKeys(service.sorted(sort, descending));
    }

    @Override
    protected void filter() {
        int choice = in.readOption("Фильтр", List.of("По статусу", "По клиенту (ID)",
                "Действующие ключи, истекающие до даты"));
        switch (choice) {
            case 1 -> ConsolePrinter.printKeys(
                    service.filterByStatus(in.readEnum("Статус", KeyStatus.values())));
            case 2 -> ConsolePrinter.printKeys(service.filterByClientId(in.readId("ID клиента: ")));
            case 3 -> ConsolePrinter.printKeys(
                    service.filterExpiringBefore(in.readDate("Истекают до (ГГГГ-ММ-ДД): ")));
            default -> System.out.println("Неизвестный фильтр.");
        }
    }

    @Override
    protected ExportService.TableData exportData() {
        return ExportService.tableOf("API-ключи", ApiKey.EXPORT_HEADERS, service.getAll());
    }
}
