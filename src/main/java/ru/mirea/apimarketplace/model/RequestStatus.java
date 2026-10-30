package ru.mirea.apimarketplace.model;

import java.util.EnumSet;
import java.util.Set;

/** Статус заявки на подключение API. */
public enum RequestStatus {
    NEW("Новая"),
    IN_REVIEW("На рассмотрении"),
    APPROVED("Одобрена"),
    CONNECTED("API подключён"),
    REJECTED("Отклонена"),
    CANCELLED("Отменена");

    private final String title;

    RequestStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    /** Статусы, в которые разрешён переход из текущего. */
    public Set<RequestStatus> allowedNext() {
        return switch (this) {
            case NEW -> EnumSet.of(IN_REVIEW, CANCELLED);
            case IN_REVIEW -> EnumSet.of(APPROVED, REJECTED, CANCELLED);
            case APPROVED -> EnumSet.of(CONNECTED, CANCELLED);
            case CONNECTED, REJECTED, CANCELLED -> EnumSet.noneOf(RequestStatus.class);
        };
    }

    public boolean canChangeTo(RequestStatus next) {
        return allowedNext().contains(next);
    }

    /** Активная заявка — ещё не рассмотрена до конца. */
    public boolean isActive() {
        return this == NEW || this == IN_REVIEW;
    }

    @Override
    public String toString() {
        return title;
    }
}
