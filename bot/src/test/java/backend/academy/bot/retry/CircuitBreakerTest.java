package backend.academy.bot.retry;

import backend.academy.bot.TestEnvConfiguration;
import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.response.ApiErrorResponse;
import backend.academy.bot.exception.ScrapperApiException;
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
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestEnvConfiguration.class)
@WireMockTest(httpPort = 9090)
public class CircuitBreakerTest {

    @Autowired
    private ScrapperClient client;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void resetCircuitBreaker() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("scrapperCircuitBreaker");
        circuitBreaker.reset();
    }

    @Test
    @SneakyThrows
    public void testCircuitBreaker() {
        WireMock.stubFor(WireMock.post("/tg-chat/100")
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericApiErrorResponse()))));
        for (int i = 0; i < 5; i++) {
            Assertions.assertThrows(ScrapperApiException.class, () -> client.registerChat(100L));
        }
        Assertions.assertThrows(CallNotPermittedException.class, () -> client.registerChat(100L));
        WireMock.verify(5, WireMock.postRequestedFor(WireMock.urlMatching("/tg-chat/100")));
    }

    private static ApiErrorResponse getGenericApiErrorResponse() {
        return new ApiErrorResponse("Некорректные параметры запроса", "400", "Wow", "Exception", List.of("StackTest"));
    }
}
