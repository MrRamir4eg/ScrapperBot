package backend.academy.bot.repository.impl;

import backend.academy.bot.command.enums.ChatState;
import backend.academy.bot.repository.ChatStateRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class BasicChatStateRepository implements ChatStateRepository {

    private static final Map<Long, ChatState> DB = new ConcurrentHashMap<>();

    @Override
    public ChatState getChatState(Long chatId) {
        log.atInfo().setMessage("Get chat state").addKeyValue("chatId", chatId).log();
        if (DB.containsKey(chatId)) {
            return DB.get(chatId);
        }
        return ChatState.UNREGISTERED;
    }

    @Override
    public void changeChatState(Long chatId, ChatState newState) {
        log.atInfo()
                .setMessage("Chat's state changed")
                .addKeyValue("chatId", chatId)
                .addKeyValue("chatState", newState)
                .log();
        DB.put(chatId, newState);
    }

    @Override
    public void addChat(Long chatId) {
        log.atInfo()
                .setMessage("New chat is tracked")
                .addKeyValue("chatId", chatId)
                .log();
        DB.put(chatId, ChatState.SERVING);
    }
}
