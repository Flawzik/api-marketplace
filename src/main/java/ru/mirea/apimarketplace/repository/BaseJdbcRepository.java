package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.exception.DatabaseException;
import ru.mirea.apimarketplace.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Общий код для JDBC-репозиториев: открыть соединение, подставить параметры,
 * выполнить запрос и превратить SQLException в наше DatabaseException.
 */
abstract class BaseJdbcRepository {

    protected final DatabaseManager db;

    protected BaseJdbcRepository(DatabaseManager db) {
        this.db = db;
    }

    /** Превращает одну строку ResultSet в объект. */
    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    /** SELECT: возвращает список объектов. */
    protected <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection connection = db.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка выполнения SQL-запроса: " + e.getMessage(), e);
        }
    }

    /** UPDATE / DELETE: возвращает число изменённых строк. */
    protected int update(String sql, Object... params) {
        try (Connection connection = db.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка выполнения SQL-запроса: " + e.getMessage(), e);
        }
    }

    /** INSERT: возвращает ID, который выдала база данных. */
    protected long insert(String sql, Object... params) {
        try (Connection connection = db.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"})) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new DatabaseException("База данных не вернула ID новой записи.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка выполнения SQL-запроса: " + e.getMessage(), e);
        }
    }

    private static void bind(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object value = params[i];
            if (value instanceof LocalDateTime dateTime) {
                ps.setTimestamp(i + 1, Timestamp.valueOf(dateTime));
            } else if (value instanceof Enum<?> enumValue) {
                ps.setString(i + 1, enumValue.name());   // enum хранится в БД как текст
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }
}
