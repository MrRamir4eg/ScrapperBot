package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.entity.LinkFilterTagsEntity;
import backend.academy.scrapper.entity.LinkFilterTagsEntityId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "persistence.type", havingValue = "jpa")
public interface JpaLinkFiltersTagsRepository extends JpaRepository<LinkFilterTagsEntity, LinkFilterTagsEntityId> {

    void deleteAllByChatId(Long chatId);
}
