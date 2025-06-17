package backend.academy.scrapper.dto.request;

import java.util.List;

public record LinkUpdate(Long id, String url, String description, List<Long> tgChatIds) {}
