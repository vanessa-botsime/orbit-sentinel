package com.orbit.sentinel.alerts;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AlertNotifier {

    private static final Logger log = LoggerFactory.getLogger(AlertNotifier.class);

    private final String webhookUrl;
    private final String format;
    private final ObjectMapper objectMapper;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public AlertNotifier(@Value("${sentinel.alerts.webhook-url:}") String webhookUrl,
                         @Value("${sentinel.alerts.webhook-format:discord}") String format,
                         ObjectMapper objectMapper) {
        this.webhookUrl = webhookUrl;
        this.format = format;
        this.objectMapper = objectMapper;
    }

    /** Logs the alert and, if a webhook is configured, posts it. Returns true only if the webhook accepted it. */
    public boolean send(Alert alert) {
        String text = sanitize("[" + alert.getSeverity() + "] " + alert.getRuleName() + ": " + alert.getMessage());
        log.warn("ALERT {}", text);

        if (webhookUrl == null || webhookUrl.isBlank()) {
            return false;
        }
        try {
            String key = "slack".equalsIgnoreCase(format) ? "text" : "content";
            String body = objectMapper.writeValueAsString(Map.of(key, text));
            HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() / 100 == 2;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("Webhook delivery failed: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    /**
     * Actor names come from user input (for example the email typed into a failed login), so they are
     * untrusted: strip control characters (log injection) and break @mentions (@everyone pings).
     */
    static String sanitize(String text) {
        String cleaned = text.replaceAll("\\p{Cntrl}", " ").replace("@", "@\u200b");
        return cleaned.length() <= 300 ? cleaned : cleaned.substring(0, 300);
    }
}
