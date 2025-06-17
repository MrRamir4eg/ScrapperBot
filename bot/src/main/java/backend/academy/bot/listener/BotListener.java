package backend.academy.bot.listener;

import backend.academy.bot.command.ExecutableBotCommand;
import backend.academy.bot.command.enums.ChatState;
import backend.academy.bot.dto.response.ApiErrorResponse;
import backend.academy.bot.exception.ScrapperApiException;
import backend.academy.bot.exception.UnknownCommandException;
import backend.academy.bot.service.ChatStateService;
import backend.academy.bot.util.BotCommandUtils;
import backend.academy.bot.util.BotCommandsConst;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BotListener implements UpdatesListener {

    private final ChatStateService chatStateService;
    private final TelegramBot bot;
    private final Map<String, ExecutableBotCommand> commandMap;
    private final Counter counter;

    public BotListener(
            ChatStateService chatStateService,
            TelegramBot bot,
            Map<String, ExecutableBotCommand> commandMap,
            MeterRegistry meterRegistry) {
        this.chatStateService = chatStateService;
        this.bot = bot;
        this.commandMap = commandMap;
        this.counter = meterRegistry.counter("bot-user-messages-count");
    }

    @SneakyThrows
    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() == null) {
                continue;
            }
            counter.increment();
            Long chatId = update.message().chat().id();
            ChatState state = chatStateService.getChatState(chatId);
            try {
                if (state == ChatState.GETTING_FILTER || state == ChatState.GETTING_TAGS) {
                    commandMap.get(BotCommandsConst.TRACK_COMMAND).execute(update, state);
                } else {
                    BotCommandUtils.getCommand(update.message().text().split(" ")[0], commandMap)
                            .execute(update, state);
                }
            } catch (UnknownCommandException e) {
                log.atWarn()
                        .setMessage("Unknown command was passed")
                        .addKeyValue("commandName", update.message().text().split(" ")[0])
                        .log();
                bot.execute(new SendMessage(chatId, BotCommandsConst.UNKNOWN_COMMAND));
            } catch (ScrapperApiException e) {
                handleError(e.error(), chatId);
            } catch (Exception e) {
                log.atError()
                        .setMessage("Internal server error occurred")
                        .addKeyValue("Error", e.getClass())
                        .addKeyValue("Message", e.getMessage())
                        .log();
                bot.execute(new SendMessage(chatId, BotCommandsConst.INTERNAL_ERROR));
                if (state != ChatState.UNREGISTERED) {
                    chatStateService.changeChatState(chatId, ChatState.SERVING);
                }
            }
        }

        return CONFIRMED_UPDATES_ALL;
    }

    private void handleError(ApiErrorResponse err, Long chatId) {
        log.atWarn()
                .setMessage("Error occurred accessing Scrapper")
                .addKeyValue("chatId", chatId)
                .addKeyValue("ApiErrorResponse", err.description())
                .log();
        bot.execute(new SendMessage(chatId, err.exceptionMessage()));
        chatStateService.changeChatState(chatId, ChatState.SERVING);
    }
}
