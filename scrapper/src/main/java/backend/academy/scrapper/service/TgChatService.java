package backend.academy.scrapper.service;

public interface TgChatService {

    void registerChat(Long id);

    void deleteChat(Long id);

    Long getChat(Long id);
}
