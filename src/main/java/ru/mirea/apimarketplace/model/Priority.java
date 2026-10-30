package ru.mirea.apimarketplace.model;

/** Приоритет заявки. Порядок констант = порядок важности (для сортировки). */
public enum Priority {
    LOW("Низкий"),
    MEDIUM("Средний"),
    HIGH("Высокий");

    private final String title;

    Priority(String title) {
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
