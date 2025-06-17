package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.command.enums.ChatState;
import backend.academy.bot.dto.response.ListLinksResponse;
import backend.academy.bot.util.BotCommandUtils;
import backend.academy.bot.util.BotCommandsConst;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListBotCommand extends AbstractExecutableBotCommand {

    private final TelegramBot bot;
    private final ScrapperClient client;

    public ListBotCommand(TelegramBot bot, ScrapperClient client) {
        super(BotCommandsConst.LIST_COMMAND, BotCommandsConst.LIST_COMMAND_DESCRIPTION);
        this.bot = bot;
        this.client = client;
    }

    @Override
    public void execute(Update update, ChatState state) {
        Long chatId = update.message().chat().id();
        switch (state) {
            case UNREGISTERED -> rememberToRegister(chatId, bot);
            case SERVING -> {
                ListLinksResponse resp = client.getAllLinks(chatId);
                log.info("List all result: {}", resp);
                bot.execute(new SendMessage(chatId, BotCommandUtils.formatListCommand(resp)));
            }
        }
    }
}
