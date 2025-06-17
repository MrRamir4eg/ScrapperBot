package backend.academy.scrapper.service;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.exception.ObjectAlreadyExistsException;
import backend.academy.scrapper.exception.UnsupportedLinkException;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class LinkServiceTest {

    @Autowired
    private LinkService linkService;

    @Autowired
    private TgChatService chatService;

    @Autowired
    private LinkFiltersTagsService filtersTagsService;

    @Test
    public void testAddLink_whenAddingDuplicate_shouldThrowException() {
        chatService.registerChat(1L);

        AddLinkRequest request = new AddLinkRequest(URI.create("https://github.com/Me/Test"), List.of(), List.of());

        linkService.addLink(1L, request);
        Assertions.assertThrows(ObjectAlreadyExistsException.class, () -> linkService.addLink(1L, request));
    }

    @Test
    public void testAddLink_whenAddingUnsupportedLink_shouldThrowException() {
        chatService.registerChat(1L);

        AddLinkRequest request = new AddLinkRequest(URI.create("https://oh.no"), List.of("Tag", "ME"), List.of());

        Assertions.assertThrows(UnsupportedLinkException.class, () -> linkService.addLink(1L, request));
    }

    @Test
    public void testAddLinkWithDeleteLink() {
        chatService.registerChat(3L);

        AddLinkRequest request1 = new AddLinkRequest(
                URI.create("https://github.com/Test/Remembrance"), List.of("Me"), List.of("filter1:value1"));

        AddLinkRequest request2 = new AddLinkRequest(
                URI.create("https://stackoverflow.com/questions/24793069/what-does-do-in-bash"), List.of(), List.of());

        LinkResponse res1 = linkService.addLink(3L, request1);
        LinkResponse res2 = linkService.addLink(3L, request2);

        checkLink(request1, res1);
        checkLink(request2, res2);

        linkService.deleteLink(3L, new RemoveLinkRequest(URI.create("https://github.com/Test/Remembrance")));
        linkService.deleteLink(
                3L,
                new RemoveLinkRequest(URI.create("https://stackoverflow.com/questions/24793069/what-does-do-in-bash")));
        Assertions.assertEquals(linkService.getAllLinks(3L).size(), 0);
    }

    @Test
    public void testSchedulerGettingValidChatIds() {
        chatService.registerChat(10L);
        chatService.registerChat(20L);

        AddLinkRequest request1 = new AddLinkRequest(
                URI.create("https://github.com/Test1/Remembrance"), List.of("Me"), List.of("filter1:value1"));

        LinkResponse resp = linkService.addLink(10L, request1);
        System.out.println(linkService.getAllLinks(10L));
        System.out.println(linkService.getAllLinks(20L));
        System.out.println(filtersTagsService.getByChatIdAndLinkId(10L, 1L));
        linkService.addLink(20L, request1);

        System.out.println(filtersTagsService.getByChatIdAndLinkId(10L, resp.id()));

        Assertions.assertEquals(linkService.getLinkChatIds(resp.id()), List.of(10L, 20L));
        chatService.deleteChat(10L);
        Assertions.assertEquals(linkService.getLinkChatIds(resp.id()), List.of(20L));
    }

    private static void checkLink(AddLinkRequest req, LinkResponse resp) {
        Assertions.assertNotNull(resp.id());
        Assertions.assertEquals(req.link(), resp.url());
        Assertions.assertEquals(req.tags(), resp.tags());
        Assertions.assertEquals(req.filters(), resp.filters());
    }
}
