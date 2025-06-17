package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.entity.LinkFilterTagsEntity;
import backend.academy.scrapper.entity.LinkFilterTagsEntityId;
import backend.academy.scrapper.exception.ObjectNotFoundException;
import backend.academy.scrapper.model.LinkFiltersTags;
import backend.academy.scrapper.repository.jpa.JpaChatRepository;
import backend.academy.scrapper.repository.jpa.JpaLinkFiltersTagsRepository;
import backend.academy.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.scrapper.service.LinkFiltersTagsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "persistence.type", havingValue = "jpa")
public class JpaLinkFiltersTagsService implements LinkFiltersTagsService {

    private final JpaLinkFiltersTagsRepository linkFiltersTagsRepository;
    private final JpaLinkRepository linkRepository;
    private final JpaChatRepository chatRepository;

    @Override
    public LinkFiltersTags getByChatIdAndLinkId(Long chatId, Long linkId) {
        return linkFiltersTagsRepository
                .findById(new LinkFilterTagsEntityId(chatId, linkId))
                .map(el -> new LinkFiltersTags(chatId, linkId, el.tags(), el.filters()))
                .orElse(null);
    }

    @Override
    public LinkFiltersTags add(Long chatId, Long linkId, List<String> tags, List<String> filters) {
        LinkFilterTagsEntity entity = new LinkFilterTagsEntity();
        entity.id(new LinkFilterTagsEntityId(chatId, linkId));
        entity.tags(String.join(" ", tags));
        entity.filters(String.join(" ", filters));
        entity.chat(chatRepository.findById(chatId).orElseThrow(() -> {
            log.atWarn()
                    .setMessage("Chat was not found")
                    .addKeyValue("linkId", linkId)
                    .addKeyValue("chatId", chatId)
                    .log();
            return new ObjectNotFoundException("Не найден чат для добавления");
        }));
        entity.link(
                linkRepository.findById(linkId).orElseThrow(() -> new ObjectNotFoundException("Ccылка не найдена")));
        linkFiltersTagsRepository.save(entity);

        return new LinkFiltersTags(chatId, linkId, entity.tags(), entity.filters());
    }

    @Override
    public void deleteByChatIdAndLinkId(Long chatId, Long linkId) {
        linkFiltersTagsRepository.deleteById(new LinkFilterTagsEntityId(chatId, linkId));
    }

    @Override
    public void deleteByChatId(Long chatId) {
        linkFiltersTagsRepository.deleteAllByChatId(chatId);
    }
}
