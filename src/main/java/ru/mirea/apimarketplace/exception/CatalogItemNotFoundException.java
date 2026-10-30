package ru.mirea.apimarketplace.exception;

public class CatalogItemNotFoundException extends EntityNotFoundException {

    public CatalogItemNotFoundException(long id) {
        super("API с ID " + id + " не найден в каталоге.");
    }
}
