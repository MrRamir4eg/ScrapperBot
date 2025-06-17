package backend.academy.bot.client;

import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.dto.response.ListLinksResponse;

public interface ScrapperClient {

    void registerChat(Long chatId);

    void deleteChat(Long chatId);

    ListLinksResponse getAllLinks(Long tgChatId);

    LinkResponse addLink(Long tgChatId, AddLinkRequest request);

    LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request);
}
