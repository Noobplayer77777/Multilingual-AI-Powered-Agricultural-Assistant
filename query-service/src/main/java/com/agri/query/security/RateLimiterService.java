package com.agri.query.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Deque<Long>> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 15;
    private static final long WINDOW_MILLIS = 60_000L;

    public boolean isAllowed(String clientIp) {
        if (clientIp == null || clientIp.isEmpty()) {
            return true;
        }

        long now = Instant.now().toEpochMilli();
        Deque<Long> timestamps = requestCounts.computeIfAbsent(clientIp, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            // Remove timestamps older than window
            while (!timestamps.isEmpty() && (now - timestamps.peekFirst()) > WINDOW_MILLIS) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                return false;
            }

            timestamps.addLast(now);
            return true;
        }
    }
}
