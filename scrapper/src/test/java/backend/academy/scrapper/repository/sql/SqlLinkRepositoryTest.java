package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.LinkRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class})
@SpringBootTest(properties = "persistence.type=jdbc")
public class SqlLinkRepositoryTest {

    @Autowired
    private LinkRepository linkRepository;

    @Test
    @Transactional
    @Rollback
    public void testLinkAdding() {
        Link link = createLink();
        Link saved = linkRepository.addLink(link);

        assertLinksEqual(link, saved);
        Assertions.assertTrue(link.id() > 0);
    }

    @Test
    @Transactional
    @Rollback
    public void testFindLink() {
        Link link = createLink();
        Link saved = linkRepository.addLink(link);
        link = linkRepository.findByUrl(saved.url());
        assertLinksEqual(saved, link);
        Assertions.assertEquals(saved.id(), link.id());
    }

    @Test
    @Transactional
    @Rollback
    public void testLinkDelete() {
        Link link = createLink();
        link = linkRepository.addLink(link);
        Assertions.assertEquals(linkRepository.findByUrl(link.url()).id(), link.id());
        Link deleted = linkRepository.deleteLink(link.url());
        Assertions.assertNull(linkRepository.findByUrl(deleted.url()));
    }

    @Test
    @Transactional
    @Rollback
    public void testLinkUpdate() {
        Link link = createLink();
        link = linkRepository.addLink(link);
        Instant newInstant = Instant.now().plus(5, ChronoUnit.MINUTES);
        linkRepository.updateLinkById(link.id(), newInstant, newInstant);
        link = linkRepository.findByUrl(link.url());
        Assertions.assertEquals(
                link.lastUpdated().truncatedTo(ChronoUnit.SECONDS), newInstant.truncatedTo(ChronoUnit.SECONDS));
        Assertions.assertEquals(
                link.lastChecked().truncatedTo(ChronoUnit.SECONDS), newInstant.truncatedTo(ChronoUnit.SECONDS));
    }

    private Link createLink() {
        Link link = new Link();
        link.url("https://stackoverflow.com/check/test");
        Instant now = Instant.now();
        link.lastUpdated(now);
        link.lastChecked(now);
        return link;
    }

    private void assertLinksEqual(Link expected, Link actual) {
        Assertions.assertEquals(expected.url(), actual.url());
        Assertions.assertEquals(
                expected.lastUpdated().truncatedTo(ChronoUnit.SECONDS),
                actual.lastUpdated().truncatedTo(ChronoUnit.SECONDS));
        Assertions.assertEquals(
                expected.lastChecked().truncatedTo(ChronoUnit.SECONDS),
                actual.lastChecked().truncatedTo(ChronoUnit.SECONDS));
    }
}
