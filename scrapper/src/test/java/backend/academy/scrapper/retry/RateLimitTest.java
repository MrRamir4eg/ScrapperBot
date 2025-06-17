package backend.academy.scrapper.retry;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.service.TgChatService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
@WireMockTest(httpPort = 9090)
public class RateLimitTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private TgChatService service;

    @Test
    @SneakyThrows
    public void testRateLimiterByIp() {
        for (int i = 0; i < 100; i++) {
            restTemplate.postForEntity("/scrapper/api/tg-chat/100", 11L, String.class);
        }

        Assertions.assertEquals(
                429,
                restTemplate
                        .postForEntity("/bot/api/updates", 11L, String.class)
                        .getStatusCode()
                        .value());
    }
}
