package ie.shep.carwatchdog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "carwatch")
public record ScraperProperties(
        Poll poll,
        Scraper scraper
) {
    public record Poll(
            String interval,
            String initialDelay,
            long requestDelayMs,
            String userAgent
    ) {
    }

    public record Scraper(
            Site donedeal,
            Site usercarsni
    ) {
    }

    public record Site(
            String baseUrl,
            String listingLinkSelector,
            String adIdRegex,
            String imageSelector,
            String nextPageSelector
    ) {
        public Site {
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalArgumentException("baseUrl is required");
            }
        }

        public String getBaseUrl(){
            return this.baseUrl;
        }
    }

    public Poll getPoll() {
        return poll;
    }

    public Scraper getScraper() {
        return scraper;
    }

    public Site getDonedeal() {
        return scraper.donedeal();
    }

    public Site getUsercarsni() {
        return scraper.usercarsni();
    }

    public String getUserAgent() {
        return poll.userAgent();
    }

    public long getRequestDelayMs() {
        return poll.requestDelayMs();
    }
}
