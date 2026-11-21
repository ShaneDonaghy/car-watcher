package ie.shep.carwatchdog.scraping;

import ie.shep.carwatchdog.config.ScraperProperties;
import ie.shep.carwatchdog.domain.SearchDefinition;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DoneDealSource implements ListingSource {

    private static final Logger log = LoggerFactory.getLogger(DoneDealSource.class);
    private static final String NAME = "donedeal";

    private final ScraperProperties properties;

    public DoneDealSource(ScraperProperties properties) {
        this.properties = properties;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String buildSearchUrl(SearchDefinition search, int page) {
        List<String> params = new ArrayList<>();

        add(params, "words", "cars");
        add(params, "make", search.getMake());
        add(params, "model", search.getModel());
        add(params, "fuel", search.getFuel());
        add(params, "engine", search.getEngineSize());

        if (search.getMinAgeYears() != null) {
            add(params, "min_year", String.valueOf(currentYear() - search.getMinAgeYears()));
        }

        if (search.getMaxAgeYears() != null) {
            add(params, "max_year", String.valueOf(currentYear() - search.getMaxAgeYears()));
        }

        if (page > 1) {
            add(params, "page", String.valueOf(page));
        }

        String query = String.join("&", params);
        return properties.getDonedeal().getBaseUrl() + "/cars?" + query;
    }

    @Override
    public SourceResult fetch(SearchDefinition search) {
        var config = properties.getDonedeal();
        List<CarListing> listings = new ArrayList<>();
        String currentUrl = buildSearchUrl(search, 1);
        int page = 1;
        int maxPages = 50;

        while (page <= maxPages) {
            Document document = get(currentUrl);
            Set<String> pageIds = new LinkedHashSet<>();

            for (Element link : document.select(config.getListingLinkSelector())) {
                String href = link.absUrl("href");
                if (href.isBlank()) {
                    continue;
                }

                Matcher matcher = Pattern.compile(config.getAdIdRegex()).matcher(href);
                if (!matcher.find()) {
                    continue;
                }

                String externalId = matcher.group(1);
                if (!pageIds.add(externalId)) {
                    continue;
                }

                String image = null;
                Element img = link.selectFirst(config.getImageSelector());
                if (img != null) {
                    image = firstNonBlank(
                            img.absUrl("src"),
                            img.absUrl("data-src"),
                            img.attr("srcset").isBlank() ? "" : img.attr("srcset").split(" ")[0]
                    );
                }

                listings.add(new CarListing(NAME, externalId, href, image));
            }

            Element next = document.selectFirst(config.getNextPageSelector());
            String nextHref = next == null ? "" : next.absUrl("href");

            if (nextHref.isBlank() || nextHref.equals(currentUrl)) {
                break;
            }

            currentUrl = nextHref;
            page++;

            sleep();
        }

        return new SourceResult(NAME, buildSearchUrl(search, 1), listings, page);
    }

    private Document get(String url) {
        try {
            return Jsoup.connect(url)
                    .userAgent(properties.getUserAgent())
                    .referrer(properties.getDonedeal().getBaseUrl())
                    .timeout(20_000)
                    .get();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to fetch DoneDeal page: " + url, e);
        }
    }

    private void add(List<String> params, String key, String value) {
        if (value != null && !value.isBlank()) {
            params.add(URLEncoder.encode(key, StandardCharsets.UTF_8)
                    + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private int currentYear() {
        return java.time.Year.now().getValue();
    }

    private void sleep() {
        try {
            Thread.sleep(properties.getRequestDelayMs());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
