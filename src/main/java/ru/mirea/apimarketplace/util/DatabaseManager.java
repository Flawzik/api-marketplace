package ru.mirea.apimarketplace.util;

import ru.mirea.apimarketplace.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Подключение к PostgreSQL через JDBC.
 * Настройки берутся из db.properties; переменные окружения DB_URL, DB_USER, DB_PASSWORD
 * имеют приоритет.
 */
public class DatabaseManager {

    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager() {
        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать db.properties: " + e.getMessage(), e);
        }
        this.url = firstNonNull(System.getenv("DB_URL"), props.getProperty("db.url"),
                "jdbc:postgresql://localhost:5432/api_marketplace");
        this.user = firstNonNull(System.getenv("DB_USER"), props.getProperty("db.user"), "postgres");
        this.password = firstNonNull(System.getenv("DB_PASSWORD"), props.getProperty("db.password"), "postgres");
    }

    /** Открывает новое соединение. Закрывать нужно через try-with-resources. */
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DatabaseException("Не удалось подключиться к базе данных (" + url + "): "
                    + e.getMessage(), e);
        }
    }

    /** Создаёт таблицы, если их ещё нет (скрипт schema.sql). */
    public void initSchema() {
        String script;
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new DatabaseException("Не найден файл schema.sql");
            }
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать schema.sql: " + e.getMessage(), e);
        }

        // убираем строки-комментарии и делим скрипт на отдельные команды по ';'
        StringBuilder clean = new StringBuilder();
        for (String line : script.split("\n")) {
            if (!line.trim().startsWith("--")) {
                clean.append(line).append('\n');
            }
        }
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            for (String sql : clean.toString().split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при создании таблиц: " + e.getMessage(), e);
        }
    }

    private static String firstNonNull(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
