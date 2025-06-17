package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.LinkFiltersTags;

public interface LinkFiltersTagsRepository {

    LinkFiltersTags findByChatIdAndLinkId(Long chatId, Long linkId);

    LinkFiltersTags add(LinkFiltersTags linkFiltersTags);

    void deleteByChatIdAndLinkId(Long chatId, Long linkId);

    void deleteByChatId(Long chatId);
}
