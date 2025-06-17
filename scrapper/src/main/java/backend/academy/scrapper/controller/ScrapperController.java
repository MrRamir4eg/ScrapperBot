package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.TgChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scrapper/api")
@RequiredArgsConstructor
public class ScrapperController {

    private final TgChatService chatService;
    private final LinkService linkService;

    private static final String TG_CHAT_ID_HEADER = "Tg-Chat-Id";

    @PostMapping("/tg-chat/{id}")
    public void registerChat(@PathVariable @Min(1) Long id) {
        chatService.registerChat(id);
    }

    @DeleteMapping("/tg-chat/{id}")
    public void deleteChat(@PathVariable @Min(1) Long id) {
        chatService.deleteChat(id);
    }

    @GetMapping("/links")
    public ListLinksResponse getAllLinks(@RequestHeader(name = TG_CHAT_ID_HEADER) @Min(1) Long tgChatId) {
        return linkService.getAllLinks(tgChatId);
    }

    @PostMapping("/links")
    public LinkResponse addLink(
            @RequestHeader(name = TG_CHAT_ID_HEADER) @Min(1) Long tgChatId,
            @Valid @RequestBody AddLinkRequest addLinkRequest) {
        return linkService.addLink(tgChatId, addLinkRequest);
    }

    @DeleteMapping("/links")
    public LinkResponse deleteLink(
            @RequestHeader(name = TG_CHAT_ID_HEADER) @Min(1) Long tgChatId,
            @Valid @RequestBody RemoveLinkRequest removeLinkRequest) {
        return linkService.deleteLink(tgChatId, removeLinkRequest);
    }
}
