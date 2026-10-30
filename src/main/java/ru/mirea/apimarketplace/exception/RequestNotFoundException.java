package ru.mirea.apimarketplace.exception;

public class RequestNotFoundException extends EntityNotFoundException {

    public RequestNotFoundException(long id) {
        super("Заявка с ID " + id + " не найдена.");
    }
}
