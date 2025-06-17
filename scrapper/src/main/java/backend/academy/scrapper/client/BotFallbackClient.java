package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.dto.request.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class BotFallbackClient implements BotClient {

    private final KafkaBotClient kafkaBotClient;
    private final HttpBotClient httpBotClient;
    private final ScrapperConfig config;

    @Override
    public void sendUpdate(LinkUpdate update) {
        try {
            switch (config.messageTransport()) {
                case Kafka -> kafkaBotClient.sendUpdate(update);
                case HTTP -> httpBotClient.sendUpdate(update);
                default -> log.error("Invalid message transport");
            }
        } catch (Exception e) {
            log.atError()
                    .setMessage("Failed to send update by chosen transport")
                    .addKeyValue("transport", config.messageTransport())
                    .log();
            fallback(update);
        }
    }

    private void fallback(LinkUpdate update) {
        try {
            switch (config.messageTransport()) {
                case Kafka -> httpBotClient.sendUpdate(update);
                case HTTP -> kafkaBotClient.sendUpdate(update);
                default -> log.error("Invalid message transport at fallback");
            }
        } catch (Exception e) {
            log.atError().setMessage("Failed to send update after fallback").log();
        }
    }
}
