package backend.academy.scrapper.repository.impl.sql;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "persistence.type", havingValue = "jdbc")
@RequiredArgsConstructor
public class SqlChatRepository implements ChatRepository {

    private final JdbcClient jdbcClient;

    private final RowMapper<Chat> chatRowMapper = (rs, rowNum) -> new Chat(rs.getLong("id"));

    @Override
    public Chat findById(Long id) {
        return jdbcClient
                .sql("select * from chats where id = ?")
                .param(id)
                .query(chatRowMapper)
                .optional()
                .orElse(null);
    }

    @Override
    public void addChat(Chat chat) {
        jdbcClient.sql("insert into chats (id) values (?)").param(chat.id()).update();
    }

    @Override
    public void deleteById(Long id) {
        jdbcClient.sql("delete from chats where id = ?").param(id).update();
    }
}
