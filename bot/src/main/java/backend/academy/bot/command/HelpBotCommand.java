package backend.academy.bot.command;

import backend.academy.bot.command.enums.ChatState;
import backend.academy.bot.util.BotCommandsConst;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HelpBotCommand extends AbstractExecutableBotCommand {

    private final TelegramBot bot;

    public HelpBotCommand(TelegramBot bot) {
        super(BotCommandsConst.HELP_COMMAND, BotCommandsConst.HELP_COMMAND_DESCRIPTION);
        this.bot = bot;
    }

    @Override
    public void execute(Update update, ChatState state) {
        Long chatId = update.message().chat().id();
        switch (state) {
            case UNREGISTERED -> rememberToRegister(chatId, bot);
            case SERVING -> bot.execute(new SendMessage(chatId, BotCommandsConst.helpDescription));
        }
    }
}
