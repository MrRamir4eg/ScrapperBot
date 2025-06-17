package backend.academy.scrapper.metrics;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.github.GitHubClient;
import backend.academy.scrapper.client.stackoverflow.StackOverflowClient;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.LinkUpdateService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.time.Instant;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public class MetricsTest {

    private static final String TIMER_NAME = "%s-request-analyze-time";
    private static final String GAUGE_NAME = "%s-link-count";

    @Autowired
    private MeterRegistry meterRegistry;

    @MockitoBean
    private LinkService linkService;

    @MockitoBean
    private GitHubClient gitHubClient;

    @MockitoBean
    private StackOverflowClient stackOverflowClient;

    @MockitoBean
    private BotClient botClient;

    @Autowired
    private LinkUpdateService service;

    @BeforeEach
    void setUp() {
        Mockito.when(linkService.getLinkCount()).thenReturn(1L);
        Mockito.when(linkService.getLinkBatch(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(List.of(new Link(1L, "https://github.com/Test/Wow", Instant.MIN, Instant.MIN)));
        Metrics.globalRegistry.add(meterRegistry);
    }

    @AfterEach
    void tearDown() {
        meterRegistry.clear();
        Metrics.globalRegistry.clear();
    }

    @Test
    @SneakyThrows
    public void testMetrics() {
        service.updateAll();
        Thread.sleep(5000);
        Timer timer = meterRegistry.find(TIMER_NAME.formatted("github.com")).timer();
        Gauge gauge = meterRegistry.find(GAUGE_NAME.formatted("github.com")).gauge();
        Assertions.assertNotNull(timer);
        Assertions.assertNotNull(gauge);
        Assertions.assertEquals(1, gauge.value());
        Assertions.assertTrue(timer.count() > 0);
    }
}
