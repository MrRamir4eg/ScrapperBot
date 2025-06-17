package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.LinkEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "persistence.type", havingValue = "jpa")
public interface JpaLinkRepository extends JpaRepository<LinkEntity, Long> {

    Optional<LinkEntity> findByUrl(String url);

    @Query("select lft.link from LinkFilterTagsEntity lft where lft.chat.id = :chatId")
    List<LinkEntity> findAllByChatId(Long chatId);

    @Query("select lft.id.chatId from LinkFilterTagsEntity lft where lft.link.id = :linkId")
    List<Long> getAllChatIds(Long linkId);
}
