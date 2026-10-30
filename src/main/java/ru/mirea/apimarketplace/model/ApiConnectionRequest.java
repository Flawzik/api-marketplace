package ru.mirea.apimarketplace.model;

import ru.mirea.apimarketplace.util.Formats;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class ApiConnectionRequest extends BaseEntity implements Exportable {

    public static final List<String> EXPORT_HEADERS = List.of(
            "ID", "Клиент", "Название", "Описание", "Тип API", "Приоритет",
            "Статус", "Запросов в сутки", "Создана", "Обновлена");

    private final long clientId;
    private String clientName;
    private String title;
    private String description;
    private ApiType apiType;
    private Priority priority;
    private RequestStatus status;
    private int expectedDailyRequests;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApiConnectionRequest(long clientId, String title, String description,
                                ApiType apiType, Priority priority, int expectedDailyRequests) {
        this(null, clientId, null, title, description, apiType, priority,
                RequestStatus.NEW, expectedDailyRequests, LocalDateTime.now(), LocalDateTime.now());
    }

    /** Конструктор для заявки, прочитанной из базы данных. */
    public ApiConnectionRequest(Long id, long clientId, String clientName, String title, String description,
                                ApiType apiType, Priority priority, RequestStatus status,
                                int expectedDailyRequests, LocalDateTime createdAt, LocalDateTime updatedAt) {
        setId(id);
        this.clientId = clientId;
        this.clientName = clientName;
        this.title = title;
        this.description = description;
        this.apiType = apiType;
        this.priority = priority;
        this.status = status;
        this.expectedDailyRequests = expectedDailyRequests;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApiType getApiType() {
        return apiType;
    }

    public void setApiType(ApiType apiType) {
        this.apiType = apiType;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public int getExpectedDailyRequests() {
        return expectedDailyRequests;
    }

    public void setExpectedDailyRequests(int expectedDailyRequests) {
        this.expectedDailyRequests = expectedDailyRequests;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String getDisplayName() {
        return "Заявка #" + getId() + " «" + title + "»";
    }

    @Override
    public List<Object> toRow() {
        return Arrays.asList(getId(), clientName, title, description, apiType.getTitle(), priority.getTitle(),
                status.getTitle(), expectedDailyRequests,
                Formats.dateTime(createdAt), Formats.dateTime(updatedAt));
    }
}
