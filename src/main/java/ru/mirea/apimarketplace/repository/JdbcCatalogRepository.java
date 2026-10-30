package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.ApiCatalogItem;
import ru.mirea.apimarketplace.model.ApiStatus;
import ru.mirea.apimarketplace.model.ApiType;
import ru.mirea.apimarketplace.util.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class JdbcCatalogRepository extends BaseJdbcRepository implements CatalogRepository {

    public JdbcCatalogRepository(DatabaseManager db) {
        super(db);
    }

    private ApiCatalogItem map(ResultSet rs) throws SQLException {
        return new ApiCatalogItem(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("version"),
                ApiType.valueOf(rs.getString("api_type")),
                ApiStatus.valueOf(rs.getString("status")),
                rs.getInt("rate_limit_per_minute"),
                rs.getInt("monthly_price"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    @Override
    public ApiCatalogItem save(ApiCatalogItem item) {
        long id = insert("INSERT INTO api_catalog (name, description, version, api_type, status, "
                        + "rate_limit_per_minute, monthly_price, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                item.getName(), item.getDescription(), item.getVersion(), item.getApiType(), item.getStatus(),
                item.getRateLimitPerMinute(), item.getMonthlyPrice(), item.getCreatedAt());
        item.setId(id);
        return item;
    }

    @Override
    public void update(ApiCatalogItem item) {
        update("UPDATE api_catalog SET name = ?, description = ?, version = ?, api_type = ?, status = ?, "
                        + "rate_limit_per_minute = ?, monthly_price = ? WHERE id = ?",
                item.getName(), item.getDescription(), item.getVersion(), item.getApiType(), item.getStatus(),
                item.getRateLimitPerMinute(), item.getMonthlyPrice(), item.getId());
    }

    @Override
    public Optional<ApiCatalogItem> findById(long id) {
        return query("SELECT * FROM api_catalog WHERE id = ?", this::map, id).stream().findFirst();
    }

    @Override
    public Optional<ApiCatalogItem> findByNameAndVersion(String name, String version) {
        return query("SELECT * FROM api_catalog WHERE LOWER(name) = LOWER(?) AND version = ?",
                this::map, name, version).stream().findFirst();
    }

    @Override
    public List<ApiCatalogItem> findAll() {
        return query("SELECT * FROM api_catalog ORDER BY id", this::map);
    }

    @Override
    public List<ApiCatalogItem> findByStatus(ApiStatus status) {
        return query("SELECT * FROM api_catalog WHERE status = ? ORDER BY id", this::map, status);
    }

    @Override
    public boolean deleteById(long id) {
        return update("DELETE FROM api_catalog WHERE id = ?", id) > 0;
    }
}
