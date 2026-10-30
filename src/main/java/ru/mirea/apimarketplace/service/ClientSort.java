package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.model.ApiClient;

import java.util.Comparator;

/** Способы сортировки клиентов. */
public enum ClientSort {
    BY_NAME("По ФИО", Comparator.comparing(ApiClient::getFullName, String.CASE_INSENSITIVE_ORDER)),
    BY_COMPANY("По названию компании", Comparator.comparing(ApiClient::getCompanyName, String.CASE_INSENSITIVE_ORDER)),
    BY_DATE("По дате регистрации", Comparator.comparing(ApiClient::getCreatedAt)),
    BY_TYPE("По типу клиента", Comparator.comparing(ApiClient::getType));

    private final String title;
    private final Comparator<ApiClient> comparator;

    ClientSort(String title, Comparator<ApiClient> comparator) {
        this.title = title;
        this.comparator = comparator;
    }

    public Comparator<ApiClient> comparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return title;
    }
}
