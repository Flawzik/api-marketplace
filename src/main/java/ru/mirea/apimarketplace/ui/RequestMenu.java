package ru.mirea.apimarketplace.ui;

import ru.mirea.apimarketplace.model.ApiConnectionRequest;
import ru.mirea.apimarketplace.model.ApiType;
import ru.mirea.apimarketplace.model.Priority;
import ru.mirea.apimarketplace.model.RequestStatus;
import ru.mirea.apimarketplace.service.ExportService;
import ru.mirea.apimarketplace.service.RequestService;
import ru.mirea.apimarketplace.service.RequestSort;

import java.time.LocalDate;
import java.util.List;

public class RequestMenu extends EntityMenu {

    private static final String LOAD_ERROR = "количество запросов должно быть целым числом.";

    private final RequestService service;

    public RequestMenu(ConsoleInput in, RequestService service, ExportService exportService) {
        super(in, exportService);
        this.service = service;
    }

    @Override
    protected String title() {
        return "ЗАЯВКИ НА ПОДКЛЮЧЕНИЕ API";
    }

    @Override
    protected List<String> extraItems() {
        return List.of("Изменить статус заявки");
    }

    @Override
    protected void handleExtra(int number) {
        if (number == 1) {
            changeStatus();
        }
    }

    @Override
    protected void create() {
        long clientId = in.readId("ID клиента: ");
        String title = in.readRequired("Название заявки: ");
        String description = in.readRequired("Описание (что нужно подключить и зачем): ");
        ApiType type = in.readEnum("Тип API", ApiType.values());
        Priority priority = in.readEnum("Приоритет", Priority.values());
        int load = in.readInt("Ожидаемое число запросов в сутки: ", LOAD_ERROR);

        ApiConnectionRequest created = service.create(clientId, title, description, type, priority, load);
        System.out.println("Заявка создана, ID = " + created.getId());
    }

    @Override
    protected void showAll() {
        ConsolePrinter.printRequests(service.getAll());
    }

    @Override
    protected void showById() {
        ConsolePrinter.printRequestDetails(service.getById(in.readId("Введите ID заявки: ")));
    }

    @Override
    protected void edit() {
        ApiConnectionRequest r = service.getById(in.readId("Введите ID заявки: "));
        String title = in.readOrKeep("Название", r.getTitle());
        String description = in.readOrKeep("Описание", r.getDescription());
        ApiType type = in.readEnum("Тип API", ApiType.values(), r.getApiType());
        Priority priority = in.readEnum("Приоритет", Priority.values(), r.getPriority());
        int load = in.readIntOrKeep("Запросов в сутки", LOAD_ERROR, r.getExpectedDailyRequests());

        service.update(r.getId(), title, description, type, priority, load);
        System.out.println("Заявка обновлена.");
    }

    @Override
    protected void delete() {
        long id = in.readId("Введите ID заявки: ");
        ApiConnectionRequest r = service.getById(id);
        if (in.confirm("Удалить " + r.getDisplayName() + "?")) {
            service.delete(id);
            System.out.println("Заявка удалена.");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    private void changeStatus() {
        ApiConnectionRequest r = service.getById(in.readId("Введите ID заявки: "));
        System.out.println("Текущий статус: " + r.getStatus());
        System.out.println("Допустимые переходы: "
                + (r.getStatus().allowedNext().isEmpty() ? "нет" : r.getStatus().allowedNext()));
        RequestStatus next = in.readEnum("Новый статус", RequestStatus.values());
        service.changeStatus(r.getId(), next);
        System.out.println("Статус изменён на «" + next + "».");
    }

    @Override
    protected void sort() {
        RequestSort sort = in.readEnum("Сортировать", RequestSort.values());
        boolean descending = in.readDescending();
        ConsolePrinter.printRequests(service.sorted(sort, descending));
    }

    @Override
    protected void filter() {
        int choice = in.readOption("Фильтр", List.of("По статусу", "По приоритету", "По типу API",
                "По диапазону дат создания"));
        switch (choice) {
            case 1 -> ConsolePrinter.printRequests(
                    service.filterByStatus(in.readEnum("Статус", RequestStatus.values())));
            case 2 -> ConsolePrinter.printRequests(
                    service.filterByPriority(in.readEnum("Приоритет", Priority.values())));
            case 3 -> ConsolePrinter.printRequests(
                    service.filterByApiType(in.readEnum("Тип API", ApiType.values())));
            case 4 -> {
                LocalDate from = in.readDate("Дата с (ГГГГ-ММ-ДД): ");
                LocalDate to = in.readDate("Дата по (ГГГГ-ММ-ДД): ");
                ConsolePrinter.printRequests(service.filterByDateRange(from, to));
            }
            default -> System.out.println("Неизвестный фильтр.");
        }
    }

    @Override
    protected ExportService.TableData exportData() {
        return ExportService.tableOf("Заявки", ApiConnectionRequest.EXPORT_HEADERS, service.getAll());
    }
}
