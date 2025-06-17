package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.net.URI;
import java.time.Instant;
import java.util.List;

public interface LinkRepository {

    List<Link> getAllLinks();

    List<Long> getLinkChatIds(Long linkId);

    List<Link> findAllLinks(Long chatId);

    Link findByUrl(String url);

    Link addLink(Link link);

    Link deleteLink(String link);

    boolean checkIfExists(Long chatId, URI link);

    void updateLinkById(Long id, Instant newUpdatedAt, Instant newCheckedAt);

    long getCount();

    List<Link> getLinkBatch(int offset, int limit);
}
