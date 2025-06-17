package backend.academy.scrapper.schedule;

import backend.academy.scrapper.service.LinkUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkScheduler {

    private final LinkUpdateService service;

    @Scheduled(fixedDelayString = "${scheduler.fixedDelay}", initialDelayString = "${scheduler.initialDelay}")
    public void update() {
        service.updateAll();
    }
}
