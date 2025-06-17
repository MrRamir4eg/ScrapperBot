package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.entity.LinkEntity;
import backend.academy.scrapper.entity.LinkFilterTagsEntity;
import backend.academy.scrapper.entity.LinkFilterTagsEntityId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@DataJpaTest(properties = "persistence.type=jpa")
public class JpaLinkFiltersTagsRepositoryTest {

    @Autowired
    private JpaLinkFiltersTagsRepository linkFiltersTagsRepository;

    @Autowired
    private JpaLinkRepository linkRepository;

    @Autowired
    private JpaChatRepository chatRepository;

    @Test
    public void testAddingAndGettingFiltersAndTags() {
        ChatEntity chat = createChatEntity();
        chatRepository.save(chat);
        LinkEntity link = createLinkEntity();
        link = linkRepository.save(link);
        LinkFilterTagsEntity info = createLinkFilterTagsEntity(chat, link);
        linkFiltersTagsRepository.save(info);

        Optional<LinkFilterTagsEntity> newInfo =
                linkFiltersTagsRepository.findById(new LinkFilterTagsEntityId(chat.id(), link.id()));
        Assertions.assertTrue(newInfo.isPresent());
        Assertions.assertEquals(chat.id(), newInfo.orElseThrow().chat().id());
        Assertions.assertEquals(link.id(), newInfo.orElseThrow().link().id());
    }

    @Test
    public void testRemovingLinkFiltersAndTags() {
        ChatEntity chat = createChatEntity();
        chatRepository.save(chat);
        LinkEntity link = createLinkEntity();
        link = linkRepository.save(link);

        LinkFilterTagsEntity info = createLinkFilterTagsEntity(chat, link);
        linkFiltersTagsRepository.save(info);

        linkFiltersTagsRepository.deleteById(new LinkFilterTagsEntityId(chat.id(), link.id()));

        Assertions.assertFalse(linkFiltersTagsRepository.findById(info.id()).isPresent());
    }

    private ChatEntity createChatEntity() {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.id(61L);
        return chatEntity;
    }

    private LinkEntity createLinkEntity() {
        LinkEntity linkEntity = new LinkEntity();
        linkEntity.url("https://github.com/test/test");
        linkEntity.lastChecked(Instant.now());
        linkEntity.lastUpdated(Instant.now());

        return linkEntity;
    }

    private LinkFilterTagsEntity createLinkFilterTagsEntity(ChatEntity chat, LinkEntity link) {
        LinkFilterTagsEntity info = new LinkFilterTagsEntity();
        info.id(new LinkFilterTagsEntityId(chat.id(), link.id()));
        info.tags("Test");
        info.filters("");
        info.chat(chat);
        info.link(link);

        return info;
    }
}
