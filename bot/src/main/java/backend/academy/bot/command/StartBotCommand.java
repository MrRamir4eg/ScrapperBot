package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.command.enums.ChatState;
import backend.academy.bot.service.ChatStateService;
import backend.academy.bot.util.BotCommandsConst;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;

@Component
public class StartBotCommand extends AbstractExecutableBotCommand {

    private final ChatStateService service;
    private final TelegramBot bot;
    private final ScrapperClient client;

    public StartBotCommand(ChatStateService service, TelegramBot bot, ScrapperClient client) {
        super(BotCommandsConst.START_COMMAND, BotCommandsConst.START_COMMAND_DESCRIPTION);
        this.service = service;
        this.bot = bot;
        this.client = client;
    }

    @Override
    public void execute(Update update, ChatState state) {
        Long chatId = update.message().chat().id();
        switch (state) {
            case UNREGISTERED -> {
                client.registerChat(chatId);
                service.addChat(chatId);
                bot.execute(new SendMessage(chatId, BotCommandsConst.REGISTER_SUCCESS));
            }
            case SERVING -> bot.execute(new SendMessage(chatId, BotCommandsConst.USER_ALREADY_REGISTERED));
        }
    }
}
