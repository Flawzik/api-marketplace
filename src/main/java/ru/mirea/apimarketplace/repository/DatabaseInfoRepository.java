package ru.mirea.apimarketplace.repository;

import ru.mirea.apimarketplace.model.TableDump;

import java.util.List;

public interface DatabaseInfoRepository {

    /** Содержимое всех таблиц приложения. */
    List<TableDump> dumpAllTables();
}
