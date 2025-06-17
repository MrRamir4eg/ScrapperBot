package backend.academy.bot.command;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.command.enums.ChatState;
import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.service.ChatStateService;
import backend.academy.bot.util.BotCommandsConst;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrackBotCommand extends AbstractExecutableBotCommand {

    private final TelegramBot bot;
    private final ScrapperClient client;
    private final ChatStateService chatStateService;

    private static final Map<Long, AddLinkRequest.AddLinkRequestBuilder> LOCAL_DB = new ConcurrentHashMap<>();
    private static final String NO = "нет";
    private static final Pattern FILTER_PATTERN = Pattern.compile("\\S+:\\S+");

    public TrackBotCommand(TelegramBot bot, ScrapperClient client, ChatStateService chatStateService) {
        super(BotCommandsConst.TRACK_COMMAND, BotCommandsConst.TRACK_COMMAND_DESCRIPTION);
        this.bot = bot;
        this.client = client;
        this.chatStateService = chatStateService;
    }

    @Override
    public void execute(Update update, ChatState state) {
        Long id = update.message().chat().id();
        String message = update.message().text();
        String[] messageParts = message.split(" ");
        switch (state) {
            case ChatState.UNREGISTERED -> rememberToRegister(id, bot);
            case ChatState.SERVING -> {
                if (messageParts.length == 1) {
                    bot.execute(new SendMessage(id, BotCommandsConst.UNKNOWN_COMMAND));
                } else {
                    try {
                        URI uri = new URL(messageParts[1]).toURI();
                        LOCAL_DB.put(id, AddLinkRequest.builder().link(uri));
                        chatStateService.changeChatState(id, ChatState.GETTING_TAGS);
                        log.info("Chat state: {}", chatStateService.getChatState(id));
                        bot.execute(new SendMessage(id, BotCommandsConst.TRACK_COMMAND_TAGS));
                    } catch (MalformedURLException | URISyntaxException e) {
                        bot.execute(new SendMessage(id, BotCommandsConst.UNTRACK_COMMAND_WRONG_URI));
                    }
                }
            }
            case ChatState.GETTING_TAGS -> {
                LOCAL_DB.put(id, LOCAL_DB.get(id).tags(new ArrayList<>()));
                if (!message.equalsIgnoreCase(NO)) {
                    LOCAL_DB.put(
                            id,
                            LOCAL_DB.get(id).tags(Arrays.stream(messageParts).toList()));
                }
                chatStateService.changeChatState(id, ChatState.GETTING_FILTER);
                bot.execute(new SendMessage(id, BotCommandsConst.TRACK_COMMAND_FILTERS));
            }
            case ChatState.GETTING_FILTER -> {
                LOCAL_DB.put(id, LOCAL_DB.get(id).filters(new ArrayList<>()));
                if (!message.equalsIgnoreCase(NO)) {
                    if (checkFilters(messageParts)) {
                        LOCAL_DB.put(
                                id,
                                LOCAL_DB.get(id)
                                        .filters(Arrays.stream(messageParts).toList()));
                    } else {
                        bot.execute(new SendMessage(id, BotCommandsConst.TRACK_COMMAND_WRONG_FILTERS));
                    }
                }

                LinkResponse link = client.addLink(id, LOCAL_DB.get(id).build());
                if (link != null) {
                    chatStateService.changeChatState(id, ChatState.SERVING);
                    bot.execute(new SendMessage(id, BotCommandsConst.TRACK_COMMAND_SUCCESS));
                    LOCAL_DB.remove(id);
                    log.atInfo()
                            .setMessage("Link is tracked")
                            .addKeyValue("chatId", id)
                            .addKeyValue("URI", link.url().toString())
                            .log();
                }
            }
        }
    }

    private boolean checkFilters(String[] messageParts) {
        for (String s : messageParts) {
            if (!FILTER_PATTERN.matcher(s).matches()) {
                return false;
            }
        }
        return true;
    }
}
