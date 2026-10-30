package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.exception.AppException;
import ru.mirea.apimarketplace.model.ApiCatalogItem;
import ru.mirea.apimarketplace.model.ApiClient;
import ru.mirea.apimarketplace.model.ApiConnectionRequest;
import ru.mirea.apimarketplace.model.ApiKey;
import ru.mirea.apimarketplace.model.Exportable;
import ru.mirea.apimarketplace.repository.CatalogRepository;
import ru.mirea.apimarketplace.repository.ClientRepository;
import ru.mirea.apimarketplace.repository.KeyRepository;
import ru.mirea.apimarketplace.repository.RequestRepository;
import ru.mirea.apimarketplace.util.CsvExporter;
import ru.mirea.apimarketplace.util.ExcelExporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Экспорт данных из базы в Excel (.xlsx) и CSV: по одной сущности или все сразу. */
public class ExportService {

    /** Таблица для выгрузки: название, заголовки, строки. */
    public record TableData(String name, List<String> headers, List<List<Object>> rows) {
    }

    private final ClientRepository clients;
    private final RequestRepository requests;
    private final CatalogRepository catalog;
    private final KeyRepository keys;

    public ExportService(ClientRepository clients, RequestRepository requests,
                         CatalogRepository catalog, KeyRepository keys) {
        this.clients = clients;
        this.requests = requests;
        this.catalog = catalog;
        this.keys = keys;
    }

    /** Превращает список любых Exportable-объектов в таблицу. */
    public static <T extends Exportable> TableData tableOf(String name, List<String> headers, List<T> items) {
        List<List<Object>> rows = new ArrayList<>();
        for (T item : items) {
            rows.add(item.toRow());
        }
        return new TableData(name, headers, rows);
    }

    // ----------------------------------------------------- одна сущность

    public Path exportToExcel(Path directory, TableData table) {
        return exportToExcel(directory, List.of(table), safeName(table.name()));
    }

    public Path exportToCsv(Path directory, TableData table) {
        try {
            Files.createDirectories(directory);
            Path file = directory.resolve(safeName(table.name()) + "_" + timestamp() + ".csv");
            CsvExporter.write(file, table.headers(), table.rows());
            return file;
        } catch (IOException e) {
            throw new AppException("Не удалось записать CSV-файл: " + e.getMessage(), e);
        }
    }

    // --------------------------------------------------------- все данные

    /** Одна книга Excel: четыре листа (клиенты, заявки, каталог, ключи). */
    public Path exportAllToExcel(Path directory) {
        return exportToExcel(directory, allTables(), "api_marketplace");
    }

    /** Четыре CSV-файла. */
    public List<Path> exportAllToCsv(Path directory) {
        List<Path> files = new ArrayList<>();
        for (TableData table : allTables()) {
            files.add(exportToCsv(directory, table));
        }
        return files;
    }

    private List<TableData> allTables() {
        return List.of(
                tableOf("Клиенты", ApiClient.EXPORT_HEADERS, clients.findAll()),
                tableOf("Заявки", ApiConnectionRequest.EXPORT_HEADERS, requests.findAll()),
                tableOf("Каталог API", ApiCatalogItem.EXPORT_HEADERS, catalog.findAll()),
                tableOf("API-ключи", ApiKey.EXPORT_HEADERS, keys.findAll()));
    }

    private Path exportToExcel(Path directory, List<TableData> tables, String baseName) {
        try {
            Files.createDirectories(directory);
            Path file = directory.resolve(baseName + "_" + timestamp() + ".xlsx");
            List<ExcelExporter.Sheet> sheets = new ArrayList<>();
            for (TableData table : tables) {
                sheets.add(new ExcelExporter.Sheet(table.name(), table.headers(), table.rows()));
            }
            ExcelExporter.export(file, sheets);
            return file;
        } catch (IOException e) {
            throw new AppException("Не удалось записать Excel-файл: " + e.getMessage(), e);
        }
    }

    private static String safeName(String name) {
        // имя файла: только латиница/цифры, чтобы не зависеть от кодировки файловой системы
        return switch (name) {
            case "Клиенты" -> "clients";
            case "Заявки" -> "requests";
            case "Каталог API" -> "catalog";
            case "API-ключи" -> "keys";
            default -> "export";
        };
    }

    private static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
}
