package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.exception.BusinessException;
import ru.mirea.apimarketplace.exception.ClientNotFoundException;
import ru.mirea.apimarketplace.exception.DuplicateEntityException;
import ru.mirea.apimarketplace.model.ApiClient;
import ru.mirea.apimarketplace.model.ClientType;
import ru.mirea.apimarketplace.repository.ClientRepository;
import ru.mirea.apimarketplace.repository.RequestRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Бизнес-логика клиентов.
 * Правила: ФИО и компания обязательны (до 100 символов); email корректен и уникален;
 * тип клиента обязателен; нельзя удалить клиента, у которого есть заявки.
 */
public class ClientService implements CrudService<ApiClient> {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final ClientRepository clients;
    private final RequestRepository requests;

    public ClientService(ClientRepository clients, RequestRepository requests) {
        this.clients = clients;
        this.requests = requests;
    }

    // ---------------------------------------------------------------- CRUD

    public ApiClient create(String fullName, String email, String companyName, ClientType type) {
        validate(fullName, email, companyName, type);
        if (clients.findByEmail(email.trim().toLowerCase()).isPresent()) {
            throw new DuplicateEntityException("Клиент с таким email уже зарегистрирован.");
        }
        return clients.save(new ApiClient(fullName.trim(), email.trim().toLowerCase(),
                companyName.trim(), type));
    }

    @Override
    public List<ApiClient> getAll() {
        return clients.findAll();
    }

    @Override
    public ApiClient getById(long id) {
        return clients.findById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }

    public ApiClient update(long id, String fullName, String email, String companyName, ClientType type) {
        ApiClient client = getById(id);
        validate(fullName, email, companyName, type);
        Optional<ApiClient> sameEmail = clients.findByEmail(email.trim().toLowerCase());
        if (sameEmail.isPresent() && !sameEmail.get().getId().equals(id)) {
            throw new DuplicateEntityException("Этот email уже используется другим клиентом.");
        }
        client.setFullName(fullName.trim());
        client.setEmail(email.trim().toLowerCase());
        client.setCompanyName(companyName.trim());
        client.setType(type);
        clients.update(client);
        return client;
    }

    @Override
    public void delete(long id) {
        getById(id);
        if (requests.countByClient(id) > 0) {
            throw new BusinessException("Нельзя удалить клиента, у которого есть заявки.");
        }
        clients.deleteById(id);
    }

    // --------------------------------------------------------- фильтрация

    public List<ApiClient> filterByType(ClientType type) {
        return clients.findAll().stream()
                .filter(c -> c.getType() == type)
                .toList();
    }

    public List<ApiClient> filterByCompany(String text) {
        if (text == null || text.isBlank()) {
            throw new BusinessException("Название компании для фильтра не может быть пустым.");
        }
        String query = text.trim().toLowerCase();
        return clients.findAll().stream()
                .filter(c -> c.getCompanyName().toLowerCase().contains(query))
                .toList();
    }

    public List<ApiClient> filterByRegistrationDate(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException("Начальная дата не может быть позже конечной.");
        }
        return clients.findAll().stream()
                .filter(c -> {
                    LocalDate created = c.getCreatedAt().toLocalDate();
                    return !created.isBefore(from) && !created.isAfter(to);
                })
                .toList();
    }

    // --------------------------------------------------------- сортировка

    /** Прямая (descending = false) и обратная (descending = true) сортировка. */
    public List<ApiClient> sorted(ClientSort sort, boolean descending) {
        Comparator<ApiClient> comparator = descending ? sort.comparator().reversed() : sort.comparator();
        return clients.findAll().stream().sorted(comparator).toList();
    }

    // ------------------------------------------------------------ helpers

    private static void validate(String fullName, String email, String companyName, ClientType type) {
        if (fullName == null || fullName.isBlank() || fullName.trim().length() > 100) {
            throw new BusinessException("ФИО обязательно и не должно быть длиннее 100 символов.");
        }
        if (companyName == null || companyName.isBlank() || companyName.trim().length() > 100) {
            throw new BusinessException("Название компании обязательно и не должно быть длиннее 100 символов.");
        }
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new BusinessException("Некорректный email.");
        }
        if (type == null) {
            throw new BusinessException("Нужно указать тип клиента.");
        }
    }
}
