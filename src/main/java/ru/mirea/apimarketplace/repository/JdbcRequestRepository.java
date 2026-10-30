package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.ApiConnectionRequest;
import ru.mirea.apimarketplace.model.ApiType;
import ru.mirea.apimarketplace.model.Priority;
import ru.mirea.apimarketplace.model.RequestStatus;
import ru.mirea.apimarketplace.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcRequestRepository extends BaseJdbcRepository implements RequestRepository {

    /** Заявка + имя клиента (JOIN), чтобы в консоли показывать клиента, а не только его ID. */
    private static final String SELECT_WITH_CLIENT =
            "SELECT r.id, r.client_id, c.full_name AS client_name, r.title, r.description, "
                    + "r.api_type, r.priority, r.status, r.expected_daily_requests, r.created_at, r.updated_at "
                    + "FROM api_connection_requests r JOIN api_clients c ON c.id = r.client_id ";

    public JdbcRequestRepository(DatabaseManager db) {
        super(db);
    }

    private ApiConnectionRequest map(ResultSet rs) throws SQLException {
        return new ApiConnectionRequest(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.getString("client_name"),
                rs.getString("title"),
                rs.getString("description"),
                ApiType.valueOf(rs.getString("api_type")),
                Priority.valueOf(rs.getString("priority")),
                RequestStatus.valueOf(rs.getString("status")),
                rs.getInt("expected_daily_requests"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime());
    }

    @Override
    public ApiConnectionRequest save(ApiConnectionRequest request) {
        long id = insert("INSERT INTO api_connection_requests (client_id, title, description, api_type, priority, "
                        + "status, expected_daily_requests, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                request.getClientId(), request.getTitle(), request.getDescription(), request.getApiType(),
                request.getPriority(), request.getStatus(), request.getExpectedDailyRequests(),
                request.getCreatedAt(), request.getUpdatedAt());
        request.setId(id);
        return request;
    }

    @Override
    public void update(ApiConnectionRequest request) {
        update("UPDATE api_connection_requests SET title = ?, description = ?, api_type = ?, priority = ?, "
                        + "status = ?, expected_daily_requests = ?, updated_at = ? WHERE id = ?",
                request.getTitle(), request.getDescription(), request.getApiType(), request.getPriority(),
                request.getStatus(), request.getExpectedDailyRequests(), request.getUpdatedAt(), request.getId());
    }

    @Override
    public Optional<ApiConnectionRequest> findById(long id) {
        return query(SELECT_WITH_CLIENT + "WHERE r.id = ?", this::map, id).stream().findFirst();
    }

    @Override
    public List<ApiConnectionRequest> findAll() {
        return query(SELECT_WITH_CLIENT + "ORDER BY r.id", this::map);
    }

    @Override
    public List<ApiConnectionRequest> findByStatus(RequestStatus status) {
        return query(SELECT_WITH_CLIENT + "WHERE r.status = ? ORDER BY r.id", this::map, status);
    }

    @Override
    public boolean deleteById(long id) {
        return update("DELETE FROM api_connection_requests WHERE id = ?", id) > 0;
    }

    @Override
    public long countByClient(long clientId) {
        return query("SELECT COUNT(*) FROM api_connection_requests WHERE client_id = ?",
                rs -> rs.getLong(1), clientId).get(0);
    }

    @Override
    public long countActiveByClient(long clientId) {
        return query("SELECT COUNT(*) FROM api_connection_requests "
                        + "WHERE client_id = ? AND status IN ('NEW', 'IN_REVIEW')",
                rs -> rs.getLong(1), clientId).get(0);
    }
}
