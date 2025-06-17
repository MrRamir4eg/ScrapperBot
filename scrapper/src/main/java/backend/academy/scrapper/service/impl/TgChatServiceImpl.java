package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.exception.ObjectNotFoundException;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.service.LinkFiltersTagsService;
import backend.academy.scrapper.service.TgChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "persistence.type", havingValue = "jdbc")
public class TgChatServiceImpl implements TgChatService {

    private final ChatRepository chatRepository;
    private final LinkFiltersTagsService linkFiltersTagsService;

    @Override
    public void registerChat(Long id) {
        if (chatRepository.findById(id) == null) {
            chatRepository.addChat(new Chat(id));
            log.atInfo().setMessage("Chat registered").addKeyValue("chatId", id).log();
        }
    }

    @Override
    public void deleteChat(Long id) {
        Chat chat = chatRepository.findById(id);
        if (chat == null) {
            log.atWarn()
                    .setMessage("Trying to delete a chat that does not exist")
                    .addKeyValue("chatId", id)
                    .log();
            throw new ObjectNotFoundException("Чат не существует");
        }
        linkFiltersTagsService.deleteByChatId(id);
        chatRepository.deleteById(id);
        log.atInfo().setMessage("Chat deleted").addKeyValue("chatId", id).log();
    }

    @Override
    public Long getChat(Long id) {
        Chat chat = chatRepository.findById(id);
        if (chat == null) {
            log.atWarn()
                    .setMessage("Chat was not found")
                    .addKeyValue("chatId", id)
                    .log();
            throw new ObjectNotFoundException("Чат не найден");
        }
        return chat.id();
    }
}
