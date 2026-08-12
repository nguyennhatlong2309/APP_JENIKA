package com.cafe.jenika.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private StringWriter responseWriter;

    @BeforeEach
    public void setUp() throws IOException {
        filter = new RateLimitingFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    public void testBypassOptionsRequests() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("OPTIONS");

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    public void testLocalhostBypass() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Send 100 requests (exceeding BUCKET_CAPACITY of 60)
        for (int i = 0; i < 100; i++) {
            filter.doFilter(request, response, chain);
        }

        // All should pass, never set status 429
        verify(chain, times(100)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    public void testUnderLimitRequests() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("192.168.1.50");

        // Send 60 requests
        for (int i = 0; i < 60; i++) {
            filter.doFilter(request, response, chain);
        }

        // All should pass
        verify(chain, times(60)).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }

    @Test
    public void testExceedLimitRequests() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");

        // Send 61 requests
        for (int i = 0; i < 61; i++) {
            filter.doFilter(request, response, chain);
        }

        // First 60 should pass, 61st should fail
        verify(chain, times(60)).doFilter(request, response);
        verify(response, times(1)).setStatus(429);
        assertTrue(responseWriter.toString().contains("Rate limit exceeded"));
    }

    @Test
    public void testXForwardedForHeader() throws ServletException, IOException {
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.195, 70.41.3.18");

        // Send 61 requests
        for (int i = 0; i < 61; i++) {
            filter.doFilter(request, response, chain);
        }

        // Verify it tracked by the X-Forwarded-For IP (203.0.113.195)
        assertTrue(filter.getBuckets().containsKey("203.0.113.195"));
        verify(response, times(1)).setStatus(429);
    }

    @Test
    public void testCleanupExpiredBuckets() {
        // Create an entry manually that has expired
        TokenBucket mockExpiredBucket = mock(TokenBucket.class);
        when(mockExpiredBucket.isExpired()).thenReturn(true);

        TokenBucket mockActiveBucket = mock(TokenBucket.class);
        when(mockActiveBucket.isExpired()).thenReturn(false);

        filter.getBuckets().put("expired-ip", mockExpiredBucket);
        filter.getBuckets().put("active-ip", mockActiveBucket);

        assertEquals(2, filter.getBuckets().size());

        // Run cleanup
        filter.cleanUpExpiredBuckets();

        // Expired should be removed, active should remain
        assertEquals(1, filter.getBuckets().size());
        assertTrue(filter.getBuckets().containsKey("active-ip"));
        assertFalse(filter.getBuckets().containsKey("expired-ip"));
    }
}
