package backend.academy.scrapper.dto.response;

import java.net.URI;
import java.util.List;
import lombok.Builder;

@Builder
public record LinkResponse(Long id, URI url, List<String> tags, List<String> filters) {}
