package ie.shep.carwatchdog.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "seen_ads",
        uniqueConstraints = @UniqueConstraint(columnNames = {"search_id", "source", "external_id"})
)
public class SeenAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "search_id", nullable = false)
    private SearchDefinition search;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String url;

    @Column(name = "first_image_url")
    private String firstImageUrl;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt = Instant.now();

    public Long getId() {
        return id;
    }

    public SearchDefinition getSearch() {
        return search;
    }

    public void setSearch(SearchDefinition search) {
        this.search = search;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFirstImageUrl() {
        return firstImageUrl;
    }

    public void setFirstImageUrl(String firstImageUrl) {
        this.firstImageUrl = firstImageUrl;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public void setFirstSeenAt(Instant firstSeenAt) {
        this.firstSeenAt = firstSeenAt;
    }
}
