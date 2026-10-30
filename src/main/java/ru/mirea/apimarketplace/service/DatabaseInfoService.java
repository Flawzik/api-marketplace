package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.model.TableDump;
import ru.mirea.apimarketplace.repository.DatabaseInfoRepository;

import java.util.List;

public class DatabaseInfoService {

    private final DatabaseInfoRepository repository;

    public DatabaseInfoService(DatabaseInfoRepository repository) {
        this.repository = repository;
    }

    public List<TableDump> dumpTables() {
        return repository.dumpAllTables();
    }
}
