package ru.mirea.apimarketplace.model;

import java.util.List;

/** Объект, который можно выгрузить в таблицу (Excel / CSV). */
public interface Exportable {

    /** Значения ячеек одной строки таблицы. */
    List<Object> toRow();
}
