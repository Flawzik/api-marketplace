package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.ApiCatalogItem;
import ru.mirea.apimarketplace.model.ApiStatus;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository extends CrudRepository<ApiCatalogItem> {

    Optional<ApiCatalogItem> findByNameAndVersion(String name, String version);

    /** Фильтрация на стороне базы данных (WHERE status = ?). */
    List<ApiCatalogItem> findByStatus(ApiStatus status);
}
