package backend.academy.bot;

import backend.academy.bot.command.ExecutableBotCommand;
import backend.academy.bot.service.ChatStateService;
import backend.academy.bot.service.TelegramBotService;
import com.pengrad.telegrambot.TelegramBot;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken) {

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(telegramToken);
    }

    @Bean
    public TelegramBotService telegramBotService(
            TelegramBot bot,
            List<ExecutableBotCommand> commands,
            ChatStateService service,
            MeterRegistry meterRegistry) {
        return new TelegramBotService(bot, commands, service, meterRegistry);
    }
}
