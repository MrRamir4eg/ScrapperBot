package backend.academy.scrapper.retry;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.client.HttpBotClient;
import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.exception.BotApiException;
import backend.academy.scrapper.exception.InternalServerException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest(properties = "app.message-transport=HTTP")
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
@WireMockTest(httpPort = 9090)
public class RetryTest {

    @Autowired
    private HttpBotClient client;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private LinkUpdate update;

    @BeforeEach
    void resetCircuitBreaker() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("botCircuitBreaker");
        circuitBreaker.reset();
        update = new LinkUpdate(1L, "", "", List.of());
    }

    @Test
    public void testRateLimit_when5xx_thenShouldRetry() {
        WireMock.stubFor(WireMock.post("/updates")
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Error")));
        Assertions.assertThrows(InternalServerException.class, () -> client.sendUpdate(update));
        WireMock.verify(3, WireMock.postRequestedFor(WireMock.urlMatching("/updates")));
    }

    @Test
    @SneakyThrows
    public void testRateLimit_when4xx_thenShouldNotRetry() {
        WireMock.stubFor(WireMock.post("/updates")
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericApiErrorResponse()))));

        Assertions.assertThrows(BotApiException.class, () -> client.sendUpdate(update));

        WireMock.verify(1, WireMock.postRequestedFor(WireMock.urlMatching("/updates")));
    }

    private static ApiErrorResponse getGenericApiErrorResponse() {
        return new ApiErrorResponse(
                "Некорректные параметры запроса", "400", "TestException", "Exception", List.of("StackTest"));
    }
}
