package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.model.ApiKey;

import java.util.Comparator;

/** Способы сортировки API-ключей. */
public enum KeySort {
    BY_ISSUED("По дате выдачи", Comparator.comparing(ApiKey::getIssuedAt)),
    BY_EXPIRES("По сроку действия", Comparator.comparing(ApiKey::getExpiresAt)),
    BY_STATUS("По статусу", Comparator.comparing(ApiKey::getStatus)),
    BY_CLIENT("По имени клиента", Comparator.comparing(ApiKey::getClientName, String.CASE_INSENSITIVE_ORDER));

    private final String title;
    private final Comparator<ApiKey> comparator;

    KeySort(String title, Comparator<ApiKey> comparator) {
        this.title = title;
        this.comparator = comparator;
    }

    public Comparator<ApiKey> comparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return title;
    }
}
