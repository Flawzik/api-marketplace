package ru.mirea.apimarketplace.model;

/** Тип клиента маркетплейса. */
public enum ClientType {
    INDIVIDUAL("Физлицо / ИП"),
    COMPANY("Компания"),
    STARTUP("Стартап");

    private final String title;

    ClientType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
