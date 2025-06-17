package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.model.LinkFiltersTags;
import backend.academy.scrapper.repository.LinkFiltersTagsRepository;
import backend.academy.scrapper.service.LinkFiltersTagsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "persistence.type", havingValue = "jdbc")
public class LinkFiltersTagsServiceImpl implements LinkFiltersTagsService {

    private final LinkFiltersTagsRepository linkFiltersTagsRepository;

    @Override
    public LinkFiltersTags getByChatIdAndLinkId(Long chatId, Long linkId) {
        log.atInfo()
                .setMessage("Getting filters and tags by chatId and linkId")
                .addKeyValue("chatId", chatId)
                .addKeyValue("linkId", linkId)
                .log();
        return linkFiltersTagsRepository.findByChatIdAndLinkId(chatId, linkId);
    }

    @Override
    public LinkFiltersTags add(Long chatId, Long linkId, List<String> tags, List<String> filters) {
        log.atInfo()
                .setMessage("Adding filters and tags by chatId and linkId")
                .addKeyValue("chatId", chatId)
                .addKeyValue("linkId", linkId)
                .log();
        return linkFiltersTagsRepository.add(
                new LinkFiltersTags(chatId, linkId, String.join(" ", tags), String.join(" ", filters)));
    }

    @Override
    public void deleteByChatIdAndLinkId(Long chatId, Long linkId) {
        log.atInfo()
                .setMessage("Deleting filters and tags by chatId and linkId")
                .addKeyValue("chatId", chatId)
                .addKeyValue("linkId", linkId)
                .log();
        linkFiltersTagsRepository.deleteByChatIdAndLinkId(chatId, linkId);
    }

    @Override
    public void deleteByChatId(Long chatId) {
        linkFiltersTagsRepository.deleteByChatId(chatId);
    }
}
