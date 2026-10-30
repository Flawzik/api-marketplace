package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.model.BaseEntity;

import java.util.List;

/** Операции, общие для сервисов всех сущностей. Создание и изменение у каждой сущности свои. */
public interface CrudService<T extends BaseEntity> {

    List<T> getAll();

    /** @throws ru.mirea.apimarketplace.exception.EntityNotFoundException если записи нет */
    T getById(long id);

    void delete(long id);
}
