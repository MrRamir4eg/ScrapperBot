package backend.academy.bot.controller;

import backend.academy.bot.dto.request.LinkUpdate;
import backend.academy.bot.service.TelegramBotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bot/api")
public class BotController {

    private final TelegramBotService botService;

    @PostMapping("/updates")
    public LinkUpdate sendUpdate(@Valid @RequestBody LinkUpdate linkUpdate) {
        botService.notifyOnUpdate(linkUpdate);
        return linkUpdate;
    }
}
