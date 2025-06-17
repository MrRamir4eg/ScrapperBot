package backend.academy.scrapper.analyzer;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public abstract class AbstractAnalyzer implements Analyzer {

    private static final String PROTOTYPE_MESSAGE =
            "Пользователь %s добавил %s на ссылку %s%n" + "Тема: %s%nВремя создания (по UTC): %s%nПревью:%n%s";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm:ss dd.MM.yyyy").withZone(ZoneOffset.UTC);

    protected String completeMessage(
            String username, String messageType, String url, String title, Instant createdAt, String preview) {

        return PROTOTYPE_MESSAGE.formatted(
                username, messageType, url, title, DATE_FORMATTER.format(createdAt), preview);
    }
}
