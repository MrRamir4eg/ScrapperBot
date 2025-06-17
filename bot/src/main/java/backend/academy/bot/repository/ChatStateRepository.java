package backend.academy.bot.repository;

import backend.academy.bot.command.enums.ChatState;

public interface ChatStateRepository {
    ChatState getChatState(Long chatId);

    void changeChatState(Long chatId, ChatState newState);

    void addChat(Long chatId);
}
