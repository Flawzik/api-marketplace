package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.ApiKey;
import ru.mirea.apimarketplace.model.KeyStatus;

import java.util.List;

public interface KeyRepository extends CrudRepository<ApiKey> {

    /** Фильтрация на стороне базы данных (WHERE status = ?). */
    List<ApiKey> findByStatus(KeyStatus status);

    long countByRequest(long requestId);

    long countActiveByRequest(long requestId);
}
