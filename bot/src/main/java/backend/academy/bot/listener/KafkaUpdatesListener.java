package backend.academy.bot.listener;

import backend.academy.bot.dto.request.LinkUpdate;
import backend.academy.bot.service.TelegramBotService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUpdatesListener {

    private final TelegramBotService service;
    private final ObjectMapper mapper;

    @RetryableTopic(
            attempts = "${kafka.retry-attempts}",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            backoff = @Backoff(delay = 3000L))
    @KafkaListener(topics = "${kafka.topic-name}", groupId = "${kafka.group-id}")
    public void listen(String updates) {
        try {
            service.notifyOnUpdate(mapper.readValue(updates, LinkUpdate.class));
        } catch (JsonProcessingException e) {
            log.atWarn()
                    .setMessage("Error parsing json")
                    .addKeyValue("json", updates)
                    .log();
        }
    }
}
