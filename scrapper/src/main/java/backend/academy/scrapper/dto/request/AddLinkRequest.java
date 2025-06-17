package backend.academy.scrapper.dto.request;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

public record AddLinkRequest(@NotNull URI link, @NotNull List<String> tags, @NotNull List<String> filters) {}
