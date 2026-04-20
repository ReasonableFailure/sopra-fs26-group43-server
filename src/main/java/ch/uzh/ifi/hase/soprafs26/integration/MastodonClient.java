package ch.uzh.ifi.hase.soprafs26.integration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class MastodonClient {

    public void postStatus(String baseUrl, String token, String content) {

        if (baseUrl == null || token == null) {
            return;
        }

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        client.post()
                .uri("/api/v1/statuses")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("status", content))
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}