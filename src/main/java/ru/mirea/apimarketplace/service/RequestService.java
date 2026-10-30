package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.exception.BusinessException;
import ru.mirea.apimarketplace.exception.ClientNotFoundException;
import ru.mirea.apimarketplace.exception.InvalidStatusTransitionException;
import ru.mirea.apimarketplace.exception.RequestNotFoundException;
import ru.mirea.apimarketplace.model.ApiConnectionRequest;
import ru.mirea.apimarketplace.model.ApiType;
import ru.mirea.apimarketplace.model.Priority;
import ru.mirea.apimarketplace.model.RequestStatus;
import ru.mirea.apimarketplace.repository.ClientRepository;
import ru.mirea.apimarketplace.repository.KeyRepository;
import ru.mirea.apimarketplace.repository.RequestRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Бизнес-логика заявок на подключение API.
 *
 * Бизнес-правила (БП):
 *  БП-1  название заявки обязательно, 3–100 символов;
 *  БП-2  описание — не короче 10 символов;
 *  БП-3  нельзя создать заявку для несуществующего клиента;
 *  БП-4  ожидаемая нагрузка — от 1 до 1 000 000 запросов в сутки;
 *  БП-5  запрещены недопустимые переходы между статусами (см. RequestStatus.allowedNext);
 *  БП-6  у одного клиента не может быть больше 3 активных заявок (NEW / IN_REVIEW);
 *  БП-7  редактировать можно только заявку в статусе NEW;
 *  БП-8  email клиента корректен и уникален (ClientService);
 *  БП-9  нельзя удалить клиента с заявками (ClientService);
 *  БП-10 удалить можно только заявку в статусе NEW, CANCELLED или REJECTED;
 *  БП-11 нельзя удалить заявку, по которой выданы API-ключи.
 */
public class RequestService implements CrudService<ApiConnectionRequest> {

    public static final int MAX_ACTIVE_PER_CLIENT = 3;
    public static final int MAX_DAILY_REQUESTS = 1_000_000;

    private final RequestRepository requests;
    private final ClientRepository clients;
    private final KeyRepository keys;

    public RequestService(RequestRepository requests, ClientRepository clients, KeyRepository keys) {
        this.requests = requests;
        this.clients = clients;
        this.keys = keys;
    }

    // ---------------------------------------------------------------- CRUD

    public ApiConnectionRequest create(long clientId, String title, String description,
                                       ApiType apiType, Priority priority, int expectedDailyRequests) {
        validateFields(title, description, apiType, priority, expectedDailyRequests);

        if (clients.findById(clientId).isEmpty()) {                                              // БП-3
            throw new ClientNotFoundException(clientId);
        }
        if (requests.countActiveByClient(clientId) >= MAX_ACTIVE_PER_CLIENT) {                  // БП-6
            throw new BusinessException("У клиента уже " + MAX_ACTIVE_PER_CLIENT
                    + " активные заявки (новые или на рассмотрении). Дождитесь их обработки.");
        }
        ApiConnectionRequest saved = requests.save(new ApiConnectionRequest(
                clientId, title.trim(), description.trim(), apiType, priority, expectedDailyRequests));
        return getById(saved.getId());   // перечитываем, чтобы подтянуть имя клиента
    }

    @Override
    public List<ApiConnectionRequest> getAll() {
        return requests.findAll();
    }

    @Override
    public ApiConnectionRequest getById(long id) {
        return requests.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
    }

    public ApiConnectionRequest update(long id, String title, String description,
                                       ApiType apiType, Priority priority, int expectedDailyRequests) {
        ApiConnectionRequest request = getById(id);
        if (request.getStatus() != RequestStatus.NEW) {                                          // БП-7
            throw new BusinessException("Редактировать можно только заявку в статусе «"
                    + RequestStatus.NEW + "». Текущий статус: «" + request.getStatus() + "».");
        }
        validateFields(title, description, apiType, priority, expectedDailyRequests);

        request.setTitle(title.trim());
        request.setDescription(description.trim());
        request.setApiType(apiType);
        request.setPriority(priority);
        request.setExpectedDailyRequests(expectedDailyRequests);
        request.setUpdatedAt(LocalDateTime.now());
        requests.update(request);
        return request;
    }

    public ApiConnectionRequest changeStatus(long id, RequestStatus newStatus) {
        ApiConnectionRequest request = getById(id);
        RequestStatus current = request.getStatus();
        if (!current.canChangeTo(newStatus)) {                                                   // БП-5
            String allowed = current.allowedNext().isEmpty()
                    ? "из этого статуса переходы запрещены"
                    : "допустимые переходы: " + current.allowedNext();
            throw new InvalidStatusTransitionException("Нельзя перевести заявку из «" + current + "» в «"
                    + newStatus + "» (" + allowed + ").");
        }
        request.setStatus(newStatus);
        request.setUpdatedAt(LocalDateTime.now());
        requests.update(request);
        return request;
    }

    @Override
    public void delete(long id) {
        ApiConnectionRequest request = getById(id);
        RequestStatus status = request.getStatus();
        boolean deletable = status == RequestStatus.NEW || status == RequestStatus.CANCELLED
                || status == RequestStatus.REJECTED;
        if (!deletable) {                                                                        // БП-10
            throw new BusinessException("Нельзя удалить заявку в статусе «" + status
                    + "». Удалять можно только новые, отменённые и отклонённые заявки.");
        }
        if (keys.countByRequest(id) > 0) {                                                       // БП-11
            throw new BusinessException("Нельзя удалить заявку, по которой выданы API-ключи.");
        }
        requests.deleteById(id);
    }

    // -------------------------------------------------------------- поиск

    public List<ApiConnectionRequest> searchByTitle(String text) {
        String query = normalizeQuery(text);
        return requests.findAll().stream()
                .filter(r -> r.getTitle().toLowerCase().contains(query))
                .toList();
    }

    public List<ApiConnectionRequest> searchByDescription(String text) {
        String query = normalizeQuery(text);
        return requests.findAll().stream()
                .filter(r -> r.getDescription().toLowerCase().contains(query))
                .toList();
    }

    public List<ApiConnectionRequest> searchByClientName(String text) {
        String query = normalizeQuery(text);
        return requests.findAll().stream()
                .filter(r -> r.getClientName() != null && r.getClientName().toLowerCase().contains(query))
                .toList();
    }

    // --------------------------------------------------------- фильтрация

    /** Фильтр по статусу выполняется в базе данных (SQL WHERE). */
    public List<ApiConnectionRequest> filterByStatus(RequestStatus status) {
        return requests.findByStatus(status);
    }

    public List<ApiConnectionRequest> filterByPriority(Priority priority) {
        return requests.findAll().stream()
                .filter(r -> r.getPriority() == priority)
                .toList();
    }

    public List<ApiConnectionRequest> filterByApiType(ApiType apiType) {
        return requests.findAll().stream()
                .filter(r -> r.getApiType() == apiType)
                .toList();
    }

    public List<ApiConnectionRequest> filterByDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("Начальная дата не может быть позже конечной.");
        }
        return requests.findAll().stream()
                .filter(r -> {
                    LocalDate created = r.getCreatedAt().toLocalDate();
                    return !created.isBefore(from) && !created.isAfter(to);
                })
                .toList();
    }

    // --------------------------------------------------------- сортировка

    public List<ApiConnectionRequest> sorted(RequestSort sort, boolean descending) {
        Comparator<ApiConnectionRequest> comparator =
                descending ? sort.comparator().reversed() : sort.comparator();
        return requests.findAll().stream()
                .sorted(comparator)
                .toList();
    }

    // ------------------------------------------------------------ helpers

    private static String normalizeQuery(String text) {
        if (text == null || text.isBlank()) {
            throw new BusinessException("Строка поиска не может быть пустой.");
        }
        return text.trim().toLowerCase();
    }

    private static void validateFields(String title, String description, ApiType apiType,
                                       Priority priority, int expectedDailyRequests) {
        if (title == null || title.isBlank()) {                                                  // БП-1
            throw new BusinessException("Название заявки обязательно.");
        }
        if (title.trim().length() < 3 || title.trim().length() > 100) {
            throw new BusinessException("Название заявки должно содержать от 3 до 100 символов.");
        }
        if (description == null || description.trim().length() < 10) {                          // БП-2
            throw new BusinessException("Описание должно содержать не менее 10 символов.");
        }
        if (apiType == null) {
            throw new BusinessException("Нужно указать тип API.");
        }
        if (priority == null) {
            throw new BusinessException("Нужно указать приоритет.");
        }
        if (expectedDailyRequests < 1 || expectedDailyRequests > MAX_DAILY_REQUESTS) {          // БП-4
            throw new BusinessException("Ожидаемая нагрузка должна быть от 1 до "
                    + MAX_DAILY_REQUESTS + " запросов в сутки.");
        }
    }
}
