package backend.academy.bot.client;

import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.request.RemoveLinkRequest;
import backend.academy.bot.dto.response.ApiErrorResponse;
import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.dto.response.ListLinksResponse;
import backend.academy.bot.exception.ScrapperApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 9090)
class ScrapperClientTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static ScrapperClient client;

    @BeforeAll
    public static void setup() {
        client = new HttpScrapperClient(mapper, "http://localhost:9090", 5000, 5000);
    }

    @Test
    void registerChatDoesNotThrowException() {
        WireMock.stubFor(
                WireMock.post("/tg-chat/100").willReturn(WireMock.aResponse().withStatus(200)));

        Assertions.assertDoesNotThrow(() -> client.registerChat(100L));
    }

    @Test
    @SneakyThrows
    void registerChatGetsApiErrorResponse_shouldThrowScrapperApiException() {
        WireMock.stubFor(WireMock.post("/tg-chat/100")
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericApiErrorResponse()))));
        Assertions.assertThrows(ScrapperApiException.class, () -> client.registerChat(100L));
    }

    @Test
    void deleteChatDoesNotThrowException() {
        WireMock.stubFor(
                WireMock.delete("/tg-chat/100").willReturn(WireMock.aResponse().withStatus(200)));
        Assertions.assertDoesNotThrow(() -> client.deleteChat(100L));
    }

    @Test
    @SneakyThrows
    void deleteChatGetsApiErrorResponse_shouldThrowScrapperApiException() {
        WireMock.stubFor(WireMock.delete("/tg-chat/100")
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericApiErrorResponse()))));

        Assertions.assertThrows(ScrapperApiException.class, () -> client.deleteChat(100L));
    }

    @Test
    @SneakyThrows
    void getAllLinksDoesNotThrowException() {
        ListLinksResponse list = new ListLinksResponse(List.of(getGenericLinkResponse()), 1);
        WireMock.stubFor(WireMock.get("/links")
                .withHeader("Tg-Chat-Id", WireMock.equalTo("100"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(list))));
        ListLinksResponse response = client.getAllLinks(100L);

        Assertions.assertEquals(list, response);
    }

    @Test
    @SneakyThrows
    void getAllLinksGetsApiErrorResponse_shouldThrowScrapperApiException() {
        WireMock.stubFor(WireMock.get("/links")
                .withHeader("Tg-Chat-Id", WireMock.equalTo("100"))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericApiErrorResponse()))));

        Assertions.assertThrows(ScrapperApiException.class, () -> client.getAllLinks(100L));
    }

    @Test
    @SneakyThrows
    void addLinkDoesNotThrowException() {
        WireMock.stubFor(WireMock.post("/links")
                .withHeader("Tg-Chat-Id", WireMock.equalTo("100"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericLinkResponse()))));

        LinkResponse resp = client.addLink(100L, getGenericAddLinkRequest());
        Assertions.assertEquals(resp, getGenericLinkResponse());
    }

    @Test
    @SneakyThrows
    void addLinkGetsApiErrorResponse_shouldThrowScrapperApiException() {
        WireMock.stubFor(WireMock.post("/links")
                .withHeader("Tg-Chat-Id", WireMock.equalTo("100"))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericApiErrorResponse()))));

        Assertions.assertThrows(ScrapperApiException.class, () -> client.addLink(100L, getGenericAddLinkRequest()));
    }

    @Test
    @SneakyThrows
    void removeLinkDoesNotThrowException() {
        WireMock.stubFor(WireMock.delete("/links")
                .withHeader("Tg-Chat-Id", WireMock.equalTo("100"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericLinkResponse()))));

        LinkResponse resp = client.removeLink(100L, new RemoveLinkRequest(URI.create("https://example.com")));
        Assertions.assertEquals(resp, getGenericLinkResponse());
    }

    @Test
    @SneakyThrows
    void removeLinkGetsApiErrorResponse_shouldThrowScrapperApiException() {
        WireMock.stubFor(WireMock.delete("/links")
                .withHeader("Tg-Chat-Id", WireMock.equalTo("100"))
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericApiErrorResponse()))));

        Assertions.assertThrows(
                ScrapperApiException.class,
                () -> client.removeLink(100L, new RemoveLinkRequest(URI.create("https://example.com"))));
    }

    private static ApiErrorResponse getGenericApiErrorResponse() {
        return new ApiErrorResponse(
                "Некорректные параметры запроса", "400", "TestException", "Exception", List.of("StackTest"));
    }

    private static LinkResponse getGenericLinkResponse() {
        return new LinkResponse(123L, URI.create("https://example.com"), List.of(), List.of());
    }

    private static AddLinkRequest getGenericAddLinkRequest() {
        return new AddLinkRequest(URI.create("https://example.com"), List.of(), List.of());
    }
}
