package backend.academy.scrapper.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LinkFiltersTags {

    private Long chatId;
    private Long linkId;
    private String tags;
    private String filters;

    public List<String> getTags() {
        if (tags == null || tags.isEmpty() || tags.isBlank()) {
            return List.of();
        }
        return List.of(tags.split(" "));
    }

    public List<String> getFilters() {
        if (filters == null || filters.isEmpty() || filters.isBlank()) {
            return List.of();
        }
        return List.of(filters.split(" "));
    }
}
