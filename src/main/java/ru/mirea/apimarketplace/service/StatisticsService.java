package ru.mirea.apimarketplace.service;

import ru.mirea.apimarketplace.model.ApiCatalogItem;
import ru.mirea.apimarketplace.model.ApiConnectionRequest;
import ru.mirea.apimarketplace.model.ApiKey;
import ru.mirea.apimarketplace.model.ApiStatus;
import ru.mirea.apimarketplace.model.ApiType;
import ru.mirea.apimarketplace.model.KeyStatus;
import ru.mirea.apimarketplace.model.Priority;
import ru.mirea.apimarketplace.model.RequestStatus;
import ru.mirea.apimarketplace.model.Statistics;
import ru.mirea.apimarketplace.repository.CatalogRepository;
import ru.mirea.apimarketplace.repository.ClientRepository;
import ru.mirea.apimarketplace.repository.KeyRepository;
import ru.mirea.apimarketplace.repository.RequestRepository;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsService {

    private final ClientRepository clients;
    private final RequestRepository requests;
    private final CatalogRepository catalog;
    private final KeyRepository keys;

    public StatisticsService(ClientRepository clients, RequestRepository requests,
                             CatalogRepository catalog, KeyRepository keys) {
        this.clients = clients;
        this.requests = requests;
        this.catalog = catalog;
        this.keys = keys;
    }

    public Statistics collect() {
        List<ApiConnectionRequest> all = requests.findAll();

        // сколько заявок в каждом статусе (включая нули)
        Map<RequestStatus, Long> counted = all.stream()
                .collect(Collectors.groupingBy(ApiConnectionRequest::getStatus,
                        () -> new EnumMap<>(RequestStatus.class), Collectors.counting()));
        Map<RequestStatus, Long> byStatus = new EnumMap<>(RequestStatus.class);
        for (RequestStatus status : RequestStatus.values()) {
            byStatus.put(status, counted.getOrDefault(status, 0L));
        }

        long highPriority = all.stream().filter(r -> r.getPriority() == Priority.HIGH).count();
        long totalDaily = all.stream().mapToLong(ApiConnectionRequest::getExpectedDailyRequests).sum();
        double avgDaily = all.stream().mapToInt(ApiConnectionRequest::getExpectedDailyRequests)
                .average().orElse(0);

        // самый востребованный тип API
        ApiType popular = all.stream()
                .collect(Collectors.groupingBy(ApiConnectionRequest::getApiType, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(Map.Entry::getKey)
                .orElse(null);

        List<ApiCatalogItem> apis = catalog.findAll();
        long activeApis = apis.stream().filter(a -> a.getStatus() == ApiStatus.ACTIVE).count();
        List<ApiKey> allKeys = keys.findAll();
        long activeKeys = allKeys.stream().filter(k -> k.getStatus() == KeyStatus.ACTIVE).count();

        return new Statistics(clients.findAll().size(), all.size(), byStatus, highPriority,
                totalDaily, avgDaily, popular, apis.size(), activeApis, allKeys.size(), activeKeys);
    }
}
