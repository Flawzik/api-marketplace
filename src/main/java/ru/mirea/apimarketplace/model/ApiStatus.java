package ru.mirea.apimarketplace.model;

import java.util.EnumSet;
import java.util.Set;

/** Статус API в каталоге маркетплейса. */
public enum ApiStatus {
    DRAFT("Черновик"),
    ACTIVE("Активен"),
    DEPRECATED("Устарел"),
    DISABLED("Отключён");

    private final String title;

    ApiStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Set<ApiStatus> allowedNext() {
        return switch (this) {
            case DRAFT -> EnumSet.of(ACTIVE, DISABLED);
            case ACTIVE -> EnumSet.of(DEPRECATED, DISABLED);
            case DEPRECATED -> EnumSet.of(DISABLED);
            case DISABLED -> EnumSet.noneOf(ApiStatus.class);
        };
    }

    public boolean canChangeTo(ApiStatus next) {
        return allowedNext().contains(next);
    }

    @Override
    public String toString() {
        return title;
    }
}
