package backend.academy.bot.service;

import backend.academy.bot.command.enums.ChatState;
import backend.academy.bot.repository.ChatStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatStateService {

    private final ChatStateRepository chatStateRepository;

    public ChatState getChatState(Long chatId) {
        return chatStateRepository.getChatState(chatId);
    }

    public void changeChatState(Long chatId, ChatState newState) {
        chatStateRepository.changeChatState(chatId, newState);
    }

    public void addChat(Long chatId) {
        chatStateRepository.addChat(chatId);
    }
}
