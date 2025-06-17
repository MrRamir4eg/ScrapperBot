package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.LinkFiltersTags;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkFiltersTagsRepository;
import backend.academy.scrapper.repository.LinkRepository;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class})
@SpringBootTest(properties = "persistence.type=jdbc")
public class SqlLinkFiltersTagsRepositoryTest {

    private static final String FILTER_EX = "Filter:filter";
    private static final String TAGS_EX = "tag";

    @Autowired
    private LinkFiltersTagsRepository linkFiltersTagsRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Test
    @Transactional
    @Rollback
    public void testAddingFiltersAndTags() {
        Chat chat = new Chat(12L);
        chatRepository.addChat(chat);
        Link link = createLink();
        link = linkRepository.addLink(link);
        LinkFiltersTags filters = createLinkFiltersTags(chat.id(), link.id());
        linkFiltersTagsRepository.add(filters);
        LinkFiltersTags saved = linkFiltersTagsRepository.findByChatIdAndLinkId(chat.id(), link.id());
        assertFilterAndTagsEquals(filters, saved);
    }

    @Test
    @Transactional
    @Rollback
    public void testDeletingFiltersAndTags() {
        Chat chat = new Chat(12L);
        chatRepository.addChat(chat);
        Link link = createLink();
        link = linkRepository.addLink(link);
        LinkFiltersTags filters = createLinkFiltersTags(chat.id(), link.id());
        linkFiltersTagsRepository.add(filters);
        Assertions.assertNotNull(linkFiltersTagsRepository.findByChatIdAndLinkId(chat.id(), link.id()));
        linkFiltersTagsRepository.deleteByChatIdAndLinkId(chat.id(), link.id());
        Assertions.assertNull(linkFiltersTagsRepository.findByChatIdAndLinkId(chat.id(), link.id()));
    }

    private Link createLink() {
        Link link = new Link();
        link.url("https://stackoverflow.com/check/test");
        Instant now = Instant.now();
        link.lastUpdated(now);
        link.lastChecked(now);
        return link;
    }

    private LinkFiltersTags createLinkFiltersTags(Long chatId, Long linkId) {
        return new LinkFiltersTags(chatId, linkId, TAGS_EX, FILTER_EX);
    }

    private void assertFilterAndTagsEquals(LinkFiltersTags expected, LinkFiltersTags actual) {
        Assertions.assertEquals(expected.chatId(), actual.chatId());
        Assertions.assertEquals(expected.linkId(), actual.linkId());
        Assertions.assertEquals(expected.tags(), actual.tags());
        Assertions.assertEquals(expected.filters(), actual.filters());
    }
}
