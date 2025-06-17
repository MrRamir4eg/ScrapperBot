package backend.academy.bot.util;

import backend.academy.bot.command.ExecutableBotCommand;
import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.dto.response.ListLinksResponse;
import backend.academy.bot.exception.UnknownCommandException;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BotCommandUtils {

    public static String formatListCommand(ListLinksResponse resp) {
        if (resp.links().isEmpty()) {
            return BotCommandsConst.LIST_COMMAND_EMPTY;
        } else {
            StringBuilder message = new StringBuilder();
            for (LinkResponse i : resp.links()) {
                message.append("- %s%n".formatted(i.url()));
            }
            return message.toString();
        }
    }

    public static ExecutableBotCommand getCommand(String command, Map<String, ExecutableBotCommand> commandMap)
            throws UnknownCommandException {
        ExecutableBotCommand cmd = commandMap.get(command);
        if (cmd == null) {
            throw new UnknownCommandException();
        }
        return cmd;
    }
}
