package ru.mirea.apimarketplace.model;

import ru.mirea.apimarketplace.util.Formats;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ApiKey extends BaseEntity implements Exportable {

    public static final List<String> EXPORT_HEADERS =
            List.of("ID", "Клиент", "ID заявки", "Ключ", "Статус", "Выдан", "Действует до");

    private final String keyValue;
    private final long clientId;
    private String clientName;      // заполняется при чтении из БД (JOIN с клиентами)
    private final long requestId;
    private KeyStatus status;
    private final LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    public ApiKey(long requestId, long clientId, int validityDays) {
        this(null, "mk_" + UUID.randomUUID().toString().replace("-", ""), clientId, null, requestId,
                KeyStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now().plusDays(validityDays));
    }

    public ApiKey(Long id, String keyValue, long clientId, String clientName, long requestId,
                  KeyStatus status, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        setId(id);
        this.keyValue = keyValue;
        this.clientId = clientId;
        this.clientName = clientName;
        this.requestId = requestId;
        this.status = status;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public long getRequestId() {
        return requestId;
    }

    public KeyStatus getStatus() {
        return status;
    }

    public void setStatus(KeyStatus status) {
        this.status = status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String getDisplayName() {
        return "Ключ #" + getId() + " (" + keyValue.substring(0, 8) + "…)";
    }

    @Override
    public List<Object> toRow() {
        return Arrays.asList(getId(), clientName, requestId, keyValue, status.getTitle(),
                Formats.dateTime(issuedAt), Formats.dateTime(expiresAt));
    }
}
