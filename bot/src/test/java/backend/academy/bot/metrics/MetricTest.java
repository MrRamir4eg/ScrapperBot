package backend.academy.bot.metrics;

import backend.academy.bot.listener.BotListener;
import backend.academy.bot.service.ChatStateService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.HashMap;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class MetricTest {

    private BotListener botListener;

    private MeterRegistry meterRegistry;

    @BeforeEach
    public void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        botListener = new BotListener(
                Mockito.mock(ChatStateService.class), Mockito.mock(TelegramBot.class), new HashMap<>(), meterRegistry);
        Metrics.globalRegistry.add(meterRegistry);
    }

    @AfterEach
    public void tearDown() {
        meterRegistry.clear();
        Metrics.globalRegistry.clear();
    }

    @Test
    @SneakyThrows
    public void testMetrics() {
        Chat chat = Mockito.mock(Chat.class);
        Mockito.when(chat.id()).thenReturn(1L);
        Message message = Mockito.mock(Message.class);
        Mockito.when(message.chat()).thenReturn(chat);
        Update update = Mockito.mock(Update.class);
        Mockito.when(update.message()).thenReturn(message);
        botListener.process(List.of(update));

        Counter counter = meterRegistry.find("bot-user-messages-count").counter();
        Assertions.assertEquals(1, counter.count());
    }
}
