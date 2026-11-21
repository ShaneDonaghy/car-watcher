package ie.shep.carwatchdog.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "search_definitions")
public class SearchDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String make;
    private String model;
    private String fuel;
    private String engineSize;

    private Integer minAgeYears;
    private Integer maxAgeYears;

    @Column(nullable = false)
    private String notifyEmail;

    @Column(nullable = false, unique = true)
    private String searchKey;

    @Column(nullable = false)
    private boolean initialised;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant lastPolledAt;

    @OneToMany(mappedBy = "search", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeenAd> ads = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public String getEngineSize() {
        return engineSize;
    }

    public void setEngineSize(String engineSize) {
        this.engineSize = engineSize;
    }

    public Integer getMinAgeYears() {
        return minAgeYears;
    }

    public void setMinAgeYears(Integer minAgeYears) {
        this.minAgeYears = minAgeYears;
    }

    public Integer getMaxAgeYears() {
        return maxAgeYears;
    }

    public void setMaxAgeYears(Integer maxAgeYears) {
        this.maxAgeYears = maxAgeYears;
    }

    public String getNotifyEmail() {
        return notifyEmail;
    }

    public void setNotifyEmail(String notifyEmail) {
        this.notifyEmail = notifyEmail;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public boolean isInitialised() {
        return initialised;
    }

    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastPolledAt() {
        return lastPolledAt;
    }

    public void setLastPolledAt(Instant lastPolledAt) {
        this.lastPolledAt = lastPolledAt;
    }

    public List<SeenAd> getAds() {
        return ads;
    }

    public void setAds(List<SeenAd> ads) {
        this.ads = ads;
    }
}
