package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.entity.ChatEntity;
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
public class JpaChatRepositoryTest {

    @Autowired
    private JpaChatRepository chatRepository;

    @Test
    public void testAddingChat() {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.id(52L);

        ChatEntity saved = chatRepository.save(chatEntity);

        Assertions.assertEquals(saved.id(), 52L);
        Assertions.assertEquals(saved.filterAndTags().size(), 0);
    }

    @Test
    public void testGettingAndDeletingChat() {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.id(1L);

        chatRepository.save(chatEntity);
        Optional<ChatEntity> saved = chatRepository.findById(1L);
        Assertions.assertTrue(saved.isPresent());
        chatRepository.deleteById(saved.orElseThrow().id());
        Assertions.assertFalse(chatRepository.findById(1L).isPresent());
    }
}
