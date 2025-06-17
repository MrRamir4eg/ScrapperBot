package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.model.Link;
import java.time.Instant;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface LinkService {

    long getLinkCount();

    List<Link> getLinkBatch(int offset, int limit);

    ListLinksResponse getAllLinks(Long chatId);

    LinkResponse addLink(Long chatId, AddLinkRequest addLinkRequest);

    LinkResponse deleteLink(Long chatId, RemoveLinkRequest removeLinkRequest);

    List<Long> getLinkChatIds(Long linkId);

    void updateLinkById(Long id, Instant newUpdatedAt, Instant newCheckedAt);

    List<Link> getAllRawLinks();
}
