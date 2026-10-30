package ru.mirea.apimarketplace.exception;

/** Запрещённый переход между статусами (заявка, API каталога, ключ). */
public class InvalidStatusTransitionException extends BusinessException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
