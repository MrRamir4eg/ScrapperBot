package backend.academy.scrapper.entity;

import backend.academy.scrapper.dto.response.LinkResponse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "links")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @Column(name = "last_checked", nullable = false)
    private Instant lastChecked;

    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
    List<LinkFilterTagsEntity> filterAndTags = new ArrayList<>();

    public LinkResponse toDto(List<String> tags, List<String> filters) {
        return new LinkResponse(this.id, URI.create(url), tags, filters);
    }
}
