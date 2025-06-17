package backend.academy.bot.command;

import backend.academy.bot.command.enums.ChatState;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;

public interface ExecutableBotCommand {

    void execute(Update update, ChatState state);

    String getDescription();

    String getName();

    BotCommand toBotCommand();

    void rememberToRegister(Long chatId, TelegramBot bot);
}
