package backend.academy.scrapper.service.jpa;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.entity.LinkEntity;
import backend.academy.scrapper.exception.ObjectAlreadyExistsException;
import backend.academy.scrapper.exception.ObjectNotFoundException;
import backend.academy.scrapper.exception.UnsupportedLinkException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.LinkFiltersTags;
import backend.academy.scrapper.parser.LinkParser;
import backend.academy.scrapper.repository.jpa.JpaLinkRepository;
import backend.academy.scrapper.service.LinkFiltersTagsService;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "persistence.type", havingValue = "jpa")
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final LinkFiltersTagsService linkFiltersTagsService;
    private final LinkParser linkParser;

    @Override
    public long getLinkCount() {
        return linkRepository.count();
    }

    @Override
    public List<Link> getLinkBatch(int offset, int limit) {
        return linkRepository
                .findAll(PageRequest.of(offset / limit, limit))
                .map(el -> new Link(el.id(), el.url(), el.lastUpdated(), el.lastChecked()))
                .toList();
    }

    @Override
    public ListLinksResponse getAllLinks(Long chatId) {
        List<LinkEntity> linkEntities = linkRepository.findAllByChatId(chatId);

        return new ListLinksResponse(
                linkEntities.stream()
                        .map(el -> {
                            LinkFiltersTags info = linkFiltersTagsService.getByChatIdAndLinkId(chatId, el.id());
                            return new LinkResponse(el.id(), URI.create(el.url()), info.getTags(), info.getFilters());
                        })
                        .toList(),
                linkEntities.size());
    }

    @Override
    public LinkResponse addLink(Long chatId, AddLinkRequest addLinkRequest) {
        if (!linkParser.checkLink(addLinkRequest.link().toString())) {
            log.atWarn()
                    .setMessage("Unsupported link was given")
                    .addKeyValue("URL", addLinkRequest.link().toString())
                    .addKeyValue("chatId", chatId)
                    .log();
            throw new UnsupportedLinkException("Эта ссылка не поддерживается");
        }

        Optional<LinkEntity> linkOptional =
                linkRepository.findByUrl(addLinkRequest.link().toString());
        if (linkOptional.isPresent()
                && linkFiltersTagsService.getByChatIdAndLinkId(
                                chatId, linkOptional.orElseThrow().id())
                        != null) {
            log.atWarn()
                    .setMessage("Tracked link given")
                    .addKeyValue("URL", addLinkRequest.link().toString())
                    .addKeyValue("chatId", chatId)
                    .log();
            throw new ObjectAlreadyExistsException("Эта ссылка уже отслеживается");
        }

        LinkEntity linkEntity = linkOptional.orElse(new LinkEntity());
        if (linkOptional.isEmpty()) {
            linkEntity.url(addLinkRequest.link().toString());
            linkEntity.lastChecked(Instant.now());
            linkEntity.lastUpdated(Instant.now());
            linkEntity = linkRepository.save(linkEntity);
        }

        LinkFiltersTags info =
                linkFiltersTagsService.add(chatId, linkEntity.id(), addLinkRequest.tags(), addLinkRequest.filters());

        return LinkResponse.builder()
                .id(linkEntity.id())
                .url(URI.create(linkEntity.url()))
                .tags(info.getTags())
                .filters(info.getFilters())
                .build();
    }

    @Override
    public LinkResponse deleteLink(Long chatId, RemoveLinkRequest removeLinkRequest) {
        LinkEntity link = linkRepository
                .findByUrl(removeLinkRequest.link().toString())
                .orElseThrow(() -> new ObjectNotFoundException("Ссылка не найдена"));
        LinkFiltersTags info = linkFiltersTagsService.getByChatIdAndLinkId(chatId, link.id());
        linkFiltersTagsService.deleteByChatIdAndLinkId(chatId, link.id());
        return new LinkResponse(link.id(), URI.create(link.url()), info.getTags(), info.getFilters());
    }

    @Override
    public List<Long> getLinkChatIds(Long linkId) {
        return linkRepository.getAllChatIds(linkId);
    }

    @Override
    public void updateLinkById(Long id, Instant newUpdatedAt, Instant newCheckedAt) {
        LinkEntity link =
                linkRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("Ссылка не найдена"));
        link.lastChecked(newCheckedAt);
        link.lastUpdated(newUpdatedAt);
        linkRepository.save(link);
    }

    @Override
    public List<Link> getAllRawLinks() {
        return linkRepository.findAll().stream()
                .map(el -> new Link(el.id(), el.url(), el.lastUpdated(), el.lastChecked()))
                .toList();
    }
}
