package backend.academy.scrapper.retry;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.client.HttpBotClient;
import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.exception.BotApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
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
public class CircuitBreakerTest {

    @Autowired
    private HttpBotClient client;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void resetCircuitBreaker() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("botCircuitBreaker");
        circuitBreaker.reset();
        this.circuitBreaker = circuitBreaker;
    }

    @Test
    @SneakyThrows
    public void testCircuitBreaker() {
        LinkUpdate update = new LinkUpdate(1L, "", "", List.of());

        WireMock.stubFor(WireMock.post("/updates")
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withBody(mapper.writeValueAsString(new ApiErrorResponse(
                                "Некорректные параметры запроса",
                                "400",
                                "Exception",
                                "Invalid data type",
                                List.of())))));

        for (int i = 0; i < circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls(); i++) {
            Assertions.assertThrows(BotApiException.class, () -> client.sendUpdate(update));
        }

        for (int i = 0; i < circuitBreaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState(); i++) {
            Assertions.assertThrows(CallNotPermittedException.class, () -> client.sendUpdate(update));
        }

        Assertions.assertSame(CircuitBreaker.State.OPEN, circuitBreaker.getState());
        WireMock.verify(5, WireMock.postRequestedFor(WireMock.urlMatching("/updates")));
    }
}
