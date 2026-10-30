package ru.mirea.apimarketplace.model;

import java.util.List;

/** Содержимое одной таблицы БД для вывода в консоль. */
public record TableDump(String tableName, List<String> columns, List<List<String>> rows) {
}
