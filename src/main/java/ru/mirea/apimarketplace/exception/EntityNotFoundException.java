package ru.mirea.apimarketplace.exception;

/** Запись с указанным ID не найдена. */
public class EntityNotFoundException extends AppException {

    public EntityNotFoundException(String message) {
        super(message);
    }
}
