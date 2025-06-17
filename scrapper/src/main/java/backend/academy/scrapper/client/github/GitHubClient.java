package backend.academy.scrapper.client.github;

import backend.academy.scrapper.dto.response.GitHubResponse;
import backend.academy.scrapper.exception.ThirdPartyServerException;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@Retry(name = "githubRetry")
public class GitHubClient {

    private final RestClient client;

    public GitHubClient(
            @Value("${app.github-token}") String githubToken,
            @Value("${api.github.url}") String githubUrl,
            @Value("${timeout.read}") int readTimeout,
            @Value("${timeout.connection}") int connectionTimeout) {
        log.info("Github API URL: {}", githubUrl);
        var factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(readTimeout);
        factory.setConnectTimeout(connectionTimeout);
        this.client = RestClient.builder()
                .baseUrl(githubUrl)
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(githubToken))
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .build();
    }

    public List<GitHubResponse> getIssues(String owner, String repo) {
        return client.get()
                .uri("/repos/{owner}/{repo}/issues?sort=created", owner, repo)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> log.atWarn()
                        .setMessage("Repository not found")
                        .addKeyValue("repository", repo)
                        .log())
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    throw new ThirdPartyServerException("Internal server at GitHub server");
                })
                .body(new ParameterizedTypeReference<List<GitHubResponse>>() {});
    }
}
