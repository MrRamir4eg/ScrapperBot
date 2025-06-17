package backend.academy.scrapper.model;

import backend.academy.scrapper.dto.response.LinkResponse;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Link {

    private Long id;
    private String url;
    private Instant lastUpdated;
    private Instant lastChecked;

    public LinkResponse toDto(List<String> tags, List<String> filters) {
        return new LinkResponse(this.id, URI.create(url), tags, filters);
    }
}
