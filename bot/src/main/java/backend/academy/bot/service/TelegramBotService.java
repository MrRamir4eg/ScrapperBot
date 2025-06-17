package backend.academy.bot.service;

import backend.academy.bot.command.ExecutableBotCommand;
import backend.academy.bot.dto.request.LinkUpdate;
import backend.academy.bot.listener.BotListener;
import backend.academy.bot.util.BotCommandsConst;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TelegramBotService {

    private final TelegramBot bot;
    private final List<ExecutableBotCommand> commands;
    private final Map<String, ExecutableBotCommand> commandMap = new HashMap<>();
    private final ChatStateService chatStateService;
    private final MeterRegistry meterRegistry;

    @PostConstruct
    public void initBot() {
        for (ExecutableBotCommand command : commands) {
            log.info("Command name: {}", command.getName());
            commandMap.put(command.getName(), command);
            BotCommandsConst.helpDescription += "%s - %s %n".formatted(command.getName(), command.getDescription());
        }

        bot.execute(new SetMyCommands(
                commands.stream().map(ExecutableBotCommand::toBotCommand).toArray(BotCommand[]::new)));

        bot.setUpdatesListener(new BotListener(chatStateService, bot, commandMap, meterRegistry), e -> log.atError()
                .setMessage("Error occurred")
                .addKeyValue("message", e.getMessage())
                .log());
    }

    public void notifyOnUpdate(LinkUpdate linkUpdate) {
        for (Long chatId : linkUpdate.tgChatIds()) bot.execute(new SendMessage(chatId, linkUpdate.description()));
    }
}
