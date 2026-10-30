package ru.mirea.apimarketplace.exception;

/** Запись с такими уникальными данными уже существует (email клиента, название и версия API). */
public class DuplicateEntityException extends BusinessException {

    public DuplicateEntityException(String message) {
        super(message);
    }
}
