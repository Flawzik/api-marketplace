package ru.mirea.apimarketplace.ui;

import ru.mirea.apimarketplace.service.RequestService;

import java.util.List;

public class SearchMenu extends Menu {

    private final RequestService service;

    public SearchMenu(ConsoleInput in, RequestService service) {
        super(in);
        this.service = service;
    }

    @Override
    protected String title() {
        return "ПОИСК ЗАЯВОК";
    }

    @Override
    protected List<String> items() {
        return List.of("По названию", "По описанию", "По имени клиента");
    }

    @Override
    protected void handle(int choice) {
        String text = in.readLine("Введите текст для поиска: ");
        switch (choice) {
            case 1 -> ConsolePrinter.printRequests(service.searchByTitle(text));
            case 2 -> ConsolePrinter.printRequests(service.searchByDescription(text));
            case 3 -> ConsolePrinter.printRequests(service.searchByClientName(text));
            default -> System.out.println("Неизвестный пункт меню.");
        }
    }
}
