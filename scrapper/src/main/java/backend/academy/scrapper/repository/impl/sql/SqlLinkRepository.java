package backend.academy.scrapper.repository.impl.sql;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "persistence.type", havingValue = "jdbc")
@RequiredArgsConstructor
public class SqlLinkRepository implements LinkRepository {

    private final JdbcClient jdbcClient;

    private final RowMapper<Link> linkRowMapper = (rs, rowNum) -> new Link(
            rs.getLong("id"),
            rs.getString("url"),
            rs.getTimestamp("last_updated").toInstant(),
            rs.getTimestamp("last_checked").toInstant());

    @Override
    public List<Link> getAllLinks() {
        return jdbcClient.sql("select * from links").query(linkRowMapper).list();
    }

    @Override
    public List<Long> getLinkChatIds(Long linkId) {
        return jdbcClient
                .sql(
                        "select chat_id from links inner join chats_links on links.id = chats_links.link_id where link_id = ?")
                .param(linkId)
                .query(Long.class)
                .list();
    }

    @Override
    public List<Link> findAllLinks(Long chatId) {
        return jdbcClient
                .sql("select * from links inner join chats_links on links.id = chats_links.link_id where chat_id = ?")
                .param(chatId)
                .query(linkRowMapper)
                .list();
    }

    @Override
    public Link findByUrl(String url) {
        return jdbcClient
                .sql("select * from links where url = ?")
                .param(url)
                .query(linkRowMapper)
                .optional()
                .orElse(null);
    }

    @Override
    public Link addLink(Link link) {
        Long id = jdbcClient
                .sql("insert into links (url, last_updated, last_checked) values (?, ?, ?) returning id")
                .param(link.url())
                .param(Timestamp.from(link.lastUpdated()))
                .param(Timestamp.from(link.lastChecked()))
                .query(Long.class)
                .single();

        link.id(id);
        return link;
    }

    @Override
    public Link deleteLink(String link) {
        return jdbcClient
                .sql("delete from links where url = ? returning id, url, last_updated, last_checked")
                .param(link)
                .query(linkRowMapper)
                .single();
    }

    @Override
    public boolean checkIfExists(Long chatId, URI link) {
        return jdbcClient
                .sql(
                        "select * from links inner join chats_links on links.id = chats_links.link_id where url = ? and chat_id = ?")
                .param(link.toString())
                .param(chatId)
                .query(linkRowMapper)
                .optional()
                .isPresent();
    }

    @Override
    public void updateLinkById(Long id, Instant newUpdatedAt, Instant newCheckedAt) {
        jdbcClient
                .sql("update links set last_updated = ?, last_checked = ? where id = ?")
                .param(Timestamp.from(newUpdatedAt))
                .param(Timestamp.from(newCheckedAt))
                .param(id)
                .update();
    }

    @Override
    public long getCount() {
        return jdbcClient.sql("select count(*) from links").query(Long.class).single();
    }

    @Override
    public List<Link> getLinkBatch(int offset, int limit) {
        return jdbcClient
                .sql("select * from links limit ? offset ?")
                .param(limit)
                .param(offset)
                .query(linkRowMapper)
                .list();
    }
}
