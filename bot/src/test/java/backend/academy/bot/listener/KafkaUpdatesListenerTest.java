package backend.academy.bot.listener;

import backend.academy.bot.TestEnvConfiguration;
import backend.academy.bot.dto.request.LinkUpdate;
import backend.academy.bot.service.TelegramBotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@Import({TestEnvConfiguration.class})
public class KafkaUpdatesListenerTest {
    @Autowired
    private TelegramBotService service;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private KafkaProperties properties;

    @Test
    @Order(1)
    @SneakyThrows
    public void testKafkaUpdatesListener_whenGivenSuccessfulMessage_shouldNotifyUser() {
        log.info("The service {}", service.toString());
        LinkUpdate update = createGenericUpdate();
        String upd = mapper.writeValueAsString(update);
        kafkaTemplate.send("updates", upd).get();
        log.info("Waiting");
        boolean processed = TestEnvConfiguration.getKafkaLatch().await(10, TimeUnit.SECONDS);

        Assertions.assertTrue(processed);
    }

    @Test
    @Order(2)
    @SneakyThrows
    public void testKafkaUpdatesListener_whenGivenWrongDto_shouldRerouteToDLQ() {
        Assertions.assertDoesNotThrow(() -> {
            Mockito.doThrow(RuntimeException.class).when(service).notifyOnUpdate(createGenericUpdate());
            kafkaTemplate.send("updates", mapper.writeValueAsString(createGenericUpdate()));
        });
    }

    @Test
    @SneakyThrows
    public void testObjectMapper_whenLinkUpdateJsonGiven_shouldParse() {
        LinkUpdate upd = createGenericUpdate();
        String json = mapper.writeValueAsString(upd);
        Assertions.assertEquals(upd, mapper.readValue(json, LinkUpdate.class));
    }

    @Test
    @SneakyThrows
    public void testObjectMapper_whenWrongJsonGiven_shouldFail() {
        String json = "Wrong one";
        Assertions.assertThrows(JsonProcessingException.class, () -> mapper.readValue(json, LinkUpdate.class));
    }

    private LinkUpdate createGenericUpdate() {
        return new LinkUpdate(1L, "https://localhost", "Hola", List.of(1L));
    }
}
