package backend.academy.scrapper.repository.impl.sql;

import backend.academy.scrapper.model.LinkFiltersTags;
import backend.academy.scrapper.repository.LinkFiltersTagsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "persistence.type", havingValue = "jdbc")
public class SqlLinkFiltersTagsRepository implements LinkFiltersTagsRepository {

    private final JdbcClient jdbcClient;

    private final RowMapper<LinkFiltersTags> linkFiltersTagsRowMapper = (rs, rowNum) -> new LinkFiltersTags(
            rs.getLong("chat_id"), rs.getLong("link_id"), rs.getString("tags"), rs.getString("filters"));

    @Override
    public LinkFiltersTags findByChatIdAndLinkId(Long chatId, Long linkId) {
        return jdbcClient
                .sql("select * from chats_links where chat_id = ? and link_id = ?")
                .param(chatId)
                .param(linkId)
                .query(linkFiltersTagsRowMapper)
                .optional()
                .orElse(null);
    }

    @Override
    public LinkFiltersTags add(LinkFiltersTags linkFiltersTags) {
        jdbcClient
                .sql("insert into chats_links (chat_id, link_id, tags, filters) values (?, ?, ?, ?)")
                .param(linkFiltersTags.chatId())
                .param(linkFiltersTags.linkId())
                .param(linkFiltersTags.tags())
                .param(linkFiltersTags.filters())
                .update();
        return linkFiltersTags;
    }

    @Override
    public void deleteByChatIdAndLinkId(Long chatId, Long linkId) {
        jdbcClient
                .sql("delete from chats_links where chat_id = ? and link_id = ?")
                .param(chatId)
                .param(linkId)
                .update();
    }

    @Override
    public void deleteByChatId(Long chatId) {
        jdbcClient
                .sql("delete from chats_links where chat_id = ?")
                .param(chatId)
                .update();
    }
}
