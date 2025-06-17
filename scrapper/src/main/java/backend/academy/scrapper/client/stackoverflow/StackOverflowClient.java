package backend.academy.scrapper.client.stackoverflow;

import backend.academy.scrapper.dto.response.StackOverflowItemsResponse;
import backend.academy.scrapper.exception.ThirdPartyServerException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@Retry(name = "stackoverflowRetry")
public class StackOverflowClient {

    private final RestClient client;
    private static final String GET_COMMENTS = "/questions/{questionId}/comments"
            + "?order=desc&sort=creation&site=stackoverflow&filter=!1zI.IAjIlZBTA1GJPCUbd";
    private static final String GET_ANSWERS = "/questions/{questionId}/answers"
            + "?order=desc&sort=creation&site=stackoverflow&filter=!)qYyqP0KwU_96lzlxJ7X";
    private static final String GET_QUESTION =
            "/questions/{questionId}" + "?order=desc&sort=creation&site=stackoverflow&filter=!-)ZoK(1l1zxF";

    public StackOverflowClient(
            @Value("${api.stack-overflow.url}") String apiUrl,
            @Value("${timeout.read}") int readTimeout,
            @Value("${timeout.connection}") int connectionTimeout) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectionTimeout);
        this.client = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    public StackOverflowItemsResponse getComments(String questionId) {
        return getItemsResponse(questionId, GET_COMMENTS);
    }

    public StackOverflowItemsResponse getAnswers(String questionId) {
        return getItemsResponse(questionId, GET_ANSWERS);
    }

    public StackOverflowItemsResponse getQuestion(String questionId) {
        return getItemsResponse(questionId, GET_QUESTION);
    }

    private StackOverflowItemsResponse getItemsResponse(String questionId, String uri) {
        return client.get()
                .uri(uri, questionId)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (req, res) -> log.warn("Question {} wasn't found", questionId))
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ThirdPartyServerException("Error getting stackoverflow items");
                })
                .body(StackOverflowItemsResponse.class);
    }
}
