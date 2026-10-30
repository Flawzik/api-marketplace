package ru.mirea.apimarketplace.model;

import ru.mirea.apimarketplace.util.Formats;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class ApiClient extends BaseEntity implements Exportable {

    public static final List<String> EXPORT_HEADERS =
            List.of("ID", "ФИО", "Email", "Компания", "Тип клиента", "Дата регистрации");

    private String fullName;
    private String email;
    private String companyName;
    private ClientType type;
    private final LocalDateTime createdAt;

    public ApiClient(String fullName, String email, String companyName, ClientType type) {
        this(null, fullName, email, companyName, type, LocalDateTime.now());
    }

    public ApiClient(Long id, String fullName, String email, String companyName,
                     ClientType type, LocalDateTime createdAt) {
        setId(id);
        this.fullName = fullName;
        this.email = email;
        this.companyName = companyName;
        this.type = type;
        this.createdAt = createdAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public ClientType getType() {
        return type;
    }

    public void setType(ClientType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String getDisplayName() {
        return fullName + " (" + companyName + ")";
    }

    @Override
    public List<Object> toRow() {
        return Arrays.asList(getId(), fullName, email, companyName, type.getTitle(), Formats.dateTime(createdAt));
    }
}
