package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.BaseEntity;

import java.util.List;
import java.util.Optional;

/** Общие операции хранилища для любой сущности. */
public interface CrudRepository<T extends BaseEntity> {

    /** Сохраняет новую запись и возвращает её с присвоенным ID. */
    T save(T entity);

    void update(T entity);

    Optional<T> findById(long id);

    List<T> findAll();

    /** @return true, если запись была удалена */
    boolean deleteById(long id);
}
