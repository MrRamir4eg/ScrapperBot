package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.command.enums.ChatState;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.util.BotCommandsConst;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UntrackBotCommand extends AbstractExecutableBotCommand {

    private final TelegramBot bot;
    private final ScrapperClient client;

    public UntrackBotCommand(TelegramBot bot, ScrapperClient client) {
        super(BotCommandsConst.UNTRACK_COMMAND, BotCommandsConst.UNTRACK_COMMAND_DESCRIPTION);
        this.bot = bot;
        this.client = client;
    }

    @Override
    public void execute(Update update, ChatState state) {
        Long id = update.message().chat().id();
        String[] messageParts = update.message().text().split(" ");
        switch (state) {
            case ChatState.UNREGISTERED -> rememberToRegister(id, bot);
            case ChatState.SERVING -> {
                if (messageParts.length == 1) {
                    bot.execute(new SendMessage(id, BotCommandsConst.UNKNOWN_COMMAND));
                } else {
                    try {
                        URI uri = URI.create(messageParts[1]);
                        LinkResponse res = client.removeLink(id, new RemoveLinkRequest(uri));
                        if (res != null) {
                            bot.execute(new SendMessage(id, "%s больше не отслеживается".formatted(uri)));
                            log.atInfo()
                                    .setMessage("Link untracked")
                                    .addKeyValue("chatId", id)
                                    .addKeyValue("URI", uri.toString())
                                    .log();
                        }
                    } catch (IllegalArgumentException e) {
                        bot.execute(new SendMessage(id, BotCommandsConst.UNTRACK_COMMAND_WRONG_URI));
                    }
                }
            }
        }
    }
}
