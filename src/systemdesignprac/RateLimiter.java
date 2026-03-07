package systemdesignprac;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SYSTEM DESIGN PRACTICE: Rate Limiter
 *
 * PROBLEM:
 *   Design a rate limiter that restricts the number of requests a user can make
 *   within a given time window.
 *
 * REQUIREMENTS:
 *   - Limit each user to N requests per time window (e.g., 5 requests/10 seconds)
 *   - Return true if the request is allowed, false if it should be rejected
 *   - Support multiple users
 *   - Be thread-safe
 *
 * ALGORITHMS IMPLEMENTED:
 *   1. Fixed Window Counter  — simple, but allows burst at window boundary
 *   2. Sliding Window Log    — accurate, but memory-heavy
 *   3. Token Bucket          — smooth burst handling, common in practice
 *
 * USAGE:
 *   Run main() to see all three algorithms in action.
 */
public class RateLimiter {

    // -------------------------------------------------------------------------
    // 1. Fixed Window Counter
    //    - Divide time into fixed windows (e.g., every 10 seconds)
    //    - Count requests per user per window
    //    - Reset count when window expires
    //
    //    Weakness: a user can send 2x requests at the boundary of two windows
    // -------------------------------------------------------------------------
    static class FixedWindowRateLimiter {
        private final int maxRequests;
        private final long windowSizeMs;
        // userId -> [count, windowStartTime]
        private final Map<String, long[]> counters = new ConcurrentHashMap<>();

        FixedWindowRateLimiter(int maxRequests, long windowSizeMs) {
            this.maxRequests = maxRequests;
            this.windowSizeMs = windowSizeMs;
        }

        synchronized boolean allowRequest(String userId) {
            long now = System.currentTimeMillis();
            counters.putIfAbsent(userId, new long[]{0, now});
            long[] state = counters.get(userId);
            long count = state[0];
            long windowStart = state[1];

            if (now - windowStart >= windowSizeMs) {
                // New window — reset
                state[0] = 1;
                state[1] = now;
                return true;
            }

            if (count < maxRequests) {
                state[0]++;
                return true;
            }
            return false; // rate limit exceeded
        }
    }

    // -------------------------------------------------------------------------
    // 2. Sliding Window Log
    //    - Keep a timestamped log of each request per user
    //    - On each request, remove timestamps older than the window
    //    - Allow if log size < maxRequests
    //
    //    Trade-off: accurate but uses O(maxRequests) memory per user
    // -------------------------------------------------------------------------
    static class SlidingWindowLogRateLimiter {
        private final int maxRequests;
        private final long windowSizeMs;
        private final Map<String, Deque<Long>> logs = new ConcurrentHashMap<>();

        SlidingWindowLogRateLimiter(int maxRequests, long windowSizeMs) {
            this.maxRequests = maxRequests;
            this.windowSizeMs = windowSizeMs;
        }

        synchronized boolean allowRequest(String userId) {
            long now = System.currentTimeMillis();
            logs.putIfAbsent(userId, new ArrayDeque<>());
            Deque<Long> log = logs.get(userId);

            // Remove timestamps outside the current window
            while (!log.isEmpty() && now - log.peekFirst() >= windowSizeMs) {
                log.pollFirst();
            }

            if (log.size() < maxRequests) {
                log.addLast(now);
                return true;
            }
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // 3. Token Bucket
    //    - Each user has a bucket with capacity N tokens
    //    - Tokens refill at a fixed rate (e.g., 1 token/second)
    //    - Each request consumes 1 token; rejected if bucket is empty
    //
    //    Trade-off: allows short bursts up to bucket capacity, smooth overall
    // -------------------------------------------------------------------------
    static class TokenBucketRateLimiter {
        private final int capacity;
        private final double refillRatePerMs; // tokens per millisecond
        // userId -> [tokens, lastRefillTime]
        private final Map<String, double[]> buckets = new ConcurrentHashMap<>();

        TokenBucketRateLimiter(int capacity, double refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerMs = refillRatePerSecond / 1000.0;
        }

        synchronized boolean allowRequest(String userId) {
            long now = System.currentTimeMillis();
            buckets.putIfAbsent(userId, new double[]{capacity, now});
            double[] bucket = buckets.get(userId);

            double tokens = bucket[0];
            long lastRefill = (long) bucket[1];

            // Refill tokens based on elapsed time
            double elapsed = now - lastRefill;
            tokens = Math.min(capacity, tokens + elapsed * refillRatePerMs);
            bucket[1] = now;

            if (tokens >= 1) {
                bucket[0] = tokens - 1;
                return true;
            }
            bucket[0] = tokens;
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Main — demo all three algorithms
    // -------------------------------------------------------------------------
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Rate Limiter Demo ===\n");

        // Config: 3 requests per 2 seconds
        int maxRequests = 3;
        long windowMs = 2000;

        FixedWindowRateLimiter fixedWindow = new FixedWindowRateLimiter(maxRequests, windowMs);
        SlidingWindowLogRateLimiter slidingLog = new SlidingWindowLogRateLimiter(maxRequests, windowMs);
        TokenBucketRateLimiter tokenBucket = new TokenBucketRateLimiter(maxRequests, maxRequests / 2.0);

        String user = "user1";

        System.out.println("-- Fixed Window Counter (3 req / 2s) --");
        for (int i = 1; i <= 5; i++) {
            System.out.printf("  Request %d: %s%n", i, fixedWindow.allowRequest(user) ? "ALLOWED" : "DENIED");
        }
        System.out.println("  [sleeping 2s to reset window...]");
        Thread.sleep(2000);
        System.out.printf("  Request 6 (after reset): %s%n%n", fixedWindow.allowRequest(user) ? "ALLOWED" : "DENIED");

        System.out.println("-- Sliding Window Log (3 req / 2s) --");
        for (int i = 1; i <= 5; i++) {
            System.out.printf("  Request %d: %s%n", i, slidingLog.allowRequest(user) ? "ALLOWED" : "DENIED");
        }
        System.out.println("  [sleeping 2s to slide window...]");
        Thread.sleep(2000);
        System.out.printf("  Request 6 (after slide): %s%n%n", slidingLog.allowRequest(user) ? "ALLOWED" : "DENIED");

        System.out.println("-- Token Bucket (capacity=3, refill=1.5 tokens/s) --");
        for (int i = 1; i <= 5; i++) {
            System.out.printf("  Request %d: %s%n", i, tokenBucket.allowRequest(user) ? "ALLOWED" : "DENIED");
        }
        System.out.println("  [sleeping 1s to refill tokens...]");
        Thread.sleep(1000);
        System.out.printf("  Request 6 (after refill): %s%n", tokenBucket.allowRequest(user) ? "ALLOWED" : "DENIED");

        System.out.println("\n=== Done ===");
    }
}