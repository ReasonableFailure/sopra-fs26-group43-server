package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.integration.MastodonClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MastodonClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private RestTemplate restTemplate;

    @Test
    public void postStatus_validInput_success() {
        MastodonClient mastodonClient = new MastodonClient(webClientBuilder, restTemplate);
        Map<String, Object> responseMap = Map.of("id", "status-id-123");

        when(webClientBuilder.baseUrl("https://mastodon.example.com")).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(any(String.class), any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseMap));

        String result = mastodonClient.postStatus("https://mastodon.example.com", "my-token", "Hello Mastodon");

        assertEquals("status-id-123", result);
    }

    @Test
    public void getLikes_validInput_success() {
        MastodonClient mastodonClient = new MastodonClient(webClientBuilder, restTemplate);
        Map<String, Object> responseMap = Map.of("favourites_count", 42);

        when(webClientBuilder.baseUrl("https://mastodon.example.com")).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.header(any(String.class), any(String.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(responseMap));

        Integer likes = mastodonClient.getLikes("https://mastodon.example.com", "my-token", "status-id-123");

        assertEquals(42, likes);
    }

    @Test
    public void fetchMastodonProfileUrl_validInput_success() {
        MastodonClient mastodonClient = new MastodonClient(webClientBuilder, restTemplate);
        Map<String, Object> responseBody = Map.of("url", "https://mastodon.example.com/@user");
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(responseBody);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        String profileUrl = mastodonClient.fetchMastodonProfileUrl("https://mastodon.example.com", "my-token");

        assertEquals("https://mastodon.example.com/@user", profileUrl);
    }
}
