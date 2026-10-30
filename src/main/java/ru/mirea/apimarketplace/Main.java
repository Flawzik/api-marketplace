package ru.mirea.apimarketplace;

import ru.mirea.apimarketplace.exception.AppException;
import ru.mirea.apimarketplace.repository.CatalogRepository;
import ru.mirea.apimarketplace.repository.ClientRepository;
import ru.mirea.apimarketplace.repository.DatabaseInfoRepository;
import ru.mirea.apimarketplace.repository.JdbcCatalogRepository;
import ru.mirea.apimarketplace.repository.JdbcClientRepository;
import ru.mirea.apimarketplace.repository.JdbcDatabaseInfoRepository;
import ru.mirea.apimarketplace.repository.JdbcKeyRepository;
import ru.mirea.apimarketplace.repository.JdbcRequestRepository;
import ru.mirea.apimarketplace.repository.KeyRepository;
import ru.mirea.apimarketplace.repository.RequestRepository;
import ru.mirea.apimarketplace.service.CatalogService;
import ru.mirea.apimarketplace.service.ClientService;
import ru.mirea.apimarketplace.service.DatabaseInfoService;
import ru.mirea.apimarketplace.service.ExportService;
import ru.mirea.apimarketplace.service.KeyService;
import ru.mirea.apimarketplace.service.RequestService;
import ru.mirea.apimarketplace.service.StatisticsService;
import ru.mirea.apimarketplace.ui.ConsoleApp;
import ru.mirea.apimarketplace.ui.ConsoleInput;
import ru.mirea.apimarketplace.ui.InputClosedException;
import ru.mirea.apimarketplace.util.DatabaseManager;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Точка входа: только собирает слои вместе и запускает меню. */
public class Main {

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        ConsoleApp app;
        try {
            DatabaseManager db = new DatabaseManager();
            db.initSchema();   // проверяет подключение и создаёт таблицы, если их нет

            // Repository
            ClientRepository clients = new JdbcClientRepository(db);
            RequestRepository requests = new JdbcRequestRepository(db);
            CatalogRepository catalog = new JdbcCatalogRepository(db);
            KeyRepository keys = new JdbcKeyRepository(db);
            DatabaseInfoRepository info = new JdbcDatabaseInfoRepository(db);

            // Service
            ClientService clientService = new ClientService(clients, requests);
            RequestService requestService = new RequestService(requests, clients, keys);
            CatalogService catalogService = new CatalogService(catalog);
            KeyService keyService = new KeyService(keys, requests);
            StatisticsService statisticsService = new StatisticsService(clients, requests, catalog, keys);
            ExportService exportService = new ExportService(clients, requests, catalog, keys);
            DatabaseInfoService infoService = new DatabaseInfoService(info);

            // Console UI
            app = new ConsoleApp(new ConsoleInput(System.in), clientService, requestService,
                    catalogService, keyService, statisticsService, exportService, infoService);
        } catch (AppException e) {
            System.out.println("Ошибка запуска: " + e.getMessage());
            System.out.println("Проверьте, что PostgreSQL запущен, а параметры в db.properties верны.");
            return;
        }

        try {
            app.run();
        } catch (InputClosedException e) {
            System.out.println();
        }
        System.out.println("Работа программы завершена.");
    }
}
