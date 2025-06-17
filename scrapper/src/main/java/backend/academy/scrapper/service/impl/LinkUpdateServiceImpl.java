package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.analyzer.Analyzer;
import backend.academy.scrapper.analyzer.model.Update;
import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.dto.request.LinkUpdate;
import backend.academy.scrapper.exception.BotApiException;
import backend.academy.scrapper.exception.InternalServerException;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.parser.LinkParser;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.LinkUpdateService;
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LinkUpdateServiceImpl implements LinkUpdateService {

    private final List<Analyzer> analyzers;
    private final LinkService linkService;
    private final LinkParser linkParser;
    private final BotClient botClient;
    private final TaskExecutor executor;
    private final int batchSize;
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> domainTimers = new HashMap<>();
    private final Map<String, Gauge> domainCounter = new HashMap<>();
    private final Map<String, AtomicInteger> count = new HashMap<>();
    private final Map<String, Analyzer> analyzerMap = new HashMap<>();

    private static final String TIMER_NAME = "%s-request-analyze-time";
    private static final String GAUGE_NAME = "%s-link-count";
    private static final int DOMAIN_POS = 1;

    @PostConstruct
    public void init() {
        for (Analyzer analyzer : analyzers) {
            analyzerMap.put(analyzer.getDomain(), analyzer);
            domainTimers.put(
                    analyzer.getDomain(),
                    Timer.builder(TIMER_NAME.formatted(analyzer.getDomain())).register(meterRegistry));
            count.put(analyzer.getDomain(), new AtomicInteger(0));
            domainCounter.put(
                    analyzer.getDomain(),
                    Gauge.builder(GAUGE_NAME.formatted(analyzer.getDomain()), count.get(analyzer.getDomain())::get)
                            .register(meterRegistry));
        }
    }

    public LinkUpdateServiceImpl(
            List<Analyzer> analyzers,
            LinkService linkService,
            BotClient botClient,
            LinkParser linkParser,
            @Qualifier("basicTaskExecutor") TaskExecutor executor,
            @Value("${persistence.batch-size}") int batchSize,
            MeterRegistry meterRegistry) {
        this.botClient = botClient;
        this.analyzers = analyzers;
        this.linkService = linkService;
        this.linkParser = linkParser;
        this.executor = executor;
        this.batchSize = batchSize;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void updateAll() {
        clear();
        long linkCount = linkService.getLinkCount();
        for (int i = 0; i <= linkCount / batchSize; i++) {
            List<Link> links = linkService.getLinkBatch(i * batchSize, batchSize);
            List<List<Link>> batches = Lists.partition(links, batchSize);
            for (List<Link> linksPart : batches) {
                executor.execute(() -> {
                    for (Link link : linksPart) {
                        log.atInfo()
                                .setMessage("Analyzing link")
                                .addKeyValue("URL", link.url())
                                .log();
                        analyzeLink(link);
                    }
                });
            }
        }
    }

    private void analyzeLink(Link link) {
        String[] linkParts = linkParser.parseLink(link.url());
        String domain = linkParts[DOMAIN_POS];
        domainTimers.get(domain).record(() -> {
            try {
                count.get(domain).incrementAndGet();
                List<Update> updates = analyzerMap.get(domain).analyze(link, linkParts);

                List<Long> ids = linkService.getLinkChatIds(link.id());
                Instant newUpdatedAt = link.lastUpdated();
                for (Update update : updates) {
                    log.atInfo()
                            .setMessage("Update will be sent")
                            .addKeyValue("chatIds", ids)
                            .addKeyValue("URL", link.url())
                            .addKeyValue("Time of update", update.updatedAt())
                            .addKeyValue("type", linkParts[DOMAIN_POS])
                            .log();
                    if (newUpdatedAt.compareTo(update.updatedAt()) < 0) {
                        newUpdatedAt = update.updatedAt();
                    }
                    botClient.sendUpdate(new LinkUpdate(link.id(), link.url(), update.comment(), ids));
                }

                if (newUpdatedAt != link.lastUpdated()) {
                    linkService.updateLinkById(link.id(), newUpdatedAt, Instant.now());
                }
            } catch (BotApiException e) {
                log.atWarn()
                        .setMessage("Error during sending updates")
                        .addKeyValue("errorMessage", e.errorResponse().exceptionMessage())
                        .log();
            } catch (InternalServerException e) {
                log.atError()
                        .setMessage("Internal server error from clients")
                        .addKeyValue("message", e.getMessage())
                        .log();
            } catch (Exception e) {
                log.atError()
                        .setMessage("Internal server error")
                        .addKeyValue("Exception", e.getClass())
                        .addKeyValue("message", e.getMessage())
                        .log();
            }
        });
    }

    private void clear() {
        count.values().forEach(el -> el.set(0));
    }
}
