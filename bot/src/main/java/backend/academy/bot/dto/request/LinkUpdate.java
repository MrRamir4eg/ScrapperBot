package backend.academy.bot.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkUpdate(
        @NotNull Long id,
        @NotNull @NotEmpty String url,
        @NotNull @NotEmpty String description,
        @NotNull List<@Min(1L) Long> tgChatIds) {}
