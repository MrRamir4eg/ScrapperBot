package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.exception.BotApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@WireMockTest(httpPort = 9090)
public class HttpBotClientTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private static BotClient botClient;

    @BeforeAll
    public static void init() {
        botClient = new HttpBotClient(mapper, "http://localhost:9090", 5000, 5000);
    }

    @Test
    public void sendUpdateDoesNotThrowException() {
        WireMock.stubFor(
                WireMock.post("/updates").willReturn(WireMock.aResponse().withStatus(200)));

        Assertions.assertDoesNotThrow(() -> botClient.sendUpdate(getGenericLinkUpdate()));
    }

    @SneakyThrows
    @Test
    public void whenBotClientGetsApiErrorResponse_ShouldThrowBotApiException() {
        WireMock.stubFor(WireMock.post("/updates")
                .willReturn(WireMock.aResponse()
                        .withStatus(400)
                        .withBody(mapper.writeValueAsString(new ApiErrorResponse(
                                "Некорректные параметры запроса",
                                "400",
                                "Exception",
                                "Invalid data type",
                                List.of("Stacktrace"))))));

        Assertions.assertThrows(BotApiException.class, () -> botClient.sendUpdate(getGenericLinkUpdate()));
    }

    private static LinkUpdate getGenericLinkUpdate() {
        return new LinkUpdate(123L, "https://google.com", "Test", List.of(121L));
    }
}
