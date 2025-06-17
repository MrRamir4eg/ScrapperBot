package backend.academy.scrapper.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@ConfigurationProperties(prefix = "kafka")
public record KafkaConfig(String topicName) {

    @Bean
    public NewTopic updateTopic() {
        return TopicBuilder.name(topicName).build();
    }
}
