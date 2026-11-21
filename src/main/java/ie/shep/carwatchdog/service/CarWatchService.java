package ie.shep.carwatchdog.service;

import ie.shep.carwatchdog.domain.SearchDefinition;
import ie.shep.carwatchdog.domain.SeenAd;
import ie.shep.carwatchdog.repository.SearchDefinitionRepository;
import ie.shep.carwatchdog.scraping.CarListing;
import ie.shep.carwatchdog.scraping.ListingSource;
import ie.shep.carwatchdog.scraping.SourceResult;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CarWatchService {

    private static final Logger log = LoggerFactory.getLogger(CarWatchService.class);

    private final SearchDefinitionRepository searches;
    private final List<ListingSource> sources;
    private final EmailNotifier emailNotifier;

    public CarWatchService(
            SearchDefinitionRepository searches,
            List<ListingSource> sources,
            EmailNotifier emailNotifier
    ) {
        this.searches = searches;
        this.sources = sources;
        this.emailNotifier = emailNotifier;
    }

    @Transactional
    public SearchDefinition createSearch(SearchDefinition request) {
        request.setSearchKey(buildSearchKey(request));
        request.setInitialised(false);
        request.setLastPolledAt(Instant.now());

        return searches.save(request);
    }

    @Scheduled(
            fixedDelayString = "${carwatch.poll.interval}",
            initialDelayString = "${carwatch.poll.initial-delay}"
    )
    public void pollAll() {
        for (SearchDefinition search : searches.findAll()) {
            try {
                poll(search);
            } catch (Exception e) {
                log.error("Poll failed for search {}: {}", search.getId(), e.getMessage(), e);
            }
        }
    }

    @Transactional
    public void poll(SearchDefinition search) {
        Map<String, CarListing> fresh = new LinkedHashMap<>();

        for (ListingSource source : sources) {
            SourceResult result = source.fetch(search);

            for (CarListing listing : result.listings()) {
                String key = source.name() + ":" + listing.externalId();
                fresh.putIfAbsent(key, listing);
            }

            log.info(
                    "Search {} source {} pages={} matches={}",
                    search.getId(),
                    source.name(),
                    result.pagesFetched(),
                    result.listings().size()
            );
        }

        Map<String, SeenAd> stored = search.getAds().stream()
                .collect(Collectors.toMap(
                        ad -> ad.getSource() + ":" + ad.getExternalId(),
                        Function.identity()
                ));

        Set<String> freshKeys = fresh.keySet();

        List<SeenAd> added = new ArrayList<>();
        List<SeenAd> removed = new ArrayList<>();

        for (Map.Entry<String, CarListing> entry : fresh.entrySet()) {
            if (!stored.containsKey(entry.getKey())) {
                CarListing listing = entry.getValue();

                SeenAd ad = new SeenAd();
                ad.setSearch(search);
                ad.setSource(listing.source());
                ad.setExternalId(listing.externalId());
                ad.setUrl(listing.url());
                ad.setFirstImageUrl(listing.firstImageUrl());

                added.add(ad);
            }
        }

        for (Map.Entry<String, SeenAd> entry : stored.entrySet()) {
            if (!freshKeys.contains(entry.getKey())) {
                removed.add(entry.getValue());
            }
        }

        if (!search.isInitialised()) {
            search.getAds().addAll(added);
            search.setInitialised(true);
            search.setLastPolledAt(Instant.now());
            searches.save(search);

            log.info(
                    "Initial run for search {}: {} matching ads. URL: {}",
                    search.getId(),
                    added.size(),
                    sources.stream()
                            .map(source -> source.buildSearchUrl(search, 1))
                            .collect(Collectors.joining(" | "))
            );

            return;
        }

        if (!added.isEmpty() || !removed.isEmpty()) {
            emailNotifier.sendDriftEmail(search, added, removed);
        }

        search.getAds().addAll(added);
        search.getAds().removeAll(removed);
        search.setLastPolledAt(Instant.now());

        searches.save(search);

        log.info(
                "Search {}: added={} removed={}",
                search.getId(),
                added.size(),
                removed.size()
        );
    }

    private String buildSearchKey(SearchDefinition search) {
        return String.join("|",
                nullToBlank(search.getMake()),
                nullToBlank(search.getModel()),
                nullToBlank(search.getFuel()),
                nullToBlank(search.getEngineSize()),
                String.valueOf(search.getMinAgeYears()),
                String.valueOf(search.getMaxAgeYears()),
                search.getNotifyEmail()
        );
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value;
    }

    @Transactional
    public boolean deleteSearch(Long id) {
        if (!searches.existsById(id)) {
            return false;
        }
        searches.deleteById(id);
        return true;
    }

    @Transactional
    public void pollById(Long id) {
        SearchDefinition search = searches.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Search not found: " + id));
        poll(search);
    }

}
