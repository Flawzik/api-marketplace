package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.model.ApiConnectionRequest;

import java.util.Comparator;

/** Способы сортировки заявок. У каждого — свой компаратор. */
public enum RequestSort {
    BY_DATE("По дате создания", Comparator.comparing(ApiConnectionRequest::getCreatedAt)),
    BY_PRIORITY("По приоритету", Comparator.comparing(ApiConnectionRequest::getPriority)),
    BY_LOAD("По ожидаемой нагрузке (запросов в сутки)",
            Comparator.comparingInt(ApiConnectionRequest::getExpectedDailyRequests)),
    BY_TITLE("По названию", Comparator.comparing(ApiConnectionRequest::getTitle, String.CASE_INSENSITIVE_ORDER));

    private final String title;
    private final Comparator<ApiConnectionRequest> comparator;

    RequestSort(String title, Comparator<ApiConnectionRequest> comparator) {
        this.title = title;
        this.comparator = comparator;
    }

    public Comparator<ApiConnectionRequest> comparator() {
        return comparator;
    }

    @Override
    public String toString() {
        return title;
    }
}
