package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.ApiConnectionRequest;
import ru.mirea.apimarketplace.model.RequestStatus;

import java.util.List;

public interface RequestRepository extends CrudRepository<ApiConnectionRequest> {

    /** Фильтрация на стороне базы данных (WHERE status = ?). */
    List<ApiConnectionRequest> findByStatus(RequestStatus status);

    /** Сколько у клиента заявок всего. */
    long countByClient(long clientId);

    /** Сколько у клиента активных заявок (NEW и IN_REVIEW). */
    long countActiveByClient(long clientId);
}
