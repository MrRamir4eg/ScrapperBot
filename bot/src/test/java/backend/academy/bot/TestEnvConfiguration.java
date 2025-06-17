package backend.academy.bot;

import backend.academy.bot.dto.request.LinkUpdate;
import backend.academy.bot.service.TelegramBotService;
import com.pengrad.telegrambot.TelegramBot;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

// isolated from the "scrapper" module's containers!
@Slf4j
@TestConfiguration(proxyBeanMethods = false)
public class TestEnvConfiguration {

    private static final CountDownLatch kafkaLatch = new CountDownLatch(1);

    public static CountDownLatch getKafkaLatch() {
        return kafkaLatch;
    }

    @Bean
    @Primary
    public TelegramBot testTelegramBot() {
        return Mockito.mock(TelegramBot.class);
    }

    @Bean
    @Primary
    public TelegramBotService testTelegramBotService() {
        TelegramBotService service = Mockito.mock(TelegramBotService.class);
        Mockito.doAnswer(invocation -> {
                    log.info("Service mock called");
                    kafkaLatch.countDown();
                    return null;
                })
                .when(service)
                .notifyOnUpdate(Mockito.any(LinkUpdate.class));
        return service;
    }

    @Bean
    @RestartScope
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
    }

    @Bean
    @RestartScope
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.8.1").withExposedPorts(9092);
        kafka.setPortBindings(List.of("65000:9092"));
        return kafka;
    }
}
