package ru.mirea.apimarketplace.ui;

import ru.mirea.apimarketplace.model.ApiClient;
import ru.mirea.apimarketplace.model.ClientType;
import ru.mirea.apimarketplace.service.ClientService;
import ru.mirea.apimarketplace.service.ClientSort;
import ru.mirea.apimarketplace.service.ExportService;

import java.time.LocalDate;
import java.util.List;

public class ClientMenu extends EntityMenu {

    private final ClientService service;

    public ClientMenu(ConsoleInput in, ClientService service, ExportService exportService) {
        super(in, exportService);
        this.service = service;
    }

    @Override
    protected String title() {
        return "КЛИЕНТЫ МАРКЕТПЛЕЙСА";
    }

    @Override
    protected void create() {
        String fullName = in.readRequired("ФИО: ");
        String email = in.readRequired("Email: ");
        String company = in.readRequired("Компания: ");
        ClientType type = in.readEnum("Тип клиента", ClientType.values());
        ApiClient client = service.create(fullName, email, company, type);
        System.out.println("Клиент добавлен, ID = " + client.getId());
    }

    @Override
    protected void showAll() {
        ConsolePrinter.printClients(service.getAll());
    }

    @Override
    protected void showById() {
        ConsolePrinter.printClientDetails(service.getById(in.readId("Введите ID клиента: ")));
    }

    @Override
    protected void edit() {
        ApiClient c = service.getById(in.readId("Введите ID клиента: "));
        String fullName = in.readOrKeep("ФИО", c.getFullName());
        String email = in.readOrKeep("Email", c.getEmail());
        String company = in.readOrKeep("Компания", c.getCompanyName());
        ClientType type = in.readEnum("Тип клиента", ClientType.values(), c.getType());
        service.update(c.getId(), fullName, email, company, type);
        System.out.println("Клиент обновлён.");
    }

    @Override
    protected void delete() {
        long id = in.readId("Введите ID клиента: ");
        ApiClient c = service.getById(id);
        if (in.confirm("Удалить клиента «" + c.getDisplayName() + "»?")) {
            service.delete(id);
            System.out.println("Клиент удалён.");
        } else {
            System.out.println("Удаление отменено.");
        }
    }

    @Override
    protected void sort() {
        ClientSort sort = in.readEnum("Сортировать", ClientSort.values());
        boolean descending = in.readDescending();
        ConsolePrinter.printClients(service.sorted(sort, descending));
    }

    @Override
    protected void filter() {
        int choice = in.readOption("Фильтр", List.of("По типу клиента", "По названию компании",
                "По дате регистрации"));
        switch (choice) {
            case 1 -> ConsolePrinter.printClients(
                    service.filterByType(in.readEnum("Тип клиента", ClientType.values())));
            case 2 -> ConsolePrinter.printClients(
                    service.filterByCompany(in.readLine("Часть названия компании: ")));
            case 3 -> {
                LocalDate from = in.readDate("Дата с (ГГГГ-ММ-ДД): ");
                LocalDate to = in.readDate("Дата по (ГГГГ-ММ-ДД): ");
                ConsolePrinter.printClients(service.filterByRegistrationDate(from, to));
            }
            default -> System.out.println("Неизвестный фильтр.");
        }
    }

    @Override
    protected ExportService.TableData exportData() {
        return ExportService.tableOf("Клиенты", ApiClient.EXPORT_HEADERS, service.getAll());
    }
}
