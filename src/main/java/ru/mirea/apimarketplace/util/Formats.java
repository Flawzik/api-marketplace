package ru.mirea.apimarketplace.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Форматы дат, общие для всего приложения. */
public final class Formats {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Formats() {
    }

    public static String dateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }
}
