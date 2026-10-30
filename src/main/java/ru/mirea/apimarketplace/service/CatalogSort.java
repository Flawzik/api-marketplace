package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.model.ApiCatalogItem;

import java.util.Comparator;

/** Способы сортировки каталога API. */
public enum CatalogSort {
    BY_NAME("По названию", Comparator.comparing(ApiCatalogItem::getName, String.CASE_INSENSITIVE_ORDER)),
    BY_PRICE("По цене", Comparator.comparingInt(ApiCatalogItem::getMonthlyPrice)),
    BY_RATE_LIMIT("По лимиту запросов в минуту", Comparator.comparingInt(ApiCatalogItem::getRateLimitPerMinute)),
    BY_DATE("По дате добавления", Comparator.comparing(ApiCatalogItem::getCreatedAt));

    private final String title;
    private final Comparator<ApiCatalogItem> comparator;

    CatalogSort(String title, Comparator<ApiCatalogItem> comparator) {
        this.title = title;
        this.comparator = comparator;
    }

    public Comparator<ApiCatalogItem> comparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return title;
    }
}
