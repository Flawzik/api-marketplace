package ru.mirea.apimarketplace.ui;

/** Поток ввода закончился (например, Ctrl+D) — программу нужно корректно завершить. */
public class InputClosedException extends RuntimeException {

    public InputClosedException() {
        super("Ввод завершён.");
    }
}
