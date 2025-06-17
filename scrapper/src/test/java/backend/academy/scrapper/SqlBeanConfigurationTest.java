package backend.academy.scrapper;

import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkFiltersTagsRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.impl.sql.SqlChatRepository;
import backend.academy.scrapper.repository.impl.sql.SqlLinkFiltersTagsRepository;
import backend.academy.scrapper.repository.impl.sql.SqlLinkRepository;
import backend.academy.scrapper.service.LinkFiltersTagsService;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.TgChatService;
import backend.academy.scrapper.service.impl.LinkFiltersTagsServiceImpl;
import backend.academy.scrapper.service.impl.LinkServiceImpl;
import backend.academy.scrapper.service.impl.TgChatServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "persistence.type=jdbc")
public class SqlBeanConfigurationTest {

    @Autowired
    private LinkService linkService;

    @Autowired
    private TgChatService chatService;

    @Autowired
    private LinkFiltersTagsService linkFiltersTagsService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private LinkFiltersTagsRepository linkFiltersTagsRepository;

    @Test
    public void testBeanLoading_onPersistenceTypeJpa() {
        Assertions.assertInstanceOf(LinkServiceImpl.class, linkService);
        Assertions.assertInstanceOf(TgChatServiceImpl.class, chatService);
        Assertions.assertInstanceOf(LinkFiltersTagsServiceImpl.class, linkFiltersTagsService);
        Assertions.assertInstanceOf(SqlChatRepository.class, chatRepository);
        Assertions.assertInstanceOf(SqlLinkRepository.class, linkRepository);
        Assertions.assertInstanceOf(SqlLinkFiltersTagsRepository.class, linkFiltersTagsRepository);
    }
}
