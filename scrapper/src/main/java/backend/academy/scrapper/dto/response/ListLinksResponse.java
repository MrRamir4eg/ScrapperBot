package backend.academy.scrapper.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record ListLinksResponse(List<LinkResponse> links, Integer size) {}
