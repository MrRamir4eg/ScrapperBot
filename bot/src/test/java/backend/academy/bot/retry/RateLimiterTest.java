package backend.academy.bot.retry;

import backend.academy.bot.TestEnvConfiguration;
import backend.academy.bot.dto.request.LinkUpdate;
import backend.academy.bot.service.TelegramBotService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
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
@Import(TestEnvConfiguration.class)
@WireMockTest(httpPort = 9090)
public class RateLimiterTest {

    @MockitoBean
    private TelegramBotService service;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @SneakyThrows
    public void testRateLimiterByIp() {
        LinkUpdate update = new LinkUpdate(123L, "https://example.com", "Desc", List.of(993L, 1000L));
        for (int i = 0; i < 100; i++) {
            restTemplate.postForEntity("/bot/api/updates", update, String.class);
        }

        Assertions.assertEquals(
                429,
                restTemplate
                        .postForEntity("/bot/api/updates", update, String.class)
                        .getStatusCode()
                        .value());
    }
}
