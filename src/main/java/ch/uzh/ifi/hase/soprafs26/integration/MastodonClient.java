package ch.uzh.ifi.hase.soprafs26.integration;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;


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

    public static String fetchMastodonProfileUrl(String baseUrl, String token) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/api/v1/accounts/verify_credentials",
                HttpMethod.GET,
                entity,
                Map.class
        );

        return (String) response.getBody().get("url");
    }
}