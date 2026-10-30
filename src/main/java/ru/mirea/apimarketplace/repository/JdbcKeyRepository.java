package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.ApiKey;
import ru.mirea.apimarketplace.model.KeyStatus;
import ru.mirea.apimarketplace.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcKeyRepository extends BaseJdbcRepository implements KeyRepository {

    /** Ключ + имя клиента (JOIN). */
    private static final String SELECT_WITH_CLIENT =
            "SELECT k.id, k.key_value, k.client_id, c.full_name AS client_name, k.request_id, "
                    + "k.status, k.issued_at, k.expires_at "
                    + "FROM api_keys k JOIN api_clients c ON c.id = k.client_id ";

    public JdbcKeyRepository(DatabaseManager db) {
        super(db);
    }

    private ApiKey map(ResultSet rs) throws SQLException {
        return new ApiKey(
                rs.getLong("id"),
                rs.getString("key_value"),
                rs.getLong("client_id"),
                rs.getString("client_name"),
                rs.getLong("request_id"),
                KeyStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("issued_at").toLocalDateTime(),
                rs.getTimestamp("expires_at").toLocalDateTime());
    }

    @Override
    public ApiKey save(ApiKey key) {
        long id = insert("INSERT INTO api_keys (key_value, client_id, request_id, status, issued_at, expires_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                key.getKeyValue(), key.getClientId(), key.getRequestId(), key.getStatus(),
                key.getIssuedAt(), key.getExpiresAt());
        key.setId(id);
        return key;
    }

    @Override
    public void update(ApiKey key) {
        update("UPDATE api_keys SET status = ?, expires_at = ? WHERE id = ?",
                key.getStatus(), key.getExpiresAt(), key.getId());
    }

    @Override
    public Optional<ApiKey> findById(long id) {
        return query(SELECT_WITH_CLIENT + "WHERE k.id = ?", this::map, id).stream().findFirst();
    }

    @Override
    public List<ApiKey> findAll() {
        return query(SELECT_WITH_CLIENT + "ORDER BY k.id", this::map);
    }

    @Override
    public List<ApiKey> findByStatus(KeyStatus status) {
        return query(SELECT_WITH_CLIENT + "WHERE k.status = ? ORDER BY k.id", this::map, status);
    }

    @Override
    public boolean deleteById(long id) {
        return update("DELETE FROM api_keys WHERE id = ?", id) > 0;
    }

    @Override
    public long countByRequest(long requestId) {
        return query("SELECT COUNT(*) FROM api_keys WHERE request_id = ?", rs -> rs.getLong(1), requestId).get(0);
    }

    @Override
    public long countActiveByRequest(long requestId) {
        return query("SELECT COUNT(*) FROM api_keys WHERE request_id = ? AND status = 'ACTIVE'",
                rs -> rs.getLong(1), requestId).get(0);
    }
}
