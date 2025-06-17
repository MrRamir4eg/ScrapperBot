package backend.academy.scrapper;

import backend.academy.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.scrapper.repository.jpa.JpaLinkFiltersTagsRepository;
import backend.academy.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.scrapper.service.LinkFiltersTagsService;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.TgChatService;
import backend.academy.scrapper.service.jpa.JpaChatService;
import backend.academy.scrapper.service.jpa.JpaLinkFiltersTagsService;
import backend.academy.scrapper.service.jpa.JpaLinkService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "persistence.type=jpa")
public class JpaBeanConfigurationTest {

    @Autowired
    private LinkService linkService;

    @Autowired
    private TgChatService chatService;

    @Autowired
    private LinkFiltersTagsService linkFiltersTagsService;

    @Autowired
    private JpaChatRepository chatRepository;

    @Autowired
    private JpaLinkRepository linkRepository;

    @Autowired
    private JpaLinkFiltersTagsRepository linkFiltersTagsRepository;

    @Test
    public void testBeanLoading_onPersistenceTypeJpa() {
        Assertions.assertInstanceOf(JpaLinkService.class, linkService);
        Assertions.assertInstanceOf(JpaChatService.class, chatService);
        Assertions.assertInstanceOf(JpaLinkFiltersTagsService.class, linkFiltersTagsService);
    }
}
