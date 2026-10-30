package ru.mirea.apimarketplace.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Запись данных в Excel (.xlsx) без внешних библиотек.
 * Файл .xlsx — это обычный ZIP-архив с XML-файлами, поэтому достаточно java.util.zip.
 * (Если на курсе используется Apache POI, этот класс можно заменить на реализацию через POI —
 * метод export(...) останется тем же.)
 */
public final class ExcelExporter {

    /** Один лист книги: название, заголовки столбцов и строки. */
    public record Sheet(String name, List<String> headers, List<List<Object>> rows) {
    }

    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    private static final String NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String REL = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

    private ExcelExporter() {
    }

    public static void export(Path file, List<Sheet> sheets) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(file), StandardCharsets.UTF_8)) {
            put(zip, "[Content_Types].xml", contentTypes(sheets.size()));
            put(zip, "_rels/.rels", XML
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"" + REL + "/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>");
            put(zip, "xl/workbook.xml", workbook(sheets));
            put(zip, "xl/_rels/workbook.xml.rels", workbookRels(sheets.size()));
            put(zip, "xl/styles.xml", styles());
            for (int i = 0; i < sheets.size(); i++) {
                put(zip, "xl/worksheets/sheet" + (i + 1) + ".xml", sheetXml(sheets.get(i)));
            }
        }
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String contentTypes(int sheetCount) {
        StringBuilder sb = new StringBuilder(XML);
        sb.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
        sb.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        sb.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>");
        sb.append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        sb.append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        for (int i = 1; i <= sheetCount; i++) {
            sb.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
                    .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        }
        return sb.append("</Types>").toString();
    }

    private static String workbook(List<Sheet> sheets) {
        StringBuilder sb = new StringBuilder(XML);
        sb.append("<workbook xmlns=\"").append(NS).append("\" xmlns:r=\"").append(REL).append("\"><sheets>");
        for (int i = 0; i < sheets.size(); i++) {
            sb.append("<sheet name=\"").append(escape(sheetName(sheets.get(i).name())))
                    .append("\" sheetId=\"").append(i + 1).append("\" r:id=\"rId").append(i + 1).append("\"/>");
        }
        return sb.append("</sheets></workbook>").toString();
    }

    private static String workbookRels(int sheetCount) {
        StringBuilder sb = new StringBuilder(XML);
        sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 1; i <= sheetCount; i++) {
            sb.append("<Relationship Id=\"rId").append(i).append("\" Type=\"").append(REL)
                    .append("/worksheet\" Target=\"worksheets/sheet").append(i).append(".xml\"/>");
        }
        sb.append("<Relationship Id=\"rId").append(sheetCount + 1).append("\" Type=\"").append(REL)
                .append("/styles\" Target=\"styles.xml\"/>");
        return sb.append("</Relationships>").toString();
    }

    /** Два стиля ячеек: 0 — обычный, 1 — жирный (для заголовков). */
    private static String styles() {
        return XML + "<styleSheet xmlns=\"" + NS + "\">"
                + "<fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts>"
                + "<fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill></fills>"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"2\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/></cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }

    private static String sheetXml(Sheet sheet) {
        int columns = sheet.headers().size();
        StringBuilder sb = new StringBuilder(XML);
        sb.append("<worksheet xmlns=\"").append(NS).append("\">");

        // ширина столбцов по самой длинной строке
        sb.append("<cols>");
        for (int c = 0; c < columns; c++) {
            int max = sheet.headers().get(c).length();
            for (List<Object> row : sheet.rows()) {
                Object value = c < row.size() ? row.get(c) : null;
                if (value != null) {
                    max = Math.max(max, value.toString().length());
                }
            }
            int width = Math.min(60, Math.max(10, max + 2));
            sb.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1)
                    .append("\" width=\"").append(width).append("\" customWidth=\"1\"/>");
        }
        sb.append("</cols><sheetData>");

        sb.append("<row r=\"1\">");
        for (int c = 0; c < columns; c++) {
            appendText(sb, column(c) + "1", sheet.headers().get(c), 1);
        }
        sb.append("</row>");

        int r = 2;
        for (List<Object> row : sheet.rows()) {
            sb.append("<row r=\"").append(r).append("\">");
            for (int c = 0; c < row.size(); c++) {
                Object value = row.get(c);
                String ref = column(c) + r;
                if (value == null) {
                    continue;
                } else if (value instanceof Number) {
                    sb.append("<c r=\"").append(ref).append("\"><v>").append(value).append("</v></c>");
                } else {
                    appendText(sb, ref, value.toString(), 0);
                }
            }
            sb.append("</row>");
            r++;
        }
        return sb.append("</sheetData></worksheet>").toString();
    }

    private static void appendText(StringBuilder sb, String ref, String text, int style) {
        sb.append("<c r=\"").append(ref).append("\" t=\"inlineStr\" s=\"").append(style)
                .append("\"><is><t xml:space=\"preserve\">").append(escape(text)).append("</t></is></c>");
    }

    /** Номер столбца (0, 1, 2...) -> буква Excel (A, B, C... Z, AA...). */
    private static String column(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index + 1;
        while (n > 0) {
            int rem = (n - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    private static String sheetName(String name) {
        String clean = name.replaceAll("[\\[\\]:*?/\\\\]", "_");
        return clean.length() > 31 ? clean.substring(0, 31) : clean;
    }

    private static String escape(String text) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            switch (ch) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&apos;");
                default -> {
                    // управляющие символы (кроме \t \n \r) в XML запрещены
                    if (ch >= 0x20 || ch == '\t' || ch == '\n' || ch == '\r') {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }
}
