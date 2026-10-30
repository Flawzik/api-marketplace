package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.exception.DatabaseException;
import ru.mirea.apimarketplace.model.TableDump;
import ru.mirea.apimarketplace.util.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Читает содержимое таблиц целиком — для пункта меню «Вывести таблицы базы данных». */
public class JdbcDatabaseInfoRepository implements DatabaseInfoRepository {

    // имена таблиц заданы константами, пользовательский ввод в SQL не подставляется
    private static final List<String> TABLES = List.of("api_clients", "api_connection_requests", "api_catalog", "api_keys");

    private final DatabaseManager db;

    public JdbcDatabaseInfoRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public List<TableDump> dumpAllTables() {
        List<TableDump> result = new ArrayList<>();
        try (Connection connection = db.getConnection(); Statement statement = connection.createStatement()) {
            for (String table : TABLES) {
                try (ResultSet rs = statement.executeQuery("SELECT * FROM " + table + " ORDER BY id")) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int count = meta.getColumnCount();
                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= count; i++) {
                        columns.add(meta.getColumnName(i));
                    }
                    List<List<String>> rows = new ArrayList<>();
                    while (rs.next()) {
                        List<String> row = new ArrayList<>();
                        for (int i = 1; i <= count; i++) {
                            row.add(String.valueOf(rs.getObject(i)));
                        }
                        rows.add(row);
                    }
                    result.add(new TableDump(table, columns, rows));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка чтения таблиц: " + e.getMessage(), e);
        }
        return result;
    }
}
