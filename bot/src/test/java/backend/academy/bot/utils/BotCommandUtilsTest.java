package backend.academy.bot.utils;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.command.ExecutableBotCommand;
import backend.academy.bot.command.HelpBotCommand;
import backend.academy.bot.command.ListBotCommand;
import backend.academy.bot.command.StartBotCommand;
import backend.academy.bot.command.TrackBotCommand;
import backend.academy.bot.command.UntrackBotCommand;
import backend.academy.bot.exception.UnknownCommandException;
import backend.academy.bot.service.ChatStateService;
import backend.academy.bot.util.BotCommandUtils;
import com.pengrad.telegrambot.TelegramBot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.Mockito;

public class BotCommandUtilsTest {

    private static TelegramBot bot;
    private static ScrapperClient client;
    private static ChatStateService chatStateService;

    @BeforeAll
    public static void init() {
        bot = Mockito.mock(TelegramBot.class);
        client = Mockito.mock(ScrapperClient.class);
        chatStateService = Mockito.mock(ChatStateService.class);
    }

    @Test
    public void testGetCommandMethod_whenUnknownCommandIsGiven_shouldThrowUnknownCommandException() {
        List<ExecutableBotCommand> commandList =
                List.of(new HelpBotCommand(bot), new TrackBotCommand(bot, client, chatStateService));

        Map<String, ExecutableBotCommand> map = createCommandMap(commandList);
        Assertions.assertThrows(UnknownCommandException.class, () -> BotCommandUtils.getCommand("/start", map));
    }

    @Test
    public void testGetCommandMethod_whenCommandIsGiven_shouldReturnCommand() {
        List<ExecutableBotCommand> commandList = List.of(
                new ListBotCommand(bot, client),
                new StartBotCommand(chatStateService, bot, client),
                new UntrackBotCommand(bot, client));

        Map<String, ExecutableBotCommand> map = createCommandMap(commandList);
        Assertions.assertDoesNotThrow(() -> BotCommandUtils.getCommand("/start", map));
    }

    private Map<String, ExecutableBotCommand> createCommandMap(List<ExecutableBotCommand> commandList) {
        Map<String, ExecutableBotCommand> map = new HashMap<>();
        for (ExecutableBotCommand command : commandList) {
            map.put(command.getName(), command);
        }
        return map;
    }
}
