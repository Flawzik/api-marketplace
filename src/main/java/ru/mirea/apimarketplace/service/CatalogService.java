package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.exception.BusinessException;
import ru.mirea.apimarketplace.exception.CatalogItemNotFoundException;
import ru.mirea.apimarketplace.exception.DuplicateEntityException;
import ru.mirea.apimarketplace.exception.InvalidStatusTransitionException;
import ru.mirea.apimarketplace.model.ApiCatalogItem;
import ru.mirea.apimarketplace.model.ApiStatus;
import ru.mirea.apimarketplace.model.ApiType;
import ru.mirea.apimarketplace.repository.CatalogRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Бизнес-логика каталога API.
 * Правила:
 *  К-1 название 3–100 символов, описание не короче 10;
 *  К-2 версия в формате 1, 1.0 или 1.2.3;
 *  К-3 лимит — от 1 до 100 000 запросов в минуту, цена — от 0 до 1 000 000 руб./мес.;
 *  К-4 пара «название + версия» уникальна;
 *  К-5 запрещены недопустимые переходы статусов (см. ApiStatus.allowedNext);
 *  К-6 отключённый API редактировать нельзя;
 *  К-7 удалить можно только API в статусе DRAFT или DISABLED.
 */
public class CatalogService implements CrudService<ApiCatalogItem> {

    public static final int MAX_RATE_LIMIT = 100_000;
    public static final int MAX_PRICE = 1_000_000;
    private static final Pattern VERSION = Pattern.compile("^[0-9]+(\\.[0-9]+){0,2}$");

    private final CatalogRepository catalog;

    public CatalogService(CatalogRepository catalog) {
        this.catalog = catalog;
    }

    // ---------------------------------------------------------------- CRUD

    public ApiCatalogItem create(String name, String description, String version, ApiType apiType,
                                 int rateLimitPerMinute, int monthlyPrice) {
        validate(name, description, version, apiType, rateLimitPerMinute, monthlyPrice);
        checkUnique(name.trim(), version.trim(), null);
        return catalog.save(new ApiCatalogItem(name.trim(), description.trim(), version.trim(),
                apiType, rateLimitPerMinute, monthlyPrice));
    }

    @Override
    public List<ApiCatalogItem> getAll() {
        return catalog.findAll();
    }

    @Override
    public ApiCatalogItem getById(long id) {
        return catalog.findById(id).orElseThrow(() -> new CatalogItemNotFoundException(id));
    }

    public ApiCatalogItem update(long id, String name, String description, String version, ApiType apiType,
                                 int rateLimitPerMinute, int monthlyPrice) {
        ApiCatalogItem item = getById(id);
        if (item.getStatus() == ApiStatus.DISABLED) {                                           // К-6
            throw new BusinessException("Отключённый API редактировать нельзя.");
        }
        validate(name, description, version, apiType, rateLimitPerMinute, monthlyPrice);
        checkUnique(name.trim(), version.trim(), id);

        item.setName(name.trim());
        item.setDescription(description.trim());
        item.setVersion(version.trim());
        item.setApiType(apiType);
        item.setRateLimitPerMinute(rateLimitPerMinute);
        item.setMonthlyPrice(monthlyPrice);
        catalog.update(item);
        return item;
    }

    public ApiCatalogItem changeStatus(long id, ApiStatus newStatus) {
        ApiCatalogItem item = getById(id);
        ApiStatus current = item.getStatus();
        if (!current.canChangeTo(newStatus)) {                                                  // К-5
            String allowed = current.allowedNext().isEmpty()
                    ? "из этого статуса переходы запрещены"
                    : "допустимые переходы: " + current.allowedNext();
            throw new InvalidStatusTransitionException("Нельзя перевести API из «" + current + "» в «"
                    + newStatus + "» (" + allowed + ").");
        }
        item.setStatus(newStatus);
        catalog.update(item);
        return item;
    }

    @Override
    public void delete(long id) {
        ApiCatalogItem item = getById(id);
        if (item.getStatus() != ApiStatus.DRAFT && item.getStatus() != ApiStatus.DISABLED) {    // К-7
            throw new BusinessException("Удалять можно только API в статусе «" + ApiStatus.DRAFT
                    + "» или «" + ApiStatus.DISABLED + "». Текущий статус: «" + item.getStatus() + "».");
        }
        catalog.deleteById(id);
    }

    // --------------------------------------------------------- фильтрация

    /** Фильтр по статусу выполняется в базе данных (SQL WHERE). */
    public List<ApiCatalogItem> filterByStatus(ApiStatus status) {
        return catalog.findByStatus(status);
    }

    public List<ApiCatalogItem> filterByApiType(ApiType apiType) {
        return catalog.findAll().stream()
                .filter(i -> i.getApiType() == apiType)
                .toList();
    }

    public List<ApiCatalogItem> filterByPriceRange(int minPrice, int maxPrice) {
        if (minPrice < 0 || minPrice > maxPrice) {
            throw new BusinessException("Диапазон цен задан неверно: минимум не может быть больше максимума или меньше 0.");
        }
        return catalog.findAll().stream()
                .filter(i -> i.getMonthlyPrice() >= minPrice && i.getMonthlyPrice() <= maxPrice)
                .toList();
    }

    // --------------------------------------------------------- сортировка

    public List<ApiCatalogItem> sorted(CatalogSort sort, boolean descending) {
        Comparator<ApiCatalogItem> comparator = descending ? sort.comparator().reversed() : sort.comparator();
        return catalog.findAll().stream().sorted(comparator).toList();
    }

    // ------------------------------------------------------------ helpers

    private void checkUnique(String name, String version, Long selfId) {
        Optional<ApiCatalogItem> same = catalog.findByNameAndVersion(name, version);
        if (same.isPresent() && !same.get().getId().equals(selfId)) {                           // К-4
            throw new DuplicateEntityException("API «" + name + "» версии " + version + " уже есть в каталоге.");
        }
    }

    private static void validate(String name, String description, String version, ApiType apiType,
                                 int rateLimitPerMinute, int monthlyPrice) {
        if (name == null || name.trim().length() < 3 || name.trim().length() > 100) {            // К-1
            throw new BusinessException("Название API должно содержать от 3 до 100 символов.");
        }
        if (description == null || description.trim().length() < 10) {
            throw new BusinessException("Описание должно содержать не менее 10 символов.");
        }
        if (version == null || !VERSION.matcher(version.trim()).matches()) {                     // К-2
            throw new BusinessException("Версия должна быть в формате 1, 1.0 или 1.2.3.");
        }
        if (apiType == null) {
            throw new BusinessException("Нужно указать тип API.");
        }
        if (rateLimitPerMinute < 1 || rateLimitPerMinute > MAX_RATE_LIMIT) {                     // К-3
            throw new BusinessException("Лимит должен быть от 1 до " + MAX_RATE_LIMIT + " запросов в минуту.");
        }
        if (monthlyPrice < 0 || monthlyPrice > MAX_PRICE) {
            throw new BusinessException("Цена должна быть от 0 до " + MAX_PRICE + " руб./мес.");
        }
    }
}
