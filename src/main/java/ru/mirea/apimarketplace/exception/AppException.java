package ru.mirea.apimarketplace.exception;

/** Базовое исключение приложения. Сообщение предназначено для показа пользователю. */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
