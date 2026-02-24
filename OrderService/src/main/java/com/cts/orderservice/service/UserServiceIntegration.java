package com.cts.orderservice.service;

import com.cts.orderservice.client.UserClient;
import com.cts.orderservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to manage User-Service integration.
 *
 * <p>This service acts as a bridge between OrderService and User-Service,
 * providing methods to fetch and validate user information with error handling
 * and optional caching.
 *
 * <p><b>Features:</b>
 * <ul>
 *   <li>User validation - Verify user exists before creating orders</li>
 *   <li>User data fetching - Get user details for order enrichment</li>
 *   <li>Smart caching - Cache user data to reduce service calls</li>
 *   <li>Error handling - Graceful degradation on service failures</li>
 * </ul>
 *
 * @author Order Service Team
 * @version 1.0
 * @since 2026-01-08
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceIntegration {

    private final UserClient userClient;

    // Simple in-memory cache (for demonstration - use Redis/Caffeine in production)
    private final Map<Long, UserDto> userCache = new HashMap<>();
    private static final long CACHE_TTL = 300000; // 5 minutes in milliseconds
    private final Map<Long, Long> cacheTimestamps = new HashMap<>();

    /**
     * Fetch user details from User-Service with caching.
     *
     * @param userId The ID of the user
     * @return UserDto or null if not found
     */
    public UserDto getUserById(Long userId) {
        return getUserById(userId, true);
    }

    /**
     * Fetch user details from User-Service.
     *
     * @param userId The ID of the user
     * @param useCache Whether to use cache or fetch fresh data
     * @return UserDto or null if not found
     */
    public UserDto getUserById(Long userId, boolean useCache) {
        try {
            // Check cache if enabled
            if (useCache && isCacheValid(userId)) {
                log.debug("Returning cached user for ID: {}", userId);
                return userCache.get(userId);
            }

            // Fetch from User-Service
            log.debug("Fetching user from User-Service for ID: {}", userId);
            UserDto user = userClient.getUserById(userId);

            if (user != null) {
                // Update cache
                userCache.put(userId, user);
                cacheTimestamps.put(userId, System.currentTimeMillis());
                log.info("User fetched successfully from User-Service: {}", userId);
            } else {
                log.warn("User not found in User-Service: {}", userId);
            }

            return user;
        } catch (Exception e) {
            log.error("Error fetching user from User-Service for ID: {}", userId, e);
            // Return cached data if available, even if expired
            return userCache.getOrDefault(userId, null);
        }
    }

    /**
     * Validate user existence and availability.
     *
     * @param userId The ID of the user
     * @throws IllegalArgumentException if user not found
     */
    public void validateUser(Long userId) {
        UserDto user = getUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        log.debug("User validated successfully: {}", userId);
    }

    /**
     * Clear cache for a specific user.
     *
     * @param userId The ID of the user
     */
    public void clearCache(Long userId) {
        userCache.remove(userId);
        cacheTimestamps.remove(userId);
        log.debug("Cache cleared for user ID: {}", userId);
    }

    /**
     * Clear all user cache.
     */
    public void clearAllCache() {
        userCache.clear();
        cacheTimestamps.clear();
        log.debug("All user cache cleared");
    }

    /**
     * Check if cached user is still valid.
     *
     * @param userId The ID of the user
     * @return true if cache is valid, false otherwise
     */
    private boolean isCacheValid(Long userId) {
        if (!userCache.containsKey(userId)) {
            return false;
        }

        Long timestamp = cacheTimestamps.get(userId);
        if (timestamp == null) {
            return false;
        }

        return (System.currentTimeMillis() - timestamp) < CACHE_TTL;
    }

    /**
     * Get cache statistics.
     *
     * @return Map with cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", userCache.size());
        stats.put("cacheTTL", CACHE_TTL);
        return stats;
    }
}

