package backend.academy.scrapper.analyzer;

import backend.academy.scrapper.analyzer.model.Update;
import backend.academy.scrapper.client.github.GitHubClient;
import backend.academy.scrapper.dto.response.GitHubResponse;
import backend.academy.scrapper.model.Link;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubAnalyzer extends AbstractAnalyzer {

    private static final String ISSUE = "ISSUE";

    private final GitHubClient gitHubClient;

    @Override
    public List<Update> analyze(Link link, String... args) {
        String owner = args[2];
        String repository = args[3];
        Instant updatedAt = link.lastUpdated();

        List<GitHubResponse> issues = gitHubClient.getIssues(owner, repository);
        if (issues != null && !issues.isEmpty()) {
            return sendGitHubUpdates(issues, updatedAt, link, ISSUE);
        }

        return List.of();
    }

    @Override
    public String getDomain() {
        return "github.com";
    }

    private List<Update> sendGitHubUpdates(
            List<GitHubResponse> responses, Instant updatedAt, Link link, String messageType) {

        List<Update> updates = new ArrayList<>();

        for (GitHubResponse pullRequest : responses) {
            Instant createdAt = Instant.parse(pullRequest.createdAt());
            if (updatedAt.compareTo(createdAt) >= 0) {
                break;
            } else {
                try {
                    String user = pullRequest.user().login();
                    String url = link.url();
                    String title = pullRequest.title();
                    String preview = pullRequest
                            .body()
                            .substring(0, Integer.min(pullRequest.body().length(), 200));

                    String res = completeMessage(user, messageType, url, title, createdAt, preview);
                    updates.add(new Update(res, createdAt));
                } catch (Exception e) {
                    log.atWarn()
                            .setMessage("Error parsing ISSUE")
                            .addKeyValue("Url", link.url())
                            .addKeyValue("Body", pullRequest)
                            .addKeyValue("Error", e.getMessage())
                            .log();
                }
            }
        }
        return updates;
    }
}
