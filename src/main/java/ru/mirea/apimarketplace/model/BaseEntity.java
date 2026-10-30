package ru.mirea.apimarketplace.model;

/** Базовый класс всех сущностей: есть идентификатор и «имя для вывода». */
public abstract class BaseEntity {

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Короткое название объекта для вывода в консоль. */
    public abstract String getDisplayName();
}
