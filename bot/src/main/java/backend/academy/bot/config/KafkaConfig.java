package backend.academy.bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableKafkaRetryTopic
@ConfigurationProperties("kafka")
public record KafkaConfig(String topicName, Integer retryAttempts, String groupId) {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("retry-task-");
        scheduler.initialize();
        return scheduler;
    }
}
