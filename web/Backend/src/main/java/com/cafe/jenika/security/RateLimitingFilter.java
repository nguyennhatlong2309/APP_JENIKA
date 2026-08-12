package com.cafe.jenika.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servlet filter for rate-limiting incoming HTTP requests.
 * Applies a limit of 60 requests per minute per IP address.
 */
@Component
public class RateLimitingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    // 60 requests per minute = capacity 60, refill rate 1.0 token per second
    private static final long BUCKET_CAPACITY = 60;
    private static final double REFILL_RATE_PER_SECOND = 1.0;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing RateLimitingFilter: Limit = 60 requests/minute");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Handle preflight OPTIONS requests without rate limiting to avoid CORS issues
            if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
                chain.doFilter(request, response);
                return;
            }

            String ip = getClientIP(httpRequest);

            // Bypass rate limiting for localhost in local development
            if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
                chain.doFilter(request, response);
                return;
            }

            TokenBucket bucket = buckets.computeIfAbsent(ip, k -> new TokenBucket(BUCKET_CAPACITY, REFILL_RATE_PER_SECOND));

            if (!bucket.tryConsume()) {
                logger.warn("Rate limit exceeded for IP: {} - Path: {}", ip, httpRequest.getRequestURI());
                httpResponse.setStatus(429); // HTTP 429 Too Many Requests
                httpResponse.setContentType("application/json");
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getWriter().write("{\"status\": 429, \"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Maximum 60 requests per minute.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Resolves the client's original IP address, checking X-Forwarded-For header first.
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        // X-Forwarded-For can contain multiple IPs separated by comma (client, proxy1, proxy2)
        // The first IP is the actual client IP
        return xfHeader.split(",")[0].trim();
    }

    /**
     * Periodically clean up expired/idle client IP records from the map to prevent memory leak.
     * Runs every 5 minutes (300,000 milliseconds).
     */
    @Scheduled(fixedDelay = 300000)
    public void cleanUpExpiredBuckets() {
        int initialSize = buckets.size();
        buckets.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int removedCount = initialSize - buckets.size();
        if (removedCount > 0) {
            logger.info("Cleaned up {} expired rate-limiting buckets from memory. Current active buckets: {}", removedCount, buckets.size());
        }
    }

    @Override
    public void destroy() {
        logger.info("Destroying RateLimitingFilter");
    }

    // For testing purposes
    public ConcurrentHashMap<String, TokenBucket> getBuckets() {
        return buckets;
    }
}
