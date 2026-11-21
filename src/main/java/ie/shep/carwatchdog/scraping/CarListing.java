package ie.shep.carwatchdog.scraping;

public record CarListing(
        String source,
        String externalId,
        String url,
        String firstImageUrl
) {
}
