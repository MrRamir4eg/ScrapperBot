package backend.academy.bot.dto.request;

import java.net.URI;
import java.util.List;
import lombok.Builder;

@Builder
public record AddLinkRequest(URI link, List<String> tags, List<String> filters) {}
