package backend.academy.scrapper.retry;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.HttpBotClient;
import backend.academy.scrapper.client.KafkaBotClient;
import backend.academy.scrapper.dto.request.LinkUpdate;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@SpringBootTest(properties = "app.message-transport=Kafka")
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class BotClientFallbackTest {

    @MockitoBean
    private KafkaBotClient kafkaBotClient;

    @MockitoBean
    private HttpBotClient httpBotClient;

    @Autowired
    private BotClient facade;

    @Test
    @SneakyThrows
    public void testFallback_whenKafkaFails_shouldSwitchToHttp() {
        LinkUpdate update = new LinkUpdate(123L, "test", "", List.of());

        Mockito.doThrow(new RuntimeException()).when(kafkaBotClient).sendUpdate(update);

        facade.sendUpdate(update);

        Mockito.verify(httpBotClient, Mockito.times(1)).sendUpdate(update);
    }
}
