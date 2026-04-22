package ch.uzh.ifi.hase.soprafs26.integration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class MastodonClient {

    public String postStatus(String baseUrl, String token, String content) {

        if (baseUrl == null || token == null) {
            return null;
        }

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        Map response = client.post()
                .uri("/api/v1/statuses")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of("status", content))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null) {
            return null;
        }

        Object id = response.get("id");
        return id != null ? id.toString() : null;
    }

    public Integer getLikes(String baseUrl, String token, String statusId) {

        if (baseUrl == null || token == null || statusId == null) {
            return 0;
        }

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        Map response = client.get()
                .uri("/api/v1/statuses/" + statusId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || response.get("favourites_count") == null) {
            return 0;
        }

        return ((Number) response.get("favourites_count")).intValue();
    }
}