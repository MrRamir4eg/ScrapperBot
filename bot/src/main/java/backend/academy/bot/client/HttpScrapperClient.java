package backend.academy.bot.client;

import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.dto.response.ApiErrorResponse;
import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.dto.response.ListLinksResponse;
import backend.academy.bot.exception.ScrapperApiException;
import backend.academy.bot.exception.ScrapperServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@Retry(name = "scrapperRetry")
@CircuitBreaker(name = "scrapperCircuitBreaker")
public class HttpScrapperClient implements ScrapperClient {
    private final RestClient restClient;
    private final ObjectMapper mapper;

    public HttpScrapperClient(
            ObjectMapper mapper,
            @Value("${api.scrapper.url}") String url,
            @Value("${timeout.read}") int readTimeout,
            @Value("${timeout.connection}") int connectionTimeout) {
        log.atInfo().setMessage("Base URL").addKeyValue("baseUrl", url).log();
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
    public void registerChat(Long chatId) {
        restClient
                .post()
                .uri("/tg-chat/{id}", chatId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    log.info("Scrapper client error");
                    throwScrapperApiException(res);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.warn("Scrapper server error caught");
                    throw new ScrapperServerException();
                })
                .toBodilessEntity();
    }

    @Override
    public void deleteChat(Long chatId) {
        restClient
                .delete()
                .uri("/tg-chat/{id}", chatId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throwScrapperApiException(res);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ScrapperServerException();
                })
                .toBodilessEntity();
    }

    @Cacheable(value = "list", key = "#tgChatId")
    @Override
    public ListLinksResponse getAllLinks(Long tgChatId) {
        return restClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(tgChatId))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throwScrapperApiException(res);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ScrapperServerException();
                })
                .body(ListLinksResponse.class);
    }

    @CacheEvict(value = "list", key = "#tgChatId")
    @Override
    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
        return restClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(tgChatId))
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throwScrapperApiException(res);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ScrapperServerException();
                })
                .body(LinkResponse.class);
    }

    @CacheEvict(value = "list", key = "#tgChatId")
    @Override
    public LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request) {
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(tgChatId))
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    throwScrapperApiException(res);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ScrapperServerException();
                })
                .body(LinkResponse.class);
    }

    @SneakyThrows
    private void throwScrapperApiException(ClientHttpResponse res) {
        throw new ScrapperApiException(mapper.readValue(
                StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8), ApiErrorResponse.class));
    }
}
