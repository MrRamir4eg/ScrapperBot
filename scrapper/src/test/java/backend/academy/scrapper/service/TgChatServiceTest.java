package backend.academy.scrapper.service;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.exception.ObjectNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
public class TgChatServiceTest {

    @Autowired
    private TgChatService tgChatService;

    @Test
    public void testAddingChat() {
        tgChatService.registerChat(123L);
        Assertions.assertDoesNotThrow(() -> tgChatService.getChat(123L));
    }

    @Test
    public void testDeletingChat() {
        tgChatService.registerChat(123L);
        Assertions.assertDoesNotThrow(() -> tgChatService.deleteChat(123L));
        Assertions.assertThrows(ObjectNotFoundException.class, () -> tgChatService.getChat(123L));
    }
}
