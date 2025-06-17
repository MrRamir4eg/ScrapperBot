package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.repository.ChatRepository;
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
public class SqlChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;

    @Test
    @Transactional
    @Rollback
    public void testAddingChat() {
        chatRepository.addChat(new Chat(12L));
        Assertions.assertEquals(chatRepository.findById(12L).id(), 12L);
    }

    @Test
    @Transactional
    @Rollback
    public void testDeleteChat() {
        chatRepository.addChat(new Chat(12L));
        Assertions.assertNotNull(chatRepository.findById(12L));
        chatRepository.deleteById(12L);
        Assertions.assertNull(chatRepository.findById(12L));
    }
}
