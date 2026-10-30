package ru.mirea.apimarketplace.exception;

/** Нарушение бизнес-правила предметной области. */
public class BusinessException extends AppException {

    public BusinessException(String message) {
        super(message);
    }
}
