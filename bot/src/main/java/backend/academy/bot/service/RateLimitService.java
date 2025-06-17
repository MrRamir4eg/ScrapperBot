package backend.academy.bot.service;

import backend.academy.bot.config.Bucket4jRateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Bucket4jRateLimitConfig config;

    public boolean allowRequest(String ipAddress) {
        if (config.whitelist().contains(ipAddress)) {
            return true;
        }

        Bucket bucket = buckets.computeIfAbsent(ipAddress, this::newBucket);
        return bucket.tryConsume(1);
    }

    private Bucket newBucket(String ipAddress) {
        log.atInfo()
                .setMessage("Created bucket for IP")
                .addKeyValue("IP", ipAddress)
                .log();
        Bandwidth limit = Bandwidth.classic(
                config.capacity(),
                Refill.intervally(config.refillTokens(), Duration.ofSeconds(config.refillDuration())));
        return Bucket.builder().addLimit(limit).build();
    }
}
