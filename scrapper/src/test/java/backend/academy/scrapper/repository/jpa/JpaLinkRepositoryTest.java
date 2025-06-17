package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.entity.LinkEntity;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@DataJpaTest(properties = "persistence.type=jpa")
public class JpaLinkRepositoryTest {

    @Autowired
    JpaLinkRepository linkRepository;

    @Test
    public void testLinkSave() {
        LinkEntity linkEntity = createEntityWithoutId();

        LinkEntity savedLink = linkRepository.save(linkEntity);

        assertLinkParamsEquality(linkEntity, savedLink);
        Assertions.assertTrue(savedLink.id() > 0);
    }

    @Test
    public void testLinkGetAndDelete() {
        LinkEntity linkEntity = createEntityWithoutId();
        linkRepository.save(linkEntity);
        Optional<LinkEntity> savedLink = linkRepository.findById(linkEntity.id());
        Assertions.assertTrue(savedLink.isPresent());
        assertLinkParamsEquality(linkEntity, savedLink.orElseThrow());
        linkRepository.deleteById(linkEntity.id());
        Assertions.assertFalse(
                linkRepository.findById(savedLink.orElseThrow().id()).isPresent());
    }

    @Test
    public void testLinkUpdate() {
        LinkEntity linkEntity = createEntityWithoutId();
        linkEntity = linkRepository.save(linkEntity);
        Instant max = Instant.MAX;
        linkEntity.lastUpdated(max);
        linkEntity.lastChecked(max);
        LinkEntity updated = linkRepository.save(linkEntity);

        Assertions.assertEquals(linkEntity.lastUpdated(), max);
        Assertions.assertEquals(linkEntity.lastChecked(), max);
        Assertions.assertEquals(linkEntity.id(), updated.id());
        Assertions.assertEquals(linkEntity.url(), updated.url());
    }

    private LinkEntity createEntityWithoutId() {
        LinkEntity linkEntity = new LinkEntity();
        linkEntity.url("http://www.google.com");
        Instant now = Instant.now();
        linkEntity.lastUpdated(now);
        linkEntity.lastChecked(now);
        return linkEntity;
    }

    private void assertLinkParamsEquality(LinkEntity expected, LinkEntity actual) {
        Assertions.assertEquals(expected.url(), actual.url());
        Assertions.assertEquals(expected.lastUpdated(), actual.lastUpdated());
        Assertions.assertEquals(expected.lastChecked(), actual.lastChecked());
    }
}
