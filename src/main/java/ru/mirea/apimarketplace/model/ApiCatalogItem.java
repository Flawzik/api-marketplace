package ru.mirea.apimarketplace.model;

import ru.mirea.apimarketplace.util.Formats;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/** API, который маркетплейс предлагает к подключению (запись каталога). */
public class ApiCatalogItem extends BaseEntity implements Exportable {

    public static final List<String> EXPORT_HEADERS = List.of(
            "ID", "Название", "Версия", "Тип API", "Статус", "Лимит запросов в минуту",
            "Цена, руб./мес.", "Описание", "Создан");

    private String name;
    private String description;
    private String version;
    private ApiType apiType;
    private ApiStatus status;
    private int rateLimitPerMinute;
    private int monthlyPrice;
    private final LocalDateTime createdAt;

    /** Новый API: статус DRAFT (черновик). */
    public ApiCatalogItem(String name, String description, String version, ApiType apiType,
                          int rateLimitPerMinute, int monthlyPrice) {
        this(null, name, description, version, apiType, ApiStatus.DRAFT,
                rateLimitPerMinute, monthlyPrice, LocalDateTime.now());
    }

    /** Конструктор для записи, прочитанной из базы данных. */
    public ApiCatalogItem(Long id, String name, String description, String version, ApiType apiType,
                          ApiStatus status, int rateLimitPerMinute, int monthlyPrice, LocalDateTime createdAt) {
        setId(id);
        this.name = name;
        this.description = description;
        this.version = version;
        this.apiType = apiType;
        this.status = status;
        this.rateLimitPerMinute = rateLimitPerMinute;
        this.monthlyPrice = monthlyPrice;
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ApiType getApiType() {
        return apiType;
    }

    public void setApiType(ApiType apiType) {
        this.apiType = apiType;
    }

    public ApiStatus getStatus() {
        return status;
    }

    public void setStatus(ApiStatus status) {
        this.status = status;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public int getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(int monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getDisplayName() {
        return name + " v" + version;
    }

    @Override
    public List<Object> toRow() {
        return Arrays.asList(getId(), name, version, apiType.getTitle(), status.getTitle(),
                rateLimitPerMinute, monthlyPrice, description, Formats.dateTime(createdAt));
    }
}
