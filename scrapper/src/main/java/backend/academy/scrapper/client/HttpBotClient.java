package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.exception.BotApiException;
import backend.academy.scrapper.exception.BotServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@Retry(name = "botRetry")
@CircuitBreaker(name = "botCircuitBreaker")
public class HttpBotClient implements BotClient {

    private final RestClient restClient;
    private final ObjectMapper mapper;

    public HttpBotClient(
            ObjectMapper mapper,
            @Value("${api.bot.url}") String url,
            @Value("${timeout.read}") int readTimeout,
            @Value("${timeout.connection}") int connectionTimeout) {
        log.info("Base URL: {}", url);
        var factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectionTimeout);
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.mapper = mapper;
    }

    @Override
    public void sendUpdate(LinkUpdate update) {
        restClient
                .post()
                .uri("/updates")
                .body(update)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throw new BotApiException(mapper.readValue(
                            StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8), ApiErrorResponse.class));
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new BotServerException("Bot server internal error");
                })
                .toBodilessEntity();
    }
}
