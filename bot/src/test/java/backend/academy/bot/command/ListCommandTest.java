package backend.academy.bot.command;

import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.dto.response.ListLinksResponse;
import backend.academy.bot.util.BotCommandUtils;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ListCommandTest {

    @Test
    public void checkListCommandFormatting() {
        URI url = URI.create("https://github.com/MrRamir4eg/ScrapperTest");
        ListLinksResponse links = new ListLinksResponse(List.of(new LinkResponse(1L, url, List.of(), List.of())), 1);

        Assertions.assertEquals(BotCommandUtils.formatListCommand(links), "- %s%n".formatted(url));
    }
}
