package ru.mirea.apimarketplace.exception;

public class KeyNotFoundException extends EntityNotFoundException {

    public KeyNotFoundException(long id) {
        super("API-ключ с ID " + id + " не найден.");
    }
}
