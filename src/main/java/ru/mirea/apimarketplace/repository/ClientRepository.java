package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.ApiClient;

import java.util.Optional;

public interface ClientRepository extends CrudRepository<ApiClient> {

    Optional<ApiClient> findByEmail(String email);
}
