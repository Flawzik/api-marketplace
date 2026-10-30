package ru.mirea.apimarketplace.ui;

import ru.mirea.apimarketplace.service.ExportService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Меню управления одной сущностью. Все пункты общие: создать, показать, найти по ID,
 * изменить, удалить, сортировка (прямая и обратная), фильтрация, экспорт.
 * Подклассы реализуют, что именно делает каждый пункт для своей сущности,
 * и могут добавить свои пункты (extraItems).
 */
public abstract class EntityMenu extends Menu {

    private static final Path EXPORT_DIR = Path.of("exports");
    private static final int BASE_ITEMS = 8;

    private final ExportService exportService;

    protected EntityMenu(ConsoleInput in, ExportService exportService) {
        super(in);
        this.exportService = exportService;
    }

    protected abstract void create();

    protected abstract void showAll();

    protected abstract void showById();

    protected abstract void edit();

    protected abstract void delete();

    protected abstract void sort();

    protected abstract void filter();

    /** Данные сущности для выгрузки в Excel / CSV. */
    protected abstract ExportService.TableData exportData();

    /** Дополнительные пункты (нумеруются после стандартных). */
    protected List<String> extraItems() {
        return List.of();
    }

    protected void handleExtra(int number) {
    }

    @Override
    protected List<String> items() {
        List<String> all = new ArrayList<>(List.of(
                "Добавить запись", "Показать все записи", "Найти запись по ID", "Изменить запись",
                "Удалить запись", "Сортировка (прямая и обратная)", "Фильтрация", "Экспорт в Excel / CSV"));
        all.addAll(extraItems());
        return all;
    }

    @Override
    protected void handle(int choice) {
        switch (choice) {
            case 1 -> create();
            case 2 -> showAll();
            case 3 -> showById();
            case 4 -> edit();
            case 5 -> delete();
            case 6 -> sort();
            case 7 -> filter();
            case 8 -> export();
            default -> handleExtra(choice - BASE_ITEMS);
        }
    }

    private void export() {
        ExportService.TableData data = exportData();
        int format = in.readOption("Формат файла", List.of("Excel (.xlsx)", "CSV"));
        Path file = format == 1
                ? exportService.exportToExcel(EXPORT_DIR, data)
                : exportService.exportToCsv(EXPORT_DIR, data);
        System.out.println("Файл сохранён: " + file.toAbsolutePath());
    }
}
