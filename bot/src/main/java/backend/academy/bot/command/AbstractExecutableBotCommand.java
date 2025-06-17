package backend.academy.bot.command;

import backend.academy.bot.util.BotCommandsConst;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SendMessage;

public abstract class AbstractExecutableBotCommand implements ExecutableBotCommand {

    protected final String command;
    protected final String description;

    protected AbstractExecutableBotCommand(String command, String description) {
        this.command = command;
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getName() {
        return command;
    }

    @Override
    public void rememberToRegister(Long chatId, TelegramBot bot) {
        bot.execute(new SendMessage(chatId, BotCommandsConst.REMEMBER_TO_REGISTER));
    }

    @Override
    public BotCommand toBotCommand() {
        return new BotCommand(command, description);
    }
}
