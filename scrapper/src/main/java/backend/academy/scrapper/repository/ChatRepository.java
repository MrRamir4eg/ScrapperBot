package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Chat;

public interface ChatRepository {
    Chat findById(Long id);

    void addChat(Chat chat);

    void deleteById(Long id);
}
