package ie.shep.carwatchdog.scraping;

import ie.shep.carwatchdog.domain.SearchDefinition;
import ie.shep.carwatchdog.scraping.SourceResult;

public interface ListingSource {
    String name();
    String buildSearchUrl(SearchDefinition search, int page);
    SourceResult fetch(SearchDefinition search);
}
