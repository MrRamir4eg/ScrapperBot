package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.entity.ChatEntity;
import backend.academy.scrapper.exception.ObjectNotFoundException;
import backend.academy.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.scrapper.service.TgChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "persistence.type", havingValue = "jpa")
@RequiredArgsConstructor
public class JpaChatService implements TgChatService {

    private final JpaChatRepository chatRepository;

    @Override
    public void registerChat(Long id) {
        if (!chatRepository.existsById(id)) {
            ChatEntity chatEntity = new ChatEntity();
            chatEntity.id(id);
            chatRepository.save(chatEntity);
        }
    }

    @Override
    public void deleteChat(Long id) {
        if (!chatRepository.existsById(id)) {
            log.atWarn()
                    .setMessage("Trying to delete a chat that does not exist")
                    .addKeyValue("chatId", id)
                    .log();
            throw new ObjectNotFoundException("Чат не существует");
        }
        chatRepository.deleteById(id);
    }

    @Override
    public Long getChat(Long id) {
        return chatRepository
                .findById(id)
                .orElseThrow(() -> {
                    log.atWarn()
                            .setMessage("Chat was not found")
                            .addKeyValue("chatId", id)
                            .log();
                    return new ObjectNotFoundException("Чат не найден");
                })
                .id();
    }
}
