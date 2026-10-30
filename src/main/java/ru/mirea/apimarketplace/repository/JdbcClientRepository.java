package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.ApiClient;
import ru.mirea.apimarketplace.model.ClientType;
import ru.mirea.apimarketplace.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcClientRepository extends BaseJdbcRepository implements ClientRepository {

    public JdbcClientRepository(DatabaseManager db) {
        super(db);
    }

    private ApiClient map(ResultSet rs) throws SQLException {
        return new ApiClient(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("company_name"),
                ClientType.valueOf(rs.getString("client_type")),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    @Override
    public ApiClient save(ApiClient client) {
        long id = insert("INSERT INTO api_clients (full_name, email, company_name, client_type, created_at) "
                        + "VALUES (?, ?, ?, ?, ?)",
                client.getFullName(), client.getEmail(), client.getCompanyName(), client.getType(),
                client.getCreatedAt());
        client.setId(id);
        return client;
    }

    @Override
    public void update(ApiClient client) {
        update("UPDATE api_clients SET full_name = ?, email = ?, company_name = ?, client_type = ? WHERE id = ?",
                client.getFullName(), client.getEmail(), client.getCompanyName(), client.getType(), client.getId());
    }

    @Override
    public Optional<ApiClient> findById(long id) {
        return query("SELECT * FROM api_clients WHERE id = ?", this::map, id).stream().findFirst();
    }

    @Override
    public Optional<ApiClient> findByEmail(String email) {
        return query("SELECT * FROM api_clients WHERE email = ?", this::map, email).stream().findFirst();
    }

    @Override
    public List<ApiClient> findAll() {
        return query("SELECT * FROM api_clients ORDER BY id", this::map);
    }

    @Override
    public boolean deleteById(long id) {
        return update("DELETE FROM api_clients WHERE id = ?", id) > 0;
    }
}
