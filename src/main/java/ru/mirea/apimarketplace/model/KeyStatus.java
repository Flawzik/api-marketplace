package ru.mirea.apimarketplace.model;

import java.util.EnumSet;
import java.util.Set;

/** Статус API-ключа. */
public enum KeyStatus {
    ACTIVE("Действует"),
    REVOKED("Отозван"),
    EXPIRED("Истёк");

    private final String title;

    KeyStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Set<KeyStatus> allowedNext() {
        return this == ACTIVE ? EnumSet.of(REVOKED, EXPIRED) : EnumSet.noneOf(KeyStatus.class);
    }

    public boolean canChangeTo(KeyStatus next) {
        return allowedNext().contains(next);
    }

    @Override
    public String toString() {
        return title;
    }
}
