package backend.academy.scrapper.analyzer;

import backend.academy.scrapper.analyzer.model.Update;
import backend.academy.scrapper.client.stackoverflow.StackOverflowClient;
import backend.academy.scrapper.dto.response.StackOverflowItemResponse;
import backend.academy.scrapper.dto.response.StackOverflowItemsResponse;
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
public class StackOverflowAnalyzer extends AbstractAnalyzer {

    private final StackOverflowClient stackOverflowClient;

    private static final String COMMENT = "комментарий";
    private static final String ANSWER = "ответ";

    @Override
    public List<Update> analyze(Link link, String... args) {
        String questionId = args[2];
        StackOverflowItemsResponse response = stackOverflowClient.getQuestion(questionId);
        List<Update> updates = new ArrayList<>();

        if (response != null) {
            List<StackOverflowItemResponse> questions = response.items();

            if (questions != null && !questions.isEmpty()) {
                String title = questions.getFirst().title();
                Instant updatedAt = link.lastUpdated();

                StackOverflowItemsResponse answersResponse = stackOverflowClient.getAnswers(questionId);

                if (answersResponse != null) {
                    List<StackOverflowItemResponse> answers = answersResponse.items();

                    if (answers != null && !answers.isEmpty()) {
                        updates.addAll(sendStackOverflowUpdate(answers, updatedAt, title, link, ANSWER));
                    }
                }

                StackOverflowItemsResponse commentsResponse = stackOverflowClient.getComments(questionId);
                if (commentsResponse != null) {
                    List<StackOverflowItemResponse> comments = commentsResponse.items();

                    if (comments != null && !comments.isEmpty()) {
                        updates.addAll(sendStackOverflowUpdate(comments, updatedAt, title, link, COMMENT));
                    }
                }
            }
        }

        return updates;
    }

    @Override
    public String getDomain() {
        return "stackoverflow.com";
    }

    private List<Update> sendStackOverflowUpdate(
            List<StackOverflowItemResponse> responses, Instant updatedAt, String title, Link link, String messageType) {

        List<Update> updates = new ArrayList<>();

        for (StackOverflowItemResponse item : responses) {
            Instant createdAt = Instant.ofEpochSecond(item.creationDate());
            if (updatedAt.compareTo(createdAt) >= 0) {
                break;
            } else {
                String username = item.owner().displayName();
                String preview =
                        item.body().substring(0, Integer.min(item.body().length(), 200));

                updates.add(new Update(
                        completeMessage(username, messageType, link.url(), title, createdAt, preview), createdAt));
            }
        }

        return updates;
    }
}
