package backend.academy.scrapper.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chats_links")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LinkFilterTagsEntity {
    @EmbeddedId
    private LinkFilterTagsEntityId id;

    private String tags;
    private String filters;

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private ChatEntity chat;

    @ManyToOne
    @MapsId("linkId")
    @JoinColumn(name = "link_id")
    private LinkEntity link;

    public List<String> tagsToList() {
        if (tags == null || tags.isEmpty() || tags.isBlank()) {
            return List.of();
        }
        return List.of(tags.split(" "));
    }

    public List<String> filtersToList() {
        if (filters == null || filters.isEmpty() || filters.isBlank()) {
            return List.of();
        }
        return List.of(filters.split(" "));
    }
}
