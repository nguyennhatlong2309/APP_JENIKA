package com.cafe.jenika.security;

import java.time.Instant;

/**
 * Thread-safe Token Bucket implementation for rate limiting.
 */
public class TokenBucket {
    private final long capacity;
    private final double refillRatePerSecond;
    private double tokens;
    private Instant lastRefillTime;
    private Instant lastAccessTime;

    public TokenBucket(long capacity, double refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.tokens = capacity;
        this.lastRefillTime = Instant.now();
        this.lastAccessTime = Instant.now();
    }

    /**
     * Tries to consume 1 token. Returns true if successful, false otherwise.
     */
    public synchronized boolean tryConsume() {
        refill();
        lastAccessTime = Instant.now();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    private void refill() {
        Instant now = Instant.now();
        double elapsedSeconds = (now.toEpochMilli() - lastRefillTime.toEpochMilli()) / 1000.0;
        if (elapsedSeconds > 0) {
            tokens = Math.min(capacity, tokens + elapsedSeconds * refillRatePerSecond);
            lastRefillTime = now;
        }
    }

    /**
     * Checks if this bucket has been idle for more than 5 minutes.
     */
    public synchronized boolean isExpired() {
        return Instant.now().toEpochMilli() - lastAccessTime.toEpochMilli() > 300_000; // 5 minutes in milliseconds
    }

    // For testing/debugging purposes
    public synchronized double getTokens() {
        refill();
        return tokens;
    }
}
