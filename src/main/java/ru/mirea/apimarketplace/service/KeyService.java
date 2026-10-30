package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.exception.BusinessException;
import ru.mirea.apimarketplace.exception.InvalidStatusTransitionException;
import ru.mirea.apimarketplace.exception.KeyNotFoundException;
import ru.mirea.apimarketplace.exception.RequestNotFoundException;
import ru.mirea.apimarketplace.model.ApiConnectionRequest;
import ru.mirea.apimarketplace.model.ApiKey;
import ru.mirea.apimarketplace.model.KeyStatus;
import ru.mirea.apimarketplace.model.RequestStatus;
import ru.mirea.apimarketplace.repository.KeyRepository;
import ru.mirea.apimarketplace.repository.RequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Бизнес-логика API-ключей.
 * Правила:
 *  Л-1 ключ выдаётся только по существующей заявке в статусе APPROVED или CONNECTED;
 *  Л-2 срок действия — от 1 до 365 дней;
 *  Л-3 у одной заявки не больше 3 действующих ключей;
 *  Л-4 изменить срок можно только у действующего ключа, новый срок должен оканчиваться в будущем;
 *  Л-5 переходы статусов: только ACTIVE → REVOKED или EXPIRED;
 *  Л-6 удалить можно только отозванный или истёкший ключ.
 */
public class KeyService implements CrudService<ApiKey> {

    public static final int MAX_ACTIVE_PER_REQUEST = 3;
    public static final int MAX_VALIDITY_DAYS = 365;

    private final KeyRepository keys;
    private final RequestRepository requests;

    public KeyService(KeyRepository keys, RequestRepository requests) {
        this.keys = keys;
        this.requests = requests;
    }

    // ---------------------------------------------------------------- CRUD

    public ApiKey create(long requestId, int validityDays) {
        ApiConnectionRequest request = requests.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
        if (request.getStatus() != RequestStatus.APPROVED && request.getStatus() != RequestStatus.CONNECTED) {  // Л-1
            throw new BusinessException("Ключ можно выдать только по заявке в статусе «" + RequestStatus.APPROVED
                    + "» или «" + RequestStatus.CONNECTED + "». Текущий статус: «" + request.getStatus() + "».");
        }
        checkValidity(validityDays);                                                                           // Л-2
        if (keys.countActiveByRequest(requestId) >= MAX_ACTIVE_PER_REQUEST) {                                  // Л-3
            throw new BusinessException("По этой заявке уже выдано " + MAX_ACTIVE_PER_REQUEST
                    + " действующих ключа. Отзовите один из них.");
        }
        ApiKey saved = keys.save(new ApiKey(requestId, request.getClientId(), validityDays));
        return getById(saved.getId());   // перечитываем, чтобы подтянуть имя клиента
    }

    @Override
    public List<ApiKey> getAll() {
        return keys.findAll();
    }

    @Override
    public ApiKey getById(long id) {
        return keys.findById(id).orElseThrow(() -> new KeyNotFoundException(id));
    }

    /** Меняет срок действия: новый срок считается от даты выдачи ключа. */
    public ApiKey updateValidity(long id, int validityDays) {
        ApiKey key = getById(id);
        if (key.getStatus() != KeyStatus.ACTIVE) {                                                             // Л-4
            throw new BusinessException("Менять срок можно только у действующего ключа.");
        }
        checkValidity(validityDays);
        LocalDateTime newExpiry = key.getIssuedAt().plusDays(validityDays);
        if (!newExpiry.isAfter(LocalDateTime.now())) {
            throw new BusinessException("С таким сроком ключ уже считался бы истёкшим. Укажите больше дней.");
        }
        key.setExpiresAt(newExpiry);
        keys.update(key);
        return key;
    }

    public ApiKey changeStatus(long id, KeyStatus newStatus) {
        ApiKey key = getById(id);
        KeyStatus current = key.getStatus();
        if (!current.canChangeTo(newStatus)) {                                                                 // Л-5
            String allowed = current.allowedNext().isEmpty()
                    ? "из этого статуса переходы запрещены"
                    : "допустимые переходы: " + current.allowedNext();
            throw new InvalidStatusTransitionException("Нельзя перевести ключ из «" + current + "» в «"
                    + newStatus + "» (" + allowed + ").");
        }
        key.setStatus(newStatus);
        keys.update(key);
        return key;
    }

    @Override
    public void delete(long id) {
        ApiKey key = getById(id);
        if (key.getStatus() == KeyStatus.ACTIVE) {                                                             // Л-6
            throw new BusinessException("Действующий ключ удалить нельзя: сначала отзовите его.");
        }
        keys.deleteById(id);
    }

    // --------------------------------------------------------- фильтрация

    /** Фильтр по статусу выполняется в базе данных (SQL WHERE). */
    public List<ApiKey> filterByStatus(KeyStatus status) {
        return keys.findByStatus(status);
    }

    public List<ApiKey> filterByClientId(long clientId) {
        return keys.findAll().stream()
                .filter(k -> k.getClientId() == clientId)
                .toList();
    }

    /** Действующие ключи, срок которых заканчивается не позже указанной даты. */
    public List<ApiKey> filterExpiringBefore(LocalDate date) {
        return keys.findAll().stream()
                .filter(k -> k.getStatus() == KeyStatus.ACTIVE && !k.getExpiresAt().toLocalDate().isAfter(date))
                .toList();
    }

    // --------------------------------------------------------- сортировка

    public List<ApiKey> sorted(KeySort sort, boolean descending) {
        Comparator<ApiKey> comparator = descending ? sort.comparator().reversed() : sort.comparator();
        return keys.findAll().stream().sorted(comparator).toList();
    }

    private static void checkValidity(int days) {
        if (days < 1 || days > MAX_VALIDITY_DAYS) {
            throw new BusinessException("Срок действия должен быть от 1 до " + MAX_VALIDITY_DAYS + " дней.");
        }
    }
}
