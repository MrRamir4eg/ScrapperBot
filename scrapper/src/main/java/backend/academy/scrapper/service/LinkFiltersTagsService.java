package backend.academy.scrapper.service;

import backend.academy.scrapper.model.LinkFiltersTags;
import java.util.List;

public interface LinkFiltersTagsService {

    LinkFiltersTags getByChatIdAndLinkId(Long chatId, Long linkId);

    LinkFiltersTags add(Long chatId, Long linkId, List<String> tags, List<String> filters);

    void deleteByChatIdAndLinkId(Long chatId, Long linkId);

    void deleteByChatId(Long chatId);
}
