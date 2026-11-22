package ie.shep.carwatchdog.scraping;

import ie.shep.carwatchdog.scraping.CarListing;

import java.util.List;

public record SourceResult(
        String source,
        String searchUrl,
        List<CarListing> listings,
        int pagesFetched
) {}
