package backend.academy.scrapper.analyzer;

import backend.academy.scrapper.analyzer.model.Update;
import backend.academy.scrapper.client.stackoverflow.StackOverflowClient;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.parser.LinkParser;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.time.Instant;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(classes = {StackOverflowAnalyzer.class, StackOverflowClient.class, ObjectMapper.class})
@TestPropertySource(
        properties = {"api.stack-overflow.url=http://localhost:9090", "timeout.read=10000", "timeout.connection=10000"})
@WireMockTest(httpPort = 9090)
public class StackOverflowAnalyzerTest {

    private final LinkParser parser = new LinkParser();

    @Autowired
    private StackOverflowAnalyzer analyzer;

    private static String question;
    private static String answer;
    private static String comment;

    @BeforeAll
    @SneakyThrows
    public static void init() {
        question = new String(StackOverflowAnalyzerTest.class
                .getResourceAsStream("/stack-exchange-answer-sample.json")
                .readAllBytes());
        comment = new String(StackOverflowAnalyzerTest.class
                .getResourceAsStream("/stack-exchange-comment-sample.json")
                .readAllBytes());
        answer = new String(StackOverflowAnalyzerTest.class
                .getResourceAsStream("/stack-exchange-answer-sample.json")
                .readAllBytes());
    }

    @Test
    @SneakyThrows
    public void testGettingUpdates_whenQuestionExists_shouldReturnUpdates() {
        wireQuestion(question);
        wireComment(comment);
        wireAnswer(answer);

        Link link = createLink();

        List<Update> updates = analyzer.analyze(link, parser.parseLink(link.url()));

        Assertions.assertEquals(updates.size(), 2);
        for (Update update : updates) {
            Assertions.assertNotNull(update.comment());
        }
    }

    @Test
    public void testGettingUpdates_whenAnswerExists_shouldReturnUpdates() {
        wireQuestion(question);
        wireAnswer(answer);
        WireMock.stubFor(WireMock.get(
                        WireMock.urlMatching(
                                "/questions/123/comments\\?order=desc&sort=creation&site=stackoverflow&filter=!1zI\\.IAjIlZBTA1GJPCUbd"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse().withStatus(400)));

        Link link = createLink();
        List<Update> updates = analyzer.analyze(link, parser.parseLink(link.url()));

        Assertions.assertEquals(updates.size(), 1);
        Assertions.assertTrue(updates.getFirst().comment().contains("StaxMan"));
    }

    @Test
    public void testGettingUpdate_whenQuestionDoesNotExist_shouldReturnEmptyUpdates() {
        WireMock.stubFor(WireMock.get(WireMock.urlMatching(
                        "/questions/123\\?order=desc&sort=creation&site=stackoverflow&filter=!-\\)ZoK\\(1l1zxF"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse().withStatus(400)));

        Link link = createLink();
        List<Update> updates = analyzer.analyze(link, parser.parseLink(link.url()));

        Assertions.assertEquals(updates.size(), 0);
    }

    private void wireQuestion(String question) {
        WireMock.stubFor(WireMock.get(WireMock.urlMatching(
                        "/questions/123\\?order=desc&sort=creation&site=stackoverflow&filter=!-\\)ZoK\\(1l1zxF"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(question)));
    }

    private void wireComment(String comment) {
        WireMock.stubFor(WireMock.get(
                        WireMock.urlMatching(
                                "/questions/123/comments\\?order=desc&sort=creation&site=stackoverflow&filter=!1zI\\.IAjIlZBTA1GJPCUbd"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(comment)));
    }

    private void wireAnswer(String answer) {
        WireMock.stubFor(WireMock.get(
                        WireMock.urlMatching(
                                "/questions/123/answers\\?order=desc&sort=creation&site=stackoverflow&filter=!\\)qYyqP0KwU_96lzlxJ7X"))
                .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo("application/json"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(answer)));
    }

    private Link createLink() {
        return new Link(123L, "https://stackoverflow.com/123/test", Instant.MIN, Instant.MIN);
    }
}
