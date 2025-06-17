package backend.academy.scrapper.client;

import backend.academy.scrapper.config.KafkaConfig;
import backend.academy.scrapper.dto.request.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaBotClient implements BotClient {

    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    private final KafkaConfig kafkaConfig;

    @Override
    public void sendUpdate(LinkUpdate update) {
        kafkaTemplate.send(kafkaConfig.topicName(), update);
    }
}
