package com.meilong.messenger.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CheckService {
    private final HttpClient client;
    private final Duration requestTimeout;

    public CheckService(
            @Value("${monitor.check.connect-timeout-ms:3000}") long connectTimeoutMs,
            @Value("${monitor.check.request-timeout-ms:5000}") long requestTimeoutMs
    ) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .build();
        this.requestTimeout = Duration.ofMillis(requestTimeoutMs);
    }

    public static String validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL must not be blank");
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null
                    || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException("URL must be a full HTTP or HTTPS address");
            }
            return uri.toString();
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("URL is invalid", exception);
        }
    }

    public CheckResult checkUrl(String url) {
        URI uri = URI.create(validateUrl(url));
        long startedAt = System.nanoTime();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(requestTimeout)
                    .GET()
                    .build();
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            int statusCode = response.statusCode();
            return new CheckResult(statusCode, elapsedMs(startedAt), statusCode >= 200 && statusCode < 300, null);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new CheckResult(null, elapsedMs(startedAt), false, "Check was interrupted");
        } catch (java.io.IOException exception) {
            return new CheckResult(null, elapsedMs(startedAt), false, exception.getMessage());
        }
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    public record CheckResult(Integer statusCode, long responseTimeMs, boolean successful, String errorMessage) {
    }
}
