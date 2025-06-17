package backend.academy.bot.client;

import backend.academy.bot.TestEnvConfiguration;
import backend.academy.bot.dto.request.AddLinkRequest;
import backend.academy.bot.dto.response.LinkResponse;
import backend.academy.bot.dto.response.ListLinksResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.net.URI;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@WireMockTest(httpPort = 9090)
@Import(TestEnvConfiguration.class)
public class CacheTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ScrapperClient scrapperClient;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testCache_whenListIsDone_shouldCacheResult() {
        checkCacheExists();
    }

    @Test
    public void testCache_whenLinksUpdated_shouldEvictCache() {
        checkCacheExists();
        wireLinksPost();
        scrapperClient.addLink(100L, new AddLinkRequest(URI.create("https://google.com"), List.of(), List.of()));
        Assertions.assertFalse(cacheManager.getCacheNames().contains("list::100"));
    }

    private void checkCacheExists() {
        wireListCommand();
        scrapperClient.getAllLinks(100L);
        Cache cache =
                cacheManager.getCache(cacheManager.getCacheNames().iterator().next());
        Assertions.assertNotNull(cache);
        log.info("{}", cacheManager.getCacheNames());
    }

    @SneakyThrows
    private void wireListCommand() {
        ListLinksResponse list = new ListLinksResponse(List.of(getGenericLinkResponse()), 1);
        WireMock.stubFor(WireMock.get("/links")
                .withHeader("Tg-Chat-Id", WireMock.equalTo("100"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(list))));
    }

    @SneakyThrows
    private void wireLinksPost() {
        WireMock.stubFor(WireMock.post("/links")
                .withHeader("Tg-Chat-Id", WireMock.equalTo("100"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(getGenericLinkResponse()))));
    }

    private static LinkResponse getGenericLinkResponse() {
        return new LinkResponse(123L, URI.create("https://example.com"), List.of(), List.of());
    }
}
