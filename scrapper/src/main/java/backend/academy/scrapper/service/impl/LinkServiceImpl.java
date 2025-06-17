package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.exception.ObjectAlreadyExistsException;
import backend.academy.scrapper.exception.ObjectNotFoundException;
import backend.academy.scrapper.exception.UnsupportedLinkException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.LinkFiltersTags;
import backend.academy.scrapper.parser.LinkParser;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.LinkFiltersTagsService;
import backend.academy.scrapper.service.LinkService;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "persistence.type", havingValue = "jdbc")
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;
    private final LinkFiltersTagsService linkFiltersTagsService;
    private final LinkParser linkParser;
    private final ChatRepository chatRepository;

    @Override
    public long getLinkCount() {
        return linkRepository.getCount();
    }

    @Override
    public List<Link> getLinkBatch(int offset, int limit) {
        return linkRepository.getLinkBatch(offset, limit);
    }

    @Override
    public ListLinksResponse getAllLinks(Long chatId) {
        log.atInfo()
                .setMessage("Get all links by chatId")
                .addKeyValue("chatId", chatId)
                .log();
        List<Link> links = linkRepository.findAllLinks(chatId);
        return ListLinksResponse.builder()
                .size(links.size())
                .links(links.stream()
                        .map(l -> {
                            LinkFiltersTags lft = linkFiltersTagsService.getByChatIdAndLinkId(chatId, l.id());
                            return l.toDto(lft.getTags(), lft.getFilters());
                        })
                        .toList())
                .build();
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

        if (chatRepository.findById(chatId) == null) {
            log.atWarn()
                    .setMessage("Chat was not found")
                    .addKeyValue("URL", addLinkRequest.link().toString())
                    .addKeyValue("chatId", chatId)
                    .log();
            throw new ObjectNotFoundException("Не найден чат для добавления");
        }

        if (linkRepository.checkIfExists(chatId, addLinkRequest.link())) {
            log.atWarn()
                    .setMessage("Tracked link given")
                    .addKeyValue("URL", addLinkRequest.link().toString())
                    .addKeyValue("chatId", chatId)
                    .log();
            throw new ObjectAlreadyExistsException("Эта ссылка уже отслеживается");
        }

        Link link = linkRepository.findByUrl(addLinkRequest.link().toString());

        log.atInfo()
                .setMessage("Added/Updated link")
                .addKeyValue("chatId", chatId)
                .addKeyValue("URL", addLinkRequest.link().toString())
                .log();

        if (link != null) {
            LinkFiltersTags info =
                    linkFiltersTagsService.add(chatId, link.id(), addLinkRequest.tags(), addLinkRequest.filters());
            return LinkResponse.builder()
                    .id(link.id())
                    .url(URI.create(link.url()))
                    .tags(info.getTags())
                    .filters(info.getFilters())
                    .build();
        } else {
            Link added = linkRepository.addLink(
                    new Link(null, addLinkRequest.link().toString(), Instant.now(), Instant.now()));

            LinkFiltersTags info =
                    linkFiltersTagsService.add(chatId, added.id(), addLinkRequest.tags(), addLinkRequest.filters());

            return LinkResponse.builder()
                    .id(added.id())
                    .url(URI.create(added.url()))
                    .tags(info.getTags())
                    .filters(info.getFilters())
                    .build();
        }
    }

    @Override
    public LinkResponse deleteLink(Long chatId, RemoveLinkRequest removeLinkRequest) {
        Link deleted = linkRepository.findByUrl(removeLinkRequest.link().toString());
        if (deleted == null) {
            log.atWarn()
                    .setMessage("Trying to delete non existent link")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("URL", removeLinkRequest.link().toString())
                    .log();
            throw new ObjectNotFoundException("Ссылка не найдена");
        }

        LinkFiltersTags info = linkFiltersTagsService.getByChatIdAndLinkId(chatId, deleted.id());
        linkFiltersTagsService.deleteByChatIdAndLinkId(chatId, deleted.id());

        log.atInfo()
                .setMessage("Deleted link")
                .addKeyValue("chatId", chatId)
                .addKeyValue("URL", removeLinkRequest.link().toString())
                .log();
        linkRepository.deleteLink(removeLinkRequest.link().toString());

        return LinkResponse.builder()
                .id(deleted.id())
                .url(URI.create(deleted.url()))
                .tags(info.getTags())
                .filters(info.getFilters())
                .build();
    }

    @Override
    public List<Long> getLinkChatIds(Long linkId) {
        return linkRepository.getLinkChatIds(linkId);
    }

    @Override
    public void updateLinkById(Long id, Instant newUpdatedAt, Instant newCheckedAt) {
        linkRepository.updateLinkById(id, newUpdatedAt, newCheckedAt);
    }

    @Override
    public List<Link> getAllRawLinks() {
        return linkRepository.getAllLinks();
    }
}
