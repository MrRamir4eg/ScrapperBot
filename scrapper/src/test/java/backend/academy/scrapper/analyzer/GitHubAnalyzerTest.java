package backend.academy.scrapper.analyzer;

import backend.academy.scrapper.analyzer.model.Update;
import backend.academy.scrapper.client.github.GitHubClient;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.parser.LinkParser;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.Instant;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GitHubAnalyzer.class, GitHubClient.class, ObjectMapper.class})
@TestPropertySource(
        properties = {"api.github.url=http://localhost:9090", "timeout.read=10000", "timeout.connection=10000"})
@WireMockTest(httpPort = 9090)
public class GitHubAnalyzerTest {

    @Autowired
    private GitHubAnalyzer gitHubAnalyzer;

    @Value("${app.github-token}")
    private String githubToken;

    private final LinkParser linkParser = new LinkParser();

    @Test
    @SneakyThrows
    public void testGettingUpdates_whenRepoExists_shouldReturnUpdates() {
        String json = new String(
                this.getClass().getResourceAsStream("/github-sample.json").readAllBytes());

        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/repos/MrRamir4eg/ScrapperTest/issues\\?sort=created"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/vnd.github+json"))
                .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer %s".formatted(githubToken)))
                .withHeader("X-GitHub-Api-Version", WireMock.equalTo("2022-11-28"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(json)));

        Link link = createLink();
        List<Update> updates = gitHubAnalyzer.analyze(link, linkParser.parseLink(link.url()));

        Assertions.assertEquals(updates.size(), 1);
        Assertions.assertTrue(updates.getFirst().comment().contains("MrRamir4eg"));
    }

    @Test
    @SneakyThrows
    public void testGettingUpdates_whenRepoNotFound_shouldReturnEmptyUpdates() {
        WireMock.stubFor(WireMock.get(WireMock.urlMatching("/repos/MrRamir4eg/ScrapperTest/issues\\?sort=created"))
                .withQueryParam("sort", WireMock.equalTo("created"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/vnd.github+json"))
                .withHeader(HttpHeaders.AUTHORIZATION, WireMock.equalTo("Bearer %s".formatted(githubToken)))
                .withHeader("X-GitHub-Api-Version", WireMock.equalTo("2022-11-28"))
                .willReturn(WireMock.aResponse().withStatus(404)));
        Link link = createLink();
        List<Update> updates = gitHubAnalyzer.analyze(link, linkParser.parseLink(link.url()));

        Assertions.assertEquals(updates.size(), 0);
    }

    private Link createLink() {
        return new Link(3L, "https://github.com/MrRamir4eg/ScrapperTest", Instant.MIN, Instant.MIN);
    }
}
