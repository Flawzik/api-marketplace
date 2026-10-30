package ru.mirea.apimarketplace.exception;

public class ClientNotFoundException extends EntityNotFoundException {

    public ClientNotFoundException(long id) {
        super("Клиент с ID " + id + " не найден.");
    }
}
