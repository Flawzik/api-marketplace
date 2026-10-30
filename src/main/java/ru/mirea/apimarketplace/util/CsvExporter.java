package ru.mirea.apimarketplace.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Запись таблицы в CSV (разделитель «;», кодировка UTF-8 с BOM — чтобы Excel не ломал кириллицу). */
public final class CsvExporter {

    private CsvExporter() {
    }

    public static void write(Path file, List<String> headers, List<List<Object>> rows) throws IOException {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(String.join(";", headers.stream().map(CsvExporter::escape).toList())).append("\r\n");
        for (List<Object> row : rows) {
            sb.append(String.join(";", row.stream().map(v -> escape(v == null ? "" : v.toString())).toList()))
                    .append("\r\n");
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        if (value.contains(";") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
