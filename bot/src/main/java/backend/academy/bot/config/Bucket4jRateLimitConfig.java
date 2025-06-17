package backend.academy.bot.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rate-limit")
public record Bucket4jRateLimitConfig(List<String> whitelist, int refillTokens, int capacity, int refillDuration) {}
