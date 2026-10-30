package ru.mirea.apimarketplace.model;

/** Тип API маркетплейса, к которому клиент хочет получить доступ. */
public enum ApiType {
    CATALOG("Каталог товаров"),
    ORDERS("Заказы"),
    PAYMENTS("Платежи"),
    DELIVERY("Доставка"),
    ANALYTICS("Аналитика"),
    REVIEWS("Отзывы");

    private final String title;

    ApiType(String title) {
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
