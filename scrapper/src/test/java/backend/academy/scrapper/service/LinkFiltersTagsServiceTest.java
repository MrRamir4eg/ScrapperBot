package backend.academy.scrapper.service;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.model.LinkFiltersTags;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class LinkFiltersTagsServiceTest {

    @Autowired
    private TgChatService chatService;

    @Autowired
    private LinkFiltersTagsService tagsService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private LinkFiltersTagsService linkFiltersTagsService;

    @Test
    public void testAddingFiltersAndTags() {
        chatService.registerChat(1001L);
        LinkResponse resp = linkService.addLink(
                1001L, new AddLinkRequest(URI.create("https://github.com/Mt/Dew"), List.of("Check"), List.of("This")));
        LinkFiltersTags lft = linkFiltersTagsService.getByChatIdAndLinkId(1001L, resp.id());
        Assertions.assertNotNull(lft);
        Assertions.assertEquals(lft.getTags(), resp.tags());
        Assertions.assertEquals(lft.getFilters(), resp.filters());
    }

    @Test
    public void testRemovingFiltersAndTags() {
        chatService.registerChat(1000L);
        LinkResponse resp = linkService.addLink(
                1000L, new AddLinkRequest(URI.create("https://github.com/Mt/Dew"), List.of("Check"), List.of("This")));
        chatService.deleteChat(1000L);
        Assertions.assertNull(linkFiltersTagsService.getByChatIdAndLinkId(1000L, 1000L));
    }
}
