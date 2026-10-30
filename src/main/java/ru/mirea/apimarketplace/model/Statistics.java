package ru.mirea.apimarketplace.model;

import java.util.Map;


public record Statistics(
        long totalClients,
        long totalRequests,
        Map<RequestStatus, Long> byStatus,
        long highPriorityCount,
        long totalDailyRequests,
        double avgDailyRequests,
        ApiType mostPopularType,
        long totalCatalogItems,
        long activeCatalogItems,
        long totalKeys,
        long activeKeys) {
}
